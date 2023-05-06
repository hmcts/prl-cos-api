package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.models.ServeOrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.AppointedGuardianFullName;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.EmailInformation;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.PostalInformation;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDate;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FINAL_TEMPLATE_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum.servedSavedOrders;
import static uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum.applicantOrApplicantSolicitor;
import static uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum.respondentOrRespondentSolicitor;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManageOrderService {

    public static final String CAFCASS_SERVED = "cafcassServed";
    public static final String SERVE_ON_RESPONDENT = "serveOnRespondent";
    public static final String OTHER_PARTIES_SERVED = "otherPartiesServed";
    public static final String SERVING_RESPONDENTS_OPTIONS = "servingRespondentsOptions";
    @Value("${document.templates.common.prl_c21_draft_template}")
    protected String sdoDraftTemplate;

    @Value("${document.templates.common.prl_c21_draft_filename}")
    protected String sdoDraftFile;

    @Value("${document.templates.common.prl_c21_draft_template}")
    protected String doiDraftTemplate;

    @Value("${document.templates.common.prl_c21_draft_filename}")
    protected String doiDraftFile;

    @Value("${document.templates.common.prl_c21_draft_template}")
    protected String c21TDraftTemplate;

    @Value("${document.templates.common.prl_c21_draft_filename}")
    protected String c21DraftFile;

    @Value("${document.templates.common.prl_c21_template}")
    protected String c21Template;

    @Value("${document.templates.common.prl_c21_filename}")
    protected String c21File;

    @Value("${document.templates.common.prl_c21_welsh_template}")
    protected String c21WelshTemplate;

    @Value("${document.templates.common.prl_c21_welsh_filename}")
    protected String c21WelshFileName;

    @Value("${document.templates.common.prl_c21_welsh_draft_template}")
    protected String c21DraftWelshTemplate;

    @Value("${document.templates.common.prl_c21_welsh_draft_filename}")
    protected String c21DraftWelshFileName;

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

    @Value("${document.templates.fl401.fl401_fl406_welsh_draft_template}")
    protected String fl406WelshDraftTemplate;

    @Value("${document.templates.fl401.fl401_fl406_welsh_draft_filename}")
    protected String fl406WelshDraftFile;

    @Value("${document.templates.fl401.fl401_fl406_welsh_template}")
    protected String fl406WelshTemplate;

    @Value("${document.templates.fl401.fl401_fl406_welsh_filename}")
    protected String fl406WelshFile;

    @Value("${document.templates.common.prl_c43a_final_template}")
    protected String c43AFinalTemplate;

    @Value("${document.templates.common.prl_c43a_final_filename}")
    protected String c43AFinalFilename;

    @Value("${document.templates.common.prl_c43a_welsh_final_template}")
    protected String c43AWelshFinalTemplate;

    @Value("${document.templates.common.prl_c43a_welsh_final_filename}")
    protected String c43AWelshFinalFilename;

    @Value("${document.templates.common.prl_c43a_welsh_draft_template}")
    protected String c43AWelshDraftTemplate;

    @Value("${document.templates.common.prl_c43a_welsh_draft_filename}")
    protected String c43AWelshDraftFilename;

    @Value("${document.templates.common.prl_c43_draft_template}")
    protected String c43DraftTemplate;

    @Value("${document.templates.common.prl_c43_draft_filename}")
    protected String c43DraftFile;

    @Value("${document.templates.common.prl_c43_template}")
    protected String c43Template;

    @Value("${document.templates.common.prl_c43_filename}")
    protected String c43File;

    @Value("${document.templates.common.prl_c43_welsh_draft_template}")
    protected String c43WelshDraftTemplate;

    @Value("${document.templates.common.prl_c43_welsh_draft_filename}")
    protected String c43WelshDraftFile;

    @Value("${document.templates.common.prl_c43_welsh_template}")
    protected String c43WelshTemplate;

    @Value("${document.templates.common.prl_c43_welsh_filename}")
    protected String c43WelshFile;

    @Value("${document.templates.common.prl_fl404_draft_template}")
    protected String fl404DraftTemplate;

    @Value("${document.templates.common.prl_fl404_draft_filename}")
    protected String fl404DraftFile;

    @Value("${document.templates.common.prl_fl404_template}")
    protected String fl404Template;

    @Value("${document.templates.common.prl_fl404_filename}")
    protected String fl404File;

    @Value("${document.templates.common.prl_fl404_welsh_draft_template}")
    protected String fl404WelshDraftTemplate;

    @Value("${document.templates.common.prl_fl404_welsh_draft_filename}")
    protected String fl404WelshDraftFile;

    @Value("${document.templates.common.prl_fl404_welsh_template}")
    protected String fl404WelshTemplate;

    @Value("${document.templates.common.prl_fl404_welsh_filename}")
    protected String fl404WelshFile;

    @Value("${document.templates.common.prl_fl404a_draft_template}")
    protected String fl404aDraftTemplate;

    @Value("${document.templates.common.prl_fl404a_draft_filename}")
    protected String fl404aDraftFile;

    @Value("${document.templates.common.prl_fl404a_final_template}")
    protected String fl404aFinalTemplate;

    @Value("${document.templates.common.prl_fl404a_final_filename}")
    protected String fl404aFinalFile;

    @Value("${document.templates.common.prl_fl404a_welsh_draft_template}")
    protected String fl404aWelshDraftTemplate;

    @Value("${document.templates.common.prl_fl404a_welsh_draft_filename}")
    protected String fl404aWelshDraftFile;

    @Value("${document.templates.common.prl_fl404a_welsh_final_template}")
    protected String fl404aWelshFinalTemplate;

    @Value("${document.templates.common.prl_fl404a_welsh_final_filename}")
    protected String fl404aWelshFinalFile;

    @Value("${document.templates.common.prl_c45a_draft_template}")
    protected String c45aDraftTemplate;

    @Value("${document.templates.common.prl_c45a_draft_filename}")
    protected String c45aDraftFile;

    @Value("${document.templates.common.prl_c45a_template}")
    protected String c45aTemplate;

    @Value("${document.templates.common.prl_c45a_filename}")
    protected String c45aFile;

    @Value("${document.templates.common.prl_c45a_welsh_draft_template}")
    protected String c45aWelshDraftTemplate;

    @Value("${document.templates.common.prl_c45a_welsh_draft_filename}")
    protected String c45aWelshDraftFile;

    @Value("${document.templates.common.prl_c45a_welsh_template}")
    protected String c45aWelshTemplate;

    @Value("${document.templates.common.prl_c45a_welsh_filename}")
    protected String c45aWelshFile;

    @Value("${document.templates.common.prl_c47a_draft_template}")
    protected String c47aDraftTemplate;

    @Value("${document.templates.common.prl_c47a_draft_filename}")
    protected String c47aDraftFile;

    @Value("${document.templates.common.prl_c47a_template}")
    protected String c47aTemplate;

    @Value("${document.templates.common.prl_c47a_filename}")
    protected String c47aFile;

    @Value("${document.templates.common.prl_c47a_welsh_draft_template}")
    protected String c47aWelshDraftTemplate;

    @Value("${document.templates.common.prl_c47a_welsh_draft_filename}")
    protected String c47aWelshDraftFile;

    @Value("${document.templates.common.prl_c47a_welsh_template}")
    protected String c47aWelshTemplate;

    @Value("${document.templates.common.prl_c47a_welsh_filename}")
    protected String c47aWelshFile;

    @Value("${document.templates.common.prl_fl402_draft_template}")
    protected String fl402DraftTemplate;

    @Value("${document.templates.common.prl_fl402_draft_filename}")
    protected String fl402DraftFile;

    @Value("${document.templates.common.prl_fl402_final_template}")
    protected String fl402FinalTemplate;

    @Value("${document.templates.common.prl_fl402_final_filename}")
    protected String fl402FinalFile;

    @Value("${document.templates.common.prl_fl402_welsh_draft_template}")
    protected String fl402WelshDraftTemplate;

    @Value("${document.templates.common.prl_fl402_welsh_draft_filename}")
    protected String fl402WelshDraftFile;

    @Value("${document.templates.common.prl_fl402_welsh_final_template}")
    protected String fl402WelshFinalTemplate;

    @Value("${document.templates.common.prl_fl402_welsh_final_filename}")
    protected String fl402WelshFinalFile;

    @Value("${document.templates.common.prl_fl404b_draft_template}")
    protected String fl404bDraftTemplate;

    @Value("${document.templates.common.prl_fl404b_draft_filename}")
    protected String fl404bDraftFile;

    @Value("${document.templates.common.prl_fl404b_welsh_draft_template}")
    protected String fl404bWelshDraftTemplate;

    @Value("${document.templates.common.prl_fl404b_welsh_draft_filename}")
    protected String fl404bWelshDraftFile;

    @Value("${document.templates.common.prl_fl404b_blank_draft_filename}")
    protected String fl404bBlankDraftFile;

    @Value("${document.templates.common.prl_fl404b_final_template}")
    protected String fl404bTemplate;

    @Value("${document.templates.common.prl_fl404b_final_filename}")
    protected String fl404bFile;

    @Value("${document.templates.common.prl_fl404b_welsh_final_template}")
    protected String fl404bWelshTemplate;

    @Value("${document.templates.common.prl_fl404b_welsh_final_filename}")
    protected String fl404bWelshFile;

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

    @Value("${document.templates.common.prl_n117_welsh_draft_template}")
    protected String n117WelshDraftTemplate;

    @Value("${document.templates.common.prl_n117_welsh_draft_filename}")
    protected String n117WelshDraftFile;

    @Value("${document.templates.common.prl_n117_welsh_template}")
    protected String n117WelshTemplate;

    @Value("${document.templates.common.prl_n117_welsh_filename}")
    protected String n117WelshFile;

    private final DocumentLanguageService documentLanguageService;

    public static final String FAMILY_MAN_ID = "Family Man ID: ";

    private final DgsService dgsService;

    private final Time dateTime;

    private final ObjectMapper objectMapper;

    private final ElementUtils elementUtils;

    public Map<String, Object> populateHeader(CaseData caseData) {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("amendOrderDynamicList", getOrdersAsDynamicList(caseData));
        headerMap.put("serveOrderDynamicList", getOrdersAsDynamicList(caseData));
        headerMap.put("caseTypeOfApplication", caseData.getCaseTypeOfApplication());
        return headerMap;
    }

    public CaseData getUpdatedCaseData(CaseData caseData) {
        return caseData.toBuilder().childrenList(getChildInfoFromCaseData(caseData))
            .manageOrders(ManageOrders.builder()
                              .childListForSpecialGuardianship(getChildInfoFromCaseData(caseData)).build())
            .selectedOrder(getSelectedOrderInfo(caseData)).build();
    }

    public Map<String, String> getOrderTemplateAndFile(CreateSelectOrderOptionsEnum selectedOrder) {
        Map<String, String> fieldsMap = new HashMap<>();
        switch (selectedOrder) {
            case blankOrderOrDirections:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, c21TDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, c21DraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, c21Template);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, c21File);
                fieldsMap.put(PrlAppsConstants.DRAFT_TEMPLATE_WELSH, c21DraftWelshTemplate);
                fieldsMap.put(PrlAppsConstants.DRAFT_WELSH_FILE_NAME, c21DraftWelshFileName);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_WELSH, c21WelshTemplate);
                fieldsMap.put(PrlAppsConstants.WELSH_FILE_NAME, c21WelshFileName);
                break;
            case powerOfArrest:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, fl406DraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, fl406DraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, fl406TemplateEnglish);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, fl406FileEnglish);

                fieldsMap.put(PrlAppsConstants.DRAFT_TEMPLATE_WELSH, fl406WelshDraftTemplate);
                fieldsMap.put(PrlAppsConstants.DRAFT_WELSH_FILE_NAME, fl406WelshDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_WELSH, fl406WelshTemplate);
                fieldsMap.put(PrlAppsConstants.WELSH_FILE_NAME, fl406WelshFile);
                break;
            case standardDirectionsOrder:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, sdoDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, sdoDraftFile);
                break;
            case directionOnIssue:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, doiDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, doiDraftFile);
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

                fieldsMap.put(PrlAppsConstants.DRAFT_TEMPLATE_WELSH, c43WelshDraftTemplate);
                fieldsMap.put(PrlAppsConstants.DRAFT_WELSH_FILE_NAME, c43WelshDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_WELSH, c43WelshTemplate);
                fieldsMap.put(PrlAppsConstants.WELSH_FILE_NAME, c43WelshFile);
                break;
            case occupation:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, fl404DraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, fl404DraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, fl404Template);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, fl404File);

                fieldsMap.put(PrlAppsConstants.DRAFT_TEMPLATE_WELSH, fl404WelshDraftTemplate);
                fieldsMap.put(PrlAppsConstants.DRAFT_WELSH_FILE_NAME, fl404WelshDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_WELSH, fl404WelshTemplate);
                fieldsMap.put(PrlAppsConstants.WELSH_FILE_NAME, fl404WelshFile);
                break;
            case specialGuardianShip:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, c43ADraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, c43ADraftFilename);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, c43AFinalTemplate);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, c43AFinalFilename);

                fieldsMap.put(PrlAppsConstants.DRAFT_TEMPLATE_WELSH, c43AWelshDraftTemplate);
                fieldsMap.put(PrlAppsConstants.DRAFT_WELSH_FILE_NAME, c43AWelshDraftFilename);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_WELSH, c43AWelshFinalTemplate);
                fieldsMap.put(PrlAppsConstants.WELSH_FILE_NAME, c43AWelshFinalFilename);
                break;
            case appointmentOfGuardian:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, c47aDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, c47aDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, c47aTemplate);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, c47aFile);
                fieldsMap.put(PrlAppsConstants.DRAFT_TEMPLATE_WELSH, c47aWelshDraftTemplate);
                fieldsMap.put(PrlAppsConstants.DRAFT_WELSH_FILE_NAME, c47aWelshDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_WELSH, c47aWelshTemplate);
                fieldsMap.put(PrlAppsConstants.WELSH_FILE_NAME, c47aWelshFile);
                break;
            case nonMolestation:
                log.info("******** Inside non molestation case ********: ");
                fieldsMap.put(PrlAppsConstants.TEMPLATE, fl404aDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, fl404aDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, fl404aFinalTemplate);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, fl404aFinalFile);

                fieldsMap.put(PrlAppsConstants.DRAFT_TEMPLATE_WELSH, fl404aWelshDraftTemplate);
                fieldsMap.put(PrlAppsConstants.DRAFT_WELSH_FILE_NAME, fl404aWelshDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_WELSH, fl404aWelshFinalTemplate);
                fieldsMap.put(PrlAppsConstants.WELSH_FILE_NAME, fl404aWelshFinalFile);
                break;
            case parentalResponsibility:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, c45aDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, c45aDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, c45aTemplate);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, c45aFile);
                fieldsMap.put(PrlAppsConstants.DRAFT_TEMPLATE_WELSH, c45aWelshDraftTemplate);
                fieldsMap.put(PrlAppsConstants.DRAFT_WELSH_FILE_NAME, c45aWelshDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_WELSH, c45aWelshTemplate);
                fieldsMap.put(PrlAppsConstants.WELSH_FILE_NAME, c45aWelshFile);
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
                fieldsMap.put(PrlAppsConstants.DRAFT_TEMPLATE_WELSH, fl402WelshDraftTemplate);
                fieldsMap.put(PrlAppsConstants.DRAFT_WELSH_FILE_NAME, fl402WelshDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_WELSH, fl402WelshFinalTemplate);
                fieldsMap.put(PrlAppsConstants.WELSH_FILE_NAME, fl402WelshFinalFile);
                break;
            case generalForm:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, n117DraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, n117DraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, n117Template);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, n117File);
                fieldsMap.put(PrlAppsConstants.DRAFT_TEMPLATE_WELSH, n117WelshDraftTemplate);
                fieldsMap.put(PrlAppsConstants.DRAFT_WELSH_FILE_NAME, n117WelshDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_WELSH, n117WelshTemplate);
                fieldsMap.put(PrlAppsConstants.WELSH_FILE_NAME, n117WelshFile);
                break;
            case amendDischargedVaried:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, fl404bDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, fl404bDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, fl404bTemplate);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, fl404bFile);
                fieldsMap.put(PrlAppsConstants.DRAFT_TEMPLATE_WELSH, fl404bWelshDraftTemplate);
                fieldsMap.put(PrlAppsConstants.DRAFT_WELSH_FILE_NAME, fl404bWelshDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_WELSH, fl404bWelshTemplate);
                fieldsMap.put(PrlAppsConstants.WELSH_FILE_NAME, fl404bWelshFile);
                break;
            case blank:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, fl404bDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, fl404bBlankDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, fl404bTemplate);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, fl404bBlankFile);
                break;
            default:
                log.info("******** Inside default case ********: ");
                break;
        }
        log.info("selected order is ********: {}", selectedOrder);
        log.info("fieldsMap is ********: {}", fieldsMap);
        return fieldsMap;
    }

    private String getSelectedOrderInfo(CaseData caseData) {
        StringBuilder selectedOrder = new StringBuilder();
        if (caseData.getManageOrdersOptions() != null) {
            selectedOrder.append(caseData.getManageOrdersOptions() == ManageOrdersOptionsEnum.createAnOrder
                                     ? caseData.getCreateSelectOrderOptions().getDisplayedValue()
                                     : caseData.getChildArrangementOrders().getDisplayedValue());
        } else {
            selectedOrder.append(caseData.getCreateSelectOrderOptions() != null
                                     ? caseData.getCreateSelectOrderOptions().getDisplayedValue() : " ");
        }
        selectedOrder.append("\n\n");
        return selectedOrder.toString();
    }

    private String getChildInfoFromCaseData(CaseData caseData) {
        String childNames = "";
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            List<Child> children = new ArrayList<>();
            if (caseData.getChildren() != null) {
                children = caseData.getChildren().stream()
                    .map(Element::getValue)
                    .collect(Collectors.toList());
            }
            List<String> childList = children.stream()
                .map(element -> element.getFirstName() + " " + element.getLastName())
                .collect(Collectors.toList());
            childNames = String.join(", ", childList);

        } else {
            Optional<List<Element<ApplicantChild>>> applicantChildDetails =
                ofNullable(caseData.getApplicantChildDetails());
            if (applicantChildDetails.isPresent()) {
                List<ApplicantChild> children = applicantChildDetails.get().stream()
                    .map(Element::getValue)
                    .collect(Collectors.toList());
                List<String> childList = children.stream()
                    .map(ApplicantChild::getFullName)
                    .collect(Collectors.toList());
                childNames = String.join(", ", childList);

            }
        }
        return childNames;
    }

    private List<Element<OrderDetails>> getCurrentOrderDetails(String authorisation, CaseData caseData)
        throws Exception {

        String flagSelectedOrder = caseData.getManageOrdersOptions() == ManageOrdersOptionsEnum.createAnOrder
            ? caseData.getCreateSelectOrderOptions().getDisplayedValue()
            : caseData.getChildArrangementOrders().getDisplayedValue();

        String flagSelectedOrderId = null;

        if (caseData.getManageOrdersOptions() == ManageOrdersOptionsEnum.createAnOrder) {
            flagSelectedOrderId = String.valueOf(caseData.getCreateSelectOrderOptions());
        } else {
            flagSelectedOrderId = String.valueOf(caseData.getChildArrangementOrders());
        }
        if (caseData.getCreateSelectOrderOptions() != null && caseData.getDateOrderMade() != null) {
            Map<String, String> fieldMap = getOrderTemplateAndFile(caseData.getCreateSelectOrderOptions());
            List<Element<OrderDetails>> orderCollection = new ArrayList<>();
            DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
            if (documentLanguage.isGenEng()) {
                log.info("*** Generating Final order in English ***");
                orderCollection.add(getOrderDetailsElement(authorisation, flagSelectedOrderId, flagSelectedOrder,
                                                           fieldMap.get(PrlAppsConstants.FINAL_TEMPLATE_NAME),
                                                           fieldMap.get(PrlAppsConstants.GENERATE_FILE_NAME),
                                                           caseData
                ));

            }
            if (documentLanguage.isGenWelsh()) {
                log.info("*** Generating Final order in Welsh ***");
                orderCollection.add(getOrderDetailsElement(authorisation, flagSelectedOrderId, flagSelectedOrder,
                                                           fieldMap.get(FINAL_TEMPLATE_WELSH),
                                                           fieldMap.get(PrlAppsConstants.WELSH_FILE_NAME), caseData
                ));
            }
            return orderCollection;
        } else {
            return List.of(element(OrderDetails.builder().orderType(flagSelectedOrder)
                                       .orderDocument(caseData.getAppointmentOfGuardian())
                                       .otherDetails(OtherOrderDetails.builder()
                                                         .createdBy(caseData.getJudgeOrMagistratesLastName())
                                                         .orderCreatedDate(dateTime.now()
                                                                               .format(DateTimeFormatter.ofPattern(
                                                                                   PrlAppsConstants.D_MMMM_YYYY,
                                                                                   Locale.UK
                                                                               )))
                                                         .orderRecipients(getAllRecipients(caseData)).build())
                                       .dateCreated(dateTime.now())
                                       .build()));
        }
    }


    public String getAllRecipients(CaseData caseData) {
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

    public Map<String, Object> addOrderDetailsAndReturnReverseSortedList(String authorisation, CaseData caseData)
        throws Exception {
        List<Element<OrderDetails>> orderCollection;
        if (!caseData.getManageOrdersOptions().equals(servedSavedOrders)) {
            List<Element<OrderDetails>> orderDetails = getCurrentOrderDetails(authorisation, caseData);
            orderCollection = caseData.getOrderCollection() != null ? caseData.getOrderCollection() : new ArrayList<>();
            orderCollection.addAll(orderDetails);
            orderCollection.sort(Comparator.comparing(m -> m.getValue().getDateCreated(), Comparator.reverseOrder()));
            return Map.of("orderCollection", orderCollection);
        } else {
            UUID selectedOrderId = caseData.getManageOrders().getServeOrderDynamicList().getValueCodeAsUuid();
            List<Element<OrderDetails>> orders = caseData.getOrderCollection();

            orders.stream()
                .filter(order -> Objects.equals(order.getId(), selectedOrderId))
                .findFirst()
                .ifPresent(order -> {
                    if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                        servedC100Order(caseData, orders, order);
                    } else {
                        servedFL401Order(caseData, orders, order);
                    }
                });
            return Map.of("orderCollection", orders);
        }
    }

    private static void servedFL401Order(CaseData caseData, List<Element<OrderDetails>> orders, Element<OrderDetails> order) {
        ServingRespondentsEnum servingRespondentsOptions = caseData.getManageOrders()
            .getServingRespondentsOptionsDA();
        YesOrNo otherPartiesServed = No;
        List<Element<PostalInformation>> postalInformation = null;
        List<Element<EmailInformation>> emailInformation = null;
        if (!caseData.getManageOrders().getServeOtherPartiesDA().isEmpty()) {
            otherPartiesServed = Yes;
            if (caseData.getManageOrders().getEmailInformationDA() != null) {
                emailInformation = caseData.getManageOrders().getEmailInformationDA();
            }
            if (caseData.getManageOrders().getPostalInformationDA() != null) {
                postalInformation = caseData.getManageOrders().getPostalInformationDA();
            }
        }
        Map<String, Object> servedOrderDetails = new HashMap<>();
        servedOrderDetails.put(OTHER_PARTIES_SERVED, otherPartiesServed);
        servedOrderDetails.put(SERVING_RESPONDENTS_OPTIONS, servingRespondentsOptions);

        updateServedOrderDetails(
            servedOrderDetails,
            null,
            orders,
            order,
            postalInformation,
            emailInformation,
            caseData.getManageOrders().getServeOrderAdditionalDocuments()
        );
    }

    private static void servedC100Order(CaseData caseData, List<Element<OrderDetails>> orders, Element<OrderDetails> order) {
        YesOrNo serveOnRespondent = caseData.getManageOrders().getServeToRespondentOptions();
        ServingRespondentsEnum servingRespondentsOptions = null;
        if (serveOnRespondent.equals(Yes)) {
            servingRespondentsOptions = caseData.getManageOrders()
                .getServingRespondentsOptionsCA();
        }
        YesOrNo otherPartiesServed = No;
        List<Element<PostalInformation>> postalInformation = null;
        List<Element<EmailInformation>> emailInformation = null;
        if (!caseData.getManageOrders().getServeOtherPartiesCA().isEmpty()) {
            otherPartiesServed = Yes;
            if (caseData.getManageOrders().getEmailInformationCA() != null) {
                emailInformation = caseData.getManageOrders().getEmailInformationCA();
            }
            if (caseData.getManageOrders().getPostalInformationCA() != null) {
                postalInformation = caseData.getManageOrders().getPostalInformationCA();
            }
        }
        YesOrNo cafcassServedOptions;
        String cafCassEmail = null;
        if (caseData.getManageOrders().getCafcassServedOptions() != null) {
            cafcassServedOptions = caseData.getManageOrders().getCafcassServedOptions();
        } else if (caseData.getManageOrders().getCafcassCymruServedOptions() != null) {
            cafcassServedOptions = caseData.getManageOrders().getCafcassCymruServedOptions();
            if (No.equals(caseData.getManageOrders().getCafcassCymruServedOptions())) {
                cafCassEmail = caseData.getManageOrders().getCafcassCymruEmail();
            }
        } else {
            cafcassServedOptions = No;
        }

        Map<String, Object> servedOrderDetails = new HashMap<>();
        servedOrderDetails.put(CAFCASS_SERVED, cafcassServedOptions);
        servedOrderDetails.put(SERVE_ON_RESPONDENT, serveOnRespondent);
        servedOrderDetails.put(OTHER_PARTIES_SERVED, otherPartiesServed);
        servedOrderDetails.put(SERVING_RESPONDENTS_OPTIONS, servingRespondentsOptions);

        updateServedOrderDetails(
            servedOrderDetails,
            cafCassEmail,
            orders,
            order,
            postalInformation,
            emailInformation,
            caseData.getManageOrders().getServeOrderAdditionalDocuments()
        );
    }

    private static void updateServedOrderDetails(Map<String, Object> servedOrderDetails, String cafCassEmail, List<Element<OrderDetails>> orders,
                                                 Element<OrderDetails> order, List<Element<PostalInformation>> postalInformation,
                                                 List<Element<EmailInformation>> emailInformation, List<Element<Document>> additionalDocuments) {

        YesOrNo cafcassServed = null;
        YesOrNo serveOnRespondent = null;
        YesOrNo otherPartiesServed = null;
        ServingRespondentsEnum servingRespondentsOptions = null;

        if (servedOrderDetails.containsKey(CAFCASS_SERVED)) {
            cafcassServed = (YesOrNo) servedOrderDetails.get(CAFCASS_SERVED);
        }
        if (servedOrderDetails.containsKey(SERVE_ON_RESPONDENT)) {
            serveOnRespondent = (YesOrNo) servedOrderDetails.get(SERVE_ON_RESPONDENT);
        }
        if (servedOrderDetails.containsKey(OTHER_PARTIES_SERVED)) {
            otherPartiesServed = (YesOrNo) servedOrderDetails.get(OTHER_PARTIES_SERVED);
        }
        if (servedOrderDetails.containsKey(SERVING_RESPONDENTS_OPTIONS)) {
            servingRespondentsOptions = (ServingRespondentsEnum) servedOrderDetails.get(SERVING_RESPONDENTS_OPTIONS);
        }

        ServeOrderDetails serveOrderDetails = ServeOrderDetails.builder().serveOnRespondent(serveOnRespondent)
            .servingRespondent(servingRespondentsOptions)
            .cafcassServed(cafcassServed)
            .cafcassEmail(cafCassEmail)
            .otherPartiesServed(otherPartiesServed)
            .postalInformation(postalInformation)
            .emailInformation(emailInformation)
            .additionalDocuments(additionalDocuments)
            .build();

        OrderDetails amended = order.getValue().toBuilder()
            .orderDocument(order.getValue().getOrderDocument())
            .orderType(order.getValue().getOrderType())
            .typeOfOrder(order.getValue().getTypeOfOrder())
            .otherDetails(updateOtherOrderDetails(order.getValue().getOtherDetails()))
            .dateCreated(order.getValue().getDateCreated())
            .orderTypeId(order.getValue().getOrderTypeId())
            .serveOrderDetails(serveOrderDetails)
            .build();
        log.info("amaneded order {}" + amended);
        orders.set(orders.indexOf(order), element(order.getId(), amended));
    }

    private static OtherOrderDetails updateOtherOrderDetails(OtherOrderDetails otherDetails) {
        return OtherOrderDetails.builder()
            .createdBy(otherDetails.getCreatedBy())
            .orderCreatedDate(otherDetails.getOrderCreatedDate())
            .orderAmendedDate(otherDetails.getOrderAmendedDate())
            .orderMadeDate(otherDetails.getOrderMadeDate())
            .orderRecipients(otherDetails.getOrderRecipients())
            .orderServedDate(LocalDate.now().format(DateTimeFormatter.ofPattern(
                PrlAppsConstants.D_MMMM_YYYY,
                Locale.UK
            )))
            .build();
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

    public Map<String, Object> getCaseData(String authorisation, CaseData caseData, CreateSelectOrderOptionsEnum selectOrderOption)
        throws Exception {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        GeneratedDocumentInfo generatedDocumentInfo = null;
        Map<String, String> fieldsMap = getOrderTemplateAndFile(selectOrderOption);
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        if (documentLanguage.isGenEng()) {
            caseDataUpdated.put("isEngDocGen", Yes.toString());
            generatedDocumentInfo = dgsService.generateDocument(
                authorisation,
                CaseDetails.builder().caseData(caseData).build(),
                fieldsMap.get(PrlAppsConstants.TEMPLATE)
            );
            caseDataUpdated.put("previewOrderDoc", Document.builder()
                .documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentFileName(fieldsMap.get(PrlAppsConstants.FILE_NAME)).build());

        }
        if (documentLanguage.isGenWelsh()) {
            caseDataUpdated.put("isWelshDocGen", Yes.toString());
            generatedDocumentInfo = dgsService.generateWelshDocument(
                authorisation,
                CaseDetails.builder().caseData(caseData).build(),
                fieldsMap.get(PrlAppsConstants.DRAFT_TEMPLATE_WELSH)
            );
            caseDataUpdated.put("previewOrderDocWelsh", Document.builder()
                .documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentFileName(fieldsMap.get(PrlAppsConstants.DRAFT_WELSH_FILE_NAME)).build());

        }
        return caseDataUpdated;
    }

    private CaseData getN117FormData(CaseData caseData) {

        log.info("Court name before N117 order {}", caseData.getCourtName());

        ManageOrders orderData = ManageOrders.builder()
            .manageOrdersCaseNo(String.valueOf(caseData.getId()))
            .recitalsOrPreamble(caseData.getManageOrders().getRecitalsOrPreamble())
            .isCaseWithdrawn(caseData.getManageOrders().getIsCaseWithdrawn())
            .isTheOrderByConsent(caseData.getManageOrders().getIsTheOrderByConsent())
            .judgeOrMagistrateTitle(caseData.getManageOrders().getJudgeOrMagistrateTitle())
            .isOrderDrawnForCafcass(caseData.getManageOrders().getIsOrderDrawnForCafcass())
            .orderDirections(caseData.getManageOrders().getOrderDirections())
            .furtherDirectionsIfRequired(caseData.getManageOrders().getFurtherDirectionsIfRequired())
            .manageOrdersCourtName(null != caseData.getCourtName() ? caseData.getCourtName() : null)
            .manageOrdersApplicant(String.format(PrlAppsConstants.FORMAT, caseData.getApplicantsFL401().getFirstName(),
                                                 caseData.getApplicantsFL401().getLastName()
            ))
            .manageOrdersRespondent(String.format(
                PrlAppsConstants.FORMAT,
                caseData.getRespondentsFL401().getFirstName(),
                caseData.getRespondentsFL401().getLastName()
            ))
            .manageOrdersApplicantReference(String.format(
                PrlAppsConstants.FORMAT,
                caseData.getApplicantsFL401().getRepresentativeFirstName(),
                caseData.getApplicantsFL401().getRepresentativeLastName()
            ))
            .build();

        log.info("Court name after N117 order set{}", orderData.getManageOrdersCourtName());

        if (ofNullable(caseData.getRespondentsFL401().getAddress()).isPresent()) {
            orderData = orderData.toBuilder()
                .manageOrdersRespondentAddress(caseData.getRespondentsFL401().getAddress()).build();
        }
        if (ofNullable(caseData.getRespondentsFL401().getDateOfBirth()).isPresent()) {
            orderData = orderData.toBuilder()
                .manageOrdersRespondentDob(caseData.getRespondentsFL401().getDateOfBirth()).build();
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
            orderData = orderData.toBuilder()
                .fl404bRespondentAddress(caseData.getRespondentsFL401().getAddress()).build();
        }
        if (ofNullable(caseData.getRespondentsFL401().getDateOfBirth()).isPresent()) {
            orderData = orderData.toBuilder()
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
                              .fl404CustomFields(orderData)
                              .build())
            .selectedOrder(getSelectedOrderInfo(caseData)).build();
        return caseData;
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
        ManageOrders orderData = ManageOrders.builder()
            .manageOrdersFl402CaseNo(String.valueOf(caseData.getId()))
            .manageOrdersFl402CourtName(caseData.getCourtName())
            .manageOrdersFl402Applicant(String.format(
                PrlAppsConstants.FORMAT,
                caseData.getApplicantsFL401().getFirstName(),
                caseData.getApplicantsFL401().getLastName()
            ))
            .manageOrdersFl402ApplicantRef(String.format(
                PrlAppsConstants.FORMAT,
                caseData.getApplicantsFL401().getRepresentativeFirstName(),
                caseData.getApplicantsFL401().getRepresentativeLastName()
            ))
            .isTheOrderByConsent(caseData.getManageOrders().getIsTheOrderByConsent())
            .judgeOrMagistrateTitle(caseData.getManageOrders().getJudgeOrMagistrateTitle())
            .recitalsOrPreamble(caseData.getManageOrders().getRecitalsOrPreamble())
            .furtherDirectionsIfRequired(caseData.getManageOrders().getFurtherDirectionsIfRequired())
            .orderDirections(caseData.getManageOrders().getOrderDirections())
            .build();
        log.info("Court name after FL402 order set{}", orderData.getManageOrdersFl402CourtName());


        return caseData.toBuilder().manageOrders(orderData)
            .selectedOrder(getSelectedOrderInfo(caseData)).build();
    }

    private Element<OrderDetails> getOrderDetailsElement(String authorisation, String flagSelectedOrderId,
                                                         String flagSelectedOrder, String template, String fileName,
                                                         CaseData caseData) throws Exception {
        log.info("Generating document for {}, {}", FINAL_TEMPLATE_WELSH, template);
        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder().build();
        if (template != null) {
            generatedDocumentInfo = template.contains("-WEL-") ? dgsService.generateWelshDocument(
                authorisation,
                CaseDetails.builder().caseData(caseData).build(),
                template
            ) : dgsService.generateDocument(
                authorisation,
                CaseDetails.builder().caseData(caseData).build(),
                template
            );
        }

        return element(OrderDetails.builder().orderType(flagSelectedOrder)
                           .orderTypeId(flagSelectedOrderId)
                           .withdrawnRequestType(null != caseData.getManageOrders().getWithdrawnOrRefusedOrder()
                                                 ? caseData.getManageOrders().getWithdrawnOrRefusedOrder().getDisplayedValue() : null)
                           .isWithdrawnRequestApproved(getWithdrawRequestInfo(caseData))
                           .typeOfOrder(caseData.getSelectTypeOfOrder() != null
                                            ? caseData.getSelectTypeOfOrder().getDisplayedValue() : null)
                           .childrenList(getChildInfoFromCaseData(caseData))
                           .orderClosesCase(caseData.getSelectTypeOfOrder().getDisplayedValue().equals("Final")
                                                ? caseData.getDoesOrderClosesCase() : null)
                           .orderDocument(Document.builder()
                                              .documentUrl(generatedDocumentInfo.getUrl())
                                              .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                              .documentHash(generatedDocumentInfo.getHashToken())
                                              .documentFileName(fileName).build())
                           .otherDetails(OtherOrderDetails.builder()
                                             .createdBy(caseData.getJudgeOrMagistratesLastName())
                                             .orderCreatedDate(dateTime.now().format(DateTimeFormatter.ofPattern(
                                                 PrlAppsConstants.D_MMMM_YYYY,
                                                 Locale.UK
                                             )))
                                             .orderMadeDate(caseData.getDateOrderMade()
                                                                .format(DateTimeFormatter.ofPattern(
                                                                    PrlAppsConstants.D_MMMM_YYYY,
                                                                    Locale.UK
                                                                )))
                                             .orderRecipients(getAllRecipients(caseData)).build())
                           .dateCreated(dateTime.now())
                           .build());
    }

    private String getWithdrawRequestInfo(CaseData caseData) {
        String withdrawApproved = "";

        if (null != caseData.getManageOrders().getWithdrawnOrRefusedOrder()
            && caseData.getManageOrders().getWithdrawnOrRefusedOrder().getDisplayedValue().equals("Withdrawn application")) {
            withdrawApproved = String.valueOf(caseData.getManageOrders().getIsCaseWithdrawn());
        }

        return withdrawApproved;
    }
}
