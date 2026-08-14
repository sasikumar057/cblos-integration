package com.cblos.webController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CorporateLoginViewController {


    @GetMapping("/corporate-login")
    public String showLoginPage() {
        return "login"; 
    }
    
    @GetMapping("/CBLOS")
    public String showLanding() {
    	return "index";
    }
}