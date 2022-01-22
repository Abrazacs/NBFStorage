package messages;

public class BigObjectStart implements AbstractMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.BIG_OBJECT_STAR_NOTIFICATION;
    }
}
