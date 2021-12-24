package server;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import messages.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class AbstractMessageHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    private Path currentPath;
    private AuthorizationService authService;


    public AbstractMessageHandler(AuthorizationService service) {
        currentPath = Paths.get("serverFiles");
        this.authService = service;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(new FilesList(currentPath));
    }

    protected void channelRead0(ChannelHandlerContext ctx,
                                AbstractMessage message) throws Exception {
        log.debug("received: {}", message);
        switch (message.getMessageType()) {
            case USER:
                User user = (User) message;
                checkUserAuthorization(ctx, user);
                break;
            case FILE_REQUEST:
                FileRequest req = (FileRequest) message;
                ctx.writeAndFlush(
                        new FileMessage(currentPath.resolve(req.getFileName()))
                );
                break;
            case FILE:
                FileMessage fileMessage = (FileMessage) message;
                Files.write(
                        currentPath.resolve(fileMessage.getFileName()),
                        fileMessage.getBytes()
                );
                ctx.writeAndFlush(new FilesList(currentPath));
                break;
            case REGISTRATION_REQUEST:
                RegistrationRequest regRequest = (RegistrationRequest) message;
                confirmRegistration(ctx, regRequest);
        }
    }

    private void confirmRegistration(ChannelHandlerContext ctx, RegistrationRequest regRequest) {
        String login = regRequest.getLogin();
        String password = regRequest.getPassword();
        for (User authUsers: authService.getUserList()) {
            if (authUsers.getUserName().equals(login)){
                ctx.writeAndFlush(new RegistrationConfirmed(false));
                return;
            }
        }
        ctx.writeAndFlush(new RegistrationConfirmed(true));
        authService.addUser(login,password);
    }

    private void checkUserAuthorization (ChannelHandlerContext ctx, User user) {
        UserConfirmation confirmation = new UserConfirmation(
                authService.isUserAuthorized(user)
        );
        ctx.writeAndFlush(confirmation);
    }
}
