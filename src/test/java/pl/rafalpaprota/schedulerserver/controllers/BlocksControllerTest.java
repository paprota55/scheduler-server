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
import pl.rafalpaprota.schedulerserver.dto.BlockDisplayDTO;
import pl.rafalpaprota.schedulerserver.dto.BlockToCreateAppointmentDTO;
import pl.rafalpaprota.schedulerserver.model.Block;
import pl.rafalpaprota.schedulerserver.model.User;
import pl.rafalpaprota.schedulerserver.services.BlockService;
import pl.rafalpaprota.schedulerserver.services.UserService;

import javax.transaction.Transactional;
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
class BlocksControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BlockService blockService;

    @Autowired
    private UserService userService;

    @Test
    void shouldGetBlocksAndOkStatus() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/api/blocks/getBlocks"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        List<BlockDisplayDTO> list = this.objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertNotEquals(null, list);
        assertEquals(2, list.size());

    }

    @Test
    void shouldGetBlocksToSchedulerAndOkStatus() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/api/blocks/getBlocksToScheduler"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();

        List<BlockToCreateAppointmentDTO> list = this.objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertNotEquals(null, list);
        assertEquals(3, list.size());
        assertEquals("Bez bloku", list.get(0).getText());

    }

    @Test
    void shouldUpdateBlockAndReturnOkStatus() throws Exception {
        User user = this.userService.getUserByLogin("test");
        MvcResult result = this.mockMvc.perform(put("/api/blocks/modifyBlock")
                .contentType("application/json")
                .content("{ \"blockName\" : \"blok1\" , \"dateTo\" :\"2019-01-20T15:00:20.310106200\" , \"dateFrom\" : \"2019-01-12T15:00:20.310106200\", \"notes\" : \"Blok numer 1\"}"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
        Block block = this.blockService.getBlockByUserAndBlockName(user, "blok1");
        assertEquals("Blok numer 1", block.getNotes());
    }

    @Test
    void shouldNotUpdateBlockAndReturnConflictStatus() throws Exception {
        //given
        //blockName which doesn't exist
        MvcResult result = this.mockMvc.perform(put("/api/blocks/modifyBlock")
                .contentType("application/json")
                .content("{ \"blockName\" : \"blok8\" , \"dateTo\" :\"2021-01-20T15:00:20.310106200\" , \"dateFrom\" : \"2021-01-12T15:00:20.310106200\", \"notes\" : \"Blok numer 1\"}"))
                .andDo(print())
                .andExpect(status().is(409))
                .andReturn();
    }

    @Test
    void shouldNotUpdateBlockAndReturnNotFoundStatus() throws Exception {
        //given
        //One from dates is empty
        MvcResult result = this.mockMvc.perform(put("/api/blocks/modifyBlock")
                .contentType("application/json")
                .content("{ \"blockName\" : \"blok1\" , \"dateTo\" :\"\" , \"dateFrom\" : \"2021-01-12T15:00:20.310106200\", \"notes\" : \"Blok numer 1\"}"))
                .andDo(print())
                .andExpect(status().is(404))
                .andReturn();
    }

    @Test
    void shouldNotUpdateBlockAndReturnNotAcceptableStatus() throws Exception {
        //given
        //Dates when already exist block in this date range
        MvcResult result = this.mockMvc.perform(put("/api/blocks/modifyBlock")
                .contentType("application/json")
                .content("{ \"blockName\" : \"blok2\" , \"dateTo\" :\"2020-12-28T15:00:20.310106200\" , \"dateFrom\" : \"2020-12-20T15:00:20.310106200\", \"notes\" : \"Blok numer 1\"}"))
                .andDo(print())
                .andExpect(status().is(406))
                .andReturn();
    }

    @Test
    void shouldNotUpdateBlockAndReturnBadRequestStatus() throws Exception {
        //given
        //DateFrom is after DateTo
        MvcResult result = this.mockMvc.perform(put("/api/blocks/modifyBlock")
                .contentType("application/json")
                .content("{ \"blockName\" : \"blok1\" , \"dateTo\" :\"2020-12-20T15:00:20.310106200\" , \"dateFrom\" : \"2020-12-25T15:00:20.310106200\", \"notes\" : \"Blok numer 1\"}"))
                .andDo(print())
                .andExpect(status().is(400))
                .andReturn();
    }

    @Test
    void shouldDeleteBlockAndReturnOkStatus() throws Exception {
        MvcResult result = this.mockMvc.perform(delete("/api/blocks/deleteBlock/blok1"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
    }

    @Test
    void shouldNotDeleteBlockAndReturnConflictStatus() throws Exception {
        //given
        //blockName which doesn't exist
        MvcResult result = this.mockMvc.perform(delete("/api/blocks/deleteBlock/blok8"))
                .andDo(print())
                .andExpect(status().is(409))
                .andReturn();
    }

    @Test
    void shouldAddBlockAndReturnOkStatus() throws Exception {
        MvcResult result = this.mockMvc.perform(post("/api/blocks/addBlock")
                .contentType("application/json")
                .content("{ \"blockName\" : \"blok3\" , \"dateTo\" :\"2020-11-30T15:00:20.310106200\" , \"dateFrom\" : \"2020-11-25T15:00:20.310106200\", \"notes\" : \"Blok numer 1\"}"))
                .andDo(print())
                .andExpect(status().is(200))
                .andReturn();
    }

    @Test
    void shouldNotAddBlockAndReturnNotAcceptableStatus() throws Exception {
        //given
        //Dates when already exist other block
        MvcResult result = this.mockMvc.perform(post("/api/blocks/addBlock")
                .contentType("application/json")
                .content("{ \"blockName\" : \"blok3\" , \"dateTo\" :\"2020-12-26T15:00:20.310106200\" , \"dateFrom\" : \"2020-12-18T15:00:20.310106200\", \"notes\" : \"Blok numer 1\"}"))
                .andDo(print())
                .andExpect(status().is(406))
                .andReturn();
    }

    @Test
    void shouldNotAddBlockAndReturnConflictStatus() throws Exception {
        //given
        //block which blockName already exist in database
        MvcResult result = this.mockMvc.perform(post("/api/blocks/addBlock")
                .contentType("application/json")
                .content("{ \"blockName\" : \"blok1\" , \"dateTo\" :\"2020-11-30T15:00:20.310106200\" , \"dateFrom\" : \"2020-11-25T15:00:20.310106200\", \"notes\" : \"Blok numer 1\"}"))
                .andDo(print())
                .andExpect(status().is(409))
                .andReturn();
    }

    @Test
    void shouldNotAddBlockAndReturnBadRequestStatus() throws Exception {
        //given
        //dates are in wrong sequence
        MvcResult result = this.mockMvc.perform(post("/api/blocks/addBlock")
                .contentType("application/json")
                .content("{ \"blockName\" : \"blok3\" , \"dateTo\" :\"2020-11-28T15:00:20.310106200\" , \"dateFrom\" : \"2020-11-30T15:00:20.310106200\", \"notes\" : \"Blok numer 1\"}"))
                .andDo(print())
                .andExpect(status().is(400))
                .andReturn();
    }

}