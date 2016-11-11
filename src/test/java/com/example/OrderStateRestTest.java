package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.SneakyThrows;


@RunWith(SpringRunner.class)
@SpringBootTest(classes= {OrderApplication.class}, webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
        "logging.level.org.springframework.web.client.RestTemplate=DEBUG",
        "logging.level.org.apache.http.wire=DEBUG"
})
public class OrderStateRestTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private URI orderLocation;

    @Before
    @SneakyThrows
    public void setup() {
        orderLocation = this.restTemplate.exchange("/orders",HttpMethod.POST, jsonPayload("{}"), Void.class).getHeaders().getLocation();
    }

    @Test
    @SneakyThrows
    public void test() {
        Link eventLink = new Traverson(orderLocation, MediaTypes.HAL_JSON).follow("unlock-delivery").asLink();
        assertThat(eventLink).isNotNull();

        ResponseEntity<Void> response = this.restTemplate.postForEntity(eventLink.getHref(), "", Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        eventLink = new Traverson(orderLocation, MediaTypes.HAL_JSON).follow("deliver").asLink();
        Link eventLink2 = eventLink;
        assertThat(eventLink).isNotNull();
        response = this.restTemplate.postForEntity(eventLink.getHref(), "", Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        eventLink = new Traverson(orderLocation, MediaTypes.HAL_JSON).follow("receive-payment").asLink();
        assertThat(eventLink).isNotNull();
        response = this.restTemplate.postForEntity(eventLink.getHref(), "", Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        eventLink = new Traverson(orderLocation, MediaTypes.HAL_JSON).follow("refund").asLink();
        assertThat(eventLink).isNotNull();
        response = this.restTemplate.postForEntity(eventLink2.getHref(), "", Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

    }

    private HttpEntity<String> jsonPayload(String bodyJson) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<String>(bodyJson ,headers);
    }
}
