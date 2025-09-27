package uk.gov.hmcts.reform.prl.controllers.testingsupport;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.prl.controllers.ControllerTestSupport;
import uk.gov.hmcts.reform.prl.services.hearingmanagement.HearingManagementService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExtendWith(SpringExtension.class)
@DisplayName("HearingSupportController (when enabled")
@WebMvcTest(HearingSupportController.class)
@TestPropertySource(properties = { "hearing.preview.bypass.enabled=true" })
@Import(ControllerTestSupport.class)
public class HearingSupportControllerEnabledTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HearingManagementService hearingManagementService;

    @Test
    @DisplayName("the endpoint should be available and return 200 OK")
    void testHearingSupportEndPointIsEnabled() throws Exception {
        String url = "/hearing-support/testing/is-enabled";
        mockMvc.perform(get(url))
            .andExpect(status().isOk());
    }
}
