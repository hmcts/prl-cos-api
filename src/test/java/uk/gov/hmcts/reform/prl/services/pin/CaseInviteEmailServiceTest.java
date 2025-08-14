package uk.gov.hmcts.reform.prl.services.pin;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.CaseInviteEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.services.EmailService;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseInviteEmailServiceTest {

    @InjectMocks
    private CaseInviteEmailService caseInviteEmailService;

    @Mock
    EmailService emailService;

    CaseData caseData;

    PartyDetails applicant;
    PartyDetails respondent;

    @Before
    public void init() {

        applicant = PartyDetails.builder()
            .firstName("applfirst")
            .lastName("applLast")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("applicant@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("respfirst")
            .lastName("respLast")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondent@test.com")
            .build();

        caseData = CaseData.builder()
            .id(12345667899855L)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .applicantsFL401(applicant)
            .respondentsFL401(respondent)
            .build();

    }

    @Test
    public void testBuildCaseInviteEmailForApplicant() {

        CaseInvite caseInvite = CaseInvite.builder()
            .partyId(UUID.randomUUID())
            .caseInviteEmail("test@caseinvite.com")
            .accessCode("657fgh")
            .invitedUserId("45679")
            .hasLinked("testLink")
            .expiryDate(LocalDate.parse("2022-11-12"))
            .build();

        EmailTemplateVars caseInviteEmail = CaseInviteEmail.builder()
            .caseInvite(caseInvite)
            .caseReference(String.valueOf(caseData.getId()))
            .caseData(caseData)
            .caseLink("testLink")
            .citizenSignUpLink("citizenTestLink")
            .party(applicant)
            .build();

        EmailTemplateVars emailTemplateVars = caseInviteEmailService.buildCaseInviteEmail(caseInvite, applicant, caseData);

        assertEquals(caseInviteEmail, emailTemplateVars);

    }

    @Test
    public void testBuildCaseInviteEmailForRespondent() {

        CaseInvite caseInvite = CaseInvite.builder()
            .partyId(UUID.randomUUID())
            .caseInviteEmail("test@caseinvite.com")
            .accessCode("657fgh")
            .invitedUserId("45679")
            .hasLinked("testLink")
            .expiryDate(LocalDate.parse("2022-11-12"))
            .build();

        EmailTemplateVars caseInviteEmail = CaseInviteEmail.builder()
            .caseInvite(caseInvite)
            .caseReference(String.valueOf(caseData.getId()))
            .caseData(caseData)
            .caseLink("testLink")
            .citizenSignUpLink("citizenTestLink")
            .party(respondent)
            .build();

        EmailTemplateVars emailTemplateVars = caseInviteEmailService.buildCaseInviteEmail(caseInvite, respondent, caseData);

        assertEquals(caseInviteEmail, emailTemplateVars);

    }

    @Test
    public void testSendEmailForApplicant() {

        CaseInvite caseInvite = CaseInvite.builder()
            .partyId(UUID.randomUUID())
            .caseInviteEmail("test@caseinvite.com")
            .accessCode("657fgh")
            .invitedUserId("45679")
            .hasLinked("testLink")
            .expiryDate(LocalDate.parse("2022-11-12"))
            .build();

        EmailTemplateVars caseInviteEmail = CaseInviteEmail.builder()
            .caseInvite(caseInvite)
            .caseReference(String.valueOf(caseData.getId()))
            .caseData(caseData)
            .caseLink("testLink")
            .citizenSignUpLink("citizenTestLink")
            .party(applicant)
            .build();

        caseInviteEmailService.sendEmail(applicant.getEmail(), caseInviteEmail);

        assertEquals("applicant@test.com", caseData.getApplicantsFL401().getEmail());

    }

    @Test
    public void testSendEmailForRespondent() {

        CaseInvite caseInvite = CaseInvite.builder()
            .partyId(UUID.randomUUID())
            .caseInviteEmail("test@caseinvite.com")
            .accessCode("657fgh")
            .invitedUserId("45679")
            .hasLinked("testLink")
            .expiryDate(LocalDate.parse("2022-11-12"))
            .build();

        EmailTemplateVars caseInviteEmail = CaseInviteEmail.builder()
            .caseInvite(caseInvite)
            .caseReference(String.valueOf(caseData.getId()))
            .caseData(caseData)
            .caseLink("testLink")
            .citizenSignUpLink("citizenTestLink")
            .party(respondent)
            .build();

        caseInviteEmailService.sendEmail(respondent.getEmail(), caseInviteEmail);

        assertEquals("respondent@test.com", caseData.getRespondentsFL401().getEmail());

    }

    @Test
    public void testSendCaseInviteEmailForApplicant() {

        CaseInvite caseInvite = CaseInvite.builder()
            .partyId(UUID.randomUUID())
            .caseInviteEmail("test@caseinvite.com")
            .accessCode("657fgh")
            .invitedUserId("45679")
            .hasLinked("testLink")
            .expiryDate(LocalDate.parse("2022-11-12"))
            .build();

        EmailTemplateVars caseInviteEmail = CaseInviteEmail.builder()
            .caseInvite(caseInvite)
            .caseReference(String.valueOf(caseData.getId()))
            .caseData(caseData)
            .caseLink("testLink")
            .citizenSignUpLink("citizenTestLink")
            .party(applicant)
            .build();

        caseInviteEmailService.sendCaseInviteEmail(caseInvite, applicant, caseData);

        assertEquals("applicant@test.com", caseData.getApplicantsFL401().getEmail());

    }

    @Test
    public void testSendCaseInviteEmailForRespondent() {

        CaseInvite caseInvite = CaseInvite.builder()
            .partyId(UUID.randomUUID())
            .caseInviteEmail("test@caseinvite.com")
            .accessCode("657fgh")
            .invitedUserId("45679")
            .hasLinked("testLink")
            .expiryDate(LocalDate.parse("2022-11-12"))
            .build();

        EmailTemplateVars caseInviteEmail = CaseInviteEmail.builder()
            .caseInvite(caseInvite)
            .caseReference(String.valueOf(caseData.getId()))
            .caseData(caseData)
            .caseLink("testLink")
            .citizenSignUpLink("citizenTestLink")
            .party(respondent)
            .build();

        caseInviteEmailService.sendCaseInviteEmail(caseInvite, respondent, caseData);

        assertEquals("respondent@test.com", caseData.getRespondentsFL401().getEmail());

    }
}
