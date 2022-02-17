package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.SolicitorEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.service.notify.NotificationClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SolicitorEmailService {

    private final NotificationClient notificationClient;
    private final EmailTemplatesConfig emailTemplatesConfig;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    @Autowired
    private EmailService emailService;

    @Value("${uk.gov.notify.email.application.email-id}")
    private String courtEmail;

    @Value("${uk.gov.notify.email.application.court-name}")
    private String courtName;

    @Value("${xui.url}")
    private String manageCaseUrl;

    @Autowired
    private CourtFinderService courtLocatorService;

    public EmailTemplateVars buildEmail(CaseDetails caseDetails) {
        try {
            CaseData caseData = emailService.getCaseData(caseDetails);
            List<PartyDetails> applicants = caseData
                .getApplicants()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            List<String> applicantNamesList = applicants.stream()
                .map(element -> element.getFirstName() + " " + element.getLastName())
                .collect(Collectors.toList());

            String applicantNames = String.join(", ", applicantNamesList);

            Court court = null;

            court = courtLocatorService.getClosestChildArrangementsCourt(caseData);

            return   SolicitorEmail.builder()
                .caseReference(String.valueOf(caseDetails.getId()))
                .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
                .applicantName(applicantNames)
                .courtName(court.getCourtName())
                .courtEmail(courtEmail)
                .caseLink(manageCaseUrl + "/" + caseDetails.getId())
                .build();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void sendEmail(CaseDetails caseDetails) {
        log.info("Sending the email to solicitor for caseId {}", caseDetails.getId()
        );

        emailService.send(
            "yogendra.upasani@hmcts.net",
            EmailTemplateNames.SOLICITOR,
            buildEmail(caseDetails),
            LanguagePreference.ENGLISH
        );

    }

    /*
     * Todo TO be removed once done with fee and pay bypass
     * */
    public void sendEmailBypss(CaseDetails caseDetails, String authorisation) {
        log.info("inside send email bypass");
        emailService.send(
            "yogendra.upasani@hmcts.net",
            EmailTemplateNames.SOLICITOR,
            buildEmail(caseDetails),
            LanguagePreference.ENGLISH
        );

    }

    private EmailTemplateVars buildCaseWithdrawEmail(CaseDetails caseDetails) {

        return SolicitorEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();
    }

    public void sendEmailToSolicitor(CaseDetails caseDetails, UserDetails userDetails) {
        String solicitorEmail = "";
        CaseData caseData = emailService.getCaseData(caseDetails);
        List<PartyDetails> applicants = caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> applicantSolicitorEmailList = applicants.stream()
            .map(element -> element.getSolicitorEmail())
            .collect(Collectors.toList());

        solicitorEmail = (!applicantSolicitorEmailList.isEmpty() && null != applicantSolicitorEmailList.get(0)
            && !applicantSolicitorEmailList.get(0).isEmpty() && applicantSolicitorEmailList.size() == 1) ? applicantSolicitorEmailList.get(0)
            : userDetails.getEmail();

        emailService.send(
            solicitorEmail,
            EmailTemplateNames.WITHDRAW,
            buildCaseWithdrawEmail(caseDetails),
            LanguagePreference.ENGLISH
        );

    }
}
