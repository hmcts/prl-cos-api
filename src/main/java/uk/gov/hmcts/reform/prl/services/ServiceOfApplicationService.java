package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.config.templates.Templates;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaCitizenServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaSolicitorServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.ConfidentialCheckFailed;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.SoaPack;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WelshCourtEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.AccessCode;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.DocumentListForLa;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.pin.C100CaseInviteService;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.services.pin.FL401CaseInviteService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.EmailUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_AP7;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_AP8;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_C100_RE6;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_FL401_RE1;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_FL401_RE2;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_FL401_RE3;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_FL401_RE4;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_RE5;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BLANK_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C1A_BLANK_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C1A_BLANK_DOCUMENT_WELSH_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C7_BLANK_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C9_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_CREATED_BY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_CAN_VIEW_ONLINE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_DASHBOARD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DD_MMM_YYYY_HH_MM_SS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_COVER_SHEET_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EUROPE_LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.L;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.M;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MISSING_ADDRESS_WARNING_TEXT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.NO;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OTHER_PEOPLE_SELECTED_C6A_MISSING_ERROR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PRIVACY_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PRIVACY_DOCUMENT_FILENAME_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_APPLICATION_SCREEN_1;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_C6A_OTHER_PARTIES_ORDER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_C6A_OTHER_PARTIES_ORDER_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_C9_PERSONAL_SERVICE_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_CAFCASS_CYMRU_SERVED_OPTIONS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_CONFIDENTIAL_DETAILS_PRESENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_CYMRU_EMAIL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_DOCUMENT_PLACE_HOLDER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_FL415_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_NOTICE_SAFETY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_ORDER_LIST_EMPTY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_OTHER_PARTIES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_OTHER_PEOPLE_PRESENT_IN_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_RECIPIENT_OPTIONS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_SOLICITOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WARNING_TEXT_DIV;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YES;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.SOA_CA_PERSONAL_UNREPRESENTED_APPLICANT;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.SOA_CA_PERSONAL_UNREPRESENTED_APPLICANT_WITHOUT_C1A;
import static uk.gov.hmcts.reform.prl.services.SendAndReplyService.ARROW_SEPARATOR;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.hasDashboardAccess;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings({"java:S3776","java:S6204","java:S112","java:S4144", "java:S5665","java:S1172","java:S6541"})
public class ServiceOfApplicationService {
    public static final String UNSERVED_APPLICANT_PACK = "unServedApplicantPack";
    public static final String UNSERVED_RESPONDENT_PACK = "unServedRespondentPack";
    public static final String UNSERVED_OTHERS_PACK = "unServedOthersPack";
    public static final String UNSERVED_LA_PACK = "unServedLaPack";
    public static final String APPLICATION_SERVED_YES_NO = "applicationServedYesNo";
    public static final String REJECTION_REASON = "rejectionReason";
    public static final String FINAL_SERVED_APPLICATION_DETAILS_LIST = "finalServedApplicationDetailsList";
    public static final String CONFIDENTIAL_CHECK_FAILED = "confidentialCheckFailed";
    public static final String APPLICANTS = "applicants";
    public static final String CASE_INVITES = "caseInvites";

    public static final String POST = "post";
    public static final String COURT = "Court";
    public static final String PERSONAL_SERVICE_SERVED_BY_CA = "Court - court admin";
    public static final String PERSONAL_SERVICE_SERVED_BY_BAILIFF = "Court - court bailiff";
    public static final String DA_APPLICANT_NAME = "daApplicantName";
    public static final String PROCEED_TO_SERVING = "proceedToServing";
    public static final String ADDRESS_MISSED_FOR_RESPONDENT_AND_OTHER_PARTIES = WARNING_TEXT_DIV
        + "</span><strong class='govuk-warning-text__text'>There is no postal address for a respondent and "
        + "other people in the case</strong></div>";
    public static final String CA_ADDRESS_MISSED_FOR_RESPONDENT = WARNING_TEXT_DIV
        + "</span><strong class='govuk-warning-text__text'>There is no postal address for a respondent"
        + "</strong></div>";

    public static final String DA_ADDRESS_MISSED_FOR_RESPONDENT = WARNING_TEXT_DIV
        + "</span><strong class='govuk-warning-text__text'>There is no postal address for the respondent"
        + "</strong></div>";

    public static final String ADDRESS_MISSED_FOR_OTHER_PARTIES = WARNING_TEXT_DIV
        + "</span><strong class='govuk-warning-text__text'>There is no postal address for other people in the "
        + "case</strong></div>";

    public static final String PRL_COURT_ADMIN = "PRL Court admin";
    public static final String DASH_BOARD_LINK = "dashBoardLink";
    public static final String SOA_DOCUMENT_DYNAMIC_LIST_FOR_LA = "soaDocumentDynamicListForLa";
    public static final String UNSERVED_CAFCASS_CYMRU_PACK = "unServedCafcassCymruPack";
    public static final String UNREPRESENTED_APPLICANT = "Unrepresented applicant";
    public static final String ENG = "eng";
    public static final String WEL = "wel";
    public static final String IS_WELSH = "isWelsh";
    public static final String IS_ENGLISH = "isEnglish";
    public static final String AUTHORIZATION = "authorization";
    public static final String COVER_LETTER_TEMPLATE = "coverLetterTemplate";

    @Value("${xui.url}")
    private String manageCaseUrl;

    public static final String RETURNED_TO_ADMIN_HEADER = "# The application cannot be served";
    public static final String CONFIDENTIAL_CONFIRMATION_NO_BODY_PREFIX = """
        ### What happens next
        A new service pack will need to be created by admin as this version will be deleted.""";

    public static final String CONFIDENTIAL_CONFIRMATION_HEADER = "# The application will be reviewed for confidential details";
    public static final String CONFIDENTIAL_CONFIRMATION_BODY_PREFIX = """
        ### What happens next
        The service pack needs to be reviewed for confidential details before it can be served.

        You can view the service packs in the <a href="%s">service of application</a> tab.
        """;

    public static final String CONFIRMATION_HEADER_NON_PERSONAL = "# The application has been served";
    public static final String CONFIRMATION_HEADER_PERSONAL = "# The application is ready to be personally served";
    public static final String CONFIRMATION_BODY_PREFIX = """
        ### What happens next
        The service pack has been served on the parties selected.

        You can view the service packs in the <a href="%s">service of application</a> tab.
        """;
    public static final String CONFIRMATION_BODY_APPLICANT_LR_SERVICE_PREFIX_CA = """
        ### What happens next
        The respondent's service pack has been sent to the applicant or their legal representative to personally serve the respondent.
        \n The applicant and any other selected parties have been served.

        You can view the service packs in the <a href="%s">service of application</a> tab.
        """;
    public static final String CONFIRMATION_BODY_COURT_ADMIN_SERVICE_PREFIX_CA = """
        ### What happens next
        You need to arrange service on the respondent based on the judge's directions.
        \n The service pack has been served on the applicant and any other selected parties.

        You can view the service packs in the <a href="%s">service of application</a> tab.
        """;
    public static final String CONFIRMATION_BODY_BAILIFF_SERVICE_PREFIX_CA = """
        ### What happens next
        You need to arrange for a court bailiff to personally serve the respondent.
        \n The service pack has been served on the applicant and any other selected parties.

        You can view the service packs in the <a href="%s">service of application</a> tab.
        """;

    public static final String CONFIRMATION_BODY_APPLICANT_LR_SERVICE_PREFIX_DA = """
        ### What happens next
        The respondent's service pack has been sent to the applicant or their legal representative to personally serve the respondent.
        \n The applicant has been served.

        You can view the service packs in the <a href="%s">service of application</a> tab.
        """;
    public static final String CONFIRMATION_BODY_COURT_ADMIN_SERVICE_PREFIX_DA = """
        ### What happens next
        You need to arrange service on the respondent based on the judge's directions.
        \n The service pack has been served on the applicant.

        You can view the service packs in the <a href="%s">service of application</a> tab.
        """;
    public static final String CONFIRMATION_BODY_BAILIFF_SERVICE_PREFIX_DA = """
        ### What happens next
        You need to arrange for a court bailiff to personally serve the respondent.
        \n The service pack has been served on the applicant.

        You can view the service packs in the <a href="%s">service of application</a> tab.
        """;

    public static final String CONFIDENTIALITY_CONFIRMATION_HEADER_PERSONAL = "# The application is ready for personal service";

    public static final String CONFIDENTIALITY_CONFIRMATION_BODY_PERSONAL = """
        ### What happens next
        The person arranging personal service will be notified
        """;

    private static final String SERVICE_OF_APPLICATION_ENDPOINT = PrlAppsConstants.URL_STRING + "#Service of application";

    private final ServiceOfApplicationEmailService serviceOfApplicationEmailService;
    private final ServiceOfApplicationPostService serviceOfApplicationPostService;
    private final C100CaseInviteService c100CaseInviteService;
    private final CaseSummaryTabService caseSummaryTabService;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final FL401CaseInviteService fl401CaseInviteService;
    private final DynamicMultiSelectListService dynamicMultiSelectListService;
    private final WelshCourtEmail welshCourtEmail;
    private final SendAndReplyService sendAndReplyService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AllTabServiceImpl allTabService;
    private final DocumentLanguageService documentLanguageService;
    private final DgsService dgsService;
    private final CaseInviteManager caseInviteManager;

    @Value("${citizen.url}")
    private String citizenUrl;

    public String getCollapsableOfSentDocuments() {
        final List<String> collapsible = new ArrayList<>();
        collapsible.add("<details class='govuk-details'>");
        collapsible.add("<summary class='govuk-details__summary'>");
        collapsible.add("<h3 class='govuk-details__summary-text'>");
        collapsible.add("Documents served in the pack");
        collapsible.add("</h3>");
        collapsible.add("</summary>");
        collapsible.add("<div class='govuk-details__text'>");
        collapsible.add(
            "Certain documents will be automatically included in the pack this is served on parties(the people in the case)");
        collapsible.add(
            "This includes");
        collapsible.add(
            "<ul><li>C100</li><li>C1A</li><li>C7</li><li>C1A (if applicable)</li><li>C8 (Cafcass/Cafcass Cymru, if applicable)</li>");
        collapsible.add("<li>Any orders and"
                            + " hearing notices created at the initial gatekeeping stage</li></ul>");
        collapsible.add(
            "You do not need to upload these documents yourself");
        collapsible.add("</div>");
        collapsible.add("</details>");
        return String.join("\n\n", collapsible);
    }

    public Map<String, Object> getOrderSelectionsEnumValues(List<String> orderList, Map<String, Object> caseData) {
        for (String s : orderList) {
            caseData.put(CreateSelectOrderOptionsEnum.mapOptionFromDisplayedValue(s), "1");
        }
        return caseData;
    }


    public List<Element<BulkPrintDetails>> sendPostToOtherPeopleInCase(CaseData caseData, String authorization,
                                                                       List<DynamicMultiselectListElement> selectedOthers,
                                                                       List<Document> packN, String servedParty) {
        List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
        List<Element<PartyDetails>> otherPeopleInCase = TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
                                                        || TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())
                                                            ? caseData.getOtherPartyInTheCaseRevised() : caseData.getOthersToNotify();
        selectedOthers.forEach(other -> {
            Optional<Element<PartyDetails>> party = getParty(other.getCode(), otherPeopleInCase);
            try {
                log.info(
                    "Sending the post notification to others in case for C100 Application for caseId {}",
                    caseData.getId()
                );

                if (party.isPresent() && null != party.get().getValue().getAddress()
                    && null != party.get().getValue().getAddress().getAddressLine1()) {
                    List<Document> docs = new ArrayList<>(serviceOfApplicationPostService
                                                              .getCoverSheets(caseData, authorization,
                                                                              party.get().getValue().getAddress(),
                                                                              party.get().getValue().getLabelForDynamicList(),
                                                                              DOCUMENT_COVER_SHEET_HINT
                                                              ));
                    docs.addAll(packN);
                    bulkPrintDetails.add(element(serviceOfApplicationPostService.sendPostNotificationToParty(
                        caseData,
                        authorization,
                        party.get(),
                        docs,
                        servedParty
                    )));
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return bulkPrintDetails;
    }

    public List<Element<EmailNotificationDetails>> sendEmailToCafcassInCase(CaseData caseData, String email, String servedParty) {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        emailNotificationDetails.add(element(serviceOfApplicationEmailService.sendEmailNotificationToCafcass(
            caseData,
            email,
            servedParty
        )));
        return emailNotificationDetails;
    }

    public ServedApplicationDetails sendNotificationForServiceOfApplication(CaseData caseData, String authorization,
                                                                            Map<String, Object> caseDataMap) {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
        String whoIsResponsibleForServing;

        if (C100_CASE_TYPE.equals(CaseUtils.getCaseTypeOfApplication(caseData))) {
            if (CaseUtils.isCaseCreatedByCitizen(caseData)) {
                whoIsResponsibleForServing = handleNotificationsForCitizenCreatedCase(
                    caseData,
                    authorization,
                    emailNotificationDetails,
                    bulkPrintDetails,
                    caseDataMap
                );
            } else {
                whoIsResponsibleForServing = handleNotificationsCaSolicitorCreatedCase(
                    caseData,
                    authorization,
                    emailNotificationDetails,
                    bulkPrintDetails,
                    caseDataMap
                );
            }
            checkAndSendCafcassCymruEmails(caseData, emailNotificationDetails);
            if (YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaServeLocalAuthorityYesOrNo())
                && null != caseData.getServiceOfApplication().getSoaLaEmailAddress()) {
                List<Document> docsForLa = getDocsToBeServedToLa(authorization, caseData);
                if (!docsForLa.isEmpty()) {
                    try {
                        EmailNotificationDetails emailNotification = serviceOfApplicationEmailService
                            .sendEmailNotificationToLocalAuthority(
                                authorization,
                                caseData,
                                caseData.getServiceOfApplication()
                                    .getSoaLaEmailAddress(),
                                docsForLa,
                                PrlAppsConstants.SERVED_PARTY_LOCAL_AUTHORITY);
                        if (null != emailNotification) {
                            emailNotificationDetails.add(element(emailNotification));
                        }
                    } catch (IOException e) {
                        log.error("Failed to serve email to Local Authority", e);
                    }
                }
            }

        } else {
            if (CaseUtils.isCaseCreatedByCitizen(caseData)) {
                whoIsResponsibleForServing = handleNotificationsForCitizenCreatedCase(caseData,
                                                                                      authorization,
                                                                                      emailNotificationDetails,
                                                                                      bulkPrintDetails,
                                                                                      caseDataMap
                );
            } else {
                whoIsResponsibleForServing = handleNotificationsDaSolicitorCreatedCase(
                    caseData,
                    authorization,
                    emailNotificationDetails,
                    caseDataMap
                );
            }
        }

        return ServedApplicationDetails.builder().emailNotificationDetails(emailNotificationDetails)
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .servedAt(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
                          .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE))))
            .modeOfService(CaseUtils.getModeOfService(emailNotificationDetails, bulkPrintDetails))
            .whoIsResponsible(whoIsResponsibleForServing)
            .bulkPrintDetails(bulkPrintDetails).build();
    }

    private String handleNotificationsForCitizenCreatedCase(CaseData caseData, String authorization,
                                                            List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                            List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                            Map<String, Object> caseDataMap) {
        //CITIZEN SCENARIO
        String whoIsResponsibleForServing = COURT;
        List<Document> c100StaticDocs = serviceOfApplicationPostService.getStaticDocs(authorization,
                                                                                      CaseUtils.getCaseTypeOfApplication(caseData),
                                                                                      caseData);
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            log.info("Sending service of application notifications to C100 citizens");
            if (YesOrNo.No.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())
                && (caseData.getServiceOfApplication().getSoaRecipientsOptions() != null)
                && (!caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue().isEmpty())) {
                handleNonPersonalServiceForCitizenC100(caseData, authorization, emailNotificationDetails,
                                                   bulkPrintDetails, c100StaticDocs);
            } else {
                whoIsResponsibleForServing = handlePersonalServiceForCitizenC100(caseData, authorization,
                                                    emailNotificationDetails,
                                                    bulkPrintDetails, c100StaticDocs,
                                                    caseDataMap);
            }
            //serving other people in case
            if (null != caseData.getServiceOfApplication().getSoaOtherParties()
                && !caseData.getServiceOfApplication().getSoaOtherParties().getValue().isEmpty()) {
                log.info("sending notification to Other in case of Citizen");
                sendNotificationToOthers(caseData, authorization, bulkPrintDetails, c100StaticDocs);
            }
        } else {
            if (SoaCitizenServingRespondentsEnum.unrepresentedApplicant
                .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsDA())) {
                getNotificationPack(caseData, PrlAppsConstants.E, c100StaticDocs);
                getNotificationPack(caseData, PrlAppsConstants.F, c100StaticDocs);
                Element<PartyDetails> applicant = Element.<PartyDetails>builder()
                    .id(caseData.getApplicantsFL401().getPartyId())
                    .value(caseData.getApplicantsFL401())
                    .build();
                CaseInvite caseInvite = getCaseInvite(applicant.getId(), caseData.getCaseInvites());
                if (Yes.equals(caseData.getDoYouNeedAWithoutNoticeHearing())) {
                    generateAccessCodeLetter(authorization, caseData, applicant, caseInvite, PRL_LET_ENG_FL401_RE2);
                } else {
                    generateAccessCodeLetter(authorization, caseData, applicant, caseInvite, PRL_LET_ENG_FL401_RE3);
                }
                whoIsResponsibleForServing = UNREPRESENTED_APPLICANT;
            } else {
                whoIsResponsibleForServing = SoaCitizenServingRespondentsEnum.courtBailiff
                    .equals(caseData.getServiceOfApplication()
                                .getSoaCitizenServingRespondentsOptionsDA()) ? PERSONAL_SERVICE_SERVED_BY_BAILIFF : PERSONAL_SERVICE_SERVED_BY_CA;
                getNotificationPack(caseData, PrlAppsConstants.C, c100StaticDocs);
                getNotificationPack(caseData, PrlAppsConstants.D, c100StaticDocs);
            }
        }
        return whoIsResponsibleForServing;
    }

    private String handleNotificationsCaSolicitorCreatedCase(CaseData caseData, String authorization,
                                                           List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                           List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                             Map<String, Object> caseDataMap) {
        String whoIsResponsibleForServing = COURT;
        List<Document> c100StaticDocs = serviceOfApplicationPostService.getStaticDocs(authorization,
                                                                                      CaseUtils.getCaseTypeOfApplication(caseData),
                                                                                      caseData);
        if (caseData.getServiceOfApplication().getSoaServeToRespondentOptions() != null
            && YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())) {
            if (SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative.equals(caseData.getServiceOfApplication()
                                                                                           .getSoaServingRespondentsOptionsCA())) {
                log.info("Personal Service - Case created by - Solicitor");
                whoIsResponsibleForServing = caseData.getApplicants().get(0).getValue().getRepresentativeFullName();
                //This is added with assumption that, For applicant legl representative selection
                // if multiple applicants are present only the first applicant solicitor will receive notification

                List<Document> packHiDocs = new ArrayList<>();
                caseData.getRespondents().forEach(respondent -> packHiDocs.add(generateAccessCodeLetter(authorization, caseData,
                                                                                                        respondent,
                                                                                                        null,
                                                                                                        PRL_LET_ENG_C100_RE6))
                );
                packHiDocs.addAll(getNotificationPack(caseData, PrlAppsConstants.HI, c100StaticDocs));
                Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
                dynamicData.put("name", caseData.getApplicants().get(0).getValue().getRepresentativeFullName());
                dynamicData.put("c100", true);
                dynamicData.put(DASH_BOARD_LINK, manageCaseUrl + PrlAppsConstants.URL_STRING + caseData.getId());
                populateLanguageMap(caseData, dynamicData);
                EmailNotificationDetails emailNotification = serviceOfApplicationEmailService
                    .sendEmailUsingTemplateWithAttachments(
                        authorization, caseData.getApplicants().get(0).getValue().getSolicitorEmail(),
                        packHiDocs,
                        SendgridEmailTemplateNames.SOA_PERSONAL_CA_DA_APPLICANT_LEGAL_REP,
                        dynamicData,
                        SERVED_PARTY_APPLICANT_SOLICITOR
                    );
                if (null != emailNotification) {
                    emailNotificationDetails.add(element(emailNotification));
                }
            } else if (SoaSolicitorServingRespondentsEnum.courtBailiff
                .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA())
                || SoaSolicitorServingRespondentsEnum.courtAdmin
                .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA())) {
                handleNotificationsCaSolicitorPersonalCourtAdminBailiff(caseData, authorization, emailNotificationDetails,
                                                                        c100StaticDocs, caseDataMap);
                whoIsResponsibleForServing = SoaSolicitorServingRespondentsEnum.courtBailiff
                    .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA())
                    ? PERSONAL_SERVICE_SERVED_BY_BAILIFF : PERSONAL_SERVICE_SERVED_BY_CA;
            }
        } else if (YesOrNo.No.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())
            && (caseData.getServiceOfApplication().getSoaRecipientsOptions() != null)
            && (!caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue().isEmpty())) {
            log.info("Non personal Service - Case created by - Solicitor");
            c100StaticDocs = c100StaticDocs.stream().filter(d -> ! d.getDocumentFileName().equalsIgnoreCase(
                C9_DOCUMENT_FILENAME)).collect(
                Collectors.toList());
            log.info("serving applicants or respondents non personal");
            List<DynamicMultiselectListElement> selectedApplicants = getSelectedApplicantsOrRespondents(
                caseData.getApplicants(),
                caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue()
            );
            if (!selectedApplicants.isEmpty()) {
                List<Document> packQDocs = getNotificationPack(caseData, PrlAppsConstants.Q, c100StaticDocs);
                emailNotificationDetails.addAll(sendNotificationToApplicantSolicitor(
                    caseData,
                    authorization,
                    selectedApplicants,
                    packQDocs,
                    SERVED_PARTY_APPLICANT_SOLICITOR
                ));
            }
            List<DynamicMultiselectListElement> selectedRespondents = getSelectedApplicantsOrRespondents(
                caseData.getRespondents(),
                caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue()
            );
            if (!selectedRespondents.isEmpty()) {
                //unrepresented
                List<Document> packRDocs = getNotificationPack(caseData, PrlAppsConstants.R, c100StaticDocs);
                //represented
                List<Document> packSDocs = getNotificationPack(caseData, PrlAppsConstants.S, c100StaticDocs);
                sendNotificationToRespondentOrSolicitorNonPersonal(
                    caseData,
                    authorization,
                    emailNotificationDetails,
                    bulkPrintDetails,
                    selectedRespondents,
                    packSDocs,
                    packRDocs
                );
            }
        }
        //serving other people in case
        if (null != caseData.getServiceOfApplication().getSoaOtherParties()
            && !caseData.getServiceOfApplication().getSoaOtherParties().getValue().isEmpty()) {
            sendNotificationToOthers(caseData, authorization, bulkPrintDetails, c100StaticDocs);
        }
        return whoIsResponsibleForServing;
    }

    private void handleNotificationsCaSolicitorPersonalCourtAdminBailiff(CaseData caseData, String authorization,
                                                                         List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                                         List<Document> c100StaticDocs,
                                                                         Map<String, Object> caseDataMap) {
        Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
        dynamicData.put("name", caseData.getApplicants().get(0).getValue().getRepresentativeFullName());
        dynamicData.put(DASH_BOARD_LINK, manageCaseUrl + PrlAppsConstants.URL_STRING + caseData.getId());
        populateLanguageMap(caseData, dynamicData);
        List<Document> packjDocs = getNotificationPack(caseData, PrlAppsConstants.J, c100StaticDocs);
        EmailNotificationDetails emailNotification = serviceOfApplicationEmailService.sendEmailUsingTemplateWithAttachments(authorization,
                                                   caseData.getApplicants().get(0).getValue().getSolicitorEmail(),
                                                   packjDocs,
                                                   SendgridEmailTemplateNames.SOA_SERVE_APPLICANT_SOLICITOR_NONPER_PER_CA_CB,
                                                   dynamicData,
                                                   PRL_COURT_ADMIN);
        if (null != emailNotification) {
            emailNotificationDetails.add(element(emailNotification));
        }
        List<Document> packkDocs = getDocumentsForCaorBailiffToServeRespondents(caseData, authorization, c100StaticDocs);
        final SoaPack unservedRespondentPack = SoaPack.builder()
            .packDocument(wrapElements(packkDocs))
            .partyIds(CaseUtils.getPartyIdList(caseData.getRespondents()))
            .servedBy(PRL_COURT_ADMIN)
            .packCreatedDate(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS).format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE))))
            .personalServiceBy(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA().toString())
            .build();
        caseDataMap.put(UNSERVED_RESPONDENT_PACK, unservedRespondentPack);
    }

    private void populateLanguageMap(CaseData caseData, Map<String, Object> dynamicData) {
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        dynamicData.put(ENG, documentLanguage.isGenEng());
        dynamicData.put(WEL, documentLanguage.isGenWelsh());
        dynamicData.put(IS_ENGLISH, documentLanguage.isGenEng());
        dynamicData.put(IS_WELSH, documentLanguage.isGenWelsh());
    }

    private void sendNotificationsAndCreatePacksForDaCourtAdminAndBailiff(CaseData caseData, String authorization,
                                                                          List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                                          List<Document> staticDocs,
                                                                          Map<String, Object> caseDataMap) {
        Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
        dynamicData.put("name", caseData.getApplicantsFL401().getRepresentativeFullName());
        dynamicData.put(DASH_BOARD_LINK, manageCaseUrl + PrlAppsConstants.URL_STRING + caseData.getId());
        populateLanguageMap(caseData, dynamicData);
        List<Document> packcDocs = new ArrayList<>(getNotificationPack(caseData, PrlAppsConstants.C, staticDocs));
        EmailNotificationDetails emailNotification = serviceOfApplicationEmailService.sendEmailUsingTemplateWithAttachments(
            authorization,
            caseData.getApplicantsFL401().getSolicitorEmail(),
            packcDocs,
            SendgridEmailTemplateNames.SOA_SERVE_APPLICANT_SOLICITOR_NONPER_PER_CA_CB,
            dynamicData,
            SERVED_PARTY_APPLICANT_SOLICITOR
        );
        if (null != emailNotification) {
            emailNotificationDetails.add(element(emailNotification));
        }
        List<Document> packdDocs = getRespondentPacksForDaPersonaServiceByCourtAdminAndBailiff(
            caseData,
            authorization,
            staticDocs
        );
        final SoaPack unservedRespondentPack = SoaPack.builder()
            .packDocument(wrapElements(packdDocs))
            .partyIds(wrapElements(caseData.getRespondentsFL401().getPartyId().toString()))
            .servedBy(PRL_COURT_ADMIN)
            .packCreatedDate(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS).format(ZonedDateTime.now(ZoneId.of(
                EUROPE_LONDON_TIME_ZONE))))
            .personalServiceBy(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA().toString())
            .build();
        caseDataMap.put(UNSERVED_RESPONDENT_PACK, unservedRespondentPack);
    }

    private List<Document> getDocumentsForCaorBailiffToServeRespondents(CaseData caseData, String authorization,
                                                                        List<Document> c100StaticDocs) {
        List<Document> re5Letters = new ArrayList<>();
        for (Element<PartyDetails> respondent: caseData.getRespondents()) {
            re5Letters.add(generateCoverLetterBasedOnCaseAccess(authorization, caseData, respondent,
                                                    PRL_LET_ENG_RE5));
        }
        List<Document> packkDocs = new ArrayList<>(re5Letters);
        packkDocs.addAll(getNotificationPack(caseData, PrlAppsConstants.K, c100StaticDocs));
        return packkDocs;
    }

    private List<Document> getRespondentPacksForDaPersonaServiceByCourtAdminAndBailiff(CaseData caseData, String authorization,
                                                                                       List<Document> staticDocs) {
        List<Document> packdDocs = new ArrayList<>();
        packdDocs.addAll(getCoverLetterForDaCourtAdminAndBailiffPersonalService(caseData, authorization));
        packdDocs.addAll(getNotificationPack(caseData, PrlAppsConstants.D, staticDocs));
        return packdDocs;
    }

    private List<Document> getCoverLetterForDaCourtAdminAndBailiffPersonalService(CaseData caseData, String authorization) {
        List<Document> reLetters = new ArrayList<>();
        Element<PartyDetails> respondent = Element.<PartyDetails>builder()
            .id(caseData.getRespondentsFL401().getPartyId())
            .value(caseData.getRespondentsFL401())
            .build();
        boolean applyOrderWithoutGivingNoticeToRespondent = CaseUtils.isApplyOrderWithoutGivingNoticeToRespondent(caseData);

        if (applyOrderWithoutGivingNoticeToRespondent) {
            reLetters.add(generateAccessCodeLetter(authorization, caseData, respondent, null,
                                                   PRL_LET_ENG_FL401_RE4
            ));
        } else {
            reLetters.add(generateAccessCodeLetter(authorization, caseData, respondent, null,
                                                   PRL_LET_ENG_FL401_RE1
            ));
        }
        return reLetters;
    }

    private List<Document> getCoverLettersAndRespondentPacksForDaApplicantSolicitor(CaseData caseData, String authorization,
                                                                                    List<Document> packA, List<Document> packB,
                                                                                    boolean attachLetters) {
        List<Document> reLetters = new ArrayList<>();
        if (attachLetters) {
            Element<PartyDetails> respondent = Element.<PartyDetails>builder()
                .id(caseData.getRespondentsFL401().getPartyId())
                .value(caseData.getRespondentsFL401())
                .build();
            getCoverLetterForDaApplicantSolicitor(caseData, authorization, reLetters, respondent);
        }
        removeDuplicatesAndGetConsolidatedDocs(packA, packB, reLetters);
        return reLetters;
    }

    private void removeDuplicatesAndGetConsolidatedDocs(List<Document> packA, List<Document> packB, List<Document> docs) {
        if (CollectionUtils.isNotEmpty(packA) && CollectionUtils.isNotEmpty(packB)) {
            for (Document packBDocument : packB) {
                boolean isPresentInPackA = false;
                for (Document packADocument : packA) {
                    if (isNotEmpty(packADocument) && isNotEmpty(packBDocument)
                        && packADocument.getDocumentBinaryUrl().equalsIgnoreCase(packBDocument.getDocumentBinaryUrl())) {
                        isPresentInPackA = true;
                        break;
                    }
                }
                if (!isPresentInPackA) {
                    docs.add(packBDocument);
                }
            }
            docs.addAll(packA);
        }
    }

    private void getCoverLetterForDaApplicantSolicitor(CaseData caseData, String authorization,
                                                       List<Document> reLetters, Element<PartyDetails> respondent) {
        boolean applyOrderWithoutGivingNoticeToRespondent = CaseUtils.isApplyOrderWithoutGivingNoticeToRespondent(
            caseData);

        if (applyOrderWithoutGivingNoticeToRespondent) {
            reLetters.add(generateAccessCodeLetter(authorization, caseData, respondent, null,
                                                   PRL_LET_ENG_FL401_RE2
            ));
        } else {
            reLetters.add(generateAccessCodeLetter(authorization, caseData, respondent, null,
                                                   PRL_LET_ENG_FL401_RE3
            ));
        }
    }

    private String handleNotificationsDaSolicitorCreatedCase(CaseData caseData, String authorization,
                                                           List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                             Map<String, Object> caseDataMap) {
        List<Document> staticDocs = serviceOfApplicationPostService.getStaticDocs(authorization,
                                                                                  CaseUtils.getCaseTypeOfApplication(caseData),
                                                                                  caseData);
        String whoIsResponsibleForServing = null;
        log.info("Fl401 case journey for caseId {}", caseData.getId());
        if (SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative.equals(caseData.getServiceOfApplication()
                                                                                       .getSoaServingRespondentsOptionsDA())) {
            List<Document> packADocs = getNotificationPack(caseData, PrlAppsConstants.A, staticDocs);
            List<Document> packBDocs = getNotificationPack(caseData, PrlAppsConstants.B, staticDocs);
            emailNotificationDetails.add(element(sendEmailDaPersonalApplicantLegalRep(caseData, authorization, packADocs, packBDocs, true)));
            whoIsResponsibleForServing = SERVED_PARTY_APPLICANT_SOLICITOR;
        } else if (SoaSolicitorServingRespondentsEnum.courtBailiff
            .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA())
            || SoaSolicitorServingRespondentsEnum.courtAdmin
            .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA())) {
            sendNotificationsAndCreatePacksForDaCourtAdminAndBailiff(caseData, authorization, emailNotificationDetails,
                                                                     staticDocs, caseDataMap
            );
            whoIsResponsibleForServing = SoaSolicitorServingRespondentsEnum.courtBailiff
                .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA())
                ? PERSONAL_SERVICE_SERVED_BY_BAILIFF : PERSONAL_SERVICE_SERVED_BY_CA;

        } else {
            log.error("#SOA TO DO...Generate packs to be served by unrepresented applicant.."
                          + "solicitor created case");
        }
        return whoIsResponsibleForServing;
    }

    private void sendNotificationToOthers(CaseData caseData, String authorization, List<Element<BulkPrintDetails>> bulkPrintDetails,
                                          List<Document> c100StaticDocs) {
        log.info("serving other people in case");

        List<DynamicMultiselectListElement> othersToNotify = getSelectedApplicantsOrRespondents(
            CaseUtils.getOthersToNotifyInCase(caseData),
            caseData.getServiceOfApplication().getSoaOtherParties().getValue());
        List<Document> packNDocs = getNotificationPack(caseData, PrlAppsConstants.N, c100StaticDocs);
        bulkPrintDetails.addAll(sendPostToOtherPeopleInCase(caseData, authorization, othersToNotify,
                                                            packNDocs,
                                                            PrlAppsConstants.SERVED_PARTY_OTHER
        ));
    }

    private void sendNotificationToRespondentOrSolicitorNonPersonal(CaseData caseData, String authorization,
                                                                    List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                                    List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                                    List<DynamicMultiselectListElement> selectedRespondents,
                                                                    List<Document> packSdocs, List<Document> packRdocs) {
        selectedRespondents.forEach(respondentc100 -> {
            Optional<Element<PartyDetails>> party = getParty(respondentc100.getCode(), caseData.getRespondents());
            if (party.isPresent() && CaseUtils.hasLegalRepresentation(party.get().getValue())) {
                if (party.get().getValue().getSolicitorEmail() != null) {
                    try {
                        log.info(
                            "Sending the email notification to respondent solicitor for C100 Application for caseId {}",
                            caseData.getId()
                        );
                        Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
                        dynamicData.put("name", party.get().getValue().getRepresentativeFullName());
                        dynamicData.put(DASH_BOARD_LINK, manageCaseUrl + PrlAppsConstants.URL_STRING + caseData.getId());
                        dynamicData.put("respondent", true);
                        populateLanguageMap(caseData, dynamicData);
                        List<Document> finalDocs = removeCoverLettersFromThePacks(packSdocs);
                        EmailNotificationDetails emailNotification = serviceOfApplicationEmailService.sendEmailUsingTemplateWithAttachments(
                            authorization,
                            party.get().getValue().getSolicitorEmail(),
                            finalDocs,
                            SendgridEmailTemplateNames.SOA_SERVE_APPLICANT_SOLICITOR_NONPER_PER_CA_CB,
                            dynamicData,
                            PrlAppsConstants.SERVED_PARTY_RESPONDENT_SOLICITOR
                        );
                        if (null != emailNotification) {
                            emailNotificationDetails.add(element(emailNotification));
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (party.isPresent() && (!CaseUtils.hasLegalRepresentation(party.get().getValue()))) {
                if (party.get().getValue().getAddress() != null && StringUtils.isNotEmpty(party.get().getValue().getAddress().getAddressLine1())) {
                    log.info(
                        "Sending the notification in post to respondent for C100 Application for caseId {}",
                        caseData.getId()
                    );
                    List<Document> finalDocs = removeCoverLettersFromThePacks(packRdocs);
                    Document coverLetter = generateCoverLetterBasedOnCaseAccess(authorization, caseData, party.get(), PRL_LET_ENG_RE5);
                    sendPostWithAccessCodeLetterToParty(caseData, authorization, finalDocs, bulkPrintDetails, party.get(),
                                                        coverLetter, SERVED_PARTY_RESPONDENT);
                } else {
                    log.info("Unable to send any notification to respondent for C100 Application for caseId {} "
                                 + "as no address available", caseData.getId());
                }
            }
        });
    }

    private List<Document> removeCoverLettersFromThePacks(List<Document> documents) {
        return documents.stream().filter(document -> !document.getDocumentFileName().contains("cover_letter")).toList();
    }

    private void handleNonPersonalServiceForCitizenC100(CaseData caseData, String authorization,
                                                    List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                    List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                    List<Document> c100StaticDocs) {
        List<DynamicMultiselectListElement> selectedApplicants = getSelectedApplicantsOrRespondents(
            caseData.getApplicants(),
            caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue()
        );
        List<DynamicMultiselectListElement> selectedRespondents = getSelectedApplicantsOrRespondents(
            caseData.getRespondents(),
            caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue()
        );
        if (CollectionUtils.isNotEmpty(selectedApplicants)) {
            List<Document> packPDocs = getNotificationPack(caseData, PrlAppsConstants.P, c100StaticDocs);
            emailNotificationDetails.addAll(sendNotificationsToCitizenApplicantsC100(
                authorization,
                selectedApplicants,
                caseData,
                bulkPrintDetails,
                packPDocs
            ));
        }

        if (CollectionUtils.isNotEmpty(selectedRespondents)) {
            emailNotificationDetails
                .addAll(sendNotificationsToCitizenRespondentsC100(authorization, selectedRespondents,
                                                                  caseData, bulkPrintDetails, c100StaticDocs, true));
        }
    }

    private String handlePersonalServiceForCitizenC100(CaseData caseData, String authorization,
                                                     List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                     List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                     List<Document> c100StaticDocs,
                                                     Map<String, Object> caseDataMap) {
        String whoIsResponsibleForServing;
        //Suppressed java:S1172 as emailNotificationDetails not used, but will be used when citizen journey comes into the picture.
        log.info("Service of application, unrepresented applicant/citizen case");
        log.info("Personal service options {}", caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsCA());
        if (SoaCitizenServingRespondentsEnum.unrepresentedApplicant
            .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsCA())) {
            whoIsResponsibleForServing = UNREPRESENTED_APPLICANT;
            List<Document> packLdocs = getNotificationPack(caseData, PrlAppsConstants.L, c100StaticDocs);
            notifyC100ApplicantsPersonalServiceUnRepApplicant(authorization,
                                                              caseData,
                                                              emailNotificationDetails,
                                                              bulkPrintDetails,
                                                              packLdocs);
            String dateCreated = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
                .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE)));
            caseDataMap.put(UNSERVED_RESPONDENT_PACK, SoaPack.builder()
                .packDocument(wrapElements(getNotificationPack(caseData, M, c100StaticDocs)))
                .partyIds(CaseUtils.getPartyIdList(caseData.getRespondents()))
                .servedBy(UNREPRESENTED_APPLICANT)
                .personalServiceBy(SoaCitizenServingRespondentsEnum.unrepresentedApplicant.toString())
                .packCreatedDate(dateCreated)
                .build());
        } else {
            log.info("personal service - court bailiff/court admin");
            whoIsResponsibleForServing = SoaCitizenServingRespondentsEnum.courtBailiff
                .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsCA())
                ? PERSONAL_SERVICE_SERVED_BY_BAILIFF : PERSONAL_SERVICE_SERVED_BY_CA;

            List<Document> packjDocs = new ArrayList<>(getNotificationPack(caseData, PrlAppsConstants.J, c100StaticDocs));

            notifyC100ApplicantsPersonalServiceCourtAdminBailiff(authorization,
                                                                 caseData,
                                                                 emailNotificationDetails,
                                                                 bulkPrintDetails,
                                                                 packjDocs);

            caseDataMap.put(UNSERVED_RESPONDENT_PACK, generateRespondentsPack(authorization, caseData, c100StaticDocs));
        }
        return whoIsResponsibleForServing;
    }

    private void notifyC100ApplicantsPersonalServiceUnRepApplicant(String authorization,
                                                                   CaseData caseData,
                                                                   List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                                   List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                                   List<Document> packLdocs) {

        List<Document> packLDocsWithoutC9 = packLdocs.stream()
            .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_C9_PERSONAL_SERVICE_FILENAME)).toList();
        AtomicBoolean isNotFirstApplicant = new AtomicBoolean(false);
        caseData.getApplicants().forEach(selectedApplicant -> {
            List<Document> docs = packLDocsWithoutC9;
            if (!isNotFirstApplicant.get()) {
                docs = packLdocs;
            }
            isNotFirstApplicant.set(true);
            if (!CaseUtils.hasLegalRepresentation(selectedApplicant.getValue())) {
                if (ContactPreferences.email.equals(selectedApplicant.getValue().getContactPreferences())) {
                    Map<String, String> fieldsMap = new HashMap<>();
                    fieldsMap.put(AUTHORIZATION, authorization);
                    fieldsMap.put(COVER_LETTER_TEMPLATE, PRL_LET_ENG_AP7);
                    sendEmailToApplicantLipPersonalC100(caseData,
                                                        emailNotificationDetails,
                                                        selectedApplicant,
                                                        docs,
                                                        SendgridEmailTemplateNames.SOA_CA_APPLICANT_LIP_PERSONAL,
                                                        fieldsMap,
                                                        doesC1aExists(caseData).equals(Yes)
                                                            ? SOA_CA_PERSONAL_UNREPRESENTED_APPLICANT
                                                            : SOA_CA_PERSONAL_UNREPRESENTED_APPLICANT_WITHOUT_C1A
                    );
                } else {
                    //Post packs to applicants
                    sendSoaPacksToPartyViaPost(
                        authorization,
                        caseData,
                        docs,
                        bulkPrintDetails,
                        selectedApplicant,
                        PRL_LET_ENG_AP7
                    );
                }
            }
        });
    }

    private void notifyC100ApplicantsPersonalServiceCourtAdminBailiff(String authorization,
                                                                      CaseData caseData,
                                                                      List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                                      List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                                      List<Document> packDocs) {
        //C9 to be excluded for personal service court admin/bailiff
        List<Document> packDocsWithoutC9 = packDocs.stream()
            .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_C9_PERSONAL_SERVICE_FILENAME)).toList();

        //Notify applicants based on contact preference
        caseData.getApplicants().forEach(applicant -> {
            if (!CaseUtils.hasLegalRepresentation(applicant.getValue())) {
                if (ContactPreferences.email.equals(applicant.getValue().getContactPreferences())) {
                    //Notify applicants via email, if dashboard access then via gov notify email else via send grid
                    Map<String, String> fieldsMap = new HashMap<>();
                    fieldsMap.put(AUTHORIZATION, authorization);
                    fieldsMap.put(COVER_LETTER_TEMPLATE, PRL_LET_ENG_AP8);
                    sendEmailToApplicantLipPersonalC100(
                        caseData,
                        emailNotificationDetails,
                        applicant,
                        packDocsWithoutC9,
                        SendgridEmailTemplateNames.SOA_CA_NON_PERSONAL_SERVICE_APPLICANT_LIP,
                        fieldsMap,
                        EmailTemplateNames.SOA_UNREPRESENTED_APPLICANT_SERVED_BY_COURT
                    );
                } else {
                    //Post packs to applicants
                    sendSoaPacksToPartyViaPost(
                        authorization,
                        caseData,
                        packDocsWithoutC9,
                        bulkPrintDetails,
                        applicant,
                        PRL_LET_ENG_AP8
                    );
                }
            }
        });
    }

    private EmailNotificationDetails sendEmailToUnrepresentedApplicant(String authorization,
                                                                       CaseData caseData,
                                                                       List<Document> packDocs,
                                                                       Element<PartyDetails> party,
                                                                       String template,
                                                                       EmailTemplateNames emailTemplate) {

        //Send a gov notify email
        serviceOfApplicationEmailService.sendGovNotifyEmail(
            LanguagePreference.getPreferenceLanguage(caseData),
            party.getValue().getEmail(),
            emailTemplate,
            serviceOfApplicationEmailService.buildCitizenEmailVars(caseData,
                                                                   party.getValue(),
                                                                   YesOrNo.Yes.equals(doesC1aExists(caseData)) ? "Yes" : null
            )
        );
        //Generate cover letter without access code for applicant who has access to dashboard
        List<Document> packsWithCoverLetter = new ArrayList<>(List.of((generateCoverLetterBasedOnCaseAccess(authorization, caseData,
                                                                                                            party, template))));
        packsWithCoverLetter.addAll(packDocs);

        //Create email notification with packs
        return EmailNotificationDetails.builder()
            .emailAddress(party.getValue().getEmail())
            .servedParty(SERVED_PARTY_APPLICANT)
            .docs(wrapElements(packsWithCoverLetter))
            .attachedDocs(CITIZEN_CAN_VIEW_ONLINE)
            .timeStamp(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
                           .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE))))
            .build();
    }

    private EmailNotificationDetails sendSoaPacksToPartyViaEmail(String authorization,
                                                                 CaseData caseData,
                                                                 List<Document> packDocs,
                                                                 Element<PartyDetails> party,
                                                                 String coverLetterTemplate,
                                                                 SendgridEmailTemplateNames emailTemplate) {
        //Generate access code if party does not have access to dashboard
        List<Document> packsWithCoverLetter = new ArrayList<>(List.of((generateCoverLetterBasedOnCaseAccess(
            authorization,
            caseData,
            party,
            coverLetterTemplate
        ))));
        packsWithCoverLetter.addAll(packDocs);

        Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
        dynamicData.put("name", party.getValue().getLabelForDynamicList());
        dynamicData.put(DASH_BOARD_LINK, citizenUrl);
        populateLanguageMap(caseData, dynamicData);

        return serviceOfApplicationEmailService
            .sendEmailUsingTemplateWithAttachments(
                authorization,
                party.getValue().getEmail(),
                packsWithCoverLetter,
                emailTemplate,
                dynamicData,
                SERVED_PARTY_APPLICANT
            );
    }

    private void sendSoaPacksToPartyViaPost(String authorization,
                                            CaseData caseData,
                                            List<Document> packDocs,
                                            List<Element<BulkPrintDetails>> bulkPrintDetails,
                                            Element<PartyDetails> party,
                                            String coverLetterTemplate) {
        log.debug("Sending applicant packs via post for {}", party.getId());
        Document coverLetter = generateCoverLetterBasedOnCaseAccess(authorization, caseData,
                                                                    party, coverLetterTemplate
        );
        sendPostWithAccessCodeLetterToParty(
            caseData,
            authorization,
            packDocs,
            bulkPrintDetails,
            party,
            coverLetter,
            SERVED_PARTY_APPLICANT
        );
    }

    private SoaPack generateRespondentsPack(String authorization,
                                            CaseData caseData,
                                            List<Document> c100StaticDocs) {
        List<Document> packkDocs = generatePartiesPackDocsWithCoverLetter(authorization, caseData, caseData.getRespondents(),
                                                                          PRL_LET_ENG_RE5, PrlAppsConstants.K, c100StaticDocs);
        return SoaPack.builder()
            .packDocument(wrapElements(packkDocs))
            .partyIds(CaseUtils.getPartyIdList(caseData.getRespondents()))
            .servedBy(PRL_COURT_ADMIN)
            .packCreatedDate(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
                                 .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE))))
            .personalServiceBy(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsCA().toString())
            .build();
    }

    private void sendEmailToApplicantLipPersonalC100(CaseData caseData,
                                                     List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                     Element<PartyDetails> selectedApplicant,
                                                     List<Document> docs,
                                                     SendgridEmailTemplateNames emailTemplate,
                                                     Map<String, String> fieldMap,
                                                     EmailTemplateNames notifyTemplate) {
        EmailNotificationDetails emailNotification;
        if (isAccessEnabled(selectedApplicant)) {
            log.debug("Applicant has access to dashboard -> send gov notify email for {}", selectedApplicant.getId());
            emailNotification = sendEmailToUnrepresentedApplicant(fieldMap.get(AUTHORIZATION),
                                                                  caseData,
                                                                  docs,
                                                                  selectedApplicant,
                                                                  fieldMap.get(COVER_LETTER_TEMPLATE),
                                                                  notifyTemplate);
        } else {
            log.debug("Applicant does not access to dashboard -> send packs via sendgrid email for {}", selectedApplicant.getId());
            emailNotification = sendSoaPacksToPartyViaEmail(fieldMap.get(AUTHORIZATION),
                                                            caseData,
                                                            docs,
                                                            selectedApplicant,
                                                            fieldMap.get(COVER_LETTER_TEMPLATE),
                                                            emailTemplate);
        }

        if (emailNotification != null) {
            emailNotificationDetails.add(element(emailNotification.toBuilder()
                                                     .partyIds(String.valueOf(selectedApplicant.getId()))
                                                     .build()));
        }
    }

    public Map<String, Object> handleAboutToSubmit(CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();
        if (caseData.getServiceOfApplication() != null && SoaCitizenServingRespondentsEnum.unrepresentedApplicant
            .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsCA())) {
            caseData.getApplicants().get(0).getValue().getResponse().getCitizenFlags().setIsApplicationToBeServed(YesOrNo.Yes);
            caseDataMap.put(APPLICANTS, caseData.getApplicants());
        }

        caseDataMap.put(CASE_INVITES, generateCaseInvitesForParties(caseData));
        caseDataMap.putAll(setSoaOrConfidentialWaFields(caseData, callbackRequest.getEventId()));
        //PRL-5566 - Set FM5 notification flag to No during SOA
        caseDataMap.put("fm5RemindersSent", "NO");
        return caseDataMap;
    }


    public Map<String, Object> setSoaOrConfidentialWaFields(CaseData caseData, String eventId) {
        Map<String, Object> soaWaMap = new HashMap<>();
        String isC8CheckNeeded = NO;
        String responsibleForService = null;
        if (Event.SOA.getId().equals(eventId)) {
            if (isRespondentDetailsConfidential(caseData) || CaseUtils.isC8Present(caseData)) {
                isC8CheckNeeded = YES;
            }
            responsibleForService = getResponsibleForService(caseData);
            if (!C100_CASE_TYPE.equals(CaseUtils.getCaseTypeOfApplication(caseData))) {
                soaWaMap.put("isOccupationOrderSelected", isOccupationOrderSelected(caseData.getTypeOfApplicationOrders()));
            }
            soaWaMap.put("isC8CheckNeeded", isC8CheckNeeded);
        } else if (Event.CONFIDENTIAL_CHECK.getId().equals(eventId)) {
            soaWaMap.put("isC8CheckApproved", (caseData.getServiceOfApplication().getApplicationServedYesNo() != null
                && Yes.equals(caseData.getServiceOfApplication().getApplicationServedYesNo())) ? YES : NO);
            responsibleForService = (caseData.getServiceOfApplication().getUnServedRespondentPack() != null
                && caseData.getServiceOfApplication().getUnServedRespondentPack().getPersonalServiceBy() != null)
                ? caseData.getServiceOfApplication().getUnServedRespondentPack().getPersonalServiceBy() : null;
            if (!C100_CASE_TYPE.equals(CaseUtils.getCaseTypeOfApplication(caseData))) {
                soaWaMap.put("isOccupationOrderSelected", isOccupationOrderSelected(caseData.getTypeOfApplicationOrders()));
            }
        }
        soaWaMap.put("responsibleForService", responsibleForService);
        return soaWaMap;
    }

    private String isOccupationOrderSelected(TypeOfApplicationOrders typeOfApplicationOrders) {
        return null != typeOfApplicationOrders
            && null != typeOfApplicationOrders.getOrderType()
            && typeOfApplicationOrders.getOrderType().contains(
            FL401OrderTypeEnum.occupationOrder) ? YES : NO;
    }

    private String getResponsibleForService(CaseData caseData) {
        String responsibleForService = null;
        if (C100_CASE_TYPE.equals(CaseUtils.getCaseTypeOfApplication(caseData))) {
            if (Yes.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())) {
                if (CaseUtils.isCaseCreatedByCitizen(caseData)) {
                    responsibleForService = caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsCA().getId();
                } else {
                    responsibleForService = caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA().getId();
                }
            }
        } else {
            if (CaseUtils.isCaseCreatedByCitizen(caseData)) {
                responsibleForService = caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsDA().getId();
            } else {
                responsibleForService = caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA().getId();
            }
        }
        return responsibleForService;
    }

    public ResponseEntity<SubmittedCallbackResponse> handleSoaSubmitted(String authorisation, CallbackRequest callbackRequest) {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = allTabService.getStartAllTabsUpdate(String.valueOf(
            callbackRequest.getCaseDetails().getId()));
        Map<String, Object> caseDataMap = startAllTabsUpdateDataContent.caseDataMap();
        CaseData caseData = startAllTabsUpdateDataContent.caseData();
        caseDataMap.putAll(caseSummaryTabService.updateTab(caseData));

        if (isRespondentDetailsConfidential(caseData) || CaseUtils.isC8Present(caseData)) {
            return processConfidentialDetailsSoa(authorisation, callbackRequest, caseData, startAllTabsUpdateDataContent);
        }
        return processNonConfidentialSoa(
            authorisation,
            caseData,
            caseDataMap,
            startAllTabsUpdateDataContent,
            String.valueOf(callbackRequest.getCaseDetails().getId())
        );
    }

    private ResponseEntity<SubmittedCallbackResponse> processNonConfidentialSoa(String authorisation, CaseData caseData,
                                                                                Map<String, Object> caseDataMap,
                                                                                StartAllTabsUpdateDataContent updatedCaseDataContent,
                                                                                String caseId) {
        log.info("Confidential details are NOT present");
        List<Element<ServedApplicationDetails>> finalServedApplicationDetailsList;
        String confirmationBody = "";
        String confirmationHeader;
        if (caseData.getServiceOfApplication().getSoaServeToRespondentOptions() != null
            && YesOrNo.No.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())) {
            confirmationBody = CONFIRMATION_BODY_PREFIX;
            confirmationHeader = CONFIRMATION_HEADER_NON_PERSONAL;
        } else {
            confirmationHeader = CONFIRMATION_HEADER_PERSONAL;
            if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                if (SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative
                    .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA())
                    || SoaCitizenServingRespondentsEnum.unrepresentedApplicant
                    .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsCA())) {
                    confirmationBody = CONFIRMATION_BODY_APPLICANT_LR_SERVICE_PREFIX_CA;
                } else if (SoaCitizenServingRespondentsEnum.courtAdmin
                    .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsCA())
                    || SoaSolicitorServingRespondentsEnum.courtAdmin
                    .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA())) {
                    confirmationBody = CONFIRMATION_BODY_COURT_ADMIN_SERVICE_PREFIX_CA;
                } else if (SoaCitizenServingRespondentsEnum.courtBailiff
                    .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsCA())
                    || SoaSolicitorServingRespondentsEnum.courtBailiff
                    .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA())) {
                    confirmationBody = CONFIRMATION_BODY_BAILIFF_SERVICE_PREFIX_CA;
                }
            } else {
                if (SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative
                    .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA())
                    || SoaCitizenServingRespondentsEnum.unrepresentedApplicant
                    .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsDA())) {
                    confirmationBody = CONFIRMATION_BODY_APPLICANT_LR_SERVICE_PREFIX_DA;
                } else if (SoaSolicitorServingRespondentsEnum.courtAdmin
                    .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA())
                    || SoaCitizenServingRespondentsEnum.courtAdmin
                    .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsDA())) {
                    confirmationBody = CONFIRMATION_BODY_COURT_ADMIN_SERVICE_PREFIX_DA;
                } else if (SoaSolicitorServingRespondentsEnum.courtBailiff
                    .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA())
                    || SoaCitizenServingRespondentsEnum.courtBailiff
                    .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsDA())) {
                    confirmationBody = CONFIRMATION_BODY_BAILIFF_SERVICE_PREFIX_DA;
                }
            }
        }
        confirmationBody = String.format(confirmationBody, manageCaseUrl + PrlAppsConstants.URL_STRING
            + caseData.getId() + SERVICE_OF_APPLICATION_ENDPOINT);

        if (caseData.getFinalServedApplicationDetailsList() != null) {
            finalServedApplicationDetailsList = caseData.getFinalServedApplicationDetailsList();
        } else {
            log.info("*** finalServedApplicationDetailsList is empty in case data ***");
            finalServedApplicationDetailsList = new ArrayList<>();
        }
        finalServedApplicationDetailsList.add(element(sendNotificationForServiceOfApplication(caseData, authorisation, caseDataMap)));
        caseDataMap.put(FINAL_SERVED_APPLICATION_DETAILS_LIST, finalServedApplicationDetailsList);
        cleanUpSoaSelections(caseDataMap);

        //SAVE TEMP GENERATED ACCESS CODE
        caseDataMap.put(CASE_INVITES, caseData.getCaseInvites());

        //PRL-3466 - Citizen case auto linking
        autoLinkCitizenCase(caseData, caseDataMap);

        allTabService.submitAllTabsUpdate(
                updatedCaseDataContent.authorisation(),
                caseId,
                updatedCaseDataContent.startEventResponse(),
                updatedCaseDataContent.eventRequestData(),
                caseDataMap
        );
        return ok(SubmittedCallbackResponse.builder()
                      .confirmationHeader(confirmationHeader)
                      .confirmationBody(confirmationBody).build());
    }

    private void autoLinkCitizenCase(CaseData caseData,
                                     Map<String, Object> caseDataMap) {
        if (CaseCreatedBy.CITIZEN.equals(caseData.getCaseCreatedBy())) {
            List<Element<PartyDetails>> applicants = new ArrayList<>(caseData.getApplicants());

            applicants.stream()
                .filter(party -> No.equals(party.getValue().getIsAccessCodeNeeded())
                    && !hasDashboardAccess(party))
                .findFirst()
                .ifPresent(party -> {
                    log.info(
                        "*** Auto linking citizen case for primary applicant, partyId: {} and partyIndex: {}",
                        party.getId(),
                        applicants.indexOf(party)
                    );
                    User user = null != party.getValue().getUser()
                        ? party.getValue().getUser().toBuilder().build()
                        : User.builder().build();
                    user = user.toBuilder()
                        .idamId(caseData.getUserInfo().get(0).getValue().getIdamId())
                        .email(caseData.getUserInfo().get(0).getValue().getEmailAddress())
                        .build();

                    PartyDetails updatedPartyDetails = party.getValue().toBuilder().user(user).build();
                    applicants.set(applicants.indexOf(party), element(party.getId(), updatedPartyDetails));

                    caseDataMap.put(APPLICANTS, applicants);
                });
        }
    }

    private ResponseEntity<SubmittedCallbackResponse> processConfidentialDetailsSoa(String authorisation, CallbackRequest callbackRequest,
                                                                                    CaseData caseData,
                                                                                    StartAllTabsUpdateDataContent updatedCaseDataContent) {
        Map<String, Object> caseDataMap;
        caseDataMap = CaseUtils.getCaseTypeOfApplication(caseData).equalsIgnoreCase(C100_CASE_TYPE)
                            ? generatePacksForConfidentialCheckC100(callbackRequest.getCaseDetails(), authorisation)
                            : generatePacksForConfidentialCheckFl401(callbackRequest.getCaseDetails(), authorisation);

        cleanUpSoaSelections(caseDataMap);

        //SAVE TEMP GENERATED ACCESS CODE
        caseDataMap.put(CASE_INVITES, caseData.getCaseInvites());

        allTabService.submitAllTabsUpdate(
                updatedCaseDataContent.authorisation(),
                String.valueOf(callbackRequest.getCaseDetails().getId()),
                updatedCaseDataContent.startEventResponse(),
                updatedCaseDataContent.eventRequestData(),
                caseDataMap
        );
        String confirmationBody = String.format(CONFIDENTIAL_CONFIRMATION_BODY_PREFIX,
                                                manageCaseUrl + PrlAppsConstants.URL_STRING + caseData.getId()
                                                    + SERVICE_OF_APPLICATION_ENDPOINT);
        log.info("Confidential details are present, case needs to be reviewed and served later");
        return ok(SubmittedCallbackResponse.builder()
                      .confirmationHeader(CONFIDENTIAL_CONFIRMATION_HEADER)
                      .confirmationBody(confirmationBody).build());
    }

    private List<Document> getDocsToBeServedToLa(String authorisation, CaseData caseData) {
        if (YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaServeLocalAuthorityYesOrNo())
            && null != caseData.getServiceOfApplication().getSoaLaEmailAddress()) {
            List<Document> docs = new ArrayList<>();
            if (null != caseData.getServiceOfApplication().getSoaDocumentDynamicListForLa()) {
                for (Element<DocumentListForLa> laDocument: caseData.getServiceOfApplication().getSoaDocumentDynamicListForLa()) {
                    uk.gov.hmcts.reform.ccd.client.model.Document document = getSelectedDocumentFromDynamicList(
                        authorisation,
                        laDocument.getValue().getDocumentsListForLa(),
                        String.valueOf(caseData.getId())
                    );
                    if (null != document) {
                        docs.add(CaseUtils.convertDocType(document));
                    }
                }
            }
            if (Yes.equals(caseData.getServiceOfApplication().getSoaServeC8ToLocalAuthorityYesOrNo())) {
                if (null != caseData.getRespondentC8Document()) {
                    if (CollectionUtils.isNotEmpty(caseData.getRespondentC8Document().getRespondentAc8Documents())) {
                        caseData.getRespondentC8Document().getRespondentAc8Documents().forEach(document -> {
                            if (null != document.getValue().getRespondentC8Document()) {
                                docs.add(document.getValue().getRespondentC8Document());
                            }
                            if (null != document.getValue().getRespondentC8DocumentWelsh()) {
                                docs.add(document.getValue().getRespondentC8DocumentWelsh());
                            }
                        });
                    }
                    if (CollectionUtils.isNotEmpty(caseData.getRespondentC8Document().getRespondentBc8Documents())) {
                        caseData.getRespondentC8Document().getRespondentBc8Documents().forEach(document -> {
                            if (null != document.getValue().getRespondentC8Document()) {
                                docs.add(document.getValue().getRespondentC8Document());
                            }
                            if (null != document.getValue().getRespondentC8DocumentWelsh()) {
                                docs.add(document.getValue().getRespondentC8DocumentWelsh());
                            }
                        });
                    }
                    if (CollectionUtils.isNotEmpty(caseData.getRespondentC8Document().getRespondentCc8Documents())) {
                        caseData.getRespondentC8Document().getRespondentCc8Documents().forEach(document -> {
                            if (null != document.getValue().getRespondentC8Document()) {
                                docs.add(document.getValue().getRespondentC8Document());
                            }
                            if (null != document.getValue().getRespondentC8DocumentWelsh()) {
                                docs.add(document.getValue().getRespondentC8DocumentWelsh());
                            }
                        });
                    }
                    if (CollectionUtils.isNotEmpty(caseData.getRespondentC8Document().getRespondentDc8Documents())) {
                        caseData.getRespondentC8Document().getRespondentDc8Documents().forEach(document -> {
                            if (null != document.getValue().getRespondentC8Document()) {
                                docs.add(document.getValue().getRespondentC8Document());
                            }
                            if (null != document.getValue().getRespondentC8DocumentWelsh()) {
                                docs.add(document.getValue().getRespondentC8DocumentWelsh());
                            }
                        });
                    }
                    if (CollectionUtils.isNotEmpty(caseData.getRespondentC8Document().getRespondentEc8Documents())) {
                        caseData.getRespondentC8Document().getRespondentEc8Documents().forEach(document -> {
                            if (null != document.getValue().getRespondentC8Document()) {
                                docs.add(document.getValue().getRespondentC8Document());
                            }
                            if (null != document.getValue().getRespondentC8DocumentWelsh()) {
                                docs.add(document.getValue().getRespondentC8DocumentWelsh());
                            }
                        });
                    }
                }
                if (null != caseData.getC8Document()) {
                    docs.add(caseData.getC8Document());
                }
                if (null != caseData.getC8WelshDocument()) {
                    docs.add(caseData.getC8WelshDocument());
                }
            }
            return docs;
        }
        return Collections.emptyList();
    }

    public uk.gov.hmcts.reform.ccd.client.model.Document getSelectedDocumentFromDynamicList(String authorisation,
                                                                                            DynamicList selectedDocument,
                                                                                            String caseId) {
        try {
            CategoriesAndDocuments categoriesAndDocuments = coreCaseDataApi.getCategoriesAndDocuments(
                authorisation,
                authTokenGenerator.generate(),
                caseId
            );
            uk.gov.hmcts.reform.ccd.client.model.Document selectedDoc = getSelectedDocumentFromCategories(categoriesAndDocuments.getCategories(),
                                                                                                          selectedDocument);
            if (selectedDoc == null) {
                for (uk.gov.hmcts.reform.ccd.client.model.Document document: categoriesAndDocuments.getUncategorisedDocuments()) {
                    if (sendAndReplyService.fetchDocumentIdFromUrl(document.getDocumentURL())
                        .equalsIgnoreCase(selectedDocument.getValue().getCode())) {
                        selectedDoc = document;
                    }
                }
            }
            return selectedDoc;
        } catch (Exception e) {
            log.error("Error in getCategoriesAndDocuments method", e);
        }
        return null;
    }

    private uk.gov.hmcts.reform.ccd.client.model.Document getSelectedDocumentFromCategories(List<Category> categoryList,
                                                                                            DynamicList selectedDocument) {
        uk.gov.hmcts.reform.ccd.client.model.Document documentSelected = null;

        for (Category category: categoryList) {
            if (category.getDocuments() != null) {
                for (uk.gov.hmcts.reform.ccd.client.model.Document document : category.getDocuments()) {
                    String[] codes = selectedDocument.getValue().getCode().split(ARROW_SEPARATOR);

                    if (sendAndReplyService.fetchDocumentIdFromUrl(document.getDocumentURL())
                        .equalsIgnoreCase(codes[codes.length - 1])) {
                        documentSelected = document;
                        break;
                    }
                }
            }
            if (null == documentSelected && category.getSubCategories() != null) {
                documentSelected = getSelectedDocumentFromCategories(
                    category.getSubCategories(),
                    selectedDocument
                );
            }
            if (documentSelected != null) {
                break;
            }
        }
        return documentSelected;
    }

    private List<Element<EmailNotificationDetails>> sendNotificationsToCitizenApplicantsC100(String authorization,
                                                                                             List<DynamicMultiselectListElement> selectedApplicants,
                                                                                             CaseData caseData,
                                                                                             List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                                                             List<Document> packDocs) {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        selectedApplicants.forEach(applicant -> {
            Optional<Element<PartyDetails>> selectedParty = getParty(applicant.getCode(), caseData.getApplicants());
            if (selectedParty.isPresent()) {
                Element<PartyDetails> selectedApplicant = selectedParty.get();
                if (ContactPreferences.email.equals(selectedApplicant.getValue().getContactPreferences())) {
                    Map<String, String> fieldsMap = new HashMap<>();
                    fieldsMap.put(AUTHORIZATION, authorization);
                    fieldsMap.put(COVER_LETTER_TEMPLATE, Templates.AP6_LETTER);
                    sendEmailToApplicantLipPersonalC100(caseData,
                                                        emailNotificationDetails,
                                                        selectedApplicant,
                                                        packDocs,
                                                        SendgridEmailTemplateNames.SOA_CA_NON_PERSONAL_SERVICE_APPLICANT_LIP,
                                                        fieldsMap,
                                                        doesC1aExists(caseData).equals(Yes)
                                                            ? SOA_CA_PERSONAL_UNREPRESENTED_APPLICANT
                                                            : SOA_CA_PERSONAL_UNREPRESENTED_APPLICANT_WITHOUT_C1A
                    );
                } else {
                    Document coverLetter = generateCoverLetterBasedOnCaseAccess(authorization, caseData,
                                                                                    selectedApplicant, Templates.AP6_LETTER);
                    sendPostWithAccessCodeLetterToParty(caseData,
                                                        authorization,
                                                        packDocs,
                                                        bulkPrintDetails,
                                                        selectedApplicant,
                                                        coverLetter,
                                                        SERVED_PARTY_APPLICANT
                    );
                }
            }
        });
        return emailNotificationDetails;
    }

    private List<Element<EmailNotificationDetails>> sendNotificationsToCitizenRespondentsC100(String authorization,
                                                              List<DynamicMultiselectListElement> selectedRespondents,
                                                          CaseData caseData,  List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                          List<Document> docs, boolean isStaticDocs) {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        selectedRespondents.forEach(respondent -> {
            Optional<Element<PartyDetails>> selectedParty = getParty(respondent.getCode(), caseData.getRespondents());
            if (selectedParty.isPresent()) {
                Element<PartyDetails> selectedRespondent = selectedParty.get();
                if (CaseUtils.hasLegalRepresentation(selectedRespondent.getValue())) {
                    log.info("Respondent is represented");
                    try {
                        Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
                        dynamicData.put("name", selectedRespondent.getValue().getRepresentativeFullName());
                        dynamicData.put(DASH_BOARD_LINK, citizenUrl + CITIZEN_DASHBOARD);
                        populateLanguageMap(caseData, dynamicData);
                        emailNotificationDetails.add(element(serviceOfApplicationEmailService
                                             .sendEmailUsingTemplateWithAttachments(authorization,
                                                selectedRespondent.getValue().getSolicitorEmail(), docs,
                                                SendgridEmailTemplateNames.SOA_CA_NON_PERSONAL_SERVICE_RESPONDENT_LIP,
                                                dynamicData, SERVED_PARTY_RESPONDENT)));
                    } catch (Exception e) {
                        log.error("Failed to send email to respondent solicitor {}", e.getMessage());
                    }
                } else {
                    log.info("Access to be granted");
                    Document coverLetter = generateCoverLetterBasedOnCaseAccess(authorization, caseData,
                                                                                selectedRespondent, Templates.PRL_LET_ENG_RE5);
                    sendPostWithAccessCodeLetterToParty(caseData, authorization,
                                                        isStaticDocs ? getNotificationPack(caseData, PrlAppsConstants.S, docs) : docs,
                                                        bulkPrintDetails, selectedRespondent, coverLetter,
                                                        SERVED_PARTY_RESPONDENT);
                }
            }
        });
        return emailNotificationDetails;
    }

    private boolean isAccessEnabled(Element<PartyDetails> party) {
        return party.getValue() != null && party.getValue().getUser() != null
            && party.getValue().getUser().getIdamId() != null;
    }

    private EmailNotificationDetails sendEmailCaPersonalApplicantLegalRep(CaseData caseData, String authorization,
                                                                          List<Document> packHiDocs) {
        EmailNotificationDetails emailNotification = null;
        if (caseData.getApplicants().get(0).getValue().getSolicitorEmail() != null) {
            try {
                log.info(
                    "Sending the email notification to applicant solicitor for C100 Application for caseId {}",
                    caseData.getId()
                );
                //Respondent's pack
                Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
                dynamicData.put("name", caseData.getApplicants().get(0).getValue().getRepresentativeFullName());
                dynamicData.put("c100", true);
                dynamicData.put(DASH_BOARD_LINK, manageCaseUrl + PrlAppsConstants.URL_STRING + caseData.getId());
                populateLanguageMap(caseData, dynamicData);
                emailNotification = serviceOfApplicationEmailService.sendEmailUsingTemplateWithAttachments(
                    authorization,
                    caseData.getApplicants().get(0).getValue().getSolicitorEmail(),
                    packHiDocs,
                    SendgridEmailTemplateNames.SOA_PERSONAL_CA_DA_APPLICANT_LEGAL_REP,
                    dynamicData,
                    SERVED_PARTY_APPLICANT_SOLICITOR
                );
                if (null != emailNotification) {
                    return emailNotification;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return emailNotification;
    }

    private EmailNotificationDetails sendEmailDaPersonalApplicantLegalRep(CaseData caseData,
                                                                          String authorization, List<Document> packA,
                                                                          List<Document> packB,
                                                                          boolean attachLetters) {
        PartyDetails applicant = caseData.getApplicantsFL401();
        EmailNotificationDetails emailNotification = null;
        if (applicant.getSolicitorEmail() != null) {
            try {
                log.info(
                    "Sending the email notification to applicant solicitor for FL401 Application for caseId {}",
                    caseData.getId()
                );
                //Respondent's pack
                Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
                dynamicData.put("name", caseData.getApplicantsFL401().getRepresentativeFullName());
                dynamicData.put(DASH_BOARD_LINK, manageCaseUrl + PrlAppsConstants.URL_STRING + caseData.getId());
                populateLanguageMap(caseData, dynamicData);
                List<Document> finalDocumentList = new ArrayList<>(
                    getCoverLettersAndRespondentPacksForDaApplicantSolicitor(caseData, authorization,
                                                                             packA, packB, attachLetters
                    ));
                emailNotification = serviceOfApplicationEmailService.sendEmailUsingTemplateWithAttachments(
                    authorization,
                    caseData.getApplicantsFL401().getSolicitorEmail(),
                    finalDocumentList,
                    SendgridEmailTemplateNames.SOA_PERSONAL_CA_DA_APPLICANT_LEGAL_REP,
                    dynamicData,
                    SERVED_PARTY_APPLICANT_SOLICITOR
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return emailNotification;
    }

    private List<DynamicMultiselectListElement> getSelectedApplicantsOrRespondents(List<Element<PartyDetails>> applicantsOrRespondents,
                                                                                   List<DynamicMultiselectListElement> value) {
        return value.stream().filter(element -> applicantsOrRespondents.stream().anyMatch(party -> party.getId().toString().equals(
            element.getCode()))).collect(
            Collectors.toList());
    }

    public List<Element<EmailNotificationDetails>> sendNotificationToApplicantSolicitor(CaseData caseData, String authorization,
                                                                                        List<DynamicMultiselectListElement> selectedApplicants,
                                                                                        List<Document> packQ, String servedParty) {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        List<Element<PartyDetails>> applicantsInCase;
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            applicantsInCase = caseData.getApplicants();
        } else {
            applicantsInCase = List.of(Element.<PartyDetails>builder()
                                           .id(caseData.getApplicantsFL401().getPartyId())
                                           .value(caseData.getApplicantsFL401()).build());
        }
        selectedApplicants.forEach(applicant -> {
            Optional<Element<PartyDetails>> party = getParty(applicant.getCode(), applicantsInCase);
            if (party.isPresent() && party.get().getValue().getSolicitorEmail() != null) {
                try {
                    log.info(
                        "Sending the email notification to applicant solicitor for C100 Application for caseId {}",
                        caseData.getId()
                    );
                    Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
                    dynamicData.put("name", party.get().getValue().getRepresentativeFullName());
                    dynamicData.put(DASH_BOARD_LINK, manageCaseUrl + PrlAppsConstants.URL_STRING + caseData.getId());
                    populateLanguageMap(caseData, dynamicData);
                    EmailNotificationDetails emailNotification = serviceOfApplicationEmailService
                        .sendEmailUsingTemplateWithAttachments(
                            authorization, party.get().getValue().getSolicitorEmail(),
                            packQ,
                            SendgridEmailTemplateNames.SOA_SERVE_APPLICANT_SOLICITOR_NONPER_PER_CA_CB,
                            dynamicData,
                            servedParty
                        );
                    if (null != emailNotification) {
                        emailNotificationDetails.add(element(emailNotification));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return emailNotificationDetails;
    }

    private void sendPostWithAccessCodeLetterToParty(CaseData caseData, String authorization, List<Document> packDocs,
                                                     List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                     Element<PartyDetails> party, Document coverLetter,
                                                     String servedParty) {

        try {
            List<Document> docs = new ArrayList<>(serviceOfApplicationPostService
                                                      .getCoverSheets(caseData, authorization,
                                                                      party.getValue().getAddress(),
                                                                      party.getValue().getLabelForDynamicList(),
                                                                      DOCUMENT_COVER_SHEET_HINT
                                                      ));
            docs.add(coverLetter);
            docs.addAll(packDocs);
            bulkPrintDetails.add(element(serviceOfApplicationPostService.sendPostNotificationToParty(
                caseData,
                authorization,
                party,
                docs,
                servedParty
            )));
        } catch (Exception e) {
            log.info("error while generating coversheet {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Optional<Element<PartyDetails>> getParty(String code, List<Element<PartyDetails>> parties) {
        Optional<Element<PartyDetails>> party = Optional.empty();
        if (CollectionUtils.isNotEmpty(parties)) {
            party = parties.stream()
                .filter(element -> code.equalsIgnoreCase(String.valueOf(element.getId()))).findFirst();
        }
        return party;
    }

    public List<Document> getNotificationPack(CaseData caseData, String requiredPack, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        switch (requiredPack) {
            case PrlAppsConstants.A -> docs.addAll(generatePackA(caseData, staticDocs));
            case PrlAppsConstants.B -> docs.addAll(generatePackB(caseData, staticDocs));
            case PrlAppsConstants.C -> docs.addAll(generatePackC(caseData, staticDocs));
            case PrlAppsConstants.D -> docs.addAll(generatePackD(caseData, staticDocs));
            case PrlAppsConstants.E -> docs.addAll(generatePackE(caseData, staticDocs));
            case PrlAppsConstants.F -> docs.addAll(generatePackF(caseData, staticDocs));
            case PrlAppsConstants.H -> docs.addAll(generatePackH(caseData, staticDocs));
            case PrlAppsConstants.I -> docs.addAll(generatePackI(caseData, staticDocs));
            case PrlAppsConstants.J -> docs.addAll(generatePackJ(caseData, staticDocs));
            case PrlAppsConstants.K -> docs.addAll(generatePackK(caseData, staticDocs));
            case PrlAppsConstants.L -> docs.addAll(generatePackL(caseData, staticDocs));
            case PrlAppsConstants.M -> docs.addAll(generatePackM(caseData, staticDocs));
            case PrlAppsConstants.N -> docs.addAll(generatePackN(caseData, staticDocs));
            case PrlAppsConstants.O -> docs.addAll(generatePackO(caseData));
            case PrlAppsConstants.P -> docs.addAll(generatePackP(caseData, staticDocs));
            case PrlAppsConstants.Q -> docs.addAll(generatePackQ(caseData, staticDocs));
            case PrlAppsConstants.R -> docs.addAll(generatePackR(caseData, staticDocs));
            case PrlAppsConstants.S -> docs.addAll(generatePackS(caseData, staticDocs));
            case PrlAppsConstants.HI -> docs.addAll(generatePackHI(caseData, staticDocs));
            case PrlAppsConstants.Z -> //not present in miro, added this by comparing to DA other org pack,confirm with PO's
                docs.addAll(generatePackZ(caseData));
            default -> log.info("No Letter selected");
        }
        return docs;

    }

    private List<Document> generatePackJ(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(staticDocs.stream()
                        .filter(d -> !(d.getDocumentFileName().equalsIgnoreCase(C1A_BLANK_DOCUMENT_FILENAME)
                            || d.getDocumentFileName().equalsIgnoreCase(C1A_BLANK_DOCUMENT_WELSH_FILENAME)
                            || d.getDocumentFileName().equalsIgnoreCase(C7_BLANK_DOCUMENT_FILENAME)
                            || d.getDocumentFileName().equalsIgnoreCase(SOA_NOTICE_SAFETY)
                        )).toList());
        docs.addAll(getSoaSelectedOrders(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        return docs;
    }

    private List<Document> generatePackK(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(staticDocs.stream()
                        .filter(d -> !(d.getDocumentFileName().equalsIgnoreCase(SOA_C9_PERSONAL_SERVICE_FILENAME)
                            || d.getDocumentFileName().equalsIgnoreCase(SOA_NOTICE_SAFETY))
                        ).toList());
        docs.addAll(getSoaSelectedOrders(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        return docs;
    }

    private List<Document> generatePackL(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        // Annex Z to be excluded
        docs.addAll(staticDocs.stream()
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(
                            C1A_BLANK_DOCUMENT_FILENAME))
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(
                            C1A_BLANK_DOCUMENT_WELSH_FILENAME))
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(
                            C7_BLANK_DOCUMENT_FILENAME)).toList());
        return docs;
    }

    private List<Document> generatePackM(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        // Annex Y to be excluded
        docs.addAll(staticDocs);
        docs = docs.stream()
                .filter(d -> !(d.getDocumentFileName().equalsIgnoreCase(SOA_C9_PERSONAL_SERVICE_FILENAME)
                || d.getDocumentFileName().equalsIgnoreCase(PrlAppsConstants.C1A_DOCUMENT_FILENAME)
            || d.getDocumentFileName().equalsIgnoreCase(PrlAppsConstants.C1A_DOCUMENT_WELSH_FILENAME)))
                .toList();
        return docs;
    }

    private List<Document> generatePackZ(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        return docs;
    }

    private List<Document> generatePackHI(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(staticDocs.stream()
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_NOTICE_SAFETY))
                        .toList());
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        return docs;
    }

    private List<Document> generatePackN(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>(getC6aIfPresent(getSoaSelectedOrders(caseData)));
        docs.addAll(staticDocs.stream()
                        .filter(d -> d.getDocumentFileName().equalsIgnoreCase(PRIVACY_DOCUMENT_FILENAME)
                        || d.getDocumentFileName().equalsIgnoreCase(PRIVACY_DOCUMENT_FILENAME_WELSH))
                        .toList());
        return docs;
    }

    public List<Document> getC6aIfPresent(List<Document> soaSelectedOrders) {
        return soaSelectedOrders.stream().filter(d -> d.getDocumentFileName().equalsIgnoreCase(
            SOA_C6A_OTHER_PARTIES_ORDER) || d.getDocumentFileName().equalsIgnoreCase(
            SOA_C6A_OTHER_PARTIES_ORDER_WELSH)).collect(Collectors.toList());
    }

    private List<Document> getNonC6aOrders(List<Document> soaSelectedOrders) {
        return soaSelectedOrders.stream().filter(d -> !(d.getDocumentFileName().equalsIgnoreCase(
            SOA_C6A_OTHER_PARTIES_ORDER) || d.getDocumentFileName().equalsIgnoreCase(
            SOA_C6A_OTHER_PARTIES_ORDER_WELSH))).collect(Collectors.toList());
    }

    private List<Document> generatePackH(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(staticDocs.stream()
                        .filter(d -> !(d.getDocumentFileName().equalsIgnoreCase(SOA_NOTICE_SAFETY)
                            || d.getDocumentFileName().equalsIgnoreCase(C1A_BLANK_DOCUMENT_FILENAME)
                            || d.getDocumentFileName().equalsIgnoreCase(C1A_BLANK_DOCUMENT_WELSH_FILENAME)
                            || d.getDocumentFileName().equalsIgnoreCase(C7_BLANK_DOCUMENT_FILENAME)))
                        .toList());
        docs.addAll(getSoaSelectedOrders(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        return docs;
    }

    private List<Document> generatePackI(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(staticDocs.stream()
                        .filter(d -> !(d.getDocumentFileName().equalsIgnoreCase(SOA_NOTICE_SAFETY)
                        || d.getDocumentFileName().equalsIgnoreCase(C9_DOCUMENT_FILENAME)))
                        .toList());
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        return docs;
    }

    private List<Document> generatePackP(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        docs.addAll(staticDocs.stream()
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_C9_PERSONAL_SERVICE_FILENAME)).toList());
        return docs;
    }

    private List<Document> generatePackQ(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        docs.addAll(staticDocs.stream()
                        .filter(d -> !(d.getDocumentFileName().equalsIgnoreCase(
                            C1A_BLANK_DOCUMENT_FILENAME) || d.getDocumentFileName().equalsIgnoreCase(SOA_NOTICE_SAFETY)))
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(
                            C1A_BLANK_DOCUMENT_WELSH_FILENAME))
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(
                            C7_BLANK_DOCUMENT_FILENAME)).toList());
        return docs;
    }

    private List<Document> generatePackS(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        docs.addAll(staticDocs);
        return docs;
    }

    private List<Document> generatePackR(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        docs.addAll(staticDocs);
        return docs;
    }

    private List<Document> generatePackA(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getWitnessStatement(caseData));
        docs.addAll(staticDocs);
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        return docs;
    }

    private List<Document> getWitnessStatement(CaseData caseData) {
        return !CollectionUtils.isEmpty(caseData.getFl401UploadWitnessDocuments()) ? ElementUtils.unwrapElements(
            caseData.getFl401UploadWitnessDocuments()) : new ArrayList<>();
    }

    private List<Document> generatePackB(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getWitnessStatement(caseData));
        docs.addAll(staticDocs.stream()
                                    .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_FL415_FILENAME)).toList());
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        return docs;
    }

    private List<Document> generatePackC(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getWitnessStatement(caseData));
        docs.addAll(staticDocs.stream()
                                    .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_FL415_FILENAME)).toList());
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        return docs;
    }

    private List<Document> generatePackD(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getWitnessStatement(caseData));
        docs.addAll(staticDocs);
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        return docs;
    }

    private List<Document> generatePackE(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getWitnessStatement(caseData));
        docs.addAll(staticDocs);
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        return docs;
    }

    private List<Document> generatePackF(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(staticDocs.stream()
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_FL415_FILENAME)).toList());
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        return docs;
    }

    private List<Document> generatePackO(CaseData caseData) {
        List<Document> docs = new ArrayList<>(getCaseDocs(caseData));
        docs.add(caseData.getC8Document());
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        return docs;
    }

    private List<Document> getCaseDocs(CaseData caseData) {
        //Welsh pack generation needs to be reviewed
        List<Document> docs = new ArrayList<>();
        if (CaseUtils.getCaseTypeOfApplication(caseData).equalsIgnoreCase(PrlAppsConstants.C100_CASE_TYPE)) {
            DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
            if (documentLanguage.isGenEng() && null != caseData.getFinalDocument()) {
                docs.add(caseData.getFinalDocument());
            }
            if (documentLanguage.isGenWelsh() && null != caseData.getFinalWelshDocument()) {
                docs.add(caseData.getFinalWelshDocument());
            }
            if (documentLanguage.isGenEng() && null != caseData.getC1ADocument()) {
                docs.add(caseData.getC1ADocument());
            }
            if (documentLanguage.isGenWelsh() && null != caseData.getC1AWelshDocument()) {
                docs.add(caseData.getC1AWelshDocument());
            }
        } else {
            docs.add(caseData.getFinalDocument());
        }
        return docs;
    }

    private List<Document> getDocumentsUploadedInServiceOfApplication(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        Optional<Document> pd36qLetter = Optional.ofNullable(caseData.getServiceOfApplicationUploadDocs().getPd36qLetter());
        Optional<Document> specialArrangementLetter = Optional.ofNullable(caseData.getServiceOfApplicationUploadDocs()
                                                                              .getSpecialArrangementsLetter());

        Optional<Document> noticeOfSafetyFl401 = Optional.ofNullable(caseData.getServiceOfApplicationUploadDocs().getNoticeOfSafetySupportLetter());

        noticeOfSafetyFl401.ifPresent(docs::add);
        pd36qLetter.ifPresent(docs::add);
        specialArrangementLetter.ifPresent(docs::add);
        List<Document> additionalDocuments = ElementUtils.unwrapElements(caseData.getServiceOfApplicationUploadDocs()
                                                                             .getAdditionalDocumentsList());
        if (CollectionUtils.isNotEmpty(additionalDocuments)) {
            docs.addAll(additionalDocuments);
        }
        return docs;
    }

    private List<Document> getSoaSelectedOrders(CaseData caseData) {
        List<Document> selectedOrders = new ArrayList<>();
        if (null != caseData.getServiceOfApplicationScreen1()
            && null != caseData.getServiceOfApplicationScreen1().getValue()
            && !caseData.getServiceOfApplicationScreen1().getValue().isEmpty()) {
            DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
            List<String> orderCodes = caseData.getServiceOfApplicationScreen1()
                .getValue().stream().map(DynamicMultiselectListElement::getCode).toList();
            orderCodes.forEach(orderCode ->
                                   caseData.getOrderCollection().stream()
                                       .filter(order -> String.valueOf(order.getId()).equalsIgnoreCase(orderCode))
                                       .findFirst()
                                       .ifPresent(o -> {
                                           if (documentLanguage.isGenEng()) {
                                               selectedOrders.add(o.getValue().getOrderDocument());
                                           }
                                           if (documentLanguage.isGenWelsh()) {
                                               selectedOrders.add(o.getValue().getOrderDocumentWelsh());
                                           }
                                       }));
            return selectedOrders;
        }
        return Collections.emptyList();

    }

    public void cleanUpSoaSelections(Map<String, Object> caseDataUpdated) {
        List<String> soaFields = new ArrayList<>(List.of(
            "pd36qLetter",
            "specialArrangementsLetter",
            "additionalDocuments",
            "sentDocumentPlaceHolder",
            "soaApplicantsList",
            "soaRespondentsList",
            "soaOtherPeopleList",
            "soaCafcassEmailOptionChecked",
            "soaOtherEmailOptionChecked",
            "soaOtherEmailOptionChecked",
            "soaCafcassEmailAddressList",
            "soaOtherEmailAddressList",
            "coverPageAddress",
            "coverPagePartyName",
            "serviceOfApplicationScreen1",
            "soaPostalInformationDA",
            "soaEmailInformationDA",
            "soaDeliveryByOptionsDA",
            "soaServeOtherPartiesDA",
            "soaPostalInformationCA",
            "soaEmailInformationCA",
            "soaDeliveryByOptionsCA",
            "soaServeOtherPartiesCA",
            "soaOtherParties",
            "soaRecipientsOptions",
            "soaServingRespondentsOptionsDA",
            "soaServingRespondentsOptionsCA",
            "soaServeToRespondentOptions",
            "soaOtherPeoplePresentInCaseFlag",
            "soaIsOrderListEmpty",
            "noticeOfSafetySupportLetter",
            "additionalDocumentsList",
            "soaCafcassCymruEmail",
            "soaCafcassCymruServedOptions",
            "soaCafcassEmailId",
            "soaCafcassServedOptions",
            PROCEED_TO_SERVING,
            MISSING_ADDRESS_WARNING_TEXT,
            SOA_DOCUMENT_DYNAMIC_LIST_FOR_LA
        ));

        for (String field : soaFields) {
            if (caseDataUpdated.containsKey(field)) {
                caseDataUpdated.put(field, null);
            }
        }
    }

    public DynamicMultiSelectList getCombinedRecipients(CaseData caseData) {
        Map<String, List<DynamicMultiselectListElement>> applicantDetails = dynamicMultiSelectListService
            .getApplicantsMultiSelectList(caseData);
        List<DynamicMultiselectListElement> applicantRespondentList = new ArrayList<>();
        List<DynamicMultiselectListElement> applicantList = applicantDetails.get(APPLICANTS);
        if (applicantList != null) {
            applicantRespondentList.addAll(applicantList);
        }
        Map<String, List<DynamicMultiselectListElement>> respondentDetails = dynamicMultiSelectListService
            .getRespondentsMultiSelectList(caseData);
        List<DynamicMultiselectListElement> respondentList = respondentDetails.get("respondents");
        if (respondentList != null) {
            applicantRespondentList.addAll(respondentList);
        }

        return DynamicMultiSelectList.builder()
            .listItems(applicantRespondentList)
            .build();
    }

    public YesOrNo getCafcass(CaseData caseData) {
        if (CaseUtils.getCaseTypeOfApplication(caseData).equalsIgnoreCase(PrlAppsConstants.C100_CASE_TYPE)) {
            if (caseData.getIsCafcass() != null) {
                return caseData.getIsCafcass();
            } else if (caseData.getCaseManagementLocation() != null) {
                return CaseUtils.cafcassFlag(caseData.getCaseManagementLocation().getRegion());
            } else {
                return YesOrNo.No;
            }
        } else {
            return YesOrNo.No;
        }
    }

    public Map<String, Object> getSoaCaseFieldsMap(String authorisation, CaseDetails caseDetails) {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        List<DynamicMultiselectListElement> otherPeopleList = dynamicMultiSelectListService.getOtherPeopleMultiSelectList(
            caseData);
        String cafcassCymruEmailAddress = welshCourtEmail
            .populateCafcassCymruEmailInManageOrders(caseData);
        caseDataUpdated.put(SOA_RECIPIENT_OPTIONS, getCombinedRecipients(caseData));
        caseDataUpdated.put(SOA_OTHER_PARTIES, DynamicMultiSelectList.builder()
            .listItems(otherPeopleList)
            .build());
        caseDataUpdated.put(SOA_OTHER_PEOPLE_PRESENT_IN_CASE, CollectionUtils.isNotEmpty(otherPeopleList) ? YesOrNo.Yes : YesOrNo.No);
        caseDataUpdated.put(SOA_CYMRU_EMAIL, cafcassCymruEmailAddress);
        caseDataUpdated.put(
            SOA_APPLICATION_SCREEN_1,
            dynamicMultiSelectListService.getOrdersAsDynamicMultiSelectList(caseData)
        );
        caseDataUpdated.put(
            IS_CAFCASS,
            getCafcass(caseData)
        );
        caseDataUpdated.put(
            SOA_ORDER_LIST_EMPTY,
            CollectionUtils.isEmpty(caseData.getOrderCollection()) ? YesOrNo.Yes : YesOrNo.No
        );
        caseDataUpdated.put(
            SOA_DOCUMENT_PLACE_HOLDER,
            C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
                ? getCollapsableOfSentDocuments()
                : getCollapsableOfSentDocumentsFL401()
        );
        caseDataUpdated.put(SOA_CONFIDENTIAL_DETAILS_PRESENT, isRespondentDetailsConfidential(caseData)
            || CaseUtils.isC8Present(caseData) ? Yes : No);
        caseDataUpdated.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        if (C100_CASE_TYPE.equalsIgnoreCase(String.valueOf(caseDataUpdated.get(CASE_TYPE_OF_APPLICATION)))) {
            caseDataUpdated.put(SOA_DOCUMENT_DYNAMIC_LIST_FOR_LA, getDocumentsDynamicListForLa(authorisation,
                                                                                               String.valueOf(caseData.getId())));
            caseDataUpdated.put(SOA_CAFCASS_CYMRU_SERVED_OPTIONS, Yes);
        }
        caseDataUpdated.put(CASE_CREATED_BY, CaseUtils.isCaseCreatedByCitizen(caseData) ? SOA_CITIZEN : SOA_SOLICITOR);
        caseDataUpdated.put(
            MISSING_ADDRESS_WARNING_TEXT,
            checkIfPostalAddressMissedForRespondentAndOtherParties(caseData)
        );
        return caseDataUpdated;
    }

    private String checkIfPostalAddressMissedForRespondentAndOtherParties(CaseData caseData) {
        String warningText = BLANK_STRING;
        boolean isRespondentAddressPresent = true;
        boolean isOtherPeopleAddressPresent = true;
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            for (Element<PartyDetails> respondent : caseData.getRespondents()) {
                if (!isPartiesAddressPresent(respondent.getValue())) {
                    isRespondentAddressPresent = false;
                    break;
                }
            }
            if (CollectionUtils.isNotEmpty(caseData.getOtherPartyInTheCaseRevised())) {
                for (Element<PartyDetails> otherParty : caseData.getOtherPartyInTheCaseRevised()) {
                    if (!isPartiesAddressPresent(otherParty.getValue())) {
                        isOtherPeopleAddressPresent = false;
                        break;
                    }
                }
            } else if (CollectionUtils.isNotEmpty(caseData.getOthersToNotify())) {
                for (Element<PartyDetails> otherParty : caseData.getOthersToNotify()) {
                    if (!isPartiesAddressPresent(otherParty.getValue())) {
                        isOtherPeopleAddressPresent = false;
                        break;
                    }
                }
            }
        } else {
            isRespondentAddressPresent = isPartiesAddressPresent(caseData.getRespondentsFL401());
        }
        if (!isRespondentAddressPresent && !isOtherPeopleAddressPresent) {
            warningText = ADDRESS_MISSED_FOR_RESPONDENT_AND_OTHER_PARTIES;
        } else if (!isRespondentAddressPresent
            && C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            warningText = CA_ADDRESS_MISSED_FOR_RESPONDENT;
        } else if (!isRespondentAddressPresent
            && FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            warningText = DA_ADDRESS_MISSED_FOR_RESPONDENT;
        } else if (!isOtherPeopleAddressPresent) {
            warningText = ADDRESS_MISSED_FOR_OTHER_PARTIES;
        }
        return warningText;
    }

    private static boolean isPartiesAddressPresent(PartyDetails partyDetails) {
        return !No.equals(partyDetails.getIsCurrentAddressKnown())
            && !ObjectUtils.isEmpty(partyDetails.getAddress())
            && !StringUtils.isEmpty(partyDetails.getAddress().getAddressLine1());
    }

    private boolean isRespondentDetailsConfidential(CaseData caseData) {
        if (PrlAppsConstants.C100_CASE_TYPE.equals(CaseUtils.getCaseTypeOfApplication(caseData))) {
            return validateRespondentConfidentialDetailsCA(caseData);
        } else {
            return validateRespondentConfidentialDetailsDA(caseData);
        }
    }

    private boolean validateRespondentConfidentialDetailsDA(CaseData caseData) {
        // Checking the Respondent Details..
        Optional<PartyDetails> flRespondents = ofNullable(caseData.getRespondentsFL401());
        if (flRespondents.isPresent()) {
            PartyDetails partyDetails = flRespondents.get();
            return YesOrNo.Yes.equals(partyDetails.getIsAddressConfidential())
                || YesOrNo.Yes.equals(partyDetails.getIsPhoneNumberConfidential())
                || YesOrNo.Yes.equals(partyDetails.getIsEmailAddressConfidential());
        }
        return false;
    }

    private boolean validateRespondentConfidentialDetailsCA(CaseData caseData) {
        // Checking the Respondent Details..
        Optional<List<Element<PartyDetails>>> respondentsWrapped = ofNullable(caseData.getRespondents());

        if (respondentsWrapped.isPresent() && !respondentsWrapped.get().isEmpty()) {
            List<PartyDetails> respondents = respondentsWrapped.get()
                .stream()
                .map(Element::getValue).toList();

            for (PartyDetails respondent : respondents) {
                if (YesOrNo.Yes.equals(respondent.getIsAddressConfidential())
                    || YesOrNo.Yes.equals(respondent.getIsPhoneNumberConfidential())
                    || YesOrNo.Yes.equals(respondent.getIsEmailAddressConfidential())) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Element<DocumentListForLa>> getDocumentsDynamicListForLa(String authorisation, String caseId) {
        DynamicList categoriesAdnDocumentsList = sendAndReplyService.getCategoriesAndDocuments(authorisation, caseId);
        if (CollectionUtils.isNotEmpty(categoriesAdnDocumentsList.getListItems())) {
            categoriesAdnDocumentsList.getListItems().removeIf(dynamicListElement -> dynamicListElement.getLabel().contains("Confidential"));
        }
        return List.of(Element.<DocumentListForLa>builder().id(UUID.randomUUID()).value(DocumentListForLa.builder()
                                                                                      .documentsListForLa(categoriesAdnDocumentsList)
                                                                                      .build()).build());
    }

    public String getCollapsableOfSentDocumentsFL401() {
        final List<String> collapsible = new ArrayList<>();
        collapsible.add("<details class='govuk-details'>");
        collapsible.add("<summary class='govuk-details__summary'>");
        collapsible.add("<h3 class='govuk-details__summary-text'>");
        collapsible.add("Documents served in the pack");
        collapsible.add("</h3>");
        collapsible.add("</summary>");
        collapsible.add("<div class='govuk-details__text'>");
        collapsible.add(
            "Certain documents will be automatically included in the pack that is sent out on parties (the people in the case).");
        collapsible.add(
            "This includes:");
        collapsible.add(
            "<ul><li>an application form (FL401)</li>"
                + "<li>witness statement</li><li>privacy notice</li><li>cover letter (if not represented)</li></ul>");
        collapsible.add(
            "You do not need to upload these documents yourself.");
        collapsible.add("</div>");
        collapsible.add("</details>");
        return String.join("\n\n", collapsible);
    }

    public Document generateAccessCodeLetter(String authorisation, CaseData caseData,Element<PartyDetails> party,
                                      CaseInvite caseInvite, String template) {

        Map<String, Object> dataMap = populateAccessCodeMap(caseData, party, caseInvite);
        return fetchCoverLetter(authorisation, template, dataMap);
    }

    private Document fetchCoverLetter(String authorisation, String template, Map<String, Object> dataMap) {
        try {
            log.info("generating letter : {} for case : {}", template, dataMap.get("id"));
            GeneratedDocumentInfo accessCodeLetter = dgsService.generateDocument(
                authorisation,
                String.valueOf(dataMap.get("id")),
                template,
                dataMap
            );
            return Document.builder().documentUrl(accessCodeLetter.getUrl())
                .documentFileName(accessCodeLetter.getDocName()).documentBinaryUrl(accessCodeLetter.getBinaryUrl())
                .documentCreatedOn(new Date())
                .build();
        } catch (Exception e) {
            log.error("*** Access code letter failed for {} :: because of {}", template, e.getMessage());
        }
        return null;
    }

    public Map<String, Object> populateAccessCodeMap(CaseData caseData, Element<PartyDetails> party, CaseInvite caseInvite) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", caseData.getId());
        dataMap.put("serviceUrl", citizenUrl);
        dataMap.put("address", party.getValue().getAddress());
        dataMap.put("name", party.getValue().getFirstName() + " " + party.getValue().getLastName());
        dataMap.put("accessCode", getAccessCode(caseInvite, party.getValue().getAddress(), party.getValue().getLabelForDynamicList()));
        dataMap.put("c1aExists", doesC1aExists(caseData));
        if (FL401_CASE_TYPE.equals(CaseUtils.getCaseTypeOfApplication(caseData))) {
            dataMap.put(DA_APPLICANT_NAME, caseData.getApplicantsFL401().getLabelForDynamicList());
        }
        dataMap.put("isCitizen", CaseUtils.isCaseCreatedByCitizen(caseData));
        return dataMap;
    }

    private AccessCode getAccessCode(CaseInvite caseInvite, Address address, String name) {
        String code = null;
        String isLinked = null;
        if (null != caseInvite) {
            code = caseInvite.getAccessCode();
            isLinked = caseInvite.getHasLinked();
        }
        return AccessCode.builder()
            .code(code)
            .recipientName(name)
            .address(address)
            .isLinked(isLinked)
            .currentDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
            .respondByDate(LocalDate.now().plusDays(14).format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
            .build();
    }

    private YesOrNo doesC1aExists(CaseData caseData) {
        if (caseData.getC1AWelshDocument() != null || caseData.getC1ADocument() != null) {
            return Yes;
        }
        return No;
    }

    private CaseInvite getCaseInvite(UUID partyId, List<Element<CaseInvite>> caseInvites) {
        if (CollectionUtils.isNotEmpty(caseInvites)) {
            Optional<Element<CaseInvite>> caseInvite = caseInvites.stream()
                .filter(caseInviteElement -> caseInviteElement.getValue().getPartyId().equals(partyId)
            ).findFirst();
            if (caseInvite.isPresent()) {
                return caseInvite.map(Element::getValue).orElse(null);
            }
        }
        return null;
    }

    private List<Element<CaseInvite>> generateCaseInvitesForParties(CaseData caseData) {
        List<Element<CaseInvite>> caseInvites = caseData.getCaseInvites();
        if (CollectionUtils.isEmpty(caseInvites)) {
            caseInvites =  new ArrayList<>();
            if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                List<Element<CaseInvite>> finalCaseInvites = caseInvites;
                //PRL-3466
                caseData.getApplicants().stream()
                    .filter(party -> Yes.equals(party.getValue().getIsAccessCodeNeeded()))
                    .forEach(party -> finalCaseInvites.add(element(c100CaseInviteService.generateCaseInvite(party, Yes))));
                caseData.getRespondents().forEach(party -> finalCaseInvites.add(element(c100CaseInviteService.generateCaseInvite(party, No))));
                return finalCaseInvites;
            } else {
                caseInvites.add(element(fl401CaseInviteService.generateCaseInvite(caseData.getApplicantsFL401(), Yes)));
                caseInvites.add(element(fl401CaseInviteService.generateCaseInvite(caseData.getRespondentsFL401(), No)));
                return caseInvites;
            }
        }
        return caseInvites;
    }

    public Map<String, Object> generatePacksForConfidentialCheckC100(CaseDetails caseDetails, String authorization) {
        log.info("Inside generatePacks for confidential check C100 method");
        Map<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        String dateCreated = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
            .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE)));
        List<Document> c100StaticDocs = serviceOfApplicationPostService.getStaticDocs(authorization,
                                                                                      CaseUtils.getCaseTypeOfApplication(caseData),
                                                                                      caseData);
        if (YesOrNo.No.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())
            && (caseData.getServiceOfApplication().getSoaRecipientsOptions() != null)
            && (!caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue().isEmpty())) {
            c100StaticDocs = buildPacksConfidentialCheckC100NonPersonal(authorization, caseDataUpdated, caseData, dateCreated, c100StaticDocs);
        } else if (YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())) {
            buildPacksConfidentialCheckC100Personal(authorization, caseDataUpdated, caseData, dateCreated, c100StaticDocs);
        }
        //serving other people in the case
        if (null != caseData.getServiceOfApplication().getSoaOtherParties()
            && !caseData.getServiceOfApplication().getSoaOtherParties().getValue().isEmpty()) {
            buildUnservedOthersPack(authorization, caseDataUpdated, caseData, dateCreated, c100StaticDocs);
        } else {
            caseDataUpdated.put(UNSERVED_OTHERS_PACK, null);
        }

        //generate packs for Cafcass cymru
        if (YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaCafcassCymruServedOptions())
            && StringUtils.isNotEmpty(caseData.getServiceOfApplication().getSoaCafcassCymruEmail())) {
            caseDataUpdated.put(UNSERVED_CAFCASS_CYMRU_PACK, SoaPack.builder()
                    .partyIds(List.of(element(caseData.getServiceOfApplication().getSoaCafcassCymruEmail())))
                .build());
        }

        //serving Local authority in the case
        if (YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaServeLocalAuthorityYesOrNo())
            && null != caseData.getServiceOfApplication().getSoaLaEmailAddress()) {
            List<Document> docsForLa = getDocsToBeServedToLa(authorization, caseData);
            if (CollectionUtils.isNotEmpty(docsForLa)) {
                caseDataUpdated.put(UNSERVED_LA_PACK, SoaPack.builder().packDocument(wrapElements(docsForLa))
                    .servedBy(userService.getUserDetails(authorization).getFullName())
                    .packCreatedDate(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
                                         .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE))))
                    .partyIds(List.of(element(caseData.getServiceOfApplication().getSoaLaEmailAddress())))
                    .build());
            }
        } else {
            caseDataUpdated.put(UNSERVED_LA_PACK, null);
        }
        return caseDataUpdated;
    }

    private void buildPacksConfidentialCheckC100Personal(String authorization,
                                                         Map<String, Object> caseDataUpdated,
                                                         CaseData caseData, String dateCreated,
                                                         List<Document> c100StaticDocs) {
        if (SoaSolicitorServingRespondentsEnum.courtAdmin
            .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA())
            || SoaSolicitorServingRespondentsEnum.courtBailiff
            .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA())) {
            generateUnServedPacksForCourtAdminBailiff(authorization, caseDataUpdated, caseData, c100StaticDocs, false,
                                                      caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA().toString());
        } else if (SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative
            .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA())) {
            List<Document> packIDocs = new ArrayList<>();
            caseData.getRespondents().forEach(respondent ->
                                                  packIDocs.add(generateAccessCodeLetter(
                                                      authorization,
                                                      caseData, respondent, null, PRL_LET_ENG_C100_RE6
                                                  ))
            );
            List<Document> packHDocs = new ArrayList<>(getNotificationPack(
                caseData,
                PrlAppsConstants.H,
                c100StaticDocs
            ));
            packIDocs.addAll(getNotificationPack(caseData, PrlAppsConstants.I, c100StaticDocs));
            final SoaPack unservedRespondentPack = SoaPack.builder()
                .packDocument(wrapElements(packIDocs))
                .partyIds(wrapElements(caseData.getApplicants().get(0).getId().toString()))
                .servedBy(SERVED_PARTY_APPLICANT_SOLICITOR)
                .personalServiceBy(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative.toString())
                .packCreatedDate(dateCreated)
                .build();
            caseDataUpdated.put(UNSERVED_RESPONDENT_PACK, unservedRespondentPack);
            final SoaPack unServedApplicantPack = SoaPack.builder()
                .packDocument(wrapElements(packHDocs))
                .partyIds(CaseUtils.getPartyIdList(caseData.getRespondents()))
                .servedBy(SERVED_PARTY_APPLICANT_SOLICITOR)
                .personalServiceBy(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative.toString())
                .packCreatedDate(dateCreated)
                .build();
            caseDataUpdated.put(UNSERVED_APPLICANT_PACK, unServedApplicantPack);
        } else if (SoaCitizenServingRespondentsEnum.unrepresentedApplicant
            .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsCA())) {
            caseDataUpdated.put(UNSERVED_APPLICANT_PACK, generatePacksForApplicantLipC100Personal(authorization, caseData,
                                                                                                  dateCreated, c100StaticDocs));
            caseDataUpdated.put(UNSERVED_RESPONDENT_PACK, SoaPack.builder()
                .packDocument(wrapElements(getNotificationPack(caseData, PrlAppsConstants.M, c100StaticDocs)))
                .partyIds(CaseUtils.getPartyIdList(caseData.getRespondents()))
                .servedBy(UNREPRESENTED_APPLICANT)
                .personalServiceBy(SoaCitizenServingRespondentsEnum.unrepresentedApplicant.toString())
                .packCreatedDate(dateCreated)
                .build());
        } else if (SoaCitizenServingRespondentsEnum.courtAdmin
            .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsCA())
            || SoaCitizenServingRespondentsEnum.courtBailiff
            .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsCA())) {
            generateUnServedPacksForCourtAdminBailiff(authorization, caseDataUpdated, caseData, c100StaticDocs, true,
                                                      caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsCA().toString());
        }
    }

    private void generateUnServedPacksForCourtAdminBailiff(String authorization,
                                                           Map<String, Object> caseDataUpdated,
                                                           CaseData caseData,
                                                           List<Document> c100StaticDocs,
                                                           boolean isCitizen,
                                                           String serviceBy) {
        List<Document> packjDocs;
        List<Document> packkDocs = generatePartiesPackDocsWithCoverLetter(authorization, caseData, caseData.getRespondents(),
                                                                                    PRL_LET_ENG_RE5, PrlAppsConstants.K, c100StaticDocs);
        if (isCitizen) {
            packjDocs = generatePartiesPackDocsWithCoverLetter(authorization, caseData, caseData.getApplicants(),
                                                               PRL_LET_ENG_AP8, PrlAppsConstants.J, c100StaticDocs);
        } else {
            packjDocs = getNotificationPack(caseData, PrlAppsConstants.J, c100StaticDocs);
        }
        final SoaPack unservedRespondentPack = SoaPack.builder()
            .packDocument(wrapElements(packkDocs))
            .partyIds(CaseUtils.getPartyIdList(caseData.getRespondents()))
            .servedBy(PRL_COURT_ADMIN)
            .personalServiceBy(serviceBy)
            .packCreatedDate(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
                                 .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE))))
            .build();
        caseDataUpdated.put(UNSERVED_RESPONDENT_PACK, unservedRespondentPack);
        final SoaPack unServedApplicantPack = SoaPack.builder()
            .packDocument(wrapElements(packjDocs))
            .partyIds(isCitizen
                          ? CaseUtils.getPartyIdList(caseData.getApplicants())
                          : wrapElements(caseData.getApplicants().get(0).getId().toString()))
            .servedBy(PRL_COURT_ADMIN)
            .personalServiceBy(serviceBy)
            .packCreatedDate(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
                                 .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE))))
            .build();
        caseDataUpdated.put(UNSERVED_APPLICANT_PACK, unServedApplicantPack);
    }

    private List<Document> generatePartiesPackDocsWithCoverLetter(String authorization,
                                                                  CaseData caseData,
                                                                  List<Element<PartyDetails>> parties,
                                                                  String template,
                                                                  String requiredPack,
                                                                  List<Document> c100StaticDocs) {
        List<Document> packDocs = new ArrayList<>();
        parties.forEach(party -> packDocs.add(generateCoverLetterBasedOnCaseAccess(authorization, caseData, party, template)));
        packDocs.addAll(getNotificationPack(caseData, requiredPack, c100StaticDocs));
        return packDocs;
    }

    private SoaPack generatePacksForApplicantLipC100Personal(String authorization, CaseData caseData, String dateCreated,
                                                             List<Document> c100StaticDocs) {
        List<Document> packLdocs = new ArrayList<>();
        caseData.getApplicants().forEach(applicant -> {
            if (!CaseUtils.hasLegalRepresentation(applicant.getValue())) {
                packLdocs.add(generateCoverLetterBasedOnCaseAccess(authorization, caseData,
                                                                   applicant, PRL_LET_ENG_AP7));
            }
        });
        packLdocs.addAll(getNotificationPack(caseData, L, c100StaticDocs));
        return SoaPack.builder()
            .packDocument(wrapElements(packLdocs))
            .partyIds(wrapElements(caseData.getApplicants().get(0).getId().toString()))
            .servedBy(UNREPRESENTED_APPLICANT)
            .personalServiceBy(SoaCitizenServingRespondentsEnum.unrepresentedApplicant.toString())
            .packCreatedDate(dateCreated)
            .build();
    }

    public Document generateCoverLetterBasedOnCaseAccess(String authorization,
                                                         CaseData caseData,
                                                         Element<PartyDetails> party,
                                                         String template) {
        Map<String, Object> dataMap;
        CaseInvite caseInvite = null;
        if (!isAccessEnabled(party) && !CaseUtils.hasLegalRepresentation(party.getValue())) {
            caseInvite = getCaseInvite(party.getId(), caseData.getCaseInvites());
        }
        dataMap = populateAccessCodeMap(caseData, party, caseInvite);
        return fetchCoverLetter(authorization, template, dataMap);
    }

    private List<Document> buildPacksConfidentialCheckC100NonPersonal(String authorization,
                                                                      Map<String, Object> caseDataUpdated,
                                                                      CaseData caseData, String dateCreated,
                                                                      List<Document> c100StaticDocs) {
        c100StaticDocs = c100StaticDocs.stream().filter(d -> ! d.getDocumentFileName().equalsIgnoreCase(
            C9_DOCUMENT_FILENAME)).collect(
            Collectors.toList());
        log.info("serving applicants or respondents");
        List<DynamicMultiselectListElement> selectedApplicants = getSelectedApplicantsOrRespondents(
            caseData.getApplicants(),
            caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue()
        );
        // Applicants pack
        if (CollectionUtils.isNotEmpty(selectedApplicants)) {
            buildUnservedApplicantPackC100NonPersonal(
                authorization,
                caseDataUpdated,
                caseData,
                dateCreated,
                c100StaticDocs, selectedApplicants);
        } else {
            caseDataUpdated.put(UNSERVED_APPLICANT_PACK, null);
        }
        // Respondent pack
        List<DynamicMultiselectListElement> selectedRespondents = getSelectedApplicantsOrRespondents(
            caseData.getRespondents(),
            caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue()
        );
        if (CollectionUtils.isNotEmpty(selectedRespondents)) {
            buildUnservedRespondentPackC100NonPersonal(
                authorization,
                caseDataUpdated,
                caseData,
                dateCreated,
                c100StaticDocs, selectedRespondents);
        } else {
            caseDataUpdated.put(UNSERVED_RESPONDENT_PACK, null);
        }
        return c100StaticDocs;
    }

    public Map<String, Object> generatePacksForConfidentialCheckFl401(CaseDetails caseDetails, String authorization) {
        log.info("Inside generatePacksForConfidentialCheck FL401 Method");
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        Map<String, Object> caseDataUpdated = new HashMap<>();
        String dateCreated = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
            .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE)));
        List<Document> fl401StaticDocs = serviceOfApplicationPostService.getStaticDocs(authorization,
                                                                                       CaseUtils.getCaseTypeOfApplication(caseData),
                                                                                       caseData);
        if (SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative
            .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA())) {
            caseDataUpdated.putAll(getPacksForConfidentialCheckDaApplicantSolicitor(authorization, caseData, dateCreated,
                                                                                    fl401StaticDocs));
        } else if (SoaSolicitorServingRespondentsEnum.courtAdmin
            .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA())
            || SoaSolicitorServingRespondentsEnum.courtBailiff
                .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA())) {
            getPacksForConfidentialCheckDaCourtAdminAndBailiff(caseData, caseDataUpdated, dateCreated, fl401StaticDocs,
                                                               authorization);
        } else if (SoaCitizenServingRespondentsEnum.courtBailiff
            .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsDA())
            || SoaCitizenServingRespondentsEnum.courtAdmin
            .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsDA())) {
            log.error("#SOA TO DO... Personal courtadmin / court bailiff - case created by - citizen");
        } else if (SoaCitizenServingRespondentsEnum.unrepresentedApplicant
            .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsDA())) {
            log.error("#SOA TO DO... Personal service unrepresented applicant- case created by - citizen/solicitor");
            getNotificationPack(caseData, PrlAppsConstants.E, fl401StaticDocs);
            getNotificationPack(caseData, PrlAppsConstants.F, fl401StaticDocs);
        }
        return caseDataUpdated;
    }

    private void getPacksForConfidentialCheckDaCourtAdminAndBailiff(CaseData caseData, Map<String, Object> caseDataUpdated,
                                                                    String dateCreated, List<Document> fl401StaticDocs,
                                                                    String authorization) {
        log.info("serving Fl401 court admin or court bailiff with confidential check");
        List<Document> packdDocs = getRespondentPacksForDaPersonaServiceByCourtAdminAndBailiff(
            caseData,
            authorization,
            fl401StaticDocs
        );
        final SoaPack unservedRespondentPack = SoaPack.builder().packDocument(wrapElements(packdDocs))
            .partyIds(wrapElements(caseData.getRespondentsFL401().getPartyId().toString()))
            .servedBy(PRL_COURT_ADMIN)
            .packCreatedDate(dateCreated)
            .personalServiceBy(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA().toString())
            .build();
        caseDataUpdated.put(UNSERVED_RESPONDENT_PACK, unservedRespondentPack);
        List<Document> packcDocs = getNotificationPack(caseData, PrlAppsConstants.C, fl401StaticDocs);
        final SoaPack unServedApplicantPack = SoaPack.builder()
            .packDocument(wrapElements(packcDocs))
            .partyIds(wrapElements(caseData.getApplicantsFL401().getPartyId().toString()))
            .servedBy(PRL_COURT_ADMIN)
            .packCreatedDate(dateCreated)
            .personalServiceBy(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA().toString())
            .build();
        caseDataUpdated.put(UNSERVED_APPLICANT_PACK, unServedApplicantPack);
    }

    private Map<String, Object> getPacksForConfidentialCheckDaApplicantSolicitor(String authorization, CaseData caseData, String dateCreated,
                                                                                 List<Document> fl401StaticDocs) {
        log.info("serving Fl401 applicant legal representative with confidential check");
        // Applicants pack
        Map<String, Object> caseDataUpdated = new HashMap<>();
        final String partyId = caseData.getApplicantsFL401().getPartyId().toString();
        List<Document> packADocs = getNotificationPack(caseData, PrlAppsConstants.A, fl401StaticDocs);
        final SoaPack unServedApplicantPack = SoaPack.builder().packDocument(wrapElements(packADocs))
            .partyIds(wrapElements(partyId))
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .packCreatedDate(dateCreated)
            .personalServiceBy(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA().toString())
            .build();
        caseDataUpdated.put(UNSERVED_APPLICANT_PACK, unServedApplicantPack);

        List<Document> packBDocs = getNotificationPack(caseData, PrlAppsConstants.B, fl401StaticDocs);
        List<Document> reLetters = new ArrayList<>();
        Element<PartyDetails> respondent = Element.<PartyDetails>builder()
            .id(caseData.getRespondentsFL401().getPartyId())
            .value(caseData.getRespondentsFL401())
            .build();
        getCoverLetterForDaApplicantSolicitor(caseData, authorization, reLetters, respondent);
        reLetters.addAll(packBDocs);
        final SoaPack unServedRespondentPack = SoaPack.builder().packDocument(wrapElements(reLetters)).partyIds(
                wrapElements(caseData.getRespondentsFL401().getPartyId().toString()))
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .personalServiceBy(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA().toString())
            .packCreatedDate(dateCreated)
            .build();
        caseDataUpdated.put(UNSERVED_RESPONDENT_PACK, unServedRespondentPack);
        return caseDataUpdated;
    }

    private void buildUnservedOthersPack(String authorization, Map<String, Object> caseDataUpdated, CaseData caseData, String dateCreated,
                                         List<Document> c100StaticDocs) {
        List<Element<PartyDetails>> otherPartiesToNotify = CaseUtils.getOthersToNotifyInCase(caseData);
        final List<DynamicMultiselectListElement> otherParties = getSelectedApplicantsOrRespondents(
            otherPartiesToNotify,
            caseData.getServiceOfApplication().getSoaOtherParties().getValue()
        );

        final List<String> othersPartyIds = otherParties.stream().map(DynamicMultiselectListElement::getCode).collect(
            Collectors.toList());
        List<Document> packNDocs = getNotificationPack(caseData, PrlAppsConstants.N, c100StaticDocs);
        final SoaPack unServedOthersPack = SoaPack.builder().packDocument(wrapElements(packNDocs))
            .partyIds(wrapElements(othersPartyIds))
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .packCreatedDate(dateCreated)
            .build();

        caseDataUpdated.put(UNSERVED_OTHERS_PACK, unServedOthersPack);
    }

    private void buildUnservedRespondentPackC100NonPersonal(String authorization, Map<String, Object> caseDataUpdated, CaseData caseData,
                                                            String dateCreated,
                                                            List<Document> c100StaticDocs, List<DynamicMultiselectListElement> selectedRespondents) {
        final List<String> selectedPartyIds = selectedRespondents.stream().map(DynamicMultiselectListElement::getCode).collect(
            Collectors.toList());
        List<Document> finalDocs = new ArrayList<>();
        selectedPartyIds.forEach(partyId -> {
            Optional<Element<PartyDetails>> party = getParty(partyId, caseData.getRespondents());
            if (party.isPresent() && !CaseUtils.hasLegalRepresentation(party.get().getValue())) {
                finalDocs.add(generateAccessCodeLetter(authorization, caseData, party.get(),
                                                       null, PRL_LET_ENG_RE5));
            }
        });
        finalDocs.addAll(getNotificationPack(caseData, PrlAppsConstants.R, c100StaticDocs));
        final SoaPack unServedRespondentPack = SoaPack.builder()
            .packDocument(wrapElements(finalDocs))
            .partyIds(wrapElements(selectedPartyIds))
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .packCreatedDate(dateCreated)
            .build();
        caseDataUpdated.put(UNSERVED_RESPONDENT_PACK, unServedRespondentPack);
    }

    private void buildUnservedApplicantPackC100NonPersonal(String authorization, Map<String, Object> caseDataUpdated, CaseData caseData,
                                                           String dateCreated,
                                                           List<Document> c100StaticDocs, List<DynamicMultiselectListElement> selectedApplicants) {
        final List<String> selectedPartyIds = selectedApplicants.stream().map(DynamicMultiselectListElement::getCode).collect(
            Collectors.toList());
        List<Element<Document>> packDocs = new ArrayList<>();
        if (CaseUtils.isCaseCreatedByCitizen(caseData)) {
            selectedPartyIds.forEach(partyId -> {
                Optional<Element<PartyDetails>> party = getParty(partyId, caseData.getApplicants());
                party.ifPresent(element -> packDocs.add(element(generateCoverLetterBasedOnCaseAccess(
                    authorization,
                    caseData,
                    element,
                    Templates.AP6_LETTER
                ))));
            });
            packDocs.addAll(wrapElements(getNotificationPack(caseData, PrlAppsConstants.P, c100StaticDocs)));
        } else {
            packDocs.addAll(wrapElements(getNotificationPack(caseData, PrlAppsConstants.Q, c100StaticDocs)));
        }
        final SoaPack unServedApplicantPack = SoaPack.builder().packDocument(packDocs).partyIds(
            wrapElements(selectedPartyIds))
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .packCreatedDate(dateCreated)
            .build();
        caseDataUpdated.put(UNSERVED_APPLICANT_PACK, unServedApplicantPack);
    }

    public CaseData sendNotificationsForUnServedPacks(CaseData caseData, String authorization) {
        //Suppressed java:S6541 , suppression will be removed after refactoring in the IP sprint.
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
        final SoaPack unServedApplicantPack = caseData.getServiceOfApplication().getUnServedApplicantPack();
        final SoaPack unServedRespondentPack = caseData.getServiceOfApplication().getUnServedRespondentPack();
        String whoIsResponsible = COURT;
        if (unServedApplicantPack != null || unServedRespondentPack != null) {
            if ((unServedApplicantPack != null
                && SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative.toString().equalsIgnoreCase(
                unServedApplicantPack.getPersonalServiceBy()))
                || (unServedRespondentPack != null
                && SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative.toString().equalsIgnoreCase(
                unServedRespondentPack.getPersonalServiceBy()))) {
                EmailNotificationDetails emailNotification = sendNotificationForApplicantLegalRepPersonalService(caseData,
                    authorization, unServedApplicantPack, unServedRespondentPack);
                if (emailNotification != null) {
                    emailNotificationDetails.add(element(emailNotification));
                }
                whoIsResponsible = SERVED_PARTY_APPLICANT_SOLICITOR;
            } else if (unServedApplicantPack != null
                && SoaCitizenServingRespondentsEnum.unrepresentedApplicant.toString().equalsIgnoreCase(
                unServedApplicantPack.getPersonalServiceBy())) {
                notifyC100ApplicantsPersonalServiceUnRepApplicant(authorization,
                                                                  caseData,
                                                                  emailNotificationDetails,
                                                                  bulkPrintDetails,
                                                                  removeCoverLettersFromThePacks(
                                                                      unwrapElements(unServedApplicantPack.getPackDocument())));
                whoIsResponsible = UNREPRESENTED_APPLICANT;
            } else {
                if (unServedApplicantPack != null) {
                    sendNotificationForUnservedApplicantPack(caseData, authorization, emailNotificationDetails,
                                                             unServedApplicantPack, bulkPrintDetails);
                    if (unServedApplicantPack.getPersonalServiceBy() != null) {
                        whoIsResponsible = SoaSolicitorServingRespondentsEnum.courtAdmin
                            .toString().equalsIgnoreCase(unServedApplicantPack.getPersonalServiceBy())
                            ? PERSONAL_SERVICE_SERVED_BY_CA : PERSONAL_SERVICE_SERVED_BY_BAILIFF;
                    }
                }
                if (unServedRespondentPack != null && null == unServedRespondentPack.getPersonalServiceBy()) {
                    final List<Element<String>> partyIds = unServedRespondentPack.getPartyIds();
                    final List<DynamicMultiselectListElement> respondentList = createPartyDynamicMultiSelectListElement(
                        partyIds);

                    final List<Document> respondentDocs = unwrapElements(unServedRespondentPack.getPackDocument());
                    if (CaseUtils.isCaseCreatedByCitizen(caseData)) {
                        sendNotificationsToCitizenRespondentsC100(authorization,
                                                                  respondentList,
                                                                  caseData,
                                                                  bulkPrintDetails,
                                                                  removeCoverLettersFromThePacks(respondentDocs),
                                                                  false
                        );
                    } else {
                        // Pack R and S only differ in acess code letter, Pack R - email, Pack S - Post
                        sendNotificationToRespondentOrSolicitorNonPersonal(caseData,
                                                                           authorization,
                                                                           emailNotificationDetails,
                                                                           bulkPrintDetails,
                                                                           respondentList,
                                                                           respondentDocs,
                                                                           respondentDocs
                        );
                    }

                }
            }
        }
        // send notification for others
        final SoaPack unServedOthersPack = caseData.getServiceOfApplication().getUnServedOthersPack();

        if (unServedOthersPack != null) {
            sendNotificationForOthersPack(caseData, authorization, bulkPrintDetails, unServedOthersPack);
        }

        // send notification for CafcassCymru
        final SoaPack unservedCymruPack = caseData.getServiceOfApplication().getUnServedCafcassCymruPack();

        if (ObjectUtils.isNotEmpty(unservedCymruPack) && CollectionUtils.isNotEmpty(unservedCymruPack.getPartyIds())) {
            emailNotificationDetails.addAll(sendEmailToCafcassInCase(
                caseData,
                unservedCymruPack.getPartyIds().get(0).getValue(),
                PrlAppsConstants.SERVED_PARTY_CAFCASS_CYMRU
            ));
        }
        //serving Local authority
        EmailNotificationDetails emailNotification = checkAndServeLocalAuthorityEmail(caseData, authorization);
        if (emailNotification != null) {
            emailNotificationDetails.add(element(emailNotification));
        }
        String formatter = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
            .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE)));
        List<Element<ServedApplicationDetails>> finalServedApplicationDetailsList;
        if (CollectionUtils.isNotEmpty(caseData.getFinalServedApplicationDetailsList())) {
            finalServedApplicationDetailsList = caseData.getFinalServedApplicationDetailsList();
        } else {
            finalServedApplicationDetailsList = new ArrayList<>();
        }
        finalServedApplicationDetailsList.add(element(ServedApplicationDetails.builder().emailNotificationDetails(
            emailNotificationDetails)
                                                          .servedBy(userService.getUserDetails(authorization).getFullName())
                                                          .servedAt(formatter)
                                                          .modeOfService(CaseUtils.getModeOfService(
                                                              emailNotificationDetails,
                                                              bulkPrintDetails
                                                          ))
                                                          .whoIsResponsible(whoIsResponsible)
                                                          .bulkPrintDetails(bulkPrintDetails).build()));
        caseData.setFinalServedApplicationDetailsList(finalServedApplicationDetailsList);
        return caseData;
    }

    private EmailNotificationDetails sendNotificationForApplicantLegalRepPersonalService(CaseData caseData, String authorization,
                                                                     SoaPack unServedApplicantPack, SoaPack unServedRespondentPack) {
        EmailNotificationDetails emailNotification;
        if (FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            emailNotification = sendEmailDaPersonalApplicantLegalRep(
                caseData,
                authorization,
                unwrapElements(null != unServedApplicantPack ? unServedApplicantPack.getPackDocument() : null),
                unwrapElements(unServedRespondentPack.getPackDocument()),
                false
            );
        } else {
            List<Document> docs = new ArrayList<>();
            removeDuplicatesAndGetConsolidatedDocs(unwrapElements(null != unServedApplicantPack ? unServedApplicantPack.getPackDocument() : null),
                                                   unwrapElements(unServedRespondentPack.getPackDocument()), docs);
            emailNotification = sendEmailCaPersonalApplicantLegalRep(
                caseData,
                authorization,
                docs
            );
        }
        return emailNotification;
    }

    private EmailNotificationDetails checkAndServeLocalAuthorityEmail(CaseData caseData, String authorization) {
        final SoaPack unServedLaPack = caseData.getServiceOfApplication().getUnServedLaPack();
        if (!ObjectUtils.isEmpty(unServedLaPack) && CollectionUtils.isNotEmpty(unServedLaPack.getPartyIds())) {
            try {
                EmailNotificationDetails emailNotification = serviceOfApplicationEmailService
                    .sendEmailNotificationToLocalAuthority(
                        authorization,
                        caseData,
                        unServedLaPack.getPartyIds().get(0).getValue(),
                        ElementUtils.unwrapElements(unServedLaPack.getPackDocument()),
                        PrlAppsConstants.SERVED_PARTY_LOCAL_AUTHORITY);
                if (null != emailNotification) {
                    return emailNotification;
                }
            } catch (IOException e) {
                log.error("Failed to serve application via email notification to La {}", e.getMessage());
            }
        }
        return null;
    }

    private void checkAndSendCafcassCymruEmails(CaseData caseData, List<Element<EmailNotificationDetails>> emailNotificationDetails) {
        if (YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaCafcassCymruServedOptions())
            && null != caseData.getServiceOfApplication().getSoaCafcassCymruEmail()) {
            emailNotificationDetails.addAll(sendEmailToCafcassInCase(
                caseData,
                caseData.getServiceOfApplication().getSoaCafcassCymruEmail(),
                PrlAppsConstants.SERVED_PARTY_CAFCASS_CYMRU));
        }
    }

    private void sendNotificationForOthersPack(CaseData caseData, String authorization, List<Element<BulkPrintDetails>> bulkPrintDetails,
                                               SoaPack unServedOthersPack) {
        final List<Element<String>> otherPartyIds = unServedOthersPack.getPartyIds();
        final List<DynamicMultiselectListElement> otherPartyList = createPartyDynamicMultiSelectListElement(
            otherPartyIds);

        bulkPrintDetails.addAll(sendPostToOtherPeopleInCase(
            caseData,
            authorization, otherPartyList,
            unwrapElements(unServedOthersPack.getPackDocument()),
            PrlAppsConstants.SERVED_PARTY_OTHER
        ));
    }

    private void sendNotificationForUnservedApplicantPack(CaseData caseData, String authorization,
                                                          List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                          SoaPack unServedApplicantPack,
                                                          List<Element<BulkPrintDetails>> bulkPrintDetails) {
        final List<Element<String>> partyIds = unServedApplicantPack.getPartyIds();
        final List<DynamicMultiselectListElement> applicantList = createPartyDynamicMultiSelectListElement(
            partyIds);
        List<Document> packDocs = new ArrayList<>(unwrapElements(unServedApplicantPack.getPackDocument()));
        if (CaseUtils.isCaseCreatedByCitizen(caseData)) {
            if (SoaCitizenServingRespondentsEnum.courtAdmin.toString().equalsIgnoreCase(
                unServedApplicantPack.getPersonalServiceBy())
                || SoaCitizenServingRespondentsEnum.courtBailiff.toString().equalsIgnoreCase(
                unServedApplicantPack.getPersonalServiceBy())) {
                //remove cover letters & notify applicants
                notifyC100ApplicantsPersonalServiceCourtAdminBailiff(authorization,
                                                                     caseData,
                                                                     emailNotificationDetails,
                                                                     bulkPrintDetails,
                                                                     removeCoverLettersFromThePacks(packDocs)
                );
            } else {
                emailNotificationDetails.addAll(sendNotificationsToCitizenApplicantsC100(
                    authorization,
                    applicantList,
                    caseData,
                    bulkPrintDetails,
                    removeCoverLettersFromThePacks(packDocs))
                );
            }
        } else {
            emailNotificationDetails.addAll(sendNotificationToApplicantSolicitor(
                caseData,
                authorization,
                applicantList,
                packDocs,
                SERVED_PARTY_APPLICANT_SOLICITOR
            ));
        }
    }

    public List<DynamicMultiselectListElement> createPartyDynamicMultiSelectListElement(List<Element<String>> partyList) {
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        final List<String> partyIds = ElementUtils.unwrapElements(partyList);

        partyIds.forEach(partyId -> listItems.add(DynamicMultiselectListElement.builder().code(partyId)
                          .label(partyId).build()));
        return listItems;
    }

    public ResponseEntity<SubmittedCallbackResponse> processConfidentialityCheck(String authorisation, CallbackRequest callbackRequest) {

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = allTabService.getStartAllTabsUpdate(String.valueOf(
            callbackRequest.getCaseDetails().getId()));
        Map<String, Object> caseDataMap = startAllTabsUpdateDataContent.caseDataMap();
        CaseData caseData = startAllTabsUpdateDataContent.caseData();

        final ResponseEntity<SubmittedCallbackResponse> response;

        if (caseData.getServiceOfApplication().getApplicationServedYesNo() != null
            && Yes.equals(caseData.getServiceOfApplication().getApplicationServedYesNo())) {
            response = servePacksWithConfidentialDetails(authorisation, caseData, caseDataMap);
            CaseUtils.setCaseState(callbackRequest, caseDataMap);

            //PRL-3466 - Citizen case auto linking
            autoLinkCitizenCase(caseData, caseDataMap);
        } else {
            response = rejectPacksWithConfidentialDetails(caseData, caseDataMap);
            caseDataMap.put(UNSERVED_RESPONDENT_PACK, null);
            caseDataMap.put(UNSERVED_APPLICANT_PACK, null);
            caseDataMap.put(UNSERVED_OTHERS_PACK, null);
            caseDataMap.put(UNSERVED_LA_PACK, null);
            caseDataMap.put(UNSERVED_CAFCASS_CYMRU_PACK, null);
        }
        caseDataMap.put(APPLICATION_SERVED_YES_NO, null);
        caseDataMap.put(REJECTION_REASON, null);

        allTabService.submitAllTabsUpdate(
            startAllTabsUpdateDataContent.authorisation(),
            String.valueOf(callbackRequest.getCaseDetails().getId()),
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseDataMap
        );
        return response;
    }

    private ResponseEntity<SubmittedCallbackResponse> rejectPacksWithConfidentialDetails(CaseData caseData, Map<String, Object> caseDataMap) {
        final ResponseEntity<SubmittedCallbackResponse> response;
        // SOA - TO DO  - create work allocation task

        log.info("Confidential check failed, Applicantion, can't be served");

        List<Element<ConfidentialCheckFailed>> confidentialCheckFailedList = new ArrayList<>();
        if (!org.springframework.util.CollectionUtils.isEmpty(caseData.getServiceOfApplication().getConfidentialCheckFailed())) {
            log.info("Reject reason list not empty");
            // get existing reject reason
            confidentialCheckFailedList.addAll(caseData.getServiceOfApplication().getConfidentialCheckFailed());
        }
        log.info("Reject reason list empty, adding first reject reason");

        String formatter = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
            .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE)));
        final ConfidentialCheckFailed confidentialCheckFailed = ConfidentialCheckFailed.builder().confidentialityCheckRejectReason(
                caseData.getServiceOfApplication().getRejectionReason())
            .dateRejected(formatter)
            .build();

        confidentialCheckFailedList.add(ElementUtils.element(confidentialCheckFailed));

        caseDataMap.put(CONFIDENTIAL_CHECK_FAILED, confidentialCheckFailedList);

        response = ok(SubmittedCallbackResponse.builder()
                      .confirmationHeader(RETURNED_TO_ADMIN_HEADER)
                      .confirmationBody(
                          CONFIDENTIAL_CONFIRMATION_NO_BODY_PREFIX).build());
        return response;
    }

    private ResponseEntity<SubmittedCallbackResponse> servePacksWithConfidentialDetails(String authorisation, CaseData caseData,
                                                                                        Map<String, Object> caseDataMap) {
        final ResponseEntity<SubmittedCallbackResponse> response;
        String confirmationHeader;
        String confirmationBody;
        caseData = sendNotificationsForUnServedPacks(
            caseData,
            authorisation
        );

        final SoaPack unServedApplicantPack = caseData.getServiceOfApplication().getUnServedApplicantPack();
        final SoaPack unServedRespondentPack = caseData.getServiceOfApplication().getUnServedRespondentPack();

        caseDataMap.put(FINAL_SERVED_APPLICATION_DETAILS_LIST, caseData.getFinalServedApplicationDetailsList());
        caseDataMap.put(UNSERVED_RESPONDENT_PACK, unServedRespondentPack);

        if ((isNotEmpty(unServedApplicantPack) && StringUtils.isNotEmpty(unServedApplicantPack.getPersonalServiceBy()))
            || (isNotEmpty(unServedRespondentPack) && StringUtils.isNotEmpty(unServedRespondentPack.getPersonalServiceBy()))) {
            confirmationBody = CONFIDENTIALITY_CONFIRMATION_BODY_PERSONAL;
            confirmationHeader = CONFIDENTIALITY_CONFIRMATION_HEADER_PERSONAL;
        } else {
            confirmationBody = CONFIRMATION_BODY_PREFIX;
            confirmationHeader = CONFIRMATION_HEADER_NON_PERSONAL;
            confirmationBody = String.format(
                confirmationBody,
                "/cases/case-details/" + caseData.getId() + "/#Service%20of%20application"

            );
        }
        caseDataMap.put(UNSERVED_APPLICANT_PACK, null);
        if (null != caseData.getServiceOfApplication().getUnServedRespondentPack()
            && (null == caseData.getServiceOfApplication().getUnServedRespondentPack().getPersonalServiceBy()
            || SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative.toString().equalsIgnoreCase(
            caseData.getServiceOfApplication().getUnServedRespondentPack().getPersonalServiceBy()))) {
            caseDataMap.put(UNSERVED_RESPONDENT_PACK, null);
        }
        caseDataMap.put(UNSERVED_OTHERS_PACK, null);
        caseDataMap.put(UNSERVED_LA_PACK, null);
        caseDataMap.put(UNSERVED_CAFCASS_CYMRU_PACK, null);
        response = ok(SubmittedCallbackResponse.builder()
                          .confirmationHeader(confirmationHeader)
                          .confirmationBody(
                              confirmationBody).build());
        return response;
    }

    public AboutToStartOrSubmitCallbackResponse soaValidation(CallbackRequest callbackRequest) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );

        log.info("inside soaValidation");
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        List<String> errorList = new ArrayList<>();

        if (null != caseData.getServiceOfApplication().getSoaOtherParties()
            && null != caseData.getServiceOfApplication().getSoaOtherParties().getValue()
            && !caseData.getServiceOfApplication().getSoaOtherParties().getValue().isEmpty()) {

            List<String> c6aOrderIds = new ArrayList<>();
            log.info("inside other people check");
            if (null != caseData.getOrderCollection()) {
                c6aOrderIds = caseData.getOrderCollection().stream()
                    .filter(element -> element.getValue() != null && (element.getValue().getOrderTypeId().equals(
                        CreateSelectOrderOptionsEnum.noticeOfProceedingsNonParties.toString())
                        || element.getValue().getOrderTypeId().equals(
                        CreateSelectOrderOptionsEnum.noticeOfProceedingsNonParties.getDisplayedValue())
                    ))
                    .map(s -> s.getId().toString()).toList();
            }

            if (c6aOrderIds.isEmpty()) {
                errorList.add(OTHER_PEOPLE_SELECTED_C6A_MISSING_ERROR);
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(errorList)
                    .build();
            }

            List<String> selectedSoaScreenOrders = caseData.getServiceOfApplicationScreen1().getValue()
                .stream().map(DynamicMultiselectListElement::getCode).toList();

            boolean isPresent = c6aOrderIds.stream().anyMatch(selectedSoaScreenOrders::contains);
            log.info("isPresent {}", isPresent);

            if (!isPresent) {
                errorList.add(OTHER_PEOPLE_SELECTED_C6A_MISSING_ERROR);
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(errorList)
                    .build();
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
            .build();
    }
}
