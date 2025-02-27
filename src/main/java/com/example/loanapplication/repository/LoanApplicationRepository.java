package com.example.loanapplication.repository;

import com.example.loanapplication.model.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    LoanApplication findByProcessInstanceId(String processInstanceId);
}
