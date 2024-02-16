package com.mammon.documntdb.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @RequestMapping(value="/error/{error}")
    public String errorMessage(@PathVariable("error") String error){
        return error;
    }

    @PostMapping("/login")
    public String validUser() throws JsonProcessingException {
        return "valid";
    }

}
