package com.campusfind.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lost_report_id", nullable = false)
    private Report lostReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "found_report_id", nullable = false)
    private Report foundReport;

    @Column(nullable = false)
    private int score;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "match_factors", joinColumns = @JoinColumn(name = "match_id"))
    @Column(name = "factor")
    private List<String> matchingFactors = new ArrayList<>();

    @Column(nullable = false)
    private String status = "POTENTIAL";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Match() {}

    public Match(Report lostReport, Report foundReport, int score, List<String> matchingFactors) {
        this.lostReport = lostReport;
        this.foundReport = foundReport;
        this.score = score;
        this.matchingFactors = matchingFactors;
        this.status = "POTENTIAL";
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public String getMatchLabel() {
        if (score >= 90) return "Excellent Match";
        if (score >= 75) return "Strong Match";
        if (score >= 60) return "Possible Match";
        return "Low Match";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Report getLostReport() {
        return lostReport;
    }

    public void setLostReport(Report lostReport) {
        this.lostReport = lostReport;
    }

    public Report getFoundReport() {
        return foundReport;
    }

    public void setFoundReport(Report foundReport) {
        this.foundReport = foundReport;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<String> getMatchingFactors() {
        return matchingFactors;
    }

    public void setMatchingFactors(List<String> matchingFactors) {
        this.matchingFactors = matchingFactors;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
