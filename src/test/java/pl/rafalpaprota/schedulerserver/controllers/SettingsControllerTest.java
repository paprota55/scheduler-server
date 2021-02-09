package pl.rafalpaprota.schedulerserver.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.rafalpaprota.schedulerserver.dto.SettingsDTO;
import pl.rafalpaprota.schedulerserver.repositories.UserRepository;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "test")
class SettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnSettings() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/api/settings/getSettings"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        SettingsDTO settingsDTO = this.objectMapper.readValue(mvcResult.getResponse().getContentAsString(), SettingsDTO.class);
        assertEquals(14, settingsDTO.getNewTime());
    }

    @Test
    void shouldReturnArchiveTime() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/api/settings/getArchiveTime"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        String time = mvcResult.getResponse().getContentAsString();
        assertEquals("14", time);
    }

    @Test
    void shouldUpdateArchiveTimeAndReturnOkStatus() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(put("/api/modify/archiveTime")
                .contentType("application/json")
                .content("{\"newTime\" : \"6\", \"password\" : \"test\"}"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        MvcResult result = this.mockMvc.perform(get("/api/settings/getArchiveTime"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        String time = result.getResponse().getContentAsString();
        assertEquals("6", time);
    }

    @Test
    void shouldNotUpdateAndReturnConflictStatus() throws Exception {
        //given
        //Wrong password
        MvcResult mvcResult = this.mockMvc.perform(put("/api/modify/archiveTime")
                .contentType("application/json")
                .content("{\"newTime\" : \"6\", \"password\" : \"test2\"}"))
                .andDo(print())
                .andExpect(status().is(409))
                .andReturn();
    }

    @Test
    void shouldNotUpdateAndReturnBadRequestStatus() throws Exception {
        //given
        //null settings
        MvcResult mvcResult = this.mockMvc.perform(put("/api/modify/archiveTime")
                .contentType("application/json")
                .content(""))
                .andDo(print())
                .andExpect(status().is(400))
                .andReturn();
    }

}