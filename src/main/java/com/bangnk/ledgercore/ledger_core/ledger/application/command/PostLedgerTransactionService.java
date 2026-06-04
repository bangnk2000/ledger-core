package com.bangnk.ledgercore.ledger_core.ledger.application.command;

import com.bangnk.ledgercore.ledger_core.audit.application.port.in.AuditCaptureUseCase;
import com.bangnk.ledgercore.ledger_core.ledger.application.PostingOutcome;
import com.bangnk.ledgercore.ledger_core.ledger.application.PostingOutcome.LedgerDomainException;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.in.LedgerUseCases.PostLedgerTransactionUseCase;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.AuditEventPublisher;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.LedgerEntryRepository;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.LedgerTransactionRepository;
import com.bangnk.ledgercore.ledger_core.ledger.config.LedgerObservabilityConfig.LedgerMetrics;
import com.bangnk.ledgercore.ledger_core.ledger.domain.model.LedgerTransaction;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.AuditTrace;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LedgerTransactionId;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PostLedgerTransactionService implements PostLedgerTransactionUseCase {

	private final LedgerTransactionRepository transactionRepository;
	private final LedgerEntryRepository entryRepository;
	private final PostingRequestHasher requestHasher;
	private final IdempotencyService idempotencyService;
	private final AuditEventPublisher auditEventPublisher;
	private final AuditCaptureUseCase auditCaptureUseCase;
	private final LedgerAuditTranslator auditTranslator;
	private final Clock clock;
	private final Counter duplicateCounter;
	private final Counter conflictCounter;
	private final Counter failedCounter;
	private final Counter acceptedCounter;
	private final Counter rejectedCounter;
	private final Timer postingTimer;

	@Autowired
	public PostLedgerTransactionService(
			LedgerTransactionRepository transactionRepository,
			LedgerEntryRepository entryRepository,
			PostingRequestHasher requestHasher,
			IdempotencyService idempotencyService,
			AuditEventPublisher auditEventPublisher,
			AuditCaptureUseCase auditCaptureUseCase,
			LedgerAuditTranslator auditTranslator,
			LedgerMetrics ledgerMetrics,
			Clock clock
	) {
		this.transactionRepository = transactionRepository;
		this.entryRepository = entryRepository;
		this.requestHasher = requestHasher;
		this.idempotencyService = idempotencyService;
		this.auditEventPublisher = auditEventPublisher;
		this.auditCaptureUseCase = auditCaptureUseCase;
		this.auditTranslator = auditTranslator;
		this.clock = clock;
		this.acceptedCounter = ledgerMetrics.postingOutcome("accepted");
		this.rejectedCounter = ledgerMetrics.postingOutcome("rejected");
		this.duplicateCounter = ledgerMetrics.postingOutcome("duplicate");
		this.conflictCounter = ledgerMetrics.postingOutcome("conflict");
		this.failedCounter = ledgerMetrics.postingOutcome("failed");
		this.postingTimer = ledgerMetrics.postingTimer();
	}

	public PostLedgerTransactionService(
			LedgerTransactionRepository transactionRepository,
			LedgerEntryRepository entryRepository,
			PostingRequestHasher requestHasher,
			IdempotencyService idempotencyService,
			AuditEventPublisher auditEventPublisher,
			LedgerMetrics ledgerMetrics,
			Clock clock
	) {
		this(
			transactionRepository,
			entryRepository,
			requestHasher,
			idempotencyService,
			auditEventPublisher,
			command -> new AuditCaptureUseCase.CaptureResult(UUID.randomUUID(), null),
			new LedgerAuditTranslator(),
			ledgerMetrics,
			clock
		);
	}

	@Override
	@Transactional
	public PostingOutcome post(PostLedgerTransactionCommand command) {
		Timer.Sample sample = Timer.start();
		try {
			var requestHash = requestHasher.hash(command);
			var existing = idempotencyService.resolveExisting(command.requestIdentity(), requestHash, command.auditTrace());
			if (existing.isPresent()) {
				return handleExistingOutcome(command, existing.get());
			}

			Instant now = Instant.now(clock);
			return persistAndPublish(command, requestHash, now);
		} finally {
			sample.stop(postingTimer);
		}
	}

	private PostingOutcome handleExistingOutcome(PostLedgerTransactionCommand command, PostingOutcome outcome) {
		if (outcome.outcome() == com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.PostingOutcomeType.CONFLICT) {
			conflictCounter.increment();
			publish("CONFLICTING_REQUEST", command.auditTrace(), outcome.transactionId(), Map.of("code", outcome.code()));
			captureAuditSafely(command, "CONFLICTING_REQUEST", outcome.transactionId(), null, command.requestIdentity().requestId());
			return outcome;
		}
		duplicateCounter.increment();
		publish("DUPLICATE_REQUEST", command.auditTrace(), outcome.transactionId(), Map.of("code", outcome.code()));
		captureAuditSafely(command, "DUPLICATE_REQUEST", outcome.transactionId(), null, command.requestIdentity().requestId());
		return outcome;
	}

	private PostingOutcome persistAndPublish(PostLedgerTransactionCommand command, com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity.RequestHash requestHash, Instant now) {
		try {
			return postAccepted(command, requestHash, now);
		} catch (LedgerDomainException ex) {
			return postRejected(command, requestHash, now, ex);
		} catch (RuntimeException ex) {
			failedCounter.increment();
			publish("POSTING_FAILED", command.auditTrace(), null, Map.of("code", "LEDGER_POSTING_FAILED"));
			throw ex;
		}
	}

	private PostingOutcome postAccepted(PostLedgerTransactionCommand command, com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity.RequestHash requestHash, Instant now) {
		var transactionId = LedgerTransactionId.newId();
		LedgerTransaction ledgerTransaction = LedgerTransaction.post(
			transactionId,
			command.businessReference(),
			command.description(),
			command.metadata(),
			command.auditTrace(),
			command.entries(),
			now);
		transactionRepository.save(ledgerTransaction);
		entryRepository.saveAll(ledgerTransaction.entries());
		var outcome = PostingOutcome.accepted(
			"LEDGER_POSTED",
			"Ledger posting accepted",
			command.requestIdentity(),
			transactionId.value().toString(),
			ledgerTransaction.postedAt(),
			command.auditTrace());
		idempotencyService.store(command.requestIdentity(), requestHash, outcome);
		acceptedCounter.increment();
		publish("POSTING_ACCEPTED", command.auditTrace(), outcome.transactionId(), Map.of("entries", ledgerTransaction.entries().size()));
		captureAuditSafely(command, "POSTING_ACCEPTED", outcome.transactionId(), null, command.requestIdentity().requestId());
		return outcome;
	}

	private PostingOutcome postRejected(
			PostLedgerTransactionCommand command,
			com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity.RequestHash requestHash,
			Instant now,
			LedgerDomainException ex
	) {
		var transactionId = LedgerTransactionId.newId();
		var rejected = LedgerTransaction.rejected(
			transactionId,
			command.businessReference(),
			command.description(),
			command.metadata(),
			command.auditTrace(),
			ex.getCode(),
			ex.getMessage(),
			now);
		transactionRepository.save(rejected);
		var outcome = PostingOutcome.rejected(
			ex.getCode(),
			ex.getMessage(),
			command.requestIdentity(),
			transactionId.value().toString(),
			command.auditTrace());
		idempotencyService.store(command.requestIdentity(), requestHash, outcome);
		rejectedCounter.increment();
		publish("POSTING_REJECTED", command.auditTrace(), outcome.transactionId(), Map.of("code", ex.getCode()));
		captureAuditSafely(command, "POSTING_REJECTED", outcome.transactionId(), null, command.requestIdentity().requestId());
		return outcome;
	}

	private void captureAuditSafely(
			PostLedgerTransactionCommand command,
			String stateTo,
			String ledgerTransactionId,
			String idempotencyRecordId,
			String idempotencyKey
	) {
		try {
			auditCaptureUseCase.capture(auditTranslator.toCaptureCommand(
				command,
				stateTo,
				ledgerTransactionId,
				idempotencyRecordId,
				idempotencyKey
			));
		} catch (RuntimeException ex) {
			log.warn("Shared audit capture failed for ledger requestId={}", command.requestIdentity().requestId(), ex);
		}
	}

	private void publish(String type, AuditTrace trace, String transactionId, Map<String, Object> safeDetails) {
		auditEventPublisher.publish(new AuditTrace.AuditEvent(
			type,
			trace.requestIdentity(),
			transactionId,
			trace.actor(),
			trace.correlationId(),
			trace.causationId(),
			Instant.now(clock),
			safeDetails));
	}
}
