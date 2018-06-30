package ru.crabushka.geekcloud.common;

public class AuthMessage extends AbstractMessage {

    private String login;
    private String passwd;

    public AuthMessage(String login, String passwd) {
        this.login = login;
        this.passwd = passwd;
    }

    public String getLogin() {
        return login;
    }

    public String getPasswd() {
        return passwd;
    }
}
