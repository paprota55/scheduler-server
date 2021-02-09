package pl.rafalpaprota.schedulerserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import pl.rafalpaprota.schedulerserver.model.Event;
import pl.rafalpaprota.schedulerserver.model.ExpiredEvent;

import java.time.LocalDateTime;

@Data
public class EventDTO {
    private Long id;

    private String title;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Boolean allDay;

    private Integer typeId;

    private Integer statusId;

    private Integer blockId;

    private String notes;

    @JsonProperty("rRule")
    private String rRule;

    private String exDate;

    public EventDTO() {
    }

    public EventDTO(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.startDate = event.getStartDate();
        this.endDate = event.getEndDate();
        this.typeId = event.getTypeId();
        this.statusId = event.getStatusId();
        this.notes = event.getNotes();
        this.rRule = event.getRRule();
        this.exDate = event.getExDate();
        this.allDay = event.getAllDay();
    }

    public EventDTO(ExpiredEvent event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.startDate = event.getStartDate();
        this.endDate = event.getEndDate();
        this.typeId = event.getTypeId();
        this.statusId = event.getStatusId();
        this.notes = event.getNotes();
        this.rRule = event.getRRule();
        this.exDate = event.getExDate();
        this.allDay = event.getAllDay();
    }
}
