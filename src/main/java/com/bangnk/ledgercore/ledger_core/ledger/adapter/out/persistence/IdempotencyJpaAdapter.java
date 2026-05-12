package com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence.LedgerJpaEntities.IdempotencyRecordJpaEntity;
import com.bangnk.ledgercore.ledger_core.ledger.adapter.out.persistence.LedgerJpaEntities.IdempotencyRecordKey;
import com.bangnk.ledgercore.ledger_core.ledger.application.PostingOutcome;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.IdempotencyRecordRepository;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.out.LedgerPorts.PostingOutcomeStore;
import com.bangnk.ledgercore.ledger_core.ledger.domain.model.IdempotencyRecord;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity.RequestHash;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class IdempotencyJpaAdapter implements IdempotencyRecordRepository, PostingOutcomeStore {

	private final IdempotencyRecordJpaRepository repository;
	private final JpaLedgerPersistenceAdapter persistenceAdapter;

	public IdempotencyJpaAdapter(IdempotencyRecordJpaRepository repository, JpaLedgerPersistenceAdapter persistenceAdapter) {
		this.repository = repository;
		this.persistenceAdapter = persistenceAdapter;
	}

	@Override
	public Optional<IdempotencyRecord> findByIdentity(RequestIdentity requestIdentity) {
		return repository.findById(new IdempotencyRecordKey(requestIdentity.requesterScope(), requestIdentity.requestId()))
			.map(this::toDomain);
	}

	@Override
	public IdempotencyRecord save(IdempotencyRecord record) {
		repository.save(toEntity(record));
		return record;
	}

	@Override
	public IdempotencyRecord touch(IdempotencyRecord record) {
		repository.save(toEntity(record));
		return record;
	}

	@Override
	public Optional<PostingOutcome> findStoredOutcome(IdempotencyRecord record) {
		if (record.transactionId() == null) {
			return Optional.of(PostingOutcome.rejected(record.responseCode(), "Stored rejected outcome", record.requestIdentity(), null,
				new com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.AuditTrace(record.requestIdentity(), null, null,
					new com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.AuditTrace.Actor("ledger-system",
						com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.ActorType.SYSTEM), null)));
		}
		return persistenceAdapter.findById(new com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LedgerTransactionId(UUID.fromString(record.transactionId())))
			.map(transaction -> switch (record.outcome()) {
				case ACCEPTED -> PostingOutcome.duplicate(record.responseCode(), "Duplicate request resolved to stored accepted outcome",
					record.requestIdentity(), record.transactionId(), transaction.postedAt(), transaction.auditTrace());
				case REJECTED -> PostingOutcome.rejected(record.responseCode(), transaction.rejectionReason(), record.requestIdentity(),
					record.transactionId(), transaction.auditTrace());
				case CONFLICT -> PostingOutcome.conflict(record.responseCode(), "Idempotency key conflict", record.requestIdentity(), transaction.auditTrace());
				case FAILED -> PostingOutcome.failed(record.responseCode(), "Stored failed outcome", record.requestIdentity(), transaction.auditTrace());
				case DUPLICATE -> PostingOutcome.duplicate(record.responseCode(), "Duplicate request resolved to stored duplicate outcome",
					record.requestIdentity(), record.transactionId(), transaction.postedAt(), transaction.auditTrace());
			});
	}

	private IdempotencyRecord toDomain(IdempotencyRecordJpaEntity entity) {
		return new IdempotencyRecord(
			new RequestIdentity(entity.getRequesterScope(), entity.getRequestId()),
			new RequestHash(entity.getRequestHash()),
			entity.getOutcome(),
			entity.getTransactionId() == null ? null : entity.getTransactionId().toString(),
			entity.getResponseCode(),
			entity.getCreatedAt(),
			entity.getLastSeenAt());
	}

	private IdempotencyRecordJpaEntity toEntity(IdempotencyRecord record) {
		return new IdempotencyRecordJpaEntity(
			record.requestIdentity().requesterScope(),
			record.requestIdentity().requestId(),
			record.requestHash().value(),
			record.outcome(),
			record.transactionId() == null ? null : UUID.fromString(record.transactionId()),
			record.responseCode(),
			record.createdAt(),
			record.lastSeenAt());
	}
}
