package uk.gov.hmcts.reform.prl.services.caseflags;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.CaseNoteDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.prl.services.caseflags.CaseFlagsService.IS_REVIEW_LANG_AND_SM_REQ_REVIEWED;
import static uk.gov.hmcts.reform.prl.services.caseflags.CaseFlagsService.PLEASE_REVIEW_THE_LANGUAGE_AND_SM_REQUEST;
import static uk.gov.hmcts.reform.prl.services.caseflags.CaseFlagsService.REQUESTED_STATUS_IS_NOT_ALLOWED;
import static uk.gov.hmcts.reform.prl.services.caseflags.CaseFlagsService.SELECTED_REVIEW_LANG_AND_SM_REQ;

@RunWith(MockitoJUnitRunner.class)
public class CaseFlagsServiceTest {
    ObjectMapper objectMapper = new ObjectMapper();
    private CaseFlagsService caseFlagsService;

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

    private static final String CASE_DATA_BEFORE_1 = """
        {
          "id": 1749209054695128
        }
        """;

    private static final String CASE_DATA_BEFORE_2 = """
        {
          "id": 1749209054695128,
          "caseFlags": {
              "details": [],
              "groupId": null,
              "partyName": null,
              "roleOnCase": null,
              "visibility": null
            }
        }
        """;

    private static final String CASE_DATA_BEFORE_3 = """
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
              }
            ],
            "groupId": null,
            "partyName": null,
            "roleOnCase": null,
            "visibility": null
          }
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
          }
        }
        """;

    private static final String CASE_DATA_CURRENT_SINGLE = """
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
                  "status": "<status>",
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
          }
        }
        """;

    @BeforeEach
    public void setUpBeforeEach() {
        setUp();
    }

    @Before
    public void setUpBefore() {
        setUp();
    }

    private void setUp() {
        objectMapper.findAndRegisterModules();
        caseFlagsService = new CaseFlagsService(objectMapper);
    }

    @Test
    public void testPrepareSelectedReviewLangAndSmReq() throws JsonProcessingException {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encode = encoder.encode(CLIENT_CONTEXT.getBytes());
        Map<String, Object> caseDataMap = objectMapper.readValue(CASE_DATA, new TypeReference<>() {});

        caseFlagsService.prepareSelectedReviewLangAndSmReq(caseDataMap, new String(encode));

        assertThat(caseDataMap.get(SELECTED_REVIEW_LANG_AND_SM_REQ))
            .isEqualTo(CaseNoteDetails.builder()
                           .user("Family Private law user")
                           .subject("Support needs request")
                           .caseNote("Mexican interpreter required")
                           .dateAdded("2025-06-02")
                           .dateCreated(LocalDateTime.parse("2025-06-02T17:16:46.227744028"))
                           .build());
    }

    @Test
    public void testWhenLangAndSmReqIsReviewed() throws JsonProcessingException {
        Map<String, Object> caseDataMap = objectMapper.readValue(CASE_DATA_WITH_REVIEW, new TypeReference<>() {});
        List<String> langAndSmReqReviewed = caseFlagsService.isLangAndSmReqReviewed(caseDataMap);
        assertThat(langAndSmReqReviewed).isEmpty();
    }

    @Test
    public void testWhenLangAndSmReqIsNotReviewed() throws JsonProcessingException {
        List<String> langAndSmReqReviewed = caseFlagsService.isLangAndSmReqReviewed(Map.of());
        assertThat(langAndSmReqReviewed).contains(PLEASE_REVIEW_THE_LANGUAGE_AND_SM_REQUEST);
    }

    @Test
    public void testWhenLangAndSmReqReviewedIsNo() throws JsonProcessingException {
        List<String> langAndSmReqReviewed = caseFlagsService.isLangAndSmReqReviewed(Map.of(IS_REVIEW_LANG_AND_SM_REQ_REVIEWED,  YesOrNo.No));
        assertThat(langAndSmReqReviewed).contains(PLEASE_REVIEW_THE_LANGUAGE_AND_SM_REQUEST);
    }


    @ParameterizedTest
    @ValueSource(strings = {CASE_DATA_BEFORE_1, CASE_DATA_BEFORE_2})
    public void validateNewCaseFlagActiveStatus(String dataCaseDataBefore) throws JsonProcessingException {
        Map<String, Object> caseDataBefore = objectMapper.readValue(
            dataCaseDataBefore, new TypeReference<>() {
            }
        );

        String currentCaseData = CASE_DATA_CURRENT_SINGLE.replace("<status>", "Active");
        Map<String, Object> caseDataCurrent = objectMapper.readValue(
            currentCaseData, new TypeReference<>() {
            }
        );

        List<String> errors = caseFlagsService.validateNewCaseFlagStatus(caseDataBefore, caseDataCurrent);
        assertThat(errors).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {CASE_DATA_BEFORE_1, CASE_DATA_BEFORE_2})
    public void validateNewCaseFlagRequestedStatus(String dataCaseDataBefore) throws JsonProcessingException {
        Map<String, Object> caseDataBefore = objectMapper.readValue(
            dataCaseDataBefore, new TypeReference<>() {
            }
        );

        String currentCaseData = CASE_DATA_CURRENT_SINGLE.replace("<status>", "Requested");
        Map<String, Object> caseDataCurrent = objectMapper.readValue(
            currentCaseData, new TypeReference<>() {
            }
        );

        List<String> errors = caseFlagsService.validateNewCaseFlagStatus(caseDataBefore, caseDataCurrent);
        assertThat(errors).contains(REQUESTED_STATUS_IS_NOT_ALLOWED);
    }

    @Test
    public void validateNewCaseFlagReviewedStatus() throws JsonProcessingException {
        Map<String, Object> caseDataBefore = objectMapper.readValue(
            CASE_DATA_BEFORE_3, new TypeReference<>() {
            }
        );

        String currentCaseData = CASE_DATA_CURRENT.replace("<status>", "Requested");

        Map<String, Object> caseDataCurrent = objectMapper.readValue(
            currentCaseData, new TypeReference<>() {
            }
        );

        List<String> errors = caseFlagsService.validateNewCaseFlagStatus(caseDataBefore, caseDataCurrent);
        assertThat(errors).contains(REQUESTED_STATUS_IS_NOT_ALLOWED);
    }

    @Test
    public void validateNewCaseFlagActiveStatus() throws JsonProcessingException {
        Map<String, Object> caseDataBefore = objectMapper.readValue(
            CASE_DATA_BEFORE_3, new TypeReference<>() {
            }
        );

        String currentCaseData = CASE_DATA_CURRENT.replace("<status>", "Active");

        Map<String, Object> caseDataCurrent = objectMapper.readValue(
            currentCaseData, new TypeReference<>() {
            }
        );

        List<String> errors = caseFlagsService.validateNewCaseFlagStatus(caseDataBefore, caseDataCurrent);
        assertThat(errors).isEmpty();
    }
}
