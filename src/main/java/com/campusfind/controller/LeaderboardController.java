package com.campusfind.controller;

import com.campusfind.dto.LeaderboardDto;
import com.campusfind.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class LeaderboardController {

    private final UserService userService;

    public LeaderboardController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        List<LeaderboardDto> leaderboard = userService.getTopFindersLeaderboard();
        model.addAttribute("leaderboard", leaderboard);
        
        // Pass top 3 podium items if available
        if (leaderboard.size() >= 1) model.addAttribute("firstPlace", leaderboard.get(0));
        if (leaderboard.size() >= 2) model.addAttribute("secondPlace", leaderboard.get(1));
        if (leaderboard.size() >= 3) model.addAttribute("thirdPlace", leaderboard.get(2));

        return "leaderboard";
    }
}
