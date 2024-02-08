package uk.gov.hmcts.reform.prl.clients.cafcass;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import org.apache.http.client.fluent.Executor;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.HashMap;
import java.util.Map;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.prl.clients.util.TestConstants.CAFCASS_AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.prl.clients.util.TestConstants.CAFCASS_CCD_CASE_TYPE_ID_QUERY_PARAM;
import static uk.gov.hmcts.reform.prl.clients.util.TestConstants.CAFCASS_GET_PACT_METHOD;
import static uk.gov.hmcts.reform.prl.clients.util.TestConstants.CAFCASS_PACT_CONSUMER_NAME;
import static uk.gov.hmcts.reform.prl.clients.util.TestConstants.CAFCASS_SEARCH_CASE_PROVIDER;
import static uk.gov.hmcts.reform.prl.clients.util.TestConstants.CAFCASS_SERVICE_AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.prl.clients.util.TestConstants.CAFCASS_TEST_AUTH_TOKEN;
import static uk.gov.hmcts.reform.prl.clients.util.TestConstants.CAFCASS_TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.prl.clients.util.TestConstants.CCD_STORE_SEARCH_CASE_ENDPOINT;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = CAFCASS_SEARCH_CASE_PROVIDER)
@PactFolder("pacts")
@ContextConfiguration(
        classes = {CafcassSearchCaseApiConsumerApplication.class}
)
@TestPropertySource(
    properties = {"bundle.api.url=http://localhost:8899","idam.api.url=localhost:5000","commonData.api.url=localhost:5000",
        "fis_hearing.api.url=localhost:5000",
        "refdata.api.url=",
        "courtfinder.api.url=",
        "prl-dgs-api.url=",
        "fees-register.api.url=",
        "fis_hearing.api.url=",
        "judicialUsers.api.url=",
        "locationfinder.api.url=",
        "rd_professional.api.url=",
        "payments.api.url=",
        "pba.validation.service.api.baseurl=",
        "staffDetails.api.url=",
        "amRoleAssignment.api.url="
    }
)

public class CafcassApiConsumerCcdSearchCaseApiTest {
    @BeforeEach
    public void setupEachTest() {
        System.setProperty("pact.verifier.publishResults", "true");
    }

    @MockBean
    private IdamClient idamClient;

    @After
    void tearDown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = CAFCASS_SEARCH_CASE_PROVIDER, consumer = CAFCASS_PACT_CONSUMER_NAME)
    public RequestResponsePact executeGetSearchCases(PactDslWithProvider builder) {
        return
                builder
                        .given("Search Cases exist in the datetime range for CafCass in CCD Store")
                        .uponReceiving("A request for Cases with date range within CCD")
                        .path(CCD_STORE_SEARCH_CASE_ENDPOINT)
                        .query(CAFCASS_CCD_CASE_TYPE_ID_QUERY_PARAM)
                        .matchHeader(CAFCASS_AUTHORISATION_HEADER, CAFCASS_TEST_AUTH_TOKEN)
                        .matchHeader(CAFCASS_SERVICE_AUTHORISATION_HEADER, CAFCASS_TEST_SERVICE_AUTH_TOKEN)
                        .headers(getResponseHeaders())
                        .method(HttpMethod.GET.toString())
                        .willRespondWith()
                        .body(buildSearchCaseResponseDsl())
                        .status(HttpStatus.OK.value())
                        .toPact();

    }

    @NotNull
    private Map<String, String> getResponseHeaders() {
        Map<String, String> responseHeaders = new HashMap<>();

        responseHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return responseHeaders;
    }

    @Test
    @JsonIgnoreProperties(ignoreUnknown = true)
    @PactTestFor(pactMethod = CAFCASS_GET_PACT_METHOD)
    public void verifySearchCasesByDateRange(MockServer mockServer) {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(CAFCASS_AUTHORISATION_HEADER, CAFCASS_TEST_AUTH_TOKEN)
                        .header(CAFCASS_SERVICE_AUTHORISATION_HEADER, CAFCASS_TEST_SERVICE_AUTH_TOKEN)
                        .contentType(ContentType.JSON)
                        .get(mockServer.getUrl() + CCD_STORE_SEARCH_CASE_ENDPOINT + "?" + CAFCASS_CCD_CASE_TYPE_ID_QUERY_PARAM)
                        .then()
                        .log().all().extract().asString();
        JSONObject jsonResponse = new JSONObject(actualResponseBody);
        assertNotNull(jsonResponse);
    }

    private DslPart buildSearchCaseResponseDsl() {
        DslPart bodyPart = newJsonBody((body) -> {
            body.numberType("total", 1)
                    .array("cases", (cases) -> {
                        cases.object((eachCase) -> {
                            eachCase.numberType("id", 16611647)
                                    .stringType("jurisdiction", "PRIVATELAW")
                                    .stringType("state", "SUBMITTED_PAID")
                                    .stringType("caseTypeOfApplication", "C100")
                                    .stringType("case_type_id", "PRLAPPS")
                                    .stringType("created_date", "2022-08-22T10:39:43.49")
                                    .stringType("last_modified", "2022-08-22T10:44:54.055")
                                    .object("case_data", (caseData) -> {
                                        caseData.stringType("dateSubmitted", "2022-08-22")
                                                .stringType("caseTypeOfApplication", "C100")
                                                .object("confidentialDetails", (confidential) -> {
                                                    confidential.stringType("isConfidentialDetailsAvailable",
                                                            "No");
                                                })
                                                .stringType("childrenKnownToLocalAuthority", "no")
                                                .eachLike("children", (children) -> {
                                                    children.stringType("id",
                                                                    "e1866c81-f3d3-484e-b13a-aad0f9dab468")
                                                            .object("value", (childValue) -> {
                                                                childValue.stringType("firstName", "fgfdg")
                                                                        .stringType("lastName", "gdgffd")
                                                                        .stringType("dateOfBirth",
                                                                                "1990-11-12")
                                                                        .stringType("gender", "male")
                                                                        .eachLike("orderAppliedFor",
                                                                            (childOrderApplied) -> {
                                                                                childOrderApplied
                                                                                        .stringType("childArrangementsOrder",
                                                                                                "childArrangementsOrder");
                                                                            })
                                                                        .stringType("applicantsRelationshipToChild",
                                                                                "stepFather")
                                                                        .stringType("respondentsRelationshipToChild",
                                                                                "mother")
                                                                        .eachLike("childLiveWith", (childLiveWith) -> {
                                                                            childLiveWith.stringType("applicant",
                                                                                    "applicant");
                                                                        })
                                                                        .eachLike("personWhoLivesWithChild",
                                                                            (liveWithChild) -> {
                                                                            })
                                                                        .stringType("parentalResponsibilityDetails",
                                                                                "adw");
                                                            });
                                                })
                                                .object("miamExemptionsTable", (miamExemptions) -> {
                                                    miamExemptions.stringType("reasonsForMiamExemption", "Urgency")
                                                            .stringType("domesticViolenceEvidence", "")
                                                            .stringType("urgencyEvidence",
                                                                    "Any delay caused by MIAM would cause "
                                                                            + "unreasonable hardship to the prospective "
                                                                            + "applicant")
                                                            .stringType("childProtectionEvidence", "")
                                                            .stringType("previousAttendenceEvidence", "")
                                                            .stringType("otherGroundsEvidence", "");
                                                })
                                                .object("summaryTabForOrderAppliedFor", (summaryTab) -> {
                                                    summaryTab.stringType("ordersApplyingFor",
                                                                    "Child Arrangements Order")
                                                            .stringType("typeOfChildArrangementsOrder", "Live with order");
                                                })
                                                .eachLike("applicants", (applicants) -> {
                                                    applicants.stringType("id", "f03aff33-42ca-4b44-9dd9-c414294f87d5")
                                                            .object("value", (applicantOne) -> {
                                                                applicantOne.stringType("firstName", "sdfdsf")
                                                                        .stringType("lastName", "sdf")
                                                                        .stringType("previousName", "sdf")
                                                                        .stringType("dateOfBirth",
                                                                                "2007-12-12")
                                                                        .stringType("gender", "male")
                                                                        .stringType("placeOfBirth",
                                                                                "Harrow")
                                                                        .stringType("isAddressConfidential",
                                                                                "No")
                                                                        .stringType("isAtAddressLessThan5Years",
                                                                                "No")
                                                                        .stringType("canYouProvideEmailAddress",
                                                                                "No")
                                                                        .stringType("isPhoneNumberConfidential",
                                                                                "No")
                                                                        .eachLike("otherPersonRelationshipToChildren",
                                                                            (otherApplicantRelationToChild) -> {
                                                                            })
                                                                        .object("solicitorOrg", (solicitorOrg) -> {
                                                                            solicitorOrg.stringType("OrganisationID",
                                                                                            "V4031UM")
                                                                                    .stringType("OrganisationName",
                                                                                            "AutoTestt43tdf4af7d");
                                                                        })
                                                                        .object("solicitorAddress", (solicitorAddress) -> {
                                                                            solicitorAddress
                                                                                    .stringType("AddressLine1", "29 Radnor Road")
                                                                                    .stringType("AddressLine2", "")
                                                                                    .stringType("AddressLine3", "")
                                                                                    .stringType("PostTown", "Harrow")
                                                                                    .stringType("County", "")
                                                                                    .stringType("Country", "United Kingdom")
                                                                                    .stringType("PostCode", "HA1 1SA");
                                                                        })
                                                                        .stringType("representativeFirstName", "Ayansh")
                                                                        .stringType("representativeLastName", "Aman")
                                                                        .stringType("solicitorEmail", "aa@gmail.com")
                                                                        .stringType("phoneNumber", "07442772347")
                                                                        .object("address", (address) -> {
                                                                            address
                                                                                    .stringType("AddressLine1", "29 Radnor Road")
                                                                                    .stringType("AddressLine2", "")
                                                                                    .stringType("AddressLine3", "")
                                                                                    .stringType("PostTown", "Harrow")
                                                                                    .stringType("County", "")
                                                                                    .stringType("Country", "United Kingdom")
                                                                                    .stringType("PostCode", "HA11SA");
                                                                        });
                                                            });
                                                })
                                                .eachLike("respondents", (respondents) -> {
                                                    respondents.stringType("id", "5581032c-1333-4bf2-9651-9d2433351c1a")
                                                            .object("value", (respondentOne) -> {
                                                                respondentOne.stringType("firstName", "sad")
                                                                        .stringType("lastName", "sdf")
                                                                        .stringType("gender", "male")
                                                                        .stringType("canYouProvideEmailAddress", "No")
                                                                        .stringType("isDateOfBirthKnown", "No")
                                                                        .stringType("isCurrentAddressKnown", "No")
                                                                        .stringType("canYouProvidePhoneNumber", "No")
                                                                        .stringType("isPlaceOfBirthKnown", "No")
                                                                        .eachLike("otherPersonRelationshipToChildren",
                                                                            (otherRespondentRelationToChild) -> {
                                                                            })
                                                                        .object("solicitorOrg", (respondentSolicitorOrg) -> {
                                                                        })
                                                                        .object("solicitorAddress", (respondentSolicitorAddress) -> {
                                                                        })
                                                                        .stringType("isAtAddressLessThan5YearsWithDontKnow", "no")
                                                                        .stringType("doTheyHaveLegalRepresentation", "no")
                                                                        .object("address", (address) -> {
                                                                        });
                                                            });
                                                })
                                                .eachLike("applicantsConfidentialDetails",
                                                    (applicantConfidentialDetails) -> {
                                                    })
                                                .stringType("applicantSolicitorEmailAddress",
                                                        "prl-e2etestsolicitor@mailinator.com")
                                                .stringType("solicitorName",
                                                        "E2E Test Solicitor")
                                                .stringType("courtName",
                                                        "West London Family Court")
                                                .eachLike("otherPeopleInTheCaseTable",
                                                    (otherPeopleInTheCase) -> {
                                                        otherPeopleInTheCase.stringType("id",
                                                                        "a5d86587-ba06-4db0-8620-10ef472af3e5")
                                                                    .object("value", (otherPeopleInTheCaseValue) -> {
                                                                        otherPeopleInTheCaseValue.object("address",
                                                                            (otherPeopleAddress) -> {
                                                                            })
                                                                            .eachLike("relationshipToChild",
                                                                                (otherRelationshipToChild) -> {
                                                                                });
                                                                    });
                                                    })
                                                .object("submitAndPayDownloadApplicationLink",
                                                    (submitAndPayDownloadApplicationLink) -> {
                                                        submitAndPayDownloadApplicationLink.stringType(
                                                                    "document_filename",
                                                                    "Draft_C100_application.pdf")
                                                                .stringType("document_id",
                                                                                "e7226e49-fc92-4c12-bac5-e3e50ee4ff15");
                                                    });
                                    });
                        });
                    });
        }).build();

        return bodyPart;
    }
}
