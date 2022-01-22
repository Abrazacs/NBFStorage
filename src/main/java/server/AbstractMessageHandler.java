package server;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import messages.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AbstractMessageHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    private final Path ROOT_PATH = Paths.get("serverstorage");
    private Path currentPath;
    private Path desiredPath;
    private AuthorizationService authService;
    private String userName;
    private boolean smallFile = true;


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
                sendFile(req, ctx);
                break;
            case FILE:
                FileMessage fileMessage = (FileMessage) message;
                Files.write(
                        currentPath.resolve(fileMessage.getFileName()),
                        fileMessage.getBytes()
                );
                if(smallFile)
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
            case BIG_OBJECT_STAR_NOTIFICATION:
                smallFile = false;
                desiredPath = currentPath;
                currentPath = Files.createDirectory(ROOT_PATH.resolve(userName).resolve("temp"));
                break;
            case BIG_OBJECT_END_NOTIFICATION:
                smallFile = true;
                BigObjectEnd end = (BigObjectEnd) message;
                String fileName = end.getFileName();
                assemblyFile(desiredPath, fileName);
                deleteTempDir();
                currentPath = desiredPath;
                ctx.writeAndFlush(new FilesList(currentPath));
                break;
        }
    }

    private void sendFile(FileRequest request, ChannelHandlerContext ctx){
      Path file = currentPath.resolve(request.getFileName());
        if(file.toFile().length()<=1024*1000){
          try{
              ctx.writeAndFlush(
                  new FileMessage(file)
              );
          }catch (IOException e){
              log.info("e=", e);
          }
        }else{
            sendBigObject(file, ctx);
        }
    }

    private void sendBigObject(Path file, ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new BigObjectStart());
        log.debug("BigObjectStartNotification");

        int partOfTheObjectSize = 1024*1000;
        byte[] buffer = new byte[partOfTheObjectSize];
        int counter = 1;
        String fileName = file.toFile().getName();

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file.toFile()))){
            while (bis.read(buffer) > 0){
                String partOfTheFileName = fileName + counter;
                File partOfTheFile = new File(
                        file.toFile().getParent(),
                        partOfTheFileName);
                try (FileOutputStream fos = new FileOutputStream(partOfTheFile)){
                    fos.write(buffer);
                }
                log.debug("partOfTheFile");
                ctx.writeAndFlush(new FileMessage(partOfTheFile.toPath()));
                partOfTheFile.delete();
                counter++;
            }
            log.debug("BigObjectEndNotification");
            ctx.writeAndFlush(new BigObjectEnd(fileName));

        }catch (IOException e){
            log.info("e=", e);
        }

    }

    private void deleteTempDir() {
        List<File> fileList = receiveListOfFiles();
        for (File f: fileList){
            f.delete();
        }
        currentPath.toFile().delete();
    }

    private void assemblyFile(Path desiredPath, String fileName) {
        List<File> fileList = receiveListOfFiles();
        File file = new File(desiredPath.toString(),fileName);
        try(BufferedOutputStream assemblyStream = new BufferedOutputStream(new FileOutputStream(file))){
            for (File f: fileList){
                Files.copy(f.toPath(),assemblyStream);
            }
        }catch (IOException e){
            log.info("e=", e);
        }

    }

    private List<File> receiveListOfFiles() {
        try {
            List<File> fileList = Files.list(currentPath).
                    map(p -> p.toFile()).
                    collect(Collectors.toList());
            return fileList;
        } catch (IOException e){
            log.info("e=", e);
        }
        return new ArrayList<File>();
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
            currentPath = ROOT_PATH.resolve(userName);
            try {
                Files.createDirectories(currentPath);
            } catch (IOException e){
               log.error("e=", e);
            }

        }
    }
}
