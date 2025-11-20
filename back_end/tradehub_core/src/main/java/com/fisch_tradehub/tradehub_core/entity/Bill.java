package com.fisch_tradehub.tradehub_core.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "bill",
    indexes = {
        @Index(name = "bill_buyer_idx", columnList = "buyer_id"),
        @Index(name = "bill_seller_idx", columnList = "seller_id"),
        @Index(name = "bill_status_idx", columnList = "status")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Buyer: luôn phải có
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "buyer_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "bill_buyer_fk")
    )
    private User buyer;

    // Seller: có thể null (tùy logic hệ thống)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "seller_id",
        foreignKey = @ForeignKey(name = "bill_seller_fk")
    )
    private User seller;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    // 0 = pending, 1 = processing, 2 = completed
    @Column(nullable = false)
    @Convert(converter = BillStatusConverter.class)
    private BillStatus status;

    @Column(name = "created_at", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    // Có thể null nếu chưa rating
    private Short rating;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}