package pl.rafalpaprota.schedulerserver.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Table(name = "events")
@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column
    private Boolean allDay;

    @Column
    private Integer typeId;

    @Column
    private Integer statusId;

    @Column
    private String notes;

    @Column
    private String rRule;

    @Column
    private String exDate;

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    @Column
    private LocalDateTime dateFromArchiveCount;
}
