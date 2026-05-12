package com.bangnk.ledgercore.ledger_core.ledger.application.command;

import com.bangnk.ledgercore.ledger_core.ledger.application.PostingOutcome;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.IdempotencyRecordRepository;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.PostingOutcomeStore;
import com.bangnk.ledgercore.ledger_core.ledger.domain.model.IdempotencyRecord;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.AuditTrace;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.PostingOutcomeType;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity.RequestHash;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyService {

	private final IdempotencyRecordRepository repository;
	private final PostingOutcomeStore postingOutcomeStore;
	private final Clock clock;

	public IdempotencyService(
			IdempotencyRecordRepository repository,
			PostingOutcomeStore postingOutcomeStore,
			Clock clock
	) {
		this.repository = repository;
		this.postingOutcomeStore = postingOutcomeStore;
		this.clock = clock;
	}

	public Optional<PostingOutcome> resolveExisting(RequestIdentity requestIdentity, RequestHash requestHash, AuditTrace auditTrace) {
		return repository.findByIdentity(requestIdentity)
			.map(existing -> {
				repository.touch(existing.touch(Instant.now(clock)));
				if (!existing.requestHash().equals(requestHash)) {
					return PostingOutcome.conflict(
						"LEDGER_IDEMPOTENCY_CONFLICT",
						"Idempotency key was reused with different request content",
						requestIdentity,
						auditTrace);
				}
				return postingOutcomeStore.findStoredOutcome(existing)
					.orElseGet(() -> PostingOutcome.duplicate(
						"LEDGER_DUPLICATE_ACCEPTED",
						"Duplicate request resolved to stored outcome",
						requestIdentity,
						existing.transactionId(),
						existing.createdAt(),
						auditTrace));
			});
	}

	public IdempotencyRecord store(RequestIdentity requestIdentity, RequestHash requestHash, PostingOutcome outcome) {
		Instant now = Instant.now(clock);
		return repository.save(new IdempotencyRecord(
			requestIdentity,
			requestHash,
			outcome.outcome(),
			outcome.transactionId(),
			outcome.code(),
			now,
			now));
	}
}
