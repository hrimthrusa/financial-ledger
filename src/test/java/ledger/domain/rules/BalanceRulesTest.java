package ledger.domain.rules;

import ledger.domain.models.AccountType;
import ledger.domain.models.EntryType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BalanceRulesTest {

    @Test
    void assetDebit_increases() {
        var v = BalanceRules.signed(AccountType.ASSET, EntryType.DEBIT, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("100.00"), v);
    }

    @Test
    void assetCredit_decreases() {
        var v = BalanceRules.signed(AccountType.ASSET, EntryType.CREDIT, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("-100.00"), v);
    }

    @Test
    void revenueCredit_increases() {
        var v = BalanceRules.signed(AccountType.REVENUE, EntryType.CREDIT, new BigDecimal("50.00"));
        assertEquals(new BigDecimal("50.00"), v);
    }
}
