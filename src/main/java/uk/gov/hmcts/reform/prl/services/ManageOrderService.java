package uk.gov.hmcts.reform.prl.services;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.OrderDetails;
import uk.gov.hmcts.reform.prl.enums.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Order;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.time.Time;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum.applicantOrApplicantSolicitor;
import static uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum.respondentOrRespondentSolicitor;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManageOrderService {

    @Value("${document.templates.common.prl_c21_draft_template}")
    protected String c21TDraftTemplate;

    @Value("${document.templates.common.prl_c21_draft_filename}")
    protected String c21DraftFile;

    public static final String FAMILY_MAN_ID = "Family Man ID: ";


    private final Time dateTime;

    public Map<String, Object> populateHeader(CaseData caseData) {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("manageOrderHeader1", getHeaderInfo(caseData));
        return headerMap;
    }

    public CaseData getUpdatedCaseData(CaseData caseData) {
        return CaseData.builder().childrenList(getChildInfoFromCaseData(caseData))
            .selectedOrder(getSelectedOrderInfo(caseData)).build();
    }

    public Map<String,String> getOrderTemplateAndFile(CreateSelectOrderOptionsEnum selectedOrder) {
        Map<String,String> fieldsMap = new HashMap();
        switch (selectedOrder) {
            case blankOrderOrDirections:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, c21TDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, c21DraftFile);
                break;
            case standardDirectionsOrder:
                fieldsMap.put(PrlAppsConstants.TEMPLATE,"");
                fieldsMap.put(PrlAppsConstants.FILE_NAME, "");
                break;
            default:
                break;
        }
        return fieldsMap;
    }

    private String getSelectedOrderInfo(CaseData caseData) {
        StringBuilder selectedOrder = new StringBuilder();
        selectedOrder.append(caseData.getManageOrdersOptions() == ManageOrdersOptionsEnum.createAnOrder
                                 ? caseData.getCreateSelectOrderOptions().getDisplayedValue()
                                 : caseData.getChildArrangementOrders().getDisplayedValue());
        selectedOrder.append("\n\n");
        return selectedOrder.toString();
    }

    private String getHeaderInfo(CaseData caseData) {
        StringBuilder headerInfo = new StringBuilder();
        headerInfo.append("Case Name: " + caseData.getApplicantCaseName());
        headerInfo.append("\n\n");
        headerInfo.append(getFamilyManNumber(caseData));
        headerInfo.append("\n\n");
        return headerInfo.toString();
    }

    private String getFamilyManNumber(CaseData caseData) {
        if (caseData.getFl401FamilymanCaseNumber() == null && caseData.getFamilymanCaseNumber() == null) {
            return FAMILY_MAN_ID;
        }
        return caseData.getCaseTypeOfApplication().equalsIgnoreCase(FL401_CASE_TYPE)
            ? FAMILY_MAN_ID + caseData.getFl401FamilymanCaseNumber()
            : FAMILY_MAN_ID + caseData.getFamilymanCaseNumber();
    }

    private String getChildInfoFromCaseData(CaseData caseData) {
        List<Child> children = new ArrayList<>();
        if (caseData.getChildren() != null) {
            children = caseData.getChildren().stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < children.size(); i++) {
            Child child = children.get(i);
            builder.append(String.format("Child %d: %s", i + 1, child.getFirstName() + child.getLastName()));
            builder.append("\n");
        }
        return builder.toString();
    }

    private OrderDetails getCurrentOrderDetails(CaseData caseData){
        //Blank doc is added only for testing, needs to be replaced with the generated document
        return OrderDetails.builder().orderType(caseData.getSelectedOrder()).orderDocument(Document.builder()
                                                                                               .documentUrl("http://dm-store:8080/documents/cb961f7b-47e2-4954-a0e0-ca9a46ed2365")
                                                                                               .documentBinaryUrl("http://dm-store:8080/documents/cb961f7b-47e2-4954-a0e0-ca9a46ed2365/binary")
                                                                                               .documentFileName("blank-test-pdf.pdf")
                                                                                               .build())
            .otherDetails(OtherOrderDetails.builder()
                              .createdBy(caseData.getJudgeOrMagistratesLastName())
                              .orderCreatedDate(dateTime.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)))
                              .orderMadeDate(caseData.getDateOrderMade().format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)))
                              .orderRecipients(getAllRecipients(caseData)).build())
            .dateCreated(dateTime.now())
            .build();
    }

    private String getAllRecipients(CaseData caseData) {
        StringBuilder recipientsList = new StringBuilder();
        Optional<List<OrderRecipientsEnum>> appResRecipientList = ofNullable(caseData.getOrderRecipients());
        if (appResRecipientList.isPresent() && caseData.getOrderRecipients().contains(applicantOrApplicantSolicitor)){
            recipientsList.append(getApplicantSolicitorDetails(caseData));
            recipientsList.append('\n');
        }
        if (appResRecipientList.isPresent() && caseData.getOrderRecipients().contains(respondentOrRespondentSolicitor)){
            recipientsList.append(getRespondentSolicitorDetails(caseData));
            recipientsList.append('\n');
        }
        return recipientsList.toString();
    }

    private String getApplicantSolicitorDetails(CaseData caseData) {
        List<PartyDetails> applicants = caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
        List<String> applicantSolicitorNames = applicants.stream()
            .map(party -> party.getSolicitorOrg().getOrganisationName() + " (Applicant's Solicitor)")
            .collect(Collectors.toList());
        return String.join("\n", applicantSolicitorNames);
    }

    private String getRespondentSolicitorDetails(CaseData caseData) {
        List<PartyDetails> respondents = caseData
            .getRespondents()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
        List<String> respondentSolicitorNames = respondents.stream()
            .map(party -> party.getSolicitorOrg().getOrganisationName() + " (Respondent's Solicitor)")
            .collect(Collectors.toList());
        return String.join("\n", respondentSolicitorNames);
    }

    public List<Element<OrderDetails>> addOrderDetailsAndReturnReverseSortedList(CaseData caseData) {
        Element<OrderDetails> orderDetails = element(getCurrentOrderDetails(caseData));
        List<Element<OrderDetails>> orderCollection;

        if (caseData.getOrderCollection() != null) {
            orderCollection = caseData.getOrderCollection();
            orderCollection.add(orderDetails);
        } else {
            orderCollection = new ArrayList<>();
            orderCollection.add(orderDetails);
        }
        orderCollection.sort(Comparator.comparing(m -> m.getValue().getDateCreated(), Comparator.reverseOrder()));
        return orderCollection;
    }

}
