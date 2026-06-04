package com.bangnk.ledgercore.ledger_core.idempotency.adapter;

import static org.junit.jupiter.api.Assertions.*;

import com.bangnk.ledgercore.ledger_core.idempotency.application.IdempotencyClaimService;
import com.bangnk.ledgercore.ledger_core.idempotency.application.IdempotencyDecision;
import com.bangnk.ledgercore.ledger_core.idempotency.application.IdempotencyDecisionTypes;
import com.bangnk.ledgercore.ledger_core.idempotency.application.IdempotencyFinalizeService;
import com.bangnk.ledgercore.ledger_core.idempotency.application.IdempotencyInspectionService;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.in.IdempotencyClaimUseCase.ClaimCommand;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.in.IdempotencyFinalizeUseCase.FinalizeCommand;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.IdempotencyState;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.OutcomeType;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyRecord;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyKey;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyScope;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.RequestFingerprint;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = com.bangnk.ledgercore.ledger_core.LedgerCoreApplication.class)
@Testcontainers
@org.springframework.test.context.ActiveProfiles("test")
@org.springframework.context.annotation.ComponentScan(basePackages = "com.bangnk.ledgercore.ledger_core")
@org.springframework.test.annotation.DirtiesContext
public class IdempotencyPersistenceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired com.bangnk.ledgercore.ledger_core.idempotency.application.port.in.IdempotencyClaimUseCase claimService;
    @Autowired com.bangnk.ledgercore.ledger_core.idempotency.application.port.in.IdempotencyFinalizeUseCase finalizeService;
    @Autowired com.bangnk.ledgercore.ledger_core.idempotency.application.port.in.IdempotencyInspectionUseCase inspectionService;

    @Test
    public void testFirstExecutionThenReplayPersisted() {
        IdempotencyScope scope = new IdempotencyScope("tenant", UUID.randomUUID().toString(), "create_payment", "DEFAULT");
        IdempotencyKey key = new IdempotencyKey("key-integration-1");
        RequestFingerprint fingerprint = new RequestFingerprint("hash-int-1", "v1", "amount=100,currency=USD", "default");

        ClaimCommand claimCmd = new ClaimCommand(scope, key, fingerprint, "ref-001", "SYSTEM", "svc-1", "corr-1", null);

        // First execution
        IdempotencyDecision decision1 = claimService.claim(claimCmd);
        assertNotNull(decision1, "Decision 1 should not be null");
        assertEquals(IdempotencyDecisionTypes.FIRST_EXECUTION, decision1.decisionType());
        assertNotNull(decision1.claimOwner());

        // Inspect to get record id
        Optional<IdempotencyRecord> recordOpt = inspectionService.inspectRecord(scope, key);
        assertTrue(recordOpt.isPresent());
        IdempotencyRecord record = recordOpt.get();
        assertNotNull(record.getId(), "Record ID should not be null");
        UUID recordId = record.getId();

        assertNotNull(finalizeService, "Finalize service should not be null");
        assertNotNull(decision1.claimOwner(), "Claim owner should not be null");
        
        FinalizeCommand finalizeCmd = new FinalizeCommand(
            recordId, decision1.claimOwner(), OutcomeType.FIRST_EXECUTION,
            "200", "{\"result\":\"created\"}", "tx-001", Instant.now(), null
        );
        finalizeService.finalizeClaim(finalizeCmd);

        // Second claim with same scope/key/fingerprint => REPLAY
        IdempotencyDecision decision2 = claimService.claim(claimCmd);
        assertEquals(IdempotencyDecisionTypes.REPLAY, decision2.decisionType());
        assertNotNull(decision2.replayOutcome());
        assertEquals("200", decision2.replayOutcome().responseCode());
    }

    @Test
    public void testDuplicateInProgressWithSameScopeKey() {
        IdempotencyScope scope = new IdempotencyScope("tenant", UUID.randomUUID().toString(), "create_payment", "DEFAULT");
        IdempotencyKey key = new IdempotencyKey("key-inprogress-1");
        RequestFingerprint fingerprint = new RequestFingerprint("hash-inprogress", "v1", "amount=50", "default");

        ClaimCommand claimCmd = new ClaimCommand(scope, key, fingerprint, "ref-002", "SYSTEM", "svc-1", "corr-2", null);

        // First claim — CLAIMED but not finalized
        IdempotencyDecision decision1 = claimService.claim(claimCmd);
        assertEquals(IdempotencyDecisionTypes.FIRST_EXECUTION, decision1.decisionType());

        // Second claim while first is still in-progress => DUPLICATE_IN_PROGRESS
        IdempotencyDecision decision2 = claimService.claim(claimCmd);
        assertEquals(IdempotencyDecisionTypes.DUPLICATE_IN_PROGRESS, decision2.decisionType());
    }

    @Test
    public void testReplayOutcomePersistsAcrossInspections() {
        IdempotencyScope scope = new IdempotencyScope("tenant", UUID.randomUUID().toString(), "transfer", "DEFAULT");
        IdempotencyKey key = new IdempotencyKey("key-inspect-1");
        RequestFingerprint fingerprint = new RequestFingerprint("hash-inspect-1", "v1", "from=A,to=B,amount=200", "default");

        ClaimCommand claimCmd = new ClaimCommand(scope, key, fingerprint, "ref-003", "SYSTEM", "svc-1", "corr-3", null);
        IdempotencyDecision decision = claimService.claim(claimCmd);

        Optional<IdempotencyRecord> before = inspectionService.inspectRecord(scope, key);
        assertTrue(before.isPresent());
        assertEquals(IdempotencyState.CLAIMED, before.get().getState());

        FinalizeCommand finalizeCmd = new FinalizeCommand(
            before.get().getId(), decision.claimOwner(), OutcomeType.FIRST_EXECUTION,
            "201", "{\"transferId\":\"xyz\"}", "tx-999", Instant.now(), null
        );
        finalizeService.finalizeClaim(finalizeCmd);

        Optional<IdempotencyRecord> after = inspectionService.inspectRecord(scope, key);
        assertTrue(after.isPresent());
        assertEquals(IdempotencyState.COMPLETED, after.get().getState());
        assertNotNull(after.get().getReplayOutcome());
        assertEquals("tx-999", after.get().getReplayOutcome().businessResultReference());
    }
}
