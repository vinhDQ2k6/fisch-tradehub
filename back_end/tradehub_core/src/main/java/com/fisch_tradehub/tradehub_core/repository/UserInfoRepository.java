package com.fisch_tradehub.tradehub_core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fisch_tradehub.tradehub_core.entity.UserInfo;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {

}