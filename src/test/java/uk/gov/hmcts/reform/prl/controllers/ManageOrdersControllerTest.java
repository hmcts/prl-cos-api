package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;

import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@PropertySource(value = "classpath:application.yaml")
@RunWith(MockitoJUnitRunner.Silent.class)
public class ManageOrdersControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private ManageOrdersController manageOrdersController;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CaseDetails caseDetails;

    @Mock
    private CaseData caseData;

    public static final String authToken = "Bearer TestAuthToken";


    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSubmitApplicationEventValidation() throws Exception {
        CaseData expectedCaseData = CaseData.builder()
            .id(12345L)
            .appointmentOfGuardian(Document.builder().build())
            .build();

        Map<String, Object> stringObjectMap = expectedCaseData.toMap(new ObjectMapper());

        CaseData caseData = CaseData.builder()
            .previewOrderDoc(Document.builder().build())
            .build();
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(expectedCaseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        CallbackResponse callbackResponse = manageOrdersController.populatePreviewOrderWhenOrderUploaded(callbackRequest);
        assertNotNull(callbackResponse.getData().getPreviewOrderDoc());
    }
}
