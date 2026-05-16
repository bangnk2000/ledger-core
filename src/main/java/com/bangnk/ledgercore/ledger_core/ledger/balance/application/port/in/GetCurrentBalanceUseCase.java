package com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.in;

import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceSnapshot;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceSnapshot.ConsistencyMode;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.CurrencyCode;

public interface GetCurrentBalanceUseCase {

	BalanceSnapshot getCurrentBalance(AccountId accountId, CurrencyCode currency, ConsistencyMode consistencyMode);
}
