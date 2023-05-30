package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplicationUploadDocs;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.DocumentUtils.toGeneratedDocumentInfo;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.class)
public class ServiceOfApplicationPostServiceTest {

    @InjectMocks
    private ServiceOfApplicationPostService postService;

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private DocumentGenService documentGenService;

    private static final String AUTH = "Auth";
    private static final String LETTER_TYPE = "RespondentServiceOfApplication";
    private DynamicMultiSelectList dynamicMultiSelectList;

    @Before
    public void setup() {
        dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder().label("standardDirectionsOrder").build())).build();
    }

    @Test
    public void givenOnlyRepresentedRespondents_thenNoPostSent() throws Exception {
        PartyDetails respondent1 = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .build();
        PartyDetails respondent2 = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .build();

        CaseData caseData = CaseData.builder()
            .respondents(List.of(element(respondent1), element(respondent2)))
            .build();

        postService.send(caseData, AUTH);
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    public void givenCaseWithNoAllegationsOfHarm_sentDocsDoesNotContainC1a() throws Exception {
        PartyDetails respondent = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .build();

        Document finalDoc = Document.builder()
            .documentUrl("finalDoc")
            .documentBinaryUrl("finalDoc")
            .documentHash("finalDoc")
            .build();

        Document coverSheet = Document.builder()
            .documentUrl("coverSheet")
            .documentBinaryUrl("coverSheet")
            .documentHash("coverSheet")
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .allegationOfHarm(AllegationOfHarm.builder().allegationsOfHarmYesNo(YesOrNo.No).build())
            .finalDocument(finalDoc)
            .serviceOfApplicationScreen1(dynamicMultiSelectList)
            .orderCollection(List.of(element(OrderDetails.builder().build())))
            .respondents(List.of(element(respondent)))
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder()
                                                .pd36qLetter(Document.builder().build()).build())
            .build();

        when(documentGenService.generateSingleDocument(
            any(String.class),
            any(CaseData.class),
            any(String.class),
            any(boolean.class)
        ))
            .thenReturn(coverSheet);
        when(bulkPrintService.send(
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenReturn(null);
        List<GeneratedDocumentInfo> sentDocs = postService.send(caseData, AUTH);
        assertTrue(sentDocs.containsAll(List.of(
            toGeneratedDocumentInfo(finalDoc),
            toGeneratedDocumentInfo(coverSheet)
        )));
    }


    @Test
    public void givenCaseWithAllegationsOfHarm_generateAllWelshDocs() throws Exception {

        Document coverSheet = Document.builder()
            .documentUrl("coverSheet")
            .documentBinaryUrl("coverSheet")
            .documentHash("coverSheet")
            .build();

        when(documentGenService.generateSingleDocument(
            any(String.class),
            any(CaseData.class),
            any(String.class),
            any(boolean.class)
        ))
            .thenReturn(coverSheet);

        Document finalWelshDoc = Document.builder()
            .documentUrl("finalWelshDoc")
            .documentBinaryUrl("finalWelshDoc")
            .documentHash("finalWelshDoc")
            .build();

        Document finalWelshC1a = Document.builder()
            .documentUrl("finalWelshC1a")
            .documentBinaryUrl("finalWelshC1a")
            .documentHash("finalWelshC1a")
            .build();

        PartyDetails respondent1 = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .solicitorEmail("test@gmail.com")
            .build();

        PartyDetails respondent2 = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .solicitorEmail("test@gmail.com")
            .isCurrentAddressKnown(YesOrNo.Yes)
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .allegationOfHarm(AllegationOfHarm.builder().allegationsOfHarmYesNo(YesOrNo.Yes).build())
            .finalWelshDocument(finalWelshDoc)
            .c1AWelshDocument(finalWelshC1a)
            .serviceOfApplicationScreen1(dynamicMultiSelectList)
            .orderCollection(List.of(element(OrderDetails.builder().build())))
            .respondents(List.of(element(respondent1), element(respondent2)))
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder()
                                                .pd36qLetter(Document.builder().build()).build())
            .build();

        List<GeneratedDocumentInfo> sentDocs = postService.send(caseData, AUTH);
        assertTrue(sentDocs.containsAll(List.of(
            toGeneratedDocumentInfo(finalWelshDoc),
            toGeneratedDocumentInfo(coverSheet),
            toGeneratedDocumentInfo(finalWelshC1a)
        )));
    }

    @Test
    public void givenCaseWithAllegationsOfHarm_generateAllEnglishDocs() throws Exception {

        Document coverSheet = Document.builder()
            .documentUrl("coverSheet")
            .documentBinaryUrl("coverSheet")
            .documentHash("coverSheet")
            .build();

        when(documentGenService.generateSingleDocument(
            any(String.class),
            any(CaseData.class),
            any(String.class),
            any(boolean.class)
        ))
            .thenReturn(coverSheet);

        Document finalDoc = Document.builder()
            .documentUrl("finalDoc")
            .documentBinaryUrl("finalDoc")
            .documentHash("finalDoc")
            .build();

        Document finalC1a = Document.builder()
            .documentUrl("finalC1a")
            .documentBinaryUrl("finalC1a")
            .documentHash("finalC1a")
            .build();

        PartyDetails respondent = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .allegationOfHarm(AllegationOfHarm.builder().allegationsOfHarmYesNo(YesOrNo.Yes).build())
            .finalDocument(finalDoc)
            .c1ADocument(finalC1a)
            .serviceOfApplicationScreen1(dynamicMultiSelectList)
            .orderCollection(List.of(element(OrderDetails.builder().build())))
            .respondents(List.of(element(respondent)))
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder()
                                                .pd36qLetter(Document.builder().build()).build())
            .build();

        List<GeneratedDocumentInfo> sentDocs = postService.send(caseData, AUTH);
        assertTrue(sentDocs.containsAll(List.of(
            toGeneratedDocumentInfo(finalDoc),
            toGeneratedDocumentInfo(coverSheet),
            toGeneratedDocumentInfo(finalC1a)
        )));
    }

    @Test
    public void givenCaseWithMultipleOrders_generateAllEnglishDocsContainingOrders() throws Exception {

        Document coverSheet = Document.builder()
            .documentUrl("coverSheet")
            .documentBinaryUrl("coverSheet")
            .documentHash("coverSheet")
            .build();

        when(documentGenService.generateSingleDocument(
            any(String.class),
            any(CaseData.class),
            any(String.class),
            any(boolean.class)
        ))
            .thenReturn(coverSheet);

        Document finalDoc = Document.builder()
            .documentUrl("finalDoc")
            .documentBinaryUrl("finalDoc")
            .documentHash("finalDoc")
            .build();

        Document finalC1a = Document.builder()
            .documentUrl("finalC1a")
            .documentBinaryUrl("finalC1a")
            .documentHash("finalC1a")
            .build();

        PartyDetails respondent = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .isCurrentAddressKnown(YesOrNo.Yes)
            .build();


        Document standardDirectionsOrder = Document.builder()
            .documentUrl("standardDirectionsOrder")
            .documentBinaryUrl("standardDirectionsOrder")
            .documentHash("standardDirectionsOrder")
            .build();

        OrderDetails standardDirectionsOrderDetails = OrderDetails.builder()
            .orderType("Standard directions order")
            .orderTypeId("standardDirectionsOrder")
            .orderDocument(standardDirectionsOrder)
            .build();

        List<Element<OrderDetails>> orderCollection = new ArrayList<>();
        orderCollection.add(element(standardDirectionsOrderDetails));

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .allegationOfHarm(AllegationOfHarm.builder().allegationsOfHarmYesNo(YesOrNo.Yes).build())
            .finalDocument(finalDoc)
            .c1ADocument(finalC1a)
            .serviceOfApplicationScreen1(dynamicMultiSelectList)
            .respondents(List.of(element(respondent)))
            .orderCollection(orderCollection)
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder()
                                                .pd36qLetter(Document.builder().build()).build())
            .build();

        List<GeneratedDocumentInfo> sentDocs = postService.send(caseData, AUTH);
        assertTrue(sentDocs.containsAll(List.of(
            toGeneratedDocumentInfo(finalDoc),
            toGeneratedDocumentInfo(coverSheet),
            toGeneratedDocumentInfo(finalC1a),
            toGeneratedDocumentInfo(standardDirectionsOrder)
        )));
    }

    @Ignore
    @Test
    public void givenPeopleInTheCaseWithAddress() throws Exception {

        Document privacyNotice = Document.builder()
            .documentUrl("privacyNotice")
            .documentBinaryUrl("privacyNotice")
            .documentHash("privacyNotice")
            .build();

        when(documentGenService.generateSingleDocument(
            any(String.class),
            any(CaseData.class),
            any(String.class),
            any(boolean.class)
        ))
            .thenReturn(privacyNotice);

        PartyDetails otherPeopleInTheCase = PartyDetails.builder()
            .isCurrentAddressKnown(YesOrNo.Yes)
            .address(Address.builder().addressLine1("test").postCode("test").build())
            .build();


        CaseData caseData = CaseData.builder()
            .id(12345L)
            .allegationOfHarm(AllegationOfHarm.builder().allegationsOfHarmYesNo(YesOrNo.Yes).build())
            .othersToNotify(List.of(element(otherPeopleInTheCase)))
            .serviceOfApplicationScreen1(DynamicMultiSelectList.builder().build())
            .orderCollection(List.of(element(OrderDetails.builder().build())))
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder()
                                                .pd36qLetter(Document.builder().build()).build())
            .build();

        postService.sendDocs(caseData, AUTH);
        assertTrue((caseData.getFinalServedApplicationDetailsList().size() > 0));
    }
}
