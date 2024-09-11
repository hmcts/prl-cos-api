package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.FL401RejectReasonEnum;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.RejectReasonEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.GatekeeperEmail;
import uk.gov.hmcts.reform.prl.models.complextypes.LocalCourtAdminEmail;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.CaseWorkerEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseWorkerEmailService {
    private static final String URL_STRING = "/";
    private static final String URGENT_CASE = "Urgent ";
    private static final String WITHOUT_NOTICE = "Without notice";
    private static final String REDUCED_NOTICE = "Reduced notice";
    private static final String STANDARAD_HEARING = "Standard hearing";
    private static final String YES = "Yes";
    private static final String NO = "No";
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private final EmailService emailService;

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
            .toList();

        List<String> applicantNamesList = applicants.stream()
            .map(element -> element.getFirstName() + " " + element.getLastName())
            .toList();

        final String applicantNames = String.join(", ", applicantNamesList);

        List<PartyDetails> respondents = caseData
            .getRespondents()
            .stream()
            .map(Element::getValue)
            .toList();

        List<String> respondentsList = respondents.stream()
            .map(PartyDetails::getLastName)
            .toList();

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
            .caseLink(manageCaseUrl + URL_STRING + caseDetails.getId())
            .build();

    }

    public void sendEmail(CaseDetails caseDetails) {
        String caseworkerEmailAddress = caseDetails.getData().get("caseworkerEmailAddress").toString();

        emailService.send(
            caseworkerEmailAddress,
            EmailTemplateNames.CASEWORKER,
            buildEmail(caseDetails),
            LanguagePreference.english
        );

    }

    private EmailTemplateVars buildReturnApplicationEmail(CaseDetails caseDetails) {

        caseData = emailService.getCaseData(caseDetails);

        String returnMessage = caseData.getReturnMessage();

        return CaseWorkerEmail.builder()
            .caseName(caseData.getApplicantCaseName())
            .contentFromDev(returnMessage)
            .caseLink(manageCaseUrl + URL_STRING + caseDetails.getId())
            .build();
    }


    public void sendReturnApplicationEmailToSolicitor(CaseDetails caseDetails) {
        caseData = emailService.getCaseData(caseDetails);
        String email = "";
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            List<PartyDetails> applicants = caseData
                .getApplicants()
                .stream()
                .map(Element::getValue)
                .toList();

            List<String> applicantEmailList = applicants.stream()
                .map(PartyDetails::getSolicitorEmail)
                .toList();

            email = applicantEmailList.get(0);

            if (applicants.size() > 1) {
                email = caseData.getApplicantSolicitorEmailAddress();
            }
            if (caseData.getRejectReason().contains(RejectReasonEnum.consentOrderNotProvided)) {
                emailService.send(
                    email,
                    EmailTemplateNames.RETURN_APPLICATION_CONSENT_ORDER,
                    buildReturnApplicationEmail(caseDetails),
                    LanguagePreference.getPreferenceLanguage(caseData)
                );
            }

        } else {
            PartyDetails fl401Applicant = caseData
                .getApplicantsFL401();

            email = fl401Applicant.getSolicitorEmail();
            if (caseData.getFl401RejectReason().contains(FL401RejectReasonEnum.consentOrderNotProvided)) {
                emailService.send(
                    email,
                    EmailTemplateNames.RETURN_APPLICATION_CONSENT_ORDER,
                    buildReturnApplicationEmail(caseDetails),
                    LanguagePreference.getPreferenceLanguage(caseData)
                );
            }
        }

        emailService.send(
            email,
            EmailTemplateNames.RETURNAPPLICATION,
            buildReturnApplicationEmail(caseDetails),
            LanguagePreference.english
        );


    }

    public void sendEmailToGateKeeper(CaseDetails caseDetails) {

        caseData = emailService.getCaseData(caseDetails);

        List<GatekeeperEmail> gatekeeperEmails = caseData
            .getGatekeeper()
            .stream()
            .map(Element::getValue)
            .toList();

        List<String> emailList = gatekeeperEmails.stream()
            .map(GatekeeperEmail::getEmail)
            .toList();

        emailList.forEach(email ->   emailService.send(
            email,
            PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
                ? EmailTemplateNames.GATEKEEPER_FL401 : EmailTemplateNames.GATEKEEPER,
            buildGatekeeperEmail(caseDetails),
            LanguagePreference.english
        ));
    }

    public EmailTemplateVars buildGatekeeperEmail(CaseDetails caseDetails) {

        caseData = emailService.getCaseData(caseDetails);

        String typeOfHearing = "";
        String isCaseUrgent = NO;

        if (YesOrNo.Yes.equals(caseData.getIsCaseUrgent())) {
            typeOfHearing = URGENT_CASE;
            isCaseUrgent = YES;
        }

        LocalDate issueDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

        return CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .caseUrgency(typeOfHearing)
            .isCaseUrgent(isCaseUrgent)
            .issueDate(issueDate.format(dateTimeFormatter))
            .caseLink(manageCaseUrl + URL_STRING + caseDetails.getId())
            .build();
    }


    public void sendEmailToCourtAdmin(CaseDetails caseDetails) {

        caseData = emailService.getCaseData(caseDetails);
        log.info("Triggering case worker email service to send mail to court admin");
        List<LocalCourtAdminEmail> localCourtAdminEmails = caseData
            .getLocalCourtAdmin()
            .stream()
            .map(Element::getValue)
            .toList();

        List<String> emailList = localCourtAdminEmails.stream()
            .map(LocalCourtAdminEmail::getEmail)
            .toList();
        emailList.forEach(email -> {
            if (null != email) {
                emailService.send(
                    email,
                    EmailTemplateNames.COURTADMIN,
                    buildCourtAdminEmail(caseDetails),
                    LanguagePreference.english
                );
            }
        });
    }

    public EmailTemplateVars buildCourtAdminEmail(CaseDetails caseDetails) {

        caseData = emailService.getCaseData(caseDetails);

        List<PartyDetails> applicants = caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .toList();

        List<YesOrNo> emailAddressInfo = applicants.stream()
            .filter(eachParty -> null != eachParty.getIsEmailAddressConfidential()
                && YesOrNo.Yes.equals(eachParty.getIsEmailAddressConfidential()))
            .map(PartyDetails::getIsEmailAddressConfidential)
            .toList();

        String isConfidential = NO;
        if (emailAddressInfo.contains(YesOrNo.Yes)
            || (applicants.stream().anyMatch(PartyDetails::hasConfidentialInfo))
            || (!TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
                && !TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())// requires review
                && caseData
                .getChildren()
                .stream()
                .map(Element::getValue).anyMatch(Child::hasConfidentialInfo))) {
            isConfidential = YES;
        }

        String typeOfHearing = "";
        String isCaseUrgent = NO;

        if (caseData.getIsCaseUrgent().equals(YesOrNo.Yes)) {
            typeOfHearing = URGENT_CASE;
            isCaseUrgent = YES;
        }

        LocalDate issueDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

        return CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .caseUrgency(typeOfHearing)
            .isCaseUrgent(isCaseUrgent)
            .issueDate(issueDate.format(dateTimeFormatter))
            .isConfidential(isConfidential)
            .caseLink(manageCaseUrl + "/" + caseDetails.getId())
            .build();
    }

    public void sendEmailToFl401LocalCourt(CaseDetails caseDetails, String courtEmail) {
        emailService.send(
            courtEmail,
            EmailTemplateNames.DA_LOCALCOURT,
            buildFl401LocalCourtAdminEmail(caseDetails),
            LanguagePreference.english
        );
    }

    public EmailTemplateVars buildFl401LocalCourtAdminEmail(CaseDetails caseDetails) {

        log.info("building FL401 email to localcourt for :{} ", caseDetails.getId());
        caseData = emailService.getCaseData(caseDetails);
        PartyDetails fl401Applicant = caseData
            .getApplicantsFL401();

        String isConfidential = NO;
        if (fl401Applicant.getCanYouProvideEmailAddress().equals(YesOrNo.Yes)
            || (null != fl401Applicant.getIsEmailAddressConfidential()
            && YesOrNo.Yes.equals(fl401Applicant.getIsEmailAddressConfidential()))
            || (fl401Applicant.hasConfidentialInfo())) {
            isConfidential = YES;
        }

        LocalDate issueDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

        return CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .issueDate(issueDate.format(dateTimeFormatter))
            .isConfidential(isConfidential)
            .caseLink(manageCaseUrl + "/" + caseData.getId())
            .build();
    }

    public void sendWithdrawApplicationEmailToLocalCourt(CaseDetails caseDetails, String courtEmail) {

        log.info("Sending FL401 withdraw application email to local court for case :{} ", caseDetails.getId());

        emailService.send(
            courtEmail,
            EmailTemplateNames.WITHDRAW_AFTER_ISSUED_LOCAL_COURT,
            buildLocalCourtAdminEmailForWithdrawAfterIssued(caseDetails),
            LanguagePreference.english
        );
    }

    public EmailTemplateVars buildLocalCourtAdminEmailForWithdrawAfterIssued(CaseDetails caseDetails) {

        log.info("building email to local court for withdraw after issued for case:{} ", caseDetails.getId());
        caseData = emailService.getCaseData(caseDetails);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

        return CaseWorkerEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .issueDate(caseData.getIssueDate().format(dateTimeFormatter))
            .build();
    }
}
