package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.DeliveryByEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherOrganisationOptions;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ServeOtherPartiesOptions;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrgSolicitors;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.SolicitorUser;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.BulkPrintOrderDetail;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.ManageOrderEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.RespondentSolicitorEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.EmailUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_TIME_PATTERN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({"java:S3776", "java:S6204"})
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

    @Autowired
    private final ServiceOfApplicationService serviceOfApplicationService;
    @Autowired
    private ServiceOfApplicationPostService serviceOfApplicationPostService;

    @Autowired
    private BulkPrintService bulkPrintService;

    private static final String ORDER_TYPE = "OrderPack";

    @Autowired
    private final OrganisationService organisationService;
    @Autowired
    private final SystemUserService systemUserService;
    @Autowired
    private final SendgridService sendgridService;


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
                    sendEmailToPartyOrPartySolicitor(isFinalOrder, appValues.getKey(),
                                                     buildApplicantRespondentEmail(caseData, appValues.getValue()),
                                                     caseData
                    );
                }
            }

            for (Map.Entry<String, String> appValues : respondentMap.entrySet()) {
                if (!StringUtils.isEmpty(appValues.getKey())) {
                    sendEmailToPartyOrPartySolicitor(isFinalOrder, appValues.getKey(),
                                                     buildApplicantRespondentEmail(caseData, appValues.getValue()),
                                                     caseData
                    );
                }
            }
        } else {
            sendEmailForFlCaseType(caseData, isFinalOrder);
        }
    }


    private void sendEmailForFlCaseType(CaseData caseData, SelectTypeOfOrderEnum isFinalOrder) {
        if (!StringUtils.isEmpty(caseData.getApplicantsFL401().getSolicitorEmail())) {
            sendEmailToPartyOrPartySolicitor(isFinalOrder, caseData.getApplicantsFL401().getSolicitorEmail(),
                                             buildApplicantRespondentSolicitorEmail(
                                                 caseData, caseData.getApplicantsFL401().getRepresentativeFirstName()
                                                     + " " + caseData.getApplicantsFL401().getRepresentativeLastName()),
                                             caseData
            );
        }
        if (!StringUtils.isEmpty(caseData.getRespondentsFL401().getEmail())) {
            sendEmailToPartyOrPartySolicitor(isFinalOrder, caseData.getRespondentsFL401().getEmail(),
                                             buildApplicantRespondentEmail(
                                                 caseData,
                                                 caseData.getRespondentsFL401().getFirstName()
                                                     + " " + caseData.getRespondentsFL401().getFirstName()
                                             ),
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

    private void sendEmailToPartyOrPartySolicitor(SelectTypeOfOrderEnum isFinalOrder,
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

    private EmailTemplateVars buildApplicantRespondentEmail(CaseData caseData, String name) {
        return ManageOrderEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .applicantName(name)
            .courtName(caseData.getCourtName())
            .dashboardLink(citizenDashboardUrl)
            .build();
    }


    private EmailTemplateVars buildApplicantRespondentSolicitorEmail(CaseData caseData, String name) {
        return ManageOrderEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .applicantName(name)
            .courtName(caseData.getCourtName())
            .dashboardLink(manageCaseUrl + "/" + caseData.getId() + "#Orders")
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

    public void sendEmailWhenOrderIsServed(String authorisation,
                                           CaseData caseData,
                                           Map<String, Object> caseDataMap) {
        List<String> listOfOtherAndCafcassEmails = new ArrayList<>();
        ManageOrders manageOrders = caseData.getManageOrders();
        String caseTypeofApplication = CaseUtils.getCaseTypeOfApplication(caseData);
        SelectTypeOfOrderEnum isFinalOrder = CaseUtils.getSelectTypeOfOrder(caseData);
        List<Element<BulkPrintOrderDetail>> bulkPrintOrderDetails = new ArrayList<>();

        if (caseTypeofApplication.equalsIgnoreCase(PrlAppsConstants.C100_CASE_TYPE)) {
            List<Document> orderDocuments = getServedOrderDocumentsAndAdditionalDocuments(caseData);
            if (YesOrNo.No.equals(manageOrders.getServeToRespondentOptions())) {
                log.info("** CA case email notifications***");
                //applicants
                sendEmailToApplicantOrSolicitor(manageOrders.getRecipientsOptions().getValue(),
                                            caseData.getApplicants(),
                                            isFinalOrder, caseData);
                //respondents
                sendEmailToSolicitorOrPostToRespondent(manageOrders.getRecipientsOptions().getValue(),
                        caseData.getRespondents(), isFinalOrder, caseData,
                        authorisation, orderDocuments, bulkPrintOrderDetails);
                log.info("*** Bulk print details after respondents {}", bulkPrintOrderDetails);
            }
            if (manageOrders.getServeOtherPartiesCA() != null && manageOrders.getServeOtherPartiesCA()
                .contains(OtherOrganisationOptions.anotherOrganisation)
                && DeliveryByEnum.email.equals(manageOrders.getDeliveryByOptionsCA())) {
                manageOrders.getEmailInformationCA().stream().map(Element::getValue).forEach(value -> listOfOtherAndCafcassEmails
                    .add(value.getEmailAddress()));
            }
            //PRL-4225 - send order & additional docs to other people via post only
            if (null != manageOrders.getOtherParties()) {
                log.info("Inside send order docs to other persons {}", manageOrders.getOtherParties());
                serveOrderToOtherPersons(authorisation,
                        manageOrders.getOtherParties(), caseData, orderDocuments, bulkPrintOrderDetails);
                log.info("### Bulk print details after other persons {}", bulkPrintOrderDetails);
            }
            //Send email notification to Cafcass or Cafcass cymru based on selection
            if (getCafcassEmail(manageOrders) != null) {
                listOfOtherAndCafcassEmails.add(getCafcassEmail(manageOrders));
            }

            //PRL-4225 - set bulkIds in the orderCollection & update in caseDataMap
            addBulkPrintIdsInOrderCollection(caseData, bulkPrintOrderDetails);
            caseDataMap.put("orderCollection", caseData.getOrderCollection());

        } else if (caseTypeofApplication.equalsIgnoreCase(PrlAppsConstants.FL401_CASE_TYPE)) {
            sendEmailForFlCaseType(caseData, isFinalOrder);
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

    private void addBulkPrintIdsInOrderCollection(CaseData caseData,
                                                  List<Element<BulkPrintOrderDetail>> bulkPrintOrderDetails) {
        caseData.getManageOrders().getServeOrderDynamicList().getValue()
                .forEach(element -> nullSafeCollection(caseData.getOrderCollection())
                        .forEach(orderDetailsElement -> {
                            if (orderDetailsElement.getId().toString().equals(element.getCode())) {
                                List<Element<BulkPrintOrderDetail>> bulkPrints = CollectionUtils.isNotEmpty(orderDetailsElement.getValue()
                                        .getBulkPrintOrderDetails()) ? orderDetailsElement.getValue().getBulkPrintOrderDetails() : new ArrayList<>();
                                bulkPrints.addAll(bulkPrintOrderDetails);
                                orderDetailsElement.getValue()
                                        .setBulkPrintOrderDetails(bulkPrints);
                            }
                        }));
    }

    private void serveOrderToOtherPersons(String authorisation,
                                          DynamicMultiSelectList otherParties,
                                          CaseData caseData,
                                          List<Document> orderDocuments,
                                          List<Element<BulkPrintOrderDetail>> bulkPrintOrderDetails) {
        otherParties.getValue()
                .stream()
                .map(DynamicMultiselectListElement::getCode)
                .forEach(id -> {
                    log.info("sending order docs for {}", id);
                    PartyDetails otherPerson = getOtherPerson(id, caseData);
                    if (isNotEmpty(otherPerson) && (isNotEmpty(otherPerson.getAddress())
                            && isNotEmpty(otherPerson.getAddress().getAddressLine1()))) {
                        try {
                            UUID bulkPrintId = sendOrderDocumentViaPost(caseData, otherPerson, authorisation, orderDocuments);
                            //PRL-4225 save bulk print details
                            bulkPrintOrderDetails.add(element(
                                    buildBulkPrintOrderDetail(bulkPrintId, id,
                                            otherPerson.getLabelForDynamicList())));
                        } catch (Exception e) {
                            log.error("Error in sending order docs to other person {}", id);
                            log.error("Exception occurred in sending order docs to other person", e);
                        }
                    } else {
                        log.info("Couldn't send serve order details to other person, address is null/empty for {}", id);
                    }
                });
    }

    private BulkPrintOrderDetail buildBulkPrintOrderDetail(UUID bulkPrintId,
                                                           String id,
                                                           String name) {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN, Locale.ENGLISH));
        log.info("Bulk print serve order date time {}", dateTime);
        return BulkPrintOrderDetail.builder()
                .bulkPrintId(String.valueOf(bulkPrintId))
                .partyId(id)
                .partyName(name)
                .servedDateTime(LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern(DATE_TIME_PATTERN, Locale.UK)))
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

    private PartyDetails getOtherPerson(String id, CaseData caseData) {
        List<Element<PartyDetails>> otherPartiesToNotify = TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
                ? caseData.getOtherPartyInTheCaseRevised()
                : caseData.getOthersToNotify();
        if (null != otherPartiesToNotify) {
            Optional<Element<PartyDetails>> otherPerson = otherPartiesToNotify.stream()
                    .filter(element -> element.getId().toString().equalsIgnoreCase(id))
                    .findFirst();
            if (otherPerson.isPresent() && null != otherPerson.get().getValue().getAddress()) {
                return otherPerson.get().getValue();
            }
        }
        return null;
    }

    private void sendEmailToApplicantOrSolicitor(List<DynamicMultiselectListElement> value,
                                             List<Element<PartyDetails>> partyDetails,
                                             SelectTypeOfOrderEnum isFinalOrder,
                                             CaseData caseData) {
        value.forEach(element -> {
            Optional<Element<PartyDetails>> partyDataOptional = partyDetails.stream()
                .filter(party -> party.getId().toString().equalsIgnoreCase(element.getCode())).findFirst();
            if (partyDataOptional.isPresent()) {
                PartyDetails partyData = partyDataOptional.get().getValue();
                if (isSolicitorEmailExists(partyData)) {
                    sendEmailToPartyOrPartySolicitor(isFinalOrder, partyData.getSolicitorEmail(),
                                                     buildApplicantRespondentSolicitorEmail(
                                                             caseData,
                                                             partyData.getRepresentativeFullName()
                                                     ),
                                                     caseData
                    );
                } else if (isPartyProvidedWithEmail(partyData)) {
                    sendEmailToPartyOrPartySolicitor(isFinalOrder, partyData.getEmail(),
                                                     buildApplicantRespondentEmail(
                                                             caseData,
                                                             partyData.getLabelForDynamicList()
                                                     ),
                                                     caseData
                    );
                }
            }
        });
    }

    private void sendEmailToSolicitorOrPostToRespondent(List<DynamicMultiselectListElement> value,
                                             List<Element<PartyDetails>> partyDetails,
                                             SelectTypeOfOrderEnum isFinalOrder,
                                             CaseData caseData, String authorisation,
                                             List<Document> orderDocuments,
                                             List<Element<BulkPrintOrderDetail>> bulkPrintOrderDetails) {
        value.forEach(element -> {
            Optional<Element<PartyDetails>> partyDataOptional = partyDetails.stream()
                    .filter(party -> party.getId().toString().equalsIgnoreCase(element.getCode())).findFirst();
            if (partyDataOptional.isPresent()) {
                PartyDetails partyData = partyDataOptional.get().getValue();
                if (isSolicitorEmailExists(partyData)) {
                    try {
                        log.info("Trying to send email to {} via send grid service", partyData.getSolicitorEmail());
                        sendgridService.sendEmailWithAttachments(
                            authorisation,
                            EmailUtils.getEmailProps(
                                partyData,
                                caseData.getApplicantCaseName(),
                                String.valueOf(caseData.getId())
                            ),
                            partyData.getSolicitorEmail(),
                            orderDocuments,
                            partyData.getLabelForDynamicList()
                        );
                    } catch (IOException e) {
                        log.error(
                            "Error in sending email to respondent solicitors, {}",
                            partyData.getSolicitorEmail()
                        );
                        log.error("Exception occurred in sending order docs to solicitor via send grid service", e);
                    }
                } else if (ContactPreferences.digital.equals(partyData.getContactPreferences())
                            && isPartyProvidedWithEmail(partyData)) {
                    log.info("Contact preference set as email");
                    sendEmailToPartyOrPartySolicitor(isFinalOrder, partyData.getEmail(),
                            buildApplicantRespondentEmail(caseData,
                                    partyData.getLabelForDynamicList()
                            ),
                            caseData
                    );
                } else {
                    try {
                        if (isNotEmpty(partyData.getAddress()) && isNotEmpty(partyData.getAddress().getAddressLine1())) {
                            UUID bulkPrintId = sendOrderDocumentViaPost(caseData, partyData, authorisation, orderDocuments);
                            //PRL-4225 save bulk print details
                            bulkPrintOrderDetails.add(element(
                                    buildBulkPrintOrderDetail(bulkPrintId, element.getCode(),
                                            partyData.getLabelForDynamicList()))
                            );
                        } else {
                            log.info("Couldn't send serve order details to respondent, address is null/empty for {}", element.getCode());
                        }
                    } catch (Exception e) {
                        log.error("Error in sending order docs to respondent {}", element.getCode());
                        log.error("Exception occurred in sending order docs to respondent", e);
                    }
                }
            }
        });
    }

    public boolean checkIfSolicitorRegistered(PartyDetails partyData) {
        boolean isSolicitorRegistered = false;
        String systemUserToken = systemUserService.getSysUserToken();
        List<Organisations> organisationList = new ArrayList<>();
        organisationList = organisationService.getAllActiveOrganisations(
            systemUserToken);
        log.info("organisationList ==>" + organisationList);
        if (CollectionUtils.isNotEmpty(organisationList)) {
            Optional<SolicitorUser> solicitorDetails = Optional.empty();
            OrgSolicitors orgSolicitors;
            List<String> registeredOrgIds =
                organisationList.stream().map(Organisations::getOrganisationIdentifier).collect(Collectors.toList());
            for (String orgId : registeredOrgIds) {
                orgSolicitors = organisationService.getOrganisationSolicitorDetails(
                    systemUserToken,
                    orgId
                );
                if (null != orgSolicitors
                    && null != orgSolicitors.getUsers()
                    && !orgSolicitors.getUsers().isEmpty()) {
                    solicitorDetails = orgSolicitors.getUsers()
                        .stream()
                        .filter(x -> partyData.getSolicitorEmail().equalsIgnoreCase(
                            x.getEmail()))
                        .findFirst();

                }
                if (solicitorDetails.isPresent()
                    && !solicitorDetails.get().getRoles().isEmpty()
                    && solicitorDetails.get().getRoles().contains(Roles.SOLICITOR.getValue())) {
                    isSolicitorRegistered = true;
                    break;
                }
            }
        }
        return isSolicitorRegistered;
    }

    private UUID sendOrderDocumentViaPost(CaseData caseData,
                                          PartyDetails partyData,
                                          String authorisation,
                                          List<Document> orderDocuments) throws Exception {
        List<Document> documents = new ArrayList<>();
        //generate cover letter
        List<Document> coverLetterDocs = serviceOfApplicationPostService.getCoverLetter(
                caseData,
                authorisation,
                partyData.getAddress(),
                partyData.getLabelForDynamicList()
        );
        if (CollectionUtils.isNotEmpty(coverLetterDocs)) {
            documents.addAll(coverLetterDocs);
        }

        //cover should be the first doc in the list, append all order docs
        documents.addAll(orderDocuments);
        log.info("docs send to bulkPrintService => " + documents);
        log.info("case id => " + caseData.getId());

        return bulkPrintService.send(
                String.valueOf(caseData.getId()),
                authorisation,
                ORDER_TYPE,
                documents,
                partyData.getLabelForDynamicList()
        );
    }

    private static List<Document> getServedOrderDocumentsAndAdditionalDocuments(CaseData caseData) {
        List<Document> orderDocuments = new ArrayList<>();
        log.info("selectedOrderIds ==> " + caseData.getManageOrders().getServeOrderDynamicList());
        if (null != caseData.getManageOrders() && null != caseData.getManageOrders().getServeOrderDynamicList()) {
            List<String> selectedOrderIds = caseData.getManageOrders().getServeOrderDynamicList().getValue()
                .stream().map(DynamicMultiselectListElement::getCode).toList();
            log.info("selectedOrderIds ==> " + selectedOrderIds);
            log.info("caseData.getOrderCollection() ==> " + caseData.getOrderCollection());
            caseData.getOrderCollection().stream()
                .filter(order -> selectedOrderIds.contains(order.getId().toString()))
                .forEach(order -> {
                    if (isNotEmpty(order.getValue().getOrderDocument())) {
                        orderDocuments.add(order.getValue().getOrderDocument());
                    }
                    if (isNotEmpty(order.getValue().getOrderDocumentWelsh())) {
                        orderDocuments.add(order.getValue().getOrderDocumentWelsh());
                    }
                    if (CollectionUtils.isNotEmpty(order.getValue().getServeOrderDetails().getAdditionalDocuments())) {
                        order.getValue().getServeOrderDetails().getAdditionalDocuments().forEach(
                            additionalDocumentEl -> orderDocuments.add(additionalDocumentEl.getValue()));
                    }
                });
        }
        return orderDocuments;
    }

    private boolean isPartyProvidedWithEmail(PartyDetails party) {
        return YesOrNo.Yes.equals(party.getCanYouProvideEmailAddress());
    }

    private boolean isSolicitorEmailExists(PartyDetails party) {
        return StringUtils.isNotEmpty(party.getSolicitorEmail());
    }

}
