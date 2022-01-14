package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.notify.CaseWorkerEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.SolicitorEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.service.notify.NotificationClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaseWorkerEmailService {

    private final NotificationClient notificationClient;
    private final EmailTemplatesConfig emailTemplatesConfig;
    private final ObjectMapper objectMapper;

    private static final String URGENT_CASE = "Urgent Case";
    private static final String WITHOUT_NOTICE = "Without Notice";
    private static final String STANDARAD_HEARING = "Standard Hearing";

    @Autowired
    private EmailService emailService;

    @Value("${uk.gov.notify.email.application.email-id}")
    private  String courtEmail;

    @Value("${uk.gov.notify.email.application.court-name}")
    private  String courtName;

    @Value("${xui.url}")
    private String manageCaseUrl;

    public EmailTemplateVars buildEmail(CaseDetails caseDetails, UserDetails userDetails) {
        List<PartyDetails> applicants = caseDetails.getCaseData()
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> applicantNamesList = applicants.stream()
            .map(element -> element.getFirstName() + " " + element.getLastName())
            .collect(Collectors.toList());

        String applicantNames = String.join(", ", applicantNamesList);

        List<PartyDetails> respondents = caseDetails.getCaseData()
            .getRespondents()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> respondentsList = respondents.stream()
            .map(PartyDetails::getLastName)
            .collect(Collectors.toList());

        String respondentNames = String.join(", ", respondentsList);

        String caseUrgency;

        if (caseDetails.getCaseData().getIsCaseUrgent().equals(YesOrNo.YES)) {
            caseUrgency = URGENT_CASE;
        } else if (caseDetails.getCaseData().getDoYouNeedAWithoutNoticeHearing().equals(YesOrNo.YES)) {
            caseUrgency = WITHOUT_NOTICE;
        } else {
            caseUrgency = STANDARAD_HEARING;
        }

        String typeOfOrder;

        if(caseDetails.getCaseData().getOrdersApplyingFor().equals(OrderTypeEnum.childArrangementsOrder)) {
            typeOfOrder = String.valueOf(OrderTypeEnum.childArrangementsOrder);
        } else if(caseDetails.getCaseData().getOrdersApplyingFor().equals(OrderTypeEnum.prohibitedStepsOrder)) {
            typeOfOrder = String.valueOf(OrderTypeEnum.prohibitedStepsOrder);
        } else {
            typeOfOrder = String.valueOf(OrderTypeEnum.specificIssueOrder);
        }

        EmailTemplateVars emailTemplateVars = CaseWorkerEmail.builder()
            .caseReference(caseDetails.getCaseId())
            .caseName(caseDetails.getCaseData().getApplicantCaseName())
            .applicantName(applicantNames)
            .respondentLastName(respondentNames)
            .hearingDateRequested("  ")
            .ordersApplyingFor(typeOfOrder)
            .typeOfHearing(caseUrgency)
            .courtEmail(courtEmail)
            .caseLink(manageCaseUrl + "/" + caseDetails.getCaseId())
            .build();

        return emailTemplateVars;

    }

    public void sendEmail(CaseDetails caseDetails, UserDetails userDetails) {

        emailService.send(
            getRecipientEmail(userDetails),
            EmailTemplateNames.CASEWORKER,
            buildEmail(caseDetails, userDetails),
            LanguagePreference.ENGLISH
        );

    }

    public String getRecipientEmail(UserDetails userDetails) {
        return userDetails.getEmail() != null ? userDetails.getEmail() : "prl_caseworker_solicitor@mailinator.com";
    }
}
