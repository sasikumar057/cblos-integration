package com.cblos.controller;

import com.cblos.dto.LoginRequest;
import com.cblos.security.AppUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request,
                                                       HttpServletRequest httpRequest) {
        try {
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
            Authentication authentication = authenticationManager.authenticate(token);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            return ResponseEntity.ok(buildUserProfile((AppUserDetails) authentication.getPrincipal()));
        } catch (BadCredentialsException ex) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", "UNAUTHORIZED");
            body.put("message", "Invalid email or password.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "OK");
        body.put("message", "Logged out successfully.");
        return ResponseEntity.ok(body);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal AppUserDetails userDetails) {
        if (userDetails == null) {
            Map<String, Object> unauthBody = new LinkedHashMap<>();
            unauthBody.put("status", "UNAUTHORIZED");
            unauthBody.put("message", "Session invalid or user not authenticated correctly.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(unauthBody);
        }
        return ResponseEntity.ok(buildUserProfile(userDetails));
    }

    private Map<String, Object> buildUserProfile(AppUserDetails userDetails) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", userDetails.getUsername());
        body.put("role", userDetails.getRole().name());
        body.put("corporateCustomerId", userDetails.getCorporateCustomerId());
        body.put("loanOfficerId", userDetails.getLoanOfficerId());
        return body;
    }
}
