package uk.gov.hmcts.reform.prl.handlers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.events.BarristerChangeEvent;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.FeatureToggleService;
import uk.gov.hmcts.reform.prl.utils.MaskEmail;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@Slf4j
class BarristerChangeEventHandlerTest {

    @Mock
    private EmailService emailService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private MaskEmail maskEmail;

    @InjectMocks
    private BarristerChangeEventHandler barristerChangeEventHandler;

    private BarristerChangeEvent barristerChangeEvent;
    private PartyDetails applicant1;
    private PartyDetails applicant2;
    private PartyDetails respondent1;
    private PartyDetails respondent2;
    private CaseData caseData;

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

        caseData = CaseData.builder()
            .id(123L)
            .allocatedBarrister(AllocatedBarrister.builder()
                                    .barristerFirstName("barristerFirstName")
                                    .barristerLastName("barristerLastName")
                                    .barristerEmail("testbarristeremail@test.com")
                                    .build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(Arrays.asList(element(applicant1), element(applicant2)))
            .respondents(Arrays.asList(element(respondent1), element(respondent2)))
            .build();

        barristerChangeEvent = BarristerChangeEvent.builder()
            .caseData(caseData)
            .build();
        when(maskEmail.mask(anyString()))
            .thenReturn("mask@email.com");
    }

    @Test
    void shouldNotifyAddBarristerWhenCaseTypeIsC100() {
        barristerChangeEventHandler.notifyAddBarrister(barristerChangeEvent);

        verify(emailService,times(3)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());

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

        verify(emailService,times(2)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());

    }

    @Test
    void shouldNotifyAddBarristerWhenCaseTypeIsC100AndHasOneSolicitor() {

        caseData = caseData.toBuilder()
            .applicants(Arrays.asList(element(applicant1)))
            .respondents(Arrays.asList(element(respondent1)))
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        barristerChangeEvent = barristerChangeEvent.toBuilder()
            .caseData(caseData)
            .build();

        barristerChangeEventHandler.notifyAddBarrister(barristerChangeEvent);

        verify(emailService,times(1)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());

    }

    @Test
    void shouldNotifyAddBarristerWhenCaseTypeIsFL401() {
        caseData = caseData.toBuilder()
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

        verify(emailService,times(1)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());

    }

    @Test
    void shouldNotifyWhenBarristerIsRemovedWhenCaseTypeIsC100() {
        when(featureToggleService.isBarristerFeatureEnabled())
            .thenReturn(true);
        barristerChangeEventHandler.notifyWhenBarristerRemoved(barristerChangeEvent);

        verify(emailService,times(3)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());

    }

    @Test
    void shouldNotNotifyWhenBarristerFeatureIsNotEnabled() {
        when(featureToggleService.isBarristerFeatureEnabled())
            .thenReturn(false);
        barristerChangeEventHandler.notifyWhenBarristerRemoved(barristerChangeEvent);

        verify(emailService, never()).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());

    }

    @Test
    void shouldNotNotifyWhenBarristerIsRemovedWhenNoEmailAddressIsProvided() {
        when(featureToggleService.isBarristerFeatureEnabled())
            .thenReturn(true);
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
        barristerChangeEventHandler.notifyWhenBarristerRemoved(barristerChangeEvent);

        verify(emailService,times(2)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());

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
            .caseData(caseData)
            .build();

        barristerChangeEventHandler.notifyWhenBarristerRemoved(barristerChangeEvent);

        verify(emailService,times(1)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());

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
            .caseData(caseData)
            .build();
        barristerChangeEventHandler.notifyWhenBarristerRemoved(barristerChangeEvent);

        verify(emailService,times(1)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());

    }

}
