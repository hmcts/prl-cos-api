package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.services.pin.C100CaseInviteService;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.services.pin.FL401CaseInviteService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.A;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.B;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.G;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.O;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.Q;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.R;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.S;
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

    public String getCollapsableOfSentDocuments() {
        final List<String> collapsible = new ArrayList<>();
        collapsible.add("<details class='govuk-details'>");
        collapsible.add("<summary class='govuk-details__summary'>");
        collapsible.add("<span class='govuk-details__summary-text'>");
        collapsible.add("Documents that will be sent out (if applicable to the case):");
        collapsible.add("</span>");
        collapsible.add("</summary>");
        collapsible.add("<div class='govuk-details__text'>");
        collapsible.add("Documents that will be sent out (if applicable to the case):<br/>");
        collapsible.add(
            "<ul><li>C100</li><li>C1A</li><li>C7</li><li>C1A (blank)</li><li>C8 (Cafcass and Local Authority only)</li>");
        collapsible.add("<li>Annex Z</li><li>Privacy notice</li><li>Any orders and"
                            + " hearing notices created at the initial gatekeeping stage</li></ul>");
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
            if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
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
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            serviceOfApplicationPostService.sendDocs(caseData, authorization);
        }
        return caseData;
    }

    public List<Element<BulkPrintDetails>> sendPostToOtherPeopleInCase(CaseDetails caseDetails, String authorization) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
        List<Element<PartyDetails>> otherPeopleInCase = caseData.getOthersToNotify();
        List<DynamicMultiselectListElement> othersToNotify = caseData.getServiceOfApplication().getSoaOtherPeopleList().getValue();
        othersToNotify.forEach(other -> {
            Optional<Element<PartyDetails>> party = getParty(other.getCode(), otherPeopleInCase);
            try {
                log.info("***SERVING OTHER PEOPLE IN CASE AND SENDING POST***");
                log.info(
                    "Sending the post notification to others in case for C100 Application for caseId {}",
                    caseDetails.getId()
                );

                List<Document> docs = new ArrayList<>();
                log.info("party value " + party.get().getValue());
                log.info("party " + party.get());
                log.info("address" + party.get().getValue().getAddress());
                if (null != party.get().getValue().getAddress()
                    && null != party.get().getValue().getAddress().getAddressLine1()) {
                    docs.add(getCoverLetter(authorization, caseData, party.get().getValue()));
                    bulkPrintDetails.add(element(serviceOfApplicationPostService.sendPostNotificationToParty(
                        caseData,
                        authorization,
                        party.get().getValue(),
                        getNotificationPack(caseData, O, docs)
                    )));
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return bulkPrintDetails;
    }

    public List<Element<EmailNotificationDetails>> sendEmailToOtherEmails(String authorization, CaseDetails caseDetails,
                                                                          CaseData caseData) throws Exception {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        if (caseData.getServiceOfApplication() != null
            && caseData.getServiceOfApplication().getSoaOtherEmailAddressList() != null) {
            for (Element<String> element : caseData.getServiceOfApplication().getSoaOtherEmailAddressList()) {
                log.info("**SERVING OTHER EMAILS**");
                List<Document> docs = new ArrayList<>();
                String email = element.getValue();
                log.info("**other email** {}", email);
                emailNotificationDetails.add(element(serviceOfApplicationEmailService.sendEmailNotificationToOtherEmails(
                    authorization,
                    caseDetails,
                    caseData,
                    email,
                    getNotificationPack(caseData, G, docs)
                )));
            }
        }
        return emailNotificationDetails;
    }

    public List<Element<EmailNotificationDetails>> sendEmailToCafcassInCase(String authorization,
                                                                            CaseDetails caseDetails, CaseData caseData)
        throws Exception {
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        if (caseData.getServiceOfApplication() != null
            && caseData.getServiceOfApplication().getSoaCafcassEmailAddressList() != null) {
            for (Element<String> element : caseData.getServiceOfApplication().getSoaCafcassEmailAddressList()) {
                log.info("**SERVING EMAIL TO CAFCASS**");
                List<Document> docs = new ArrayList<>();
                String email = element.getValue();
                log.info("**CAFCASS EMAIL** {}", email);
                emailNotificationDetails.add(element(serviceOfApplicationEmailService.sendEmailNotificationToCafcass(
                    authorization, caseDetails, caseData, email, getNotificationPack(caseData, O, docs))));
            }
        }
        return emailNotificationDetails;

    }

    public ServedApplicationDetails sendNotificationForServiceOfApplication(CaseDetails caseDetails, String authorization)
        throws Exception {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
        List<Element<ServedApplicationDetails>> servedApplicationDetails = new ArrayList<>();
        if (!CaseCreatedBy.CITIZEN.equals(caseData.getCaseCreatedBy())) {
            log.info("Not created by citizen");
            if ((caseData.getServiceOfApplication().getSoaApplicantsList() != null)
                && (caseData.getServiceOfApplication().getSoaApplicantsList().getValue().size() > 0)) {
                log.info("serving applicants");
                emailNotificationDetails.addAll(sendNotificationToApplicantSolicitor(caseDetails, authorization));
            }
            if ((caseData.getServiceOfApplication().getSoaRespondentsList() != null)
                && (caseData.getServiceOfApplication().getSoaRespondentsList().getValue().size() > 0)) {
                log.info("serving respondents");
                List<Element<EmailNotificationDetails>> tempEmail = new ArrayList<>();
                List<Element<BulkPrintDetails>> tempPost = new ArrayList<>();
                Map<String, Object> resultMap = sendNotificationToRespondentOrSolicitor(caseDetails, authorization);
                if (null != resultMap && resultMap.containsKey("email")) {
                    tempEmail = (List<Element<EmailNotificationDetails>>) resultMap.get("email");
                }
                if (null != resultMap && resultMap.containsKey("post")) {
                    tempPost = (List<Element<BulkPrintDetails>>) resultMap.get("post");
                }
                emailNotificationDetails.addAll(tempEmail);
                bulkPrintDetails.addAll(tempPost);
            }
            //serving other people in case
            if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
                && null != caseData.getServiceOfApplication().getSoaOtherPeopleList()
                && caseData.getServiceOfApplication().getSoaOtherPeopleList().getValue().size() > 0) {
                log.info("serving other people in case");
                bulkPrintDetails.addAll(sendPostToOtherPeopleInCase(caseDetails, authorization));
            }
            //serving other emails
            log.info(
                "caseData.getServiceOfApplication().getSoaOtherEmailAddressList {}",
                caseData.getServiceOfApplication().getSoaOtherEmailAddressList()
            );
            if ((caseData.getServiceOfApplication() != null
                && caseData.getServiceOfApplication().getSoaOtherEmailAddressList().size() > 0)) {
                log.info("serving other emails");
                emailNotificationDetails.addAll(sendEmailToOtherEmails(authorization, caseDetails, caseData));
            }
            //serving cafcass
            log.info(
                "Before serving cafcass emails {}",
                caseData.getServiceOfApplication().getSoaCafcassEmailOptionChecked()
            );
            if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
                && null != caseData.getServiceOfApplication().getSoaCafcassEmailAddressList()
                && caseData.getServiceOfApplication().getSoaCafcassEmailAddressList().size() > 0
                && null != caseData.getServiceOfApplication().getSoaCafcassEmailOptionChecked()
                && caseData.getServiceOfApplication().getSoaCafcassEmailOptionChecked().get(0) != null) {
                log.info("serving cafcass emails");
                emailNotificationDetails.addAll(sendEmailToCafcassInCase(authorization, caseDetails, caseData));
            }

        } else {
            //CITIZEN SCENARIO
            if (caseData.getServiceOfApplication().getSoaApplicantsList().getValue() != null) {
                generatePinAndSendNotificationEmailForCitizen(caseData);
            }
        }
        return ServedApplicationDetails.builder().emailNotificationDetails(emailNotificationDetails)
            .servedBy(userService.getUserDetails(authorization).getFullName())
            .bulkPrintDetails(bulkPrintDetails).build();
    }

    public CaseData generatePinAndSendNotificationEmailForCitizen(CaseData caseData) {
        if (launchDarklyClient.isFeatureEnabled("generate-pin")) {
            if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
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

    public List<Element<EmailNotificationDetails>> sendNotificationToApplicantSolicitor(CaseDetails caseDetails, String authorization)
        throws Exception {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            List<Element<PartyDetails>> applicantsInCase = caseData.getApplicants();
            List<DynamicMultiselectListElement> applicantsList = caseData.getServiceOfApplication().getSoaApplicantsList().getValue();
            applicantsList.forEach(applicant -> {
                Optional<Element<PartyDetails>> party = getParty(applicant.getCode(), applicantsInCase);
                String docPackFlag = "";
                if (party.isPresent() && party.get().getValue().getSolicitorEmail() != null) {
                    try {
                        log.info(
                            "Sending the email notification to applicant solicitor for C100 Application for caseId {}",
                            caseDetails.getId()
                        );

                        docPackFlag = "Q";
                        List<Document> docs = new ArrayList<>();
                        emailNotificationDetails.add(element(serviceOfApplicationEmailService
                                                                 .sendEmailNotificationToApplicantSolicitor(
                                                                     authorization,
                                                                     caseDetails,
                                                                     party.get().getValue(),
                                                                     EmailTemplateNames.APPLICANT_SOLICITOR_CA,
                                                                     getNotificationPack(
                                                                         caseData,
                                                                         docPackFlag,
                                                                         docs
                                                                     )
                                                                 )));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } /*else if (party.isPresent() && party.get().getValue().getSolicitorEmail() == null) {
                    if (party.get().getValue().getSolicitorAddress() != null) {
                        log.info("Sending the notification in post to applicant solicitor for C100 Application for caseId {}", caseDetails.getId());
                        log.info("*** postal address ***" + party.get().getValue().getSolicitorAddress());
                        docPackFlag = "Q";
                        List<Document> docs = new ArrayList<>();
                        try {
                            getCoverLetter(authorization, caseData, party.get().getValue().getAddress(), docs);
                            serviceOfApplicationPostService.sendPostNotificationToParty(caseData, authorization, party.get().getValue(),
                                                                                        getNotificationPack(caseData,
                                                                                                            docPackFlag, docs));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        log.info("Unable to send any notification to applicant solicitor for C100 Application for caseId {} "
                                     + "as no address available", caseDetails.getId());
                    }


                }*/

            }
            );
            return emailNotificationDetails;
        } else {
            PartyDetails applicant = caseData.getApplicantsFL401();
            log.info("applicant FL401" + applicant);
            log.info("applicant FL401 sol email" + applicant.getSolicitorEmail());
            log.info("soa applicant list  FL401" + caseData.getServiceOfApplication().getSoaApplicantsList().getValue());
            String docPackFlag = "";
            if (applicant.getSolicitorEmail() != null) {
                try {
                    log.info(
                        "Sending the email notification to applicant solicitor for FL401 Application for caseId {}",
                        caseDetails.getId()
                    );
                    docPackFlag = "A";
                    List<Document> docs = new ArrayList<>();
                    emailNotificationDetails.add(element(serviceOfApplicationEmailService.sendEmailNotificationToApplicantSolicitor(
                        authorization,
                        caseDetails,
                        applicant,
                        EmailTemplateNames.APPLICANT_SOLICITOR_DA,
                        getNotificationPack(
                            caseData,
                            docPackFlag,
                            docs
                        )
                    )));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } /*else {
                if (applicant.getSolicitorAddress() != null) {
                    log.info("Sending the notification in post to applicant solicitor for FL401 Application for caseId {}", caseDetails.getId());
                    docPackFlag = "A";
                    List<Document> docs = new ArrayList<>();
                    getCoverLetter(authorization, caseData, applicant.getSolicitorAddress(), docs);
                    serviceOfApplicationPostService.sendPostNotificationToParty(caseData, authorization, applicant,
                                                                                getNotificationPack(caseData,
                                                                                                    docPackFlag,docs));
                } else {
                    log.info("Unable to send any notification to applicant solicitor for FL401 Application for caseId {} "
                                 + "as no address available", caseDetails.getId());
                }

            }
        }*/
            return emailNotificationDetails;
        }
    }

    public Map<String, Object> sendNotificationToRespondentOrSolicitor(CaseDetails caseDetails, String authorization) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            List<Element<PartyDetails>> respondentListC100 = caseData.getRespondents();
            List<DynamicMultiselectListElement> respondentsList = caseData.getServiceOfApplication()
                .getSoaRespondentsList().getValue();
            respondentsList.forEach(respondentc100 -> {
                Optional<Element<PartyDetails>> party = getParty(respondentc100.getCode(), respondentListC100);
                Map<String, Object> caseDataUpdated = new HashMap<>();
                String docPackFlag = "";
                if (party.isPresent() && YesNoDontKnow.yes.equals(party.get().getValue().getDoTheyHaveLegalRepresentation())) {

                    if (party.get().getValue().getSolicitorEmail() != null) {
                        try {
                            log.info(
                                "Sending the email notification to respondent solicitor for C100 Application for caseId {}",
                                caseDetails.getId()
                            );

                            docPackFlag = "S";
                            List<Document> docs = new ArrayList<>();
                            emailNotificationDetails.add(element(serviceOfApplicationEmailService.sendEmailNotificationToRespondentSolicitor(
                                authorization, caseDetails,
                                party.get().getValue(),
                                EmailTemplateNames.RESPONDENT_SOLICITOR,
                                getNotificationPack(caseData,
                                                    docPackFlag, docs
                                )
                            )));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } /*else {
                        if (party.get().getValue().getSolicitorAddress() != null) {
                            log.info(
                                "Sending the notification in post to respondent solicitor for C100 Application for caseId {}",
                                caseDetails.getId()
                            );

                            docPackFlag = "S";
                            List<Document> docs = new ArrayList<>();
                            try {
                                getCoverLetter(authorization, caseData, party.get().getValue().getAddress(), docs);
                                serviceOfApplicationPostService.sendPostNotificationToParty(
                                    caseData, authorization,
                                    party.get().getValue(),
                                    getNotificationPack(caseData,
                                                        docPackFlag, docs
                                    )
                                );
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            log.info(
                                "Unable to send any notification to respondent solicitor for C100 Application for caseId {} "
                                    + "as no address available",
                                caseDetails.getId()
                            );
                        }

                    }*/
                } else if (party.isPresent() && (YesNoDontKnow.no.equals(party.get().getValue().getDoTheyHaveLegalRepresentation())
                    || YesNoDontKnow.dontKnow.equals(party.get().getValue().getDoTheyHaveLegalRepresentation()))) {
                    log.info("The respondent is unrepresented");
                    if (party.get().getValue().getAddress() != null) {
                        log.info(
                            "Sending the notification in post to respondent for C100 Application for caseId {}",
                            caseDetails.getId()
                        );
                        docPackFlag = "R";
                        List<Document> docs = new ArrayList<>();
                        try {
                            docs.add(getCoverLetter(authorization, caseData, party.get().getValue()));
                            bulkPrintDetails.add(element(serviceOfApplicationPostService.sendPostNotificationToParty(
                                caseData,
                                authorization,
                                party.get().getValue(),
                                getNotificationPack(caseData,
                                                    docPackFlag, docs
                                )
                            )));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        log.info("Unable to send any notification to respondent for C100 Application for caseId {} "
                                     + "as no address available", caseDetails.getId());
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
        } else {
            String docPackFlag = "";
            PartyDetails respondentFL401 = caseData.getRespondentsFL401();
            PartyDetails applicantFL401 = caseData.getApplicantsFL401();
            log.info("respondentFL401 FL401" + respondentFL401);
            log.info("applicantFL401  sol email" + applicantFL401.getSolicitorEmail());
            log.info("soa resp list  FL401" + caseData.getServiceOfApplication().getSoaRespondentsList().getValue());

            if (applicantFL401.getSolicitorEmail() != null) {
                log.info("The respondent is represented");
                if (applicantFL401.getSolicitorEmail() != null) {
                    try {
                        log.info(
                            "Sending the email notification pack to applicant solicitor for FL401 respondent for caseId {}",
                            caseDetails.getId()
                        );
                        docPackFlag = "B";
                        List<Document> docs = new ArrayList<>();
                        emailNotificationDetails.add(element(serviceOfApplicationEmailService.sendEmailNotificationToApplicantSolicitor(
                            authorization,
                            caseDetails,
                            applicantFL401,
                            EmailTemplateNames.APPLICANT_SOLICITOR_DA,
                            getNotificationPack(
                                caseData,
                                docPackFlag,
                                docs
                            )
                        )));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } /*else {
                    if (applicantFL401.getSolicitorAddress() != null) {
                        log.info(
                            "Sending the notification pack  in post to applicant solicitor for FL401 respondent for caseId {}",
                            caseDetails.getId()
                        );
                        docPackFlag = "B";
                        List<Document> docs = new ArrayList<>();
                        getCoverLetter(authorization, caseData, applicantFL401.getSolicitorAddress(), docs);
                        serviceOfApplicationPostService.sendPostNotificationToParty(
                            caseData,
                            authorization,
                            respondentFL401,
                            getNotificationPack(caseData, docPackFlag, docs)
                        );
                    } else {
                        log.info(
                            "Unable to send any notification to respondent solicitor for FL401 Application for caseId {} "
                                + "as no address available",
                            caseDetails.getId()
                        );
                    }
                }*/
                resultMap.put("email", emailNotificationDetails);
                resultMap.put("post", bulkPrintDetails);
            } else {
                //LD feature toggle- needs review
                log.info("The respondent is unrepresented");
                if (respondentFL401.getEmail() != null
                    && launchDarklyClient.isFeatureEnabled("send-res-email-notification")) {
                    fl401CaseInviteService.generateAndSendCaseInviteForFL401Respondent(caseData, respondentFL401);
                }
            }
        }

        return resultMap;
    }

    private Optional<Element<PartyDetails>> getParty(String code, List<Element<PartyDetails>> parties) {
        Optional<Element<PartyDetails>> party = Optional.empty();
        party = parties.stream()
            .filter(element -> element.getId().toString().equalsIgnoreCase(code)).findFirst();

        return party;
    }

    public Document getCoverLetter(String authorization, CaseData caseData, PartyDetails partyDetails) throws Exception {
        return DocumentUtils.toCoverLetterDocument(serviceOfApplicationPostService
                                                       .getCoverLetterGeneratedDocInfo(caseData, authorization,
                                                                                       partyDetails
                                                       ));
    }

    private List<Document> getNotificationPack(CaseData caseData, String requiredPack, List<Document> docs) throws Exception {
        switch (requiredPack) {
            case Q:
                docs.addAll(generatePackQ(caseData));
                break;
            case S:
                docs.addAll(generatePackS(caseData));
                break;
            case R:
                docs.addAll(generatePackR(caseData));
                break;
            case A:
                docs.addAll(generatePackA(caseData));
                break;
            case B:
                docs.addAll(generatePackB(caseData));
                break;
            case G:
                docs.addAll(generatePackG(caseData));
                break;
            case O:
                docs.addAll(generatePackO(caseData));
                break;
            default:
                break;
        }
        log.info("DOCUMENTS IN THE PACK" + docs);
        return docs;

    }

    private List<Document> generatePackQ(CaseData caseData) throws Exception {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getStaticDocs(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        return docs;
    }

    private List<Document> generatePackS(CaseData caseData) throws Exception {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getStaticDocs(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        return docs;
    }

    private List<Document> generatePackR(CaseData caseData) throws Exception {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getStaticDocs(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        return docs;
    }

    private List<Document> generatePackA(CaseData caseData) throws Exception {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getStaticDocs(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        return docs;
    }

    private List<Document> generatePackB(CaseData caseData) throws Exception {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getStaticDocs(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        return docs;
    }

    private List<Document> generatePackG(CaseData caseData) throws Exception {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getStaticDocs(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        return docs;
    }

    private List<Document> generatePackO(CaseData caseData) throws Exception {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        docs.addAll(getStaticDocs(caseData));
        docs.addAll(getSoaSelectedOrders(caseData));
        return docs;
    }

    private List<Document> getCaseDocs(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        if (YesOrNo.Yes.equals(caseData.getLanguagePreferenceWelsh())) {
            if (null != caseData.getFinalWelshDocument()) {
                docs.add(caseData.getFinalWelshDocument());
            }
            if (null != caseData.getC1AWelshDocument()) {
                docs.add(caseData.getC1AWelshDocument());
            }
        } else {
            if (null != caseData.getFinalDocument()) {
                docs.add(caseData.getFinalDocument());
            }
            if (null != caseData.getC1ADocument()) {
                docs.add(caseData.getC1ADocument());
            }
        }
        return docs;
    }

    private List<Document> getDocumentsUploadedInServiceOfApplication(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        Optional<Document> pd36qLetter = Optional.ofNullable(caseData.getServiceOfApplicationUploadDocs().getPd36qLetter());
        Optional<Document> specialArrangementLetter = Optional.ofNullable(caseData.getServiceOfApplicationUploadDocs()
                                                                              .getSpecialArrangementsLetter());
        Optional<Document> additionalDocuments = Optional.ofNullable(caseData.getServiceOfApplicationUploadDocs()
                                                                         .getAdditionalDocuments());
        pd36qLetter.ifPresent(document -> docs.add(document));
        specialArrangementLetter.ifPresent(document -> docs.add(document));
        additionalDocuments.ifPresent(document -> docs.add(document));
        return docs;
    }

    private List<Document> getStaticDocs(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        //String filePath = "classpath:Privacy_Notice.pdf";
        Document privacyNotice = Document.builder().documentUrl("classpath:Privacy_Notice.pdf")
            .documentBinaryUrl("classpath:Privacy_Notice.pdf")
            .documentHash("classpath:Privacy_Notice.pdf")
            .documentFileName("Privacy_Notice.pdf").build();
        docs.add(privacyNotice);
        return docs;
    }

    private List<Document> getSoaSelectedOrders(CaseData caseData) {
        log.info("Orders on SoA" + caseData.getServiceOfApplicationScreen1().getValue());
        if (caseData.getServiceOfApplicationScreen1()
            .getValue().size() > 0) {

            List<String> orderNames = caseData.getServiceOfApplicationScreen1()
                .getValue().stream().map(DynamicMultiselectListElement::getCode)
                .map(xyz -> xyz.substring(0, xyz.indexOf("-")))
                .collect(Collectors.toList());
            log.info("order Names {}", orderNames);
            log.info("order Collection {}", caseData.getOrderCollection());

            return caseData.getOrderCollection().stream()
                .map(Element::getValue)
                .filter(i -> orderNames.contains(i.getOrderTypeId()))
                .map(i -> i.getOrderDocument())
                .collect(Collectors.toList());

        }
        return null;

    }

    public Map<String, Object> cleanUpSoaSelections(Map<String, Object> caseDataUpdated) {
        String[] soaFields = {"pd36qLetter", "specialArrangementsLetter",
            "additionalDocuments", "sentDocumentPlaceHolder", "soaApplicantsList",
            "soaRespondentsList", "soaOtherPeopleList", "soaCafcassEmailOptionChecked",
            "soaOtherEmailOptionChecked", "soaOtherEmailOptionChecked", "soaCafcassEmailAddressList",
            "soaOtherEmailAddressList", "coverPageAddress", "coverPagePartyName"};
        Arrays.stream(soaFields).forEach(s -> caseDataUpdated.put(s, null));
        return caseDataUpdated;
    }
}
