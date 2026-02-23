package ledger.application.services;

import ledger.domain.models.AccountType;
import ledger.domain.rules.BalanceRules;
import ledger.api.dtos.AccountDto;
import ledger.persistence.entities.Account;
import ledger.api.exceptions.BusinessRuleViolationException;
import ledger.api.exceptions.NotFoundException;
import ledger.persistence.repositories.AccountRepository;
import ledger.persistence.repositories.EntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final EntryRepository entryRepository;

    public AccountService(AccountRepository accountRepository, EntryRepository entryRepository) {
        this.accountRepository = accountRepository;
        this.entryRepository = entryRepository;
    }

    @Transactional
    public AccountDto.AccountResponse create(AccountDto.CreateAccountRequest req) {
        if (accountRepository.existsByNameIgnoreCase(req.name().trim())) {
            throw new BusinessRuleViolationException("Account name must be unique");
        }
        var entity = new Account(UUID.randomUUID(), req.name().trim(), req.type(), Instant.now());
        accountRepository.save(entity);

        return toResponse(entity, BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public List<AccountDto.AccountResponse> getAll() {
        return accountRepository.findAll().stream()
                .map(a -> toResponse(a, calculateBalance(a.getId(), a.getType())))
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountDto.AccountResponse getById(UUID id) {
        var account = accountRepository.findById(id).orElseThrow(() -> new NotFoundException("Account not found: " + id));
        return toResponse(account, calculateBalance(account.getId(), account.getType()));
    }

    private BigDecimal calculateBalance(UUID accountId, AccountType type) {
        return entryRepository.findAllByAccountId(accountId).stream()
                .map(e -> BalanceRules.signed(type, e.getEntryType(), e.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static AccountDto.AccountResponse toResponse(Account account, BigDecimal balance) {
        return new AccountDto.AccountResponse(account.getId(), account.getName(), account.getType(), balance);
    }
}

