package com.qapel.rfid.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
        //return "redirect:/tag/monitor";
        //Use this when security is enabled to direct to login if user not logged in
        return principal != null ? "redirect:/tag/monitor" : "login";
    }

    @GetMapping("/tag/test")
    String test(Principal principal) {
        //return "redirect:/tag/monitor";
        //Use this when security is enabled to direct to login if user not logged in
        return "redirect:/tag/test";
    }

    /**
     * Login rule
     * @return reference to login template
     */
    @RequestMapping("/login.html")
    public String login() {
        return "login";
    }

    /**
     * Login error rule
     * @param model model for login template to modify on error
     * @return reference to login template
     */
    @RequestMapping("/login-error.html")
    public String loginError(Model model) {
        model.addAttribute("loginError", true);
        return "login";
    }

}
