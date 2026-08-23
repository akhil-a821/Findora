package com.campusfind.service;

import com.campusfind.entity.Match;
import com.campusfind.entity.Report;
import com.campusfind.entity.User;
import com.campusfind.entity.enums.NotificationType;
import com.campusfind.entity.enums.ReportType;
import com.campusfind.repository.MatchRepository;
import com.campusfind.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    private final MatchRepository matchRepository;
    private final ReportRepository reportRepository;
    private final NotificationService notificationService;

    public MatchingService(MatchRepository matchRepository, ReportRepository reportRepository, NotificationService notificationService) {
        this.matchRepository = matchRepository;
        this.reportRepository = reportRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public List<Match> findAndSaveMatchesForReport(Report newReport) {
        List<Match> generatedMatches = new ArrayList<>();

        ReportType oppositeType = (newReport.getType() == ReportType.LOST) ? ReportType.FOUND : ReportType.LOST;
        List<Report> potentialCandidates = reportRepository.findByTypeOrderByCreatedAtDesc(oppositeType);

        for (Report candidate : potentialCandidates) {
            // Determine lost vs found
            Report lostReport = (newReport.getType() == ReportType.LOST) ? newReport : candidate;
            Report foundReport = (newReport.getType() == ReportType.FOUND) ? newReport : candidate;

            // Avoid comparing user to themselves
            if (lostReport.getUser().getId().equals(foundReport.getUser().getId())) {
                continue;
            }

            MatchCalculation calc = calculateMatchScore(lostReport, foundReport);

            if (calc.getScore() >= 50) {
                Optional<Match> existingOpt = matchRepository.findByLostReportAndFoundReport(lostReport, foundReport);
                Match match;
                if (existingOpt.isPresent()) {
                    match = existingOpt.get();
                    match.setScore(calc.getScore());
                    match.setMatchingFactors(calc.getFactors());
                    match = matchRepository.save(match);
                } else {
                    match = new Match(lostReport, foundReport, calc.getScore(), calc.getFactors());
                    match = matchRepository.save(match);

                    // Send notifications with direct link to the match breakdown
                    notificationService.createNotification(
                            lostReport.getUser(),
                            "Potential Match Found (" + calc.getScore() + "% Match)",
                            "A found item '" + foundReport.getItemName() + "' matches your lost report '" + lostReport.getItemName() + "'.",
                            NotificationType.MATCH_FOUND,
                            "/matches/" + match.getId()
                    );

                    notificationService.createNotification(
                            foundReport.getUser(),
                            "Potential Match Found (" + calc.getScore() + "% Match)",
                            "Your found report '" + foundReport.getItemName() + "' matches a lost report for '" + lostReport.getItemName() + "'.",
                            NotificationType.MATCH_FOUND,
                            "/matches/" + match.getId()
                    );
                }

                generatedMatches.add(match);
            }
        }

        return generatedMatches;
    }

    public MatchCalculation calculateMatchScore(Report lost, Report found) {
        int score = 0;
        List<String> factors = new ArrayList<>();

        // 1. Category match (20 pts)
        if (lost.getCategory() != null && lost.getCategory().equalsIgnoreCase(found.getCategory())) {
            score += 20;
            factors.add("✓ Category match: " + lost.getCategory() + " (+20 pts)");
        }

        // 2. Location match (25 pts)
        if (lost.getLocation() != null && found.getLocation() != null) {
            String locLost = lost.getLocation().toLowerCase().trim();
            String locFound = found.getLocation().toLowerCase().trim();
            if (locLost.equals(locFound)) {
                score += 25;
                factors.add("✓ Exact location match: " + lost.getLocation() + " (+25 pts)");
            } else if (locLost.contains(locFound) || locFound.contains(locLost)) {
                score += 20;
                factors.add("✓ Proximity location match: " + lost.getLocation() + " (+20 pts)");
            }
        }

        // 3. Date proximity (20 pts)
        if (lost.getDate() != null && found.getDate() != null) {
            long daysDiff = Math.abs(ChronoUnit.DAYS.between(lost.getDate(), found.getDate()));
            if (daysDiff == 0) {
                score += 20;
                factors.add("✓ Date proximity: Same day (+20 pts)");
            } else if (daysDiff == 1) {
                score += 15;
                factors.add("✓ Date proximity: 1 day difference (+15 pts)");
            } else if (daysDiff <= 3) {
                score += 10;
                factors.add("✓ Date proximity: " + daysDiff + " days difference (+10 pts)");
            } else if (daysDiff <= 7) {
                score += 5;
                factors.add("✓ Date proximity: " + daysDiff + " days difference (+5 pts)");
            }
        }

        // 4. Brand match (10 pts)
        if (lost.getBrand() != null && !lost.getBrand().isBlank() &&
            found.getBrand() != null && !found.getBrand().isBlank()) {
            String brandLost = lost.getBrand().toLowerCase().trim();
            String brandFound = found.getBrand().toLowerCase().trim();
            if (brandLost.equals(brandFound) || brandLost.contains(brandFound) || brandFound.contains(brandLost)) {
                score += 10;
                factors.add("✓ Matching brand: " + lost.getBrand() + " (+10 pts)");
            }
        }

        // 5. Color match (10 pts)
        if (lost.getColor() != null && !lost.getColor().isBlank() &&
            found.getColor() != null && !found.getColor().isBlank()) {
            String colorLost = lost.getColor().toLowerCase().trim();
            String colorFound = found.getColor().toLowerCase().trim();
            if (colorLost.equals(colorFound) || colorLost.contains(colorFound) || colorFound.contains(colorLost)) {
                score += 10;
                factors.add("✓ Matching color: " + lost.getColor() + " (+10 pts)");
            }
        }

        // 6. Item name similarity (10 pts)
        if (lost.getItemName() != null && found.getItemName() != null) {
            double nameSim = calculateSimilarity(lost.getItemName(), found.getItemName());
            if (nameSim >= 0.4) {
                int pts = Math.max(2, (int) Math.round(nameSim * 10));
                score += pts;
                factors.add("✓ Title similarity (" + Math.round(nameSim * 100) + "%) (+" + pts + " pts)");
            }
        }

        // 7. Description keywords overlap (5 pts)
        if (lost.getDescription() != null && found.getDescription() != null) {
            Set<String> lostWords = extractKeywords(lost.getDescription());
            Set<String> foundWords = extractKeywords(found.getDescription());
            lostWords.retainAll(foundWords);
            if (!lostWords.isEmpty()) {
                score += 5;
                factors.add("✓ Keyword overlap: " + String.join(", ", lostWords.stream().limit(3).collect(Collectors.toList())) + " (+5 pts)");
            }
        }

        return new MatchCalculation(Math.min(100, score), factors);
    }

    private double calculateSimilarity(String s1, String s2) {
        String str1 = s1.toLowerCase().trim();
        String str2 = s2.toLowerCase().trim();
        if (str1.equals(str2)) return 1.0;
        if (str1.contains(str2) || str2.contains(str1)) return 0.8;
        
        Set<String> words1 = Arrays.stream(str1.split("\\s+")).collect(Collectors.toSet());
        Set<String> words2 = Arrays.stream(str2.split("\\s+")).collect(Collectors.toSet());
        Set<String> intersection = new java.util.HashSet<>(words1);
        intersection.retainAll(words2);
        
        if (intersection.isEmpty()) return 0.0;
        return (2.0 * intersection.size()) / (words1.size() + words2.size());
    }

    private Set<String> extractKeywords(String text) {
        String[] stopWords = {"the", "a", "an", "in", "on", "at", "to", "for", "with", "my", "was", "is", "it", "and", "or", "of"};
        Set<String> stopWordSet = new java.util.HashSet<>(Arrays.asList(stopWords));
        
        return Arrays.stream(text.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "").split("\\s+"))
                .filter(w -> w.length() > 2 && !stopWordSet.contains(w))
                .collect(Collectors.toSet());
    }

    public List<Match> getUserMatches(User user) {
        return matchRepository.findMatchesForUser(user);
    }

    public Optional<Match> getMatchById(Long id) {
        return matchRepository.findById(id);
    }

    public List<Match> getMatchesByReportId(Long reportId) {
        return matchRepository.findMatchesByReportId(reportId);
    }

    public long countMatches() {
        return matchRepository.countByScoreGreaterThanEqual(50);
    }

    public static class MatchCalculation {
        private final int score;
        private final List<String> factors;

        public MatchCalculation(int score, List<String> factors) {
            this.score = score;
            this.factors = factors;
        }

        public int getScore() { return score; }
        public List<String> getFactors() { return factors; }
    }
}
