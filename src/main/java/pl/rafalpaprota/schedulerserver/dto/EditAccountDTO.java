package pl.rafalpaprota.schedulerserver.dto;

import lombok.Data;

@Data
public class EditAccountDTO {

    private String newEmail;
    private String password;
    private String newPassword;
}
