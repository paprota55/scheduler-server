package pl.rafalpaprota.schedulerserver.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.rafalpaprota.schedulerserver.dto.EventDTO;
import pl.rafalpaprota.schedulerserver.model.Block;
import pl.rafalpaprota.schedulerserver.services.BlockService;
import pl.rafalpaprota.schedulerserver.services.EventService;

@RestController
@CrossOrigin
@RequestMapping
public class EventsController {
    private final EventService eventService;
    private final BlockService blockService;

    @Autowired
    public EventsController(EventService eventService, BlockService blockService) {
        this.eventService = eventService;
        this.blockService = blockService;
    }


    @RequestMapping(method = RequestMethod.POST, value = "api/events/addEvent")
    public ResponseEntity<?> addEvent(@RequestBody EventDTO eventDTO) {
        System.out.println(eventDTO);
        if (eventDTO.getStartDate() != null && eventDTO.getEndDate() != null) {
            if (eventDTO.getStartDate().isBefore(eventDTO.getEndDate())) {
                return ResponseEntity.ok(this.eventService.addNewEvent(eventDTO));
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Złe daty");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nie podano dat lub daty");
        }
    }

    @RequestMapping(method = RequestMethod.PUT, value = "api/events/changeEvent/{id}")
    public ResponseEntity<?> changeEvent(@PathVariable Long id, @RequestBody EventDTO eventDTO) {
        System.out.println(eventDTO);
        if (this.eventService.checkIfExist(id)) {
            if (eventDTO.getStartDate().isBefore(eventDTO.getEndDate())) {
                return ResponseEntity.ok(this.eventService.changeEvent(eventDTO));
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Wrong date.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Event with this id doesn't exist");
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "api/events/getEvents")
    public ResponseEntity<?> getCurrentUserEvents() {
        return ResponseEntity.ok(this.eventService.getCurrentUserEventsDTO());
    }

    @RequestMapping(method = RequestMethod.GET, value = "api/events/getEvents/{blockName}")
    public ResponseEntity<?> getCurrentUserEventsWhereBlockName(@PathVariable String blockName) {
        if (blockName.equals("all")) {
            return ResponseEntity.ok(this.eventService.getCurrentUserEventsDTO());
        } else {
            Block block = this.blockService.getCurrentUserBlockByName(blockName);
            if (block == null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("You haven't block with this name.");
            } else {
                return ResponseEntity.ok(this.eventService.getCurrentUserEventsByBlock(blockName));
            }
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "api/events/getEvents/block/{blockName}")
    public ResponseEntity<?> getCurrentUserEventsByBlock(@PathVariable String blockName) {
        return ResponseEntity.ok(this.eventService.getCurrentUserEventsByBlock(blockName));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "api/events/deleteEvent/{eventId}")
    public ResponseEntity<?> deleteUserEvent(@PathVariable final Long eventId) {
        if (eventId != null) {
            if (this.eventService.checkIfEventIsThisUser(eventId)) {
                this.eventService.deleteEvent(eventId);
                return ResponseEntity.ok("Deleted");
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("It isn't your event");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Id doesn't exist");
        }
    }


}
