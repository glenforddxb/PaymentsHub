package com.cth.sdm.controller;

import com.cth.sdm.model.DocumentDeliverable;
import com.cth.sdm.model.DocumentSubmission;
import com.cth.sdm.model.Phase;
import com.cth.sdm.model.User;
import com.cth.sdm.repository.DocumentDeliverableRepository;
import com.cth.sdm.repository.PhaseRepository;
import com.cth.sdm.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebController {

    private final PhaseRepository phaseRepository;
    private final DocumentDeliverableRepository deliverableRepository;
    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    private final MfaService mfaService;
    private final ApprovalService approvalService;
    private final ReportService reportService;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("ldapEnabled", userDetailsService.isLdapEnabled());
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        return "register";
    }

    @PostMapping("/register")
    public String handleRegistration(@RequestParam String username,
                                     @RequestParam String password,
                                     @RequestParam String email) {
        User user = User.builder()
                .username(username)
                .password(password)
                .email(email)
                .isEnabled(true)
                .isLocked(false)
                .build();
        userService.createUser(user, "ROLE_MAKER");
        log.info("Successfully registered maker account for: {}", username);
        return "redirect:/login?registered=true";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        List<Phase> phases = phaseRepository.findAll();
        phases.sort(Comparator.comparing(Phase::getPhaseNumber));
        model.addAttribute("phases", phases);

        List<DocumentDeliverable> deliverables = deliverableRepository.findAll();
        model.addAttribute("deliverables", deliverables);

        List<DocumentSubmission> submissions = approvalService.getAllSubmissions();
        model.addAttribute("submissions", submissions);

        model.addAttribute("username", authentication.getName());
        model.addAttribute("isAdmin", authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        model.addAttribute("isMaker", authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MAKER")));
        model.addAttribute("isChecker", authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CHECKER")));

        return "dashboard";
    }

    @GetMapping("/admin")
    public String adminPage(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("ldapEnabled", userDetailsService.isLdapEnabled());
        model.addAttribute("mfaEnabled", mfaService.isMfaToggledOn());
        model.addAttribute("notificationsEnabled", approvalService.isNotificationsEnabled());
        model.addAttribute("deliverables", deliverableRepository.findAll());
        return "admin";
    }

    @PostMapping("/admin/toggle-ldap")
    public String toggleLdap(@RequestParam boolean ldap) {
        userDetailsService.setLdapEnabled(ldap);
        return "redirect:/admin";
    }

    @PostMapping("/admin/toggle-mfa")
    public String toggleMfa(@RequestParam boolean mfa) {
        mfaService.setMfaToggledOn(mfa);
        return "redirect:/admin";
    }

    @PostMapping("/admin/toggle-notifications")
    public String toggleNotifications(@RequestParam boolean notifications) {
        approvalService.setNotificationsEnabled(notifications);
        return "redirect:/admin";
    }

    @PostMapping("/admin/user/lock")
    public String lockUser(@RequestParam Long userId) {
        userService.findAll().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .ifPresent(u -> {
                    u.setLocked(true);
                    userService.updateUser(u);
                });
        return "redirect:/admin";
    }

    @PostMapping("/admin/user/unlock")
    public String unlockUser(@RequestParam Long userId) {
        userService.findAll().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .ifPresent(u -> {
                    u.setLocked(false);
                    userService.updateUser(u);
                });
        return "redirect:/admin";
    }

    @PostMapping("/admin/user/reset")
    public String resetUserPassword(@RequestParam Long userId) {
        userService.findAll().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .ifPresent(u -> {
                    u.setPassword("password");
                    userService.updateUser(u);
                });
        return "redirect:/admin";
    }

    @PostMapping("/admin/template/upload")
    public String uploadTemplate(@RequestParam String docId, @RequestParam String description, @RequestParam String version) {
        deliverableRepository.findByDocId(docId).ifPresent(d -> {
            d.setDescription(description);
            d.setVersion(version);
            deliverableRepository.save(d);
            log.info("Admin updated Template for Deliverable Doc ID: {}", docId);
        });
        return "redirect:/admin";
    }

    @PostMapping("/submit-document")
    public String submitDoc(@RequestParam String docId,
                            @RequestParam String appCode,
                            @RequestParam MultipartFile file,
                            Authentication authentication) throws Exception {
        if (file.isEmpty()) {
            return "redirect:/dashboard?error=empty_file";
        }
        File tempFile = File.createTempFile("uploaded-doc-", file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }

        approvalService.submitDocument(docId, appCode, tempFile, authentication.getName());
        tempFile.delete();
        return "redirect:/dashboard?success=true";
    }

    @GetMapping("/approvals")
    public String approvalsPage(Model model, Authentication authentication) {
        model.addAttribute("pendingSubmissions", approvalService.getPendingSubmissions());
        model.addAttribute("username", authentication.getName());
        return "approvals";
    }

    @PostMapping("/approvals/action")
    public String actionApproval(@RequestParam Long submissionId, @RequestParam String action, Authentication authentication) {
        if ("approve".equalsIgnoreCase(action)) {
            approvalService.approveDocument(submissionId, authentication.getName());
        } else {
            approvalService.rejectDocument(submissionId, authentication.getName());
        }
        return "redirect:/approvals";
    }

    @GetMapping("/reports")
    public String reportsPage(Model model) {
        model.addAttribute("submissions", approvalService.getAllSubmissions());
        return "reports";
    }

    @GetMapping("/reports/download/excel")
    @ResponseBody
    public ResponseEntity<byte[]> downloadExcel() throws Exception {
        byte[] data = reportService.generateExcelReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=approvals-report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @GetMapping("/reports/download/pdf")
    @ResponseBody
    public ResponseEntity<byte[]> downloadPdf() throws Exception {
        byte[] data = reportService.generatePdfReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=approvals-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @GetMapping("/docs/download/{docType}")
    @ResponseBody
    public ResponseEntity<byte[]> downloadDoc(@PathVariable String docType) throws Exception {
        String filename = "";
        String mediaType = "";
        if ("spec".equalsIgnoreCase(docType)) {
            filename = "Functional_Specification.docx";
            mediaType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if ("guide".equalsIgnoreCase(docType)) {
            filename = "Walkthrough_Guide.xlsx";
            mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else {
            filename = "Instruction_Deck.pptx";
            mediaType = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        }

        File file = new File("src/main/resources/templates/docs/" + filename);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        byte[] bytes = Files.readAllBytes(file.toPath());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType(mediaType))
                .body(bytes);
    }
}
