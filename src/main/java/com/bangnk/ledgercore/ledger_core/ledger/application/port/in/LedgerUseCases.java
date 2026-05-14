package com.bangnk.ledgercore.ledger_core.ledger.application.port.in;

import com.bangnk.ledgercore.ledger_core.ledger.application.PostingOutcome;
import com.bangnk.ledgercore.ledger_core.ledger.application.command.PostLedgerTransactionCommand;
import com.bangnk.ledgercore.ledger_core.ledger.application.query.AccountBalance;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.AccountId;

public final class LedgerUseCases {

	private LedgerUseCases() {
	}

	public interface PostLedgerTransactionUseCase {
		PostingOutcome post(PostLedgerTransactionCommand command);
	}

	public interface GetAccountBalanceQuery {
		AccountBalance getBalance(AccountId accountId, String currency);
	}
}
