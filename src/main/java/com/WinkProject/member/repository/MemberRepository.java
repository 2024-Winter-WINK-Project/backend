package com.WinkProject.member.repository;

import com.WinkProject.meeting.domain.Meeting;
import com.WinkProject.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    @Query("SELECT m.meeting FROM Member m WHERE m.auth.id = :userId")
    List<Meeting> findMeetingsByUserId(@Param("userId") Long userId);

    Optional<Member> findByAuthId(Long authId);
} 