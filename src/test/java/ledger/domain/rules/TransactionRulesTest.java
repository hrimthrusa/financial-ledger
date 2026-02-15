package ledger.domain.rules;

import ledger.domain.models.EntryType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionRulesTest {

    @Test
    void validTransaction_passes() {
        TransactionRules.validate(
                "Purchase",
                OffsetDateTime.parse("2024-01-15T10:30:00Z"),
                List.of(
                        new TransactionRules.EntryDraft(UUID.randomUUID(), EntryType.DEBIT, new BigDecimal("100.00")),
                        new TransactionRules.EntryDraft(UUID.randomUUID(), EntryType.CREDIT, new BigDecimal("100.00"))
                )
        );
    }

    @Test
    void rejects_whenDebitsNotEqualCredits() {
        var ex = assertThrows(IllegalArgumentException.class, () ->
                TransactionRules.validate(
                        "Bad",
                        OffsetDateTime.parse("2024-01-15T10:30:00Z"),
                        List.of(
                                new TransactionRules.EntryDraft(UUID.randomUUID(), EntryType.DEBIT, new BigDecimal("100.00")),
                                new TransactionRules.EntryDraft(UUID.randomUUID(), EntryType.CREDIT, new BigDecimal("90.00"))
                        )
                )
        );

        assertEquals("Total debits must equal total credits", ex.getMessage());
    }

    @Test
    void rejects_whenLessThanTwoEntries() {
        var ex = assertThrows(IllegalArgumentException.class, () ->
                TransactionRules.validate(
                        "Too small",
                        OffsetDateTime.parse("2024-01-15T10:30:00Z"),
                        List.of(new TransactionRules.EntryDraft(UUID.randomUUID(), EntryType.DEBIT, new BigDecimal("1.00")))
                )
        );

        assertEquals("Transaction must have at least 2 entries", ex.getMessage());
    }
}
