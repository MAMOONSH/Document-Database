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
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;

//todo solve the problem in adding users
@Controller
public class UserController {

    String USERS_URI = "http://localhost:8080/v2/add/document/users/user";
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/addUser")
    public String getAddUserPage(@ModelAttribute User user, HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        if (sessionUser.getRole().equals("user"))
            return "redirect:userHome";
        model.addAttribute("user", user);
        return "addUser";
    }

    @PostMapping("addUser")
    public String addUser(@ModelAttribute User user, HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        if (sessionUser.getRole().equals("user"))
            return "redirect:userHome";
        model.addAttribute("user", user);
        addUserToDatabase(user, HeaderMaker.getHeaderForUser(sessionUser));
        return "redirect:adminHome";
    }

    //todo handle response
    private void addUserToDatabase(User user, HttpHeaders headers) {
        HttpEntity<User> request = new HttpEntity<>(user, headers);
        try {
            ResponseEntity<Object> response = restTemplate
                    .exchange(USERS_URI, HttpMethod.POST, request, Object.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
