package com.WinkProject.budget.repository;

import com.WinkProject.budget.domain.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget,Long> {
    @Query(value = "SELECT * FROM 예산 WHERE meeting_id = :meetingId", nativeQuery = true)
    Optional<Budget> findByMeetingId(@Param("meetingId") Long meetingId);
}

