package com.mamoon.demodocumentdb.controller;

import com.mamoon.demodocumentdb.model.Cat;
import com.mamoon.demodocumentdb.model.Dog;
import com.mamoon.demodocumentdb.model.User;
import com.mamoon.demodocumentdb.util.HeaderMaker;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class CollectionController {
    private String CAT_COLLECTION_URI = "http://localhost:8080/v2/add/schema/animal-shop/cat";
    private String DOG_COLLECTION_URI = "http://localhost:8080/v2/add/schema/animal-shop/dog";
    private String DOWNLOAD_DATABASE = "http://localhost:8080/v2/export/animal-shop";
    private String UPLOAD_DATABASE = "http://localhost:8080/v2/import";
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/addCollection")
    public String getAddCollectionPage(HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        if (sessionUser.getRole().equals("user"))
            return "redirect:userHome";
        return "addCollection";
    }

    @PostMapping("/addCollection")
    public String addCollection(HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        if (sessionUser.getRole().equals("user"))
            return "redirect:userHome";
        createCollections(sessionUser);
        return "redirect:/adminHome";
    }

    private void createCollections(User user) {
        HttpHeaders headers = HeaderMaker.getHeaderForUser(user);
        createCatCollection(headers);
        createDogCollection(headers);
    }

    private void createDogCollection(HttpHeaders headers) {
        Dog dog = new Dog("husky", "blue", "male", "22");
        HttpEntity<Dog> request = new HttpEntity<>(dog, headers);
        try {
            ResponseEntity<Object> response = restTemplate
                    .exchange(DOG_COLLECTION_URI, HttpMethod.POST, request, Object.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createCatCollection(HttpHeaders headers) {
        Cat cat = new Cat("thick", "black", "male", "1000");
        HttpEntity<Cat> request = new HttpEntity<>(cat, headers);
        try {
            ResponseEntity<Object> response = restTemplate
                    .exchange(CAT_COLLECTION_URI, HttpMethod.POST, request, Object.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/downloadDatabase")
    @ResponseBody
    public void downloadDatabase(HttpSession session, HttpServletResponse response) throws IOException {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return;
        HttpEntity<Object> request = new HttpEntity<>(HeaderMaker.getHeaderForUser(sessionUser));
        ResponseEntity<byte[]> bytes = restTemplate.exchange(DOWNLOAD_DATABASE, HttpMethod.GET, request, byte[].class);
        Files.write(Paths.get("animal-shop"), bytes.getBody());
        File file = new File(String.valueOf(Paths.get("animal-shop")));
        InputStream resource = new FileInputStream(file);
        IOUtils.copy(resource, response.getOutputStream());
        response.flushBuffer();
    }
}
