package br.com.fiap.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
    private static Connection connection;

    /** Abre uma conexão exclusiva para operações que precisam de transação. */
    public static Connection getNewConnection() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("Erro nome da classe: " + e.getMessage());
            return null;
        }

        String url = System.getenv("DB_URL");
        String usuario = System.getenv("DB_USER");
        String senha = System.getenv("DB_PASSWORD");
        if (url == null || usuario == null || senha == null) {
            System.out.println("As variáveis de ambiente do banco não estão configuradas corretamente");
            return null;
        }
        try {
            return DriverManager.getConnection(url, usuario, senha);
        } catch (SQLException e) {
            System.out.println("Erro de SQL: " + e.getMessage());
            return null;
        }
    }

    public static void closeConnection(){
        try {
            if (!connection.isClosed()){
                connection.close();
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public static Connection getConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }

            Class.forName("oracle.jdbc.driver.OracleDriver");

            //busca dados de conexão das variáveis de ambiente
            String url = System.getenv("DB_URL");
            String usuario = System.getenv("DB_USER");
            String senha = System.getenv("DB_PASSWORD");

            if (url == null || usuario
                    == null || senha == null) {
                throw new RuntimeException("As variaveis de ambiente do banco não estão configuradas corretamente");
            }

            connection = DriverManager.getConnection(url,usuario,senha);


        } catch (SQLException e) {
            System.out.println("Erro de SQL: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Erro nome da clsse: " + e.getMessage());
        }
        return connection;
    }
}
