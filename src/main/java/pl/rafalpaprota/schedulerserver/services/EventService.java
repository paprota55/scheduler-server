package pl.rafalpaprota.schedulerserver.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.rafalpaprota.schedulerserver.dto.EventDTO;
import pl.rafalpaprota.schedulerserver.model.Block;
import pl.rafalpaprota.schedulerserver.model.Event;
import pl.rafalpaprota.schedulerserver.model.Settings;
import pl.rafalpaprota.schedulerserver.model.User;
import pl.rafalpaprota.schedulerserver.repositories.EventRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final UserService userService;
    private final BlockService blockService;
    private final SettingsService settingsService;
    private final ExpiredEventsService expiredEventsService;

    @Autowired
    public EventService(EventRepository eventRepository, UserService userService, BlockService blockService, SettingsService settingsService, ExpiredEventsService expiredEventsService) {
        this.eventRepository = eventRepository;
        this.userService = userService;
        this.blockService = blockService;
        this.settingsService = settingsService;
        this.expiredEventsService = expiredEventsService;
    }

    public Long addNewEvent(EventDTO eventDTO) {
        Event newEvent = new Event();
        eventDTO.setStartDate(eventDTO.getStartDate().plusHours(1));
        eventDTO.setEndDate(eventDTO.getEndDate().plusHours(1));
        newEvent.setDateFromArchiveCount(calculateEndDate(eventDTO));
        newEvent.setId(null);
        if (eventDTO.getAllDay() != null) {
            newEvent.setAllDay(eventDTO.getAllDay());
        } else {
            newEvent.setAllDay(false);
        }
        newEvent.setEndDate(eventDTO.getEndDate());
        newEvent.setStartDate(eventDTO.getStartDate());
        newEvent.setTitle(eventDTO.getTitle());
        newEvent.setNotes(eventDTO.getNotes());
        if (eventDTO.getTypeId() != null) {
            newEvent.setTypeId(eventDTO.getTypeId());
        } else {
            newEvent.setTypeId(0);
        }
        newEvent.setStatusId(0);
        newEvent.setRRule(eventDTO.getRRule());
        newEvent.setExDate(eventDTO.getExDate());
        newEvent.setUser(this.userService.getCurrentUser());
        System.out.println("New event add");
        System.out.println(newEvent);
        return this.eventRepository.save(newEvent).getId();
    }

    public LocalDateTime calculateEndDate(EventDTO eventDTO) {
        LocalDateTime endDate;

        if (eventDTO.getRRule().equals("")) {
            endDate = eventDTO.getEndDate();
        } else {

            String newRrule = eventDTO.getRRule().replace("RRULE:", "");
            eventDTO.setRRule(newRrule);
            Block block = null;
            if (eventDTO.getBlockId() != 0) {
                block = this.blockService.getBlockBySortedIdFromScheduler(eventDTO.getBlockId());
            }
            if (newRrule.contains("FREQ=DAILY")) {
                endDate = calculateByDaily(eventDTO, block);
            } else if (newRrule.contains("FREQ=WEEKLY")) {
                endDate = calculateByWeekly(eventDTO, block);
            } else if (newRrule.contains("FREQ=MONTHLY")) {
                endDate = calculateByMonthly(eventDTO, block);
            } else {
                endDate = calculateByYearly(eventDTO, block);
            }
        }

        return endDate;
    }

    private LocalDateTime calculateByYearly(EventDTO eventDTO, Block block) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        LocalDateTime endDate = null;
        String[] parts = eventDTO.getRRule().split(";");
        StringBuilder newRrule = new StringBuilder();
        if (block != null) {
            changeIfBeforeOrAfterBlock(eventDTO, block);
            if (eventDTO.getRRule().contains("COUNT")) {
                for (String part : parts) {
                    if (part.contains("COUNT")) {
                        String endTimeString = part.replace("COUNT=", "");
                        int count = Integer.parseInt(endTimeString);
                        LocalDateTime endTime = eventDTO.getEndDate().plusYears(count);
                        endDate = endTime;
                        if (endTime.isAfter(block.getDateTo())) {
                            endDate = block.getDateTo();
                            newRrule.append("UNTIL=").append(endDate.format(formatter)).append(";");
                        } else {
                            newRrule.append(part).append(";");
                        }

                    } else if (part.contains("UNTIL")) {
                        String endTimeString = part.replace("UNTIL=", "");
                        endTimeString = endTimeString.replace("Z", "");
                        LocalDateTime endTime = LocalDateTime.parse(endTimeString, formatter);

                        if (endTime.isAfter(block.getDateTo())) {
                            newRrule.append("UNTIL=").append(block.getDateTo().format(formatter)).append(";");
                            endDate = block.getDateTo();
                        } else if (endTime.isBefore(block.getDateFrom())) {
                            newRrule.append("UNTIL=").append(eventDTO.getEndDate().format(formatter)).append(";");
                            endDate = eventDTO.getEndDate();
                        } else {
                            newRrule.append("UNTIL=").append(endTime.format(formatter)).append(";");
                            endDate = endTime;
                        }
                    } else {
                        newRrule.append(part).append(";");
                    }
                }
                eventDTO.setRRule(newRrule.substring(0, newRrule.length() - 1));
            }
        } else {
            for (String part : parts) {
                if (part.contains("UNTIL")) {
                    String endTimeString = part.replace("UNTIL=", "");
                    endTimeString = endTimeString.replace("Z", "");
                    endDate = LocalDateTime.parse(endTimeString, formatter);
                    if (endDate.isBefore(eventDTO.getEndDate())) {
                        endDate = eventDTO.getEndDate();
                        newRrule.append("UNTIL=").append(endDate.format(formatter)).append(";");
                    }
                } else if (part.contains("COUNT")) {
                    String endTimeString = part.replace("COUNT=", "");
                    int count = Integer.parseInt(endTimeString);
                    endDate = eventDTO.getEndDate().plusYears(count);
                } else {
                    newRrule.append(part).append(";");
                }
            }
            eventDTO.setRRule(newRrule.substring(0, newRrule.length() - 1));
        }

        return endDate;
    }

    private LocalDateTime calculateByMonthly(EventDTO eventDTO, Block block) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        LocalDateTime endDate = null;
        String[] parts = eventDTO.getRRule().split(";");
        StringBuilder newRrule = new StringBuilder();
        if (block != null) {
            changeIfBeforeOrAfterBlock(eventDTO, block);
            if (eventDTO.getRRule().contains("COUNT")) {
                for (String part : parts) {
                    if (part.contains("COUNT")) {
                        String endTimeString = part.replace("COUNT=", "");
                        int count = Integer.parseInt(endTimeString);
                        LocalDateTime endTime = eventDTO.getEndDate().plusMonths(count);
                        endDate = endTime;
                        if (endTime.isAfter(block.getDateTo())) {
                            endDate = block.getDateTo();
                            newRrule.append("UNTIL=").append(endDate.format(formatter)).append(";");
                        } else {
                            newRrule.append(part).append(";");
                        }

                    } else if (part.contains("UNTIL")) {
                        String endTimeString = part.replace("UNTIL=", "");
                        endTimeString = endTimeString.replace("Z", "");
                        LocalDateTime endTime = LocalDateTime.parse(endTimeString, formatter);

                        if (endTime.isAfter(block.getDateTo())) {
                            newRrule.append("UNTIL=").append(block.getDateTo().format(formatter)).append(";");
                            endDate = block.getDateTo();
                        } else if (endTime.isBefore(block.getDateFrom())) {
                            newRrule.append("UNTIL=").append(eventDTO.getEndDate().format(formatter)).append(";");
                            endDate = eventDTO.getEndDate();
                        } else {
                            newRrule.append("UNTIL=").append(endTime.format(formatter)).append(";");
                            endDate = endTime;
                        }
                    } else {
                        newRrule.append(part).append(";");
                    }
                }
                eventDTO.setRRule(newRrule.substring(0, newRrule.length() - 1));
            }
        } else {
            for (String part : parts) {
                if (part.contains("UNTIL")) {
                    String endTimeString = part.replace("UNTIL=", "");
                    endTimeString = endTimeString.replace("Z", "");
                    endDate = LocalDateTime.parse(endTimeString, formatter);
                    if (endDate.isBefore(eventDTO.getEndDate())) {
                        endDate = eventDTO.getEndDate();
                        newRrule.append("UNTIL=").append(endDate.format(formatter)).append(";");
                    }
                } else if (part.contains("COUNT")) {
                    String endTimeString = part.replace("COUNT=", "");
                    int count = Integer.parseInt(endTimeString);
                    endDate = eventDTO.getEndDate().plusMonths(count);
                } else {
                    newRrule.append(part).append(";");
                }
            }
            eventDTO.setRRule(newRrule.substring(0, newRrule.length() - 1));
        }

        return endDate;
    }

    private LocalDateTime calculateByWeekly(EventDTO eventDTO, Block block) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        LocalDateTime endDate = null;
        String[] parts = eventDTO.getRRule().split(";");
        StringBuilder newRrule = new StringBuilder();
        if (block != null) {
            changeIfBeforeOrAfterBlock(eventDTO, block);
            if (parts.length == 2 || (parts.length == 3 && eventDTO.getRRule().contains("BYDAY"))) {
                eventDTO.setRRule(eventDTO.getRRule() + ";UNTIL=" + block.getDateTo().format(formatter));
                endDate = block.getDateTo();
            } else if (eventDTO.getRRule().contains("UNTIL")) {
                for (String part : parts) {
                    if (part.contains("UNTIL")) {
                        String endTimeString = part.replace("UNTIL=", "");
                        endTimeString = endTimeString.replace("Z", "");
                        LocalDateTime endTime = LocalDateTime.parse(endTimeString, formatter);

                        if (endTime.isAfter(block.getDateTo())) {
                            newRrule.append("UNTIL=").append(block.getDateTo().format(formatter)).append(";");
                            endDate = block.getDateTo();
                        } else if (endTime.isBefore(block.getDateFrom())) {
                            newRrule.append("UNTIL=").append(eventDTO.getEndDate().format(formatter)).append(";");
                            endDate = eventDTO.getEndDate();
                        } else {
                            newRrule.append("UNTIL=").append(endTime.format(formatter)).append(";");
                            endDate = endTime;
                        }
                    } else {
                        newRrule.append(part).append(";");
                    }
                }
                eventDTO.setRRule(newRrule.substring(0, newRrule.length() - 1));
            } else {
                for (String part : parts) {
                    if (part.contains("COUNT")) {
                        String endTimeString = part.replace("COUNT=", "");
                        int count = Integer.parseInt(endTimeString);
                        LocalDateTime endTime = eventDTO.getEndDate().plusDays(count * 7);
                        endDate = endTime;
                        if (endTime.isAfter(block.getDateTo())) {
                            endDate = block.getDateTo();
                            newRrule.append("UNTIL=").append(endDate.format(formatter)).append(";");
                        } else {
                            newRrule.append(part).append(";");
                        }

                    } else {
                        newRrule.append(part).append(";");
                    }
                }
                eventDTO.setRRule(newRrule.substring(0, newRrule.length() - 1));
            }
        } else {
            for (String part : parts) {
                if (part.contains("UNTIL")) {
                    String endTimeString = part.replace("UNTIL=", "");
                    endTimeString = endTimeString.replace("Z", "");
                    endDate = LocalDateTime.parse(endTimeString, formatter);
                    if (endDate.isBefore(eventDTO.getEndDate())) {
                        endDate = eventDTO.getEndDate();
                        newRrule.append("UNTIL=").append(endDate.format(formatter)).append(";");
                    }
                } else if (part.contains("COUNT")) {
                    String endTimeString = part.replace("COUNT=", "");
                    int count = Integer.parseInt(endTimeString);
                    endDate = eventDTO.getEndDate().plusDays(count * 7);
                    newRrule.append("UNTIL=").append(endDate.format(formatter)).append(";");
                } else {
                    newRrule.append(part).append(";");
                }
            }
            eventDTO.setRRule(newRrule.substring(0, newRrule.length() - 1));
        }

        return endDate;
    }

    private LocalDateTime calculateByDaily(EventDTO eventDTO, Block block) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        LocalDateTime endDate = null;
        String[] parts = eventDTO.getRRule().split(";");
        StringBuilder newRrule = new StringBuilder();
        if (block != null) {
            changeIfBeforeOrAfterBlock(eventDTO, block);
            if (parts.length == 2) {
                eventDTO.setRRule(eventDTO.getRRule() + ";UNTIL=" + block.getDateTo().format(formatter));
                endDate = block.getDateTo();
            } else if (eventDTO.getRRule().contains("UNTIL")) {
                for (String part : parts) {
                    if (part.contains("UNTIL")) {
                        String endTimeString = part.replace("UNTIL=", "");
                        endTimeString = endTimeString.replace("Z", "");
                        LocalDateTime endTime = LocalDateTime.parse(endTimeString, formatter);

                        if (endTime.isAfter(block.getDateTo())) {
                            newRrule.append("UNTIL=").append(block.getDateTo().format(formatter)).append(";");
                            endDate = block.getDateTo();
                        } else if (endTime.isBefore(block.getDateFrom())) {
                            newRrule.append("UNTIL=").append(eventDTO.getEndDate().format(formatter)).append(";");
                            endDate = eventDTO.getEndDate();
                        } else {
                            newRrule.append("UNTIL=").append(endTime.format(formatter)).append(";");
                            endDate = endTime;
                        }
                    } else {
                        newRrule.append(part).append(";");
                    }
                }
                eventDTO.setRRule(newRrule.substring(0, newRrule.length() - 1));
            } else {
                for (String part : parts) {
                    if (part.contains("COUNT")) {
                        String endTimeString = part.replace("COUNT=", "");
                        int count = Integer.parseInt(endTimeString);
                        LocalDateTime endTime = eventDTO.getEndDate().plusDays(count);
                        endDate = endTime;
                        if (endTime.isAfter(block.getDateTo())) {
                            endDate = block.getDateTo();
                            newRrule.append("UNTIL=").append(endDate.format(formatter)).append(";");
                        } else {
                            newRrule.append(part).append(";");
                        }

                    } else {
                        newRrule.append(part).append(";");
                    }
                }
                eventDTO.setRRule(newRrule.substring(0, newRrule.length() - 1));
            }
        } else {
            for (String part : parts) {
                if (part.contains("UNTIL")) {
                    String endTimeString = part.replace("UNTIL=", "");
                    endTimeString = endTimeString.replace("Z", "");
                    endDate = LocalDateTime.parse(endTimeString, formatter);
                    if (endDate.isBefore(eventDTO.getEndDate())) {
                        endDate = eventDTO.getEndDate();
                    }
                    newRrule.append("UNTIL=").append(endDate.format(formatter)).append(";");
                } else if (part.contains("COUNT")) {
                    String endTimeString = part.replace("COUNT=", "");
                    int count = Integer.parseInt(endTimeString);
                    endDate = eventDTO.getEndDate().plusDays(count);
                } else {
                    newRrule.append(part).append(";");
                }
            }
            eventDTO.setRRule(newRrule.substring(0, newRrule.length() - 1));
        }
        return endDate;
    }

    private void changeIfBeforeOrAfterBlock(EventDTO eventDTO, Block block) {
        if (eventDTO.getStartDate().isBefore(block.getDateFrom())) {
            eventDTO.setStartDate(eventDTO.getStartDate().withYear(block.getDateFrom().getYear()).withMonth(block.getDateFrom().getMonthValue()).withDayOfMonth(block.getDateFrom().getDayOfMonth()));
            eventDTO.setEndDate(eventDTO.getEndDate().withYear(block.getDateFrom().getYear()).withMonth(block.getDateFrom().getMonthValue()).withDayOfMonth(block.getDateFrom().getDayOfMonth()));
        } else if (eventDTO.getStartDate().isAfter(block.getDateTo())) {
            eventDTO.setStartDate(eventDTO.getStartDate().withYear(block.getDateFrom().getYear()).withMonth(block.getDateFrom().getMonthValue()).withDayOfMonth(block.getDateFrom().getDayOfMonth()));
            eventDTO.setEndDate(eventDTO.getEndDate().withYear(block.getDateFrom().getYear()).withMonth(block.getDateFrom().getMonthValue()).withDayOfMonth(block.getDateFrom().getDayOfMonth()));
        }
    }

    public boolean checkIfExist(Long id) {
        Optional<Event> oldEvent = this.eventRepository.findById(id);
        return oldEvent != null;
    }

    public void moveEventsToExpiredEventsWhenReachArchiveTime() {
        List<Event> eventList = this.eventRepository.findAllByUser(this.userService.getCurrentUser());
        Settings settings = this.settingsService.getCurrentUserSettings();
        for (Event event : eventList) {
            if (event.getDateFromArchiveCount() != null) {
                if (event.getDateFromArchiveCount().isBefore(LocalDateTime.now().minusDays(settings.getTimeToArchive()))) {
                    this.expiredEventsService.addExpiredEvent(event);
                    this.eventRepository.delete(event);
                }
            }
        }
    }

    public Long changeEvent(EventDTO eventDTO) {
        Event oldEvent = this.eventRepository.findById(eventDTO.getId()).get();
        boolean edited = false;
        boolean moved = false;
        System.out.println(eventDTO);
        if (!oldEvent.getStartDate().isEqual(eventDTO.getStartDate())
                || !oldEvent.getEndDate().isEqual(eventDTO.getEndDate())
                || !oldEvent.getAllDay().equals(eventDTO.getAllDay())
                || !oldEvent.getRRule().equals(eventDTO.getRRule())) {
            moved = true;
            if (oldEvent.getExDate().equals(eventDTO.getExDate())) {
                oldEvent.setEndDate(eventDTO.getEndDate().plusHours(1));
                oldEvent.setStartDate(eventDTO.getStartDate().plusHours(1));
            } else {
                oldEvent.setEndDate(eventDTO.getEndDate());
                oldEvent.setStartDate(eventDTO.getStartDate());
            }
        }
        if (!oldEvent.getNotes().equals(eventDTO.getNotes())
                || !oldEvent.getExDate().equals(eventDTO.getExDate())
                || !oldEvent.getTitle().equals(eventDTO.getTitle())
                || !oldEvent.getTypeId().equals(eventDTO.getTypeId())) {
            edited = true;
        }

        if (edited && moved) {
            oldEvent.setStatusId(3);
            oldEvent.setDateFromArchiveCount(calculateEndDate(eventDTO));
        } else if (!edited && moved) {
            oldEvent.setStatusId(1);
            oldEvent.setDateFromArchiveCount(calculateEndDate(eventDTO));
        } else {
            oldEvent.setStatusId(2);
        }
        oldEvent.setAllDay(eventDTO.getAllDay());
        if (oldEvent.getTitle() == null && eventDTO.getTitle() == null) {
            oldEvent.setTitle("");
        } else {
            oldEvent.setTitle(eventDTO.getTitle());
        }
        oldEvent.setNotes(eventDTO.getNotes());
        oldEvent.setTypeId(eventDTO.getTypeId());
        oldEvent.setRRule(eventDTO.getRRule());
        oldEvent.setExDate(eventDTO.getExDate());
        System.out.println("Old event change");
        System.out.println(oldEvent);
        return this.eventRepository.save(oldEvent).getId();

    }


    public List<EventDTO> getCurrentUserEventsDTO() {
        User user = this.userService.getCurrentUser();
        List<Event> eventArrayList = this.eventRepository.findAllByUser(user);
        ArrayList<EventDTO> eventDTOArrayList = new ArrayList<>();
        for (Event current : eventArrayList) {
            eventDTOArrayList.add(new EventDTO(current));
        }
        return eventDTOArrayList;
    }


    public List<EventDTO> getCurrentUserEventsByBlock(String blockName) {
        User user = this.userService.getCurrentUser();
        Block block = this.blockService.getBlockByUserAndBlockName(user, blockName);

        List<Event> eventArrayList = this.eventRepository.findAllByUser(user);
        ArrayList<EventDTO> eventDTOArrayList = new ArrayList<>();
        for (Event current : eventArrayList) {
            if (current.getStartDate().isAfter(block.getDateFrom()) && current.getStartDate().isBefore(block.getDateTo())) {
                eventDTOArrayList.add(new EventDTO(current));
            }
        }
        return eventDTOArrayList;
    }


    public void deleteEvent(Long eventId) {
        this.eventRepository.deleteById(eventId);
    }

    public boolean checkIfEventIsThisUser(Long eventId) {
        return this.eventRepository.findByIdAndUser(eventId, this.userService.getCurrentUser()) != null;
    }


}
