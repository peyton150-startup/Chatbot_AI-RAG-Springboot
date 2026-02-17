package com.harmony.chatbot.admin;

import com.harmony.chatbot.config.AppSettingsService;
import com.harmony.chatbot.theme.ChatbotThemeEntity;
import com.harmony.chatbot.theme.ChatbotThemeService;
import com.harmony.chatbot.user.UserEntity;
import com.harmony.chatbot.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final ChatbotThemeService themeService;
    private final AppSettingsService appSettingsService;

    public AdminController(UserService userService,
            ChatbotThemeService themeService,
            AppSettingsService appSettingsService) {
        this.userService = userService;
        this.themeService = themeService;
        this.appSettingsService = appSettingsService;
    }

    @GetMapping
    public String adminDashboard(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        if (currentUser == null)
            throw new IllegalStateException("No authenticated user");
        List<UserEntity> users = (List<UserEntity>) userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("user", new UserEntity());
        model.addAttribute("editMode", false);
        UserEntity adminUser = userService.getUserByUsernameOptional(currentUser.getUsername())
                .orElseThrow(() -> new IllegalStateException("Admin not found"));
        model.addAttribute("theme", themeService.getOrCreateThemeForUser(adminUser));
        return "admin";
    }

    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return userService.getUserById(id).map(user -> {
            model.addAttribute("user", user);
            return "edit-user";
        }).orElseGet(() -> {
            ra.addFlashAttribute("successMessage", "User not found");
            return "redirect:/admin";
        });
    }

    @PostMapping("/users/{id}/edit")
    public String editUserSubmit(@PathVariable Long id,
            @ModelAttribute UserEntity formUser,
            RedirectAttributes ra) {
        userService.getUserById(id).ifPresent(existing -> {
            existing.setUsername(formUser.getUsername());
            existing.setEmail(formUser.getEmail());
            existing.setRole(formUser.getRole());
            if (formUser.getPassword() != null && !formUser.getPassword().isBlank())
                existing.setPassword(formUser.getPassword());
            userService.saveUser(existing);
        });
        ra.addFlashAttribute("successMessage", "User updated successfully");
        return "redirect:/admin";
    }

    @PostMapping("/users")
    public String saveUser(@ModelAttribute UserEntity user) {
        if (user.getId() != null) {
            userService.getUserById(user.getId()).ifPresent(existing -> {
                existing.setUsername(user.getUsername());
                existing.setEmail(user.getEmail());
                existing.setRole(user.getRole());
                if (user.getPassword() != null && !user.getPassword().isBlank())
                    existing.setPassword(user.getPassword());
                userService.saveUser(existing);
            });
        } else {
            userService.saveUser(user);
        }
        return "redirect:/admin";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        userService.getUserById(id).ifPresent(u -> userService.deleteUserCompletely(u));
        ra.addFlashAttribute("successMessage", "User deleted successfully");
        return "redirect:/admin";
    }

    @PostMapping("/theme")
    @ResponseBody
    public ResponseEntity<String> saveTheme(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestParam(required = false) String headerColor,
            @RequestParam(required = false) String backgroundColor,
            @RequestParam(required = false) String textColor,
            @RequestParam(required = false) String iconColor,
            @RequestParam(required = false) String chipBackgroundColor,
            @RequestParam(required = false) String chipHoverColor,
            @RequestParam(required = false) String chipBorderColor,
            @RequestParam(required = false) String suggestionsJson,
            @RequestParam(required = false) String bookingUrl,
            @RequestParam(required = false) MultipartFile avatar,
            @RequestParam(required = false) MultipartFile banner) throws IOException {

        if (currentUser == null)
            return ResponseEntity.status(401).body("Not authenticated");

        // Save to whichever user is set as the active theme.
        // If no active theme user is set, fall back to the logged-in admin.
        UserEntity targetUser = resolveTargetUser(currentUser);
        if (targetUser == null)
            return ResponseEntity.status(404).body("Target user not found");

        ChatbotThemeEntity f = new ChatbotThemeEntity();
        if (headerColor != null)
            f.setHeaderColor(headerColor);
        if (backgroundColor != null)
            f.setBackgroundColor(backgroundColor);
        if (textColor != null)
            f.setTextColor(textColor);
        if (iconColor != null)
            f.setIconColor(iconColor);
        if (chipBackgroundColor != null)
            f.setChipBackgroundColor(chipBackgroundColor);
        if (chipHoverColor != null)
            f.setChipHoverColor(chipHoverColor);
        if (chipBorderColor != null)
            f.setChipBorderColor(chipBorderColor);
        if (suggestionsJson != null)
            f.setSuggestionsJson(suggestionsJson);
        if (bookingUrl != null)
            f.setBookingUrl(bookingUrl.isBlank() ? "" : bookingUrl.trim());

        if (avatar != null && !avatar.isEmpty()) {
            String mime = avatar.getContentType() != null ? avatar.getContentType() : "image/jpeg";
            f.setAvatarData("data:" + mime + ";base64," + Base64.getEncoder().encodeToString(avatar.getBytes()));
        }
        if (banner != null && !banner.isEmpty()) {
            String mime = banner.getContentType() != null ? banner.getContentType() : "image/jpeg";
            f.setBannerData("data:" + mime + ";base64," + Base64.getEncoder().encodeToString(banner.getBytes()));
        }

        themeService.updateThemeForUser(targetUser, f, null);
        return ResponseEntity.ok("Theme saved");
    }

    /**
     * Determines which user's theme the admin form should save to.
     * Uses the active theme user if one is set, otherwise falls back to the
     * logged-in admin.
     */
    private UserEntity resolveTargetUser(UserDetails currentUser) {
        Long activeUserId = appSettingsService.getActiveThemeUserId();
        if (activeUserId != null) {
            UserEntity active = userService.getUserById(activeUserId).orElse(null);
            if (active != null)
                return active;
        }
        // Fallback: save to the logged-in admin's own theme
        return userService.getUserByUsernameOptional(currentUser.getUsername()).orElse(null);
    }
}