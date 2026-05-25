package com.bangnk.ledgercore.ledger_core.ledger.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.bangnk.ledgercore.ledger_core.ledger.application.command.PostLedgerTransactionCommand;
import com.bangnk.ledgercore.ledger_core.ledger.application.command.PostLedgerTransactionService;
import com.bangnk.ledgercore.ledger_core.ledger.domain.model.LedgerTransaction.LedgerEntryDraft;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.AuditTrace;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.ActorType;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.Direction;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.PostingOutcomeType;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LineId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.Money;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PostLedgerTransactionCharacterizationTest extends PostgresIntegrationTestBase {

    @Autowired
    private PostLedgerTransactionService service;

    @Test
    void keepsAcceptedOutcomeAndAuditTraceStable() {
        var command = command("characterization-accepted", List.of(
            draft("line-1", "cash", Direction.DEBIT, "100.0000"),
            draft("line-2", "revenue", Direction.CREDIT, "100.0000")));

        var outcome = service.post(command);

        assertThat(outcome.outcome()).isEqualTo(PostingOutcomeType.ACCEPTED);
        assertThat(outcome.code()).isEqualTo("LEDGER_POSTED");
        assertThat(outcome.postedAt()).isNotNull();
        assertThat(outcome.auditTrace()).isEqualTo(command.auditTrace());
    }

    @Test
    void keepsRejectedOutcomeAndAuditTraceStable() {
        var command = command("characterization-rejected", List.of(
            draft("line-1", "cash", Direction.DEBIT, "100.0000"),
            draft("line-2", "revenue", Direction.CREDIT, "90.0000")));

        var outcome = service.post(command);

        assertThat(outcome.outcome()).isEqualTo(PostingOutcomeType.REJECTED);
        assertThat(outcome.code()).isEqualTo("LEDGER_UNBALANCED");
        assertThat(outcome.postedAt()).isNull();
        assertThat(outcome.auditTrace()).isEqualTo(command.auditTrace());
    }

    @Test
    void keepsDuplicateBranchStableWhenExistingOutcomeIsReturned() {
        var first = command("characterization-duplicate", List.of(
            draft("line-1", "cash", Direction.DEBIT, "100.0000"),
            draft("line-2", "revenue", Direction.CREDIT, "100.0000")));
        var duplicateRequest = command("characterization-duplicate", List.of(
            draft("line-1", "cash", Direction.DEBIT, "100.0000"),
            draft("line-2", "revenue", Direction.CREDIT, "100.0000")));

        var firstOutcome = service.post(first);
        var duplicateOutcome = service.post(duplicateRequest);

        assertThat(firstOutcome.outcome()).isEqualTo(PostingOutcomeType.ACCEPTED);
        assertThat(duplicateOutcome.outcome()).isEqualTo(PostingOutcomeType.DUPLICATE);
        assertThat(duplicateOutcome.code()).isEqualTo("LEDGER_POSTED");
        assertThat(duplicateOutcome.transactionId()).isEqualTo(firstOutcome.transactionId());
        assertThat(duplicateOutcome.auditTrace()).isEqualTo(duplicateRequest.auditTrace());
    }

    @Test
    void keepsConflictBranchStableWhenExistingRequestIdentityHasDifferentContent() {
        var first = command("characterization-conflict", List.of(
            draft("line-1", "cash", Direction.DEBIT, "100.0000"),
            draft("line-2", "revenue", Direction.CREDIT, "100.0000")));
        var conflictingRequest = command("characterization-conflict", List.of(
            draft("line-1", "cash", Direction.DEBIT, "100.0000"),
            draft("line-2", "revenue", Direction.CREDIT, "90.0000"),
            draft("line-3", "adjustment", Direction.CREDIT, "10.0000")));

        var firstOutcome = service.post(first);
        var conflictOutcome = service.post(conflictingRequest);

        assertThat(firstOutcome.outcome()).isEqualTo(PostingOutcomeType.ACCEPTED);
        assertThat(conflictOutcome.outcome()).isEqualTo(PostingOutcomeType.CONFLICT);
        assertThat(conflictOutcome.code()).isEqualTo("LEDGER_IDEMPOTENCY_CONFLICT");
        assertThat(conflictOutcome.transactionId()).isNull();
        assertThat(conflictOutcome.auditTrace()).isEqualTo(conflictingRequest.auditTrace());
    }

    private static PostLedgerTransactionCommand command(String requestId, List<LedgerEntryDraft> entries) {
        RequestIdentity identity = new RequestIdentity("characterization", requestId);
        return new PostLedgerTransactionCommand(
            identity,
            new AuditTrace(
                identity,
                "corr-" + requestId,
                "cause-" + requestId,
                new AuditTrace.Actor("characterization-suite", ActorType.SYSTEM),
                Instant.parse("2026-05-12T00:00:00Z")),
            "biz-" + requestId,
            "characterization",
            null,
            entries);
    }

    private static LedgerEntryDraft draft(String lineId, String accountId, Direction direction, String amount) {
        return new LedgerEntryDraft(
            new LineId(lineId),
            new AccountId(accountId),
            direction,
            new Money(new BigDecimal(amount), "USD"),
            null);
    }
}
