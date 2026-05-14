package com.bangnk.ledgercore.ledger_core.ledger.application.port.out;

import com.bangnk.ledgercore.ledger_core.ledger.application.PostingOutcome;
import com.bangnk.ledgercore.ledger_core.ledger.application.query.AccountBalance;
import com.bangnk.ledgercore.ledger_core.ledger.domain.model.IdempotencyRecord;
import com.bangnk.ledgercore.ledger_core.ledger.domain.model.LedgerEntry;
import com.bangnk.ledgercore.ledger_core.ledger.domain.model.LedgerTransaction;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.AuditTrace.AuditEvent;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LedgerTransactionId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.RequestIdentity;
import java.util.List;
import java.util.Optional;

public final class LedgerPorts {

	private LedgerPorts() {
	}

	public interface LedgerTransactionRepository {
		LedgerTransaction save(LedgerTransaction transaction);
		Optional<LedgerTransaction> findById(LedgerTransactionId transactionId);
	}

	public interface LedgerEntryRepository {
		List<LedgerEntry> saveAll(List<LedgerEntry> entries);
		BalanceSnapshot summarizePostedBalance(AccountId accountId, String currency);
	}

	public interface IdempotencyRecordRepository {
		Optional<IdempotencyRecord> findByIdentity(RequestIdentity requestIdentity);
		IdempotencyRecord save(IdempotencyRecord record);
		IdempotencyRecord touch(IdempotencyRecord record);
	}

	public interface AuditEventPublisher {
		void publish(AuditEvent event);
	}

	public interface PostingOutcomeStore {
		Optional<PostingOutcome> findStoredOutcome(IdempotencyRecord record);
	}

	public record BalanceSnapshot(
		java.math.BigDecimal debitTotal,
		java.math.BigDecimal creditTotal,
		long entryCount
	) {
	}
}
