package ledger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class LedgerApiIT extends AbstractIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void createAccount_thenGetAccountWithZeroBalance() throws Exception {
        var create = Map.of("name", "Cash", "type", "ASSET");

        var res = mvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.balance").value(0))
                .andReturn();

        var id = mapper.readTree(res.getResponse().getContentAsString()).get("id").asText();

        mvc.perform(get("/api/accounts/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void rejectTransaction_whenDebitsNotEqualCredits() throws Exception {
        var acc1 = createAccount("Cash", "ASSET");
        var acc2 = createAccount("Supplies", "EXPENSE");

        var tx = Map.of(
                "description", "Bad tx",
                "date", "2024-01-15T10:30:00Z",
                "entries", new Object[] {
                        Map.of("accountId", acc1, "type", "CREDIT", "amount", 100.00),
                        Map.of("accountId", acc2, "type", "DEBIT", "amount", 90.00)
                }
        );

        mvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(tx)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void balanceCalculatedCorrectly_afterValidTransactions() throws Exception {
        var cash = createAccount("Cash2", "ASSET");
        var revenue = createAccount("Revenue", "REVENUE");

        // revenue +100 (CREDIT), cash +100 (DEBIT)
        var tx = Map.of(
                "description", "Sale",
                "date", "2024-01-15T10:30:00Z",
                "entries", new Object[] {
                        Map.of("accountId", cash, "type", "DEBIT", "amount", 100.00),
                        Map.of("accountId", revenue, "type", "CREDIT", "amount", 100.00)
                }
        );

        mvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(tx)))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/accounts/" + cash))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.00));

        mvc.perform(get("/api/accounts/" + revenue))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    private String createAccount(String name, String type) throws Exception {
        var uniqueName = name + "-" + UUID.randomUUID();

        var body = Map.of("name", uniqueName, "type", type);

        var res = mvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();

        return mapper.readTree(res.getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    void balanceAccumulatedAfterMultipleTransactions() throws Exception {
        var cash = createAccount("Cash", "ASSET");
        var revenue = createAccount("Revenue", "REVENUE");

        var tx1 = Map.of(
                "description", "Sale1",
                "date", "2024-01-15T10:30:00Z",
                "entries", new Object[]{
                        Map.of("accountId", cash, "type", "DEBIT", "amount", 100.00),
                        Map.of("accountId", revenue, "type", "CREDIT", "amount", 100.00)
                }
        );

        var tx2 = Map.of(
                "description", "Sale2",
                "date", "2024-01-16T10:30:00Z",
                "entries", new Object[]{
                        Map.of("accountId", cash, "type", "DEBIT", "amount", 50.00),
                        Map.of("accountId", revenue, "type", "CREDIT", "amount", 50.00)
                }
        );

        mvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(tx1)))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(tx2)))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/accounts/" + cash))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(150.00));
    }

    @Test
    void createTransaction_withUnknownAccount_returns404() throws Exception {
        var tx = Map.of(
                "description", "Bad",
                "date", "2024-01-15T10:30:00Z",
                "entries", new Object[]{
                        Map.of("accountId", UUID.randomUUID(), "type", "DEBIT", "amount", 100.00),
                        Map.of("accountId", UUID.randomUUID(), "type", "CREDIT", "amount", 100.00)
                }
        );

        mvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(tx)))
                .andExpect(status().isNotFound());
    }
}
