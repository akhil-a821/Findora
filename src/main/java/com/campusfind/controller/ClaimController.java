package com.campusfind.controller;

import com.campusfind.dto.ClaimRequestDto;
import com.campusfind.entity.Claim;
import com.campusfind.entity.User;
import com.campusfind.service.ClaimService;
import com.campusfind.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ClaimController {

    private final ClaimService claimService;
    private final UserService userService;

    public ClaimController(ClaimService claimService, UserService userService) {
        this.claimService = claimService;
        this.userService = userService;
    }

    @PostMapping("/claims")
    public String submitClaim(
            @Valid @ModelAttribute("claimRequestDto") ClaimRequestDto dto,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Validation error submitting claim. Please complete all fields.");
            return "redirect:/item/" + dto.getReportId();
        }

        try {
            User currentUser = userService.findByEmail(authentication.getName()).orElseThrow();
            claimService.submitClaim(dto, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Claim submitted successfully 🎉 The finder has been notified!");
            return "redirect:/my-claims";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/item/" + dto.getReportId();
        }
    }

    @GetMapping("/my-claims")
    public String myClaims(Authentication authentication, Model model) {
        User currentUser = userService.findByEmail(authentication.getName()).orElseThrow();

        List<Claim> submittedClaims = claimService.getUserSubmittedClaims(currentUser);
        List<Claim> receivedClaims = claimService.getClaimsReceivedForFinder(currentUser);

        model.addAttribute("submittedClaims", submittedClaims);
        model.addAttribute("receivedClaims", receivedClaims);

        return "claim/my-claims";
    }

    @PostMapping("/claims/approve/{id}")
    public String approveClaim(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        User finder = userService.findByEmail(authentication.getName()).orElseThrow();
        try {
            claimService.approveClaim(id, finder);
            redirectAttributes.addFlashAttribute("successMessage", "Claim approved successfully 🎉 You can now coordinate returning the item.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/my-claims";
    }

    @PostMapping("/claims/reject/{id}")
    public String rejectClaim(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        User finder = userService.findByEmail(authentication.getName()).orElseThrow();
        try {
            claimService.rejectClaim(id, finder);
            redirectAttributes.addFlashAttribute("successMessage", "Claim rejected.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/my-claims";
    }

    @PostMapping("/claims/return/{reportId}")
    public String markAsReturned(@PathVariable Long reportId, Authentication authentication, RedirectAttributes redirectAttributes) {
        User currentUser = userService.findByEmail(authentication.getName()).orElseThrow();
        try {
            claimService.markItemAsReturned(reportId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Item successfully reunited with its owner ❤️ Report marked as RETURNED.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard";
    }
}
