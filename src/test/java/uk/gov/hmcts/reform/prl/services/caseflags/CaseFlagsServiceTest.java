package uk.gov.hmcts.reform.prl.services.caseflags;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.CaseNoteDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.prl.services.caseflags.CaseFlagsService.ADD_CASE_NOTE_TYPE;
import static uk.gov.hmcts.reform.prl.services.caseflags.CaseFlagsService.IS_REVIEW_LANG_AND_SM_REQ_REVIEWED;

@RunWith(MockitoJUnitRunner.Silent.class)
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

    @Before
    public void setUp() {
        objectMapper.findAndRegisterModules();
        caseFlagsService = new CaseFlagsService(objectMapper);
    }

    @Test
    public void testPrepareSelectedReviewLangAndSmReq() throws JsonProcessingException {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encode = encoder.encode(CLIENT_CONTEXT.getBytes());
        Map<String, Object> caseDataMap = objectMapper.readValue(CASE_DATA, new TypeReference<>() {});

        caseFlagsService.prepareSelectedReviewLangAndSmReq(caseDataMap, new String(encode));

        assertThat(caseDataMap.get(ADD_CASE_NOTE_TYPE))
            .isEqualTo(CaseNoteDetails.builder()
                           .user("Family Private law user")
                           .subject("Support needs request")
                           .caseNote("Mexican interpreter required")
                           .dateAdded("2025-06-02")
                           .dateCreated(LocalDateTime.parse("2025-06-02T17:16:46.227744028"))
                           .build());

        assertThat(caseDataMap.get(IS_REVIEW_LANG_AND_SM_REQ_REVIEWED))
            .isEqualTo(YesOrNo.No);
    }
}
