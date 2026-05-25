package com.bangnk.ledgercore.ledger_core.balance.adapter.in.web;

import com.bangnk.ledgercore.ledger_core.balance.application.BalanceApplicationErrors.BalanceDomainException;
import com.bangnk.ledgercore.ledger_core.balance.application.BalanceApplicationErrors.BalanceOutcomeType;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.in.web")
public class BalanceApiExceptionHandler {

	@ExceptionHandler(BalanceDomainException.class)
	ResponseEntity<Map<String, Object>> handleDomain(BalanceDomainException ex) {
		return ResponseEntity.status(statusFor(ex.getOutcome()))
			.body(Map.of("outcome", ex.getOutcome().name(), "code", ex.getCode(), "message", ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
		return ResponseEntity.badRequest()
			.body(Map.of("outcome", "REJECTED", "code", "BALANCE_INVALID_REQUEST", "message", "Request validation failed"));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
		return ResponseEntity.badRequest()
			.body(Map.of("outcome", "REJECTED", "code", "BALANCE_INVALID_REQUEST", "message", ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(Map.of("outcome", "FAILED", "code", "BALANCE_OPERATION_FAILED", "message", "Unexpected balance failure"));
	}

	private HttpStatus statusFor(BalanceOutcomeType outcome) {
		return switch (outcome) {
			case ACCEPTED, DUPLICATE -> HttpStatus.OK;
			case REJECTED -> HttpStatus.BAD_REQUEST;
			case CONFLICT -> HttpStatus.CONFLICT;
			case LOCKED -> HttpStatus.LOCKED;
			case FAILED -> HttpStatus.INTERNAL_SERVER_ERROR;
		};
	}
}
