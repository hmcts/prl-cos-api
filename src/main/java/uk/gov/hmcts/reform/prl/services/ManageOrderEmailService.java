package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.ManageOrderEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;


@Service
@Slf4j
@RequiredArgsConstructor
public class ManageOrderEmailService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private CourtFinderService courtLocatorService;

    @Value("${uk.gov.notify.email.application.email-id}")
    private String courtEmail;

    @Value("${xui.url}")
    private String manageCaseUrl;

    private static final String URL_STRING = "/";
    private static final String URGENT_CASE = "Urgent ";
    private static final String DATE_FORMAT = "dd-MM-yyyy";


    public void sendEmail(CaseDetails caseDetails) {
        log.info("Sending the manage order emails for caseId {}", caseDetails.getId());
        List<String> emailList = new ArrayList<>();

        CaseData caseData = emailService.getCaseData(caseDetails);

        emailList.addAll(getEmailAddress(caseData.getApplicants()));
        emailList.addAll(getEmailAddress(caseData.getRespondents()));
        emailList.forEach(email -> emailService.send(
            email,
            EmailTemplateNames.SOLICITOR,
            buildEmail(caseDetails),
            LanguagePreference.english
        ));

    }


    private EmailTemplateVars buildEmail(CaseDetails caseDetails) {

        CaseData caseData = emailService.getCaseData(caseDetails);
        String applicantNames = getApplicants(caseData).stream()
            .map(element -> element.getFirstName() + " " + element.getLastName())
            .collect(Collectors.joining(", "));

        return ManageOrderEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .applicantName(applicantNames)
            .courtName(caseData.getCourtName())
            .caseLink(manageCaseUrl + URL_STRING + caseDetails.getId())
            .courtEmail(courtEmail).build();
    }


    private List<PartyDetails> getApplicants(CaseData caseData) {
        return caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
    }


    private List<String> getEmailAddress(List<Element<PartyDetails>> partyDetails) {
        return partyDetails
            .stream()
            .map(Element::getValue)
                .filter(a -> a.getCanYouProvideEmailAddress().equals(YesOrNo.Yes))
            .map(PartyDetails::getEmail)
            .collect(Collectors.toList());
    }


    public void sendEmailToCafcass(CaseDetails caseDetails) {

        log.info("We are about to send an email to cafcass");

        CaseData caseData = emailService.getCaseData(caseDetails);

        ManageOrders manageOrders  = caseData.getManageOrders();

        List<String> cafcassEmails = manageOrders.getCafcassEmailAddress()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> otherEmails = manageOrders.getOtherEmailAddres()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        cafcassEmails.addAll(otherEmails);

        log.info("Cafcass email id {}", cafcassEmails);

        cafcassEmails.forEach(email ->   emailService.send(
            email,
            EmailTemplateNames.CAFCASS,
            buildCafcassEmail(caseData),
            LanguagePreference.english
        ));

        log.info("An email has been sent to cafcass");
    }

    public EmailTemplateVars buildCafcassEmail(CaseData caseData) {

        String typeOfHearing = " ";

        log.info("-----Case urgency: {} =---",caseData.getIsCaseUrgent());
        if (YesOrNo.Yes.equals(caseData.getIsCaseUrgent())) {
            typeOfHearing = URGENT_CASE;
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

        log.info("----- Case issue date: {} -----", caseData.getIssueDate());
        log.info("===== Case Document URL: {} ====", caseData.getPreviewOrderDoc().getDocumentUrl());
        return ManageOrderEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .caseUrgency(typeOfHearing)
            .issueDate(caseData.getIssueDate().format(dateTimeFormatter))
            .familyManNumber(C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
                                 ? caseData.getFamilymanCaseNumber() : caseData.getFl401FamilymanCaseNumber())
            .orderLink(caseData.getPreviewOrderDoc().getDocumentUrl())
            .build();

    }

}
