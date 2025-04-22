package com.ticketreservation;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.http.*;

public class CookieValidation {

	public String cookieSet(HttpServletResponse response) {
	    String sessionToken = UUID.randomUUID().toString();
	    Cookie sessionCookie = new Cookie("session_token", sessionToken);
	    sessionCookie.setMaxAge(60 * 60 * 24); 
	    sessionCookie.setPath("/"); 
	    response.addCookie(sessionCookie);
	    System.out.println("✅ Cookie set with token: " + sessionToken);
	    return sessionToken;
	}

    public String cookieRead(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("session_token".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    System.out.println("✅ Token from cookie: " + token);
                    return token;
                }
            }
        }
        return null; 
    }

    public void clearCookie(HttpServletResponse response) {
        Cookie sessionCookie = new Cookie("session_token", null);
        sessionCookie.setMaxAge(0); 
        sessionCookie.setPath("/"); 
        response.addCookie(sessionCookie);
        System.out.println("🚫 Session cookie cleared");
    }
}
