package com.cblos.security;

import com.cblos.model.UserRole;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        UserRole role = userDetails.getRole();
        
        System.out.println("🔑 [Sign-In Handler] Routing context path for user: " + userDetails.getUsername() + " | Authority: " + role);

        if (role == UserRole.ADMIN) {
            response.sendRedirect("/customer/dashboard/admin");
        } else if (role == UserRole.MANAGER) {
            response.sendRedirect("/customer/dashboard/manager/workdesk/" + userDetails.getLoanOfficerId());
        } else if (role == UserRole.OFFICER) {
            response.sendRedirect("/customer/dashboard/officer/workdesk/" + userDetails.getLoanOfficerId());
        } else if (role == UserRole.CUSTOMER) {
            response.sendRedirect("/customer/dashboard/" + userDetails.getCorporateCustomerId());
        } else {
            response.sendRedirect("/corporate-login?error=unauthorized");
        }
    }
}