package pl.rafalpaprota.schedulerserver.dto;

import lombok.Data;

@Data
public class LogRegUserDTO {
    private String login;
    private String email;
    private String password;
}
