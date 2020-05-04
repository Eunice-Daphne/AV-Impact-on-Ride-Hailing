package DataParsing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class JDBC_Connection {

    public static void main(String[] args)
    {
        try{
        // create our mysql database connection
        String databaseURL = "jdbc:mysql://localhost:3306/av_schema";
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(databaseURL, "root", "admin");
        if (conn == null) {
            System.out.println("Not Connected to the database");
        }

        // our SQL SELECT query.
        // if you only need a few columns, specify them by name instead of using "*"
        String query = "SELECT * FROM av_schema.assignedtable";

        // create the java statement
        Statement st = conn.createStatement();

        // execute the query, and get a java resultset
        ResultSet rs = st.executeQuery(query);

        // iterate through the java resultset
        while (rs.next())
        {

        }
        st.close();
    }
        catch (Exception e)
    {
        System.err.println("Got an exception! ");
        System.err.println(e.getMessage());
    }

        }
    }
