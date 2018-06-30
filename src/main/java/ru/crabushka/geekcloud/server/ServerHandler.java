package ru.crabushka.geekcloud.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private String nickName;
    private boolean logged;
    private final static String ROOT_DIR = "rootDir";
    private String clientDir;
    private Logger logger;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.write("Hello \n");
        ctx.write("Lets start at " + LocalDateTime.now());
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) return;
            if (msg instanceof AbstractMessage) {
                proccessMsg((AbstractMessage) msg, ctx);
            } else {
                System.out.println("wrong data");
            }

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.write("Got an error");
        System.out.println(cause.getMessage());
        ctx.close();
    }

    private void proccessMsg(AbstractMessage msg, ChannelHandlerContext ctx) {

        System.out.println("user: " + nickName);

        if (msg instanceof AuthMessage) {
            System.out.println("User " + nickName + "connect");

            checkAutorization((AuthMessage) msg, ctx);
        }

        if (logged) {
            if (msg instanceof FileTransferMessage) {
                saveFileToStorage((FileTranferMessage) msg);
            } else if (msg instanceof CommandMessage) {
                System.out.println("Server received a command " + ((CommandMessage) msg).getCommand());
                proccessCommand((CommandMessage) msg, ctx);
            }
        }


    }

    private void proccessCommand(CommandMessage msg, ChannelHandlerContext ctx) {
        if (msg.getCommand() == CommandMessage.LIST_FILES) {
            sendData(new FileListMessage(getClientFilesList(msg.getObject()[0])), ctx);
        } else if (msg.getCommand() == CommandMessage.DOWNLOAD_FILE) {
            try {
                Path filePath = Paths.get(clientDir, (String) (msg.getObject()[0]));
                sendData(new FileTransferMessage(filePath), ctx);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (msg.getCommand() == CommandMessage.DELETE_FILE) {
            deleteFileFromStorage((String) (msg.getObject()[0]));
        } else if (msg.getCommand() == CommandMessage.CREATE_DIR) {
            System.out.println("Create new directory by user " + nickName);
            createDirectory(msg);
        }
    }

    private void createDirectory(CommandMessage msg) {
        Object inObj1 = msg.getObject()[0];
        Object inObj2 = msg.getObject()[1];

        if (inObj1 instanceof  String && inObj2 instanceof String) {
            Path tempPath1 = Paths.get((String) inObj1);
            Path folderRootPath = Paths.get((String) inObj2);
            System.out.println(tempPath1.toString());
            System.out.println(folderRootPath.toString());

            Path newPath = Paths.get(clientDir + "/" + tempPath1.subpath(1, tempPath1.getNameCount()).toString());

            try {
                Files.createDirectories(newPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("CreateDirectory command is wrong");
        }
    }

    private void deleteFileFromStorage(String filename) {
        try {
            Files.delete(Paths.get(clientDir + "/" + filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFileToStorage(FileTransferMessage msg) {
        try {
            Path relPath = Paths.get(msg.getPath());
            String tempPath = relPath.subpath(1, relPath.getNameCount()).toString();
            Path newFilePath = Paths.get(clientDir + "/" + tempPath );

            if (Files.exists(newFilePath)) {
                Files.write(newFilePath, msg.getData(), StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                Files.write(newFilePath, msg.getData(), StandardOpenOption.CREATE);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    private List<String> getClientFilesList(Object folderName) {
        List<String> fileList = new ArrayList<>();

        if (folderName != null) {
            if (folderName.equals("..")) {
                if (!clientDir.equals(ROOT_DIR + "/" + nickName)) {
                    clientDir = Paths.get(clientDir).getParent().toString() + "/";
                    fileList.add("..");
                }
            } else {
                fileList.add("..");
                clientDir += folderName + "/";
            }
        }

        System.out.println(clientDir);

        try {
            Files.newDirectoryStream(
                    Paths.get(clientDir)).forEach(
                            p -> fileList.add(p.getFileName().toString())
                    );

        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileList;

    }

    private String getNickName(AuthMessage msg) {
        return DbConnector.getNickname(msg.getLogin(), msg.getPassword);
    }


    private void checkAutorization(AuthMessage incomingMsg, ChannelHandlerContext ctx) {
        if (incomingMsg != null) {

            nickName = getNickname(incomingMsg);

            if (nickName != null) {
                System.out.println("Client successfully autorized.");
                logged = true;
                clientDir = ROOT_DIR + "/" + nickName;
                sendData(new CommandMessage(CommandMessage.AUTH_OK, null), ctx);
            } else {
                System.out.println("Client not found");
                logged = false;
            }
        }
    }
}
