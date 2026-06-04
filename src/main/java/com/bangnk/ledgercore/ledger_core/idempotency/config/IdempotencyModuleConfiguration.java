package com.bangnk.ledgercore.ledger_core.idempotency.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.bangnk.ledgercore.ledger_core.idempotency")
public class IdempotencyModuleConfiguration {
}
