package com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out;

import java.util.function.Supplier;

public interface BalanceTransactionPort {

	<T> T withinProtectedWrite(Supplier<T> action);

	<T> T withinReadOnlySnapshot(Supplier<T> action);
}
