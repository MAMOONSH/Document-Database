package com.mamoon.demodocumentdb.controller;

import com.mamoon.demodocumentdb.model.Cat;
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
public class CatController {
    private static List<Cat> searchResult = new ArrayList<>();
    private String SEND_CATS_URI = "http://localhost:8080/v2/add/document/animal-shop/cat";
    private String UPDATE_CATS_URI = "http://localhost:8080/v2/update/animal-shop/cat";
    private String DELETE_CAT_URI = "http://localhost:8080/v2/delete/animal-shop/cat";
    private String GET_CATS_URI = "http://localhost:8080/node/read/animal-shop/cat";
    private String SEARCH_CATS_URI = "http://localhost:8080/node/search/animal-shop/cat";
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/addCat")
    public String getAddCatPage(@ModelAttribute Cat cat, HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        if (sessionUser.getRole().equals("user"))
            return "redirect:userHome";
        model.addAttribute("cat", cat);
        return "addCat";
    }

    @PostMapping("addCat")
    public String addCat(@ModelAttribute Cat cat, HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        if (sessionUser.getRole().equals("user"))
            return "redirect:userHome";
        model.addAttribute("cat", cat);
        addCatToDatabase(sessionUser, cat);
        return "redirect:adminHome";
    }

    //todo handle response
    private void addCatToDatabase(User user, Cat cat) {
        HttpEntity<Cat> request = new HttpEntity<>(cat, HeaderMaker.getHeaderForUser(user));
        try {
            ResponseEntity<Object> response = restTemplate
                    .exchange(SEND_CATS_URI, HttpMethod.POST, request, Object.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/readCat")
    public String getReadCatPage(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        List<Cat> cats = readAllCats(sessionUser);
        System.out.println(cats);
        model.addAttribute("cats", cats);
        return "readCat";
    }

    private List<Cat> readAllCats(User user) {
        HttpEntity<Cat> request = new HttpEntity<>(HeaderMaker.getHeaderForUser(user));
        ResponseEntity<Cat[]> response = restTemplate
                .exchange(GET_CATS_URI, HttpMethod.GET, request, Cat[].class);
        return Arrays.asList(response.getBody());
    }

    @GetMapping("/searchCat")
    public String getSearchDogsPage(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        model.addAttribute("cats", searchResult);
        return "searchDog";
    }

    @PostMapping("/searchCat")
    public String searchCats(HttpSession session
            , @RequestParam("furLevel") String furLevel
            , @RequestParam("price") String price
            , @RequestParam("gender") String gender
            , @RequestParam("color") String color) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        Map<String, String> searchValues = setMapValues(furLevel, price, gender, color);
        System.out.println(searchValues);
        System.out.println(searchCats(sessionUser, searchValues));
        searchResult = searchCats(sessionUser, searchValues);
        return "redirect:searchCat";
    }

    private Map<String, String> setMapValues(String furLevel, String price, String gender, String color) {
        Map<String, String> searchValues = new HashMap<>();
        if (furLevel != null && !furLevel.equals(""))
            searchValues.put("furLevel", furLevel);
        if (price != null && !price.equals(""))
            searchValues.put("price", price);
        if (gender != null && !gender.equals(""))
            searchValues.put("gender", gender);
        if (color != null && !color.equals(""))
            searchValues.put("color", color);
        return searchValues;
    }

    private List<Cat> searchCats(User user, Map<String, String> dog) {
        HttpEntity<Map<String, String>> request = new HttpEntity<>(dog, HeaderMaker.getHeaderForUser(user));
        ResponseEntity<Object> response = restTemplate
                .exchange(SEARCH_CATS_URI, HttpMethod.POST, request, Object.class);
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
        List<Cat> cats = (List<Cat>) searchResponse.getBody();
        System.out.println("list:" + cats);
        return cats;
    }

    @GetMapping("/updateCat")
    public String getUpdateCatPage(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        return "updateCat";
    }

    @PostMapping("/updateCat")
    public String updateCat(HttpSession session
            , @RequestParam("_id") String _id
            , @RequestParam("furLevel") String furLevel
            , @RequestParam("price") String price
            , @RequestParam("gender") String gender
            , @RequestParam("color") String color
            , @RequestParam("newFurLevel") String newFurLevel
            , @RequestParam("newPrice") String newPrice
            , @RequestParam("newGender") String newGender
            , @RequestParam("newColor") String newColor) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        Map<String, String> searchValues = setMapValues(furLevel, price, gender, color);
        if (_id != null && !_id.equals(""))
            searchValues.put("_id", _id);
        Map<String, String> newValues = setMapValues(newFurLevel, newPrice, newGender, newColor);
        updateValues(sessionUser, searchValues, newValues);
        return "updateCat";
    }

    private void updateValues(User user, Map<String, String> searchValues, Map<String, String> newValues) {
        HttpEntity<List<Map<String, String>>> request
                = new HttpEntity<>(Arrays.asList(searchValues, newValues), HeaderMaker.getHeaderForUser(user));
        restTemplate.exchange(UPDATE_CATS_URI, HttpMethod.POST, request, Object.class);
    }

    @GetMapping("/deleteCat")
    public String getDeleteCatPage(HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        return "deleteCat";
    }

    @PostMapping("/deleteCat")
    public String deleteCat(HttpSession session
            , @RequestParam("_id") String _id
            , @RequestParam("furLevel") String furLevel
            , @RequestParam("price") String price
            , @RequestParam("gender") String gender
            , @RequestParam("color") String color) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "login";
        Map<String, String> searchValues = setMapValues(furLevel, price, gender, color);
        if (_id != null && !_id.equals(""))
            searchValues.put("_id", _id);
        deleteDog(sessionUser, searchValues);
        return "redirect:adminHome";
    }

    private void deleteDog(User user, Map<String, String> matchValues) {
        HttpEntity<Map<String, String>> request
                = new HttpEntity<>(matchValues, HeaderMaker.getHeaderForUser(user));
        restTemplate.exchange(DELETE_CAT_URI, HttpMethod.POST, request, Object.class);
    }
}
