package com.campusfind.repository;

import com.campusfind.entity.Claim;
import com.campusfind.entity.Report;
import com.campusfind.entity.User;
import com.campusfind.entity.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByClaimantOrderByCreatedAtDesc(User claimant);

    List<Claim> findByReportOrderByCreatedAtDesc(Report report);

    void deleteByReport(Report report);

    boolean existsByReportAndClaimant(Report report, User claimant);

    @Query("SELECT c FROM Claim c WHERE c.report.user = :finder ORDER BY c.createdAt DESC")
    List<Claim> findClaimsForFinder(@Param("finder") User finder);

    long countByStatus(ClaimStatus status);
}
