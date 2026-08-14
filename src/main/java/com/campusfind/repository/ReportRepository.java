package com.campusfind.repository;

import com.campusfind.entity.Report;
import com.campusfind.entity.User;
import com.campusfind.entity.enums.ReportStatus;
import com.campusfind.entity.enums.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByUserOrderByCreatedAtDesc(User user);

    List<Report> findByTypeOrderByCreatedAtDesc(ReportType type);

    List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    List<Report> findByTypeAndStatusOrderByCreatedAtDesc(ReportType type, ReportStatus status);

    long countByType(ReportType type);

    long countByStatus(ReportStatus status);

    long countByUserAndType(User user, ReportType type);

    @Query("SELECT r FROM Report r WHERE " +
           "(:type IS NULL OR r.type = :type) AND " +
           "(:category IS NULL OR r.category = :category) AND " +
           "(:location IS NULL OR r.location = :location) AND " +
           "(:query IS NULL OR LOWER(r.itemName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.brand) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.location) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.category) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY r.createdAt DESC")
    List<Report> searchReports(
            @Param("type") ReportType type,
            @Param("category") String category,
            @Param("location") String location,
            @Param("query") String query
    );
}
