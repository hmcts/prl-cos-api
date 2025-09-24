package uk.gov.hmcts.reform.prl.handlers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.barrister.TypeOfBarristerEventEnum;
import uk.gov.hmcts.reform.prl.events.BarristerChangeEvent;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.BarristerEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.FeatureToggleService;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;
import uk.gov.hmcts.reform.prl.utils.MaskEmail;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.D_MMM_YYYY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.barrister.TypeOfBarristerEventEnum.removeBarrister;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.CA_DA_ADD_BARRISTER_SELF;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.CA_DA_ADD_BARRISTER_TO_SOLICITOR;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.CA_DA_REMOVE_BARRISTER_SELF;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.CA_DA_REMOVE_BARRISTER_TO_SOLICITOR;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@Slf4j
class BarristerChangeEventHandlerTest {

    @Mock
    private EmailService emailService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Spy
    private MaskEmail maskEmail;
    @Captor
    private ArgumentCaptor<BarristerEmail> barristerEmailTemplateVarsArgumentCaptor;
    @InjectMocks
    private BarristerChangeEventHandler barristerChangeEventHandler;

    private BarristerChangeEvent barristerChangeEvent;
    private PartyDetails applicant1;
    private PartyDetails applicant2;
    private PartyDetails respondent1;
    private PartyDetails respondent2;
    private CaseData caseData;
    private AllocatedBarrister allocatedBarrister;

    @BeforeEach
    void init() {
        applicant1 = PartyDetails.builder()
            .firstName("af1").lastName("al1")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("afl11@test.com")
            .contactPreferences(ContactPreferences.email)
            .build();
        applicant2 = PartyDetails.builder()
            .firstName("af2").lastName("al2")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("asf2").representativeLastName("asl2")
            .solicitorEmail("asl22@test.com")
            .build();
        respondent1 = PartyDetails.builder()
            .firstName("rf1").lastName("rl1")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("rfl11@test.com")
            .partyId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
            .contactPreferences(ContactPreferences.email)
            .build();
        respondent2 = PartyDetails.builder()
            .firstName("rf2").lastName("rl2")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .email("rfl11@test.com")
            .representativeFirstName("rsf2").representativeLastName("rsl2")
            .solicitorEmail("rsl22@test.com")
            .build();

        allocatedBarrister = AllocatedBarrister.builder()
            .barristerFirstName("barristerFirstName")
            .barristerLastName("barristerLastName")
            .barristerEmail("testbarristeremail@test.com")
            .solicitorEmail("solicitorEmail@gmail.com")
            .solicitorFullName("Solfirst Sollast")
            .build();
        caseData = CaseData.builder()
            .id(123L)
            .allocatedBarrister(allocatedBarrister)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(Arrays.asList(element(applicant1), element(applicant2)))
            .respondents(Arrays.asList(element(respondent1), element(respondent2)))
            .build();

        barristerChangeEvent = BarristerChangeEvent.builder()
            .typeOfEvent(TypeOfBarristerEventEnum.addBarrister)
            .caseData(caseData)
            .build();
    }

    @Test
    void shouldNotifyAddBarristerWhenCaseTypeIsC100() {
        when(featureToggleService.isBarristerFeatureEnabled())
            .thenReturn(true);
        barristerChangeEventHandler.notifyAddBarrister(barristerChangeEvent);

        BarristerEmail expectedBarristerEmailVars = BarristerEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .barristerName(allocatedBarrister.getBarristerFullName())
            .solicitorName(allocatedBarrister.getSolicitorFullName())
            .issueDate(CommonUtils.formatDate(D_MMM_YYYY, caseData.getIssueDate()))
            .caseLink("null/123")
            .build();

        verify(emailService).send(
            anyString(),
            eq(CA_DA_ADD_BARRISTER_TO_SOLICITOR),
            barristerEmailTemplateVarsArgumentCaptor.capture(),
            any());


        BarristerEmail barristerEmailVars = barristerEmailTemplateVarsArgumentCaptor.getValue();
        assertThat(barristerEmailVars)
            .isEqualTo(expectedBarristerEmailVars);

        verify(emailService).send(
            anyString(),
            eq(CA_DA_ADD_BARRISTER_SELF),
            barristerEmailTemplateVarsArgumentCaptor.capture(),
            any());

        barristerEmailVars = barristerEmailTemplateVarsArgumentCaptor.getValue();
        assertThat(barristerEmailVars)
            .isEqualTo(expectedBarristerEmailVars);
    }

    @Test
    void shouldNotNotifyAddBarristerWhenNoEmailAddressIsProvided() {
        caseData = CaseData.builder()
            .id(123L)
            .allocatedBarrister(AllocatedBarrister.builder().build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(Arrays.asList(element(applicant1), element(applicant2)))
            .respondents(Arrays.asList(element(respondent1), element(respondent2)))
            .build();
        barristerChangeEvent = barristerChangeEvent.toBuilder()
            .caseData(caseData)
            .build();
        barristerChangeEventHandler.notifyAddBarrister(barristerChangeEvent);

        verify(emailService, never()).send(
            anyString(),
            isA(EmailTemplateNames.class),
            any(),
            any());
    }

    @Test
    void shouldNotifyAddBarristerWhenCaseTypeIsC100AndHasOneSolicitor() {
        when(featureToggleService.isBarristerFeatureEnabled())
            .thenReturn(true);
        caseData = caseData.toBuilder()
            .allocatedBarrister(caseData.getAllocatedBarrister().toBuilder()
                                    .solicitorEmail(null)
                                    .build())
            .applicants(Arrays.asList(element(applicant1)))
            .respondents(Arrays.asList(element(respondent1)))
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        barristerChangeEvent = barristerChangeEvent.toBuilder()
            .caseData(caseData)
            .build();

        barristerChangeEventHandler.notifyAddBarrister(barristerChangeEvent);

        verify(emailService).send(
            anyString(),
            eq(CA_DA_ADD_BARRISTER_SELF),
            any(),
            any());

        verify(emailService, never()).send(
            anyString(),
            eq(CA_DA_ADD_BARRISTER_TO_SOLICITOR),
            any(),
            any());
    }

    @Test
    void shouldNotifyAddBarristerWhenCaseTypeIsFL401() {
        when(featureToggleService.isBarristerFeatureEnabled())
            .thenReturn(true);
        caseData = caseData.toBuilder()
            .allocatedBarrister(caseData.getAllocatedBarrister().toBuilder()
                                    .solicitorEmail(null)
                                    .build())
            .applicants(Collections.emptyList())
            .respondents(Collections.emptyList())
            .applicantsFL401(applicant1)
            .respondentsFL401(respondent1)
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .build();

        barristerChangeEvent = barristerChangeEvent.toBuilder()
            .caseData(caseData)
            .build();
        barristerChangeEventHandler.notifyAddBarrister(barristerChangeEvent);

        verify(emailService).send(
            anyString(),
            eq(CA_DA_ADD_BARRISTER_SELF),
            any(),
            any());

        verify(emailService, never()).send(
            anyString(),
            eq(CA_DA_ADD_BARRISTER_TO_SOLICITOR),
            any(),
            any());


    }

    @Test
    void shouldNotifyWhenBarristerIsRemovedWhenCaseTypeIsC100() {
        when(featureToggleService.isBarristerFeatureEnabled())
            .thenReturn(true);
        BarristerChangeEvent barristerRemoveEvent = barristerChangeEvent.toBuilder()
            .typeOfEvent(removeBarrister)
            .build();
        BarristerEmail expectedBarristerEmailVars = BarristerEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .barristerName(allocatedBarrister.getBarristerFullName())
            .solicitorName(allocatedBarrister.getSolicitorFullName())
            .issueDate(CommonUtils.formatDate(D_MMM_YYYY, caseData.getIssueDate()))
            .caseLink("null/123")
            .build();

        barristerChangeEventHandler.notifyWhenBarristerRemoved(barristerRemoveEvent);

        verify(emailService).send(
            anyString(),
            eq(CA_DA_REMOVE_BARRISTER_SELF),
            barristerEmailTemplateVarsArgumentCaptor.capture(),
            any());

        BarristerEmail barristerEmailVars = barristerEmailTemplateVarsArgumentCaptor.getValue();
        assertThat(barristerEmailVars)
            .isEqualTo(expectedBarristerEmailVars);

        verify(emailService).send(
            anyString(),
            eq(CA_DA_REMOVE_BARRISTER_TO_SOLICITOR),
            barristerEmailTemplateVarsArgumentCaptor.capture(),
            any());

        barristerEmailVars = barristerEmailTemplateVarsArgumentCaptor.getValue();
        assertThat(barristerEmailVars)
            .isEqualTo(expectedBarristerEmailVars);
    }

    @Test
    void shouldNotNotifyWhenBarristerFeatureIsNotEnabled() {
        when(featureToggleService.isBarristerFeatureEnabled())
            .thenReturn(false);
        barristerChangeEventHandler.notifyWhenBarristerRemoved(barristerChangeEvent);

        verify(emailService, never()).send(
            anyString(),
            any(),
            any(),
            any());

    }

    @Test
    void shouldNotNotifyWhenBarristerIsRemovedWhenNoEmailAddressIsProvided() {
        when(featureToggleService.isBarristerFeatureEnabled())
            .thenReturn(true);
        caseData = CaseData.builder()
            .id(123L)
            .allocatedBarrister(AllocatedBarrister.builder()
                                    .solicitorEmail("solicitorEmail@gmail.com")
                                    .build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(Arrays.asList(element(applicant1), element(applicant2)))
            .respondents(Arrays.asList(element(respondent1), element(respondent2)))
            .build();
        barristerChangeEvent = barristerChangeEvent.toBuilder()
            .typeOfEvent(removeBarrister)
            .caseData(caseData)
            .build();
        barristerChangeEventHandler.notifyWhenBarristerRemoved(barristerChangeEvent);

        verify(emailService, never()).send(
            anyString(),
            eq(CA_DA_REMOVE_BARRISTER_SELF),
            any(),
            any());

        verify(emailService).send(
            anyString(),
            eq(CA_DA_REMOVE_BARRISTER_TO_SOLICITOR),
            any(),
            any());
    }

    @Test
    void shouldNotifyWhenBarristerIsRemovedWhenCaseTypeIsC100AndHasOneSolicitor() {
        when(featureToggleService.isBarristerFeatureEnabled())
            .thenReturn(true);
        caseData = caseData.toBuilder()
            .applicants(Arrays.asList(element(applicant1)))
            .respondents(Arrays.asList(element(respondent1)))
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        barristerChangeEvent = barristerChangeEvent.toBuilder()
            .typeOfEvent(removeBarrister)
            .caseData(caseData)
            .build();

        barristerChangeEventHandler.notifyWhenBarristerRemoved(barristerChangeEvent);

        verify(emailService).send(
            anyString(),
            eq(CA_DA_REMOVE_BARRISTER_SELF),
            any(),
            any());

    }

    @Test
    void shouldNotifyWhenBarristerIsRemovedWhenCaseTypeIsFL401() {
        when(featureToggleService.isBarristerFeatureEnabled())
            .thenReturn(true);
        caseData = caseData.toBuilder()
            .applicants(Collections.emptyList())
            .respondents(Collections.emptyList())
            .applicantsFL401(applicant1)
            .respondentsFL401(respondent1)
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .build();

        barristerChangeEvent = barristerChangeEvent.toBuilder()
            .typeOfEvent(removeBarrister)
            .caseData(caseData)
            .build();
        barristerChangeEventHandler.notifyWhenBarristerRemoved(barristerChangeEvent);

        verify(emailService).send(
            anyString(),
            eq(CA_DA_REMOVE_BARRISTER_SELF),
            any(),
            any());
    }

}
