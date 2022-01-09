package server;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import messages.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

@Slf4j
public class AbstractMessageHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    private final Path ROOT_PATH = Paths.get("serverstorage");
    private Path currentPath;
    private AuthorizationService authService;
    private String userName;


    public AbstractMessageHandler(AuthorizationService service) {
        this.authService = service;
    }

    protected void channelRead0(ChannelHandlerContext ctx,
                                AbstractMessage message) throws Exception {
        log.debug("received: {}", message);
        switch (message.getMessageType()) {
            case USER:
                User user = (User) message;;
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
                break;
            case FILES_LIST_REQUEST:
                ctx.writeAndFlush(new FilesList(currentPath));
                break;
            case CHANGE_DIR:
                ChangeDir changeDir = (ChangeDir) message;
                Path path =currentPath.resolve(changeDir.getItemName());
                if (Files.isDirectory(path)){
                    currentPath = path;
                    ctx.writeAndFlush(new FilesList(currentPath));
                }
                break;
            case CHANGE_DIR_UP:
                if(!currentPath.getParent().equals(ROOT_PATH)){
                    currentPath = currentPath.getParent();
                    ctx.writeAndFlush(new FilesList(currentPath));
                }
                break;
            case CREATE_NEW_FOLDER:
                NewFolder folder = (NewFolder) message;
                Files.createDirectory(currentPath.resolve(folder.getFolderName()));
                ctx.writeAndFlush(new FilesList(currentPath));
                break;
        }
    }

    private void confirmRegistration(ChannelHandlerContext ctx, RegistrationRequest regRequest) throws SQLException {
        String login = regRequest.getLogin();
        String password = regRequest.getPassword();
        log.debug(login, " ", password);
        if(!authService.isUserExist(login)) {
            ctx.writeAndFlush(new RegistrationConfirmed(true));
            authService.addUser(login, password);
        } else{
            ctx.writeAndFlush(new RegistrationConfirmed(false));
        }
    }

    private void checkUserAuthorization (ChannelHandlerContext ctx, User user) throws SQLException {
        log.debug("checking auth");
        UserConfirmation confirmation = new UserConfirmation(
                authService.isUserAuthorized(user)
        );
        ctx.writeAndFlush(confirmation);
        if (authService.isUserAuthorized(user)){
            userName = user.getUserName();
            currentPath = Paths.get("serverstorage/", userName);
            try {
                Files.createDirectories(currentPath);
            } catch (IOException e){
               log.error("e=", e);
            }

        }
    }
}
