package com.campusfind.controller;

import com.campusfind.entity.Report;
import com.campusfind.entity.User;
import com.campusfind.entity.enums.ReportStatus;
import com.campusfind.entity.enums.ReportType;
import com.campusfind.service.ClaimService;
import com.campusfind.service.MatchingService;
import com.campusfind.service.ReportService;
import com.campusfind.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final ReportService reportService;
    private final MatchingService matchingService;
    private final ClaimService claimService;

    public AdminController(UserService userService, ReportService reportService, MatchingService matchingService, ClaimService claimService) {
        this.userService = userService;
        this.reportService = reportService;
        this.matchingService = matchingService;
        this.claimService = claimService;
    }

    @GetMapping
    public String adminDashboard(Model model) {
        long totalUsers = userService.countUsers();
        long totalReports = reportService.countReports();
        long lostReports = reportService.countByType(ReportType.LOST);
        long foundReports = reportService.countByType(ReportType.FOUND);
        long potentialMatches = matchingService.countMatches();
        long returnedItems = reportService.countByStatus(ReportStatus.RETURNED);
        long pendingClaims = claimService.countPendingClaims();

        List<User> users = userService.findAllUsers();
        List<Report> reports = reportService.getAllReports();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalReports", totalReports);
        model.addAttribute("lostReports", lostReports);
        model.addAttribute("foundReports", foundReports);
        model.addAttribute("potentialMatches", potentialMatches);
        model.addAttribute("returnedItems", returnedItems);
        model.addAttribute("pendingClaims", pendingClaims);
        model.addAttribute("users", users);
        model.addAttribute("reports", reports);

        return "admin/dashboard";
    }

    @PostMapping("/reports/delete/{id}")
    public String adminDeleteReport(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        User admin = userService.findByEmail(authentication.getName()).orElseThrow();
        try {
            reportService.deleteReport(id, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Report deleted by admin.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/reports/close/{id}")
    public String adminCloseReport(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reportService.updateReportStatus(id, ReportStatus.CLOSED);
            redirectAttributes.addFlashAttribute("successMessage", "Report closed by admin.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin";
    }
}
