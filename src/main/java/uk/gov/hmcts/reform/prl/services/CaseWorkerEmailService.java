package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.CaseWorkerEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private static final String YES = "Yes";
    private static final String NO = "No";

    @Autowired
    private EmailService emailService;

    @Value("${uk.gov.notify.email.application.email-id}")
    private String courtEmail;

    @Value("${uk.gov.notify.email.application.court-name}")
    private String courtName;

    @Value("${xui.url}")
    private String manageCaseUrl;

    private CaseData caseData;

    public EmailTemplateVars buildEmail(CaseDetails caseDetails) {

        caseData = emailService.getCaseData(caseDetails);

        List<PartyDetails> applicants = caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> applicantNamesList = applicants.stream()
            .map(element -> element.getFirstName() + " " + element.getLastName())
            .collect(Collectors.toList());

        final String applicantNames = String.join(", ", applicantNamesList);

        List<PartyDetails> respondents = caseData
            .getRespondents()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> respondentsList = respondents.stream()
            .map(PartyDetails::getLastName)
            .collect(Collectors.toList());

        final String respondentNames = String.join(", ", respondentsList);

        List<String> typeOfHearing = new ArrayList<>();

        if (caseData.getIsCaseUrgent().equals(YesOrNo.Yes)) {
            typeOfHearing.add(URGENT_CASE);
        }
        if (caseData.getDoYouNeedAWithoutNoticeHearing().equals(YesOrNo.Yes)) {
            typeOfHearing.add(WITHOUT_NOTICE);
        }
        if (caseData.getDoYouRequireAHearingWithReducedNotice().equals(YesOrNo.Yes)) {
            typeOfHearing.add(REDUCED_NOTICE);
        }
        if ((caseData.getIsCaseUrgent().equals(YesOrNo.No))
            && (caseData.getDoYouNeedAWithoutNoticeHearing().equals(YesOrNo.No))
            && (caseData.getDoYouRequireAHearingWithReducedNotice().equals(YesOrNo.No))) {
            typeOfHearing.add(STANDARAD_HEARING);
        }
        final String typeOfHearings = String.join(", ", typeOfHearing);

        List<String> typeOfOrder = new ArrayList<>();

        if (caseData.getOrdersApplyingFor().contains(OrderTypeEnum.childArrangementsOrder)) {
            typeOfOrder.add(OrderTypeEnum.childArrangementsOrder.getDisplayedValue());
        }
        if (caseData.getOrdersApplyingFor().contains(OrderTypeEnum.prohibitedStepsOrder)) {
            typeOfOrder.add(OrderTypeEnum.prohibitedStepsOrder.getDisplayedValue());
        }
        if (caseData.getOrdersApplyingFor().contains(OrderTypeEnum.specificIssueOrder)) {
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
            .caseName(caseData.getApplicantCaseName())
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
        String caseworkerEmailId = "fprl_caseworker_solicitor@mailinator.com";
        emailService.send(
            caseworkerEmailId,
            EmailTemplateNames.CASEWORKER,
            buildEmail(caseDetails),
            LanguagePreference.ENGLISH
        );

    }

    private EmailTemplateVars buildReturnApplicationEmail(CaseDetails caseDetails) {

        String returnMessage = emailService.getCaseData(caseDetails).getReturnMessage();

        return CaseWorkerEmail.builder()
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .contentFromDev(returnMessage)
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();

    }

    public void sendReturnApplicationEmailToSolicitor(CaseDetails caseDetails) {

        List<PartyDetails> applicants = emailService.getCaseData(caseDetails)
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> applicantEmailList = applicants.stream()
            .map(PartyDetails::getSolicitorEmail)
            .collect(Collectors.toList());

        String email = applicantEmailList.get(0);

        if (applicants.size() > 1) {
            email = emailService.getCaseData(caseDetails).getApplicantSolicitorEmailAddress();
        }
        emailService.send(
            email,
            EmailTemplateNames.RETURNAPPLICATION,
            buildReturnApplicationEmail(caseDetails),
            LanguagePreference.ENGLISH
        );

    }

    public void sendEmailToLocalCourt(CaseDetails caseDetails, String courtEmail) {

        emailService.send(
            courtEmail,
            EmailTemplateNames.DA_LOCALCOURT,
            buildCourtAdminEmail(caseDetails),
            LanguagePreference.ENGLISH
        );
    }

    public EmailTemplateVars buildCourtAdminEmail(CaseDetails caseDetails) {

        caseData = emailService.getCaseData(caseDetails);
        List<PartyDetails> applicants = caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<Child> child = caseData
            .getChildren()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        String isConfidential = NO;
        if ((applicants.stream().noneMatch(PartyDetails::isCanYouProvideEmailAddress)
            && applicants.stream().anyMatch(PartyDetails::isEmailAddressNull))
            || (applicants.stream().anyMatch(PartyDetails::hasConfidentialInfo))
            || (child.stream().anyMatch(Child::hasConfidentialInfo))) {
            isConfidential = YES;
        }

        String typeOfHearing = "";
        String isCaseUrgent = NO;

        if (caseData.getIsCaseUrgent().equals(YesOrNo.Yes)) {
            typeOfHearing = URGENT_CASE;
            isCaseUrgent = YES;
        }

        LocalDate issueDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        return CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .caseUrgency(typeOfHearing)
            .isCaseUrgent(isCaseUrgent)
            .issueDate(issueDate.format(dateTimeFormatter))
            .isConfidential(isConfidential)
            .caseLink(manageCaseUrl + "/" + caseData.getId())
            .build();
    }
}
