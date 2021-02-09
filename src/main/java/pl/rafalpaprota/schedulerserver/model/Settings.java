package pl.rafalpaprota.schedulerserver.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "settings")
@Entity
public class Settings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false)
    private Integer timeToArchive;
}
