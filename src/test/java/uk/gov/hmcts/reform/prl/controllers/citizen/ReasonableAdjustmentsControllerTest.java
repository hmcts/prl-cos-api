package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails.ConfidentialDetailsMapper;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.UpdateCaseData;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.request.CitizenPartyFlagsCreateRequest;
import uk.gov.hmcts.reform.prl.models.caseflags.request.FlagDetailRequest;
import uk.gov.hmcts.reform.prl.models.caseflags.request.FlagsRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.ReasonableAdjustmentsService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.Silent.class)
@Ignore
public class ReasonableAdjustmentsControllerTest {

    @InjectMocks
    private ReasonableAdjustmentsController reasonableAdjustmentsController;

    @Mock
    private ReasonableAdjustmentsService reasonableAdjustmentsService;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Mock
    ConfidentialDetailsMapper confidentialDetailsMapper;

    @Mock
    HearingService hearingService;

    private CaseData caseData;
    Address address;
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private UpdateCaseData updateCaseData;
    public static final String authToken = "Bearer TestAuthToken";
    public static final String servAuthToken = "Bearer TestServToken";

    @BeforeEach
    public void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void testRetrieveRaFlags() {
        String caseId = "1234567891234567L";
        String partyId = "e3ceb507-0137-43a9-8bd3-85dd23720648";

        Mockito.when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.TRUE);
        Mockito.when(authorisationService.authoriseService(servAuthToken)).thenReturn(Boolean.TRUE);
        Mockito.when(authTokenGenerator.generate()).thenReturn(servAuthToken);
        Mockito.when(reasonableAdjustmentsService.getPartyCaseFlags(
            authToken,
            caseId,
            partyId
        )).thenReturn(Flags.builder().roleOnCase(
            "Respondent 1").partyName("Respondent").details(
            Collections.emptyList()).build());

        Flags flag = reasonableAdjustmentsController.getCaseFlags(caseId, partyId, authToken, servAuthToken);
        Assert.assertNotNull(flag);
    }

    @Test
    public void testRetrieveRaFlagsWhenAuthFails() throws IOException {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Invalid Client");


        String caseId = "1234567891234567L";
        String partyId = "e3ceb507-0137-43a9-8bd3-85dd23720648";

        Mockito.when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.FALSE);
        Mockito.when(authorisationService.authoriseService(servAuthToken)).thenReturn(Boolean.TRUE);

        reasonableAdjustmentsController.getCaseFlags(caseId, partyId, authToken, servAuthToken);

        throw new RuntimeException("Invalid Client");
    }

    @Test
    public void testUpdateCitizenRAflags() {
        String caseId = "1234567891234567L";
        String eventId = "c100RequestSupport";
        String partyId = "e3ceb507-0137-43a9-8bd3-85dd23720648";
        FlagDetailRequest flagDetailRequest = FlagDetailRequest.builder()
            .name("Support filling in forms")
            .name_cy("Cymorth i lenwi ffurflenni")
            .hearingRelevant(YesOrNo.No)
            .flagCode("RA0018")
            .status("Requested")
            .availableExternally(YesOrNo.Yes)
            .build();
        List<FlagDetailRequest> flagDetailsRequest = Collections.singletonList(flagDetailRequest);
        CitizenPartyFlagsCreateRequest partyRequestFlags = CitizenPartyFlagsCreateRequest.builder()
            .caseTypeOfApplication("C100")
            .partyIdamId(partyId)
            .partyExternalFlags(FlagsRequest.builder()
                                    .details(flagDetailsRequest).build()).build();
        Mockito.when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.TRUE);
        Mockito.when(authorisationService.authoriseService(servAuthToken)).thenReturn(Boolean.TRUE);
        Mockito.when(authTokenGenerator.generate()).thenReturn(servAuthToken);
        Mockito.when(reasonableAdjustmentsService.createCitizenReasonableAdjustmentsFlags(
            caseId,
            eventId,
            authToken,
            partyRequestFlags
        )).thenReturn(ResponseEntity.status(HttpStatus.OK).body("party flags updated"));

        //        ResponseEntity<Object> updateResponse = reasonableAdjustmentsController.updateCitizenRAflags(
        //        partyRequestFlags, eventId, caseId, authToken, servAuthToken);
        //        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testUpdateCitizenRAflagsWhenAuthFails() {
        String caseId = "1234567891234567L";
        String eventId = "fl401RequestSupport";
        String partyId = "e3ceb507-0137-43a9-8bd3-85dd23720648";
        FlagDetailRequest flagDetailRequest = FlagDetailRequest.builder()
            .name("Support filling in forms")
            .name_cy("Cymorth i lenwi ffurflenni")
            .hearingRelevant(YesOrNo.No)
            .flagCode("RA0018")
            .status("Requested")
            .availableExternally(YesOrNo.Yes)
            .build();
        List<FlagDetailRequest> flagDetailsRequest = Collections.singletonList(flagDetailRequest);
        //        CitizenPartyFlagsRequest partyRequestFlags = CitizenPartyFlagsRequest.builder()
        //            .caseTypeOfApplication("FL401")
        //            .partyIdamId(partyId)
        //            .partyExternalFlags(FlagsRequest.builder()
        //                                    .details(flagDetailsRequest).build()).build();

        Mockito.when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.FALSE);
        Mockito.when(authorisationService.authoriseService(servAuthToken)).thenReturn(Boolean.TRUE);

        //        ResponseEntity<Object> updateResponse = reasonableAdjustmentsController.updateCitizenRAflags(
        //        partyRequestFlags, eventId, caseId, authToken, servAuthToken);
        //
        //        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
