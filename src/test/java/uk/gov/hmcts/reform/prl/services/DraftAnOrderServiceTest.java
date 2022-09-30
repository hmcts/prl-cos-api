package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DraftAnOrderServiceTest {


    @InjectMocks
    private DraftAnOrderService draftAnOrderService;

    @Mock
    private DgsService dgsService;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    @Mock
    private Time dateTime;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ElementUtils elementUtils;

    @Mock
    private CaseInviteManager caseInviteManager;

    private static CaseData caseData;

    private List<Element<DraftOrder>> draftOrderCollection;
    private DynamicList dynamicList;
    private Address address;
    private Document document;

    @Before
    public void testDataToUse() throws Exception {
        document = Document.builder()
            .documentFileName("test")
            .build();
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        draftOrderCollection = List.of(element(DraftOrder.builder()
                                                   .orderTypeId("")
                                                   .orderDocument(document)
                                                   .build()));
        dynamicList = ElementUtils.asDynamicList(draftOrderCollection, null, DraftOrder::getLabelForOrdersDynamicList);
        address = Address.builder().build();
        Element<DraftOrder> draftOrderElement = element(DraftOrder.builder()
                                                            .orderText("test")
                                                            .adminNotes("adminNotes")
                                                            .judgeNotes("judgeNotes")
                                                            .orderDocument(document)
                                                            .otherDetails(OtherDraftOrderDetails.builder()
                                                                              .dateCreated(LocalDateTime.now())
                                                                              .build())
                                                            .build());
        caseData = CaseData.builder()
            .replyMessageDynamicList(dynamicList)
            .draftOrdersDynamicList(dynamicList)
            .dateOrderMade(LocalDate.now())
            .solicitorOrJudgeDraftOrderDoc(document)
            .draftOrderCollection(List.of(draftOrderElement))
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper)).thenReturn(draftOrderElement.getId());
        UUID selectedValue = UUID.randomUUID();
        when(dateTime.now()).thenReturn(LocalDateTime.now());

        when(dgsService.generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        )).thenReturn(generatedDocumentInfo);
    }

    @Test
    public void testPopulateSelectedOrderText() {
        Map<String, Object> caseDataMap = draftAnOrderService.populateSelectedOrderText(caseData);
        assertEquals("test", caseDataMap.get("previewDraftAnOrder"));
    }

    @Test
    public void testGetDraftOrderDynamicList() {
        Map<String, Object> caseDataMap = draftAnOrderService.getDraftOrderDynamicList(draftOrderCollection);
        assertNotNull(caseDataMap.get("draftOrdersDynamicList"));
    }

    @Test
    public void testGenerateDraftOrderCollection() {
        Map<String, Object> caseDataMap = draftAnOrderService.generateDraftOrderCollection(caseData);
        assertNotNull(caseDataMap.get("draftOrderCollection"));
    }

    @Test
    public void testGenerateDraftOrderCollectionElseCondition() {
        caseData = caseData.toBuilder().draftOrderCollection(null).build();
        Map<String, Object> caseDataMap = draftAnOrderService.generateDraftOrderCollection(caseData);
        assertNotNull(caseDataMap.get("draftOrderCollection"));
    }

    @Test
    public void testgetTheOrderDraftString() {
        assertNull(draftAnOrderService.getTheOrderDraftString(caseData));
    }

    @Test
    public void testgetTheOrderDraftStringNonMolestationOrder() {
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .manageOrders(ManageOrders.builder()
                              .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge)
                              .build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .build();
        assertNotNull(draftAnOrderService.getTheOrderDraftString(caseData));
    }


    @Ignore
    @Test
    public void testupdateDraftOrderCollection() {
        assertNotNull(draftAnOrderService.updateDraftOrderCollection(caseData).get("draftOrderCollection"));
    }

    @Test
    public void testPopulateSelectedOrder() {
        assertNotNull(draftAnOrderService.populateSelectedOrder(caseData).get("previewDraftOrder"));
    }

    @Test
    public void testgetTheOrderDraftStringNonMolestationOrderWithcustomFields() {
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .dateOrderMade(null)
            .recitalsOrPreamble("")
            .respondentsFL401(PartyDetails.builder().address(address).build())
            .applicantChildDetails(List.of(element(ApplicantChild.builder()
                                                       .fullName("")
                                                       .dateOfBirth(LocalDate.now())
                                                       .build())))
            .manageOrders(ManageOrders.builder()
                              .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge)
                              .recitalsOrPreamble("")
                              .isTheOrderByConsent(YesOrNo.Yes)
                              .furtherDirectionsIfRequired("")
                              .fl404CustomFields(FL404.builder()
                                                     .fl404bApplicantName("")
                                                     .fl404bCourtName("")
                                                     .fl404bApplicantReference("")
                                                     .fl404bRespondentDob(LocalDate.now())
                                                     .fl404bRespondentName("")
                                                     .fl404bRespondentNotToThreat(List.of(""))
                                                     .fl404bRespondentNotIntimidate(List.of(""))
                                                     .fl404bRespondentNotToTelephone(List.of(""))
                                                     .fl404bRespondentNotToDamageOrThreat(List.of(""))
                                                     .fl404bRespondentNotToDamage(List.of(""))
                                                     .fl404bRespondentNotToEnterProperty(List.of(""))
                                                     .fl404bRespondentNotToThreatChild(List.of(""))
                                                     .fl404bRespondentNotHarassOrIntimidate(List.of(""))
                                                     .fl404bRespondentNotToTelephoneChild(List.of(""))
                                                     .fl404bRespondentNotToEnterSchool(List.of(""))
                                                     .fl404bMentionedProperty("Yes")
                                                     .fl404bAddressOfProperty("Yes")
                                                     .fl404bAddMoreDetailsTelephone("")
                                                     .fl404bDateOrderEnd("")
                                                     .fl404bDateOrderEndTime("")
                                                     .fl404bIsNoticeGiven("WithoutNotice")
                                                     .fl404bDateOfNextHearing("")
                                                     .fl404bTimeOfNextHearing("")
                                                     .fl404bCourtName1("")
                                                     .fl404bOtherCourtAddress(address)
                                                     .fl404bTimeEstimate("")
                                                     .fl404bCostOfApplication("")
                                                     .fl404bIsNoticeGiven("WithoutNotice")
                                                     .build())
                              .build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .build();
        assertNotNull(draftAnOrderService.getTheOrderDraftString(caseData));
    }

    @Test
    public void testGenerateSolicitorDraftOrder() throws Exception {
        Document document = draftAnOrderService.generateSolicitorDraftOrder("", caseData);
        assertEquals(document.getDocumentBinaryUrl(), generatedDocumentInfo.getBinaryUrl());
        assertEquals(document.getDocumentUrl(), generatedDocumentInfo.getUrl());
        assertEquals(document.getDocumentHash(), generatedDocumentInfo.getHashToken());
    }

    @Test
    public void testGenerateJudgeDraftOrder() throws Exception {
        Document document = draftAnOrderService.generateJudgeDraftOrder("", caseData);
        assertEquals(document.getDocumentBinaryUrl(), generatedDocumentInfo.getBinaryUrl());
        assertEquals(document.getDocumentUrl(), generatedDocumentInfo.getUrl());
        assertEquals(document.getDocumentHash(), generatedDocumentInfo.getHashToken());
    }
}
