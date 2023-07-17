package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.pin.C100CaseInviteService;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.services.pin.FL401CaseInviteService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

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

import static org.springframework.http.ResponseEntity.ok;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_CONFIDENTIAL_DETAILS_PRESENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_CYMRU_EMAIL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_DOCUMENT_PLACE_HOLDER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_ORDER_LIST_EMPTY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_OTHER_PARTIES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_OTHER_PEOPLE_PRESENT_IN_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_RECIPIENT_OPTIONS;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.CommonUtils.formatDateTime;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;


@Service
@Slf4j
@RequiredArgsConstructor
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
    private final ObjectMapper objectMapper;

    @Autowired
    private final UserService userService;

    private final FL401CaseInviteService fl401CaseInviteService;

    private final DynamicMultiSelectListService dynamicMultiSelectListService;

    @Autowired
    WelshCourtEmail welshCourtEmail;

    private final DgsService dgsService;

    @Value("${citizen.url}")
    private String citizenUrl;

    @Autowired
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

    public CaseData sendEmail(CaseDetails caseDetails) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        log.info("Sending service of application email notifications");
        //PRL-3326 - send email to all applicants on application served & issued
        if (CaseCreatedBy.CITIZEN.equals(caseData.getCaseCreatedBy())) {
            serviceOfApplicationEmailService.sendEmailToC100Applicants(caseData);
        } else {
            //PRL-3156 - Skip sending emails for solicitors for c100 case created by Citizen
            if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                //serviceOfApplicationEmailService.sendEmailC100(caseDetails);
            } else {
                //serviceOfApplicationEmailService.sendEmailFL401(caseDetails);
            }
        }
        if (launchDarklyClient.isFeatureEnabled("send-res-email-notification")) {
            caseData = caseInviteManager.generatePinAndSendNotificationEmail(caseData);
        }
        return caseData;
    }

    public CaseData sendPost(CaseDetails caseDetails, String authorization) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        log.info(" Sending post to the parties involved ");
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            serviceOfApplicationPostService.sendDocs(caseData, authorization);
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
            //CITIZEN SCENARIO
            if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                log.info("Sending service of application notifications to C100 citizens");
                serviceOfApplicationEmailService.sendEmailToC100Applicants(caseData);
                if (YesOrNo.No.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())
                    && (caseData.getServiceOfApplication().getSoaRecipientsOptions() != null)
                    && (caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue().size() > 0)) {
                    handleNonPersonalServiceForCitizen(caseData, authorization, emailNotificationDetails, bulkPrintDetails);
                }
                //serving other people in case
                List<Document> c100StaticDocs = serviceOfApplicationPostService.getStaticDocs(authorization, caseData);
                if (null != caseData.getServiceOfApplication().getSoaOtherParties()
                    && caseData.getServiceOfApplication().getSoaOtherParties().getValue().size() > 0) {
                    log.info("sending notification to Other in case of Citizen");
                    sendNotificationToOthers(caseData, authorization, bulkPrintDetails, c100StaticDocs);
                }
                log.info(" ** ci 1 {}", caseData.getCaseInvites());
            }
        } else {
            log.info("Not created by citizen");
            if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                List<Document> c100StaticDocs = serviceOfApplicationPostService.getStaticDocs(authorization, caseData);
                if (caseData.getServiceOfApplication().getSoaServeToRespondentOptions() != null
                    && YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())
                    && SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative
                    .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA())) {
                    whoIsResponsibleForServing =  caseData.getApplicants().get(0).getValue().getRepresentativeFullName();
                    //This is added with assumption that, For applicant legl representative selection
                    // if multiple applicants are present only the first applicant solicitor will receive notification
                    List<Document> packHiDocs = getNotificationPack(caseData, PrlAppsConstants.HI);
                    packHiDocs.addAll(c100StaticDocs);
                    emailNotificationDetails.addAll(sendNotificationToFirstApplicantSolicitor(
                        caseData,
                        authorization,
                        caseData.getApplicants().get(0).getValue(),
                        packHiDocs,
                        SERVED_PARTY_APPLICANT_SOLICITOR
                    ));
                } else if (YesOrNo.No.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())
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

                    log.info("selected Applicants " + selectedApplicants.size());
                    if (selectedApplicants.size() > 0) {
                        List<Document> packQDocs = getNotificationPack(caseData, PrlAppsConstants.Q);
                        packQDocs.addAll(c100StaticDocs.stream()
                                             .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(
                                                 C1A_BLANK_DOCUMENT_FILENAME))
                                             .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(
                                                 C7_BLANK_DOCUMENT_FILENAME))
                                             .collect(Collectors.toList()));
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
                        List<Document> packRDocs = getNotificationPack(caseData, PrlAppsConstants.R);
                        packRDocs.addAll(c100StaticDocs);
                        List<Document> packSDocs = getNotificationPack(caseData, PrlAppsConstants.S);
                        packSDocs.addAll(c100StaticDocs);
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

            } else {
                List<Document> staticDocs = serviceOfApplicationPostService.getStaticDocs(authorization, caseData);
                List<Document> packADocs = getNotificationPack(caseData, PrlAppsConstants.A);
                List<Document> packBDocs = getNotificationPack(caseData, PrlAppsConstants.B);
                packADocs.addAll(staticDocs);
                packBDocs.addAll(staticDocs);
                whoIsResponsibleForServing = caseData.getApplicantsFL401().getRepresentativeFullName();
                log.info("Fl401 case journey for caseId {}", caseData.getId());
                if (SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative.equals(caseData.getServiceOfApplication()
                                                                                   .getSoaServingRespondentsOptionsDA())) {
                    emailNotificationDetails.addAll(sendEmailToFl404Parties(
                        caseData,
                        authorization,
                        packADocs,
                        packBDocs
                    ));
                }
            }

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
            if (YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaCafcassCymruServedOptions())
                && null != caseData.getServiceOfApplication().getSoaCafcassCymruEmail()) {
                emailNotificationDetails.addAll(sendEmailToCafcassInCase(
                    caseData,
                    caseData.getServiceOfApplication().getSoaCafcassCymruEmail(),
                    PrlAppsConstants.SERVED_PARTY_CAFCASS_CYMRU
                ));
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

    private void sendNotificationToOthers(CaseData caseData, String authorization, List<Element<BulkPrintDetails>> bulkPrintDetails,
                                          List<Document> c100StaticDocs) {
        log.info("serving other people in case");

        List<DynamicMultiselectListElement> othersToNotify = getSelectedApplicantsOrRespondents(
            caseData.getOthersToNotify(),
            caseData.getServiceOfApplication().getSoaOtherParties().getValue());

        List<Document> packNDocs = c100StaticDocs.stream().filter(d -> d.getDocumentFileName()
            .equalsIgnoreCase(PRIVACY_DOCUMENT_FILENAME)).collect(
            Collectors.toList());
        packNDocs.addAll(getNotificationPack(caseData, PrlAppsConstants.N));
        bulkPrintDetails.addAll(sendPostToOtherPeopleInCase(caseData, authorization, othersToNotify,
                                                            packNDocs,
                                                            PrlAppsConstants.SERVED_PARTY_OTHER
        ));
    }

    private void sendNotificationToRespondentNonPersonal(CaseData caseData, String authorization,
                                                         List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                         List<Element<BulkPrintDetails>> bulkPrintDetails,
                                                         List<DynamicMultiselectListElement> selectedRespondents, List<Document> packRDocs,
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

    private void handleNonPersonalServiceForCitizen(CaseData caseData, String authorization,
                                                    List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                                    List<Element<BulkPrintDetails>> bulkPrintDetails) {
        List<DynamicMultiselectListElement> selectedApplicants = getSelectedApplicantsOrRespondents(
            caseData.getApplicants(),
            caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue()
        );
        List<DynamicMultiselectListElement> selectedRespondents = getSelectedApplicantsOrRespondents(
            caseData.getRespondents(),
            caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue()
        );
        log.info("*** Selected respondents *** {}", selectedRespondents);
        if (selectedApplicants != null
            && selectedApplicants.size() > 0) {
            emailNotificationDetails
                .addAll(sendNotificationsToCitizenApplicants(
                    authorization, selectedApplicants,
                    caseData,
                    bulkPrintDetails
                ));
        }
        log.info(" ** emailnotification 3 {}", emailNotificationDetails);

        if (selectedRespondents != null
            && selectedRespondents.size() > 0) {
            emailNotificationDetails
                .addAll(sendNotificationsToCitizenRespondants(authorization, selectedRespondents, caseData));
        }
    }

    public Map<String, Object> handleAboutToSubmit(CallbackRequest callbackRequest) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();
        if (caseData.getServiceOfApplication() != null && SoaCitizenServingRespondentsEnum.unrepresentedApplicant
            .equals(caseData.getServiceOfApplication().getSoaCitizenServingRespondentsOptionsCA())) {
            caseData.getApplicants().get(0).getValue().getResponse().getCitizenFlags().setIsApplicationToBeServed(YesOrNo.Yes);
            caseDataMap.put(APPLICANTS, caseData.getApplicants());
        }

        caseDataMap.put(CASE_INVITES, generateCaseInvitesForParties(caseData));
        cleanUpSoaSelections(caseDataMap, true);
        return caseDataMap;
    }

    public ResponseEntity<SubmittedCallbackResponse> handleSoaSubmitted(String authorisation, CallbackRequest callbackRequest) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();

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
        caseDataMap = generatePacksForConfidentialCheck(callbackRequest.getCaseDetails(), authorisation);

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

    private List<Element<EmailNotificationDetails>> sendNotificationsToCitizenApplicants(String authorization,
                                                                 List<DynamicMultiselectListElement> selectedApplicants,
                                                        CaseData caseData, List<Element<BulkPrintDetails>> bulkPrintDetails) {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        List<Element<CaseInvite>> caseInvites = caseData.getCaseInvites() != null ? caseData.getCaseInvites()
            : new ArrayList<>();
        selectedApplicants.forEach(applicant -> {
            Optional<Element<PartyDetails>> selectedParty = getParty(applicant.getCode(), caseData.getApplicants());
            if (selectedParty.isPresent()) {
                Element<PartyDetails> selectedApplicant = selectedParty.get();
                if (isAccessEnabled(selectedApplicant)) {
                    log.info("Access already enabled");
                    if (ContactPreferences.digital.equals(selectedApplicant.getValue().getContactPreferences())) {
                        sendEmailToCitizen(authorization, caseData, selectedApplicant, emailNotificationDetails, null);
                    } else {
                        CaseInvite caseInvite = getCaseInvite(selectedApplicant.getId(), caseData.getCaseInvites());
                        List<Document> docs = new ArrayList<>();
                        docs.add(getCoverSheet(authorization, caseData,
                                      selectedApplicant.getValue().getAddress(),
                                      selectedApplicant.getValue().getLabelForDynamicList()));
                        docs.add(generateAp6Letter(authorization,caseData, selectedApplicant, caseInvite));
                        docs.addAll(getNotificationPack(caseData, PrlAppsConstants.P));
                        bulkPrintDetails.addAll(sendPostToCitizen(authorization, caseData, selectedApplicant, docs));
                    }
                } else {
                    log.info("Access to be granted");
                    CaseInvite caseInvite = getCaseInvite(selectedApplicant.getId(),caseData.getCaseInvites());
                    if (caseInvite == null) {
                        caseInvite = c100CaseInviteService.generateCaseInvite(selectedApplicant, Yes);
                        caseInvites.add(element(caseInvite));
                    }
                    if (ContactPreferences.digital.equals(selectedApplicant.getValue().getContactPreferences())) {
                        Document ap6Letter = generateAp6Letter(authorization, caseData, selectedApplicant, caseInvite);

                        sendEmailToCitizen(authorization, caseData, selectedApplicant,
                                                                           emailNotificationDetails, ap6Letter);
                    } else {
                        List<Document> docs = new ArrayList<>();
                        docs.add(getCoverSheet(authorization, caseData,
                                                selectedApplicant.getValue().getAddress(),
                                                selectedApplicant.getValue().getLabelForDynamicList()));
                        docs.add(generateAp6Letter(authorization,caseData, selectedApplicant, caseInvite));
                        log.info("*** docs 1 : {}", docs);
                        docs.addAll(getNotificationPack(caseData, PrlAppsConstants.P));
                        log.info("*** docs 2 : {}", docs);
                        bulkPrintDetails.addAll(sendPostToCitizen(authorization, caseData, selectedApplicant, docs));
                    }
                }
            }
            log.info("*** bulk details {}", bulkPrintDetails);
            caseData.setCaseInvites(caseInvites);
            log.info("** CAse invites  are {}", caseInvites);
        });
        return emailNotificationDetails;
    }

    private Boolean isAccessEnabled(Element<PartyDetails> party) {
        return party.getValue() != null && party.getValue().getUser() != null
            && party.getValue().getUser().getIdamId() != null;
    }

    private void sendEmailToCitizen(String authorization,
                                    CaseData caseData, Element<PartyDetails> applicant,
                                    List<Element<EmailNotificationDetails>> notificationList, Document ap6Letter) {
        List<Document> packPDocs = getNotificationPack(caseData, PrlAppsConstants.P);
        if (ap6Letter != null) {
            packPDocs.add(ap6Letter);
        }
        log.info("** Docs being sent *** {}", packPDocs);
        try {
            notificationList.add(element(serviceOfApplicationEmailService
                                             .sendEmailNotificationToApplicant(
                                                 authorization, caseData, applicant.getValue(),
                                                 packPDocs,
                                                 SERVED_PARTY_APPLICANT
                                             )));
        } catch (Exception e) {
            log.error("Failed to send notification to applicant {}", e.getMessage());
        }
    }

    private List<Element<BulkPrintDetails>> sendPostToCitizen(String authorization, CaseData caseData,
                                                                      Element<PartyDetails> party, List<Document> docs) {
        List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
        log.info("*** docs {}", docs);
        bulkPrintDetails.add(element(serviceOfApplicationPostService.sendPostNotificationToParty(
            caseData,
            authorization,
            party.getValue(),
            docs,
            SERVED_PARTY_RESPONDENT
        )));
        return bulkPrintDetails;
    }

    private List<Element<EmailNotificationDetails>> sendNotificationsToCitizenRespondants(String authorization,
                                                        List<DynamicMultiselectListElement> selectedRespondents,
                                                         CaseData caseData) {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        selectedRespondents.forEach(respondent -> {
            Element<PartyDetails> selectedRespondent = null;
            Optional<Element<PartyDetails>> selectedParty = getParty(respondent.getCode(), caseData.getRespondents());
            if (selectedParty.isPresent()) {
                selectedRespondent = selectedParty.get();
            }
            generateAp6Letter(authorization, caseData, selectedRespondent, getCaseInvite(selectedRespondent.getId(),
                                                                                         caseData.getCaseInvites()));

        });
        return emailNotificationDetails;
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

    private List<Element<EmailNotificationDetails>> sendEmailToFl404Parties(CaseData caseData, String authorization,
                                                                            List<Document> packA, List<Document> packB) {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        emailNotificationDetails.addAll(sendNotificationToFl401Solicitor(caseData, authorization, packA, packB));
        return emailNotificationDetails;
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

    public List<Element<EmailNotificationDetails>> sendNotificationToFirstApplicantSolicitor(CaseData caseData,
                                                                                             String authorization,
                                                                                             PartyDetails party,
                                                                                             List<Document> hiPack,
                                                                                             String servedParty) throws Exception {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();

        emailNotificationDetails.add(element(serviceOfApplicationEmailService
                                                 .sendEmailNotificationToSolicitor(
                                                     authorization, caseData, party,
                                                     EmailTemplateNames.APPLICANT_SOLICITOR_CA,
                                                     hiPack,
                                                     servedParty
                                                 )));

        return emailNotificationDetails;
    }

    public List<Element<EmailNotificationDetails>> sendNotificationToApplicantSolicitor(CaseData caseData, String authorization,
                                                                                        List<DynamicMultiselectListElement> selectedApplicants,
                                                                                        List<Document> packQ, String servedParty) {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        List<Element<PartyDetails>> applicantsInCase = caseData.getApplicants();
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
                                                                 EmailTemplateNames.APPLICANT_SOLICITOR_CA,
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
                    List<Document> docs = new ArrayList<>();
                    CaseInvite caseInvite = getCaseInvite(party.get().getId(), caseData.getCaseInvites());
                    try {
                        docs.add(getCoverSheet(authorization, caseData,
                                               party.get().getValue().getAddress(),
                                               party.get().getValue().getLabelForDynamicList()
                        ));
                        docs.add(generateAccessCodeLetter(authorization, caseData, party.get(), caseInvite, PRL_LET_ENG_RE5));
                        docs.addAll(packR);
                        bulkPrintDetails.add(element(serviceOfApplicationPostService.sendPostNotificationToParty(
                            caseData,
                            authorization,
                            party.get().getValue(),
                            docs,
                            SERVED_PARTY_RESPONDENT
                        )));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
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
            e.printStackTrace();
        }
        return null;
    }

    private List<Document> getNotificationPack(CaseData caseData, String requiredPack) {
        List<Document> docs = new ArrayList<>();
        switch (requiredPack) {
            case PrlAppsConstants.P:
                docs.addAll(generatePackP(caseData));
                break;
            case PrlAppsConstants.Q:
                docs.addAll(generatePackQ(caseData));
                break;
            case PrlAppsConstants.S:
                docs.addAll(generatePackS(caseData));
                break;
            case PrlAppsConstants.R:
                docs.addAll(generatePackR(caseData));
                break;
            case PrlAppsConstants.A:
                docs.addAll(generatePackA(caseData));
                break;
            case PrlAppsConstants.B:
                docs.addAll(generatePackB(caseData));
                break;
            case PrlAppsConstants.G:
                docs.addAll(generatePackG(caseData));
                break;
            case PrlAppsConstants.O:
                docs.addAll(generatePackO(caseData));
                break;
            case PrlAppsConstants.H:
                docs.addAll(generatePackH(caseData));
                break;
            case PrlAppsConstants.I:
                docs.addAll(generatePackI(caseData));
                break;
            case PrlAppsConstants.N:
                docs.addAll(generatePackN(caseData));
                break;
            case PrlAppsConstants.HI:
                docs.addAll(generatePackHI(caseData));
                break;
            case PrlAppsConstants.Z: //not present in miro, added this by comparing to DA other org pack,confirm with PO's
                docs.addAll(generatePackZ(caseData));
                break;
            case PrlAppsConstants.L:
                docs.addAll(generatePackL(caseData));
                break;
            default:
                break;
        }
        return docs;

    }

    private List<Document> generatePackL(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        return docs;
    }

    private List<Document> generatePackZ(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        return docs;
    }

    private List<Document> generatePackHI(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        return docs;
    }

    private List<Document> generatePackN(CaseData caseData) {
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

    private List<Document> generatePackH(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        return docs;
    }

    private List<Document> generatePackI(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        return docs;
    }

    private List<Document> generatePackP(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        return docs;
    }

    private List<Document> generatePackQ(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        return docs;
    }

    private List<Document> generatePackS(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        return docs;
    }

    private List<Document> generatePackR(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        return docs;
    }

    private List<Document> generatePackA(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        return docs;
    }

    private List<Document> generatePackB(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getNonC6aOrders(getSoaSelectedOrders(caseData)));
        return docs;
    }

    private List<Document> generatePackG(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        return docs;
    }

    private List<Document> generatePackO(CaseData caseData) {
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
            orderCodes.stream().forEach(orderCode -> {
                caseData.getOrderCollection().stream()
                    .filter(order -> String.valueOf(order.getId()).equalsIgnoreCase(orderCode))
                    .findFirst()
                    .ifPresent(o -> selectedOrders.add(o.getValue().getOrderDocument()));
            });
            return selectedOrders;
        }
        return Collections.EMPTY_LIST;

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


    public Map<String, Object> getSoaCaseFieldsMap(CaseDetails caseDetails) {
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
        return caseDataUpdated;
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
            e.printStackTrace();
            log.error("*** Access code letter failed for {} :: because of {}", template, e.getStackTrace());
        }
        return null;
    }

    public Document generateAp6Letter(String authorisation, CaseData caseData,Element<PartyDetails> party,
                                      CaseInvite caseInvite) {
        Map<String, Object> dataMap = populateAccessCodeMap(caseData, party, caseInvite);
        GeneratedDocumentInfo ltrAp6Document;
        try {
            ltrAp6Document = dgsService.generateDocument(
                authorisation,
                String.valueOf(dataMap.get("id")),
                Templates.AP6_LETTER,
                dataMap
            );
            return Document.builder().documentUrl(ltrAp6Document.getUrl())
                .documentFileName(ltrAp6Document.getDocName()).documentBinaryUrl(ltrAp6Document.getBinaryUrl())
                .documentCreatedOn(new Date())
                .build();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("*** failed ltr ap6 {}", (Object) e.getStackTrace());
        }
        return null;
    }

    public Map<String, Object> populateAccessCodeMap(CaseData caseData, Element<PartyDetails> party, CaseInvite caseInvite) {
        log.info("*** case invite {}", caseInvite);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", caseData.getId());
        dataMap.put("url", citizenUrl);
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

    public Map<String, Object> generatePacksForConfidentialCheck(CaseDetails caseDetails, String authorization) {

        log.info("Inside generatePacksForConfidentialCheck Method");

        Map<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE));
        String dateCreated = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS).format(zonedDateTime);
        List<Document> c100StaticDocs = serviceOfApplicationPostService.getStaticDocs(authorization, caseData);
        log.info(
            "caseData.getServiceOfApplication().getSoaServeToRespondentOptions() {}",
            caseData.getServiceOfApplication().getSoaServeToRespondentOptions()
        );
        log.info(
            "caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue().size() {}",
            caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue().size()
        );
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
            }

        }

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
        packNDocs.addAll(getNotificationPack(caseData, PrlAppsConstants.N));

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

        List<Element<Document>> packRDocs = wrapElements(getNotificationPack(caseData, PrlAppsConstants.R));
        packRDocs.addAll(wrapElements(c100StaticDocs));

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

        List<Element<Document>> packQDocs = wrapElements(getNotificationPack(caseData, PrlAppsConstants.Q));
        packQDocs.addAll(wrapElements(c100StaticDocs.stream()
                             .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(
                                 C1A_BLANK_DOCUMENT_FILENAME))
                             .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(
                                 C7_BLANK_DOCUMENT_FILENAME))
                             .collect(Collectors.toList())));

        final SoaPack unServedApplicantPack = SoaPack.builder().packDocument(packQDocs).partyIds(
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
            sendNotificationForUnservedApplicantPack(caseData, authorization, emailNotificationDetails, unServedApplicantPack);
        }
        final SoaPack unServedRespondentPack = caseData.getServiceOfApplication().getUnServedRespondentPack();
        if (unServedRespondentPack != null) {

            final List<Element<String>> partyIds = unServedRespondentPack.getPartyIds();
            log.info("Sending notification for Respondents ==> {}", partyIds);

            final List<DynamicMultiselectListElement> respondentList = createPartyDynamicMultiSelectListElement(
                partyIds);

            final List<Document> packR = unwrapElements(
                unServedRespondentPack.getPackDocument());

            sendNotificationToRespondentNonPersonal(caseData, authorization,emailNotificationDetails, bulkPrintDetails, respondentList, packR, packR);
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

        //serving cafcass cymru
        if (YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaCafcassCymruServedOptions())
            && null != caseData.getServiceOfApplication().getSoaCafcassCymruEmail()) {
            log.info("Sending notifiction for Cafcass Cymru");
            emailNotificationDetails.addAll(sendEmailToCafcassInCase(
                caseData,
                caseData.getServiceOfApplication().getSoaCafcassCymruEmail(),
                PrlAppsConstants.SERVED_PARTY_CAFCASS_CYMRU));
        }

        String whoIsResponsibleForServing = COURT;
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE));
        String formatter = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS).format(zonedDateTime);
        return ServedApplicationDetails.builder().emailNotificationDetails(emailNotificationDetails)
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .servedAt(formatter)
            .modeOfService(getModeOfService(emailNotificationDetails, bulkPrintDetails))
            .whoIsResponsible(whoIsResponsibleForServing)
            .bulkPrintDetails(bulkPrintDetails).build();
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
                                                          SoaPack unServedApplicantPack) {
        final List<Element<String>> partyIds = unServedApplicantPack.getPartyIds();
        final List<DynamicMultiselectListElement> applicantList = createPartyDynamicMultiSelectListElement(
            partyIds);

        log.info("Sending notification for Applicants ====> {}", partyIds);

        emailNotificationDetails.addAll(sendNotificationToApplicantSolicitor(caseData, authorization, applicantList,
                                                                             unwrapElements(unServedApplicantPack.getPackDocument()),
                                                                             SERVED_PARTY_APPLICANT_SOLICITOR
        ));
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

        final String formatDateTime = formatDateTime(DD_MMM_YYYY_HH_MM_SS, LocalDateTime.now());
        final ConfidentialCheckFailed confidentialCheckFailed = ConfidentialCheckFailed.builder().confidentialityCheckRejectReason(
                caseData.getServiceOfApplication().getRejectionReason())
            .dateRejected(formatDateTime)
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
}
