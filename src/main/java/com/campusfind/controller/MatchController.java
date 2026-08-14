package com.campusfind.controller;

import com.campusfind.entity.Match;
import com.campusfind.entity.User;
import com.campusfind.exception.ResourceNotFoundException;
import com.campusfind.service.MatchingService;
import com.campusfind.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class MatchController {

    private final MatchingService matchingService;
    private final UserService userService;

    public MatchController(MatchingService matchingService, UserService userService) {
        this.matchingService = matchingService;
        this.userService = userService;
    }

    @GetMapping("/matches/{id}")
    public String matchDetail(@PathVariable Long id, Authentication authentication, Model model) {
        Match match = matchingService.getMatchById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match record not found with ID: " + id));

        boolean canClaim = false;
        if (authentication != null && authentication.isAuthenticated()) {
            User currentUser = userService.findByEmail(authentication.getName()).orElse(null);
            if (currentUser != null) {
                // User can submit a claim if they own the lost report or are a viewer who didn't create the found report
                canClaim = !match.getFoundReport().getUser().getId().equals(currentUser.getId());
            }
        }

        model.addAttribute("match", match);
        model.addAttribute("lostReport", match.getLostReport());
        model.addAttribute("foundReport", match.getFoundReport());
        model.addAttribute("canClaim", canClaim);

        return "match/detail";
    }
}
