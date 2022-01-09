package messages;


import client.NetworkService;
import lombok.Getter;

public class NewFolder implements AbstractMessage{

    @Getter
    private String folderName;

    public NewFolder(String folderName){
        this.folderName = folderName;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CREATE_NEW_FOLDER;
    }
}
