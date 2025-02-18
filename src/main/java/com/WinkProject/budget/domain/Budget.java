package com.WinkProject.budget.domain;

import com.WinkProject.meeting.domain.Meeting;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "예산")
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meetingId")
    private Meeting meeting;

    private Long totalAmount;
    
    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<BudgetDetail> details = new ArrayList<>();

    private String kakaoRemitLink;
    private String tossRemitLink;
    private String accountNumber;

}