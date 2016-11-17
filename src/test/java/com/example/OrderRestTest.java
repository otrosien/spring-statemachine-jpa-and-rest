package com.example;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.relaxedLinks;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.maskLinks;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.document;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;

import java.net.URI;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;

import lombok.SneakyThrows;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = { OrderApplication.class }, 
    webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class OrderRestTest {

    private static final Matcher<Integer> ACCEPTED = is(HttpStatus.ACCEPTED.value());

    private static final Matcher<Integer> UNPROCESSABLE_ENTITY = is(HttpStatus.UNPROCESSABLE_ENTITY.value());

    // TODO: Why is this not working?
    private static final OperationRequestPreprocessor PREPROCESS_REQUEST = preprocessRequest(maskLinks());

    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    private RequestSpecification documentationSpec;

    private URI orderLocation;

    @LocalServerPort
    private int port;

    @Before
    public void setUp() {
        this.documentationSpec = new RequestSpecBuilder()
                .addFilter(documentationConfiguration(restDocumentation)).build();

        orderLocation = URI.create(given(this.documentationSpec)
        .accept("application/hal+json")
        .contentType(ContentType.JSON)
        .filter(document("orders-create",
                requestFields()
                ))
        .body("{}")
        .when()
            .port(port)
            .post("/orders")
        .then()
            .assertThat().statusCode(HttpStatus.CREATED.value())
        .extract()
            .header("Location"));
    }

    @Test
    @SneakyThrows
    public void should_trigger_state_transitions_via_links() {
        given(this.documentationSpec)
        .accept("application/hal+json")
        .filter(document("order-get",
                PREPROCESS_REQUEST,
                responseFields(
                        fieldWithPath("currentState").description("Current order state. Localized and meant for display purposes only"),
                        subsectionWithPath("_links").ignored()
                ),
                relaxedLinks(
                    linkWithRel("unlock-delivery")
                        .description("Unlock delivery, to enable fulfillment process even if payment has not been received yet.").optional(),
                    linkWithRel("deliver")
                        .description("Deliver products").optional(),
                    linkWithRel("receive-payment")
                        .description("Receive Payment").optional(),
                    linkWithRel("refund")
                        .description("Start refund process").optional(),
                    linkWithRel("reopen")
                        .description("Reopen a closed order").optional(),
                    linkWithRel("cancel")
                        .description("Cancel an order").optional()
                )))
        .when()
            .get(orderLocation)
        .then()
            .assertThat().statusCode(is(200));

        // traverse and test some links.

        // unlock delivery
        Link eventLink = new Traverson(orderLocation, MediaTypes.HAL_JSON).follow("unlock-delivery").asLink();
        assertThat(eventLink).isNotNull();
        postToLink(eventLink, ACCEPTED);

        // deliver
        eventLink = new Traverson(orderLocation, MediaTypes.HAL_JSON).follow("deliver").asLink();
        Link eventLinkForLater = eventLink;
        assertThat(eventLink).isNotNull();
        postToLink(eventLink, ACCEPTED);

        // receive payment
        eventLink = new Traverson(orderLocation, MediaTypes.HAL_JSON).follow("receive-payment").asLink();
        assertThat(eventLink).isNotNull();
        postToLink(eventLink, ACCEPTED);

        // refund
        eventLink = new Traverson(orderLocation, MediaTypes.HAL_JSON).follow("refund").asLink();
        assertThat(eventLink).isNotNull();
        postToLink(eventLink, ACCEPTED);

        // .. and try to post to a link that is not valid anymore.
        postToLink(eventLinkForLater, UNPROCESSABLE_ENTITY);
    }

    @Test
    public void should_list_orders() {
        given(this.documentationSpec)
        .filter(document("orders-list", 
                responseFields(
                        subsectionWithPath("_embedded.orders").description("Embedded list of <<resources-order,Order resources>>"),
                        subsectionWithPath("page").description("paging information"),
                        subsectionWithPath("_links").ignored()
                ), links(
                        linkWithRel("self").ignored(),
                        linkWithRel("profile").description("APLS profile"),
                        linkWithRel("search").description("<<resources-order-search,Order search subresource>>")
                )))
        .when()
            .port(port)
            .get("/orders")
        .then()
            .assertThat().statusCode(HttpStatus.OK.value());
    }

    @Test
    public void should_delete_order() {
        given(this.documentationSpec)
        .filter(document("order-delete"))
        .when().delete(orderLocation)
        .then().assertThat().statusCode(HttpStatus.NO_CONTENT.value());
    }

    private void postToLink(Link eventLink, Matcher<Integer> statusCodeMatcher) {
        given(this.documentationSpec)
                .accept("application/hal+json")
        .when()
            .post(eventLink.getHref())
        .then()
            .assertThat().statusCode(statusCodeMatcher);
    }

}
