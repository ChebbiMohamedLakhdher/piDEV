package com.example.pidev;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
    public Connection databaseLink;

    public static Connection getConnection() {
        String databaseName = "legallink";  // Change this to your database name
        String databaseUser = "root";               // Default XAMPP username is 'root'
        String databasePassword = "";               // Default XAMPP password is empty
        String url = "jdbc:mysql://localhost:3306/" + databaseName;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            databaseLink = DriverManager.getConnection(url, databaseUser, databasePassword);
        } catch(Exception e) {
            e.printStackTrace();
            e.getCause();
        }
        return databaseLink;
    }
}