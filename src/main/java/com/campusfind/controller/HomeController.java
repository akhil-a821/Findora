package com.campusfind.controller;

import com.campusfind.entity.enums.ReportStatus;
import com.campusfind.service.MatchingService;
import com.campusfind.service.ReportService;
import com.campusfind.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ReportService reportService;
    private final MatchingService matchingService;
    private final UserService userService;

    public HomeController(ReportService reportService, MatchingService matchingService, UserService userService) {
        this.reportService = reportService;
        this.matchingService = matchingService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("totalReported", reportService.countReports());
        model.addAttribute("totalReturned", reportService.countByStatus(ReportStatus.RETURNED));
        model.addAttribute("activeReports", reportService.countByStatus(ReportStatus.ACTIVE));
        model.addAttribute("potentialMatches", matchingService.countMatches());
        model.addAttribute("topFinders", userService.getTopFindersLeaderboard().stream().limit(5).toList());

        return "index";
    }
}

