package com.bangnk.ledgercore.ledger_core.idempotency.application;

import com.bangnk.ledgercore.ledger_core.idempotency.application.port.out.IdempotencyObservabilityPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IdempotencyObservability implements IdempotencyObservabilityPort {

    private final Counter claimGrantedCounter;
    private final Counter duplicateSeenCounter;
    private final Counter conflictDetectedCounter;
    private final Counter duplicateInProgressCounter;
    private final Counter indeterminateRecordedCounter;
    private final Counter expirationCounter;
    private final Counter cleanupCounter;

    public IdempotencyObservability(MeterRegistry meterRegistry) {
        MeterRegistry registry = meterRegistry != null ? meterRegistry : new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        this.claimGrantedCounter = Counter.builder("idempotency.claim.granted")
            .register(registry);
        this.duplicateSeenCounter = Counter.builder("idempotency.duplicate.seen")
            .register(registry);
        this.conflictDetectedCounter = Counter.builder("idempotency.conflict.detected")
            .register(registry);
        this.duplicateInProgressCounter = Counter.builder("idempotency.duplicate.inprogress")
            .register(registry);
        this.indeterminateRecordedCounter = Counter.builder("idempotency.indeterminate.recorded")
            .register(registry);
        this.expirationCounter = Counter.builder("idempotency.expiration")
            .register(registry);
        this.cleanupCounter = Counter.builder("idempotency.cleanup")
            .register(registry);
    }

    @Override
    public void trackDuplicateSeen(String scopeType, String scopeValue, String key) {
        log.info("Idempotency duplicate request seen: scopeType={}, scopeValue={}, key={}", scopeType, scopeValue, key);
        duplicateSeenCounter.increment();
    }

    @Override
    public void trackConflictDetected(String scopeType, String scopeValue, String key) {
        log.warn("Idempotency conflict detected: scopeType={}, scopeValue={}, key={}", scopeType, scopeValue, key);
        conflictDetectedCounter.increment();
    }

    @Override
    public void trackClaimGranted(String scopeType, String scopeValue, String key) {
        log.info("Idempotency claim granted for first execution: scopeType={}, scopeValue={}, key={}", scopeType, scopeValue, key);
        claimGrantedCounter.increment();
    }

    @Override
    public void trackDuplicateInProgress(String scopeType, String scopeValue, String key) {
        log.info("Idempotency duplicate in progress request seen: scopeType={}, scopeValue={}, key={}", scopeType, scopeValue, key);
        duplicateInProgressCounter.increment();
    }

    @Override
    public void trackIndeterminateRecorded(String scopeType, String scopeValue, String key) {
        log.warn("Idempotency indeterminate outcome recorded: scopeType={}, scopeValue={}, key={}", scopeType, scopeValue, key);
        indeterminateRecordedCounter.increment();
    }

    @Override
    public void trackExpiration(String scopeType, String scopeValue, String key) {
        log.info("Idempotency key expired: scopeType={}, scopeValue={}, key={}", scopeType, scopeValue, key);
        expirationCounter.increment();
    }

    @Override
    public void trackCleanup(int cleanedCount) {
        log.info("Idempotency cleanup run: purged={} records", cleanedCount);
        cleanupCounter.increment(cleanedCount);
    }
}
