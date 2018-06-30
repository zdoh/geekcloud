package ru.crabushka.geekcloud.common;

public class CommandMessage extends AbstractMessage {

    private int command;
    public static final int LIST_FILES = 222;
    public static final int DOWNLOAD_FILE = 333;
    public static final int DELETE_FILE = 444;
    public static final int AUTH_OK = 555;
    public static final int CREATE_DIR = 666;

    private Object[] objects;

    public CommandMessage(int command, Object... objects) {
        this.command = command;
        this.objects = objects;
    }

    public int getCommand() {
        return command;
    }

    public Object[] getObjects() {
        return objects;
    }
}
