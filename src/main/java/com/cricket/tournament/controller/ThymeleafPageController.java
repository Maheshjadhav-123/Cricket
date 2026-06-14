package com.cricket.tournament.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ui")
public class ThymeleafPageController {

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("appName", "Cricket Tournament System");
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboardPage(Model model) {
        model.addAttribute("title", "User Dashboard");
        return "dashboard";
    }

    @GetMapping("/leaderboard")
    public String leaderboardPage(Model model) {
        model.addAttribute("title", "Tournament Leaderboard");
        return "leaderboard";
    }
}
