package com.mamoon.demodocumentdb.controller;

import com.mamoon.demodocumentdb.model.User;
import com.mamoon.demodocumentdb.util.HeaderMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;


@Controller
public class NodeController {
    private String ADD_NODE = "http://localhost:8080/node/add/animal-shop/";
    private String DELETE_NODE = "http://localhost:8080/node/delete/animal-shop/";

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/addNode")
    public String getAddCollectionPage(HttpSession session
            , @RequestParam("collectionName") String collectionName
            , @RequestParam("numberOfNewNodes") int numberOfNewNodes) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        if (sessionUser.getRole().equals("user"))
            return "redirect:userHome";
        addNodes(sessionUser, collectionName, numberOfNewNodes);
        //restTemplate.exchange(ADD_NODE + collectionName + "/" + numberOfNewNodes, HttpMethod.GET, Object.class);
        return "addCollection";
    }

    private void addNodes(User user, String collectionName, int numberOfNewNodes) {
        HttpEntity<Object> request = new HttpEntity<>(HeaderMaker.getHeaderForUser(user));
        restTemplate.exchange(ADD_NODE + collectionName + "/" + numberOfNewNodes, HttpMethod.GET, request, Object.class);
    }


    @PostMapping("/deleteNode")
    public String addCollection(HttpSession session
            , @RequestParam("collectionName") String collectionName
            , @RequestParam("numberOfDeleteNodes") int numberOfDeleteNodes) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        if (sessionUser.getRole().equals("user"))
            return "redirect:userHome";
        deleteNodes(sessionUser, collectionName, numberOfDeleteNodes);
        return "addCollection";
    }

    private void deleteNodes(User user, String collectionName, int numberOfDeleteNodes) {
        HttpEntity<Object> request = new HttpEntity<>(HeaderMaker.getHeaderForUser(user));
        restTemplate.exchange(DELETE_NODE + collectionName + "/" + numberOfDeleteNodes, HttpMethod.GET, request, Object.class);
    }
}
