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
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.pin.C100CaseInviteService;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.services.pin.FL401CaseInviteService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.A;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.B;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMAIL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.G;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.O;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.POST;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.Q;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.R;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.S;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.hasLegalRepresentation;


@Service
@Slf4j
@RequiredArgsConstructor
public class ServiceOfApplicationService {

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
    private LaunchDarklyClient launchDarklyClient;

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
        collapsible.add("<ul><li>C100</li><li>C1A</li><li>C7</li><li>C1A (blank)</li><li>C8 (Cafcass and Local Authority only)</li>");
        collapsible.add("<li>Annex Z</li><li>Privacy notice</li><li>Any orders and"
                            + " hearing notices created at the initial gatekeeping stage</li></ul>");
        collapsible.add("</div>");
        collapsible.add("</details>");
        return String.join("\n\n", collapsible);
    }

    public Map<String,Object> getOrderSelectionsEnumValues(List<String> orderList, Map<String,Object> caseData) {
        for (String s : orderList) {
            caseData.put(CreateSelectOrderOptionsEnum.mapOptionFromDisplayedValue(s),"1");
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
            if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                serviceOfApplicationEmailService.sendEmailC100(caseDetails);
            } else {
                serviceOfApplicationEmailService.sendEmailFL401(caseDetails);
            }
        }
        return caseInviteManager.generatePinAndSendNotificationEmail(caseData);
    }

    public CaseData sendPostToOtherPeopleInCase(CaseDetails caseDetails, String authorization) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
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

                serviceOfApplicationPostService.sendPostNotificationToParty(
                        caseData,
                        authorization,
                        party.get().getValue(),
                        getNotificationPack(authorization,caseData, O,POST)
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return caseData;
    }

    public void sendEmailToOtherEmails(String authorization, CaseDetails caseDetails, CaseData caseData) throws Exception {
        serviceOfApplicationEmailService.sendEmailNotificationToOtherEmails(authorization, caseDetails, caseData,
                                                                                   getNotificationPack(authorization,caseData, G,EMAIL));

    }

    public void sendEmailToCafcassInCase(String authorization, CaseDetails caseDetails, CaseData caseData) throws Exception {
        serviceOfApplicationEmailService.sendEmailNotificationToCafcass(authorization, caseDetails, caseData,
                                                                            getNotificationPack(authorization,caseData, O,EMAIL));

    }

    public CaseData sendNotificationForServiceOfApplication(CaseDetails caseDetails, String authorization) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        if (!CaseCreatedBy.CITIZEN.equals(caseData.getCaseCreatedBy())) {
            if ((caseData.getServiceOfApplication().getSoaApplicantsList() != null)
                && (caseData.getServiceOfApplication().getSoaApplicantsList().getValue() != null)) {
                log.info("serving applicants");
                caseData = sendNotificationToApplicantSolicitor(caseDetails,authorization);
            }
            if ((caseData.getServiceOfApplication().getSoaRespondentsList() != null)
                && (caseData.getServiceOfApplication().getSoaRespondentsList().getValue() != null)) {
                log.info("serving respondents");
                sendNotificationToRespondentOrSolicitor(caseDetails, authorization);
            }
            //serving other people in case
            if ((C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication()))
                && (caseData.getServiceOfApplication().getSoaOtherPeopleList().getValue() != null)) {
                caseData = sendPostToOtherPeopleInCase(caseDetails, authorization);
            }
            //serving other emails
            if ((caseData.getServiceOfApplication() != null
                && caseData.getServiceOfApplication().getSoaOtherEmailAddressList() != null)) {
                sendEmailToOtherEmails(authorization, caseDetails, caseData);
            }
            //serving cafcass
            if ((C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication()))
                && (caseData.getServiceOfApplication().getSoaCafcassEmailAddressList() != null)) {
                sendEmailToCafcassInCase(authorization,caseDetails, caseData);
            }

        } else {
            //CITIZEN SCENARIO
            if (caseData.getServiceOfApplication().getSoaApplicantsList().getValue() != null) {
                generatePinAndSendNotificationEmailForCitizen(caseData);
            }
        }
        return caseData;
    }

    public CaseData generatePinAndSendNotificationEmailForCitizen(CaseData caseData) {
        if (launchDarklyClient.isFeatureEnabled("generate-pin")) {
            if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                log.info("In Generating and sending PIN to Citizen C100");
                List<Element<PartyDetails>> citizensInCase = caseData.getApplicants();
                List<DynamicMultiselectListElement> citizenList = caseData.getServiceOfApplication().getSoaApplicantsList().getValue();
                citizenList.forEach(applicant -> {
                    Optional<Element<PartyDetails>> party = getParty(applicant.getCode(), citizensInCase);
                    if (party.isPresent()) {
                        c100CaseInviteService.generateAndSendCaseInviteEmailForC100Citizen(caseData,party.get().getValue());
                    }
                });
            } else {
                log.info("In Generating and sending PIN to Citizen FL401");
                PartyDetails applicant = caseData.getApplicantsFL401();
                if (caseData.getServiceOfApplication().getSoaApplicantsList().getValue().contains(applicant)) {
                    fl401CaseInviteService.generateAndSendCaseInviteEmailForFL401Citizen(caseData,applicant);
                }

            }
        }
        return  caseData;
    }

    public CaseData sendNotificationToApplicantSolicitor(CaseDetails caseDetails, String authorization) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            log.info("***In sendNotificationToApplicantSolicitor*** case type" + caseData.getCaseTypeOfApplication());
            List<Element<PartyDetails>> applicantsInCase = caseData.getApplicants();
            List<DynamicMultiselectListElement> applicantsList = caseData.getServiceOfApplication().getSoaApplicantsList().getValue();
            applicantsList.forEach(applicant -> {
                Optional<Element<PartyDetails>> party = getParty(applicant.getCode(), applicantsInCase);
                String docPackFlag = "";
                if (party.isPresent() && party.get().getValue().getSolicitorEmail() != null) {
                    try {
                        log.info("Sending the email notification to applicant solicitor for C100 Application for caseId {}", caseDetails.getId());

                        docPackFlag = "Q";
                        serviceOfApplicationEmailService.sendEmailNotificationToApplicantSolicitor(authorization, caseDetails, party.get().getValue(),
                                                                                                   EmailTemplateNames.APPLICANT_SOLICITOR_CA,
                                                                                                   getNotificationPack(authorization, caseData,
                                                                                                                       docPackFlag,EMAIL));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else if (party.isPresent() && party.get().getValue().getSolicitorEmail() == null) {
                    if (party.get().getValue().getSolicitorAddress() != null) {
                        log.info("Sending the notification in post to applicant solicitor for C100 Application for caseId {}", caseDetails.getId());
                        log.info("*** postal address ***" + party.get().getValue().getSolicitorAddress());
                        docPackFlag = "Q";
                        try {
                            serviceOfApplicationPostService.sendPostNotificationToParty(caseData, authorization, party.get().getValue(),
                                                                                        getNotificationPack(authorization,caseData,
                                                                                                            docPackFlag, POST));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        log.info("Unable to send any notification to applicant solicitor for C100 Application for caseId {} "
                                     + "as no address available", caseDetails.getId());
                    }


                }

            }
            );
        } else {
            PartyDetails applicant = caseData.getApplicantsFL401();
            String docPackFlag = "";
            if (caseData.getServiceOfApplication().getSoaApplicantsList().getValue().contains(applicant) && YesOrNo.Yes.getDisplayedValue()
                    .equalsIgnoreCase(applicant.getSolicitorEmail())) {
                try {
                    log.info("Sending the email notification to applicant solicitor for FL401 Application for caseId {}", caseDetails.getId());
                    docPackFlag = "A";
                    serviceOfApplicationEmailService.sendEmailNotificationToApplicantSolicitor(authorization,caseDetails,
                                                                                               applicant,EmailTemplateNames.APPLICANT_SOLICITOR_DA,
                                                                                               getNotificationPack(authorization, caseData,
                                                                                                                   docPackFlag,EMAIL));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (caseData.getServiceOfApplication().getSoaApplicantsList().getValue().contains(applicant) && YesOrNo.No.getDisplayedValue()
                .equalsIgnoreCase(applicant.getSolicitorEmail())) {
                if (applicant.getSolicitorAddress() != null) {
                    log.info("Sending the notification in post to applicant solicitor for FL401 Application for caseId {}", caseDetails.getId());
                    docPackFlag = "A";
                    serviceOfApplicationPostService.sendPostNotificationToParty(caseData, authorization, applicant,
                                                                                getNotificationPack(authorization, caseData,
                                                                                                    docPackFlag,POST));
                } else {
                    log.info("Unable to send any notification to applicant solicitor for FL401 Application for caseId {} "
                                 + "as no address available", caseDetails.getId());
                }

            }
        }
        return  caseData;
    }

    public CaseData sendNotificationToRespondentOrSolicitor(CaseDetails caseDetails, String authorization) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
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
                            log.info("Sending the email notification to respondent solicitor for C100 Application for caseId {}",
                                     caseDetails.getId());

                            docPackFlag = "S";
                            serviceOfApplicationEmailService.sendEmailNotificationToRespondentSolicitor(
                                authorization,caseDetails,
                                party.get().getValue(),
                                EmailTemplateNames.RESPONDENT_SOLICITOR,
                                getNotificationPack(authorization, caseData,
                                                    docPackFlag, EMAIL)
                            );
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        if (party.get().getValue().getSolicitorAddress() != null) {
                            log.info("Sending the notification in post to respondent solicitor for C100 Application for caseId {}",
                                                       caseDetails.getId());

                            docPackFlag = "S";
                            try {
                                serviceOfApplicationPostService.sendPostNotificationToParty(
                                                           caseData,authorization,
                                                           party.get().getValue(),
                                                           getNotificationPack(authorization, caseData,
                                                                               docPackFlag, POST));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            log.info("Unable to send any notification to respondent solicitor for C100 Application for caseId {} "
                                         + "as no address available", caseDetails.getId());
                        }

                    }
                } else if (party.isPresent() && YesNoDontKnow.no.equals(party.get().getValue().getDoTheyHaveLegalRepresentation())) {
                    log.info("The respondent is unrepresented");
                    if (party.get().getValue().getAddress() != null) {
                        log.info("Sending the notification in post to respondent for C100 Application for caseId {}",
                                 caseDetails.getId());
                        docPackFlag = "R";
                        try {
                            serviceOfApplicationPostService.sendPostNotificationToParty(
                                                       caseData,
                                                       authorization,
                                                       party.get().getValue(),
                                                       getNotificationPack(authorization, caseData,
                                                                           docPackFlag, POST));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        log.info("Unable to send any notification to respondent for C100 Application for caseId {} "
                                     + "as no address available", caseDetails.getId());
                    }
                    if (!hasLegalRepresentation(party.get().getValue())
                        && Yes.equals(party.get().getValue().getCanYouProvideEmailAddress())) {

                        c100CaseInviteService.generateAndSendCaseInviteForC100Respondent(caseData, party.get().getValue());
                    }

                }
            });
        } else {
            String docPackFlag = "";
            PartyDetails respondentFL401 = caseData.getRespondentsFL401();
            PartyDetails applicantFL401 = caseData.getApplicantsFL401();
            if (caseData.getServiceOfApplication().getSoaRespondentsList().getValue().contains(respondentFL401)
                && YesNoDontKnow.yes.equals(applicantFL401.getDoTheyHaveLegalRepresentation())) {
                if (YesOrNo.Yes.getDisplayedValue().equalsIgnoreCase(applicantFL401.getSolicitorEmail())) {
                    try {
                        log.info(
                            "Sending the email notification pack to applicant solicitor for FL401 respondent for caseId {}",
                            caseDetails.getId()
                        );
                        docPackFlag = "B";
                        serviceOfApplicationEmailService.sendEmailNotificationToApplicantSolicitor(authorization,caseDetails,
                                                                                                    respondentFL401,
                                                                                                    EmailTemplateNames.APPLICANT_SOLICITOR_DA,
                                                                                                   getNotificationPack(authorization, caseData,
                                                                                                                       docPackFlag, EMAIL)
                        );
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else  {
                    if (applicantFL401.getSolicitorAddress() != null) {
                        log.info(
                            "Sending the notification pack  in post to applicant solicitor for FL401 respondent for caseId {}",
                            caseDetails.getId()
                        );
                        docPackFlag = "B";
                        serviceOfApplicationPostService.sendPostNotificationToParty(
                            caseData,
                            authorization,
                            respondentFL401,
                            getNotificationPack(authorization, caseData, docPackFlag, POST)
                        );
                    } else {
                        log.info(
                            "Unable to send any notification to respondent solicitor for FL401 Application for caseId {} "
                                + "as no address available",
                            caseDetails.getId()
                        );
                    }
                }
            } else if (YesNoDontKnow.no.equals(respondentFL401.getDoTheyHaveLegalRepresentation())
                    && Yes.equals(respondentFL401.getCanYouProvideEmailAddress())) {
                fl401CaseInviteService.generateAndSendCaseInviteForFL401Respondent(caseData, respondentFL401);
            }


        }

        return  caseData;
    }

    private Optional<Element<PartyDetails>> getParty(String code, List<Element<PartyDetails>> parties) {
        Optional<Element<PartyDetails>> party = Optional.empty();
        party = parties.stream()
            .filter(element -> element.getId().toString().equalsIgnoreCase(code)).findFirst();

        return party;
    }

    private List<Document> getNotificationPack(String authorization, CaseData caseData, String requiredPack,
                                               String flag) throws Exception {
        List<Document> docs = new ArrayList<>();
        if (flag.equals("Post")) {
            docs.add(DocumentUtils.toDocument(serviceOfApplicationPostService
                                                  .getCoverLetterGeneratedDocInfo(caseData, authorization)));
        }
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
        return  docs;

    }

    private List<Document> generatePackQ(CaseData caseData) throws Exception {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getMandatoryCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        return docs;
    }

    private List<Document> generatePackS(CaseData caseData) throws Exception {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getMandatoryCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        return docs;
    }

    private List<Document> generatePackR(CaseData caseData) throws Exception {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getMandatoryCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        return docs;
    }

    private List<Document> generatePackA(CaseData caseData) throws Exception {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getMandatoryCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        return docs;
    }

    private List<Document> generatePackB(CaseData caseData) throws Exception {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getMandatoryCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        return docs;
    }

    private List<Document> generatePackG(CaseData caseData) throws Exception {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getMandatoryCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        return docs;
    }

    private List<Document> generatePackO(CaseData caseData) throws Exception {
        List<Document> docs = new ArrayList<>();
        docs.addAll(getMandatoryCaseDocs(caseData));
        docs.addAll(getDocumentsUploadedInServiceOfApplication(caseData));
        return docs;
    }

    private List<Document> getMandatoryCaseDocs(CaseData caseData) {
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
        pd36qLetter.ifPresent(document -> docs.add(document));
        specialArrangementLetter.ifPresent(document -> docs.add(document));
        return docs;
    }

}
