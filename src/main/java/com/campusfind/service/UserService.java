package com.campusfind.service;

import com.campusfind.dto.LeaderboardDto;
import com.campusfind.dto.UserRegistrationDto;
import com.campusfind.entity.Report;
import com.campusfind.entity.User;
import com.campusfind.entity.enums.ReportStatus;
import com.campusfind.entity.enums.ReportType;
import com.campusfind.entity.enums.Role;
import com.campusfind.repository.ReportRepository;
import com.campusfind.repository.UserRepository;
import com.campusfind.repository.ClaimRepository;
import com.campusfind.repository.MatchRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClaimRepository claimRepository;
    private final MatchRepository matchRepository;

    public UserService(UserRepository userRepository, ReportRepository reportRepository, PasswordEncoder passwordEncoder,
                       ClaimRepository claimRepository, MatchRepository matchRepository) {
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
        this.passwordEncoder = passwordEncoder;
        this.claimRepository = claimRepository;
        this.matchRepository = matchRepository;
    }

    @Transactional
    public User registerUser(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.ROLE_USER);

        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim());
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public long countUsers() {
        return userRepository.count();
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Remove relationships that block report deletion due to foreign key constraints
        List<Report> userReports = reportRepository.findByUserOrderByCreatedAtDesc(user);
        for (Report report : userReports) {
            claimRepository.deleteByReport(report);
            matchRepository.deleteByLostReportOrFoundReport(report, report);
        }
        
        // Delete user (JPA will cascade to Reports, Claims made by claimant, and Notifications)
        userRepository.delete(user);
    }

    public List<LeaderboardDto> getTopFindersLeaderboard() {
        List<User> users = userRepository.findAll();
        List<LeaderboardDto> leaderboard = new ArrayList<>();

        for (User user : users) {
            List<Report> userReports = reportRepository.findByUserOrderByCreatedAtDesc(user);
            long itemsFound = userReports.stream()
                    .filter(r -> r.getType() == ReportType.FOUND)
                    .count();
            long itemsReturned = userReports.stream()
                    .filter(r -> r.getType() == ReportType.FOUND && (r.getStatus() == ReportStatus.RETURNED || r.getStatus() == ReportStatus.CLAIMED))
                    .count();

            long totalPoints = (itemsReturned * 100) + (itemsFound * 25);

            leaderboard.add(new LeaderboardDto(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    itemsFound,
                    itemsReturned,
                    totalPoints,
                    "",
                    0
            ));
        }

        // Filter users who have at least 1 reported found item or returned item, then sort
        List<LeaderboardDto> sorted = leaderboard.stream()
                .filter(item -> item.getItemsFound() > 0 || item.getItemsReturned() > 0)
                .sorted(Comparator.comparingLong(LeaderboardDto::getTotalPoints)
                        .thenComparingLong(LeaderboardDto::getItemsReturned).reversed())
                .collect(Collectors.toList());

        // Assign ranks and badges
        List<LeaderboardDto> finalLeaderboard = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            LeaderboardDto item = sorted.get(i);
            int rank = i + 1;
            String badge;
            if (rank == 1) {
                badge = "🏆 Campus Legend";
            } else if (rank == 2) {
                badge = "🌟 Super Finder";
            } else if (rank == 3) {
                badge = "🏅 Good Samaritan";
            } else {
                badge = "🎖️ Campus Hero";
            }

            finalLeaderboard.add(new LeaderboardDto(
                    item.getUserId(),
                    item.getName(),
                    item.getEmail(),
                    item.getItemsFound(),
                    item.getItemsReturned(),
                    item.getTotalPoints(),
                    badge,
                    rank
            ));
        }

        return finalLeaderboard;
    }
}

