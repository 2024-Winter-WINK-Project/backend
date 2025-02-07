package com.WinkProject.meeting.domain;

import com.WinkProject.meeting.dto.request.MeetingCreateRequest.SettlementRequest;
import io.micrometer.common.lang.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Settlement {
    @Id
    private Long meetingId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    @Nullable
    private String kakaoPayString;
    @Nullable
    private String tossPayString;
    @Nullable
    private String accountNumber;

    public Settlement(Meeting meeting) {
        this.meeting = meeting;
        this.meetingId = meeting.getId();
    }

    public static Settlement from(SettlementRequest request, Meeting meeting) {
        return new Settlement(
            meeting.getId(),
            meeting,
            request.getKakaoPayString(),
            request.getTossPayString(),
            request.getAccountNumber()
        );
    }

    public void updateSettlement(SettlementRequest request) {   
        this.kakaoPayString = request.getKakaoPayString();
        this.tossPayString = request.getTossPayString();
        this.accountNumber = request.getAccountNumber();
    }
} 
