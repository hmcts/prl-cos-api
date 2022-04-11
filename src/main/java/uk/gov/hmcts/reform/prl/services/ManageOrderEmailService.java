package uk.gov.hmcts.reform.prl.services;

import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.ManageOrderEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;

import java.util.*;
import java.util.function.Predicate;
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
        try {
            CaseData caseData = emailService.getCaseData(caseDetails);
            String applicantNames = String.join(", ", getApplicants(caseData).stream()
                .map(element -> element.getFirstName() + " " + element.getLastName())
                .collect(Collectors.toList()));

            Court court = courtLocatorService.getNearestFamilyCourt(caseData);

            return ManageOrderEmail.builder()
                .caseReference(String.valueOf(caseDetails.getId()))
                .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
                .applicantName(applicantNames)
                .courtName(court.getCourtName())
                .caseLink("/caselink12345")
                .courtEmail(courtEmail).build();

        } catch (NotFoundException e) {
            log.info("Cannot send email {}", e.getMessage());
        }
        return null;
    }


    private List<PartyDetails> getApplicants(CaseData caseData) {
        return caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
    }


    private List<String> getEmailAddress(List<Element<PartyDetails>> partyDetails) {
        List<String> emails= partyDetails
            .stream()
            .map(Element::getValue).filter(partyDetails1 -> true).map(element -> element.getEmail())
            .collect(Collectors.toList());


       emails.removeIf(Objects::isNull);

       return emails;
    }


}
