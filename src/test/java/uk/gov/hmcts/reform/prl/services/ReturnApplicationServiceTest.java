package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.FL401RejectReasonEnum;
import uk.gov.hmcts.reform.prl.enums.RejectReasonEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.FL401RejectReasonEnum.witnessStatementNotProvided;
import static uk.gov.hmcts.reform.prl.enums.RejectReasonEnum.consentOrderNotProvided;

@RunWith(MockitoJUnitRunner.class)
public class ReturnApplicationServiceTest {

    @InjectMocks
    ReturnApplicationService returnApplicationService;

    @Mock
    private UserDetails userDetails;

    CaseData casedata;

    CaseData caseDataFl401;

    @Before
    public void setUp() {
        PartyDetails applicant = PartyDetails.builder().representativeFirstName("John").representativeLastName("Smith").build();

        casedata = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicantCaseName("TestCase")
            .id(123L)
            .rejectReason(Collections.singletonList(consentOrderNotProvided))
            .build();

        caseDataFl401 = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .applicantCaseName("TestCase")
            .applicantsFL401(applicant)
            .id(123L)
            .fl401RejectReason(Collections.singletonList(witnessStatementNotProvided))
            .build();

        userDetails = UserDetails.builder()
            .forename("solicitor@example.com")
            .surname("Solicitor")
            .build();
    }

    @Test
    public void whenNoOptionSelectedThenNoRejectReasonSelectedReturnTrue() {
        casedata = CaseData.builder().build();

        assertTrue(returnApplicationService.noRejectReasonSelected(casedata));
    }

    @Test
    public void whenHasOptionSelectedThenNoRejectReasonSelectedReturnFalse() {

        casedata = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .rejectReason(Collections.singletonList(consentOrderNotProvided))
            .build();

        assertFalse(returnApplicationService.noRejectReasonSelected(casedata));
    }

    @Test
    public void whenHasOptionSelectedThenNoFl401RejectReasonSelectedReturnFalse() {

        casedata = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .fl401RejectReason(Collections.singletonList(witnessStatementNotProvided))
            .build();

        assertFalse(returnApplicationService.noRejectReasonSelected(casedata));
    }

    @Test
    public void whenMulitpleApplicantsGetLegalFullNameReturnsSolicitorFullNameWhoFileTheApplication() {
        PartyDetails applicant = PartyDetails.builder().representativeFirstName("John").representativeLastName("Smith").build();
        PartyDetails applicant2 = PartyDetails.builder().representativeFirstName("Mary").representativeLastName("Walker").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        Element<PartyDetails> wrappedApplicant2 = Element.<PartyDetails>builder().value(applicant2).build();
        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(wrappedApplicant);
        applicantList.add(wrappedApplicant2);

        casedata = CaseData.builder()
                           .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
                           .applicants(applicantList).solicitorName("Testing Solicitor").build();

        assertEquals("Testing Solicitor",returnApplicationService.getLegalFullName(casedata));
    }

    @Test
    public void whenOnlyOneApplicantWithOneSolicitorGetLegalFullNameReturnConstantInputSolicitorName() {
        PartyDetails applicant = PartyDetails.builder().representativeFirstName("John").representativeLastName("Smith").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        casedata = CaseData.builder().caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).applicants(applicantList).build();

        assertEquals("John Smith",returnApplicationService.getLegalFullName(casedata));
    }

    @Test
    public void whenHasApplicantRepresentativeNameGetLegalFullNameReturnLegalRepresentativeFullName() {
        PartyDetails applicant = PartyDetails.builder().representativeFirstName("John").representativeLastName("Smith").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        casedata = CaseData.builder().caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).applicants(applicantList).build();

        assertEquals("John Smith",returnApplicationService.getLegalFullName(casedata));
    }

    @Test
    public void whenHasApplicantRepresentativeNameGetLegalFullNameReturnLegalRepresentativeFullNameForFl401() {
        assertEquals("John Smith",returnApplicationService.getLegalFullName(caseDataFl401));
    }

    @Test
    public void testGetReturnMessage() {
        StringBuilder returnMsgStr = new StringBuilder();

        returnMsgStr
            .append("Case name: TestCase\n")
            .append("Reference code: 123\n\n")
            .append("Dear " + returnApplicationService.getLegalFullName(casedata) + ",\n\n")
            .append("Thank you for your application."
                        + " Your application has been reviewed and is being returned for the following reasons:" + "\n\n");

        for (RejectReasonEnum reasonEnum : casedata.getRejectReason()) {
            returnMsgStr.append(reasonEnum.getReturnMsgText());
        }

        returnMsgStr.append("Please resolve these issues and resubmit your application.\n\n")
            .append("Kind regards,\n")
            .append(userDetails.getFullName());


        assertEquals(returnMsgStr.toString(),returnApplicationService.getReturnMessage(casedata,userDetails));
    }

    @Test
    public void testGetFl401ReturnMessage() {
        StringBuilder returnMsgStr = new StringBuilder();

        returnMsgStr
            .append("Case name: TestCase\n")
            .append("Reference code: 123\n\n")
            .append("Dear " + returnApplicationService.getLegalFullName(caseDataFl401) + ",\n\n")
            .append("Thank you for your application."
                        + " Your application has been reviewed and is being returned for the following reasons:" + "\n\n");

        for (FL401RejectReasonEnum reasonEnum : caseDataFl401.getFl401RejectReason()) {
            returnMsgStr.append(reasonEnum.getReturnMsgText());
        }

        returnMsgStr.append("Please resolve these issues and resubmit your application.\n\n")
            .append("Kind regards,\n")
            .append(userDetails.getFullName());


        assertEquals(returnMsgStr.toString(),returnApplicationService.getReturnMessage(caseDataFl401,userDetails));
    }

    @Test
    public void testGetReturnMessageForTaskList() {
        StringBuilder returnMsgStr = new StringBuilder();
        returnMsgStr.append("\n\n");
        returnMsgStr.append("<div class='govuk-warning-text'><span class='govuk-warning-text__icon'>!"
                                + "</span><strong class='govuk-warning-text__text'>Application has been returned</strong></div>" + "\n\n");

        returnMsgStr.append("Your application has been  returned for the following reasons:" + "\n\n");

        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(casedata.getCaseTypeOfApplication())) {
            for (RejectReasonEnum reasonEnum : casedata.getRejectReason()) {
                returnMsgStr.append(reasonEnum.getDisplayedValue());
                returnMsgStr.append("\n\n");
            }

        } else if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(casedata.getCaseTypeOfApplication())) {
            for (FL401RejectReasonEnum reasonEnum : casedata.getFl401RejectReason()) {
                returnMsgStr.append(reasonEnum.getDisplayedValue());
                returnMsgStr.append("\n\n");
            }
        }

        returnMsgStr.append("Resolve these concerns and resend your application."
                                + "You have been emailed the full details of your application return.");


        assertEquals(returnMsgStr.toString(), returnApplicationService.getReturnMessageForTaskList(casedata));

    }

    @Test
    public void testGetReturnMessageForTaskListfl401() {
        StringBuilder returnMsgStr = new StringBuilder();
        returnMsgStr.append("\n\n");
        returnMsgStr.append("<div class='govuk-warning-text'><span class='govuk-warning-text__icon'>!"
                                + "</span><strong class='govuk-warning-text__text'>Application has been returned</strong></div>" + "\n\n");

        returnMsgStr.append("Your application has been  returned for the following reasons:" + "\n\n");

        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseDataFl401.getCaseTypeOfApplication())) {
            for (RejectReasonEnum reasonEnum : caseDataFl401.getRejectReason()) {
                returnMsgStr.append(reasonEnum.getDisplayedValue());
                returnMsgStr.append("\n\n");
            }

        } else if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseDataFl401.getCaseTypeOfApplication())) {
            for (FL401RejectReasonEnum reasonEnum : caseDataFl401.getFl401RejectReason()) {
                returnMsgStr.append(reasonEnum.getDisplayedValue());
                returnMsgStr.append("\n\n");
            }
        }

        returnMsgStr.append("Resolve these concerns and resend your application."
                                + "You have been emailed the full details of your application return.");


        assertEquals(returnMsgStr.toString(), returnApplicationService.getReturnMessageForTaskList(caseDataFl401));

    }


}
