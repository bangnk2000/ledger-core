package com.bangnk.ledgercore.ledger_core.ledger.adapter.in.web;

import com.bangnk.ledgercore.ledger_core.ledger.application.PostingOutcome;
import com.bangnk.ledgercore.ledger_core.ledger.domain.model.LedgerTransaction.LedgerEntryDraft;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.AuditTrace;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.ActorType;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.Direction;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LineId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class PostLedgerTransactionDtos {

	private PostLedgerTransactionDtos() {
	}

	public record PostLedgerTransactionRequest(
			@Size(max = 128) String businessReference,
			@Size(max = 512) String description,
			Map<String, Object> metadata,
			@Size(max = 128) String causationId,
			Instant submittedAt,
			@NotNull @Valid ActorDto actor,
			@NotEmpty List<@Valid PostingEntryDto> entries
	) {
	}

	public record PostingEntryDto(
			@NotBlank String lineId,
			@NotBlank String accountId,
			@NotNull Direction direction,
			@NotBlank String amount,
			@NotBlank String currency,
			Map<String, Object> metadata
	) {
		LedgerEntryDraft toDraft() {
			return new LedgerEntryDraft(
				new LineId(lineId),
				new AccountId(accountId),
				direction,
				new Money(new BigDecimal(amount).setScale(4), currency),
				metadata);
		}
	}

	public record ActorDto(
			@NotBlank String actorId,
			@NotNull ActorType actorType
	) {
		AuditTrace.Actor toDomain() {
			return new AuditTrace.Actor(actorId, actorType);
		}
	}

	public record PostingOutcomeResponse(
			String outcome,
			String code,
			String requestId,
			String requesterScope,
			String transactionId,
			Instant postedAt,
			String message,
			TraceDto trace
	) {
		static PostingOutcomeResponse from(PostingOutcome outcome) {
			return new PostingOutcomeResponse(
				outcome.outcome().name(),
				outcome.code(),
				outcome.requestIdentity().requestId(),
				outcome.requestIdentity().requesterScope(),
				outcome.transactionId(),
				outcome.postedAt(),
				outcome.message(),
				new TraceDto(
					outcome.auditTrace().correlationId(),
					outcome.auditTrace().causationId(),
					outcome.auditTrace().actor().actorId(),
					outcome.auditTrace().actor().actorType().name()));
		}
	}

	public record TraceDto(
			String correlationId,
			String causationId,
			String actorId,
			String actorType
	) {
	}
}
