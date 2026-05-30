package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=Zalora_Kelompok2_BasDat;encrypt=false;";
    private static final String USER = "sa";
    private static final String PASSWORD = "password";


    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC SQL Server tidak ditemukan! Pastikan .jar sudah ada di Referenced Libraries.");
            throw new SQLException("Driver tidak ditemukan", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

        public static void closeConnection() {
            }
}
