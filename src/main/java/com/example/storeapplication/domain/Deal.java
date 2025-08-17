package com.example.storeapplication.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "deals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Deal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    private Product product;

    @Column(nullable = false)
    private String description;

    @Column(name = "buy_quantity", nullable = false)
    private int buyQuantity;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "get_quantity")
    private Integer getQuantity;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expirationDate != null && LocalDateTime.now().isAfter(expirationDate);
    }

    // Custom constructor for business logic
    public Deal(Product product, String description, int buyQuantity,
                BigDecimal discountPercentage, LocalDateTime expirationDate) {
        this.product = product;
        this.description = description;
        this.buyQuantity = buyQuantity;
        this.discountPercentage = discountPercentage;
        this.expirationDate = expirationDate;
        this.active = true;
    }
}
