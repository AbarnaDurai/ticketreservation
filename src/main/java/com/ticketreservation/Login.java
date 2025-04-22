package com.ticketreservation;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

@WebServlet("/login")
public class Login extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json"); 
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {

            String requestBody = request.getReader().lines().collect(Collectors.joining());
            JSONObject json = new JSONObject(requestBody); 

            String username = json.optString("username");
            String password = json.optString("password");

            System.out.println("login attempt - username: " + username);

            if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"Username and password must not be empty\"}");
                return;
            }

            if (validateLogin(username, password)) {
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("message", "Login successful");
                response.setStatus(HttpServletResponse.SC_OK);
                out.write(jsonResponse.toString());
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("{\"error\": \"Invalid username or password\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Unexpected error occurred\"}");
        }
    }

    private boolean validateLogin(String username, String password) {
        String dbUrl = "jdbc:mysql://localhost:3306/booking";
        String dbUser = "root";
        String dbPass = "Abarna@1234";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                String sql = "SELECT * FROM ticketbooking WHERE name = ? AND password = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    ResultSet rs = stmt.executeQuery();
                    return rs.next();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/login.html").forward(request, response);
    }
}
