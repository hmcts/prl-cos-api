package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.ManageOrderFieldsEnum;
import uk.gov.hmcts.reform.prl.enums.OrderStatusEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.ServeOrderFieldsEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.amroles.InternalCaseworkerAmRolesEnum;
import uk.gov.hmcts.reform.prl.enums.editandapprove.OrderApprovalDecisionsForSolicitorOrderEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.AmendOrderCheckEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.C21OrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.DeliveryByEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.serveorder.WhatToDoWithOrderEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaSolicitorServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.exception.ManageOrderRuntimeException;
import uk.gov.hmcts.reform.prl.models.Address;
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
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.AppointedGuardianFullName;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.ServedParties;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.EmailInformation;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.PostalInformation;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AdditionalOrderDocument;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServeOrderData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.StandardDirectionOrder;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WelshCourtEmail;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
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
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.logging.log4j.util.Strings.concat;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AM_LOWER_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AM_UPPER_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COMMA;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_TIME_PATTERN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DA_ORDER_FOR_CA_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_CASEREVIEW_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_FHDRA_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_PERMISSION_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_URGENT_FIRST_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_URGENT_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DIO_WITHOUT_NOTICE_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EUROPE_LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FINAL_TEMPLATE_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGS_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.NO;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_COLLECTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PM_LOWER_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PM_UPPER_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_HEARING_OPTION_SELECTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_IS_HEARING_TASK_NEEDED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_IS_MULTIPLE_HEARING_SELECTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_IS_ORDER_APPROVED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_JUDGE_LA_MANAGER_REVIEW_REQUIRED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_JUDGE_LA_REVIEW_REQUIRED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_MULTIPLE_OPTIONS_SELECTED_VALUE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_ORDER_NAME_ADMIN_CREATED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_ORDER_NAME_JUDGE_CREATED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_PERFORMING_ACTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_PERFORMING_USER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_WHO_APPROVED_THE_ORDER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YES;
import static uk.gov.hmcts.reform.prl.constants.PrlLaunchDarklyFlagConstants.ROLE_ASSIGNMENT_API_IN_ORDERS_JOURNEY;
import static uk.gov.hmcts.reform.prl.enums.Event.MANAGE_ORDERS;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum.blankOrderOrDirections;
import static uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum.other;
import static uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum.standardDirectionsOrder;
import static uk.gov.hmcts.reform.prl.enums.manageorders.DraftOrderOptionsEnum.draftAnOrder;
import static uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum.amendOrderUnderSlipRule;
import static uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum.createAnOrder;
import static uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum.servedSavedOrders;
import static uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum.uploadAnOrder;
import static uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum.applicantOrApplicantSolicitor;
import static uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum.respondentOrRespondentSolicitor;
import static uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum.factFindingHearing;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getDynamicMultiSelectedValueLabels;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;
import static uk.gov.hmcts.reform.prl.utils.ManageOrdersUtils.isHearingPageNeeded;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings({"java:S3776","java:S6204"})
public class ManageOrderService {

    public static final String IS_THE_ORDER_ABOUT_CHILDREN = "isTheOrderAboutChildren";

    public static final String IS_THE_ORDER_ABOUT_ALL_CHILDREN = "isTheOrderAboutAllChildren";
    public static final String PREVIEW_ORDER_DOC = "previewOrderDoc";

    public static final String CHILD_OPTION = "childOption";

    public static final String IS_ONLY_C_47_A_ORDER_SELECTED_TO_SERVE = "isOnlyC47aOrderSelectedToServe";
    public static final String OTHER_PEOPLE_PRESENT_IN_CASE_FLAG = "otherPeoplePresentInCaseFlag";
    public static final String C_47_A = "C47A";
    public static final String DISPLAY_LEGAL_REP_OPTION = "displayLegalRepOption";
    public static final String CAFCASS_SERVED = "cafcassServed";
    public static final String CAFCASS_EMAIL = "cafcassEmail";
    public static final String CAFCASS_CYMRU_SERVED = "cafcassCymruServed";
    public static final String SERVE_RECIPIENT_NAME = "servingRecipientName";
    public static final String SERVE_ON_RESPONDENT = "serveOnRespondent";
    public static final String OTHER_PARTIES_SERVED = "otherPartiesServed";
    public static final String SERVING_RESPONDENTS_OPTIONS = "servingRespondentsOptions";
    public static final String WHO_IS_RESPONSIBLE_TO_SERVE = "whoIsResponsibleToServe";
    public static final String IS_MULTIPLE_ORDERS_SERVED = "multipleOrdersServed";
    public static final String RECIPIENTS_OPTIONS = "recipientsOptions";
    public static final String OTHER_PARTIES = "otherParties";
    public static final String SERVED_PARTIES = "servedParties";

    public static final String VALIDATION_ADDRESS_ERROR_RESPONDENT = "This order cannot be served by post until the respondent's "
        + "address is given.";
    public static final String VALIDATION_ADDRESS_ERROR_OTHER_PARTY = "This order cannot be served by post until the other"
        + " people's address is given.";

    public static final String EMAIL = "email";
    public static final String POST = "post";
    public static final String SDO_FACT_FINDING_FLAG = "sdoFactFindingFlag";
    public static final String AND = " and";

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

    @Value("${document.templates.common.prl_fl404b_blank_welsh_draft_filename}")
    protected String fl404bBlankWelshDraftFile;

    @Value("${document.templates.common.prl_fl404b_blank_welsh_final_filename}")
    protected String fl404bBlankWelshFile;

    @Value("${hearing_component.hearingStatusesToFilter}")
    private String hearingStatusesToFilter;

    private final DocumentLanguageService documentLanguageService;

    public static final String FAMILY_MAN_ID = "Family Man ID: ";

    private final DgsService dgsService;

    private final DynamicMultiSelectListService dynamicMultiSelectListService;

    private final Time dateTime;

    private final ObjectMapper objectMapper;

    private final ElementUtils elementUtils;

    private final RefDataUserService refDataUserService;
    private static final String BOLD_BEGIN = "<span class='heading-h3'>";
    private static final String BOLD_END = "</span>";

    private final UserService userService;
    private final HearingService hearingService;
    private final HearingDataService hearingDataService;
    private final WelshCourtEmail welshCourtEmail;
    private final RoleAssignmentApi roleAssignmentApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final LaunchDarklyClient launchDarklyClient;

    private final ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE));

    public Map<String, Object> populateHeader(CaseData caseData) {
        Map<String, Object> headerMap = new HashMap<>();
        //PRL-4212 - populate fields only when it's needed
        headerMap.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        if (caseData.getOrderCollection() != null) {
            if (amendOrderUnderSlipRule.equals(caseData.getManageOrdersOptions())) {
                headerMap.put("amendOrderDynamicList", getOrdersAsDynamicList(caseData));
            }
            if (servedSavedOrders.equals(caseData.getManageOrdersOptions())) {
                populateServeOrderDetails(caseData, headerMap);
            }
        }
        //PRL-4854 - Set isSdoSelected=No default
        headerMap.put("isSdoSelected", No);
        return headerMap;
    }

    public void populateServeOrderDetails(CaseData caseData, Map<String, Object> headerMap) {

        DynamicMultiSelectList orderList = dynamicMultiSelectListService.getOrdersAsDynamicMultiSelectList(caseData);
        if (!servedSavedOrders.equals(caseData.getManageOrdersOptions())) {
            //this is when we are trying to serve the order, auto selecting the chosen order
            List<DynamicMultiselectListElement> values = new ArrayList<>();
            values.add(orderList.getListItems().get(0));
            orderList = orderList.toBuilder().value(values).build();
        }
        headerMap.put("serveOrderDynamicList", orderList);
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
            setRecipientsOptions(caseData, headerMap);
            setOtherParties(caseData, headerMap);
            headerMap.put(PrlAppsConstants.IS_CAFCASS, No);
        }
    }

    private void setRecipientsOptions(CaseData caseData, Map<String, Object> headerMap) {

        List<DynamicMultiselectListElement> applicantRespondentList = getPartyDynamicMultiselectList(caseData);
        headerMap.put(
            RECIPIENTS_OPTIONS, DynamicMultiSelectList.builder()
                .listItems(applicantRespondentList)
                .build());
    }

    public List<DynamicMultiselectListElement> getPartyDynamicMultiselectList(CaseData caseData) {
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
        return applicantRespondentList;
    }

    private void setOtherParties(CaseData caseData, Map<String, Object> headerMap) {
        List<DynamicMultiselectListElement> otherPeopleList = dynamicMultiSelectListService
            .getOtherPeopleMultiSelectList(caseData);
        headerMap.put(
            OTHER_PARTIES, DynamicMultiSelectList.builder()
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
        String childList = dynamicMultiSelectListService
            .getStringFromDynamicMultiSelectList(caseData.getManageOrders()
                                                     .getChildOption());
        caseDataUpdated.put("childrenList", childList);
        caseDataUpdated.put("childListForSpecialGuardianship", childList);
        caseDataUpdated.put("selectedOrder", getSelectedOrderInfo(caseData) != null
            ? BOLD_BEGIN + getSelectedOrderInfo(caseData) + BOLD_END : "");
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
                fieldsMap.put(PrlAppsConstants.DRAFT_TEMPLATE_WELSH, fl404bWelshDraftTemplate);
                fieldsMap.put(PrlAppsConstants.DRAFT_WELSH_FILE_NAME, fl404bBlankWelshDraftFile);
                fieldsMap.put(PrlAppsConstants.FINAL_TEMPLATE_WELSH, fl404bWelshTemplate);
                fieldsMap.put(PrlAppsConstants.WELSH_FILE_NAME, fl404bBlankWelshFile);
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
        } else if (caseData.getOtherOrdersOption() != null && caseData.getNameOfOrder() != null) {
            selectedOrder = caseData.getOtherOrdersOption().getDisplayedValue() + " : " + caseData.getNameOfOrder();
        } else {
            selectedOrder = "";
        }
        return selectedOrder;
    }

    public String getSelectedOrderIdForUpload(CaseData caseData) {
        String selectedOrder;
        if (caseData.getChildArrangementOrders() != null) {
            selectedOrder = String.valueOf(caseData.getChildArrangementOrders());
        } else if (caseData.getDomesticAbuseOrders() != null) {
            selectedOrder = String.valueOf(caseData.getDomesticAbuseOrders());
        } else if (caseData.getFcOrders() != null) {
            selectedOrder = String.valueOf(caseData.getFcOrders());
        } else if (caseData.getOtherOrdersOption() != null && caseData.getNameOfOrder() != null) {
            selectedOrder = caseData.getOtherOrdersOption() + " : " + caseData.getNameOfOrder();
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

    private List<Element<OrderDetails>> getCurrentOrderDetails(String authorisation, CaseData caseData, UserDetails userDetails)
        throws Exception {

        String flagSelectedOrder = caseData.getManageOrdersOptions() == ManageOrdersOptionsEnum.createAnOrder
            ? caseData.getCreateSelectOrderOptions().getDisplayedValue()
            : getSelectedOrderInfoForUpload(caseData);

        String flagSelectedOrderId;

        if (caseData.getManageOrdersOptions() == ManageOrdersOptionsEnum.createAnOrder) {
            flagSelectedOrderId = String.valueOf(caseData.getCreateSelectOrderOptions());
        } else {
            flagSelectedOrderId = getSelectedOrderIdForUpload(caseData);
        }
        if (caseData.getCreateSelectOrderOptions() != null
            && !uploadAnOrder.equals(caseData.getManageOrdersOptions())) {
            Map<String, String> fieldMap = getOrderTemplateAndFile(caseData.getCreateSelectOrderOptions());
            List<Element<OrderDetails>> orderCollection = new ArrayList<>();
            if (FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
                || ManageOrdersUtils.isDaOrderSelectedForCaCase(String.valueOf(caseData.getCreateSelectOrderOptions()),
                                                            caseData)) {
                caseData = populateCustomOrderFields(caseData, caseData.getCreateSelectOrderOptions());
            }
            if (CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(caseData.getCreateSelectOrderOptions())) {
                caseData = populateJudgeNames(caseData);
                caseData = populatePartyDetailsOfNewParterForDocmosis(caseData);
                if (isNotEmpty(caseData.getStandardDirectionOrder())
                    && CollectionUtils.isNotEmpty(caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList())
                    && caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(factFindingHearing)) {
                    caseData = populateDirectionOfFactFindingHearingFieldsForDocmosis(caseData);
                }
            }
            orderCollection.add(getOrderDetailsElement(authorisation, flagSelectedOrderId, flagSelectedOrder,
                                                       fieldMap, caseData
            ));

            return orderCollection;
        } else {
            return getListOfOrders(authorisation, caseData, flagSelectedOrder, flagSelectedOrderId, userDetails);
        }
    }

    private List<Element<OrderDetails>> getListOfOrders(String authorisation,
                                                        CaseData caseData,
                                                        String flagSelectedOrder,
                                                        String flagSelectedOrderId,
                                                        UserDetails userDetails) {
        ServeOrderData serveOrderData = CaseUtils.getServeOrderData(caseData);
        String loggedInUserType = getLoggedInUserType(authorisation);
        SelectTypeOfOrderEnum typeOfOrder = CaseUtils.getSelectTypeOfOrder(caseData);
        String orderSelectionType = CaseUtils.getOrderSelectionType(caseData);
        List<Element<OrderDetails>> newOrderDetails = new ArrayList<>();
        newOrderDetails.add(element(OrderDetails.builder().orderType(flagSelectedOrderId)
                                   .orderTypeId(flagSelectedOrder)
                                   .orderDocument(caseData.getUploadOrderDoc())
                                   .isTheOrderAboutChildren(caseData.getManageOrders().getIsTheOrderAboutChildren())
                                   .isTheOrderAboutAllChildren(caseData.getManageOrders().getIsTheOrderAboutAllChildren())
                                   .childrenList(getSelectedChildInfoFromMangeOrder(caseData))
                                   .otherDetails(OtherOrderDetails.builder()
                                                     .createdBy(caseData.getJudgeOrMagistratesLastName())
                                                     .orderCreatedBy(userDetails.getFullName())
                                                     .orderCreatedByEmailId(userDetails.getEmail())
                                                     .orderCreatedDate(dateTime.now()
                                                                           .format(DateTimeFormatter.ofPattern(
                                                                               PrlAppsConstants.D_MMM_YYYY,
                                                                               Locale.ENGLISH
                                                                           )))
                                                     .orderMadeDate(caseData.getDateOrderMade() != null ? caseData.getDateOrderMade().format(
                                                         DateTimeFormatter.ofPattern(
                                                             PrlAppsConstants.D_MMM_YYYY,
                                                             Locale.ENGLISH
                                                         )) : null)
                                                     .approvalDate(caseData.getApprovalDate() != null ? caseData.getApprovalDate().format(
                                                         DateTimeFormatter.ofPattern(
                                                             PrlAppsConstants.D_MMM_YYYY,
                                                             Locale.ENGLISH
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
                                   .isOrderUploaded(Yes)
                                   .build()));
        return newOrderDetails;
    }

    public static ServeOrderDetails buildServeOrderDetails(ServeOrderData serveOrderData) {
        return ServeOrderDetails.builder()
            .cafcassOrCymruNeedToProvideReport(
                serveOrderData.getCafcassOrCymruNeedToProvideReport())
            .cafcassCymruDocuments(serveOrderData.getCafcassCymruDocuments())
            .whenReportsMustBeFiled(serveOrderData.getWhenReportsMustBeFiled() != null
                                        ? serveOrderData.getWhenReportsMustBeFiled()
                .format(DateTimeFormatter.ofPattern(
                    PrlAppsConstants.D_MMM_YYYY,
                    Locale.ENGLISH
                )) : null)
            .orderEndsInvolvementOfCafcassOrCymru(
                serveOrderData.getOrderEndsInvolvementOfCafcassOrCymru())
            .build();
    }

    public String getSelectedChildInfoFromMangeOrder(CaseData caseData) {
        DynamicMultiSelectList childOption = caseData.getManageOrders().getChildOption();
        if ((YesOrNo.Yes.equals(caseData.getManageOrders().getIsTheOrderAboutChildren())
            || YesOrNo.No.equals(caseData.getManageOrders().getIsTheOrderAboutAllChildren()))
            && childOption != null && childOption.getValue() != null) {
            return getChildNames(childOption.getValue());
        } else if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
            && childOption != null && Yes.equals(caseData.getManageOrders().getIsTheOrderAboutAllChildren())) {
            return getChildNames(childOption.getListItems());
        }
        return null;
    }

    private static String getChildNames(List<DynamicMultiselectListElement> dynamicMultiselectListElements) {
        List<String> childList;
        String selectedChildNames;
        childList = new ArrayList<>();
        for (DynamicMultiselectListElement dynamicMultiselectChildElement : dynamicMultiselectListElements) {
            childList.add(org.apache.commons.lang3.StringUtils.trimToEmpty(dynamicMultiselectChildElement.getLabel()));
        }
        selectedChildNames = String.join(", ", childList);
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
                .map(Element::getValue).toList();

            List<String> applicantSolicitorNames = applicants.stream()
                .map(party -> Objects.nonNull(party.getSolicitorOrg().getOrganisationName())
                    ? party.getSolicitorOrg().getOrganisationName() + APPLICANT_SOLICITOR
                    : APPLICANT_SOLICITOR)
                .toList();
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
                .filter(r -> YesNoDontKnow.yes.equals(r.getDoTheyHaveLegalRepresentation())).toList();
            if (respondents.isEmpty()) {
                return "";
            }
            List<String> respondentSolicitorNames = respondents.stream()
                .map(party -> party.getSolicitorOrg().getOrganisationName() + RESPONDENT_SOLICITOR)
                .toList();
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
        String loggedInUserType = getLoggedInUserType(authorisation);
        UserDetails userDetails = userService.getUserDetails(authorisation);
        boolean saveAsDraft = isNotEmpty(caseData.getServeOrderData()) && No.equals(caseData.getServeOrderData().getDoYouWantToServeOrder())
            && WhatToDoWithOrderEnum.saveAsDraft.equals(caseData.getServeOrderData().getWhatDoWithOrder());
        if (UserRoles.JUDGE.name().equals(loggedInUserType)) {
            return setDraftOrderCollection(caseData, loggedInUserType,userDetails);
        } else if (UserRoles.COURT_ADMIN.name().equals(loggedInUserType)) {
            if (!AmendOrderCheckEnum.noCheck.equals(caseData.getManageOrders().getAmendOrderSelectCheckOptions())
                || saveAsDraft) {
                return setDraftOrderCollection(caseData, loggedInUserType,userDetails);
            } else {
                return setFinalOrderCollection(authorisation, caseData, userDetails);
            }
        }
        return new HashMap<>();
    }

    private Map<String, Object> setFinalOrderCollection(String authorisation, CaseData caseData, UserDetails userDetails) throws Exception {
        List<Element<OrderDetails>> orderCollection;
        orderCollection = caseData.getOrderCollection() != null ? caseData.getOrderCollection() : new ArrayList<>();
        List<Element<OrderDetails>> newOrderDetails = getCurrentOrderDetails(authorisation, caseData, userDetails);
        if (isNotEmpty(caseData.getManageOrders().getServeOrderDynamicList())
            && CollectionUtils.isNotEmpty(caseData.getManageOrders().getServeOrderDynamicList().getValue())
            && Yes.equals(caseData.getServeOrderData().getDoYouWantToServeOrder())) {
            updateCurrentOrderId(
                caseData.getManageOrders().getServeOrderDynamicList(),
                orderCollection,
                newOrderDetails
            );
        }
        orderCollection.addAll(newOrderDetails);
        orderCollection.sort(Comparator.comparing(
            m -> m.getValue().getDateCreated(),
            Comparator.reverseOrder()
        ));
        if (Yes.equals(caseData.getServeOrderData().getDoYouWantToServeOrder())) {
            orderCollection = serveOrder(caseData, orderCollection);
        }
        LocalDateTime currentOrderCreatedDateTime = newOrderDetails.get(0).getValue().getDateCreated();
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("currentOrderCreatedDateTime", currentOrderCreatedDateTime);
        orderMap.put(ORDER_COLLECTION, orderCollection);
        return orderMap;
    }

    public static void updateCurrentOrderId(DynamicMultiSelectList serveOrderDynamicList,
                                            List<Element<OrderDetails>> existingOrderCollection,
                                            List<Element<OrderDetails>> newOrderDetails) {
        String currentOrderId;
        List<String> selectedOrderIds = serveOrderDynamicList.getValue()
            .stream().map(DynamicMultiselectListElement::getCode).toList();
        List<UUID> existingOrderIds = existingOrderCollection.stream().map(Element::getId).toList();

        currentOrderId = selectedOrderIds
            .stream()
            .filter(selectedOrderId -> !existingOrderIds.contains(UUID.fromString(selectedOrderId)))
            .collect(Collectors.joining());
        if (StringUtils.isNotBlank((currentOrderId))) {
            newOrderDetails.set(
                0,
                element(UUID.fromString(currentOrderId), newOrderDetails.get(0).getValue())
            );
        }
    }

    public Map<String, Object> setDraftOrderCollection(CaseData caseData, String loggedInUserType,UserDetails userDetails) {
        List<Element<DraftOrder>> draftOrderList = new ArrayList<>();
        Element<DraftOrder> draftOrderElement = null;
        if (caseData.getManageOrdersOptions().equals(uploadAnOrder)) {
            draftOrderElement = element(getCurrentUploadDraftOrderDetails(caseData, loggedInUserType, userDetails));
        } else {
            draftOrderElement = element(getCurrentCreateDraftOrderDetails(caseData, loggedInUserType, userDetails));
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

    public DraftOrder getCurrentCreateDraftOrderDetails(CaseData caseData, String loggedInUserType, UserDetails userDetails) {
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
                              .orderCreatedBy(userDetails.getFullName())
                              .orderCreatedByEmailId(userDetails.getEmail())
                              .dateCreated(dateTime.now())
                              .status(getOrderStatus(orderSelectionType, loggedInUserType, null, null))
                              .isJudgeApprovalNeeded(AmendOrderCheckEnum.noCheck.equals(
                                  caseData.getManageOrders().getAmendOrderSelectCheckOptions())
                                                         || AmendOrderCheckEnum.managerCheck.equals(
                                  caseData.getManageOrders().getAmendOrderSelectCheckOptions())
                                                         || UserRoles.JUDGE.name().equalsIgnoreCase(loggedInUserType)
                                                         ? No : Yes)
                              .reviewRequiredBy(caseData.getManageOrders().getAmendOrderSelectCheckOptions())
                              .nameOfJudgeForReview(caseData.getManageOrders().getNameOfJudgeAmendOrder())
                              .nameOfLaForReview(caseData.getManageOrders().getNameOfLaAmendOrder())
                              .nameOfJudgeForReviewOrder(String.valueOf(caseData.getManageOrders().getNameOfJudgeToReviewOrder()))
                              .nameOfLaForReviewOrder(String.valueOf(caseData.getManageOrders().getNameOfLaToReviewOrder()))
                              .additionalRequirementsForHearingReq(getAdditionalRequirementsForHearingReq(
                                                                           caseData.getManageOrders().getOrdersHearingDetails(),
                                                                           true,
                                                                           caseData.getStandardDirectionOrder(),
                                                                           caseData.getCreateSelectOrderOptions(),
                                                                           caseData.getManageOrders().getC21OrderOptions()))
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
            .manageOrdersApplicant(CaseUtils.getApplicant(caseData))
            .manageOrdersApplicantReference(CaseUtils.getApplicantReference(caseData))
            .manageOrdersRespondent(CaseUtils.getRespondent(caseData))
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
            .underTakingExpiryDateTime(caseData.getManageOrders().getUnderTakingExpiryDateTime())
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

    public String getApplicant(CaseData caseData) {
        if (!C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            return String.format(PrlAppsConstants.FORMAT, caseData.getApplicantsFL401().getFirstName(),
                                 caseData.getApplicantsFL401().getLastName()
            );
        }
        return null;
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

    public DraftOrder getCurrentUploadDraftOrderDetails(CaseData caseData, String loggedInUserType, UserDetails userDetails) {
        String flagSelectedOrder = getSelectedOrderInfoForUpload(caseData);
        SelectTypeOfOrderEnum typeOfOrder = CaseUtils.getSelectTypeOfOrder(caseData);
        String orderSelectionType = CaseUtils.getOrderSelectionType(caseData);

        return DraftOrder.builder()
            .typeOfOrder(typeOfOrder != null ? typeOfOrder.getDisplayedValue() : null)
            .orderType(CreateSelectOrderOptionsEnum.getIdFromValue(flagSelectedOrder))
            .orderTypeId(flagSelectedOrder)
            .orderDocument(caseData.getUploadOrderDoc())
            .isTheOrderAboutChildren(caseData.getManageOrders().getIsTheOrderAboutChildren())
            .isTheOrderAboutAllChildren(caseData.getManageOrders().getIsTheOrderAboutAllChildren())
            .childOption(getChildOption(caseData))
            .childrenList(caseData.getManageOrders() != null
                              ? getSelectedChildInfoFromMangeOrder(caseData) : null)
            .otherDetails(OtherDraftOrderDetails.builder()
                              .createdBy(caseData.getJudgeOrMagistratesLastName())
                              .orderCreatedBy(userDetails.getFullName())
                              .orderCreatedByEmailId(userDetails.getEmail())
                              .dateCreated(dateTime.now())
                              .status(getOrderStatus(orderSelectionType, loggedInUserType, null, null))
                              .isJudgeApprovalNeeded(AmendOrderCheckEnum.noCheck.equals(
                                  caseData.getManageOrders().getAmendOrderSelectCheckOptions())
                                                         || AmendOrderCheckEnum.managerCheck.equals(
                                  caseData.getManageOrders().getAmendOrderSelectCheckOptions())
                                                         || UserRoles.JUDGE.name().equalsIgnoreCase(loggedInUserType)
                                                         ? No : Yes)
                              .reviewRequiredBy(caseData.getManageOrders().getAmendOrderSelectCheckOptions()) //PRL-4854
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
            //PRL-4854 - persist hearingsType dynamicList
            .hearingsType(caseData.getManageOrders().getHearingsType())
            .wasTheOrderApprovedAtHearing(caseData.getWasTheOrderApprovedAtHearing())
            .isTheOrderByConsent(caseData.getManageOrders().getIsTheOrderByConsent())
            .build();
    }

    private YesOrNo getIsUploadedFlag(ManageOrdersOptionsEnum manageOrdersOptions, String loggedInUserType) {
        YesOrNo isUploaded = No;
        if (UserRoles.SOLICITOR.name().equals(loggedInUserType) || (uploadAnOrder.equals(manageOrdersOptions))) {
            isUploaded = Yes;
        }
        return isUploaded;
    }

    public String getOrderStatus(String orderSelectionType, String loggedInUserType, String eventId, String previousOrderStatus) {
        String currentOrderStatus;
        if (Event.ADMIN_EDIT_AND_APPROVE_ORDER.getId().equals(eventId)) {
            currentOrderStatus = OrderStatusEnum.reviewedByCA.getDisplayedValue();
        } else if (Event.EDIT_AND_APPROVE_ORDER.getId().equals(eventId)) {
            if (UserRoles.JUDGE.name().equals(loggedInUserType)) {
                currentOrderStatus = OrderStatusEnum.reviewedByJudge.getDisplayedValue();
            } else {
                currentOrderStatus = OrderStatusEnum.reviewedByManager.getDisplayedValue();
            }
        } else if (Event.EDIT_RETURNED_ORDER.getId().equals(eventId)) {
            return OrderStatusEnum.draftedByLR.getDisplayedValue();
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
        if (null != caseData.getManageOrders() && null != caseData.getManageOrders().getServeOrderDynamicList()) {
            List<String> selectedOrderIds = caseData.getManageOrders().getServeOrderDynamicList().getValue()
                .stream().map(DynamicMultiselectListElement::getCode).toList();
            orders.stream()
                .filter(order -> selectedOrderIds.contains(order.getId().toString()))
                .forEach(order -> {
                    if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                        servedC100Order(caseData, orders, order, selectedOrderIds.size() > 1);
                    } else {
                        servedFL401Order(caseData, orders, order, selectedOrderIds.size() > 1);
                    }
                });
        }
        return orders;
    }

    private void servedFL401Order(CaseData caseData, List<Element<OrderDetails>> orders, Element<OrderDetails> order,
                                  boolean isMultipleOrdersServed) {
        YesOrNo otherPartiesServed = No;
        List<Element<PostalInformation>> postalInformation = null;
        List<Element<EmailInformation>> emailInformation = null;
        PartyDetails partyDetails = caseData.getApplicantsFL401();
        String serveRecipientName = null;
        if (null != partyDetails && null != partyDetails.getRepresentativeFullName()) {
            serveRecipientName =  partyDetails.getRepresentativeFullName();
        }
        if (isNotEmpty(caseData.getManageOrders().getServeOtherPartiesDA())) {
            otherPartiesServed = Yes;
            Map<String, Object> emailOrPostalInfo = new HashMap<>();
            getEmailAndPostalInfoOfOrg(caseData, emailOrPostalInfo);
            postalInformation = (List<Element<PostalInformation>>) emailOrPostalInfo.get(POST);
            emailInformation = (List<Element<EmailInformation>>) emailOrPostalInfo.get(EMAIL);
        }
        List<Element<ServedParties>> servedParties = getUpdatedServedParties(caseData, order,serveRecipientName);
        SoaSolicitorServingRespondentsEnum servingRespondentsOptions = caseData.getManageOrders()
            .getServingRespondentsOptionsDA();
        Map<String, Object> servedOrderDetails = new HashMap<>();
        servedOrderDetails.put(SERVING_RESPONDENTS_OPTIONS, servingRespondentsOptions);
        servedOrderDetails.put(SERVED_PARTIES, servedParties);
        servedOrderDetails.put(OTHER_PARTIES_SERVED, otherPartiesServed);
        servedOrderDetails.put(WHO_IS_RESPONSIBLE_TO_SERVE, getWhoIsResponsibleToServeOrderDA(caseData.getManageOrders()));
        servedOrderDetails.put(IS_MULTIPLE_ORDERS_SERVED, isMultipleOrdersServed);

        if (null != serveRecipientName
            && null != servingRespondentsOptions) {
            servedOrderDetails.put(SERVE_RECIPIENT_NAME, serveRecipientName + " (" + SoaSolicitorServingRespondentsEnum
                .applicantLegalRepresentative.getDisplayedValue() + ")");
        }

        updateServedOrderDetails(
            servedOrderDetails,
            null,
            orders,
            order,
            postalInformation,
            emailInformation
        );
    }

    private List<Element<ServedParties>> getUpdatedServedParties(CaseData caseData, Element<OrderDetails> order,
                                                                 String representativeName) {
        List<Element<ServedParties>> servedParties  = getServedParties(caseData, representativeName);
        if (null != order.getValue().getServeOrderDetails()
            && CollectionUtils.isNotEmpty(order.getValue().getServeOrderDetails().getServedParties())) {
            servedParties.addAll(order.getValue().getServeOrderDetails().getServedParties());
        }
        return servedParties;
    }

    private void servedC100Order(CaseData caseData, List<Element<OrderDetails>> orders, Element<OrderDetails> order, boolean isMultipleOrdersServed) {
        YesOrNo serveOnRespondent = caseData.getManageOrders().getServeToRespondentOptions();
        Element<PartyDetails> partyDetailsElement = caseData.getApplicants().get(0);
        String serveRecipientName = null;
        if (null != partyDetailsElement.getValue().getRepresentativeFullName()) {
            serveRecipientName =  partyDetailsElement.getValue().getRepresentativeFullName();
        }

        SoaSolicitorServingRespondentsEnum servingRespondentsOptions = null;
        String recipients = null;
        if (Yes.equals(serveOnRespondent)) {
            servingRespondentsOptions = getServingRespondentsOptions(caseData);
        } else {
            recipients = getRecipients(caseData);
        }

        String otherParties;
        otherParties = getOtherParties(caseData);
        YesOrNo otherPartiesServed = No;
        List<Element<PostalInformation>> postalInformation = null;
        List<Element<EmailInformation>> emailInformation = null;
        if (isNotEmpty(caseData.getManageOrders().getServeOtherPartiesCA())) {
            otherPartiesServed = Yes;
            Map<String, Object> emailOrPostalInfo = new HashMap<>();
            getEmailAndPostalInfoOfOrg(caseData, emailOrPostalInfo);
            postalInformation = (List<Element<PostalInformation>>) emailOrPostalInfo.get(POST);
            emailInformation = (List<Element<EmailInformation>>) emailOrPostalInfo.get(EMAIL);
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

        List<Element<ServedParties>> servedParties = getUpdatedServedParties(caseData, order,serveRecipientName);
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
        servedOrderDetails.put(WHO_IS_RESPONSIBLE_TO_SERVE, getWhoIsResponsibleToServeOrderCA(caseData.getManageOrders()));
        servedOrderDetails.put(IS_MULTIPLE_ORDERS_SERVED, isMultipleOrdersServed);

        if (null != serveRecipientName
            && null != servingRespondentsOptions) {
            servedOrderDetails.put(SERVE_RECIPIENT_NAME, serveRecipientName + " (" + SoaSolicitorServingRespondentsEnum
                .applicantLegalRepresentative.getDisplayedValue() + ")");
        }
        updateServedOrderDetails(
            servedOrderDetails,
            cafcassCymruEmail,
            orders,
            order,
            postalInformation,
            emailInformation
        );
    }

    private String getWhoIsResponsibleToServeOrderCA(ManageOrders manageOrders) {
        if (Yes.equals(manageOrders.getServeToRespondentOptions())) {
            return NO.equals(manageOrders.getDisplayLegalRepOption())
                ? manageOrders.getServingOptionsForNonLegalRep().getId()
                : manageOrders.getServingRespondentsOptionsCA().getId();
        }
        return null;
    }

    private String getWhoIsResponsibleToServeOrderDA(ManageOrders manageOrders) {
        if (Yes.equals(manageOrders.getServeToRespondentOptions())) {
            return NO.equals(manageOrders.getDisplayLegalRepOption())
                ? manageOrders.getServingOptionsForNonLegalRep().getId()
                : manageOrders.getServingRespondentsOptionsDA().getId();
        }
        return null;
    }

    private List<Element<ServedParties>> getServedParties(CaseData caseData, String representativeName) {
        List<Element<ServedParties>> servedParties = new ArrayList<>();
        //applicants & respondents
        if (caseData.getManageOrders()
            .getRecipientsOptions() != null) {
            servedParties = dynamicMultiSelectListService
                .getServedPartyDetailsFromDynamicSelectList(caseData
                                                                .getManageOrders()
                                                                .getRecipientsOptions());
        }

        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            //other parties
            if (caseData.getManageOrders().getOtherParties() != null) {
                servedParties.addAll(dynamicMultiSelectListService.getServedPartyDetailsFromDynamicSelectList(
                    caseData.getManageOrders().getOtherParties()
                ));
            }
            //personal service
            SoaSolicitorServingRespondentsEnum servingRespondentsOptionsCA = caseData
                .getManageOrders().getServingRespondentsOptionsCA();
            updatePersonalServedParties(servingRespondentsOptionsCA, servedParties, representativeName);
            //PRL-4113 - update all applicants party ids in case of personal service
            if (Yes.equals(caseData.getManageOrders().getServeToRespondentOptions())) {
                servedParties.addAll(getServedParties(caseData.getApplicants()));
            }
        }
        //FL401 - personal service
        if (FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            SoaSolicitorServingRespondentsEnum servingRespondentsOptionsDA = caseData.getManageOrders().getServingRespondentsOptionsDA();
            updatePersonalServedParties(servingRespondentsOptionsDA, servedParties, representativeName);
            //PRL-4113 - update applicant party id in case of personal service
            servedParties.add(getServedParty(caseData.getApplicantsFL401()));
        }

        return servedParties;
    }

    private List<Element<ServedParties>> getServedParties(List<Element<PartyDetails>> parties) {
        return nullSafeCollection(parties).stream()
            .map(applicant -> element(ServedParties.builder()
                                          .partyId(String.valueOf(applicant.getId()))
                                          .partyName(applicant.getValue().getLabelForDynamicList())
                                          .servedDateTime(zonedDateTime.toLocalDateTime())
                                          .build()))
            .toList();
    }

    private Element<ServedParties> getServedParty(PartyDetails party) {
        return element(ServedParties.builder()
                           .partyId(String.valueOf(party.getPartyId()))
                           .partyName(party.getLabelForDynamicList())
                           .servedDateTime(zonedDateTime.toLocalDateTime())
                           .build());
    }

    private void updatePersonalServedParties(SoaSolicitorServingRespondentsEnum servingRespondentsOptions,
                                             List<Element<ServedParties>> servedParties, String representativeName) {
        if (null != servingRespondentsOptions) {
            if (SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative
                .equals(servingRespondentsOptions)) {
                servedParties.add(element(ServedParties.builder()
                                              .partyId("11111111-1111-1111-1111-111111111111")//adding some default value
                                              .partyName(representativeName + " (" + servingRespondentsOptions
                                                  .getDisplayedValue() + ")")
                                              .servedDateTime(zonedDateTime.toLocalDateTime())
                                              .build()));

            } else {
                servedParties.add(element(ServedParties.builder()
                                              .partyId("00000000-0000-0000-0000-000000000000")//adding some default value
                                              .partyName(servingRespondentsOptions.getDisplayedValue())
                                              .servedDateTime(zonedDateTime.toLocalDateTime())
                                              .build()));
            }
        }
    }

    private static SoaSolicitorServingRespondentsEnum getServingRespondentsOptions(CaseData caseData) {
        SoaSolicitorServingRespondentsEnum servingRespondentsOptions = null;
        if (caseData.getManageOrders()
            .getServingRespondentsOptionsCA() != null) {
            servingRespondentsOptions = caseData.getManageOrders()
                .getServingRespondentsOptionsCA();
        }
        return servingRespondentsOptions;
    }

    private static void getEmailAndPostalInfoOfOrg(CaseData caseData, Map<String, Object> emailOrPostalInfo) {
        List<Element<PostalInformation>> postalInformation = new ArrayList<>();
        List<Element<EmailInformation>> emailInformation = new ArrayList<>();
        if (null != caseData.getManageOrders().getServeOrgDetailsList()) {
            caseData.getManageOrders().getServeOrgDetailsList().stream().map(Element::getValue)
                .forEach(serveOther -> {
                    if (DeliveryByEnum.post.equals(serveOther.getServeByPostOrEmail())) {
                        postalInformation.add(element(serveOther.getPostalInformation()));
                    } else if (DeliveryByEnum.email.equals(serveOther.getServeByPostOrEmail())) {
                        emailInformation.add(element(serveOther.getEmailInformation()));
                    }
                });
        }
        emailOrPostalInfo.put(EMAIL, emailInformation);
        emailOrPostalInfo.put(POST, postalInformation);
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
        }
        otherParties = String.join(", ", otherPartiesList);
        return otherParties;
    }

    private String getRecipients(CaseData caseData) {
        String recipients = null;
        if (caseData.getManageOrders()
            .getRecipientsOptions() != null) {
            recipients = dynamicMultiSelectListService
                .getStringFromDynamicMultiSelectList(caseData.getManageOrders().getRecipientsOptions());

        }
        return recipients;
    }

    private static void updateServedOrderDetails(Map<String, Object> servedOrderDetails, String cafcassCymruEmail, List<Element<OrderDetails>> orders,
                                                 Element<OrderDetails> order, List<Element<PostalInformation>> postalInformation,
                                                 List<Element<EmailInformation>> emailInformation) {
        YesOrNo cafcassServed = null;
        YesOrNo cafcassCymruServed = null;
        String cafcassEmail = null;
        YesOrNo serveOnRespondent = null;
        YesOrNo otherPartiesServed = null;
        SoaSolicitorServingRespondentsEnum servingRespondentsOptions = null;
        SoaSolicitorServingRespondentsEnum courtPersonalService = null;
        String recipients = null;
        String otherParties = null;
        List<Element<ServedParties>> servedParties = new ArrayList<>();

        String serveRecipientName = null;
        String whoIsResponsibleToServe = null;
        YesOrNo multipleOrdersServed = null;

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
            servingRespondentsOptions = (SoaSolicitorServingRespondentsEnum) servedOrderDetails.get(SERVING_RESPONDENTS_OPTIONS);
            courtPersonalService = (SoaSolicitorServingRespondentsEnum) servedOrderDetails.get(SERVING_RESPONDENTS_OPTIONS);
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
        if (servedOrderDetails.containsKey(SERVE_RECIPIENT_NAME)) {
            serveRecipientName = (String) servedOrderDetails.get(SERVE_RECIPIENT_NAME);
        }
        if (servedOrderDetails.containsKey(WHO_IS_RESPONSIBLE_TO_SERVE)) {
            whoIsResponsibleToServe = (String) servedOrderDetails.get(WHO_IS_RESPONSIBLE_TO_SERVE);
        }
        if (servedOrderDetails.containsKey(IS_MULTIPLE_ORDERS_SERVED)) {
            multipleOrdersServed = (boolean) servedOrderDetails.get(IS_MULTIPLE_ORDERS_SERVED) ? Yes : No;
        }

        ServeOrderDetails tempServeOrderDetails;
        if (order.getValue().getServeOrderDetails() != null) {
            tempServeOrderDetails = order.getValue().getServeOrderDetails();
        } else {
            tempServeOrderDetails = ServeOrderDetails.builder().build();
        }

        List<String> orgNameList = new ArrayList<>();
        String organisationsName = null;
        if (null != postalInformation) {
            orgNameList.addAll(postalInformation.stream()
                .map(postalInfoElem -> postalInfoElem.getValue().getPostalName()).toList());
        }

        if (null != emailInformation) {
            orgNameList.addAll(emailInformation.stream()
                .map(emailInfoElem -> emailInfoElem.getValue().getEmailName()).toList());
        }

        if (!orgNameList.isEmpty()) {
            organisationsName = String.join(", ", orgNameList);
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
            .organisationsName(organisationsName)
            .servedParties(servedParties)
            .servingRecipientName(serveRecipientName)
            .courtPersonalService(courtPersonalService)
            .whoIsResponsibleToServe(whoIsResponsibleToServe)
            .multipleOrdersServed(multipleOrdersServed)
            .build();

        OrderDetails amended = order.getValue().toBuilder()
            .otherDetails(updateOtherOrderDetails(order.getValue().getOtherDetails()))
            .serveOrderDetails(serveOrderDetails)
            .build();

        orders.set(orders.indexOf(order), element(order.getId(), amended));
    }

    private static OtherOrderDetails updateOtherOrderDetails(OtherOrderDetails otherDetails) {
        return otherDetails.toBuilder()
            .orderCreatedBy(otherDetails.getOrderCreatedBy())
            .orderServedDate(LocalDate.now().format(DateTimeFormatter.ofPattern(
                PrlAppsConstants.D_MMM_YYYY,
                Locale.ENGLISH
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
            .toList();

        List<String> nameList = appointedGuardianFullNameList.stream()
            .map(AppointedGuardianFullName::getGuardianFullName)
            .toList();

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


    public Map<String, Object> generateOrderDocumentFromDocmosis(String authorisation,
                                                                 CaseData caseData,
                                                                 CreateSelectOrderOptionsEnum selectOrderOption)
        throws Exception {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        try {
            populateChildrenListForDocmosis(caseData);

            if (CollectionUtils.isNotEmpty(caseData.getManageOrders().getOrdersHearingDetails())) {
                caseDataUpdated.put(ORDER_HEARING_DETAILS, caseData.getManageOrders().getOrdersHearingDetails());
            }
            if (CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(selectOrderOption)) {
                caseData = populateJudgeNames(caseData);
                caseData = populatePartyDetailsOfNewParterForDocmosis(caseData);
                if (isNotEmpty(caseData.getStandardDirectionOrder())
                    && CollectionUtils.isNotEmpty(caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList())
                    && caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(factFindingHearing)) {
                    caseData = populateDirectionOfFactFindingHearingFieldsForDocmosis(caseData);
                }
            }
            Map<String, String> fieldsMap = getOrderTemplateAndFile(selectOrderOption);
            updateDocmosisAttributes(authorisation, caseData, caseDataUpdated, fieldsMap);
        } catch (Exception ex) {
            log.error("Error occured while generating Draft document ==> ", ex);
        }
        return caseDataUpdated;
    }

    private void updateDocmosisAttributes(String authorisation,
                                          CaseData caseData,
                                          Map<String, Object> caseDataUpdated,
                                          Map<String, String> fieldsMap) throws Exception {
        GeneratedDocumentInfo generatedDocumentInfo;
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        if (documentLanguage.isGenEng()) {
            caseDataUpdated.put("isEngDocGen", Yes.toString());
            generatedDocumentInfo = dgsService.generateDocument(
                authorisation,
                CaseDetails.builder().caseData(caseData).build(),
                fieldsMap.get(PrlAppsConstants.TEMPLATE)
            );
            caseDataUpdated.put(PREVIEW_ORDER_DOC, Document.builder()
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
                caseData = populateJudgeNames(caseData);
                caseData = populatePartyDetailsOfNewParterForDocmosis(caseData);
                if (isNotEmpty(caseData.getStandardDirectionOrder())
                    && CollectionUtils.isNotEmpty(caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList())
                    && caseData.getStandardDirectionOrder().getSdoHearingsAndNextStepsList().contains(factFindingHearing)) {
                    caseData = populateDirectionOfFactFindingHearingFieldsForDocmosis(caseData);
                }
            }
            DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
            if (documentLanguage.isGenEng()) {
                caseDataUpdated.put("isEngDocGen", Yes.toString());
                generatedDocumentInfo = dgsService.generateDocument(
                        authorisation,
                        CaseDetails.builder().caseData(caseData).build(),
                        fieldsMap.get(PrlAppsConstants.TEMPLATE)
                    );
                caseDataUpdated.put(PREVIEW_ORDER_DOC, Document.builder()
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
            log.error("Error occured while generating Draft document ==> ", ex);
        }
        return caseDataUpdated;
    }

    public CaseData populateJudgeNames(CaseData caseData) {
        if (isNotEmpty(caseData.getStandardDirectionOrder())
            && (isNotEmpty(caseData.getStandardDirectionOrder().getSdoAllocateOrReserveJudgeName())
            || isNotEmpty(caseData.getStandardDirectionOrder().getSdoNextStepJudgeName()))) {
            String sdoNamedJudgeFullName = getJudgeFullName(
                caseData.getStandardDirectionOrder().getSdoAllocateOrReserveJudgeName()
            );
            String sdoAllocateDecisionJudgeFullName = getJudgeFullName(
                caseData.getStandardDirectionOrder().getSdoNextStepJudgeName()
            );
            caseData = caseData.toBuilder()
                .standardDirectionOrder(caseData.getStandardDirectionOrder().toBuilder()
                                            .sdoNamedJudgeFullName(sdoNamedJudgeFullName)
                                            .sdoAllocateDecisionJudgeFullName(sdoAllocateDecisionJudgeFullName).build())
                .build();
        }
        return caseData;
    }

    public CaseData populatePartyDetailsOfNewParterForDocmosis(CaseData caseData) {
        if (isNotEmpty(caseData.getStandardDirectionOrder().getSdoNewPartnerPartiesCafcass()) && CollectionUtils.isNotEmpty(
            caseData.getStandardDirectionOrder().getSdoNewPartnerPartiesCafcass().getValue())) {
            String partyDetailsForCafcass = dynamicMultiSelectListService
                .getStringFromDynamicMultiSelectList(caseData.getStandardDirectionOrder().getSdoNewPartnerPartiesCafcass());
            caseData = caseData.toBuilder()
                .standardDirectionOrder(caseData.getStandardDirectionOrder().toBuilder().sdoNewPartnerPartiesCafcassText(
                    partyDetailsForCafcass).build())
                .build();
        }
        if (isNotEmpty(caseData.getStandardDirectionOrder().getSdoNewPartnerPartiesCafcassCymru()) && CollectionUtils.isNotEmpty(
            caseData.getStandardDirectionOrder().getSdoNewPartnerPartiesCafcassCymru().getValue())) {
            String partyDetailsForCafcassCymru = dynamicMultiSelectListService
                .getStringFromDynamicMultiSelectList(caseData.getStandardDirectionOrder().getSdoNewPartnerPartiesCafcassCymru());
            caseData = caseData.toBuilder()
                .standardDirectionOrder(caseData.getStandardDirectionOrder().toBuilder().sdoNewPartnerPartiesCafcassCymruText(
                    partyDetailsForCafcassCymru).build())
                .build();
        }
        return caseData;
    }

    public CaseData populateDirectionOfFactFindingHearingFieldsForDocmosis(CaseData caseData) {
        String sdoWhoNeedsToRespondAllegationsListText = null;
        String sdoWhoMadeAllegationsListText = null;
        if (isNotEmpty(caseData.getStandardDirectionOrder().getSdoWhoMadeAllegationsList()) && CollectionUtils.isNotEmpty(
            caseData.getStandardDirectionOrder().getSdoWhoMadeAllegationsList().getValue())) {
            sdoWhoMadeAllegationsListText = dynamicMultiSelectListService
                .getStringFromDynamicMultiSelectList(caseData.getStandardDirectionOrder().getSdoWhoMadeAllegationsList());
            sdoWhoMadeAllegationsListText = sdoWhoMadeAllegationsListText.replace(COMMA, AND);
        }
        if (isNotEmpty(caseData.getStandardDirectionOrder().getSdoWhoNeedsToRespondAllegationsList()) && CollectionUtils.isNotEmpty(
            caseData.getStandardDirectionOrder().getSdoWhoNeedsToRespondAllegationsList().getValue())) {
            sdoWhoNeedsToRespondAllegationsListText = dynamicMultiSelectListService
                .getStringFromDynamicMultiSelectList(caseData.getStandardDirectionOrder().getSdoWhoNeedsToRespondAllegationsList());
            sdoWhoNeedsToRespondAllegationsListText = sdoWhoNeedsToRespondAllegationsListText.replace(",", AND);
        }
        caseData = caseData.toBuilder()
            .standardDirectionOrder(caseData.getStandardDirectionOrder().toBuilder()
                                        .sdoWhoNeedsToRespondAllegationsListText(sdoWhoNeedsToRespondAllegationsListText)
                                        .sdoWhoMadeAllegationsListText(sdoWhoMadeAllegationsListText)
                                        .build())
            .isCafcass(caseData.getCaseManagementLocation() != null
                           ? CaseUtils.cafcassFlag(caseData.getCaseManagementLocation().getRegion()) : null)
            .build();
        return caseData;
    }

    public  CaseData filterEmptyHearingDetails(CaseData caseData) {
        List<Element<HearingData>> filteredHearingDataList = caseData.getManageOrders().getOrdersHearingDetails()
            .stream()
            .filter(element -> ((element.getValue().getHearingTypes() != null && element.getValue().getHearingTypes().getValue() != null)
                || element.getValue().getHearingDateConfirmOptionEnum() != null))
            .toList();
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

    public CaseData getN117FormData(CaseData caseData) {

        PartyDetails applicant1 = C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
            ? caseData.getApplicants().get(0).getValue() : caseData.getApplicantsFL401();
        PartyDetails respondent1 = C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
            ? caseData.getRespondents().get(0).getValue() : caseData.getRespondentsFL401();
        ManageOrders orderData = caseData.getManageOrders().toBuilder()
            .manageOrdersCaseNo(String.valueOf(caseData.getId()))
            .recitalsOrPreamble(caseData.getManageOrders().getRecitalsOrPreamble())
            .isCaseWithdrawn(caseData.getManageOrders().getIsCaseWithdrawn())
            .isTheOrderByConsent(caseData.getManageOrders().getIsTheOrderByConsent())
            .judgeOrMagistrateTitle(caseData.getManageOrders().getJudgeOrMagistrateTitle())
            .orderDirections(caseData.getManageOrders().getOrderDirections())
            .furtherDirectionsIfRequired(caseData.getManageOrders().getFurtherDirectionsIfRequired())
            .furtherInformationIfRequired(caseData.getManageOrders().getFurtherInformationIfRequired())
            .manageOrdersCourtName(null != caseData.getCourtName() ? caseData.getCourtName() : null)
            .manageOrdersApplicant(String.format(PrlAppsConstants.FORMAT, applicant1.getFirstName(),
                                                 applicant1.getLastName()
            ))
            .manageOrdersRespondent(String.format(
                PrlAppsConstants.FORMAT,
                respondent1.getFirstName(),
                respondent1.getLastName()
            ))
            .manageOrdersApplicantReference(applicant1.getSolicitorReference() != null
                                                ? applicant1.getSolicitorReference() : "")
            //PRL-5137
            .manageOrdersRespondentReference(respondent1.getSolicitorReference() != null
                                                 ? respondent1.getSolicitorReference() : "")
            .build();

        if (ofNullable(respondent1.getAddress()).isPresent()) {
            orderData = orderData.toBuilder()
                .manageOrdersRespondentAddress(respondent1.getAddress()).build();
        }
        if (ofNullable(respondent1.getDateOfBirth()).isPresent()) {
            orderData = orderData.toBuilder()
                .manageOrdersRespondentDob(respondent1.getDateOfBirth()).build();
        }

        return caseData.toBuilder().manageOrders(orderData)
            .selectedOrder(getSelectedOrderInfo(caseData)).build();
    }

    public CaseData populateCustomOrderFields(CaseData caseData, CreateSelectOrderOptionsEnum order) {

        switch (order) {
            case amendDischargedVaried, occupation, nonMolestation, powerOfArrest, blank:
                return getFl404bFields(caseData);
            case generalForm:
                return getN117FormData(caseData);
            case noticeOfProceedings:
                return getFL402FormData(caseData);
            default:
                return caseData;
        }
    }

    public CaseData getFl404bFields(CaseData caseData) {

        FL404 orderData = caseData.getManageOrders().getFl404CustomFields();

        if (orderData != null) {

            PartyDetails applicant1 = C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
                && CollectionUtils.isNotEmpty(caseData.getApplicants())
                ? caseData.getApplicants().get(0).getValue() : caseData.getApplicantsFL401();
            PartyDetails respondent1 = C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
                && CollectionUtils.isNotEmpty(caseData.getRespondents())
                ? caseData.getRespondents().get(0).getValue() : caseData.getRespondentsFL401();
            orderData = orderData.toBuilder()
                .fl404bCaseNumber(String.valueOf(caseData.getId()))
                .fl404bCourtName(caseData.getCourtName())
                .fl404bApplicantName(String.format(
                    PrlAppsConstants.FORMAT,
                    applicant1.getFirstName(),
                    applicant1.getLastName()
                ))
                .fl404bRespondentName(String.format(
                    PrlAppsConstants.FORMAT,
                    respondent1.getFirstName(),
                    respondent1.getLastName()
                ))
                .fl404bApplicantReference(applicant1.getSolicitorReference() != null
                                              ? applicant1.getSolicitorReference() : "")
                .fl404bRespondentReference(respondent1.getSolicitorReference() != null
                                               ? respondent1.getSolicitorReference() : "")
                .build();

            if (ofNullable(respondent1.getAddress()).isPresent()) {
                orderData = orderData.toBuilder()
                    .fl404bRespondentAddress(respondent1.getAddress()).build();
            }
            if (ofNullable(respondent1.getDateOfBirth()).isPresent()) {
                orderData = orderData.toBuilder()
                    .fl404bRespondentDob(respondent1.getDateOfBirth()).build();
            }
        }
        caseData = caseData.toBuilder()
            .manageOrders(caseData.getManageOrders().toBuilder()
                              .fl404CustomFields(orderData)
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

        //FL402-noticeOfProceedings - fix not to overwrite manage orders
        ManageOrders orderData = caseData.getManageOrders().toBuilder()
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
            String template = fieldMap.get(PrlAppsConstants.FINAL_TEMPLATE_NAME);

            GeneratedDocumentInfo generatedDocumentInfo = dgsService.generateDocument(
                authorisation,
                CaseDetails.builder().caseData(caseData).build(),
                template
            );
            orderDetails = orderDetails
                .toBuilder()
                .orderDocument(getGeneratedDocument(generatedDocumentInfo, false, fieldMap))
                .build();
        }
        if (documentLanguage.isGenWelsh()) {
            String welshTemplate = fieldMap.get(FINAL_TEMPLATE_WELSH);
            if (welshTemplate != null && welshTemplate.contains("-WEL-")) {
                GeneratedDocumentInfo generatedDocumentInfoWelsh = dgsService.generateWelshDocument(
                    authorisation,
                    CaseDetails.builder().caseData(caseData).build(),
                    welshTemplate
                );
                orderDetails = orderDetails.toBuilder().orderDocumentWelsh(getGeneratedDocument(
                    generatedDocumentInfoWelsh,
                    true,
                    fieldMap
                )).build();
            }
        }

        UserDetails userDetails = userService.getUserDetails(authorisation);

        return element(orderDetails.toBuilder()
                           .otherDetails(OtherOrderDetails.builder()
                                             .createdBy(caseData.getJudgeOrMagistratesLastName())
                                             .orderCreatedBy(
                                                 (null != orderDetails.getOtherDetails()
                                                     && null != orderDetails.getOtherDetails().getOrderCreatedBy())
                                                            ? orderDetails.getOtherDetails().getOrderCreatedBy() : userDetails.getFullName())
                                             .orderCreatedByEmailId(
                                                 (null != orderDetails.getOtherDetails()
                                                     && null != orderDetails.getOtherDetails().getOrderCreatedByEmailId())
                                                     ? orderDetails.getOtherDetails().getOrderCreatedByEmailId() : userDetails.getEmail()
                                             )
                                             .orderCreatedDate(dateTime.now().format(DateTimeFormatter.ofPattern(
                                                 PrlAppsConstants.D_MMM_YYYY,
                                                 Locale.ENGLISH
                                             )))
                                             .orderMadeDate(caseData.getDateOrderMade() != null ? caseData.getDateOrderMade()
                                                 .format(DateTimeFormatter.ofPattern(
                                                     PrlAppsConstants.D_MMM_YYYY,
                                                     Locale.ENGLISH
                                                 )) : null)
                                             .approvalDate(caseData.getApprovalDate() != null ? caseData.getApprovalDate()
                                                 .format(DateTimeFormatter.ofPattern(
                                                     PrlAppsConstants.D_MMM_YYYY,
                                                     Locale.ENGLISH
                                                 )) : null)
                                             .orderRecipients(getAllRecipients(caseData))
                                             .status(getOrderStatus(CaseUtils.getOrderSelectionType(caseData),
                                                                    getLoggedInUserType(authorisation), null, null
                                             ))
                                             .additionalRequirementsForHearingReq(getAdditionalRequirementsForHearingReq(
                                                     caseData.getManageOrders().getOrdersHearingDetails(),
                                                     false,
                                                     caseData.getStandardDirectionOrder(),
                                                     caseData.getCreateSelectOrderOptions(),
                                                     caseData.getManageOrders().getC21OrderOptions()))
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
                           .isOrderUploaded(No)
                           .build());
    }

    private OrderDetails getNewOrderDetails(String flagSelectedOrderId, String flagSelectedOrder,
                                            CaseData caseData, SelectTypeOfOrderEnum typeOfOrder,
                                            ServeOrderData serveOrderData) {
        return OrderDetails.builder().orderType(flagSelectedOrderId)
            .orderTypeId(flagSelectedOrder)
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
            .childrenList(getChildrenListForNewOrder(caseData))
            .orderClosesCase(SelectTypeOfOrderEnum.finl.equals(typeOfOrder)
                                 ? caseData.getDoesOrderClosesCase() : null)
            .serveOrderDetails(buildServeOrderDetails(serveOrderData))
            .sdoDetails(CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(caseData.getCreateSelectOrderOptions())
                            ? copyPropertiesToSdoDetails(caseData) : null)
            .build();
    }

    private String getChildrenListForNewOrder(CaseData caseData) {
        if (Yes.equals(caseData.getManageOrders().getIsTheOrderAboutChildren())
            || No.equals(caseData.getManageOrders().getIsTheOrderAboutAllChildren())) {
            return dynamicMultiSelectListService.getStringFromDynamicMultiSelectList(caseData.getManageOrders()
                                                                                         .getChildOption());
        } else if (Yes.equals(caseData.getManageOrders().getIsTheOrderAboutAllChildren())) {
            return dynamicMultiSelectListService
                .getStringFromDynamicMultiSelectListFromListItems(caseData.getManageOrders()
                                                                      .getChildOption());
        }
        return null;
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
        if (launchDarklyClient.isFeatureEnabled(ROLE_ASSIGNMENT_API_IN_ORDERS_JOURNEY)) {
            //This would check for roles from AM for Judge/Legal advisor/Court admin
            //if it doesn't find then it will check for idam roles for rest of the users
            RoleAssignmentServiceResponse roleAssignmentServiceResponse = roleAssignmentApi.getRoleAssignments(
                authorisation,
                authTokenGenerator.generate(),
                null,
                userDetails.getId()
            );
            List<String> roles = roleAssignmentServiceResponse.getRoleAssignmentResponse().stream().map(role -> role.getRoleName()).collect(
                Collectors.toList());
            if (roles.stream().anyMatch(InternalCaseworkerAmRolesEnum.JUDGE.getRoles()::contains)
                || roles.stream().anyMatch(InternalCaseworkerAmRolesEnum.LEGAL_ADVISER.getRoles()::contains)) {
                loggedInUserType = UserRoles.JUDGE.name();
            } else if (roles.stream().anyMatch(InternalCaseworkerAmRolesEnum.COURT_ADMIN.getRoles()::contains)) {
                loggedInUserType = UserRoles.COURT_ADMIN.name();
            } else if (userDetails.getRoles().contains(Roles.SOLICITOR.getValue())) {
                loggedInUserType = UserRoles.SOLICITOR.name();
            } else if (userDetails.getRoles().contains(Roles.CITIZEN.getValue())) {
                loggedInUserType = UserRoles.CITIZEN.name();
            } else if (userDetails.getRoles().contains(Roles.SYSTEM_UPDATE.getValue())) {
                loggedInUserType = UserRoles.SYSTEM_UPDATE.name();
            } else {
                loggedInUserType = "";
            }
        } else {
            if (userDetails.getRoles().contains(Roles.JUDGE.getValue()) || userDetails.getRoles().contains(Roles.LEGAL_ADVISER.getValue())) {
                loggedInUserType = UserRoles.JUDGE.name();
            } else if (userDetails.getRoles().contains(Roles.COURT_ADMIN.getValue())) {
                loggedInUserType = UserRoles.COURT_ADMIN.name();
            } else if (userDetails.getRoles().contains(Roles.SOLICITOR.getValue())) {
                loggedInUserType = UserRoles.SOLICITOR.name();
            } else if (userDetails.getRoles().contains(Roles.CITIZEN.getValue())) {
                loggedInUserType = UserRoles.CITIZEN.name();
            } else if (userDetails.getRoles().contains(Roles.SYSTEM_UPDATE.getValue())) {
                loggedInUserType = UserRoles.SYSTEM_UPDATE.name();
            } else {
                loggedInUserType = "";
            }
        }

        return loggedInUserType;
    }

    public static void cleanUpSelectedManageOrderOptions(Map<String, Object> caseDataUpdated) {
        for (ManageOrderFieldsEnum field : ManageOrderFieldsEnum.values()) {
            caseDataUpdated.remove(field.getValue());
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
            if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
                || ManageOrdersUtils.isDaOrderSelectedForCaCase(
                String.valueOf(caseData.getCreateSelectOrderOptions()),
                caseData)) {
                caseData = populateCustomOrderFields(caseData, caseData.getCreateSelectOrderOptions());
            }
            caseDataUpdated.putAll(getCaseData(authorisation, caseData, caseData.getCreateSelectOrderOptions()));
            if (caseData.getCreateSelectOrderOptions() != null
                && CreateSelectOrderOptionsEnum.specialGuardianShip.equals(caseData.getCreateSelectOrderOptions())) {
                caseDataUpdated.put("appointedGuardianName",
                                    addGuardianDetails(caseData));

            }
        } else {
            caseDataUpdated.put(PREVIEW_ORDER_DOC, caseData.getUploadOrderDoc());
        }
        return caseDataUpdated;
    }

    public List<Element<AppointedGuardianFullName>> addGuardianDetails(CaseData caseData) {
        if (Objects.isNull(caseData.getAppointedGuardianName())
            || CollectionUtils.size(caseData.getAppointedGuardianName()) < 1) {
            List<Element<AppointedGuardianFullName>> appointedGuardianList = new ArrayList<>();
            Element<AppointedGuardianFullName> appointedGuardianFullNameElement =
                element(AppointedGuardianFullName.builder().guardianFullName("").build());
            appointedGuardianList.add(appointedGuardianFullNameElement);
            return appointedGuardianList;
        }
        return caseData.getAppointedGuardianName();
    }

    public Map<String, Object> serveOrderMidEvent(CallbackRequest callbackRequest) {
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
        log.info(" serve order dynamic select listoo {}", caseDataUpdated.get("serveOrderDynamicList"));
        caseDataUpdated.put(DISPLAY_LEGAL_REP_OPTION, "No");
        log.info("---- Check display legal rep options  ----");
        if (C100_CASE_TYPE.equals(CaseUtils.getCaseTypeOfApplication(caseData))) {
            log.info("---- C100 check ----");
            caseData.getApplicants().stream().findFirst().ifPresent(party ->
                populateLegalRepFlag(party.getValue().getSolicitorEmail(), caseDataUpdated));
        } else {
            populateLegalRepFlag(caseData.getApplicantsFL401().getSolicitorEmail(), caseDataUpdated);
        }
        log.info("---- display Legal rep ----{}", caseDataUpdated.get(DISPLAY_LEGAL_REP_OPTION));
        return caseDataUpdated;
    }

    private void populateLegalRepFlag(String email, Map<String, Object> caseDataUpdated) {
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(email)) {
            log.info("---- Legal rep present ----");
            caseDataUpdated.put(DISPLAY_LEGAL_REP_OPTION, "Yes");
        }
    }


    public DynamicList populateHearingsDropdown(String authorization, CaseData caseData) {
        Optional<Hearings> hearings = Optional.ofNullable(hearingService.getHearings(
            authorization,
            String.valueOf(caseData.getId())
        ));
        List<CaseHearing> caseHearings = hearings.map(Hearings::getCaseHearings).orElseGet(ArrayList::new);
        List<String> hearingStatusFilterList = Arrays.stream(hearingStatusesToFilter.trim().split(COMMA)).map(String::trim).toList();
        log.info("Hearing statuses to filter {}", hearingStatusFilterList);
        List<CaseHearing> filteredHearings = caseHearings.stream()
            .filter(caseHearing -> hearingStatusFilterList.contains(caseHearing.getHmcStatus()))
            .toList();
        log.info("Filtered hearing {}", filteredHearings);
        //get hearings dropdown
        List<DynamicListElement> hearingDropdowns = filteredHearings.stream()
            .map(caseHearing -> {
                //get hearingType
                String hearingType = String.valueOf(caseHearing.getHearingTypeValue());
                //return hearingId concatenated with hearingDate
                Optional<List<HearingDaySchedule>> hearingDaySchedules = Optional.ofNullable(caseHearing.getHearingDaySchedule());
                return hearingDaySchedules.map(daySchedules -> daySchedules.stream().map(hearingDaySchedule -> {
                    if (null != hearingDaySchedule && null != hearingDaySchedule.getHearingStartDateTime()) {
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss");
                        String hearingDate = hearingDaySchedule.getHearingStartDateTime().format(dateTimeFormatter);
                        return concat(concat(hearingType, " - "), hearingDate);
                    }
                    return null;
                }).filter(Objects::nonNull).toList()).orElse(Collections.emptyList());
            }).map(this::getDynamicListElements)
            .flatMap(Collection::stream)
            .toList();

        //if there are no hearings then dropdown would be empty
        DynamicList existingHearingsType = (null != caseData.getManageOrders() && null != caseData.getManageOrders().getHearingsType())
            ? caseData.getManageOrders().getHearingsType() : null;

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
        if (servedSavedOrders.equals(caseData.getManageOrdersOptions())
            || (null != caseData.getServeOrderData() && Yes.equals(caseData.getServeOrderData().getDoYouWantToServeOrder()))) {
            log.info("inside Mark to serve email notification");
            caseDataUpdated.put("markedToServeEmailNotification", Yes);
        } else {
            caseDataUpdated.put("markedToServeEmailNotification", No);
        }
    }

    public String getJudgeFullName(JudicialUser judge) {
        String judgeFullName = EMPTY_STRING;
        if (isNotEmpty(judge)) {
            String[] personalCodes = new String[1];
            personalCodes[0] = judge.getPersonalCode();
            try {
                List<JudicialUsersApiResponse> judicialUsersApiResponses = refDataUserService.getAllJudicialUserDetails(
                    JudicialUsersApiRequest.builder()
                        .personalCode(personalCodes).build());
                if (CollectionUtils.isNotEmpty(judicialUsersApiResponses)) {
                    judgeFullName = judicialUsersApiResponses.get(0).getFullName();
                }
            } catch (Exception e) {
                log.error("User details not found for personal code {}", personalCodes, e);
            }
        }
        return judgeFullName;
    }

    public Map<String, Object> handlePreviewOrder(CallbackRequest callbackRequest, String authorisation) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        if (MANAGE_ORDERS.getId().equals(callbackRequest.getEventId()) && ManageOrdersOptionsEnum.uploadAnOrder.equals(
            caseData.getManageOrdersOptions())) {
            List<DynamicListElement> legalAdviserList = refDataUserService.getLegalAdvisorList();
            caseDataUpdated.put(
                "nameOfLaToReviewOrder",
                DynamicList.builder().value(DynamicListElement.EMPTY).listItems(legalAdviserList)
                    .build()
            );
        } else {
            //PRL-4212 - update only if existing order hearings are present
            caseData = updateExistingHearingData(authorisation, caseData, caseDataUpdated);
            caseDataUpdated.putAll(populatePreviewOrder(
                authorisation,
                callbackRequest,
                caseData
            ));
            if (CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(caseData.getCreateSelectOrderOptions())) {
                populateWarningMessageIfRequiredForFactFindingHearing(caseData, caseDataUpdated);
            }
        }
        return caseDataUpdated;
    }

    public void populateWarningMessageIfRequiredForFactFindingHearing(CaseData caseData,
                                                                       Map<String, Object> caseDataUpdated) {
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
            && (caseData.getRespondents().size() > 1 || caseData.getApplicants().size() > 1)) {
            caseDataUpdated.put(SDO_FACT_FINDING_FLAG, "<div class=\"govuk-inset-text\"> "
                + "If you need to include directions for a fact-finding hearing, you need to upload the"
                + " order in manage orders instead.</div>");
        } else {
            caseDataUpdated.put(SDO_FACT_FINDING_FLAG, null);
        }
    }

    public CaseData setHearingDataForSdo(CaseData caseData, Hearings hearings, String authorisation) {
        StandardDirectionOrder standardDirectionOrder = caseData.getStandardDirectionOrder();
        HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
            hearingDataService.populateHearingDynamicLists(
                authorisation,
                String.valueOf(caseData.getId()),
                caseData,
                hearings
            );
        HearingData hearingData;
        if (isNotEmpty(standardDirectionOrder.getSdoUrgentHearingDetails())) {
            hearingData =  hearingDataService.getHearingDataForSdo(
                standardDirectionOrder.getSdoUrgentHearingDetails(),
                hearingDataPrePopulatedDynamicLists,
                caseData
            );
            standardDirectionOrder = standardDirectionOrder.toBuilder()
                .sdoUrgentHearingDetails(hearingDataService.getHearingDataForSelectedHearingForSdo(
                    hearingData,
                    hearings,
                    caseData
                ))
                .build();
        }
        if (isNotEmpty(standardDirectionOrder.getSdoPermissionHearingDetails())) {
            hearingData =  hearingDataService.getHearingDataForSdo(
                standardDirectionOrder.getSdoPermissionHearingDetails(),
                hearingDataPrePopulatedDynamicLists,
                caseData
            );
            standardDirectionOrder = standardDirectionOrder.toBuilder()
                .sdoPermissionHearingDetails(hearingDataService.getHearingDataForSelectedHearingForSdo(
                    hearingData,
                    hearings,
                    caseData
                ))
                .build();
        }
        if (isNotEmpty(standardDirectionOrder.getSdoSecondHearingDetails())) {
            hearingData =  hearingDataService.getHearingDataForSdo(
                standardDirectionOrder.getSdoSecondHearingDetails(),
                hearingDataPrePopulatedDynamicLists,
                caseData
            );
            standardDirectionOrder = standardDirectionOrder.toBuilder()
                .sdoSecondHearingDetails(hearingDataService.getHearingDataForSelectedHearingForSdo(
                    hearingData,
                    hearings,
                    caseData
                ))
                .build();
        }
        if (isNotEmpty(standardDirectionOrder.getSdoFhdraHearingDetails())) {
            hearingData =  hearingDataService.getHearingDataForSdo(
                standardDirectionOrder.getSdoFhdraHearingDetails(),
                hearingDataPrePopulatedDynamicLists,
                caseData
            );
            standardDirectionOrder = standardDirectionOrder.toBuilder()
                .sdoFhdraHearingDetails(hearingDataService.getHearingDataForSelectedHearingForSdo(
                    hearingData,
                    hearings,
                    caseData
                ))
                .build();
        }
        if (isNotEmpty(standardDirectionOrder.getSdoDraHearingDetails())) {
            hearingData =  hearingDataService.getHearingDataForSdo(
                standardDirectionOrder.getSdoDraHearingDetails(),
                hearingDataPrePopulatedDynamicLists,
                caseData
            );
            standardDirectionOrder = standardDirectionOrder.toBuilder()
                .sdoDraHearingDetails(hearingDataService.getHearingDataForSelectedHearingForSdo(
                    hearingData,
                    hearings,
                    caseData
                ))
                .build();
        }
        if (isNotEmpty(standardDirectionOrder.getSdoSettlementHearingDetails())) {
            hearingData =  hearingDataService.getHearingDataForSdo(
                standardDirectionOrder.getSdoSettlementHearingDetails(),
                hearingDataPrePopulatedDynamicLists,
                caseData
            );
            standardDirectionOrder = standardDirectionOrder.toBuilder()
                .sdoSettlementHearingDetails(hearingDataService.getHearingDataForSelectedHearingForSdo(
                    hearingData,
                    hearings,
                    caseData
                ))
                .build();
        }
        if (isNotEmpty(standardDirectionOrder.getSdoDirectionsForFactFindingHearingDetails())
            && isNotEmpty(standardDirectionOrder.getSdoDirectionsForFactFindingHearingDetails()
                              .getHearingDateConfirmOptionEnum())) {
            hearingData = hearingDataService.getHearingDataForSdo(
                standardDirectionOrder.getSdoDirectionsForFactFindingHearingDetails(),
                hearingDataPrePopulatedDynamicLists,
                caseData
            );
            standardDirectionOrder = standardDirectionOrder.toBuilder()
                .sdoDirectionsForFactFindingHearingDetails(hearingDataService.getHearingDataForSelectedHearingForSdo(
                    hearingData,
                    hearings,
                    caseData
                ))
                .build();
        }
        caseData = caseData.toBuilder().standardDirectionOrder(standardDirectionOrder).build();
        return caseData;
    }

    private CaseData updateExistingHearingData(String authorisation,
                                               CaseData caseData,
                                               Map<String, Object> caseDataUpdated) {
        String caseReferenceNumber = String.valueOf(caseData.getId());
        Hearings hearings = hearingService.getHearings(authorisation, caseReferenceNumber);
        if (isNotEmpty(caseData.getManageOrders().getOrdersHearingDetails())) {
            HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
                hearingDataService.populateHearingDynamicLists(authorisation, caseReferenceNumber, caseData, hearings);

            caseDataUpdated.put(
                ORDER_HEARING_DETAILS,
                hearingDataService.getHearingDataForOtherOrders(caseData.getManageOrders().getOrdersHearingDetails(),
                                                                hearingDataPrePopulatedDynamicLists, caseData
                )
            );
            caseData.getManageOrders()
                .setOrdersHearingDetails(hearingDataService.getHearingDataForSelectedHearing(caseData, hearings, authorisation));
        } else if (CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(caseData.getCreateSelectOrderOptions())) {
            caseData = setHearingDataForSdo(caseData, hearings, authorisation);
        }
        return caseData;
    }

    /**
     * Save additional documents uploaded during serve order.
     */
    public void saveAdditionalOrderDocuments(String authorization,
                                             CaseData caseData,
                                             Map<String, Object> caseDataUpdated) {
        if (null != caseData.getManageOrders()
            && isNotEmpty(caseData.getManageOrders().getServeOrderAdditionalDocuments())) {
            UserDetails userDetails = userService.getUserDetails(authorization);
            List<Element<AdditionalOrderDocument>> additionalOrderDocuments = null != caseData.getManageOrders().getAdditionalOrderDocuments()
                ? caseData.getManageOrders().getAdditionalOrderDocuments() : new ArrayList<>();
            additionalOrderDocuments.add(
                element(AdditionalOrderDocument.builder()
                            .uploadedBy(userDetails.getFullName())
                            .uploadedDateTime(dateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN, Locale.ENGLISH))
                                                  .replace(AM_LOWER_CASE, AM_UPPER_CASE)
                                                  .replace(PM_LOWER_CASE, PM_UPPER_CASE))
                            .additionalDocuments(caseData.getManageOrders().getServeOrderAdditionalDocuments()
                                                     .stream()
                                                     .map(Element::getValue)
                                                     .map(ElementUtils::element)
                                                     .toList())
                            .servedOrders(null != caseData.getManageOrders().getServeOrderDynamicList()
                                              ? getDynamicMultiSelectedValueLabels(
                                caseData.getManageOrders().getServeOrderDynamicList().getValue())
                                              : null)
                            .build()
                )
            );

            //update in case data
            caseDataUpdated.put("additionalOrderDocuments", additionalOrderDocuments);
        }

    }


    public void setIsHearingTaskNeeded(List<Element<HearingData>> ordersHearingDetails,
                                       Map<String,Object> caseDataUpdated,
                                       String isOrderApproved,
                                       AmendOrderCheckEnum amendOrderCheck,
                                       String eventId) {

        String isHearingTaskNeeded = NO;

        // If rejected by judge, then isHearingTaskNeeded should be always 'No'.
        if (null != isOrderApproved && isOrderApproved.equals(NO)) {
            caseDataUpdated.put(WA_IS_HEARING_TASK_NEEDED, isHearingTaskNeeded);
            return;
        }

        // If judge or la or manager approval is required (for managerOrders journey only) , then isHearingTaskNeeded should be always 'No'.
        if (eventId.equals(MANAGE_ORDERS.getId())
            && (AmendOrderCheckEnum.judgeOrLegalAdvisorCheck.equals(amendOrderCheck)
            || AmendOrderCheckEnum.managerCheck.equals(amendOrderCheck))) {
            caseDataUpdated.put(WA_IS_HEARING_TASK_NEEDED, isHearingTaskNeeded);
            return;
        }

        //In case if no hearings at all, then default value for isHearingTaskNeeded should be null
        if (CollectionUtils.isEmpty(ordersHearingDetails)) {
            isHearingTaskNeeded = null;
        } else if (CollectionUtils.isNotEmpty(ordersHearingDetails)) {
            List<HearingData> hearingList = ordersHearingDetails.stream()
                .map(Element::getValue).toList();
            for (HearingData hearing : hearingList) {
                if (hearing.getHearingDateConfirmOptionEnum() != null
                    && (HearingDateConfirmOptionEnum.dateReservedWithListAssit.equals(hearing.getHearingDateConfirmOptionEnum())
                    || HearingDateConfirmOptionEnum.dateToBeFixed.equals(hearing.getHearingDateConfirmOptionEnum())
                    || HearingDateConfirmOptionEnum.dateConfirmedByListingTeam.equals(hearing.getHearingDateConfirmOptionEnum()))) {
                    isHearingTaskNeeded = YES;
                    break;
                }
            }
        }
        caseDataUpdated.put(WA_IS_HEARING_TASK_NEEDED, isHearingTaskNeeded);
    }

    public void setHearingSelectedInfoForTask(List<Element<HearingData>> ordersHearingDetails, Map<String,Object> caseDataUpdated) {
        String isMultipleHearingSelected = null;
        String hearingOptionSelected = null;

        if (CollectionUtils.isNotEmpty(ordersHearingDetails)) {
            List<HearingData> hearingList = ordersHearingDetails.stream()
                .map(Element::getValue).toList();

            List<Element<HearingData>> hearingsWithOptionsSelected = ordersHearingDetails.stream()
                .filter(elem -> null != elem.getValue().getHearingDateConfirmOptionEnum()).toList();

            if (hearingsWithOptionsSelected.size() == 1) {
                hearingOptionSelected =  hearingList.get(0).getHearingDateConfirmOptionEnum().toString();
                isMultipleHearingSelected = NO;
            } else if (hearingsWithOptionsSelected.size() > 1) {
                hearingOptionSelected = WA_MULTIPLE_OPTIONS_SELECTED_VALUE;
                isMultipleHearingSelected = YES;
            }
        }
        caseDataUpdated.put(WA_IS_MULTIPLE_HEARING_SELECTED, isMultipleHearingSelected);
        caseDataUpdated.put(WA_HEARING_OPTION_SELECTED, hearingOptionSelected);
    }

    public void setHearingOptionDetailsForTask(CaseData caseData, Map<String, Object> caseDataUpdated, String eventId, String performingUser) {

        AmendOrderCheckEnum amendOrderCheckEnum = caseData.getManageOrders().getAmendOrderSelectCheckOptions();
        String judgeLaManagerReviewRequired = null;
        if (null != amendOrderCheckEnum) {
            judgeLaManagerReviewRequired = amendOrderCheckEnum.toString();
        }
        caseDataUpdated.put(WA_JUDGE_LA_MANAGER_REVIEW_REQUIRED, judgeLaManagerReviewRequired);

        if (eventId.equals(MANAGE_ORDERS.getId())) {
            boolean isSdoOrder = CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(caseData.getCreateSelectOrderOptions());
            if (isSdoOrder) {
                List<Element<HearingData>> sdoHearings = buildSdoHearingsListFromStandardDirectionOrder(caseData.getStandardDirectionOrder());
                setHearingSelectedInfoForTask(sdoHearings, caseDataUpdated);
                setIsHearingTaskNeeded(sdoHearings,caseDataUpdated,null,amendOrderCheckEnum,eventId);
            } else {
                setHearingSelectedInfoForTask(caseData.getManageOrders().getOrdersHearingDetails(), caseDataUpdated);
                setIsHearingTaskNeeded(caseData.getManageOrders().getOrdersHearingDetails(),caseDataUpdated,null,amendOrderCheckEnum,eventId);
            }
            caseDataUpdated.put(WA_IS_ORDER_APPROVED, null);
            caseDataUpdated.put(WA_WHO_APPROVED_THE_ORDER, null);
        } else if (eventId.equals(Event.EDIT_AND_APPROVE_ORDER.getId())) {
            boolean isSdoOrder = false;
            Object dynamicList = caseData.getDraftOrdersDynamicList();
            DraftOrder selectedDraftOrder = getSelectedDraftOrderDetails(caseData.getDraftOrderCollection(),
                                                                         dynamicList);
            List<Element<HearingData>> sdoHearings = new ArrayList<>();
            if (selectedDraftOrder != null && (standardDirectionsOrder.equals(selectedDraftOrder.getOrderType()))) {
                isSdoOrder = true;
                sdoHearings = buildSdoHearingsListFromSdoDetails(selectedDraftOrder.getSdoDetails());
            }

            if (ManageOrdersUtils.isOrderEdited(caseData, eventId)) {
                setHearingSelectedInfoForTask(isSdoOrder ? sdoHearings : caseData.getManageOrders().getOrdersHearingDetails(), caseDataUpdated);
                String isOrderApproved = isOrderApproved(caseData, caseDataUpdated, performingUser);
                setIsHearingTaskNeeded(isSdoOrder ? sdoHearings : caseData.getManageOrders().getOrdersHearingDetails(),
                                       caseDataUpdated,isOrderApproved,amendOrderCheckEnum,eventId);
            } else {
                UUID selectedOrderId = elementUtils.getDynamicListSelectedValue(
                    caseData.getDraftOrdersDynamicList(), objectMapper);

                if (null != caseData.getDraftOrderCollection()) {
                    for (Element<DraftOrder> e : caseData.getDraftOrderCollection()) {
                        DraftOrder draftOrder = e.getValue();
                        if (e.getId().equals(selectedOrderId)) {
                            setHearingSelectedInfoForTask(isSdoOrder ? sdoHearings : draftOrder.getManageOrderHearingDetails(), caseDataUpdated);
                            String isOrderApproved = isOrderApproved(caseData, caseDataUpdated, performingUser);
                            setIsHearingTaskNeeded(isSdoOrder ? sdoHearings : draftOrder.getManageOrderHearingDetails(),
                                                   caseDataUpdated,isOrderApproved,amendOrderCheckEnum,eventId);
                        }
                    }
                }
            }
        }
    }


    private List<Element<HearingData>> buildSdoHearingsListFromStandardDirectionOrder(StandardDirectionOrder sdo) {

        List<Element<HearingData>> sdoHearingsList = new ArrayList<>();
        if (null != sdo) {
            if (null != sdo.getSdoSecondHearingDetails()) {
                sdoHearingsList.add(element(sdo.getSdoSecondHearingDetails()));
            }

            if (null != sdo.getSdoUrgentHearingDetails()) {
                sdoHearingsList.add(element(sdo.getSdoUrgentHearingDetails()));
            }

            if (null != sdo.getSdoFhdraHearingDetails()) {
                sdoHearingsList.add(element(sdo.getSdoFhdraHearingDetails()));
            }

            if (null != sdo.getSdoPermissionHearingDetails()) {
                sdoHearingsList.add(element(sdo.getSdoPermissionHearingDetails()));
            }

            if (null != sdo.getSdoDraHearingDetails()) {
                sdoHearingsList.add(element(sdo.getSdoDraHearingDetails()));
            }

            if (null != sdo.getSdoSettlementHearingDetails()) {
                sdoHearingsList.add(element(sdo.getSdoSettlementHearingDetails()));
            }

            if (null != sdo.getSdoDirectionsForFactFindingHearingDetails()) {
                sdoHearingsList.add(element(sdo.getSdoDirectionsForFactFindingHearingDetails()));
            }
        }

        return sdoHearingsList;
    }

    private List<Element<HearingData>> buildSdoHearingsListFromSdoDetails(SdoDetails sdoDetails) {

        List<Element<HearingData>> sdoHearingsList = new ArrayList<>();
        if (null != sdoDetails) {
            if (null != sdoDetails.getSdoSecondHearingDetails()) {
                sdoHearingsList.add(element(sdoDetails.getSdoSecondHearingDetails()));
            }

            if (null != sdoDetails.getSdoUrgentHearingDetails()) {
                sdoHearingsList.add(element(sdoDetails.getSdoUrgentHearingDetails()));
            }

            if (null != sdoDetails.getSdoFhdraHearingDetails()) {
                sdoHearingsList.add(element(sdoDetails.getSdoFhdraHearingDetails()));
            }

            if (null != sdoDetails.getSdoPermissionHearingDetails()) {
                sdoHearingsList.add(element(sdoDetails.getSdoPermissionHearingDetails()));
            }

            if (null != sdoDetails.getSdoDraHearingDetails()) {
                sdoHearingsList.add(element(sdoDetails.getSdoDraHearingDetails()));
            }

            if (null != sdoDetails.getSdoSettlementHearingDetails()) {
                sdoHearingsList.add(element(sdoDetails.getSdoSettlementHearingDetails()));
            }

            if (null != sdoDetails.getSdoDirectionsForFactFindingHearingDetails()) {
                sdoHearingsList.add(element(sdoDetails.getSdoDirectionsForFactFindingHearingDetails()));
            }
        }

        return sdoHearingsList;
    }

    public DraftOrder getSelectedDraftOrderDetails(List<Element<DraftOrder>> draftOrderCollection, Object dynamicList) {
        UUID orderId = elementUtils.getDynamicListSelectedValue(dynamicList, objectMapper);
        return draftOrderCollection.stream()
            .filter(element -> element.getId().equals(orderId))
            .map(Element::getValue)
            .findFirst()
            .orElseThrow(() -> new UnsupportedOperationException("Could not find order"));
    }

    public String isOrderApproved(CaseData caseData, Map<String, Object> caseDataUpdated, String performingUser) {
        String whoApprovedTheOrder = null;
        String isOrderApproved = NO;

        if (null != caseData.getManageOrders().getWhatToDoWithOrderCourtAdmin()
            || (null != caseData.getManageOrders().getWhatToDoWithOrderSolicitor()
            && !OrderApprovalDecisionsForSolicitorOrderEnum.askLegalRepToMakeChanges.toString()
            .equalsIgnoreCase(caseData.getManageOrders().getWhatToDoWithOrderSolicitor().toString()))) {
            whoApprovedTheOrder = performingUser;
            isOrderApproved = YES;
        }

        caseDataUpdated.put(WA_IS_ORDER_APPROVED, isOrderApproved);
        caseDataUpdated.put(WA_WHO_APPROVED_THE_ORDER, whoApprovedTheOrder);

        return isOrderApproved;
    }

    public CaseData updateOrderFieldsForDocmosis(DraftOrder draftOrder,CaseData caseData) {
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            caseData = caseData.toBuilder()
                .judgeOrMagistratesLastName(draftOrder.getJudgeOrMagistratesLastName())
                .justiceLegalAdviserFullName(draftOrder.getJusticeLegalAdviserFullName())
                .magistrateLastName(draftOrder.getMagistrateLastName())
                .dateOrderMade(draftOrder.getDateOrderMade() != null ? draftOrder.getDateOrderMade() : null)
                .build();

        }
        return  caseData;

    }

    public List<Element<HearingData>> getHearingDataFromExistingHearingData(String authorisation,
                                                                            List<Element<HearingData>> existingOrderHearingDetails,
                                                                            CaseData caseData) {
        String caseReferenceNumber = String.valueOf(caseData.getId());
        if (CollectionUtils.isNotEmpty(existingOrderHearingDetails)) {
            Hearings hearings = hearingService.getHearings(authorisation, caseReferenceNumber);
            HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
                hearingDataService.populateHearingDynamicLists(authorisation, caseReferenceNumber, caseData, hearings);

            return hearingDataService.getHearingDataForOtherOrders(
                existingOrderHearingDetails,
                hearingDataPrePopulatedDynamicLists,
                caseData
            );
        }
        return Collections.emptyList();
    }

    public Map<String, Object> handleFetchOrderDetails(String authorisation,
                                                       CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        caseDataUpdated.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        //Added for SDO Orders
        caseData = updateIsSdoSelected(caseData);

        caseDataUpdated.put("isSdoSelected", caseData.getIsSdoSelected());
        //C21 order related
        addC21OrderDetails(caseData, caseDataUpdated);
        //update courtName
        updateCourtName(callbackRequest, caseDataUpdated);
        //common fields
        updateOrderDetails(authorisation, caseData, caseDataUpdated);

        //PRL-4212 - populate hearing details only orders where it's needed
        populateHearingData(authorisation, caseData, caseDataUpdated);

        return caseDataUpdated;
    }

    private void populateHearingData(String authorisation,
                                     CaseData caseData,
                                     Map<String, Object> caseDataUpdated) {
        //Set only in case order needs hearing details
        if (isHearingPageNeeded(caseData.getCreateSelectOrderOptions(), caseData.getManageOrders().getC21OrderOptions())) {
            HearingData hearingData = getHearingData(authorisation, caseData);
            caseDataUpdated.put(ORDER_HEARING_DETAILS, ElementUtils.wrapElements(hearingData));
            //add hearing screen field show params
            ManageOrdersUtils.addHearingScreenFieldShowParams(hearingData, caseDataUpdated, caseData);
        }

        //For DIO
        if (CreateSelectOrderOptionsEnum.directionOnIssue.equals(caseData.getCreateSelectOrderOptions())) {
            HearingData hearingData = getHearingData(authorisation, caseData);
            //add hearing screen field show params
            ManageOrdersUtils.addHearingScreenFieldShowParams(hearingData, caseDataUpdated, caseData);

            //check with Shashi if these needed individually?
            caseDataUpdated.put(DIO_CASEREVIEW_HEARING_DETAILS, hearingData);
            caseDataUpdated.put(DIO_PERMISSION_HEARING_DETAILS, hearingData);
            caseDataUpdated.put(DIO_URGENT_HEARING_DETAILS, hearingData);
            caseDataUpdated.put(DIO_URGENT_FIRST_HEARING_DETAILS, hearingData);
            caseDataUpdated.put(DIO_FHDRA_HEARING_DETAILS, hearingData);
            caseDataUpdated.put(DIO_WITHOUT_NOTICE_HEARING_DETAILS, hearingData);
        }
    }

    public HearingData getHearingData(String authorization,
                                      CaseData caseData) {
        String caseReferenceNumber = String.valueOf(caseData.getId());
        log.info("Inside Prepopulate getHearingData for the case id {}", caseReferenceNumber);
        Hearings hearings = hearingService.getHearings(authorization, caseReferenceNumber);
        log.info("Fetched Hearings {}", hearings);
        HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
            hearingDataService.populateHearingDynamicLists(authorization, caseReferenceNumber, caseData, hearings);

        return hearingDataService.generateHearingData(hearingDataPrePopulatedDynamicLists, caseData);
    }

    private void addC21OrderDetails(CaseData caseData,
                                    Map<String, Object> caseDataUpdated) {
        caseDataUpdated.put("selectedC21Order", (null != caseData.getManageOrders()
            && caseData.getManageOrdersOptions() == ManageOrdersOptionsEnum.createAnOrder)
            ? BOLD_BEGIN + caseData.getCreateSelectOrderOptions().getDisplayedValue() + BOLD_END : " ");

        C21OrderOptionsEnum c21OrderType = (null != caseData.getManageOrders())
            ? caseData.getManageOrders().getC21OrderOptions() : null;
        caseDataUpdated.put("c21OrderOptions", c21OrderType);
        caseDataUpdated.put("typeOfC21Order", c21OrderType != null ? BOLD_BEGIN + c21OrderType.getDisplayedValue() + BOLD_END : "");
    }

    private void updateCourtName(CallbackRequest callbackRequest,
                                 Map<String, Object> caseDataUpdated) {
        if (callbackRequest.getCaseDetailsBefore() != null
            && callbackRequest.getCaseDetailsBefore().getData().get(COURT_NAME) != null) {
            caseDataUpdated.put("courtName", callbackRequest
                .getCaseDetailsBefore().getData().get(COURT_NAME).toString());
        }
    }

    private void updateOrderDetails(String authorisation,
                                    CaseData caseData,
                                    Map<String, Object> caseDataUpdated) {

        caseDataUpdated.putAll(getUpdatedCaseData(caseData));

        //children dynamic multi select list
        caseDataUpdated.put(CHILD_OPTION, DynamicMultiSelectList.builder()
            .listItems(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).build());
        caseDataUpdated.put(DA_ORDER_FOR_CA_CASE,
                            ManageOrdersUtils.isDaOrderSelectedForCaCase(String.valueOf(caseData.getCreateSelectOrderOptions()),
                                                                         caseData) ? Yes : No);
        caseDataUpdated.put("loggedInUserType", getLoggedInUserType(authorisation));

        //PRL-3254 - Populate hearing details dropdown for create order
        caseDataUpdated.put(HEARINGS_TYPE, populateHearingsDropdown(authorisation, caseData));
        caseDataUpdated.put("dateOrderMade", LocalDate.now());
        caseDataUpdated.put("magistrateLastName", isNotEmpty(caseData.getMagistrateLastName())
            ? caseData.getMagistrateLastName() : Arrays.asList(element(MagistrateLastName.builder().build())));
    }

    public Document getGeneratedDocument(GeneratedDocumentInfo generatedDocumentInfo,
                                         boolean isWelsh, Map<String, String> fieldMap) {
        if (generatedDocumentInfo != null) {
            return Document.builder().documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentFileName(!isWelsh ? fieldMap.get(PrlAppsConstants.GENERATE_FILE_NAME)
                                      : fieldMap.get(PrlAppsConstants.WELSH_FILE_NAME)).build();
        }
        return null;
    }

    public static void cleanUpServeOrderOptions(Map<String, Object> caseDataUpdated) {
        for (ServeOrderFieldsEnum field : ServeOrderFieldsEnum.values()) {
            caseDataUpdated.put(field.getValue(), null);
        }
    }

    public Map<String, Object> setFieldsForWaTask(String authorisation, CaseData caseData, String eventId) {
        String judgeLaReviewRequired = null;
        String performingUser = null;
        String performingAction = null;
        String orderNameForWA = null;
        Map<String, Object> waFieldsMap = new HashMap<>();
        if (ManageOrdersOptionsEnum.createAnOrder.equals(caseData.getManageOrdersOptions())
            || ManageOrdersOptionsEnum.uploadAnOrder.equals(caseData.getManageOrdersOptions())) {
            if (ManageOrdersOptionsEnum.createAnOrder.equals(caseData.getManageOrdersOptions())) {
                orderNameForWA = ManageOrdersUtils.getOrderNameAlongWithTime(
                    caseData.getCreateSelectOrderOptions() != null
                        ? caseData.getCreateSelectOrderOptions().getDisplayedValue() : " ",
                    dateTime.now()
                );
            } else if (ManageOrdersOptionsEnum.uploadAnOrder.equals(caseData.getManageOrdersOptions())) {
                orderNameForWA = ManageOrdersUtils.getOrderNameAlongWithTime(
                    getSelectedOrderInfoForUpload(caseData),
                    dateTime.now()
                );
            }
            performingUser = getLoggedInUserType(authorisation);
            performingAction = caseData.getManageOrdersOptions().getDisplayedValue();

            if (ManageOrdersOptionsEnum.createAnOrder.equals(caseData.getManageOrdersOptions())) {
                setHearingOptionDetailsForTask(caseData, waFieldsMap, eventId, performingUser);
            }

            if (null != performingUser && performingUser.equalsIgnoreCase(UserRoles.COURT_ADMIN.toString())) {
                judgeLaReviewRequired = AmendOrderCheckEnum.judgeOrLegalAdvisorCheck
                    .equals(caseData.getManageOrders().getAmendOrderSelectCheckOptions()) ? "Yes" : "No";
                waFieldsMap.put(WA_ORDER_NAME_ADMIN_CREATED, orderNameForWA);
            } else if (null != performingUser && performingUser.equalsIgnoreCase(UserRoles.JUDGE.toString())) {
                waFieldsMap.put(WA_ORDER_NAME_JUDGE_CREATED, orderNameForWA);
            }
        }
        waFieldsMap.put(WA_PERFORMING_USER, performingUser);
        waFieldsMap.put(WA_PERFORMING_ACTION, performingAction);
        waFieldsMap.put(WA_JUDGE_LA_REVIEW_REQUIRED, judgeLaReviewRequired);
        return waFieldsMap;
    }


    private CaseData updateIsSdoSelected(CaseData caseData) {
        log.info("**ManageOrdersOptions" + caseData.getManageOrdersOptions());
        log.info("**CreateSelectOrderOptions" + caseData.getCreateSelectOrderOptions());
        if (null != caseData.getManageOrdersOptions()
            && caseData.getManageOrdersOptions().equals(createAnOrder)
            && null != caseData.getCreateSelectOrderOptions()) {
            if ((standardDirectionsOrder).equals(caseData.getCreateSelectOrderOptions())
                || (other).equals(caseData.getCreateSelectOrderOptions())) {
                caseData.setIsSdoSelected(Yes);
                log.info("isSdoSelected set to Yes" + caseData.getIsSdoSelected());
            } else {
                caseData.setIsSdoSelected(No);
                log.info("isSdoSelected set to No" + caseData.getIsSdoSelected());
            }
        }
        return  caseData;

    }

    public String getAdditionalRequirementsForHearingReq(List<Element<HearingData>> ordersHearingDetails,
                                                         boolean isDraftOrder,
                                                         StandardDirectionOrder standardDirectionOrder,
                                                         CreateSelectOrderOptionsEnum orderType,
                                                         C21OrderOptionsEnum c21OrderOptions) {
        log.info("inside getAdditionalRequirementsForHearingReq");
        List<String> additionalRequirementsForHearingReqList = new ArrayList<>();
        if (isHearingPageNeeded(orderType, c21OrderOptions) && CollectionUtils.isNotEmpty(ordersHearingDetails)) {
            getAdditionalRequirementsForHearingReqForOtherOrders(
                ordersHearingDetails,
                isDraftOrder,
                additionalRequirementsForHearingReqList
            );
        } else if (CreateSelectOrderOptionsEnum.standardDirectionsOrder.equals(orderType) && ObjectUtils.isNotEmpty(standardDirectionOrder)) {
            getAdditionalRequirementsForHearingReqForSdo(
                standardDirectionOrder,
                isDraftOrder,
                additionalRequirementsForHearingReqList
            );
        }
        log.info("additionalRequirementsForHearingReqList " + additionalRequirementsForHearingReqList);
        if (CollectionUtils.isNotEmpty(additionalRequirementsForHearingReqList)) {
            return String.join(", ", additionalRequirementsForHearingReqList);
        } else {
            return null;
        }
    }

    private void getAdditionalRequirementsForHearingReqForOtherOrders(List<Element<HearingData>> ordersHearingDetails,
                                                                             boolean isDraftOrder,
                                                                             List<String> additionalRequirementsForHearingReqList) {
        ordersHearingDetails.stream()
            .map(Element::getValue)
            .forEach(hearingData -> populateAdditionalRequirementsForHearingReqList(isDraftOrder,
                                                                                    additionalRequirementsForHearingReqList,
                                                                                    hearingData));
    }

    private void populateAdditionalRequirementsForHearingReqList(boolean isDraftOrder,
                                                                        List<String> additionalRequirementsForHearingReqList,
                                                                        HearingData hearingData) {
        boolean isOption3Selected = ObjectUtils.isNotEmpty(hearingData.getHearingDateConfirmOptionEnum())
            && HearingDateConfirmOptionEnum.dateConfirmedByListingTeam
            .equals(hearingData.getHearingDateConfirmOptionEnum());
        boolean isOption4Selected = ObjectUtils.isNotEmpty(hearingData.getHearingDateConfirmOptionEnum())
            && HearingDateConfirmOptionEnum.dateToBeFixed
            .equals(hearingData.getHearingDateConfirmOptionEnum());
        if (((isDraftOrder && (isOption3Selected || isOption4Selected))
            || (!isDraftOrder && isOption4Selected))
            && ObjectUtils.isNotEmpty(hearingData.getAdditionalDetailsForHearingDateOptions())) {
            additionalRequirementsForHearingReqList.add(hearingData.getAdditionalDetailsForHearingDateOptions());
        }
    }

    private void getAdditionalRequirementsForHearingReqForSdo(StandardDirectionOrder standardDirectionOrder,
                                                                     boolean isDraftOrder,
                                                                     List<String> additionalRequirementsForHearingReqList) {

        if (ObjectUtils.isNotEmpty(standardDirectionOrder.getSdoUrgentHearingDetails())) {
            populateAdditionalRequirementsForHearingReqList(
                isDraftOrder,
                additionalRequirementsForHearingReqList,
                standardDirectionOrder.getSdoUrgentHearingDetails()
            );
        }
        if (ObjectUtils.isNotEmpty(standardDirectionOrder.getSdoPermissionHearingDetails())) {
            populateAdditionalRequirementsForHearingReqList(
                isDraftOrder,
                additionalRequirementsForHearingReqList,
                standardDirectionOrder.getSdoPermissionHearingDetails()
            );
        }
        if (ObjectUtils.isNotEmpty(standardDirectionOrder.getSdoSecondHearingDetails())) {
            populateAdditionalRequirementsForHearingReqList(
                isDraftOrder,
                additionalRequirementsForHearingReqList,
                standardDirectionOrder.getSdoSecondHearingDetails()
            );
        }
        if (ObjectUtils.isNotEmpty(standardDirectionOrder.getSdoFhdraHearingDetails())) {
            populateAdditionalRequirementsForHearingReqList(
                isDraftOrder,
                additionalRequirementsForHearingReqList,
                standardDirectionOrder.getSdoFhdraHearingDetails()
            );
        }
        if (ObjectUtils.isNotEmpty(standardDirectionOrder.getSdoDraHearingDetails())) {
            populateAdditionalRequirementsForHearingReqList(
                isDraftOrder,
                additionalRequirementsForHearingReqList,
                standardDirectionOrder.getSdoDraHearingDetails()
            );
        }
        if (ObjectUtils.isNotEmpty(standardDirectionOrder.getSdoSettlementHearingDetails())) {
            populateAdditionalRequirementsForHearingReqList(
                isDraftOrder,
                additionalRequirementsForHearingReqList,
                standardDirectionOrder.getSdoSettlementHearingDetails()
            );
        }
        if (ObjectUtils.isNotEmpty(standardDirectionOrder.getSdoDirectionsForFactFindingHearingDetails())
            && ObjectUtils.isNotEmpty(standardDirectionOrder.getSdoDirectionsForFactFindingHearingDetails()
                                          .getHearingDateConfirmOptionEnum())) {
            populateAdditionalRequirementsForHearingReqList(
                isDraftOrder,
                additionalRequirementsForHearingReqList,
                standardDirectionOrder.getSdoDirectionsForFactFindingHearingDetails()
            );
        }
    }

    public AboutToStartOrSubmitCallbackResponse validateRespondentLipAndOtherPersonAddress(CallbackRequest callbackRequest) {
        List<String> errorList = new ArrayList<>();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        if (null != caseData.getManageOrders().getRecipientsOptions()
            && No.equals(caseData.getManageOrders().getServeToRespondentOptions())) {
            List<String> selectedRespondentIds = caseData.getManageOrders().getRecipientsOptions().getValue()
                .stream().map(DynamicMultiselectListElement::getCode).toList();
            checkPartyAddressAndReturnError(caseData.getRespondents(), selectedRespondentIds, errorList, true);

        }
        List<Element<PartyDetails>> otherPeopleInCase = TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
            || TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())
            ? caseData.getOtherPartyInTheCaseRevised() : caseData.getOthersToNotify();

        if (null != caseData.getManageOrders().getOtherParties()) {
            List<String> selectedOtherPartyIds = caseData.getManageOrders().getOtherParties().getValue()
                .stream().map(DynamicMultiselectListElement::getCode).toList();
            checkPartyAddressAndReturnError(otherPeopleInCase, selectedOtherPartyIds, errorList, false);
        }

        if (isNotEmpty(errorList)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errorList)
                .build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(callbackRequest.getCaseDetails().getData())
            .build();

    }

    private void checkPartyAddressAndReturnError(List<Element<PartyDetails>> partyDetails,
                                                 List<String> selectedPartyIds, List<String> errorList,
                                                 Boolean isRespondent) {
        List<Element<PartyDetails>> selectedPartyList = partyDetails.stream()
            .filter(party -> selectedPartyIds.contains(party.getId().toString()))
            .toList();
        for (Element<PartyDetails> party : selectedPartyList) {
            if ((isRespondent
                && YesNoDontKnow.no.equals(party.getValue().getDoTheyHaveLegalRepresentation()))
                && checkForContactPreference(party) && !checkIfAddressIsPresent(party.getValue().getAddress())) {
                errorList.add(VALIDATION_ADDRESS_ERROR_RESPONDENT);
            } else if (Boolean.FALSE.equals(isRespondent) && !(checkIfAddressIsPresent(party.getValue().getAddress()))) {
                errorList.add(VALIDATION_ADDRESS_ERROR_OTHER_PARTY);
            }
            if (!errorList.isEmpty()) {
                break;
            }
        }
    }

    private boolean checkForContactPreference(Element<PartyDetails> party) {
        return null == party.getValue().getContactPreferences()
            || party.getValue().getContactPreferences().equals(ContactPreferences.post)
            || null == party.getValue().getEmail();
    }

    private boolean checkIfAddressIsPresent(Address address) {
        return null != address
            && null != address.getAddressLine1();
    }
}
