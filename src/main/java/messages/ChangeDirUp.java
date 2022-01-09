package messages;

public class ChangeDirUp implements AbstractMessage{
    @Override
    public MessageType getMessageType() {
        return MessageType.CHANGE_DIR_UP;
    }
}
