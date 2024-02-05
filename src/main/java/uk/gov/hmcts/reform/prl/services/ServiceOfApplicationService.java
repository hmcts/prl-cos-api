package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.config.templates.Templates;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
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
import uk.gov.hmcts.reform.prl.models.serviceofapplication.AccessCode;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.DocumentListForLa;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.pin.C100CaseInviteService;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.services.pin.FL401CaseInviteService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.ConfidentialDetailsGenerator;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.EmailUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C7_BLANK_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C9_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_CREATED_BY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DD_MMM_YYYY_HH_MM_SS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EUROPE_LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HI;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MISSING_ADDRESS_WARNING_TEXT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OTHER_PEOPLE_SELECTED_C6A_MISSING_ERROR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PRIVACY_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_APPLICATION_SCREEN_1;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_C6A_OTHER_PARTIES_ORDER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_C9_PERSONAL_SERVICE_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_CONFIDENTIAL_DETAILS_PRESENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_CYMRU_EMAIL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_DOCUMENT_PLACE_HOLDER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_FL415_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_FL416_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_ORDER_LIST_EMPTY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_OTHER_PARTIES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_OTHER_PEOPLE_PRESENT_IN_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_RECIPIENT_OPTIONS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_SOLICITOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WARNING_TEXT_DIV;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.services.SendAndReplyService.ARROW_SEPARATOR;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings({"java:S3776","java:S6204","java:S112","java:S4144"})
public class ServiceOfApplicationService {
    public static final String UNSERVED_APPLICANT_PACK = "unServedApplicantPack";
    public static final String UNSERVED_RESPONDENT_PACK = "unServedRespondentPack";
    public static final String UNSERVED_OTHERS_PACK = "unServedOthersPack";
    public static final String UNSERVED_LA_PACK = "unServedLaPack";
    public static final String APPLICATION_SERVED_YES_NO = "applicationServedYesNo";
    public static final String REJECTION_REASON = "rejectionReason";
    public static final String FINAL_SERVED_APPLICATION_DETAILS_LIST = "finalServedApplicationDetailsList";
    public static final String CONFIDENTIAL_CHECK_FAILED = "confidentialCheckFailed";
    public static final String INTERNAL_UPDATE_ALL_TABS = "internal-update-all-tabs";
    public static final String APPLICANTS = "applicants";
    public static final String CASE_INVITES = "caseInvites";

    public static final String FAMILY_MAN_ID = "Family Man ID: ";
    public static final String EMAIL = "email";
    public static final String POST = "post";
    public static final String COURT = "Court";
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
    private final LaunchDarklyClient launchDarklyClient;
    @Value("${xui.url}")
    private String manageCaseUrl;
    public static final String RETURNED_TO_ADMIN_HEADER = "# Application returned to admin";
    public static final String APPLICATION_SERVED_HEADER = "# Application served";
    public static final String CONFIDENTIAL_CONFIRMATION_NO_BODY_PREFIX = """
        ### What happens next
        The application will be served to relevant people in the case""";
    public static final String CONFIDENTIAL_CONFIRMATION_YES_BODY_PREFIX = """
           ### What happens next
           The application cannot be served. The packs will be sent to the filling team to be redacted.""";
    public static final String CONFIDENTIAL_CONFIRMATION_HEADER = "# The application will be reviewed for confidential details";
    public static final String CONFIDENTIAL_CONFIRMATION_BODY_PREFIX = """
        ### What happens next
        The document will be reviewed for confidential details
        """;

    public static final String CONFIRMATION_HEADER = "# The application is served";
    public static final String CONFIRMATION_BODY_PREFIX = "### What happens next \n\n The document packs will be served to parties ";

    private final ServiceOfApplicationEmailService serviceOfApplicationEmailService;
    private final ServiceOfApplicationPostService serviceOfApplicationPostService;
    private final CaseInviteManager caseInviteManager;
    private final C100CaseInviteService c100CaseInviteService;

    @Qualifier("caseSummaryTab")
    private final CaseSummaryTabService caseSummaryTabService;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final FL401CaseInviteService fl401CaseInviteService;
    private final DynamicMultiSelectListService dynamicMultiSelectListService;
    private final WelshCourtEmail welshCourtEmail;
    private final SendAndReplyService sendAndReplyService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final ConfidentialDetailsGenerator confidentialDetailsGenerator;

    private final DgsService dgsService;

    @Value("${citizen.url}")
    private String citizenUrl;

    private final CoreCaseDataService coreCaseDataService;

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
                                                            ? caseData.getOtherPartyInTheCaseRevised() : caseData.getOthersToNotify();
        selectedOthers.forEach(other -> {
            Optional<Element<PartyDetails>> party = getParty(other.getCode(), otherPeopleInCase);
            try {
                log.info(
                    "Sending the post notification to others in case for C100 Application for caseId {}",
                    caseData.getId()
                );

                List<Document> docs = new ArrayList<>();
                if (party.isPresent() && null != party.get().getValue().getAddress()
                    && null != party.get().getValue().getAddress().getAddressLine1()) {
                    docs.add(getCoverSheet(authorization, caseData, party.get().getValue().getAddress(),
                                                party.get().getValue().getLabelForDynamicList()));
                    bulkPrintDetails.add(element(serviceOfApplicationPostService.sendPostNotificationToParty(
                        caseData,
                        authorization,
                        party.get().getValue(),
                        ListUtils.union(docs, packN),
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
                                                                            Map<String, Object> caseDataMap)
        throws Exception {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
        String whoIsResponsibleForServing;

        if (C100_CASE_TYPE.equals(CaseUtils.getCaseTypeOfApplication(caseData))) {
            if (CaseUtils.isCaseCreatedByCitizen(caseData)) {
                whoIsResponsibleForServing = handleNotificationsForCitizenCreatedCase(
                    caseData,
                    authorization,
                    emailNotificationDetails,
                    bulkPrintDetails
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
                        emailNotificationDetails.add(element(serviceOfApplicationEmailService
                                                                 .sendEmailNotificationToLocalAuthority(
                                                                     authorization,
                                                                     caseData,
                                                                     caseData.getServiceOfApplication()
                                                                         .getSoaLaEmailAddress(),
                                                                     docsForLa,
                                                                     PrlAppsConstants.SERVED_PARTY_LOCAL_AUTHORITY
                                                                 )));
                    } catch (IOException e) {
                        log.error("Failed to serve email to Local Authority");
                    }
                }
            }

        } else {
            if (CaseUtils.isCaseCreatedByCitizen(caseData)) {
                whoIsResponsibleForServing = handleNotificationsForCitizenCreatedCase(caseData,
                                                                                      authorization,
                                                                                      emailNotificationDetails,
                                                                                      bulkPrintDetails
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

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE));
        String formatter = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS).format(zonedDateTime);
        log.info("*** Email notification details {}", emailNotificationDetails);
        return ServedApplicationDetails.builder().emailNotificationDetails(emailNotificationDetails)
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .servedAt(formatter)
            .modeOfService(CaseUtils.getModeOfService(emailNotificationDetails, bulkPrintDetails))
            .whoIsResponsible(whoIsResponsibleForServing)
            .bulkPrintDetails(bulkPrintDetails).build();
    }

    private String handleNotificationsForCitizenCreatedCase(CaseData caseData, String authorization,
                                                          List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                          List<Element<BulkPrintDetails>> bulkPrintDetails) {
        //CITIZEN SCENARIO
        String whoIsResponsibleForServing = COURT;
        List<Document> c100StaticDocs = serviceOfApplicationPostService.getStaticDocs(authorization, CaseUtils.getCaseTypeOfApplication(caseData));
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            log.info("Sending service of application notifications to C100 citizens");
            if (YesOrNo.No.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())
                && (caseData.getServiceOfApplication().getSoaRecipientsOptions() != null)
                && (!caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue().isEmpty())) {
                handleNonPersonalServiceForCitizenC100(caseData, authorization, emailNotificationDetails,
                                                   bulkPrintDetails, c100StaticDocs);
            } else {
                log.info(" update who is responsible flag here");
                log.error("#SOA TO DO ... citizen created case personal service");
                handlePersonalServiceForCitizenC100(caseData, authorization, emailNotificationDetails,
                                                   bulkPrintDetails, c100StaticDocs);
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
            } else {
                getNotificationPack(caseData, PrlAppsConstants.C, c100StaticDocs);
                getNotificationPack(caseData, PrlAppsConstants.D, c100StaticDocs);
            }
            log.info("#SOA TO DO ... FL401 citizen created case");
        }
        return whoIsResponsibleForServing;
    }

    private String handleNotificationsCaSolicitorCreatedCase(CaseData caseData, String authorization,
                                                           List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                           List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                             Map<String, Object> caseDataMap) throws Exception {
        String whoIsResponsibleForServing = COURT;
        List<Document> c100StaticDocs = serviceOfApplicationPostService.getStaticDocs(authorization, CaseUtils.getCaseTypeOfApplication(caseData));
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

                emailNotificationDetails.add(element(serviceOfApplicationEmailService
                                                         .sendEmailUsingTemplateWithAttachments(
                                                             authorization, caseData.getApplicants().get(0).getValue().getSolicitorEmail(),
                                                             packHiDocs,
                                                             SendgridEmailTemplateNames.SOA_PERSONAL_CA_DA_APPLICANT_LEGAL_REP,
                                                             dynamicData,
                                                             SERVED_PARTY_APPLICANT_SOLICITOR
                                                         )));
            } else if (SoaSolicitorServingRespondentsEnum.courtBailiff
                .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA())
                || SoaSolicitorServingRespondentsEnum.courtAdmin
                .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA())) {
                handleNotificationsCaSolicitorPersonalCourtAdminBailiff(caseData, authorization, emailNotificationDetails,
                                                                        c100StaticDocs, caseDataMap);
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
            log.info("selected Applicants " + selectedApplicants.size());
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
            log.info("selected respondents " + selectedRespondents.size());
            if (!selectedRespondents.isEmpty()) {
                List<Document> packRDocs = getNotificationPack(caseData, PrlAppsConstants.R, c100StaticDocs);
                List<Document> packSDocs = getNotificationPack(caseData, PrlAppsConstants.S, c100StaticDocs);
                sendNotificationToRespondentNonPersonal(
                    caseData,
                    authorization,
                    emailNotificationDetails,
                    bulkPrintDetails,
                    selectedRespondents,
                    packRDocs,
                    packSDocs
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
        List<Document> packjDocs = getDocumentsForCaOrBailiffToServeApplicantSolcitor(caseData, authorization, c100StaticDocs,
                                                                                      true);
        Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
        dynamicData.put("name", caseData.getApplicants().get(0).getValue().getRepresentativeFullName());
        dynamicData.put(DASH_BOARD_LINK, manageCaseUrl + PrlAppsConstants.URL_STRING + caseData.getId());
        EmailNotificationDetails emailNotification = serviceOfApplicationEmailService.sendEmailUsingTemplateWithAttachments(authorization,
                                                   caseData.getApplicants().get(0).getValue().getSolicitorEmail(),
                                                   packjDocs,
                                                   SendgridEmailTemplateNames.SOA_SERVE_APPLICANT_SOLICITOR_NONPER_PER_CA_CB,
                                                   dynamicData,
                                                   PRL_COURT_ADMIN);
        if (null != emailNotification) {
            emailNotificationDetails.add(element(emailNotification));
        }
        List<Document> packkDocs = getDocumentsForCaorBailiffToServeRespondents(caseData, authorization, c100StaticDocs, true);
        final SoaPack unservedRespondentPack = SoaPack.builder()
            .packDocument(wrapElements(packkDocs))
            .partyIds(CaseUtils.getPartyIdList(caseData.getRespondents()))
            .servedBy(PRL_COURT_ADMIN)
            .packCreatedDate(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS).format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE))))
            .personalServiceBy(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA().toString())
            .build();
        caseDataMap.put(UNSERVED_RESPONDENT_PACK, unservedRespondentPack);
    }

    private void handleNotificationsDaSolicitorPersonalCourtAdminBailiff(CaseData caseData, String authorization,
                                                                         List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                                         List<Document> staticDocs,
                                                                         Map<String, Object> caseDataMap) {
        List<Document> packcDocs = getDocumentsForDaOrBailiffToServeApplicantSolicitor(caseData, staticDocs);
        Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
        //Add dynamicData - name & manageCaseUrl
        dynamicData.put("name", caseData.getApplicantsFL401().getRepresentativeFullName());
        dynamicData.put("dashBoardLink", manageCaseUrl + PrlAppsConstants.URL_STRING + caseData.getId()
            + PrlAppsConstants.URL_STRING + "#Service of application");
        EmailNotificationDetails emailNotification = serviceOfApplicationEmailService.sendEmailUsingTemplateWithAttachments(
            authorization,
            caseData.getApplicantsFL401().getSolicitorEmail(),
            packcDocs,
            SendgridEmailTemplateNames.SOA_SERVE_APPLICANT_SOLICITOR_NONPER_PER_CA_CB,
            dynamicData,
            PRL_COURT_ADMIN
        );
        if (null != emailNotification) {
            emailNotificationDetails.add(element(emailNotification));
        }
        List<Document> packdDocs = getDocumentsForDaorBailiffToServeRespondents(
            caseData,
            authorization,
            staticDocs,
            true
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
                                                                        List<Document> c100StaticDocs, boolean attachLetters) {
        List<Document> re5Letters = new ArrayList<>();
        if (attachLetters) {
            for (Element<PartyDetails> respondent: caseData.getRespondents()) {
                re5Letters.add(generateAccessCodeLetter(authorization, caseData, respondent, null,
                                                        PRL_LET_ENG_RE5));
            }
        }
        List<Document> packkDocs = new ArrayList<>(re5Letters);
        packkDocs.addAll(getNotificationPack(caseData, PrlAppsConstants.K, c100StaticDocs));
        return packkDocs;
    }

    private List<Document> getDocumentsForDaorBailiffToServeRespondents(CaseData caseData, String authorization,
                                                                        List<Document> staticDocs, boolean attachLetters) {
        List<Document> packdDocs = new ArrayList<>();
        if (attachLetters) {
            packdDocs.addAll(getCoverLettertsforDaCourtAdminCourtBailiffPersonalService(caseData, authorization));
        }
        packdDocs.addAll(getNotificationPack(caseData, PrlAppsConstants.D, staticDocs));
        return packdDocs;
    }

    private List<Document> getCoverLettertsforDaCourtAdminCourtBailiffPersonalService(CaseData caseData, String authorization) {
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
                                                                                    List<Document> packA, List<Document> packB) {
        List<Document> reLetters = new ArrayList<>();
        Element<PartyDetails> respondent = Element.<PartyDetails>builder()
            .id(caseData.getRespondentsFL401().getPartyId())
            .value(caseData.getRespondentsFL401())
            .build();
        boolean applyOrderWithoutGivingNoticeToRespondent = CaseUtils.isApplyOrderWithoutGivingNoticeToRespondent(caseData);

        if (applyOrderWithoutGivingNoticeToRespondent) {
            reLetters.add(generateAccessCodeLetter(authorization, caseData, respondent, null,
                                                   PRL_LET_ENG_FL401_RE2
            ));
        } else {
            reLetters.add(generateAccessCodeLetter(authorization, caseData, respondent, null,
                                                   PRL_LET_ENG_FL401_RE3
            ));
        }
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
                    reLetters.add(packBDocument);
                }
            }
        }
        return reLetters;
    }

    private List<Document> getDocumentsForCaOrBailiffToServeApplicantSolcitor(CaseData caseData, String authorization,
                                                                              List<Document> c100StaticDocs, boolean attachLetters) {
        List<Document> ap8Letters = new ArrayList<>();
        if (attachLetters) {
            for (Element<PartyDetails> applicant : caseData.getApplicants()) {
                ap8Letters.add(generateAccessCodeLetter(authorization, caseData, applicant, null,
                                                        PRL_LET_ENG_AP8
                ));
            }
        }
        List<Document> packjDocs = new ArrayList<>(ap8Letters);
        packjDocs.addAll(getNotificationPack(caseData, PrlAppsConstants.J, c100StaticDocs));
        return packjDocs;
    }

    private List<Document> getDocumentsForDaOrBailiffToServeApplicantSolicitor(CaseData caseData,
                                                                              List<Document> staticDocs) {
        List<Document> packcDocs = new ArrayList<>();
        packcDocs.addAll(getNotificationPack(caseData, PrlAppsConstants.C, staticDocs));
        return packcDocs;
    }

    private String handleNotificationsDaSolicitorCreatedCase(CaseData caseData, String authorization,
                                                           List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                             Map<String, Object> caseDataMap) {
        List<Document> staticDocs = serviceOfApplicationPostService.getStaticDocs(authorization, CaseUtils.getCaseTypeOfApplication(caseData));
        String whoIsResponsibleForServing = caseData.getApplicantsFL401().getRepresentativeFullName();
        log.info("Fl401 case journey for caseId {}", caseData.getId());
        if (SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative.equals(caseData.getServiceOfApplication()
                                                                                       .getSoaServingRespondentsOptionsDA())) {
            List<Document> packADocs = getNotificationPack(caseData, PrlAppsConstants.A, staticDocs);
            List<Document> packBDocs = getNotificationPack(caseData, PrlAppsConstants.B, staticDocs);
            emailNotificationDetails.addAll(sendEmailDaPersonalApplicantLegalRep(caseData, authorization, packADocs, packBDocs));

        } else if (SoaSolicitorServingRespondentsEnum.courtBailiff
            .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA())
            || SoaSolicitorServingRespondentsEnum.courtAdmin
            .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA())) {
            log.info("#SOA... Generate C, D packs to be served by admin/bailiff.. common method to be used by "
                          + "solicitor created case");
            handleNotificationsDaSolicitorPersonalCourtAdminBailiff(caseData, authorization, emailNotificationDetails,
                                                                    staticDocs, caseDataMap
            );
        } else {
            log.error("#SOA TO DO... Generate C, D packs to be served by admin/bailiff.. common method to be used by "
                          + "citizen created case");
        }
        return whoIsResponsibleForServing;
    }

    private void sendNotificationToOthers(CaseData caseData, String authorization, List<Element<BulkPrintDetails>> bulkPrintDetails,
                                          List<Document> c100StaticDocs) {
        log.info("serving other people in case");

        List<DynamicMultiselectListElement> othersToNotify = getSelectedApplicantsOrRespondents(
            caseData.getOthersToNotify(),
            caseData.getServiceOfApplication().getSoaOtherParties().getValue());

        List<Document> packNDocs = c100StaticDocs.stream().filter(d -> d.getDocumentFileName()
            .equalsIgnoreCase(PRIVACY_DOCUMENT_FILENAME)).collect(
            Collectors.toList());
        packNDocs.addAll(getNotificationPack(caseData, PrlAppsConstants.N, c100StaticDocs));
        bulkPrintDetails.addAll(sendPostToOtherPeopleInCase(caseData, authorization, othersToNotify,
                                                            packNDocs,
                                                            PrlAppsConstants.SERVED_PARTY_OTHER
        ));
    }

    private void sendNotificationToRespondentNonPersonal(CaseData caseData, String authorization,
                                                         List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                         List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                         List<DynamicMultiselectListElement> selectedRespondents,
                                                         List<Document> packRDocs,
                                                         List<Document> packSDocs) {

        Map<String, Object> resultMap = sendNotificationToRespondentOrSolicitor(
            caseData,
            authorization,
            selectedRespondents,
            packRDocs,
            packSDocs,
            PrlAppsConstants.SERVED_PARTY_RESPONDENT_SOLICITOR
        );
        if (null != resultMap && resultMap.containsKey(EMAIL)) {
            emailNotificationDetails.addAll((List<Element<EmailNotificationDetails>>) resultMap.get(EMAIL));
        }
        if (null != resultMap && resultMap.containsKey(POST)) {
            bulkPrintDetails.addAll((List<Element<BulkPrintDetails>>) resultMap.get(POST));
        }
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
        log.info("*** Selected respondents *** {}", selectedRespondents);
        log.info("*** Selected applicants *** {}", selectedApplicants);
        if (CollectionUtils.isNotEmpty(selectedApplicants)) {
            emailNotificationDetails
                .addAll(sendNotificationsToCitizenApplicantsC100(
                    authorization, selectedApplicants,
                    caseData,
                    bulkPrintDetails,
                    c100StaticDocs
                ));
        }
        log.info(" ** emailnotification {}", emailNotificationDetails);
        log.info(" ** bulk print details {}", bulkPrintDetails);

        if (CollectionUtils.isNotEmpty(selectedRespondents)) {
            emailNotificationDetails
                .addAll(sendNotificationsToCitizenRespondentsC100(authorization, selectedRespondents,
                                                                  caseData, bulkPrintDetails, c100StaticDocs, true));
        }
    }

    private void handlePersonalServiceForCitizenC100(CaseData caseData, String authorization,
                                                        List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                        List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                        List<Document> c100StaticDocs) {
        if (SoaCitizenServingRespondentsEnum.unrepresentedApplicant
            .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsCA())) {
            getNotificationPack(caseData, PrlAppsConstants.L, c100StaticDocs);
            getNotificationPack(caseData, PrlAppsConstants.M, c100StaticDocs);
            for (Element<PartyDetails> applicant : caseData.getApplicants()) {
                if (!YesNoDontKnow.yes.equals(applicant.getValue().getDoTheyHaveLegalRepresentation())) {
                    sendPostWithAccessCodeLetterToParty(caseData, authorization, new ArrayList<>(), bulkPrintDetails,
                                                        applicant, PRL_LET_ENG_AP7, SERVED_PARTY_APPLICANT);
                }
            }
            for (Element<PartyDetails> respondent : caseData.getRespondents()) {
                if (!YesNoDontKnow.yes.equals(respondent.getValue().getDoTheyHaveLegalRepresentation())) {
                    sendPostWithAccessCodeLetterToParty(caseData, authorization, new ArrayList<>(), bulkPrintDetails,
                                                        respondent, PRL_LET_ENG_RE5, SERVED_PARTY_RESPONDENT);

                }
            }
        } else {
            getNotificationPack(caseData, PrlAppsConstants.J, c100StaticDocs);
            getNotificationPack(caseData, PrlAppsConstants.K, c100StaticDocs);
        }
        log.info(" {}", emailNotificationDetails);
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
        return caseDataMap;
    }

    public ResponseEntity<SubmittedCallbackResponse> handleSoaSubmitted(String authorisation, CallbackRequest callbackRequest) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();
        caseDataMap.putAll(caseSummaryTabService.updateTab(caseData));
        if (CaseUtils.isC8Present(caseData)) {
            return processConfidentialDetailsSoa(authorisation, callbackRequest, caseData);
        }
        return processNonConfidentialSoa(authorisation, caseData, caseDataMap);
    }

    private ResponseEntity<SubmittedCallbackResponse> processNonConfidentialSoa(String authorisation, CaseData caseData,
                                                                                Map<String, Object> caseDataMap) throws Exception {
        log.info("Confidential details are NOT present");
        List<Element<ServedApplicationDetails>> finalServedApplicationDetailsList;
        if (caseData.getFinalServedApplicationDetailsList() != null) {
            finalServedApplicationDetailsList = caseData.getFinalServedApplicationDetailsList();
        } else {
            log.info("*** finalServedApplicationDetailsList is empty in case data ***");
            finalServedApplicationDetailsList = new ArrayList<>();
        }
        finalServedApplicationDetailsList.add(element(sendNotificationForServiceOfApplication(caseData, authorisation, caseDataMap)));
        caseDataMap.put(FINAL_SERVED_APPLICATION_DETAILS_LIST, finalServedApplicationDetailsList);
        cleanUpSoaSelections(caseDataMap, true);

        coreCaseDataService.triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            INTERNAL_UPDATE_ALL_TABS,
            caseDataMap
        );
        return ok(SubmittedCallbackResponse.builder().confirmationHeader(
            CONFIRMATION_HEADER).confirmationBody(
            CONFIRMATION_BODY_PREFIX).build());
    }

    private ResponseEntity<SubmittedCallbackResponse> processConfidentialDetailsSoa(String authorisation, CallbackRequest callbackRequest,
                                                                                    CaseData caseData) {
        Map<String, Object> caseDataMap;
        caseDataMap = CaseUtils.getCaseTypeOfApplication(caseData).equalsIgnoreCase(C100_CASE_TYPE)
                            ? generatePacksForConfidentialCheckC100(callbackRequest.getCaseDetails(), authorisation)
                            : generatePacksForConfidentialCheckFl401(callbackRequest.getCaseDetails(), authorisation);

        cleanUpSoaSelections(caseDataMap, false);

        log.info("============= updated case data for confidentialy pack ================> {}", caseDataMap);

        coreCaseDataService.triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            INTERNAL_UPDATE_ALL_TABS,
            caseDataMap
        );

        log.info("Confidential details are present, case needs to be reviewed and served later");
        return ok(SubmittedCallbackResponse.builder().confirmationHeader(
            CONFIDENTIAL_CONFIRMATION_HEADER).confirmationBody(
            CONFIDENTIAL_CONFIRMATION_BODY_PREFIX).build());
    }

    private List<Document> getDocsToBeServedToLa(String authorisation, CaseData caseData) {
        if (YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaServeLocalAuthorityYesOrNo())
            && null != caseData.getServiceOfApplication().getSoaLaEmailAddress()) {
            List<Document> docs = new ArrayList<>();
            if (null != caseData.getServiceOfApplication().getSoaDocumentDynamicListForLa()) {
                for (Element<DocumentListForLa> laDocument: caseData.getServiceOfApplication().getSoaDocumentDynamicListForLa()) {
                    log.info("fetching doc for {}", laDocument);
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
                docs.add(caseData.getC8Document());
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
            uk.gov.hmcts.reform.ccd.client.model.Document selectedDoc = null;
            selectedDoc = getSelectedDocumentFromCategories(categoriesAndDocuments.getCategories(),selectedDocument);

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
                                                        CaseData caseData, List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                                         List<Document> staticDocs) {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        List<Element<CaseInvite>> caseInvites = caseData.getCaseInvites() != null ? caseData.getCaseInvites()
            : new ArrayList<>();
        selectedApplicants.forEach(applicant -> {
            Optional<Element<PartyDetails>> selectedParty = getParty(applicant.getCode(), caseData.getApplicants());
            if (selectedParty.isPresent()) {
                Element<PartyDetails> selectedApplicant = selectedParty.get();
                CaseInvite caseInvite = getCaseInvite(selectedApplicant.getId(),caseInvites);
                if (caseInvite == null) {
                    caseInvite = c100CaseInviteService.generateCaseInvite(selectedApplicant, Yes);
                    caseInvites.add(element(caseInvite));
                }
                if (isAccessEnabled(selectedApplicant)) {
                    log.info("Access already enabled");
                    if (ContactPreferences.digital.equals(selectedApplicant.getValue().getContactPreferences())) {
                        List<Document> docs = getNotificationPack(caseData, PrlAppsConstants.P, staticDocs);
                        sendEmailToCitizen(authorization, caseData, selectedApplicant, emailNotificationDetails, docs);
                    } else {
                        sendPostWithAccessCodeLetterToParty(caseData, authorization,
                                                            getNotificationPack(caseData, PrlAppsConstants.R, staticDocs),
                                                            bulkPrintDetails, selectedApplicant, Templates.AP6_LETTER,
                                                            SERVED_PARTY_APPLICANT);
                    }
                } else {
                    log.info("Access yet to be granted");
                    if (ContactPreferences.digital.equals(selectedApplicant.getValue().getContactPreferences())) {
                        Document ap6Letter = generateAccessCodeLetter(authorization, caseData, selectedApplicant, caseInvite,
                                                                      Templates.AP6_LETTER);
                        List<Document> docs = new ArrayList<>(Collections.singletonList(ap6Letter));
                        docs.addAll(getNotificationPack(caseData, PrlAppsConstants.P, staticDocs));
                        sendEmailToCitizen(authorization, caseData, selectedApplicant,
                                                                           emailNotificationDetails, docs);
                    } else {
                        sendPostWithAccessCodeLetterToParty(caseData, authorization,
                                                            getNotificationPack(caseData, PrlAppsConstants.R, staticDocs),
                                                            bulkPrintDetails, selectedApplicant, Templates.AP6_LETTER,
                                                            SERVED_PARTY_APPLICANT);
                    }
                }
            }
            caseData.setCaseInvites(caseInvites);
        });
        return emailNotificationDetails;
    }

    private List<Element<EmailNotificationDetails>> sendNotificationsToCitizenRespondentsC100(String authorization,
                                                              List<DynamicMultiselectListElement> selectedRespondents,
                                                          CaseData caseData,  List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                          List<Document> docs, boolean isStaticDocs) {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        List<Element<CaseInvite>> caseInvites = caseData.getCaseInvites() != null ? caseData.getCaseInvites()
            : new ArrayList<>();
        selectedRespondents.forEach(respondent -> {
            Optional<Element<PartyDetails>> selectedParty = getParty(respondent.getCode(), caseData.getRespondents());
            if (selectedParty.isPresent()) {
                Element<PartyDetails> selectedRespondent = selectedParty.get();
                if (YesNoDontKnow.yes.equals(selectedRespondent.getValue().getDoTheyHaveLegalRepresentation())) {
                    log.info("Respondent is represented");
                    try {
                        emailNotificationDetails.add(element(serviceOfApplicationEmailService.sendEmailNotificationToSolicitor(
                            authorization, caseData,
                            selectedRespondent.getValue(),
                            EmailTemplateNames.RESPONDENT_SOLICITOR,
                            isStaticDocs ? getNotificationPack(caseData, PrlAppsConstants.S, docs) : docs,
                            SERVED_PARTY_RESPONDENT
                        )));
                    } catch (Exception e) {
                        log.error("Failed to send email to respondent solicitor {}", e.getMessage());
                    }
                } else {
                    CaseInvite caseInvite = getCaseInvite(selectedRespondent.getId(),caseInvites);
                    if (caseInvite == null) {
                        caseInvite = c100CaseInviteService.generateCaseInvite(selectedRespondent, Yes);
                        caseInvites.add(element(caseInvite));
                    }
                    log.info("Access to be granted");
                    sendPostWithAccessCodeLetterToParty(caseData, authorization,
                                                        isStaticDocs ? getNotificationPack(caseData, PrlAppsConstants.S, docs) : docs,
                                                        bulkPrintDetails, selectedRespondent, PRL_LET_ENG_RE5,
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

    private void sendEmailToCitizen(String authorization,
                                    CaseData caseData, Element<PartyDetails> applicant,
                                    List<Element<EmailNotificationDetails>> notificationList, List<Document> docs) {
        log.info("** Docs being sent *** {}", docs);
        try {
            notificationList.add(element(serviceOfApplicationEmailService
                                             .sendEmailNotificationToApplicant(
                                                 authorization, caseData, applicant.getValue(),
                                                 docs,
                                                 SERVED_PARTY_APPLICANT
                                             )));
        } catch (Exception e) {
            log.error("Failed to send notification to applicant {}", e.getMessage());
        }
    }

    private List<Element<EmailNotificationDetails>> sendEmailCaPersonalApplicantLegalRep(CaseData caseData, String authorization,
                                                                                         List<Document> packHiDocs) {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        List<Document> finalDocs = new ArrayList<>();
        if (caseData.getApplicants().get(0).getValue().getSolicitorEmail() != null) {
            try {
                log.info(
                    "Sending the email notification to applicant solicitor for C100 Application for caseId {}",
                    caseData.getId()
                );
                //Respondent's pack
                log.error("#SOA attach RE6 letter");
                caseData.getRespondents().forEach(respondent ->
                    finalDocs.add(generateAccessCodeLetter(authorization, caseData,respondent, null, PRL_LET_ENG_C100_RE6))
                );
                finalDocs.addAll(packHiDocs);
                log.info("applicant docs {}", packHiDocs);
                log.info("final docs {}", finalDocs);
                Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
                dynamicData.put("name", caseData.getApplicants().get(0).getValue().getRepresentativeFullName());
                dynamicData.put("c100", true);
                dynamicData.put(DASH_BOARD_LINK, manageCaseUrl + PrlAppsConstants.URL_STRING + caseData.getId());
                emailNotificationDetails.add(element(serviceOfApplicationEmailService.sendEmailUsingTemplateWithAttachments(
                    authorization,
                    caseData.getApplicants().get(0).getValue().getSolicitorEmail(),
                    finalDocs,
                    SendgridEmailTemplateNames.SOA_PERSONAL_CA_DA_APPLICANT_LEGAL_REP,
                    dynamicData,
                    SERVED_PARTY_APPLICANT_SOLICITOR
                )));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return emailNotificationDetails;
    }

    private List<Element<EmailNotificationDetails>> sendEmailDaPersonalApplicantLegalRep(CaseData caseData,
                                                                                         String authorization, List<Document> packA,
                                                                                         List<Document> packB) {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        PartyDetails applicant = caseData.getApplicantsFL401();
        List<Document> finalDocumentList = new ArrayList<>();
        if (applicant.getSolicitorEmail() != null) {
            try {
                log.info(
                    "Sending the email notification to applicant solicitor for FL401 Application for caseId {}",
                    caseData.getId()
                );
                //Respondent's pack
                log.error("#SOA TO DO With notice add RE3 letter, without notice add RE2, gov notification not required so remove it");
                finalDocumentList.addAll(getCoverLettersAndRespondentPacksForDaApplicantSolicitor(caseData, authorization, packA, packB));
                finalDocumentList.addAll(packA);
                Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
                dynamicData.put("name", caseData.getApplicantsFL401().getRepresentativeFullName());
                dynamicData.put(DASH_BOARD_LINK, manageCaseUrl + PrlAppsConstants.URL_STRING + caseData.getId());
                emailNotificationDetails.add(element(serviceOfApplicationEmailService.sendEmailUsingTemplateWithAttachments(
                    authorization,
                    caseData.getApplicantsFL401().getSolicitorEmail(),
                    finalDocumentList,
                    SendgridEmailTemplateNames.SOA_PERSONAL_CA_DA_APPLICANT_LEGAL_REP,
                    dynamicData,
                    SERVED_PARTY_APPLICANT_SOLICITOR
                )));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return emailNotificationDetails;
    }

    private List<DynamicMultiselectListElement> getSelectedApplicantsOrRespondents(List<Element<PartyDetails>> applicantsOrRespondents,
                                                                                   List<DynamicMultiselectListElement> value) {

        log.info("applicantsOrRespondents {}", applicantsOrRespondents);
        log.info("value {}", value);
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
                    emailNotificationDetails.add(element(serviceOfApplicationEmailService
                                                             .sendEmailUsingTemplateWithAttachments(
                                                                 authorization, party.get().getValue().getSolicitorEmail(),
                                                                 packQ,
                                                                 SendgridEmailTemplateNames.SOA_SERVE_APPLICANT_SOLICITOR_NONPER_PER_CA_CB,
                                                                 dynamicData,
                                                                 servedParty
                                                             )));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return emailNotificationDetails;
    }

    public Map<String, Object> sendNotificationToRespondentOrSolicitor(CaseData caseData,
                                                                       String authorization,
                                                                       List<DynamicMultiselectListElement> selectedRespondent,
                                                                       List<Document> packR,
                                                                       List<Document> packS, String servedParty) {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();
        List<Element<PartyDetails>> respondentListC100 = C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
            ? caseData.getRespondents()
            : List.of(Element.<PartyDetails>builder()
                          .id(caseData.getRespondentsFL401().getPartyId())
                          .value(caseData.getRespondentsFL401()).build());
        log.info("sending notifications to respondents : {}", respondentListC100);
        selectedRespondent.forEach(respondentc100 -> {
            Optional<Element<PartyDetails>> party = getParty(respondentc100.getCode(), respondentListC100);
            log.info("Party details {}", party);
            if (party.isPresent() && YesNoDontKnow.yes.equals(party.get().getValue().getDoTheyHaveLegalRepresentation())) {
                if (party.get().getValue().getSolicitorEmail() != null) {
                    try {
                        log.info(
                            "Sending the email notification to respondent solicitor for C100 Application for caseId {}",
                            caseData.getId()
                        );
                        emailNotificationDetails.add(element(serviceOfApplicationEmailService.sendEmailNotificationToSolicitor(
                            authorization, caseData,
                            party.get().getValue(),
                            EmailTemplateNames.RESPONDENT_SOLICITOR,
                            packS,
                            servedParty
                        )));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (party.isPresent() && (YesNoDontKnow.no.equals(party.get().getValue().getDoTheyHaveLegalRepresentation())
                || YesNoDontKnow.dontKnow.equals(party.get().getValue().getDoTheyHaveLegalRepresentation()))) {
                if (party.get().getValue().getAddress() != null && StringUtils.isNotEmpty(party.get().getValue().getAddress().getAddressLine1())) {
                    log.info(
                        "Sending the notification in post to respondent for C100 Application for caseId {}",
                        caseData.getId()
                    );

                    sendPostWithAccessCodeLetterToParty(caseData, authorization, packR, bulkPrintDetails, party.get(),
                                                        PRL_LET_ENG_RE5, SERVED_PARTY_RESPONDENT);
                } else {
                    log.info("Unable to send any notification to respondent for C100 Application for caseId {} "
                                 + "as no address available", caseData.getId());
                }
            }
        });
        resultMap.put(EMAIL, emailNotificationDetails);
        resultMap.put(POST, bulkPrintDetails);
        return resultMap;
    }

    private void sendPostWithAccessCodeLetterToParty(CaseData caseData, String authorization, List<Document> packDocs,
                                                     List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                     Element<PartyDetails> party, String template,
                                                     String servedParty) {
        List<Document> docs = new ArrayList<>();
        CaseInvite caseInvite = getCaseInvite(party.getId(), caseData.getCaseInvites());
        try {
            docs.add(getCoverSheet(authorization, caseData,
                                   party.getValue().getAddress(),
                                   party.getValue().getLabelForDynamicList()
            ));
            docs.add(generateAccessCodeLetter(authorization, caseData, party, caseInvite, template));
            docs.addAll(packDocs);
            bulkPrintDetails.add(element(serviceOfApplicationPostService.sendPostNotificationToParty(
                caseData,
                authorization,
                party.getValue(),
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
                .filter(element -> element.getId().toString().equalsIgnoreCase(code)).findFirst();
        }
        return party;
    }

    public Document getCoverSheet(String authorization, CaseData caseData, Address address, String name) {

        try {
            return DocumentUtils.toCoverLetterDocument(serviceOfApplicationPostService
                                                           .getCoverLetterGeneratedDocInfo(caseData, authorization,
                                                                                           address,
                                                                                           name
                                                           ));
        } catch (Exception e) {
            log.error("Failed to generate cover sheet {}", e.getMessage());
        }
        return null;
    }

    public List<Document> getNotificationPack(CaseData caseData, String requiredPack, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        switch (requiredPack) {
            case PrlAppsConstants.A:
                docs.addAll(generatePackA(caseData, staticDocs));
                break;
            case PrlAppsConstants.B:
                docs.addAll(generatePackB(caseData, staticDocs));
                break;
            case PrlAppsConstants.C:
                docs.addAll(generatePackC(caseData, staticDocs));
                break;
            case PrlAppsConstants.D:
                docs.addAll(generatePackD(caseData, staticDocs));
                break;
            case PrlAppsConstants.E:
                docs.addAll(generatePackE(caseData, staticDocs));
                break;
            case PrlAppsConstants.F:
                docs.addAll(generatePackF(caseData, staticDocs));
                break;
            case PrlAppsConstants.G:
                docs.addAll(generatePackG(caseData, staticDocs));
                break;
            case PrlAppsConstants.H:
                docs.addAll(generatePackH(caseData, staticDocs));
                break;
            case PrlAppsConstants.I:
                docs.addAll(generatePackI(caseData, staticDocs));
                break;
            case PrlAppsConstants.J:
                docs.addAll(generatePackJ(caseData, staticDocs));
                break;
            case PrlAppsConstants.K:
                docs.addAll(generatePackK(caseData, staticDocs));
                break;
            case PrlAppsConstants.L:
                docs.addAll(generatePackL(caseData, staticDocs));
                break;
            case PrlAppsConstants.M:
                docs.addAll(generatePackM(caseData, staticDocs));
                break;
            case PrlAppsConstants.N:
                docs.addAll(generatePackN(caseData, staticDocs));
                break;
            case PrlAppsConstants.O:
                docs.addAll(generatePackO(caseData, staticDocs));
                break;
            case PrlAppsConstants.P:
                docs.addAll(generatePackP(caseData, staticDocs));
                break;
            case PrlAppsConstants.Q:
                docs.addAll(generatePackQ(caseData, staticDocs));
                break;
            case PrlAppsConstants.R:
                docs.addAll(generatePackR(caseData, staticDocs));
                break;
            case PrlAppsConstants.S:
                docs.addAll(generatePackS(caseData, staticDocs));
                break;
            case PrlAppsConstants.HI:
                docs.addAll(generatePackHI(caseData, staticDocs));
                break;
            case PrlAppsConstants.Z: //not present in miro, added this by comparing to DA other org pack,confirm with PO's
                docs.addAll(generatePackZ(caseData, staticDocs));
                break;
            default:
                break;
        }
        return docs;

    }

    private List<Document> generatePackJ(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        // Annex Z to be excluded
        docs.addAll(staticDocs.stream()
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(
                            C1A_BLANK_DOCUMENT_FILENAME))
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(
                            C7_BLANK_DOCUMENT_FILENAME)).toList());
        return docs;
    }

    private List<Document> generatePackK(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        // Annex Y to be excluded
        docs.addAll(staticDocs.stream()
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_C9_PERSONAL_SERVICE_FILENAME)).toList());
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
                            C7_BLANK_DOCUMENT_FILENAME))
                        .collect(Collectors.toList()));
        return docs;
    }

    private List<Document> generatePackM(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        // Annex Y to be excluded
        docs.addAll(staticDocs.stream()
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_C9_PERSONAL_SERVICE_FILENAME)).toList());
        return docs;
    }

    private List<Document> generatePackZ(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        log.info("{}",staticDocs);
        return docs;
    }

    private List<Document> generatePackHI(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        docs.addAll(staticDocs);
        return docs;
    }

    private List<Document> generatePackN(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getC6aIfPresent(getSoaSelectedOrders(caseData)));
        log.info("{}",staticDocs);
        return docs;
    }

    public List<Document> getC6aIfPresent(List<Document> soaSelectedOrders) {
        return soaSelectedOrders.stream().filter(d -> d.getDocumentFileName().equalsIgnoreCase(
            SOA_C6A_OTHER_PARTIES_ORDER)).collect(Collectors.toList());
    }

    private List<Document> getNonC6aOrders(List<Document> soaSelectedOrders) {
        return soaSelectedOrders.stream().filter(d -> ! d.getDocumentFileName().equalsIgnoreCase(
            SOA_C6A_OTHER_PARTIES_ORDER)).collect(Collectors.toList());
    }

    private List<Document> generatePackH(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        log.info("{}",staticDocs);
        return docs;
    }

    private List<Document> generatePackI(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        log.info("{}",staticDocs);
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
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(
                            C1A_BLANK_DOCUMENT_FILENAME))
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
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        docs.addAll(staticDocs.stream()
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_FL416_FILENAME)).toList());
        return docs;
    }

    private List<Document> generatePackB(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        docs.addAll(staticDocs.stream()
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_FL416_FILENAME))
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_FL415_FILENAME)).toList());
        return docs;
    }

    private List<Document> generatePackC(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        docs.addAll(staticDocs.stream()
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_FL416_FILENAME))
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_FL415_FILENAME)).toList());
        return docs;
    }

    private List<Document> generatePackD(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        docs.addAll(staticDocs.stream()
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_FL416_FILENAME))
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_FL415_FILENAME)).toList());
        return docs;
    }

    private List<Document> generatePackE(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        docs.addAll(staticDocs);
        return docs;
    }

    private List<Document> generatePackF(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        docs.addAll(staticDocs.stream()
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_FL416_FILENAME))
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_FL415_FILENAME)).toList());
        return docs;
    }

    private List<Document> generatePackG(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        log.info("{}",staticDocs);
        return docs;
    }

    private List<Document> generatePackO(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.add(caseData.getC8Document());
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        log.info("{}",staticDocs);
        return docs;
    }

    private List<Document> getCaseDocs(CaseData caseData) {
        //Welsh pack generation needs to be reviewed
        List<Document> docs = new ArrayList<>();
        if (CaseUtils.getCaseTypeOfApplication(caseData).equalsIgnoreCase(PrlAppsConstants.C100_CASE_TYPE)) {
            if (null != caseData.getFinalDocument()) {
                docs.add(caseData.getFinalDocument());
            }
            if (null != caseData.getC1ADocument()) {
                docs.add(caseData.getC1ADocument());
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
            List<String> orderCodes = caseData.getServiceOfApplicationScreen1()
                .getValue().stream().map(DynamicMultiselectListElement::getCode).toList();
            orderCodes.forEach(orderCode ->
                caseData.getOrderCollection().stream()
                    .filter(order -> String.valueOf(order.getId()).equalsIgnoreCase(orderCode))
                    .findFirst()
                    .ifPresent(o -> selectedOrders.add(o.getValue().getOrderDocument())));
            return selectedOrders;
        }
        return Collections.emptyList();

    }

    public void cleanUpSoaSelections(Map<String, Object> caseDataUpdated, boolean removeCafcassFields) {
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
            PROCEED_TO_SERVING,
            MISSING_ADDRESS_WARNING_TEXT,
            SOA_DOCUMENT_DYNAMIC_LIST_FOR_LA
        ));

        if (removeCafcassFields) {
            soaFields.addAll(List.of("soaCafcassCymruEmail", "soaCafcassCymruServedOptions", "soaCafcassEmailId", "soaCafcassServedOptions"));

        }

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
        caseDataUpdated.put(SOA_DOCUMENT_DYNAMIC_LIST_FOR_LA, getDocumentsDynamicListForLa(authorisation,
                                                                                           String.valueOf(caseData.getId())));
        caseDataUpdated.put(CASE_CREATED_BY, CaseUtils.isCaseCreatedByCitizen(caseData) ? SOA_CITIZEN : SOA_SOLICITOR);
        List<Element<DocumentListForLa>> documentDynamicListLa = getDocumentsDynamicListForLa(authorisation,
                                                                                              String.valueOf(caseData.getId()));
        log.info("** case created by ** {}", caseDataUpdated.get(CASE_CREATED_BY));
        log.info("** dynamic list 1 ** {}", documentDynamicListLa);
        caseDataUpdated.put(
            MISSING_ADDRESS_WARNING_TEXT,
            checkIfPostalAddressMissedForRespondentAndOtherParties(caseData)
        );
        caseDataUpdated.put(SOA_DOCUMENT_DYNAMIC_LIST_FOR_LA, documentDynamicListLa);
        log.info("** dynamic list 2 ** {}", caseDataUpdated.get(SOA_DOCUMENT_DYNAMIC_LIST_FOR_LA));
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
        log.info("isRespondentAddressPresent ==> " + isRespondentAddressPresent);
        log.info("isOtherPeopleAddressPresent ==> " + isOtherPeopleAddressPresent);
        log.info("warningText ==> " + warningText);
        return warningText;
    }

    private static boolean isPartiesAddressPresent(PartyDetails partyDetails) {
        boolean isAddressPresent = true;
        if (No.equals(partyDetails.getIsCurrentAddressKnown())
            || ObjectUtils.isEmpty(partyDetails.getAddress())
            || StringUtils.isEmpty(partyDetails.getAddress().getAddressLine1())) {
            isAddressPresent = false;
        }
        return isAddressPresent;
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
            if (YesOrNo.Yes.equals(partyDetails.getIsAddressConfidential())
                || YesOrNo.Yes.equals(partyDetails.getIsPhoneNumberConfidential())
                || YesOrNo.Yes.equals(partyDetails.getIsEmailAddressConfidential())) {
                return true;
            }

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
        categoriesAdnDocumentsList.getListItems().removeIf(dynamicListElement -> dynamicListElement.getLabel().contains("Confidential"));
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
        log.info("Access code map {} for {}",dataMap, template);
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
            log.error("*** Access code letter failed for {} :: because of {}", template, e.getStackTrace());
        }
        return null;
    }

    public Map<String, Object> populateAccessCodeMap(CaseData caseData, Element<PartyDetails> party, CaseInvite caseInvite) {
        log.info("*** case invite {}", caseInvite);
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
        log.info("data map tto letters {}", dataMap);
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
        if (caseInvites != null) {
            if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                caseData.getApplicants().forEach(party -> caseInvites.add(element(c100CaseInviteService.generateCaseInvite(party, Yes))));
                caseData.getRespondents().forEach(party -> caseInvites.add(element(c100CaseInviteService.generateCaseInvite(party, No))));
            } else {
                caseInvites.add(element(fl401CaseInviteService.generateCaseInvite(caseData.getApplicantsFL401(), Yes)));
                caseInvites.add(element(fl401CaseInviteService.generateCaseInvite(caseData.getRespondentsFL401(), No)));
            }
        }
        return caseInvites;
    }

    public Map<String, Object> generatePacksForConfidentialCheckC100(CaseDetails caseDetails, String authorization) {
        log.info("Inside generatePacks for confidential check C100 method");
        Map<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE));
        String dateCreated = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS).format(zonedDateTime);
        List<Document> c100StaticDocs = serviceOfApplicationPostService.getStaticDocs(authorization, CaseUtils.getCaseTypeOfApplication(caseData));
        log.info("caseData.getServiceOfApplication() {}", caseData.getServiceOfApplication());
        if (YesOrNo.No.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())
            && (caseData.getServiceOfApplication().getSoaRecipientsOptions() != null)
            && (!caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue().isEmpty())) {
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
                buildUnservedApplicantPack(authorization, caseDataUpdated, caseData, dateCreated, c100StaticDocs, selectedApplicants);
            } else {
                caseDataUpdated.put(UNSERVED_APPLICANT_PACK, null);
            }
            // Respondent pack
            List<DynamicMultiselectListElement> selectedRespondents = getSelectedApplicantsOrRespondents(
                caseData.getRespondents(),
                caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue()
            );
            if (CollectionUtils.isNotEmpty(selectedRespondents)) {
                buildUnservedRespondentPack(authorization, caseDataUpdated, caseData, dateCreated, c100StaticDocs, selectedRespondents);
            } else {
                caseDataUpdated.put(UNSERVED_RESPONDENT_PACK, null);
            }
        } else if (YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())) {
            log.error("#SOA TO DO .. Personal Service to be added - for 4 options");
            if (SoaSolicitorServingRespondentsEnum.courtAdmin
                .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA())
                || SoaSolicitorServingRespondentsEnum.courtBailiff
                .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA())) {
                List<Document> packjDocs = getDocumentsForCaOrBailiffToServeApplicantSolcitor(caseData, authorization, c100StaticDocs,
                                                                                              false);
                List<Document> packkDocs = getDocumentsForCaorBailiffToServeRespondents(caseData, authorization, c100StaticDocs,
                                                                                        true);
                final SoaPack unservedRespondentPack = SoaPack.builder()
                    .packDocument(wrapElements(packkDocs))
                    .partyIds(CaseUtils.getPartyIdList(caseData.getRespondents()))
                    .servedBy(PRL_COURT_ADMIN)
                    .personalServiceBy(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA().toString())
                    .packCreatedDate(dateCreated)
                    .build();
                caseDataUpdated.put(UNSERVED_RESPONDENT_PACK, unservedRespondentPack);
                final SoaPack unServedApplicantPack = SoaPack.builder()
                    .packDocument(wrapElements(packjDocs))
                    .partyIds(wrapElements(caseData.getApplicants().get(0).getId().toString()))
                    .servedBy(PRL_COURT_ADMIN)
                    .personalServiceBy(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA().toString())
                    .packCreatedDate(dateCreated)
                    .build();
                caseDataUpdated.put(UNSERVED_APPLICANT_PACK, unServedApplicantPack);
            } else if (SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative
                .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA())) {
                List<Document> packHDocs = getNotificationPack(caseData, HI, c100StaticDocs);
                final SoaPack unservedRespondentPack = SoaPack.builder()
                    .packDocument(wrapElements(packHDocs))
                    .partyIds(wrapElements(caseData.getApplicants().get(0).getId().toString()))
                    .servedBy(SERVED_PARTY_APPLICANT_SOLICITOR)
                    .personalServiceBy(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative.toString())
                    .packCreatedDate(dateCreated)
                    .build();
                caseDataUpdated.put(UNSERVED_RESPONDENT_PACK, unservedRespondentPack);
                final SoaPack unServedApplicantPack = SoaPack.builder()
                    .packDocument(wrapElements(packHDocs))
                    .partyIds(wrapElements(caseData.getApplicants().get(0).getId().toString()))
                    .servedBy(SERVED_PARTY_APPLICANT_SOLICITOR)
                    .personalServiceBy(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative.toString())
                    .packCreatedDate(dateCreated)
                    .build();
                caseDataUpdated.put(UNSERVED_APPLICANT_PACK, unServedApplicantPack);
            }
        }
        //serving other people in the case
        if (null != caseData.getServiceOfApplication().getSoaOtherParties()
            && !caseData.getServiceOfApplication().getSoaOtherParties().getValue().isEmpty()) {
            buildUnservedOthersPack(authorization, caseDataUpdated, caseData, dateCreated, c100StaticDocs);
        } else {
            caseDataUpdated.put(UNSERVED_OTHERS_PACK, null);
        }

        //serving Local authority in the case
        if (YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaServeLocalAuthorityYesOrNo())
            && null != caseData.getServiceOfApplication().getSoaLaEmailAddress()) {
            List<Document> docsForLa = getDocsToBeServedToLa(authorization, caseData);
            if (CollectionUtils.isNotEmpty(docsForLa)) {
                caseDataUpdated.put(UNSERVED_LA_PACK, SoaPack.builder().packDocument(wrapElements(docsForLa))
                    .servedBy(userService.getUserDetails(authorization).getFullName())
                    .packCreatedDate(LocalDateTime.now().toString())
                        .partyIds(List.of(element(caseData.getServiceOfApplication().getSoaLaEmailAddress())))
                    .build());
            }
        } else {
            caseDataUpdated.put(UNSERVED_LA_PACK, null);
        }
        return caseDataUpdated;
    }

    public Map<String, Object> generatePacksForConfidentialCheckFl401(CaseDetails caseDetails, String authorization) {
        log.info("Inside generatePacksForConfidentialCheck FL401 Method");
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE));
        Map<String, Object> caseDataUpdated = new HashMap<>();
        String dateCreated = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS).format(zonedDateTime);
        List<Document> fl401StaticDocs = serviceOfApplicationPostService.getStaticDocs(authorization, CaseUtils.getCaseTypeOfApplication(caseData));
        log.info("caseData.getServiceOfApplication() {}", caseData.getServiceOfApplication());
        if (SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative
            .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA())) {
            caseDataUpdated.putAll(genPacksConfidentialCheckDaApplicantSolicitor(authorization, caseData, dateCreated,
                                                                                 fl401StaticDocs));
        } else if (SoaSolicitorServingRespondentsEnum.courtAdmin
            .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA())
            || SoaSolicitorServingRespondentsEnum.courtBailiff
                .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA())) {
            log.info("#SOA Personal courtadmin / court bailiff - case created by - solicitor");
            List<Document> packdDocs = getNotificationPack(caseData, PrlAppsConstants.D, fl401StaticDocs);
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

        } else if (SoaCitizenServingRespondentsEnum.courtBailiff
            .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsDA())
            || SoaCitizenServingRespondentsEnum.courtAdmin
            .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsDA())) {
            log.error("#SOA TO DO... Personal courtadmin / court bailiff - case created by - citizen");
        } else if (SoaCitizenServingRespondentsEnum.unrepresentedApplicant
            .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsDA())) {
            log.error("#SOA TO DO... Personal courtadmin / court bailiff - case created by- citizen/solicitor");
            getNotificationPack(caseData, PrlAppsConstants.E, fl401StaticDocs);
            getNotificationPack(caseData, PrlAppsConstants.F, fl401StaticDocs);
        }
        return caseDataUpdated;
    }

    private Map<String, Object> genPacksConfidentialCheckDaApplicantSolicitor(String authorization, CaseData caseData, String dateCreated,
                                                               List<Document> fl401StaticDocs) {
        log.info("serving applicants or respondents");
        // Applicants pack
        Map<String, Object> caseDataUpdated = new HashMap<>();
        final String partyId = caseData.getApplicantsFL401().getPartyId().toString();
        List<Document> packADocs = getNotificationPack(caseData, PrlAppsConstants.A, fl401StaticDocs);
        List<Document> packBDocs = getNotificationPack(caseData, PrlAppsConstants.B, fl401StaticDocs);
        final SoaPack unServedApplicantPack = SoaPack.builder().packDocument(wrapElements(packADocs))
            .partyIds(wrapElements(partyId))
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .packCreatedDate(dateCreated)
            .personalServiceBy(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA().toString())
            .build();
        caseDataUpdated.put(UNSERVED_APPLICANT_PACK, unServedApplicantPack);

        final SoaPack unServedRespondentPack = SoaPack.builder().packDocument(wrapElements(packBDocs)).partyIds(
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
        log.info("serving other people in case");
        log.info("Before caseData.getOthersToNotify {}", caseData.getOthersToNotify());
        List<Element<PartyDetails>> otherPartiesToNotify = TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
            ? caseData.getOtherPartyInTheCaseRevised()
            : caseData.getOthersToNotify();
        final List<DynamicMultiselectListElement> otherParties = getSelectedApplicantsOrRespondents(
            otherPartiesToNotify,
            caseData.getServiceOfApplication().getSoaOtherParties().getValue()
        );

        final List<String> othersPartyIds = otherParties.stream().map(DynamicMultiselectListElement::getCode).collect(
            Collectors.toList());

        List<Document> packNDocs = c100StaticDocs.stream().filter(d -> d.getDocumentFileName()
            .equalsIgnoreCase(PRIVACY_DOCUMENT_FILENAME)).collect(
            Collectors.toList());
        packNDocs.addAll(getNotificationPack(caseData, PrlAppsConstants.N, c100StaticDocs));

        final SoaPack unServedOthersPack = SoaPack.builder().packDocument(wrapElements(packNDocs))
            .partyIds(wrapElements(othersPartyIds))
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .packCreatedDate(dateCreated)
            .build();

        caseDataUpdated.put(UNSERVED_OTHERS_PACK, unServedOthersPack);
    }

    private void buildUnservedRespondentPack(String authorization, Map<String, Object> caseDataUpdated, CaseData caseData, String dateCreated,
                                             List<Document> c100StaticDocs, List<DynamicMultiselectListElement> selectedRespondents) {
        final List<String> selectedPartyIds = selectedRespondents.stream().map(DynamicMultiselectListElement::getCode).collect(
            Collectors.toList());
        log.info("selected respondents ========= {}", selectedRespondents.size());
        log.info("selected Respondent PartyIds ========= {}", selectedPartyIds);
        List<Element<Document>> packRDocs = wrapElements(getNotificationPack(caseData, PrlAppsConstants.R, c100StaticDocs));
        final SoaPack unServedRespondentPack = SoaPack.builder().packDocument(packRDocs).partyIds(
            wrapElements(selectedPartyIds))
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .packCreatedDate(dateCreated)
            .build();
        caseDataUpdated.put(UNSERVED_RESPONDENT_PACK, unServedRespondentPack);
    }

    private void buildUnservedApplicantPack(String authorization, Map<String, Object> caseDataUpdated, CaseData caseData, String dateCreated,
                                            List<Document> c100StaticDocs, List<DynamicMultiselectListElement> selectedApplicants) {
        final List<String> selectedPartyIds = selectedApplicants.stream().map(DynamicMultiselectListElement::getCode).collect(
            Collectors.toList());
        log.info("selected Applicant ========= {}", selectedApplicants.size());
        log.info("selected Applicant PartyIds ========= {}", selectedPartyIds);
        List<Element<Document>> packDocs = new ArrayList<>();
        if (CaseUtils.isCaseCreatedByCitizen(caseData)) {
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

        log.info("Inside sendNotificationsForUnServedPacks method");
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
        final SoaPack unServedApplicantPack = caseData.getServiceOfApplication().getUnServedApplicantPack();
        final SoaPack unServedRespondentPack = caseData.getServiceOfApplication().getUnServedRespondentPack();

        if (unServedApplicantPack != null || unServedRespondentPack != null) {
            if (SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative.toString().equalsIgnoreCase(
                unServedApplicantPack.getPersonalServiceBy())
                || SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative.toString().equalsIgnoreCase(
                unServedRespondentPack.getPersonalServiceBy())) {
                sendNotificationApplicantLegalRepPersonal(caseData, authorization, emailNotificationDetails,
                                                          unServedApplicantPack, unServedRespondentPack);
            } else {
                if (unServedApplicantPack != null) {
                    sendNotificationForUnservedApplicantPack(caseData, authorization, emailNotificationDetails,
                                                             unServedApplicantPack, bulkPrintDetails);
                }
                if (unServedRespondentPack != null) {
                    if (null == unServedRespondentPack.getPersonalServiceBy()) {
                        final List<Element<String>> partyIds = unServedRespondentPack.getPartyIds();
                        log.info("Sending notification for Respondents ==> {}", partyIds);

                        final List<DynamicMultiselectListElement> respondentList = createPartyDynamicMultiSelectListElement(
                            partyIds);

                        final List<Document> respondentDocs = unwrapElements(unServedRespondentPack.getPackDocument());
                        if (CaseUtils.isCaseCreatedByCitizen(caseData)) {
                            sendNotificationsToCitizenRespondentsC100(authorization, respondentList, caseData, bulkPrintDetails,
                                                                      respondentDocs, false);
                        } else {
                            // Pack R and S only differ in acess code letter, Pack R - email, Pack S - Post
                            sendNotificationToRespondentNonPersonal(caseData, authorization,emailNotificationDetails, bulkPrintDetails,
                                                                    respondentList, respondentDocs, respondentDocs);
                        }
                    } else if (SoaSolicitorServingRespondentsEnum.courtAdmin.toString()
                        .equalsIgnoreCase(unServedRespondentPack.getPersonalServiceBy())
                        || SoaSolicitorServingRespondentsEnum.courtBailiff.toString()
                        .equalsIgnoreCase(unServedRespondentPack.getPersonalServiceBy())) {
                        if (FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                            List<Element<Document>> unServedRespondentPackDocument = new ArrayList<>();
                            unServedRespondentPackDocument.addAll(wrapElements(
                                getCoverLettertsforDaCourtAdminCourtBailiffPersonalService(caseData, authorization)));
                            unServedRespondentPackDocument.addAll(unServedRespondentPack.getPackDocument());
                            caseData = caseData.toBuilder().serviceOfApplication(caseData.getServiceOfApplication().toBuilder()
                                                                                     .unServedRespondentPack(
                                                                                         unServedRespondentPack
                                                                                             .toBuilder()
                                                                                             .packDocument(
                                                                                                 unServedRespondentPackDocument)
                                                                                             .build())
                                                                                     .build()).build();
                        }
                    }
                }

            }
        }
        // send notification for others
        final SoaPack unServedOthersPack = caseData.getServiceOfApplication().getUnServedOthersPack();

        if (unServedOthersPack != null) {
            sendNotificationForOthersPack(caseData, authorization, bulkPrintDetails, unServedOthersPack);
        }
        //serving Local authority
        checkAndServeLocalAuthorityEmail(caseData, authorization, emailNotificationDetails);
        log.info("Cafcass Cymru option {}", caseData.getServiceOfApplication().getSoaCafcassCymruServedOptions());
        log.info("Cafcass Cymru email {}", caseData.getServiceOfApplication().getSoaCafcassCymruEmail());
        //serving cafcass cymru
        checkAndSendCafcassCymruEmails(caseData, emailNotificationDetails);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE));
        String formatter = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS).format(zonedDateTime);
        List<Element<ServedApplicationDetails>> finalServedApplicationDetailsList;
        if (caseData.getFinalServedApplicationDetailsList() != null) {
            finalServedApplicationDetailsList = caseData.getFinalServedApplicationDetailsList();
        } else {
            finalServedApplicationDetailsList = new ArrayList<>();
        }
        finalServedApplicationDetailsList.add(element(ServedApplicationDetails.builder().emailNotificationDetails(emailNotificationDetails)
                                                          .servedBy(userService.getUserDetails(authorization).getFullName())
                                                          .servedAt(formatter)
                                                          .modeOfService(CaseUtils.getModeOfService(emailNotificationDetails, bulkPrintDetails))
                                                          .whoIsResponsible(COURT)
                                                          .bulkPrintDetails(bulkPrintDetails).build()));
        caseData.setFinalServedApplicationDetailsList(finalServedApplicationDetailsList);
        return caseData;
    }

    private void sendNotificationApplicantLegalRepPersonal(CaseData caseData, String authorization,
                                                           List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                           SoaPack unServedApplicantPack, SoaPack unServedRespondentPack) {
        if (FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            emailNotificationDetails.addAll(sendEmailDaPersonalApplicantLegalRep(
                caseData,
                authorization,
                unwrapElements(unServedApplicantPack.getPackDocument()),
                unwrapElements(unServedRespondentPack.getPackDocument())
            ));
        } else {
            emailNotificationDetails.addAll(sendEmailCaPersonalApplicantLegalRep(
                caseData,
                authorization,
                unwrapElements(unServedApplicantPack.getPackDocument())
            ));
        }
    }

    private void checkAndServeLocalAuthorityEmail(CaseData caseData, String authorization,
                                                  List<Element<EmailNotificationDetails>> emailNotificationDetails) {
        final SoaPack unServedLaPack = caseData.getServiceOfApplication().getUnServedLaPack();
        if (!ObjectUtils.isEmpty(unServedLaPack) && CollectionUtils.isNotEmpty(unServedLaPack.getPartyIds())) {
            log.info("*** La pack present *** {}", unServedLaPack);
            try {
                emailNotificationDetails.add(element(serviceOfApplicationEmailService
                    .sendEmailNotificationToLocalAuthority(
                        authorization,
                        caseData,
                        unServedLaPack.getPartyIds().get(0).getValue(),
                        ElementUtils.unwrapElements(unServedLaPack.getPackDocument()),
                        PrlAppsConstants.SERVED_PARTY_LOCAL_AUTHORITY)));
            } catch (IOException e) {
                log.error("Failed to serve application via email notification to La {}", e.getMessage());
            }
        }
    }

    private void checkAndSendCafcassCymruEmails(CaseData caseData, List<Element<EmailNotificationDetails>> emailNotificationDetails) {
        if (YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaCafcassCymruServedOptions())
            && null != caseData.getServiceOfApplication().getSoaCafcassCymruEmail()) {
            log.info("Sending notifiction for Cafcass Cymru");
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

        log.info("Sending notification for others ==> {}", otherPartyIds);
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
        log.info("Sending notification for Applicants ====> {}", unServedApplicantPack);
        log.info("Case created by {}", CaseUtils.isCaseCreatedByCitizen(caseData));
        List<Document> packDocs = new ArrayList<>(unwrapElements(unServedApplicantPack.getPackDocument()));
        if (CaseUtils.isCaseCreatedByCitizen(caseData)) {
            //#SOA TO DO... Add a new method to handle after check emails
            emailNotificationDetails.addAll(sendNotificationsAfterConfCheckToCitizenApplicantsC100(
                authorization,
                applicantList,
                caseData,
                bulkPrintDetails,
                packDocs
            ));
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

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();
        final ResponseEntity<SubmittedCallbackResponse> response;

        if (caseData.getServiceOfApplication().getApplicationServedYesNo() != null
            && Yes.equals(caseData.getServiceOfApplication().getApplicationServedYesNo())) {
            response = servePacksWithConfidentialDetails(authorisation, caseData, caseDataMap);
            CaseUtils.setCaseState(callbackRequest, caseDataMap);
            log.info("**** Case status :  {}", caseDataMap.get("caseStatus"));
        } else {
            response = rejectPacksWithConfidentialDetails(caseData, caseDataMap);
        }
        caseDataMap.put(APPLICATION_SERVED_YES_NO, null);
        caseDataMap.put(REJECTION_REASON, null);
        caseDataMap.put(UNSERVED_APPLICANT_PACK, null);
        if (null != caseData.getServiceOfApplication().getUnServedRespondentPack()
            && (null == caseData.getServiceOfApplication().getUnServedRespondentPack().getPersonalServiceBy()
            || SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative.toString().equalsIgnoreCase(
            caseData.getServiceOfApplication().getUnServedRespondentPack().getPersonalServiceBy()))) {
            caseDataMap.put(UNSERVED_RESPONDENT_PACK, null);
        }
        caseDataMap.put(UNSERVED_OTHERS_PACK, null);
        caseDataMap.put(UNSERVED_LA_PACK, null);
        coreCaseDataService.triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            INTERNAL_UPDATE_ALL_TABS,
            caseDataMap
        );
        return response;
    }

    private ResponseEntity<SubmittedCallbackResponse> rejectPacksWithConfidentialDetails(CaseData caseData, Map<String, Object> caseDataMap) {
        final ResponseEntity<SubmittedCallbackResponse> response;
        // TODO  - create work allocation task

        log.info("Confidential check failed, Applicantion, can't be served");

        List<Element<ConfidentialCheckFailed>> confidentialCheckFailedList = new ArrayList<>();
        if (!org.springframework.util.CollectionUtils.isEmpty(caseData.getServiceOfApplication().getConfidentialCheckFailed())) {
            log.info("Reject reason list not empty");
            // get existing reject reason
            confidentialCheckFailedList.addAll(caseData.getServiceOfApplication().getConfidentialCheckFailed());
        }
        log.info("Reject reason list empty, adding first reject reason");

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE));
        String formatter = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS).format(zonedDateTime);
        final ConfidentialCheckFailed confidentialCheckFailed = ConfidentialCheckFailed.builder().confidentialityCheckRejectReason(
                caseData.getServiceOfApplication().getRejectionReason())
            .dateRejected(formatter)
            .build();

        confidentialCheckFailedList.add(ElementUtils.element(confidentialCheckFailed));

        log.info("Confidential check Reject Reason ======> {}", confidentialCheckFailedList);

        caseDataMap.put(CONFIDENTIAL_CHECK_FAILED, confidentialCheckFailedList);

        response = ok(SubmittedCallbackResponse.builder()
                      .confirmationHeader(RETURNED_TO_ADMIN_HEADER)
                      .confirmationBody(
                          CONFIDENTIAL_CONFIRMATION_YES_BODY_PREFIX).build());
        return response;
    }

    private ResponseEntity<SubmittedCallbackResponse> servePacksWithConfidentialDetails(String authorisation, CaseData caseData,
                                                                                        Map<String, Object> caseDataMap) {
        final ResponseEntity<SubmittedCallbackResponse> response;
        caseData = sendNotificationsForUnServedPacks(
            caseData,
            authorisation
        );

        caseDataMap.put(FINAL_SERVED_APPLICATION_DETAILS_LIST, caseData.getFinalServedApplicationDetailsList());
        caseDataMap.put(UNSERVED_RESPONDENT_PACK, caseData.getServiceOfApplication().getUnServedRespondentPack());
        response = ok(SubmittedCallbackResponse.builder()
                          .confirmationHeader(APPLICATION_SERVED_HEADER)
                          .confirmationBody(
                              CONFIDENTIAL_CONFIRMATION_NO_BODY_PREFIX).build());
        return response;
    }

    private List<Element<EmailNotificationDetails>> sendNotificationsAfterConfCheckToCitizenApplicantsC100(String authorization,
                                                                                             List<DynamicMultiselectListElement> selectedApplicants,
                                                                                             CaseData caseData,
                                                                                   List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                                                             List<Document> docs) {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        List<Element<CaseInvite>> caseInvites = caseData.getCaseInvites() != null ? caseData.getCaseInvites()
            : new ArrayList<>();
        selectedApplicants.forEach(applicant -> {
            Optional<Element<PartyDetails>> selectedParty = getParty(applicant.getCode(), caseData.getApplicants());
            if (selectedParty.isPresent()) {
                Element<PartyDetails> selectedApplicant = selectedParty.get();
                CaseInvite caseInvite = getCaseInvite(selectedApplicant.getId(),caseInvites);
                if (caseInvite == null) {
                    caseInvite = c100CaseInviteService.generateCaseInvite(selectedApplicant, Yes);
                    caseInvites.add(element(caseInvite));
                }
                if (isAccessEnabled(selectedApplicant)) {
                    log.info("Access already enabled");
                    if (ContactPreferences.digital.equals(selectedApplicant.getValue().getContactPreferences())) {
                        sendEmailToCitizen(authorization, caseData, selectedApplicant, emailNotificationDetails, docs);
                    } else {
                        sendPostWithAccessCodeLetterToParty(caseData, authorization,
                                                            docs,
                                                            bulkPrintDetails, selectedApplicant, Templates.AP6_LETTER,
                                                            SERVED_PARTY_APPLICANT);
                    }
                } else {
                    log.info("Access to be granted");
                    if (ContactPreferences.digital.equals(selectedApplicant.getValue().getContactPreferences())) {
                        Document ap6Letter = generateAccessCodeLetter(authorization, caseData, selectedApplicant, caseInvite,
                                                                      Templates.AP6_LETTER);
                        List<Document> combinedDocs = new ArrayList<>(Collections.singletonList(ap6Letter));
                        combinedDocs.addAll(docs);
                        sendEmailToCitizen(authorization, caseData, selectedApplicant,
                                           emailNotificationDetails, combinedDocs);
                    } else {
                        sendPostWithAccessCodeLetterToParty(caseData, authorization,
                                                            getNotificationPack(caseData, PrlAppsConstants.R, docs),
                                                            bulkPrintDetails, selectedApplicant, Templates.AP6_LETTER,
                                                            SERVED_PARTY_APPLICANT);
                    }
                }
            }
            caseData.setCaseInvites(caseInvites);
        });
        return emailNotificationDetails;
    }

    public AboutToStartOrSubmitCallbackResponse soaValidation(CallbackRequest callbackRequest) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        List<String> errorList = new ArrayList<>();

        if (null != caseData.getServiceOfApplication().getSoaOtherParties().getValue()
            && !caseData.getServiceOfApplication().getSoaOtherParties().getValue().isEmpty()) {

            List<String> c6aOrderIds = new ArrayList<>();

            if (null != caseData.getOrderCollection()) {
                c6aOrderIds = caseData.getOrderCollection().stream()
                    .filter(element -> element.getValue() != null && element.getValue().getOrderTypeId().equals(
                        CreateSelectOrderOptionsEnum.noticeOfProceedingsNonParties.toString()))
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
