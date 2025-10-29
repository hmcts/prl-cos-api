package uk.gov.hmcts.reform.prl.services.cafcass;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.filter.cafcaas.CafCassFilter;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.ContactInformation;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.ResponseToAllegationsOfHarm;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.Bundle;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingInformation;
import uk.gov.hmcts.reform.prl.models.dto.bundle.DocumentLink;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.ApplicantDetails;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassCaseData;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassCaseDetail;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.Element;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.OtherDocuments;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.TestResourceUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertNull;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;


@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseDataServiceTest {
    private final String s2sToken = "s2s token";

    private final String userToken = "Bearer testToken";

    private static final String REDACTED_DOCUMENT_URL = "http://test/documents/00000000-0000-0000-0000-000000000000";

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

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private ObjectMapper objMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
    }

    @Nested
    public class GetCaseData {

        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();

        @BeforeEach
        public void init() {
            objectMapper.registerModule(new ParameterNamesModule());
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

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

            Map<String, String> refDataMap = new HashMap<>();
            refDataMap.put("ABA5-APL","Appeal");
            when(refDataService.getRefDataCategoryValueMap(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(refDataMap);

            CategoriesAndDocuments categoriesAndDocuments = new CategoriesAndDocuments(
                1,
                new ArrayList<>(),
                new ArrayList<>()
            );
            when(coreCaseDataApi.getCategoriesAndDocuments(
                Mockito.any(),
                Mockito.any(),
                Mockito.any()
            )).thenReturn(categoriesAndDocuments);

            List<String> caseStateList = new LinkedList<>();
            caseStateList.add("DECISION_OUTCOME");
            ReflectionTestUtils.setField(caseDataService, "caseStateList", caseStateList);

            List<String> caseTypeList = new ArrayList<>();
            caseTypeList.add("C100");
            ReflectionTestUtils.setField(caseDataService, "caseTypeList", caseTypeList);

        }

        @Test
        public void shouldNotThrowExceptionForFailedBundles() throws IOException {
            String expectedCafCassResponse =
                TestResourceUtil.readFileFrom("classpath:response/CafcassResponseNullBundles.json");

            SearchResult searchResult = objectMapper.readValue(expectedCafCassResponse, SearchResult.class);
            CafCassResponse cafCassResponse = objectMapper.readValue(expectedCafCassResponse, CafCassResponse.class);

            when(cafcassCcdDataStoreService.searchCases(anyString(),anyString(),any(),any())).thenReturn(searchResult);
            Mockito.doNothing().when(cafCassFilter).filter(cafCassResponse);

            assertDoesNotThrow(() -> caseDataService.getCaseData("authorisation", "start", "end"));
        }

        @Test
        public void getCaseData() throws IOException {
            String expectedCafCassResponse = TestResourceUtil.readFileFrom("classpath:response/CafCaasResponse.json");
            SearchResult searchResult = objectMapper.readValue(
                expectedCafCassResponse,
                SearchResult.class
            );
            CafCassResponse cafCassResponse = objectMapper.readValue(expectedCafCassResponse, CafCassResponse.class);

            when(cafcassCcdDataStoreService.searchCases(
                anyString(),
                anyString(),
                any(),
                any()
            )).thenReturn(searchResult);
            Mockito.doNothing().when(cafCassFilter).filter(cafCassResponse);

            CafCassResponse realCafCassResponse = caseDataService.getCaseData("authorisation", "start", "end");
            assertEquals(
                objectMapper.writeValueAsString(cafCassResponse),
                objectMapper.writeValueAsString(realCafCassResponse)
            );
        }
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
        List<String> excludedDocumentCategoryList = new ArrayList<>();
        excludedDocumentCategoryList.add("draftOrders");
        ReflectionTestUtils.setField(caseDataService, "excludedDocumentCategoryList", excludedDocumentCategoryList);
        List<String> excludedDocumentList = new ArrayList<>();
        excludedDocumentList.add("Draft_C100_application");
        ReflectionTestUtils.setField(caseDataService, "excludedDocumentList", excludedDocumentList);
        ReflectionTestUtils.setField(caseDataService, "objMapper", objectMapper);
        uk.gov.hmcts.reform.ccd.client.model.Document documents =
            new uk.gov.hmcts.reform.ccd.client.model
                .Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());
        Category subCategory = new Category("applicantC1AResponse", "categoryName", 2, List.of(documents), null);
        Category category = new Category("applicantC1AResponse", "categoryName", 2, List.of(documents), List.of(subCategory));



        CategoriesAndDocuments categoriesAndDocuments = new CategoriesAndDocuments(1, List.of(category), List.of(documents));
        when(coreCaseDataApi.getCategoriesAndDocuments(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenReturn(categoriesAndDocuments);

        CafCassResponse realCafCassResponse = caseDataService.getCaseData("authorisation",
                                                                          "start", "end"
        );
        assertNotNull(objectMapper.writeValueAsString(realCafCassResponse));
        List<CafCassCaseDetail> realCafCassCaseDetail = realCafCassResponse.getCases().stream()
            .filter(c -> c.getId().equals(1673970714366224L)).toList();
        //it must filter the Redacted document
        assertEquals(1, realCafCassCaseDetail.get(0).getCaseData().getOtherDocuments().size());

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

    @Test
    public void testFilterCancelledHearingsBeforeListing() {

        final List<CaseHearing> caseHearings = new ArrayList();


        final CaseHearing caseHearing = CaseHearing.caseHearingWith().hearingID(Long.valueOf("1234"))
            .hmcStatus("CANCELLED").hearingType("ABA5-FFH").hearingID(Long.valueOf("2000004660")).hearingDaySchedule(
                List.of(
                    HearingDaySchedule.hearingDayScheduleWith()
                        .hearingVenueName("ROYAL COURTS OF JUSTICE - QUEENS BUILDING (AND WEST GREEN BUILDING)")
                        .hearingStartDateTime(LocalDateTime.parse("2023-05-09T09:00:00")).hearingEndDateTime(LocalDateTime.parse(
                            "2023-05-09T09:45:00")).build())).build();

        final CaseHearing caseHearing1 = CaseHearing.caseHearingWith().hearingID(Long.valueOf("1234"))
            .hmcStatus("LISTED").hearingType("ABA5-FFH").hearingID(Long.valueOf("2000004659")).hearingDaySchedule(
                List.of(
                    HearingDaySchedule.hearingDayScheduleWith()
                        .hearingVenueName("ROYAL COURTS OF JUSTICE - QUEENS BUILDING (AND WEST GREEN BUILDING)")
                        .hearingStartDateTime(LocalDateTime.parse("2023-05-09T09:00:00")).hearingEndDateTime(LocalDateTime.parse(
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

        caseDataService.filterCancelledHearingsBeforeListing(listOfHearings);

        assertEquals(2, listOfHearings.get(0).getCaseHearings().size());

    }

    @Test
    public void testCheckIfDocumentsNeedToExcludeScenario1() {
        List<String> excludedDocumentList = List.of(
            "Draft_C100_application",
            "C8Document",
            "C1A_Document",
            "C100FinalDocument"
        );
        String documentFilename = "Draft_C100_application.pdf";
        assertTrue(caseDataService.checkIfDocumentsNeedToExclude(excludedDocumentList, documentFilename));
    }

    @Test
    public void testCheckIfDocumentsNeedToExcludeScenario2() {
        List<String> excludedDocumentList = List.of(
            "Draft_C100_application",
            "C8Document",
            "C1A_Document",
            "C100FinalDocument"
        );
        String documentFilename = "abc.pdf";
        assertFalse(caseDataService.checkIfDocumentsNeedToExclude(excludedDocumentList, documentFilename));
    }

    @Test
    public void testaddSpecificDocumentsFromCaseFileViewBasedOnCategories() throws NoSuchMethodException,
        InvocationTargetException, IllegalAccessException {
        Document document = Document.builder().documentUrl("test").documentFileName("test").build();
        when(objMapper.convertValue(any(QuarantineLegalDoc.class), eq(Map.class))).thenReturn(new HashMap<>());
        when(objMapper.convertValue(anyMap(), eq(uk.gov.hmcts.reform.prl.models.documents.Document.class))).thenReturn(
            document);
        Bundle caseBundles = Bundle.builder()
            .value(BundleDetails.builder().stitchedDocument(DocumentLink.builder().documentUrl("http://test.link")
                                                                .documentFilename("test").build()).build())
            .build();
        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder().categoryId("section7Report")
            .section7ReportDocument(document).build();
        ResponseDocuments responseDocuments = ResponseDocuments.builder()
            .citizenDocument(document)
            .respondentC8Document(document)
            .respondentC8DocumentWelsh(document)
            .build();
        StmtOfServiceAddRecipient stmtOfServiceAddRecipient = StmtOfServiceAddRecipient.builder()
            .stmtOfServiceDocument(document)
            .build();
        ApplicantDetails applicantDetails = ApplicantDetails.builder()
            .response(Response.builder().responseToAllegationsOfHarm(ResponseToAllegationsOfHarm.builder()
                                                                         .responseToAllegationsOfHarmDocument(document)
                                                                         .build()).build())
            .build();
        CafCassCaseData cafCassCaseData = CafCassCaseData.builder()
            .respondents(List.of(Element.<ApplicantDetails>builder().id(UUID.randomUUID()).value(applicantDetails).build()))
            .c8FormDocumentsUploaded(List.of(document))
            .bundleInformation(BundlingInformation.builder().caseBundles(List.of(caseBundles)).build())
            .otherDocumentsUploaded(List.of(document))
            .uploadOrderDoc(document)
            .courtStaffUploadDocListDocTab(List.of(element(quarantineLegalDoc)))
            .legalProfUploadDocListDocTab(List.of(element(quarantineLegalDoc)))
            .cafcassUploadDocListDocTab(List.of(element(quarantineLegalDoc)))
            .courtStaffUploadDocListDocTab(List.of(element(quarantineLegalDoc)))
            .citizenUploadedDocListDocTab(List.of(element(quarantineLegalDoc)))
            .restrictedDocuments(List.of(element(quarantineLegalDoc)))
            .confidentialDocuments(List.of(element(quarantineLegalDoc)))
            .respondentAc8Documents(List.of(element(responseDocuments)))
            .respondentBc8Documents(List.of(element(responseDocuments)))
            .respondentCc8Documents(List.of(element(responseDocuments)))
            .respondentDc8Documents(List.of(element(responseDocuments)))
            .respondentEc8Documents(List.of(element(responseDocuments)))
            .specialArrangementsLetter(document)
            .additionalDocuments(document)
            .additionalDocumentsList(List.of(element(document)))
            .stmtOfServiceAddRecipient(List.of(element(stmtOfServiceAddRecipient)))
            .stmtOfServiceForOrder(List.of(element(stmtOfServiceAddRecipient)))
            .stmtOfServiceForApplication(List.of(element(stmtOfServiceAddRecipient)))
            .build();
        CafCassCaseDetail cafCassCaseDetail = CafCassCaseDetail.builder()
            .caseData(cafCassCaseData)
            .build();
        CafCassResponse cafCassResponse = CafCassResponse.builder().cases(List.of(cafCassCaseDetail)).build();
        Method privateMethod = CaseDataService.class.getDeclaredMethod(
            "addSpecificDocumentsFromCaseFileViewBasedOnCategories",
            CafCassResponse.class
        );
        privateMethod.setAccessible(true);
        privateMethod.invoke(caseDataService, cafCassResponse);

        assertEquals("test", cafCassResponse.getCases().get(0).getCaseData().getOtherDocuments().get(0).getValue().getDocumentName());
        assertNull(cafCassResponse.getCases().get(0).getCaseData().getCourtStaffUploadDocListDocTab());
        assertNull(cafCassResponse.getCases().get(0).getCaseData().getCafcassUploadDocListDocTab());
    }

    @Test
    public void testRedactedDocumentsOnAddSpecificDocumentsFromCaseFileViewBasedOnCategories() throws NoSuchMethodException,
        InvocationTargetException, IllegalAccessException {
        Document document = Document.builder().documentUrl(REDACTED_DOCUMENT_URL).documentFileName("*Redacted*").build();
        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder().categoryId("MIAMCertificate")
            .miamCertificateDocument(document).build();
        Map<String, Object> attributes = Map.of("miamCertificateDocument", Element.builder().value(quarantineLegalDoc));
        when(objMapper.convertValue(any(QuarantineLegalDoc.class), eq(Map.class))).thenReturn(attributes);
        when(objMapper.convertValue(any(), eq(uk.gov.hmcts.reform.prl.models.documents.Document.class))).thenReturn(
            document);
        Bundle caseBundles = Bundle.builder()
            .value(BundleDetails.builder().stitchedDocument(DocumentLink.builder().documentUrl("http://test.link")
                                                                .documentFilename("test").build()).build())
            .build();
        ResponseDocuments responseDocuments = ResponseDocuments.builder()
            .citizenDocument(document)
            .respondentC8Document(document)
            .respondentC8DocumentWelsh(document)
            .build();
        ApplicantDetails applicantDetails = ApplicantDetails.builder()
            .response(Response.builder().responseToAllegationsOfHarm(ResponseToAllegationsOfHarm.builder()
                                                                         .responseToAllegationsOfHarmDocument(document)
                                                                         .build()).build())
            .build();
        CafCassCaseData cafCassCaseData = CafCassCaseData.builder()
            .respondents(List.of(Element.<ApplicantDetails>builder().id(UUID.randomUUID()).value(applicantDetails).build()))
            .bundleInformation(BundlingInformation.builder().caseBundles(List.of(caseBundles)).build())
            .respondentAc8Documents(List.of(element(responseDocuments)))
            .respondentBc8Documents(List.of(element(responseDocuments)))
            .respondentCc8Documents(List.of(element(responseDocuments)))
            .respondentDc8Documents(List.of(element(responseDocuments)))
            .respondentEc8Documents(List.of(element(responseDocuments)))
            .build();
        CafCassCaseDetail cafCassCaseDetail = CafCassCaseDetail.builder()
            .caseData(cafCassCaseData)
            .build();
        CafCassResponse cafCassResponse = CafCassResponse.builder().cases(List.of(cafCassCaseDetail)).build();
        Method privateMethod = CaseDataService.class.getDeclaredMethod(
            "addSpecificDocumentsFromCaseFileViewBasedOnCategories",
            CafCassResponse.class
        );
        privateMethod.setAccessible(true);
        privateMethod.invoke(caseDataService, cafCassResponse);

        assertEquals("test", cafCassResponse.getCases().get(0).getCaseData().getOtherDocuments().get(0).getValue().getDocumentName());
        assertNull(cafCassResponse.getCases().get(0).getCaseData().getCourtStaffUploadDocListDocTab());
        assertNull(cafCassResponse.getCases().get(0).getCaseData().getCafcassUploadDocListDocTab());

    }

    @Test
    public void testRedactedDocumentsOnAddInOtherDocuments() throws NoSuchMethodException,
        InvocationTargetException, IllegalAccessException {
        String category = "MIAMCertificate";
        Document document = Document.builder().documentUrl(REDACTED_DOCUMENT_URL).documentFileName("*Redacted*").build();
        List<Element<OtherDocuments>> otherDocsList = new ArrayList<>();
        Method privateMethod = CaseDataService.class.getDeclaredMethod(
            "addInOtherDocuments",
            String.class, Document.class, List.class
        );
        privateMethod.setAccessible(true);
        privateMethod.invoke(caseDataService, category, document, otherDocsList);

        assertTrue(otherDocsList.isEmpty());

    }

    @Test
    public void testAddInOtherDocuments() throws NoSuchMethodException,
        InvocationTargetException, IllegalAccessException {
        String category = "MIAMCertificate";
        Document document = Document.builder().documentUrl("http://test").documentFileName("test").build();
        List<Element<OtherDocuments>> otherDocsList = new ArrayList<>();
        Method privateMethod = CaseDataService.class.getDeclaredMethod(
            "addInOtherDocuments",
            String.class, Document.class, List.class
        );
        privateMethod.setAccessible(true);
        privateMethod.invoke(caseDataService, category, document, otherDocsList);

        assertFalse(otherDocsList.isEmpty());
        assertEquals("test", otherDocsList.get(0).getValue().getDocumentName());
    }
    @Test
    public void shouldMapFinalisedServiceOfApplicationDocuments() throws NoSuchMethodException,
        InvocationTargetException, IllegalAccessException {
        Document document = Document.builder().documentUrl("test").documentFileName("test").build();
        when(objMapper.convertValue(any(QuarantineLegalDoc.class), eq(Map.class))).thenReturn(new HashMap<>());
        when(objMapper.convertValue(anyMap(), eq(uk.gov.hmcts.reform.prl.models.documents.Document.class))).thenReturn(
            document);
        CafCassCaseData cafCassCaseData = CafCassCaseData.builder()
            .finalServedApplicationDetailsList(List.of(element(ServedApplicationDetails.builder()
                                                                   .bulkPrintDetails(List.of(element(
                                                                       BulkPrintDetails.builder()
                                                                           .printDocs(List.of(element(
                                                                               Document.builder()
                                                                                   .documentUrl("http://test.link")
                                                                                   .documentFileName("testPrint")
                                                                                   .build()
                                                                           )))
                                                                           .build()
                                                                   )))
                                                                   .emailNotificationDetails(List.of(element(
                                                                       EmailNotificationDetails.builder()
                                                                           .docs(List.of(element(
                                                                               Document.builder()
                                                                                   .documentUrl("http://test.link")
                                                                                   .documentFileName("testEmail")
                                                                                   .build())))
                                                                           .build()
                                                                   )))
                                                                   .build())))
            .build();
        CafCassCaseDetail cafCassCaseDetail = CafCassCaseDetail.builder()
            .caseData(cafCassCaseData)
            .build();
        CafCassResponse cafCassResponse = CafCassResponse.builder().cases(List.of(cafCassCaseDetail)).build();
        Method privateMethod = CaseDataService.class.getDeclaredMethod(
            "addSpecificDocumentsFromCaseFileViewBasedOnCategories",
            CafCassResponse.class
        );
        privateMethod.setAccessible(true);
        privateMethod.invoke(caseDataService, cafCassResponse);

        CafCassCaseData response = cafCassResponse.getCases().get(0).getCaseData();
        assertEquals(2, response.getOtherDocuments().size());
        List<String> otherDocs = response.getOtherDocuments().stream()
            .map(el -> el.getValue().getDocumentName()).toList();

        assertTrue(otherDocs.contains("testPrint"));
        assertTrue(otherDocs.contains("testEmail"));
    }

}
