package com.bangnk.ledgercore.ledger_core.ledger.balance.adapter.out.persistence;

import com.bangnk.ledgercore.ledger_core.ledger.balance.application.port.out.LedgerPostingReferencePort;
import com.bangnk.ledgercore.ledger_core.ledger.balance.domain.valueobject.RequestIdentity;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ContractLedgerPostingReferenceAdapter implements LedgerPostingReferencePort {

	@Override
	public Optional<PostingReference> findPostedReference(RequestIdentity requestIdentity) {
		return Optional.empty();
	}
}
