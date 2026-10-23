
package database;
import java.sql.*;

public class DBConnection {
    
 Connection con;

String url = "jdbc:postgresql://localhost:5432/Inventory_tracker";
String user = "DB_USER";
String password = "DB_PASSWORD";

    public DBConnection() {

        con = getConnection();
    }

    public Connection getConnection(){
        Connection connection=null;
        try{
            Class.forName("org.postgresql.Driver");
            System.out.println("Driver loaded Successfully");

        }catch(ClassNotFoundException cnfe){
            System.out.println("Driver loading failed"+cnfe.getMessage());

        }

        try{
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Database connected successfully");
        }catch(SQLException sqle){
            System.out.println("Database connection failed"+sqle.getMessage());
        }
        return connection;
    }


}
