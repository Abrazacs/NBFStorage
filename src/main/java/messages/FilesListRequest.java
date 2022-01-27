package messages;

public class FilesListRequest implements AbstractMessage{
    @Override
    public MessageType getMessageType() {
        return MessageType.FILES_LIST_REQUEST;
    }
}
