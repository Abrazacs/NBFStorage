package messages;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class User implements AbstractMessage{
    private String userName;
    private String userPassword;

    public User (String userName, String userPassword){
        this.userName = userName;
        this.userPassword = userPassword;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.USER;
    }


}
