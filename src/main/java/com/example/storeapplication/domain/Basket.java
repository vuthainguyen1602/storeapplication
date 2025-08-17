package com.example.storeapplication.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Entity
@Table(name = "baskets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Basket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @OneToMany(mappedBy = "basket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<BasketItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Thread-safe operations using concurrent map
    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final ConcurrentMap<Long, BasketItem> itemsMap = new ConcurrentHashMap<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Custom constructor for business logic
    public Basket(String sessionId) {
        this.sessionId = sessionId;
        this.items = new ArrayList<>();
    }

    // Thread-safe basket operations
    public synchronized void addItem(Product product, int quantity) {
        BasketItem existingItem = itemsMap.get(product.getId());
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            BasketItem newItem = new BasketItem(this, product, quantity);
            items.add(newItem);
            itemsMap.put(product.getId(), newItem);
        }
    }

    public synchronized boolean removeItem(Long productId, int quantity) {
        BasketItem item = itemsMap.get(productId);
        if (item != null) {
            if (item.getQuantity() <= quantity) {
                items.remove(item);
                itemsMap.remove(productId);
                return true;
            } else {
                item.setQuantity(item.getQuantity() - quantity);
                return true;
            }
        }
        return false;
    }
}
