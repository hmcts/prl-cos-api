package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.DeliveryByEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherOrganisationOptions;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ServeOtherPartiesOptions;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.ManageOrderEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.RespondentSolicitorEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Value("${citizen.url}")
    private String citizenDashboardUrl;

    private static final String URL_STRING = "/";
    private static final String URGENT_CASE = "Urgent ";
    private static final String DATE_FORMAT = "dd-MM-yyyy";


    public void sendEmail(CaseDetails caseDetails) {
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

    public void sendEmailToApplicantAndRespondent(CaseDetails caseDetails) {
        CaseData caseData = emailService.getCaseData(caseDetails);
        SelectTypeOfOrderEnum isFinalOrder = CaseUtils.getSelectTypeOfOrder(caseData);
        String caseTypeofApplication = CaseUtils.getCaseTypeOfApplication(caseData);
        if (caseTypeofApplication.equalsIgnoreCase(PrlAppsConstants.C100_CASE_TYPE)) {
            Map<String, String> applicantsMap = getEmailPartyWithName(caseData
                                                                          .getApplicants());
            Map<String, String> respondentMap = getEmailPartyWithName(caseData
                                                                          .getRespondents());
            for (Map.Entry<String, String> appValues : applicantsMap.entrySet()) {
                if (!StringUtils.isEmpty(appValues.getKey())) {
                    sendEmailToParty(isFinalOrder, appValues.getKey(),
                                     buildApplicantRespondentEmail(caseDetails, appValues.getValue()),
                                     caseData
                    );
                }
            }

            for (Map.Entry<String, String> appValues : respondentMap.entrySet()) {
                if (!StringUtils.isEmpty(appValues.getKey())) {
                    sendEmailToParty(isFinalOrder, appValues.getKey(),
                                     buildApplicantRespondentEmail(caseDetails, appValues.getValue()),
                                     caseData
                    );
                }
            }
        } else {
            sendEmailForFlCaseType(caseDetails, caseData, isFinalOrder);
        }
    }


    private void sendEmailForFlCaseType(CaseDetails caseDetails, CaseData caseData, SelectTypeOfOrderEnum isFinalOrder) {
        if (!StringUtils.isEmpty(caseData.getApplicantsFL401().getSolicitorEmail())) {
            sendEmailToParty(isFinalOrder, caseData.getApplicantsFL401().getSolicitorEmail(),
                             buildApplicantRespondentEmail(
                                 caseDetails, caseData.getApplicantsFL401().getRepresentativeFirstName()
                                     + " " + caseData.getApplicantsFL401().getRepresentativeLastName()),
                             caseData
            );
        }
        if (!StringUtils.isEmpty(caseData.getRespondentsFL401().getEmail())) {
            sendEmailToParty(isFinalOrder, caseData.getRespondentsFL401().getEmail(),
                             buildApplicantRespondentEmail(caseDetails, caseData.getRespondentsFL401().getFirstName()
                                 + " " + caseData.getRespondentsFL401().getFirstName()),
                             caseData
            );
        }
    }

    public void sendFinalOrderIssuedNotification(CaseDetails caseDetails) {
        CaseData caseData = emailService.getCaseData(caseDetails);
        if (State.ALL_FINAL_ORDERS_ISSUED.equals(caseData.getState())) {
            sendNotificationToRespondentSolicitor(caseDetails);
            sendNotificationToRespondent(caseDetails);
        }
    }

    private void sendNotificationToRespondent(CaseDetails caseDetails) {
        CaseData caseData = emailService.getCaseData(caseDetails);
        if (CaseUtils.getCaseTypeOfApplication(caseData).equalsIgnoreCase(PrlAppsConstants.C100_CASE_TYPE)) {
            for (Element<PartyDetails> respondent : caseData.getRespondents()) {
                if (!StringUtils.isEmpty(respondent.getValue().getEmail())) {
                    emailService.send(
                        respondent.getValue().getEmail(),
                        EmailTemplateNames.CA_CITIZEN_RES_NOTIFICATION,
                        buildRespondentEmail(caseDetails, respondent.getValue()),
                        LanguagePreference.english
                    );
                }
            }
            //send notification for applicants
            for (Element<PartyDetails> applicant : caseData.getApplicants()) {
                if (!StringUtils.isEmpty(applicant.getValue().getEmail())) {
                    emailService.send(
                        applicant.getValue().getEmail(),
                        EmailTemplateNames.CA_CITIZEN_RES_NOTIFICATION,
                        buildRespondentEmail(caseDetails, applicant.getValue()),
                        LanguagePreference.english
                    );
                }
            }
        } else {
            if (!StringUtils.isEmpty(caseData.getRespondentsFL401().getEmail())) {
                emailService.send(
                    caseData.getRespondentsFL401().getEmail(),
                    EmailTemplateNames.CA_CITIZEN_RES_NOTIFICATION,
                    buildRespondentEmail(caseDetails, caseData.getRespondentsFL401()),
                    LanguagePreference.english
                );
            }
        }

    }


    private void sendNotificationToRespondentSolicitor(CaseDetails caseDetails) {
        CaseData caseData = emailService.getCaseData(caseDetails);
        if (CaseUtils.getCaseTypeOfApplication(caseData).equalsIgnoreCase(PrlAppsConstants.C100_CASE_TYPE)) {
            for (Map<String, List<String>> resSols : getRespondentSolicitor(caseDetails)) {
                String solicitorEmail = resSols.keySet().toArray()[0].toString();
                if (!StringUtils.isEmpty(solicitorEmail)) {
                    emailService.send(
                        solicitorEmail,
                        EmailTemplateNames.CA_RESPONDENT_SOLICITOR_RES_NOTIFICATION,
                        buildRespondentSolicitorEmail(caseDetails, resSols.get(solicitorEmail).get(0),
                                                      resSols.get(solicitorEmail).get(1)
                        ),
                        LanguagePreference.english
                    );
                }
            }
        }
    }

    private void sendEmailToParty(SelectTypeOfOrderEnum isFinalOrder,
                                  String emailAddress,
                                  EmailTemplateVars email,
                                  CaseData caseData) {
        emailService.send(
            emailAddress,
            (isFinalOrder == SelectTypeOfOrderEnum.finl) ? EmailTemplateNames.CA_DA_FINAL_ORDER_EMAIL
                : EmailTemplateNames.CA_DA_MANAGE_ORDER_EMAIL,
            email,
            LanguagePreference.getPreferenceLanguage(caseData)
        );
    }

    private Map<String, String> getEmailPartyWithName(List<Element<PartyDetails>> party) {
        return party
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toMap(
                PartyDetails::getEmail,
                i -> i.getFirstName() + " " + i.getLastName()
            ));
    }

    private EmailTemplateVars buildApplicantRespondentEmail(CaseDetails caseDetails, String name) {
        CaseData caseData = emailService.getCaseData(caseDetails);

        return ManageOrderEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .applicantName(name)
            .courtName(caseData.getCourtName())
            .dashboardLink(citizenDashboardUrl)
            .build();
    }

    private EmailTemplateVars buildRespondentEmail(CaseDetails caseDetails, PartyDetails partyDetails) {
        CaseData caseData = emailService.getCaseData(caseDetails);
        return ManageOrderEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .applicantName(String.format("%s %s", partyDetails.getFirstName(), partyDetails.getLastName()))
            .dashboardLink(citizenDashboardUrl)
            .build();
    }

    private EmailTemplateVars buildRespondentSolicitorEmail(CaseDetails caseDetails, String solicitorName,
                                                            String respondentName) {
        CaseData caseData = emailService.getCaseData(caseDetails);

        return RespondentSolicitorEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(caseData.getApplicantCaseName())
            .solicitorName(solicitorName)
            .caseLink(manageCaseUrl + URL_STRING + caseDetails.getId())
            .respondentName(respondentName)
            .issueDate(caseData.getIssueDate())
            .build();
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

    private List<Map<String, List<String>>> getRespondentSolicitor(CaseDetails caseDetails) {
        CaseData caseData = emailService.getCaseData(caseDetails);
        return caseData
            .getRespondents()
            .stream()
            .map(Element::getValue)
            .filter(i -> YesNoDontKnow.yes.equals(i.getDoTheyHaveLegalRepresentation()))
            .map(i -> {
                Map<String, List<String>> temp = new HashMap<>();
                temp.put(i.getSolicitorEmail(), List.of(
                    i.getRepresentativeFirstName() + " " + i.getRepresentativeLastName(),
                    i.getFirstName() + " " + i.getLastName()
                ));
                return temp;
            })
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

        ManageOrders manageOrders = caseData.getManageOrders();

        List<String> cafcassEmails = new ArrayList<>();
        List<String> otherEmails = new ArrayList<>();
        if (manageOrders.getCafcassEmailAddress() != null) {
            cafcassEmails = manageOrders.getCafcassEmailAddress()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
        }
        if (manageOrders.getOtherEmailAddress() != null) {
            otherEmails = manageOrders.getOtherEmailAddress()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
        }

        cafcassEmails.addAll(otherEmails);

        cafcassEmails.forEach(email -> emailService.send(
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
            .familyManNumber(caseData.getFamilymanCaseNumber() != null ? caseData.getFamilymanCaseNumber() : "")
            .orderLink(manageCaseUrl + "/" + caseData.getId() + "#Orders")
            .build();
    }

    public void sendEmailWhenOrderIsServed(CaseDetails caseDetails) {
        List<String> listOfOtherAndCafcassEmails = new ArrayList<>();
        CaseData caseData = emailService.getCaseData(caseDetails);
        ManageOrders manageOrders = caseData.getManageOrders();
        String caseTypeofApplication = CaseUtils.getCaseTypeOfApplication(caseData);
        SelectTypeOfOrderEnum isFinalOrder = CaseUtils.getSelectTypeOfOrder(caseData);
        if (caseTypeofApplication.equalsIgnoreCase(PrlAppsConstants.C100_CASE_TYPE)) {
            if (YesOrNo.No.equals(manageOrders.getServeToRespondentOptions())) {
                log.info("** CA case email notifications***");
                sendEmailToPartyOrSolicitor(manageOrders.getRecipientsOptions().getValue(),
                                            caseData.getApplicants(),
                                            isFinalOrder, caseDetails, caseData
                );
                sendEmailToPartyOrSolicitor(manageOrders.getRecipientsOptions().getValue(),
                                            caseData.getRespondents(),
                                            isFinalOrder, caseDetails, caseData
                );
            }
            if (manageOrders.getServeOtherPartiesCA() != null && manageOrders.getServeOtherPartiesCA()
                .contains(OtherOrganisationOptions.anotherOrganisation)
                && DeliveryByEnum.email.equals(manageOrders.getDeliveryByOptionsCA())) {
                manageOrders.getEmailInformationCA().stream().map(Element::getValue).forEach(value -> listOfOtherAndCafcassEmails
                    .add(value.getEmailAddress()));
            }
            //send email notification to other people in the case
            if (null != manageOrders.getOtherParties()) {
                manageOrders.getOtherParties().getValue().stream().map(DynamicMultiselectListElement::getCode).forEach(
                    id -> {
                        String otherEmail = getOtherPeopleEmailAddress(id, caseData);
                        if (null != otherEmail) {
                            listOfOtherAndCafcassEmails.add(otherEmail);
                        }
                    });
            }
            //Send email notification to Cafcass or Cafcass cymru based on selection
            if (getCafcassEmail(manageOrders) != null) {
                listOfOtherAndCafcassEmails.add(getCafcassEmail(manageOrders));
            }
        } else if (caseTypeofApplication.equalsIgnoreCase(PrlAppsConstants.FL401_CASE_TYPE)) {
            sendEmailForFlCaseType(caseDetails, caseData, isFinalOrder);
            if (manageOrders.getServeOtherPartiesDA() != null && manageOrders.getServeOtherPartiesDA()
                .contains(ServeOtherPartiesOptions.other)
                && DeliveryByEnum.email.equals(manageOrders.getDeliveryByOptionsDA())) {
                manageOrders.getEmailInformationDA().stream().map(Element::getValue).forEach(value -> listOfOtherAndCafcassEmails
                    .add(value.getEmailAddress()));
            }
        }

        // Send email notification to other organisations
        listOfOtherAndCafcassEmails.forEach(email ->
                                                emailService.send(
                                                    email,
                                                    EmailTemplateNames.CAFCASS_OTHER,
                                                    buildEmailToCafcassAndOtherParties(caseData),
                                                    LanguagePreference.english
                                                )
        );
    }

    public void sendEmailToC100CitizenParty(CaseDetails caseDetails) {
        CaseData caseData = emailService.getCaseData(caseDetails);
        String typeOfHearing = " ";
        if (YesOrNo.Yes.equals(caseData.getIsCaseUrgent())) {
            typeOfHearing = URGENT_CASE;
        }
        List<PartyDetails> applicants = caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> applicantsEmailList = applicants.stream()
            .map(PartyDetails::getEmail)
            .collect(Collectors.toList());

        if (!applicantsEmailList.isEmpty() && !applicantsEmailList.contains(null)) {
            for (String email : applicantsEmailList) {
                Optional<String> partyName = applicants.stream()
                    .filter(p -> p.getEmail().equals(email))
                    .map(element -> element.getFirstName() + " " + element.getLastName())
                    .findAny();
                emailService.send(
                    email,
                    EmailTemplateNames.PERSONAL_SERVICE,
                    buildC100CitizenApplicantOrRespondentEmail(caseDetails, partyName.orElse(null), typeOfHearing),
                    LanguagePreference.english
                );
            }
        }
        List<PartyDetails> respondents = caseData
            .getRespondents()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> respondentsEmailList = respondents.stream()
            .map(PartyDetails::getEmail)
            .collect(Collectors.toList());

        if (!respondentsEmailList.isEmpty() && !respondentsEmailList.contains(null)) {
            for (String email : respondentsEmailList) {
                Optional<String> partyName = respondents.stream()
                    .filter(p -> p.getEmail().equals(email))
                    .map(element -> element.getFirstName() + " " + element.getLastName())
                    .findAny();
                emailService.send(
                    email,
                    EmailTemplateNames.PERSONAL_SERVICE,
                    buildC100CitizenApplicantOrRespondentEmail(caseDetails, partyName.orElse(null), typeOfHearing),
                    LanguagePreference.english
                );
            }
        }
    }

    private EmailTemplateVars buildC100CitizenApplicantOrRespondentEmail(CaseDetails caseDetails, String partyName, String typeOfHearing) {
        return ManageOrderEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .applicantName(partyName)
            .caseUrgency(typeOfHearing)
            .dashboardLink(citizenDashboardUrl)
            .build();
    }

    private String getCafcassEmail(ManageOrders manageOrders) {
        String cafcassEmail = null;
        if (YesOrNo.Yes.equals(manageOrders.getCafcassCymruServedOptions())) {
            cafcassEmail = manageOrders.getCafcassCymruEmail();
        }
        if (YesOrNo.Yes.equals(manageOrders.getCafcassServedOptions())) {
            cafcassEmail = manageOrders.getCafcassEmailId();
        }
        return cafcassEmail;
    }

    private String getOtherPeopleEmailAddress(String id, CaseData caseData) {
        String other = null;
        if (null != caseData.getOthersToNotify()) {
            Optional<Element<PartyDetails>> otherPerson = caseData.getOthersToNotify().stream()
                .filter(element -> element.getId().toString().equalsIgnoreCase(id)).findFirst();
            if (otherPerson.isPresent() && YesOrNo.Yes.equals(otherPerson.get().getValue().getCanYouProvideEmailAddress())) {
                other = otherPerson.get().getValue().getEmail();
            }
        }
        return other;
    }

    private void sendEmailToPartyOrSolicitor(List<DynamicMultiselectListElement> value,
                                             List<Element<PartyDetails>> partyDetails,
                                             SelectTypeOfOrderEnum isFinalOrder,
                                             CaseDetails caseDetails, CaseData caseData) {
        Map<String, String> partyMap = new HashMap<>();
        value.forEach(element -> {
            Map<String, String> partyMapTemp;
            partyMapTemp = getPartyMap(element.getCode(), partyDetails);
            partyMap.putAll(partyMapTemp);
        });
        for (Map.Entry<String, String> appValues : partyMap.entrySet()) {
            if (!StringUtils.isEmpty(appValues.getKey())) {
                sendEmailToParty(isFinalOrder, appValues.getKey(),
                                 buildApplicantRespondentEmail(caseDetails, appValues.getValue()),
                                 caseData
                );
            }
        }
    }

    private Map<String, String> getPartyMap(String code, List<Element<PartyDetails>> parties) {
        Map<String, String> applicantMap = new HashMap<>();
        Optional<Element<PartyDetails>> applicant = parties.stream()
            .filter(element -> element.getId().toString().equalsIgnoreCase(code)).findFirst();
        if (applicant.isPresent()) {
            if (null != applicant.get().getValue().getSolicitorEmail()) {
                applicantMap.put(applicant.get().getValue().getSolicitorEmail(), applicant.get().getValue()
                    .getRepresentativeFirstName() + " "
                    + applicant.get().getValue().getRepresentativeLastName());
            } else if (YesOrNo.Yes.equals(applicant.get().getValue().getCanYouProvideEmailAddress())) {
                applicantMap.put(applicant.get().getValue().getEmail(), applicant.get().getValue().getFirstName() + " "
                    + applicant.get().getValue().getLastName());
            }
        }
        return applicantMap;
    }

    public void sendEmailToFL401CitizenParty(CaseDetails caseDetails) {
        CaseData caseData = emailService.getCaseData(caseDetails);

        if (null != caseData.getApplicantsFL401().getEmail()) {
            String applicantName = caseData.getApplicantsFL401().getFirstName() + " " + caseData.getApplicantsFL401().getLastName();
            emailService.send(
                caseData.getApplicantsFL401().getEmail(),
                EmailTemplateNames.PERSONAL_SERVICE,
                buildFL401CitizenApplicantOrRespondentEmail(caseDetails, applicantName),
                LanguagePreference.english
            );
        }

        if (null != caseData.getRespondentsFL401().getEmail()) {
            String respondentName = caseData.getRespondentsFL401().getFirstName() + " " + caseData.getRespondentsFL401().getLastName();
            emailService.send(
                caseData.getRespondentsFL401().getEmail(),
                EmailTemplateNames.PERSONAL_SERVICE,
                buildFL401CitizenApplicantOrRespondentEmail(caseDetails, respondentName),
                LanguagePreference.english
            );
        }
    }

    private EmailTemplateVars buildFL401CitizenApplicantOrRespondentEmail(CaseDetails caseDetails, String partyName) {
        return ManageOrderEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .applicantName(partyName)
            .dashboardLink(citizenDashboardUrl)
            .build();
    }
}
