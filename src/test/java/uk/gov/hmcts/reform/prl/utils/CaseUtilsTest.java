package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.prl.models.wa.WaMapper;

import java.io.IOException;

import static java.util.Base64.getDecoder;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.base64Encode;

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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testWhenClientContextSetTaskCompletionFlag(boolean completeTask) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        WaMapper waMapper = mapper.readValue(CLIENT_CONTEXT, WaMapper.class);
        String encodedString = base64Encode(waMapper, mapper);
        assertThat(encodedString).isNotNull();

        String encodedClientContext = CaseUtils.setTaskCompletion(encodedString, mapper, () -> completeTask);
        byte[] decodeClientContext = getDecoder().decode(encodedClientContext);
        WaMapper updatedWaMapper = mapper.readValue(decodeClientContext, WaMapper.class);
        assertThat(updatedWaMapper.getClientContext().getUserTask().isCompleteTask())
            .isEqualTo(completeTask);
    }

    @Test
    public void testWhenClientContextNotPresent() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        assertThat(CaseUtils.setTaskCompletion(null, mapper, () -> false))
            .isNull();
    }
}
