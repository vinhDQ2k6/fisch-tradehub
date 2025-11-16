package com.fisch_tradehub.tradehub_core.web.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity mapping for table `bill_info`
 *
 * CREATE TABLE `bill_info` (
 *   `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
 *   `bill_id` BIGINT UNSIGNED NOT NULL,
 *   `fish_id` BIGINT UNSIGNED NOT NULL,
 *   `price` DECIMAL(10, 2) NOT NULL,
 *   `amount` INT NOT NULL,
 *   `sum` DECIMAL(10, 2) NOT NULL,
 *   CONSTRAINT `bill_info_bill_fk` FOREIGN KEY (`bill_id`) REFERENCES `bill` (`id`),
 *   CONSTRAINT `bill_info_fish_fk` FOREIGN KEY (`fish_id`) REFERENCES `fish` (`id`),
 *   KEY `bill_info_bill_idx` (`bill_id`),
 *   UNIQUE KEY `bill_info_bill_fish_unique` (`bill_id`, `fish_id`)
 * );
 */
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