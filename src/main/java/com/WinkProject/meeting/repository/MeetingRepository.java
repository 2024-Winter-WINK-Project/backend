package com.winkproject.meeting.repository;

import com.winkproject.meeting.domain.Meeting;
import com.winkproject.meeting.dto.response.MeetingResponse;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    @Deprecated
    @Query("SELECT m FROM Meeting m JOIN m.members mem WHERE mem.auth.id = :userId ORDER BY m.createdAt DESC")
    List<Meeting> findLatestMeetings(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT new com.winkproject.meeting.dto.response.MeetingResponse(m.id, m.name, m.createdAt) " +
       "FROM Meeting m JOIN m.members mem WHERE mem.auth.id = :userId ORDER BY m.createdAt DESC")
    List<MeetingResponse> findLatestMeetingDTOs(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT m FROM Meeting m JOIN m.members mem WHERE mem.auth.id = :userId")
    List<Meeting> findMeetingsByUserId(@Param("userId") Long userId);
} 