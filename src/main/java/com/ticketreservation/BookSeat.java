package com.ticketreservation;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.json.JSONObject;

@WebServlet("/ticketreservation/bookSeat")

public class BookSeat extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            String requestBody = request.getReader().lines().collect(Collectors.joining());
            JSONObject json = new JSONObject(requestBody);

            String username = json.optString("username");
            String seatNumber = json.optString("seatNumber");

            System.out.println("Booking attempt - Username: " + username + ", Seat Number: " + seatNumber);

            if (username == null || username.trim().isEmpty() || seatNumber == null || seatNumber.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"Username and seat number must not be empty\"}");
                return;
            }

            if (isSeatBooked(seatNumber)) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.write("{\"error\": \"Seat " + seatNumber + " already booked\"}");
                return;
            }

            if (bookSeat(username, seatNumber)) {
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("message", "Seat " + seatNumber + " booked successfully!");
                response.setStatus(HttpServletResponse.SC_OK);
                out.write(jsonResponse.toString());
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write("{\"error\": \"Failed to book seat\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Unexpected error occurred\"}");
        }
    }

    private boolean isSeatBooked(String seatNumber) {
        String dbUrl = "jdbc:mysql://localhost:3306/busseate";
        String dbUser = "root";
        String dbPass = "Abarna@1234";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                String sql = "SELECT * FROM seatbooking WHERE seat_number = ? AND status = 'booked'";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, seatNumber);
                    ResultSet rs = stmt.executeQuery();
                    return rs.next();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean bookSeat(String username, String seatNumber) {
        String dbUrl = "jdbc:mysql://localhost:3306/busseate";
        String dbUser = "root";
        String dbPass = "Abarna@1234";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                String sql = "INSERT INTO seatbooking (username, seat_number, status) VALUES (?, ?, 'booked')";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, username);
                    stmt.setString(2, seatNumber);
                    return stmt.executeUpdate() > 0;
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
        request.getRequestDispatcher("/Seat.html").forward(request, response);
    }
}
