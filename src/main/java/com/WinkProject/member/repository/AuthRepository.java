package com.winkproject.member.repository;

import com.winkproject.member.domain.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthRepository extends JpaRepository<Auth, Long> {
} 