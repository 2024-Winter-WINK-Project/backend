package com.WinkProject.auth.repository;

import com.WinkProject.auth.schema.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Controller;

@Controller
public interface MemberRepository extends JpaRepository<Member,Long> {
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Member m WHERE m.socialId = :socialId")
    boolean existsBySocialId(Long socialId);
}
