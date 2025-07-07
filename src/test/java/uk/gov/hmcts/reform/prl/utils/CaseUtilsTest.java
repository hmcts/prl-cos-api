package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.wa.WaMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Base64.getDecoder;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_INVOKED_FROM_TASK;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.base64Encode;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getHearingId;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.setTaskCompletion;

public class CaseUtilsTest {

    private static final String CLIENT_CONTEXT = """
        {
          "client_context": {
            "user_task": {
              "task_data": {
                "additional_properties": {
                  "hearingId": "12345"
                }
              },
              "complete_task" : true
            }
          }
        }
        """;
    private static final String CLIENT_CONTEXT_WITH_LANGUAGE = """
        {
           "client_context": {
             "user_language": {
               "language": "en"
             }
           }
        }
        """;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testWhenClientContextSetTaskCompletionFlag(boolean completeTask) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        WaMapper waMapper = mapper.readValue(CLIENT_CONTEXT, WaMapper.class);
        String encodedString = base64Encode(waMapper, mapper);
        assertThat(encodedString).isNotNull();

        String encodedClientContext = setTaskCompletion(encodedString,
                                                                  mapper,
                                                                  CaseData.builder().build(),
                                                                  (data) -> completeTask);
        byte[] decodeClientContext = getDecoder().decode(encodedClientContext);
        WaMapper updatedWaMapper = mapper.readValue(decodeClientContext, WaMapper.class);
        assertThat(updatedWaMapper.getClientContext().getUserTask().isCompleteTask())
            .isEqualTo(completeTask);
    }

    @Test
    public void testWhenClientContextNotPresent() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        assertThat(setTaskCompletion(null, mapper, CaseData.builder().build(), (data) -> false))
            .isNull();
    }

    @Test
    public void testWhenClientContextDoesnNotContainTask() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        WaMapper waMapper = mapper.readValue(CLIENT_CONTEXT_WITH_LANGUAGE, WaMapper.class);
        String encodedString = base64Encode(waMapper, mapper);
        assertThat(encodedString).isNotNull();

        assertThat(setTaskCompletion(encodedString, mapper, CaseData.builder().build(), (data) -> false))
            .isNull();
    }

    @Test
    public void testGetHearingIdWhenClientContextIsNull() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(IS_INVOKED_FROM_TASK, YesOrNo.Yes);
        mapper.findAndRegisterModules();
        WaMapper waMapper = mapper.readValue(CLIENT_CONTEXT_WITH_LANGUAGE, WaMapper.class);

        Optional<Long> actualHearingId = getHearingId(waMapper, caseDataMap);

        assertThat(actualHearingId.isEmpty()).isTrue();

    }

    @Test
    public void testGetHearingIdWhenClientContextIsNotNull() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(IS_INVOKED_FROM_TASK, YesOrNo.Yes);
        mapper.findAndRegisterModules();
        WaMapper waMapper = mapper.readValue(CLIENT_CONTEXT, WaMapper.class);

        Optional<Long> actualHearingId = getHearingId(waMapper, caseDataMap);

        assertThat(actualHearingId.orElse(null)).isEqualTo(12345L);
    }
}
