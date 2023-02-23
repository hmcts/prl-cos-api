package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;

@RunWith(MockitoJUnitRunner.Silent.class)
public class HearingUtilsTest {

    @InjectMocks
    HearingUtils hearingUtils;

    @Mock
    HearingService hearingService;

    @Mock
    ObjectMapper objectMapper;

    public static final String authToken = "Bearer TestAuthToken";

    @Test
    public void testGetHearingStartDateWithValidData() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTyoeOfApplication","FL401");
        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);

        LocalDateTime date =  LocalDateTime.now();

        List<HearingDaySchedule> hearingDaySchedules = new ArrayList<>();
        hearingDaySchedules.add(HearingDaySchedule.hearingDayScheduleWith().hearingStartDateTime(date)
                                    .hearingJudgeId("123").hearingJudgeName("hearingJudgeName")
                                    .hearingVenueId("venueId").hearingVenueAddress("venueAddress")
                                    .hearingStartDateTime(LocalDateTime.now()).build());
        List<CaseHearing> caseHearings = new ArrayList<>();
        caseHearings.add(CaseHearing.caseHearingWith().hmcStatus(LISTED).hearingDaySchedule(hearingDaySchedules).build());
        when(hearingService.getHearings(authToken,String.valueOf(12345L))).thenReturn(Hearings.hearingsWith().caseHearings(caseHearings).build());
        List<DynamicListElement> hearingStartDate = hearingUtils.getHearingStartDate(authToken, caseData);
        assertNotNull(hearingStartDate);
        assertNotNull(hearingStartDate.get(0));

    }

}
