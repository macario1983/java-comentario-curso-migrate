package br.com.isvor.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DbConnectionData {

    private String url;
    private String port;
    private String user;
    private String password;
    private String database;
}
