package uk.gov.hmcts.reform.prl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.events.CaseFlagsEvent;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.AllPartyFlags;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.caseflags.CaseFlagsWaService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseFlagsEventHandlerTest {

    public static final String TEST_AUTH = "test-auth";

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private CaseFlagsWaService caseFlagsWaService;

    @InjectMocks
    private CaseFlagsEventHandler caseFlagsEventHandler;

    @Test
    public void testTriggerDummyEventForCaseFlags() {

        getCaseFlagsAndAllPartyFlagsWithStatus("Requested");

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(123L).build())
            .caseDetailsBefore(CaseDetails.builder()
                                   .id(123L).build())
            .build();
        CaseFlagsEvent caseFlagsEvent = new CaseFlagsEvent(callbackRequest, TEST_AUTH);
        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(CaseData.builder().build());

        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetailsBefore(),
            objectMapper
        )).thenReturn(CaseData.builder().build());

        caseFlagsEventHandler.triggerDummyEventForCaseFlags(caseFlagsEvent);
        Mockito.verify(
            caseFlagsWaService,
            Mockito.times(1)
        ).checkCaseFlagsToCreateTask(Mockito.any(), Mockito.any());

    }

    private void getCaseFlagsAndAllPartyFlagsWithStatus(String status) {
        List<Element<FlagDetail>> flagDetails = List.of(
            new Element<>(UUID.randomUUID(), FlagDetail.builder()
                .status(status)
                .dateTimeCreated(LocalDateTime.now())
                .build())
        );

        Flags caseFlags = Flags.builder()
            .details(flagDetails)
            .build();

        AllPartyFlags partyFlags = AllPartyFlags.builder()
            .caApplicant1InternalFlags(caseFlags)
            .build();

        CaseData mappedCaseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication("C100")
            .state(State.CASE_ISSUED)
            .caseFlags(caseFlags)
            .allPartyFlags(partyFlags)
            .build();

        Mockito.when(objectMapper.convertValue(Mockito.any(), Mockito.eq(CaseData.class)))
            .thenReturn(mappedCaseData);
    }

}
