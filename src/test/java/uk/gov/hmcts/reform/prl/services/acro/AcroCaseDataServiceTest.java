package uk.gov.hmcts.reform.prl.services.acro;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroCaseData;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroResponse;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.cafcass.HearingService;
import uk.gov.hmcts.reform.prl.utils.TestResourceUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.Silent.class)
public class AcroCaseDataServiceTest {
    private final String s2sToken = "s2s token";

    private final String userToken = "Bearer testToken";

    @Mock
    HearingService hearingService;

    @Mock
    AcroCaseSearchService acroCaseSearchService;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private AcroCaseDataService acroCaseDataService;

    @Mock
    SystemUserService systemUserService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
    }


    @Test
    public void getCaseData() throws IOException {

        final List<CaseHearing> caseHearings = new ArrayList();

        final CaseHearing caseHearing = CaseHearing.caseHearingWith().hearingID(Long.valueOf("1234"))
            .hmcStatus("LISTED")
            .hearingType("ABA5-FFH")
            .hearingID(Long.valueOf("2000004659"))
            .hearingDaySchedule(
                List.of(
                    HearingDaySchedule.hearingDayScheduleWith()
                        .hearingVenueName("ROYAL COURTS OF JUSTICE - QUEENS BUILDING (AND WEST GREEN BUILDING)")
                        .hearingStartDateTime(LocalDateTime.parse("2023-05-09T09:00:00")).hearingEndDateTime(
                            LocalDateTime.parse(
                                "2023-05-09T09:45:00")).build())).build();

        caseHearings.add(caseHearing);

        Hearings hearings = Hearings.hearingsWith().caseRef("1673970714366224")
            .courtName("Swansea Civil And Family Justice Centre")
            .courtTypeId("12")
            .caseHearings(caseHearings)
            .build();
        List<Hearings> listOfHearings = new ArrayList<>();
        listOfHearings.add(hearings);

        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
        objectMapper.registerModule(new ParameterNamesModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        String expectedAcroResponse = TestResourceUtil.readFileFrom("classpath:response/AcroResponse.json");
        SearchResult searchResult = objectMapper.readValue(
            expectedAcroResponse,
            SearchResult.class
        );
        when(acroCaseSearchService.searchCases(anyString(), anyString(), any(), any())).thenReturn(searchResult);
        when(hearingService.getHearingsForAllCases(anyString(), anyMap())).thenReturn(listOfHearings);
        when(systemUserService.getSysUserToken()).thenReturn(userToken);

        AcroResponse realAcroResponse = acroCaseDataService.getCaseData("authorisation");
        AcroCaseData caseData = realAcroResponse.getCases().getFirst().getCaseData();
        assertEquals(1, caseData.getFl404Orders().size());
        assertEquals(1, caseData.getCaseHearings().size());
        assertNotNull(caseData.getApplicantsFL401());
        assertNotNull(caseData.getRespondentsFL401());
        assertEquals(1, caseData.getCaseHearings().size());
        assertEquals("12", caseData.getCourtEpimsId());
        assertEquals("Swansea Civil And Family Justice Centre", caseData.getCourtName());
    }

    @Test
    public void testGetCaseDataWithRegion() throws IOException {

        final List<CaseHearing> caseHearings = new ArrayList();

        final CaseHearing caseHearing = CaseHearing.caseHearingWith().hearingID(Long.valueOf("1234"))
            .hmcStatus("LISTED").hearingType("ABA5-FFH").hearingID(Long.valueOf("2000004659")).hearingDaySchedule(
                List.of(
                    HearingDaySchedule.hearingDayScheduleWith()
                        .hearingVenueName("ROYAL COURTS OF JUSTICE - QUEENS BUILDING (AND WEST GREEN BUILDING)")
                        .hearingStartDateTime(LocalDateTime.parse("2023-05-09T09:00:00")).hearingEndDateTime(
                            LocalDateTime.parse(
                                "2023-05-09T09:45:00")).build())).build();

        caseHearings.add(caseHearing);

        Hearings hearings = Hearings.hearingsWith().caseRef("1673970714366224")
            .courtName("Swansea Civil And Family Justice Centre")
            .courtTypeId("12")
            .caseHearings(caseHearings)
            .build();
        hearings.setCaseHearings(caseHearings);

        List<Hearings> listOfHearings = new ArrayList<>();
        listOfHearings.add(hearings);

        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
        objectMapper.registerModule(new ParameterNamesModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        String expectedAcroResponse = TestResourceUtil.readFileFrom("classpath:response/AcroResponseWithRegion.json");
        SearchResult searchResult = objectMapper.readValue(
            expectedAcroResponse,
            SearchResult.class
        );

        when(acroCaseSearchService.searchCases(anyString(), anyString(), any(), any())).thenReturn(searchResult);
        when(hearingService.getHearings(anyString(), anyString())).thenReturn(hearings);
        when(hearingService.getHearingsForAllCases(anyString(), anyMap())).thenReturn(listOfHearings);
        when(systemUserService.getSysUserToken()).thenReturn(userToken);
        AcroResponse realAcroResponse = acroCaseDataService.getCaseData("authorisation");
        AcroCaseData caseData = realAcroResponse.getCases().getFirst().getCaseData();
        assertEquals(1, caseData.getFl404Orders().size());
        assertEquals(1, caseData.getCaseHearings().size());
        assertNotNull(caseData.getApplicantsFL401());
        assertNotNull(caseData.getRespondentsFL401());
        assertEquals(1, caseData.getCaseHearings().size());
        assertEquals("234946", caseData.getCourtEpimsId());
        assertEquals("Swansea Civil And Family Justice Centre", caseData.getCourtName());
    }

    @Test
    public void testGetCaseDataThrowingException() throws Exception {

        final List<CaseHearing> caseHearings = new ArrayList();

        final CaseHearing caseHearing = CaseHearing.caseHearingWith().hearingID(Long.valueOf("1234"))
            .hmcStatus("LISTED").hearingType("ABA5-FFH").hearingID(Long.valueOf("2000004659")).hearingDaySchedule(
                List.of(
                    HearingDaySchedule.hearingDayScheduleWith()
                        .hearingVenueName("ROYAL COURTS OF JUSTICE - QUEENS BUILDING (AND WEST GREEN BUILDING)")
                        .hearingStartDateTime(LocalDateTime.parse("2023-05-09T09:00:00")).hearingEndDateTime(
                            LocalDateTime.parse(
                                "2023-05-09T09:45:00")).build())).build();

        caseHearings.add(caseHearing);

        Hearings hearings = new Hearings();
        hearings.setCaseRef("1673970714366224");
        hearings.setCaseHearings(caseHearings);

        List<Hearings> listOfHearings = new ArrayList<>();
        listOfHearings.add(hearings);

        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
        objectMapper.registerModule(new ParameterNamesModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        String expectedCafCassResponse = TestResourceUtil.readFileFrom("classpath:response/AcroResponseWithRegion.json");
        SearchResult searchResult = objectMapper.readValue(expectedCafCassResponse, SearchResult.class);
        CafCassResponse cafCassResponse = objectMapper.readValue(expectedCafCassResponse, CafCassResponse.class);
        Exception exception = new RuntimeException();
        when(acroCaseSearchService.searchCases(anyString(), anyString(), any(), any())).thenThrow(exception);
        when(systemUserService.getSysUserToken()).thenReturn(userToken);

        assertThrows(RuntimeException.class, () -> acroCaseDataService.getCaseData("authorisation"));
    }

    @Test
    public void testFilterCancelledHearingsBeforeListing() {

        final List<CaseHearing> caseHearings = new ArrayList();


        final CaseHearing caseHearing = CaseHearing.caseHearingWith().hearingID(Long.valueOf("1234"))
            .hmcStatus("CANCELLED").hearingType("ABA5-FFH").hearingID(Long.valueOf("2000004660")).hearingDaySchedule(
                List.of(
                    HearingDaySchedule.hearingDayScheduleWith()
                        .hearingVenueName("ROYAL COURTS OF JUSTICE - QUEENS BUILDING (AND WEST GREEN BUILDING)")
                        .hearingStartDateTime(LocalDateTime.parse("2023-05-09T09:00:00")).hearingEndDateTime(
                            LocalDateTime.parse(
                                "2023-05-09T09:45:00")).build())).build();

        final CaseHearing caseHearing1 = CaseHearing.caseHearingWith().hearingID(Long.valueOf("1234"))
            .hmcStatus("LISTED").hearingType("ABA5-FFH").hearingID(Long.valueOf("2000004659")).hearingDaySchedule(
                List.of(
                    HearingDaySchedule.hearingDayScheduleWith()
                        .hearingVenueName("ROYAL COURTS OF JUSTICE - QUEENS BUILDING (AND WEST GREEN BUILDING)")
                        .hearingStartDateTime(LocalDateTime.parse("2023-05-09T09:00:00")).hearingEndDateTime(
                            LocalDateTime.parse(
                                "2023-05-09T09:45:00")).build())).build();

        final CaseHearing caseHearing2 = CaseHearing.caseHearingWith().hearingID(Long.valueOf("1234"))
            .hmcStatus("CANCELLED").hearingType("ABA5-FFH").hearingID(Long.valueOf("2000004661")).hearingDaySchedule(
                List.of(
                    HearingDaySchedule.hearingDayScheduleWith()
                        .hearingVenueName("ROYAL COURTS OF JUSTICE - QUEENS BUILDING (AND WEST GREEN BUILDING)")
                        .build())).build();

        caseHearings.add(caseHearing);
        caseHearings.add(caseHearing1);
        caseHearings.add(caseHearing2);

        Hearings hearings = new Hearings();
        hearings.setCaseRef("1673970714366224");
        hearings.setCaseHearings(caseHearings);

        List<Hearings> listOfHearings = new ArrayList<>();
        listOfHearings.add(hearings);

        acroCaseDataService.filterCancelledHearingsBeforeListing(listOfHearings);

        assertEquals(2, listOfHearings.get(0).getCaseHearings().size());

    }


}
