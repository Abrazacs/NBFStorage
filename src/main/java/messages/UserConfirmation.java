package messages;

import lombok.Setter;


public class UserConfirmation implements AbstractMessage{

    private final boolean isAuthorized;

    public UserConfirmation(boolean isAuthorized){
        this.isAuthorized = isAuthorized;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.USER_CONFIRMATION;
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }
}
