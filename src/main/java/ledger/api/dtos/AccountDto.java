package ledger.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ledger.domain.models.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

public final class AccountDto {
    private AccountDto() {}

    public record CreateAccountRequest(
            @NotBlank String name,
            @NotNull AccountType type
    ) {}

    public record AccountResponse(
            UUID id,
            String name,
            AccountType type,
            BigDecimal balance
    ) {}
}
