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
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.config.templates.Templates;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesNoNotApplicable;
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
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.CoverLetterMap;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.AP13_HINT;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.AP14_HINT;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.AP15_HINT;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_AP1;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_AP2;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_AP7;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_AP8;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_C100_AP13;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_C100_AP14;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_C100_AP15;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_C100_RE6;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_C100_RE7;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_FL401_RE1;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_FL401_RE2;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_FL401_RE3;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_FL401_RE4;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_FL401_RE8;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_RE5;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_WEL_C100_AP13;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_WEL_C100_AP14;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_WEL_C100_AP15;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_WEL_C100_RE7;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_WEL_FL401_RE8;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.RE7_HINT;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.RE8_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BLANK_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C1A_BLANK_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C1A_BLANK_DOCUMENT_WELSH_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C7_BLANK_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C9_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_CAN_VIEW_ONLINE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_COVER_SHEET_HINT;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_CONFIDENTIAL_DETAILS_PRESENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_CYMRU_EMAIL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_DOCUMENT_PLACE_HOLDER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_FL415_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_NOTICE_SAFETY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_ORDER_LIST_EMPTY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_OTHER_PARTIES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_OTHER_PEOPLE_PRESENT_IN_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_RECIPIENT_OPTIONS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WARNING_TEXT_DIV;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YES;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.SOA_CA_PERSONAL_UNREPRESENTED_APPLICANT;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.SOA_CA_PERSONAL_UNREPRESENTED_APPLICANT_WITHOUT_C1A;
import static uk.gov.hmcts.reform.prl.services.SendAndReplyService.ARROW_SEPARATOR;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.hasDashboardAccess;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.hasLegalRepresentation;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings({"java:S3776","java:S6204","java:S112","java:S4144", "java:S5665","java:S1172"})
public class ServiceOfApplicationService {
    public static final String UNSERVED_APPLICANT_PACK = "unServedApplicantPack";
    public static final String UNSERVED_RESPONDENT_PACK = "unServedRespondentPack";
    public static final String UNSERVED_APPLICANT_LIP_RESPONDENT_PACK = "unservedCitizenRespondentPack";
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

    public static final String IS_C8_CHECK_NEEDED = "isC8CheckNeeded";
    public static final String IS_C8_CHECK_APPROVED = "isC8CheckApproved";
    public static final String ENABLE_CITIZEN_ACCESS_CODE_IN_COVER_LETTER = "enable-citizen-access-code-in-cover-letter";
    public static final String DISPLAY_LEGAL_REP_OPTION = "displayLegalRepOption";
    public static final String SEND_GRID_TEMPLATE = "sendGridTemplate";
    public static final String CONFIRMATION_BODY = "confirmationBody";
    public static final String CONFIRMATION_HEADER = "confirmationHeader";

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
    private final CaseInviteManager caseInviteManager;
    private final DgsService dgsService;
    private final LaunchDarklyClient launchDarklyClient;

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
                                                                       List<Element<PartyDetails>> selectedOthers,
                                                                       List<Document> packN, String servedParty) {
        List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
        selectedOthers.forEach(other -> {
            try {
                log.info(
                    "Sending the post notification to others in case for C100 Application for caseId {}",
                    caseData.getId()
                );

                if (null != other.getValue().getAddress()
                    && null != other.getValue().getAddress().getAddressLine1()) {
                    List<Document> docs = new ArrayList<>(serviceOfApplicationPostService
                                                              .getCoverSheets(caseData, authorization,
                                                                              other.getValue().getAddress(),
                                                                              other.getValue().getLabelForDynamicList(),
                                                                              DOCUMENT_COVER_SHEET_HINT
                                                              ));
                    docs.addAll(packN);
                    bulkPrintDetails.add(element(serviceOfApplicationPostService.sendPostNotificationToParty(
                        caseData,
                        authorization,
                        other,
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
        String whoIsResponsibleForServing = COURT;
        List<Document> staticDocs = serviceOfApplicationPostService.getStaticDocs(authorization,
                                                                                  CaseUtils.getCaseTypeOfApplication(caseData),
                                                                                  caseData);
        if (C100_CASE_TYPE.equals(CaseUtils.getCaseTypeOfApplication(caseData))) {
            whoIsResponsibleForServing = sendNotificationsSoaC100(
                caseData,
                authorization,
                caseDataMap,
                emailNotificationDetails,
                bulkPrintDetails,
                whoIsResponsibleForServing,
                staticDocs
            );
        } else {
            whoIsResponsibleForServing = sendNotificationsSoaFl401(
                caseData,
                authorization,
                caseDataMap,
                emailNotificationDetails,
                bulkPrintDetails,
                whoIsResponsibleForServing,
                staticDocs
            );
        }

        return ServedApplicationDetails.builder().emailNotificationDetails(emailNotificationDetails)
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .servedAt(CaseUtils.getCurrentDate())
            .modeOfService(CaseUtils.getModeOfService(emailNotificationDetails, bulkPrintDetails))
            .whoIsResponsible(whoIsResponsibleForServing)
            .bulkPrintDetails(bulkPrintDetails).build();
    }

    private String sendNotificationsSoaFl401(CaseData caseData, String authorization, Map<String, Object> caseDataMap,
                                             List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                             List<Element<BulkPrintDetails>> bulkPrintDetails, String whoIsResponsibleForServing,
                                             List<Document> staticDocs) {
        if (YesNoNotApplicable.No.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())
            && (caseData.getServiceOfApplication().getSoaRecipientsOptions() != null)
            && (!caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue().isEmpty())) {
            log.info("*** Handling notifications for DA Non personal service ***");
            handleNotificationDaNonPersonalService(caseData, authorization, emailNotificationDetails, bulkPrintDetails,
                                                   staticDocs
            );
        } else {
            whoIsResponsibleForServing = sendNotificationsSoaFl401PersonalService(
                caseData,
                authorization,
                caseDataMap,
                emailNotificationDetails,
                bulkPrintDetails,
                whoIsResponsibleForServing,
                staticDocs
            );
        }
        return whoIsResponsibleForServing;
    }

    private String sendNotificationsSoaFl401PersonalService(CaseData caseData, String authorization, Map<String, Object> caseDataMap,
                                                            List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                            List<Element<BulkPrintDetails>> bulkPrintDetails, String whoIsResponsibleForServing,
                                                            List<Document> staticDocs) {
        if (CaseUtils.isCitizenCase(caseData)) {
            log.info("Handling Personal service for DA Citizen case");
            whoIsResponsibleForServing = handlePersonalServiceNotificationsDaCitizenOptions(
                caseData,
                authorization,
                emailNotificationDetails,
                bulkPrintDetails,
                caseDataMap,
                staticDocs
            );
        } else {
            log.info("Handling Personal service for DA Represented case");
            whoIsResponsibleForServing = handleNotificationsDaPersonalServiceSolicitorOptions(
                caseData,
                authorization,
                emailNotificationDetails,
                caseDataMap,
                staticDocs,
                whoIsResponsibleForServing
            );
        }
        return whoIsResponsibleForServing;
    }

    private String sendNotificationsSoaC100(CaseData caseData, String authorization, Map<String, Object> caseDataMap,
                                            List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                            List<Element<BulkPrintDetails>> bulkPrintDetails, String whoIsResponsibleForServing,
                                            List<Document> staticDocs) {
        if (YesNoNotApplicable.No.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())
            && (caseData.getServiceOfApplication().getSoaRecipientsOptions() != null)
            && (!caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue().isEmpty())) {
            staticDocs = staticDocs.stream().filter(d -> !C9_DOCUMENT_FILENAME.equalsIgnoreCase(d.getDocumentFileName())).toList();
            sendNotificationsSoaC100NonPersonal(caseData, authorization, emailNotificationDetails, bulkPrintDetails, staticDocs);
        } else {
            whoIsResponsibleForServing = sendNotificationsSoaC100PersonalService(
                caseData,
                authorization,
                caseDataMap,
                emailNotificationDetails,
                bulkPrintDetails,
                whoIsResponsibleForServing,
                staticDocs
            );
        }
        if (null != caseData.getServiceOfApplication().getSoaOtherParties()
            && !caseData.getServiceOfApplication().getSoaOtherParties().getValue().isEmpty()) {
            log.info("sending notification to Other in case of Citizen");
            sendNotificationToOthers(caseData, authorization, bulkPrintDetails, staticDocs);
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
        return whoIsResponsibleForServing;
    }

    private void sendNotificationsSoaC100NonPersonal(CaseData caseData, String authorization,
                                                     List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                     List<Element<BulkPrintDetails>> bulkPrintDetails, List<Document> staticDocs) {
        List<Element<PartyDetails>> selectedApplicants = getSelectedApplicantsOrRespondentsElements(
            caseData.getApplicants(),
            caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue()
        );
        List<Element<PartyDetails>> selectedRespondents = getSelectedApplicantsOrRespondentsElements(
            caseData.getRespondents(),
            caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue()
        );
        if (CollectionUtils.isNotEmpty(selectedApplicants)) {
            selectedApplicants.forEach(applicant -> {
                if (!CaseUtils.hasLegalRepresentation(applicant.getValue())) {
                    List<Document> packPDocs = getNotificationPack(caseData, PrlAppsConstants.Q, staticDocs);
                    sendNotificationCaNonPersonalApplicantCitizen(
                        caseData,
                        authorization,
                        emailNotificationDetails,
                        bulkPrintDetails,
                        applicant,
                        packPDocs
                    );
                } else {
                    List<Document> packQDocs = getNotificationPack(caseData, PrlAppsConstants.Q, staticDocs);
                    sendEmailToApplicantSolicitor(caseData,
                                                  authorization, packQDocs, SERVED_PARTY_APPLICANT_SOLICITOR,
                                                  emailNotificationDetails, applicant);
                }
            });
        }
        if (!selectedRespondents.isEmpty()) {
            //unrepresented
            List<Document> packRDocs = getNotificationPack(caseData, PrlAppsConstants.R, staticDocs);
            //represented
            List<Document> packSDocs = getNotificationPack(caseData, PrlAppsConstants.S, staticDocs);
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

    private String sendNotificationsSoaC100PersonalService(CaseData caseData, String authorization, Map<String, Object> caseDataMap,
                                                           List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                           List<Element<BulkPrintDetails>> bulkPrintDetails, String whoIsResponsibleForServing,
                                                           List<Document> staticDocs) {
        if (CaseUtils.isCitizenCase(caseData)) {
            whoIsResponsibleForServing = handlePersonalServiceForCitizenC100(caseData, authorization,
                                                                             emailNotificationDetails,
                                                                             bulkPrintDetails, staticDocs,
                                                                             caseDataMap
            );
        } else {
            whoIsResponsibleForServing = handlePersonalServiceNotificationsCaSolicitorOptions(
                caseData,
                authorization,
                emailNotificationDetails,
                caseDataMap,
                whoIsResponsibleForServing,
                staticDocs
            );
        }
        return whoIsResponsibleForServing;
    }

    private void sendNotificationCaNonPersonalApplicantCitizen(CaseData caseData, String authorization,
                                                               List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                               List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                               Element<PartyDetails> selectedApplicant,
                                                               List<Document> packDocs) {
        if (ContactPreferences.email.equals(selectedApplicant.getValue().getContactPreferences())) {
            Map<String, String> fieldsMap = new HashMap<>();
            fieldsMap.put(AUTHORIZATION, authorization);
            fieldsMap.put(COVER_LETTER_TEMPLATE, Templates.PRL_LET_ENG_AP6);
            sendEmailToCitizenLipPersonalServiceCaDa(
                caseData,
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
                                                                        selectedApplicant, Templates.PRL_LET_ENG_AP6);
            sendPostWithAccessCodeLetterToParty(
                caseData,
                authorization,
                packDocs,
                bulkPrintDetails,
                selectedApplicant,
                coverLetter,
                SERVED_PARTY_APPLICANT
            );
        }
    }

    private String handlePersonalServiceNotificationsDaCitizenOptions(CaseData caseData, String authorization,
                                                                      List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                                      List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                                      Map<String, Object> caseDataMap, List<Document> staticDocs) {
        String whoIsResponsibleForServing;
        if (SoaCitizenServingRespondentsEnum.unrepresentedApplicant
            .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptions())) {
            log.info("Sending service of application notifications to FL401 citizens - applicant Lip Personal service");
            whoIsResponsibleForServing = handleNotificationsDaPersonalApplicantLip(
                caseData,
                authorization,
                emailNotificationDetails,
                bulkPrintDetails,
                caseDataMap,
                staticDocs
            );
        } else {
            whoIsResponsibleForServing = SoaCitizenServingRespondentsEnum.courtBailiff
                .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptions())
                ? PERSONAL_SERVICE_SERVED_BY_BAILIFF : PERSONAL_SERVICE_SERVED_BY_CA;
            List<Document> packCdocs = new ArrayList<>();
            Element<PartyDetails> applicant = element(caseData.getApplicantsFL401().getPartyId(), caseData.getApplicantsFL401());
            packCdocs.add(generateCoverLetterBasedOnCaseAccess(authorization, caseData, applicant, Templates.PRL_LET_ENG_AP1));
            packCdocs.addAll(getNotificationPack(caseData, PrlAppsConstants.C, staticDocs));
            if (ContactPreferences.email.equals(caseData.getApplicantsFL401().getContactPreferences())) {
                Map<String, String> fieldsMap = new HashMap<>();
                fieldsMap.put(AUTHORIZATION, authorization);
                fieldsMap.put(COVER_LETTER_TEMPLATE, PRL_LET_ENG_AP1);
                sendEmailToCitizenLipPersonalServiceCaDa(
                    caseData,
                    emailNotificationDetails,
                    element(caseData.getApplicantsFL401().getPartyId(), caseData.getApplicantsFL401()),
                    packCdocs,
                    SendgridEmailTemplateNames.SOA_SERVE_APPLICANT_PER_CA_CB,
                    fieldsMap,
                    EmailTemplateNames.SOA_DA_PERSONAL_CB_CA_UNREPRESENTED_APPLICANT_COURTNAV
                );
            } else {
                sendSoaPacksToPartyViaPost(authorization, caseData, packCdocs,
                                           bulkPrintDetails,
                                           element(
                                               caseData.getApplicantsFL401().getPartyId(),
                                               caseData.getApplicantsFL401()
                                           ),
                                           Templates.PRL_LET_ENG_AP1
                );
            }
            generateUnservedRespondentPackDaCbCa(caseData, authorization, staticDocs, caseDataMap,
                                                 caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptions()
                                                     .toString());
        }
        return whoIsResponsibleForServing;
    }

    private String handleNotificationsDaPersonalApplicantLip(CaseData caseData, String authorization,
                                                             List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                             List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                             Map<String, Object> caseDataMap,
                                                             List<Document> fl401StaticDocs) {
        Element<PartyDetails> applicant = element(caseData.getApplicantsFL401().getPartyId(), caseData.getApplicantsFL401());
        Element<PartyDetails> respondent = element(caseData.getRespondentsFL401().getPartyId(), caseData.getRespondentsFL401());
        List<Document> docs = new ArrayList<>();
        List<Document> packEdocs = getNotificationPack(caseData, PrlAppsConstants.E, fl401StaticDocs);
        List<Document> packFdocs = new ArrayList<>();
        if (Yes.equals(caseData.getDoYouNeedAWithoutNoticeHearing())) {
            packFdocs.add(generateAccessCodeLetter(authorization, caseData, respondent, null, PRL_LET_ENG_FL401_RE2));
        } else {
            packFdocs.add(generateAccessCodeLetter(authorization, caseData, respondent, null, PRL_LET_ENG_FL401_RE3));
        }
        packFdocs.addAll(getNotificationPack(caseData, PrlAppsConstants.F, fl401StaticDocs));
        removeDuplicatesAndGetConsolidatedDocs(packEdocs, packFdocs, docs);
        if (ContactPreferences.email.equals(caseData.getApplicantsFL401().getContactPreferences())) {
            Map<String, String> fieldsMap = new HashMap<>();
            fieldsMap.put(AUTHORIZATION, authorization);
            fieldsMap.put(COVER_LETTER_TEMPLATE, PRL_LET_ENG_AP1);
            sendEmailToCitizenLipPersonalServiceCaDa(
                caseData,
                emailNotificationDetails,
                applicant,
                docs,
                SendgridEmailTemplateNames.SOA_DA_APPLICANT_LIP_PERSONAL,
                fieldsMap,
                EmailTemplateNames.SOA_UNREPRESENTED_APPLICANT_COURTNAV
            );
        } else {
            sendSoaPacksToPartyViaPost(authorization, caseData, docs,
                                       bulkPrintDetails,
                                       applicant,
                                       Templates.PRL_LET_ENG_AP1);
        }
        packFdocs = packFdocs.stream().filter(d -> !SOA_FL415_FILENAME.equalsIgnoreCase(d.getDocumentFileName()))
            .toList();
        caseDataMap.put(UNSERVED_APPLICANT_LIP_RESPONDENT_PACK, SoaPack.builder()
            .packDocument(wrapElements(packFdocs))
            .partyIds(List.of(element(String.valueOf(caseData.getRespondentsFL401().getPartyId()))))
            .servedBy(UNREPRESENTED_APPLICANT)
            .personalServiceBy(SoaCitizenServingRespondentsEnum.unrepresentedApplicant.toString())
            .packCreatedDate(CaseUtils.getCurrentDate())
            .build());
        return UNREPRESENTED_APPLICANT;
    }

    private String handlePersonalServiceNotificationsCaSolicitorOptions(CaseData caseData, String authorization,
                                                                        List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                                        Map<String, Object> caseDataMap, String whoIsResponsibleForServing,
                                                                        List<Document> c100StaticDocs) {
        if (SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative.equals(caseData.getServiceOfApplication()
                                                                                       .getSoaServingRespondentsOptions())) {
            log.info("Personal Service - Case created by - Solicitor");
            whoIsResponsibleForServing = caseData.getApplicants().get(0).getValue().getRepresentativeFullName();
            //This is added with assumption that, For applicant legl representative selection
            // if multiple applicants are present only the first applicant solicitor will receive notification

            List<Document> packHiDocs = new ArrayList<>();
            caseData.getRespondents().forEach(respondent -> packHiDocs.add(generateAccessCodeLetter(authorization,
                                                                                                    caseData,
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
            .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptions())
            || SoaSolicitorServingRespondentsEnum.courtAdmin
            .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptions())) {
            handleNotificationsCaSolicitorPersonalCourtAdminBailiff(caseData, authorization, emailNotificationDetails,
                                                                    c100StaticDocs, caseDataMap
            );
            whoIsResponsibleForServing = SoaSolicitorServingRespondentsEnum.courtBailiff
                .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptions())
                ? PERSONAL_SERVICE_SERVED_BY_BAILIFF : PERSONAL_SERVICE_SERVED_BY_CA;
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
            .packCreatedDate(CaseUtils.getCurrentDate())
            .personalServiceBy(caseData.getServiceOfApplication().getSoaServingRespondentsOptions().toString())
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
        generateUnservedRespondentPackDaCbCa(caseData, authorization, staticDocs, caseDataMap,
                                             caseData.getServiceOfApplication().getSoaServingRespondentsOptions().toString());
    }

    private void generateUnservedRespondentPackDaCbCa(CaseData caseData, String authorization, List<Document> staticDocs,
                                                      Map<String, Object> caseDataMap, String personalServiceBy) {
        List<Element<CoverLetterMap>> coverLetterMap = new ArrayList<>();
        staticDocs = staticDocs.stream().filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_FL415_FILENAME))
            .toList();
        List<Document> packdDocs = getRespondentPacksForDaPersonaServiceByCourtAdminAndBailiff(
            caseData,
            authorization,
            staticDocs,
            coverLetterMap
        );
        final SoaPack unservedRespondentPack = SoaPack.builder()
            .packDocument(wrapElements(packdDocs))
            .partyIds(wrapElements(caseData.getRespondentsFL401().getPartyId().toString()))
            .servedBy(PRL_COURT_ADMIN)
            .packCreatedDate(CaseUtils.getCurrentDate())
            .personalServiceBy(personalServiceBy)
            .coverLettersMap(coverLetterMap)
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
                                                                                       List<Document> staticDocs,
                                                                                       List<Element<CoverLetterMap>> coverLetterMap) {
        List<Document> packdDocs = new ArrayList<>();
        packdDocs.addAll(getCoverLetterForDaCourtAdminAndBailiffPersonalService(caseData, authorization, coverLetterMap));
        packdDocs.addAll(getNotificationPack(caseData, PrlAppsConstants.D, staticDocs));
        return packdDocs;
    }

    private List<Document> getCoverLetterForDaCourtAdminAndBailiffPersonalService(CaseData caseData, String authorization,
                                                                                  List<Element<CoverLetterMap>> coverLetterMap) {
        List<Document> reLetters = new ArrayList<>();
        Element<PartyDetails> respondent = Element.<PartyDetails>builder()
            .id(caseData.getRespondentsFL401().getPartyId())
            .value(caseData.getRespondentsFL401())
            .build();
        reLetters.add(getRe1OrRe4BasedOnWithOrWithoutNotice(caseData, authorization, respondent));
        coverLetterMap.add(element(respondent.getId(), CoverLetterMap.builder().coverLetters(wrapElements(reLetters)).build()));
        return reLetters;
    }

    private Document getRe1OrRe4BasedOnWithOrWithoutNotice(CaseData caseData, String authorization, Element<PartyDetails> respondent) {
        boolean applyOrderWithoutGivingNoticeToRespondent = CaseUtils.isApplyOrderWithoutGivingNoticeToRespondent(
            caseData);
        if (applyOrderWithoutGivingNoticeToRespondent) {
            return generateAccessCodeLetter(authorization, caseData, respondent, null, PRL_LET_ENG_FL401_RE4);
        } else {
            return generateAccessCodeLetter(authorization, caseData, respondent, null, PRL_LET_ENG_FL401_RE1);
        }
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

    private String handleNotificationsDaPersonalServiceSolicitorOptions(CaseData caseData, String authorization,
                                                                        List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                                        Map<String, Object> caseDataMap,
                                                                        List<Document> staticDocs, String whoIsResponsibleForServing) {
        if (SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative.equals(caseData.getServiceOfApplication()
                                                                                       .getSoaServingRespondentsOptions())) {
            List<Document> packADocs = getNotificationPack(caseData, PrlAppsConstants.A, staticDocs);
            List<Document> packBDocs = getNotificationPack(caseData, PrlAppsConstants.B, staticDocs);
            emailNotificationDetails.add(element(sendEmailDaPersonalApplicantLegalRep(
                caseData,
                authorization, packADocs, packBDocs, true)));
            whoIsResponsibleForServing = SERVED_PARTY_APPLICANT_SOLICITOR;
        } else if (SoaSolicitorServingRespondentsEnum.courtBailiff
            .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptions())
            || SoaSolicitorServingRespondentsEnum.courtAdmin
            .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptions())) {
            sendNotificationsAndCreatePacksForDaCourtAdminAndBailiff(caseData, authorization, emailNotificationDetails,
                                                                     staticDocs, caseDataMap
            );
            whoIsResponsibleForServing = SoaSolicitorServingRespondentsEnum.courtBailiff
                .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptions())
                ? PERSONAL_SERVICE_SERVED_BY_BAILIFF : PERSONAL_SERVICE_SERVED_BY_CA;
        }
        return whoIsResponsibleForServing;
    }

    private void handleNotificationDaNonPersonalService(CaseData caseData, String authorization,
                                                        List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                        List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                        List<Document> staticDocs) {
        staticDocs = staticDocs.stream().filter(d -> ! d.getDocumentFileName().equalsIgnoreCase(SOA_FL415_FILENAME)).toList();
        List<Element<PartyDetails>> applicantFl401 = Arrays.asList(element(caseData.getApplicantsFL401().getPartyId(),
                                                                           caseData.getApplicantsFL401()));
        List<Element<PartyDetails>> respondentFl401 = Arrays.asList(element(caseData.getRespondentsFL401().getPartyId(),
                                                                            caseData.getRespondentsFL401()));

        applicantFl401 = getSelectedApplicantsOrRespondentsElements(applicantFl401, caseData.getServiceOfApplication()
            .getSoaRecipientsOptions().getValue());
        respondentFl401 = getSelectedApplicantsOrRespondentsElements(respondentFl401, caseData.getServiceOfApplication()
            .getSoaRecipientsOptions().getValue());
        sendNotificationsDaNonPersonalApplicant(caseData, authorization, emailNotificationDetails, bulkPrintDetails, staticDocs, applicantFl401);
        sendNotificationsDaNonPersonalRespondent(caseData, authorization, emailNotificationDetails, bulkPrintDetails, staticDocs, respondentFl401);
    }

    private void sendNotificationsDaNonPersonalRespondent(CaseData caseData, String authorization,
                                                          List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                          List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                          List<Document> staticDocs, List<Element<PartyDetails>> respondentFl401) {
        if (CollectionUtils.isNotEmpty(respondentFl401)) {
            String emailAddress = respondentFl401.get(0).getValue().getSolicitorEmail();
            String servedParty = respondentFl401.get(0).getValue().getLabelForDynamicList();
            List<Document> docs = new ArrayList<>();
            boolean sendEmail = true;
            Document coverLetter = null;
            Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
            dynamicData.put(SEND_GRID_TEMPLATE, SendgridEmailTemplateNames.SOA_SERVE_APPLICANT_SOLICITOR_NONPER_PER_CA_CB);
            if (CaseUtils.hasLegalRepresentation(respondentFl401.get(0).getValue())) {
                servedParty = respondentFl401.get(0).getValue().getRepresentativeFullName();
            } else {
                coverLetter = getRe1OrRe4BasedOnWithOrWithoutNotice(caseData, authorization, respondentFl401.get(0));
                sendEmail = false;
                docs.add(coverLetter);
            }
            List<Document> packDocs = getNotificationPack(caseData, PrlAppsConstants.A, staticDocs);
            docs.addAll(packDocs);
            if (sendEmail) {
                sendEmailDaNonPersonalService(
                    caseData,
                    authorization,
                    emailNotificationDetails,
                    emailAddress,
                    servedParty,
                    docs,
                    dynamicData
                );
            } else {
                sendPostWithAccessCodeLetterToParty(caseData,
                                                    authorization, packDocs,
                                                    bulkPrintDetails, respondentFl401.get(0),
                                                    coverLetter, servedParty);
            }
        }
    }

    private void sendEmailDaNonPersonalService(CaseData caseData, String authorization,
                                               List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                               String emailAddress, String servedParty, List<Document> docs,
                                               Map<String, Object> dynamicData) {
        try {
            log.info(
                "Sending the email notification to applicant solicitor for fl401 Application for caseId {}",
                caseData.getId()
            );
            dynamicData.put("name", servedParty);
            dynamicData.put(DASH_BOARD_LINK, manageCaseUrl + PrlAppsConstants.URL_STRING + caseData.getId());
            populateLanguageMap(caseData, dynamicData);
            SendgridEmailTemplateNames sendgridEmailTemplateName = (SendgridEmailTemplateNames) dynamicData.get(
                SEND_GRID_TEMPLATE);
            dynamicData.remove(SEND_GRID_TEMPLATE);
            emailNotificationDetails.add(element(serviceOfApplicationEmailService
                                                     .sendEmailUsingTemplateWithAttachments(
                                                         authorization, emailAddress,
                                                         docs,
                                                         sendgridEmailTemplateName,
                                                         dynamicData,
                                                         servedParty
                                                     )));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendNotificationsDaNonPersonalApplicant(CaseData caseData, String authorization,
                                                         List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                         List<Element<BulkPrintDetails>> bulkPrintDetails, List<Document> staticDocs,
                                                         List<Element<PartyDetails>> applicantFl401) {
        if (CollectionUtils.isNotEmpty(applicantFl401)) {
            String emailAddress = applicantFl401.get(0).getValue().getEmail();
            String servedParty = applicantFl401.get(0).getValue().getLabelForDynamicList();
            List<Document> docs = new ArrayList<>();
            Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
            Document coverLetter = null;
            boolean sendEmail = true;
            SendgridEmailTemplateNames sendgridEmailTemplateName = SendgridEmailTemplateNames.SOA_SERVE_APPLICANT_SOLICITOR_NONPER_PER_CA_CB;
            if (CaseUtils.hasLegalRepresentation(applicantFl401.get(0).getValue())) {
                emailAddress = applicantFl401.get(0).getValue().getSolicitorEmail();
                servedParty = applicantFl401.get(0).getValue().getRepresentativeFullName();
            } else {
                sendgridEmailTemplateName = SendgridEmailTemplateNames.SOA_DA_NON_PERSONAL_SERVICE_APPLICANT_LIP;
                coverLetter = generateCoverLetterBasedOnCaseAccess(
                    authorization,
                    caseData, applicantFl401.get(0), PRL_LET_ENG_AP2);
                docs.add(coverLetter);
                if (!ContactPreferences.email.equals(applicantFl401.get(0).getValue().getContactPreferences())) {
                    sendEmail = false;
                }
            }
            List<Document> packDocs = getNotificationPack(caseData, PrlAppsConstants.A, staticDocs);
            docs.addAll(packDocs);
            if (sendEmail) {
                try {
                    log.info(
                        "Sending the email notification to applicant solicitor for fl401 Application for caseId {}",
                        caseData.getId()
                    );
                    dynamicData.put("name", servedParty);
                    populateLanguageMap(caseData, dynamicData);
                    emailNotificationDetails.add(element(serviceOfApplicationEmailService
                                                             .sendEmailUsingTemplateWithAttachments(
                                                                 authorization, emailAddress,
                                                                 docs,
                                                                 sendgridEmailTemplateName,
                                                                 dynamicData,
                                                                 servedParty
                                                             )));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                sendPostWithAccessCodeLetterToParty(caseData,
                                                    authorization, packDocs,
                                                    bulkPrintDetails, applicantFl401.get(0),
                                                    coverLetter, servedParty);
            }
        }
    }

    private void sendNotificationToOthers(CaseData caseData, String authorization, List<Element<BulkPrintDetails>> bulkPrintDetails,
                                          List<Document> c100StaticDocs) {
        log.info("serving other people in case");

        List<Element<PartyDetails>> othersToNotify = getSelectedApplicantsOrRespondentsElements(
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
                                                                    List<Element<PartyDetails>> selectedRespondents,
                                                                    List<Document> packSdocs, List<Document> packRdocs) {
        log.info("Sending notification to respondent or solicitor");
        selectedRespondents.forEach(respondent -> {
            if (CaseUtils.hasLegalRepresentation(respondent.getValue())) {
                sendEmailToRespondentSolicitorNonPersonal(caseData, authorization, emailNotificationDetails, packSdocs, respondent);
            } else if (!CaseUtils.hasLegalRepresentation(respondent.getValue())) {
                if (respondent.getValue().getAddress() != null && StringUtils.isNotEmpty(respondent.getValue().getAddress().getAddressLine1())) {
                    log.info(
                        "Sending the notification in post to respondent for C100 Application for caseId {}",
                        caseData.getId()
                    );
                    List<Document> finalDocs = removeCoverLettersFromThePacks(packRdocs);
                    Document coverLetter = generateCoverLetterBasedOnCaseAccess(authorization, caseData, respondent, PRL_LET_ENG_RE5);
                    sendPostWithAccessCodeLetterToParty(caseData, authorization, finalDocs, bulkPrintDetails, respondent,
                                                        coverLetter, SERVED_PARTY_RESPONDENT);
                } else {
                    log.info("Unable to send any notification to respondent for C100 Application for caseId {} "
                                 + "as no address available", caseData.getId());
                }
            }
        });
    }

    private void sendNotificationToRespondentOrSolicitorNonPersonalConfCheckSuccess(CaseData caseData, String authorization,
                                                                    List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                                    List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                                    List<DynamicMultiselectListElement> selectedRespondents,
                                                                    List<Document> packSdocs, List<Document> packRdocs) {
        log.info("Sending notification to respondent or solicitor");
        selectedRespondents.forEach(respondentc100 -> {
            Optional<Element<PartyDetails>> party = getParty(respondentc100.getCode(), caseData.getRespondents());
            if (party.isPresent() && CaseUtils.hasLegalRepresentation(party.get().getValue())) {
                sendEmailToRespondentSolicitorNonPersonal(caseData, authorization, emailNotificationDetails, packSdocs, party.get());
            } else if (party.isPresent() && (!CaseUtils.hasLegalRepresentation(party.get().getValue()))) {
                if (party.get().getValue().getAddress() != null && StringUtils.isNotEmpty(party.get().getValue().getAddress().getAddressLine1())) {
                    log.info(
                        "Sending the notification in post to respondent for C100 Application for caseId {}",
                        caseData.getId()
                    );
                    List<Document> finalDocs = removeCoverLettersFromThePacks(packRdocs);
                    sendPostWithAccessCodeLetterToParty(caseData, authorization, finalDocs, bulkPrintDetails, party.get(),
                                                        CaseUtils.getCoverLettersForParty(party.get().getId(),
                                                                                          caseData.getServiceOfApplication()
                                                                                              .getUnServedRespondentPack()
                                                                                              .getCoverLettersMap()).get(0),
                                                        SERVED_PARTY_RESPONDENT);
                } else {
                    log.info("Unable to send any notification to respondent for C100 Application for caseId {} "
                                 + "as no address available", caseData.getId());
                }
            }
        });
    }

    private void sendEmailToRespondentSolicitorNonPersonal(CaseData caseData, String authorization,
                                                           List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                           List<Document> packSdocs, Element<PartyDetails> party) {
        if (party.getValue().getSolicitorEmail() != null) {
            try {
                log.info(
                    "Sending the email notification to respondent solicitor for C100 Application for caseId {}",
                    caseData.getId()
                );
                Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
                dynamicData.put("name", party.getValue().getRepresentativeFullName());
                dynamicData.put(DASH_BOARD_LINK, manageCaseUrl + PrlAppsConstants.URL_STRING + caseData.getId());
                dynamicData.put("respondent", true);
                populateLanguageMap(caseData, dynamicData);
                List<Document> finalDocs = removeCoverLettersFromThePacks(packSdocs);
                EmailNotificationDetails emailNotification = serviceOfApplicationEmailService.sendEmailUsingTemplateWithAttachments(
                    authorization,
                    party.getValue().getSolicitorEmail(),
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
    }

    public List<Document> removeCoverLettersFromThePacks(List<Document> documents) {
        return documents.stream().filter(document -> !document.getDocumentFileName().contains("cover_letter")).toList();
    }

    private String handlePersonalServiceForCitizenC100(CaseData caseData, String authorization,
                                                     List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                     List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                     List<Document> c100StaticDocs,
                                                     Map<String, Object> caseDataMap) {
        String whoIsResponsibleForServing;
        //Suppressed java:S1172 as emailNotificationDetails not used, but will be used when citizen journey comes into the picture.
        log.info("Service of application, unrepresented applicant/citizen case");
        if (SoaCitizenServingRespondentsEnum.unrepresentedApplicant
            .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptions())) {
            whoIsResponsibleForServing = UNREPRESENTED_APPLICANT;
            List<Document> packLdocs = getNotificationPack(caseData, PrlAppsConstants.L, c100StaticDocs);
            notifyC100ApplicantsPersonalServiceUnRepApplicant(authorization,
                                                              caseData,
                                                              emailNotificationDetails,
                                                              bulkPrintDetails,
                                                              packLdocs);
            caseDataMap.put(UNSERVED_APPLICANT_LIP_RESPONDENT_PACK, SoaPack.builder()
                .packDocument(wrapElements(getNotificationPack(caseData, M, c100StaticDocs)))
                .partyIds(CaseUtils.getPartyIdList(caseData.getRespondents()))
                .servedBy(UNREPRESENTED_APPLICANT)
                .personalServiceBy(SoaCitizenServingRespondentsEnum.unrepresentedApplicant.toString())
                .packCreatedDate(CaseUtils.getCurrentDate())
                .build());
        } else {
            log.info("personal service - court bailiff/court admin");
            whoIsResponsibleForServing = SoaCitizenServingRespondentsEnum.courtBailiff
                .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptions())
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
                                                                   List<Document> docs) {
        caseData.getApplicants().forEach(selectedApplicant -> {
            if (!CaseUtils.hasLegalRepresentation(selectedApplicant.getValue())) {
                if (ContactPreferences.email.equals(selectedApplicant.getValue().getContactPreferences())) {
                    Map<String, String> fieldsMap = new HashMap<>();
                    fieldsMap.put(AUTHORIZATION, authorization);
                    fieldsMap.put(COVER_LETTER_TEMPLATE, PRL_LET_ENG_AP7);
                    sendEmailToCitizenLipPersonalServiceCaDa(caseData,
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
                        docs, //C9 to be sent for all applicants
                        bulkPrintDetails,
                        selectedApplicant,
                        PRL_LET_ENG_AP7
                    );
                }
            }
        });
    }

    private void notifyC100PersonalServiceUnRepApplicantAfterConfCheckSuccess(String authorization,
                                                                   CaseData caseData,
                                                                   List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                                   List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                                   List<Document> docs) {
        List<Element<CoverLetterMap>> coverLetterMap = caseData.getServiceOfApplication().getUnServedApplicantPack().getCoverLettersMap();
        caseData.getApplicants().forEach(selectedApplicant -> {
            if (!CaseUtils.hasLegalRepresentation(selectedApplicant.getValue())) {
                if (ContactPreferences.email.equals(selectedApplicant.getValue().getContactPreferences())) {
                    Map<String, String> fieldsMap = new HashMap<>();
                    fieldsMap.put(AUTHORIZATION, authorization);
                    sendEmailToCitizenLipByCheckingDashboardAccess(caseData,
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
                    sendPostWithAccessCodeLetterToParty(
                        caseData,
                        authorization,
                        docs, //C9 to be sent for all applicants
                        bulkPrintDetails,
                        selectedApplicant,
                        CaseUtils.getCoverLettersForParty(selectedApplicant.getId(), coverLetterMap).get(0),
                        SERVED_PARTY_APPLICANT
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
                    sendEmailToCitizenLipPersonalServiceCaDa(
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

    private void notifyC100ApplicantsPersonalServiceCaCbAfterConfCheckSuccessful(String authorization,
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
                    sendEmailToCitizenLipByCheckingDashboardAccess(
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
                    sendPostWithAccessCodeLetterToParty(
                        caseData,
                        authorization,
                        packDocs,
                        bulkPrintDetails,
                        applicant,
                        CaseUtils.getCoverLettersForParty(applicant.getId(),
                                                          caseData.getServiceOfApplication()
                                                              .getUnServedApplicantPack()
                                                              .getCoverLettersMap()).get(0),
                        SERVED_PARTY_APPLICANT
                    );
                }
            } else {
                log.info("Sending notification to applicant solicitor");
                sendEmailToApplicantSolicitor(caseData, authorization, packDocs, SERVED_PARTY_APPLICANT_SOLICITOR,
                                                                              emailNotificationDetails, applicant);
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
        sendGovNotifyEmail(caseData, party, emailTemplate);
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
            .timeStamp(CaseUtils.getCurrentDate())
            .build();
    }

    private void sendGovNotifyEmail(CaseData caseData, Element<PartyDetails> party, EmailTemplateNames emailTemplate) {
        serviceOfApplicationEmailService.sendGovNotifyEmail(
            LanguagePreference.getPreferenceLanguage(caseData),
            party.getValue().getEmail(),
            emailTemplate,
            serviceOfApplicationEmailService.buildCitizenEmailVars(
                caseData,
                party.getValue(),
                YesOrNo.Yes.equals(doesC1aExists(caseData)) ? "Yes" : null
            )
        );
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
        return sendEmailViaSendGridWithAttachedDocsToParty(
            authorization,
            caseData,
            packDocs,
            party,
            emailTemplate,
            packsWithCoverLetter
        );
    }

    private EmailNotificationDetails sendEmailViaSendGridWithAttachedDocsToParty(String authorization, CaseData caseData,
                                                                                 List<Document> packDocs, Element<PartyDetails> party,
                                                                                 SendgridEmailTemplateNames emailTemplate,
                                                                                 List<Document> packsWithCoverLetter) {
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
        List<Element<CoverLetterMap>> coverLetters = new ArrayList<>();
        List<Document> packkDocs = generatePartiesPackDocsWithCoverLetter(authorization, caseData, caseData.getRespondents(),
                                                                          PRL_LET_ENG_RE5, PrlAppsConstants.K, c100StaticDocs,
                                                                          coverLetters
        );
        return SoaPack.builder()
            .packDocument(wrapElements(packkDocs))
            .partyIds(CaseUtils.getPartyIdList(caseData.getRespondents()))
            .servedBy(PRL_COURT_ADMIN)
            .packCreatedDate(CaseUtils.getCurrentDate())
            .personalServiceBy(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptions().toString())
            .coverLettersMap(coverLetters)
            .build();
    }

    private void sendEmailToCitizenLipPersonalServiceCaDa(CaseData caseData,
                                                          List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                          Element<PartyDetails> party,
                                                          List<Document> docs,
                                                          SendgridEmailTemplateNames emailTemplate,
                                                          Map<String, String> fieldMap,
                                                          EmailTemplateNames notifyTemplate) {
        EmailNotificationDetails emailNotification;
        if (CaseUtils.isCitizenAccessEnabled(party.getValue())) {
            log.debug("Applicant has access to dashboard -> send gov notify email for {}", party.getId());
            emailNotification = sendEmailToUnrepresentedApplicant(fieldMap.get(AUTHORIZATION),
                                                                  caseData,
                                                                  docs,
                                                                  party,
                                                                  fieldMap.get(COVER_LETTER_TEMPLATE),
                                                                  notifyTemplate);
        } else {
            log.debug("Applicant does not access to dashboard -> send packs via sendgrid email for {}", party.getId());
            emailNotification = sendSoaPacksToPartyViaEmail(fieldMap.get(AUTHORIZATION),
                                                            caseData,
                                                            docs,
                                                            party,
                                                            fieldMap.get(COVER_LETTER_TEMPLATE),
                                                            emailTemplate);
        }

        if (emailNotification != null) {
            emailNotificationDetails.add(element(emailNotification.toBuilder()
                                                     .partyIds(String.valueOf(party.getId()))
                                                     .build()));
        }
    }

    private void sendEmailToCitizenLipByCheckingDashboardAccess(CaseData caseData,
                                                          List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                          Element<PartyDetails> party,
                                                          List<Document> docs,
                                                          SendgridEmailTemplateNames emailTemplate,
                                                          Map<String, String> fieldMap,
                                                          EmailTemplateNames notifyTemplate) {
        EmailNotificationDetails emailNotification;
        List<Element<CoverLetterMap>> coverLettersPack = caseData.getServiceOfApplication().getUnServedApplicantPack().getCoverLettersMap();
        List<Document> packDocs = new ArrayList<>();
        List<Document> coverLetters = CaseUtils.getCoverLettersForParty(party.getId(), coverLettersPack);
        if (CollectionUtils.isNotEmpty(coverLetters)) {
            packDocs.addAll(coverLetters);
        }
        packDocs.addAll(docs);
        if (CaseUtils.isCitizenAccessEnabled(party.getValue())) {
            log.debug("Applicant has access to dashboard -> send gov notify email for {}", party.getId());
            sendGovNotifyEmail(caseData, party, notifyTemplate);
            emailNotification = EmailNotificationDetails.builder()
                .emailAddress(party.getValue().getEmail())
                .servedParty(SERVED_PARTY_APPLICANT)
                .docs(wrapElements(packDocs))
                .attachedDocs(CITIZEN_CAN_VIEW_ONLINE)
                .timeStamp(CaseUtils.getCurrentDate())
                .build();
        } else {
            log.debug("Applicant does not access to dashboard -> send packs via sendgrid email for {}", party.getId());
            emailNotification = sendEmailViaSendGridWithAttachedDocsToParty(
                fieldMap.get(AUTHORIZATION),
                caseData,
                packDocs,
                party,
                emailTemplate,
                packDocs
            );
        }

        if (emailNotification != null) {
            emailNotificationDetails.add(element(emailNotification.toBuilder()
                                                     .partyIds(String.valueOf(party.getId()))
                                                     .build()));
        }
    }

    public Map<String, Object> handleAboutToSubmit(CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();
        if (caseData.getServiceOfApplication() != null && SoaCitizenServingRespondentsEnum.unrepresentedApplicant
            .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptions())) {
            if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                caseData.getApplicants().get(0).getValue().getResponse().getCitizenFlags().setIsApplicationToBeServed(YesOrNo.Yes);
                caseDataMap.put(APPLICANTS, caseData.getApplicants());
            } else {
                caseData.getApplicantsFL401().getResponse().getCitizenFlags().setIsApplicationToBeServed(YesOrNo.Yes);
                caseDataMap.put(PrlAppsConstants.FL401_APPLICANTS, caseData.getApplicantsFL401());
            }
        }

        caseDataMap.put(CASE_INVITES, generateCaseInvitesForParties(caseData));
        caseDataMap.putAll(setSoaOrConfidentialWaFields(caseData, callbackRequest.getEventId()));
        //PRL-5566 - Set FM5 notification flag to No during SOA
        caseDataMap.put("fm5RemindersSent", "NO");
        //PRL-3466 - auto link citizen case if conf check is not required
        autoLinkCitizenCase(caseData, caseDataMap, callbackRequest.getEventId());

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
            soaWaMap.put(IS_C8_CHECK_NEEDED, isC8CheckNeeded);
        } else if (Event.CONFIDENTIAL_CHECK.getId().equals(eventId)) {
            soaWaMap.put(IS_C8_CHECK_APPROVED, (caseData.getServiceOfApplication().getApplicationServedYesNo() != null
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
        if (YesNoNotApplicable.Yes.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())) {
            if (CaseUtils.isCitizenCase(caseData)) {
                responsibleForService = caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptions().getId();
            } else {
                responsibleForService = caseData.getServiceOfApplication().getSoaServingRespondentsOptions().getId();
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

        if (launchDarklyClient.isFeatureEnabled("generate-pin")) {
            //TEMP SOLUTION TO GET ACCESS CODES - GENERATE AND SEND ACCESS CODE TO APPLICANTS & RESPONDENTS OVER EMAIL
            caseData = caseInviteManager.sendAccessCodeNotificationEmail(caseData);
            //TEMP SOLUTION TO GET ACCESS CODES - GENERATE AND SEND ACCESS CODE TO APPLICANTS & RESPONDENTS OVER EMAIL
        }

        if (isRespondentDetailsConfidential(caseData) || CaseUtils.isC8Present(caseData)) {
            return processConfidentialDetailsSoa(authorisation, caseDataMap, caseData, startAllTabsUpdateDataContent);
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
        Map<String,String> confirmationBanner = new HashMap<>();
        getConfirmationBanner(caseData, confirmationBanner);
        String confirmationBody = confirmationBanner.get(CONFIRMATION_BODY);
        confirmationBody = String.format(confirmationBody, manageCaseUrl + PrlAppsConstants.URL_STRING
            + caseData.getId() + SERVICE_OF_APPLICATION_ENDPOINT);
        List<Element<ServedApplicationDetails>> finalServedApplicationDetailsList;
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

        allTabService.submitAllTabsUpdate(
                updatedCaseDataContent.authorisation(),
                caseId,
                updatedCaseDataContent.startEventResponse(),
                updatedCaseDataContent.eventRequestData(),
                caseDataMap
        );
        return ok(SubmittedCallbackResponse.builder()
                      .confirmationHeader(confirmationBanner.get(CONFIRMATION_HEADER))
                      .confirmationBody(confirmationBody).build());
    }

    private void getConfirmationBanner(CaseData caseData, Map<String, String> confirmationBanner) {
        if (caseData.getServiceOfApplication().getSoaServeToRespondentOptions() != null
            && YesNoNotApplicable.No.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())) {
            confirmationBanner.put(CONFIRMATION_BODY, CONFIRMATION_BODY_PREFIX);
            confirmationBanner.put(CONFIRMATION_HEADER, CONFIRMATION_HEADER_NON_PERSONAL);
        } else {
            confirmationBanner.put(CONFIRMATION_HEADER, CONFIRMATION_HEADER_PERSONAL);
            if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                if (SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative
                    .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptions())
                    || SoaCitizenServingRespondentsEnum.unrepresentedApplicant
                    .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptions())) {
                    confirmationBanner.put(CONFIRMATION_BODY, CONFIRMATION_BODY_APPLICANT_LR_SERVICE_PREFIX_CA);
                } else if (SoaCitizenServingRespondentsEnum.courtAdmin
                    .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptions())
                    || SoaSolicitorServingRespondentsEnum.courtAdmin
                    .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptions())) {
                    confirmationBanner.put(CONFIRMATION_BODY, CONFIRMATION_BODY_COURT_ADMIN_SERVICE_PREFIX_CA);
                } else if (SoaCitizenServingRespondentsEnum.courtBailiff
                    .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptions())
                    || SoaSolicitorServingRespondentsEnum.courtBailiff
                    .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptions())) {
                    confirmationBanner.put(CONFIRMATION_BODY, CONFIRMATION_BODY_BAILIFF_SERVICE_PREFIX_CA);
                }
            } else {
                if (SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative
                    .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptions())
                    || SoaCitizenServingRespondentsEnum.unrepresentedApplicant
                    .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptions())) {
                    confirmationBanner.put(CONFIRMATION_BODY, CONFIRMATION_BODY_APPLICANT_LR_SERVICE_PREFIX_DA);
                } else if (SoaSolicitorServingRespondentsEnum.courtAdmin
                    .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptions())
                    || SoaCitizenServingRespondentsEnum.courtAdmin
                    .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptions())) {
                    confirmationBanner.put(CONFIRMATION_BODY, CONFIRMATION_BODY_COURT_ADMIN_SERVICE_PREFIX_DA);
                } else if (SoaSolicitorServingRespondentsEnum.courtBailiff
                    .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptions())
                    || SoaCitizenServingRespondentsEnum.courtBailiff
                    .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptions())) {
                    confirmationBanner.put(CONFIRMATION_BODY, CONFIRMATION_BODY_BAILIFF_SERVICE_PREFIX_DA);
                }
            }
        }
    }

    private ResponseEntity<SubmittedCallbackResponse> processConfidentialDetailsSoa(String authorisation, Map<String, Object> caseDataMap,
                                                                                    CaseData caseData,
                                                                                    StartAllTabsUpdateDataContent updatedCaseDataContent) {
        if (CaseUtils.getCaseTypeOfApplication(caseData).equalsIgnoreCase(C100_CASE_TYPE)) {
            generatePacksForConfidentialCheckC100(authorisation, caseData, caseDataMap);
        } else {
            generatePacksForConfidentialCheckFl401(authorisation, caseData, caseDataMap);
        }
        cleanUpSoaSelections(caseDataMap);
        //SAVE TEMP GENERATED ACCESS CODE
        caseDataMap.put(CASE_INVITES, caseData.getCaseInvites());

        allTabService.submitAllTabsUpdate(
                updatedCaseDataContent.authorisation(),
                String.valueOf(caseData.getId()),
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
                checkAndAddC8docsToBeServedToLocalAuthority(caseData, docs);
            }
            return docs;
        }
        return Collections.emptyList();
    }

    private void checkAndAddC8docsToBeServedToLocalAuthority(CaseData caseData, List<Document> docs) {
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

    private List<Element<EmailNotificationDetails>> sendNotificationsNonPersonalApplicantsC100(String authorization,
                                                                                             List<Element<PartyDetails>> selectedApplicants,
                                                                                             CaseData caseData,
                                                                                             List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                                                             List<Document> packDocs) {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        selectedApplicants.forEach(applicant -> {
            if (CaseUtils.hasLegalRepresentation(applicant.getValue())) {
                sendEmailToApplicantSolicitor(caseData, authorization, packDocs, SERVED_PARTY_APPLICANT_SOLICITOR,
                                              emailNotificationDetails,
                                              applicant);
            } else {
                if (ContactPreferences.email.equals(applicant.getValue().getContactPreferences())) {
                    Map<String, String> fieldsMap = new HashMap<>();
                    fieldsMap.put(AUTHORIZATION, authorization);
                    sendEmailToCitizenLipByCheckingDashboardAccess(caseData,
                                                             emailNotificationDetails,
                                                                   applicant,
                                                             packDocs,
                                                             SendgridEmailTemplateNames.SOA_CA_NON_PERSONAL_SERVICE_APPLICANT_LIP,
                                                             fieldsMap,
                                                             doesC1aExists(caseData).equals(Yes)
                                                                 ? SOA_CA_PERSONAL_UNREPRESENTED_APPLICANT
                                                                 : SOA_CA_PERSONAL_UNREPRESENTED_APPLICANT_WITHOUT_C1A
                    );
                } else {
                    sendPostWithAccessCodeLetterToParty(caseData,
                                                        authorization,
                                                        packDocs,
                                                        bulkPrintDetails,
                                                        applicant,
                                                        CaseUtils.getCoverLettersForParty(applicant.getId(),
                                                                                          caseData.getServiceOfApplication()
                                                                                              .getUnServedApplicantPack()
                                                                                              .getCoverLettersMap()).get(0),
                                                        SERVED_PARTY_APPLICANT
                    );
                }
            }
        });
        return emailNotificationDetails;
    }

    private void sendNotificationsNonPersonalApplicantFl401(String authorization, List<Element<PartyDetails>> selectedApplicants,
                                                            CaseData caseData, List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                            List<Element<BulkPrintDetails>> bulkPrintDetails, List<Document> packDocs) {
        log.info("Sending notification to DA Applicant {}", selectedApplicants);
        selectedApplicants.forEach(applicant -> {
            if (CaseUtils.hasLegalRepresentation(applicant.getValue())) {
                sendEmailToApplicantSolicitor(caseData, authorization, packDocs, SERVED_PARTY_APPLICANT_SOLICITOR,
                                              emailNotificationDetails,
                                              applicant);
            } else {
                if (ContactPreferences.email.equals(applicant.getValue().getContactPreferences())) {
                    Map<String, String> fieldsMap = new HashMap<>();
                    fieldsMap.put(AUTHORIZATION, authorization);
                    sendEmailToCitizenLipByCheckingDashboardAccess(caseData,
                                                             emailNotificationDetails,
                                                             applicant,
                                                             packDocs,
                                                             SendgridEmailTemplateNames.SOA_DA_NON_PERSONAL_SERVICE_APPLICANT_LIP,
                                                             fieldsMap,
                                                             SOA_CA_PERSONAL_UNREPRESENTED_APPLICANT_WITHOUT_C1A
                    );
                } else {
                    sendPostWithAccessCodeLetterToParty(caseData,
                                                        authorization,
                                                        packDocs,
                                                        bulkPrintDetails,
                                                        applicant,
                                                        CaseUtils.getCoverLettersForParty(applicant.getId(),
                                                                                          caseData.getServiceOfApplication()
                                                                                              .getUnServedApplicantPack()
                                                                                              .getCoverLettersMap()).get(0),
                                                        SERVED_PARTY_APPLICANT
                    );
                }
            }
        });
        log.info("email notification details {}", emailNotificationDetails);
        log.info("bulk print details {}", bulkPrintDetails);
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

    private List<Element<PartyDetails>> getSelectedApplicantsOrRespondentsElements(List<Element<PartyDetails>> applicantsOrRespondents,
                                                                                   List<DynamicMultiselectListElement> value) {
        return applicantsOrRespondents.stream().filter(element -> value.stream().anyMatch(party -> element.getId().toString().equals(
            party.getCode()))).toList();
    }

    public List<Element<EmailNotificationDetails>> sendNotificationToApplicantSolicitor(CaseData caseData, String authorization,
                                                                                        List<Element<PartyDetails>> selectedApplicants,
                                                                                        List<Document> packQ, String servedParty) {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        selectedApplicants.forEach(applicant -> {
            if (applicant.getValue().getSolicitorEmail() != null) {
                sendEmailToApplicantSolicitor(caseData, authorization, packQ, servedParty, emailNotificationDetails, applicant);
            }
        });
        return emailNotificationDetails;
    }

    private void sendEmailToApplicantSolicitor(CaseData caseData, String authorization, List<Document> packQ, String servedParty,
                                               List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                               Element<PartyDetails> party) {
        try {
            log.info(
                "Sending the email notification to applicant solicitor for C100 Application for caseId {}",
                caseData.getId()
            );
            Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
            dynamicData.put("name", party.getValue().getRepresentativeFullName());
            dynamicData.put(DASH_BOARD_LINK, manageCaseUrl + PrlAppsConstants.URL_STRING + caseData.getId());
            populateLanguageMap(caseData, dynamicData);
            EmailNotificationDetails emailNotification = serviceOfApplicationEmailService
                .sendEmailUsingTemplateWithAttachments(
                    authorization, party.getValue().getSolicitorEmail(),
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

    public void sendPostWithAccessCodeLetterToParty(CaseData caseData, String authorization, List<Document> packDocs,
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
            log.info("*** Docs to applicant Lip post {}", docs);
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
        return CollectionUtils.isNotEmpty(soaSelectedOrders)
            ? soaSelectedOrders.stream().filter(d -> !(SOA_C6A_OTHER_PARTIES_ORDER.equalsIgnoreCase(d.getDocumentFileName())
                || SOA_C6A_OTHER_PARTIES_ORDER_WELSH.equalsIgnoreCase(d.getDocumentFileName())))
            .collect(Collectors.toList()) : Collections.emptyList();
    }

    private List<Document> generatePackH(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(staticDocs.stream()
                        .filter(d -> !(SOA_NOTICE_SAFETY.equalsIgnoreCase(d.getDocumentFileName())
                            || C1A_BLANK_DOCUMENT_FILENAME.equalsIgnoreCase(d.getDocumentFileName())
                            || C1A_BLANK_DOCUMENT_WELSH_FILENAME.equalsIgnoreCase(d.getDocumentFileName())
                            || C7_BLANK_DOCUMENT_FILENAME.equalsIgnoreCase(d.getDocumentFileName())))
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
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_C9_PERSONAL_SERVICE_FILENAME))
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
        List<Document> witnessStatements = new ArrayList<>();
        if (!CollectionUtils.isEmpty(caseData.getFl401UploadWitnessDocuments())) {
            witnessStatements.addAll(ElementUtils.unwrapElements(
                caseData.getFl401UploadWitnessDocuments()));
        }
        if (YesOrNo.Yes.equals(caseData.getIsCourtNavCase())
            && !CollectionUtils.isEmpty(caseData.getReviewDocuments().getCourtNavUploadedDocListDocTab())) {
            caseData.getReviewDocuments().getCourtNavUploadedDocListDocTab().stream()
                .map(Element::getValue)
                .forEach(
                    document -> {
                        if ("WITNESS_STATEMENT".equalsIgnoreCase(document.getDocumentType())) {
                            witnessStatements.add(document.getApplicantStatementsDocument());
                        }
                    });

        }
        return witnessStatements;
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
            if (null != caseData.getFinalDocument()) {
                docs.add(caseData.getFinalDocument());
            }
            if (null != caseData.getFinalWelshDocument()) {
                docs.add(caseData.getFinalWelshDocument());
            }
        }
        log.info("case docs {}", docs);
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
        log.info("uploaded docs {}", docs);
        return docs;
    }

    private List<Document> getSoaSelectedOrders(CaseData caseData) {
        List<Document> selectedOrders = new ArrayList<>();
        log.info("SOA Screen 1 {}", caseData.getServiceOfApplicationScreen1());
        log.info("orders  {}", caseData.getOrderCollection());

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
                                           if (documentLanguage.isGenEng() && null != o.getValue().getOrderDocument()) {
                                               selectedOrders.add(o.getValue().getOrderDocument());
                                           }
                                           if (documentLanguage.isGenWelsh() && null != o.getValue().getOrderDocumentWelsh()) {
                                               selectedOrders.add(o.getValue().getOrderDocumentWelsh());
                                           }
                                       }));
            log.info("Selected orders {}", selectedOrders);
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
            "soaServingRespondentsOptions",
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
        caseDataUpdated.put(DISPLAY_LEGAL_REP_OPTION, CaseUtils.isCitizenCase(caseData) ? "No" : "Yes");
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

    public Document fetchCoverLetter(String authorisation, String template, Map<String, Object> dataMap) {
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
        if (C100_CASE_TYPE.equals(CaseUtils.getCaseTypeOfApplication(caseData))) {
            dataMap.put("applicantName", caseData.getApplicants().get(0).getValue().getLabelForDynamicList());
        }
        if (launchDarklyClient.isFeatureEnabled(ENABLE_CITIZEN_ACCESS_CODE_IN_COVER_LETTER)) {
            dataMap.put("isCitizen", CaseUtils.isCitizenCase(caseData));
            // This check is added to disable or enable DA citizen journey as needed
            if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
                && !launchDarklyClient.isFeatureEnabled(PrlAppsConstants.CITIZEN_ALLOW_DA_JOURNEY)) {
                dataMap.put("isCitizen", false);
            }
        }
        return dataMap;
    }

    private AccessCode getAccessCode(CaseInvite caseInvite, Address address, String name) {
        String code = null;
        String isLinked = null;
        if (null != caseInvite && launchDarklyClient.isFeatureEnabled(
            ENABLE_CITIZEN_ACCESS_CODE_IN_COVER_LETTER)) {
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

    public CaseInvite getCaseInvite(UUID partyId, List<Element<CaseInvite>> caseInvites) {
        if (CollectionUtils.isNotEmpty(caseInvites)) {
            Optional<Element<CaseInvite>> caseInvite = caseInvites.stream()
                .filter(caseInviteElement -> Optional.ofNullable(caseInviteElement.getValue().getPartyId()).isPresent())
                .filter(caseInviteElement -> caseInviteElement.getValue().getPartyId().equals(partyId)
            ).findFirst();
            if (caseInvite.isPresent()) {
                return caseInvite.map(Element::getValue).orElse(null);
            }
        }
        return null;
    }

    public List<Element<CaseInvite>> generateCaseInvitesForParties(CaseData caseData) {
        List<Element<CaseInvite>> caseInvites = caseData.getCaseInvites();
        if (CollectionUtils.isEmpty(caseInvites)) {
            caseInvites =  new ArrayList<>();
            if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                List<Element<CaseInvite>> finalCaseInvites = caseInvites;
                caseData.getApplicants().forEach(party -> finalCaseInvites.add(element(c100CaseInviteService.generateCaseInvite(party, Yes))));
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

    public void generatePacksForConfidentialCheckC100(String authorization, CaseData caseData, Map<String, Object> caseDataMap) {
        log.info("Inside generatePacks for confidential check C100 method");
        List<Document> c100StaticDocs = serviceOfApplicationPostService.getStaticDocs(authorization,
                                                                                      CaseUtils.getCaseTypeOfApplication(caseData),
                                                                                      caseData);
        if (YesNoNotApplicable.No.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())
            && (caseData.getServiceOfApplication().getSoaRecipientsOptions() != null)
            && (!caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue().isEmpty())) {
            c100StaticDocs = buildPacksConfidentialCheckC100NonPersonal(authorization, caseDataMap, caseData,
                                                                        c100StaticDocs);
        } else if (YesNoNotApplicable.Yes.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())) {
            buildPacksConfidentialCheckC100Personal(authorization, caseDataMap, caseData, c100StaticDocs);
        }
        //serving other people in the case
        if (null != caseData.getServiceOfApplication().getSoaOtherParties()
            && !caseData.getServiceOfApplication().getSoaOtherParties().getValue().isEmpty()) {
            buildUnservedOthersPack(authorization, caseDataMap, caseData, c100StaticDocs);
        } else {
            caseDataMap.put(UNSERVED_OTHERS_PACK, null);
        }

        //generate packs for Cafcass cymru
        if (YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaCafcassCymruServedOptions())
            && StringUtils.isNotEmpty(caseData.getServiceOfApplication().getSoaCafcassCymruEmail())) {
            caseDataMap.put(UNSERVED_CAFCASS_CYMRU_PACK, SoaPack.builder()
                    .partyIds(List.of(element(caseData.getServiceOfApplication().getSoaCafcassCymruEmail())))
                    .servedBy(userService.getUserDetails(authorization).getFullName())
                    .servedPartyEmail(caseData.getServiceOfApplication().getSoaCafcassCymruEmail())
                .build());
        }

        //serving Local authority in the case
        if (YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaServeLocalAuthorityYesOrNo())
            && null != caseData.getServiceOfApplication().getSoaLaEmailAddress()) {
            List<Document> docsForLa = getDocsToBeServedToLa(authorization, caseData);
            if (CollectionUtils.isNotEmpty(docsForLa)) {
                caseDataMap.put(UNSERVED_LA_PACK, SoaPack.builder().packDocument(wrapElements(docsForLa))
                    .servedBy(userService.getUserDetails(authorization).getFullName())
                    .packCreatedDate(CaseUtils.getCurrentDate())
                    .partyIds(List.of(element(caseData.getServiceOfApplication().getSoaLaEmailAddress())))
                    .build());
            }
        } else {
            caseDataMap.put(UNSERVED_LA_PACK, null);
        }
    }

    private void buildPacksConfidentialCheckC100Personal(String authorization,
                                                         Map<String, Object> caseDataUpdated,
                                                         CaseData caseData,
                                                         List<Document> c100StaticDocs) {
        if (SoaSolicitorServingRespondentsEnum.courtAdmin
            .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptions())
            || SoaSolicitorServingRespondentsEnum.courtBailiff
            .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptions())) {
            generateUnServedPacksForCourtAdminBailiff(authorization, caseDataUpdated, caseData, c100StaticDocs, false,
                                                      caseData.getServiceOfApplication().getSoaServingRespondentsOptions().toString());
        } else if (SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative
            .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptions())) {
            List<Document> packIDocs = new ArrayList<>();
            List<Element<CoverLetterMap>> coverLetterMap = new ArrayList<>();
            caseData.getRespondents().forEach(respondent -> {
                List<Document> coverLetters = new ArrayList<>();
                coverLetters.add(generateAccessCodeLetter(
                    authorization,
                    caseData, respondent, null, PRL_LET_ENG_C100_RE6
                ));
                packIDocs.addAll(coverLetters);
                CaseUtils.mapCoverLetterToTheParty(respondent.getId(), coverLetterMap, coverLetters);
            });
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
                .packCreatedDate(CaseUtils.getCurrentDate())
                .coverLettersMap(coverLetterMap)
                .build();
            caseDataUpdated.put(UNSERVED_RESPONDENT_PACK, unservedRespondentPack);
            final SoaPack unServedApplicantPack = SoaPack.builder()
                .packDocument(wrapElements(packHDocs))
                .partyIds(CaseUtils.getPartyIdList(caseData.getRespondents()))
                .servedBy(SERVED_PARTY_APPLICANT_SOLICITOR)
                .personalServiceBy(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative.toString())
                .packCreatedDate(CaseUtils.getCurrentDate())
                .build();
            caseDataUpdated.put(UNSERVED_APPLICANT_PACK, unServedApplicantPack);
        } else if (SoaCitizenServingRespondentsEnum.unrepresentedApplicant
            .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptions())) {
            caseDataUpdated.put(UNSERVED_APPLICANT_PACK, generatePacksForApplicantLipC100Personal(authorization, caseData,
                                                                                                  c100StaticDocs));
            caseDataUpdated.put(UNSERVED_APPLICANT_LIP_RESPONDENT_PACK, SoaPack.builder()
                .packDocument(wrapElements(getNotificationPack(caseData, PrlAppsConstants.M, c100StaticDocs)))
                .partyIds(CaseUtils.getPartyIdList(caseData.getRespondents()))
                .servedBy(UNREPRESENTED_APPLICANT)
                .personalServiceBy(SoaCitizenServingRespondentsEnum.unrepresentedApplicant.toString())
                .packCreatedDate(CaseUtils.getCurrentDate())
                .build());
        } else if (SoaCitizenServingRespondentsEnum.courtAdmin
            .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptions())
            || SoaCitizenServingRespondentsEnum.courtBailiff
            .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptions())) {
            generateUnServedPacksForCourtAdminBailiff(authorization, caseDataUpdated, caseData, c100StaticDocs, true,
                                                      caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptions().toString());
        }
        log.info("casedataupdated packs {} *** {}", caseDataUpdated.get(UNSERVED_APPLICANT_PACK), caseDataUpdated.get(UNSERVED_RESPONDENT_PACK));
    }

    private void generateUnServedPacksForCourtAdminBailiff(String authorization,
                                                           Map<String, Object> caseDataUpdated,
                                                           CaseData caseData,
                                                           List<Document> c100StaticDocs,
                                                           boolean isCitizen,
                                                           String serviceBy) {

        List<Element<CoverLetterMap>> respondentCoverLetterMap = new ArrayList<>();
        List<Element<CoverLetterMap>> applicantCoverLetterMap = new ArrayList<>();

        List<Document> packkDocs = generatePartiesPackDocsWithCoverLetter(authorization, caseData, caseData.getRespondents(),
                                                                                    PRL_LET_ENG_RE5, PrlAppsConstants.K, c100StaticDocs,
                                                                          respondentCoverLetterMap);
        List<Document> packjDocs = generatePartiesPackDocsWithCoverLetter(authorization, caseData, caseData.getApplicants(),
                                                           PRL_LET_ENG_AP8, PrlAppsConstants.J, c100StaticDocs,
                                                           applicantCoverLetterMap
        );
        final SoaPack unservedRespondentPack = SoaPack.builder()
            .packDocument(wrapElements(packkDocs))
            .partyIds(CaseUtils.getPartyIdList(caseData.getRespondents()))
            .servedBy(PRL_COURT_ADMIN)
            .personalServiceBy(serviceBy)
            .packCreatedDate(CaseUtils.getCurrentDate())
            .coverLettersMap(respondentCoverLetterMap)
            .build();
        caseDataUpdated.put(UNSERVED_RESPONDENT_PACK, unservedRespondentPack);
        final SoaPack unServedApplicantPack = SoaPack.builder()
            .packDocument(wrapElements(packjDocs))
            .partyIds(isCitizen
                          ? CaseUtils.getPartyIdList(caseData.getApplicants())
                          : wrapElements(caseData.getApplicants().get(0).getId().toString()))
            .servedBy(PRL_COURT_ADMIN)
            .personalServiceBy(serviceBy)
            .packCreatedDate(CaseUtils.getCurrentDate())
            .coverLettersMap(applicantCoverLetterMap)
            .build();
        caseDataUpdated.put(UNSERVED_APPLICANT_PACK, unServedApplicantPack);
    }

    private List<Document> generatePartiesPackDocsWithCoverLetter(String authorization,
                                                                  CaseData caseData,
                                                                  List<Element<PartyDetails>> parties,
                                                                  String template,
                                                                  String requiredPack,
                                                                  List<Document> c100StaticDocs,
                                                                  List<Element<CoverLetterMap>> coverLetterMap) {
        List<Document> packDocs = new ArrayList<>();
        parties.forEach(party -> {
            if (!CaseUtils.hasLegalRepresentation(party.getValue())) {
                List<Document> coverLetters = new ArrayList<>();
                coverLetters.add(generateCoverLetterBasedOnCaseAccess(authorization, caseData, party, template));
                packDocs.addAll(coverLetters);
                CaseUtils.mapCoverLetterToTheParty(party.getId(), coverLetterMap, coverLetters);
            }
        });
        packDocs.addAll(getNotificationPack(caseData, requiredPack, c100StaticDocs));
        return packDocs;
    }

    private SoaPack generatePacksForApplicantLipC100Personal(String authorization, CaseData caseData,
                                                             List<Document> c100StaticDocs) {
        List<Document> packLdocs = new ArrayList<>();
        List<Element<CoverLetterMap>> coverLetterMap = new ArrayList<>();
        caseData.getApplicants().forEach(applicant -> {
            if (!CaseUtils.hasLegalRepresentation(applicant.getValue())) {
                List<Document> coverLetters = new ArrayList<>();
                coverLetters.add(generateCoverLetterBasedOnCaseAccess(authorization, caseData,
                                                                      applicant, PRL_LET_ENG_AP7));
                packLdocs.addAll(coverLetters);
                CaseUtils.mapCoverLetterToTheParty(applicant.getId(), coverLetterMap, coverLetters);
            }
        });
        packLdocs.addAll(getNotificationPack(caseData, L, c100StaticDocs));
        return SoaPack.builder()
            .packDocument(wrapElements(packLdocs))
            .partyIds(wrapElements(caseData.getApplicants().get(0).getId().toString()))
            .servedBy(UNREPRESENTED_APPLICANT)
            .personalServiceBy(SoaCitizenServingRespondentsEnum.unrepresentedApplicant.toString())
            .packCreatedDate(CaseUtils.getCurrentDate())
            .coverLettersMap(coverLetterMap)
            .build();
    }

    public Document generateCoverLetterBasedOnCaseAccess(String authorization,
                                                         CaseData caseData,
                                                         Element<PartyDetails> party,
                                                         String template) {
        Map<String, Object> dataMap;
        CaseInvite caseInvite = null;
        if (!CaseUtils.isCitizenAccessEnabled(party.getValue()) && !CaseUtils.hasLegalRepresentation(party.getValue())) {
            caseInvite = getCaseInvite(party.getId(), caseData.getCaseInvites());
        }
        dataMap = populateAccessCodeMap(caseData, party, caseInvite);
        return fetchCoverLetter(authorization, template, dataMap);
    }

    private List<Document> buildPacksConfidentialCheckC100NonPersonal(String authorization,
                                                                      Map<String, Object> caseDataUpdated,
                                                                      CaseData caseData,
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
                c100StaticDocs, selectedRespondents);
        } else {
            caseDataUpdated.put(UNSERVED_RESPONDENT_PACK, null);
        }
        return c100StaticDocs;
    }

    public void generatePacksForConfidentialCheckFl401(String authorization, CaseData caseData, Map<String, Object> caseDataUpdated) {
        log.info("Inside generatePacksForConfidentialCheck FL401 Method");
        List<Document> fl401StaticDocs = serviceOfApplicationPostService.getStaticDocs(authorization,
                                                                                       CaseUtils.getCaseTypeOfApplication(caseData),
                                                                                       caseData);
        if (YesNoNotApplicable.No.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())
            && (caseData.getServiceOfApplication().getSoaRecipientsOptions() != null)
            && (!caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue().isEmpty())) {
            caseDataUpdated.putAll(getPacksForConfidentialCheckDaNonPersonalService(authorization, caseData,
                                                                                    fl401StaticDocs));
        } else {
            if (SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative
                .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptions())) {
                caseDataUpdated.putAll(getPacksForConfidentialCheckDaApplicantSolicitor(authorization, caseData,
                                                                                        fl401StaticDocs));
            } else if (SoaSolicitorServingRespondentsEnum.courtAdmin
                .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptions())
                || SoaSolicitorServingRespondentsEnum.courtBailiff
                .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptions())) {
                getPacksForConfidentialCheckDaCourtAdminAndBailiff(caseData, caseDataUpdated, fl401StaticDocs, authorization,
                                                                   caseData.getServiceOfApplication().getSoaServingRespondentsOptions().toString());
            } else if (SoaCitizenServingRespondentsEnum.courtBailiff
                .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptions())
                || SoaCitizenServingRespondentsEnum.courtAdmin
                .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptions())) {
                getPacksForConfidentialCheckDaCourtAdminAndBailiff(caseData, caseDataUpdated, fl401StaticDocs, authorization,
                                                                   caseData.getServiceOfApplication()
                                                                       .getSoaCitizenServingRespondentsOptions().toString());
            } else if (SoaCitizenServingRespondentsEnum.unrepresentedApplicant
                .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptions())) {
                generatePacksForConfidentialCheckDaApplicantLip(authorization, caseData, caseDataUpdated, fl401StaticDocs);

            }
        }
    }

    private Map<String, Object> getPacksForConfidentialCheckDaNonPersonalService(String authorization, CaseData caseData,
                                                                                 List<Document> fl401StaticDocs) {
        log.info("serving Fl401 Non personal service with confidential check");
        // Applicants pack
        List<Element<PartyDetails>> applicantFl401 = Arrays.asList(element(caseData.getApplicantsFL401().getPartyId(),
                                                                           caseData.getApplicantsFL401()));
        List<Element<PartyDetails>> respondentFl401 = Arrays.asList(element(caseData.getRespondentsFL401().getPartyId(),
                                                                            caseData.getRespondentsFL401()));

        applicantFl401 = getSelectedApplicantsOrRespondentsElements(applicantFl401, caseData.getServiceOfApplication()
                                                                                                        .getSoaRecipientsOptions().getValue());
        respondentFl401 = getSelectedApplicantsOrRespondentsElements(respondentFl401, caseData.getServiceOfApplication()
            .getSoaRecipientsOptions().getValue());
        fl401StaticDocs = fl401StaticDocs.stream().filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_FL415_FILENAME))
            .toList();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        if (CollectionUtils.isNotEmpty(applicantFl401)) {
            List<Document> docs = new ArrayList<>();
            List<Element<CoverLetterMap>> coverLetterMap = new ArrayList<>();
            String partyId = String.valueOf(applicantFl401.get(0).getValue().getSolicitorPartyId());
            if (!CaseUtils.hasLegalRepresentation(applicantFl401.get(0).getValue())) {
                log.info("applicant lip");
                partyId = String.valueOf(applicantFl401.get(0).getId());
                Document coverletter = generateCoverLetterBasedOnCaseAccess(authorization, caseData, applicantFl401.get(0), PRL_LET_ENG_AP2);
                docs.add(coverletter);
                coverLetterMap.add(element(UUID.fromString(partyId), CoverLetterMap.builder()
                    .coverLetters(List.of(element(coverletter))).build()));
            }
            docs.addAll(getNotificationPack(caseData, PrlAppsConstants.A, fl401StaticDocs));
            final SoaPack unServedApplicantPack = SoaPack.builder()
                .packDocument(wrapElements(docs))
                .partyIds(wrapElements(partyId))
                .servedBy(userService.getUserDetails(authorization).getFullName())
                .packCreatedDate(CaseUtils.getCurrentDate())
                .coverLettersMap(coverLetterMap)
                .build();
            caseDataUpdated.put(UNSERVED_APPLICANT_PACK, unServedApplicantPack);
        }
        if (CollectionUtils.isNotEmpty(respondentFl401)) {
            String partyId = String.valueOf(respondentFl401.get(0).getValue().getSolicitorPartyId());
            List<Document> docs = new ArrayList<>();
            List<Element<CoverLetterMap>> coverLetterMap = new ArrayList<>();
            if (!CaseUtils.hasLegalRepresentation(respondentFl401.get(0).getValue())) {
                log.info("respondent lip");
                partyId = String.valueOf(respondentFl401.get(0).getId());
                Document coverLetter = getRe1OrRe4BasedOnWithOrWithoutNotice(caseData, authorization, respondentFl401.get(0));
                docs.add(coverLetter);
                coverLetterMap.add(element(UUID.fromString(partyId), CoverLetterMap.builder()
                    .coverLetters(List.of(element(coverLetter))).build()));
            }
            docs.addAll(getNotificationPack(caseData, PrlAppsConstants.A, fl401StaticDocs));
            final SoaPack unServedRespondentPack = SoaPack.builder()
                .packDocument(wrapElements(docs))
                .partyIds(wrapElements(partyId))
                .servedBy(userService.getUserDetails(authorization).getFullName())
                .packCreatedDate(CaseUtils.getCurrentDate())
                .coverLettersMap(coverLetterMap)
                .build();
            caseDataUpdated.put(UNSERVED_RESPONDENT_PACK, unServedRespondentPack);
        }
        return caseDataUpdated;
    }

    private void generatePacksForConfidentialCheckDaApplicantLip(String authorization, CaseData caseData,
                                                                 Map<String, Object> caseDataUpdated,
                                                                 List<Document> fl401StaticDocs) {
        Element<PartyDetails> applicant = element(caseData.getApplicantsFL401().getPartyId(), caseData.getApplicantsFL401());
        Element<PartyDetails> respondent = element(caseData.getRespondentsFL401().getPartyId(), caseData.getRespondentsFL401());
        Document applicantCoverLetter = generateCoverLetterBasedOnCaseAccess(authorization, caseData, applicant, Templates.PRL_LET_ENG_AP1);
        List<Document> packEDocs = new ArrayList<>();
        packEDocs.add(applicantCoverLetter);
        Document respondentCoverLetter;
        packEDocs.addAll(getNotificationPack(caseData, PrlAppsConstants.E, fl401StaticDocs));
        fl401StaticDocs = fl401StaticDocs.stream().filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_FL415_FILENAME))
            .toList();
        List<Document> packFDocs = getNotificationPack(caseData, PrlAppsConstants.F, fl401StaticDocs);
        if (Yes.equals(caseData.getDoYouNeedAWithoutNoticeHearing())) {
            respondentCoverLetter = generateAccessCodeLetter(authorization, caseData, respondent, null, PRL_LET_ENG_FL401_RE2);
        } else {
            respondentCoverLetter = generateAccessCodeLetter(authorization, caseData, respondent, null, PRL_LET_ENG_FL401_RE3);
        }
        packFDocs.add(respondentCoverLetter);
        final SoaPack unservedRespondentPack = SoaPack.builder().packDocument(wrapElements(packFDocs))
            .partyIds(wrapElements(caseData.getRespondentsFL401().getPartyId().toString()))
            .servedBy(UNREPRESENTED_APPLICANT)
            .personalServiceBy(SoaCitizenServingRespondentsEnum.unrepresentedApplicant.toString())
            .packCreatedDate(CaseUtils.getCurrentDate())
            .coverLettersMap(List.of(element(applicant.getId(), CoverLetterMap.builder()
                                                 .coverLetters(List.of(element(applicantCoverLetter))).build())))
            .build();
        caseDataUpdated.put(UNSERVED_APPLICANT_LIP_RESPONDENT_PACK, unservedRespondentPack);
        final SoaPack unServedApplicantPack = SoaPack.builder()
            .packDocument(wrapElements(packEDocs))
            .partyIds(wrapElements(caseData.getApplicantsFL401().getPartyId().toString()))
            .servedBy(UNREPRESENTED_APPLICANT)
            .personalServiceBy(SoaCitizenServingRespondentsEnum.unrepresentedApplicant.toString())
            .coverLettersMap(List.of(element(respondent.getId(), CoverLetterMap.builder()
                .coverLetters(List.of(element(respondentCoverLetter))).build())))
            .packCreatedDate(CaseUtils.getCurrentDate())
            .build();
        caseDataUpdated.put(UNSERVED_APPLICANT_PACK, unServedApplicantPack);
    }

    private void getPacksForConfidentialCheckDaCourtAdminAndBailiff(CaseData caseData, Map<String, Object> caseDataUpdated,
                                                                    List<Document> fl401StaticDocs,
                                                                    String authorization, String personalServiceBy) {
        log.info("serving Fl401 court admin or court bailiff with confidential check");
        List<Element<CoverLetterMap>> coverLetterMap = new ArrayList<>();
        List<Document> packdDocs = getRespondentPacksForDaPersonaServiceByCourtAdminAndBailiff(
            caseData,
            authorization,
            fl401StaticDocs,
            coverLetterMap
        );
        packdDocs = packdDocs.stream().filter(d -> !SOA_FL415_FILENAME.equalsIgnoreCase(d.getDocumentFileName()))
            .toList();
        final SoaPack unservedRespondentPack = SoaPack.builder().packDocument(wrapElements(packdDocs))
            .partyIds(wrapElements(caseData.getRespondentsFL401().getPartyId().toString()))
            .servedBy(PRL_COURT_ADMIN)
            .packCreatedDate(CaseUtils.getCurrentDate())
            .personalServiceBy(personalServiceBy)
            .coverLettersMap(coverLetterMap)
            .build();
        caseDataUpdated.put(UNSERVED_RESPONDENT_PACK, unservedRespondentPack);
        List<Document> packcDocs = new ArrayList<>();
        List<Element<CoverLetterMap>> applicantCoverLetters = new ArrayList<>();
        if (!CaseUtils.hasLegalRepresentation(caseData.getApplicantsFL401())) {
            Document coverLetter = generateCoverLetterBasedOnCaseAccess(authorization, caseData,
                                                 element(caseData.getApplicantsFL401().getPartyId(),
                                                         caseData.getApplicantsFL401()), PRL_LET_ENG_AP2);
            applicantCoverLetters.add(element(caseData.getApplicantsFL401().getPartyId(), CoverLetterMap.builder()
                .coverLetters(List.of(element(coverLetter)))
                .build()));
            packcDocs.add(coverLetter);
        }
        packcDocs.addAll(getNotificationPack(caseData, PrlAppsConstants.C, fl401StaticDocs));
        final SoaPack unServedApplicantPack = SoaPack.builder()
            .packDocument(wrapElements(packcDocs))
            .partyIds(wrapElements(caseData.getApplicantsFL401().getPartyId().toString()))
            .servedBy(PRL_COURT_ADMIN)
            .packCreatedDate(CaseUtils.getCurrentDate())
            .personalServiceBy(personalServiceBy)
            .coverLettersMap(applicantCoverLetters)
            .build();
        caseDataUpdated.put(UNSERVED_APPLICANT_PACK, unServedApplicantPack);
    }

    private Map<String, Object> getPacksForConfidentialCheckDaApplicantSolicitor(String authorization, CaseData caseData,
                                                                                 List<Document> fl401StaticDocs) {
        log.info("serving Fl401 applicant legal representative with confidential check");
        // Applicants pack
        Map<String, Object> caseDataUpdated = new HashMap<>();
        final String partyId = caseData.getApplicantsFL401().getPartyId().toString();
        List<Document> packADocs = getNotificationPack(caseData, PrlAppsConstants.A, fl401StaticDocs);
        final SoaPack unServedApplicantPack = SoaPack.builder().packDocument(wrapElements(packADocs))
            .partyIds(wrapElements(partyId))
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .packCreatedDate(CaseUtils.getCurrentDate())
            .personalServiceBy(caseData.getServiceOfApplication().getSoaServingRespondentsOptions().toString())
            .build();
        caseDataUpdated.put(UNSERVED_APPLICANT_PACK, unServedApplicantPack);
        fl401StaticDocs = fl401StaticDocs.stream().filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_FL415_FILENAME))
            .toList();
        List<Document> packBDocs = getNotificationPack(caseData, PrlAppsConstants.B, fl401StaticDocs);
        List<Document> reLetters = new ArrayList<>();
        Element<PartyDetails> respondent = Element.<PartyDetails>builder()
            .id(caseData.getRespondentsFL401().getPartyId())
            .value(caseData.getRespondentsFL401())
            .build();
        getCoverLetterForDaApplicantSolicitor(caseData, authorization, reLetters, respondent);
        List<Element<CoverLetterMap>> coverletterMap = new ArrayList<>();
        coverletterMap.add(element(respondent.getId(), CoverLetterMap.builder()
            .coverLetters(wrapElements(reLetters)).build()));
        reLetters.addAll(packBDocs);
        final SoaPack unServedRespondentPack = SoaPack.builder().packDocument(wrapElements(reLetters)).partyIds(
                wrapElements(caseData.getRespondentsFL401().getPartyId().toString()))
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .personalServiceBy(caseData.getServiceOfApplication().getSoaServingRespondentsOptions().toString())
            .packCreatedDate(CaseUtils.getCurrentDate())
            .coverLettersMap(coverletterMap)
            .build();
        caseDataUpdated.put(UNSERVED_RESPONDENT_PACK, unServedRespondentPack);
        return caseDataUpdated;
    }

    private void buildUnservedOthersPack(String authorization, Map<String, Object> caseDataUpdated, CaseData caseData,
                                         List<Document> c100StaticDocs) {
        final List<DynamicMultiselectListElement> otherParties = getSelectedApplicantsOrRespondents(
            CaseUtils.getOthersToNotifyInCase(caseData),
            caseData.getServiceOfApplication().getSoaOtherParties().getValue()
        );
        final List<String> othersPartyIds = otherParties.stream().map(DynamicMultiselectListElement::getCode).collect(
            Collectors.toList());
        List<Document> packNDocs = getNotificationPack(caseData, PrlAppsConstants.N, c100StaticDocs);
        final SoaPack unServedOthersPack = SoaPack.builder().packDocument(wrapElements(packNDocs))
            .partyIds(wrapElements(othersPartyIds))
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .packCreatedDate(CaseUtils.getCurrentDate())
            .build();

        caseDataUpdated.put(UNSERVED_OTHERS_PACK, unServedOthersPack);
    }

    private void buildUnservedRespondentPackC100NonPersonal(String authorization, Map<String, Object> caseDataUpdated, CaseData caseData,
                                                            List<Document> c100StaticDocs, List<DynamicMultiselectListElement> selectedRespondents) {
        final List<String> selectedPartyIds = selectedRespondents.stream().map(DynamicMultiselectListElement::getCode).collect(
            Collectors.toList());
        List<Document> finalDocs = new ArrayList<>();
        List<Element<CoverLetterMap>> coverLetterMap = new ArrayList<>();
        selectedPartyIds.forEach(partyId -> {
            Optional<Element<PartyDetails>> party = getParty(partyId, caseData.getRespondents());
            if (party.isPresent() && !CaseUtils.hasLegalRepresentation(party.get().getValue())) {
                List<Document> coverLetters = new ArrayList<>();
                coverLetters.add(generateAccessCodeLetter(authorization, caseData, party.get(),
                                                          null, PRL_LET_ENG_RE5));
                finalDocs.addAll(coverLetters);
                CaseUtils.mapCoverLetterToTheParty(UUID.fromString(partyId), coverLetterMap, coverLetters);
            }
        });
        finalDocs.addAll(getNotificationPack(caseData, PrlAppsConstants.R, c100StaticDocs));
        final SoaPack unServedRespondentPack = SoaPack.builder()
            .packDocument(wrapElements(finalDocs))
            .partyIds(wrapElements(selectedPartyIds))
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .packCreatedDate(CaseUtils.getCurrentDate())
            .coverLettersMap(coverLetterMap)
            .build();
        caseDataUpdated.put(UNSERVED_RESPONDENT_PACK, unServedRespondentPack);
    }

    private void buildUnservedApplicantPackC100NonPersonal(String authorization, Map<String, Object> caseDataUpdated, CaseData caseData,
                                                           List<Document> c100StaticDocs, List<DynamicMultiselectListElement> selectedApplicants) {
        final List<String> selectedPartyIds = selectedApplicants.stream().map(DynamicMultiselectListElement::getCode).collect(
            Collectors.toList());
        List<Element<Document>> packDocs = new ArrayList<>();
        List<Element<CoverLetterMap>> coverLetterMap = new ArrayList<>();
        for (String partyId : selectedPartyIds) {
            Optional<Element<PartyDetails>> party = getParty(partyId, caseData.getApplicants());
            if (party.isPresent()) {
                if (!CaseUtils.hasLegalRepresentation(party.get().getValue())) {
                    List<Document> coverLetters = new ArrayList<>();
                    coverLetters.add(generateCoverLetterBasedOnCaseAccess(
                        authorization,
                        caseData,
                        party.get(),
                        Templates.PRL_LET_ENG_AP6
                    ));
                    packDocs.add(element(coverLetters.get(0)));
                    CaseUtils.mapCoverLetterToTheParty(UUID.fromString(partyId), coverLetterMap, coverLetters);
                }
                packDocs.addAll(wrapElements(getNotificationPack(caseData, PrlAppsConstants.Q, c100StaticDocs)));
            }
        }
        final SoaPack unServedApplicantPack = SoaPack.builder().packDocument(packDocs).partyIds(
            wrapElements(selectedPartyIds))
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .packCreatedDate(CaseUtils.getCurrentDate())
            .coverLettersMap(coverLetterMap)
            .build();
        caseDataUpdated.put(UNSERVED_APPLICANT_PACK, unServedApplicantPack);
    }

    public CaseData sendNotificationsAfterConfidentialCheckSuccessful(CaseData caseData, String authorization) {
        //Suppressed java:S6541 , suppression will be removed after refactoring in the IP sprint.
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
        // send notification for others
        final SoaPack unServedOthersPack = caseData.getServiceOfApplication().getUnServedOthersPack();

        if (ObjectUtils.isNotEmpty(unServedOthersPack) && CollectionUtils.isNotEmpty(unServedOthersPack.getPartyIds())) {
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
        List<Element<ServedApplicationDetails>> finalServedApplicationDetailsList;
        if (CollectionUtils.isNotEmpty(caseData.getFinalServedApplicationDetailsList())) {
            finalServedApplicationDetailsList = caseData.getFinalServedApplicationDetailsList();
        } else {
            finalServedApplicationDetailsList = new ArrayList<>();
        }
        String whoIsResponsible = handleNotificationsForApplicantsAndRespondents(caseData, authorization, emailNotificationDetails,
                                                                                 bulkPrintDetails);
        finalServedApplicationDetailsList.add(element(ServedApplicationDetails.builder().emailNotificationDetails(
            emailNotificationDetails)
                                                          .servedBy(userService.getUserDetails(authorization).getFullName())
                                                          .servedAt(CaseUtils.getCurrentDate())
                                                          .modeOfService(CaseUtils.getModeOfService(
                                                              emailNotificationDetails,
                                                              bulkPrintDetails
                                                          ))
                                                          .whoIsResponsible(whoIsResponsible)
                                                          .bulkPrintDetails(bulkPrintDetails).build()));
        caseData.setFinalServedApplicationDetailsList(finalServedApplicationDetailsList);
        return caseData;
    }

    private String handleNotificationsForApplicantsAndRespondents(CaseData caseData, String authorization,
                                                                  List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                                  List<Element<BulkPrintDetails>> bulkPrintDetails) {
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
                                                                                                                 authorization,
                                                                                                                 unServedApplicantPack,
                                                                                                                 unServedRespondentPack);
                if (emailNotification != null) {
                    emailNotificationDetails.add(element(emailNotification));
                }
                whoIsResponsible = SERVED_PARTY_APPLICANT_SOLICITOR;
            } else if (unServedApplicantPack != null
                && SoaCitizenServingRespondentsEnum.unrepresentedApplicant.toString().equalsIgnoreCase(
                unServedApplicantPack.getPersonalServiceBy())) {
                whoIsResponsible = sendNotificationsAfterConfCheckPersonalServiceApplicantLip(
                    caseData,
                    authorization,
                    emailNotificationDetails,
                    bulkPrintDetails,
                    unServedApplicantPack,
                    unServedRespondentPack
                );
            } else {
                if (null != unServedApplicantPack
                    && CollectionUtils.isNotEmpty(unServedApplicantPack.getPackDocument())) {
                    sendNotificationForUnservedApplicantPack(caseData, authorization, emailNotificationDetails,
                                                             unServedApplicantPack, bulkPrintDetails
                    );
                    if (unServedApplicantPack.getPersonalServiceBy() != null) {
                        whoIsResponsible = SoaSolicitorServingRespondentsEnum.courtAdmin
                            .toString().equalsIgnoreCase(unServedApplicantPack.getPersonalServiceBy())
                            ? PERSONAL_SERVICE_SERVED_BY_CA : PERSONAL_SERVICE_SERVED_BY_BAILIFF;
                    }
                }
                handleNonPersonalServiceForRespondentsAfterConfCheckSuccessful(caseData,
                                                                               authorization,
                                                                               emailNotificationDetails,
                                                                               bulkPrintDetails, unServedRespondentPack);
            }
        }
        return whoIsResponsible;
    }

    private void handleNonPersonalServiceForRespondentsAfterConfCheckSuccessful(CaseData caseData, String authorization,
                                                                                List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                                                List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                                                SoaPack unServedRespondentPack) {
        if (ObjectUtils.isNotEmpty(unServedRespondentPack) && null == unServedRespondentPack.getPersonalServiceBy()) {
            final List<Element<String>> partyIds = unServedRespondentPack.getPartyIds();
            final List<DynamicMultiselectListElement> respondentList = createPartyDynamicMultiSelectListElement(
                partyIds);
            final List<Document> respondentDocs = unwrapElements(unServedRespondentPack.getPackDocument());
            if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                sendNotificationToRespondentOrSolicitorNonPersonalConfCheckSuccess(
                    caseData,
                    authorization,
                    emailNotificationDetails,
                    bulkPrintDetails,
                    respondentList,
                    respondentDocs,
                    respondentDocs
                );
            } else {
                if (CaseUtils.hasLegalRepresentation(caseData.getRespondentsFL401())) {
                    log.info("respondent is represented -> serving notification to solicitor");
                    Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
                    dynamicData.put(SEND_GRID_TEMPLATE, SendgridEmailTemplateNames.SOA_SERVE_APPLICANT_SOLICITOR_NONPER_PER_CA_CB);
                    sendEmailDaNonPersonalService(
                        caseData,
                        authorization,
                        emailNotificationDetails,
                        caseData.getRespondentsFL401().getSolicitorEmail(),
                        caseData.getRespondentsFL401().getRepresentativeFullName(),
                        removeCoverLettersFromThePacks(respondentDocs),
                        dynamicData
                    );
                } else {
                    log.info("respondent is not represented");
                    handleDaNonPersonalServiceRespondentOnConfCheckSuccessful(
                        caseData,
                        authorization,
                        emailNotificationDetails,
                        bulkPrintDetails,
                        removeCoverLettersFromThePacks(respondentDocs)
                    );
                }

            }
        }
    }

    private void handleDaNonPersonalServiceRespondentOnConfCheckSuccessful(CaseData caseData, String authorization,
                                                                           List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                                           List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                                           List<Document> respondentDocs) {
        log.info("Da non personal service to respondent");
        Element<PartyDetails> respondent = element(
            caseData.getRespondentsFL401().getPartyId(),
            caseData.getRespondentsFL401());
        if (ContactPreferences.email.equals(caseData.getRespondentsFL401().getContactPreferences())) {
            Map<String, String> fieldsMap = new HashMap<>();
            fieldsMap.put(AUTHORIZATION, authorization);
            sendEmailToCitizenLipByCheckingDashboardAccess(
                caseData,
                emailNotificationDetails,
                respondent,
                respondentDocs,
                SendgridEmailTemplateNames.SOA_DA_NON_PERSONAL_SERVICE_APPLICANT_LIP,
                fieldsMap,
                SOA_CA_PERSONAL_UNREPRESENTED_APPLICANT_WITHOUT_C1A
            );
        } else {
            sendPostWithAccessCodeLetterToParty(
                caseData,
                authorization,
                respondentDocs,
                bulkPrintDetails,
                respondent,
                CaseUtils.getCoverLettersForParty(respondent.getId(),
                                                  caseData.getServiceOfApplication()
                                                      .getUnServedApplicantPack()
                                                      .getCoverLettersMap()).get(0),
                respondent.getValue().getLabelForDynamicList()
            );
        }
    }

    private String sendNotificationsAfterConfCheckPersonalServiceApplicantLip(CaseData caseData, String authorization,
                                                                              List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                                              List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                                              SoaPack unServedApplicantPack, SoaPack unServedRespondentPack) {
        String whoIsResponsible;
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            notifyC100PersonalServiceUnRepApplicantAfterConfCheckSuccess(
                authorization,
                caseData,
                emailNotificationDetails,
                bulkPrintDetails,
                removeCoverLettersFromThePacks(unwrapElements(unServedApplicantPack.getPackDocument())));
        } else {
            List<Document> packEDocuments = removeCoverLettersFromThePacks(
                unwrapElements(unServedApplicantPack.getPackDocument()));
            assert unServedRespondentPack != null;
            List<Document> packFDocuments = unwrapElements(unServedRespondentPack.getPackDocument());
            List<Document> docs = new ArrayList<>();
            removeDuplicatesAndGetConsolidatedDocs(packEDocuments, packFDocuments, docs);
            if (ContactPreferences.email.equals(caseData.getApplicantsFL401().getContactPreferences())) {
                Map<String, String> fieldsMap = new HashMap<>();
                fieldsMap.put(AUTHORIZATION, authorization);
                sendEmailToCitizenLipByCheckingDashboardAccess(
                    caseData,
                    emailNotificationDetails,
                    element(caseData.getApplicantsFL401().getPartyId(), caseData.getApplicantsFL401()),
                    docs,
                    SendgridEmailTemplateNames.SOA_DA_APPLICANT_LIP_PERSONAL,
                    fieldsMap,
                    EmailTemplateNames.SOA_UNREPRESENTED_APPLICANT_COURTNAV
                );
            } else {
                sendPostWithAccessCodeLetterToParty(caseData, authorization, docs,
                                           bulkPrintDetails,
                                           element(caseData.getApplicantsFL401().getPartyId(), caseData.getApplicantsFL401()),
                                                    CaseUtils.getCoverLettersForParty(caseData.getApplicantsFL401().getPartyId(),
                                                                                      caseData.getServiceOfApplication()
                                                                                          .getUnServedApplicantPack()
                                                                                          .getCoverLettersMap()).get(0),
                                                    SERVED_PARTY_APPLICANT);
            }
        }

        whoIsResponsible = UNREPRESENTED_APPLICANT;
        return whoIsResponsible;
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
            authorization, getSelectedApplicantsOrRespondentsElements(CaseUtils.getOthersToNotifyInCase(caseData), otherPartyList),
            unwrapElements(unServedOthersPack.getPackDocument()),
            PrlAppsConstants.SERVED_PARTY_OTHER
        ));
    }

    private void sendNotificationForUnservedApplicantPack(CaseData caseData, String authorization,
                                                          List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                          SoaPack unServedApplicantPack,
                                                          List<Element<BulkPrintDetails>> bulkPrintDetails) {
        List<Element<PartyDetails>> applicantList = getSelectedApplicantsOrRespondentsElements(
            caseData.getApplicants(),
            createPartyDynamicMultiSelectListElement(unServedApplicantPack.getPartyIds())
        );
        if (FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            applicantList = Arrays.asList(element(caseData.getApplicantsFL401().getPartyId(), caseData.getApplicantsFL401()));
        }
        List<Document> packDocs = new ArrayList<>(unwrapElements(unServedApplicantPack.getPackDocument()));
        if (SoaCitizenServingRespondentsEnum.courtAdmin.toString().equalsIgnoreCase(
            unServedApplicantPack.getPersonalServiceBy())
            || SoaCitizenServingRespondentsEnum.courtBailiff.toString().equalsIgnoreCase(
            unServedApplicantPack.getPersonalServiceBy())) {
            log.info("Court admin/ Bailiff personal service - notification to applicant");
            if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                //remove cover letters & notify applicants
                notifyC100ApplicantsPersonalServiceCaCbAfterConfCheckSuccessful(authorization,
                                                                     caseData,
                                                                     emailNotificationDetails,
                                                                     bulkPrintDetails,
                                                                     removeCoverLettersFromThePacks(packDocs)
                );
            } else {
                notifyFl401ApplicantPersonalServiceCaCbAfterConfCheckSuccessful(caseData, authorization, emailNotificationDetails,
                                                                                bulkPrintDetails, packDocs);
            }
        } else {
            log.info("Non personal service - notification to applicant");
            if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                emailNotificationDetails.addAll(sendNotificationsNonPersonalApplicantsC100(
                    authorization,
                    applicantList,
                    caseData,
                    bulkPrintDetails,
                    removeCoverLettersFromThePacks(packDocs))
                );
            } else {
                sendNotificationsNonPersonalApplicantFl401(
                        authorization,
                        Arrays.asList(element(caseData.getApplicantsFL401().getPartyId(), caseData.getApplicantsFL401())),
                        caseData,
                        emailNotificationDetails,
                        bulkPrintDetails,
                        removeCoverLettersFromThePacks(packDocs));
            }
        }
    }

    private void notifyFl401ApplicantPersonalServiceCaCbAfterConfCheckSuccessful(CaseData caseData, String authorization,
                                                                                 List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                                                 List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                                                 List<Document> packDocs) {
        if (CaseUtils.hasLegalRepresentation(caseData.getApplicantsFL401())) {
            sendEmailToApplicantSolicitor(caseData, authorization, packDocs, SERVED_PARTY_APPLICANT_SOLICITOR,
                                          emailNotificationDetails,
                                          element(caseData.getApplicantsFL401().getPartyId(), caseData.getApplicantsFL401()));
        } else {
            if (ContactPreferences.email.equals(caseData.getApplicantsFL401().getContactPreferences())) {
                Map<String, String> fieldsMap = new HashMap<>();
                fieldsMap.put(AUTHORIZATION, authorization);
                sendEmailToCitizenLipByCheckingDashboardAccess(
                    caseData,
                    emailNotificationDetails,
                    element(caseData.getApplicantsFL401().getPartyId(), caseData.getApplicantsFL401()),
                    packDocs,
                    SendgridEmailTemplateNames.SOA_SERVE_APPLICANT_SOLICITOR_NONPER_PER_CA_CB,
                    fieldsMap,
                    EmailTemplateNames.SOA_DA_PERSONAL_CB_CA_UNREPRESENTED_APPLICANT_COURTNAV
                );
            } else {
                //Post packs to applicants
                sendPostWithAccessCodeLetterToParty(
                    caseData,
                    authorization,
                    packDocs,
                    bulkPrintDetails,
                    element(caseData.getApplicantsFL401().getPartyId(), caseData.getApplicantsFL401()),
                    CaseUtils.getCoverLettersForParty(caseData.getApplicantsFL401().getPartyId(),
                                                      caseData.getServiceOfApplication().getUnServedApplicantPack()
                                                          .getCoverLettersMap()).get(0),
                    SERVED_PARTY_APPLICANT
                );
            }
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
            response = handleConfidentialCheckSuccessful(authorisation, caseData, caseDataMap);
            CaseUtils.setCaseState(callbackRequest, caseDataMap);
        } else {
            response = rejectPacksWithConfidentialDetails(caseData, caseDataMap);
            caseDataMap.put(UNSERVED_RESPONDENT_PACK, null);
            caseDataMap.put(UNSERVED_APPLICANT_LIP_RESPONDENT_PACK, null);
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
        final ConfidentialCheckFailed confidentialCheckFailed = ConfidentialCheckFailed.builder().confidentialityCheckRejectReason(
                caseData.getServiceOfApplication().getRejectionReason())
            .dateRejected(CaseUtils.getCurrentDate())
            .build();

        confidentialCheckFailedList.add(ElementUtils.element(confidentialCheckFailed));

        caseDataMap.put(CONFIDENTIAL_CHECK_FAILED, confidentialCheckFailedList);

        response = ok(SubmittedCallbackResponse.builder()
                      .confirmationHeader(RETURNED_TO_ADMIN_HEADER)
                      .confirmationBody(
                          CONFIDENTIAL_CONFIRMATION_NO_BODY_PREFIX).build());
        return response;
    }

    private ResponseEntity<SubmittedCallbackResponse> handleConfidentialCheckSuccessful(String authorisation, CaseData caseData,
                                                                                        Map<String, Object> caseDataMap) {
        final ResponseEntity<SubmittedCallbackResponse> response;
        String confirmationHeader;
        String confirmationBody;
        caseData = sendNotificationsAfterConfidentialCheckSuccessful(
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

    public List<Document> getCoverLetters(String authorization,
                                          CaseData caseData,
                                          Element<PartyDetails> party,
                                          String templateHint,
                                          boolean isAccessCodeNeeded) {
        CaseInvite caseInvite = null;
        if (isAccessCodeNeeded
            && !CaseUtils.hasDashboardAccess(party)
            && !CaseUtils.hasLegalRepresentation(party.getValue())) {
            caseInvite = getCaseInvite(party.getId(), caseData.getCaseInvites());
        }
        Map<String, Object> dataMap = populateAccessCodeMap(caseData, party, caseInvite);

        return getCoverLetters(authorization,
                               caseData,
                               templateHint,
                               dataMap);
    }

    public List<Document> getCoverLetters(String authorization,
                                          CaseData caseData,
                                          String templateHint,
                                          Map<String, Object> dataMap) {
        List<Document> coverLetters = new ArrayList<>();
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        //English
        if (documentLanguage.isGenEng()) {
            coverLetters.add(fetchCoverLetter(authorization, getCoverLetterTemplate(templateHint, false), dataMap));
        }
        //Welsh
        if (documentLanguage.isGenWelsh) {
            coverLetters.add(fetchCoverLetter(authorization, getCoverLetterTemplate(templateHint, true), dataMap));
        }

        return coverLetters;
    }

    private String getCoverLetterTemplate(String templateHint,
                                          boolean isWelsh) {
        return switch (templateHint) {
            case RE7_HINT -> getRe7Template(isWelsh);
            case RE8_HINT -> getRe8Template(isWelsh);
            case AP13_HINT -> getAp13Template(isWelsh);
            case AP14_HINT -> getAp14Template(isWelsh);
            case AP15_HINT -> getAp15Template(isWelsh);

            default -> "";
        };
    }

    private String getRe7Template(boolean isWelsh) {
        return isWelsh ? PRL_LET_WEL_C100_RE7 : PRL_LET_ENG_C100_RE7;
    }

    private String getRe8Template(boolean isWelsh) {
        return isWelsh ? PRL_LET_WEL_FL401_RE8 : PRL_LET_ENG_FL401_RE8;
    }

    private String getAp13Template(boolean isWelsh) {
        return isWelsh ? PRL_LET_ENG_C100_AP13 : PRL_LET_WEL_C100_AP13;
    }

    private String getAp14Template(boolean isWelsh) {
        return isWelsh ? PRL_LET_ENG_C100_AP14 : PRL_LET_WEL_C100_AP14;
    }

    private String getAp15Template(boolean isWelsh) {
        return isWelsh ? PRL_LET_ENG_C100_AP15 : PRL_LET_WEL_C100_AP15;
    }

    /**
     * Auto link citizen case
     * 1. After SOA event & no confidential check required.
     * 2. After Confidential check event is approved.
     */
    public void autoLinkCitizenCase(CaseData caseData,
                                    Map<String, Object> caseDataMap,
                                    String eventId) {
        if (isAutoLinkRequired(eventId, caseDataMap)
            && CaseCreatedBy.CITIZEN.equals(caseData.getCaseCreatedBy())) {
            List<Element<PartyDetails>> applicants = new ArrayList<>(caseData.getApplicants());

            applicants.stream()
                .filter(party -> !hasLegalRepresentation(party.getValue())
                    && !hasDashboardAccess(party)
                    && isPartyEmailSameAsIdamEmail(caseData, party))
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

    private boolean isAutoLinkRequired(String eventId,
                                       Map<String, Object> caseDataMap) {
        return ((Event.SOA.getId().equals(eventId)
            && NO.equals(caseDataMap.get(IS_C8_CHECK_NEEDED)))
            || (Event.CONFIDENTIAL_CHECK.getId().equals(eventId)
            && YES.equals(caseDataMap.get(IS_C8_CHECK_APPROVED))));
    }

    private boolean isPartyEmailSameAsIdamEmail(CaseData caseData,
                                                Element<PartyDetails> party) {
        return CollectionUtils.isNotEmpty(caseData.getUserInfo())
            && isNotEmpty(party.getValue().getEmail())
            && party.getValue().getEmail().equalsIgnoreCase(
            caseData.getUserInfo().get(0).getValue().getEmailAddress());
    }
}
