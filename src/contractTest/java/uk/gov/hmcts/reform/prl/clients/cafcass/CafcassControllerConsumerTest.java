package uk.gov.hmcts.reform.prl.clients.cafcass;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.controllers.cafcass.CafCassController;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.prl.clients.util.TestConstants.CAFCASS_AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.prl.clients.util.TestConstants.CAFCASS_AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.prl.clients.util.TestConstants.CAFCASS_CCD_CASE_TYPE_ID_QUERY_PARAM;
import static uk.gov.hmcts.reform.prl.clients.util.TestConstants.CAFCASS_SEARCH_CASE_ENDPOINT;
import static uk.gov.hmcts.reform.prl.clients.util.TestConstants.CAFCASS_SERVICE_AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.prl.clients.util.TestConstants.CAFCASS_SERVICE_AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.prl.clients.util.TestConstants.CAFCASS_TEST_AUTH_TOKEN;
import static uk.gov.hmcts.reform.prl.clients.util.TestConstants.CAFCASS_TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.prl.clients.util.TestConstants.CCD_STORE_SEARCH_CASE_ENDPOINT;
import static uk.gov.hmcts.reform.prl.clients.util.TestConstants.PRL_CAFCASS_PACT_CONSUMER_NAME;
import static uk.gov.hmcts.reform.prl.clients.util.TestConstants.PRL_CAFCASS_SEARCH_CASE_PROVIDER;

/*@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = PRL_CAFCASS_SEARCH_CASE_PROVIDER)
@PactFolder("pacts")
@ContextConfiguration(
        classes = {CafcassControllerConsumerApplication.class}
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
        "amRoleAssignment.api.url=",
        "core_case_data.api.url="
    }
)*/
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = PRL_CAFCASS_SEARCH_CASE_PROVIDER)
@PactFolder("pacts")
@ContextConfiguration(
    classes = {CafcassControllerConsumerApplication.class}
)
@TestPropertySource(
    properties = {"bundle.api.url=http://localhost:8899",
        "idam.api.url=localhost:5000", "commonData.api.url=localhost:5000",
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
        "amRoleAssignment.api.url=",
        "core_case_data.api.url="
    }
)
public class CafcassControllerConsumerTest {
    @BeforeEach
    public void setupEachTest() {
        System.setProperty("pact.verifier.publishResults", "true");
    }

    /*@After
    void tearDown() {
        Executor.closeIdleConnections();
    }*/

    @Pact(provider = PRL_CAFCASS_SEARCH_CASE_PROVIDER, consumer = PRL_CAFCASS_PACT_CONSUMER_NAME)
    public V4Pact executeGetSearchCases1(PactDslWithProvider builder) {
        return
            builder
                .given("Search cases with valid credentials")
                .uponReceiving("Search Cases exist in the datetime range for CafCass in CCD Store")
                .path(CAFCASS_SEARCH_CASE_ENDPOINT)
                .query("start_date=2025-03-12T11:10:40.999&end_date=2025-03-12T11:23:40.999")
                .matchHeader(CAFCASS_AUTHORIZATION_HEADER, CAFCASS_TEST_AUTH_TOKEN)
                .matchHeader(CAFCASS_SERVICE_AUTHORIZATION_HEADER, CAFCASS_TEST_SERVICE_AUTH_TOKEN)
                .headers(getResponseHeaders())
                .method(HttpMethod.GET.toString())
                .willRespondWith()
                .body(buildSearchCaseResponseDsl())
                .status(HttpStatus.OK.value())
                .toPact(V4Pact.class);

    }

    @NotNull
    private Map<String, String> getResponseHeaders() {
        Map<String, String> responseHeaders = new HashMap<>();

        responseHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return responseHeaders;
    }

    @Test
    @PactTestFor(pactMethod = "executeGetSearchCases1")
    public void verifySearchCasesByDateRange(MockServer mockServer) {
        String actualResponseBody =
            SerenityRest
                .given()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(CAFCASS_AUTHORIZATION_HEADER, CAFCASS_TEST_AUTH_TOKEN)
                .header(CAFCASS_SERVICE_AUTHORIZATION_HEADER, CAFCASS_TEST_SERVICE_AUTH_TOKEN)
                .contentType(ContentType.JSON)
                .get(mockServer.getUrl() + CAFCASS_SEARCH_CASE_ENDPOINT + "?" + "start_date=2025-03-12T11:10:40.999&end_date=2025-03-12T11:23:40.999")
                .then()
                .log().all().extract().asString();
        JSONObject jsonResponse = new JSONObject(actualResponseBody);

        /*CafCassController cafCassController = new CafCassController(null, null, null, null);
        ResponseEntity<Object> response = cafCassController.searcCasesByDates("", "", "", "");*/

        assertNotNull(jsonResponse);
    }

    /*private DslPart buildSearchCaseResponseDsl() {
     *//*DslPart bodyPart = new PactDslJsonBody((body) -> {
            body.numberType("total", 1)
                .array(
                    "cases", (cases) -> {
                        cases.object((eachCase) -> {
                            eachCase.numberType("id", 16611647)
                                .stringType("jurisdiction", "PRIVATELAW")
                                .stringType("state", "SUBMITTED_PAID")
                                .stringType("caseTypeOfApplication", "C100")
                                .stringType("case_type_id", "PRLAPPS")
                                .stringType("created_date", "2022-08-22T10:39:43.49")
                                .stringType("last_modified", "2022-08-22T10:44:54.055")
                                .object(
                                    "case_data", (caseData) -> {
                                        caseData.stringType("dateSubmitted", "2022-08-22")
                                            .stringType("caseTypeOfApplication", "C100")
                                            .object(
                                                "confidentialDetails", (confidential) -> {
                                                    confidential.stringType(
                                                        "isConfidentialDetailsAvailable",
                                                        "No"
                                                    );
                                                }
                                            )
                                            .stringType("childrenKnownToLocalAuthority", "no")
                                            .eachLike(
                                                "children", (children) -> {
                                                    children.stringType(
                                                            "id",
                                                            "e1866c81-f3d3-484e-b13a-aad0f9dab468"
                                                        )
                                                        .object(
                                                            "value", (childValue) -> {
                                                                childValue.stringType("firstName", "fgfdg")
                                                                    .stringType("lastName", "gdgffd")
                                                                    .stringType(
                                                                        "dateOfBirth",
                                                                        "1990-11-12"
                                                                    )
                                                                    .stringType("gender", "male")
                                                                    .eachLike(
                                                                        "orderAppliedFor",
                                                                        (childOrderApplied) -> {
                                                                            childOrderApplied
                                                                                .stringType(
                                                                                    "childArrangementsOrder",
                                                                                    "childArrangementsOrder"
                                                                                );
                                                                        }
                                                                    )
                                                                    .stringType(
                                                                        "applicantsRelationshipToChild",
                                                                        "stepFather"
                                                                    )
                                                                    .stringType(
                                                                        "respondentsRelationshipToChild",
                                                                        "mother"
                                                                    )
                                                                    .eachLike(
                                                                        "childLiveWith", (childLiveWith) -> {
                                                                            childLiveWith.stringType(
                                                                                "applicant",
                                                                                "applicant"
                                                                            );
                                                                        }
                                                                    )
                                                                    .eachLike(
                                                                        "personWhoLivesWithChild",
                                                                        (liveWithChild) -> {
                                                                        }
                                                                    )
                                                                    .stringType(
                                                                        "parentalResponsibilityDetails",
                                                                        "adw"
                                                                    );
                                                            }
                                                        );
                                                }
                                            )
                                            .object(
                                                "miamExemptionsTable", (miamExemptions) -> {
                                                    miamExemptions.stringType("reasonsForMiamExemption", "Urgency")
                                                        .stringType("domesticViolenceEvidence", "")
                                                        .stringType(
                                                            "urgencyEvidence",
                                                            "Any delay caused by MIAM would cause "
                                                                + "unreasonable hardship to the prospective "
                                                                + "applicant"
                                                        )
                                                        .stringType("childProtectionEvidence", "")
                                                        .stringType("previousAttendenceEvidence", "")
                                                        .stringType("otherGroundsEvidence", "");
                                                }
                                            )
                                            .object(
                                                "summaryTabForOrderAppliedFor", (summaryTab) -> {
                                                    summaryTab.stringType(
                                                            "ordersApplyingFor",
                                                            "Child Arrangements Order"
                                                        )
                                                        .stringType("typeOfChildArrangementsOrder", "Live with order");
                                                }
                                            )
                                            .eachLike(
                                                "applicants", (applicants) -> {
                                                    applicants.stringType("id", "f03aff33-42ca-4b44-9dd9-c414294f87d5")
                                                        .object(
                                                            "value", (applicantOne) -> {
                                                                applicantOne.stringType("firstName", "sdfdsf")
                                                                    .stringType("lastName", "sdf")
                                                                    .stringType("previousName", "sdf")
                                                                    .stringType(
                                                                        "dateOfBirth",
                                                                        "2007-12-12"
                                                                    )
                                                                    .stringType("gender", "male")
                                                                    .stringType(
                                                                        "placeOfBirth",
                                                                        "Harrow"
                                                                    )
                                                                    .stringType(
                                                                        "isAddressConfidential",
                                                                        "No"
                                                                    )
                                                                    .stringType(
                                                                        "isAtAddressLessThan5Years",
                                                                        "No"
                                                                    )
                                                                    .stringType(
                                                                        "canYouProvideEmailAddress",
                                                                        "No"
                                                                    )
                                                                    .stringType(
                                                                        "isPhoneNumberConfidential",
                                                                        "No"
                                                                    )
                                                                    .eachLike(
                                                                        "otherPersonRelationshipToChildren",
                                                                        (otherApplicantRelationToChild) -> {
                                                                        }
                                                                    )
                                                                    .object(
                                                                        "solicitorOrg", (solicitorOrg) -> {
                                                                            solicitorOrg.stringType(
                                                                                    "OrganisationID",
                                                                                    "V4031UM"
                                                                                )
                                                                                .stringType(
                                                                                    "OrganisationName",
                                                                                    "AutoTestt43tdf4af7d"
                                                                                );
                                                                        }
                                                                    )
                                                                    .object(
                                                                        "solicitorAddress", (solicitorAddress) -> {
                                                                            solicitorAddress
                                                                                .stringType(
                                                                                    "AddressLine1",
                                                                                    "29 Radnor Road"
                                                                                )
                                                                                .stringType("AddressLine2", "")
                                                                                .stringType("AddressLine3", "")
                                                                                .stringType("PostTown", "Harrow")
                                                                                .stringType("County", "")
                                                                                .stringType("Country", "United Kingdom")
                                                                                .stringType("PostCode", "HA1 1SA");
                                                                        }
                                                                    )
                                                                    .stringType("representativeFirstName", "Ayansh")
                                                                    .stringType("representativeLastName", "Aman")
                                                                    .stringType("solicitorEmail", "aa@gmail.com")
                                                                    .stringType("phoneNumber", "07442772347")
                                                                    .object(
                                                                        "address", (address) -> {
                                                                            address
                                                                                .stringType(
                                                                                    "AddressLine1",
                                                                                    "29 Radnor Road"
                                                                                )
                                                                                .stringType("AddressLine2", "")
                                                                                .stringType("AddressLine3", "")
                                                                                .stringType("PostTown", "Harrow")
                                                                                .stringType("County", "")
                                                                                .stringType("Country", "United Kingdom")
                                                                                .stringType("PostCode", "HA11SA");
                                                                        }
                                                                    );
                                                            }
                                                        );
                                                }
                                            )
                                            .eachLike(
                                                "respondents", (respondents) -> {
                                                    respondents.stringType("id", "5581032c-1333-4bf2-9651-9d2433351c1a")
                                                        .object(
                                                            "value", (respondentOne) -> {
                                                                respondentOne.stringType("firstName", "sad")
                                                                    .stringType("lastName", "sdf")
                                                                    .stringType("gender", "male")
                                                                    .stringType("canYouProvideEmailAddress", "No")
                                                                    .stringType("isDateOfBirthKnown", "No")
                                                                    .stringType("isCurrentAddressKnown", "No")
                                                                    .stringType("canYouProvidePhoneNumber", "No")
                                                                    .stringType("isPlaceOfBirthKnown", "No")
                                                                    .eachLike(
                                                                        "otherPersonRelationshipToChildren",
                                                                        (otherRespondentRelationToChild) -> {
                                                                        }
                                                                    )
                                                                    .object(
                                                                        "solicitorOrg", (respondentSolicitorOrg) -> {
                                                                        }
                                                                    )
                                                                    .object(
                                                                        "solicitorAddress",
                                                                        (respondentSolicitorAddress) -> {
                                                                        }
                                                                    )
                                                                    .stringType(
                                                                        "isAtAddressLessThan5YearsWithDontKnow",
                                                                        "no"
                                                                    )
                                                                    .stringType("doTheyHaveLegalRepresentation", "no")
                                                                    .object(
                                                                        "address", (address) -> {
                                                                        }
                                                                    );
                                                            }
                                                        );
                                                }
                                            )
                                            .eachLike(
                                                "applicantsConfidentialDetails",
                                                (applicantConfidentialDetails) -> {
                                                }
                                            )
                                            .stringType(
                                                "applicantSolicitorEmailAddress",
                                                "prl-e2etestsolicitor@mailinator.com"
                                            )
                                            .stringType(
                                                "solicitorName",
                                                "E2E Test Solicitor"
                                            )
                                            .stringType(
                                                "courtName",
                                                "West London Family Court"
                                            )
                                            .eachLike(
                                                "otherPeopleInTheCaseTable",
                                                (otherPeopleInTheCase) -> {
                                                    otherPeopleInTheCase.stringType(
                                                            "id",
                                                            "a5d86587-ba06-4db0-8620-10ef472af3e5"
                                                        )
                                                        .object(
                                                            "value", (otherPeopleInTheCaseValue) -> {
                                                                otherPeopleInTheCaseValue.object(
                                                                        "address",
                                                                        (otherPeopleAddress) -> {
                                                                        }
                                                                    )
                                                                    .eachLike(
                                                                        "relationshipToChild",
                                                                        (otherRelationshipToChild) -> {
                                                                        }
                                                                    );
                                                            }
                                                        );
                                                }
                                            )
                                            .object(
                                                "submitAndPayDownloadApplicationLink",
                                                (submitAndPayDownloadApplicationLink) -> {
                                                    submitAndPayDownloadApplicationLink.stringType(
                                                            "document_filename",
                                                            "Draft_C100_application.pdf"
                                                        )
                                                        .stringType(
                                                            "document_id",
                                                            "e7226e49-fc92-4c12-bac5-e3e50ee4ff15"
                                                        );
                                                }
                                            );
                                    }
                                );
                        });
                    }
                );
        }).build();*//*



        return null;


    }*/

    private DslPart buildSearchCaseResponseDsl() {
        /*return new PactDslJsonBody()
            .numberType("total", 1)
            .array("cases")
            .object()
            .numberType("id", 1727708503642694L)
            .stringType("jurisdiction", "PRIVATELAW")
            .stringType("state", "PREPARE_FOR_HEARING_CONDUCT_HEARING")
            .stringType("caseTypeOfApplication", "C100")
            .stringType("case_type_id", "PRLAPPS")
            .stringType("created_date", "2024-09-30T15:01:43.6")
            .stringType("last_modified", "2024-12-18T16:04:56.925")
            .object("case_data")
            .stringType("dateSubmitted", "2024-09-30")
            .stringType("issueDate", "2024-12-18")
            .stringType("caseTypeOfApplication", "C100")
            .object("confidentialDetails")
            .stringType("isConfidentialDetailsAvailable", "Yes")
            .closeObject()
            .stringType("childrenKnownToLocalAuthority", "no")
            .object("finalDocument")
            .stringType("document_filename", "C100FinalDocument.pdf")
            .stringType("document_id", "0e03abac-303b-4772-9522-8dc2139035b3")
            .closeObject()
            .eachLike("ordersApplyingFor")
            .stringType("childArrangementsOrder")
            .closeArray()
            .eachLike("children")
            .stringType("id", "25c694c6-7fe1-479c-a2d3-5cbb5bdfa5ba")
            .object("value")
            .stringType("firstName", "Melissa")
            .stringType("lastName", "Mouse")
            .stringType("dateOfBirth", "2016-03-11")
            .stringType("gender", "female")
            .stringType("otherGender", "Non-binary")
            .eachLike("orderAppliedFor")
            .stringType("childArrangementsOrder")
            .closeArray()
            .stringType("parentalResponsibilityDetails", "Child's mother")
            .object("whoDoesTheChildLiveWith")
            .stringType("partyId", "7fbe97eb-38b1-4675-8bbf-5ab2cdb315d0")
            .stringType("partyFullName", "Minnie Souris")
            .stringType("partyType", "RESPONDENT")
            .closeObject()
            .closeObject()
            .closeArray()
            .object("miamExemptionsTable")
            .stringType("urgencyEvidence", "Urgency")
            .closeObject()
            .stringType("claimingExemptionMiam", "Yes")
            .stringType("applicantAttendedMiam", "No")
            .stringType("mpuChildInvolvedInMiam", "No")
            .stringType("miamUrgencyReason", "Any delay caused by attending a MIAM would cause significant financial hardship to the prospective applicant.")
            .object("summaryTabForOrderAppliedFor")
            .stringType("ordersApplyingFor", "Child Arrangements Order")
            .stringType("typeOfChildArrangementsOrder", "spendTimeWithOrder")
            .closeObject()
            .eachLike("applicants")
            .stringType("id", "d08abac8-2f1a-4e56-945b-572c0ac73a4b")
            .object("value")
            .stringType("firstName", "Mickey")
            .stringType("lastName", "Mouse")
            .stringType("dateOfBirth", "1972-06-23")
            .stringType("gender", "male")
            .stringType("placeOfBirth", "Birmingham")
            .stringType("isAddressConfidential", "Yes")
            .stringType("isAtAddressLessThan5Years", "No")
            .stringType("canYouProvideEmailAddress", "No")
            .stringType("isPhoneNumberConfidential", "Yes")
            .object("solicitorOrg")
            .stringType("OrganisationID", "M1YPZ85")
            .stringType("OrganisationName", "Private solicitors")
            .closeObject()
            .stringType("representativeFirstName", "Donald")
            .stringType("representativeLastName", "Duck")
            .stringType("solicitorEmail", "mm@gmail.com")
            .stringType("phoneNumber", "07920398411")
            .object("address")
            .stringType("AddressLine1", "2 Castle Stead Crescent")
            .stringType("AddressLine3", "Cullingworth")
            .stringType("PostTown", "Bradford")
            .stringType("Country", "United Kingdom")
            .stringType("PostCode", "BD13 5FB")
            .closeObject()
            .closeObject()
            .closeArray()
            .eachLike("respondents")
            .stringType("id", "7fbe97eb-38b1-4675-8bbf-5ab2cdb315d0")
            .object("value")
            .stringType("firstName", "Minnie")
            .stringType("lastName", "Souris")
            .stringType("previousName", "Mouse")
            .stringType("dateOfBirth", "1974-07-03")
            .stringType("gender", "female")
            .stringType("canYouProvideEmailAddress", "No")
            .stringType("isDateOfBirthKnown", "Yes")
            .stringType("isCurrentAddressKnown", "No")
            .stringType("canYouProvidePhoneNumber", "No")
            .stringType("isPlaceOfBirthKnown", "No")
            .stringType("isAtAddressLessThan5YearsWithDontKnow", "dontKnow")
            .stringType("doTheyHaveLegalRepresentation", "dontKnow")
            .closeObject()
            .closeArray()
            .eachLike("applicantsConfidentialDetails")
            .stringType("id", "64410959-52a4-4108-90d0-5d981a1533d8")
            .object("value")
            .stringType("firstName", "Mickey")
            .stringType("lastName", "Mouse")
            .stringType("phoneNumber", "07920398411")
            .object("address")
            .stringType("AddressLine1", "2 Castle Stead Crescent")
            .stringType("AddressLine3", "Cullingworth")
            .stringType("PostTown", "Bradford")
            .stringType("Country", "United Kingdom")
            .stringType("PostCode", "BD13 5FB")
            .closeObject()
            .closeObject()
            .closeArray()
            .stringType("applicantSolicitorEmailAddress", "prl_demo_org1_solicitor_2@mailinator.com")
            .stringType("solicitorName", "PRL DEMO ORG1 Solicitor 2")
            .stringType("courtEpimsId", "36791")
            .stringType("courtTypeId", "18")
            .stringType("courtName", "Brentford County Court And Family Court")
            .eachLike("otherPeopleInTheCaseTable")
            .stringType("id", "d4be8b6f-a3c3-4dd8-8eeb-03ef6519d3b6")
            .object("value")
            .stringType("firstName", "Marigold")
            .stringType("lastName", "Mouse")
            .stringType("isDateOfBirthKnown", "No")
            .stringType("gender", "Female")
            .stringType("isPlaceOfBirthKnown", "No")
            .stringType("isCurrentAddressKnown", "No")
            .stringType("canYouProvideEmailAddress", "No")
            .stringType("canYouProvidePhoneNumber", "No")
            .closeObject()
            .closeArray()
            .object("hearingData")
            .stringType("hmctsServiceCode", "ABA5")
            .stringType("caseRef", "1727708503642694")
            .eachLike("caseHearings")
            .numberType("hearingID", 2000012938L)
            .stringType("hearingType", "ABA5-FHR")
            .stringType("hearingTypeValue", "First Hearing")
            .eachLike("hearingDaySchedule")
            .stringType("hearingVenueName", "Brentford County Court And Family Court")
            .stringType("hearingStartDateTime", "2025-01-10T10:00:00")
            .stringType("hearingEndDateTime", "2025-01-10T10:30:00")
            .stringType("courtTypeId", "18")
            .stringType("epimsId", "36791")
            .closeArray()
            .stringType("hearingStatus", "LISTED")
            .closeArray()
            .closeObject()
            .eachLike("orderCollection")
            .stringType("id", "651bd0d7-f640-4f34-84b5-5d08b4432f34")
            .object("value")
            .eachLike("courtReportType")
            .stringType("Section 7 report")
            .stringType("S16A risk assessment")
            .closeArray()
            .stringType("dateCreated", "2024-12-18T16:02:00.465340")
            .stringType("hearingId", "2000012938")
            .object("orderDocument")
            .stringType("document_filename", "Notice_Of_Proceeding_Order_C6.pdf")
            .stringType("document_id", "71a19473-5e61-4dce-837a-1ba1c8b85a3a")
            .closeObject()
            .stringType("orderType", "noticeOfProceedingsParties")
            .stringType("orderTypeId", "Notice of proceedings (C6) (Notice to parties)")
            .stringType("originalFilingDate", "2025-01-05")
            .object("otherDetails")
            .stringType("orderCreatedDate", "2024-12-18")
            .stringType("orderMadeDate", "2024-12-18")
            .closeObject()
            .object("serveOrderDetails")
            .eachLike("cafcassCymruDocuments")
            .stringType("section7Report")
            .stringType("s16RiskAssessment")
            .closeArray()
            .stringType("whenReportsMustBeFiled", "5 Jan 2025")
            .closeObject()
            .closeObject()
            .closeArray()
            .object("caseManagementLocation")
            .stringType("regionName", "London")
            .stringType("baseLocationName", "Brentford")
            .stringType("region", "1")
            .stringType("baseLocation", "36791")
            .closeObject()
            .eachLike("childAndApplicantRelations")
            .stringType("id", "99b36896-ca7b-4d9a-801e-87dea61b9865")
            .object("value")
            .stringType("partyId", "d08abac8-2f1a-4e56-945b-572c0ac73a4b")
            .stringType("partyFullName", "Mickey Mouse")
            .stringType("partyType", "applicant")
            .stringType("childId", "25c694c6-7fe1-479c-a2d3-5cbb5bdfa5ba")
            .stringType("childFullName", "Melissa Mouse")
            .stringType("relationType", "stepMother")
            .stringType("childLivesWith", "No")
            .closeObject()
            .closeArray()
            .eachLike("childAndRespondentRelations")
            .stringType("id", "f6f30c71-3bf1-4039-bfca-165e557a942a")
            .object("value")
            .stringType("partyId", "7fbe97eb-38b1-4675-8bbf-5ab2cdb315d0")
            .stringType("partyFullName", "Minnie Souris")
            .stringType("partyType", "RESPONDENT")
            .stringType("childId", "25c694c6-7fe1-479c-a2d3-5cbb5bdfa5ba")
            .stringType("childFullName", "Melissa Mouse")
            .stringType("relationType", "mother")
            .stringType("childLivesWith", "Yes")
            .closeObject()
            .closeArray()
            .eachLike("childAndOtherPeopleRelations")
            .stringType("id", "c44dc38d-5877-445d-830f-2d333a60c382")
            .object("value")
            .stringType("partyId", "d4be8b6f-a3c3-4dd8-8eeb-03ef6519d3b6")
            .stringType("partyFullName", "Marigold Mouse")
            .stringType("partyType", "OtherPeople")
            .stringType("childId", "25c694c6-7fe1-479c-a2d3-5cbb5bdfa5ba")
            .stringType("childFullName", "Melissa Mouse")
            .stringType("relationType", "grandParent")
            .stringType("childLivesWith", "No")
            .closeObject()
            .closeArray()
            .object("submitAndPayDownloadApplicationLink")
            .stringType("document_filename", "Draft_C100_application.pdf")
            .stringType("document_id", "050ae0f4-915f-4007-a61e-f2cae0f55813")
            .closeObject()
            .object("c8Document")
            .stringType("document_filename", "C8Document.pdf")
            .stringType("document_id", "c604668d-0eac-49b3-9bd6-a9c35a384ae0")
            .closeObject()
            .closeObject()
            .closeObject()
            .closeArray();*/

        return new PactDslJsonBody()
            .numberType("total", 1)
            .array("cases")
            .object()
            .numberType("id", 1727708503642694L)
            .stringType("jurisdiction", "PRIVATELAW")
            .stringType("state", "PREPARE_FOR_HEARING_CONDUCT_HEARING")
            .stringType("caseTypeOfApplication", "C100")
            .stringType("case_type_id", "PRLAPPS")
            .stringType("created_date", "2024-09-30T15:01:43.6")
            .stringType("last_modified", "2024-12-18T16:04:56.925")
            .object("case_data")
            .stringType("dateSubmitted", "2024-09-30")
            .stringType("issueDate", "2024-12-18")
            .stringType("caseTypeOfApplication", "C100")
            .stringType("childrenKnownToLocalAuthority", "no")
            .stringType("claimingExemptionMiam", "Yes")
            .stringType("applicantAttendedMiam", "No")
            .stringType("mpuChildInvolvedInMiam", "No")
            .stringType(
                "miamUrgencyReason",
                "Any delay caused by attending a MIAM would cause significant financial hardship to the prospective applicant."
            )
            .stringType("applicantSolicitorEmailAddress", "prl_demo_org1_solicitor_2@mailinator.com")
            .stringType("solicitorName", "PRL DEMO ORG1 Solicitor 2")
            .stringType("courtEpimsId", "36791")
            .stringType("courtTypeId", "18")
            .stringType("courtName", "Brentford County Court And Family Court")
            .object("confidentialDetails")
            .stringType("isConfidentialDetailsAvailable", "Yes")
            .closeObject()
            .object("finalDocument")
            .stringType("document_filename", "C100FinalDocument.pdf")
            .stringType("document_id", "0e03abac-303b-4772-9522-8dc2139035b3")
            .closeObject()
            .array("ordersApplyingFor")
            .stringType("childArrangementsOrder")
            .closeArray()
            .eachLike("children")
            .stringType("id", "25c694c6-7fe1-479c-a2d3-5cbb5bdfa5ba")
            .object("value")
            .stringType("firstName", "Melissa")
            .stringType("lastName", "Mouse")
            .stringType("dateOfBirth", "2016-03-11")
            .stringType("gender", "female")
            .stringType("otherGender", "Non-binary")
            .stringType("parentalResponsibilityDetails", "Child's mother")
            .array("orderAppliedFor")
            .stringType("childArrangementsOrder")
            .closeArray()
            .object("whoDoesTheChildLiveWith")
            .stringType("partyId", "7fbe97eb-38b1-4675-8bbf-5ab2cdb315d0")
            .stringType("partyFullName", "Minnie Souris")
            .stringType("partyType", "RESPONDENT")
            .closeObject()
            .closeObject()
            .closeArray()
            .object("miamExemptionsTable")
            .stringType("urgencyEvidence", "Urgency")
            .closeObject()
            .object("summaryTabForOrderAppliedFor")
            .stringType("ordersApplyingFor", "Child Arrangements Order")
            .stringType("typeOfChildArrangementsOrder", "spendTimeWithOrder")
            .closeObject()
            .eachLike("applicants")
            .stringType("id", "d08abac8-2f1a-4e56-945b-572c0ac73a4b")
            .object("value")
            .stringType("firstName", "Mickey")
            .stringType("lastName", "Mouse")
            .stringType("dateOfBirth", "1972-06-23")
            .stringType("gender", "male")
            .stringType("placeOfBirth", "Birmingham")
            .stringType("isAddressConfidential", "Yes")
            .stringType("isAtAddressLessThan5Years", "No")
            .stringType("canYouProvideEmailAddress", "No")
            .stringType("isPhoneNumberConfidential", "Yes")
            .stringType("representativeFirstName", "Donald")
            .stringType("representativeLastName", "Duck")
            .stringType("solicitorEmail", "mm@gmail.com")
            .stringType("phoneNumber", "07920398411")
            .object("solicitorOrg")
            .stringType("OrganisationID", "M1YPZ85")
            .stringType("OrganisationName", "Private solicitors")
            .closeObject()
            .object("address")
            .stringType("AddressLine1", "2 Castle Stead Crescent")
            .stringType("AddressLine3", "Cullingworth")
            .stringType("PostTown", "Bradford")
            .stringType("Country", "United Kingdom")
            .stringType("PostCode", "BD13 5FB")
            .closeObject()
            .closeObject()
            .closeArray()
            .eachLike("respondents")
            .stringType("id", "7fbe97eb-38b1-4675-8bbf-5ab2cdb315d0")
            .object("value")
            .stringType("firstName", "Minnie")
            .stringType("lastName", "Souris")
            .stringType("previousName", "Mouse")
            .stringType("dateOfBirth", "1974-07-03")
            .stringType("gender", "female")
            .stringType("canYouProvideEmailAddress", "No")
            .stringType("isDateOfBirthKnown", "Yes")
            .stringType("isCurrentAddressKnown", "No")
            .stringType("canYouProvidePhoneNumber", "No")
            .stringType("isPlaceOfBirthKnown", "No")
            .stringType("isAtAddressLessThan5YearsWithDontKnow", "dontKnow")
            .stringType("doTheyHaveLegalRepresentation", "dontKnow")
            .closeObject()
            .closeArray()
            .eachLike("applicantsConfidentialDetails")
            .stringType("id", "64410959-52a4-4108-90d0-5d981a1533d8")
            .object("value")
            .stringType("firstName", "Mickey")
            .stringType("lastName", "Mouse")
            .stringType("phoneNumber", "07920398411")
            .object("address")
            .stringType("AddressLine1", "2 Castle Stead Crescent")
            .stringType("AddressLine3", "Cullingworth")
            .stringType("PostTown", "Bradford")
            .stringType("Country", "United Kingdom")
            .stringType("PostCode", "BD13 5FB")
            .closeObject()
            .closeObject()
            .closeArray()
            .eachLike("otherPeopleInTheCaseTable")
            .stringType("id", "d4be8b6f-a3c3-4dd8-8eeb-03ef6519d3b6")
            .object("value")
            .stringType("firstName", "Marigold")
            .stringType("lastName", "Mouse")
            .stringType("isDateOfBirthKnown", "No")
            .stringType("gender", "Female")
            .stringType("isPlaceOfBirthKnown", "No")
            .stringType("isCurrentAddressKnown", "No")
            .stringType("canYouProvideEmailAddress", "No")
            .stringType("canYouProvidePhoneNumber", "No")
            .closeObject()
            .closeArray()
            .object("hearingData")
            .stringType("hmctsServiceCode", "ABA5")
            .stringType("caseRef", "1727708503642694")
            .eachLike("caseHearings")
            .numberType("hearingID", 2000012938L)
            .stringType("hearingType", "ABA5-FHR")
            .stringType("hearingTypeValue", "First Hearing")
            .stringType("hearingStatus", "LISTED")
            .eachLike("hearingDaySchedule")
            .stringType("hearingVenueName", "Brentford County Court And Family Court")
            .stringType("hearingStartDateTime", "2025-01-10T10:00:00")
            .stringType("hearingEndDateTime", "2025-01-10T10:30:00")
            .stringType("courtTypeId", "18")
            .stringType("epimsId", "36791")
            .closeArray()
            .closeArray()
            .closeObject()
            .eachLike("orderCollection")
            .stringType("id", "651bd0d7-f640-4f34-84b5-5d08b4432f34")
            .object("value")
            .stringType("dateCreated", "2024-12-18T16:02:00.465340")
            .stringType("hearingId", "2000012938")
            .stringType("orderType", "noticeOfProceedingsParties")
            .stringType("orderTypeId", "Notice of proceedings (C6) (Notice to parties)")
            .stringType("originalFilingDate", "2025-01-05")
            .array("courtReportType")
            .stringType("Section 7 report")
            .stringType("S16A risk assessment")
            .closeArray()
            .object("orderDocument")
            .stringType("document_filename", "Notice_Of_Proceeding_Order_C6.pdf")
            .stringType("document_id", "71a19473-5e61-4dce-837a-1ba1c8b85a3a")
            .closeObject()
            .object("otherDetails")
            .stringType("orderCreatedDate", "2024-12-18")
            .stringType("orderMadeDate", "2024-12-18")
            .closeObject()
            .closeObject()
            .closeArray()
            .object("caseManagementLocation")
            .stringType("regionName", "London")
            .stringType("baseLocationName", "Brentford")
            .stringType("region", "1")
            .stringType("baseLocation", "36791")
            .closeObject()
            .eachLike("childAndApplicantRelations")
            .stringType("id", "99b36896-ca7b-4d9a-801e-87dea61b9865")
            .object("value")
            .stringType("partyId", "d08abac8-2f1a-4e56-945b-572c0ac73a4b")
            .stringType("partyFullName", "Mickey Mouse")
            .stringType("partyType", "applicant")
            .stringType("childId", "25c694c6-7fe1-479c-a2d3-5cbb5bdfa5ba")
            .stringType("childFullName", "Melissa Mouse")
            .stringType("relationType", "stepMother")
            .stringType("childLivesWith", "No")
            .closeObject()
            .closeArray()
            .eachLike("childAndRespondentRelations")
            .stringType("id", "f6f30c71-3bf1-4039-bfca-165e557a942a")
            .object("value")
            .stringType("partyId", "7fbe97eb-38b1-4675-8bbf-5ab2cdb315d0")
            .stringType("partyFullName", "Minnie Souris")
            .stringType("partyType", "RESPONDENT")
            .stringType("childId", "25c694c6-7fe1-479c-a2d3-5cbb5bdfa5ba")
            .stringType("childFullName", "Melissa Mouse")
            .stringType("relationType", "mother")
            .stringType("childLivesWith", "Yes")
            .closeObject()
            .closeArray()
            .eachLike("childAndOtherPeopleRelations")
            .stringType("id", "c44dc38d-5877-445d-830f-2d333a60c382")
            .object("value")
            .stringType("partyId", "d4be8b6f-a3c3-4dd8-8eeb-03ef6519d3b6")
            .stringType("partyFullName", "Marigold Mouse")
            .stringType("partyType", "OtherPeople")
            .stringType("childId", "25c694c6-7fe1-479c-a2d3-5cbb5bdfa5ba")
            .stringType("childFullName", "Melissa Mouse")
            .stringType("relationType", "grandParent")
            .stringType("childLivesWith", "No")
            .closeObject()
            .closeArray()
            .object("submitAndPayDownloadApplicationLink")
            .stringType("document_filename", "Draft_C100_application.pdf")
            .stringType("document_id", "050ae0f4-915f-4007-a61e-f2cae0f55813")
            .closeObject()
            .object("c8Document")
            .stringType("document_filename", "C8Document.pdf")
            .stringType("document_id", "c604668d-0eac-49b3-9bd6-a9c35a384ae0")
            .closeObject()
            .closeObject()
            .closeObject()
            .closeArray();
    }
}

