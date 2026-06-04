package com.bangnk.ledgercore.ledger_core.idempotency.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bangnk.ledgercore.ledger_core.idempotency.application.IdempotencyDecision;
import com.bangnk.ledgercore.ledger_core.idempotency.application.IdempotencyDecisionTypes;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.in.IdempotencyClaimUseCase;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.in.IdempotencyClaimUseCase.ClaimCommand;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.in.IdempotencyCleanupUseCase;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.in.IdempotencyFinalizeUseCase;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.in.IdempotencyFinalizeUseCase.FinalizeCommand;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.in.IdempotencyInspectionUseCase;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.IdempotencyRecordRepositoryPort;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.OutcomeType;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.model.IdempotencyEnums.RetentionStatus;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = com.bangnk.ledgercore.ledger_core.LedgerCoreApplication.class)
@Testcontainers
@ActiveProfiles("test")
@ComponentScan(basePackages = "com.bangnk.ledgercore.ledger_core")
@DirtiesContext
class IdempotencyExpirationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private IdempotencyClaimUseCase claimUseCase;

    @Autowired
    private IdempotencyFinalizeUseCase finalizeUseCase;

    @Autowired
    private IdempotencyInspectionUseCase inspectionUseCase;

    @Autowired
    private IdempotencyCleanupUseCase cleanupUseCase;

    @Autowired
    private IdempotencyRecordRepositoryPort recordRepository;

    @Test
    void keepsReexecutionBlockedAcrossReplayAndTombstoneWindows() {
        IdempotencyScope scope = new IdempotencyScope("tenant", UUID.randomUUID().toString(), "reserve", "DEFAULT");
        IdempotencyKey key = new IdempotencyKey("expire-key");
        RequestFingerprint fingerprint = new RequestFingerprint("hash-expire", "v1", "amount=100", "balance-reserve-v1");
        ClaimCommand claimCommand = new ClaimCommand(scope, key, fingerprint, "business-ref", "SYSTEM", "svc-1", "corr-1", null);

        IdempotencyDecision firstDecision = claimUseCase.claim(claimCommand);
        IdempotencyRecord record = inspectionUseCase.inspectRecord(scope, key).orElseThrow();
        finalizeUseCase.finalizeClaim(new FinalizeCommand(
            record.getId(), firstDecision.claimOwner(), OutcomeType.FIRST_EXECUTION, "200", "{\"ok\":true}", "reservation-1", Instant.now(), null
        ));

        IdempotencyRecord replayable = inspectionUseCase.inspectRecord(scope, key).orElseThrow();
        replayable.setReplayWindowExpiresAt(Instant.now().minusSeconds(60));
        replayable.setTombstoneExpiresAt(Instant.now().plusSeconds(3600));
        recordRepository.save(replayable);

        assertEquals(0, cleanupUseCase.cleanupExpiredRecords(Instant.now()));
        IdempotencyRecord tombstoned = inspectionUseCase.inspectRecord(scope, key).orElseThrow();
        assertEquals(RetentionStatus.TOMBSTONED, tombstoned.getRetentionStatus());

        IdempotencyDecision expiredDecision = claimUseCase.claim(claimCommand);
        assertEquals(IdempotencyDecisionTypes.EXPIRED_KEY, expiredDecision.decisionType());

        tombstoned.setTombstoneExpiresAt(Instant.now().minusSeconds(60));
        recordRepository.save(tombstoned);

        assertEquals(1, cleanupUseCase.cleanupExpiredRecords(Instant.now()));
        Optional<IdempotencyRecord> deleted = inspectionUseCase.inspectRecord(scope, key);
        assertFalse(deleted.isPresent());
    }
}
