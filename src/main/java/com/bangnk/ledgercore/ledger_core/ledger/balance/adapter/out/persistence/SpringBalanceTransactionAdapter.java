package com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.BalanceTransactionPort;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SpringBalanceTransactionAdapter implements BalanceTransactionPort {

	@Override
	@Transactional
	public <T> T withinProtectedWrite(Supplier<T> action) {
		return action.get();
	}

	@Override
	@Transactional(readOnly = true)
	public <T> T withinReadOnlySnapshot(Supplier<T> action) {
		return action.get();
	}
}
