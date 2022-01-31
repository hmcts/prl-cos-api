package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.notify.CaseWorkerEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.service.notify.NotificationClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaseWorkerEmailService {

    private final NotificationClient notificationClient;
    private final EmailTemplatesConfig emailTemplatesConfig;
    private final ObjectMapper objectMapper;

    private static final String URGENT_CASE = "Urgent ";
    private static final String WITHOUT_NOTICE = "Without notice";
    private static final String REDUCED_NOTICE = "Reduced notice";
    private static final String STANDARAD_HEARING = "Standard hearing";

    @Autowired
    private EmailService emailService;

    @Value("${uk.gov.notify.email.application.email-id}")
    private  String courtEmail;

    @Value("${uk.gov.notify.email.application.court-name}")
    private  String courtName;

    @Value("${xui.url}")
    private String manageCaseUrl;

    public EmailTemplateVars buildEmail(CaseDetails caseDetails) {

        List<PartyDetails> applicants = emailService.getCaseData(caseDetails)
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> applicantNamesList = applicants.stream()
            .map(element -> element.getFirstName() + " " + element.getLastName())
            .collect(Collectors.toList());

        final String applicantNames = String.join(", ", applicantNamesList);

        List<PartyDetails> respondents = emailService.getCaseData(caseDetails)
            .getRespondents()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> respondentsList = respondents.stream()
            .map(PartyDetails::getLastName)
            .collect(Collectors.toList());

        final String respondentNames = String.join(", ", respondentsList);

        List<String> typeOfHearing = new ArrayList<>();

        if (emailService.getCaseData(caseDetails).getIsCaseUrgent().equals(YesOrNo.Yes)) {
            typeOfHearing.add(URGENT_CASE);
        }
        if (emailService.getCaseData(caseDetails).getDoYouNeedAWithoutNoticeHearing().equals(YesOrNo.Yes)) {
            typeOfHearing.add(WITHOUT_NOTICE);
        }
        if (emailService.getCaseData(caseDetails).getDoYouRequireAHearingWithReducedNotice().equals(YesOrNo.Yes)) {
            typeOfHearing.add(REDUCED_NOTICE);
        }
        if ((emailService.getCaseData(caseDetails).getIsCaseUrgent().equals(YesOrNo.No))
                && (emailService.getCaseData(caseDetails).getDoYouNeedAWithoutNoticeHearing().equals(YesOrNo.No))
                && (emailService.getCaseData(caseDetails).getDoYouRequireAHearingWithReducedNotice().equals(YesOrNo.No))) {
            typeOfHearing.add(STANDARAD_HEARING);
        }
        final String typeOfHearings = String.join(", ", typeOfHearing);

        List<String> typeOfOrder = new ArrayList<>();

        if (emailService.getCaseData(caseDetails).getOrdersApplyingFor().contains(OrderTypeEnum.childArrangementsOrder)) {
            typeOfOrder.add(OrderTypeEnum.childArrangementsOrder.getDisplayedValue());
        }
        if (emailService.getCaseData(caseDetails).getOrdersApplyingFor().contains(OrderTypeEnum.prohibitedStepsOrder)) {
            typeOfOrder.add(OrderTypeEnum.prohibitedStepsOrder.getDisplayedValue());
        }
        if (emailService.getCaseData(caseDetails).getOrdersApplyingFor().contains(OrderTypeEnum.specificIssueOrder)) {
            typeOfOrder.add(OrderTypeEnum.specificIssueOrder.getDisplayedValue());
        }

        String typeOfOrders;
        if (typeOfOrder.size() == 2) {
            typeOfOrders = String.join(" and ", typeOfOrder);
        } else {
            typeOfOrders = String.join(", ", typeOfOrder);
        }

        return CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .applicantName(applicantNames)
            .respondentLastName(respondentNames)
            .hearingDateRequested("  ")
            .ordersApplyingFor(typeOfOrders)
            .typeOfHearing(typeOfHearings)
            .courtEmail(courtEmail)
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();

    }

    public void sendEmail(CaseDetails caseDetails) {

        emailService.send(
            caseDetails.getData().get("caseworkerEmailAddress").toString(),
            EmailTemplateNames.CASEWORKER,
            buildEmail(caseDetails),
            LanguagePreference.ENGLISH
        );

    }

    public String getRecipientEmail(UserDetails userDetails) {
        return userDetails.getEmail() != null ? userDetails.getEmail() : "prl_caseworker_solicitor@mailinator.com";
    }
}
