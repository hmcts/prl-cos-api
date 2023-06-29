package uk.gov.hmcts.reform.prl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.events.TransferToAnotherCourtEvent;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.transfercase.TransferCaseContentProvider;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TransferToAnotherCourtEventHandler {
    private final EmailService emailService;
    private final TransferCaseContentProvider transferCaseContentProvider;

    @EventListener(condition = "#event.typeOfEvent eq 'Transfer to another court'")
    public void notifyAllParties(final TransferToAnotherCourtEvent event) {
        CaseData caseData = event.getCaseData();

        sendEmailToApplicantSolicitor(caseData, EmailTemplateNames.TRANSFER_COURT_EMAIL_NOTIFICATION_APPLICANT);

        sendEmailToApplicant(caseData, EmailTemplateNames.TRANSFER_COURT_EMAIL_NOTIFICATION_APPLICANT);

        sendEmailToRespondentSolicitor(caseData, EmailTemplateNames.TRANSFER_COURT_EMAIL_NOTIFICATION_RESPONDENT);

        sendEmailToRespondents(caseData, EmailTemplateNames.TRANSFER_COURT_EMAIL_NOTIFICATION_RESPONDENT);

        sendEmailToOtherParties(caseData, EmailTemplateNames.TRANSFER_COURT_EMAIL_NOTIFICATION_OTHER_PARTIES);
    }

    private void sendEmailToRespondents(CaseData caseData, EmailTemplateNames emailTemplateNames) {
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            Map<String, String> respondentEmail = caseData.getRespondents().stream()
                .map(Element::getValue)
                .filter(respondent -> !CaseUtils.hasLegalRepresentation(respondent)
                    && Yes.equals(respondent.getCanYouProvideEmailAddress()))
                .collect(Collectors.toMap(
                    PartyDetails::getEmail,
                    party -> party.getFirstName() + " " + party.getLastName(),
                    (x, y) -> x
                ));

            if (!respondentEmail.isEmpty()) {
                respondentEmail.forEach(
                    (key, value) ->
                        emailService.send(
                            key,
                            emailTemplateNames,
                            transferCaseContentProvider.buildCourtTransferEmailCitizen(caseData, value, false),
                            LanguagePreference.getPreferenceLanguage(caseData)
                        ));
            }
        } else {
            PartyDetails respondent  = caseData.getRespondentsFL401();
            String respondentName = respondent.getFirstName() + " " + respondent.getLastName();
            emailService.send(
                respondent.getEmail(),
                emailTemplateNames,
                transferCaseContentProvider.buildCourtTransferEmailCitizen(caseData, respondentName, false),
                LanguagePreference.getPreferenceLanguage(caseData)
            );
        }
    }

    private void sendEmailToRespondentSolicitor(CaseData caseData, EmailTemplateNames emailTemplateName) {
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            List<Map<String, List<String>>> respondentSolicitors = caseData
                .getRespondents()
                .stream()
                .map(Element::getValue)
                .filter(i -> YesNoDontKnow.yes.equals(i.getDoTheyHaveLegalRepresentation()))
                .map(i -> {
                    Map<String, List<String>> temp = new HashMap<>();
                    temp.put(i.getSolicitorEmail(), List.of(
                        i.getRepresentativeFullName()
                    ));
                    return temp;
                })
                .collect(Collectors.toList());

            for (Map<String, List<String>> resSols : respondentSolicitors) {
                String solicitorEmail = resSols.keySet().toArray()[0].toString();
                emailService.send(
                    solicitorEmail,
                    emailTemplateName,
                    transferCaseContentProvider.buildCourtTransferEmailSolicitor(
                        caseData,
                        resSols.get(solicitorEmail).get(0)
                    ),
                    LanguagePreference.english
                );
            }
        } else {
            PartyDetails respondent  = caseData.getRespondentsFL401();
            String solicitorName = respondent.getRepresentativeFullName();
            if (YesNoDontKnow.yes.equals(respondent.getDoTheyHaveLegalRepresentation())) {
                emailService.send(
                    respondent.getSolicitorEmail(),
                    emailTemplateName,
                    transferCaseContentProvider.buildCourtTransferEmailSolicitor(
                        caseData,
                        solicitorName
                    ),
                    LanguagePreference.english
                );
            }
        }
    }

    private void sendEmailToApplicant(CaseData caseData, EmailTemplateNames emailTemplateName) {
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            Map<String, String> applicantEmails = caseData.getApplicants().stream()
                .map(Element::getValue)
                .filter(applicant -> !CaseUtils.hasLegalRepresentation(applicant)
                    && Yes.equals(applicant.getCanYouProvideEmailAddress()))
                .collect(Collectors.toMap(
                    PartyDetails::getEmail,
                    PartyDetails::getLabelForDynamicList
                ));

            if (!applicantEmails.isEmpty()) {
                applicantEmails.forEach(
                    (key, value) ->
                        emailService.send(
                            key,
                            emailTemplateName,
                            transferCaseContentProvider.buildCourtTransferEmailCitizen(caseData, value, false),
                            LanguagePreference.getPreferenceLanguage(caseData)
                        ));
            }
        } else {
            PartyDetails applicant  = caseData.getApplicantsFL401();
            String applicantName = applicant.getLabelForDynamicList();
            emailService.send(
                applicant.getEmail(),
                emailTemplateName,
                transferCaseContentProvider.buildCourtTransferEmailCitizen(caseData, applicantName, false),
                LanguagePreference.getPreferenceLanguage(caseData)
            );
        }
    }

    private void sendEmailToApplicantSolicitor(CaseData caseData, EmailTemplateNames emailTemplateName) {
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            Map<String, String> applicantSolicitors = caseData
                .getApplicants()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toMap(
                    PartyDetails::getSolicitorEmail,
                    PartyDetails::getRepresentativeFullName
                ));

            for (Map.Entry<String, String> appSols : applicantSolicitors.entrySet()) {
                emailService.send(
                    appSols.getKey(),
                    emailTemplateName,
                    transferCaseContentProvider.buildCourtTransferEmailSolicitor(caseData, appSols.getValue()),
                    LanguagePreference.getPreferenceLanguage(caseData)
                );
            }
        } else {
            PartyDetails applicant  = caseData.getApplicantsFL401();
            String solicitorName = applicant.getRepresentativeFullName();
            emailService.send(
                applicant.getSolicitorEmail(),
                emailTemplateName,
                transferCaseContentProvider.buildCourtTransferEmailSolicitor(caseData, solicitorName),
                LanguagePreference.getPreferenceLanguage(caseData)
            );
        }
    }

    private void sendEmailToOtherParties(CaseData caseData, EmailTemplateNames emailTemplateNames) {
        Map<String, String> othersToNotify = CaseUtils.getOthersToNotify(caseData);
        if (!othersToNotify.isEmpty()) {
            othersToNotify.forEach(
                (key, value) -> emailService.send(
                    key,
                    emailTemplateNames,
                    transferCaseContentProvider.buildCourtTransferEmailCitizen(
                        caseData,
                        value,
                        true
                    ),
                    LanguagePreference.getPreferenceLanguage(caseData)
                ));
        }
    }
}
