package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.AppointedGuardianFullName;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENT_SOLICITOR;
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

    @Value("${document.templates.common.prl_c43a_draft_template}")
    protected String c43ADraftTemplate;

    @Value("${document.templates.common.prl_c43a_draft_filename}")
    protected String c43ADraftFilename;

    @Value("${document.templates.common.prl_c49_draft_template}")
    protected String c49TDraftTemplate;

    @Value("${document.templates.common.prl_c49_draft_filename}")
    protected String c49DraftFile;

    @Value("${document.templates.common.prl_c49_template}")
    protected String c49Template;

    @Value("${document.templates.common.prl_c49_filename}")
    protected String c49File;

    @Value("${document.templates.fl401.fl401_fl406_draft_template}")
    protected String fl406DraftTemplate;

    @Value("${document.templates.fl401.fl401_fl406_draft_filename}")
    protected String fl406DraftFile;

    @Value("${document.templates.fl401.fl401_fl406_english_template}")
    protected String fl406TemplateEnglish;

    @Value("${document.templates.fl401.fl401_fl406_engllish_filename}")
    protected String fl406FileEnglish;

    @Value("${document.templates.fl401.fl401_fl406_welsh_template}")
    protected String fl406TemplateWelsh;

    @Value("${document.templates.fl401.fl401_fl406_welsh_filename}")
    protected String fl406FileWelsh;

    @Value("${document.templates.common.prl_c43a_final_template}")
    protected String c43AFinalTemplate;

    @Value("${document.templates.common.prl_c43a_final_filename}")
    protected String c43AFinalFilename;

    @Value("${document.templates.common.prl_c43_draft_template}")
    protected String c43DraftTemplate;

    @Value("${document.templates.common.prl_c43_draft_filename}")
    protected String c43DraftFile;

    @Value("${document.templates.common.prl_c43_template}")
    protected String c43Template;

    @Value("${document.templates.common.prl_c43_filename}")
    protected String c43File;

    @Value("${document.templates.common.prl_fl404_draft_template}")
    protected String fl404DraftTemplate;

    @Value("${document.templates.common.prl_fl404_draft_filename}")
    protected String fl404DraftFile;

    @Value("${document.templates.common.prl_fl404_template}")
    protected String fl404Template;

    @Value("${document.templates.common.prl_fl404_filename}")
    protected String fl404File;

    @Value("${document.templates.common.prl_fl404a_draft_template}")
    protected String fl404aDraftTemplate;

    @Value("${document.templates.common.prl_fl404a_draft_filename}")
    protected String fl404aDraftFile;

    @Value("${document.templates.common.prl_fl404a_final_template}")
    protected String fl404aFinalTemplate;

    @Value("${document.templates.common.prl_fl404a_final_filename}")
    protected String fl404aFinalFile;

    @Value("${document.templates.common.prl_c45a_draft_template}")
    protected String c45aDraftTemplate;

    @Value("${document.templates.common.prl_c45a_draft_filename}")
    protected String c45aDraftFile;

    @Value("${document.templates.common.prl_c45a_template}")
    protected String c45aTemplate;

    @Value("${document.templates.common.prl_c45a_filename}")
    protected String c45aFile;

    @Value("${document.templates.common.prl_c47a_draft_template}")
    protected String c47aDraftTemplate;

    @Value("${document.templates.common.prl_c47a_draft_filename}")
    protected String c47aDraftFile;

    @Value("${document.templates.common.prl_c47a_template}")
    protected String c47aTemplate;

    @Value("${document.templates.common.prl_c47a_filename}")
    protected String c47aFile;

    @Value("${document.templates.common.prl_fl402_draft_template}")
    protected String fl402DraftTemplate;

    @Value("${document.templates.common.prl_fl402_draft_filename}")
    protected String fl402DraftFile;

    @Value("${document.templates.common.prl_fl402_final_template}")
    protected String fl402FinalTemplate;

    @Value("${document.templates.common.prl_fl402_final_filename}")
    protected String fl402FinalFile;

    @Value("${document.templates.common.prl_fl404b_draft_template}")
    protected String fl404bDraftTemplate;

    @Value("${document.templates.common.prl_fl404b_draft_filename}")
    protected String fl404bDraftFile;

    @Value("${document.templates.common.prl_fl404b_blank_draft_filename}")
    protected String fl404bBlankDraftFile;

    @Value("${document.templates.common.prl_fl404b_final_template}")
    protected String fl404bTemplate;

    @Value("${document.templates.common.prl_fl404b_final_filename}")
    protected String fl404bFile;

    @Value("${document.templates.common.prl_fl404b_blank_final_filename}")
    protected String fl404bBlankFile;

    @Value("${document.templates.common.prl_n117_draft_template}")
    protected String n117DraftTemplate;

    @Value("${document.templates.common.prl_n117_draft_filename}")
    protected String n117DraftFile;

    @Value("${document.templates.common.prl_n117_template}")
    protected String n117Template;

    @Value("${document.templates.common.prl_n117_filename}")
    protected String n117File;

    public static final String FAMILY_MAN_ID = "Family Man ID: ";

    private final DgsService dgsService;

    private final Time dateTime;

    private final ObjectMapper objectMapper;

    private final ElementUtils elementUtils;

    public Map<String, Object> populateHeader(CaseData caseData) {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("amendOrderDynamicList", getOrdersAsDynamicList(caseData));
        return headerMap;
    }

    public CaseData getUpdatedCaseData(CaseData caseData) {
        return caseData.toBuilder().childrenList(getChildInfoFromCaseData(caseData))
            .manageOrders(ManageOrders.builder().childListForSpecialGuardianship(getChildInfoFromCaseData(caseData)).build())
            .selectedOrder(getSelectedOrderInfo(caseData)).build();
    }

    private Map<String, String> getOrderTemplateAndFile(CreateSelectOrderOptionsEnum selectedOrder) {
        Map<String, String> fieldsMap = new HashMap<>();
        switch (selectedOrder) {
            case blankOrderOrDirections:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, c21TDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, c21DraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, c21Template);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, c21File);
                break;
            case powerOfArrest:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, fl406DraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, fl406DraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, fl406TemplateEnglish);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, fl406FileEnglish);
                break;
            case standardDirectionsOrder:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, "");
                fieldsMap.put(PrlAppsConstants.FILE_NAME, "");
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
            case occupation:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, fl404DraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, fl404DraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, fl404Template);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, fl404File);
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
            case nonMolestation:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, fl404aDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, fl404aDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, fl404aFinalTemplate);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, fl404aFinalFile);
                break;
            case parentalResponsibility:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, c45aDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, c45aDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, c45aTemplate);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, c45aFile);
                break;
            case transferOfCaseToAnotherCourt:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, c49TDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, c49DraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, c49Template);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, c49File);
                break;
            case noticeOfProceedings:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, fl402DraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, fl402DraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, fl402FinalTemplate);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, fl402FinalFile);
                break;
            case generalForm:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, n117DraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, n117DraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, n117Template);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, n117File);
                break;
            case amendDischargedVaried:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, fl404bDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, fl404bDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, fl404bTemplate);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, fl404bFile);
                break;
            case blank:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, fl404bDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, fl404bBlankDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, fl404bTemplate);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, fl404bBlankFile);
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

        String flagSelectedOrder = caseData.getManageOrdersOptions() == ManageOrdersOptionsEnum.createAnOrder
            ? caseData.getCreateSelectOrderOptions().getDisplayedValue()
            : caseData.getChildArrangementOrders().getDisplayedValue();

        if (caseData.getCreateSelectOrderOptions() != null && caseData.getDateOrderMade() != null) {
            Map<String, String> fieldMap = getOrderTemplateAndFile(caseData.getCreateSelectOrderOptions());
            GeneratedDocumentInfo generatedDocumentInfo = dgsService.generateDocument(
                authorisation,
                CaseDetails.builder().caseData(caseData).build(),
                fieldMap.get(PrlAppsConstants.FINAL_TEMPLATE_NAME)
            );
            return OrderDetails.builder().orderType(flagSelectedOrder)
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
            return OrderDetails.builder().orderType(flagSelectedOrder)
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

    public Map<String, Object> addOrderDetailsAndReturnReverseSortedList(String authorisation, CaseData caseData) throws Exception {
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
        return Map.of("orderCollection", orderCollection);
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

    public Map<String, Object> getCaseData(String authorisation, CaseData caseData)
        throws Exception {
        Map<String, Object> caseDataUpdated = new HashMap<>();
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

    private CaseData getN117FormData(CaseData caseData) {

        log.info("Court name before N117 order {}", caseData.getCourtName());

        ManageOrders orderData = ManageOrders.builder()
            .manageOrdersCaseNo(String.valueOf(caseData.getId()))
            .manageOrdersCourtName(caseData.getCourtName())
            .manageOrdersApplicant(String.format(PrlAppsConstants.FORMAT, caseData.getApplicantsFL401().getFirstName(),
                                                 caseData.getApplicantsFL401().getLastName()
            ))
            .manageOrdersRespondent(String.format(PrlAppsConstants.FORMAT, caseData.getRespondentsFL401().getFirstName(),
                                                  caseData.getRespondentsFL401().getLastName()
            ))
            .manageOrdersApplicantReference(String.format(PrlAppsConstants.FORMAT,
                                                          caseData.getApplicantsFL401().getRepresentativeFirstName(),
                                                          caseData.getApplicantsFL401().getRepresentativeLastName()
            ))
            .build();

        log.info("Court name after N117 order set{}", orderData.getManageOrdersCourtName());

        if (ofNullable(caseData.getRespondentsFL401().getAddress()).isPresent()) {
            orderData = orderData.toBuilder().manageOrdersRespondentAddress(caseData.getRespondentsFL401().getAddress()).build();
        }
        if (ofNullable(caseData.getRespondentsFL401().getDateOfBirth()).isPresent()) {
            orderData = orderData.toBuilder().manageOrdersRespondentDob(caseData.getRespondentsFL401().getDateOfBirth()).build();
        }

        return caseData.toBuilder().manageOrders(orderData)
            .selectedOrder(getSelectedOrderInfo(caseData)).build();
    }

    public CaseData populateCustomOrderFields(CaseData caseData) {
        CreateSelectOrderOptionsEnum order = caseData.getCreateSelectOrderOptions();

        switch (order) {
            case amendDischargedVaried:
            case occupation:
            case nonMolestation:
            case powerOfArrest:
            case blank:
                return getFl404bFields(caseData);
            case generalForm:
                return getN117FormData(caseData);
            case noticeOfProceedings:
                return getFL402FormData(caseData);
            default:
                return caseData;
        }
    }

    private CaseData getFl404bFields(CaseData caseData) {

        log.info("Court name before FL404 order {}", caseData.getCourtName());

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
            orderData = orderData.toBuilder().fl404bRespondentAddress(caseData.getRespondentsFL401().getAddress()).build();
        }
        if (ofNullable(caseData.getRespondentsFL401().getDateOfBirth()).isPresent()) {
            orderData = orderData.toBuilder().fl404bRespondentDob(caseData.getRespondentsFL401().getDateOfBirth()).build();
        }
        return caseData.toBuilder().manageOrders(ManageOrders.builder()
                                                     .fl404CustomFields(orderData)
                                                     .build())
            .selectedOrder(getSelectedOrderInfo(caseData)).build();
    }

    public DynamicList getOrdersAsDynamicList(CaseData caseData) {
        List<Element<OrderDetails>> orders = caseData.getOrderCollection();

        return ElementUtils.asDynamicList(
            orders,
            null,
            OrderDetails::getLabelForDynamicList
        );
    }

    public Map<String, Object> getOrderToAmendDownloadLink(CaseData caseData) {

        UUID orderId = elementUtils.getDynamicListSelectedValue(
            caseData.getManageOrders().getAmendOrderDynamicList(), objectMapper);

        OrderDetails selectedOrder = caseData.getOrderCollection().stream()
            .filter(element -> element.getId().equals(orderId))
            .map(Element::getValue)
            .findFirst()
            .orElseThrow(() -> new UnsupportedOperationException(String.format(
                "Could not find action to amend order for order with id \"%s\"",
                caseData.getManageOrders().getAmendOrderDynamicList().getValueCode()
            )));

        return Map.of("manageOrdersDocumentToAmend", selectedOrder.getOrderDocument());


    }

    public CaseData getFL402FormData(CaseData caseData) {

        log.info("Court name before FL402 order {}", caseData.getCourtName());
        ManageOrders orderData =  ManageOrders.builder()
            .manageOrdersFl402CaseNo(String.valueOf(caseData.getId()))
            .manageOrdersFl402CourtName(caseData.getCourtName())
            .manageOrdersFl402Applicant(String.format(PrlAppsConstants.FORMAT, caseData.getApplicantsFL401().getFirstName(),
                                                      caseData.getApplicantsFL401().getLastName()
            ))
            .manageOrdersFl402ApplicantRef(String.format(PrlAppsConstants.FORMAT,
                                                         caseData.getApplicantsFL401().getRepresentativeFirstName(),
                                                         caseData.getApplicantsFL401().getRepresentativeLastName()
            ))
            .build();
        log.info("Court name after FL402 order set{}", orderData.getManageOrdersFl402CourtName());


        return caseData.toBuilder().manageOrders(orderData)
            .selectedOrder(getSelectedOrderInfo(caseData)).build();
    }
}
