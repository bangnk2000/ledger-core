package com.bangnk.ledgercore.ledger_core.balance.application.port.out;

import com.bangnk.ledgercore.ledger_core.balance.domain.valueobject.RequestIdentity;
import java.time.Instant;
import java.util.Optional;

public interface LedgerPostingReferencePort {

	Optional<PostingReference> findPostedReference(RequestIdentity requestIdentity);

	record PostingReference(
			String ledgerTransactionId,
			String postingReference,
			Instant postedAt
	) {
	}
}
