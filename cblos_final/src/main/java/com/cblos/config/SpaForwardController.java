package com.cblos.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({
            "/",
            "/login",
            "/dashboard",
            "/customers",
            "/officers",
            "/loans",
            "/collateral",
            "/documents",
            "/credit",
            "/approvals",
            "/disbursements",
            "/repayments"
    })
    public String forwardSpa() {
        return "forward:/index.html";
    }
}
