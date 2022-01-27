package messages;

import lombok.Getter;

public class ChangeDir implements  AbstractMessage{

    @Getter
    private String itemName;

    public ChangeDir(String itemName){
        this.itemName = itemName;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CHANGE_DIR;
    }
}
