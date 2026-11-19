package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.prl.services.UpdatePartyDetailsService;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** CCD-8320: real MVC request binding, controller, child service and response serialisation. No CCD save. */
@ExtendWith(MockitoExtension.class)
class CallbackControllerChildIdentityTest {

    private static final String FIRST_ID = "2d148b3a-7f45-4346-bc3d-d4e12306940f";
    private static final String SECOND_ID = "11111111-2222-4333-8444-555555555555";

    @Spy
    private ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
    @InjectMocks
    private UpdatePartyDetailsService childService;
    @InjectMocks
    private CallbackController controller;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        // Explicitly wire the REAL service: nested @InjectMocks fields are not automatically injected.
        ReflectionTestUtils.setField(controller, "updatePartyDetailsService", childService);
        mvc = MockMvcBuilders.standaloneSetup(controller)
            .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
    }

    @ParameterizedTest
    @CsvSource({
        "v2, childDetailsRevised, AWAITING_SUBMISSION_TO_HMCTS",
        "v3, childDetailsRevised, AWAITING_SUBMISSION_TO_HMCTS",
        "v2, amendChildDetailsRevised, CASE_ISSUED",
        "v3, amendChildDetailsRevised, CASE_ISSUED"
    })
    void shouldReturnExistingChildIdsAndMatchingRelationshipsInCallbackResponse(String version,
                                                                               String eventId,
                                                                               String state) throws Exception {
        final String request = """
            {
              "event_id": "%s",
              "case_details": {
                "id": 1111222233334444,
                "case_type_id": "PRLAPPS",
                "state": "%s",
                "case_data": {
                  "caseTypeOfApplication": "C100",
                  "taskListVersion": "%s",
                  "newChildDetails": [
                    {"id": "%s", "value": {"firstName": "First", "lastName": "Example"}},
                    {"id": "%s", "value": {"firstName": "Second", "lastName": "Example"}}
                  ],
                  "childAndApplicantRelations": [
                    {"id": "22222222-2222-4222-8222-222222222222",
                     "value": {"childId": "%s", "childAndApplicantRelation": "father"}},
                    {"id": "33333333-3333-4333-8333-333333333333",
                     "value": {"childId": "%s", "childAndApplicantRelation": "mother"}}
                  ]
                }
              }
            }
            """.formatted(eventId, state, version, FIRST_ID, SECOND_ID, FIRST_ID, SECOND_ID);
        Path evidence = Path.of("build", "reports", "ccd8320-callback", version, eventId);
        Files.createDirectories(evidence);
        Files.writeString(evidence.resolve("01-before-callback-request.json"), request);

        MvcResult result = mvc.perform(post("/pre-populate-child-information")
                .header("Authorization", "Bearer synthetic-local-test")
                .contentType(APPLICATION_JSON).content(request))
            .andExpect(status().isOk()).andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Files.writeString(evidence.resolve("02-after-callback-response.json"), responseBody);
        final JsonNode response = mapper.readTree(responseBody).path("data");
        assertThat(response.path("childAndApplicantRelations"))
            .isEqualTo(mapper.readTree(request).at("/case_details/case_data/childAndApplicantRelations"));
        assertAll(
            () -> assertThat(response.at("/newChildDetails/0/id").asText())
                .as("callback preserves first child's UUID").isEqualTo(FIRST_ID),
            () -> assertThat(response.at("/newChildDetails/1/id").asText())
                .as("callback preserves second child's UUID").isEqualTo(SECOND_ID),
            () -> assertThat(response.at("/childAndApplicantRelations/0/value/childId").asText())
                .as("callback keeps Father linked").isEqualTo(response.at("/newChildDetails/0/id").asText()),
            () -> assertThat(response.at("/childAndApplicantRelations/1/value/childId").asText())
                .as("callback keeps Mother linked").isEqualTo(response.at("/newChildDetails/1/id").asText())
        );
    }
}
