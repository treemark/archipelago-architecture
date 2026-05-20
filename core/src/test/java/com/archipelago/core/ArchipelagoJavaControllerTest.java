package com.archipelago.core;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArchipelagoJavaController.class)
class ArchipelagoJavaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void helloReturnsMessage() throws Exception {
        mockMvc.perform(get("/api/java/hello"))
                .andExpect(status().isOk())
                .andExpect(result -> 
                    org.junit.jupiter.api.Assertions.assertEquals("Hello from Java!", result.getResponse().getContentAsString())
                );
    }

    @Test
    void statusReturnsUp() throws Exception {
        mockMvc.perform(get("/api/java/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
