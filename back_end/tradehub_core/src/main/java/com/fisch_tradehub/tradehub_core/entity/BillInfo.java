package com.fisch_tradehub.tradehub_core.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "bill_info",
    indexes = {
        @Index(name = "bill_info_bill_idx", columnList = "bill_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "bill_info_bill_fish_unique",
            columnNames = { "bill_id", "fish_id" }
        )
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // N line items thuộc về 1 bill
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "bill_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "bill_info_bill_fk")
    )
    private Bill bill;

    // Mỗi line item chỉ trỏ đến 1 fish cụ thể
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "fish_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "bill_info_fish_fk")
    )
    private Fish fish;

    // Giá tại thời điểm mua
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer amount;

    // price * amount tại thời điểm tạo bill
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal sum;
}