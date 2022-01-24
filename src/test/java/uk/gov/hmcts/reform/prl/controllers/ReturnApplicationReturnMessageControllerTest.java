package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.UserService;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.RejectReasonEnum.consentOrderNotProvided;


@PropertySource(value = "classpath:application.yaml")
@RunWith(SpringRunner.class)
public class ReturnApplicationReturnMessageControllerTest {

    public static final String authToken = "Bearer TestAuthToken";

    public static final String caseName = "123";

    public static final String ccdId = "1234567890123456";

    public static final String legalName = "John Smith";

    @InjectMocks
    private ReturnApplicationReturnMessageController returnApplicationReturnMessageController;

    CaseData casedata;

    private CallbackRequest callbackRequest;

    @Mock
    private UserDetails userDetails;

    @Mock
    private UserService userService;

    @Before
    public void setUp() {

        userDetails = UserDetails.builder()
            .forename("solicitor@example.com")
            .surname("Solicitor")
            .build();
    }


    @Test
    public void whenNoOptionSelectedThenNoRejectReasonSelectedReturnTrue() {
        casedata = CaseData.builder().build();

        Assertions.assertTrue(returnApplicationReturnMessageController.noRejectReasonSelected(casedata));
    }

    @Test
    public void whenHasOptionSelectedThenNoRejectReasonSelectedReturnFalse() {

        casedata = CaseData.builder()
            .rejectReason(Collections.singletonList(consentOrderNotProvided))
            .build();

        Assertions.assertFalse(returnApplicationReturnMessageController.noRejectReasonSelected(casedata));
    }

    @Test
    public void whenNoApplicantGetLegalFullNameReturnConstantString() {
        casedata = CaseData.builder().build();

        Assertions.assertEquals("[Legal representative name]",returnApplicationReturnMessageController.getLegalFullName(casedata));
    }

    @Test
    public void whenHasApplicantNoRepresentativeNameGetLegalFullNameReturnConstantString() {
        casedata = CaseData.builder().applicants(null).build();

        Assertions.assertEquals("[Legal representative name]",returnApplicationReturnMessageController.getLegalFullName(casedata));
    }

    @Test
    public void whenHasApplicantRepresentativeNameGetLegalFullNameReturnLegalRepresentativeFullName() {
        PartyDetails applicant = PartyDetails.builder().representativeFirstName("John").representativeLastName("Smith").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        casedata = CaseData.builder().applicants(applicantList).build();

        Assertions.assertEquals("John Smith",returnApplicationReturnMessageController.getLegalFullName(casedata));
    }

    @Test
    public void shouldStartReturnApplicationReturnMessageWithCaseDetails() throws Exception {
        PartyDetails applicant = PartyDetails.builder().representativeFirstName("John").representativeLastName("Smith").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        StringBuilder returnMsgStr = new StringBuilder();

        returnMsgStr.append("Subject line: Application returned: " + caseName + "\n")
            .append("Case name: " + caseName + "\n")
            .append("Reference code: " + ccdId + "\n\n")
            .append("Dear " + legalName + ",\n\n")
            .append("Thank you for your application."
                        + " Your application has been reviewed and is being returned for the following reasons:" + "\n\n")
            .append(consentOrderNotProvided.getReturnMsgText());

        returnMsgStr.append("Please resolve these issues and resubmit your application.\n\n")
            .append("Kind regards,\n")
            .append("solicitor@example.com Solicitor");

        callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder()
                             .caseId(ccdId)
                             .caseData(CaseData.builder()
                                           .applicantCaseName(caseName)
                                           .applicants(applicantList)
                                           .rejectReason(Collections.singletonList(consentOrderNotProvided))
                                           .returnMessage(returnMsgStr.toString())
                                           .build())
                             .build())
            .build();


        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        returnApplicationReturnMessageController.returnApplicationReturnMessage(authToken, callbackRequest);

        verify(userService).getUserDetails(authToken);
        verifyNoMoreInteractions(userService);


    }
}
