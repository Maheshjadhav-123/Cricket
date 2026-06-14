package com.cricket.tournament.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import com.cricket.tournament.service.AsyncService;

@RestController
public class HomeController {

    private final AsyncService asyncService;

    @Autowired
    public HomeController(AsyncService asyncService) {
        this.asyncService = asyncService;
    }

    @GetMapping("/")
    public String home() {
        asyncService.logAsyncEvent("Home endpoint accessed!");
        return "Cricket Tournament System Running";
    }
}
