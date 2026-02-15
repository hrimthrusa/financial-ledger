package ledger.api.controllers;

import ledger.api.dtos.AccountDto;
import ledger.application.services.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accounts;

    public AccountController(AccountService accounts) {
        this.accounts = accounts;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDto.AccountResponse create(@Valid @RequestBody AccountDto.CreateAccountRequest req) {
        return accounts.create(req);
    }

    @GetMapping
    public List<AccountDto.AccountResponse> getAll() {
        return accounts.getAll();
    }

    @GetMapping("/{id}")
    public AccountDto.AccountResponse getById(@PathVariable UUID id) {
        return accounts.getById(id);
    }
}
