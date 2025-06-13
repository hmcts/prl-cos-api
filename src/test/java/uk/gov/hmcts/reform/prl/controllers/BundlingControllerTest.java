package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.DocTypeOtherDocumentsEnum;
import uk.gov.hmcts.reform.prl.enums.FurtherEvidenceDocumentType;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FurtherEvidence;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bundle.Bundle;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleData;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleHearingInfo;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingData;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingInformation;
import uk.gov.hmcts.reform.prl.models.dto.bundle.DocumentLink;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamDetails;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.bundle.BundlingService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YOUR_POSITION_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YOUR_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@ExtendWith(MockitoExtension.class)
class BundlingControllerTest {

    @InjectMocks
    private BundlingController bundlingController;

    @Mock
    private BundlingService bundlingService;

    @Mock
    private ObjectMapper objectMapper;

    private BundleCreateResponse bundleCreateResponse;

    private CaseDetails caseDetails;

    @Mock
    private AboutToStartOrSubmitCallbackResponse response;

    @Mock
    private AuthorisationService authorisationService;

    private static final String AUTH_TOKEN = "Bearer TestAuthToken";
    private CaseData c100CaseData;
    private CaseData c100CaseDataWithCaseBundle;

    @BeforeEach
    void setUp() {
        List<Bundle> bundleList = new ArrayList<>();
        bundleList.add(Bundle.builder().value(BundleDetails.builder()
                                                   .id("bundleId-1")
                                                   .title("Bundle Title 1")
                                                   .stitchStatus("New")
                                                   .stitchedDocument(DocumentLink.builder().build())
                                                   .build())
                            .build());
        bundleCreateResponse = BundleCreateResponse.builder().data(BundleData.builder().id("334")
                                                                       .caseBundles(bundleList).data(BundlingData.builder()
            .hearingDetails(BundleHearingInfo.builder().build()).build()).build()).build();

        Map<String, Object> caseData = new HashMap<>();
        caseDetails = CaseDetails.builder().data(caseData).state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue())
            .id(123488888L).createdDate(LocalDateTime.now()).lastModified(LocalDateTime.now()).build();

        List<FurtherEvidence> furtherEvidences = new ArrayList<>();
        furtherEvidences.add(FurtherEvidence.builder().typeOfDocumentFurtherEvidence(FurtherEvidenceDocumentType.miamCertificate)
            .documentFurtherEvidence(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("Sample1.pdf").build())
            .restrictCheckboxFurtherEvidence(new ArrayList<>()).build());

        List<OtherDocuments> otherDocuments = new ArrayList<>();
        otherDocuments.add(OtherDocuments.builder().documentName("Application document")
            .documentOther(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("Sample2.pdf").build()).documentTypeOther(
                DocTypeOtherDocumentsEnum.applicantStatement).restrictCheckboxOtherDocuments(new ArrayList<>()).build());

        List<OrderDetails> orders = new ArrayList<>();
        orders.add(OrderDetails.builder().orderType("orders")
            .orderDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("Order.pdf").build()).build());

        List<ResponseDocuments> citizenC7uploadedDocs = new ArrayList<>();
        citizenC7uploadedDocs.add(ResponseDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("C7Document.pdf").build()).build());

        List<UploadedDocuments> uploadedDocuments = new ArrayList<>();
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("PositionStatement.pdf").build())
            .documentType(YOUR_POSITION_STATEMENTS).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("PositionStatement.pdf").build())
            .documentType(YOUR_POSITION_STATEMENTS).isApplicant("Yes").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("WitnessStatement.pdf").build())
            .documentType(YOUR_WITNESS_STATEMENTS).isApplicant("Yes").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("WitnessStatement.pdf").build())
            .documentType(YOUR_WITNESS_STATEMENTS).isApplicant("No").build());

        c100CaseData = CaseData.builder()
            .id(123456789123L)
            .languagePreferenceWelsh(No)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .finalDocument(Document.builder().documentFileName("C100AppDoc").documentUrl("Url").build())
            .c1ADocument(Document.builder().documentFileName("c1ADocument").documentUrl("Url").build())
            .otherDocuments(ElementUtils.wrapElements(otherDocuments))
            .furtherEvidences(ElementUtils.wrapElements(furtherEvidences))
            .orderCollection(ElementUtils.wrapElements(orders))
            .bundleInformation(BundlingInformation.builder().build())
            .citizenResponseC7DocumentList(ElementUtils.wrapElements(citizenC7uploadedDocs))
            .citizenUploadedDocumentList(ElementUtils.wrapElements(uploadedDocuments))
            .bundleInformation(BundlingInformation.builder().bundleConfiguration("sample.yaml").historicalBundles(bundleList).build())
            .miamDetails(MiamDetails.builder().miamCertificationDocumentUpload(Document.builder()
                    .documentFileName("maimCertDoc1").documentUrl("Url").build())
                .miamCertificationDocumentUpload1(Document.builder().documentFileName("maimCertDoc2")
                    .documentUrl("Url").build()).build())
            .applicantName("ApplicantFirstNameAndLastName")
            .build();

        List<Bundle> caseBundleList = new ArrayList<>();
        caseBundleList.add(Bundle.builder().value(BundleDetails.builder()
                                                  .id("caseBundleId-1")
                                                  .title("Case Bundle Title 1")
                                                  .stitchStatus("New")
                                                  .stitchedDocument(DocumentLink.builder().build())
                                                  .build())
                           .build());

        c100CaseDataWithCaseBundle = CaseData.builder()
            .id(123456789123L)
            .languagePreferenceWelsh(No)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .finalDocument(Document.builder().documentFileName("C100AppDoc").documentUrl("Url").build())
            .c1ADocument(Document.builder().documentFileName("c1ADocument").documentUrl("Url").build())
            .otherDocuments(ElementUtils.wrapElements(otherDocuments))
            .furtherEvidences(ElementUtils.wrapElements(furtherEvidences))
            .orderCollection(ElementUtils.wrapElements(orders))
            .citizenResponseC7DocumentList(ElementUtils.wrapElements(citizenC7uploadedDocs))
            .citizenUploadedDocumentList(ElementUtils.wrapElements(uploadedDocuments))
            .bundleInformation(BundlingInformation.builder()
                                   .caseBundles(caseBundleList)
                                   .bundleConfiguration("sample.yaml")
                                   .historicalBundles(bundleList).build())
            .miamDetails(MiamDetails.builder().miamCertificationDocumentUpload(Document.builder()
                                                                                   .documentFileName("maimCertDoc1").documentUrl("Url").build())
                             .miamCertificationDocumentUpload1(Document.builder().documentFileName("maimCertDoc2")
                                                                   .documentUrl("Url").build()).build())
            .applicantName("ApplicantFirstNameAndLastName")
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
    }

    @Test
    void testCreateBundleWhenBundleApiResponseIsNull() {
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(c100CaseData);
        when(bundlingService.createBundleServiceRequest(any(CaseData.class), anyString(), anyString())).thenReturn(null);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).eventId("eventId").build();
        response = bundlingController.createBundle(AUTH_TOKEN, "serviceAuth", callbackRequest);
        assertNull(response.getData().get("bundleInformation"));
    }

    @Test
    void testCreateBundleWhenBundleDataInResponseIsNull() {
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(c100CaseData);
        when(bundlingService.createBundleServiceRequest(any(CaseData.class), anyString(), anyString()))
            .thenReturn(BundleCreateResponse.builder().build());
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).eventId("eventId").build();
        response = bundlingController.createBundle(AUTH_TOKEN, "serviceAuth", callbackRequest);
        assertNull(response.getData().get("bundleInformation"));
    }

    @Test
    void testCreateBundleWhenCaseBundlesInResponseIsNull() {
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(c100CaseData);
        when(bundlingService.createBundleServiceRequest(any(CaseData.class), anyString(), anyString()))
            .thenReturn(BundleCreateResponse.builder().data(BundleData.builder()
            .build()).build());
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).eventId("eventId").build();
        response = bundlingController.createBundle(AUTH_TOKEN, "serviceAuth", callbackRequest);
        assertNull(response.getData().get("bundleInformation"));
    }

    @Test
    void testCreateBundleWhenCaseBundlesInResponseIsNotNull() {
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(c100CaseData);
        when(bundlingService.createBundleServiceRequest(any(CaseData.class), anyString(), anyString()))
            .thenReturn(bundleCreateResponse);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).eventId("eventId").build();
        response = bundlingController.createBundle(AUTH_TOKEN, "serviceAuth", callbackRequest);
        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertNotNull(response.getData().get("bundleInformation"));
        BundlingInformation bundlingInformation = (BundlingInformation)response.getData().get("bundleInformation");
        assertEquals(1, bundlingInformation.getCaseBundles().size());
        assertEquals("Bundle Title 1",(bundlingInformation.getCaseBundles().getFirst()).getValue().getTitle());
        assertEquals(1, bundlingInformation.getHistoricalBundles().size());
        assertEquals("Bundle Title 1",(bundlingInformation.getHistoricalBundles().getFirst()).getValue().getTitle());
    }

    @Test
    void testCreateBundleWithCaseDataWithCaseBundleIsNotNull() {
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(c100CaseDataWithCaseBundle);
        when(bundlingService.createBundleServiceRequest(any(CaseData.class), anyString(), anyString()))
            .thenReturn(bundleCreateResponse);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).eventId("eventId").build();
        response = bundlingController.createBundle(AUTH_TOKEN, "serviceAuth", callbackRequest);
        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertNotNull(response.getData().get("bundleInformation"));
        BundlingInformation bundlingInformation = (BundlingInformation)response.getData().get("bundleInformation");
        assertEquals(1, bundlingInformation.getCaseBundles().size());
        assertEquals("Bundle Title 1",(bundlingInformation.getCaseBundles().getFirst())
            .getValue().getTitle());
        assertEquals(2, bundlingInformation.getHistoricalBundles().size());
        assertEquals("Bundle Title 1",(bundlingInformation.getHistoricalBundles().getFirst())
            .getValue().getTitle());
        assertEquals("Case Bundle Title 1",(bundlingInformation.getHistoricalBundles().get(1))
            .getValue().getTitle());

    }

    @Test
    void testCreateBundleWhenAuthorisationIsFalse() {
        when(authorisationService.isAuthorized(any(),any())).thenReturn(false);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).eventId("eventId").build();
        assertThrows(RuntimeException.class, () ->
            bundlingController.createBundle(AUTH_TOKEN, "serviceAuth", callbackRequest));
    }

    @Test
    void testCreateBundleWhenCaseBundlesInCreateBundleServiceRequestIsNull() {
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(c100CaseData);
        when(bundlingService.createBundleServiceRequest(any(CaseData.class), anyString(), anyString()))
            .thenReturn(BundleCreateResponse.builder().data(BundleData.builder()
                                                                .build()).build());
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).eventId("eventId").build();
        response = bundlingController.createBundle(AUTH_TOKEN, "serviceAuth", callbackRequest);
        assertNotNull(response);
        assertEquals(0, response.getData().size());
        assertNull(response.getData().get("bundleInformation"));
    }
}
