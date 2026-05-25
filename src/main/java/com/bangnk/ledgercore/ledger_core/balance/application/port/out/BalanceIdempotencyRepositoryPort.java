package com.bangnk.ledgercore.ledger_core.balance.application.port.out;

import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceMutationType;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.RequestIdentity;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.RequestIdentity.RequestHash;
import java.time.Instant;
import java.util.Optional;

public interface BalanceIdempotencyRepositoryPort {

	Optional<IdempotencyRecord> find(RequestIdentity requestIdentity);

	IdempotencyRecord save(IdempotencyRecord record);

	void markSeen(RequestIdentity requestIdentity, Instant seenAt);

	record IdempotencyRecord(
			RequestIdentity requestIdentity,
			RequestHash requestHash,
			BalanceMutationType mutationType,
			String outcome,
			String responseCode,
			String responsePayload,
			Instant createdAt,
			Instant lastSeenAt
	) {
		public boolean sameIntent(RequestHash otherHash, BalanceMutationType otherMutationType) {
			return requestHash.equals(otherHash) && mutationType == otherMutationType;
		}
	}
}
