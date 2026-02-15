package ledger.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    private UUID id;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(nullable = false)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Transaction() {}

    public Transaction(UUID id, Instant occurredAt, String description, Instant createdAt) {
        this.id = id;
        this.occurredAt = occurredAt;
        this.description = description;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
}
