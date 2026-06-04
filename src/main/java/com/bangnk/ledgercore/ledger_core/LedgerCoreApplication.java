package com.bangnk.ledgercore.ledger_core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.bangnk.ledgercore.ledger_core")
public class LedgerCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(LedgerCoreApplication.class, args);
	}

}
