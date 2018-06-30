package ru.crabushka.geekcloud.common;

public class RegisterUserMessage extends AbstractMessage {
    private String fio;
    private String username;
    private String password;
    private String nickname;

    public RegisterUserMessage(String fio, String username, String password, String nickname) {
        this.fio = fio;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
    }

    public String getFio() {
        return fio;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }
}
