package uk.gov.hmcts.reform.prl.services.cafcass;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.Test;
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
import uk.gov.hmcts.reform.prl.models.ContactInformation;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.TestResourceUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
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

    @Mock
    private OrganisationService organisationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
    }

    @Test
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
        objectMapper.registerModule(new ParameterNamesModule());
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
        when(hearingService.getHearingsForAllCases(anyString(),anyMap())).thenReturn(listOfHearings);
        when(systemUserService.getSysUserToken()).thenReturn(userToken);
        when(organisationService.getOrganisationDetails(anyString(),anyString()))
            .thenReturn(Organisations.builder()
                            .name("test 1")
                            .organisationIdentifier("EJK3DHI")
                            .contactInformation(List.of(ContactInformation.builder()
                                                            .addressLine1("Physio In The City")
                                                            .addressLine2("1 Kingdom Street")
                                                            .postCode("W2 6BD")
                                                            .build()))
                            .build());
        List<String> caseStateList = new LinkedList<>();
        caseStateList.add("DECISION_OUTCOME");
        ReflectionTestUtils.setField(caseDataService, "caseStateList", caseStateList);

        List<String> caseTypeList = new ArrayList<>();
        caseTypeList.add("C100");
        ReflectionTestUtils.setField(caseDataService, "caseTypeList", caseTypeList);


        Map<String, String> refDataMap = new HashMap<>();
        refDataMap.put("ABA5-APL","Appeal");
        when(refDataService.getRefDataCategoryValueMap(anyString(),anyString(),anyString(),anyString())).thenReturn(refDataMap);

        CafCassResponse realCafCassResponse = caseDataService.getCaseData("authorisation",
                                                                          "start", "end"
        );
        assertEquals(objectMapper.writeValueAsString(cafCassResponse), objectMapper.writeValueAsString(realCafCassResponse));

    }

    @Test
    public void testGetCaseDataWithRegion() throws IOException {

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
        objectMapper.registerModule(new ParameterNamesModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        String expectedCafCassResponse = TestResourceUtil.readFileFrom("classpath:response/CafCaasResponseWithRegion.json");
        SearchResult searchResult = objectMapper.readValue(expectedCafCassResponse,
                                                           SearchResult.class);
        CafCassResponse cafCassResponse = objectMapper.readValue(expectedCafCassResponse, CafCassResponse.class);

        when(cafcassCcdDataStoreService.searchCases(anyString(),anyString(),any(),any())).thenReturn(searchResult);
        Mockito.doNothing().when(cafCassFilter).filter(cafCassResponse);
        when(hearingService.getHearings(anyString(),anyString())).thenReturn(hearings);
        when(hearingService.getHearingsForAllCases(anyString(),anyMap())).thenReturn(listOfHearings);
        when(systemUserService.getSysUserToken()).thenReturn(userToken);
        when(organisationService.getOrganisationDetails(anyString(),anyString()))
            .thenReturn(Organisations.builder()
                            .name("test 1")
                            .organisationIdentifier("EJK3DHI")
                            .contactInformation(List.of(ContactInformation.builder()
                                                            .addressLine1("Physio In The City")
                                                            .addressLine2("1 Kingdom Street")
                                                            .postCode("W2 6BD")
                                                            .build()))
                            .build());
        List<String> caseStateList = new LinkedList<>();
        caseStateList.add("DECISION_OUTCOME");
        ReflectionTestUtils.setField(caseDataService, "caseStateList", caseStateList);

        List<String> caseTypeList = new ArrayList<>();
        caseTypeList.add("C100");
        ReflectionTestUtils.setField(caseDataService, "caseTypeList", caseTypeList);


        Map<String, String> refDataMap = new HashMap<>();
        refDataMap.put("ABA5-APL","Appeal");
        when(refDataService.getRefDataCategoryValueMap(anyString(),anyString(),anyString(),anyString())).thenReturn(refDataMap);

        CafCassResponse realCafCassResponse = caseDataService.getCaseData("authorisation",
                                                                          "start", "end"
        );
        assertNotNull(objectMapper.writeValueAsString(realCafCassResponse));

    }

    @org.junit.Test
    public void testGetCaseDataWithZeroRecords() throws IOException {

        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
        objectMapper.registerModule(new ParameterNamesModule());
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

        assertEquals(0, realCafCassResponse.getTotal());

    }

    @Test
    public void testGetCaseDataThrowingException() throws Exception {

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
        objectMapper.registerModule(new ParameterNamesModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        String expectedCafCassResponse = TestResourceUtil.readFileFrom("classpath:response/CafCaasResponseWithRegion.json");
        SearchResult searchResult = objectMapper.readValue(expectedCafCassResponse,
                                                           SearchResult.class);
        CafCassResponse cafCassResponse = objectMapper.readValue(expectedCafCassResponse, CafCassResponse.class);
        Exception exception = new RuntimeException();
        when(cafcassCcdDataStoreService.searchCases(anyString(),anyString(),any(),any())).thenThrow(exception);
        when(systemUserService.getSysUserToken()).thenReturn(userToken);
        List<String> caseTypeList = new ArrayList<>();
        caseTypeList.add("C100");
        ReflectionTestUtils.setField(caseDataService, "caseTypeList", caseTypeList);
        List<String> caseStateList = new LinkedList<>();
        caseStateList.add("DECISION_OUTCOME");
        ReflectionTestUtils.setField(caseDataService, "caseStateList", caseStateList);

        assertThrows(RuntimeException.class, () -> caseDataService.getCaseData("authorisation",
                                                                               "start", "end"
        ));

    }
}

