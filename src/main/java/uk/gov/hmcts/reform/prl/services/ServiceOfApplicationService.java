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
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

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

    public CaseData sendPost(CaseDetails caseDetails, String authorization) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        log.info(" Sending post to the parties involved ");
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            serviceOfApplicationPostService.sendDocs(caseData,authorization);
        }
        return caseData;
    }

    public CaseData sendNotificationForServiceOfApplication(CaseDetails caseDetails, String authorization) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        if (CaseCreatedBy.SOLICITOR.equals(caseData.getCaseCreatedBy())) {
            sendNotificationToApplicantSolicitor(caseDetails,authorization);
            sendNotificationToRespondentOrSolicitor(caseDetails,authorization);
            /*Notifying other people in the case*/
            caseData = sendPost(caseDetails, authorization);
            caseInviteManager.generatePinAndSendNotificationEmail(caseData);
        }

        return caseData;
    }

    public CaseData sendNotificationToApplicantSolicitor(CaseDetails caseDetails, String authorization) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            List<Element<PartyDetails>> applicantsList = caseData.getApplicants();
            applicantsList.forEach(applicant -> {
                if (YesOrNo.Yes.getDisplayedValue()
                    .equalsIgnoreCase(applicant.getValue().getSolicitorEmail())) {
                    try {
                        log.info("Sending the email notification to applicant solicitor for C100 Application for caseId {}", caseDetails.getId());

                        serviceOfApplicationEmailService.sendEmailNotificationToApplicantSolicitor(caseDetails, applicant.getValue(),
                                                                                                       EmailTemplateNames.APPLICANT_SOLICITOR_CA);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    if (applicant.getValue().getSolicitorAddress() != null) {
                        log.info("Sending the notification in post to applicant solicitor for C100 Application for caseId {}", caseDetails.getId());

                        serviceOfApplicationPostService.sendPostNotificationToParty(caseData, authorization, applicant.getValue());
                    } else {
                        log.info("Unable to send any notification to applicant solicitor for C100 Application for caseId {} "
                                     + "as no address available", caseDetails.getId());
                    }

                }

            }
            );
        } else {
            PartyDetails applicant = caseData.getApplicantsFL401();
            if (YesOrNo.Yes.getDisplayedValue()
                    .equalsIgnoreCase(applicant.getSolicitorEmail())) {
                try {
                    log.info("Sending the email notification to applicant solicitor for FL401 Application for caseId {}", caseDetails.getId());
                    serviceOfApplicationEmailService.sendEmailNotificationToApplicantSolicitor(caseDetails,
                                                                                               applicant,EmailTemplateNames.APPLICANT_SOLICITOR_DA);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                if (applicant.getSolicitorAddress() != null) {
                    log.info("Sending the notification in post to applicant solicitor for FL401 Application for caseId {}", caseDetails.getId());

                    serviceOfApplicationPostService.sendPostNotificationToParty(caseData, authorization, applicant);
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
            respondentListC100.forEach(respondentc100 -> {
                if (YesNoDontKnow.yes.equals(respondentc100.getValue().getDoTheyHaveLegalRepresentation())) {

                    if (YesOrNo.Yes.getDisplayedValue()
                        .equalsIgnoreCase(respondentc100.getValue().getSolicitorEmail())) {
                        try {
                            log.info("Sending the email notification to respondent solicitor for C100 Application for caseId {}",
                                     caseDetails.getId());

                            serviceOfApplicationEmailService.sendEmailNotificationToRespondentSolicitor(
                                caseDetails,
                                respondentc100.getValue(),
                                EmailTemplateNames.APPLICANT_SOLICITOR_CA
                            );
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        if (respondentc100.getValue().getSolicitorAddress() != null) {
                            log.info("Sending the notification in post to respondent solicitor for C100 Application for caseId {}",
                                                       caseDetails.getId());

                            serviceOfApplicationPostService.sendPostNotificationToParty(
                                                       caseData,
                                                       authorization,
                                                       respondentc100.getValue());
                        } else {
                            log.info("Unable to send any notification to respondent solicitor for C100 Application for caseId {} "
                                         + "as no address available", caseDetails.getId());
                        }

                    }
                } else {
                    if (respondentc100.getValue().getAddress() != null) {
                        log.info("Sending the notification in post to respondent for C100 Application for caseId {}",
                                 caseDetails.getId());
                        serviceOfApplicationPostService.sendPostNotificationToParty(
                                                   caseData,
                                                   authorization,
                                                   respondentc100.getValue());
                    }
                }
            });
        } else {
            PartyDetails respondentFL401 = caseData.getRespondentsFL401();
            if (YesNoDontKnow.yes.equals(respondentFL401.getDoTheyHaveLegalRepresentation())) {
                if (YesOrNo.Yes.getDisplayedValue().equalsIgnoreCase(respondentFL401.getSolicitorEmail())) {
                    try {
                        log.info(
                            "Sending the email notification to respondent solicitor for FL401 Application for caseId {}",
                            caseDetails.getId()
                        );
                        serviceOfApplicationEmailService.sendEmailNotificationToRespondentSolicitor(caseDetails,
                                                                                                    respondentFL401,
                                                                                                    EmailTemplateNames.APPLICANT_SOLICITOR_DA
                        );
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    if (respondentFL401.getSolicitorAddress() != null) {
                        log.info(
                            "Sending the notification in post to respondent solicitor for FL401 Application for caseId {}",
                            caseDetails.getId()
                        );

                        serviceOfApplicationPostService.sendPostNotificationToParty(
                            caseData,
                            authorization,
                            respondentFL401
                        );
                    } else {
                        log.info(
                            "Unable to send any notification to respondent solicitor for FL401 Application for caseId {} "
                                + "as no address available",
                            caseDetails.getId()
                        );
                    }
                }
            } else {
                if (respondentFL401.getAddress() != null) {
                    log.info("Sending the notification in post to respondent for FL401 Application for caseId {}",
                             caseDetails.getId());
                    serviceOfApplicationPostService.sendPostNotificationToParty(
                        caseData,
                        authorization,
                        respondentFL401);
                }
            }

        }

        return  caseData;
    }
}
