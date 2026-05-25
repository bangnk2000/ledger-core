package com.bangnk.ledgercore.ledger_core.balance.application;

import com.bangnk.ledgercore.ledger_core.balance.application.BalanceApplicationErrors.BalanceDomainException;
import com.bangnk.ledgercore.ledger_core.balance.application.BalanceApplicationErrors.BalanceOutcomeType;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.RequestIdentity;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Component;

@Component
public class BalanceConsistencyGuard {

	private final AtomicBoolean degraded = new AtomicBoolean(false);

	public void assertWritable(RequestIdentity requestIdentity) {
		if (degraded.get()) {
			throw new BalanceDomainException(
				BalanceOutcomeType.FAILED,
				"BALANCE_FAIL_CLOSED",
				"Balance write flow blocked while balance-management is degraded for request " + requestIdentity.requestId());
		}
	}

	public void setDegraded(boolean degradedState) {
		degraded.set(degradedState);
	}
}
