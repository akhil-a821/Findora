package com.campusfind.controller;

import com.campusfind.dto.ReportRequestDto;
import com.campusfind.entity.Match;
import com.campusfind.entity.Report;
import com.campusfind.entity.User;
import com.campusfind.entity.enums.ReportType;
import com.campusfind.service.MatchingService;
import com.campusfind.service.ReportService;
import com.campusfind.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;
    private final MatchingService matchingService;
    private final com.campusfind.service.ClaimService claimService;

    public ReportController(ReportService reportService, UserService userService, MatchingService matchingService, com.campusfind.service.ClaimService claimService) {
        this.reportService = reportService;
        this.userService = userService;
        this.matchingService = matchingService;
        this.claimService = claimService;
    }

    @GetMapping("/report")
    public String showReportForm(Model model) {
        if (!model.containsAttribute("reportRequestDto")) {
            ReportRequestDto dto = new ReportRequestDto();
            dto.setType(ReportType.LOST);
            model.addAttribute("reportRequestDto", dto);
        }
        return "report/create";
    }

    @PostMapping("/report")
    public String submitReport(
            @Valid @ModelAttribute("reportRequestDto") ReportRequestDto dto,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "report/create";
        }

        try {
            User user = userService.findByEmail(authentication.getName()).orElseThrow();
            Report createdReport = reportService.createReport(dto, user);
            redirectAttributes.addFlashAttribute("successMessage", "Report submitted successfully 🎉 Smart Matching engine is scanning for matches!");
            return "redirect:/item/" + createdReport.getId();
        } catch (IOException | IllegalArgumentException e) {
            bindingResult.rejectValue("imageFile", "error.reportRequestDto", e.getMessage());
            return "report/create";
        }
    }

    @GetMapping("/browse")
    public String browseReports(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String query,
            Model model
    ) {
        ReportType reportType = null;
        if (type != null && !type.isBlank() && !type.equalsIgnoreCase("ALL")) {
            try {
                reportType = ReportType.valueOf(type.toUpperCase());
            } catch (Exception ignored) {}
        }

        List<Report> reports = reportService.searchAndFilterReports(reportType, category, location, query);

        model.addAttribute("reports", reports);
        model.addAttribute("totalCount", reports.size());
        model.addAttribute("selectedType", type != null ? type : "ALL");
        model.addAttribute("selectedCategory", category != null ? category : "ALL");
        model.addAttribute("selectedLocation", location != null ? location : "ALL");
        model.addAttribute("searchQuery", query != null ? query : "");

        return "report/browse";
    }

    @GetMapping("/item/{id}")
    public String viewItemDetail(@PathVariable Long id, Authentication authentication, Model model) {
        Report report = reportService.getReportById(id);
        List<Match> matches = matchingService.getMatchesByReportId(id);

        boolean isOwner = false;
        boolean hasClaimed = false;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            User currentUser = userService.findByEmail(authentication.getName()).orElse(null);
            if (currentUser != null) {
                if (report.getUser().getId().equals(currentUser.getId())) {
                    isOwner = true;
                }
                hasClaimed = claimService.hasUserClaimedReport(report, currentUser);
            }
        }

        model.addAttribute("report", report);
        model.addAttribute("matches", matches);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("hasClaimed", hasClaimed);

        return "report/detail";
    }

    @GetMapping("/my-reports")
    public String myReports(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName()).orElseThrow();
        List<Report> reports = reportService.getUserReports(user);

        model.addAttribute("reports", reports);
        return "report/my-reports";
    }

    @PostMapping("/reports/delete/{id}")
    public String deleteReport(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        User currentUser = userService.findByEmail(authentication.getName()).orElseThrow();
        try {
            reportService.deleteReport(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Report deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/my-reports";
    }
}
