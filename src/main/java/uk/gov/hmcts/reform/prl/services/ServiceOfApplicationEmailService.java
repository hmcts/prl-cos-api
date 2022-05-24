package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.ManageOrderEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.ServiceOfApplicationSolicitorEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.URL_STRING;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServiceOfApplicationEmailService {

    @Autowired
    private EmailService emailService;

    @Value("${uk.gov.notify.email.application.email-id}")
    private String courtEmail;

    @Value("${xui.url}")
    private String manageCaseUrl;


    public void sendEmail(CaseDetails caseDetails) {
        log.info("Sending the server Parties emails for caseId {}", caseDetails.getId());

        CaseData caseData = emailService.getCaseData(caseDetails);
        List<PartyDetails> applicants = caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        if(applicants != null && !applicants.isEmpty()) {
            List<String> applicantSolicitorEmailList = applicants.stream()
                .map(PartyDetails::getSolicitorEmail)
                .collect(Collectors.toList());

            if(applicantSolicitorEmailList != null && !applicantSolicitorEmailList.isEmpty()) {
                applicantSolicitorEmailList.forEach(email -> emailService.send(
                    email,
                    EmailTemplateNames.APPLICANT_SOLICITOR,
                    buildEmail(caseDetails),
                    LanguagePreference.english
                ));
            }
        }
    }


    private EmailTemplateVars buildEmail(CaseDetails caseDetails, String solicitorName) {

        CaseData caseData = emailService.getCaseData(caseDetails);

        return ServiceOfApplicationSolicitorEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .solicitorName(solicitorName)
            .caseLink(manageCaseUrl + URL_STRING + caseDetails.getId())
            .privacyNoticeLink()
            .build();
    }


    private List<PartyDetails> getApplicants(CaseData caseData) {
        return caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
    }

}
