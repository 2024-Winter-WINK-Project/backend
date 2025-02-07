package com.WinkProject.auth.repository;

import com.WinkProject.auth.schema.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AuthRepository extends JpaRepository<Auth,Long> {
}
