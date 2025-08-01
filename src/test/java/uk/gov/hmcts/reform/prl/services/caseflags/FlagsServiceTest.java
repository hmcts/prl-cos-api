package uk.gov.hmcts.reform.prl.services.caseflags;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.CaseNoteDetails;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.prl.services.caseflags.FlagsService.IS_REVIEW_LANG_AND_SM_REQ_REVIEWED;
import static uk.gov.hmcts.reform.prl.services.caseflags.FlagsService.REQUESTED_STATUS_IS_NOT_ALLOWED;
import static uk.gov.hmcts.reform.prl.services.caseflags.FlagsService.SELECTED_REVIEW_LANG_AND_SM_REQ;

@RunWith(MockitoJUnitRunner.class)
public class FlagsServiceTest {
    ObjectMapper objectMapper = new ObjectMapper();
    private FlagsService flagsService;

    private static final String CLIENT_CONTEXT = """
        {
          "client_context": {
            "user_task": {
              "task_data": {
                "additional_properties": {
                  "caseNoteId": "5e3e4330-4314-4d0b-b3ae-5d08841cbcce"
                }
              }
            }
          }
        }
        """;

    private static final String CASE_DATA = """
        {
          "id": 1748881230535856,
          "caseNotes": [
            {
              "id": "d5a0814d-dceb-4a4d-865e-4a4a01e49474",
              "value": {
                "user": "Family Private law user",
                "subject": "Support needs request",
                "caseNote": "Italian interpreter",
                "dateAdded": "2025-06-02",
                "dateCreated": "2025-06-02T17:27:56.876821996"
              }
            },
            {
              "id": "5e3e4330-4314-4d0b-b3ae-5d08841cbcce",
              "value": {
                "user": "Family Private law user",
                "subject": "Support needs request",
                "caseNote": "Mexican interpreter required",
                "dateAdded": "2025-06-02",
                "dateCreated": "2025-06-02T17:16:46.227744028"
              }
            },
            {
              "id": "4dd0f818-c4ba-4d3e-859e-2b14eb95694f",
              "value": {
                "user": "Family Private law user",
                "subject": "Support needs request",
                "caseNote": "Korean interpreter required",
                "dateAdded": "2025-06-02",
                "dateCreated": "2025-06-02T16:38:06.614934858"
              }
            },
            {
              "id": "35c722be-9a07-4207-aa4c-93ae7fdd71eb",
              "value": {
                "user": "Family Private law user",
                "subject": "Support needs request",
                "caseNote": "Italian interpreter",
                "dateAdded": "2025-06-02",
                "dateCreated": "2025-06-02T16:27:44.593764142"
              }
            }
          ]
        }
        """;

    private static final String CASE_DATA_WITH_REVIEW = """
        {
          "id": 1748881230535856,
          "isReviewLangAndSmReqReviewed": "Yes"
        }
        """;

    private static final String CASE_DATA_CURRENT = """
        {
          "id": 1749209054695128,
          "caseFlags": {
            "details": [
              {
                "id": "3378d61d-0a38-4288-9a10-1b6bb012fe0b",
                "value": {
                  "name": "Complex Case",
                  "path": [
                    {
                      "id": "d60813fa-0630-41f6-b94a-012fe46292fa",
                      "value": "Case"
                    }
                  ],
                  "status": "Active",
                  "name_cy": "Achos Cymhleth",
                  "flagCode": "CF0002",
                  "subTypeKey": null,
                  "flagComment": "test",
                  "subTypeValue": null,
                  "flagComment_cy": null,
                  "dateTimeCreated": "2025-06-09T11:02:09.598Z",
                  "hearingRelevant": "Yes",
                  "subTypeValue_cy": null,
                  "otherDescription": null,
                  "flagUpdateComment": "ttt",
                  "availableExternally": "No",
                  "otherDescription_cy": null
                }
              },
              {
                "id": "2f78d61d-0a38-4288-9a10-1b6bb012fe0b",
                "value": {
                  "name": "Complex Case",
                  "path": [
                    {
                      "id": "d60813fa-0630-41f6-b94a-012fe46292fa",
                      "value": "Case"
                    }
                  ],
                  "status": "<status>",
                  "name_cy": "Achos Cymhleth",
                  "flagCode": "CF0002",
                  "subTypeKey": null,
                  "flagComment": "test",
                  "subTypeValue": null,
                  "flagComment_cy": null,
                  "dateTimeCreated": "2025-06-09T11:04:09.598Z",
                  "hearingRelevant": "Yes",
                  "subTypeValue_cy": null,
                  "otherDescription": null,
                  "flagUpdateComment": "ttt",
                  "availableExternally": "No",
                  "otherDescription_cy": null
                }
              }
            ],
            "groupId": null,
            "partyName": null,
            "roleOnCase": null,
            "visibility": null
          }
        }
        """;

    private static final String CASE_DATA_WITH_CASE_APPLICANT_1_FLAGS_CURRENT = """
        {
          "id": 1749209054695128,
          "caseFlags": {
            "details": [
              {
                "id": "2f78d61d-0a38-4288-9a10-1b6bb012fe0b",
                "value": {
                  "name": "Complex Case",
                  "path": [
                    {
                      "id": "d60813fa-0630-41f6-b94a-012fe46292fa",
                      "value": "Case"
                    }
                  ],
                  "status": "Active",
                  "name_cy": "Achos Cymhleth",
                  "flagCode": "CF0002",
                  "subTypeKey": null,
                  "flagComment": "test",
                  "subTypeValue": null,
                  "flagComment_cy": null,
                  "dateTimeCreated": "2025-06-09T11:02:09.598Z",
                  "hearingRelevant": "Yes",
                  "subTypeValue_cy": null,
                  "otherDescription": null,
                  "flagUpdateComment": "ttt",
                  "availableExternally": "No",
                  "otherDescription_cy": null
                }
              }
            ],
            "groupId": null,
            "partyName": null,
            "roleOnCase": null,
            "visibility": null
          },
          "caApplicant1InternalFlags": {
              "details": [
                {
                  "id": "fb9b4c1d-a6e7-488d-8a8b-7e033d910956",
                  "value": {
                    "name": "Evidence by live link",
                    "path": [
                      {
                        "id": "f75b6c11-e063-44f8-9ef5-7ce78e20841c",
                        "value": "Party"
                      },
                      {
                        "id": "510763d4-f79c-437d-8846-5e6f489ebe6b",
                        "value": "Special measure"
                      }
                    ],
                    "status": "<status>",
                    "name_cy": "Tystiolaeth drwy gyswllt byw",
                    "flagCode": "SM0003",
                    "subTypeKey": null,
                    "flagComment": "test",
                    "subTypeValue": null,
                    "flagComment_cy": null,
                    "dateTimeCreated": "2025-06-10T09:08:23.351Z",
                    "hearingRelevant": "Yes",
                    "subTypeValue_cy": null,
                    "otherDescription": null,
                    "flagUpdateComment": "",
                    "availableExternally": "No",
                    "otherDescription_cy": null
                  }
                },
                {
                  "id": "130049ef-38ff-48d5-8d07-9e742ac85a11",
                  "value": {
                    "name": "Screening witness from accused",
                    "path": [
                      {
                        "id": "ecff55ac-896d-4f7f-a057-b9e6ed93f3d5",
                        "value": "Party"
                      },
                      {
                        "id": "71aec611-83c7-496c-923a-d42c73820fff",
                        "value": "Special measure"
                      }
                    ],
                    "status": "Requested",
                    "name_cy": "Sgrinio tyst rhag y diffynnydd",
                    "flagCode": "SM0002",
                    "subTypeKey": null,
                    "flagComment": "testing",
                    "subTypeValue": null,
                    "flagComment_cy": null,
                    "dateTimeCreated": "2025-06-09T17:01:21.508Z",
                    "hearingRelevant": "Yes",
                    "subTypeValue_cy": null,
                    "otherDescription": null,
                    "flagUpdateComment": "",
                    "availableExternally": "No",
                    "otherDescription_cy": null
                  }
                }
              ],
              "groupId": "caApplicant1",
              "partyName": "John Doe",
              "roleOnCase": "Applicant 1",
              "visibility": "Internal"
            },
            "caApplicant2ExternalFlags": {
                "details": [],
                "groupId": "caApplicant2",
                "partyName": "Martina Graham",
                "roleOnCase": "Applicant 2",
                "visibility": "External"
             },
             "caApplicant3ExternalFlags": {}
        }
        """;

    @Before
    public void setUp() {
        objectMapper.findAndRegisterModules();
        flagsService = new FlagsService(objectMapper);
    }

    @Test
    public void testPrepareSelectedReviewLangAndSmReq() throws JsonProcessingException {
        byte[] encode = Base64.getEncoder().encode(CLIENT_CONTEXT.getBytes());
        Map<String, Object> caseDataMap = objectMapper.readValue(CASE_DATA, new TypeReference<>() {});

        flagsService.prepareSelectedReviewLangAndSmReq(caseDataMap, new String(encode));

        assertThat(caseDataMap)
            .containsEntry(SELECTED_REVIEW_LANG_AND_SM_REQ,
                           CaseNoteDetails.builder()
                              .user("Family Private law user")
                              .subject("Support needs request")
                              .caseNote("Mexican interpreter required")
                              .dateAdded("2025-06-02")
                              .dateCreated(LocalDateTime.parse("2025-06-02T17:16:46.227744028"))
                              .build()
            );
        assertThat(caseDataMap.get(IS_REVIEW_LANG_AND_SM_REQ_REVIEWED)).isNull();
    }

    @Test
    public void validateNewCaseFlagActiveStatus() throws JsonProcessingException {
        String currentCaseData = CASE_DATA_CURRENT.replace("<status>", "Active");

        Map<String, Object> caseDataCurrent = objectMapper.readValue(
            currentCaseData, new TypeReference<>() {
            }
        );

        List<String> errors = flagsService.validateNewFlagStatus(caseDataCurrent);
        assertThat(errors).isEmpty();
    }

    @Test
    public void validateNewCaseFlagRequestedStatus() throws JsonProcessingException {
        String currentCaseData = CASE_DATA_CURRENT.replace("<status>", "Requested");

        Map<String, Object> caseDataCurrent = objectMapper.readValue(
            currentCaseData, new TypeReference<>() {
            }
        );

        List<String> errors = flagsService.validateNewFlagStatus(caseDataCurrent);
        assertThat(errors).contains(REQUESTED_STATUS_IS_NOT_ALLOWED);
    }

    @Test
    public void validateNewApplicantFlagRequestedStatus() throws JsonProcessingException {
        String currentCaseData = CASE_DATA_WITH_CASE_APPLICANT_1_FLAGS_CURRENT.replace("<status>", "Requested");

        Map<String, Object> caseDataCurrent = objectMapper.readValue(
            currentCaseData, new TypeReference<>() {
            }
        );

        List<String> errors = flagsService.validateNewFlagStatus(caseDataCurrent);
        assertThat(errors).contains(REQUESTED_STATUS_IS_NOT_ALLOWED);
    }

    @Test
    public void validateNewApplicantFlagActiveStatus() throws JsonProcessingException {
        String currentCaseData = CASE_DATA_WITH_CASE_APPLICANT_1_FLAGS_CURRENT.replace("<status>", "Active");

        Map<String, Object> caseDataCurrent = objectMapper.readValue(
            currentCaseData, new TypeReference<>() {
            }
        );

        List<String> errors = flagsService.validateNewFlagStatus(caseDataCurrent);
        assertThat(errors).isEmpty();
    }
}
