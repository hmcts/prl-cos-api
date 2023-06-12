package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.DeliveryByEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherOrganisationOptions;
import uk.gov.hmcts.reform.prl.enums.manageorders.ServeOtherPartiesOptions;
import uk.gov.hmcts.reform.prl.enums.manageorders.ServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.EmailInformation;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.PostalInformation;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.pin.C100CaseInviteService;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.services.pin.FL401CaseInviteService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PRIVACY_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_OTHER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_RESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.hasLegalRepresentation;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;


@Service
@Slf4j
@RequiredArgsConstructor
public class ServiceOfApplicationService {
    private final LaunchDarklyClient launchDarklyClient;

    public static final String FAMILY_MAN_ID = "Family Man ID: ";

    @Autowired
    private final ServiceOfApplicationEmailService serviceOfApplicationEmailService;

    @Autowired
    private final ServiceOfApplicationPostService serviceOfApplicationPostService;

    @Autowired
    private final CaseInviteManager caseInviteManager;

    @Autowired
    private final ObjectMapper objectMapper;

    @Autowired
    private final UserService userService;

    @Autowired
    private C100CaseInviteService c100CaseInviteService;
    @Autowired
    private FL401CaseInviteService fl401CaseInviteService;

    @Autowired
    private DynamicMultiSelectListService dynamicMultiSelectListService;

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
            "Certain documents will be automatically included in the pack this is sent out on parties(the people in the case)");
        collapsible.add(
            "This includes");
        collapsible.add(
            "<ul><li>C100</li><li>C1A</li><li>C7</li><li>C1A (blank)</li><li>C8 (Cafcass and Local Authority only)</li>");
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
                serviceOfApplicationEmailService.sendEmailC100(caseDetails);
            } else {
                serviceOfApplicationEmailService.sendEmailFL401(caseDetails);
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
                                                                       List<Document> packN, String servedParty) {
        List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
        List<Element<PartyDetails>> otherPeopleInCase = caseData.getOthersToNotify();
        List<DynamicMultiselectListElement> othersToNotify = caseData.getServiceOfApplication().getSoaOtherParties().getValue();
        othersToNotify.forEach(other -> {
            Optional<Element<PartyDetails>> party = getParty(other.getCode(), otherPeopleInCase);
            try {
                log.info(
                    "Sending the post notification to others in case for C100 Application for caseId {}",
                    caseData.getId()
                );

                List<Document> docs = new ArrayList<>();
                log.info("address" + party.get().getValue().getAddress());
                if (null != party.get().getValue().getAddress()
                    && null != party.get().getValue().getAddress().getAddressLine1()) {
                    docs.add(getCoverLetter(authorization, caseData, party.get().getValue().getAddress(),
                                            CaseUtils.getName(
                                                party.get().getValue().getFirstName(),
                                                party.get().getValue().getLastName()
                                            )
                    ));
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

    public List<Element<EmailNotificationDetails>> sendEmailToOtherOtherOrg(String authorization, CaseData caseData,
                                                                            boolean isCaCase,
                                                                            List<Document> packDocs,
                                                                            String servedParty) throws Exception {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        if (isCaCase && caseData.getServiceOfApplication() != null
            && caseData.getServiceOfApplication().getSoaEmailInformationCA() != null
            && caseData.getServiceOfApplication().getSoaEmailInformationCA().size() > 0) {
            for (Element<EmailInformation> element : caseData.getServiceOfApplication().getSoaEmailInformationCA()) {
                log.info("**SERVING OTHER EMAILS**");
                log.info("**other email** {}", element.getValue().getEmailAddress());
                if (null != element.getValue().getEmailAddress()) {
                    emailNotificationDetails.add(element(serviceOfApplicationEmailService.sendEmailNotificationToOtherEmails(
                        authorization,
                        caseData,
                        element.getValue().getEmailAddress(),
                        packDocs,
                        servedParty
                    )));
                }
            }
        }
        if (!isCaCase && caseData.getServiceOfApplication() != null
            && caseData.getServiceOfApplication().getSoaEmailInformationDA() != null
            && caseData.getServiceOfApplication().getSoaEmailInformationDA().size() > 0) {
            for (Element<EmailInformation> element : caseData.getServiceOfApplication().getSoaEmailInformationDA()) {
                log.info("**SERVING OTHER EMAILS**");
                log.info("**other email** {}", element.getValue().getEmailAddress());
                if (null != element.getValue().getEmailAddress()) {
                    emailNotificationDetails.add(element(serviceOfApplicationEmailService.sendEmailNotificationToOtherEmails(
                        authorization,
                        caseData,
                        element.getValue().getEmailAddress(),
                        packDocs,
                        SERVED_PARTY_OTHER
                    )));
                }
            }
        }
        return emailNotificationDetails;
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
        log.info("service of application {}", caseData.getServiceOfApplication());
        String whoIsResponsibleForServing = "Court";
        if (!CaseCreatedBy.CITIZEN.equals(caseData.getCaseCreatedBy())) {
            log.info("Not created by citizen");
            if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                List<Document> c100StaticDocs = serviceOfApplicationPostService.getStaticDocs(authorization, caseData);
                if (caseData.getServiceOfApplication().getSoaServeToRespondentOptions() != null
                    && YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())
                    && ServingRespondentsEnum.applicantLegalRepresentative
                    .equals(caseData.getServiceOfApplication().getSoaServingRespondentsOptionsCA())) {
                    whoIsResponsibleForServing = CaseUtils.getName(caseData.getApplicants().get(0).getValue().getRepresentativeFirstName(),
                                                            caseData.getApplicants().get(0).getValue().getRepresentativeLastName());
                    //This is added with assumption that, For applicant legl representative selection
                    // if multiple applicants are present only the first applicant solicitor will receive notification
                    log.info("First applicant solicitor going to receive pack");
                    List<Document> packHiDocs = getNotificationPack(caseData, PrlAppsConstants.HI);
                    packHiDocs.addAll(c100StaticDocs);
                    emailNotificationDetails.addAll(sendNotificationToFirstApplicantSolicitor(
                        caseData,
                        authorization,
                        caseData.getApplicants().get(0).getValue(),
                        packHiDocs,
                        SERVED_PARTY_APPLICANT_SOLICITOR
                    ));
                }
                log.info("C100 case journey");
                if (YesOrNo.No.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())
                    && (caseData.getServiceOfApplication().getSoaRecipientsOptions() != null)
                    && (caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue().size() > 0)) {
                    log.info("serving applicants or respondents");
                    List<DynamicMultiselectListElement> selectedApplicants = getSelectedApplicantsOrRespondents(
                        caseData.getApplicants(),
                        caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue()
                    );
                    List<Document> packQDocs = getNotificationPack(caseData, PrlAppsConstants.Q);
                    packQDocs.addAll(c100StaticDocs);
                    log.info("selected Applicants " + selectedApplicants.size());
                    if (selectedApplicants != null
                        && selectedApplicants.size() > 0) {
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
                    if (selectedRespondents != null && selectedRespondents.size() > 0) {
                        List<Document> packRDocs = getNotificationPack(caseData, PrlAppsConstants.R);
                        packRDocs.addAll(c100StaticDocs);
                        List<Document> packSDocs = getNotificationPack(caseData, PrlAppsConstants.S);
                        packSDocs.addAll(c100StaticDocs);
                        List<Element<EmailNotificationDetails>> tempEmail = new ArrayList<>();
                        List<Element<BulkPrintDetails>> tempPost = new ArrayList<>();
                        Map<String, Object> resultMap = sendNotificationToRespondentOrSolicitor(
                            caseData,
                            authorization,
                            selectedRespondents,
                            packRDocs,
                            packSDocs,
                            PrlAppsConstants.SERVED_PARTY_RESPONDENT_SOLICITOR
                        );
                        if (null != resultMap && resultMap.containsKey("email")) {
                            tempEmail = (List<Element<EmailNotificationDetails>>) resultMap.get("email");
                        }
                        if (null != resultMap && resultMap.containsKey("post")) {
                            tempPost = (List<Element<BulkPrintDetails>>) resultMap.get("post");
                        }
                        emailNotificationDetails.addAll(tempEmail);
                        bulkPrintDetails.addAll(tempPost);
                    }
                }
                //serving other people in case
                if (null != caseData.getServiceOfApplication().getSoaOtherParties()
                    && caseData.getServiceOfApplication().getSoaOtherParties().getValue().size() > 0) {
                    log.info("serving other people in case");
                    List<Document> packNDocs = c100StaticDocs.stream().filter(d -> d.getDocumentFileName()
                        .equalsIgnoreCase(PRIVACY_DOCUMENT_FILENAME)).collect(
                        Collectors.toList());
                    packNDocs.addAll(getNotificationPack(caseData, PrlAppsConstants.N));
                    bulkPrintDetails.addAll(sendPostToOtherPeopleInCase(caseData, authorization, packNDocs, PrlAppsConstants.SERVED_PARTY_OTHER));
                }
                //serving cafcass
                if (YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaCafcassServedOptions())
                    && null != caseData.getServiceOfApplication().getSoaCafcassEmailId()) {
                    log.info("serving cafcass emails : " + caseData.getServiceOfApplication().getSoaCafcassEmailId());
                    emailNotificationDetails.addAll(sendEmailToCafcassInCase(
                        caseData,
                        caseData.getServiceOfApplication().getSoaCafcassEmailId(),
                        PrlAppsConstants.SERVED_PARTY_CAFCASS
                    ));
                }

                if (YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaCafcassCymruServedOptions())
                    && null != caseData.getServiceOfApplication().getSoaCafcassCymruEmail()) {
                    log.info("serving cafcass cymru emails : " + caseData.getServiceOfApplication().getSoaCafcassCymruEmail());
                    emailNotificationDetails.addAll(sendEmailToCafcassInCase(
                        caseData,
                        caseData.getServiceOfApplication().getSoaCafcassCymruEmail(),
                        PrlAppsConstants.SERVED_PARTY_CAFCASS_CYMRU));
                }

                if (caseData.getServiceOfApplication() != null
                    && caseData.getServiceOfApplication().getSoaServeOtherPartiesCA().contains(OtherOrganisationOptions.anotherOrganisation)
                    && DeliveryByEnum.email.equals(caseData.getServiceOfApplication().getSoaDeliveryByOptionsCA())) {
                    log.info("serving email to other organisation");
                    List<Document> packZDocs = getNotificationPack(caseData, PrlAppsConstants.Z);
                    emailNotificationDetails.addAll(sendEmailToOtherOtherOrg(
                        authorization,
                        caseData,
                        true,
                        packZDocs,
                        PrlAppsConstants.SERVED_PARTY_OTHER_ORGANISATION
                    ));
                }

                //serving post to other organisation
                if (caseData.getServiceOfApplication() != null
                    && caseData.getServiceOfApplication().getSoaServeOtherPartiesCA().contains(OtherOrganisationOptions.anotherOrganisation)
                    && DeliveryByEnum.post.equals(caseData.getServiceOfApplication().getSoaDeliveryByOptionsCA())) {
                    log.info("serving post to other organisation");
                    List<Document> packZDocs = getNotificationPack(caseData, PrlAppsConstants.Z);
                    bulkPrintDetails.addAll(sendPostToOtherOrganisation(
                        caseData,
                        authorization,
                        packZDocs,
                        PrlAppsConstants.SERVED_PARTY_OTHER_ORGANISATION
                    ));
                }
            } else {
                List<Document> staticDocs = serviceOfApplicationPostService.getStaticDocs(authorization, caseData);
                List<Document> packADocs = getNotificationPack(caseData, PrlAppsConstants.A);
                List<Document> packBDocs = getNotificationPack(caseData, PrlAppsConstants.B);
                packADocs.addAll(staticDocs);
                packBDocs.addAll(staticDocs);
                whoIsResponsibleForServing = CaseUtils.getName(caseData.getApplicantsFL401().getRepresentativeFirstName(),
                                                               caseData.getApplicantsFL401().getRepresentativeLastName());
                log.info("Fl401 case journey");
                if (ServingRespondentsEnum.applicantLegalRepresentative.equals(caseData.getServiceOfApplication()
                                                                                   .getSoaServingRespondentsOptionsDA())) {
                    emailNotificationDetails.addAll(sendEmailToFl404Parties(caseData, authorization, packADocs, packBDocs));
                }

                if (caseData.getServiceOfApplication() != null
                    && caseData.getServiceOfApplication().getSoaServeOtherPartiesDA().contains(ServeOtherPartiesOptions.other)
                    && DeliveryByEnum.email.equals(caseData.getServiceOfApplication().getSoaDeliveryByOptionsDA())) {
                    log.info("serving email to other organisation");
                    List<Document> packGDocs = getNotificationPack(caseData, PrlAppsConstants.G);
                    packGDocs.addAll(staticDocs);
                    emailNotificationDetails.addAll(sendEmailToOtherOtherOrg(
                        authorization,
                        caseData,
                        false,
                        packGDocs,
                        SERVED_PARTY_OTHER
                    ));
                }

                //serving post to other organisation
                if (caseData.getServiceOfApplication() != null
                    && caseData.getServiceOfApplication().getSoaServeOtherPartiesDA().contains(ServeOtherPartiesOptions.other)
                    && DeliveryByEnum.post.equals(caseData.getServiceOfApplication().getSoaDeliveryByOptionsDA())) {
                    log.info("serving post to other organisation");
                    List<Document> packGDocs = getNotificationPack(caseData, PrlAppsConstants.G);
                    packGDocs.addAll(staticDocs);
                    bulkPrintDetails.addAll(sendPostToOtherOrganisation(
                        caseData,
                        authorization,
                        packGDocs,
                        PrlAppsConstants.SERVED_PARTY_OTHER
                    ));
                }
            }

        } else {
            //CITIZEN SCENARIO
            if (caseData.getServiceOfApplication().getSoaApplicantsList().getValue() != null) {
                generatePinAndSendNotificationEmailForCitizen(caseData);
            }
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM YYYY HH:mm:ss");
        LocalDateTime datetime = LocalDateTime.now();
        String currentDate = datetime.format(formatter);
        log.info("emailNotificationDetails {}", emailNotificationDetails);
        return ServedApplicationDetails.builder().emailNotificationDetails(emailNotificationDetails)
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .servedAt(currentDate)
            .modeOfService(getModeOfService(emailNotificationDetails, bulkPrintDetails))
            .whoIsResponsible(whoIsResponsibleForServing)
            .bulkPrintDetails(bulkPrintDetails).build();
    }

    private String getModeOfService(List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                    List<Element<BulkPrintDetails>> bulkPrintDetails) {
        String temp = null;
        if (null != emailNotificationDetails && !emailNotificationDetails.isEmpty()) {
            temp = "By email";
        }
        if (null != bulkPrintDetails && !bulkPrintDetails.isEmpty()) {
            if (null != temp) {
                temp = "By email and post";
            } else {
                temp = "By post";
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
        log.info("applicant FL401 Sol email" + applicant.getSolicitorEmail());
        if (applicant.getSolicitorEmail() != null) {
            try {
                log.info(
                    "Sending the email notification to applicant solicitor for FL401 Application for caseId {}",
                    caseData.getId()
                );
                log.info("Sending applicant pack to " + applicant.getSolicitorEmail());
                //Applicant's pack
                emailNotificationDetails.add(element(serviceOfApplicationEmailService.sendEmailNotificationToApplicantSolicitor(
                    authorization,
                    caseData,
                    applicant,
                    EmailTemplateNames.APPLICANT_SOLICITOR_DA,
                    packA,
                    SERVED_PARTY_APPLICANT_SOLICITOR
                )));
                log.info("Sending respondent pack to " + applicant.getSolicitorEmail());
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

    public CaseData generatePinAndSendNotificationEmailForCitizen(CaseData caseData) {
        if (launchDarklyClient.isFeatureEnabled("generate-pin")) {
            if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                log.info("In Generating and sending PIN to Citizen C100");
                List<Element<PartyDetails>> citizensInCase = caseData.getApplicants();
                List<DynamicMultiselectListElement> citizenList = caseData.getServiceOfApplication().getSoaApplicantsList().getValue();
                citizenList.forEach(applicant -> {
                    Optional<Element<PartyDetails>> party = getParty(applicant.getCode(), citizensInCase);
                    if (party.isPresent()) {
                        c100CaseInviteService.generateAndSendCaseInviteEmailForC100Citizen(
                            caseData,
                            party.get().getValue()
                        );
                    }
                });
            } else {
                log.info("In Generating and sending PIN to Citizen FL401");
                PartyDetails applicant = caseData.getApplicantsFL401();
                if (caseData.getServiceOfApplication().getSoaApplicantsList().getValue().contains(applicant)) {
                    fl401CaseInviteService.generateAndSendCaseInviteEmailForFL401Citizen(caseData, applicant);
                }

            }
        }
        return caseData;
    }

    public List<Element<EmailNotificationDetails>> sendNotificationToFirstApplicantSolicitor(CaseData caseData,
                                                                                             String authorization,
                                                                                             PartyDetails party,
                                                                                             List<Document> hiPack,
                                                                                             String servedParty) throws Exception {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();

        log.info("email {}", party.getSolicitorEmail());
        emailNotificationDetails.add(element(serviceOfApplicationEmailService
                                                 .sendEmailNotificationToFirstApplicantSolicitor(
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

                    List<Document> docs = new ArrayList<>();
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
                        emailNotificationDetails.add(element(serviceOfApplicationEmailService.sendEmailNotificationToRespondentSolicitor(
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
                log.info("The respondent is unrepresented");
                if (party.get().getValue().getAddress() != null && StringUtils.isNotEmpty(party.get().getValue().getAddress().getAddressLine1())) {
                    log.info(
                        "Sending the notification in post to respondent for C100 Application for caseId {}",
                        caseData.getId()
                    );
                    List<Document> docs = new ArrayList<>();
                    try {
                        docs.add(getCoverLetter(authorization, caseData,
                                                party.get().getValue().getAddress(),
                                                CaseUtils.getName(
                                                    party.get().getValue().getFirstName(),
                                                    party.get().getValue().getLastName()
                                                )
                        ));
                        bulkPrintDetails.add(element(serviceOfApplicationPostService.sendPostNotificationToParty(
                            caseData,
                            authorization,
                            party.get().getValue(),
                            ListUtils.union(docs, packR),
                            SERVED_PARTY_RESPONDENT
                        )));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    log.info("Unable to send any notification to respondent for C100 Application for caseId {} "
                                 + "as no address available", caseData.getId());
                }
                //LD feature toggle- needs review
                if (!hasLegalRepresentation(party.get().getValue())
                    && Yes.equals(party.get().getValue().getCanYouProvideEmailAddress())
                    && launchDarklyClient.isFeatureEnabled("send-res-email-notification")) {

                    c100CaseInviteService.generateAndSendCaseInviteForC100Respondent(
                        caseData,
                        party.get().getValue()
                    );
                }

            }
        });
        resultMap.put("email", emailNotificationDetails);
        resultMap.put("post", bulkPrintDetails);
        return resultMap;
    }

    private Optional<Element<PartyDetails>> getParty(String code, List<Element<PartyDetails>> parties) {
        Optional<Element<PartyDetails>> party = Optional.empty();
        party = parties.stream()
            .filter(element -> element.getId().toString().equalsIgnoreCase(code)).findFirst();

        return party;
    }

    public Document getCoverLetter(String authorization, CaseData caseData, Address address, String name) throws Exception {
        return DocumentUtils.toCoverLetterDocument(serviceOfApplicationPostService
                                                       .getCoverLetterGeneratedDocInfo(caseData, authorization,
                                                                                       address,
                                                                                       name
                                                       ));
    }

    private List<Document> getNotificationPack(CaseData caseData, String requiredPack) {
        List<Document> docs = new ArrayList<>();
        switch (requiredPack) {
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
            default:
                break;
        }
        //log.info("DOCUMENTS IN THE PACK" + docs);
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
        docs.addAll(getSoaSelectedOrders(caseData));
        return docs;
    }

    private List<Document> generatePackN(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        //As per miro link only privacy notice and selected orders needs to be sent
        //docs.addAll(getCaseDocs(caseData));
        //docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        return docs;
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

    private List<Document> generatePackQ(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        return docs;
    }

    private List<Document> generatePackS(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        return docs;
    }

    private List<Document> generatePackR(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        return docs;
    }

    private List<Document> generatePackA(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        return docs;
    }

    private List<Document> generatePackB(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
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

    private List<Document> getStaticDocs() {
        List<Document> docs = new ArrayList<>();
        //String filePath = "classpath:Privacy_Notice.pdf";
        Document privacyNotice = Document.builder().documentUrl("classpath:Privacy_Notice.pdf")
            .documentBinaryUrl("classpath:Privacy_Notice.pdf")
            .documentHash("classpath:Privacy_Notice.pdf")
            .documentFileName("Privacy_Notice.pdf").build();

        docs.add(privacyNotice);
        return docs;
    }

    private Document getDocument(String classPath, String documentName) {
        return Document.builder().documentUrl(classPath)
            .documentBinaryUrl(classPath)
            .documentHash(classPath)
            .documentFileName(documentName).build();
    }

    private List<Document> getSoaSelectedOrders(CaseData caseData) {
        if (null != caseData.getServiceOfApplicationScreen1()
            && null != caseData.getServiceOfApplicationScreen1().getValue()
            && !caseData.getServiceOfApplicationScreen1().getValue().isEmpty()) {
            log.info("Orders on SoA" + caseData.getServiceOfApplicationScreen1().getValue());

            List<String> orderNames = caseData.getServiceOfApplicationScreen1()
                .getValue().stream().map(DynamicMultiselectListElement::getCode)
                .map(xyz -> xyz.substring(0, xyz.indexOf("-")))
                .collect(Collectors.toList());
            log.info("order Names {}", orderNames);
            return caseData.getOrderCollection().stream()
                .map(Element::getValue)
                .filter(i -> orderNames.contains(i.getOrderTypeId()))
                .map(OrderDetails::getOrderDocument)
                .collect(Collectors.toList());

        }
        return Collections.EMPTY_LIST;

    }

    public Map<String, Object> cleanUpSoaSelections(Map<String, Object> caseDataUpdated) {
        String[] soaFields = {"pd36qLetter", "specialArrangementsLetter",
            "additionalDocuments", "sentDocumentPlaceHolder", "soaApplicantsList",
            "soaRespondentsList", "soaOtherPeopleList", "soaCafcassEmailOptionChecked",
            "soaOtherEmailOptionChecked", "soaOtherEmailOptionChecked", "soaCafcassEmailAddressList",
            "soaOtherEmailAddressList", "coverPageAddress", "coverPagePartyName",
            "serviceOfApplicationScreen1", "soaPostalInformationDA", "soaEmailInformationDA", "soaDeliveryByOptionsDA",
            "soaServeOtherPartiesDA", "soaPostalInformationCA", "soaEmailInformationCA", "soaDeliveryByOptionsCA", "soaServeOtherPartiesCA",
            "soaCafcassCymruEmail", "soaCafcassCymruServedOptions", "soaCafcassEmailId", "soaCafcassServedOptions",
            "soaOtherParties", "soaRecipientsOptions", "soaServingRespondentsOptionsDA", "soaServingRespondentsOptionsCA",
            "soaServeToRespondentOptions", "soaOtherPeoplePresentInCaseFlag", "soaIsOrderListEmpty", "noticeOfSafetySupportLetter",
            "additionalDocumentsList"};

        for (String field : soaFields) {
            if (caseDataUpdated.containsKey(field)) {
                caseDataUpdated.put(field, null);
            }
        }
        return caseDataUpdated;
    }

    public DynamicMultiSelectList getCombinedRecipients(CaseData caseData) {
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
                return No;
            }
        } else {
            return No;
        }
    }


    public List<Element<BulkPrintDetails>> sendPostToOtherOrganisation(CaseData caseData, String authorization,
                                                                       List<Document> documents, String servedParty) throws Exception {
        List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
        if (caseData.getServiceOfApplication() != null
            && caseData.getServiceOfApplication().getSoaPostalInformationCA() != null
            && caseData.getServiceOfApplication().getSoaPostalInformationCA().size() > 0) {
            for (Element<PostalInformation> element : caseData.getServiceOfApplication().getSoaPostalInformationCA()) {
                List<Document> docs = new ArrayList<>();
                log.info("**Postal Address for the other organisation** {}", element.getValue().getPostalAddress());
                if (null != element.getValue().getPostalAddress()
                    && null != element.getValue().getPostalAddress().getAddressLine1()) {
                    docs.add(getCoverLetter(
                        authorization,
                        caseData,
                        element.getValue().getPostalAddress(),
                        element.getValue().getPostalName()
                    ));
                    bulkPrintDetails.add(element(serviceOfApplicationPostService.sendPostNotification(
                        caseData,
                        authorization,
                        element.getValue().getPostalAddress(),
                        element.getValue().getPostalName(),
                        ListUtils.union(docs, documents),
                        servedParty
                    )));
                }
            }
        }
        return bulkPrintDetails;
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
}
