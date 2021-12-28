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
        this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/archive_users","root","12_qswd_12" );
        this.statement =  connection.createStatement();
        log.debug("db connected");

    }

   boolean isUserAuthorized (User user) throws SQLException{
        log.debug("checking auth");
        pStatement = connection.prepareStatement("SELECT * FROM users WHERE login = ? AND password = ?");
        pStatement.setString(1, user.getUserName());
        pStatement.setString(2, user.getUserPassword());
        rSet = pStatement.executeQuery();
        if (rSet == null) return false;
        else return true;
   }




}
