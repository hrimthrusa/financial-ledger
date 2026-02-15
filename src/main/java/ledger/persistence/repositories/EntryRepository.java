package ledger.persistence.repositories;

import ledger.persistence.entities.TransactionEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EntryRepository extends JpaRepository<TransactionEntry, UUID> {

    List<TransactionEntry> findByTransactionId(UUID transactionId);

    List<TransactionEntry> findAllByAccountId(UUID accountId);
}
