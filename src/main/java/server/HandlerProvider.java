package server;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class HandlerProvider {

    private final AuthorizationService authService;

    public HandlerProvider(AuthorizationService service){
        this.authService = service;
    }

    public ChannelHandler[] getSerializePipeline() {
        log.debug("AMS created");
        return new ChannelHandler[] {
                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                new ObjectEncoder(),
                new AbstractMessageHandler(authService)
        };

    }
}
