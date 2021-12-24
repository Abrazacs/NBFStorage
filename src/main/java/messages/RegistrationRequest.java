package messages;

import lombok.Getter;

@Getter
public class RegistrationRequest implements AbstractMessage{

    private String login;
    private String password;

    public RegistrationRequest(String login, String password){
        this.login = login;
        this.password = password;
    }

    @Override
    public MessageType getMessageType() {
        return null;
    }
}
