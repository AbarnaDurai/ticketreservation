package com.ticketreservation;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/CookieServlet")
public class CookieServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Cookie[] cookies = request.getCookies();
        
        if (cookies != null) {
            for (Cookie c : cookies) {
                System.out.println("Cookie Name: " + c.getName() + ", Value: " + c.getValue());
            }
        } else {
            System.out.println("No cookies found.");
        }

        boolean isLoggedIn = false;
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("session_token".equals(c.getName()) && c.getValue() != null && !c.getValue().isEmpty()) {
                    isLoggedIn = true;
                    break;
                }
            }
        }

        if (isLoggedIn) {
            response.sendRedirect("Seat.html");
        } else {
            response.sendRedirect("Signup.html");
        }
    }

    }

