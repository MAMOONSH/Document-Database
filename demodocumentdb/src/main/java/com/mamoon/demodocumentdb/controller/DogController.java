package com.mamoon.demodocumentdb.controller;

import com.mamoon.demodocumentdb.model.Dog;
import com.mamoon.demodocumentdb.model.User;
import com.mamoon.demodocumentdb.util.HeaderMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
public class DogController {
    private static List<Dog> searchResult = new ArrayList<>();
    private String SEND_DOGS_URI = "http://localhost:8080/v2/add/document/animal-shop/dog";
    private String UPDATE_DOGS_URI = "http://localhost:8080/v2/update/animal-shop/dog";
    private String DELETE_DOG_URI = "http://localhost:8080/v2/delete/animal-shop/dog";
    private String GET_DOGS_URI = "http://localhost:8080/node/read/animal-shop/dog";
    private String SEARCH_DOGS_URI = "http://localhost:8080/node/search/animal-shop/dog";

    @Autowired
    private RestTemplate restTemplate;


    @GetMapping("/addDog")
    public String getAddDogPage(@ModelAttribute Dog dog, HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        if (sessionUser.getRole().equals("user"))
            return "redirect:userHome";
        model.addAttribute("dog", dog);
        return "addDog";
    }

    @PostMapping("/addDog")
    public String addDog(@ModelAttribute Dog dog, HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        if (sessionUser.getRole().equals("user"))
            return "redirect:userHome";
        model.addAttribute("dog", dog);
        addDogToDatabase(sessionUser, dog);
        return "redirect:adminHome";
    }

    //todo handle response
    private void addDogToDatabase(User user, Dog dog) {
        HttpEntity<Dog> request = new HttpEntity<>(dog, HeaderMaker.getHeaderForUser(user));
        try {
            ResponseEntity<Object> response = restTemplate
                    .exchange(SEND_DOGS_URI, HttpMethod.POST, request, Object.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/readDog")
    public String getReadDogPage(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        List<Dog> dogs = readAllDogs(sessionUser);
        System.out.println(dogs);
        model.addAttribute("dogs", dogs);
        return "readDog";
    }

    private List<Dog> readAllDogs(User user) {
        HttpEntity<Dog> request = new HttpEntity<>(HeaderMaker.getHeaderForUser(user));
        ResponseEntity<Dog[]> response = restTemplate
                .exchange(GET_DOGS_URI, HttpMethod.GET, request, Dog[].class);
        return Arrays.asList(response.getBody());
    }

    @GetMapping("/searchDog")
    public String getSearchDogsPage(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        model.addAttribute("dogs", searchResult);
        return "searchDog";
    }

    @PostMapping("/searchDog")
    public String searchDogs(HttpSession session
            , @RequestParam("kind") String kind
            , @RequestParam("price") String price
            , @RequestParam("gender") String gender
            , @RequestParam("color") String color
    ) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        Map<String, String> searchValues = setMapValues(kind, price, gender, color);
        searchResult = searchDogs(sessionUser, searchValues);
        return "redirect:searchDog";
    }

    private Map<String, String> setMapValues(String kind, String price, String gender, String color) {
        Map<String, String> searchValues = new HashMap<>();
        if (kind != null && !kind.equals(""))
            searchValues.put("kind", kind);
        if (price != null && !price.equals(""))
            searchValues.put("price", price);
        if (gender != null && !gender.equals(""))
            searchValues.put("gender", gender);
        if (color != null && !color.equals(""))
            searchValues.put("color", color);
        return searchValues;
    }

    private List<Dog> searchDogs(User user, Map<String, String> dog) {
        HttpEntity<Map<String, String>> request = new HttpEntity<>(dog, HeaderMaker.getHeaderForUser(user));
        ResponseEntity<Object> response = restTemplate
                .exchange(SEARCH_DOGS_URI, HttpMethod.POST, request, Object.class);
        String nodeSearchUri = response.getHeaders().get("Location").get(0);
        ResponseEntity<Object> searchResponse;
        try {
            searchResponse = restTemplate.exchange(nodeSearchUri, HttpMethod.POST, request, Object.class);
        } catch (HttpClientErrorException exception) {
            System.out.println(exception);
            return new ArrayList<>();
        }

        System.out.println(searchResponse);
        System.out.println(searchResponse.getBody());
        List<Dog> dogs = (List<Dog>) searchResponse.getBody();
        System.out.println("list:" + dogs);
        return dogs;
    }

    @GetMapping("/updateDog")
    public String getUpdateDogPage(HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        return "updateDog";
    }

    @PostMapping("/updateDog")
    public String updateDog(HttpSession session
            , @RequestParam("_id") String _id
            , @RequestParam("kind") String kind
            , @RequestParam("price") String price
            , @RequestParam("gender") String gender
            , @RequestParam("color") String color
            , @RequestParam("newKind") String newKind
            , @RequestParam("newPrice") String newPrice
            , @RequestParam("newGender") String newGender
            , @RequestParam("newColor") String newColor) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        Map<String, String> searchValues = setMapValues(kind, price, gender, color);
        if (_id != null && !_id.equals(""))
            searchValues.put("_id", _id);
        Map<String, String> newValues = setMapValues(newKind, newPrice, newGender, newColor);
        updateValues(sessionUser, searchValues, newValues);
        return "redirect:adminHome";
    }

    private void updateValues(User user, Map<String, String> searchValues, Map<String, String> newValues) {
        HttpEntity<List<Map<String, String>>> request
                = new HttpEntity<>(Arrays.asList(searchValues, newValues), HeaderMaker.getHeaderForUser(user));
        restTemplate.exchange(UPDATE_DOGS_URI, HttpMethod.POST, request, Object.class);
    }

    @GetMapping("/deleteDog")
    public String getDeleteDogPage(HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        return "deleteDog";
    }

    @PostMapping("/deleteDog")
    public String deleteDog(HttpSession session
            , @RequestParam("_id") String _id
            , @RequestParam("kind") String kind
            , @RequestParam("price") String price
            , @RequestParam("gender") String gender
            , @RequestParam("color") String color) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        Map<String, String> searchValues = setMapValues(kind, price, gender, color);
        if (_id != null && !_id.equals(""))
            searchValues.put("_id", _id);
        deleteDog(sessionUser, searchValues);
        return "redirect:adminHome";
    }

    private void deleteDog(User user, Map<String, String> matchValues) {
        HttpEntity<Map<String, String>> request
                = new HttpEntity<>(matchValues, HeaderMaker.getHeaderForUser(user));
        restTemplate.exchange(DELETE_DOG_URI, HttpMethod.POST, request, Object.class);
    }
}
