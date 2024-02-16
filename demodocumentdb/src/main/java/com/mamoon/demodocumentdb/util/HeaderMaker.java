package com.mamoon.demodocumentdb.util;

import com.mamoon.demodocumentdb.model.User;
import org.springframework.http.HttpHeaders;

public class HeaderMaker {
    public static HttpHeaders setNewPasswordHeader(User user, String newPassword) {
        HttpHeaders headers = getHeaderForUser(user);
        headers.add("new-password", newPassword);
        return headers;
    }

    public static HttpHeaders getHeaderForUser(User user) {
        HttpHeaders header = new HttpHeaders();
        header.add("name", user.getUsername());
        header.add("password", user.getPassword());
        header.add("role", user.getRole());
        return header;
    }
}
