package uk.gov.hmcts.reform.prl.services;

import groovy.util.logging.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGTYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_HEARINGCHILDREQUIRED_N;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class HearingDataServiceTest {

    @InjectMocks
    HearingDataService hearingDataService;

    @Mock
    RefDataUserService refDataUserService;

    @Mock
    HearingService hearingService;

    public static final String authToken = "Bearer TestAuthToken";

    @Test()
    public void testPrePopulateHearingType() {
        List<CategoryValues> categoryValues = new ArrayList<>();
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Review").build());
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Allocation").build());
        CommonDataResponse commonDataResponse = CommonDataResponse.builder().categoryValues(categoryValues).build();
        when(refDataUserService.retrieveCategoryValues(authToken,HEARINGTYPE,IS_HEARINGCHILDREQUIRED_N)).thenReturn(commonDataResponse);
        List<DynamicListElement> listHearingTypes = new ArrayList<>();
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-REV").label("Review").build());
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-ALL").label("Allocation").build());
        when(refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse,HEARINGTYPE)).thenReturn(listHearingTypes);
        List<DynamicListElement> expectedResponse = hearingDataService.prePopulateHearingType(authToken);
        assertEquals(expectedResponse.get(0).getCode(),"ABA5-REV");
        assertEquals(expectedResponse.get(0).getLabel(),"Review");
    }

    @Test()
    public void testPrePopulateHearingDates() {

        List<HearingDaySchedule> hearingDaySchedules = new ArrayList<>();
        hearingDaySchedules.add(HearingDaySchedule.hearingDayScheduleWith().hearingJudgeId("123").hearingJudgeName("hearingJudgeName")
                                    .hearingVenueId("venueId").hearingVenueAddress("venueAddress")
                                    .hearingStartDateTime(LocalDateTime.now()).build());
        List<CaseHearing> caseHearings = new ArrayList<>();
        caseHearings.add(CaseHearing.caseHearingWith().hmcStatus(LISTED).hearingDaySchedule(hearingDaySchedules).build());
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .build();
        when((hearingService.getHearings(authToken,"1234")))
            .thenReturn(Hearings.hearingsWith().caseHearings(caseHearings).build());
        List<DynamicListElement> expectedResponse = hearingDataService.getHearingStartDate(authToken,caseData);
        assertNotNull(expectedResponse);
    }


}





