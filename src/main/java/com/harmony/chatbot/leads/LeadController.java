package com.harmony.chatbot.leads;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private static final int MAX_FIELD_LENGTH = 255;

    private final LeadRepository leadRepository;

    public LeadController(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    /** Called by the chatbot widget when the user submits the lead form. */
    @PostMapping
    public ResponseEntity<String> captureLead(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        String name = sanitize(body.get("name"));
        String email = sanitize(body.get("email"));

        // Require at least one field to be non-blank so we don't save empty leads
        if (isBlank(name) && isBlank(email)) {
            return ResponseEntity.badRequest().body("At least a name or email is required.");
        }

        // Basic email format check — rejects obvious garbage without a full regex
        if (!isBlank(email) && !email.contains("@")) {
            return ResponseEntity.badRequest().body("Invalid email address.");
        }

        LeadEntity lead = new LeadEntity();
        lead.setName(isBlank(name) ? null : name);
        lead.setEmail(isBlank(email) ? null : email);
        lead.setSessionId(request.getSession(true).getId());
        leadRepository.save(lead);

        return ResponseEntity.ok("ok");
    }

    /** Admin-only: list all leads. */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<LeadEntity> getLeads() {
        return leadRepository.findAllByOrderByCapturedAtDesc();
    }

    /** Trim, null-safe, and enforce a max length to prevent oversized inputs. */
    private String sanitize(String value) {
        if (value == null)
            return null;
        String trimmed = value.trim();
        return trimmed.length() > MAX_FIELD_LENGTH ? trimmed.substring(0, MAX_FIELD_LENGTH) : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}