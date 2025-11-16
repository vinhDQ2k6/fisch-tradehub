package com.fisch_tradehub.tradehub_core.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fisch_tradehub.tradehub_core.web.model.UserInfo;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
  
}