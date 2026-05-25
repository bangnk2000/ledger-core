package com.bangnk.ledgercore.ledger_core.ledger.balance.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.LedgerReplayExportPort.ReplayRecord;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.model.BalanceEnums.BalanceDirection;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.CurrencyCode;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.MoneyAmount;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class BalanceReplayOrderingServiceTest {

	private final BalanceReplayOrderingService service = new BalanceReplayOrderingService();

	@Test
	void ordersReplayRecordsBySequenceThenTransactionThenEntry() {
		List<ReplayRecord> unordered = List.of(
			record(10, "tx-2", "entry-2", "acc-2", "2.0000"),
			record(9, "tx-9", "entry-9", "acc-9", "9.0000"),
			record(10, "tx-1", "entry-3", "acc-3", "3.0000"),
			record(10, "tx-1", "entry-1", "acc-1", "1.0000"));

		List<ReplayRecord> ordered = service.orderDeterministically(unordered);

		assertThat(ordered)
			.extracting(ReplayRecord::ledgerSequence, ReplayRecord::ledgerTransactionId, ReplayRecord::ledgerEntryId)
			.containsExactly(
				org.assertj.core.groups.Tuple.tuple(9L, "tx-9", "entry-9"),
				org.assertj.core.groups.Tuple.tuple(10L, "tx-1", "entry-1"),
				org.assertj.core.groups.Tuple.tuple(10L, "tx-1", "entry-3"),
				org.assertj.core.groups.Tuple.tuple(10L, "tx-2", "entry-2"));
	}

	@Test
	void keepsRecordPayloadsIntactAfterDeterministicOrdering() {
		ReplayRecord first = record(4, "tx-a", "entry-b", "acc-a", "7.2500");
		ReplayRecord second = record(3, "tx-b", "entry-a", "acc-b", "1.5000");

		List<ReplayRecord> ordered = service.orderDeterministically(List.of(first, second));

		assertThat(ordered).containsExactly(second, first);
		assertThat(ordered.get(0).amount()).isEqualTo(new MoneyAmount(new BigDecimal("1.5000")));
		assertThat(ordered.get(1).accountId()).isEqualTo(new AccountId("acc-a"));
	}

	private static ReplayRecord record(long sequence, String transactionId, String entryId, String accountId, String amount) {
		return new ReplayRecord(
			sequence,
			transactionId,
			entryId,
			new AccountId(accountId),
			new CurrencyCode("USD"),
			BalanceDirection.CREDIT,
			new MoneyAmount(new BigDecimal(amount)),
			Instant.parse("2026-05-19T00:00:00Z"));
	}
}
