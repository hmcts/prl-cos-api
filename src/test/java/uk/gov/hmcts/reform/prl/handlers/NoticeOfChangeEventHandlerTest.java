package uk.gov.hmcts.reform.prl.handlers;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangeContentProvider;

import java.util.Arrays;
import java.util.Collections;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CARESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DARESPONDENT;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
@Slf4j
public class NoticeOfChangeEventHandlerTest {

    @Mock
    private NoticeOfChangeContentProvider noticeOfChangeContentProvider;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailTemplateVars emailTemplateVars;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @InjectMocks
    private NoticeOfChangeEventHandler noticeOfChangeEventHandler;

    private NoticeOfChangeEvent noticeOfChangeEvent;

    @Before
    public void init() {
        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("af1").lastName("al1")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("afl11@test.com")
            .build();
        PartyDetails applicant2 = PartyDetails.builder()
            .firstName("af2").lastName("al2")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("asf2").representativeLastName("asl2")
            .solicitorEmail("asl22@test.com")
            .build();
        PartyDetails respondent1 = PartyDetails.builder()
            .firstName("rf1").lastName("rl1")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("rfl11@test.com")
            .build();
        PartyDetails respondent2 = PartyDetails.builder()
            .firstName("rf2").lastName("rl2")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .email("rfl11@test.com")
            .representativeFirstName("rsf2").representativeLastName("rsl2")
            .solicitorEmail("rsl22@test.com")
            .build();
        PartyDetails otherPerson = PartyDetails.builder()
            .firstName("of").lastName("ol")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("ofl@test.com")
            .build();
        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(Arrays.asList(element(applicant1), element(applicant2)))
            .respondents(Arrays.asList(element(respondent1), element(respondent2)))
            .othersToNotify(Collections.singletonList(element(otherPerson)))
            .build();
        final String solicitorEmailAddress = "test solicitor email";
        final String solicitorName = "test solicitor name";
        final int representedPartyIndex = 0;
        final SolicitorRole.Representing representing = CAAPPLICANT;
        noticeOfChangeEvent = NoticeOfChangeEvent.builder()
            .caseData(caseData).solicitorEmailAddress(solicitorEmailAddress)
            .solicitorName(solicitorName)
            .representedPartyIndex(representedPartyIndex)
            .representing(representing)
            .accessCode("ABCD1234")
            .build();
    }

    @Test
    public void shouldNotifyLegalRepresentative() {

        noticeOfChangeEventHandler.notifyLegalRepresentative(noticeOfChangeEvent);

        verify(emailService,times(6)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());

    }

    @Test
    public void shouldNotifyWhenLegalRepresentativeRemoved() {
        when(launchDarklyClient.isFeatureEnabled("generate-access-code-for-noc")).thenReturn(true);
        noticeOfChangeEventHandler.notifyWhenLegalRepresentativeRemoved(noticeOfChangeEvent);

        verify(emailService,times(6)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());

    }

    @Test
    public void shouldNotifyWhenCaRespondentRemoved() {
        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("af1").lastName("al1")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("afl11@test.com")
            .build();
        PartyDetails applicant2 = PartyDetails.builder()
            .firstName("af2").lastName("al2")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("asf2").representativeLastName("asl2")
            .solicitorEmail("asl22@test.com")
            .build();
        PartyDetails respondent1 = PartyDetails.builder()
            .firstName("rf1").lastName("rl1")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("rfl11@test.com")
            .build();
        PartyDetails respondent2 = PartyDetails.builder()
            .firstName("rf2").lastName("rl2")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .email("rfl11@test.com")
            .representativeFirstName("rsf2").representativeLastName("rsl2")
            .solicitorEmail("rsl22@test.com")
            .build();
        PartyDetails otherPerson = PartyDetails.builder()
            .firstName("of").lastName("ol")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("ofl@test.com")
            .build();
        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(Arrays.asList(element(applicant1), element(applicant2)))
            .respondents(Arrays.asList(element(respondent1), element(respondent2)))
            .othersToNotify(Collections.singletonList(element(otherPerson)))
            .build();
        final String solicitorEmailAddress = "test solicitor email";
        final String solicitorName = "test solicitor name";
        final int representedPartyIndex = 0;

        final SolicitorRole.Representing representing = CARESPONDENT;

        when(launchDarklyClient.isFeatureEnabled("generate-access-code-for-noc")).thenReturn(true);

        noticeOfChangeEvent = NoticeOfChangeEvent.builder()
            .caseData(caseData).solicitorEmailAddress(solicitorEmailAddress)
            .solicitorName(solicitorName)
            .representedPartyIndex(representedPartyIndex)
            .representing(representing)
            .accessCode("ABCD1234")
            .build();

        noticeOfChangeEventHandler.notifyWhenLegalRepresentativeRemoved(noticeOfChangeEvent);

        verify(emailService,times(6)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());

    }

    @Test
    public void shouldNotifyWhenDaApplicantRemoved() {
        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("af1").lastName("al1")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("afl11@test.com")
            .build();
        PartyDetails respondent1 = PartyDetails.builder()
            .firstName("rf1").lastName("rl1")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("rfl11@test.com")
            .build();
        PartyDetails otherPerson = PartyDetails.builder()
            .firstName("of").lastName("ol")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("ofl@test.com")
            .build();
        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantsFL401(applicant1)
            .respondentsFL401(respondent1)
            .othersToNotify(Collections.singletonList(element(otherPerson)))
            .build();
        final String solicitorEmailAddress = "test solicitor email";
        final String solicitorName = "test solicitor name";
        final int representedPartyIndex = 0;

        final SolicitorRole.Representing representing = DAAPPLICANT;

        noticeOfChangeEvent = NoticeOfChangeEvent.builder()
            .caseData(caseData).solicitorEmailAddress(solicitorEmailAddress)
            .solicitorName(solicitorName)
            .representedPartyIndex(representedPartyIndex)
            .representing(representing)
            .accessCode("ABCD1234")
            .build();

        when(launchDarklyClient.isFeatureEnabled("generate-access-code-for-noc")).thenReturn(true);
        noticeOfChangeEventHandler.notifyWhenLegalRepresentativeRemoved(noticeOfChangeEvent);

        verify(emailService,times(3)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());

    }

    @Test
    public void shouldNotifyWhenDaRespondentRemoved() {
        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("af1").lastName("al1")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("afl11@test.com")
            .build();
        PartyDetails applicant2 = PartyDetails.builder()
            .firstName("af2").lastName("al2")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("asf2").representativeLastName("asl2")
            .solicitorEmail("asl22@test.com")
            .build();
        PartyDetails respondent1 = PartyDetails.builder()
            .firstName("rf1").lastName("rl1")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("rfl11@test.com")
            .build();
        PartyDetails respondent2 = PartyDetails.builder()
            .firstName("rf2").lastName("rl2")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .email("rfl11@test.com")
            .representativeFirstName("rsf2").representativeLastName("rsl2")
            .solicitorEmail("rsl22@test.com")
            .build();
        PartyDetails otherPerson = PartyDetails.builder()
            .firstName("of").lastName("ol")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("ofl@test.com")
            .build();
        final CaseData caseData = CaseData.builder()
            .id(nextLong())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantsFL401(applicant1)
            .respondentsFL401(respondent1)
            .othersToNotify(Collections.singletonList(element(otherPerson)))
            .build();
        final String solicitorEmailAddress = "test solicitor email";
        final String solicitorName = "test solicitor name";
        final int representedPartyIndex = 0;

        final SolicitorRole.Representing representing = DARESPONDENT;

        noticeOfChangeEvent = NoticeOfChangeEvent.builder()
            .caseData(caseData).solicitorEmailAddress(solicitorEmailAddress)
            .solicitorName(solicitorName)
            .representedPartyIndex(representedPartyIndex)
            .representing(representing)
            .accessCode("ABCD1234")
            .build();

        when(launchDarklyClient.isFeatureEnabled("generate-access-code-for-noc")).thenReturn(true);
        noticeOfChangeEventHandler.notifyWhenLegalRepresentativeRemoved(noticeOfChangeEvent);

        verify(emailService,times(3)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());

    }

}
