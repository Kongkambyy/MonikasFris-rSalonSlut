package com.example.projektopgave1.Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;

    private String url;
    private String username;
    private String password;

    // Private constructor to prevent instantiation
    private DatabaseConnection() {
        this.url = "jdbc:mysql://lasseblunckshillerx.tplinkdns.com:3306/MonikasFrisorsalon";
        this.username = "ZealandGruppen";
        this.password = "KunForZealand";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
