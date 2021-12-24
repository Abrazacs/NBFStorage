package server;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class HandlerProvider {

    private final AuthorizationService authService;

    public HandlerProvider(AuthorizationService service){
        this.authService = service;
    }

    public ChannelHandler[] getSerializePipeline() {
        return new ChannelHandler[] {
                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                new ObjectEncoder(),
                new AbstractMessageHandler(authService)
        };
    }
}
