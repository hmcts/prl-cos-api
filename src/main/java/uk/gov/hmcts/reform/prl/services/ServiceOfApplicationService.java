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
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaSolicitorServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WelshCourtEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.pin.C100CaseInviteService;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.services.pin.FL401CaseInviteService;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.ConfidentialDetailsGenerator;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C1A_BLANK_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C7_BLANK_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C9_DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_CREATED_BY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PRIVACY_DOCUMENT_FILENAME;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;


@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({"java:S3776","java:S6204","java:S112","java:S4144"})
public class ServiceOfApplicationService {
    private final LaunchDarklyClient launchDarklyClient;

    public static final String FAMILY_MAN_ID = "Family Man ID: ";
    public static final String EMAIL = "email";

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
    private final C100CaseInviteService c100CaseInviteService;

    @Autowired
    private final FL401CaseInviteService fl401CaseInviteService;

    @Autowired
    private final DynamicMultiSelectListService dynamicMultiSelectListService;

    @Autowired
    private final WelshCourtEmail welshCourtEmail;

    @Autowired
    ConfidentialDetailsGenerator confidentialDetailsGenerator;

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
                                                                       List<Document> packN, String servedParty) {
        List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
        List<Element<PartyDetails>> otherPeopleInCase = TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
                                                            ? caseData.getOtherPartyInTheCaseRevised() : caseData.getOthersToNotify();
        List<DynamicMultiselectListElement> othersToNotify = caseData.getServiceOfApplication().getSoaOtherParties().getValue();
        othersToNotify.forEach(other -> {
            Optional<Element<PartyDetails>> party = getParty(other.getCode(), otherPeopleInCase);
            try {
                log.info(
                    "Sending the post notification to others in case for C100 Application for caseId {}",
                    caseData.getId()
                );

                List<Document> docs = new ArrayList<>();
                if (null != party.get().getValue().getAddress()
                    && null != party.get().getValue().getAddress().getAddressLine1()) {
                    docs.add(getCoverLetter(authorization, caseData, party.get().getValue().getAddress(),
                                                party.get().getValue().getLabelForDynamicList()

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
        String whoIsResponsibleForServing = "Court";
        if (!CaseCreatedBy.CITIZEN.equals(caseData.getCaseCreatedBy())) {
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
                }

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
                    List<Document> packQDocs = getNotificationPack(caseData, PrlAppsConstants.Q);
                    packQDocs.addAll(c100StaticDocs.stream()
                                         .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(
                                             C1A_BLANK_DOCUMENT_FILENAME))
                                         .filter(d -> !d.getDocumentFileName().equalsIgnoreCase(
                                             C7_BLANK_DOCUMENT_FILENAME))
                                         .collect(Collectors.toList()));
                    log.info("selected Applicants " + selectedApplicants.size());
                    if (selectedApplicants != null
                        && !selectedApplicants.isEmpty()) {
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
                    if (selectedRespondents != null && !selectedRespondents.isEmpty()) {
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
                        if (null != resultMap && resultMap.containsKey(EMAIL)) {
                            tempEmail = (List<Element<EmailNotificationDetails>>) resultMap.get(EMAIL);
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
                    && !caseData.getServiceOfApplication().getSoaOtherParties().getValue().isEmpty()) {
                    log.info("serving other people in case");
                    List<Document> packNDocs = c100StaticDocs.stream().filter(d -> d.getDocumentFileName()
                        .equalsIgnoreCase(PRIVACY_DOCUMENT_FILENAME)).collect(
                        Collectors.toList());
                    packNDocs.addAll(getNotificationPack(caseData, PrlAppsConstants.N));
                    bulkPrintDetails.addAll(sendPostToOtherPeopleInCase(caseData, authorization, packNDocs, PrlAppsConstants.SERVED_PARTY_OTHER));
                }

                //serving cafcass cymru
                if (YesOrNo.Yes.equals(caseData.getServiceOfApplication().getSoaCafcassCymruServedOptions())
                    && null != caseData.getServiceOfApplication().getSoaCafcassCymruEmail()) {
                    emailNotificationDetails.addAll(sendEmailToCafcassInCase(
                        caseData,
                        caseData.getServiceOfApplication().getSoaCafcassCymruEmail(),
                        PrlAppsConstants.SERVED_PARTY_CAFCASS_CYMRU));
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

        } else {
            //CITIZEN SCENARIO
            if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                if (CaseCreatedBy.CITIZEN.equals(caseData.getCaseCreatedBy())) {
                    log.info("Sending service of application notifications to C100 citizens");
                    serviceOfApplicationEmailService.sendEmailToC100Applicants(caseData);
                }

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
        }
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss").format(zonedDateTime);
        return ServedApplicationDetails.builder().emailNotificationDetails(emailNotificationDetails)
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .servedAt(formatter)
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
                    try {
                        docs.add(getCoverLetter(authorization, caseData,
                                                party.get().getValue().getAddress(),
                                                party.get().getValue().getLabelForDynamicList()
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
            }
        });
        resultMap.put(EMAIL, emailNotificationDetails);
        resultMap.put("post", bulkPrintDetails);
        return resultMap;
    }

    private Optional<Element<PartyDetails>> getParty(String code, List<Element<PartyDetails>> parties) {
        Optional<Element<PartyDetails>> party;
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
            orderCodes.stream().forEach(orderCode ->
                caseData.getOrderCollection().stream()
                    .filter(order -> String.valueOf(order.getId()).equalsIgnoreCase(orderCode))
                    .findFirst()
                    .ifPresent(o -> selectedOrders.add(o.getValue().getOrderDocument())));
            return selectedOrders;
        }
        return Collections.emptyList();

    }

    public Map<String, Object> cleanUpSoaSelections(Map<String, Object> caseDataUpdated) {
        String[] soaFields = {
            "pd36qLetter", "specialArrangementsLetter",
            "additionalDocuments", "sentDocumentPlaceHolder", "soaApplicantsList",
            "soaRespondentsList", "soaOtherPeopleList", "soaCafcassEmailOptionChecked",
            "soaOtherEmailOptionChecked", "soaOtherEmailOptionChecked", "soaCafcassEmailAddressList",
            "soaOtherEmailAddressList", "coverPageAddress", "coverPagePartyName",
            "serviceOfApplicationScreen1", "soaPostalInformationDA", "soaEmailInformationDA", "soaDeliveryByOptionsDA",
            "soaServeOtherPartiesDA", "soaPostalInformationCA", "soaEmailInformationCA", "soaDeliveryByOptionsCA", "soaServeOtherPartiesCA",
            "soaCafcassCymruEmail", "soaCafcassCymruServedOptions", "soaCafcassEmailId", "soaCafcassServedOptions",
            "soaOtherParties", "soaRecipientsOptions", "soaServingRespondentsOptionsDA", "soaServingRespondentsOptionsCA",
            "soaServeToRespondentOptions", "soaOtherPeoplePresentInCaseFlag", "soaIsOrderListEmpty", "noticeOfSafetySupportLetter",
            "additionalDocumentsList","proceedToServing"};

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
        caseDataUpdated.put(CASE_CREATED_BY, caseData.getCaseCreatedBy());
        return caseDataUpdated;
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

        if (!respondentsWrapped.isEmpty() && !respondentsWrapped.get().isEmpty()) {
            List<PartyDetails> respondents = respondentsWrapped.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

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

    public List<Element<CaseInvite>> sendAndReturnCaseInvites(CaseData caseData) {
        List<Element<CaseInvite>> caseInvites = caseData.getCaseInvites() != null ? caseData.getCaseInvites() : new ArrayList<>();
        if (CaseUtils.getCaseTypeOfApplication(caseData).equalsIgnoreCase(PrlAppsConstants.C100_CASE_TYPE)) {
            if (YesOrNo.No.equals(caseData.getServiceOfApplication().getSoaServeToRespondentOptions())
                && caseData.getServiceOfApplication().getSoaRecipientsOptions() != null) {
                log.info("Non personal service access code generation for case {}", caseData.getId());
                caseInvites.addAll(getCaseInvitesForSelectedApplicantAndRespondent(caseData));
            } else {
                log.info("Personal service is selected , so no need to generate access code for {}", caseData.getId());
            }
        } else {
            log.info("In Generating and sending PIN to Citizen FL401");
            caseInvites.addAll(fl401CaseInviteService.generateAndSendCaseInviteForDaApplicant(
                caseData,
                caseData.getApplicantsFL401()
            ));
            caseInvites.addAll(fl401CaseInviteService.generateAndSendCaseInviteForDaRespondent(
                caseData,
                caseData.getRespondentsFL401()
            ));
        }
        return caseInvites;
    }

    public List<Element<CaseInvite>> getCaseInvitesForSelectedApplicantAndRespondent(CaseData caseData) {
        List<Element<CaseInvite>> caseInvites = new ArrayList<>();
        List<DynamicMultiselectListElement> selectedApplicants = getSelectedApplicantsOrRespondents(
            caseData.getApplicants(),
            caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue()
        );

        List<Element<PartyDetails>> applicantsInCase = caseData.getApplicants();
        selectedApplicants.forEach(applicant -> {
            Optional<Element<PartyDetails>> party = getParty(applicant.getCode(), applicantsInCase);
            if (party.isPresent()) {
                try {
                    log.info(
                        "Sending caseinvites/access codes to the selected applicants for case id {}",
                        caseData.getId()
                    );

                    caseInvites.addAll(c100CaseInviteService.generateAndSendCaseInviteEmailForCaApplicant(
                        caseData,
                        party.get()
                    ));

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        List<DynamicMultiselectListElement> selectedRespondents = getSelectedApplicantsOrRespondents(
            caseData.getRespondents(),
            caseData.getServiceOfApplication().getSoaRecipientsOptions().getValue()
        );
        List<Element<PartyDetails>> respondentsInCase = caseData.getRespondents();
        selectedRespondents.forEach(respondent -> {
            Optional<Element<PartyDetails>> party = getParty(respondent.getCode(), respondentsInCase);
            if (party.isPresent()) {
                try {
                    log.info(
                        "Sending caseinvites/access codes to the selected respondents for case id {}",
                        caseData.getId()
                    );
                    caseInvites.addAll(c100CaseInviteService.generateAndSendCaseInviteForCaRespondent(
                        caseData,
                        party.get()
                    ));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return caseInvites;
    }
}
