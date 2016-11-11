package com.example;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import lombok.SneakyThrows;


@RunWith(SpringRunner.class)
@SpringBootTest(classes= {OrderApplication.class})
public class OrderStateRestTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private String orderLocation;

    @Before
    @SneakyThrows
    public void setup() {
        mockMvc = webAppContextSetup(webApplicationContext).build();
        orderLocation = mockMvc.perform(post("/orders")
                .content("{}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn()
                .getResponse()
                .getHeader("Location");
    }

    @Test
    @SneakyThrows
    public void test() {
        mockMvc.perform(get(orderLocation))
        .andDo(print());

        mockMvc.perform(post(orderLocation + "/receive/UnlockDelivery"))
        .andExpect(status().isAccepted());

        mockMvc.perform(get(orderLocation))
        .andDo(print());

        mockMvc.perform(post(orderLocation + "/receive/Deliver"))
        .andExpect(status().isAccepted());

        mockMvc.perform(get(orderLocation))
        .andDo(print());

        mockMvc.perform(post(orderLocation + "/receive/ReceivePayment"))
        .andExpect(status().isAccepted());

        mockMvc.perform(get(orderLocation))
        .andDo(print());

        mockMvc.perform(post(orderLocation + "/receive/Deliver"))
        .andExpect(status().isUnprocessableEntity());

    }
}
