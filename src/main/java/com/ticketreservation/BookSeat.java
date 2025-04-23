package com.ticketreservation;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/BookSeat")
public class BookSeat extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            String body = request.getReader().lines().collect(Collectors.joining());
            JSONObject json = new JSONObject(body);

            String username = json.optString("username");
            String seatNumber = json.optString("seatNumber");

            JSONObject jsonResponse = new JSONObject();

            if (username.isEmpty() || seatNumber.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.put("success", false);
                jsonResponse.put("error", "Username and seat number are required.");
                out.write(jsonResponse.toString());
                return;
            }

            if (isSeatBooked(seatNumber)) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                jsonResponse.put("success", false);
                jsonResponse.put("error", "Seat already booked");
                out.write(jsonResponse.toString());
                return;
            }

            if (bookSeat(username, seatNumber)) {
                response.setStatus(HttpServletResponse.SC_OK);
                jsonResponse.put("success", true);
                jsonResponse.put("message", "Seat booked successfully");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                jsonResponse.put("success", false);
                jsonResponse.put("error", "Failed to book seat");
            }

            out.write(jsonResponse.toString());
        }
    }

    private boolean isSeatBooked(String seatNumber) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT 1 FROM seatbooking WHERE seat_number = ? AND status = 'booked'";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, seatNumber);
                return stmt.executeQuery().next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean bookSeat(String username, String seatNumber) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO seatbooking (username, seat_number, status) VALUES (?, ?, 'booked')";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, seatNumber);
                return stmt.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private JSONArray getBookingHistory(String username) {
        JSONArray history = new JSONArray();
        try (Connection conn = getConnection()) {
            String sql = "SELECT seat_number, status FROM seatbooking WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    JSONObject historyItem = new JSONObject();
                    historyItem.put("seatNumber", rs.getString("seat_number"));
                    historyItem.put("status", rs.getString("status"));
                    history.put(historyItem);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return history;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        if (username != null && !username.isEmpty()) {
            JSONArray history = getBookingHistory(username);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            out.write(history.toString());
        } else {
            request.getRequestDispatcher("/Seat.html").forward(request, response);
        }
    }

    private Connection getConnection() throws Exception {
        String url = "jdbc:mysql://localhost:3306/busseate";
        String user = "root";
        String pass = "Abarna@1234";
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, user, pass);
    }
}
