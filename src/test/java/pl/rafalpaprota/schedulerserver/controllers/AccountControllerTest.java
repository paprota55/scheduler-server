package pl.rafalpaprota.schedulerserver.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    void shouldLoginAndGetContent() throws Exception {

        MvcResult login = this.mockMvc.perform(post("/login")
                .contentType("application/json")
                .content("{\"login\" : \"test\", \"email\" : \"\", \"password\" : \"test\"}"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

    }

    @Test
    void shouldNotLoginAndGiveException() throws Exception {

        MvcResult login = this.mockMvc.perform(post("/login")
                .contentType("application/json")
                .content("{\"login\" : \"test2\", \"email\" : \"\", \"password\" : \"test\"}"))
                .andDo(print())
                .andExpect(status().is(403))
                .andReturn();

    }

    @Test
    void shouldRegisterAndGetOkStatus() throws Exception {
        MvcResult register = this.mockMvc.perform(post("/register")
                .contentType("application/json")
                .content("{\"login\" : \"repo\", \"email\" : \"kowalski@gmail.com\", \"password\" : \"test\"}"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
    }

    @Test
    void shouldNotRegisterAndGetConflictStatus() throws Exception {
        MvcResult register = this.mockMvc.perform(post("/register")
                .contentType("application/json")
                .content("{\"login\" : \"test\", \"email\" : \"kowalski@gmail.com\", \"password\" : \"test\"}"))
                .andDo(print())
                .andExpect(status().is(409))
                .andReturn();
    }
}