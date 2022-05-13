package uk.gov.hmcts.reform.prl.services;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.OrderDetails;
import uk.gov.hmcts.reform.prl.enums.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.ManageOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404b;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum.applicantOrApplicantSolicitor;
import static uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum.respondentOrRespondentSolicitor;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.unwrapElements;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManageOrderService {

    @Value("${document.templates.common.prl_c21_draft_template}")
    protected String c21TDraftTemplate;

    @Value("${document.templates.common.prl_c21_draft_filename}")
    protected String c21DraftFile;

    @Value("${document.templates.common.prl_c21_template}")
    protected String c21Template;

    @Value("${document.templates.common.prl_c21_filename}")
    protected String c21File;

    @Value("${document.templates.common.C43A_draft_template}")
    protected String c43ADraftTemplate;

    @Value("${document.templates.common.C43A_draft_filename}")
    protected String c43ADraftFilename;

    @Value("${document.templates.common.prl_c43_draft_template}")
    protected String c43DraftTemplate;

    @Value("${document.templates.common.prl_c43_draft_filename}")
    protected String c43DraftFile;

    @Value("${document.templates.common.prl_c43_template}")
    protected String c43Template;

    @Value("${document.templates.common.prl_c43_filename}")
    protected String c43File;

    @Value("${document.templates.common.prl_c47a_draft_template}")
    protected String c47aDraftTemplate;

    @Value("${document.templates.common.prl_c47a_draft_filename}")
    protected String c47aDraftFile;

    @Value("${document.templates.common.prl_c47a_template}")
    protected String c47aTemplate;

    @Value("${document.templates.common.prl_c47a_filename}")
    protected String c47aFile;

    @Value("${document.templates.common.prl_fl404b_draft_template}")
    protected String fl404bDraftTemplate;

    @Value("${document.templates.common.prl_fl404b_draft_filename}")
    protected String fl404bDraftFile;

    @Value("${document.templates.common.prl_fl404b_final_template}")
    protected String fl404bTemplate;

    @Value("${document.templates.common.prl_fl404b_final_filename}")
    protected String fl404bFile;

    public static final String FAMILY_MAN_ID = "Family Man ID: ";

    @Autowired
    private final DgsService dgsService;

    private final Time dateTime;

    public Map<String, Object> populateHeader(CaseData caseData) {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("manageOrderHeader1", getHeaderInfo(caseData));
        return headerMap;
    }

    public CaseData getUpdatedCaseData(CaseData caseData) {
        return caseData.toBuilder().childrenList(getChildInfoFromCaseData(caseData))
            .selectedOrder(getSelectedOrderInfo(caseData)).build();
    }

    private Map<String,String> getOrderTemplateAndFile(CreateSelectOrderOptionsEnum selectedOrder) {
        Map<String,String> fieldsMap = new HashMap();
        switch (selectedOrder) {
            case blankOrderOrDirections:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, c21TDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, c21DraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, c21Template);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, c21File);
                break;
            case standardDirectionsOrder:
                fieldsMap.put(PrlAppsConstants.TEMPLATE,"");
                fieldsMap.put(PrlAppsConstants.FILE_NAME, "");
                break;
            case childArrangementsSpecificProhibitedOrder:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, c43DraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, c43DraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, c43Template);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, c43File);
                break;
            case specialGuardianShip:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, c43ADraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, c43ADraftFilename);
                break;
            case appointmentOfGuardian:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, c47aDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, c47aDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, c47aTemplate);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, c47aFile);
                break;
            case amendDischargedVaried:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, fl404bDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, fl404bDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, fl404bTemplate);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, fl404bFile);
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

        StringBuilder builder = new StringBuilder();

        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            List<Child> children = caseData.getChildren().stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            for (int i = 0; i < children.size(); i++) {
                Child child = children.get(i);
                builder.append(String.format("Child %d: %s", i + 1, child.getFirstName() + child.getLastName()));
                builder.append("\n");
            }

        } else {
            builder.append(getFl401ChildrenString(caseData));
        }
        return builder.toString();
    }

    private String getFl401ChildrenString(CaseData caseData) {
        StringBuilder builder = new StringBuilder();
        if (ofNullable(caseData.getApplicantChildDetails()).isPresent()) {
            List<ApplicantChild> children = unwrapElements(caseData.getApplicantChildDetails());
            for (int i = 0; i < children.size(); i++) {
                ApplicantChild child = children.get(i);
                builder.append(String.format("Child %d: %s", i + 1, child.getFullName()));
                builder.append("\n");
            }
        }
        if (ofNullable(caseData.getHome()).isPresent() && ofNullable(caseData.getHome().getChildren()).isPresent()) {
            List<ChildrenLiveAtAddress> childrenInHome = caseData.getHome().getChildren().stream()
                .map(Element::getValue).collect(Collectors.toList());

            for (int i = 0; i < childrenInHome.size(); i++) {
                ChildrenLiveAtAddress child = childrenInHome.get(i);
                builder.append(String.format("Child %d: %s", i + 1, child.getChildFullName()));
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    private OrderDetails getCurrentOrderDetails(String authorisation, CaseData caseData)
        throws Exception {
        if (caseData.getCreateSelectOrderOptions() != null && caseData.getDateOrderMade() != null) {
            Map<String, String> fieldMap = getOrderTemplateAndFile(caseData.getCreateSelectOrderOptions());
            GeneratedDocumentInfo generatedDocumentInfo = dgsService.generateDocument(
                authorisation,
                CaseDetails.builder().caseData(caseData).build(),
                fieldMap.get(PrlAppsConstants.FINAL_TEMPLATE_NAME)
            );
            return OrderDetails.builder().orderType(caseData.getSelectedOrder())
                .orderDocument(Document.builder()
                                   .documentUrl(generatedDocumentInfo.getUrl())
                                   .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                   .documentHash(generatedDocumentInfo.getHashToken())
                                   .documentFileName(fieldMap.get(PrlAppsConstants.GENERATE_FILE_NAME)).build())
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
                                  .orderRecipients(getAllRecipients(caseData)).build())
                .dateCreated(dateTime.now())
                .build();
        } else {
            return OrderDetails.builder().orderType(caseData.getSelectedOrder())
                .orderDocument(caseData.getAppointmentOfGuardian())
                .otherDetails(OtherOrderDetails.builder()
                                  .createdBy(caseData.getJudgeOrMagistratesLastName())
                                  .orderCreatedDate(dateTime.now().format(DateTimeFormatter.ofPattern(
                                      PrlAppsConstants.D_MMMM_YYYY,
                                      Locale.UK
                                  )))
                                  .orderRecipients(getAllRecipients(caseData)).build())
                .dateCreated(dateTime.now())
                .build();
        }

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
            List<String> applicantSolicitorNames  = applicants.stream()
                .map(party -> party.getSolicitorOrg().getOrganisationName() + " (Applicant's Solicitor)")
                .collect(Collectors.toList());
            return String.join("\n", applicantSolicitorNames);
        } else {
            PartyDetails applicantFl401 = caseData.getApplicantsFL401();
            String applicantSolicitorName = applicantFl401.getRepresentativeFirstName()
                + " "
                + applicantFl401.getRepresentativeLastName();
            return  applicantSolicitorName;
        }
    }

    private String getRespondentSolicitorDetails(CaseData caseData) {
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            List<PartyDetails> respondents = caseData
                .getRespondents()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
            List<String> respondentSolicitorNames = respondents.stream()
                .map(party -> party.getSolicitorOrg().getOrganisationName() + " (Respondent's Solicitor)")
                .collect(Collectors.toList());
            return String.join("\n", respondentSolicitorNames);
        } else {
            PartyDetails respondentFl401 = caseData.getRespondentsFL401();
            String respondentSolicitorName = respondentFl401.getRepresentativeFirstName()
                + " "
                + respondentFl401.getRepresentativeLastName();
            return  respondentSolicitorName;
        }
    }

    public List<Element<OrderDetails>> addOrderDetailsAndReturnReverseSortedList(String authorisation, CaseData caseData)
        throws Exception {
        Element<OrderDetails> orderDetails = element(getCurrentOrderDetails(authorisation, caseData));
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

    public void getCaseData(String authorisation, CaseData caseData1,
                             Map<String, Object> caseDataUpdated)
        throws Exception {

        Map<String, String> fieldsMap = getOrderTemplateAndFile(caseData1.getCreateSelectOrderOptions());

        GeneratedDocumentInfo generatedDocumentInfo = dgsService.generateDocument(
            authorisation,
            uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData1).build(),
            fieldsMap.get(PrlAppsConstants.TEMPLATE)
        );

        caseDataUpdated.put("isEngDocGen", Yes.toString());
        caseDataUpdated.put("previewOrderDoc", Document.builder()
            .documentUrl(generatedDocumentInfo.getUrl())
            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
            .documentHash(generatedDocumentInfo.getHashToken())
            .documentFileName(fieldsMap.get(PrlAppsConstants.FILE_NAME)).build());
    }

    public CaseData populateCustomOrderFields(CaseData caseData) {
        CreateSelectOrderOptionsEnum order = caseData.getCreateSelectOrderOptions();

        switch (order) {
            case amendDischargedVaried:
            case blank:
                return getFl404bFields(caseData);
            default:
                return caseData;
        }
    }

    private CaseData getFl404bFields(CaseData caseData) {
        FL404b orderData = FL404b.builder()
            .fl404bCaseNumber(String.valueOf(caseData.getId()))
            .fl404bCourtName(caseData.getCourtName())
            .fl404bApplicantName(String.format("%s %s", caseData.getApplicantsFL401().getFirstName(),
                                               caseData.getApplicantsFL401().getLastName()))
            .fl404bRespondentName(String.format("%s %s", caseData.getRespondentsFL401().getFirstName(),
                                                caseData.getRespondentsFL401().getLastName()))
            .build();

        if (ofNullable(caseData.getRespondentsFL401().getAddress()).isPresent()) {
            orderData = orderData.toBuilder().fl404bRespondentAddress(caseData.getRespondentsFL401().getAddress()).build();
        }
        if (ofNullable(caseData.getRespondentsFL401().getDateOfBirth()).isPresent()) {
            orderData = orderData.toBuilder().fl404bRespondentDob(caseData.getRespondentsFL401().getDateOfBirth()).build();
        }
        return caseData.toBuilder().manageOrders(ManageOrders.builder()
                                                     .fl404bCustomFields(orderData).build()).build();

    }

}
