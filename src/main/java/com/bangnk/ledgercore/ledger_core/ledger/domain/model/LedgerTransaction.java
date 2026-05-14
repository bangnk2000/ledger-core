package com.bangnk.ledgercore.ledger_core.ledger.domain.model;

import com.bangnk.ledgercore.ledger_core.ledger.application.PostingOutcome.LedgerDomainException;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.AuditTrace;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.Direction;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.TransactionStatus;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LedgerEntryId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LedgerTransactionId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record LedgerTransaction(
		LedgerTransactionId id,
		TransactionStatus status,
		String businessReference,
		String description,
		Map<String, Object> metadata,
		AuditTrace auditTrace,
		Instant createdAt,
		Instant postedAt,
		String rejectionCode,
		String rejectionReason,
		List<LedgerEntry> entries
) {
	public LedgerTransaction {
		Objects.requireNonNull(id, "id must not be null");
		Objects.requireNonNull(status, "status must not be null");
		Objects.requireNonNull(auditTrace, "auditTrace must not be null");
		Objects.requireNonNull(createdAt, "createdAt must not be null");
		metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
		entries = entries == null ? List.of() : List.copyOf(entries);
	}

	public static LedgerTransaction post(
			LedgerTransactionId transactionId,
			String businessReference,
			String description,
			Map<String, Object> metadata,
			AuditTrace auditTrace,
			List<LedgerEntryDraft> drafts,
			Instant now
	) {
		validateDrafts(drafts);
		var entries = drafts.stream()
			.map(draft -> new LedgerEntry(
				LedgerEntryId.newId(),
				transactionId,
				draft.lineId(),
				draft.accountId(),
				draft.direction(),
				draft.money(),
				draft.metadata(),
				now))
			.toList();
		return new LedgerTransaction(
			transactionId,
			TransactionStatus.POSTED,
			businessReference,
			description,
			metadata,
			auditTrace,
			now,
			now,
			null,
			null,
			entries);
	}

	public static LedgerTransaction rejected(
			LedgerTransactionId transactionId,
			String businessReference,
			String description,
			Map<String, Object> metadata,
			AuditTrace auditTrace,
			String rejectionCode,
			String rejectionReason,
			Instant now
	) {
		return new LedgerTransaction(
			transactionId,
			TransactionStatus.REJECTED,
			businessReference,
			description,
			metadata,
			auditTrace,
			now,
			null,
			rejectionCode,
			rejectionReason,
			List.of());
	}

	private static void validateDrafts(List<LedgerEntryDraft> drafts) {
		if (drafts == null || drafts.size() < 2) {
			throw new LedgerDomainException("LEDGER_MINIMUM_ENTRIES", "At least two entries are required");
		}
		Set<String> lineIds = new HashSet<>();
		boolean hasDebit = false;
		boolean hasCredit = false;
		BigDecimal debitTotal = BigDecimal.ZERO.setScale(4);
		BigDecimal creditTotal = BigDecimal.ZERO.setScale(4);
		String currency = null;
		for (LedgerEntryDraft draft : drafts) {
			if (!lineIds.add(draft.lineId().value())) {
				throw new LedgerDomainException("LEDGER_DUPLICATE_LINE_ID", "Duplicate lineId detected");
			}
			if (currency == null) {
				currency = draft.money().currency();
			} else if (!currency.equals(draft.money().currency())) {
				throw new LedgerDomainException("LEDGER_CURRENCY_MISMATCH", "All entries must share the same currency");
			}
			if (draft.direction() == Direction.DEBIT) {
				hasDebit = true;
				debitTotal = debitTotal.add(draft.money().amount());
			} else {
				hasCredit = true;
				creditTotal = creditTotal.add(draft.money().amount());
			}
		}
		if (!hasDebit || !hasCredit) {
			throw new LedgerDomainException("LEDGER_MISSING_DIRECTION", "At least one debit and one credit are required");
		}
		if (debitTotal.compareTo(creditTotal) != 0) {
			throw new LedgerDomainException("LEDGER_UNBALANCED", "Debit and credit totals must balance");
		}
	}

	public record LedgerEntryDraft(
			com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LineId lineId,
			com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.AccountId accountId,
			Direction direction,
			Money money,
			Map<String, Object> metadata
	) {
		public LedgerEntryDraft {
			Objects.requireNonNull(lineId, "lineId must not be null");
			Objects.requireNonNull(accountId, "accountId must not be null");
			Objects.requireNonNull(direction, "direction must not be null");
			Objects.requireNonNull(money, "money must not be null");
			metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
		}
	}
}
