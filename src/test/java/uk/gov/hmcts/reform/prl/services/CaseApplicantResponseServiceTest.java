package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.proceedings.CurrentOrPreviousProceedings;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.proceedings.OtherProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.proceedings.Proceedings;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.safetyconcerns.SafetyConcerns;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CitizenResponseDocuments;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenResponseNotificationEmailService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService.IS_CONFIDENTIAL_DATA_PRESENT;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseApplicantResponseServiceTest {

    @InjectMocks
    private CaseApplicationResponseService caseApplicationResponseService;

    @Mock
    private CaseService caseService;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    DocumentGenService documentGenService;

    @Mock
    CitizenResponseNotificationEmailService solicitorNotificationService;

    @Mock
    IdamClient idamClient;

    @Mock
    C100RespondentSolicitorService c100RespondentSolicitorService;

    @Mock
    DocumentLanguageService documentLanguageService;

    private CaseData caseData;
    private CaseDetails caseDetails;
    public static final String authToken = "Bearer TestAuthToken";
    public static final String servAuthToken = "Bearer TestServToken";
    private static final String caseId = "1234567891234567";
    private static final String partyId = "e3ceb507-0137-43a9-8bd3-85dd23720648";

    private  final Map<String, Object> dataMap = new HashMap<>();

    private Map<String, Object> stringObjectMap;

    @Before
    public void setUp() throws Exception {
        dataMap.put(IS_CONFIDENTIAL_DATA_PRESENT, true);
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .respondents(List.of(Element.<PartyDetails>builder()
                .id(UUID.fromString(partyId))
                .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                    .response(Response.builder().safetyConcerns(
                        SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
                .build()))
            .build();
        stringObjectMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();

        when(documentGenService.generateSingleDocument(
            Mockito.anyString(),
            Mockito.any(CaseData.class),
            Mockito.anyString(),
            Mockito.anyBoolean()
        ))
            .thenReturn(Document.builder().build());
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(coreCaseDataApi.getCase(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(caseDetails);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(caseService.updateCase(Mockito.any(CaseData.class), Mockito.anyString(), Mockito.anyString(),
                                    Mockito.anyString()
        )).thenReturn(caseDetails);

        when(idamClient.getUserDetails(Mockito.anyString())).thenReturn(UserDetails.builder().build());
        when(c100RespondentSolicitorService.populateDataMap(any(), any(), any())).thenReturn(new HashMap<>());
    }

    @Test
    public void testGenerateC7finalDocument() throws Exception {
        when(c100RespondentSolicitorService.populateDataMap(Mockito.any(CallbackRequest.class),
                                                            Mockito.any(Element.class), Mockito.anyString()))
            .thenReturn(dataMap);
        when(documentGenService.generateSingleDocument(Mockito.anyString(), Mockito.any(CaseData.class),
            Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(
            Document.builder().build());
        when(documentGenService.generateSingleDocument(Mockito.anyString(), Mockito.any(CaseData.class),
            Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(
            Document.builder().build());
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(DocumentLanguage
                                                                               .builder()
                                                                               .isGenEng(true)
                                                                               .isGenWelsh(true)
                                                                               .build());
        CaseDetails responseCaseDetails = caseApplicationResponseService
            .generateCitizenResponseFinalDocuments(caseData, caseDetails, authToken, partyId, caseId);
        assertNotNull(responseCaseDetails);
    }

    @Test
    public void testGenerateC7finalDocumentForRespondent1() throws Exception {
        List<Element<OtherProceedingDetails>> proceedingsDetailsList = new ArrayList<>();
        OtherProceedingDetails proceedingDetails = OtherProceedingDetails
            .builder().orderDocument(Document.builder().documentFileName("C7_Document.pdf").build()).build();
        OtherProceedingDetails proceedingDetails1 = OtherProceedingDetails
            .builder().orderDocument(Document.builder().documentFileName("C1A_allegation_of_harm.pdf").build()).build();
        OtherProceedingDetails proceedingDetails2 = OtherProceedingDetails
            .builder().orderDocument(Document.builder().documentFileName("test").build()).build();


        proceedingsDetailsList.add(element(proceedingDetails));
        proceedingsDetailsList.add(element(proceedingDetails1));
        proceedingsDetailsList.add(element(proceedingDetails2));

        Proceedings proceedings = Proceedings.builder().proceedingDetails(proceedingsDetailsList).build();

        List<Element<Proceedings>> proceedingsList = new ArrayList<>();
        proceedingsList.add(element(proceedings));
        proceedingsList.add(element(proceedings));

        Element partyDetails1 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .currentRespondent(YesOrNo.Yes)
                .response(Response.builder()
                    .currentOrPreviousProceedings(CurrentOrPreviousProceedings
                        .builder().proceedingsList(proceedingsList).build()).safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        List<Element<PartyDetails>> elementList = new ArrayList<>();
        elementList.add(partyDetails1);
        caseData = caseData.toBuilder()
            .respondents(elementList).build();

        when(c100RespondentSolicitorService.populateDataMap(Mockito.any(CallbackRequest.class),
                                                            Mockito.any(Element.class), Mockito.anyString()))
            .thenReturn(dataMap);
        when(documentGenService.generateSingleDocument(Mockito.anyString(), Mockito.any(CaseData.class),
            Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(
            Document.builder().build());
        when(documentGenService.generateSingleDocument(Mockito.anyString(), Mockito.any(CaseData.class),
            Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(
            Document.builder().build());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(DocumentLanguage
                                                                               .builder()
                                                                               .isGenEng(true)
                                                                               .isGenWelsh(true)
                                                                               .build());
        CaseDetails responseCaseDetails = caseApplicationResponseService
            .generateCitizenResponseFinalDocuments(caseData, caseDetails, authToken, partyId, caseId);
        assertNotNull(responseCaseDetails);
    }

    @Test
    public void testGenerateC7finalDocumentForRespondent2() throws Exception {
        List<Element<OtherProceedingDetails>> proceedingsDetailsList = new ArrayList<>();
        OtherProceedingDetails proceedingDetails = OtherProceedingDetails
            .builder().orderDocument(Document.builder().build()).build();

        proceedingsDetailsList.add(element(proceedingDetails));

        Proceedings proceedings = Proceedings.builder().proceedingDetails(proceedingsDetailsList).build();

        List<Element<Proceedings>> proceedingsList = new ArrayList<>();
        proceedingsList.add(element(proceedings));

        Element partyDetails2 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .currentRespondent(YesOrNo.Yes)
                .response(Response.builder()
                    .currentOrPreviousProceedings(CurrentOrPreviousProceedings
                        .builder().proceedingsList(proceedingsList).build()).safetyConcerns(
                        SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();

        Element partyDetails1 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        Element partyDetails3 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        Element partyDetails4 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        Element partyDetails5 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        List<Element<PartyDetails>> elementList = new ArrayList<>();
        elementList.add(partyDetails1);
        elementList.add(partyDetails2);
        elementList.add(partyDetails3);
        elementList.add(partyDetails4);
        elementList.add(partyDetails5);
        caseData = caseData.toBuilder()
            .respondents(elementList).build();

        when(c100RespondentSolicitorService.populateDataMap(Mockito.any(CallbackRequest.class),
                                                            Mockito.any(Element.class), Mockito.anyString()))
            .thenReturn(dataMap);
        when(documentGenService.generateSingleDocument(Mockito.anyString(), Mockito.any(CaseData.class),
            Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(
            Document.builder().build());
        when(documentGenService.generateSingleDocument(Mockito.anyString(), Mockito.any(CaseData.class),
            Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(
            Document.builder().build());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(DocumentLanguage
                                                                               .builder()
                                                                               .isGenEng(true)
                                                                               .isGenWelsh(true)
                                                                               .build());
        CaseDetails responseCaseDetails = caseApplicationResponseService
            .generateCitizenResponseFinalDocuments(caseData, caseDetails, authToken, partyId, caseId);
        assertNotNull(responseCaseDetails);
    }

    @Test
    public void testGenerateC7finalDocumentForRespondent3() throws Exception {

        List<Element<OtherProceedingDetails>> proceedingsDetailsList = new ArrayList<>();
        OtherProceedingDetails proceedingDetails = OtherProceedingDetails
            .builder().build();

        proceedingsDetailsList.add(element(proceedingDetails));

        Proceedings proceedings = Proceedings.builder().proceedingDetails(proceedingsDetailsList).build();

        List<Element<Proceedings>> proceedingsList = new ArrayList<>();
        proceedingsList.add(element(proceedings));

        Element partyDetails3 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .currentRespondent(YesOrNo.Yes)
                .response(Response.builder()
                    .currentOrPreviousProceedings(CurrentOrPreviousProceedings
                        .builder().proceedingsList(proceedingsList).build()).safetyConcerns(
                        SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();

        Element partyDetails1 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        Element partyDetails2 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        Element partyDetails4 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        Element partyDetails5 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        List<Element<PartyDetails>> elementList = new ArrayList<>();
        elementList.add(partyDetails1);
        elementList.add(partyDetails2);
        elementList.add(partyDetails3);
        elementList.add(partyDetails4);
        elementList.add(partyDetails5);
        caseData = caseData.toBuilder()
            .respondents(elementList).build();

        when(c100RespondentSolicitorService.populateDataMap(Mockito.any(CallbackRequest.class),
                                                            Mockito.any(Element.class), Mockito.anyString()))
            .thenReturn(dataMap);
        when(documentGenService.generateSingleDocument(Mockito.anyString(), Mockito.any(CaseData.class),
            Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(
            Document.builder().build());
        when(documentGenService.generateSingleDocument(Mockito.anyString(), Mockito.any(CaseData.class),
            Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(
            Document.builder().build());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(DocumentLanguage
                                                                               .builder()
                                                                               .isGenEng(true)
                                                                               .isGenWelsh(true)
                                                                               .build());
        CaseDetails responseCaseDetails = caseApplicationResponseService
            .generateCitizenResponseFinalDocuments(caseData, caseDetails, authToken, partyId, caseId);
        assertNotNull(responseCaseDetails);
    }

    @Test
    public void testGenerateC7finalDocumentForRespondent4() throws Exception {
        Proceedings proceedings = Proceedings.builder().build();

        List<Element<Proceedings>> proceedingsList = new ArrayList<>();
        proceedingsList.add(element(proceedings));

        Element partyDetails4 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .currentRespondent(YesOrNo.Yes)
                .response(Response.builder()
                    .currentOrPreviousProceedings(CurrentOrPreviousProceedings
                        .builder().proceedingsList(proceedingsList).build()).safetyConcerns(
                        SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();

        Element partyDetails1 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        Element partyDetails2 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        Element partyDetails3 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        Element partyDetails5 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        List<Element<PartyDetails>> elementList = new ArrayList<>();
        elementList.add(partyDetails1);
        elementList.add(partyDetails2);
        elementList.add(partyDetails3);
        elementList.add(partyDetails4);
        elementList.add(partyDetails5);
        caseData = caseData.toBuilder()
            .respondents(elementList).build();

        when(c100RespondentSolicitorService.populateDataMap(Mockito.any(CallbackRequest.class),
                                                            Mockito.any(Element.class), Mockito.anyString()))
            .thenReturn(dataMap);
        when(documentGenService.generateSingleDocument(Mockito.anyString(), Mockito.any(CaseData.class),
            Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(
            Document.builder().build());
        when(documentGenService.generateSingleDocument(Mockito.anyString(), Mockito.any(CaseData.class),
            Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(
            Document.builder().build());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(DocumentLanguage
                                                                               .builder()
                                                                               .isGenEng(true)
                                                                               .isGenWelsh(true)
                                                                               .build());
        CaseDetails responseCaseDetails = caseApplicationResponseService
            .generateCitizenResponseFinalDocuments(caseData, caseDetails, authToken, partyId, caseId);
        assertNotNull(responseCaseDetails);
    }

    @Test
    public void testGenerateC7finalDocumentForRespondent5() throws Exception {
        Element partyDetails1 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        Element partyDetails2 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        Element partyDetails3 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        Element partyDetails4 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        Element partyDetails5 =  Element.<PartyDetails>builder()
            .id(UUID.fromString(partyId))
            .value(PartyDetails.builder().firstName("a").isAddressConfidential(YesOrNo.Yes)
                .currentRespondent(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        List<Element<PartyDetails>> elementList = new ArrayList<>();
        elementList.add(partyDetails1);
        elementList.add(partyDetails2);
        elementList.add(partyDetails3);
        elementList.add(partyDetails4);
        elementList.add(partyDetails5);
        caseData = caseData.toBuilder()
            .respondents(elementList).build();

        when(c100RespondentSolicitorService.populateDataMap(Mockito.any(CallbackRequest.class),
                                                            Mockito.any(Element.class), Mockito.anyString()))
            .thenReturn(dataMap);
        when(documentGenService.generateSingleDocument(Mockito.anyString(), Mockito.any(CaseData.class),
            Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(
            Document.builder().build());
        when(documentGenService.generateSingleDocument(Mockito.anyString(), Mockito.any(CaseData.class),
            Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(
            Document.builder().build());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(DocumentLanguage
                                                                               .builder()
                                                                               .isGenEng(true)
                                                                               .isGenWelsh(true)
                                                                               .build());
        CaseDetails responseCaseDetails = caseApplicationResponseService
            .generateCitizenResponseFinalDocuments(caseData, caseDetails, authToken, partyId, caseId);
        assertNotNull(responseCaseDetails);
    }

    @Test
    public void testGenerateC7finalDocumentTriggersDefault() throws Exception {
        Element partyDetails1 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        Element partyDetails2 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        Element partyDetails3 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        Element partyDetails4 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        Element partyDetails5 =  Element.<PartyDetails>builder()
            .id(UUID.fromString(partyId))
            .value(PartyDetails.builder().firstName("a").isAddressConfidential(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        Element partyDetails6 =  Element.<PartyDetails>builder()
            .id(UUID.fromString(partyId))
            .value(PartyDetails.builder().firstName("a").isAddressConfidential(YesOrNo.Yes)
                .currentRespondent(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        List<Element<PartyDetails>> elementList = new ArrayList<>();
        elementList.add(partyDetails1);
        elementList.add(partyDetails2);
        elementList.add(partyDetails3);
        elementList.add(partyDetails4);
        elementList.add(partyDetails5);
        elementList.add(partyDetails6);
        caseData = caseData.toBuilder()
            .citizenResponseDocuments(CitizenResponseDocuments.builder().build())
            .respondents(elementList).build();

        when(c100RespondentSolicitorService.populateDataMap(Mockito.any(CallbackRequest.class),
                                                            Mockito.any(Element.class), Mockito.anyString()))
            .thenReturn(dataMap);
        when(documentGenService.generateSingleDocument(Mockito.anyString(), Mockito.any(CaseData.class),
            Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(
            Document.builder().build());
        when(documentGenService.generateSingleDocument(Mockito.anyString(), Mockito.any(CaseData.class),
            Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyMap())).thenReturn(
            Document.builder().build());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(DocumentLanguage
                                                                               .builder()
                                                                               .isGenEng(true)
                                                                               .isGenWelsh(true)
                                                                               .build());
        CaseDetails responseCaseDetails = caseApplicationResponseService
            .generateCitizenResponseFinalDocuments(caseData, caseDetails, authToken, partyId, caseId);
        assertNotNull(responseCaseDetails);
    }

    @Test
    public void testGenerateC7DraftDocument() throws Exception {
        Document document = caseApplicationResponseService
            .generateC7DraftDocument(authToken, caseData,false);
        assertNotNull(document);
    }

    @Test
    public void testUpdateCurrentRespondent() {
        Element partyDetails1 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        List<Element<PartyDetails>> elementList = new ArrayList<>();
        elementList.add(partyDetails1);
        caseData = caseData.toBuilder()
            .respondents(elementList).build();
        CaseData updatedCaseData = caseApplicationResponseService.updateCurrentRespondent(caseData, YesOrNo.Yes, partyDetails1.getId().toString());
        assertNotNull(updatedCaseData);
    }

    @Test
    public void testUpdateCurrentRespondentIdsDontMatch() {
        Element partyDetails1 =  Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(PartyDetails.builder().firstName("test").isAddressConfidential(YesOrNo.Yes)
                .response(Response.builder().safetyConcerns(
                    SafetyConcerns.builder().haveSafetyConcerns(YesOrNo.Yes).build()).build()).build())
            .build();
        List<Element<PartyDetails>> elementList = new ArrayList<>();
        elementList.add(partyDetails1);
        caseData = caseData.toBuilder()
            .respondents(elementList).build();
        CaseData updatedCaseData = caseApplicationResponseService.updateCurrentRespondent(caseData, YesOrNo.Yes, UUID.randomUUID().toString());
        assertNotNull(updatedCaseData);
    }
}
