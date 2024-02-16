package com.mammon.documntdb.authentication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mammon.documntdb.dao.*;
import org.json.JSONArray;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import java.util.*;


@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthenticationFilter implements Filter {
    private static boolean isNewPasswordSet = false;
    private DAO databaseDAO;
    private String errors;
    private Optional<String> error;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        boolean firstTime = false;
        HttpServletRequest request = (HttpServletRequest) req;
        databaseDAO = new DatabaseDAO();
        error = Optional.empty();
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        try {
            startValidation(req, firstTime, httpRequest);
        } catch (InterruptedException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        req = redirectToError(req, (HttpServletRequest) request);
        chain.doFilter(req, res);
    }

    private void startValidation(ServletRequest req, boolean firstTime, HttpServletRequest httpRequest) throws IOException, InterruptedException, NoSuchAlgorithmException {
        Map<String, Object> userDetails = new HashMap<>();
        String name = httpRequest.getHeader("name");
        String password = httpRequest.getHeader("password");
        String role = httpRequest.getHeader("role");
        checkIfValidHeader(name, password, role);
        if (!error.isPresent())
            userDetails.put("username", name);
        if (!error.isPresent())
            firstTime = updateAdminFirstTime(httpRequest, userDetails, name, password);
        if (!error.isPresent())
            try {
                userDetails.put("password", SHA512.toHexString(SHA512.getSHA(password)));
                userDetails.put("role", role);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        if (!firstTime && !error.isPresent())
            checkIfValidUser(req, userDetails);
    }

    private void checkIfValidHeader(String name, String password, String role) {
        if ((name == null || name.equals("")) || (password == null || password.equals(""))
                || (role == null || role.equals("")))
            error = Optional.of(error.isPresent() ? error.get() + " not valid header" : " not valid header");
    }

    private boolean updateAdminFirstTime(HttpServletRequest httpRequest, Map<String, Object> userDetails, String name, String password) throws IOException, InterruptedException, NoSuchAlgorithmException {
        checkIfNewPasswordSet();
        if (name.equals("super") && password.equals("1234") && !isNewPasswordSet) {
            String newPassword = httpRequest.getHeader("new-password");
            if (newPassword == null || newPassword.equals("")) {
                error = Optional.of(error.isPresent() ? error.get() + "set new password" : "set new password");
                return true;
            } else {
                return setNewPassword(userDetails, password, newPassword);
            }
        }
        return false;
    }

    private boolean setNewPassword(Map<String, Object> userDetails, String password, String newPassword) throws IOException, InterruptedException, NoSuchAlgorithmException {
        Map<String, Object>[] updateUserDetails = new HashMap[2];
        userDetails.put("password", password);
        updateUserDetails[0] = userDetails;
        Map<String, Object> newUserDetails = new HashMap<>();
        try {
            newUserDetails.put("password", SHA512.toHexString(SHA512.getSHA(newPassword)));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        updateUserDetails[1] = newUserDetails;
        databaseDAO.updateV2("users", "user", updateUserDetails);
        isNewPasswordSet = true;
        return true;
    }

    private void checkIfNewPasswordSet() throws JsonProcessingException {
        if (isNewPasswordSet) return;
        Map<String, Object> defaultAdminData = new HashMap<>();
        defaultAdminData.put("username", "super");
        defaultAdminData.put("password", "1234");
        if (!databaseDAO.searchRead("users", "user", defaultAdminData).isPresent())
            isNewPasswordSet = true;
    }

    private void checkIfValidUser(ServletRequest req, Map<String, Object> userDetails) throws JsonProcessingException {
        Optional<JSONArray> result = databaseDAO.searchRead("users", "user", userDetails);
        if (!result.isPresent())
            error = Optional.of(error.isPresent() ? error.get() + "not valid user" : "not valid user");
        else {
            req.setAttribute("role", result.get().getJSONObject(0).getString("role"));
        }
    }

    private ServletRequest redirectToError(ServletRequest req, HttpServletRequest request) {
        final String errors = error.isPresent() ? error.get() : "no error";
        if (error.isPresent())
            req = new HttpServletRequestWrapper(request) {
                @Override
                public String getRequestURI() {
                    return "/error/" + errors;
                }
            };
        return req;
    }

    @Override
    public void destroy() {
        databaseDAO = null;
    }
}

