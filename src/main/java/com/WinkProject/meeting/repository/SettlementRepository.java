package com.WinkProject.meeting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.WinkProject.meeting.domain.Settlement;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
} 