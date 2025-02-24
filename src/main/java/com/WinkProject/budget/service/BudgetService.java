package com.WinkProject.budget.service;

import com.WinkProject.budget.domain.Budget;
import com.WinkProject.budget.domain.BudgetDetail;
import com.WinkProject.budget.dto.request.*;
import com.WinkProject.budget.dto.response.AdjustmentResponse;
import com.WinkProject.budget.dto.response.BudgetResponse;
import com.WinkProject.budget.repository.BudgetDetailRepository;
import com.WinkProject.budget.repository.BudgetRepository;
import com.WinkProject.meeting.domain.Meeting;
import com.WinkProject.meeting.dto.request.MeetingCreateRequest;
import com.WinkProject.meeting.repository.MeetingRepository;
import com.WinkProject.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {
    private final MeetingRepository meetingRepository;
    private final BudgetRepository budgetRepository;

    public Budget initBudget(Long groupId, MeetingCreateRequest request){
        Meeting meeting = meetingRepository.findById(groupId)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Meeting not found"));
        Budget budget = new Budget();
        budget.setMeeting(meeting);
        budget.setTotalAmount(0L);
        budget.setKakaoRemitLink(request.getSettlement().getKakaoPayString());
        budget.setTossRemitLink(request.getSettlement().getTossPayString());
        budget.setAccountNumber(request.getSettlement().getAccountNumber());
        budget.setBank(request.getSettlement().getBank());
        budget.setDetails(new ArrayList<>());

        return budget;

    }

    public Long addHistory(Long groupId , HistoryRequest history){
        Budget budget = budgetRepository.findByMeetingId(groupId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Meeting not found"));
        BudgetDetail newDetail = new BudgetDetail();
        newDetail.setAmount(history.getAmount());
        newDetail.setCategory(history.getCategory());
        newDetail.setDescription(history.getDescription());
        newDetail.setBudget(budget);

        //잔고 업데이트
        Long balance = budget.getTotalAmount()+history.getAmount();
        budget.setTotalAmount(balance);
        budget.getDetails().add(newDetail);
        budgetRepository.save(budget);

        return balance;
    }

    public Long modifyHistory(Long groupId ,Long transactionId, HistoryRequest history){
        Budget budget = budgetRepository.findByMeetingId(groupId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));
        BudgetDetail budgetDetail = budget.getDetails().stream().filter(detail ->detail.getId().equals(transactionId)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        budget.setTotalAmount(budget.getTotalAmount() - budgetDetail.getAmount());


        budgetDetail.setCategory(history.getCategory());
        budgetDetail.setAmount(history.getAmount());
        budgetDetail.setDescription(history.getDescription());

        budget.setTotalAmount(budget.getTotalAmount() + history.getAmount());
        budgetRepository.save(budget);
        return budget.getTotalAmount();
    }

    public void deleteHistory(Long groupId, Long transactionId){
        Budget budget = budgetRepository.findByMeetingId(groupId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));
        BudgetDetail detailToRemove = budget.getDetails().stream()
                .filter(detail -> detail.getId().equals(transactionId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        budget.getDetails().remove(detailToRemove);


        budget.setTotalAmount(budget.getTotalAmount() - detailToRemove.getAmount());
        budget.getDetails().remove(detailToRemove);
        budgetRepository.save(budget);
    }

    public void addKakaoUrl(Long groupId , Kakao kakao){
        Budget budget = budgetRepository.findByMeetingId(groupId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));
        budget.setKakaoRemitLink(kakao.getKakaoUrl());
        budgetRepository.save(budget);
    }

    public void addTossUrl(Long groupId , Toss toss){
        Budget budget = budgetRepository.findByMeetingId(groupId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));
        budget.setTossRemitLink(toss.getTossUrl());
        budgetRepository.save(budget);
    }

    public void addBankAccount(Long groupId, BankAccount bankAccount){
        Budget budget = budgetRepository.findByMeetingId(groupId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));
        budget.setAccountNumber(bankAccount.getBankAccountNumber());
        budgetRepository.save(budget);
    }

    public void addBankName(Long groupId , BankName bankName){
        Budget budget = budgetRepository.findByMeetingId(groupId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));
        budget.setBank(bankName.getBankName());
        budgetRepository.save(budget);
    }

    public AdjustmentResponse getInfo(Long groupId){
        Meeting meeting = meetingRepository.findById(groupId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Meeting not found"));
        List<Long> member = meeting.getMembers().stream().map(Member::getId).toList();

        Budget budget = budgetRepository.findByMeetingId(groupId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));

        return AdjustmentResponse.builder()
                .accountNumber(budget.getAccountNumber())
                .tossUrl(budget.getTossRemitLink())
                .kakaoUrl(budget.getKakaoRemitLink())
                .bankName(budget.getBank())
                .memberID(member)
                .build();

    }
}
