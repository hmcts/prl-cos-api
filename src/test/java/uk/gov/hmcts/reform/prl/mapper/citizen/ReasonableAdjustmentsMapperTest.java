package uk.gov.hmcts.reform.prl.mapper.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.caseflags.AllPartyFlags;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.TestUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ReasonableAdjustmentsMapperTest {

    public static final String authToken = "Bearer TestAuthToken";
    public static final String caseId = "1234567891234567";

    private static final String CASE_TYPE = "C100";

    @Mock
    ObjectMapper objectMapper;
    @Mock
    IdamClient idamClient;
    @Mock
    CcdCoreCaseDataService coreCaseDataService;
    @InjectMocks
    private ReasonableAdjustmentsMapper reasonableAdjustmentsMapper;
    private UserDetails userDetails;

    private CaseData caseData;
    Map<String, Object> caseDataMap;
    private CaseDetails caseDetails;
    private CaseData updatedCaseData;
    Map<String, Object> updatedCaseDataMap;
    private CaseDetails updatedCaseDetails;
    @Mock
    private CaseService caseService;
    @Mock
    private StartEventResponse startEventResponse;

    @Before
    public void setUp() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        userDetails = UserDetails.builder().id("tesUserId").email("testEmail").build();
        C100RebuildData c100RebuildData = C100RebuildData.builder()
            .c100RebuildInternationalElements(TestUtil.readFileFrom("classpath:c100-rebuild/ie.json"))
            .c100RebuildHearingWithoutNotice(TestUtil.readFileFrom("classpath:c100-rebuild/hwn.json"))
            .c100RebuildTypeOfOrder(TestUtil.readFileFrom("classpath:c100-rebuild/too.json"))
            .c100RebuildOtherProceedings(TestUtil.readFileFrom("classpath:c100-rebuild/op.json"))
            .c100RebuildMaim(TestUtil.readFileFrom("classpath:c100-rebuild/miam.json"))
            .c100RebuildHearingUrgency(TestUtil.readFileFrom("classpath:c100-rebuild/hu.json"))
            .c100RebuildChildDetails(TestUtil.readFileFrom("classpath:c100-rebuild/cd.json"))
            .c100RebuildApplicantDetails(TestUtil.readFileFrom("classpath:c100-rebuild/appl.json"))
            .c100RebuildOtherChildrenDetails(TestUtil.readFileFrom("classpath:c100-rebuild/ocd.json"))
            .c100RebuildReasonableAdjustments(TestUtil.readFileFrom("classpath:c100-rebuild/ra.json"))
            .c100RebuildOtherPersonsDetails(TestUtil.readFileFrom("classpath:c100-rebuild/oprs.json"))
            .c100RebuildRespondentDetails(TestUtil.readFileFrom("classpath:c100-rebuild/resp.json"))
            .c100RebuildConsentOrderDetails(TestUtil.readFileFrom("classpath:c100-rebuild/co.json"))
            .build();
        caseData = CaseData.builder()
            .id(Long.parseLong(caseId))
            .caseTypeOfApplication(CASE_TYPE)
            .c100RebuildData(c100RebuildData)
            .allPartyFlags(AllPartyFlags.builder().caApplicant1ExternalFlags(Flags.builder().build()).caApplicant2ExternalFlags(
                Flags.builder().build()).build())
            .build();

        caseDataMap = caseData.toMap(mapper);
        caseDetails = CaseDetails.builder()
            .data(caseDataMap)
            .id(Long.valueOf(caseId))
            .state("SUBMITTED_PAID")
            .build();

        FlagDetail flagDetailRequestForFillingForms = FlagDetail.builder()
            .name("Support filling in forms")
            .name_cy("Cymorth i lenwi ffurflenni")
            .hearingRelevant(No)
            .flagCode("RA0018")
            .status("Requested")
            .dateTimeCreated(LocalDateTime.parse(
                "2023-11-16T16:05:25.000Z",
                DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    Locale.ENGLISH
                )
            ))
            .dateTimeModified(LocalDateTime.parse(
                "2023-11-16T16:05:53.000Z",
                DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    Locale.ENGLISH
                )
            ))
            .availableExternally(Yes)
            .build();
        Flags applicant1PartyFlags = Flags.builder().roleOnCase("Applicant 1").partyName("applicantFN1 applicantLN1").details(
            Collections.singletonList(element(flagDetailRequestForFillingForms))).build();

        updatedCaseData = CaseData.builder()
            .id(Long.parseLong(caseId))
            .caseTypeOfApplication(CASE_TYPE)
            .c100RebuildData(c100RebuildData)
            .allPartyFlags(AllPartyFlags.builder().caApplicant1ExternalFlags(applicant1PartyFlags).build())
            .build();

        updatedCaseDataMap = updatedCaseData.toMap(mapper);
        updatedCaseDetails = CaseDetails.builder()
            .data(updatedCaseDataMap)
            .id(Long.valueOf(caseId))
            .state("SUBMITTED_PAID")
            .build();
    }

    @Test
    public void testMapRAforC100MainApplicant() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);

        String systemUserId = "systemUserID";
        when(coreCaseDataService.eventRequest(CaseEvent.C100_REQUEST_SUPPORT, systemUserId)).thenReturn(
            EventRequestData.builder().build());
        String eventToken = "eventToken";
        startEventResponse = StartEventResponse.builder().eventId(String.valueOf(CaseEvent.C100_REQUEST_SUPPORT))
            .caseDetails(caseDetails)
            .token(eventToken).build();
        when(coreCaseDataService.startUpdate(
            Mockito.anyString(),
            Mockito.any(),
            Mockito.anyString(),
            Mockito.anyBoolean()
        )).thenReturn(
            startEventResponse);

        when(caseService.getPartyExternalCaseFlagField(
            CASE_TYPE,
            PartyEnum.applicant,
            0
        )).thenReturn(Optional.of("caApplicant1ExternalFlags"));

        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(Flags.class))).thenReturn(Flags.builder().build());

        CaseDataContent caseDataContent = CaseDataContent.builder().build();
        when(coreCaseDataService.createCaseDataContent(Mockito.any(), Mockito.any()))
            .thenReturn(caseDataContent);
        when(coreCaseDataService
                 .submitUpdate(
                     Mockito.anyString(),
                     Mockito.any(),
                     Mockito.any(),
                     Mockito.anyString(),
                     Mockito.anyBoolean()
                 ))
            .thenReturn(updatedCaseDetails);

        when(objectMapper.convertValue(updatedCaseDataMap, CaseData.class)).thenReturn(updatedCaseData);
        when(CaseUtils.getCaseData(
            updatedCaseDetails,
            objectMapper
        )).thenReturn(updatedCaseData);

        CaseData updatedCaseData = reasonableAdjustmentsMapper.mapRAforC100MainApplicant(TestUtil.readFileFrom(
            "classpath:c100-rebuild/appl.json"), caseData, "citizen-case-submit", authToken);

        //Then
        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.getAllPartyFlags().getCaApplicant1ExternalFlags().getDetails());
        assertThat(updatedCaseData.getAllPartyFlags().getCaApplicant1ExternalFlags().getDetails().get(0).getValue().getFlagCode()).isEqualTo(
            "RA0018");
    }
}
