package uk.gov.hmcts.reform.prl.handlers;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.BulkPrintService;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationPostService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangeContentProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
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
    private LaunchDarklyClient launchDarklyClient;

    @InjectMocks
    private NoticeOfChangeEventHandler noticeOfChangeEventHandler;

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private ServiceOfApplicationPostService serviceOfApplicationPostService;

    @Mock
    private ServiceOfApplicationService serviceOfApplicationService;

    @Mock
    private SystemUserService systemUserService;

    private NoticeOfChangeEvent noticeOfChangeEvent;
    private PartyDetails applicant1;
    private PartyDetails applicant2;
    private PartyDetails respondent1;
    private PartyDetails respondent2;
    private PartyDetails otherPerson;
    private CaseData caseData;

    @Before
    public void init() {
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
        otherPerson = PartyDetails.builder()
            .firstName("of").lastName("ol")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("ofl@test.com")
            .build();
        caseData = CaseData.builder()
            .id(nextLong())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(Arrays.asList(element(applicant1), element(applicant2)))
            .respondents(Arrays.asList(element(respondent1), element(respondent2)))
            .othersToNotify(Collections.singletonList(element(otherPerson)))
            .build();

        noticeOfChangeEvent = NoticeOfChangeEvent.builder()
            .caseData(caseData).solicitorEmailAddress("testemail@test.com")
            .solicitorName("test sol name")
            .representedPartyIndex(0)
            .representing(CAAPPLICANT)
            .accessCode("ABCD1234")
            .build();

        when(launchDarklyClient.isFeatureEnabled("generate-access-code-for-noc")).thenReturn(true);
        when(systemUserService.getSysUserToken()).thenReturn("test auth");
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
        noticeOfChangeEventHandler.notifyWhenLegalRepresentativeRemoved(noticeOfChangeEvent);

        verify(emailService,times(4)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());

    }

    @Test
    public void shouldNotifyWhenCaRespondentRemoved() {
        noticeOfChangeEvent = noticeOfChangeEvent.toBuilder()
            .representing(CARESPONDENT).build();

        noticeOfChangeEventHandler.notifyWhenLegalRepresentativeRemoved(noticeOfChangeEvent);

        verify(emailService,times(4)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());

    }

    @Test
    public void shouldNotifyWhenDaApplicantRemoved() {
        caseData = caseData.toBuilder()
            .applicants(Collections.emptyList())
            .respondents(Collections.emptyList())
            .applicantsFL401(applicant1)
            .respondentsFL401(respondent1)
            .build();
        noticeOfChangeEvent = noticeOfChangeEvent.toBuilder()
            .caseData(caseData)
            .representing(DAAPPLICANT).build();

        noticeOfChangeEventHandler.notifyWhenLegalRepresentativeRemoved(noticeOfChangeEvent);

        verify(emailService,times(2)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());

    }

    @Test
    public void shouldNotifyWhenDaRespondentRemoved() {
        caseData = caseData.toBuilder()
            .applicants(Collections.emptyList())
            .respondents(Collections.emptyList())
            .applicantsFL401(applicant1)
            .respondentsFL401(respondent1)
            .build();
        noticeOfChangeEvent = noticeOfChangeEvent.toBuilder()
            .caseData(caseData)
            .representing(DARESPONDENT).build();

        noticeOfChangeEventHandler.notifyWhenLegalRepresentativeRemoved(noticeOfChangeEvent);

        verify(emailService,times(2)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());

    }

    @Test
    public void shouldNotSendAccessCodeToLipWhenAccessCodeNull() {

        noticeOfChangeEvent = noticeOfChangeEvent.toBuilder()
            .accessCode(null)
            .build();

        noticeOfChangeEventHandler.notifyWhenLegalRepresentativeRemoved(noticeOfChangeEvent);

        Assert.assertNull(bulkPrintService.send(anyString(), anyString(), anyString(), anyList(), anyString()));
    }

    @Test
    public void shouldNotSendAccessCodeToLipWhenAddressIsNull() {
        applicant1 = applicant1.toBuilder()
            .contactPreferences(ContactPreferences.post)
            .build();
        caseData = caseData.toBuilder()
            .applicants(Arrays.asList(element(applicant1), element(applicant2)))
            .build();
        noticeOfChangeEvent = noticeOfChangeEvent.toBuilder()
            .caseData(caseData)
            .build();

        noticeOfChangeEventHandler.notifyWhenLegalRepresentativeRemoved(noticeOfChangeEvent);

        Assert.assertNull(bulkPrintService.send(anyString(), anyString(), anyString(), anyList(), anyString()));
    }

    @Test
    public void shouldSendAccessCodeToLipWhenAvailableViaEmail() throws Exception {
        applicant1 = applicant1.toBuilder()
            .contactPreferences(ContactPreferences.post)
            .address(Address.builder()
                         .addressLine1("test")
                         .build())
            .build();
        caseData = caseData.toBuilder()
            .applicants(Arrays.asList(element(applicant1), element(applicant2)))
            .build();
        noticeOfChangeEvent = noticeOfChangeEvent.toBuilder()
            .caseData(caseData)
            .build();

        when(serviceOfApplicationPostService.getCoverSheets(
            any(CaseData.class),
            anyString(),
            any(Address.class),
            anyString(),
            anyString()
        )).thenReturn(
            List.of(Document.builder().build()));
        when(serviceOfApplicationService.generateAccessCodeLetter(
            anyString(),
            any(CaseData.class),
            any(Element.class),
            any(
                CaseInvite.class),
            anyString()
        )).thenReturn(
            Document.builder().build());
        when(bulkPrintService.send(anyString(), anyString(), anyString(), anyList(), anyString())).thenReturn(UUID.randomUUID());

        noticeOfChangeEventHandler.notifyWhenLegalRepresentativeRemoved(noticeOfChangeEvent);

        Assert.assertNotNull(bulkPrintService.send(anyString(), anyString(), anyString(), anyList(), anyString()));
        verify(emailService,times(4)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());
    }
}
