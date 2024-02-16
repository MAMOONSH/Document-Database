package com.mamoon.demodocumentdb.controller;

import com.mamoon.demodocumentdb.model.User;
import com.mamoon.demodocumentdb.util.HeaderMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;

@Controller
public class LoginController {
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("*")
    public String gateway(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null)
            return "redirect:login";
        return "redirect:error";
    }

    @GetMapping("/login")
    public String getLogin(@ModelAttribute User user, HttpSession session, Model model) {

        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        model.addAttribute("user", user);
        return "redirect:insertNumbers";
    }

    @PostMapping("/login")
    public String postLogin(@ModelAttribute User user, HttpSession session, Model model) {
        model.addAttribute("user", user);
        String responseMessage = isAuthenticated(HeaderMaker.getHeaderForUser(user));
        if (responseMessage.equals("not valid user")) {
            System.out.println("not valid user");
            return "login";
        }
        if (responseMessage.equals("valid")) {
            System.out.println("valid");
            session.setAttribute("user", user);
            return "redirect:adminHome";
            //return "redirect:setNewPassword";
        }
        if (responseMessage.equals("set new password")) {
            System.out.println("set new password");
            session.setAttribute("user", user);
            return "redirect:setNewPassword";
        }
        return "login";
    }

    private String isAuthenticated(HttpHeaders header) {
        HttpEntity<String> request = new HttpEntity<>(header);
        String AUTHENTICATION_URL = "http://localhost:8080/login";
        ResponseEntity<String> response = restTemplate
                .exchange(AUTHENTICATION_URL, HttpMethod.POST, request, String.class);
        String responseMessage = response.getBody();
        System.out.println(responseMessage);
        return responseMessage;
    }

    @GetMapping("/setNewPassword")
    public String getSettingNewPasswordPage(HttpSession session) {

        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        return "setNewPassword";
    }

    @PostMapping("/setNewPassword")
    public String setNewPassword(HttpSession session, @RequestParam("newPassword") String newPassword) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        System.out.println(newPassword);
        isAuthenticated(HeaderMaker.setNewPasswordHeader(sessionUser, newPassword));
        sessionUser.setPassword(newPassword);
        session.setAttribute("user", sessionUser);
        return "redirect:home";
    }

}
