package com.bangnk.ledgercore.ledger_core.ledger.adapter.in.web;

import com.bangnk.ledgercore.ledger_core.ledger.adapter.in.web.GetAccountBalanceDtos.AccountBalanceResponse;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.in.LedgerUseCases.GetAccountBalanceQuery;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.AccountId;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/ledger/accounts")
public class LedgerBalanceController {

	private final GetAccountBalanceQuery query;

	public LedgerBalanceController(GetAccountBalanceQuery query) {
		this.query = query;
	}

	@GetMapping("/{accountId}/balance")
	public AccountBalanceResponse getBalance(
			@PathVariable @NotBlank String accountId,
			@RequestParam(defaultValue = "USD") String currency
	) {
		return AccountBalanceResponse.from(query.getBalance(new AccountId(accountId), currency));
	}
}
