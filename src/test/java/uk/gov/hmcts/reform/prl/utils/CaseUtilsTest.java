package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ApplicantRespondentOrgPolicies;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.wa.WaMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

class CaseUtilsTest {

    @Test
    void shouldMapCaseDetailsToCaseData() {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.findAndRegisterModules(); // picks up ParameterNamesModule, JavaTimeModule, etc.

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        objectMapper.disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
        Map<String, Object> data = new HashMap<>();
        data.put("taskListVersion", "V2");
        data.put("caseTypeOfApplication", "C100");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .state("SUBMITTED_PAID")
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .data(data)
            .build();

        // Act
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        // Assert
        assertThat(caseData.getTaskListVersion()).isEqualTo("V2");
        assertThat(caseData.getCaseTypeOfApplication()).isEqualTo("C100");
        assertThat(caseData.getId()).isEqualTo(12345L);
        assertThat(caseData.getState()).isEqualTo(State.SUBMITTED_PAID);
        assertThat(caseData.getCreatedDate()).isNotNull();
        assertThat(caseData.getLastModifiedDate()).isNotNull();
    }

    @Test
    void shouldUnwrapApplicantRespondentOrganisationPolicies() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        OrganisationPolicy applicantPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder()
                              .organisationID("ORG-123")
                              .organisationName("Test organisation")
                              .build())
            .orgPolicyReference("applicant-policy-ref")
            .orgPolicyCaseAssignedRole("[C100APPLICANTSOLICITOR1]")
            .build();

        CaseData caseData = CaseData.builder()
            .applicantRespondentOrgPolicies(ApplicantRespondentOrgPolicies.builder()
                                                .caApplicant1Policy(applicantPolicy)
                                                .build())
            .build();

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(caseData));

        // The crucial JsonUnwrapped assertion:
        assertThat(json.has("caApplicant1Policy")).isTrue();
        assertThat(json.has("applicantRespondentOrgPolicies")).isFalse();

        // Verify the CCD policy data has not been altered.
        assertThat(json.at("/caApplicant1Policy/OrgPolicyReference").asText())
            .isEqualTo("applicant-policy-ref");
        assertThat(json.at("/caApplicant1Policy/OrgPolicyCaseAssignedRole").asText())
            .isEqualTo("[C100APPLICANTSOLICITOR1]");
        assertThat(json.at("/caApplicant1Policy/Organisation/OrganisationID").asText())
            .isEqualTo("ORG-123");

        // Also prove incoming flattened CCD JSON binds back to the wrapper.
        CaseData deserialised = objectMapper.readValue(json.toString(), CaseData.class);

        assertThat(deserialised.getApplicantRespondentOrgPolicies()
                       .getCaApplicant1Policy().getOrgPolicyReference())
            .isEqualTo("applicant-policy-ref");
    }

    @ParameterizedTest
    @MethodSource
    void testIsC100CaseIssued(CaseData caseData, boolean expected) {
        assertThat(CaseUtils.isC100CaseIssued(caseData)).isEqualTo(expected);
    }

    private static Stream<Arguments> testIsC100CaseIssued() {
        return Stream.of(
            Arguments.of(caseData(C100_CASE_TYPE, null), false),
            Arguments.of(caseData(C100_CASE_TYPE, LocalDate.now()), true),
            Arguments.of(caseData(FL401_CASE_TYPE, null), false),
            Arguments.of(caseData(FL401_CASE_TYPE, LocalDate.now()), false)
        );
    }

    @Test
    void testGetC8FileName() {
        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("John")
            .lastName("Doe")
            .build();

        String c8FileName = CaseUtils.getC8FileName(partyDetails, false, false);
        assertThat(c8FileName)
            .startsWith("Confidential_C8 of John Doe ")
            .endsWith(".pdf")
            .matches("^Confidential_C8 of John Doe .+\\.pdf$");

        String welshC8FileName = CaseUtils.getC8FileName(partyDetails, true, false);
        assertThat(welshC8FileName)
            .startsWith("Confidential_C8 of John Doe ")
            .endsWith("Welsh.pdf")
            .matches("^Confidential_C8 of John Doe .+ Welsh\\.pdf$");
    }

    @Test
    void testGetDraftC8FileName() {
        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("John")
            .lastName("Doe")
            .build();

        String c8FileName = CaseUtils.getC8FileName(partyDetails, false, true);
        assertThat(c8FileName)
            .startsWith("Confidential_C8 of John Doe ")
            .endsWith("Draft.pdf")
            .matches("^Confidential_C8 of John Doe .+ Draft\\.pdf$");

        String welshC8FileName = CaseUtils.getC8FileName(partyDetails, true, true);
        assertThat(welshC8FileName)
            .startsWith("Confidential_C8 of John Doe ")
            .endsWith(" Welsh Draft.pdf")
            .matches("^Confidential_C8 of John Doe .+ Welsh Draft\\.pdf$");
    }

    private static CaseData caseData(String caseType, LocalDate issueDate) {
        return CaseData.builder()
            .caseTypeOfApplication(caseType)
            .issueDate(issueDate)
            .build();
    }

    @Test
    void getWaMapperReturnsNullForNullClientContext() {
        assertThat(CaseUtils.getWaMapper(null)).isNull();
    }

    @Test
    void getWaMapperReturnsNullForMalformedBase64ClientContext() {
        assertThat(CaseUtils.getWaMapper("not-base64!!!")).isNull();
    }

    @Test
    void getWaMapperReturnsNullForBase64StringThatIsNotValidJson() {
        String notJson = Base64.getEncoder().encodeToString("not json".getBytes(StandardCharsets.UTF_8));

        assertThat(CaseUtils.getWaMapper(notJson)).isNull();
    }

    @Test
    void getWaMapperParsesValidBase64EncodedClientContext() {
        String json = "{\"client_context\":{\"user_task\":{\"task_data\":{\"additional_properties\":{\"hearingId\":\"999\"}}}}}";
        String encoded = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));

        WaMapper waMapper = CaseUtils.getWaMapper(encoded);

        assertThat(waMapper).isNotNull();
        assertThat(CaseUtils.getHearingId(waMapper)).isEqualTo("999");
    }
}
