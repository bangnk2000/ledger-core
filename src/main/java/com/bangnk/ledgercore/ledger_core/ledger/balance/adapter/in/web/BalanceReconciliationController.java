package com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.in.web;

import com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.in.web.BalanceRecoveryDtos.ReconciliationRunAcceptedDto;
import com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.in.web.BalanceRecoveryDtos.StartReconciliationRequest;
import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.in.BalanceReconciliationUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/balances/reconciliations")
public class BalanceReconciliationController {

	private final BalanceReconciliationUseCase reconciliationUseCase;

	public BalanceReconciliationController(BalanceReconciliationUseCase reconciliationUseCase) {
		this.reconciliationUseCase = reconciliationUseCase;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	public ReconciliationRunAcceptedDto start(@Valid @RequestBody StartReconciliationRequest request) {
		return ReconciliationRunAcceptedDto.from(reconciliationUseCase.start(request.toCommand()));
	}
}
