package uk.gov.hmcts.reform.prl.services.solicitornotifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.CitizenEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.ApplicantSolicitorEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.RespondentSolicitorEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.URL_STRING;

@Service
@Slf4j
@RequiredArgsConstructor
public class SolicitorNotificationEmailService {


    @Autowired
    EmailService emailService;

    @Value("${xui.url}")
    private String manageCaseUrl;

    @Value("${citizen.url}")
    private String dashboardUrl;

    public void sendC100ApplicantSolicitorNotification(CaseDetails caseDetails) throws Exception {

        CaseData caseData = emailService.getCaseData(caseDetails);
        Map<String, String> applicantSolicitors = caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toMap(
                PartyDetails::getSolicitorEmail,
                i -> i.getRepresentativeFirstName() + " " + i.getRepresentativeLastName()
            ));

        for (Map.Entry<String, String> appSols : applicantSolicitors.entrySet()) {

            emailService.send(
                appSols.getKey(),
                EmailTemplateNames.CA_APPLICANT_SOLICITOR_RES_NOTIFICATION,
                buildApplicantSolicitorEmail(caseDetails, appSols.getValue()),
                LanguagePreference.english
            );
        }

    }

    public void sendC100RespondentSolicitorNotification(CaseDetails caseDetails) throws Exception {
        CaseData caseData = emailService.getCaseData(caseDetails);

        List<Map<String, List<String>>> respondentSolicitors = caseData
            .getRespondents()
            .stream()
            .map(Element::getValue)
            .filter(i -> YesNoDontKnow.yes.equals(i.getDoTheyHaveLegalRepresentation()))
            .map(i -> {
                Map<String, List<String>> temp = new HashMap<>();
                temp.put(i.getSolicitorEmail(),List.of(i.getRepresentativeFirstName() + " " + i.getRepresentativeLastName(),
                                                       i.getFirstName() + " " + i.getLastName()));
                return temp;
            })
            .collect(Collectors.toList());

        for (Map<String,List<String>> resSols : respondentSolicitors) {
            String solicitorEmail = resSols.keySet().toArray()[0].toString();
            emailService.send(
                solicitorEmail,
                EmailTemplateNames.CA_RESPONDENT_SOLICITOR_RES_NOTIFICATION,
                buildRespondentSolicitorEmail(caseDetails, resSols.get(solicitorEmail).get(0),
                                              resSols.get(solicitorEmail).get(1)),
                LanguagePreference.english
            );

        }
    }

    public void sendC100RespondentNotification(CaseDetails caseDetails) throws Exception {
        CaseData caseData = emailService.getCaseData(caseDetails);

        for (Element<PartyDetails> respondent : caseData.getRespondents()) {
            emailService.send(
                respondent.getValue().getEmail(),
                EmailTemplateNames.CA_CITIZEN_RES_NOTIFICATION,
                buildRespondentEmail(caseDetails, respondent.getValue()),
                LanguagePreference.english
            );
        }
    }

    private EmailTemplateVars buildApplicantSolicitorEmail(CaseDetails caseDetails, String solicitorName) {
        CaseData caseData = emailService.getCaseData(caseDetails);
        return ApplicantSolicitorEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .solicitorName(solicitorName)
            .caseLink(manageCaseUrl + URL_STRING + caseDetails.getId())
            .issueDate(caseData.getIssueDate())
            .build();
    }

    private EmailTemplateVars buildRespondentEmail(CaseDetails caseDetails, PartyDetails partyDetails) {
        CaseData caseData = emailService.getCaseData(caseDetails);
        return CitizenEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .respondentName(String.format("%s %s", partyDetails.getFirstName(), partyDetails.getLastName()))
            .dashboardLink(dashboardUrl + URL_STRING + caseDetails.getId())
            .build();
    }

    private EmailTemplateVars buildRespondentSolicitorEmail(CaseDetails caseDetails, String solicitorName,
                                                            String respondentName) {
        CaseData caseData = emailService.getCaseData(caseDetails);

        return RespondentSolicitorEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .solicitorName(solicitorName)
            .caseLink(manageCaseUrl + URL_STRING + caseDetails.getId())
            .respondentName(respondentName)
            .issueDate(caseData.getIssueDate())
            .build();
    }
}
