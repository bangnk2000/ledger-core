package com.bangnk.ledgercore.ledger_core.ledger.adapter.in.web;

import com.bangnk.ledgercore.ledger_core.ledger.application.PostingOutcome.LedgerDomainException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class LedgerExceptionHandler {

	@ExceptionHandler(LedgerDomainException.class)
	ResponseEntity<Map<String, Object>> handleDomain(LedgerDomainException ex) {
		return ResponseEntity.badRequest()
			.body(Map.of("outcome", "REJECTED", "code", ex.getCode(), "message", ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
		return ResponseEntity.badRequest()
			.body(Map.of("outcome", "REJECTED", "code", "LEDGER_INVALID_REQUEST", "message", "Request validation failed"));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(Map.of("outcome", "FAILED", "code", "LEDGER_POSTING_FAILED", "message", "Unexpected ledger failure"));
	}
}
