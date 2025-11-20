package com.fisch_tradehub.tradehub_core.entity;

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
    name = "cart",
    indexes = {
        @Index(name = "cart_user_idx", columnList = "user_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "cart_user_fish_unique",
            columnNames = { "user_id", "fish_id" }
        )
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "cart_user_fk")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "fish_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "cart_fish_fk")
    )
    private Fish fish;

    @Column(nullable = false)
    private Integer quantity;
}