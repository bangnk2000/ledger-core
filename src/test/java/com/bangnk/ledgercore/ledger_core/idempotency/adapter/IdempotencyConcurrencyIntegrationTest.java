package com.bangnk.ledgercore.ledger_core.idempotency.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bangnk.ledgercore.ledger_core.idempotency.application.IdempotencyDecision;
import com.bangnk.ledgercore.ledger_core.idempotency.application.IdempotencyDecisionTypes;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.in.IdempotencyClaimUseCase;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.in.IdempotencyClaimUseCase.ClaimCommand;
import com.bangnk.ledgercore.ledger_core.idempotency.application.port.in.IdempotencyInspectionUseCase;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyKey;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.IdempotencyScope;
import com.bangnk.ledgercore.ledger_core.idempotency.domain.valueobject.RequestFingerprint;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
class IdempotencyConcurrencyIntegrationTest {

    private static final int CONCURRENT_ATTEMPTS = 20;

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
    private IdempotencyInspectionUseCase inspectionUseCase;

    @Test
    void allowsExactlyOneWinnerForConcurrentDuplicateClaims() throws Exception {
        IdempotencyScope scope = new IdempotencyScope("tenant", UUID.randomUUID().toString(), "ledger-posting", "DEFAULT");
        IdempotencyKey key = new IdempotencyKey("concurrency-key");
        RequestFingerprint fingerprint = new RequestFingerprint("same-hash", "v1", "amount=100", "ledger-posting-v1");
        ClaimCommand command = new ClaimCommand(scope, key, fingerprint, "business-ref", "SYSTEM", "svc-1", "corr-1", null);

        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<Callable<IdempotencyDecision>> tasks = new ArrayList<>();
            for (int i = 0; i < CONCURRENT_ATTEMPTS; i++) {
                tasks.add(() -> claimUseCase.claim(command));
            }

            List<Future<IdempotencyDecision>> futures = executor.invokeAll(tasks);
            List<IdempotencyDecision> results = new ArrayList<>();
            for (Future<IdempotencyDecision> future : futures) {
                results.add(future.get());
            }

            long firstExecutionCount = results.stream()
                .filter(decision -> decision.decisionType() == IdempotencyDecisionTypes.FIRST_EXECUTION)
                .count();
            long duplicateInProgressCount = results.stream()
                .filter(decision -> decision.decisionType() == IdempotencyDecisionTypes.DUPLICATE_IN_PROGRESS)
                .count();

            assertEquals(1, firstExecutionCount);
            assertEquals(CONCURRENT_ATTEMPTS - 1L, duplicateInProgressCount);
            assertTrue(inspectionUseCase.inspectRecord(scope, key).isPresent());
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
