package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.FeeService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsProvider;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@PropertySource(value = "classpath:application.yaml")
@RunWith(SpringRunner.class)
public class PrePopulateFeeAndSolicitorNameControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private PrePopulateFeeAndSolicitorNameController prePopulateFeeAndSolicitorNameController;

    @Mock
    private UserService userService;

    @Mock
    private DgsService dgsService;
    @Mock
    private FeeService feesService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserDetails userDetails;

    @Mock
    private FeeResponse feeResponse;

    @Mock
    private ApplicationsTabService applicationsTabService;

    public static final String authToken = "Bearer TestAuthToken";

    @Before
    public void setUp() {

        userDetails = UserDetails.builder()
            .forename("solicitor@example.com")
            .surname("Solicitor")
            .build();

        feeResponse = FeeResponse.builder()
            .amount(BigDecimal.valueOf(232.00))
            .build();

    }

    //TODO Update this testcase once we have integration with Fee and Pay
    //@Test
    public void testUserDetailsForSolicitorName() throws Exception {
        CaseDetails caseDetails = CaseDetailsProvider.full();


        CallbackRequest callbackRequest = CallbackRequest.builder().build();
        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder().build();

        when(dgsService.generateDocument(authToken,
                                          callbackRequest.getCaseDetails(),
                                          "PRL-DRAFT-C100-20.docx")).thenReturn(generatedDocumentInfo);

        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        prePopulateFeeAndSolicitorNameController.prePoppulateSolicitorAndFees(authToken, callbackRequest);

        verify(userService).getUserDetails(authToken);
        verifyNoMoreInteractions(userService);

    }

    @Test
    public void testWhenControllerCalledOneInvokeToDgsService() throws Exception {
        CaseDetails caseDetails = CaseDetailsProvider.full();


        CallbackRequest callbackRequest = CallbackRequest.builder().build();
        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder().build();

        when(dgsService.generateDocument(authToken,
                                          callbackRequest.getCaseDetails(),
                                          "PRL-DRAFT-C100-20.docx")).thenReturn(generatedDocumentInfo);

        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        prePopulateFeeAndSolicitorNameController.prePoppulateSolicitorAndFees(authToken, callbackRequest);

        verify(dgsService).generateDocument(authToken,
                                            callbackRequest.getCaseDetails(),
                                            "PRL-DRAFT-C100-20.docx");
        verifyNoMoreInteractions(dgsService);

    }

    @Test
    public void testFeeDetailsForFeeAmount() throws Exception {
        CaseDetails caseDetails = CaseDetailsProvider.full();

        CallbackRequest callbackRequest = CallbackRequest.builder().build();
        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder().build();

        when(dgsService.generateDocument(authToken,
                                          callbackRequest.getCaseDetails(),
                                          "PRL-DRAFT-C100-20.docx")).thenReturn(generatedDocumentInfo);
        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        prePopulateFeeAndSolicitorNameController.prePoppulateSolicitorAndFees(authToken, callbackRequest);

        verify(feesService).fetchFeeDetails(FeeType.C100_SUBMISSION_FEE);
        verifyNoMoreInteractions(feesService);

    }

}
