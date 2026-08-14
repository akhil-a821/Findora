package com.campusfind.controller;

import com.campusfind.entity.enums.ReportStatus;
import com.campusfind.service.MatchingService;
import com.campusfind.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ReportService reportService;
    private final MatchingService matchingService;

    public HomeController(ReportService reportService, MatchingService matchingService) {
        this.reportService = reportService;
        this.matchingService = matchingService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("totalReported", reportService.countReports());
        model.addAttribute("totalReturned", reportService.countByStatus(ReportStatus.RETURNED));
        model.addAttribute("activeReports", reportService.countByStatus(ReportStatus.ACTIVE));
        model.addAttribute("potentialMatches", matchingService.countMatches());

        return "index";
    }
}
