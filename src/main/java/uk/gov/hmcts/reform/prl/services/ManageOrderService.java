package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.ManageOrderFieldsEnum;
import uk.gov.hmcts.reform.prl.enums.OrderStatusEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.AmendOrderCheckEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serveorder.WhatToDoWithOrderEnum;
import uk.gov.hmcts.reform.prl.exception.ManageOrderRuntimeException;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.models.SdoDetails;
import uk.gov.hmcts.reform.prl.models.ServeOrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.AppointedGuardianFullName;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.ServedParties;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.EmailInformation;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.PostalInformation;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServeOrderData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.StandardDirectionOrder;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WelshCourtEmail;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.apache.logging.log4j.util.Strings.concat;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FINAL_TEMPLATE_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HMC_STATUS_COMPLETED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HYPHEN_SEPARATOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum.blankOrderOrDirections;
import static uk.gov.hmcts.reform.prl.enums.manageorders.DraftOrderOptionsEnum.draftAnOrder;
import static uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum.amendOrderUnderSlipRule;
import static uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum.createAnOrder;
import static uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum.servedSavedOrders;
import static uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum.uploadAnOrder;
import static uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum.applicantOrApplicantSolicitor;
import static uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum.respondentOrRespondentSolicitor;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({"java:S3776","java:S6204"})
public class ManageOrderService {

    public static final String IS_THE_ORDER_ABOUT_CHILDREN = "isTheOrderAboutChildren";

    public static final String IS_THE_ORDER_ABOUT_ALL_CHILDREN = "isTheOrderAboutAllChildren";

    public static final String CHILD_OPTION = "childOption";

    public static final String IS_ONLY_C_47_A_ORDER_SELECTED_TO_SERVE = "isOnlyC47aOrderSelectedToServe";
    public static final String OTHER_PEOPLE_PRESENT_IN_CASE_FLAG = "otherPeoplePresentInCaseFlag";
    public static final String C_47_A = "C47A";
    public static final String RECIPIENTS_OPTIONS_ONLY_C_47_A = "recipientsOptionsOnlyC47a";
    public static final String OTHER_PARTIES_ONLY_C_47_A = "otherPartiesOnlyC47a";
    @Autowired
    LocationRefDataService locationRefDataService;

    public static final String CAFCASS_SERVED = "cafcassServed";
    public static final String CAFCASS_EMAIL = "cafcassEmail";
    public static final String CAFCASS_CYMRU_SERVED = "cafcassCymruServed";
    public static final String SERVE_ON_RESPONDENT = "serveOnRespondent";
    public static final String OTHER_PARTIES_SERVED = "otherPartiesServed";
    public static final String SERVING_RESPONDENTS_OPTIONS = "servingRespondentsOptions";

    public static final String RECIPIENTS_OPTIONS = "recipientsOptions";

    public static final String OTHER_PARTIES = "otherParties";
    public static final String SERVED_PARTIES = "servedParties";

    @Value("${document.templates.common.prl_sdo_draft_template}")
    protected String sdoDraftTemplate;

    @Value("${document.templates.common.prl_sdo_draft_filename}")
    protected String sdoDraftFile;

    @Value("${document.templates.common.prl_sdo_template}")
    protected String sdoTemplate;

    @Value("${document.templates.common.prl_sdo_filename}")
    protected String sdoFile;

    @Value("${document.templates.common.prl_sdo_welsh_draft_template}")
    protected String sdoWelshDraftTemplate;

    @Value("${document.templates.common.prl_sdo_welsh_draft_filename}")
    protected String sdoWelshDraftFile;

    @Value("${document.templates.common.prl_sdo_welsh_template}")
    protected String sdoWelshTemplate;

    @Value("${document.templates.common.prl_sdo_welsh_filename}")
    protected String sdoWelshFile;

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

    @Value("${document.templates.common.prl_c6_draft_template}")
    protected String nopPartiesDraftTemplate;

    @Value("${document.templates.common.prl_c6_draft_filename}")
    protected String nopPartiesDraftFile;

    @Value("${document.templates.common.prl_c6_template}")
    protected String nopPartiesTemplate;

    @Value("${document.templates.common.prl_c6_filename}")
    protected String nopPartiesFile;

    @Value("${document.templates.common.prl_c6_welsh_draft_template}")
    protected String nopPartiesWelshDraftTemplate;

    @Value("${document.templates.common.prl_c6_welsh_draft_filename}")
    protected String nopPartiesWelshDraftFile;

    @Value("${document.templates.common.prl_c6_welsh_template}")
    protected String nopPartiesWelshTemplate;

    @Value("${document.templates.common.prl_c6_welsh_filename}")
    protected String nopPartiesWelshFile;


    @Value("${document.templates.common.prl_c6a_draft_template}")
    protected String nopNonPartiesDraftTemplate;

    @Value("${document.templates.common.prl_c6a_draft_filename}")
    protected String nopNonPartiesDraftFile;

    @Value("${document.templates.common.prl_c6a_template}")
    protected String nopNonPartiesTemplate;

    @Value("${document.templates.common.prl_c6a_filename}")
    protected String nopNonPartiesFile;

    @Value("${document.templates.common.prl_c6a_welsh_draft_template}")
    protected String nopNonPartiesWelshDraftTemplate;

    @Value("${document.templates.common.prl_c6a_welsh_draft_filename}")
    protected String nopNonPartiesWelshDraftFile;

    @Value("${document.templates.common.prl_c6a_welsh_template}")
    protected String nopNonPartiesWelshTemplate;

    @Value("${document.templates.common.prl_c6a_welsh_filename}")
    protected String nopNonPartiesWelshFile;

    private final DocumentLanguageService documentLanguageService;

    public static final String FAMILY_MAN_ID = "Family Man ID: ";

    private final DgsService dgsService;

    private final DynamicMultiSelectListService dynamicMultiSelectListService;

    private final Time dateTime;

    private final ObjectMapper objectMapper;

    private final ElementUtils elementUtils;

    private final RefDataUserService refDataUserService;

    @Autowired
    private final UserService userService;

    @Autowired
    private final HearingService hearingService;

    @Autowired
    private final WelshCourtEmail welshCourtEmail;


    public Map<String, Object> populateHeader(CaseData caseData) {
        Map<String, Object> headerMap = new HashMap<>();
        if (caseData.getOrderCollection() != null) {
            headerMap.put("amendOrderDynamicList", getOrdersAsDynamicList(caseData));
            populateServeOrderDetails(caseData, headerMap);
        }
        headerMap.put(
            CASE_TYPE_OF_APPLICATION,
            CaseUtils.getCaseTypeOfApplication(caseData)
        );
        return headerMap;
    }

    public void populateServeOrderDetails(CaseData caseData, Map<String, Object> headerMap) {
        headerMap.put(
            "serveOrderDynamicList",
            dynamicMultiSelectListService.getOrdersAsDynamicMultiSelectList(caseData)
        );
        populateOtherServeOrderDetails(caseData, headerMap);
    }

    private void populateOtherServeOrderDetails(CaseData caseData, Map<String, Object> headerMap) {
        if (CaseUtils.getCaseTypeOfApplication(caseData).equalsIgnoreCase(C100_CASE_TYPE)) {
            setRecipientsOptions(caseData, headerMap);
            setOtherParties(caseData, headerMap);
            if (caseData.getIsCafcass() != null) {
                headerMap.put(
                    PrlAppsConstants.IS_CAFCASS,
                    caseData.getIsCafcass()
                );
            } else if (caseData.getCaseManagementLocation() != null) {
                headerMap.put(
                    PrlAppsConstants.IS_CAFCASS,
                    CaseUtils.cafcassFlag(caseData.getCaseManagementLocation().getRegion())
                );
            } else {
                headerMap.put(PrlAppsConstants.IS_CAFCASS, No);
            }
        } else {
            headerMap.put(PrlAppsConstants.IS_CAFCASS, No);
        }
    }

    private void setRecipientsOptions(CaseData caseData, Map<String, Object> headerMap) {

        Map<String, List<DynamicMultiselectListElement>> applicantDetails = dynamicMultiSelectListService
            .getApplicantsMultiSelectList(caseData);
        List<DynamicMultiselectListElement> applicantRespondentList = new ArrayList<>();
        List<DynamicMultiselectListElement> applicantList = applicantDetails.get("applicants");
        if (applicantList != null) {
            applicantRespondentList.addAll(applicantList);
        }
        Map<String, List<DynamicMultiselectListElement>> respondentDetails = dynamicMultiSelectListService
            .getRespondentsMultiSelectList(caseData);
        List<DynamicMultiselectListElement> respondentList = respondentDetails.get("respondents");
        if (respondentList != null) {
            applicantRespondentList.addAll(respondentList);
        }
        headerMap.put(
            RECIPIENTS_OPTIONS, DynamicMultiSelectList.builder()
                .listItems(applicantRespondentList)
                .build());
        headerMap.put(
            RECIPIENTS_OPTIONS_ONLY_C_47_A, DynamicMultiSelectList.builder()
                .listItems(applicantRespondentList)
                .build());

    }

    private void setOtherParties(CaseData caseData, Map<String, Object> headerMap) {
        List<DynamicMultiselectListElement> otherPeopleList = dynamicMultiSelectListService
            .getOtherPeopleMultiSelectList(caseData);
        headerMap.put(
            OTHER_PARTIES, DynamicMultiSelectList.builder()
                .listItems(otherPeopleList)
                .build());
        headerMap.put(
            OTHER_PARTIES_ONLY_C_47_A, DynamicMultiSelectList.builder()
                .listItems(otherPeopleList)
                .build());
        if (otherPeopleList.isEmpty()) {
            headerMap.put(
                OTHER_PEOPLE_PRESENT_IN_CASE_FLAG, No);
        } else {
            headerMap.put(
                OTHER_PEOPLE_PRESENT_IN_CASE_FLAG, Yes);
        }
    }

    public Map<String, Object> getUpdatedCaseData(CaseData caseData) {
        Map<String, Object> caseDataUpdated = new HashMap<>();

        caseDataUpdated.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        caseDataUpdated.put("childrenList", dynamicMultiSelectListService
                              .getStringFromDynamicMultiSelectList(caseData.getManageOrders()
                                                                       .getChildOption()));
        caseDataUpdated.put("childListForSpecialGuardianship", dynamicMultiSelectListService
                                                                   .getStringFromDynamicMultiSelectList(caseData.getManageOrders()
                                                                                                            .getChildOption()));
        caseDataUpdated.put("selectedOrder", getSelectedOrderInfo(caseData));
        return caseDataUpdated;
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
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, sdoTemplate);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, sdoFile);
                fieldsMap.put(PrlAppsConstants.DRAFT_TEMPLATE_WELSH, sdoWelshDraftTemplate);
                fieldsMap.put(PrlAppsConstants.DRAFT_WELSH_FILE_NAME, sdoWelshDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_WELSH, sdoWelshTemplate);
                fieldsMap.put(PrlAppsConstants.WELSH_FILE_NAME, sdoWelshFile);
                break;
            case directionOnIssue:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, doiDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, doiDraftFile);
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
            case noticeOfProceedingsParties:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, nopPartiesDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, nopPartiesDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, nopPartiesTemplate);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, nopPartiesFile);
                fieldsMap.put(PrlAppsConstants.DRAFT_TEMPLATE_WELSH, nopPartiesWelshDraftTemplate);
                fieldsMap.put(PrlAppsConstants.DRAFT_WELSH_FILE_NAME, nopPartiesWelshDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_WELSH, nopPartiesWelshTemplate);
                fieldsMap.put(PrlAppsConstants.WELSH_FILE_NAME, nopPartiesWelshFile);
                break;
            case noticeOfProceedingsNonParties:
                fieldsMap.put(PrlAppsConstants.TEMPLATE, nopNonPartiesDraftTemplate);
                fieldsMap.put(PrlAppsConstants.FILE_NAME, nopNonPartiesDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_NAME, nopNonPartiesTemplate);
                fieldsMap.put(PrlAppsConstants.GENERATE_FILE_NAME, nopNonPartiesFile);
                fieldsMap.put(PrlAppsConstants.DRAFT_TEMPLATE_WELSH, nopNonPartiesWelshDraftTemplate);
                fieldsMap.put(PrlAppsConstants.DRAFT_WELSH_FILE_NAME, nopNonPartiesWelshDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_WELSH, nopNonPartiesWelshTemplate);
                fieldsMap.put(PrlAppsConstants.WELSH_FILE_NAME, nopNonPartiesWelshFile);
                break;
            default:
                break;
        }
        return fieldsMap;
    }

    public String getSelectedOrderInfoForUpload(CaseData caseData) {
        String selectedOrder;
        if (caseData.getChildArrangementOrders() != null) {
            selectedOrder = caseData.getChildArrangementOrders().getDisplayedValue();
        } else if (caseData.getDomesticAbuseOrders() != null) {
            selectedOrder = caseData.getDomesticAbuseOrders().getDisplayedValue();
        } else if (caseData.getFcOrders() != null) {
            selectedOrder = caseData.getFcOrders().getDisplayedValue();
        } else if (caseData.getOtherOrdersOption() != null) {
            selectedOrder = caseData.getOtherOrdersOption().getDisplayedValue();
        } else {
            selectedOrder = "";
        }
        return selectedOrder;
    }

    private String getSelectedOrderInfo(CaseData caseData) {
        StringBuilder selectedOrder = new StringBuilder();
        if (caseData.getManageOrdersOptions() != null) {
            selectedOrder.append(caseData.getManageOrdersOptions() == ManageOrdersOptionsEnum.createAnOrder
                                     ? caseData.getCreateSelectOrderOptions().getDisplayedValue()
                                     : getSelectedOrderInfoForUpload(caseData));
        } else {
            selectedOrder.append(caseData.getCreateSelectOrderOptions() != null
                                     ? caseData.getCreateSelectOrderOptions().getDisplayedValue() : " ");
        }
        selectedOrder.append("\n\n");
        return selectedOrder.toString();
    }

    private List<Element<OrderDetails>> getCurrentOrderDetails(String authorisation, CaseData caseData)
        throws Exception {

        String flagSelectedOrder = caseData.getManageOrdersOptions() == ManageOrdersOptionsEnum.createAnOrder
            ? caseData.getCreateSelectOrderOptions().getDisplayedValue()
            : getSelectedOrderInfoForUpload(caseData);

        String flagSelectedOrderId;

        if (caseData.getManageOrdersOptions() == ManageOrdersOptionsEnum.createAnOrder) {
            flagSelectedOrderId = String.valueOf(caseData.getCreateSelectOrderOptions());
        } else {
            flagSelectedOrderId = getSelectedOrderInfoForUpload(caseData);
        }
        if (caseData.getCreateSelectOrderOptions() != null
            && !uploadAnOrder.equals(caseData.getManageOrdersOptions())) {
            Map<String, String> fieldMap = getOrderTemplateAndFile(caseData.getCreateSelectOrderOptions());
            List<Element<OrderDetails>> orderCollection = new ArrayList<>();
            if (FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                caseData = populateCustomOrderFields(caseData);
            }
            if (CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(caseData.getCreateSelectOrderOptions())) {
                caseData = populateJudgeName(authorisation, caseData);
            }
            orderCollection.add(getOrderDetailsElement(authorisation, flagSelectedOrderId, flagSelectedOrder,
                                                       fieldMap, caseData
            ));

            return orderCollection;
        } else {
            return getListOfOrders(authorisation, caseData, flagSelectedOrder, flagSelectedOrderId);
        }
    }

    private List<Element<OrderDetails>> getListOfOrders(String authorisation,
                                                        CaseData caseData,
                                                        String flagSelectedOrder,
                                                        String flagSelectedOrderId) {
        ServeOrderData serveOrderData = CaseUtils.getServeOrderData(caseData);
        String loggedInUserType = getLoggedInUserType(authorisation);
        SelectTypeOfOrderEnum typeOfOrder = CaseUtils.getSelectTypeOfOrder(caseData);
        String orderSelectionType = CaseUtils.getOrderSelectionType(caseData);

        return List.of(element(OrderDetails.builder().orderType(flagSelectedOrder)
                                   .orderTypeId(flagSelectedOrderId)
                                   .orderDocument(caseData.getUploadOrderDoc())
                                   .isTheOrderAboutChildren(caseData.getManageOrders().getIsTheOrderAboutChildren())
                                   .isTheOrderAboutAllChildren(caseData.getManageOrders().getIsTheOrderAboutAllChildren())
                                   .childrenList(getSelectedChildInfoFromMangeOrder(caseData))
                                   .otherDetails(OtherOrderDetails.builder()
                                                     .createdBy(caseData.getJudgeOrMagistratesLastName())
                                                     .orderCreatedDate(dateTime.now()
                                                                           .format(DateTimeFormatter.ofPattern(
                                                                               PrlAppsConstants.D_MMMM_YYYY,
                                                                               Locale.UK
                                                                           )))
                                                     .orderMadeDate(caseData.getDateOrderMade() != null ? caseData.getDateOrderMade().format(
                                                         DateTimeFormatter.ofPattern(
                                                             PrlAppsConstants.D_MMMM_YYYY,
                                                             Locale.UK
                                                         )) : null)
                                                     .approvalDate(caseData.getApprovalDate() != null ? caseData.getApprovalDate().format(
                                                         DateTimeFormatter.ofPattern(
                                                             PrlAppsConstants.D_MMMM_YYYY,
                                                             Locale.UK
                                                         )) : null)
                                                     .orderRecipients(caseData.getManageOrdersOptions().equals(
                                                         ManageOrdersOptionsEnum.createAnOrder) ? getAllRecipients(
                                                         caseData) : null)
                                                     .status(getOrderStatus(
                                                         orderSelectionType,
                                                         loggedInUserType,
                                                         null,
                                                         null
                                                     ))
                                                     .build())
                                   .dateCreated(caseData.getManageOrders().getCurrentOrderCreatedDateTime() != null
                                                    ? caseData.getManageOrders().getCurrentOrderCreatedDateTime() : dateTime.now())
                                   .typeOfOrder(typeOfOrder != null
                                                    ? typeOfOrder.getDisplayedValue() : null)
                                   .orderClosesCase(SelectTypeOfOrderEnum.finl.equals(typeOfOrder)
                                                        ? caseData.getDoesOrderClosesCase() : null)
                                   .serveOrderDetails(buildServeOrderDetails(serveOrderData))
                                   .selectedHearingType(null != caseData.getManageOrders().getHearingsType()
                                                            ? caseData.getManageOrders().getHearingsType().getValueCode() : null)
                                   .childOption(getChildOption(caseData))
                                   .build()));
    }

    public static ServeOrderDetails buildServeOrderDetails(ServeOrderData serveOrderData) {
        return ServeOrderDetails.builder()
            .cafcassOrCymruNeedToProvideReport(
                serveOrderData.getCafcassOrCymruNeedToProvideReport())
            .cafcassCymruDocuments(serveOrderData.getCafcassCymruDocuments())
            .whenReportsMustBeFiled(serveOrderData.getWhenReportsMustBeFiled() != null
                                        ? serveOrderData.getWhenReportsMustBeFiled()
                .format(DateTimeFormatter.ofPattern(
                    PrlAppsConstants.D_MMMM_YYYY,
                    Locale.UK
                )) : null)
            .orderEndsInvolvementOfCafcassOrCymru(
                serveOrderData.getOrderEndsInvolvementOfCafcassOrCymru())
            .build();
    }

    public String getSelectedChildInfoFromMangeOrder(CaseData caseData) {
        DynamicMultiSelectList childOption = caseData.getManageOrders().getChildOption();
        if ((YesOrNo.Yes.equals(caseData.getManageOrders().getIsTheOrderAboutChildren())
            || YesOrNo.No.equals(caseData.getManageOrders().getIsTheOrderAboutAllChildren()))
            && childOption != null) {
            return getChildNames(childOption.getValue());
        } else if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
            && childOption != null && childOption.getListItems() != null) {
            return getChildNames(childOption.getListItems());
        }
        return null;
    }

    private static String getChildNames(List<DynamicMultiselectListElement> dynamicMultiselectListElements) {
        List<String> childList;
        String selectedChildNames;
        childList = new ArrayList<>();
        for (DynamicMultiselectListElement dynamicMultiselectChildElement : dynamicMultiselectListElements) {
            childList.add(dynamicMultiselectChildElement.getLabel());
        }
        selectedChildNames = String.join(",", childList);
        return selectedChildNames;
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
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
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
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
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
        String loggedInUserType = getLoggedInUserType(authorisation);

        Map<String, Object> orderMap = new HashMap<>();

        if (!servedSavedOrders.equals(caseData.getManageOrdersOptions())) {
            if (uploadAnOrder.equals(caseData.getManageOrdersOptions())
                && (UserRoles.JUDGE.name().equals(loggedInUserType) || (No.equals(caseData.getServeOrderData().getDoYouWantToServeOrder())
                && WhatToDoWithOrderEnum.saveAsDraft.equals(caseData.getServeOrderData().getWhatDoWithOrder())))) {
                return setDraftOrderCollection(caseData, loggedInUserType);
            } else {
                if (caseData.getManageOrdersOptions().equals(createAnOrder)
                    && ((caseData.getServeOrderData() != null
                    && (YesOrNo.No.equals(caseData.getServeOrderData().getDoYouWantToServeOrder())
                    && WhatToDoWithOrderEnum.saveAsDraft.equals(caseData.getServeOrderData().getWhatDoWithOrder())))
                    || (caseData.getManageOrders() != null
                    && !AmendOrderCheckEnum.noCheck.equals(caseData.getManageOrders().getAmendOrderSelectCheckOptions())))) {
                    return setDraftOrderCollection(caseData, loggedInUserType);
                } else {
                    List<Element<OrderDetails>> orderDetails = getCurrentOrderDetails(authorisation, caseData);
                    log.info("orderDetails ==> " + orderDetails);
                    orderCollection = caseData.getOrderCollection() != null ? caseData.getOrderCollection() : new ArrayList<>();
                    orderCollection.addAll(orderDetails);
                    orderCollection.sort(Comparator.comparing(
                        m -> m.getValue().getDateCreated(),
                        Comparator.reverseOrder()
                    ));
                    if (Yes.equals(caseData.getManageOrders().getOrdersNeedToBeServed())) {
                        orderCollection = serveOrder(caseData, orderCollection);
                    }
                    LocalDateTime currentOrderCreatedDateTime = orderDetails.get(0).getValue().getDateCreated();
                    log.info("currentOrderCreatedDateTime ==> " + currentOrderCreatedDateTime);
                    orderMap.put("currentOrderCreatedDateTime", currentOrderCreatedDateTime);
                }
            }
        } else {
            orderCollection = serveOrder(caseData, caseData.getOrderCollection());
        }
        orderMap.put("orderCollection", orderCollection);
        return orderMap;
    }

    public Map<String, Object> setDraftOrderCollection(CaseData caseData, String loggedInUserType) {
        List<Element<DraftOrder>> draftOrderList = new ArrayList<>();
        Element<DraftOrder> draftOrderElement = null;
        if (caseData.getManageOrdersOptions().equals(uploadAnOrder)) {
            draftOrderElement = element(getCurrentUploadDraftOrderDetails(caseData, loggedInUserType));
        } else {
            draftOrderElement = element(getCurrentCreateDraftOrderDetails(caseData, loggedInUserType));
        }
        if (caseData.getDraftOrderCollection() != null) {
            draftOrderList.addAll(caseData.getDraftOrderCollection());
            draftOrderList.add(draftOrderElement);
        } else {
            draftOrderList.add(draftOrderElement);
        }
        draftOrderList.sort(Comparator.comparing(
            m -> m.getValue().getOtherDetails().getDateCreated(),
            Comparator.reverseOrder()
        ));
        return Map.of("draftOrderCollection", draftOrderList
        );
    }

    public DraftOrder getCurrentCreateDraftOrderDetails(CaseData caseData, String loggedInUserType) {
        String orderSelectionType = CaseUtils.getOrderSelectionType(caseData);
        SelectTypeOfOrderEnum typeOfOrder = CaseUtils.getSelectTypeOfOrder(caseData);
        return DraftOrder.builder().orderType(caseData.getCreateSelectOrderOptions())
            .c21OrderOptions(blankOrderOrDirections.equals(caseData.getCreateSelectOrderOptions())
                                 ? caseData.getManageOrders().getC21OrderOptions() : null)
            .typeOfOrder(typeOfOrder != null
                             ? typeOfOrder.getDisplayedValue() : null)
            .orderTypeId(caseData.getCreateSelectOrderOptions().getDisplayedValue())
            .orderDocument(
                CreateSelectOrderOptionsEnum.other.equals(caseData.getCreateSelectOrderOptions())
                    ? caseData.getUploadOrderDoc() : caseData.getPreviewOrderDoc())
            .orderDocumentWelsh(caseData.getPreviewOrderDocWelsh())
            .otherDetails(OtherDraftOrderDetails.builder()
                              .createdBy(caseData.getJudgeOrMagistratesLastName())
                              .dateCreated(dateTime.now())
                              .status(getOrderStatus(orderSelectionType, loggedInUserType, null, null))
                              .reviewRequiredBy(caseData.getManageOrders().getAmendOrderSelectCheckOptions())
                              .nameOfJudgeForReview(caseData.getManageOrders().getNameOfJudgeAmendOrder())
                              .nameOfLaForReview(caseData.getManageOrders().getNameOfLaAmendOrder())
                              .nameOfJudgeForReviewOrder(String.valueOf(caseData.getManageOrders().getNameOfJudgeToReviewOrder()))
                              .nameOfLaForReviewOrder(String.valueOf(caseData.getManageOrders().getNameOfLaToReviewOrder()))
                              .build())
            .isTheOrderByConsent(caseData.getManageOrders().getIsTheOrderByConsent())
            .dateOrderMade(caseData.getDateOrderMade())
            .approvalDate(caseData.getApprovalDate())
            .wasTheOrderApprovedAtHearing(caseData.getWasTheOrderApprovedAtHearing())
            .judgeOrMagistrateTitle(caseData.getManageOrders().getJudgeOrMagistrateTitle())
            .judgeOrMagistratesLastName(caseData.getJudgeOrMagistratesLastName())
            .justiceLegalAdviserFullName(caseData.getJusticeLegalAdviserFullName())
            .magistrateLastName(caseData.getMagistrateLastName())
            .recitalsOrPreamble(caseData.getManageOrders().getRecitalsOrPreamble())
            .isTheOrderAboutChildren(caseData.getManageOrders().getIsTheOrderAboutChildren())
            .isTheOrderAboutAllChildren(caseData.getManageOrders().getIsTheOrderAboutAllChildren())
            .childOption(getChildOption(caseData))
            .orderDirections(caseData.getManageOrders().getOrderDirections())
            .furtherDirectionsIfRequired(caseData.getManageOrders().getFurtherDirectionsIfRequired())
            .furtherInformationIfRequired(caseData.getManageOrders().getFurtherInformationIfRequired())
            .fl404CustomFields(caseData.getManageOrders().getFl404CustomFields())
            .parentName(caseData.getManageOrders().getParentName())
            .childArrangementsOrdersToIssue(caseData.getManageOrders().getChildArrangementsOrdersToIssue())
            .selectChildArrangementsOrder(caseData.getManageOrders().getSelectChildArrangementsOrder())
            .cafcassOfficeDetails(caseData.getManageOrders().getCafcassOfficeDetails())
            .appointedGuardianName(caseData.getAppointedGuardianName())
            .fl402HearingCourtAddress(caseData.getManageOrders().getFl402HearingCourtAddress())
            .fl402HearingCourtname(caseData.getManageOrders().getFl402HearingCourtname())
            .manageOrdersFl402CourtName(caseData.getManageOrders().getManageOrdersFl402CourtName())
            .manageOrdersFl402Applicant(caseData.getManageOrders().getManageOrdersFl402Applicant())
            .manageOrdersFl402CaseNo(caseData.getManageOrders().getManageOrdersFl402CaseNo())
            .manageOrdersFl402CourtAddress(caseData.getManageOrders().getManageOrdersFl402CourtAddress())
            .manageOrdersFl402ApplicantRef(caseData.getManageOrders().getManageOrdersFl402ApplicantRef())
            .dateOfHearingTime(caseData.getManageOrders().getDateOfHearingTime())
            .dateOfHearingTimeEstimate(caseData.getManageOrders().getDateOfHearingTimeEstimate())
            .manageOrdersDateOfhearing(caseData.getManageOrders().getManageOrdersDateOfhearing())
            .manageOrdersCourtName(caseData.getManageOrders().getManageOrdersCourtName())
            .manageOrdersCourtAddress(caseData.getManageOrders().getManageOrdersCourtAddress())
            .manageOrdersCaseNo(caseData.getManageOrders().getManageOrdersCaseNo())
            .manageOrdersApplicant(caseData.getManageOrders().getManageOrdersApplicant())
            .manageOrdersApplicantReference(caseData.getManageOrders().getManageOrdersApplicantReference())
            .manageOrdersRespondent(caseData.getManageOrders().getManageOrdersRespondent())
            .manageOrdersRespondentReference(caseData.getManageOrders().getManageOrdersRespondentReference())
            .manageOrdersRespondentDob(caseData.getManageOrders().getManageOrdersRespondentDob())
            .manageOrdersRespondentAddress(caseData.getManageOrders().getManageOrdersRespondentAddress())
            .manageOrdersUnderTakingRepr(caseData.getManageOrders().getManageOrdersUnderTakingRepr())
            .underTakingSolicitorCounsel(caseData.getManageOrders().getUnderTakingSolicitorCounsel())
            .manageOrdersUnderTakingPerson(caseData.getManageOrders().getManageOrdersUnderTakingPerson())
            .manageOrdersUnderTakingAddress(caseData.getManageOrders().getManageOrdersUnderTakingAddress())
            .manageOrdersUnderTakingTerms(caseData.getManageOrders().getManageOrdersUnderTakingTerms())
            .manageOrdersDateOfUnderTaking(caseData.getManageOrders().getManageOrdersDateOfUnderTaking())
            .underTakingDateExpiry(caseData.getManageOrders().getUnderTakingDateExpiry())
            .underTakingExpiryTime(caseData.getManageOrders().getUnderTakingExpiryTime())
            .underTakingFormSign(caseData.getManageOrders().getUnderTakingFormSign())
            .orderSelectionType(orderSelectionType)
            .orderCreatedBy(loggedInUserType)
            .isOrderUploadedByJudgeOrAdmin(No)
            .manageOrderHearingDetails(caseData.getManageOrders().getOrdersHearingDetails())
            .childrenList(getSelectedChildInfoFromMangeOrder(caseData))
            .hasJudgeProvidedHearingDetails(caseData.getManageOrders().getHasJudgeProvidedHearingDetails())
            .sdoDetails(CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(caseData.getCreateSelectOrderOptions())
                            ? copyPropertiesToSdoDetails(caseData) : null)
            .hearingsType(caseData.getManageOrders().getHearingsType())
            .isOrderCreatedBySolicitor(UserRoles.SOLICITOR.name().equals(loggedInUserType) ? Yes : No)
            .judgeNotes(caseData.getJudgeDirectionsToAdmin())
            .build();
    }

    public DynamicMultiSelectList getChildOption(CaseData caseData) {
        return (Yes.equals(caseData.getManageOrders().getIsTheOrderAboutChildren())
            || No.equals(caseData.getManageOrders().getIsTheOrderAboutAllChildren()))
            ? caseData.getManageOrders().getChildOption() : DynamicMultiSelectList.builder()
            .listItems(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).build();
    }

    public SdoDetails copyPropertiesToSdoDetails(CaseData caseData) {
        if (null != caseData.getStandardDirectionOrder()) {
            SdoDetails sdoDetails;
            try {
                String standardDirectionOrderObjectJson = objectMapper.writeValueAsString(caseData.getStandardDirectionOrder());
                sdoDetails = objectMapper.readValue(standardDirectionOrderObjectJson, SdoDetails.class);
            } catch (JsonProcessingException e) {
                throw new ManageOrderRuntimeException("Invalid Json", e);
            }
            return sdoDetails;
        }
        return null;
    }

    public DraftOrder getCurrentUploadDraftOrderDetails(CaseData caseData, String loggedInUserType) {
        String flagSelectedOrderId = getSelectedOrderInfoForUpload(caseData);
        SelectTypeOfOrderEnum typeOfOrder = CaseUtils.getSelectTypeOfOrder(caseData);
        String orderSelectionType = CaseUtils.getOrderSelectionType(caseData);

        return DraftOrder.builder()
            .typeOfOrder(typeOfOrder != null ? typeOfOrder.getDisplayedValue() : null)
            .orderTypeId(flagSelectedOrderId)
            .orderDocument(caseData.getUploadOrderDoc())
            .isTheOrderAboutChildren(caseData.getManageOrders().getIsTheOrderAboutChildren())
            .isTheOrderAboutAllChildren(caseData.getManageOrders().getIsTheOrderAboutAllChildren())
            .childOption(getChildOption(caseData))
            .childrenList(caseData.getManageOrders() != null
                              ? getSelectedChildInfoFromMangeOrder(caseData) : null)
            .otherDetails(OtherDraftOrderDetails.builder()
                              .createdBy(caseData.getJudgeOrMagistratesLastName())
                              .dateCreated(dateTime.now())
                              .status(getOrderStatus(orderSelectionType, loggedInUserType, null, null))
                              .build())
            .dateOrderMade(caseData.getDateOrderMade())
            .approvalDate(caseData.getApprovalDate())
            .judgeNotes(caseData.getJudgeDirectionsToAdmin())
            .orderSelectionType(orderSelectionType)
            .orderCreatedBy(loggedInUserType)
            .isOrderUploadedByJudgeOrAdmin(getIsUploadedFlag(caseData.getManageOrdersOptions(), loggedInUserType))
            .manageOrderHearingDetails(caseData.getManageOrders().getOrdersHearingDetails())
            .hasJudgeProvidedHearingDetails(caseData.getManageOrders().getHasJudgeProvidedHearingDetails())
            .isOrderCreatedBySolicitor(UserRoles.SOLICITOR.name().equals(loggedInUserType) ? Yes : No)
            .build();
    }

    private YesOrNo getIsUploadedFlag(ManageOrdersOptionsEnum manageOrdersOptions, String loggedInUserType) {
        YesOrNo isUploaded = No;
        if (UserRoles.SOLICITOR.name().equals(loggedInUserType)) {
            isUploaded = Yes;
        } else if (null != manageOrdersOptions && uploadAnOrder.equals(manageOrdersOptions)) {
            isUploaded = Yes;
        }
        return isUploaded;
    }

    public String getOrderStatus(String orderSelectionType, String loggedInUserType, String eventId, String previousOrderStatus) {
        String currentOrderStatus;
        if (Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId().equals(eventId)) {
            currentOrderStatus = OrderStatusEnum.reviewedByCA.getDisplayedValue();
        } else if (Event.EDIT_AND_APPROVE_ORDER.getId().equals(eventId)) {
            currentOrderStatus = OrderStatusEnum.reviewedByJudge.getDisplayedValue();
        } else if (createAnOrder.toString().equals(orderSelectionType) || uploadAnOrder.toString().equals(
            orderSelectionType) || amendOrderUnderSlipRule.toString().equals(orderSelectionType)
            || draftAnOrder.toString().equals(orderSelectionType)) {
            currentOrderStatus = orderStatusForCreateUploadAmend(loggedInUserType);
        } else {
            currentOrderStatus = "";
        }

        if (!StringUtils.isBlank(previousOrderStatus) && !StringUtils.isBlank(currentOrderStatus)
            && OrderStatusEnum.fromDisplayedValue(previousOrderStatus).getPriority() > OrderStatusEnum.fromDisplayedValue(
            currentOrderStatus).getPriority()) {
            currentOrderStatus = previousOrderStatus;
        }

        return currentOrderStatus;
    }

    private static String orderStatusForCreateUploadAmend(String loggedInUserType) {
        String status = "";
        if (UserRoles.JUDGE.name().equals(loggedInUserType)) {
            status = OrderStatusEnum.createdByJudge.getDisplayedValue();
        } else if (UserRoles.COURT_ADMIN.name().equals(loggedInUserType)) {
            status = OrderStatusEnum.createdByCA.getDisplayedValue();
        } else if (UserRoles.SOLICITOR.name().equals(loggedInUserType)) {
            status = OrderStatusEnum.draftedByLR.getDisplayedValue();
        }
        return status;
    }

    public List<Element<OrderDetails>> serveOrder(CaseData caseData, List<Element<OrderDetails>> orders) {
        log.info("***** inside serveOrder********");
        log.info("***** orders size******** {}", orders.size());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        if (null != caseData.getManageOrders() && null != caseData.getManageOrders().getServeOrderDynamicList()) {
            List<String> selectedOrderIds = caseData.getManageOrders().getServeOrderDynamicList().getValue()
                .stream().map(DynamicMultiselectListElement::getCode).collect(Collectors.toList());
            log.info("order collection id's {}", orders.stream().map(a -> a.getValue().getOrderType()
                + HYPHEN_SEPARATOR + dtf.format(a.getValue().getDateCreated())).collect(Collectors.toList()));
            log.info("***** selected order Ids******** {}", selectedOrderIds);
            orders.stream()
                .filter(order -> selectedOrderIds.contains(order.getValue().getOrderType()
                                                               + HYPHEN_SEPARATOR + dtf.format(order.getValue().getDateCreated())))
                .forEach(order -> {
                    if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                        log.info("***** serving c100 order *******");
                        servedC100Order(caseData, orders, order);
                    } else {
                        servedFL401Order(caseData, orders, order);
                    }
                });
        }
        log.info("***** orders size before returning******** {}", orders.size());
        return orders;
    }

    private void servedFL401Order(CaseData caseData, List<Element<OrderDetails>> orders, Element<OrderDetails> order) {
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
        List<Element<ServedParties>> servedParties  = getServedParties(caseData);
        Map<String, Object> servedOrderDetails = new HashMap<>();
        servedOrderDetails.put(OTHER_PARTIES_SERVED, otherPartiesServed);
        servedOrderDetails.put(SERVING_RESPONDENTS_OPTIONS, servingRespondentsOptions);
        servedOrderDetails.put(SERVED_PARTIES, servedParties);

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

    private void servedC100Order(CaseData caseData, List<Element<OrderDetails>> orders, Element<OrderDetails> order) {
        YesOrNo serveOnRespondent = caseData.getManageOrders().getServeToRespondentOptions();
        YesOrNo serveOnRespondentOnly47a = caseData.getManageOrders().getServeToRespondentOptionsOnlyC47a();
        ServingRespondentsEnum servingRespondentsOptions = null;
        String recipients = null;
        String otherParties;
        if (Yes.equals(serveOnRespondent) || Yes.equals(serveOnRespondentOnly47a)) {
            servingRespondentsOptions = getServingRespondentsOptions(caseData);
        } else {
            recipients = getRecipients(caseData);
        }
        otherParties = getOtherParties(caseData);
        YesOrNo otherPartiesServed = No;
        List<Element<PostalInformation>> postalInformation = null;
        List<Element<EmailInformation>> emailInformation = null;
        if (CollectionUtils.isNotEmpty(caseData.getManageOrders().getServeOtherPartiesCA())
            || CollectionUtils.isNotEmpty(caseData.getManageOrders().getServeOtherPartiesCaOnlyC47a())) {
            otherPartiesServed = Yes;
            emailInformation = getEmailInformationCA(caseData);
            postalInformation = getPostalInformationCA(caseData);
        }
        YesOrNo cafcassServedOptions = No;
        YesOrNo cafcassCymruServedOptions = No;
        String cafCassEmail = null;
        String cafcassCymruEmail = null;
        if (caseData.getManageOrders().getCafcassServedOptions() != null) {
            cafcassServedOptions = caseData.getManageOrders().getCafcassServedOptions();
            cafCassEmail = caseData.getManageOrders().getCafcassEmailId();
        } else if (caseData.getManageOrders().getCafcassCymruServedOptions() != null) {
            cafcassCymruServedOptions = caseData.getManageOrders().getCafcassCymruServedOptions();
            cafcassCymruEmail = caseData.getManageOrders().getCafcassCymruEmail();
        }
        List<Element<ServedParties>> servedParties  = getServedParties(caseData);
        Map<String, Object> servedOrderDetails = new HashMap<>();
        servedOrderDetails.put(CAFCASS_SERVED, cafcassServedOptions);
        servedOrderDetails.put(CAFCASS_CYMRU_SERVED, cafcassCymruServedOptions);
        servedOrderDetails.put(CAFCASS_EMAIL, cafCassEmail);
        servedOrderDetails.put(SERVE_ON_RESPONDENT, serveOnRespondent);
        servedOrderDetails.put(OTHER_PARTIES_SERVED, otherPartiesServed);
        servedOrderDetails.put(SERVING_RESPONDENTS_OPTIONS, servingRespondentsOptions);
        servedOrderDetails.put(RECIPIENTS_OPTIONS, recipients);
        servedOrderDetails.put(OTHER_PARTIES, otherParties);
        servedOrderDetails.put(SERVED_PARTIES, servedParties);

        updateServedOrderDetails(
            servedOrderDetails,
            cafcassCymruEmail,
            orders,
            order,
            postalInformation,
            emailInformation,
            caseData.getManageOrders().getServeOrderAdditionalDocuments()
        );
    }

    private List<Element<ServedParties>> getServedParties(CaseData caseData) {
        List<Element<ServedParties>> servedParties = new ArrayList<>();
        if (caseData.getManageOrders()
            .getRecipientsOptions() != null) {
            servedParties = dynamicMultiSelectListService
                .getServedPartyDetailsFromDynamicSelectList(caseData
                                                                .getManageOrders()
                                                                .getRecipientsOptions());
        } else if (caseData.getManageOrders()
            .getRecipientsOptionsOnlyC47a() != null) {
            servedParties = dynamicMultiSelectListService
                .getServedPartyDetailsFromDynamicSelectList(caseData.getManageOrders()
                                                         .getRecipientsOptionsOnlyC47a());
        }

        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            if (caseData.getManageOrders().getChildOption() != null) {
                servedParties.addAll(dynamicMultiSelectListService
                                     .getServedPartyDetailsFromDynamicSelectList(caseData.getManageOrders()
                                                                                     .getChildOption()));
            }
            if (caseData.getManageOrders().getOtherParties() != null) {
                servedParties.addAll(dynamicMultiSelectListService.getServedPartyDetailsFromDynamicSelectList(
                    caseData.getManageOrders().getOtherParties()
                ));
            } else if (caseData.getManageOrders().getOtherPartiesOnlyC47a() != null) {
                servedParties.addAll(dynamicMultiSelectListService.getServedPartyDetailsFromDynamicSelectList(
                    caseData.getManageOrders().getOtherPartiesOnlyC47a()
                ));
            }
        }
        return servedParties;
    }

    private static ServingRespondentsEnum getServingRespondentsOptions(CaseData caseData) {
        ServingRespondentsEnum servingRespondentsOptions = null;
        if (caseData.getManageOrders()
            .getServingRespondentsOptionsCA() != null) {
            servingRespondentsOptions = caseData.getManageOrders()
                .getServingRespondentsOptionsCA();
        } else if (caseData.getManageOrders()
            .getServingRespondentsOptionsCaOnlyC47a() != null) {
            servingRespondentsOptions = caseData.getManageOrders()
                .getServingRespondentsOptionsCaOnlyC47a();
        }
        return servingRespondentsOptions;
    }

    private static List<Element<PostalInformation>> getPostalInformationCA(CaseData caseData) {
        List<Element<PostalInformation>> postalInformation = null;
        if (caseData.getManageOrders().getPostalInformationCA() != null) {
            postalInformation = caseData.getManageOrders().getPostalInformationCA();
        } else if (caseData.getManageOrders().getPostalInformationCaOnlyC47a() != null) {
            postalInformation = caseData.getManageOrders().getPostalInformationCaOnlyC47a();
        }
        return postalInformation;
    }

    private static List<Element<EmailInformation>> getEmailInformationCA(CaseData caseData) {
        List<Element<EmailInformation>> emailInformation = null;
        if (caseData.getManageOrders().getEmailInformationCA() != null) {
            emailInformation = caseData.getManageOrders().getEmailInformationCA();
        } else if (caseData.getManageOrders().getEmailInformationCaOnlyC47a() != null) {
            emailInformation = caseData.getManageOrders().getEmailInformationCaOnlyC47a();
        }
        return emailInformation;
    }

    private static String getOtherParties(CaseData caseData) {
        List<String> otherPartiesList = new ArrayList<>();
        String otherParties;
        if (caseData.getManageOrders()
            .getOtherParties() != null && caseData.getManageOrders()
            .getOtherParties().getValue() != null) {
            for (DynamicMultiselectListElement dynamicMultiselectChildElement : caseData.getManageOrders()
                .getOtherParties().getValue()) {
                otherPartiesList.add(dynamicMultiselectChildElement.getLabel());
            }
        } else if (caseData.getManageOrders()
            .getOtherPartiesOnlyC47a() != null && caseData.getManageOrders()
            .getOtherPartiesOnlyC47a().getValue() != null) {
            for (DynamicMultiselectListElement dynamicMultiselectChildElement : caseData.getManageOrders()
                .getOtherPartiesOnlyC47a().getValue()) {
                otherPartiesList.add(dynamicMultiselectChildElement.getLabel());
            }
        }
        otherParties = String.join(",", otherPartiesList);
        return otherParties;
    }

    private String getRecipients(CaseData caseData) {
        String recipients = null;
        if (caseData.getManageOrders()
            .getRecipientsOptions() != null) {
            recipients = dynamicMultiSelectListService
                .getStringFromDynamicMultiSelectList(caseData.getManageOrders().getRecipientsOptions());

        } else if (caseData.getManageOrders()
            .getRecipientsOptionsOnlyC47a() != null) {
            recipients = dynamicMultiSelectListService
                .getStringFromDynamicMultiSelectList(caseData.getManageOrders()
                                                         .getRecipientsOptionsOnlyC47a());
        }
        return recipients;
    }

    private static void updateServedOrderDetails(Map<String, Object> servedOrderDetails, String cafcassCymruEmail, List<Element<OrderDetails>> orders,
                                                 Element<OrderDetails> order, List<Element<PostalInformation>> postalInformation,
                                                 List<Element<EmailInformation>> emailInformation, List<Element<Document>> additionalDocuments) {
        log.info("***** inside updateServedOrderDetails********");

        YesOrNo cafcassServed = null;
        YesOrNo cafcassCymruServed = null;
        String cafcassEmail = null;
        YesOrNo serveOnRespondent = null;
        YesOrNo otherPartiesServed = null;
        ServingRespondentsEnum servingRespondentsOptions = null;
        String recipients = null;
        String otherParties = null;
        List<Element<ServedParties>> servedParties = new ArrayList<>();


        if (servedOrderDetails.containsKey(CAFCASS_EMAIL) && null != servedOrderDetails.get(CAFCASS_EMAIL)) {
            cafcassEmail = (String) servedOrderDetails.get(CAFCASS_EMAIL);
        }
        if (servedOrderDetails.containsKey(CAFCASS_SERVED)) {
            cafcassServed = (YesOrNo) servedOrderDetails.get(CAFCASS_SERVED);
        }
        if (servedOrderDetails.containsKey(CAFCASS_CYMRU_SERVED)) {
            cafcassCymruServed = (YesOrNo) servedOrderDetails.get(CAFCASS_CYMRU_SERVED);
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
        if (servedOrderDetails.containsKey(RECIPIENTS_OPTIONS)) {
            recipients = (String) servedOrderDetails.get(RECIPIENTS_OPTIONS);
        }
        if (servedOrderDetails.containsKey(OTHER_PARTIES)) {
            otherParties = (String) servedOrderDetails.get(OTHER_PARTIES);
        }
        if (servedOrderDetails.containsKey(SERVED_PARTIES)) {
            servedParties = (List<Element<ServedParties>>)servedOrderDetails.get(SERVED_PARTIES);
        }
        ServeOrderDetails tempServeOrderDetails;
        if (order.getValue().getServeOrderDetails() != null) {
            tempServeOrderDetails = order.getValue().getServeOrderDetails();
        } else {
            tempServeOrderDetails = ServeOrderDetails.builder().build();
        }
        ServeOrderDetails serveOrderDetails = tempServeOrderDetails.toBuilder().serveOnRespondent(serveOnRespondent)
            .servingRespondent(servingRespondentsOptions)
            .recipientsOptions(recipients)
            .otherParties(otherParties)
            .cafcassServed(cafcassServed)
            .cafcassEmail(cafcassEmail)
            .cafcassCymruServed(cafcassCymruServed)
            .cafcassCymruEmail(cafcassCymruEmail)
            .otherPartiesServed(otherPartiesServed)
            .postalInformation(postalInformation)
            .emailInformation(emailInformation)
            .additionalDocuments(additionalDocuments)
            .servedParties(servedParties)
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
        orders.set(orders.indexOf(order), element(order.getId(), amended));
    }

    private static OtherOrderDetails updateOtherOrderDetails(OtherOrderDetails otherDetails) {
        log.info("***** inside updateOtherOrderDetails******** {}", otherDetails);
        return OtherOrderDetails.builder()
            .createdBy(otherDetails.getCreatedBy())
            .orderCreatedDate(otherDetails.getOrderCreatedDate())
            .orderAmendedDate(otherDetails.getOrderAmendedDate())
            .orderMadeDate(otherDetails.getOrderMadeDate())
            .approvalDate(otherDetails.getApprovalDate())
            .orderRecipients(otherDetails.getOrderRecipients())
            .orderServedDate(LocalDate.now().format(DateTimeFormatter.ofPattern(
                PrlAppsConstants.D_MMMM_YYYY,
                Locale.UK
            )))
            .status(otherDetails.getStatus())
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
        try {
            GeneratedDocumentInfo generatedDocumentInfo;
            Map<String, String> fieldsMap = getOrderTemplateAndFile(selectOrderOption);
            populateChildrenListForDocmosis(caseData);
            if (caseData.getManageOrders().getOrdersHearingDetails() != null) {
                caseData = filterEmptyHearingDetails(caseData);
                if (!caseData.getManageOrders().getOrdersHearingDetails().isEmpty()) {
                    caseDataUpdated.put(ORDER_HEARING_DETAILS, caseData.getManageOrders().getOrdersHearingDetails());
                }
            }
            if (CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(selectOrderOption)) {
                caseData = populateJudgeName(authorisation, caseData);
            }
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
            if (documentLanguage.isGenWelsh() && fieldsMap.get(PrlAppsConstants.DRAFT_TEMPLATE_WELSH) != null) {
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
        } catch (Exception ex) {
            log.info("Error occured while generating Draft document ==> " + ex.getMessage());
        }
        return caseDataUpdated;
    }

    public  CaseData filterEmptyHearingDetails(CaseData caseData) {
        List<Element<HearingData>> filteredHearingDataList = caseData.getManageOrders().getOrdersHearingDetails()
            .stream()
            .filter(element -> ((element.getValue().getHearingTypes() != null && element.getValue().getHearingTypes().getValue() != null)
                || element.getValue().getHearingDateConfirmOptionEnum() != null))
            .collect(Collectors.toList());
        return caseData.toBuilder()
            .manageOrders(caseData.getManageOrders().toBuilder()
                              .ordersHearingDetails(filteredHearingDataList).build()).build();
    }

    public void populateChildrenListForDocmosis(CaseData caseData) {
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            List<Element<Child>> children = dynamicMultiSelectListService
                .getChildrenForDocmosis(caseData);
            if (!children.isEmpty()) {
                caseData.setChildrenListForDocmosis(children);
            }
        } else {
            List<Element<ApplicantChild>> applicantChildren = dynamicMultiSelectListService
                .getApplicantChildDetailsForDocmosis(caseData);
            if (!applicantChildren.isEmpty()) {
                caseData.setApplicantChildDetailsForDocmosis(applicantChildren);
            }
        }
    }

    private CaseData getN117FormData(CaseData caseData) {

        ManageOrders orderData = ManageOrders.builder()
            .manageOrdersCaseNo(String.valueOf(caseData.getId()))
            .recitalsOrPreamble(caseData.getManageOrders().getRecitalsOrPreamble())
            .isCaseWithdrawn(caseData.getManageOrders().getIsCaseWithdrawn())
            .isTheOrderByConsent(caseData.getManageOrders().getIsTheOrderByConsent())
            .judgeOrMagistrateTitle(caseData.getManageOrders().getJudgeOrMagistrateTitle())
            .orderDirections(caseData.getManageOrders().getOrderDirections())
            .furtherDirectionsIfRequired(caseData.getManageOrders().getFurtherDirectionsIfRequired())
            .furtherInformationIfRequired(caseData.getManageOrders().getFurtherInformationIfRequired())
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

        FL404 orderData = caseData.getManageOrders().getFl404CustomFields();

        if (orderData != null) {
            orderData = orderData.toBuilder()
                .fl404bCaseNumber(String.valueOf(caseData.getId()))
                .fl404bCourtName(caseData.getCourtName())
                .fl404bApplicantName(String.format(
                    PrlAppsConstants.FORMAT,
                    caseData.getApplicantsFL401().getFirstName(),
                    caseData.getApplicantsFL401().getLastName()
                ))
                .fl404bRespondentName(String.format(
                    PrlAppsConstants.FORMAT,
                    caseData.getRespondentsFL401().getFirstName(),
                    caseData.getRespondentsFL401().getLastName()
                ))
                .fl404bApplicantReference(caseData.getApplicantsFL401().getSolicitorReference() != null
                                              ? caseData.getApplicantsFL401().getSolicitorReference() : "")
                .fl404bRespondentReference(caseData.getRespondentsFL401().getSolicitorReference() != null
                                               ? caseData.getRespondentsFL401().getSolicitorReference() : "")
                .build();

            if (ofNullable(caseData.getRespondentsFL401().getAddress()).isPresent()) {
                orderData = orderData.toBuilder()
                    .fl404bRespondentAddress(caseData.getRespondentsFL401().getAddress()).build();
            }
            if (ofNullable(caseData.getRespondentsFL401().getDateOfBirth()).isPresent()) {
                orderData = orderData.toBuilder()
                    .fl404bRespondentDob(caseData.getRespondentsFL401().getDateOfBirth()).build();
            }
        }
        caseData = caseData.toBuilder()
            .manageOrders(ManageOrders.builder()
                              .recitalsOrPreamble(caseData.getManageOrders().getRecitalsOrPreamble())
                              .isCaseWithdrawn(caseData.getManageOrders().getIsCaseWithdrawn())
                              .isTheOrderByConsent(caseData.getManageOrders().getIsTheOrderByConsent())
                              .judgeOrMagistrateTitle(caseData.getManageOrders().getJudgeOrMagistrateTitle())
                              .orderDirections(caseData.getManageOrders().getOrderDirections())
                              .furtherDirectionsIfRequired(caseData.getManageOrders().getFurtherDirectionsIfRequired())
                              .furtherInformationIfRequired(caseData.getManageOrders().getFurtherInformationIfRequired())
                              .fl404CustomFields(orderData)
                              .isTheOrderAboutChildren(caseData.getManageOrders().getIsTheOrderAboutChildren())
                              .ordersHearingDetails(caseData.getManageOrders().getOrdersHearingDetails())
                              .childOption(getChildOption(caseData))
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
            .furtherInformationIfRequired(caseData.getManageOrders().getFurtherInformationIfRequired())
            .ordersHearingDetails(caseData.getManageOrders().getOrdersHearingDetails())
            .build();


        return caseData.toBuilder().manageOrders(orderData)
            .selectedOrder(getSelectedOrderInfo(caseData)).build();
    }

    private Element<OrderDetails> getOrderDetailsElement(String authorisation, String flagSelectedOrderId,
                                                         String flagSelectedOrder, Map<String, String> fieldMap,
                                                         CaseData caseData) throws Exception {
        SelectTypeOfOrderEnum typeOfOrder = CaseUtils.getSelectTypeOfOrder(caseData);
        ServeOrderData serveOrderData = CaseUtils.getServeOrderData(caseData);

        OrderDetails orderDetails = getNewOrderDetails(
            flagSelectedOrderId,
            flagSelectedOrder,
            caseData,
            typeOfOrder,
            serveOrderData
        );

        populateChildrenListForDocmosis(caseData);
        if (caseData.getManageOrders().getOrdersHearingDetails() != null) {
            caseData = filterEmptyHearingDetails(caseData);
        }
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        if (documentLanguage.isGenEng()) {
            log.info("*** Generating Final order in English ***");
            String template = fieldMap.get(PrlAppsConstants.FINAL_TEMPLATE_NAME);

            GeneratedDocumentInfo generatedDocumentInfo = dgsService.generateDocument(
                authorisation,
                CaseDetails.builder().caseData(caseData).build(),
                template
            );
            orderDetails = orderDetails
                .toBuilder()
                .orderDocument(Document.builder()
                                   .documentUrl(generatedDocumentInfo.getUrl())
                                   .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                   .documentHash(generatedDocumentInfo.getHashToken())
                                   .documentFileName(fieldMap.get(PrlAppsConstants.GENERATE_FILE_NAME))
                                   .build())
                .build();
        }
        if (documentLanguage.isGenWelsh()) {
            log.info("*** Generating Final order in Welsh ***");
            String welshTemplate = fieldMap.get(FINAL_TEMPLATE_WELSH);
            log.info("Generating document for {}, {}", FINAL_TEMPLATE_WELSH, welshTemplate);
            if (welshTemplate != null && welshTemplate.contains("-WEL-")) {
                GeneratedDocumentInfo generatedDocumentInfoWelsh = dgsService.generateWelshDocument(
                    authorisation,
                    CaseDetails.builder().caseData(caseData).build(),
                    welshTemplate
                );
                orderDetails = orderDetails.toBuilder().orderDocumentWelsh(Document.builder()
                                                                               .documentUrl(generatedDocumentInfoWelsh.getUrl())
                                                                               .documentBinaryUrl(
                                                                                   generatedDocumentInfoWelsh.getBinaryUrl())
                                                                               .documentHash(generatedDocumentInfoWelsh.getHashToken())
                                                                               .documentFileName(fieldMap.get(
                                                                                   PrlAppsConstants.WELSH_FILE_NAME)).build()).build();
            }
        }
        log.info("inside getOrderDetailsElement ==> " + caseData.getManageOrders().getCurrentOrderCreatedDateTime());
        return element(orderDetails.toBuilder()
                           .otherDetails(OtherOrderDetails.builder()
                                             .createdBy(caseData.getJudgeOrMagistratesLastName())
                                             .orderCreatedDate(dateTime.now().format(DateTimeFormatter.ofPattern(
                                                 PrlAppsConstants.D_MMMM_YYYY,
                                                 Locale.UK
                                             )))
                                             .orderMadeDate(caseData.getDateOrderMade() != null ? caseData.getDateOrderMade()
                                                 .format(DateTimeFormatter.ofPattern(
                                                     PrlAppsConstants.D_MMMM_YYYY,
                                                     Locale.UK
                                                 )) : null)
                                             .approvalDate(caseData.getApprovalDate() != null ? caseData.getApprovalDate()
                                                 .format(DateTimeFormatter.ofPattern(
                                                     PrlAppsConstants.D_MMMM_YYYY,
                                                     Locale.UK
                                                 )) : null)
                                             .orderRecipients(getAllRecipients(caseData))
                                             .status(getOrderStatus(CaseUtils.getOrderSelectionType(caseData),
                                                                    getLoggedInUserType(authorisation), null, null
                                             ))
                                             .build())
                           .dateCreated(caseData.getManageOrders().getCurrentOrderCreatedDateTime() != null
                                            ? caseData.getManageOrders().getCurrentOrderCreatedDateTime() : dateTime.now())
                           .manageOrderHearingDetails(caseData.getManageOrders().getOrdersHearingDetails())
                           .selectedHearingType(null != caseData.getManageOrders().getHearingsType()
                                                    ? caseData.getManageOrders().getHearingsType().getValueCode() : null)
                           .c21OrderOptions(caseData.getManageOrders().getC21OrderOptions())
                           .selectChildArrangementsOrder(caseData.getManageOrders().getSelectChildArrangementsOrder())
                           .childArrangementsOrdersToIssue(caseData.getManageOrders().getChildArrangementsOrdersToIssue())
                           .childOption(getChildOption(caseData))
                           .build());
    }

    private OrderDetails getNewOrderDetails(String flagSelectedOrderId, String flagSelectedOrder,
                                            CaseData caseData, SelectTypeOfOrderEnum typeOfOrder,
                                            ServeOrderData serveOrderData) {
        return OrderDetails.builder().orderType(flagSelectedOrder)
            .orderTypeId(flagSelectedOrderId)
            .withdrawnRequestType(null != caseData.getManageOrders().getWithdrawnOrRefusedOrder()
                                      ? caseData.getManageOrders().getWithdrawnOrRefusedOrder().getDisplayedValue() : null)
            .isWithdrawnRequestApproved(getWithdrawRequestInfo(caseData))
            .typeOfOrder(typeOfOrder != null
                             ? typeOfOrder.getDisplayedValue() : null)
            .isTheOrderAboutChildren(caseData.getManageOrders().getIsTheOrderAboutChildren())
            .isTheOrderAboutAllChildren(caseData.getManageOrders().getIsTheOrderAboutAllChildren())
            .typeOfChildArrangementsOrder(CreateSelectOrderOptionsEnum.childArrangementsSpecificProhibitedOrder
                                              .equals(CreateSelectOrderOptionsEnum.getValue(flagSelectedOrderId))
                                              ? getChildArrangementOrder(caseData) : "")
            .childrenList((Yes.equals(caseData.getManageOrders().getIsTheOrderAboutChildren())
                || No.equals(caseData.getManageOrders().getIsTheOrderAboutAllChildren()))
                              ? dynamicMultiSelectListService
                .getStringFromDynamicMultiSelectList(caseData.getManageOrders()
                                                         .getChildOption())
                              : dynamicMultiSelectListService
                .getStringFromDynamicMultiSelectListFromListItems(caseData.getManageOrders()
                                                                      .getChildOption()))
            .orderClosesCase(SelectTypeOfOrderEnum.finl.equals(typeOfOrder)
                                 ? caseData.getDoesOrderClosesCase() : null)
            .serveOrderDetails(buildServeOrderDetails(serveOrderData))
            .sdoDetails(CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(caseData.getCreateSelectOrderOptions())
                            ? copyPropertiesToSdoDetails(caseData) : null)
            .build();
    }

    private String getChildArrangementOrder(CaseData caseData) {
        return caseData.getManageOrders().getChildArrangementsOrdersToIssue()
            .stream()
            .flatMap(element -> OrderTypeEnum.childArrangementsOrder.equals(element)
                ? Stream.of(element.getDisplayedValue() + "(" + caseData.getManageOrders()
                .getSelectChildArrangementsOrder().getDisplayedValue() + ")")
                : Stream.of(element.getDisplayedValue()))
            .collect(Collectors.joining(", "));
    }

    private String getWithdrawRequestInfo(CaseData caseData) {
        String withdrawApproved = "";

        if (null != caseData.getManageOrders().getWithdrawnOrRefusedOrder()
            && caseData.getManageOrders().getWithdrawnOrRefusedOrder().getDisplayedValue().equals(
            "Withdrawn application")) {
            withdrawApproved = String.valueOf(caseData.getManageOrders().getIsCaseWithdrawn());
        }

        return withdrawApproved;
    }

    public String getLoggedInUserType(String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        String loggedInUserType;
        List<String> roles = userDetails.getRoles();
        if (roles.contains(Roles.JUDGE.getValue()) || roles.contains(Roles.LEGAL_ADVISER.getValue())) {
            loggedInUserType = UserRoles.JUDGE.name();
        } else if (roles.contains(Roles.COURT_ADMIN.getValue())) {
            loggedInUserType = UserRoles.COURT_ADMIN.name();
        } else if (roles.contains(Roles.SOLICITOR.getValue())) {
            loggedInUserType = UserRoles.SOLICITOR.name();
        } else if (roles.contains(Roles.CITIZEN.getValue())) {
            loggedInUserType = UserRoles.CITIZEN.name();
        } else if (roles.contains(Roles.SYSTEM_UPDATE.getValue())) {
            loggedInUserType = UserRoles.SYSTEM_UPDATE.name();
        } else {
            loggedInUserType = "";
        }

        return loggedInUserType;
    }

    public static void cleanUpSelectedManageOrderOptions(Map<String, Object> caseDataUpdated) {
        for (ManageOrderFieldsEnum field : ManageOrderFieldsEnum.values()) {
            if (caseDataUpdated.containsKey(field.getValue())) {
                caseDataUpdated.remove(field.getValue());
            }
        }
    }

    public Map<String, Object> populatePreviewOrder(String authorisation, CallbackRequest callbackRequest, CaseData caseData) throws Exception {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if (callbackRequest
            .getCaseDetailsBefore() != null && callbackRequest
            .getCaseDetailsBefore().getData().get(COURT_NAME) != null) {
            caseData.setCourtName(callbackRequest
                                      .getCaseDetailsBefore().getData().get(COURT_NAME).toString());
        }
        if (caseData.getCreateSelectOrderOptions() != null && !uploadAnOrder.equals(caseData.getManageOrdersOptions())) {
            if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                caseData = populateCustomOrderFields(caseData);
            }
            caseDataUpdated.putAll(getCaseData(authorisation, caseData, caseData.getCreateSelectOrderOptions()));
        } else {
            caseDataUpdated.put("previewOrderDoc", caseData.getUploadOrderDoc());
        }
        return caseDataUpdated;
    }

    public Map<String, Object> checkOnlyC47aOrderSelectedToServe(CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        List<DynamicMultiselectListElement> selectedServedOrderList = caseData.getManageOrders().getServeOrderDynamicList().getValue();
        if (selectedServedOrderList != null && selectedServedOrderList.size() == 1
            && selectedServedOrderList.get(0).getLabel().contains(C_47_A)) {
            caseDataUpdated.put(IS_ONLY_C_47_A_ORDER_SELECTED_TO_SERVE, Yes);
        } else {
            caseDataUpdated.put(IS_ONLY_C_47_A_ORDER_SELECTED_TO_SERVE, No);
            String courtEmail = welshCourtEmail.populateCafcassCymruEmailInManageOrders(caseData);
            if (courtEmail != null) {
                caseDataUpdated.put("cafcassCymruEmail", courtEmail);
            }
        }
        caseDataUpdated.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        populateOtherServeOrderDetails(caseData, caseDataUpdated);
        return caseDataUpdated;
    }

    public DynamicList populateHearingsDropdown(String authorization, CaseData caseData) {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        log.info("Retrieving hearings for caseId: {}", caseData.getId());
        Optional<Hearings> hearings = Optional.ofNullable(hearingService.getHearings(
            authorization,
            String.valueOf(caseData.getId())
        ));
        List<CaseHearing> caseHearings = hearings.map(Hearings::getCaseHearings).orElseGet(ArrayList::new);
        List<CaseHearing> completedHearings = caseHearings.stream()
            .filter(caseHearing -> HMC_STATUS_COMPLETED.equalsIgnoreCase(caseHearing.getHmcStatus()))
            .collect(Collectors.toList());
        log.info("Total completed hearings: {}", completedHearings.size());

        //get hearings dropdown
        List<DynamicListElement> hearingDropdowns = completedHearings.stream()
            .map(caseHearing -> {
                //get hearingId
                String hearingId = String.valueOf(caseHearing.getHearingID());
                //return hearingId concatenated with hearingDate
                Optional<List<HearingDaySchedule>> hearingDaySchedules = Optional.ofNullable(caseHearing.getHearingDaySchedule());
                return hearingDaySchedules.map(daySchedules -> daySchedules.stream().map(hearingDaySchedule -> {
                    if (null != hearingDaySchedule && null != hearingDaySchedule.getHearingStartDateTime()) {
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        String hearingDate = hearingDaySchedule.getHearingStartDateTime().format(dateTimeFormatter);
                        return concat(concat(hearingId, " - "), hearingDate);
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toList())).orElse(Collections.emptyList());
            }).map(this::getDynamicListElements)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        //if there are no hearings then dropdown would be empty
        DynamicList existingHearingsType = (null != caseData.getManageOrders() && null != caseData.getManageOrders().getHearingsType())
            ? caseData.getManageOrders().getHearingsType() : null;
        caseDataUpdated.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        caseDataUpdated.put(CHILD_OPTION, DynamicMultiSelectList.builder()
                                               .listItems(dynamicMultiSelectListService.getChildrenMultiSelectList(
                                                   caseData)).build());

        return DynamicList.builder()
            .value(null != existingHearingsType ? existingHearingsType.getValue() : DynamicListElement.EMPTY)
            .listItems(hearingDropdowns.isEmpty() ? Collections.singletonList(DynamicListElement.defaultListItem(
                "No hearings available")) : hearingDropdowns)
            .build();
    }

    private List<DynamicListElement> getDynamicListElements(List<String> dropdowns) {
        return dropdowns.stream().map(dropdown -> DynamicListElement.builder().code(dropdown).label(dropdown).build()).collect(
            Collectors.toList());
    }

    public void resetChildOptions(CallbackRequest callbackRequest) {
        Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();
        List<DynamicMultiselectListElement> listElements = List.of(DynamicMultiselectListElement.EMPTY);
        boolean isTheOrderAboutChildrenForDA = caseDataMap.containsKey(IS_THE_ORDER_ABOUT_CHILDREN)
            && caseDataMap.get(IS_THE_ORDER_ABOUT_CHILDREN) != null
            && caseDataMap.get(IS_THE_ORDER_ABOUT_CHILDREN).toString().equalsIgnoreCase(PrlAppsConstants.NO);

        boolean isTheOrderAboutAllChildrenForCA = caseDataMap.containsKey(IS_THE_ORDER_ABOUT_ALL_CHILDREN)
            && caseDataMap.get(IS_THE_ORDER_ABOUT_ALL_CHILDREN) != null
            && caseDataMap.get(IS_THE_ORDER_ABOUT_ALL_CHILDREN).toString().equalsIgnoreCase(PrlAppsConstants.YES);

        if (isTheOrderAboutChildrenForDA || isTheOrderAboutAllChildrenForCA) {
            callbackRequest.getCaseDetails().getData().put(CHILD_OPTION, DynamicMultiSelectList.builder()
                .listItems(List.of(DynamicMultiselectListElement.EMPTY))
                .value(listElements)
                .build());
        }
    }

    public CaseData setChildOptionsIfOrderAboutAllChildrenYes(CaseData caseData) {
        if (YesOrNo.Yes.equals(caseData.getManageOrders().getIsTheOrderAboutAllChildren())) {
            caseData = caseData.toBuilder()
                .manageOrders(caseData.getManageOrders().toBuilder()
                                  .childOption(DynamicMultiSelectList.builder()
                                                   .listItems(dynamicMultiSelectListService.getChildrenMultiSelectList(
                                                       caseData))
                                                   .build())
                                  .build())
                .build();
        }
        return caseData;
    }

    public void setMarkedToServeEmailNotification(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if ((null != caseData.getManageOrders() && Yes.equals(caseData.getManageOrders().getOrdersNeedToBeServed()))
            || (null != caseData.getServeOrderData() && Yes.equals(caseData.getServeOrderData().getDoYouWantToServeOrder()))) {
            caseDataUpdated.put("markedToServeEmailNotification", Yes);
        } else {
            caseDataUpdated.put("markedToServeEmailNotification", No);
        }
    }

    public CaseData populateJudgeName(String authorisation, CaseData caseData) {
        StandardDirectionOrder sdo = caseData.getStandardDirectionOrder();
        if (null != sdo && null != sdo.getSdoAllocateOrReserveJudgeName()) {
            String idamId = caseData.getStandardDirectionOrder()
                .getSdoAllocateOrReserveJudgeName().getIdamId();
            if (null != idamId && StringUtils.isNotBlank(idamId)) {
                try {
                    UserDetails userDetails = userService.getUserByUserId(authorisation, idamId);
                    if (null != userDetails) {
                        return caseData.toBuilder()
                            .standardDirectionOrder(sdo.toBuilder().sdoNamedJudgeFullName(userDetails.getFullName()).build())
                            .build();
                    }
                } catch (Exception e) {
                    log.error("User details not found for idam id {}", idamId);
                }
            }
        }
        return caseData;
    }
}
