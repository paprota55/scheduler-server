package pl.rafalpaprota.schedulerserver.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.rafalpaprota.schedulerserver.dto.EventDTO;
import pl.rafalpaprota.schedulerserver.model.Event;
import pl.rafalpaprota.schedulerserver.model.User;
import pl.rafalpaprota.schedulerserver.repositories.EventRepository;
import pl.rafalpaprota.schedulerserver.services.EventService;
import pl.rafalpaprota.schedulerserver.services.RoleService;
import pl.rafalpaprota.schedulerserver.services.UserService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "test")
class EventsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventService eventService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserService userService;

    @Autowired
    private EventRepository eventRepository;


    @Test
    void shouldAddEventAndReturnOkStatus() throws Exception {
        MvcResult result = this.mockMvc.perform(post("/api/events/addEvent")
                .contentType("application/json")
                .content("{ \"id\" : \"null\" , \"allDay\" : \"false\" , \"endDate\" : \"2020-12-17T08:30\" , \"exDate\" : \"\", \"notes\" : \"nowa notatka\" , \"rRule\" : \"\", \"startDate\" : \"2020-12-17T08:00\" , \"statusId\" : \"\", \"title\" : \"nowy\" , \"typeId\" : \"5\", \"blockId\" : \"0\" }"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        String idToParse = result.getResponse().getContentAsString();
        Long id = Long.parseLong(idToParse);

        Event event = this.eventRepository.findById(id).get();

        assertEquals("", event.getRRule());
        assertNotEquals(true, event.getAllDay());
        assertEquals("", event.getExDate());
        assertEquals("nowa notatka", event.getNotes());
        assertEquals(5, event.getTypeId());
        assertEquals(0, event.getStatusId());

    }

    @Test
    void shouldAddEventAndCalculateEndDateAndReturnOkStatus() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        MvcResult result = this.mockMvc.perform(post("/api/events/addEvent")
                .contentType("application/json")
                .content("{ \"id\" : \"null\" , \"allDay\" : \"false\" , \"endDate\" : \"2020-12-17T08:30\" , \"exDate\" : \"\", \"notes\" : \"nowa notatka\" , \"rRule\" : \"INTERVAL=1;FREQ=WEEKLY;BYDAY=MO,WE,TH\", \"startDate\" : \"2020-12-17T08:00\" , \"statusId\" : \"0\", \"title\" : \"nowy\" , \"typeId\" : \"5\", \"blockId\" : \"1\" }"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        String idToParse = result.getResponse().getContentAsString();
        Long id = Long.parseLong(idToParse);

        Event event = this.eventRepository.findById(id).get();

        assertNotEquals(true, event.getAllDay());
        assertEquals("", event.getExDate());
        assertEquals("nowa notatka", event.getNotes());
        assertEquals(5, event.getTypeId());
        assertEquals(0, event.getStatusId());
        assertEquals("INTERVAL=1;FREQ=WEEKLY;BYDAY=MO,WE,TH;UNTIL=20210101T235959", event.getRRule());
        assertEquals(LocalDateTime.parse("20210101T235959", formatter), event.getDateFromArchiveCount().withNano(0));
    }

    @Test
    void shouldAddEventAndCalculateEndDateWhenGetCountInRruleAndReturnOkStatus() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        MvcResult result = this.mockMvc.perform(post("/api/events/addEvent")
                .contentType("application/json")
                .content("{ \"id\" : \"null\" , \"allDay\" : \"false\" , \"endDate\" : \"2020-12-17T08:30\" , \"exDate\" : \"\", \"notes\" : \"nowa notatka\" , \"rRule\" : \"INTERVAL=1;FREQ=WEEKLY;COUNT=13\", \"startDate\" : \"2020-12-17T08:00\" , \"statusId\" : \"0\", \"title\" : \"nowy\" , \"typeId\" : \"5\", \"blockId\" : \"0\" }"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        String idToParse = result.getResponse().getContentAsString();
        Long id = Long.parseLong(idToParse);

        Event event = this.eventRepository.findById(id).get();

        assertNotEquals(true, event.getAllDay());
        assertEquals("", event.getExDate());
        assertEquals("nowa notatka", event.getNotes());
        assertEquals(5, event.getTypeId());
        assertEquals(0, event.getStatusId());
        assertEquals("INTERVAL=1;FREQ=WEEKLY;UNTIL=2021-03-18T09:30", event.getRRule());
        assertEquals(LocalDateTime.parse("20210318T093000", formatter), event.getDateFromArchiveCount().withNano(0));
    }


    @Test
    void shouldNotAddEventAndReturnConflictStatus() throws Exception {
        //given
        //Wrong date sequence
        MvcResult result = this.mockMvc.perform(post("/api/events/addEvent")
                .contentType("application/json")
                .content("{ \"id\" : \"null\" , \"allDay\" : \"false\" , \"endDate\" : \"2020-12-17T08:30\" , \"exDate\" : \"\", \"notes\" : \"nowa notatka\" , \"rRule\" : \"\", \"startDate\" : \"2020-12-17T09:00\" , \"statusId\" : \"0\", \"title\" : \"nowy\" , \"typeId\" : \"5\", \"blockId\" : \"0\" }"))
                .andDo(print())
                .andExpect(status().is(409))
                .andReturn();

    }

    @Test
    void shouldNotAddEventAndReturnBadRequestStatus() throws Exception {
        //given
        //Two or one from dates is empty
        MvcResult result = this.mockMvc.perform(post("/api/events/addEvent")
                .contentType("application/json")
                .content("{ \"id\" : \"null\" , \"allDay\" : \"false\" , \"endDate\" : \"\" , \"exDate\" : \"\", \"notes\" : \"nowa notatka\" , \"rRule\" : \"\", \"startDate\" : \"2020-12-17T09:00\" , \"statusId\" : \"0\", \"title\" : \"nowy\" , \"typeId\" : \"5\", \"blockId\" : \"0\" }"))
                .andDo(print())
                .andExpect(status().is(400))
                .andReturn();

    }

    @Test
    void shouldChangeAndCalculateEndDateReturnOkStatus() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        MvcResult result = this.mockMvc.perform(put("/api/events/changeEvent/1")
                .contentType("application/json")
                .content("{ \"id\" : \"1\" , \"allDay\" : \"false\" , \"endDate\" : \"2020-12-17T08:30\" , \"exDate\" : \"\", \"notes\" : \"nowa notatka\" , \"rRule\" : \"\", \"startDate\" : \"2020-12-17T08:00\" , \"statusId\" : \"0\", \"title\" : \"nowy\" , \"typeId\" : \"5\", \"blockId\" : \"0\" }"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        String idToParse = result.getResponse().getContentAsString();
        Long id = Long.parseLong(idToParse);

        Event event = this.eventRepository.findById(id).get();

        assertNotEquals(true, event.getAllDay());
        assertEquals("", event.getExDate());
        assertEquals("nowa notatka", event.getNotes());
        assertEquals(5, event.getTypeId());
        assertEquals(3, event.getStatusId());
        assertEquals("", event.getRRule());
        assertEquals(LocalDateTime.parse("20201217T083000", formatter), event.getDateFromArchiveCount().withNano(0));
    }

    @Test
    void shouldChangeAndCalculateEndDateWhenCountReturnOkStatus() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        MvcResult result = this.mockMvc.perform(put("/api/events/changeEvent/1")
                .contentType("application/json")
                .content("{ \"id\" : \"1\" , \"allDay\" : \"false\" , \"endDate\" : \"2020-12-17T08:30\" , \"exDate\" : \"\", \"notes\" : \"nowa notatka\" , \"rRule\" : \"INTERVAL=1;FREQ=WEEKLY;COUNT=13\", \"startDate\" : \"2020-12-17T08:00\" , \"statusId\" : \"0\", \"title\" : \"nowy\" , \"typeId\" : \"5\", \"blockId\" : \"1\" }"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        String idToParse = result.getResponse().getContentAsString();
        Long id = Long.parseLong(idToParse);

        Event event = this.eventRepository.findById(id).get();

        assertNotEquals(true, event.getAllDay());
        assertEquals("", event.getExDate());
        assertEquals("nowa notatka", event.getNotes());
        assertEquals(5, event.getTypeId());
        assertEquals(3, event.getStatusId());
        assertEquals("INTERVAL=1;FREQ=WEEKLY;UNTIL=20210101T235959", event.getRRule());
        assertEquals(LocalDateTime.parse("20210101T235959", formatter), event.getDateFromArchiveCount().withNano(0));
    }

    @Test
    void shouldChangeAndCalculateEndDateWhenWithoutRangeReturnOkStatus() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        MvcResult result = this.mockMvc.perform(put("/api/events/changeEvent/1")
                .contentType("application/json")
                .content("{ \"id\" : \"1\" , \"allDay\" : \"false\" , \"endDate\" : \"2020-12-17T08:30\" , \"exDate\" : \"\", \"notes\" : \"nowa notatka\" , \"rRule\" : \"INTERVAL=1;FREQ=WEEKLY;BYDAY=MO,WE,TH\", \"startDate\" : \"2020-12-17T08:00\" , \"statusId\" : \"0\", \"title\" : \"nowy\" , \"typeId\" : \"5\", \"blockId\" : \"1\" }"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        String idToParse = result.getResponse().getContentAsString();
        Long id = Long.parseLong(idToParse);

        Event event = this.eventRepository.findById(id).get();

        assertNotEquals(true, event.getAllDay());
        assertEquals("", event.getExDate());
        assertEquals("nowa notatka", event.getNotes());
        assertEquals(5, event.getTypeId());
        assertEquals(3, event.getStatusId());
        assertEquals("INTERVAL=1;FREQ=WEEKLY;BYDAY=MO,WE,TH;UNTIL=20210101T235959", event.getRRule());
        assertEquals(LocalDateTime.parse("20210101T235959", formatter), event.getDateFromArchiveCount().withNano(0));
    }

    @Test
    void shouldDeleteEventAndReturnOkStatus() throws Exception {

        MvcResult result = this.mockMvc.perform(delete("/api/events/deleteEvent/1"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

    }

    @Test
    void shouldNotDeleteEventAndReturnConflictStatus() throws Exception {
        //given
        //Try delete event which isn't current user
        User user = new User();
        user.setLogin("test2");
        user.setEmail("kowalski@gmail.com");
        user.setPassword("test2");
        user.setRole(this.roleService.getRoleByName("USER"));
        this.userService.addUser(user);

        Event event = new Event();
        event.setId(9999L);
        event.setUser(this.userService.getUserByLogin("test2"));
        event.setStartDate(LocalDateTime.now());
        event.setEndDate(LocalDateTime.now().plusHours(1));

        this.eventRepository.save(event);


        MvcResult result = this.mockMvc.perform(delete("/api/events/deleteEvent/9999"))
                .andDo(print())
                .andExpect(status().is(409))
                .andReturn();
    }

    @Test
    void shouldNotDeleteEventAndReturnBadRequestStatus() throws Exception {
        //given
        //Don't give event Id

        MvcResult result = this.mockMvc.perform(delete("/api/events/deleteEvent/null"))
                .andDo(print())
                .andExpect(status().is(400))
                .andReturn();
    }

    @Test
    void shouldGetOneEventAndReturnOkStatus() throws Exception {

        MvcResult result = this.mockMvc.perform(get("/api/events/getEvents/block/blok1"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        List<EventDTO> list = this.objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertEquals(1, list.size());
    }

    @Test
    void shouldNotGetOneEventAndReturnOkStatus() throws Exception {

        MvcResult result = this.mockMvc.perform(get("/api/events/getEvents/block/blok2"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        List<EventDTO> list = this.objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertEquals(0, list.size());
    }

    @Test
    void shouldGetEventListAndReturnOkStatus() throws Exception {

        MvcResult result = this.mockMvc.perform(get("/api/events/getEvents/blok1"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        List<EventDTO> list = this.objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertEquals(1, list.size());
    }

    @Test
    void shouldNotGetEventListAndReturnConflictStatus() throws Exception {
        //given
        //blockName which doesn't exist

        MvcResult result = this.mockMvc.perform(get("/api/events/getEvents/blok8"))
                .andDo(print())
                .andExpect(status().is(409))
                .andReturn();

    }

    @Test
    void shouldGetUserEventListAndReturnOkStatus() throws Exception {

        MvcResult result = this.mockMvc.perform(get("/api/events/getEvents"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        List<EventDTO> list = this.objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });
        assertEquals(1, list.size());

    }

}