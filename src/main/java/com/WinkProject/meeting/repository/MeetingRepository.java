package com.WinkProject.meeting.repository;

import com.WinkProject.meeting.domain.Meeting;
import com.WinkProject.meeting.dto.response.MeetingResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    @Query("SELECT m FROM Meeting m " +
            "JOIN m.members mem " +
            "WHERE mem.auth.id = :userId " +
            "AND mem.isWithdrawn = false " +
            "ORDER BY m.startTime DESC")
    List<Meeting> findLatestMeetings(@Param("userId") Long userId, Pageable pageable);

//    @Query("SELECT new com.winkproject.meeting.dto.response.MeetingResponse(" +
//            "m.id, m.name, m.description, " +
//            "m.maxParticipants, m.currentParticipants, " +
//            "m.startTime, m.endTime, " +
//            "m.place.name, " +
//            "m.createdAt, m.updatedAt) " +
//            "FROM Meeting m " +
//            "JOIN m.members mem " +
//            "WHERE mem.auth.id = :userId " +
//            "AND mem.isWithdrawn = false " +
//            "ORDER BY m.startTime DESC")
    @Deprecated
    List<MeetingResponse> findLatestMeetingDTOs(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT DISTINCT m FROM Meeting m " +
            "JOIN FETCH m.members mem " +
            "WHERE mem.auth.id = :userId " +
            "AND mem.isWithdrawn = false " +
            "ORDER BY m.startTime DESC")
    List<Meeting> findMeetingsByUserId(@Param("userId") Long userId);
} 