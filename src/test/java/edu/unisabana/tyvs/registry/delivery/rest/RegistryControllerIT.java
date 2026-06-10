package edu.unisabana.tyvs.registry.delivery.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class RegistryControllerIT {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void shouldReturnValidWhenPersonIsOk() throws Exception {
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"id\":100,\"age\":30," +
                                "\"gender\":\"FEMALE\",\"alive\":true}"))
                .andExpect(status().isOk())
                .andExpect(content().string("VALID"));
    }

    @Test
    public void shouldReturnUnderageForMinor() throws Exception {
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Luis\",\"id\":200,\"age\":15," +
                                "\"gender\":\"MALE\",\"alive\":true}"))
                .andExpect(status().isOk())
                .andExpect(content().string("UNDERAGE"));
    }
}