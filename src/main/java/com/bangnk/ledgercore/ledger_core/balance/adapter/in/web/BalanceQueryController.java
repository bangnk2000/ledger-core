package com.bangnk.ledgercore.ledger_core.balance.adapter.in.web;

import com.bangnk.ledgercore.ledger_core.balance.adapter.in.web.BalanceQueryDtos.CurrentBalanceViewDto;
import com.bangnk.ledgercore.ledger_core.balance.application.port.in.GetCurrentBalanceUseCase;
import com.bangnk.ledgercore.ledger_core.balance.domain.model.BalanceSnapshot.ConsistencyMode;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.CurrencyCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/balances/accounts")
public class BalanceQueryController {

	private final GetCurrentBalanceUseCase getCurrentBalanceUseCase;

	public BalanceQueryController(GetCurrentBalanceUseCase getCurrentBalanceUseCase) {
		this.getCurrentBalanceUseCase = getCurrentBalanceUseCase;
	}

	@GetMapping("/{accountId}/current")
	public CurrentBalanceViewDto getCurrentBalance(
			@PathVariable @NotBlank String accountId,
			@RequestParam @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$") String currency,
			@RequestParam(defaultValue = "STRONG") ConsistencyMode consistency
	) {
		return CurrentBalanceViewDto.from(getCurrentBalanceUseCase.getCurrentBalance(
			new AccountId(accountId),
			new CurrencyCode(currency),
			consistency));
	}
}
