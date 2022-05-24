package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.AppointedGuardianFullName;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
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

    @Value("${document.templates.common.C43A_final_template}")
    protected String c43AFinalTemplate;

    @Value("${document.templates.common.C43A_final_filename}")
    protected String c43AFinalFilename;

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

    @Value("${document.templates.common.prl_n117_draft_template}")
    protected String n117DraftTemplate;

    @Value("${document.templates.common.prl_n117_draft_filename}")
    protected String n117DraftFile;

    @Value("${document.templates.common.prl_n117_template}")
    protected String n117Template;

    @Value("${document.templates.common.prl_n117_filename}")
    protected String n117File;

    @Value("${document.templates.common.prl_c45a_draft_template}")
    protected String c45aDraftTemplate;

    @Value("${document.templates.common.prl_c45a_draft_filename}")
    protected String c45aDraftFile;

    @Value("${document.templates.common.prl_c45a_template}")
    protected String c45aTemplate;

    @Value("${document.templates.common.prl_c45a_filename}")
    protected String c45aFile;

    public static final String FAMILY_MAN_ID = "Family Man ID: ";

    @Autowired
    private final DgsService dgsService;

    private final Time dateTime;

    private final ObjectMapper objectMapper;

    public Map<String, Object> populateHeader(CaseData caseData) {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("manageOrderHeader1", getHeaderInfo(caseData));
        return headerMap;
    }

    public CaseData getUpdatedCaseData(CaseData caseData) {
        return CaseData.builder().childrenList(getChildInfoFromCaseData(caseData))
            .manageOrders(ManageOrders.builder().childListForSpecialGuardianship(getChildInfoFromCaseData(caseData)).build())
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
            case blankOrderOrDirectionsWithdraw:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, c21TDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, c21DraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, c21Template);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, c21File);
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
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, c43AFinalTemplate);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, c43AFinalFilename);
                break;
            case appointmentOfGuardian:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, c47aDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, c47aDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, c47aTemplate);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, c47aFile);
                break;
            case generalForm:
                fieldsMap.put(PrlAppsConstants.TEMPLATE,n117DraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, n117DraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME,n117Template);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, n117File);
                break;
            case parentalResponsibility:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, c45aDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, c45aDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, c45aTemplate);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, c45aFile);
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
        return FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
            ? FAMILY_MAN_ID + caseData.getFl401FamilymanCaseNumber()
            : FAMILY_MAN_ID + caseData.getFamilymanCaseNumber();
    }

    private String getChildInfoFromCaseData(CaseData caseData) {
        StringBuilder builder = new StringBuilder();
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            List<Child> children = new ArrayList<>();
            if (caseData.getChildren() != null) {
                children = caseData.getChildren().stream()
                    .map(Element::getValue)
                    .collect(Collectors.toList());
            }
            for (int i = 0; i < children.size(); i++) {
                Child child = children.get(i);
                builder.append(String.format("Child %d: %s", i + 1, child.getFirstName() + " " + child.getLastName()));
                builder.append("\n");
            }
        } else {
            Optional<List<Element<ApplicantChild>>> applicantChildDetails = ofNullable(caseData.getApplicantChildDetails());
            if (applicantChildDetails.isPresent()) {
                List<ApplicantChild> children = applicantChildDetails.get().stream()
                    .map(Element::getValue)
                    .collect(Collectors.toList());
                for (int i = 0; i < children.size(); i++) {
                    ApplicantChild child = children.get(i);
                    builder.append(String.format("Child %d: %s", i + 1, child.getFullName()));
                    builder.append("\n");
                }
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

    public void updateCaseDataWithAppointedGuardianNames(uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails,
                                                    List<Element<AppointedGuardianFullName>> guardianNamesList) {
        CaseData mappedCaseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
        List<AppointedGuardianFullName> appointedGuardianFullNameList = mappedCaseData
            .getAppointedGuardianName()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> nameList = appointedGuardianFullNameList.stream()
            .map(AppointedGuardianFullName::getGuardianFullName)
            .collect(Collectors.toList());

        nameList.forEach(name -> {
            AppointedGuardianFullName appointedGuardianFullName
                = AppointedGuardianFullName
                .builder()
                .guardianFullName(name)
                .build();
            Element<AppointedGuardianFullName> wrappedName
                = Element.<AppointedGuardianFullName>builder()
                .value(appointedGuardianFullName)
                .build();
            guardianNamesList.add(wrappedName);
        });
    }

    public Map<String, Object> getCaseData(String authorisation, CaseData caseData,
                             Map<String, Object> caseDataUpdated)
        throws Exception {

        Map<String, String> fieldsMap = getOrderTemplateAndFile(caseData.getCreateSelectOrderOptions());

        GeneratedDocumentInfo generatedDocumentInfo = dgsService.generateDocument(
            authorisation,
            uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder().caseData(caseData).build(),
            fieldsMap.get(PrlAppsConstants.TEMPLATE)
        );

        caseDataUpdated.put("isEngDocGen", Yes.toString());
        caseDataUpdated.put("previewOrderDoc", Document.builder()
            .documentUrl(generatedDocumentInfo.getUrl())
            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
            .documentHash(generatedDocumentInfo.getHashToken())
            .documentFileName(fieldsMap.get(PrlAppsConstants.FILE_NAME)).build());
        return caseDataUpdated;
    }

    public ManageOrders getN117FormData(CaseData caseData) {

        ManageOrders orderData = ManageOrders.builder()
            .manageOrdersCaseNo(String.valueOf(caseData.getId()))
            .manageOrdersCourtName(caseData.getCourtName())
            .manageOrdersApplicant(String.format("%s %s", caseData.getApplicantsFL401().getFirstName(),
                                                 caseData.getApplicantsFL401().getLastName()))
            .manageOrdersRespondent(String.format("%s %s", caseData.getRespondentsFL401().getFirstName(),
                                                  caseData.getRespondentsFL401().getLastName()))
            .manageOrdersApplicantReference(String.format("%s %s", caseData.getApplicantsFL401().getRepresentativeFirstName(),
                                                          caseData.getApplicantsFL401().getRepresentativeLastName()))
            .build();

        if (ofNullable(caseData.getRespondentsFL401().getAddress()).isPresent()) {
            orderData = orderData.toBuilder().manageOrdersRespondentAddress(caseData.getRespondentsFL401().getAddress()).build();
        }
        if (ofNullable(caseData.getRespondentsFL401().getDateOfBirth()).isPresent()) {
            orderData = orderData.toBuilder().manageOrdersRespondentDob(caseData.getRespondentsFL401().getDateOfBirth()).build();
        }
        return orderData;
    }


}
