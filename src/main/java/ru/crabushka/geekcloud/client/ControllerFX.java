package ru.crabushka.geekcloud.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import ru.crabushka.geekcloud.common.AuthMessage;
import ru.crabushka.geekcloud.common.CommandMessage;
import ru.crabushka.geekcloud.common.FileTransferMessage;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ResourceBundle;

public class ControllerFX implements Initializable {

    @FXML
    public TextField usernameField;

    @FXML
    public PasswordField passwordField;

    @FXML
    public ListView localList;

    @FXML
    public ListView cloudList;

    @FXML
    public HBox authPanel;

    @FXML
    public HBox actionPanel1;

    @FXML
    public HBox actionPanel2;


    private CloudClient cloudClient;

    private String rootDir;

    private String command;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cloudClient = new CloudClient("localhost", 8189);

        rootDir = "clientDir";

        localList.setOnMouseClicked( mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (mouseEvent.getClickCount() == 2) {
                    System.out.println("Double clicked localList");
                    goDeeper((ListView) mouseEvent.getSource());
                }
            }
        });

        cloudList.setOnMouseClicked( mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (mouseEvent.getClickCount() == 2) {
                    System.out.println("Dpuble clicked cloudList");
                    goDeeper((ListView) mouseEvent.getSource());
                }
            }
        });
    }

    public void login() {
        System.out.println("client try to connect");
        String login = usernameField.getText().trim();
        String pass = passwordField.getText().trim();
        cloudClient.startReadingThread(this);
        cloudClient.sendMessage(new AuthMessage(login, pass));
    }


    private void send(String fileName) {
        Path filePath = Paths.get(rootDir, fileName);
        Platform.runLater( () -> {
            try {
                FileTransferMessage ftm = new FileTransferMessage(filePath);
                cloudClient.sendMessage(ftm);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    public void getCloudFilesList() {
        cloudClient.sendMessage(new CommandMessage(CommandMessage.LIST_FILES, command));

        Platform.runLater( () -> {
            cloudList.getItems().clear();

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            List<String> list = cloudClient.getFilesList();

            if (list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    cloudList.getItems().add(list.get(i));

                }
            } else {
                cloudList.getItems().add("hz");
            }

        });
    }

    public void uploadFileOrFolder(ActionEvent event) {
        String itemName = localList.getItems()
                .get(localList.getFocusModel().getFocusedIndex())
                .toString();

        Path path = Paths.get(rootDir, itemName);
        if (Files.isDirectory(path)) {
            try {
                sendFolder(path);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("hz2" + itemName);
            }
        } else {
            uploadFile(path);
        }
    }

    private void sendFolder(Path folderPath) throws IOException {
        Files.walkFileTree(folderPath, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                System.out.println("hz3" + path.toString());
                cloudClient.sendMessage(new CommandMessage(
                        CommandMessage.CREATE_DIR,
                        path.toString(), path.getParent().toString()
                ));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                System.out.println("hz4" + path.toString());
                uploadFile(path);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
                return FileVisitResult.TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void uploadFile(Path filePath) {
        System.out.println("Send file: " + filePath.getFileName().toString());
        try {
            cloudClient.sendMessage(new FileTransferMessage(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        getCloudFilesList();
    }


    public void downloadFile(ActionEvent event) {
        String fileName = cloudList.getItems().get(cloudList.getFocusModel().getFocusedIndex()).toString();

        System.out.println(fileName);
        cloudClient.sendMessage(new CommandMessage(CommandMessage.DOWNLOAD_FILE, fileName));
        getCloudFilesList();
    }

    public void deleteFile(ActionEvent event) {
        String fileName = cloudList.getItems().get(cloudList.getFocusModel().getFocusedIndex()).toString();
        System.out.println(filename);
        cloudClient.sendMessage(new CommandMessage(CommandMessage.DELETE_FILE, filename));
        getCloudFilesList();
    }

    public void deleteLocalFile() {
        String fileName = localList.getItems().get(localList.getFocusModel().getFocusedIndex()).toString();
        Path newFilePath = Paths.get(rootDir, fileName);
        System.out.println(fileName);
        try {
            Files.delete(newFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateLocalFilesList();
    }

    public void dragDropFile(DragEvent dragEvent) {
        System.out.println("Dran'n'Drop files");

        dragEvent.acceptTransferModes(TransferMode.LINK);
        Dragboard dragboard = dragEvent.getDragboard();
        List<File> files;

        if (dragboard.hasFiles()) {
            files = dragboard.getFiles();
            for (int i = 0l i < files.size(); i++) {
                System.out.println("Send file " + files.get(i).getName);
            }
        }
    }

    public void loginOk() {
        authPanel.setVisible(false);
        authPanel.setManaged(false);

        actionPanel1.setVisible(true);
        actionPanel2.setVisible(true);

        actionPanel1.setManaged(true);
        actionPanel2.setManaged(true);

        updateLocalFilesList();
        getCloudFilesList();
    }

    public void updateLocalFilesList() {
        localList.getItems().clear();
        localList.getItems().add(0, "..");

        try {
            Files.newDirectoryStream(Paths.get(rootDir)).forEach(
                    p -> localList.getItems().add(p.getFileName().toString()));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void goDeeper(ListView listView) {
        String itemName = listView.getItems().get(listView.getFocusModel().getFocusedIndex()).toString();
        if (itemName.equals("..")) {
            if (listView.equals(localList)) {
                rootDir = Paths.get(rootDir).getParent().toString();
                updateLocalFilesList();

            } else if (listView.equals(cloudList)) {
                command = "..";
                getCloudFilesList();
            }
        } else {
            if (listView.equals(localList)) {
                Path path = Paths.get(rootDir, itemName);
                if (Files.isDirectory(path)) {
                    rootDir += itemName + "/";
                    updateLocalFilesList();
                }
            } else if (listView.equals(cloudList)) {
                command = itemName;
                getCloudFilesList();
            }
        }
    }



}
