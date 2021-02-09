package pl.rafalpaprota.schedulerserver.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pl.rafalpaprota.schedulerserver.services.ExpiredEventsService;

@RestController
@CrossOrigin
@RequestMapping
public class ExpiredEventsController {
    private final ExpiredEventsService expiredEventsService;

    @Autowired
    public ExpiredEventsController(ExpiredEventsService expiredEventsService) {

        this.expiredEventsService = expiredEventsService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "api/expiredEvents/getEvents")
    public ResponseEntity<?> getCurrentUserEvents() {
        return ResponseEntity.ok(this.expiredEventsService.getCurrentUserExpiredEventsDTO());
    }
}
