package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.UUID;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum.applicantOrApplicantSolicitor;
import static uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum.respondentOrRespondentSolicitor;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor
public class DraftAnOrderService {

    private final Time dateTime;
    private final ElementUtils elementUtils;
    private final ObjectMapper objectMapper;

    private static final String NON_MOLESTATION_ORDER = "draftAnOrder/non-molestation-order.html";
    private static final String SPECIAL_GUARDIANSHIP_ORDER = "draftAnOrder/special-guardianship-c43.html";
    private static final String BLANK_ORDER_C21 = "draftAnOrder/blank-order-c21.html";

    /*public Document generateSolicitorDraftOrder(String authorisation, CaseData caseData) throws Exception {

        GeneratedDocumentInfo generatedDocumentInfo = dgsService.generateDocument(
            authorisation,
            uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails
                .builder().caseData(caseData).build(),
            solicitorDraftAnOrder
        );
        Document document = Document.builder()
            .documentUrl(generatedDocumentInfo.getUrl())
            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
            .documentHash(generatedDocumentInfo.getHashToken())
            .documentFileName(caseData.getCreateSelectOrderOptions().getDisplayedValue() + ".pdf").build();
        return document;

    }*/

    /*public Document generateJudgeDraftOrder(String authorisation, CaseData caseData) throws Exception {

        DraftOrder selectedOrder = getSelectedDraftOrderDetails(caseData);
        GeneratedDocumentInfo generatedDocumentInfo = dgsService.generateDocument(
            authorisation,
            uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails
                .builder().caseData(caseData).build(),
            solicitorDraftAnOrder
        );
        Document document = Document.builder()
            .documentUrl(generatedDocumentInfo.getUrl())
            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
            .documentHash(generatedDocumentInfo.getHashToken())
            .documentFileName(selectedOrder.getOrderDocument().getDocumentFileName()).build();
        return document;

    }*/

    /*private String getChildNameList(CaseData caseData) {
        return FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
            ? getDaChildDetails(caseData.getApplicantChildDetails()) : getCaChildDetails(
            caseData.getChildren());
    }*/

    public Map<String, Object> generateDraftOrderCollection(CaseData caseData) {
        log.info(" ************caseData {}", caseData);
        log.info(" ************previewDraftAnOrder {}", caseData.getPreviewDraftAnOrder());
        List<Element<DraftOrder>> draftOrderList = new ArrayList<>();
        Element<DraftOrder> orderDetails = element(getCurrentOrderDetails(caseData));
        if (caseData.getDraftOrderCollection() != null) {
            draftOrderList.addAll(caseData.getDraftOrderCollection());
            draftOrderList.add(orderDetails);
        } else {
            draftOrderList.add(orderDetails);
        }
        draftOrderList.sort(Comparator.comparing(
            m -> m.getValue().getOtherDetails().getDateCreated(),
            Comparator.reverseOrder()
        ));
        return Map.of("draftOrderCollection", draftOrderList
        );
    }

    /*public Map<String, Object> updateDraftOrderCollection(CaseData caseData) {

        log.info(" ************previewDraftAnOrder {}", caseData.getPreviewDraftAnOrder());
        log.info(" ************solicitorOrJudgeDraftOrderDoc {}", caseData.getSolicitorOrJudgeDraftOrderDoc());
        log.info(" ************ casedata {}", caseData);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection = caseData.getDraftOrderCollection();
        for (Element<DraftOrder> e : caseData.getDraftOrderCollection()) {
            DraftOrder draftOrder = e.getValue();
            if (draftOrder.getOrderDocument().getDocumentFileName()
                .equalsIgnoreCase(caseData.getSolicitorOrJudgeDraftOrderDoc().getDocumentFileName())) {
                log.info("matching draftorder {}", draftOrder);
                draftOrderCollection.set(
                    draftOrderCollection.indexOf(e),
                    element(getUpdatedDraftOrder(draftOrder, caseData))
                );
                break;
            }
        }
        draftOrderCollection.sort(Comparator.comparing(
            m -> m.getValue().getOtherDetails().getDateCreated(),
            Comparator.reverseOrder()
        ));
        return Map.of("draftOrderCollection", draftOrderCollection
        );
    }*/

    /*private DraftOrder getUpdatedDraftOrder(DraftOrder draftOrder, CaseData caseData) {

        return DraftOrder.builder().orderType(draftOrder.getOrderType())
            .orderTypeId(draftOrder.getOrderTypeId())
            .orderDocument(caseData.getSolicitorOrJudgeDraftOrderDoc())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .createdBy(draftOrder.getOtherDetails().getCreatedBy())
                              .dateCreated(dateTime.now())
                              .status("Judge reviewed").build())
            .orderText(caseData.getPreviewDraftAnOrder())
            .judgeNotes(caseData.getMessageToCourtAdmin())
            .adminNotes(caseData.getCourtAdminNotes())
            .build();
    }*/

    private DraftOrder getCurrentOrderDetails(CaseData caseData) {
        return DraftOrder.builder().orderType(caseData.getSelectedOrder())
            .typeOfOrder(caseData.getSelectTypeOfOrder() != null
                             ? caseData.getSelectTypeOfOrder().getDisplayedValue() : null)
            .orderTypeId(caseData.getCreateSelectOrderOptions().getDisplayedValue())
            .orderDocument(caseData.getPreviewOrderDoc())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .createdBy(caseData.getJudgeOrMagistratesLastName())
                              .dateCreated(dateTime.now())
                              .status("Draft").build())
            .fl404CustomFields(FL404.builder().fl404bCourtName("Test").build())
            .build();
    }

    public Map<String, Object> getDraftOrderDynamicList(List<Element<DraftOrder>> draftOrderCollection) {

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
            draftOrderCollection,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));
        return caseDataMap;
    }

    /*public Map<String, Object> populateSelectedOrder(CaseData caseData) {
        Map<String, Object> caseDataMap = new HashMap<>();
        DraftOrder draftOrder = getDraftOrderDocument(caseData);
        caseDataMap.put("previewDraftOrder", draftOrder.getOrderDocument());
        if (draftOrder.getJudgeNotes() != null) {
            caseDataMap.put("instructionsFromJudge", draftOrder.getJudgeNotes());
        }
        log.info("inside populateSelectedOrder {}", caseDataMap);
        return caseDataMap;
    }


    private DraftOrder getDraftOrderDocument(CaseData caseData) {

        DraftOrder selectedOrder = getSelectedDraftOrderDetails(caseData);

        log.info("inside getDraftOrderDocument selectedOrder {}", selectedOrder.getOrderDocument());

        return selectedOrder;
    }

    public DraftOrder getSelectedDraftOrderDetails(CaseData caseData) {
        UUID orderId = elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper);
        log.info("draftOrderdynamicList {}", caseData.getDraftOrdersDynamicList());
        log.info("inside getDraftOrderDocument orderId {}", orderId);
        return caseData.getDraftOrderCollection().stream()
            .filter(element -> element.getId().equals(orderId))
            .map(Element::getValue)
            .findFirst()
            .orElseThrow(() -> new UnsupportedOperationException(String.format(
                "Could not find order")));
    }

    public Map<String, Object> removeDraftOrderAndAddToFinalOrder(String authorisation, CaseData caseData) {
        Map<String, Object> updatedCaseData = new HashMap<>();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection = caseData.getDraftOrderCollection();
        for (Element<DraftOrder> e : caseData.getDraftOrderCollection()) {
            DraftOrder draftOrder = e.getValue();
            if (draftOrder.getOrderDocument().getDocumentFileName()
                .equalsIgnoreCase(caseData.getSolicitorOrJudgeDraftOrderDoc().getDocumentFileName())) {
                updatedCaseData.put("orderCollection", getFinalOrderCollection(authorisation, caseData, draftOrder));
                draftOrderCollection.remove(
                    draftOrderCollection.indexOf(e)
                );
                break;
            }
        }

        draftOrderCollection.sort(Comparator.comparing(
            m -> m.getValue().getOtherDetails().getDateCreated(),
            Comparator.reverseOrder()
        ));
        updatedCaseData.put("draftOrderCollection", draftOrderCollection);
        return updatedCaseData;

    }

    private List<Element<OrderDetails>> getFinalOrderCollection(String auth, CaseData caseData, DraftOrder draftOrder) {

        List<Element<OrderDetails>> orderCollection;
        if (caseData.getOrderCollection() != null) {
            orderCollection = caseData.getOrderCollection();
        } else {
            orderCollection = new ArrayList<>();
        }
        orderCollection.add(convertDraftOrderToFinal(auth, caseData, draftOrder));
        orderCollection.sort(Comparator.comparing(m -> m.getValue().getDateCreated(), Comparator.reverseOrder()));
        log.info("final collection {}", orderCollection);
        return orderCollection;
    }

    private Element<OrderDetails> convertDraftOrderToFinal(String auth, CaseData caseData, DraftOrder draftOrder) {
        GeneratedDocumentInfo generatedDocumentInfo = null;
        try {
            generatedDocumentInfo = dgsService.generateDocument(
                auth,
                uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails
                    .builder().caseData(caseData).build(),
                solicitorFinalOrder
            );
        } catch (Exception e) {
            log.error("Error while generating the final document");
        }
        log.info("*************courtAdminNotes {}", caseData.getPreviewDraftAnOrder());
        log.info("***************draftorder {}", draftOrder);
        log.info("*************caseData.getSelectTypeOfOrder() {}", caseData.getSelectTypeOfOrder());
        return element(OrderDetails.builder()
                           .orderType(draftOrder.getOrderTypeId())
                           .typeOfOrder(caseData.getSelectTypeOfOrder() != null
                                            ? caseData.getSelectTypeOfOrder().getDisplayedValue() : null)
                           .orderDocument(
                               Document.builder().documentUrl(generatedDocumentInfo.getUrl())
                                   .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                   .documentHash(generatedDocumentInfo.getHashToken())
                                   .documentFileName(draftOrder.getOrderDocument().getDocumentFileName()).build())
                           .adminNotes(caseData.getCourtAdminNotes())
                           .dateCreated(draftOrder.getOtherDetails().getDateCreated())
                           .judgeNotes(draftOrder.getJudgeNotes())
                           .otherDetails(
                               OtherOrderDetails.builder().createdBy(draftOrder.getOtherDetails().getCreatedBy())
                                   .orderCreatedDate(dateTime.now().format(DateTimeFormatter.ofPattern(
                                       PrlAppsConstants.D_MMMM_YYYY,
                                       Locale.UK
                                   )))
                                   .orderMadeDate(draftOrder.getOtherDetails().getDateCreated().format(
                                       DateTimeFormatter.ofPattern(
                                           PrlAppsConstants.D_MMMM_YYYY,
                                           Locale.UK
                                       )))
                                   .orderRecipients(getAllRecipients(caseData)).build()).build());

    }*/


    private String getAllRecipients(CaseData caseData) {
        StringBuilder recipientsList = new StringBuilder();
        Optional<List<OrderRecipientsEnum>> appResRecipientList = ofNullable(caseData.getOrderRecipients());
        if (appResRecipientList.isPresent() && caseData.getOrderRecipients().contains(applicantOrApplicantSolicitor)) {
            recipientsList.append(getApplicantSolicitorDetails(caseData));
            recipientsList.append('\n');
        }
        if (appResRecipientList.isPresent()
            && caseData.getOrderRecipients().contains(respondentOrRespondentSolicitor)) {
            recipientsList.append(getRespondentSolicitorDetails(caseData));
            recipientsList.append('\n');
        }
        return recipientsList.toString();
    }


    private String getApplicantSolicitorDetails(CaseData caseData) {
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            List<PartyDetails> applicants = caseData
                .getApplicants()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            List<String> applicantSolicitorNames = applicants.stream()
                .map(party -> Objects.nonNull(party.getSolicitorOrg().getOrganisationName())
                    ? party.getSolicitorOrg().getOrganisationName() + APPLICANT_SOLICITOR
                    : APPLICANT_SOLICITOR)
                .collect(Collectors.toList());
            return String.join("\n", applicantSolicitorNames);
        } else {
            PartyDetails applicantFl401 = caseData.getApplicantsFL401();
            return applicantFl401.getRepresentativeLastName();
        }
    }

    private String getRespondentSolicitorDetails(CaseData caseData) {
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            List<PartyDetails> respondents = caseData
                .getRespondents()
                .stream()
                .map(Element::getValue)
                .filter(r -> YesNoDontKnow.yes.equals(r.getDoTheyHaveLegalRepresentation()))
                .collect(Collectors.toList());
            if (respondents.isEmpty()) {
                return "";
            }
            List<String> respondentSolicitorNames = respondents.stream()
                .map(party -> party.getSolicitorOrg().getOrganisationName() + RESPONDENT_SOLICITOR)
                .collect(Collectors.toList());
            return String.join("\n", respondentSolicitorNames);
        } else {
            PartyDetails respondentFl401 = caseData.getRespondentsFL401();
            if (YesNoDontKnow.yes.equals(respondentFl401.getDoTheyHaveLegalRepresentation())) {
                return respondentFl401.getRepresentativeFirstName()
                    + " "
                    + respondentFl401.getRepresentativeLastName();
            }
            return "";
        }
    }

    public CaseData populateCustomFields(CaseData caseData) {
        log.info("inside populateCustomFields {}", caseData.getCreateSelectOrderOptions());
        switch (caseData.getCreateSelectOrderOptions()) {
            case blankOrderOrDirections:
                return null;
            case nonMolestation:
                return null;
            default:
                return caseData;

        }
    }

    public Map<String, Object> populateDraftOrderFields(CaseData caseData) {
        Map<String, Object> caseDataMap = new HashMap<>();
        DraftOrder selectedOrder = getSelectedDraftOrderDetails(caseData);
        caseDataMap.put("previewDraftOrder", selectedOrder.getOrderDocument());
        return caseDataMap;
    }


    public DraftOrder getSelectedDraftOrderDetails(CaseData caseData) {
        UUID orderId = elementUtils.getDynamicListSelectedValue(
            caseData.getDraftOrdersDynamicList(), objectMapper);
        log.info("draftOrderdynamicList {}", caseData.getDraftOrdersDynamicList());
        log.info("inside getDraftOrderDocument orderId {}", orderId);
        return caseData.getDraftOrderCollection().stream()
            .filter(element -> element.getId().equals(orderId))
            .map(Element::getValue)
            .findFirst()
            .orElseThrow(() -> new UnsupportedOperationException(String.format(
                "Could not find order")));
    }
}
