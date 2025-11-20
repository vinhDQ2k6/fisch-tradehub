package com.fisch_tradehub.tradehub_core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fisch_tradehub.tradehub_core.entity.Fish;

public interface FishRepository extends JpaRepository<Fish, Long> {

}