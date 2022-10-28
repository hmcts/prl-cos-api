package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.citizen.hearing.HearingResponseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseHearingControllerTest {

    @InjectMocks
    private CaseHearingController caseHearingController;

    @Mock
    private CaseService caseService;

    @Mock
    private AuthorisationService authorisationService;

    private HearingResponseData hearingResponseData;
    public static final String authToken = "Bearer TestAuthToken";
    public static final String servAuthToken = "Bearer TestServToken";

    @Before
    public void setUp() {

    }

    @Test
    public void testGetCase() {
        HearingResponseData hearingResponseData = HearingResponseData.builder()
            .date("Monday 12 July 2021")
            .time("10:30am")
            .durationOfHearing("2 hours")
            .typeOfHearing("Remote (by telephone or video)")
            .hearingLink("The court will email you instructions to join the hearing")
            .courtName("Bristol family court")
            .support("Support you need during your case")
            .hearingNotice("Check all details of the hearing in the hearing notice Hearing-notice-pdf")
            .build();

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        Map<String, Object> stringObjectMap = hearingResponseData.toMap(new ObjectMapper());
        String caseId = "1234567891234567";
        HearingResponseData caseData1 = caseHearingController.getCaseHearings(caseId, authToken, servAuthToken);
        assertEquals(hearingResponseData.getDate(), caseData1.getDate());
        assertEquals(hearingResponseData.getTime(), caseData1.getTime());
        assertEquals(hearingResponseData.getTypeOfHearing(), caseData1.getTypeOfHearing());

    }

}
