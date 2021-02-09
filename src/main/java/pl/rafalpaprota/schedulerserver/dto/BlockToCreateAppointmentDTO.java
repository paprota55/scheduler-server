package pl.rafalpaprota.schedulerserver.dto;

import lombok.Data;

@Data
public class BlockToCreateAppointmentDTO {
    private Long id;
    private String text;

    public BlockToCreateAppointmentDTO() {
    }

    public BlockToCreateAppointmentDTO(Long id, String blockName) {
        this.id = id;
        this.text = blockName;
    }

}
