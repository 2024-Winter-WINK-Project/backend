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
                        Math.abs(detail.getAmount()),
                        detail.getDescription()
            )).collect(Collectors.toCollection(ArrayList::new))
                    : new ArrayList<>()
                    );
            return ResponseEntity.ok(budgetResponse);
        }
    }

    @PostMapping("/{groupId}/ledger/transactions")
    public ResponseEntity<BalanceResponse> addExpense(@PathVariable("groupId") Long groupId, @RequestBody HistoryRequest history){
        Long balance = budgetService.addHistory(groupId,history);
        return ResponseEntity.ok(BalanceResponse.builder().balance(balance).build());
    }

    @PutMapping("/{groupId}/ledger/transactions/{transactionId}")
    public ResponseEntity<BalanceResponse> modifyExpense(@PathVariable("groupId") Long groupId, @PathVariable("transactionId") Long transactionId,@RequestBody HistoryRequest history){
        Long balance = budgetService.modifyHistory(groupId,transactionId,history);
        return ResponseEntity.ok(BalanceResponse.builder().balance(balance).build());
    }

    @DeleteMapping("/{groupId}/ledger/transactions/{transactionId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable("groupId") Long groupId, @PathVariable("transactionId") Long transactionId){
        budgetService.deleteHistory(groupId,transactionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{groupId}/transfer/kakao")
    public ResponseEntity<Void> kakaoUrl(@PathVariable("groupId") Long groupId, @RequestBody Kakao kakao){
        budgetService.addKakaoUrl(groupId,kakao);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{groupId}/transfer/toss")
    public ResponseEntity<Void> tossUrl(@PathVariable("groupId") Long groupId, @RequestBody Toss toss){
        budgetService.addTossUrl(groupId,toss);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{groupId}/transfer/bank-account-number")
    public ResponseEntity<Void> bankAccountNum(@PathVariable("groupId") Long groupId, @RequestBody BankAccount bankAccount){
        budgetService.addBankAccount(groupId,bankAccount);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{groupId}/transfer")
    public ResponseEntity<AdjustmentResponse> adjustment(@PathVariable("groupId")Long groupId){
        AdjustmentResponse response = budgetService.getInfo(groupId);
        return ResponseEntity.ok(response);
    }

}
