package com.campusfind.controller;

import com.campusfind.entity.Notification;
import com.campusfind.entity.User;
import com.campusfind.service.NotificationService;
import com.campusfind.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping("/notifications")
    public String notifications(Authentication authentication, Model model) {
        User user = userService.findByEmail(authentication.getName()).orElseThrow();
        List<Notification> notifications = notificationService.getUserNotifications(user);

        model.addAttribute("notifications", notifications);
        return "notification/list";
    }

    @PostMapping("/notifications/read/{id}")
    @ResponseBody
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName()).orElseThrow();
        notificationService.markAsRead(id, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/notifications/read-all")
    public String markAllAsRead(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName()).orElseThrow();
        notificationService.markAllAsRead(user);
        return "redirect:/notifications";
    }
}
