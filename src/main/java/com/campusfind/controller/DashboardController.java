package com.campusfind.controller;

import com.campusfind.entity.Match;
import com.campusfind.entity.Notification;
import com.campusfind.entity.Report;
import com.campusfind.entity.User;
import com.campusfind.entity.enums.ReportStatus;
import com.campusfind.entity.enums.ReportType;
import com.campusfind.service.MatchingService;
import com.campusfind.service.NotificationService;
import com.campusfind.service.ReportService;
import com.campusfind.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalTime;
import java.util.List;

@Controller
public class DashboardController {

    private final UserService userService;
    private final ReportService reportService;
    private final MatchingService matchingService;
    private final NotificationService notificationService;

    public DashboardController(UserService userService, ReportService reportService, MatchingService matchingService, NotificationService notificationService) {
        this.userService = userService;
        this.reportService = reportService;
        this.matchingService = matchingService;
        this.notificationService = notificationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName()).orElseThrow();

        // Greeting time of day
        int hour = LocalTime.now().getHour();
        String greeting = (hour < 12) ? "Good morning" : (hour < 17) ? "Good afternoon" : "Good evening";

        // Statistics
        long myLostCount = reportService.countUserReportsByType(user, ReportType.LOST);
        long myFoundCount = reportService.countUserReportsByType(user, ReportType.FOUND);
        List<Match> userMatches = matchingService.getUserMatches(user);
        long reunitedCount = reportService.getUserReports(user).stream()
                .filter(r -> r.getStatus() == ReportStatus.RETURNED)
                .count();

        List<Notification> recentNotifications = notificationService.getUserNotifications(user);
        List<Report> userReports = reportService.getUserReports(user);

        model.addAttribute("greeting", greeting);
        model.addAttribute("user", user);
        model.addAttribute("myLostCount", myLostCount);
        model.addAttribute("myFoundCount", myFoundCount);
        model.addAttribute("matchCount", userMatches.size());
        model.addAttribute("reunitedCount", reunitedCount);
        model.addAttribute("userMatches", userMatches.stream().limit(5).toList());
        model.addAttribute("recentNotifications", recentNotifications.stream().limit(5).toList());
        model.addAttribute("userReports", userReports.stream().limit(5).toList());

        return "dashboard";
    }
}
