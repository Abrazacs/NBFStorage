package server;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import messages.User;

import java.sql.*;


@Getter
@Slf4j
public class AuthorizationService {
    private static Connection connection;
    private static Statement statement;
    private PreparedStatement pStatement;
    private ResultSet rSet;

    public void start() throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/archive_users","root","12_qswd_12" );
        statement =  connection.createStatement();
        log.debug("db connected");

    }

   public boolean isUserAuthorized (User user) throws SQLException{
        log.debug("checking auth");
        pStatement = connection.prepareStatement("SELECT * FROM users WHERE login = ? AND password = ?");
        pStatement.setString(1, user.getUserName());
        pStatement.setString(2, user.getUserPassword());
        rSet = pStatement.executeQuery();
        return rSet.isBeforeFirst();
   }

   public boolean isUserExist(String login) throws SQLException{
       pStatement = connection.prepareStatement("SELECT * FROM users WHERE login = ?");
       pStatement.setString(1, login);
       rSet = pStatement.executeQuery();
       return rSet.isBeforeFirst();
   }


    public void addUser(String login, String password) throws SQLException {
        pStatement = connection.prepareStatement("INSERT INTO users (login, password) VALUES (?,?)");
        pStatement.setString(1,login);
        pStatement.setString(2,password);
        pStatement.executeUpdate();
    }

    public void stop() {
        try {
            rSet.close();
            pStatement.close();
            statement.close();
            connection.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
