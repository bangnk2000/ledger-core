package com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence.LedgerJpaEntities.LedgerEntryJpaEntity;
import com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence.LedgerJpaEntities.LedgerTransactionJpaEntity;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.BalanceSnapshot;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.LedgerEntryRepository;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.LedgerTransactionRepository;
import com.bangnk.ledgercore.ledger_core.ledger.domain.model.LedgerEntry;
import com.bangnk.ledgercore.ledger_core.ledger.domain.model.LedgerTransaction;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.AuditTrace;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.Direction;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.TransactionStatus;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LedgerEntryId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LedgerTransactionId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LineId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.Money;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JpaLedgerPersistenceAdapter implements LedgerTransactionRepository, LedgerEntryRepository {

	private final LedgerTransactionJpaRepository transactionRepository;
	private final LedgerEntryJpaRepository entryRepository;
	private final ObjectMapper objectMapper;

	public JpaLedgerPersistenceAdapter(
			LedgerTransactionJpaRepository transactionRepository,
			LedgerEntryJpaRepository entryRepository,
			ObjectMapper objectMapper
	) {
		this.transactionRepository = transactionRepository;
		this.entryRepository = entryRepository;
		this.objectMapper = objectMapper;
	}

	@Override
	public LedgerTransaction save(LedgerTransaction transaction) {
		transactionRepository.save(toEntity(transaction));
		return transaction;
	}

	@Override
	public Optional<LedgerTransaction> findById(LedgerTransactionId transactionId) {
		return transactionRepository.findById(transactionId.value()).map(this::toDomain);
	}

	@Override
	public List<LedgerEntry> saveAll(List<LedgerEntry> entries) {
		entryRepository.saveAll(entries.stream().map(this::toEntity).toList());
		return entries;
	}

	@Override
	public BalanceSnapshot summarizePostedBalance(AccountId accountId, String currency) {
		var result = entryRepository.calculateBalance(accountId.value(), currency);
		if (result == null) {
			return new BalanceSnapshot(java.math.BigDecimal.ZERO.setScale(4), java.math.BigDecimal.ZERO.setScale(4), 0L);
		}
		return new BalanceSnapshot(result.debitTotal(), result.creditTotal(), result.entryCount());
	}

	public List<LedgerEntry> findEntriesByTransactionId(LedgerTransactionId transactionId) {
		return entryRepository.findByTransactionId(transactionId.value()).stream().map(this::toDomain).toList();
	}

	private LedgerTransactionJpaEntity toEntity(LedgerTransaction transaction) {
		return new LedgerTransactionJpaEntity(
			transaction.id().value(),
			transaction.auditTrace().requestIdentity().requesterScope(),
			transaction.auditTrace().requestIdentity().requestId(),
			"stored-in-idempotency-record",
			transaction.status(),
			transaction.businessReference(),
			transaction.description(),
			writeMap(transaction.metadata()),
			transaction.auditTrace().correlationId(),
			transaction.auditTrace().causationId(),
			transaction.auditTrace().actor().actorId(),
			transaction.auditTrace().actor().actorType(),
			transaction.auditTrace().submittedAt(),
			transaction.postedAt(),
			transaction.rejectionCode(),
			transaction.rejectionReason(),
			transaction.createdAt());
	}

	private LedgerEntryJpaEntity toEntity(LedgerEntry entry) {
		return new LedgerEntryJpaEntity(
			entry.id().value(),
			entry.transactionId().value(),
			entry.lineId().value(),
			entry.accountId().value(),
			entry.direction(),
			entry.money().amount(),
			entry.money().currency(),
			writeMap(entry.metadata()),
			entry.postedAt(),
			entry.postedAt());
	}

	private LedgerTransaction toDomain(LedgerTransactionJpaEntity entity) {
		return new LedgerTransaction(
			new LedgerTransactionId(entity.getId()),
			entity.getStatus(),
			entity.getBusinessReference(),
			entity.getDescription(),
			readMap(entity.getMetadata()),
			new AuditTrace(
				new com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity(entity.getRequesterScope(), entity.getRequestId()),
				entity.getCorrelationId(),
				entity.getCausationId(),
				new AuditTrace.Actor(entity.getActorId(), entity.getActorType()),
				entity.getSubmittedAt()),
			entity.getCreatedAt(),
			entity.getPostedAt(),
			entity.getRejectionCode(),
			entity.getRejectionReason(),
			findEntriesByTransactionId(new LedgerTransactionId(entity.getId())));
	}

	private LedgerEntry toDomain(LedgerEntryJpaEntity entity) {
		return new LedgerEntry(
			new LedgerEntryId(entity.getId()),
			new LedgerTransactionId(entity.getTransactionId()),
			new LineId(entity.getLineId()),
			new AccountId(entity.getAccountId()),
			entity.getDirection(),
			new Money(entity.getAmount(), entity.getCurrency()),
			readMap(entity.getEntryMetadata()),
			entity.getPostedAt());
	}

	private String writeMap(Map<String, Object> map) {
		try {
			return objectMapper.writeValueAsString(map == null ? Map.of() : map);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Unable to serialize metadata", ex);
		}
	}

	private Map<String, Object> readMap(String value) {
		if (value == null || value.isBlank()) {
			return Map.of();
		}
		try {
			return objectMapper.readValue(value, new TypeReference<>() { });
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Unable to deserialize metadata", ex);
		}
	}
}
