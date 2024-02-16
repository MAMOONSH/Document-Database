package com.mamoon.demodocumentdb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Controller
public class LogoutController {
    @GetMapping("/logout")
    public String logout(HttpSession session, Model model) {
        session.invalidate();
        System.out.println(model.asMap());
        if (model.containsAttribute("user")) model.asMap().remove("user");
        if (model.containsAttribute("dogs")) model.asMap().remove("dogs");
        if (model.containsAttribute("dog")) model.asMap().remove("dog");
        if (model.containsAttribute("cats")) model.asMap().remove("cats");
        if (model.containsAttribute("cat")) model.asMap().remove("cat");
        return "redirect:login";
    }
}
