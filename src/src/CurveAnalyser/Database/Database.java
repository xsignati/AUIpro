package src.CurveAnalyser.Database;

import java.sql.*;

/**
 * Created by Xsignati on 17.04.2017.
 */
public class Database {
    private String dbName;
    private String serverName;
    private String password;
    private String userName;
    private Connection conn;

    public Database(){
        dbName = "databasec1";
        password = "";
        userName = "root";
        serverName = "localhost";
    }

    public void connect(){
        try {
            conn = DriverManager.getConnection(
                    "jdbc:mariadb://" + serverName + "/" + dbName,
                    userName,
                    password);
        }
        catch(SQLException e){System.out.println("Error connecting database");}
    }

    public void disconnect(){
        try {
            conn.close();
        }
        catch (SQLException e){e.printStackTrace();}
    }

    public ResultSet select(String sql) throws SQLException {
        Statement st = conn.createStatement();
        return st.executeQuery(sql);
    }

    public void insert(String sql) throws SQLException{
        Statement st = conn.createStatement();
        st.executeQuery(sql);
    }

    public Connection getConn() {
        return conn;
    }
}
