package ledger.api.controllers;

import jakarta.validation.Valid;
import ledger.api.dtos.TransactionDto;
import ledger.application.services.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionDto.TransactionResponse create(@Valid @RequestBody TransactionDto.CreateTransactionRequest req) {
        return transactionService.create(req);
    }

    @GetMapping("/transactions/{id}")
    public TransactionDto.TransactionResponse getById(@PathVariable UUID id) {
        return transactionService.getById(id);
    }

    @GetMapping("/accounts/{id}/transactions")
    public List<TransactionDto.TransactionResponse> getAccount(@PathVariable UUID id) {
        return transactionService.getAccount(id);
    }
}
