package pl.rafalpaprota.schedulerserver.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldUpdatePasswordAndReturnOkStatus() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(put("/api/modify/password")
                .contentType("application/json")
                .content("{\"newEmail\" : \"\", \"password\" : \"test\", \"newPassword\" : \"Kowalski.111\"}"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
    }

    @Test
    void shouldNotUpdatePasswordAndReturnConflictStatus() throws Exception {
        //given
        //Doesn't match regex with newPassword
        MvcResult mvcResult = this.mockMvc.perform(put("/api/modify/password")
                .contentType("application/json")
                .content("{\"newEmail\" : \"\", \"password\" : \"test\", \"newPassword\" : \"test2\"}"))
                .andDo(print())
                .andExpect(status().is(409))
                .andReturn();
    }

    @Test
    void shouldNotUpdatePasswordAndReturnNotAcceptableStatus() throws Exception {
        //given
        //Wrong current password
        MvcResult mvcResult = this.mockMvc.perform(put("/api/modify/password")
                .contentType("application/json")
                .content("{\"newEmail\" : \"\", \"password\" : \"test2\", \"newPassword\" : \"Kowalski.111\"}"))
                .andDo(print())
                .andExpect(status().is(406))
                .andReturn();
    }

    @Test
    void shouldUpdateEmailAndReturnOkStatus() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(put("/api/modify/email")
                .contentType("application/json")
                .content("{\"newEmail\" : \"kowalski@gmail.com\", \"password\" : \"test\", \"newPassword\" : \"\"}"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
    }

    @Test
    void shouldNotUpdateEmailAndReturnConflictStatus() throws Exception {
        //given
        //Doesn't match regex with email
        MvcResult mvcResult = this.mockMvc.perform(put("/api/modify/email")
                .contentType("application/json")
                .content("{\"newEmail\" : \"kowalskigmail.com\", \"password\" : \"test\", \"newPassword\" : \"\"}"))
                .andDo(print())
                .andExpect(status().is(409))
                .andReturn();
    }

    @Test
    void shouldNotUpdateEmailAndReturnNotAcceptableStatus() throws Exception {
        //given
        //Wrong current password
        MvcResult mvcResult = this.mockMvc.perform(put("/api/modify/email")
                .contentType("application/json")
                .content("{\"newEmail\" : \"kowalski@gmail.com\", \"password\" : \"test2\", \"newPassword\" : \"\"}"))
                .andDo(print())
                .andExpect(status().is(406))
                .andReturn();
    }
}