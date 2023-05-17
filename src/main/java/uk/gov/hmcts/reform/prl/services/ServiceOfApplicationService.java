package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.A;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.B;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.Q;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.R;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.S;


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
        log.info(" Sending post to others involved ");
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            List<Element<PartyDetails>> otherPeopleInCase = caseData.getOthersToNotify();
            DynamicMultiSelectList othersToNotify = caseData.getConfirmRecipients().getOtherPeopleList();
            List<DynamicMultiselectListElement> othersList = othersToNotify.getListItems();
            othersList.forEach(other -> {
                Optional<Element<PartyDetails>> party = getParty(other.getCode(), otherPeopleInCase);
                try {
                    log.info(
                        "Sending the post notification to others in case for C100 Application for caseId {}",
                        caseDetails.getId()
                    );

                    serviceOfApplicationPostService.sendPostNotificationToParty(
                        caseData,
                        authorization,
                        party.get().getValue(),
                        getNotificationPack(caseData, "O")
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return caseData;
    }

    public void sendEmailToOtherEmails(String authorization, CaseDetails caseDetails, CaseData caseData) throws Exception {
        serviceOfApplicationEmailService.sendEmailNotificationToOtherEmails(authorization, caseDetails, caseData,
                                                                                   getNotificationPack(caseData, "G"));

    }

    public CaseData sendNotificationForServiceOfApplication(CaseDetails caseDetails, String authorization) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        log.info("Confirm recipients {}", caseData.getConfirmRecipients());
        if (!CaseCreatedBy.CITIZEN.equals(caseData.getCaseCreatedBy())) {
            if ((caseData.getConfirmRecipients() != null) && (caseData.getConfirmRecipients().getApplicantsList() != null)) {
                log.info("serving applicants");
                caseData = sendNotificationToApplicantSolicitor(caseDetails,authorization);
            }
            if ((caseData.getConfirmRecipients() != null) && (caseData.getConfirmRecipients().getRespondentsList() != null)) {
                log.info("serving respondents");
                caseData = sendNotificationToRespondentOrSolicitor(caseDetails, authorization);
            }
            if ((caseData.getConfirmRecipients() != null) && (caseData.getConfirmRecipients().getOtherEmailAddressList() != null)) {
                log.info("serving LA");
                sendEmailToOtherEmails(authorization, caseDetails, caseData);
            }
            if ((caseData.getConfirmRecipients() != null) && (caseData.getConfirmRecipients().getOtherPeopleList() != null)) {
                log.info("serving other people in case");
                caseData = sendPostToOtherPeopleInCase(caseDetails, authorization);
            }

        }
        caseInviteManager.generatePinAndSendNotificationEmail(caseData);
        return caseData;
    }

    public CaseData sendNotificationToApplicantSolicitor(CaseDetails caseDetails, String authorization) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            log.info("case type" + caseData.getCaseTypeOfApplication());
            List<Element<PartyDetails>> applicantsInCase = caseData.getApplicants();
            DynamicMultiSelectList applicantsToNotify = caseData.getConfirmRecipients().getApplicantsList();
            List<DynamicMultiselectListElement> applicantsList = applicantsToNotify.getListItems();
            applicantsList.forEach(applicant -> {
                Optional<Element<PartyDetails>> party = getParty(applicant.getCode(), applicantsInCase);
                log.info("party" + party.get().getValue());
                String docPackFlag = "";
                if (party.isPresent() && party.get().getValue().getSolicitorEmail() != null) {
                    log.info("party" + party.get().getValue().getSolicitorEmail());
                    try {
                        log.info("Sending the email notification to applicant solicitor for C100 Application for caseId {}", caseDetails.getId());

                        docPackFlag = "Q";
                        serviceOfApplicationEmailService.sendEmailNotificationToApplicantSolicitor(authorization, caseDetails, party.get().getValue(),
                                                                                                   EmailTemplateNames.APPLICANT_SOLICITOR_CA,
                                                                                                   getNotificationPack(caseData, docPackFlag));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else if (party.isPresent() && party.get().getValue().getSolicitorEmail() == null) {
                    if (party.get().getValue().getSolicitorAddress() != null) {
                        log.info("Sending the notification in post to applicant solicitor for C100 Application for caseId {}", caseDetails.getId());

                        docPackFlag = "Q";
                        try {
                            serviceOfApplicationPostService.sendPostNotificationToParty(caseData, authorization, party.get().getValue(),
                                                                                        getNotificationPack(caseData, docPackFlag));
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
            if (caseData.getConfirmRecipients().getApplicantsList().getListItems().contains(applicant) && YesOrNo.Yes.getDisplayedValue()
                    .equalsIgnoreCase(applicant.getSolicitorEmail())) {
                try {
                    log.info("Sending the email notification to applicant solicitor for FL401 Application for caseId {}", caseDetails.getId());
                    docPackFlag = "A";
                    serviceOfApplicationEmailService.sendEmailNotificationToApplicantSolicitor(authorization,caseDetails,
                                                                                               applicant,EmailTemplateNames.APPLICANT_SOLICITOR_DA,
                                                                                               getNotificationPack(caseData, docPackFlag));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (caseData.getConfirmRecipients().getApplicantsList().getListItems().contains(applicant) && YesOrNo.No.getDisplayedValue()
                .equalsIgnoreCase(applicant.getSolicitorEmail())) {
                if (applicant.getSolicitorAddress() != null) {
                    log.info("Sending the notification in post to applicant solicitor for FL401 Application for caseId {}", caseDetails.getId());
                    docPackFlag = "A";
                    serviceOfApplicationPostService.sendPostNotificationToParty(caseData, authorization, applicant,
                                                                                getNotificationPack(caseData, docPackFlag));
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
            DynamicMultiSelectList respondentsToNotify = caseData.getConfirmRecipients().getRespondentsList();
            List<DynamicMultiselectListElement> respondentsList = respondentsToNotify.getListItems();
            respondentsList.forEach(respondentc100 -> {
                Optional<Element<PartyDetails>> party = getParty(respondentc100.getCode(), respondentListC100);
                String docPackFlag = "";
                if (party.isPresent() && YesNoDontKnow.yes.equals(party.get().getValue().getDoTheyHaveLegalRepresentation())) {

                    if (YesOrNo.Yes.getDisplayedValue()
                        .equalsIgnoreCase(party.get().getValue().getSolicitorEmail())) {
                        try {
                            log.info("Sending the email notification to respondent solicitor for C100 Application for caseId {}",
                                     caseDetails.getId());

                            docPackFlag = "S";
                            serviceOfApplicationEmailService.sendEmailNotificationToRespondentSolicitor(
                                authorization,caseDetails,
                                party.get().getValue(),
                                EmailTemplateNames.RESPONDENT_SOLICITOR,
                                getNotificationPack(caseData, docPackFlag)
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
                                                           getNotificationPack(caseData, docPackFlag));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            log.info("Unable to send any notification to respondent solicitor for C100 Application for caseId {} "
                                         + "as no address available", caseDetails.getId());
                        }

                    }
                } else if (party.isPresent() && YesNoDontKnow.no.equals(party.get().getValue().getDoTheyHaveLegalRepresentation())) {
                    if (party.get().getValue().getAddress() != null) {
                        log.info("Sending the notification in post to respondent for C100 Application for caseId {}",
                                 caseDetails.getId());
                        docPackFlag = "R";
                        try {
                            serviceOfApplicationPostService.sendPostNotificationToParty(
                                                       caseData,
                                                       authorization,
                                                       party.get().getValue(),
                                                       getNotificationPack(caseData, docPackFlag));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        } else {
            String docPackFlag = "";
            PartyDetails respondentFL401 = caseData.getRespondentsFL401();
            PartyDetails applicantFL401 = caseData.getApplicantsFL401();
            if (caseData.getConfirmRecipients().getRespondentsList().getListItems().contains(respondentFL401)
                && YesNoDontKnow.yes.equals(applicantFL401.getDoTheyHaveLegalRepresentation())) {
                if (YesOrNo.Yes.getDisplayedValue().equalsIgnoreCase(applicantFL401.getSolicitorEmail())) {
                    try {
                        log.info(
                            "Sending the email notification to respondent solicitor for FL401 Application for caseId {}",
                            caseDetails.getId()
                        );
                        docPackFlag = "B";
                        serviceOfApplicationEmailService.sendEmailNotificationToApplicantSolicitor(authorization,caseDetails,
                                                                                                    respondentFL401,
                                                                                                    EmailTemplateNames.APPLICANT_SOLICITOR_DA,
                                                                                                   getNotificationPack(caseData, docPackFlag)
                        );
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else  {
                    if (applicantFL401.getSolicitorAddress() != null) {
                        log.info(
                            "Sending the notification in post to respondent solicitor for FL401 Application for caseId {}",
                            caseDetails.getId()
                        );
                        docPackFlag = "B";
                        serviceOfApplicationPostService.sendPostNotificationToParty(
                            caseData,
                            authorization,
                            respondentFL401,
                            getNotificationPack(caseData, docPackFlag)
                        );
                    } else {
                        log.info(
                            "Unable to send any notification to respondent solicitor for FL401 Application for caseId {} "
                                + "as no address available",
                            caseDetails.getId()
                        );
                    }
                }
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

    private List<Document> getNotificationPack(CaseData caseData, String requiredPack) throws Exception {
        List<Document> docs = new ArrayList<>();
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
            default:
                break;
        }
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
