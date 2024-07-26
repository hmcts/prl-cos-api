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
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.SolicitorEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SolicitorEmailService {
    private final ObjectMapper objectMapper;
    private final EmailService emailService;
    @Value("${uk.gov.notify.email.application.email-id}")
    private String courtEmail;
    @Value("${xui.url}")
    private String manageCaseUrl;
    private  final CourtFinderService courtLocatorService;

    public EmailTemplateVars buildEmail(CaseDetails caseDetails, boolean isC100PendingPaymentSolEmail) {
        try {
            CaseData caseData = emailService.getCaseData(caseDetails);
            List<PartyDetails> applicants = caseData
                .getApplicants()
                .stream()
                .map(Element::getValue)
                .toList();

            List<String> applicantNamesList = applicants.stream()
                .map(PartyDetails::getLabelForDynamicList)
                .toList();

            String applicantNames = String.join(", ", applicantNamesList);
            Court court = courtLocatorService.getNearestFamilyCourt(caseData);

            String caseLink = manageCaseUrl + "/" + caseDetails.getId();

            return SolicitorEmail.builder()
                .caseReference(String.valueOf(caseDetails.getId()))
                .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
                .applicantName(applicantNames)
                .courtName((court != null) ? court.getCourtName() : "")
                .courtEmail(courtEmail)
                .caseLink(caseLink + (isC100PendingPaymentSolEmail ? "#Service%20Request" : ""))
                .solicitorName(caseData.getSolicitorName())
                .build();

        } catch (NotFoundException e) {
            log.error("Cannot send email", e);
        }
        return null;
    }


    public void sendEmail(CaseDetails caseDetails) {
        String applicantSolicitorEmailAddress = caseDetails.getData()
            .get(PrlAppsConstants.APPLICANT_SOLICITOR_EMAIL_ADDRESS).toString();
        emailService.send(
            applicantSolicitorEmailAddress,
            EmailTemplateNames.SOLICITOR,
            buildEmail(caseDetails, false),
            LanguagePreference.english
        );

    }

    public void sendReSubmitEmail(CaseDetails caseDetails) {
        String applicantSolicitorEmailAddress = caseDetails.getData()
            .get(PrlAppsConstants.APPLICANT_SOLICITOR_EMAIL_ADDRESS).toString();
        emailService.send(
            applicantSolicitorEmailAddress,
            EmailTemplateNames.SOLICITOR_RESUBMIT_EMAIL,
            buildEmail(caseDetails, false),
            LanguagePreference.english
        );

    }

    public void sendAwaitingPaymentEmail(uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails caseDetails) {
        String applicantSolicitorEmailAddress = caseDetails.getCaseData()
            .getApplicantSolicitorEmailAddress();

        emailService.send(
            applicantSolicitorEmailAddress,
            EmailTemplateNames.CA_AWAITING_PAYMENT,
            buildEmail(CaseDetails.builder().state(caseDetails.getState())
                           .id(Long.valueOf(caseDetails.getCaseId()))
                           .data(caseDetails.getCaseData()
                                     .toMap(objectMapper)).build(), true),
            LanguagePreference.getPreferenceLanguage(caseDetails.getCaseData())
        );

    }

    public void sendHelpWithFeesEmail(uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails caseDetails) {
        String applicantSolicitorEmailAddress = caseDetails.getCaseData()
            .getApplicantSolicitorEmailAddress();

        emailService.send(
            applicantSolicitorEmailAddress,
            EmailTemplateNames.CA_HELP_WITH_FEES,
            buildEmail(CaseDetails.builder().state(caseDetails.getState())
                           .id(Long.valueOf(caseDetails.getCaseId()))
                           .data(caseDetails.getCaseData()
                                     .toMap(objectMapper)).build(), false),
            LanguagePreference.getPreferenceLanguage(caseDetails.getCaseData())
        );

    }


    private EmailTemplateVars buildCaseWithdrawEmail(CaseDetails caseDetails) {

        return SolicitorEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();
    }

    public void sendWithDrawEmailToSolicitor(CaseDetails caseDetails, UserDetails userDetails) {
        String solicitorEmail = "";
        CaseData caseData = emailService.getCaseData(caseDetails);
        List<PartyDetails> applicants = caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .toList();

        List<String> applicantSolicitorEmailList = applicants.stream()
            .map(PartyDetails::getSolicitorEmail)
            .toList();

        solicitorEmail = (!applicantSolicitorEmailList.isEmpty() && null != applicantSolicitorEmailList.get(0)
            && !applicantSolicitorEmailList.get(0).isEmpty() && applicantSolicitorEmailList.size() == 1) ? applicantSolicitorEmailList.get(
            0)
            : userDetails.getEmail();

        emailService.send(
            solicitorEmail,
            EmailTemplateNames.WITHDRAW,
            buildCaseWithdrawEmail(caseDetails),
            LanguagePreference.english
        );

    }

    public void sendEmailToFl401Solicitor(CaseDetails caseDetails, UserDetails userDetails) {
        String solicitorEmail = "";

        String applicantSolicitorEmail = emailService.getCaseData(caseDetails)
            .getApplicantsFL401()
            .getSolicitorEmail();

        solicitorEmail = applicantSolicitorEmail != null ? applicantSolicitorEmail : userDetails.getEmail();

        emailService.send(
            solicitorEmail,
            EmailTemplateNames.DA_SOLICITOR,
            buildFl401SolicitorEmail(caseDetails),
            LanguagePreference.english
        );

    }

    public EmailTemplateVars buildFl401SolicitorEmail(CaseDetails caseDetails) {
        CaseData caseData = emailService.getCaseData(caseDetails);

        PartyDetails fl401Applicant = caseData
            .getApplicantsFL401();

        String applicantFullName = fl401Applicant.getFirstName() + " " + fl401Applicant.getLastName();

        return SolicitorEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .applicantName(applicantFullName)
            .courtName(caseData.getCourtName())
            .courtEmail(caseData.getCourtEmailAddress())
            .caseLink(manageCaseUrl + "/" + caseData.getId())
            .build();
    }

    public void sendWithDrawEmailToFl401Solicitor(CaseDetails caseDetails, UserDetails userDetails) {
        String fl401SolicitorEmail = "";

        String applicantSolicitorEmail = emailService.getCaseData(caseDetails)
            .getApplicantsFL401()
            .getSolicitorEmail();

        fl401SolicitorEmail = applicantSolicitorEmail != null ? applicantSolicitorEmail : userDetails.getEmail();

        emailService.send(
            fl401SolicitorEmail,
            EmailTemplateNames.WITHDRAW,
            buildCaseWithdrawEmail(caseDetails),
            LanguagePreference.english
        );
    }

    public void sendWithDrawEmailToSolicitorAfterIssuedState(CaseDetails caseDetails, UserDetails userDetails) {
        String solicitorEmail = "";
        CaseData caseData = emailService.getCaseData(caseDetails);
        List<PartyDetails> applicants = caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .toList();

        List<String> applicantSolicitorEmailList = applicants.stream()
            .map(PartyDetails::getSolicitorEmail)
            .toList();

        solicitorEmail = (!applicantSolicitorEmailList.isEmpty() && null != applicantSolicitorEmailList.get(0)
            && !applicantSolicitorEmailList.get(0).isEmpty() && applicantSolicitorEmailList.size() == 1) ? applicantSolicitorEmailList.get(
            0)
            : userDetails.getEmail();
        emailService.send(
            solicitorEmail,
            EmailTemplateNames.WITHDRAW_AFTER_ISSUED_SOLICITOR,
            buildCaseWithdrawEmailAfterIssuedState(caseDetails),
            LanguagePreference.english
        );
    }

    public void sendWithDrawEmailToFl401SolicitorAfterIssuedState(CaseDetails caseDetails, UserDetails userDetails) {
        String fl401SolicitorEmail = "";

        String applicantSolicitorEmail = emailService.getCaseData(caseDetails)
            .getApplicantsFL401()
            .getSolicitorEmail();

        fl401SolicitorEmail = applicantSolicitorEmail != null ? applicantSolicitorEmail : userDetails.getEmail();

        emailService.send(
            fl401SolicitorEmail,
            EmailTemplateNames.WITHDRAW_AFTER_ISSUED_SOLICITOR,
            buildCaseWithdrawEmailAfterIssuedState(caseDetails),
            LanguagePreference.english
        );
    }

    private EmailTemplateVars buildCaseWithdrawEmailAfterIssuedState(CaseDetails caseDetails) {
        return SolicitorEmail.builder()
            .issueDate(caseDetails.getData().get("issueDate").toString())
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();
    }
}
