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


    public void sendEmailToCafcassAndOtherParties(CaseDetails caseDetails) {

        CaseData caseData = emailService.getCaseData(caseDetails);

        ManageOrders manageOrders  = caseData.getManageOrders();

        List<String> cafcassEmails = manageOrders.getCafcassEmailAddress()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> otherEmails = manageOrders.getOtherEmailAddress()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        cafcassEmails.addAll(otherEmails);

        cafcassEmails.forEach(email ->   emailService.send(
            email,
            EmailTemplateNames.CAFCASS_OTHER,
            buildEmailToCafcassAndOtherParties(caseData),
            LanguagePreference.english
        ));

    }

    public EmailTemplateVars buildEmailToCafcassAndOtherParties(CaseData caseData) {

        String typeOfHearing = " ";

        if (YesOrNo.Yes.equals(caseData.getIsCaseUrgent())) {
            typeOfHearing = URGENT_CASE;
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

        return ManageOrderEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .caseUrgency(typeOfHearing)
            .issueDate(caseData.getIssueDate().format(dateTimeFormatter))
            .familyManNumber(caseData.getFamilymanCaseNumber())
            .orderLink(caseData.getPreviewOrderDoc().getDocumentFileName())
            .build();

    }

}
