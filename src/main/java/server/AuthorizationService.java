package server;

import lombok.Getter;
import messages.User;

import java.util.*;

@Getter
public class AuthorizationService {

    private List<User> userList;

    public AuthorizationService(){
        this.userList = new ArrayList<>(
                Arrays.asList(
                        new User("user1", "123"),
                        new User("user2", "123"),
                        new User("user3", "123")
                )
        );

    }

   boolean isUserAuthorized (User user){
       for (User authorizedUsers: userList) {
           if (authorizedUsers.equals(user)) return true;
       }
       return false;
   }

   public void addUser (String login, String password){
        userList.add(new User(login, password));
   }



}
