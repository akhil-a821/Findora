package com.campusfind.repository;

import com.campusfind.entity.Match;
import com.campusfind.entity.Report;
import com.campusfind.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    boolean existsByLostReportAndFoundReport(Report lostReport, Report foundReport);

    Optional<Match> findByLostReportAndFoundReport(Report lostReport, Report foundReport);

    void deleteByLostReportOrFoundReport(Report lostReport, Report foundReport);

    @Query("SELECT m FROM Match m WHERE m.lostReport.user = :user OR m.foundReport.user = :user ORDER BY m.score DESC, m.createdAt DESC")
    List<Match> findMatchesForUser(@Param("user") User user);

    @Query("SELECT m FROM Match m WHERE m.lostReport.id = :reportId OR m.foundReport.id = :reportId ORDER BY m.score DESC")
    List<Match> findMatchesByReportId(@Param("reportId") Long reportId);

    List<Match> findAllByOrderByCreatedAtDesc();

    long countByScoreGreaterThanEqual(int minScore);

    @Query("SELECT COUNT(m) FROM Match m WHERE m.lostReport.user = :user OR m.foundReport.user = :user")
    long countByUser(@Param("user") User user);
}
