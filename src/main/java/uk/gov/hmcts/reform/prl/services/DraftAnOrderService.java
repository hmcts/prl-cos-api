package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME;
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
    private final ManageOrderService manageOrderService;
    private final DgsService dgsService;

    private static final String NON_MOLESTATION_ORDER = "draftAnOrder/non-molestation-order.html";
    private static final String SPECIAL_GUARDIANSHIP_ORDER = "draftAnOrder/special-guardianship-c43.html";
    private static final String BLANK_ORDER_C21 = "draftAnOrder/blank-order-c21.html";

    public Map<String, Object> generateDraftOrderCollection(CaseData caseData) {
        List<Element<DraftOrder>> draftOrderList = new ArrayList<>();
        Element<DraftOrder> orderDetails = element(getCurrentOrderDetails(caseData));
        log.info("current order details {}", orderDetails);
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

    private DraftOrder getCurrentOrderDetails(CaseData caseData) {
        log.info(" Getting current order details from case data {}", caseData);
        return DraftOrder.builder().orderType(caseData.getCreateSelectOrderOptions())
            .typeOfOrder(caseData.getSelectTypeOfOrder() != null
                             ? caseData.getSelectTypeOfOrder().getDisplayedValue() : null)
            .orderTypeId(caseData.getCreateSelectOrderOptions().getDisplayedValue())
            .orderDocument(caseData.getPreviewOrderDoc())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .createdBy(caseData.getJudgeOrMagistratesLastName())
                              .dateCreated(dateTime.now())
                              .status("Draft").build())
            .isTheOrderByConsent(caseData.getManageOrders().getIsTheOrderByConsent())
            .wasTheOrderApprovedAtHearing(caseData.getWasTheOrderApprovedAtHearing())
            .judgeOrMagistrateTitle(caseData.getManageOrders().getJudgeOrMagistrateTitle())
            .judgeOrMagistratesLastName(caseData.getJudgeOrMagistratesLastName())
            .justiceLegalAdviserFullName(caseData.getJusticeLegalAdviserFullName())
            .magistrateLastName(caseData.getMagistrateLastName())
            .recitalsOrPreamble(caseData.getManageOrders().getRecitalsOrPreamble())
            .isTheOrderAboutAllChildren(caseData.getIsTheOrderAboutAllChildren())
            .orderDirections(caseData.getManageOrders().getOrderDirections())
            .furtherDirectionsIfRequired(caseData.getManageOrders().getFurtherDirectionsIfRequired())
            .fl404CustomFields(caseData.getManageOrders().getFl404CustomFields())
            .build();
    }

    public Map<String, Object> getDraftOrderDynamicList(List<Element<DraftOrder>> draftOrderCollection, String caseTypeOfApplication) {

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("draftOrdersDynamicList", ElementUtils.asDynamicList(
            draftOrderCollection,
            null,
            DraftOrder::getLabelForOrdersDynamicList
        ));
        caseDataMap.put("caseTypeOfApplication", caseTypeOfApplication);
        return caseDataMap;
    }

    public Map<String, Object> removeDraftOrderAndAddToFinalOrder(String authorisation, CaseData caseData) {
        Map<String, Object> updatedCaseData = new HashMap<>();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection = caseData.getDraftOrderCollection();
        for (Element<DraftOrder> e : caseData.getDraftOrderCollection()) {
            DraftOrder draftOrder = e.getValue();
            if (draftOrder.getOrderDocument().getDocumentFileName()
                .equalsIgnoreCase(caseData.getPreviewOrderDoc().getDocumentFileName())) {
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
        log.info("draftOrder.getOrderType************ {}", draftOrder.getOrderType());
        Map<String, String> fieldMap = manageOrderService.getOrderTemplateAndFile(draftOrder.getOrderType());
        GeneratedDocumentInfo generatedDocumentInfo = null;
        try {
            generatedDocumentInfo = dgsService.generateDocument(
                auth,
                CaseDetails.builder().caseData(caseData).build(),
                fieldMap.get(PrlAppsConstants.FINAL_TEMPLATE_NAME)
            );
        } catch (Exception e) {
            log.error(
                "Error while generating the final document for case {} and  order {}",
                caseData.getId(),
                draftOrder.getOrderType()
            );
        }

        return element(OrderDetails.builder()
                           .orderType(draftOrder.getOrderTypeId())
                           .typeOfOrder(caseData.getSelectTypeOfOrder() != null
                                            ? caseData.getSelectTypeOfOrder().getDisplayedValue() : null)
                           .doesOrderClosesCase(caseData.getDoesOrderClosesCase())
                           .orderDocument(
                               Document.builder().documentUrl(generatedDocumentInfo.getUrl())
                                   .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                   .documentHash(generatedDocumentInfo.getHashToken())
                                   .documentFileName(fieldMap.get(PrlAppsConstants.GENERATE_FILE_NAME)).build())
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

    }


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

    public Map<String, Object> populateDraftOrderDocument(CaseData caseData) {
        Map<String, Object> caseDataMap = new HashMap<>();
        DraftOrder selectedOrder = getSelectedDraftOrderDetails(caseData);
        caseDataMap.put("previewDraftOrder", selectedOrder.getOrderDocument());
        if (selectedOrder.getJudgeNotes() != null) {
            caseDataMap.put("instructionsFromJudge", selectedOrder.getJudgeNotes());
        }
        return caseDataMap;
    }

    public Map<String, Object> populateDraftOrderCustomFields(CaseData caseData) {
        Map<String, Object> caseDataMap = new HashMap<>();
        DraftOrder selectedOrder = getSelectedDraftOrderDetails(caseData);
        caseDataMap.put("fl404CustomFields", selectedOrder.getFl404CustomFields());
        return caseDataMap;
    }

    public Map<String, Object> populateCommonDraftOrderFields(CaseData caseData) {
        Map<String, Object> caseDataMap = new HashMap<>();
        DraftOrder selectedOrder = getSelectedDraftOrderDetails(caseData);
        caseDataMap.put("isTheOrderByConsent", selectedOrder.getIsTheOrderByConsent());
        caseDataMap.put("wasTheOrderApprovedAtHearing", selectedOrder.getWasTheOrderApprovedAtHearing());
        caseDataMap.put("judgeOrMagistrateTitle", selectedOrder.getJudgeOrMagistrateTitle());
        caseDataMap.put("judgeOrMagistratesLastName", selectedOrder.getJudgeOrMagistratesLastName());
        caseDataMap.put("justiceLegalAdviserFullName", selectedOrder.getJusticeLegalAdviserFullName());
        caseDataMap.put("magistrateLastName", selectedOrder.getMagistrateLastName());
        caseDataMap.put("isTheOrderAboutAllChildren", selectedOrder.getIsTheOrderAboutAllChildren());
        caseDataMap.put("recitalsOrPreamble", selectedOrder.getRecitalsOrPreamble());
        caseDataMap.put("orderDirections", selectedOrder.getOrderDirections());
        caseDataMap.put("furtherDirectionsIfRequired", selectedOrder.getFurtherDirectionsIfRequired());
        log.info("Common fields map {}", caseDataMap);
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


    public Map<String, Object> updateDraftOrderCollection(CaseData caseData) {

        log.info(" ************previewDraftAnOrder {}", caseData.getPreviewDraftAnOrder());
        log.info(" ************ casedata {}", caseData);
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();
        draftOrderCollection = caseData.getDraftOrderCollection();
        for (Element<DraftOrder> e : caseData.getDraftOrderCollection()) {
            DraftOrder draftOrder = e.getValue();
            if (draftOrder.getOrderDocument().getDocumentFileName()
                .equalsIgnoreCase(caseData.getPreviewOrderDoc().getDocumentFileName())) {
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
    }

    private DraftOrder getUpdatedDraftOrder(DraftOrder draftOrder, CaseData caseData) {

        return DraftOrder.builder().orderType(draftOrder.getOrderType())
            .typeOfOrder(caseData.getSelectTypeOfOrder() != null
                             ? caseData.getSelectTypeOfOrder().getDisplayedValue() : null)
            .orderTypeId(caseData.getCreateSelectOrderOptions().getDisplayedValue())
            .orderDocument(caseData.getPreviewOrderDoc())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .createdBy(caseData.getJudgeOrMagistratesLastName())
                              .dateCreated(dateTime.now())
                              .status("Judge reviewed").build())
            .isTheOrderByConsent(caseData.getManageOrders().getIsTheOrderByConsent())
            .wasTheOrderApprovedAtHearing(caseData.getWasTheOrderApprovedAtHearing())
            .judgeOrMagistrateTitle(caseData.getManageOrders().getJudgeOrMagistrateTitle())
            .judgeOrMagistratesLastName(caseData.getJudgeOrMagistratesLastName())
            .justiceLegalAdviserFullName(caseData.getJusticeLegalAdviserFullName())
            .magistrateLastName(caseData.getMagistrateLastName())
            .recitalsOrPreamble(caseData.getManageOrders().getRecitalsOrPreamble())
            .isTheOrderAboutAllChildren(caseData.getIsTheOrderAboutAllChildren())
            .orderDirections(caseData.getManageOrders().getOrderDirections())
            .furtherDirectionsIfRequired(caseData.getManageOrders().getFurtherDirectionsIfRequired())
            .fl404CustomFields(caseData.getManageOrders().getFl404CustomFields())
            .judgeNotes(caseData.getJudgeDirectionsToAdmin())
            .build();
    }

    public CaseData generateDocument(@RequestBody CallbackRequest callbackRequest, CaseData caseData) {
        if (callbackRequest
            .getCaseDetailsBefore() != null && callbackRequest
            .getCaseDetailsBefore().getData().get(COURT_NAME) != null) {
            caseData.setCourtName(callbackRequest
                                      .getCaseDetailsBefore().getData().get(COURT_NAME).toString());
        }
        log.info("Case data {}", caseData);
        log.info("Case data before prepopulate: {}", caseData.getManageOrders().getFl404CustomFields());
        FL404 fl404CustomFields = caseData.getManageOrders().getFl404CustomFields();
        fl404CustomFields = fl404CustomFields.toBuilder().fl404bApplicantName(String.format(
            PrlAppsConstants.FORMAT,
            caseData.getApplicantsFL401().getFirstName(),
            caseData.getApplicantsFL401().getLastName()
        ))
            .fl404bRespondentName(String.format(PrlAppsConstants.FORMAT, caseData.getRespondentsFL401().getFirstName(),
                                                caseData.getRespondentsFL401().getLastName()
            )).build();
        if (ofNullable(caseData.getRespondentsFL401().getAddress()).isPresent()) {
            fl404CustomFields = fl404CustomFields.toBuilder()
                .fl404bRespondentAddress(caseData.getRespondentsFL401().getAddress()).build();
        }
        if (ofNullable(caseData.getRespondentsFL401().getDateOfBirth()).isPresent()) {
            fl404CustomFields = fl404CustomFields.toBuilder()
                .fl404bRespondentDob(caseData.getRespondentsFL401().getDateOfBirth()).build();
        }
        caseData = caseData.toBuilder()
            .manageOrders(ManageOrders.builder()
                              .recitalsOrPreamble(caseData.getManageOrders().getRecitalsOrPreamble())
                              .isCaseWithdrawn(caseData.getManageOrders().getIsCaseWithdrawn())
                              .isTheOrderByConsent(caseData.getManageOrders().getIsTheOrderByConsent())
                              .judgeOrMagistrateTitle(caseData.getManageOrders().getJudgeOrMagistrateTitle())
                              .isOrderDrawnForCafcass(caseData.getManageOrders().getIsOrderDrawnForCafcass())
                              .orderDirections(caseData.getManageOrders().getOrderDirections())
                              .furtherDirectionsIfRequired(caseData.getManageOrders().getFurtherDirectionsIfRequired())
                              .fl404CustomFields(fl404CustomFields)
                              .build()).build();
        log.info("Case data after prepopulate: {}", caseData.getManageOrders().getFl404CustomFields());
        return caseData;
    }
}
