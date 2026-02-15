package ledger.domain.rules;

import ledger.domain.models.EntryType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class TransactionRules {
    private TransactionRules() {}

    public static void validate(String description, OffsetDateTime date, List<EntryDraft> entries) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction description cannot be empty");
        }
        Objects.requireNonNull(date, "Transaction date is required");

        if (entries == null || entries.size() < 2) {
            throw new IllegalArgumentException("Transaction must have at least 2 entries");
        }

        boolean hasDebit = entries.stream().anyMatch(e -> e.type() == EntryType.DEBIT);
        boolean hasCredit = entries.stream().anyMatch(e -> e.type() == EntryType.CREDIT);
        if (!hasDebit || !hasCredit) {
            throw new IllegalArgumentException("Transaction must have at least one DEBIT and one CREDIT");
        }

        for (var e : entries) {
            if (e.accountId() == null) throw new IllegalArgumentException("Entry accountId is required");
            if (e.type() == null) throw new IllegalArgumentException("Entry type is required");
            if (e.amount() == null || e.amount().signum() <= 0) throw new IllegalArgumentException("Amounts must be positive");
        }

        var debits = entries.stream()
                .filter(e -> e.type() == EntryType.DEBIT)
                .map(EntryDraft::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var credits = entries.stream()
                .filter(e -> e.type() == EntryType.CREDIT)
                .map(EntryDraft::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (debits.compareTo(credits) != 0) {
            throw new IllegalArgumentException("Total debits must equal total credits");
        }
    }

    public record EntryDraft(UUID accountId, EntryType type, BigDecimal amount) {}
}
