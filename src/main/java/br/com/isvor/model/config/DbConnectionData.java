package br.com.isvor.model.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DbConnectionData {

    private String url;
    private String port;
    private String user;
    private String password;
    private String schema;


    @Override
    public String toString() {
        return "jdbc:mysql://" + url + ":" + port + "/" + schema + "?useSSL=false";
    }
}
