package client;

import messages.AbstractMessage;

public interface MessageProcessor {

    void processMessage(AbstractMessage message);
}
