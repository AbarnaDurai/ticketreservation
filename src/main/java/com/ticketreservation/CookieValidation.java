package com.ticketreservation;

import javax.servlet.http.*;
import java.util.UUID;
import java.io.IOException;

public class CookieValidation {

    
    public String cookieSet(HttpServletResponse response) {
        String sessionToken = UUID.randomUUID().toString();
        Cookie sessionCookie = new Cookie("session_token", sessionToken);
        sessionCookie.setMaxAge(60 * 60 * 24);
        response.addCookie(sessionCookie);
        System.out.println("Cookie set with token: " + sessionToken);
        return sessionToken;
    }

    
    public void cookieRead(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("session_token".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    System.out.println("Token from cookie: " + token);
                    response.getWriter().println("Session token: " + token);
                    return;
                }
            }
            response.getWriter().println("Session token cookie not found.");
        } else {
            response.getWriter().println("No cookies found.");
        }
    }
}
