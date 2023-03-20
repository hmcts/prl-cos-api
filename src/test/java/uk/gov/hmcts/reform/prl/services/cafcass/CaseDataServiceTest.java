package uk.gov.hmcts.reform.prl.services.cafcass;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.filter.cafcaas.CafCassFilter;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.TestResourceUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseDataServiceTest {
    private final String s2sToken = "s2s token";

    private final String userToken = "Bearer testToken";

    @Mock
    HearingService hearingService;

    @Mock
    CafcassCcdDataStoreService cafcassCcdDataStoreService;

    @Mock
    private CafCassFilter cafCassFilter;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private CaseDataService caseDataService;

    @Mock
    SystemUserService systemUserService;

    @Mock
    private RefDataService refDataService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
    }

    @org.junit.Test
    public void getCaseData() throws IOException {

        final List<CaseHearing> caseHearings = new ArrayList();

        final CaseHearing caseHearing = CaseHearing.caseHearingWith().hearingID(Long.valueOf("1234"))
            .hmcStatus("LISTED").hearingType("ABA5-FFH").hearingID(Long.valueOf("2000004659")).hearingDaySchedule(
                List.of(
                    HearingDaySchedule.hearingDayScheduleWith()
                        .hearingVenueName("ROYAL COURTS OF JUSTICE - QUEENS BUILDING (AND WEST GREEN BUILDING)")
                        .hearingStartDateTime(LocalDateTime.parse("2023-05-09T09:00:00")).hearingEndDateTime(LocalDateTime.parse(
                            "2023-05-09T09:45:00")).build())).build();

        caseHearings.add(caseHearing);

        Hearings hearings = new Hearings();
        hearings.setCaseRef("1673970714366224");
        hearings.setCaseHearings(caseHearings);

        List<Hearings> listOfHearings = new ArrayList<>();
        listOfHearings.add(hearings);

        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        String expectedCafCassResponse = TestResourceUtil.readFileFrom("classpath:response/CafCaasResponse.json");
        SearchResult searchResult = objectMapper.readValue(expectedCafCassResponse,
                                                                    SearchResult.class);
        CafCassResponse cafCassResponse = objectMapper.readValue(expectedCafCassResponse, CafCassResponse.class);

        when(cafcassCcdDataStoreService.searchCases(anyString(),anyString(),any(),any())).thenReturn(searchResult);
        Mockito.doNothing().when(cafCassFilter).filter(cafCassResponse);
        when(hearingService.getHearings(anyString(),anyString())).thenReturn(hearings);
        when(hearingService.getHearingsForAllCases(anyString(),anyList())).thenReturn(listOfHearings);
        when(systemUserService.getSysUserToken()).thenReturn(userToken);
        List<String> caseStateList = new LinkedList<>();
        caseStateList.add("DECISION_OUTCOME");
        ReflectionTestUtils.setField(caseDataService, "caseStateList", caseStateList);

        Map<String, String> refDataMap = new HashMap<>();
        refDataMap.put("ABA5-APL","Appeal");
        when(refDataService.getRefDataCategoryValueMap(anyString(),anyString(),anyString())).thenReturn(refDataMap);

        CafCassResponse realCafCassResponse = caseDataService.getCaseData("authorisation",
                                                                          "start", "end"
        );
        assertEquals(objectMapper.writeValueAsString(cafCassResponse), objectMapper.writeValueAsString(realCafCassResponse));

    }

    @org.junit.Test
    public void testGetCaseDataWithZeroRecords() throws IOException {

        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
        String expectedCafCassResponse = TestResourceUtil.readFileFrom("classpath:response/CafcassResponseNoData.json");
        SearchResult searchResult = objectMapper.readValue(expectedCafCassResponse,
                                                                    SearchResult.class);
        final CafCassResponse cafCassResponse = objectMapper.readValue(expectedCafCassResponse, CafCassResponse.class);

        when(cafcassCcdDataStoreService.searchCases(anyString(),anyString(),any(),any())).thenReturn(searchResult);
        when(systemUserService.getSysUserToken()).thenReturn(userToken);
        List<String> caseStateList = new LinkedList<>();
        caseStateList.add("DECISION_OUTCOME");
        ReflectionTestUtils.setField(caseDataService, "caseStateList", caseStateList);

        CafCassResponse realCafCassResponse = caseDataService.getCaseData("authorisation",
                                                                          "start", "end"
        );
        assertEquals(cafCassResponse, realCafCassResponse);

        assertEquals(realCafCassResponse.getTotal(), 0);

    }
}

