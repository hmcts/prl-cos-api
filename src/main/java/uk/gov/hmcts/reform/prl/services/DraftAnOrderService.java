package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.time.Time;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ResourceReader.readString;

@Service
@RequiredArgsConstructor
public class DraftAnOrderService {

    private static final String NOTICE_TEXT = "If the respondent intends to oppose to the order at the next hearing, "
        + "they must notify the court in advance that they intend to attend the hearing and oppose the order.If the "
        + "respondent does not notify the court, the court may decide that the applicant or applicant’s solicitor does"
        + "not need to attend the next hearing, and at the next hearing may make an order to extend the injunction.";
    @Value("${document.templates.common.prl_solicitor_draft_an_order_template}")
    String solicitorDraftAnOrder;

    private final DgsService dgsService;
    private final Time dateTime;

    private static final String NON_MOLESTATION_ORDER = "draftAnOrder/non-molestation-order.html";

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
                caseData.getDateOrderMade() != null ? caseData.getDateOrderMade().toString() : " "
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
                    ? caseData.getManageOrders().getFl404CustomFields().getFl404bRespondentDob().toString() : " "
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
                "fl404bRespondentAddress", getAddress(caseData.getRespondentsFL401().getAddress())
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
                    child.getDateOfBirth().toString()
                ));
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    public Map<String, Object> generateDraftOrderCollection(CaseData caseData) {
        List<Element<OrderDetails>> uploadedDraftOrderList;
        Element<OrderDetails> orderDetails = element(getCurrentOrderDetails(caseData));
        if (caseData.getDraftOrderCollection() != null) {
            uploadedDraftOrderList = caseData.getDraftOrderCollection();
            uploadedDraftOrderList.add(orderDetails);
        } else {
            uploadedDraftOrderList = new ArrayList<>();
            uploadedDraftOrderList.add(orderDetails);
        }
        uploadedDraftOrderList.sort(Comparator.comparing(
            m -> m.getValue().getDateCreated(),
            Comparator.reverseOrder()
        ));
        return Map.of("draftOrderCollection", uploadedDraftOrderList);
    }

    private OrderDetails getCurrentOrderDetails(CaseData caseData) {
        return OrderDetails.builder().orderType(caseData.getSelectedOrder())
            .orderTypeId(caseData.getCreateSelectOrderOptions().name())
            .orderDocument(caseData.getSolicitorDraftOrderDoc())
            .otherDetails(OtherOrderDetails.builder()
                              .createdBy(caseData.getJudgeOrMagistratesLastName())
                              .orderCreatedDate(dateTime.now().format(DateTimeFormatter.ofPattern(
                                  PrlAppsConstants.D_MMMM_YYYY,
                                  Locale.UK
                              )))
                              .orderMadeDate(caseData.getDateOrderMade().format(DateTimeFormatter.ofPattern(
                                  PrlAppsConstants.D_MMMM_YYYY,
                                  Locale.UK
                              )))
                              .orderRecipients("NA").build())
            .dateCreated(dateTime.now())
            .build();
    }
}
