package messages;

import lombok.Getter;

public class BigObjectEnd implements AbstractMessage{
    @Getter
    private String fileName;

    public  BigObjectEnd(String fileName){
        this.fileName = fileName;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.BIG_OBJECT_END_NOTIFICATION;
    }
}
