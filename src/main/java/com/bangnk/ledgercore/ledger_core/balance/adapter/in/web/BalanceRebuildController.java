package com.bangnk.ledgercore.ledger_core.balance.adapter.in.web;

import com.bangnk.ledgercore.ledger_core.balance.adapter.in.web.BalanceRecoveryDtos.RebuildJobDto;
import com.bangnk.ledgercore.ledger_core.balance.adapter.in.web.BalanceRecoveryDtos.StartRebuildRequest;
import com.bangnk.ledgercore.ledger_core.balance.application.port.in.BalanceRebuildUseCase;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/balances/rebuild-jobs")
public class BalanceRebuildController {

	private final BalanceRebuildUseCase rebuildUseCase;

	public BalanceRebuildController(BalanceRebuildUseCase rebuildUseCase) {
		this.rebuildUseCase = rebuildUseCase;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	public RebuildJobDto start(@Valid @RequestBody StartRebuildRequest request) {
		return RebuildJobDto.from(rebuildUseCase.start(request.toCommand()));
	}

	@GetMapping("/{jobId}")
	public RebuildJobDto get(@PathVariable UUID jobId) {
		return rebuildUseCase.get(jobId).map(RebuildJobDto::from).orElseThrow(() -> new IllegalArgumentException("Rebuild job not found"));
	}
}
