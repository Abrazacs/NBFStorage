package server;

import lombok.Getter;
import messages.User;

import java.sql.*;


@Getter
public class AuthorizationService {
    private static Connection connection;
    private static Statement statement;
    private PreparedStatement pStatement;
    private ResultSet rSet;

    public void start() throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/archive_users","Abrazacs","12_qswd_12" );
        this.statement =  connection.createStatement();

    }

   boolean isUserAuthorized (User user) throws SQLException{
        pStatement = connection.prepareStatement("SELECT * FROM users WHERE login = ? AND password = ?");
        pStatement.setString(1, user.getUserName());
        pStatement.setString(2, user.getUserPassword());
        return !rSet.wasNull();
   }




}
