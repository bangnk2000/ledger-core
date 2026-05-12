package com.bangnk.ledgercore.ledger_core.ledger.adapter.in.web;

import com.bangnk.ledgercore.ledger_core.ledger.adapter.in.web.PostLedgerTransactionDtos.PostLedgerTransactionRequest;
import com.bangnk.ledgercore.ledger_core.ledger.adapter.in.web.PostLedgerTransactionDtos.PostingOutcomeResponse;
import com.bangnk.ledgercore.ledger_core.ledger.application.command.PostLedgerTransactionCommand;
import com.bangnk.ledgercore.ledger_core.ledger.application.port.in.LedgerUseCases.PostLedgerTransactionUseCase;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.AuditTrace;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.PostingOutcomeType;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/ledger/postings")
public class LedgerPostingController {

	private final PostLedgerTransactionUseCase useCase;

	public LedgerPostingController(PostLedgerTransactionUseCase useCase) {
		this.useCase = useCase;
	}

	@PostMapping
	public ResponseEntity<PostingOutcomeResponse> post(
			@RequestHeader("Idempotency-Key") String idempotencyKey,
			@RequestHeader("X-Requester-Scope") String requesterScope,
			@RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
			@Valid @RequestBody PostLedgerTransactionRequest request
	) {
		var command = new PostLedgerTransactionCommand(
			new RequestIdentity(requesterScope, idempotencyKey),
			new AuditTrace(
				new RequestIdentity(requesterScope, idempotencyKey),
				correlationId,
				request.causationId(),
				request.actor().toDomain(),
				request.submittedAt()),
			request.businessReference(),
			request.description(),
			request.metadata(),
			request.entries().stream().map(PostLedgerTransactionDtos.PostingEntryDto::toDraft).toList());

		var outcome = useCase.post(command);
		HttpStatus status = switch (outcome.outcome()) {
			case ACCEPTED -> HttpStatus.CREATED;
			case DUPLICATE -> HttpStatus.OK;
			case CONFLICT -> HttpStatus.CONFLICT;
			case REJECTED -> HttpStatus.BAD_REQUEST;
			case FAILED -> HttpStatus.INTERNAL_SERVER_ERROR;
		};
		return ResponseEntity.status(status).body(PostingOutcomeResponse.from(outcome));
	}
}
