package messages;

import lombok.Getter;

@Getter
public class RegistrationConfirmed implements AbstractMessage{

    private boolean confirmed;

    public RegistrationConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.REGISTRATION_CONFIRMED;
    }
}
