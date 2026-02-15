package ledger.application.services;

import ledger.api.dtos.TransactionDto;
import ledger.api.exceptions.BusinessRuleViolationException;
import ledger.api.exceptions.NotFoundException;
import ledger.domain.rules.TransactionRules;
import ledger.persistence.entities.Transaction;
import ledger.persistence.entities.TransactionEntry;
import ledger.persistence.repositories.AccountRepository;
import ledger.persistence.repositories.EntryRepository;
import ledger.persistence.repositories.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final EntryRepository entryRepository;

    public TransactionService(AccountRepository accountRepository,
                              TransactionRepository transactionRepository,
                              EntryRepository entryRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.entryRepository = entryRepository;
    }

    @Transactional
    public TransactionDto.TransactionResponse create(TransactionDto.CreateTransactionRequest req) {
        try {
            TransactionRules.validate(
                    req.description(),
                    req.date(),
                    req.entries().stream()
                            .map(e -> new TransactionRules.EntryDraft(e.accountId(), e.type(), e.amount()))
                            .toList()
            );
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleViolationException(ex.getMessage());
        }

        var accountIds = req.entries().stream().map(TransactionDto.EntryRequest::accountId).collect(Collectors.toSet());
        for (UUID id : accountIds) {
            if (!accountRepository.existsById(id)) {
                throw new NotFoundException("Account not found: " + id);
            }
        }

        var txId = UUID.randomUUID();
        var occurredAt = req.date().toInstant();
        var tx = new Transaction(txId, occurredAt, req.description().trim(), Instant.now());
        transactionRepository.save(tx);

        var entryEntities = req.entries().stream()
                .map(e -> new TransactionEntry(
                        UUID.randomUUID(),
                        txId,
                        e.accountId(),
                        e.type(),
                        e.amount(),
                        Instant.now()
                ))
                .toList();

        entryRepository.saveAll(entryEntities);

        return getById(txId);
    }

    @Transactional(readOnly = true)
    public TransactionDto.TransactionResponse getById(UUID id) {
        var tx = transactionRepository.findById(id).orElseThrow(() -> new NotFoundException("Transaction not found: " + id));
        var entryEntities = entryRepository.findByTransactionId(id);

        return new TransactionDto.TransactionResponse(
                tx.getId(),
                tx.getDescription(),
                tx.getOccurredAt(),
                entryEntities.stream().map(e -> new TransactionDto.EntryResponse(
                        e.getId(), e.getAccountId(), e.getEntryType(), e.getAmount()
                )).toList()
        );
    }

    @Transactional(readOnly = true)
    public List<TransactionDto.TransactionResponse> getAccount(UUID accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new NotFoundException("Account not found: " + accountId);
        }

        var txIds = entryRepository.findAllByAccountId(accountId).stream()
                .map(TransactionEntry::getTransactionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return txIds.stream().map(this::getById).toList();
    }
}
