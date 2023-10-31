package uk.gov.hmcts.reform.prl.services.citizen;

import javassist.NotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.CA_DA_CASE_WITHDRAWN;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.CITIZEN_CASE_SUBMISSION;


@PropertySource(value = "classpath:application.yaml")
@RunWith(MockitoJUnitRunner.Silent.class)
public class CitizenEmailServiceTest {

    private static final String authToken = "Bearer TestAuthToken";
    private static final String emailId = "test@test.com";

    @Value("${citizen.url}")
    private String citizenSignUpLink;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private CitizenEmailService citizenEmailService;

    @Test
    public void sendCaseSubmissionEmail() throws NotFoundException {

        //Given
        UserDetails userDetails = UserDetails.builder()
            .email(emailId)
            .build();

        CaseData caseData = CaseData.builder().id(12345L).applicantCaseName("Test case").build();

        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        //When
        citizenEmailService.sendCitizenCaseSubmissionEmail(authToken, caseData);

        //Then
        verify(emailService).send(eq(emailId), eq(CITIZEN_CASE_SUBMISSION), any(),
                                  eq(LanguagePreference.english)
        );
    }

    @Test
    public void sendCaseWithdrawEmail() {

        //Given
        UserDetails userDetails = UserDetails.builder()
            .email(emailId)
            .forename("first")
            .surname("last")
            .build();

        CaseData caseData = CaseData.builder().id(12345L).applicantCaseName("Test case").build();

        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        //When
        citizenEmailService.sendCitizenCaseWithdrawalEmail(authToken, caseData);

        //Then
        verify(emailService).send(eq(emailId), eq(CA_DA_CASE_WITHDRAWN), any(),
                                  eq(LanguagePreference.english)
        );
    }

}
