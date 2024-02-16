package com.mamoon.demodocumentdb.controller;

import com.mamoon.demodocumentdb.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Controller
public class HomeController {
    @GetMapping("/adminHome")
    public String getAdminHomePage(HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        if (sessionUser.getRole().equals("user"))
            return "redirect:userHome";
        return "adminHome";
    }

    @GetMapping("/userHome")
    public String getUserHomePage(HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        return "userHome";
    }

    @GetMapping("/home")
    public String getHomePage(HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        if (sessionUser.getRole().equals("user"))
            return "redirect:userHome";
        if (sessionUser.getRole().equals("admin"))
            return "redirect:adminHome";
        return "login";
    }
}
