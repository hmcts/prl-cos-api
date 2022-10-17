package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum.applicantOrApplicantSolicitor;
import static uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum.respondentOrRespondentSolicitor;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ResourceReader.readString;

@Slf4j
@Service
@RequiredArgsConstructor
public class DraftAnOrderService {

    private static final String NOTICE_TEXT = "If the respondent intends to oppose to the order at the next hearing, "
        + "they must notify the court in advance that they intend to attend the hearing and oppose the order.If the "
        + "respondent does not notify the court, the court may decide that the applicant or applicant’s solicitor does"
        + "not need to attend the next hearing, and at the next hearing may make an order to extend the injunction.";
    @Value("${document.templates.solicitor.prl_solicitor_draft_an_order_template}")
    String solicitorDraftAnOrder;

    @Value("${document.templates.solicitor.prl_solicitor_final_order_template}")
    String solicitorFinalOrder;

    private final DgsService dgsService;
    private final Time dateTime;
    private final ElementUtils elementUtils;
    private final ObjectMapper objectMapper;

    private static final String NON_MOLESTATION_ORDER = "draftAnOrder/non-molestation-order.html";
    private static final String BLANK_ORDER_C21 = "draftAnOrder/blank-order-c21.html";

    public Document generateSolicitorDraftOrder(String authorisation, CaseData caseData) throws Exception {

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

    }

    public Document generateJudgeDraftOrder(String authorisation, CaseData caseData) throws Exception {

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

    }

    public String getTheOrderDraftString(CaseData caseData) {
        String temp = null;
        switch (caseData.getCreateSelectOrderOptions()) {
            case nonMolestation:
                temp = getNonMolestationString(readString(NON_MOLESTATION_ORDER), caseData);
                break;

            default:
                break;

        }
        return temp;
    }

    private String getNonMolestationString(String nonMolestationOrderString, CaseData caseData) {
        Map<String, String> nonMolestationPlaceHoldersMap = new HashMap<>();
        if (nonMolestationOrderString != null) {

            nonMolestationPlaceHoldersMap.put(
                "familyManNumber", caseData.getFamilymanCaseNumber() != null ? caseData.getFamilymanCaseNumber() : " "
            );

            nonMolestationPlaceHoldersMap.put("ccdId", String.valueOf(caseData.getId()));

            nonMolestationPlaceHoldersMap.put(
                "orderDate",
                caseData.getDateOrderMade() != null ? caseData.getDateOrderMade().format(DateTimeFormatter.ofPattern(
                    PrlAppsConstants.D_MMMM_YYYY,
                    Locale.UK
                )) : " "
            );
            nonMolestationPlaceHoldersMap.put(
                "judgeOrMagistrateTitle",
                caseData.getManageOrders().getJudgeOrMagistrateTitle() != null
                    ? caseData.getManageOrders().getJudgeOrMagistrateTitle().getDisplayedValue() : " "
            );
            nonMolestationPlaceHoldersMap.put("judgeOrMagistratesLastName", caseData.getJudgeOrMagistratesLastName());
            nonMolestationPlaceHoldersMap.put("justiceLegalAdviserFullName", caseData.getJusticeLegalAdviserFullName());
            nonMolestationPlaceHoldersMap.put(
                "familyCourtName",
                (caseData.getManageOrders().getFl404CustomFields() != null
                    && caseData.getManageOrders().getFl404CustomFields().getFl404bCourtName() != null)
                    ? caseData.getManageOrders().getFl404CustomFields().getFl404bCourtName() : " "
            );
            nonMolestationPlaceHoldersMap.put(
                "fl401ApplicantName",
                (caseData.getManageOrders().getFl404CustomFields() != null
                    && caseData.getManageOrders().getFl404CustomFields().getFl404bApplicantName() != null)
                    ? caseData.getManageOrders().getFl404CustomFields().getFl404bApplicantName() : " "
            );
            nonMolestationPlaceHoldersMap.put(
                "fl404bApplicantReference",
                (caseData.getManageOrders().getFl404CustomFields() != null
                    && caseData.getManageOrders().getFl404CustomFields().getFl404bApplicantReference() != null)
                    ? caseData.getManageOrders().getFl404CustomFields().getFl404bApplicantReference() : " "
            );
            nonMolestationPlaceHoldersMap.put(
                "fl404bRespondentName",
                (caseData.getManageOrders().getFl404CustomFields() != null
                    && caseData.getManageOrders().getFl404CustomFields().getFl404bRespondentName() != null)
                    ? caseData.getManageOrders().getFl404CustomFields().getFl404bRespondentName() : " "
            );
            nonMolestationPlaceHoldersMap.put(
                "fl404bRespondentDob",
                (caseData.getManageOrders().getFl404CustomFields() != null
                    && caseData.getManageOrders().getFl404CustomFields().getFl404bRespondentDob() != null)
                    ? caseData.getManageOrders().getFl404CustomFields().getFl404bRespondentDob().format(
                    DateTimeFormatter.ofPattern(
                        PrlAppsConstants.D_MMMM_YYYY,
                        Locale.UK
                    )) : " "
            );
            nonMolestationPlaceHoldersMap.put(
                "fl404bRespondentReference",
                (caseData.getManageOrders().getFl404CustomFields() != null
                    && caseData.getManageOrders().getFl404CustomFields().getFl404bRespondentReference() != null)
                    ? caseData.getManageOrders().getFl404CustomFields().getFl404bRespondentReference() : " "
            );

            nonMolestationPlaceHoldersMap.put(
                "applicantChildNameDob", getApplicantChildDetails(caseData.getApplicantChildDetails())
            );
            nonMolestationPlaceHoldersMap.put(
                "fl404bRespondentAddress",
                getAddress(
                    caseData.getRespondentsFL401() != null
                        ? caseData.getRespondentsFL401().getAddress() : null)
            );
            nonMolestationPlaceHoldersMap.put(
                "fl404bMentionedProperty", (caseData.getManageOrders().getFl404CustomFields() != null
                    && caseData.getManageOrders().getFl404CustomFields()
                    .getFl404bMentionedProperty() != null && caseData.getManageOrders().getFl404CustomFields()
                    .getFl404bMentionedProperty().equalsIgnoreCase("Yes"))
                    ? "The property in this order is: " : " ");
            nonMolestationPlaceHoldersMap.put(
                "fl404bAddressOfProperty", (caseData.getManageOrders().getFl404CustomFields() != null
                    && caseData.getManageOrders().getFl404CustomFields().getFl404bAddressOfProperty() != null)
                    ? caseData.getManageOrders().getFl404CustomFields().getFl404bAddressOfProperty() : " "
            );
            nonMolestationPlaceHoldersMap.put(
                "recitalsOrPreamble", caseData.getRecitalsOrPreamble() != null
                    ? caseData.getRecitalsOrPreamble() : " "
            );
            nonMolestationPlaceHoldersMap.put(
                "isTheOrderByConsent",
                (caseData.getIsTheOrderByConsent() != null
                    && caseData.getIsTheOrderByConsent().equals(
                    YesOrNo.Yes) ? "By consent" : " "
                )
            );
            nonMolestationPlaceHoldersMap.put(
                "respondentMustNotOrders", getRespondentMustNotOrders(caseData.getManageOrders().getFl404CustomFields())
            );
            nonMolestationPlaceHoldersMap.put(
                "furtherDirectionsIfRequired", caseData.getManageOrders().getFurtherDirectionsIfRequired() != null
                    ? caseData.getManageOrders().getFurtherDirectionsIfRequired() : " "
            );
            nonMolestationPlaceHoldersMap.put(
                "dateOrderEnds",
                (caseData.getManageOrders().getFl404CustomFields() != null
                    && caseData.getManageOrders().getFl404CustomFields().getFl404bDateOrderEnd() != null)
                    ? caseData.getManageOrders().getFl404CustomFields().getFl404bDateOrderEnd() : " "
            );
            nonMolestationPlaceHoldersMap.put(
                "dateOrderEndTime",
                (caseData.getManageOrders().getFl404CustomFields() != null
                    && caseData.getManageOrders().getFl404CustomFields().getFl404bDateOrderEndTime() != null)
                    ? caseData.getManageOrders().getFl404CustomFields().getFl404bDateOrderEndTime() : " "
            );

            nonMolestationPlaceHoldersMap.put(
                "WithoutNotice",
                (caseData.getManageOrders().getFl404CustomFields() != null
                    && caseData.getManageOrders().getFl404CustomFields().getFl404bIsNoticeGiven() != null
                    && caseData.getManageOrders().getFl404CustomFields().getFl404bIsNoticeGiven().equalsIgnoreCase(
                    "WithoutNotice"))
                    ? NOTICE_TEXT : " "
            );
            nonMolestationPlaceHoldersMap.put(
                "fl404bDateOfNextHearing",
                (caseData.getManageOrders().getFl404CustomFields() != null
                    && caseData.getManageOrders().getFl404CustomFields().getFl404bDateOfNextHearing() != null)
                    ? caseData.getManageOrders().getFl404CustomFields().getFl404bDateOfNextHearing() : " "
            );
            nonMolestationPlaceHoldersMap.put(
                "fl404bTimeOfNextHearing",
                (caseData.getManageOrders().getFl404CustomFields() != null
                    && caseData.getManageOrders().getFl404CustomFields().getFl404bTimeOfNextHearing() != null)
                    ? caseData.getManageOrders().getFl404CustomFields().getFl404bTimeOfNextHearing() : " "
            );

            nonMolestationPlaceHoldersMap.put(
                "fl404bOtherCourtName1",
                (caseData.getManageOrders().getFl404CustomFields() != null
                    && caseData.getManageOrders().getFl404CustomFields().getFl404bCourtName1() != null)
                    ? caseData.getManageOrders().getFl404CustomFields().getFl404bCourtName1() : " "
            );

            nonMolestationPlaceHoldersMap.put(
                "fl404bOtherCourtAddress",
                (caseData.getManageOrders().getFl404CustomFields() != null
                    && caseData.getManageOrders().getFl404CustomFields().getFl404bOtherCourtAddress() != null)
                    ? getAddress(caseData.getManageOrders().getFl404CustomFields().getFl404bOtherCourtAddress()) : " "
            );

            nonMolestationPlaceHoldersMap.put(
                "fl404bTimeEstimate",
                (caseData.getManageOrders().getFl404CustomFields() != null
                    && caseData.getManageOrders().getFl404CustomFields().getFl404bTimeEstimate() != null)
                    ? caseData.getManageOrders().getFl404CustomFields().getFl404bTimeEstimate() : " "
            );

            nonMolestationPlaceHoldersMap.put(
                "fl404bCostOfApplication",
                (caseData.getManageOrders().getFl404CustomFields() != null
                    && caseData.getManageOrders().getFl404CustomFields().getFl404bCostOfApplication() != null)
                    ? caseData.getManageOrders().getFl404CustomFields().getFl404bCostOfApplication() : " "
            );

            nonMolestationPlaceHoldersMap.put(
                "fl404bWithoutNotice",
                (caseData.getManageOrders().getFl404CustomFields() != null
                    && caseData.getManageOrders().getFl404CustomFields().getFl404bIsNoticeGiven() != null
                    && caseData.getManageOrders().getFl404CustomFields().getFl404bIsNoticeGiven().equalsIgnoreCase(
                    "WithoutNotice"))
                    ? "out" : ""
            );

            StringSubstitutor substitutor = new StringSubstitutor(nonMolestationPlaceHoldersMap);
            return substitutor.replace(nonMolestationOrderString);
        }
        return null;
    }

    private String getRespondentMustNotOrders(FL404 fl404CustomFields) {
        StringBuilder builder = new StringBuilder();
        if (fl404CustomFields != null) {
            builder.append("<ol>");
            if (fl404CustomFields.getFl404bRespondentNotToThreat().size() > 0) {
                builder.append("<li>");
                builder.append("The respondent "
                                   + fl404CustomFields.getFl404bRespondentName()
                                   + " must not use or threaten violence against the applicant "
                                   + fl404CustomFields.getFl404bApplicantName() + ", and must not instruct, encourage "
                                   + "or in any way suggest that any other person should do so.");
                builder.append("</li>");
            }
            if (fl404CustomFields.getFl404bRespondentNotIntimidate().size() > 0) {
                builder.append("<li>");
                builder.append("The respondent " + fl404CustomFields.getFl404bRespondentName()
                                   + " must not intimidate, harass or pester the applicant"
                                   + fl404CustomFields.getFl404bApplicantName() + ", and must not instruct, "
                                   + "encourage or in any way suggest that any other person should do so.");
                builder.append("</li>");
            }
            if (fl404CustomFields.getFl404bRespondentNotToTelephone().size() > 0) {
                builder.append("<li>");
                builder.append("The respondent " + fl404CustomFields.getFl404bRespondentName()
                                   + " must not telephone, text, email or otherwise contact or attempt to contact the "
                                   + "applicant " + fl404CustomFields.getFl404bApplicantName() + ".");
                if (fl404CustomFields.getFl404bAddMoreDetailsTelephone() != null) {
                    builder.append(fl404CustomFields.getFl404bAddMoreDetailsTelephone());
                }
                builder.append("</li>");
            }
            if (fl404CustomFields.getFl404bRespondentNotToDamageOrThreat().size() > 0) {
                builder.append("<li>");
                builder.append("The respondent " + fl404CustomFields.getFl404bRespondentName()
                                   + " must not damage, attempt to damage or threaten to damage any property owned by "
                                   + "or in the possession or control of the applicant "
                                   + fl404CustomFields.getFl404bApplicantName() + ", and must not instruct, "
                                   + "encourage or in any way suggest that any other person should do so");
                builder.append("</li>");
            }
            if (fl404CustomFields.getFl404bRespondentNotToDamage().size() > 0) {
                builder.append("<li>");
                builder.append("The respondent " + fl404CustomFields.getFl404bRespondentName()
                                   + " must not damage, attempt to damage or threaten to damage the property or "
                                   + "contents of the property at " + fl404CustomFields.getFl404bAddressOfProperty()
                                   + " , and must not instruct, encourage or in any way suggest that any other "
                                   + "person should do so.");
                builder.append("</li>");
            }
            if (fl404CustomFields.getFl404bRespondentNotToEnterProperty().size() > 0) {
                builder.append("<li>");
                builder.append("The respondent " + fl404CustomFields.getFl404bRespondentName()
                                   + " must not go to, enter or attempt to enter the property at "
                                   + fl404CustomFields.getFl404bAddressOfProperty() + " ,"
                                   + fl404CustomFields.getFl404bAddMoreDetailsProperty() + " .");
                builder.append("</li>");
            }
            if (fl404CustomFields.getFl404bRespondentNotToThreatChild().size() > 0) {
                builder.append("<li>");
                builder.append("The respondent " + fl404CustomFields.getFl404bRespondentName()
                                   + " must not use or threaten violence against the relevant children, and must not "
                                   + "instruct, encourage or in any way suggest that any other person should do so.");
                builder.append("</li>");
            }
            if (fl404CustomFields.getFl404bRespondentNotHarassOrIntimidate().size() > 0) {
                builder.append("<li>");
                builder.append("The respondent " + fl404CustomFields.getFl404bRespondentName()
                                   + " must not intimidate, harass or pester the relevant children, and"
                                   + " must not instruct, encourage or in any way suggest that any "
                                   + "other person should do so.");
                builder.append("</li>");
            }
            if (fl404CustomFields.getFl404bRespondentNotToTelephoneChild().size() > 0) {
                builder.append("<li>");
                builder.append("The respondent " + fl404CustomFields.getFl404bRespondentName()
                                   + " must not telephone, text, email or otherwise contact or attempt to contact the "
                                   + "relevant children " + fl404CustomFields.getFl404bAddMoreDetailsPhoneChild());
                builder.append("</li>");
            }
            if (fl404CustomFields.getFl404bRespondentNotToEnterSchool().size() > 0) {
                builder.append("<li>");
                builder.append("The respondent " + fl404CustomFields.getFl404bRespondentName()
                                   + " must not go to, enter or attempt to the school known as "
                                   + fl404CustomFields.getFl404bAddSchool() + " and "
                                   + fl404CustomFields.getFl404bAddMoreDetailsSchool());
                builder.append("</li>");
            }
            builder.append("</ol>");
        }
        return builder.toString();
    }

    private String getAddress(Address address) {
        StringBuilder builder = new StringBuilder();
        if (address != null) {
            builder.append(address.getAddressLine1());
            builder.append(address.getPostTown() != null ? "\n" : "");
            builder.append(address.getPostTown());
            builder.append(address.getPostCode() != null ? "\n" : "");
            builder.append(address.getPostCode());
            builder.append("\n");
        }
        return builder.toString();
    }

    private String getApplicantChildDetails(List<Element<ApplicantChild>> applicantChildDetails) {
        Optional<List<Element<ApplicantChild>>> appChildDetails = ofNullable(applicantChildDetails);
        StringBuilder builder = new StringBuilder();
        if (appChildDetails.isPresent()) {
            List<ApplicantChild> children = appChildDetails.get().stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
            for (int i = 0; i < children.size(); i++) {
                ApplicantChild child = children.get(i);
                builder.append(String.format(
                    "Child : %s  born %s",
                    child.getFullName(),
                    child.getDateOfBirth() != null ? child.getDateOfBirth().format(DateTimeFormatter.ofPattern(
                        PrlAppsConstants.D_MMMM_YYYY,
                        Locale.UK
                    )) : " "
                ));
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    public Map<String, Object> generateDraftOrderCollection(CaseData caseData) {

        log.info(" ************previewDraftAnOrder {}", caseData.getPreviewDraftAnOrder());
        log.info(" ************solicitorOrJudgeDraftOrderDoc {}", caseData.getSolicitorOrJudgeDraftOrderDoc());
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

    public Map<String, Object> updateDraftOrderCollection(CaseData caseData) {

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
    }

    private DraftOrder getUpdatedDraftOrder(DraftOrder draftOrder, CaseData caseData) {

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
    }

    private DraftOrder getCurrentOrderDetails(CaseData caseData) {
        return DraftOrder.builder().orderType(caseData.getSelectedOrder())
            .typeOfOrder(caseData.getSelectTypeOfOrder() != null
                             ? caseData.getSelectTypeOfOrder().getDisplayedValue() : null)
            .orderTypeId(caseData.getCreateSelectOrderOptions().getDisplayedValue())
            .orderDocument(caseData.getSolicitorOrJudgeDraftOrderDoc())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .createdBy(caseData.getJudgeOrMagistratesLastName())
                              .dateCreated(dateTime.now())
                              .status("Draft").build())
            .orderText(caseData.getPreviewDraftAnOrder())
            .judgeNotes(caseData.getMessageToCourtAdmin())
            .adminNotes(caseData.getCourtAdminNotes())
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

    public Map<String, Object> populateSelectedOrder(CaseData caseData) {
        Map<String, Object> caseDataMap = new HashMap<>();
        DraftOrder draftOrder = getDraftOrderDocument(caseData);
        caseDataMap.put("previewDraftOrder", draftOrder.getOrderDocument());
        if (draftOrder.getJudgeNotes() != null) {
            caseDataMap.put("instructionsFromJudge", draftOrder.getJudgeNotes());
        }
        log.info("inside populateSelectedOrder {}", caseDataMap);
        return caseDataMap;
    }


    public Map<String, Object> populateSelectedOrderText(CaseData caseData) {
        log.info("inside populateSelectedOrderText caseData {}", caseData);
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("previewDraftAnOrder", getDraftOrderText(caseData));
        log.info("inside populateSelectedOrderText {}", caseDataMap);
        return caseDataMap;
    }

    private String getDraftOrderText(CaseData caseData) {

        DraftOrder selectedOrder = getSelectedDraftOrderDetails(caseData);

        log.info("inside getDraftOrderDocument selectedOrder {}", selectedOrder.getOrderText());
        return selectedOrder.getOrderText();

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
            case blank:
                return caseData.toBuilder().previewDraftAnOrder(getBlankOrderString(
                    readString(NON_MOLESTATION_ORDER),
                    caseData
                )).build();
            case nonMolestation:
                return populateFl404Fields(caseData);
            default:
                return caseData;

        }
    }

    public CaseData populateFl404Fields(CaseData caseData) {
        FL404 orderData = FL404.builder()
            .fl404bCaseNumber(String.valueOf(caseData.getId()))
            .fl404bCourtName(caseData.getCourtName())
            .fl404bApplicantName(String.format(PrlAppsConstants.FORMAT, caseData.getApplicantsFL401().getFirstName(),
                                               caseData.getApplicantsFL401().getLastName()
            ))
            .fl404bRespondentName(String.format(PrlAppsConstants.FORMAT, caseData.getRespondentsFL401().getFirstName(),
                                                caseData.getRespondentsFL401().getLastName()
            ))
            .build();

        log.info("FL404b court name: {}", orderData.getFl404bCourtName());

        if (ofNullable(caseData.getRespondentsFL401().getAddress()).isPresent()) {
            orderData = orderData.toBuilder().fl404bRespondentAddress(caseData.getRespondentsFL401()
                                                                          .getAddress()).build();
        }
        if (ofNullable(caseData.getRespondentsFL401().getDateOfBirth()).isPresent()) {
            orderData = orderData.toBuilder().fl404bRespondentDob(caseData.getRespondentsFL401()
                                                                      .getDateOfBirth()).build();
        }
        return caseData.toBuilder().manageOrders(ManageOrders.builder()
                                                     .fl404CustomFields(orderData)
                                                     .build())
            .selectedOrder(caseData.getCreateSelectOrderOptions().getDisplayedValue()).build();
    }

    public String getBlankOrderString(String blankOrderString, CaseData caseData) {
        log.info("Preparing blank order string {}", blankOrderString);
        Map<String, String> blankOrderPlaceHoldersMap = new HashMap<>();
        if (blankOrderString != null) {

            blankOrderPlaceHoldersMap.put(
                "familyManNumber", caseData.getFamilymanCaseNumber() != null ? caseData.getFamilymanCaseNumber() : " "
            );

            blankOrderPlaceHoldersMap.put("ccdId", String.valueOf(caseData.getId()));

            blankOrderPlaceHoldersMap.put(
                "orderDate",
                caseData.getDateOrderMade() != null ? caseData.getDateOrderMade().format(DateTimeFormatter.ofPattern(
                    PrlAppsConstants.D_MMMM_YYYY,
                    Locale.UK
                )) : " "
            );
            blankOrderPlaceHoldersMap.put(
                "judgeOrMagistrateTitle",
                caseData.getManageOrders().getJudgeOrMagistrateTitle() != null
                    ? caseData.getManageOrders().getJudgeOrMagistrateTitle().getDisplayedValue() : " "
            );
            blankOrderPlaceHoldersMap.put("judgeOrMagistratesLastName", caseData.getJudgeOrMagistratesLastName());
            blankOrderPlaceHoldersMap.put("justiceLegalAdviserFullName", caseData.getJusticeLegalAdviserFullName());
            StringSubstitutor substitutor = new StringSubstitutor(blankOrderPlaceHoldersMap);
            return substitutor.replace(blankOrderString);
        } else {
            return null;
        }
    }
}
