package tn.esprit.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    public static MyDataBase instance;

    private final String URL="jdbc:mysql://127.0.0.1:3306/LegalLink";
    private final String USERNAME="root";
    private final String PASSWORD="";
    private Connection cnx;





    private MyDataBase (){
        try {
            cnx = DriverManager.getConnection(URL,USERNAME,PASSWORD);
            System.out.println("Connected to database");
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static MyDataBase getInstance(){
        if(instance==null){
            instance=new MyDataBase();

        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}
