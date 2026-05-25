package com.bangnk.ledgercore.ledger_core.balance.application;

import com.bangnk.ledgercore.ledger_core.balance.application.BalanceApplicationErrors.BalanceOutcome;
import com.bangnk.ledgercore.ledger_core.balance.application.port.out.BalanceIdempotencyRepositoryPort;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceEnums.BalanceMutationType;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.RequestIdentity;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.RequestIdentity.RequestHash;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class BalanceIdempotencyService {

	private final BalanceIdempotencyRepositoryPort idempotencyRepository;

	public BalanceIdempotencyService(BalanceIdempotencyRepositoryPort idempotencyRepository) {
		this.idempotencyRepository = idempotencyRepository;
	}

	public Optional<IdempotencyDecision> evaluate(
			RequestIdentity requestIdentity,
			RequestHash requestHash,
			BalanceMutationType mutationType,
			String duplicateCode,
			String duplicateMessage,
			String conflictMessage,
			Instant now
	) {
		Optional<BalanceIdempotencyRepositoryPort.IdempotencyRecord> existing = idempotencyRepository.find(requestIdentity);
		if (existing.isEmpty()) {
			return Optional.empty();
		}
		var stored = existing.orElseThrow();
		idempotencyRepository.markSeen(requestIdentity, now);
		if (!stored.sameIntent(requestHash, mutationType)) {
			return Optional.of(new IdempotencyDecision(
				BalanceOutcome.conflict("BALANCE_IDEMPOTENCY_CONFLICT", conflictMessage, requestIdentity),
				stored.responsePayload()));
		}
		return Optional.of(new IdempotencyDecision(
			BalanceOutcome.duplicate(duplicateCode, duplicateMessage, requestIdentity),
			stored.responsePayload()));
	}

	public record IdempotencyDecision(BalanceOutcome outcome, String responsePayload) {
	}
}
