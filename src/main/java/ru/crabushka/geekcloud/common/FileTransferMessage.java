package ru.crabushka.geekcloud.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileTransferMessage extends AbstractMessage {
    private String fileName;
    private String path;
    private byte[] data;
    private int size;

    public FileTransferMessage(Path path) throws IOException {
        this.path = path.toString();
        this.fileName = path.getFileName().toString();
        this.data = Files.readAllBytes(path);
        this.size = data.length;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getData() {
        return data;
    }

    public String getPath() {
        return path;
    }
}
