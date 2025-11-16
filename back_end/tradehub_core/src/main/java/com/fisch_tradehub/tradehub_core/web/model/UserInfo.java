package com.fisch_tradehub.tradehub_core.web.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfo {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private String fullname;

    private Short age;      // TINYINT UNSIGNED -> dùng Short cho an toàn

    private Boolean gender; // true/false hoặc 1/0, tùy bạn quy ước
}