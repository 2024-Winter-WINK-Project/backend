package com.WinkProject.meeting.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.WinkProject.meeting.domain.Meeting;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    @Query("SELECT m FROM Meeting m " +
            "JOIN m.members mem " +
            "WHERE mem.auth.id = :userId " +
            "AND mem.isWithdrawn = false " +
            "ORDER BY m.startTime DESC " +
            "LIMIT :limit")
    List<Meeting> findLatestMeetings(@Param("userId") Long userId, @Param("limit") int limit);

    @Query("SELECT DISTINCT m FROM Meeting m " +
            "JOIN FETCH m.members mem " +
            "WHERE mem.auth.id = :userId " +
            "AND mem.isWithdrawn = false " +
            "ORDER BY m.startTime DESC")
    List<Meeting> findMeetingsByUserId(@Param("userId") Long userId);
} 