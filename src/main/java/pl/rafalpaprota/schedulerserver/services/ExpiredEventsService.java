package pl.rafalpaprota.schedulerserver.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.rafalpaprota.schedulerserver.dto.EventDTO;
import pl.rafalpaprota.schedulerserver.model.Event;
import pl.rafalpaprota.schedulerserver.model.ExpiredEvent;
import pl.rafalpaprota.schedulerserver.model.User;
import pl.rafalpaprota.schedulerserver.repositories.ExpiredEventRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExpiredEventsService {
    private final ExpiredEventRepository expiredEventRepository;
    private final UserService userService;

    @Autowired
    public ExpiredEventsService(ExpiredEventRepository expiredEventRepository, UserService userService) {

        this.expiredEventRepository = expiredEventRepository;
        this.userService = userService;
    }

    public List<EventDTO> getCurrentUserExpiredEventsDTO() {
        User user = this.userService.getCurrentUser();
        List<ExpiredEvent> eventArrayList = this.expiredEventRepository.findAllByUser(user);
        ArrayList<EventDTO> eventDTOArrayList = new ArrayList<>();
        for (ExpiredEvent current : eventArrayList) {
            eventDTOArrayList.add(new EventDTO(current));
        }
        return eventDTOArrayList;
    }

    public void addExpiredEvent(Event event) {
        ExpiredEvent expiredEvent = new ExpiredEvent();
        expiredEvent.setTitle(event.getTitle());
        expiredEvent.setStartDate(event.getStartDate());
        expiredEvent.setEndDate(event.getEndDate());
        expiredEvent.setAllDay(event.getAllDay());
        expiredEvent.setTypeId(event.getTypeId());
        expiredEvent.setStatusId(4);
        expiredEvent.setNotes(event.getNotes());
        expiredEvent.setRRule(event.getRRule());
        expiredEvent.setExDate(event.getExDate());
        expiredEvent.setUser(event.getUser());
        this.expiredEventRepository.save(expiredEvent);
    }
}
