package com.cblos.security;

import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response, 
                                        Authentication authentication) throws IOException, ServletException {
        
        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        boolean isCustomer = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_CUSTOMER"));

        boolean isOfficer = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_OFFICER"));
        
        boolean isManager = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_MANAGER"));

        String contextPath = request.getContextPath();

        if (isAdmin) {
            response.sendRedirect(contextPath + "/admin/dashboard");
        } else if (isCustomer) {
            Integer customerId = userDetails.getCorporateCustomerId();
            response.sendRedirect(contextPath + "/customer/dashboard/" + customerId);
        } else if (isOfficer) {
            Integer officerId = userDetails.getLoanOfficerId();
            response.sendRedirect(contextPath + "/officer/dashboard/workdesk/" + officerId);
        } else if (isManager) {
            Integer managerId = userDetails.getLoanOfficerId();
            response.sendRedirect(contextPath + "/manager/dashboard/workdesk/" + managerId);
        } else {
            response.sendRedirect(contextPath + "/corporate-login?error=true");
        }
    }
}
