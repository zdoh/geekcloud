package ru.crabushka.geekcloud.client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import ru.crabushka.geekcloud.common.AbstractMessage;
import ru.crabushka.geekcloud.common.CommandMessage;
import ru.crabushka.geekcloud.common.FileListMessage;
import ru.crabushka.geekcloud.common.FileTransferMessage;


import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class CloudClient {
    private Socket clientSocket;
    private ObjectDecoderInputStream inputStream;
    private ObjectEncoderOutputStream outputStream;
    private boolean isConnected;
    private List<String> filesList;

    public CloudClient(String address, int port) {
        try {
            clientSocket = new Socket(address, port);
            outputStream = new ObjectEncoderOutputStream(clientSocket.getOutputStream());
            inputStream = new ObjectDecoderInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        isConnected = true;
    }

    public synchronized void sendMessage(AbstractMessage outgoingMessage) {
        try {
            outputStream.writeObject(outgoingMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void startReadingThread(ControllerFX controllerFX) {
        new Thread( () -> {
            try {
                while (isConnected) {
                    Object msg = inputStream.readObject();

                    if (msg != null) {
                        System.out.println("One message received: " + msg.toString());

                        if (msg instanceof AbstractMessage) {
                            AbstractMessage incomingMsg = (AbstractMessage) msg;

                            if (incomingMsg instanceof CommandMessage) {
                                CommandMessage cmdMsg = (CommandMessage) incomingMsg;
                                if (cmdMsg.getCommand() == CommandMessage.AUTH_OK) {
                                    System.out.println("AUTHOK");
                                    controllerFX.loginOk();
                                }
                            }

                            if (incomingMsg instanceof FileListMessage) {
                                filesList = ((FileListMessage) incomingMsg).getFileList();
                            }

                            if (incomingMsg instanceof FileTransferMessage) {
                                saveFileToStorage((FileTransferMessage) incomingMsg);
                            }
                        }
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void saveFileToStorage(FileTransferMessage incomingMsg) {
        try {
            Path newFilePath = Paths.get("clientDir" + "/" + incomingMsg.getFileName());

            if (Files.exists(newFilePath)) {
                Files.write(
                        newFilePath,
                        incomingMsg.getData(),
                        StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                Files.write(
                        newFilePath,
                        incomingMsg.getData(),
                        StandardOpenOption.CREATE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getFilesList() {
        return filesList;
    }



}
