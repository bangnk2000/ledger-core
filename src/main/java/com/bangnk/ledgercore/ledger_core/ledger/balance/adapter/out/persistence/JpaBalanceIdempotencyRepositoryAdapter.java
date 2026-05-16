package com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.out.persistence.BalanceIdempotencyJpaEntity.BalanceIdempotencyKey;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.BalanceIdempotencyRepositoryPort;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.RequestIdentity;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.RequestIdentity.RequestHash;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JpaBalanceIdempotencyRepositoryAdapter implements BalanceIdempotencyRepositoryPort {

	private final BalanceIdempotencyJpaRepository repository;

	public JpaBalanceIdempotencyRepositoryAdapter(BalanceIdempotencyJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<IdempotencyRecord> find(RequestIdentity requestIdentity) {
		return repository.findById(new BalanceIdempotencyKey(requestIdentity.requesterScope(), requestIdentity.requestId()))
			.map(this::toRecord);
	}

	@Override
	public IdempotencyRecord save(IdempotencyRecord record) {
		repository.save(toEntity(record));
		return record;
	}

	@Override
	public void markSeen(RequestIdentity requestIdentity, Instant seenAt) {
		repository.findById(new BalanceIdempotencyKey(requestIdentity.requesterScope(), requestIdentity.requestId()))
			.ifPresent(entity -> {
				entity.setLastSeenAt(seenAt);
				repository.save(entity);
			});
	}

	private IdempotencyRecord toRecord(BalanceIdempotencyJpaEntity entity) {
		return new IdempotencyRecord(
			new RequestIdentity(entity.getRequesterScope(), entity.getRequestId()),
			new RequestHash(entity.getRequestHash()),
			entity.getMutationType(),
			entity.getOutcome(),
			entity.getResponseCode(),
			entity.getResponsePayload(),
			entity.getCreatedAt(),
			entity.getLastSeenAt());
	}

	private BalanceIdempotencyJpaEntity toEntity(IdempotencyRecord record) {
		return new BalanceIdempotencyJpaEntity(
			record.requestIdentity().requesterScope(),
			record.requestIdentity().requestId(),
			record.requestHash().value(),
			record.mutationType(),
			record.outcome(),
			record.responseCode(),
			record.responsePayload(),
			record.createdAt(),
			record.lastSeenAt());
	}
}
