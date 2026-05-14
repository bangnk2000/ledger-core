package com.bangnk.ledgercore.ledger_core.ledger.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bangnk.ledgercore.ledger_core.ledger.domain.model.LedgerEntry;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerEnums.Direction;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.AccountId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LedgerEntryId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LedgerTransactionId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.LedgerIds.LineId;
import com.bangnk.ledgercore.ledger_core.ledger.domain.valueobject.Money;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LedgerEntryImmutabilityTest {

	@Test
	void blocksMutationOfFinancialAndReferenceFields() throws Exception {
		Class<LedgerEntry> type = LedgerEntry.class;

		assertTrue(type.isRecord());
		assertTrue(Modifier.isFinal(type.getDeclaredField("transactionId").getModifiers()));
		assertTrue(Modifier.isFinal(type.getDeclaredField("lineId").getModifiers()));
		assertTrue(Modifier.isFinal(type.getDeclaredField("accountId").getModifiers()));
		assertTrue(Modifier.isFinal(type.getDeclaredField("direction").getModifiers()));
		assertTrue(Modifier.isFinal(type.getDeclaredField("money").getModifiers()));
		assertTrue(Modifier.isFinal(type.getDeclaredField("postedAt").getModifiers()));
		assertEquals(0L, java.util.Arrays.stream(type.getDeclaredMethods())
			.filter(method -> method.getName().startsWith("set"))
			.count());
	}

	@Test
	void returnsImmutableMetadataView() {
		LedgerEntry entry = new LedgerEntry(
			new LedgerEntryId(UUID.randomUUID()),
			new LedgerTransactionId(UUID.randomUUID()),
			new LineId("line-1"),
			new AccountId("cash"),
			Direction.DEBIT,
			new Money(new BigDecimal("100.0000"), "USD"),
			new HashMap<>(Map.of("traceId", "corr-1")),
			Instant.parse("2026-05-12T00:00:00Z"));

		assertThrows(UnsupportedOperationException.class, () -> entry.metadata().put("traceId", "corr-2"));
	}
}
