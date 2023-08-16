package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.config.templates.Templates;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
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
import uk.gov.hmcts.reform.prl.models.serviceofapplication.AccessCode;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.DocumentListForLa;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.pin.C100CaseInviteService;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.services.pin.FL401CaseInviteService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

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
import java.util.stream.Collectors;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_AP7;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_RE5;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PRIVACY_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_APPLICATION_SCREEN_1;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_C6A_OTHER_PARTIES_ORDER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_C9_PERSONAL_SERVICE_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_CONFIDENTIAL_DETAILS_PRESENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_CYMRU_EMAIL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_DOCUMENT_PLACE_HOLDER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_FL415_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_FL416_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_ORDER_LIST_EMPTY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_OTHER_PARTIES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_OTHER_PEOPLE_PRESENT_IN_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_RECIPIENT_OPTIONS;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.services.SendAndReplyService.ARROW_SEPARATOR;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;


@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({"java:S3776","java:S6204","java:S112","java:S4144"})
public class ServiceOfApplicationService {
    public static final String UNSERVED_APPLICANT_PACK = "unServedApplicantPack";
    public static final String UNSERVED_RESPONDENT_PACK = "unServedRespondentPack";
    public static final String UNSERVED_OTHERS_PACK = "unServedOthersPack";
    public static final String APPLICATION_SERVED_YES_NO = "applicationServedYesNo";
    public static final String REJECTION_REASON = "rejectionReason";
    public static final String FINAL_SERVED_APPLICATION_DETAILS_LIST = "finalServedApplicationDetailsList";
    public static final String CONFIDENTIAL_CHECK_FAILED = "confidentialCheckFailed";
    public static final String INTERNAL_UPDATE_ALL_TABS = "internal-update-all-tabs";
    public static final String APPLICANTS = "applicants";
    public static final String CASE_INVITES = "caseInvites";
    public static final String EMAIL = "email";
    public static final String POST = "post";
    public static final String COURT = "Court";
    public static final String BY_EMAIL = "By email";
    public static final String BY_EMAIL_AND_POST = "By email and post";
    public static final String BY_POST = "By post";
    public static final String DA_APPLICANT_NAME = "daApplicantName";
    private final LaunchDarklyClient launchDarklyClient;

    public static final String RETURNED_TO_ADMIN_HEADER = "# Application returned to admin";
    public static final String APPLICATION_SERVED_HEADER = "# Application served";
    public static final String CONFIDENTIAL_CONFIRMATION_NO_BODY_PREFIX = "### What happens next \n\n The application will "
        + "be served to relevant people in the case";
    public static final String CONFIDENTIAL_CONFIRMATION_YES_BODY_PREFIX = "### What happens next \n\n The application cannot "
        + "be served. The packs will be sent to the filling team to be redacted.";
    public static final String CONFIDENTIAL_CONFIRMATION_HEADER = "# The application will be reviewed for confidential details";
    public static final String CONFIDENTIAL_CONFIRMATION_BODY_PREFIX = "### What happens next \n\n The document will "
        + "be reviewed for confidential details";

    public static final String CONFIRMATION_HEADER = "# The application is served";
    public static final String CONFIRMATION_BODY_PREFIX = "### What happens next \n\n The document packs will be served to parties ";

    @Autowired
    private final ServiceOfApplicationEmailService serviceOfApplicationEmailService;

    @Autowired
    private final ServiceOfApplicationPostService serviceOfApplicationPostService;

    @Autowired
    private final CaseInviteManager caseInviteManager;

    private final C100CaseInviteService c100CaseInviteService;

    @Autowired
    @Qualifier("caseSummaryTab")
    private CaseSummaryTabService caseSummaryTabService;

    @Autowired
    private final ObjectMapper objectMapper;

    @Autowired
    private final UserService userService;

    private final FL401CaseInviteService fl401CaseInviteService;

    private final DynamicMultiSelectListService dynamicMultiSelectListService;

    @Autowired
    private final WelshCourtEmail welshCourtEmail;

    private final DgsService dgsService;

    @Value("${citizen.url}")
    private String citizenUrl;

    @Autowired
    private final CoreCaseDataService coreCaseDataService;

    private final SendAndReplyService sendAndReplyService;

    private final AuthTokenGenerator authTokenGenerator;

    private final CoreCaseDataApi coreCaseDataApi;

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
            "<ul><li>C100</li><li>C1A</li><li>C7</li><li>C1A (blank)</li><li>C8 (Cafcass)</li>");
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
        List<Element<PartyDetails>> otherPeopleInCase = caseData.getOthersToNotify();
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

    public ServedApplicationDetails sendNotificationForServiceOfApplication(CaseData caseData, String authorization)
        throws Exception {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
        String whoIsResponsibleForServing = COURT;
        if (CaseCreatedBy.CITIZEN.equals(caseData.getCaseCreatedBy())) {
            whoIsResponsibleForServing = handleNotificationsForCitizenCreatedCase(caseData, authorization,
                                                                                  emailNotificationDetails, bulkPrintDetails);
        } else {
            log.info("Not created by citizen");
            whoIsResponsibleForServing = handleNotificationsForSolicitorCreatedCase(caseData, authorization, emailNotificationDetails,
                                                       bulkPrintDetails);
        }
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            //serving cafcass will be enabled after business confirmation
            /*if (YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaCafcassServedOptions())
            && null != caseData.getServiceOfApplication().getSoaCafcassEmailId()) {
            log.info("serving cafcass email : " + caseData.getServiceOfApplication().getSoaCafcassEmailId());
            emailNotificationDetails.addAll(sendEmailToCafcassInCase(
            caseData,
            caseData.getServiceOfApplication().getSoaCafcassEmailId(),
            PrlAppsConstants.SERVED_PARTY_CAFCASS
            ));
            }*/
            //serving cafcass cymru
            checkAndSendCafcassCymruEmails(caseData, emailNotificationDetails);
        }
        List<Document> docsForLa = getDocsToBeServedToLa(authorization, caseData, emailNotificationDetails);
        log.info("Sending notifiction to LA");
        if (null != docsForLa) {
            try {
                emailNotificationDetails.add(element(serviceOfApplicationEmailService
                                                         .sendEmailNotificationToLocalAuthority(authorization,
                                                                            caseData,
                                                                            caseData.getServiceOfApplication()
                                                                                .getSoaLaEmailAddress(),
                                                                            docsForLa,
                                                                            PrlAppsConstants.SERVED_PARTY_CAFCASS_CYMRU)));
            } catch (IOException e) {
                log.error("Failed to serve email to Local Authority");
            }
        }
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE));
        String formatter = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS).format(zonedDateTime);
        log.info("*** Email notification details {}", emailNotificationDetails);
        return ServedApplicationDetails.builder().emailNotificationDetails(emailNotificationDetails)
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .servedAt(formatter)
            .modeOfService(getModeOfService(emailNotificationDetails, bulkPrintDetails))
            .whoIsResponsible(whoIsResponsibleForServing)
            .bulkPrintDetails(bulkPrintDetails).build();
    }

    private String handleNotificationsForCitizenCreatedCase(CaseData caseData, String authorization,
                                                          List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                          List<Element<BulkPrintDetails>> bulkPrintDetails) {
        //CITIZEN SCENARIO
        String whoIsResponsibleForServing = "";
        List<Document> c100StaticDocs = serviceOfApplicationPostService.getStaticDocs(authorization, caseData);
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            log.info("Sending service of application notifications to C100 citizens");
            //serviceOfApplicationEmailService.sendEmailToC100Applicants(caseData);
            if (YesOrNo.No.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())
                && (caseData.getServiceOfApplication().getSoaRecipientsOptions() != null)
                && (caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue().size() > 0)) {
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
                && caseData.getServiceOfApplication().getSoaOtherParties().getValue().size() > 0) {
                log.info("sending notification to Other in case of Citizen");
                sendNotificationToOthers(caseData, authorization, bulkPrintDetails, c100StaticDocs);
            }
        } else {
            if (SoaCitizenServingRespondentsEnum.unrepresentedApplicant
                .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsDA())) {
                List<Document> packEdocs = getNotificationPack(caseData, PrlAppsConstants.E, c100StaticDocs);
                List<Document> packFdocs = getNotificationPack(caseData, PrlAppsConstants.F, c100StaticDocs);
                Element<PartyDetails> applicant = Element.<PartyDetails>builder()
                    .id(caseData.getApplicantsFL401().getPartyId())
                    .value(caseData.getApplicantsFL401())
                    .build();
                CaseInvite caseInvite = getCaseInvite(applicant.getId(), caseData.getCaseInvites());
                if (Yes.equals(caseData.getDoYouNeedAWithoutNoticeHearing())) {
                    generateAccessCodeLetter(authorization, caseData, applicant,caseInvite,Templates.PRL_LET_ENG_FL401_RE2);
                } else {
                    generateAccessCodeLetter(authorization, caseData, applicant,caseInvite,Templates.PRL_LET_ENG_FL401_RE3);
                }
            } else {
                List<Document> packCdocs = getNotificationPack(caseData, PrlAppsConstants.C, c100StaticDocs);
                List<Document> packDdocs = getNotificationPack(caseData, PrlAppsConstants.D, c100StaticDocs);
            }
            log.info("#SOA TO DO ... FL401 citizen created case");
        }
        return whoIsResponsibleForServing;
    }

    private String handleNotificationsForSolicitorCreatedCase(CaseData caseData, String authorization,
                                                            List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                            List<Element<BulkPrintDetails>> bulkPrintDetails) throws Exception {

        String whoIsResponsibleForServing;
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            whoIsResponsibleForServing = handleNotificationsCaSolicitorCreatedCase(caseData, authorization,
                                                                                   emailNotificationDetails, bulkPrintDetails);
        } else {
            whoIsResponsibleForServing = handleNotificationsDaSolicitorCreatedCase(caseData, authorization, emailNotificationDetails);
        }
        return whoIsResponsibleForServing;
    }

    private String handleNotificationsCaSolicitorCreatedCase(CaseData caseData, String authorization,
                                                           List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                           List<Element<BulkPrintDetails>> bulkPrintDetails) throws Exception {
        String whoIsResponsibleForServing = COURT;
        List<Document> c100StaticDocs = serviceOfApplicationPostService.getStaticDocs(authorization, caseData);
        if (caseData.getServiceOfApplication().getSoaServeToRespondentOptions() != null
            && YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())) {
            if (SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative.equals(caseData.getServiceOfApplication()
                                                                                           .getSoaServingRespondentsOptionsCA())) {
                log.info("Personal Service - Case created by - Solicitor");
                whoIsResponsibleForServing = caseData.getApplicants().get(0).getValue().getRepresentativeFullName();
                //This is added with assumption that, For applicant legl representative selection
                // if multiple applicants are present only the first applicant solicitor will receive notification
                List<Document> packHiDocs = getNotificationPack(caseData, PrlAppsConstants.HI, c100StaticDocs);
                packHiDocs.addAll(c100StaticDocs);
                emailNotificationDetails.add(element(serviceOfApplicationEmailService
                                                         .sendEmailNotificationToSolicitor(
                                                             authorization, caseData,
                                                             caseData.getApplicants().get(0).getValue(),
                                                             EmailTemplateNames.APPLICANT_SOLICITOR_CA,
                                                             packHiDocs,
                                                             SERVED_PARTY_APPLICANT_SOLICITOR
                                                         )));
            } else if (SoaSolicitorServingRespondentsEnum.courtBailiff
                .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA())
                || SoaSolicitorServingRespondentsEnum.courtAdmin
                .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA())) {
                log.error("#SOA TO DO ... generate J K packs to serve by courtadmin/bailiff");
                List<Document> packJkDocs = getNotificationPack(caseData, PrlAppsConstants.H, c100StaticDocs);
            }
        } else if (YesOrNo.No.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())
            && (caseData.getServiceOfApplication().getSoaRecipientsOptions() != null)
            && (caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue().size() > 0)) {
            log.info("Non personal Service - Case created by - Solicitor");
            c100StaticDocs = c100StaticDocs.stream().filter(d -> ! d.getDocumentFileName().equalsIgnoreCase(
                C9_DOCUMENT_FILENAME)).collect(
                Collectors.toList());
            log.info("serving applicants or respondents");
            List<DynamicMultiselectListElement> selectedApplicants = getSelectedApplicantsOrRespondents(
                caseData.getApplicants(),
                caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue()
            );

            log.info("selected Applicants " + selectedApplicants.size());
            if (selectedApplicants.size() > 0) {
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
            if (selectedRespondents.size() > 0) {
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
            && caseData.getServiceOfApplication().getSoaOtherParties().getValue().size() > 0) {
            sendNotificationToOthers(caseData, authorization, bulkPrintDetails, c100StaticDocs);
        }
        return whoIsResponsibleForServing;
    }

    private String handleNotificationsDaSolicitorCreatedCase(CaseData caseData, String authorization,
                                                           List<Element<EmailNotificationDetails>> emailNotificationDetails) {
        String whoIsResponsibleForServing;
        List<Document> staticDocs = serviceOfApplicationPostService.getStaticDocs(authorization, caseData);


        whoIsResponsibleForServing = caseData.getApplicantsFL401().getRepresentativeFullName();
        log.info("Fl401 case journey for caseId {}", caseData.getId());
        if (SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative.equals(caseData.getServiceOfApplication()
                                                                                       .getSoaServingRespondentsOptionsDA())) {
            List<Document> packADocs = getNotificationPack(caseData, PrlAppsConstants.A, staticDocs);
            List<Document> packBDocs = getNotificationPack(caseData, PrlAppsConstants.B, staticDocs);
            emailNotificationDetails.addAll(sendNotificationToFl401Solicitor(caseData, authorization, packADocs, packBDocs));

        } else {
            log.error("#SOA TO DO... Generate C, D packs to be served by admin/bailiff.. common method to be used by "
                          + "citizen and solicitor created case");
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
        if (selectedApplicants != null
            && selectedApplicants.size() > 0) {
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

        if (selectedRespondents != null
            && selectedRespondents.size() > 0) {
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
            List<Document> packLdocs = getNotificationPack(caseData, PrlAppsConstants.L, c100StaticDocs);
            List<Document> packMdocs = getNotificationPack(caseData, PrlAppsConstants.M, c100StaticDocs);
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
            List<Document> packJdocs = getNotificationPack(caseData, PrlAppsConstants.J, c100StaticDocs);
            List<Document> packKdocs = getNotificationPack(caseData, PrlAppsConstants.K, c100StaticDocs);
        }
    }

    public Map<String, Object> handleAboutToSubmit(CallbackRequest callbackRequest) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();
        if (ObjectUtils.isEmpty(caseDataMap.get("proceedToServing"))) {
            caseDataMap.put("proceedToServing", Yes);
            log.info("SOA proceed to serving {}", caseDataMap.get("proceedToServing"));
        }
        if (caseData.getServiceOfApplication() != null && SoaCitizenServingRespondentsEnum.unrepresentedApplicant
            .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsCA())) {
            caseData.getApplicants().get(0).getValue().getResponse().getCitizenFlags().setIsApplicationToBeServed(YesOrNo.Yes);
            caseDataMap.put(APPLICANTS, caseData.getApplicants());
        }

        caseDataMap.put(CASE_INVITES, generateCaseInvitesForParties(caseData));
        //cleanUpSoaSelections(caseDataMap, true);
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
        finalServedApplicationDetailsList.add(element(sendNotificationForServiceOfApplication(
            caseData,
            authorisation
        )));
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
                    log.info("Access to be granted");
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

    private Boolean isAccessEnabled(Element<PartyDetails> party) {
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

    private List<Element<BulkPrintDetails>> sendPostToCitizen(String authorization, CaseData caseData,
                                                                      Element<PartyDetails> party, List<Document> docs,
                                                              String servedParty) {
        List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
        log.info("*** docs {}", docs);
        bulkPrintDetails.add(element(serviceOfApplicationPostService.sendPostNotificationToParty(
            caseData,
            authorization,
            party.getValue(),
            docs,
            servedParty
        )));
        return bulkPrintDetails;
    }

    private String getModeOfService(List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                    List<Element<BulkPrintDetails>> bulkPrintDetails) {
        String temp = null;
        if (null != emailNotificationDetails && !emailNotificationDetails.isEmpty()) {
            temp = BY_EMAIL;
        }
        if (null != bulkPrintDetails && !bulkPrintDetails.isEmpty()) {
            if (null != temp) {
                temp = BY_EMAIL_AND_POST;
            } else {
                temp = BY_POST;
            }
        }
        return temp;
    }

    private List<Element<EmailNotificationDetails>> sendNotificationToFl401Solicitor(CaseData caseData, String authorization, List<Document> packA,
                                                                                     List<Document> packB) {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        PartyDetails applicant = caseData.getApplicantsFL401();
        if (applicant.getSolicitorEmail() != null) {
            try {
                log.info(
                    "Sending the email notification to applicant solicitor for FL401 Application for caseId {}",
                    caseData.getId()
                );
                //Applicant's pack
                emailNotificationDetails.add(element(serviceOfApplicationEmailService.sendEmailNotificationToApplicantSolicitor(
                    authorization,
                    caseData,
                    applicant,
                    EmailTemplateNames.APPLICANT_SOLICITOR_DA,
                    packA,
                    SERVED_PARTY_APPLICANT_SOLICITOR
                )));
                //Respondent's pack
                log.error("#SOA TO DO With notice add RE3 letter, without notice add RE2, gov notification not required so remove it");
                emailNotificationDetails.add(element(serviceOfApplicationEmailService.sendEmailNotificationToApplicantSolicitor(
                    authorization,
                    caseData,
                    applicant,
                    EmailTemplateNames.APPLICANT_SOLICITOR_DA,
                    packB,
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

        return value.stream().filter(element -> applicantsOrRespondents.stream().anyMatch(party -> party.getId().toString().equals(
            element.getCode()))).collect(
            Collectors.toList());
    }

    public List<Element<EmailNotificationDetails>> sendNotificationToApplicantSolicitor(CaseData caseData, String authorization,
                                                                                        List<DynamicMultiselectListElement> selectedApplicants,
                                                                                        List<Document> packQ, String servedParty) {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        List<Element<PartyDetails>> applicantsInCase;
        EmailTemplateNames templateName;
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            applicantsInCase = caseData.getApplicants();
            templateName = EmailTemplateNames.APPLICANT_SOLICITOR_CA;
        } else {
            applicantsInCase = List.of(Element.<PartyDetails>builder()
                                           .id(caseData.getApplicantsFL401().getPartyId())
                                           .value(caseData.getApplicantsFL401()).build());
            templateName = EmailTemplateNames.APPLICANT_SOLICITOR_DA;
        }

        selectedApplicants.forEach(applicant -> {
            Optional<Element<PartyDetails>> party = getParty(applicant.getCode(), applicantsInCase);
            if (party.isPresent() && party.get().getValue().getSolicitorEmail() != null) {
                try {
                    log.info(
                        "Sending the email notification to applicant solicitor for C100 Application for caseId {}",
                        caseData.getId()
                    );

                    emailNotificationDetails.add(element(serviceOfApplicationEmailService
                                                             .sendEmailNotificationToApplicantSolicitor(
                                                                 authorization, caseData, party.get().getValue(),
                                                                 templateName,
                                                                 packQ,
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
        List<Element<PartyDetails>> respondentListC100 = caseData.getRespondents();
        selectedRespondent.forEach(respondentc100 -> {
            Optional<Element<PartyDetails>> party = getParty(respondentc100.getCode(), respondentListC100);
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
            throw new RuntimeException(e);
        }
    }

    private Optional<Element<PartyDetails>> getParty(String code, List<Element<PartyDetails>> parties) {
        Optional<Element<PartyDetails>> party;
        party = parties.stream()
            .filter(element -> element.getId().toString().equalsIgnoreCase(code)).findFirst();

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

    private List<Document> getNotificationPack(CaseData caseData, String requiredPack, List<Document> staticDocs) {
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
                            C7_BLANK_DOCUMENT_FILENAME))
                        .collect(Collectors.toList()));
        return docs;
    }

    private List<Document> generatePackK(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        // Annex Y to be excluded
        docs.addAll(staticDocs.stream()
            .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_C9_PERSONAL_SERVICE_FILENAME))
            .collect(Collectors.toList()));
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
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_C9_PERSONAL_SERVICE_FILENAME))
                        .collect(Collectors.toList()));
        return docs;
    }

    private List<Document> generatePackZ(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        return docs;
    }

    private List<Document> generatePackHI(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        return docs;
    }

    private List<Document> generatePackN(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getC6aIfPresent(getSoaSelectedOrders(caseData)));
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
        return docs;
    }

    private List<Document> generatePackI(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
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
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_C9_PERSONAL_SERVICE_FILENAME))
                        .collect(Collectors.toList()));
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
                            C7_BLANK_DOCUMENT_FILENAME))
                        .collect(Collectors.toList()));
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
        docs.addAll(staticDocs);
        return docs;
    }

    private List<Document> generatePackB(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        docs.addAll(staticDocs);
        return docs;
    }

    private List<Document> generatePackC(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        docs.addAll(staticDocs.stream()
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_FL415_FILENAME))
                        .collect(Collectors.toList()));
        return docs;
    }

    private List<Document> generatePackD(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        docs.addAll(staticDocs.stream()
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_FL416_FILENAME))
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_FL415_FILENAME))
                        .collect(Collectors.toList()));
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
                        .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(SOA_FL415_FILENAME))
                        .collect(Collectors.toList()));
        return docs;
    }

    private List<Document> generatePackG(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        return docs;
    }

    private List<Document> generatePackO(CaseData caseData, List<Document> staticDocs) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.add(caseData.getC8Document());
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
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
                .getValue().stream().map(DynamicMultiselectListElement::getCode)
                .collect(Collectors.toList());
            orderCodes.stream().forEach(orderCode ->
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
            "proceedToServing"
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
            dynamicMultiSelectListService.getOrdersAsDynamicMultiSelectList(caseData, null)
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
        caseDataUpdated.put(SOA_CONFIDENTIAL_DETAILS_PRESENT, CaseUtils.isC8Present(caseData) ? Yes : No);
        caseDataUpdated.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        caseDataUpdated.put(CASE_CREATED_BY, caseData.getCaseCreatedBy());
        List<Element<DocumentListForLa>> documentDynamicListLa = getDocumentsDynamicListForLa(authorisation,
                                                                                              String.valueOf(caseData.getId()));
        log.info("** dynamic list 1 ** {}", documentDynamicListLa);
        caseDataUpdated.put("soaDocumentDynamicListForLa", documentDynamicListLa);
        log.info("** dynamic list 2 ** {}", caseDataUpdated.get("soaDocumentDynamicListForLa"));
        return caseDataUpdated;
    }

    private List<Element<DocumentListForLa>> getDocumentsDynamicListForLa(String authorisation, String caseId) {
        return List.of(Element.<DocumentListForLa>builder().id(UUID.randomUUID()).value(DocumentListForLa.builder()
                                                                                      .documentsListForLa(sendAndReplyService
                                                                .getCategoriesAndDocuments(authorisation, caseId))
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
            "Certain documents will be automatically included in the pack this is served on parties(the people in the case)");
        collapsible.add(
            "This includes");
        collapsible.add(
            "<ul><li>an application form</li><li>witness statement</li><li>privacy notice</li><li>cover letter</li></ul>");
        collapsible.add(
            "You do not need to upload these documents yourself");
        collapsible.add("</div>");
        collapsible.add("</details>");
        return String.join("\n\n", collapsible);
    }

    public Document generateAccessCodeLetter(String authorisation, CaseData caseData,Element<PartyDetails> party,
                                      CaseInvite caseInvite, String template) {
        Map<String, Object> dataMap = populateAccessCodeMap(caseData, party, caseInvite);
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
            log.error("*** Access code letter failed for {} :: because of {}", template, e.getStackTrace());
        }
        return null;
    }

    public Map<String, Object> populateAccessCodeMap(CaseData caseData, Element<PartyDetails> party, CaseInvite caseInvite) {
        log.info("*** case invite {}", caseInvite);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", caseData.getId());
        dataMap.put("serviceUrl", citizenUrl);
        dataMap.put("accessCode", getAccessCode(caseInvite, party.getValue().getAddress(), party.getValue().getLabelForDynamicList()));
        dataMap.put("c1aExists", doesC1aExists(caseData));
        if (FL401_CASE_TYPE.equals(CaseUtils.getCaseTypeOfApplication(caseData))) {
            dataMap.put(DA_APPLICANT_NAME, caseData.getApplicantsFL401().getLabelForDynamicList());
        }
        return dataMap;
    }

    private AccessCode getAccessCode(CaseInvite caseInvite, Address address, String name) {
        if (null != caseInvite) {
            return AccessCode.builder()
                .code(caseInvite.getAccessCode())
                .recipientName(name)
                .address(address)
                .isLinked(caseInvite.getHasLinked())
                .currentDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                .respondByDate(LocalDate.now().plusDays(14).format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                .build();
        }
        return null;
    }

    private YesOrNo doesC1aExists(CaseData caseData) {
        if (caseData.getC1AWelshDocument() != null || caseData.getC1ADocument() != null) {
            return Yes;
        }
        return No;
    }

    private CaseInvite getCaseInvite(UUID partyId, List<Element<CaseInvite>> caseInvites) {
        if (caseInvites != null) {
            Optional<Element<CaseInvite>> caseInvite = caseInvites.stream()
                .filter(caseInviteElement -> caseInviteElement.getValue().getPartyId().equals(partyId)
            ).findFirst();
            return caseInvite.map(Element::getValue).orElse(null);
        }
        return null;
    }

    private List<Element<CaseInvite>> generateCaseInvitesForParties(CaseData caseData) {
        List<Element<CaseInvite>> caseInvites = caseData.getCaseInvites();
        if (caseInvites != null) {
            if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                caseData.getApplicants().forEach(party -> {
                    caseInvites.add(element(c100CaseInviteService.generateCaseInvite(party, Yes)));
                });
                caseData.getRespondents().forEach(party -> caseInvites.add(element(c100CaseInviteService.generateCaseInvite(party, No))));
            } else {
                caseInvites.add(element(fl401CaseInviteService.generateCaseInvite(caseData.getApplicantsFL401(), Yes)));
                caseInvites.add(element(fl401CaseInviteService.generateCaseInvite(caseData.getRespondentsFL401(), No)));
            }
        }
        return caseInvites;
    }

    public Map<String, Object> generatePacksForConfidentialCheckC100(CaseDetails caseDetails, String authorization) {

        log.info("Inside generatePacksForConfidentialCheckC100 Method");

        Map<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE));
        String dateCreated = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS).format(zonedDateTime);
        List<Document> c100StaticDocs = serviceOfApplicationPostService.getStaticDocs(authorization, caseData);
        log.info(
            "caseData.getServiceOfApplication().getSoaServeToRespondentOptions() {}",
            caseData.getServiceOfApplication().getSoaServeToRespondentOptions()
        );
        log.info("caseData.getServiceOfApplication() {}", caseData.getServiceOfApplication());
        if (YesOrNo.No.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())
            && (caseData.getServiceOfApplication().getSoaRecipientsOptions() != null)
            && (caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue().size() > 0)) {
            c100StaticDocs = c100StaticDocs.stream().filter(d -> ! d.getDocumentFileName().equalsIgnoreCase(
                C9_DOCUMENT_FILENAME)).collect(
                Collectors.toList());
            log.info("serving applicants or respondents");
            List<DynamicMultiselectListElement> selectedApplicants = getSelectedApplicantsOrRespondents(
                caseData.getApplicants(),
                caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue()
            );

            // Applicants pack
            if (selectedApplicants != null
                && selectedApplicants.size() > 0) {
                buildUnservedApplicantPack(authorization, caseDataUpdated, caseData, dateCreated, c100StaticDocs, selectedApplicants);

            } else {
                caseDataUpdated.put(UNSERVED_APPLICANT_PACK, null);
            }

            // Respondent pack

            List<DynamicMultiselectListElement> selectedRespondents = getSelectedApplicantsOrRespondents(
                caseData.getRespondents(),
                caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue()
            );
            if (selectedRespondents != null && selectedRespondents.size() > 0) {

                buildUnservedRespondentPack(authorization, caseDataUpdated, caseData, dateCreated, c100StaticDocs, selectedRespondents);

            } else {
                caseDataUpdated.put(UNSERVED_RESPONDENT_PACK, null);
            }

            //serving other people in case
            if (null != caseData.getServiceOfApplication().getSoaOtherParties()
                && caseData.getServiceOfApplication().getSoaOtherParties().getValue().size() > 0) {
                buildUnservedOthersPack(authorization, caseDataUpdated, caseData, dateCreated, c100StaticDocs);
            } else {
                caseDataUpdated.put(UNSERVED_OTHERS_PACK, null);
            }
        } else if (YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())) {
            log.error("#SOA TO DO .. Personal Service to be added - for 4 options");
        }
        return caseDataUpdated;
    }

    public Map<String, Object> generatePacksForConfidentialCheckFl401(CaseDetails caseDetails, String authorization) {
        log.info("Inside generatePacksForConfidentialCheck FL401 Method");
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE));
        Map<String, Object> caseDataUpdated = new HashMap<>();
        String dateCreated = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS).format(zonedDateTime);
        List<Document> fl401StaticDocs = serviceOfApplicationPostService.getStaticDocs(authorization, caseData);
        log.info("caseData.getServiceOfApplication() {}", caseData.getServiceOfApplication());
        if (SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative
            .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA())) {
            caseDataUpdated.putAll(genPacksConfidentialCheckDaApplicantSolicitor(authorization, caseData, dateCreated,
                                                                                 fl401StaticDocs));
        } else if (SoaSolicitorServingRespondentsEnum.courtAdmin
            .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA())
            || SoaSolicitorServingRespondentsEnum.courtBailiff
                .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsDA())
            || SoaCitizenServingRespondentsEnum.courtBailiff
                .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsCA())
            || SoaCitizenServingRespondentsEnum.courtAdmin
                .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsCA())) {
            log.error("#SOA TO DO... Personal courtadmin / court bailiff - case created by- citizen/solicitor");
            List<Document> packCDocs = getNotificationPack(caseData, PrlAppsConstants.C, fl401StaticDocs);
            List<Document> packDDocs = getNotificationPack(caseData, PrlAppsConstants.D, fl401StaticDocs);

        } else if (SoaCitizenServingRespondentsEnum.unrepresentedApplicant
            .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsCA())) {
            log.error("#SOA TO DO... Personal courtadmin / court bailiff - case created by- citizen/solicitor");
            List<Document> packEDocs = getNotificationPack(caseData, PrlAppsConstants.E, fl401StaticDocs);
            List<Document> packFDocs = getNotificationPack(caseData, PrlAppsConstants.F, fl401StaticDocs);
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
        final SoaPack unServedApplicantPack = SoaPack.builder().packDocument(wrapElements(packADocs)).partyIds(
                wrapElements(partyId))
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .packCreatedDate(dateCreated)
            .build();
        caseDataUpdated.put(UNSERVED_APPLICANT_PACK, unServedApplicantPack);

        final SoaPack unServedRespondentPack = SoaPack.builder().packDocument(wrapElements(packBDocs)).partyIds(
                wrapElements(partyId))
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .packCreatedDate(dateCreated)
            .build();

        caseDataUpdated.put(UNSERVED_RESPONDENT_PACK, unServedRespondentPack);
        return caseDataUpdated;
    }

    private void buildUnservedOthersPack(String authorization, Map<String, Object> caseDataUpdated, CaseData caseData, String dateCreated,
                                         List<Document> c100StaticDocs) {
        log.info("serving other people in case");

        final List<DynamicMultiselectListElement> otherParties = getSelectedApplicantsOrRespondents(
            caseData.getOthersToNotify(),
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

        // TODO - do we need respondent pack with bullk print cover letter?

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
        if (CaseCreatedBy.CITIZEN.equals(caseData.getCaseCreatedBy())) {
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


    public ServedApplicationDetails sendNotificationsForUnServedPacks(CaseData caseData, String authorization) {

        log.info("Inside sendNotificationsForUnServedPacks method");
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
        final SoaPack unServedApplicantPack = caseData.getServiceOfApplication().getUnServedApplicantPack();
        if (unServedApplicantPack != null) {
            sendNotificationForUnservedApplicantPack(caseData, authorization, emailNotificationDetails,
                                                     unServedApplicantPack, bulkPrintDetails);
        }
        final SoaPack unServedRespondentPack = caseData.getServiceOfApplication().getUnServedRespondentPack();
        if (unServedRespondentPack != null) {

            final List<Element<String>> partyIds = unServedRespondentPack.getPartyIds();
            log.info("Sending notification for Respondents ==> {}", partyIds);

            final List<DynamicMultiselectListElement> respondentList = createPartyDynamicMultiSelectListElement(
                partyIds);

            final List<Document> packR = unwrapElements(unServedRespondentPack.getPackDocument());
            if (CaseCreatedBy.CITIZEN.equals(caseData.getCaseCreatedBy())) {
                sendNotificationsToCitizenRespondentsC100(authorization, respondentList, caseData, bulkPrintDetails,
                                                          packR, false);
            } else {
                // Pack R and S only differ in acess code letter, Pack R - email, Pack S - Post
                sendNotificationToRespondentNonPersonal(caseData, authorization,emailNotificationDetails, bulkPrintDetails,
                                                        respondentList, packR, packR);
            }
        }
        // send notification for others
        final SoaPack unServedOthersPack = caseData.getServiceOfApplication().getUnServedOthersPack();

        if (unServedOthersPack != null) {
            sendNotificationForOthersPack(caseData, authorization, bulkPrintDetails, unServedOthersPack);
        }

        //serving cafcass will be eneabled after business confirmation
        /*if (YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaCafcassServedOptions())
            && null != caseData.getServiceOfApplication().getSoaCafcassEmailId()) {
            log.info("serving cafcass email : " + caseData.getServiceOfApplication().getSoaCafcassEmailId());
            emailNotificationDetails.addAll(sendEmailToCafcassInCase(
                caseData,
                caseData.getServiceOfApplication().getSoaCafcassEmailId(),
                PrlAppsConstants.SERVED_PARTY_CAFCASS
            ));
        }*/

        log.info("Cafcass Cymru option {}", caseData.getServiceOfApplication().getSoaCafcassCymruServedOptions());
        log.info("Cafcass Cymru email {}", caseData.getServiceOfApplication().getSoaCafcassCymruEmail());
        //serving cafcass cymru
        checkAndSendCafcassCymruEmails(caseData, emailNotificationDetails);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE));
        String formatter = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS).format(zonedDateTime);
        return ServedApplicationDetails.builder().emailNotificationDetails(emailNotificationDetails)
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .servedAt(formatter)
            .modeOfService(getModeOfService(emailNotificationDetails, bulkPrintDetails))
            .whoIsResponsible(COURT)
            .bulkPrintDetails(bulkPrintDetails).build();
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

    private List<Document> getDocsToBeServedToLa(String authorisation, CaseData caseData,
                                               List<Element<EmailNotificationDetails>> emailNotificationDetails) {
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
                    log.info("** Document selected {}", document);
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
        return null;
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
            log.info("** Selected doc {}", selectedDoc);
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
            if (category.getSubCategories() != null) {
                getSelectedDocumentFromCategories(
                    category.getSubCategories(),
                    selectedDocument
                );
            }
        }
        return documentSelected;
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

        log.info("Sending notification for Applicants ====> {}", partyIds);

        if (CaseCreatedBy.CITIZEN.equals(caseData.getCaseCreatedBy())) {
            //#SOA TO DO... Add a new method to handle after check emails
            emailNotificationDetails.addAll(sendNotificationsAfterConfCheckToCitizenApplicantsC100(authorization,applicantList,caseData,
                                                                             bulkPrintDetails,
                                                                   unwrapElements(unServedApplicantPack.getPackDocument())));
        } else {
            emailNotificationDetails.addAll(sendNotificationToApplicantSolicitor(caseData, authorization, applicantList,
                                                                                 unwrapElements(unServedApplicantPack.getPackDocument()),
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
        } else {
            response = rejectPacksWithConfidentialDetails(caseData, caseDataMap);
        }
        caseDataMap.put(APPLICATION_SERVED_YES_NO, null);
        caseDataMap.put(REJECTION_REASON, null);
        caseDataMap.put(UNSERVED_APPLICANT_PACK, null);
        caseDataMap.put(UNSERVED_RESPONDENT_PACK, null);
        caseDataMap.put(UNSERVED_OTHERS_PACK, null);
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
        List<Element<ServedApplicationDetails>> finalServedApplicationDetailsList;
        if (caseData.getFinalServedApplicationDetailsList() != null) {
            finalServedApplicationDetailsList = caseData.getFinalServedApplicationDetailsList();
        } else {
            finalServedApplicationDetailsList = new ArrayList<>();
        }
        finalServedApplicationDetailsList.add(element(sendNotificationsForUnServedPacks(
            caseData,
            authorisation
        )));

        caseDataMap.put(FINAL_SERVED_APPLICATION_DETAILS_LIST, finalServedApplicationDetailsList);

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
}
