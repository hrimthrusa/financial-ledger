package ledger.domain.rules;

import ledger.domain.models.AccountType;
import ledger.domain.models.EntryType;

import java.math.BigDecimal;

public final class BalanceRules {
    private BalanceRules() {}

    public static BigDecimal signed(AccountType accountType, EntryType entryType, BigDecimal amount) {
        int sign = switch (accountType) {
            case ASSET, EXPENSE -> (entryType == EntryType.DEBIT) ? +1 : -1;
            case LIABILITY, REVENUE -> (entryType == EntryType.CREDIT) ? +1 : -1;
        };
        return amount.multiply(BigDecimal.valueOf(sign));
    }
}
