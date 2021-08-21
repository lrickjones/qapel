package com.qapel.rfid.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
public class HTMLController {

    /**
     * Setup html rules for root /
     * @param principal security principle, != null if user logged in
     * @return url to use
     */
    @GetMapping("/")
    String index(Principal principal) {
        return principal != null ? "redirect:/tag/monitor" : "login";
    }

    /**
     * Login map
     * @return reference to login template
     */
    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Login map for extension
     * @return reference to login template
     */
    @RequestMapping("/login.html")
    public String loginHtml() {
        return "login";
    }

}
