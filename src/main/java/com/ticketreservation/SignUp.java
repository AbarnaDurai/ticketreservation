package com.ticketreservation;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.json.JSONObject;

@WebServlet("/SignUp")
public class SignUp extends HttpServlet {

    @Override
    public void init() throws ServletException {
        System.out.println("SignUp servlet initialized");
    }
    

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {

            String requestBody = request.getReader().lines().collect(Collectors.joining());
            JSONObject json = new JSONObject(requestBody);

            String username = json.optString("username").trim();
            String password = json.optString("password").trim();

            System.out.println("Username:" + username);
            System.out.println("Password:" + password);

            if (username.isEmpty() || password.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"Username and password must not be empty\"}");
                return;
            }

            String userStatus = checkUserExists(username);
            if (userStatus.equalsIgnoreCase("User exists")) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.write("{\"error\": \"User already exists\"}");
                return;
            }

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/booking", "root", "Abarna@1234")) {

                    Timestamp createdTime = Timestamp.valueOf(LocalDateTime.now());
                    String sql = "INSERT INTO ticketbooking (name, password, created_time) VALUES (?, ?, ?)";

                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, username);
                        stmt.setString(2, password);
                        stmt.setTimestamp(3, createdTime);
                        stmt.executeUpdate();
                    }

                    // Set cookie and get token
                    CookieValidation cookieValidation = new CookieValidation();
                    String sessionToken = cookieValidation.cookieSet(response);

                    // Update token in DB
                    String updateSql = "UPDATE ticketbooking SET session_token = ? WHERE name = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, sessionToken);
                        updateStmt.setString(2, username);
                        updateStmt.executeUpdate();
                    }

                    JSONObject jsonResponse = new JSONObject();
                    jsonResponse.put("message", "User registered successfully");
                    out.write(jsonResponse.toString());

                } catch (SQLException dbEx) {
                    dbEx.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.write("{\"error\": \"Database error: " + dbEx.getMessage() + "\"}");
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write("{\"error\": \"MySQL Driver not found\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Unexpected error occurred\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("Inside SignUp servlet doGet()");
        String username = request.getParameter("username");

        if (username != null && !username.trim().isEmpty()) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            try (PrintWriter out = response.getWriter()) {
                String userStatus = checkUserExists(username.trim());
                JSONObject jsonResponse = new JSONObject();

                if (userStatus.equalsIgnoreCase("User exists")) {
                    jsonResponse.put("status", "exists");
                } else if (userStatus.equalsIgnoreCase("User not found")) {
                    jsonResponse.put("status", "available");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    jsonResponse.put("error", userStatus);
                }

                out.write(jsonResponse.toString());
            }
        } else {
            request.getRequestDispatcher("/Signup.html").forward(request, response);
        }
    }

    protected String checkUserExists(String username) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/booking", "root", "Abarna@1234")) {

                String sql = "SELECT * FROM ticketbooking WHERE name = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, username);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        return "User exists";
                    } else {
                        return "User not found";
                    }
                }

            } catch (SQLException dbEx) {
                dbEx.printStackTrace();
                return "Database error: " + dbEx.getMessage();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return "MySQL Driver not found";
        } catch (Exception e) {
            e.printStackTrace();
            return "Unexpected error occurred";
        }
    }
}
