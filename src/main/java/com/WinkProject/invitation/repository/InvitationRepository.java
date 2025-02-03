package com.WinkProject.invitation.repository;

import com.WinkProject.invitation.domain.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    boolean existsByInviteCode(String inviteCode);

    Optional<Invitation> findByMeetingIdAndInviteCode(Long meetingId, String inviteCode);

    Optional<Invitation> findByInviteCode(String inviteCode);

    Optional<Invitation> findByMeetingId(Long meetingId);

    @Modifying
    @Query("DELETE FROM Invitation i WHERE i.expiresAt < :now")
    void deleteAllExpired(@Param("now") LocalDateTime now);
} 