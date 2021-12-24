package messages;

import java.io.Serializable;

public interface AbstractMessage extends Serializable {

    MessageType getMessageType();
}
