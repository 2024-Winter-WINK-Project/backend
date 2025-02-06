package com.WinkProject.budget.controller;


import com.WinkProject.budget.domain.Budget;
import com.WinkProject.budget.dto.request.BankAccount;
import com.WinkProject.budget.dto.request.HistoryRequest;
import com.WinkProject.budget.dto.request.Kakao;
import com.WinkProject.budget.dto.request.Toss;
import com.WinkProject.budget.dto.response.AdjustmentResponse;
import com.WinkProject.budget.dto.response.BalanceResponse;
import com.WinkProject.budget.dto.response.BudgetDetailResponse;
import com.WinkProject.budget.dto.response.BudgetResponse;
import com.WinkProject.budget.repository.BudgetRepository;
import com.WinkProject.budget.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
@Slf4j
public class BudgetController {
    private final BudgetRepository budgetRepository;
    private final BudgetService budgetService;

    @GetMapping("/{groupId}/ledger")
    @Operation(summary = "모임 가계부 조회",description = "가계부의 상세 내용을 볼 때 사용하는 api")
    public ResponseEntity<BudgetResponse> showBudget(@PathVariable("groupId") Long groupID){
        Budget budget = budgetRepository.findByMeetingId(groupID).orElse(null);

        if (budget == null){
            BudgetResponse budgetResponse = budgetService.initBudget(groupID);
            return ResponseEntity.ok(budgetResponse);
        }
        else{
            BudgetResponse budgetResponse = new BudgetResponse();
            budgetResponse.setTotalAmount(budget.getTotalAmount());
            budgetResponse.setDetails(budget.getDetails() != null
                    ? budget.getDetails().stream().map((detail)-> new BudgetDetailResponse(
                        detail.getCategory(),
                            detail.getAmount() >= 0,
                        Math.abs(detail.getAmount()),
                        detail.getDescription()
            )).collect(Collectors.toCollection(ArrayList::new))
                    : new ArrayList<>()
                    );
            return ResponseEntity.ok(budgetResponse);
        }
    }

    @PostMapping("/{groupId}/ledger/transactions")
    @Operation(summary = "지출/수입 내역 추가",description = "내역 추가 하는 api")
    public ResponseEntity<BalanceResponse> addExpense(@PathVariable("groupId") Long groupId, @RequestBody HistoryRequest history){
        Long balance = budgetService.addHistory(groupId,history);
        return ResponseEntity.ok(BalanceResponse.builder().balance(balance).build());
    }

    @PutMapping("/{groupId}/ledger/transactions/{transactionId}")
    @Operation(summary = "지출/수입 내역 수정",description = "내역 수정 하는 api")
    public ResponseEntity<BalanceResponse> modifyExpense(@PathVariable("groupId") Long groupId, @PathVariable("transactionId") Long transactionId,@RequestBody HistoryRequest history){
        Long balance = budgetService.modifyHistory(groupId,transactionId,history);
        return ResponseEntity.ok(BalanceResponse.builder().balance(balance).build());
    }

    @DeleteMapping("/{groupId}/ledger/transactions/{transactionId}")
    @Operation(summary = "지출/수입 내역 삭제",description = "내역 삭제 하는 api")
    public ResponseEntity<Void> deleteExpense(@PathVariable("groupId") Long groupId, @PathVariable("transactionId") Long transactionId){
        budgetService.deleteHistory(groupId,transactionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{groupId}/transfer/kakao")
    @Operation(summary = "카카오 송금 코드 등록",description = "카카오 송금 코드를 저장하는 api")
    public ResponseEntity<Void> kakaoUrl(@PathVariable("groupId") Long groupId, @RequestBody Kakao kakao){
        budgetService.addKakaoUrl(groupId,kakao);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{groupId}/transfer/toss")
    @Operation(summary = "토스 송금 코드 등록",description = "토스 송금 코드를 저장하는 api")
    public ResponseEntity<Void> tossUrl(@PathVariable("groupId") Long groupId, @RequestBody Toss toss){
        budgetService.addTossUrl(groupId,toss);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{groupId}/transfer/bank-account-number")
    @Operation(summary = "계좌번호 등록",description = "계좌번호를 저장하는 api")
    public ResponseEntity<Void> bankAccountNum(@PathVariable("groupId") Long groupId, @RequestBody BankAccount bankAccount){
        budgetService.addBankAccount(groupId,bankAccount);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{groupId}/transfer")
    @Operation(summary = "등록된 송금코드 조회",description = "해당 모임에 등록된 송금코드를 조회하는 api")
    public ResponseEntity<AdjustmentResponse> adjustment(@PathVariable("groupId")Long groupId){
        AdjustmentResponse response = budgetService.getInfo(groupId);
        return ResponseEntity.ok(response);
    }

}
