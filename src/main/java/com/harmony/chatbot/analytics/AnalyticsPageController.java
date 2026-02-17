package com.harmony.chatbot.analytics;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/analytics")
@PreAuthorize("hasAuthority('ADMIN')")
public class AnalyticsPageController {

    @GetMapping
    public String analyticsPage() {
        return "analytics";
    }
}
