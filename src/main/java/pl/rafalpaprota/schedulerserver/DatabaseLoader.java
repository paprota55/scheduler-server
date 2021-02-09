package pl.rafalpaprota.schedulerserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.rafalpaprota.schedulerserver.dto.BlockDTO;
import pl.rafalpaprota.schedulerserver.model.Event;
import pl.rafalpaprota.schedulerserver.model.ExpiredEvent;
import pl.rafalpaprota.schedulerserver.model.User;
import pl.rafalpaprota.schedulerserver.repositories.EventRepository;
import pl.rafalpaprota.schedulerserver.repositories.ExpiredEventRepository;
import pl.rafalpaprota.schedulerserver.repositories.UserRepository;
import pl.rafalpaprota.schedulerserver.services.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Component
public class DatabaseLoader implements CommandLineRunner {

    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final SettingsService settingsService;
    private final BlockService blockService;
    private final EventService eventService;
    private final EventRepository eventRepository;
    private final ExpiredEventRepository expiredEventRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Value("${spring.datasource.username}")
    private String dataBase;

    @Autowired
    public DatabaseLoader(UserService userService, UserRepository userRepository, RoleService roleService, SettingsService settingsService, BlockService blockService, EventService eventService, EventRepository eventRepository, ExpiredEventRepository expiredEventRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.settingsService = settingsService;
        this.blockService = blockService;
        this.eventService = eventService;

        this.eventRepository = eventRepository;
        this.expiredEventRepository = expiredEventRepository;
    }

    @Override
    public void run(final String... strings) {

        if (this.dataBase.equals("postgres")) {
            this.roleService.addRole("USER");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

            User user = new User();
            user.setEmail("admin@o2.pl");
            user.setLogin("test");
            user.setPassword(this.passwordEncoder.encode("test"));
            user.setRole(this.roleService.getRoleByName("USER"));

            Long id = this.userService.addUser(user);
            user = this.userRepository.findById(id).get();
            this.settingsService.createNewSettings(user);
            this.blockService.addBlockToDB(new BlockDTO("blok1", LocalDateTime.parse("20201222T000000", formatter), LocalDateTime.parse("20210101T235959", formatter), "Notka"), user);
            this.userService.addUser(user);
            this.blockService.addBlockToDB(new BlockDTO("blok2", LocalDateTime.parse("20210102T000000", formatter), LocalDateTime.parse("20210111T235959", formatter), "Druga"), user);
            Event event = new Event();
            event.setUser(user);
            event.setExDate("");
            event.setRRule("INTERVAL=1;FREQ=DAILY;COUNT=30");
            event.setStatusId(0);
            event.setTypeId(12);
            event.setAllDay(false);
            event.setNotes("Witam");
            event.setDateFromArchiveCount(LocalDateTime.now().withHour(10).plusDays(30));
            event.setTitle("Pierwszy event");
            event.setStartDate(LocalDateTime.now().withHour(10));
            event.setEndDate(LocalDateTime.now().withHour(11));
            this.eventRepository.save(event);

            ExpiredEvent expiredEvent = new ExpiredEvent();
            expiredEvent.setUser(user);
            expiredEvent.setExDate("");
            expiredEvent.setRRule("INTERVAL=1;FREQ=DAILY;COUNT=30");
            expiredEvent.setStatusId(0);
            expiredEvent.setTypeId(12);
            expiredEvent.setAllDay(false);
            expiredEvent.setNotes("Witam");
            expiredEvent.setTitle("Testowa historia");
            expiredEvent.setStartDate(LocalDateTime.now().withHour(10).minusDays(1));
            expiredEvent.setEndDate(LocalDateTime.now().withHour(11).minusDays(1));
            this.expiredEventRepository.save(expiredEvent);
        }
    }
}
