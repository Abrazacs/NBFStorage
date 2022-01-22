package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import lombok.Setter;
import messages.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ControllerMainScene implements MessageProcessor {
    public ListView clientViewField;
    public ListView serverViewField;
    public Button downloadButton;
    public Button uploadButton;
    public Button changeDir;
    public Button changeServerDir;
    private boolean smallFile = true;


    @Setter
    private NetworkService networkService;


    public void launch () throws  IOException{
        fillView(getFileNames(),clientViewField);
        networkService.sendMessage(new FilesListRequest());

        clientViewField.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String file = (String) clientViewField.getSelectionModel().getSelectedItem();
                Path path = networkService.getCurrentDir().resolve(file);
                if (Files.isDirectory(path)) {
                    networkService.setCurrentDir(path);
                    fillView(getFileNames(), clientViewField);
                }
            }
        });

        serverViewField.setOnMouseClicked(e ->{
            if (e.getClickCount() == 2) {
                String file = (String) serverViewField.getSelectionModel().getSelectedItem();
                try {
                    networkService.sendMessage(new ChangeDir(file));
                } catch (IOException exception){
                    exception.printStackTrace();
                }
            }
        });

    }

    public void download(ActionEvent event) throws IOException {
        String file = (String) serverViewField.getSelectionModel().getSelectedItem();
        networkService.sendMessage(new FileRequest(file));
    }

    public void upload(ActionEvent event) throws IOException{
        String file = (String) clientViewField.getSelectionModel().getSelectedItem();
        Path filePath = networkService.getCurrentDir().resolve(file);
        if (filePath.toFile().length()<= 1048576)
            networkService.sendMessage(new FileMessage(filePath));
        else {
            networkService.sendBigObject(filePath);
        }
    }



    private List<String> getFileNames() {
        try {
            return Files.list(networkService.getCurrentDir())
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }




    @Override
    public void processMessage(AbstractMessage message){
        switch (message.getMessageType()){
            case FILES_LIST:
                FilesList files = (FilesList) message;
                Platform.runLater(() -> fillView(files.getFiles(),serverViewField));
                break;
            case FILE:
                FileMessage file = (FileMessage) message;
                try {
                    Files.write(networkService.getCurrentDir().resolve(file.getFileName()), file.getBytes());
                }catch (IOException e){
                    e.printStackTrace();
                }
                if(smallFile){
                    Platform.runLater(()->fillView(getFileNames(), clientViewField));
                }
                break;
            case BIG_OBJECT_STAR_NOTIFICATION:
                smallFile = false;
                networkService.setDesiredDir(networkService.getCurrentDir());
                try {
                    networkService.setCurrentDir(
                            Files.createDirectory(
                                    networkService.getCurrentDir().resolve("tempNBFS"))
                        );
                }catch (IOException e){
                    e.printStackTrace();
                }
                break;
            case BIG_OBJECT_END_NOTIFICATION:
                BigObjectEnd end = (BigObjectEnd) message;
                String fileName = end.getFileName();
                assemblyFile(networkService.getDesiredDir(),fileName);
                deleteTempDir();
                networkService.setCurrentDir(
                        networkService.getDesiredDir());
                Platform.runLater(()->fillView(getFileNames(), clientViewField));
                break;
        }
    }

    private void deleteTempDir() {
        List<File> fileList = receiveListOfFiles();
        for (File f: fileList){
            f.delete();
        }
        networkService.getCurrentDir().toFile().delete();
    }

    private void assemblyFile(Path desiredDir, String fileName) {
        List<File> fileList = receiveListOfFiles();
        File file = new File(
                desiredDir.toString(),fileName);
        try(BufferedOutputStream assemblyStream = new BufferedOutputStream(new FileOutputStream(file))){
            for (File f: fileList){
                Files.copy(f.toPath(),assemblyStream);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private List<File> receiveListOfFiles() {
        try {
            List<File> fileList = Files.list(networkService.getCurrentDir()).
                    map(p -> p.toFile()).
                    collect(Collectors.toList());
            return fileList;
        } catch (IOException e){
            e.printStackTrace();
        }
        return new ArrayList<File>();
    }

    private void fillView(List<String> list, ListView view) {
       view.getItems().clear();
       view.getItems().addAll(list);
    }


    public void changeDirOneLevelUp(ActionEvent event) {
        if(!networkService.getCurrentDir().getParent().equals(null)){
            networkService.setCurrentDir(networkService.getCurrentDir().getParent());
            fillView(getFileNames(), clientViewField);
        }
    }

    public void changeServerDirUp(ActionEvent event) {
        try{
            networkService.sendMessage(new ChangeDirUp());
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public void createNewFolderWindow(ActionEvent event) throws Exception {
        Stage additionalStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource("newFolderName.fxml"));
        Parent parent = loader.load();
        NewFolderController controller = loader.getController();
        controller.setStage(additionalStage);
        controller.setNetworkService(networkService);
        Scene scene = new Scene(parent);
        additionalStage.setScene(scene);
        additionalStage.show();
    }


}
