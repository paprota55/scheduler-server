package pl.rafalpaprota.schedulerserver.dto;

import lombok.Data;
import pl.rafalpaprota.schedulerserver.model.Settings;

@Data
public class SettingsDTO {
    private Integer newTime;
    private String password;

    public SettingsDTO() {
    }

    public SettingsDTO(Settings settings) {
        this.newTime = settings.getTimeToArchive();
    }
}

