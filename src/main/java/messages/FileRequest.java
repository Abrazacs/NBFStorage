package messages;

import lombok.Data;

@Data
public class FileRequest implements AbstractMessage {

    private final String fileName;


    @Override
    public MessageType getMessageType() {
        return MessageType.FILE_REQUEST;
    }
}
