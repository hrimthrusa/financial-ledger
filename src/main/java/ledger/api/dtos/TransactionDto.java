package ledger.api.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ledger.domain.models.EntryType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class TransactionDto {
    private TransactionDto() {}

    public record EntryRequest(
            @NotNull UUID accountId,
            @NotNull EntryType type,
            @NotNull BigDecimal amount
    ) {}

    public record CreateTransactionRequest(
            @NotBlank String description,
            @NotNull OffsetDateTime date,
            @Valid @Size(min = 2) List<EntryRequest> entries
    ) {}

    public record EntryResponse(
            UUID id,
            UUID accountId,
            EntryType type,
            BigDecimal amount
    ) {}

    public record TransactionResponse(
            UUID id,
            String description,
            java.time.Instant occurredAt,
            List<EntryResponse> entries
    ) {}
}
