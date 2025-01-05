package com.WinkProject.budget.repository;

import com.WinkProject.budget.domain.BudgetDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetDetailRepository extends JpaRepository<BudgetDetail,Long> {
}
