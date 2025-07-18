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
import uk.gov.hmcts.reform.prl.services.pin.C100CaseInviteService;
import uk.gov.hmcts.reform.prl.services.pin.FL401CaseInviteService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlLaunchDarklyFlagConstants.ENABLE_CITIZEN_ACCESS_CODE_IN_COVER_LETTER;
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

    @Mock
    private C100CaseInviteService c100CaseInviteService;

    @Mock
    private FL401CaseInviteService fl401CaseInviteService;

    private NoticeOfChangeEvent noticeOfChangeEvent;
    private PartyDetails applicant1;
    private PartyDetails applicant2;
    private PartyDetails respondent1;
    private PartyDetails respondent2;
    private PartyDetails respondent3;
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
        respondent3 = PartyDetails.builder()
            .firstName("rf1").lastName("rl1")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("rfl11@test.com")
            .address(Address.builder().addressLine1("test").build())
            .partyId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
            .contactPreferences(ContactPreferences.post)
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
        List<Document> documents = new ArrayList<>();
        documents.add(Document.builder().build());
        when(launchDarklyClient.isFeatureEnabled("generate-access-code-for-noc")).thenReturn(true);
        when(systemUserService.getSysUserToken()).thenReturn("test auth");
        when(serviceOfApplicationService.generateAccessCodeLetter(Mockito.anyString(), Mockito.any(), Mockito.any(),
                                                                  Mockito.any(), Mockito.anyString()))
            .thenReturn(documents);
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

        verify(emailService,times(5)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());

    }

    @Test
    public void shouldNotifyWhenCaRespondentRemoved() {
        noticeOfChangeEvent = noticeOfChangeEvent.toBuilder()
            .representing(CARESPONDENT).build();

        noticeOfChangeEventHandler.notifyWhenLegalRepresentativeRemoved(noticeOfChangeEvent);

        verify(emailService,times(5)).send(Mockito.anyString(),
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
            .caseInvites(List.of(element(CaseInvite
                .builder().partyId(UUID.fromString("00000000-0000-0000-0000-000000000000")).accessCode("test").build())))
            .applicantsFL401(applicant1)
            .respondentsFL401(respondent1)
            .build();
        noticeOfChangeEvent = noticeOfChangeEvent.toBuilder()
            .caseData(caseData)
            .representing(DARESPONDENT).build();
        when(launchDarklyClient.isFeatureEnabled(ENABLE_CITIZEN_ACCESS_CODE_IN_COVER_LETTER)).thenReturn(true);

        noticeOfChangeEventHandler.notifyWhenLegalRepresentativeRemoved(noticeOfChangeEvent);

        verify(emailService,times(3)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());

    }

    @Test
    public void shouldNotifyWhenDaRespondentRemovedPreferencePost() {
        caseData = caseData.toBuilder()
            .applicants(Collections.emptyList())
            .respondents(Collections.emptyList())
            .caseInvites(List.of(element(CaseInvite
                .builder().partyId(UUID.fromString("00000000-0000-0000-0000-000000000000")).accessCode("test").build())))
            .applicantsFL401(applicant1)
            .respondentsFL401(respondent3)
            .build();
        noticeOfChangeEvent = noticeOfChangeEvent.toBuilder()
            .caseData(caseData)
            .representing(DARESPONDENT).build();
        when(launchDarklyClient.isFeatureEnabled(ENABLE_CITIZEN_ACCESS_CODE_IN_COVER_LETTER)).thenReturn(true);

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
    public void shouldSendAccessCodeToLipWhenAvailableViaEmail() {
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
            List.of(Document.builder().build()));
        when(bulkPrintService.send(anyString(), anyString(), anyString(), anyList(), anyString())).thenReturn(UUID.randomUUID());

        noticeOfChangeEventHandler.notifyWhenLegalRepresentativeRemoved(noticeOfChangeEvent);

        Assert.assertNotNull(bulkPrintService.send(anyString(), anyString(), anyString(), anyList(), anyString()));
        verify(emailService,times(5)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());
    }

    @Test
    public void shouldFallbackToPostWhenLiPPrefersEmailButNoAddress() {
        // 1) LiP with no email, preferring email, but **with** a postal address
        PartyDetails noEmailLip = applicant1.toBuilder()
            .email(null)
            .contactPreferences(ContactPreferences.email)
            .address(Address.builder()
                         .addressLine1("1 High Street")
                         .postTown("London")
                         .postCode("E1 1AA")
                         .build())
            .build();
        Element<PartyDetails> lipElement = element(noEmailLip);

        // 2) Match caseInvite by the same ID
        CaseInvite invite = CaseInvite.builder()
            .partyId(lipElement.getId())
            .accessCode("FALLBACK123")
            .build();
        Element<CaseInvite> inviteElement = element(invite);

        // 3) Rebuild CaseData
        caseData = CaseData.builder()
            .id(caseData.getId())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(lipElement))
            .respondents(List.of(element(respondent2)))
            .caseInvites(List.of(inviteElement))
            .othersToNotify(Collections.emptyList())
            .build();

        // 4) Rebuild event with non-null accessCode
        noticeOfChangeEvent = noticeOfChangeEvent.toBuilder()
            .caseData(caseData)
            .representedPartyIndex(0)
            .representing(CAAPPLICANT)
            .accessCode("FALLBACK123")
            .build();

        // 5) Feature flag on
        when(launchDarklyClient.isFeatureEnabled(ENABLE_CITIZEN_ACCESS_CODE_IN_COVER_LETTER))
            .thenReturn(true);

        // 6) Stub document providers
        when(serviceOfApplicationPostService.getCoverSheets(
            any(CaseData.class),
            anyString(),
            any(Address.class),
            anyString(),
            anyString()
        )).thenReturn(Collections.emptyList());
        // generateAccessCodeLetter already stubbed in @Before to return non-empty list

        // Act
        noticeOfChangeEventHandler.notifyWhenLegalRepresentativeRemoved(noticeOfChangeEvent);

        // Assert: fallback to bulk print
        verify(bulkPrintService, times(1))
            .send(anyString(), anyString(), anyString(), anyList(), anyString());

        // And still notify at least one solicitor via email
        verify(emailService, atLeastOnce()).send(anyString(), any(), any(), any());
    }

    @Test
    public void testFetchOrCreateAccessCode_GeneratesAndPersistsInvite() {
        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("Test")
            .partyId(UUID.randomUUID())
            .build();
        Element<PartyDetails> partyElement = element(partyDetails);
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .caseInvites(new ArrayList<>())
            .build();

        CaseInvite generatedInvite = CaseInvite.builder()
            .partyId(partyElement.getId())
            .accessCode("NEWCODE123")
            .build();

        // Mock the correct service
        when(c100CaseInviteService.generateCaseInvite(any(), any()))
            .thenReturn(generatedInvite);

        String accessCode = noticeOfChangeEventHandler.fetchOrCreateAccessCode(
            caseData,
            partyElement,
            CAAPPLICANT
        );

        // Assert
        Assert.assertEquals("NEWCODE123", accessCode);
        Assert.assertFalse(caseData.getCaseInvites().isEmpty());
        Assert.assertEquals("NEWCODE123", caseData.getCaseInvites().get(0).getValue().getAccessCode());
    }

    @Test
    public void testFetchOrCreateAccessCode_FL401_GeneratesAndPersistsInvite() {
        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("TestFL401")
            .partyId(UUID.randomUUID())
            .build();
        Element<PartyDetails> partyElement = element(partyDetails);
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .caseInvites(new ArrayList<>())
            .build();

        CaseInvite generatedInvite = CaseInvite.builder()
            .partyId(partyElement.getId())
            .accessCode("FL401CODE")
            .build();

        // Mock the correct service
        when(fl401CaseInviteService.generateCaseInvite(any(), any()))
            .thenReturn(generatedInvite);

        String accessCode = noticeOfChangeEventHandler.fetchOrCreateAccessCode(
            caseData,
            partyElement,
            CAAPPLICANT
        );

        Assert.assertEquals("FL401CODE", accessCode);
        Assert.assertFalse(caseData.getCaseInvites().isEmpty());
        Assert.assertEquals("FL401CODE", caseData.getCaseInvites().get(0).getValue().getAccessCode());
    }

    @Test
    public void testFetchOrCreateAccessCode_UsesExistingInvite_DoesNotRegenerate() {
        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("Existing")
            .partyId(UUID.randomUUID())
            .build();
        Element<PartyDetails> partyElement = element(partyDetails);

        CaseInvite existingInvite = CaseInvite.builder()
            .partyId(partyElement.getId())
            .accessCode("EXISTINGCODE")
            .build();
        Element<CaseInvite> inviteElement = element(existingInvite);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .caseInvites(new ArrayList<>(List.of(inviteElement)))
            .build();

        // Act
        String accessCode = noticeOfChangeEventHandler.fetchOrCreateAccessCode(
            caseData,
            partyElement,
            CAAPPLICANT
        );

        // Assert
        Assert.assertEquals("EXISTINGCODE", accessCode);
        // Ensure generateCaseInvite is NOT called
        verify(c100CaseInviteService, times(0)).generateCaseInvite(any(), any());
    }
}
