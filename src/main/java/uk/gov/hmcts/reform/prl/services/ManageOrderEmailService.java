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
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.DeliveryByEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherOrganisationOptions;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ServeOtherPartiesOptions;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaCitizenServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaSolicitorServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.EmailInformation;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.PostalInformation;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.BulkPrintOrderDetail;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.ManageOrderEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.ManageOrderEmailLip;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.RespondentSolicitorEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailConfig;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.EmailUtils;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AM_LOWER_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AM_UPPER_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_TIME_PATTERN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.NO;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ORDER_COLLECTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PM_LOWER_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PM_UPPER_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.hasDashboardAccess;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings({"java:S3776", "java:S6204"})
public class ManageOrderEmailService {
    public static final String NEW_AND_FINAL = "newAndFinal";
    public static final String FINAL = "final";
    public static final String NEW = "new";
    public static final String MULTIPLE_ORDERS = "multipleOrders";
    public static final String ORDERS = "#Orders";
    public static final String NAME = "name";
    public static final String DASH_BOARD_LINK = "dashBoardLink";

    @Value("${uk.gov.notify.email.application.email-id}")
    private String courtEmail;
    @Value("${xui.url}")
    private String manageCaseUrl;
    @Value("${citizen.url}")
    private String citizenDashboardUrl;

    private static final String URL_STRING = "/";
    private static final String URGENT_CASE = "Urgent ";
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String ORDER_TYPE = "OrderPack";

    private final EmailService emailService;
    private final ServiceOfApplicationPostService serviceOfApplicationPostService;
    private final BulkPrintService bulkPrintService;
    private final SendgridService sendgridService;
    private final Time dateTime;
    private final DocumentLanguageService documentLanguageService;
    public static final String ENGLISH_EMAIL = "english";
    public static final String WELSH_EMAIL = "welsh";

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
                .toList();
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
            .orderLink(manageCaseUrl + URL_STRING + caseData.getId() + ORDERS)
            .build();
    }

    private EmailTemplateVars buildEmailTemplateVarsApplicantLip(Map<String, Object> dynamicData) {

        boolean isFinalOrderFlag = dynamicData.get(FINAL).equals(true);
        boolean multipleOrderFlag = dynamicData.get(MULTIPLE_ORDERS).equals(true);
        boolean newAndFinalOrderFlag = dynamicData.get(NEW_AND_FINAL).equals(true);

        return ManageOrderEmailLip
            .builder()
            .emailSubject(buildEmailTitleForPersonalServiceApplicantLip(isFinalOrderFlag, multipleOrderFlag, newAndFinalOrderFlag))
            .emailText(buildEmailTextForPersonalServiceApplicantLip(isFinalOrderFlag, multipleOrderFlag, newAndFinalOrderFlag))
            .orders(multipleOrderFlag || newAndFinalOrderFlag ? "yes" : "no")
            .order(!multipleOrderFlag || !newAndFinalOrderFlag ? "yes" : "no")
            .caseName(String.valueOf(dynamicData.get("caseName")))
            .applicantName(String.valueOf(dynamicData.get("name")))
            .caseLink(String.valueOf(dynamicData.get(DASH_BOARD_LINK)))
            .caseReference(String.valueOf(dynamicData.get("caseReference")))
            .build();
    }

    private String buildEmailTitleForPersonalServiceApplicantLip(boolean isFinalOrderFlag, boolean multipleOrderFlag, boolean newAndFinalOrderFLag) {

        if (newAndFinalOrderFLag) {
            return "New and final court orders issued";
        } else if (multipleOrderFlag) {
            if (isFinalOrderFlag) {
                return "Final court orders issued";
            } else {
                return "New court orders issued";
            }
        } else {
            if (isFinalOrderFlag) {
                return "Final court order issued";
            } else {
                return "New court order issued";
            }
        }
    }

    private String buildEmailTextForPersonalServiceApplicantLip(boolean isFinalOrderFlag, boolean multipleOrderFlag, boolean newAndFinalOrderFLag) {

        if (newAndFinalOrderFLag) {
            return """
                There are new and final court orders for your case.
                
                An order tells you what the court has decided or what will happen next.
                
                A final order tells you what the court’s final decision is.""";
        } else if (multipleOrderFlag) {
            if (isFinalOrderFlag) {
                return """
                    There are final court orders for your case.
                    
                    A final order tells you what the court’s final decision is.""";
            } else {
                return """
                There are new court orders for your case.
                
                An order tells you what the court has decided or what will happen next.""";
            }
        } else {
            if (isFinalOrderFlag) {
                return """
                    There is a final court order for your case.
                    
                    A final order tells you what the court’s final decision is.""";
            } else {
                return """
                    There is a new court order for your case.
                    
                    A final order tells you what the court’s final decision is.""";
            }
        }
    }

    public void sendEmailWhenOrderIsServed(String authorisation,
                                           CaseData caseData,
                                           Map<String, Object> caseDataMap) {
        List<EmailInformation> otherOrganisationEmailList = new ArrayList<>();
        List<PostalInformation> otherOrganisationPostList = new ArrayList<>();
        ManageOrders manageOrders = caseData.getManageOrders();
        final String caseTypeofApplication = CaseUtils.getCaseTypeOfApplication(caseData);
        List<Element<BulkPrintOrderDetail>> bulkPrintOrderDetails = new ArrayList<>();
        List<Document> orderDocuments = getServedOrderDocumentsAndAdditionalDocuments(caseData);
        log.info("inside SendEmailWhenOrderIsServed**");
        Map<String,Object> dynamicDataForEmail = getDynamicDataForEmail(caseData);
        if (caseTypeofApplication.equalsIgnoreCase(PrlAppsConstants.C100_CASE_TYPE)) {
            if (YesOrNo.No.equals(manageOrders.getServeToRespondentOptions())) {
                log.info("*** CA non personal service email notifications ***");
                handleNonPersonalServiceNotifications(
                    authorisation,
                    caseData,
                    manageOrders,
                    bulkPrintOrderDetails,
                    orderDocuments,
                    dynamicDataForEmail
                );
            } else if (YesOrNo.Yes.equals(manageOrders.getServeToRespondentOptions())) {
                log.info("*** CA personal service notifications ***");
                log.info("*** CA Personal service option selected {}",manageOrders.getServingRespondentsOptionsCA());
                String servingRespondentsOptions = NO.equals(manageOrders.getDisplayLegalRepOption())
                    ? manageOrders.getServingOptionsForNonLegalRep().getId() : manageOrders.getServingRespondentsOptionsCA().getId();
                handleC100PersonalServiceNotifications(authorisation, caseData, orderDocuments, dynamicDataForEmail,
                                                       servingRespondentsOptions,
                                                       bulkPrintOrderDetails
                );
            }
            //PRL-4225 - send order & additional docs to other people via post only
            if (isNotEmpty(manageOrders.getOtherParties())) {
                serveOrderToOtherPersons(authorisation,
                                         manageOrders.getOtherParties(), caseData, orderDocuments, bulkPrintOrderDetails
                );
            }
            //Send email notification to Cafcass or Cafcass cymru based on selection
            String cafcassCymruEmailId = getCafcassCymruEmail(manageOrders);
            if (cafcassCymruEmailId != null) {
                sendEmailToCafcassCymru(caseData,cafcassCymruEmailId,authorisation, orderDocuments);
            }
            //get email and postal information for other organisations.
            if (manageOrders.getServeOtherPartiesCA() != null && manageOrders.getServeOtherPartiesCA()
                .contains(OtherOrganisationOptions.anotherOrganisation)) {
                manageOrders.getServeOrgDetailsList().stream().map(Element::getValue).forEach(value -> {
                    if (DeliveryByEnum.email.equals(value.getServeByPostOrEmail())) {
                        otherOrganisationEmailList.add(value.getEmailInformation());
                    } else {
                        otherOrganisationPostList.add(value.getPostalInformation());
                    }
                });
            }
        } else if (caseTypeofApplication.equalsIgnoreCase(PrlAppsConstants.FL401_CASE_TYPE)) {
            handleFL401ServeOrderNotifications(authorisation, caseData, orderDocuments, dynamicDataForEmail,
                                               bulkPrintOrderDetails, otherOrganisationEmailList, otherOrganisationPostList);
        }
        // Send email notification to other organisations
        if (!otherOrganisationEmailList.isEmpty()) {
            sendEmailToOtherOrganisation(otherOrganisationEmailList, authorisation, orderDocuments, dynamicDataForEmail);
        }
        // Send post to other organisations
        if (!otherOrganisationPostList.isEmpty()) {
            serveOrdersToOtherOrganisation(caseData, authorisation, orderDocuments, bulkPrintOrderDetails, otherOrganisationPostList);
        }

        //PRL-4225 - set bulkIds in the orderCollection & update in caseDataMap
        addBulkPrintIdsInOrderCollection(caseData, bulkPrintOrderDetails);
        caseDataMap.put(ORDER_COLLECTION, caseData.getOrderCollection());
    }

    private void handleFL401ServeOrderNotifications(String authorisation,
                                                    CaseData caseData,
                                                    List<Document> orderDocuments,
                                                    Map<String, Object> dynamicDataForEmail,
                                                    List<Element<BulkPrintOrderDetail>> bulkPrintOrderDetails,
                                                    List<EmailInformation> otherOrganisationEmailList,
                                                    List<PostalInformation> otherOrganisationPostList) {
        ManageOrders manageOrders = caseData.getManageOrders();
        log.info("*** DA Personal service represented serving option selected {}",manageOrders.getServingRespondentsOptionsDA());
        log.info("*** DA Personal service unrepresented serving option selected {}", manageOrders.getServingOptionsForNonLegalRep());
        String servingOptions = NO.equals(manageOrders.getDisplayLegalRepOption())
            ? manageOrders.getServingOptionsForNonLegalRep().getId() : manageOrders.getServingRespondentsOptionsDA().getId();

        handleFL401PersonalServiceNotifications(authorisation,
                                                caseData,
                                                orderDocuments,
                                                dynamicDataForEmail,
                                                servingOptions,
                                                bulkPrintOrderDetails);

        if (manageOrders.getServeOtherPartiesDA() != null && manageOrders.getServeOtherPartiesDA()
            .contains(ServeOtherPartiesOptions.other)) {
            manageOrders.getServeOrgDetailsList().stream().map(Element::getValue).forEach(value -> {
                if (DeliveryByEnum.email.equals(value.getServeByPostOrEmail())) {
                    otherOrganisationEmailList.add(value.getEmailInformation());
                } else {
                    otherOrganisationPostList.add(value.getPostalInformation());
                }
            });
        }
    }

    private void handleC100PersonalServiceNotifications(String authorisation, CaseData caseData,
                                                        List<Document> orderDocuments,
                                                        Map<String, Object> dynamicDataForEmail,
                                                        String respondentOption,
                                                        List<Element<BulkPrintOrderDetail>> bulkPrintOrderDetails) {
        if (!SoaCitizenServingRespondentsEnum.unrepresentedApplicant.getId()
            .equals(respondentOption)) {
            nullSafeCollection(caseData.getApplicants()).stream().findFirst().ifPresent(party -> {
                dynamicDataForEmail.put("name", party.getValue().getRepresentativeFullName());
                sendPersonalServiceNotifications(
                    party,
                    respondentOption,
                    authorisation,
                    orderDocuments,
                    dynamicDataForEmail,
                    caseData,
                    bulkPrintOrderDetails
                );
            });
        } else {
            log.info("*** Send email/post notifications to applicants ***");
            caseData.getApplicants().forEach(party -> sendNotificationsToParty(
                caseData,
                party,
                authorisation,
                dynamicDataForEmail,
                orderDocuments,
                bulkPrintOrderDetails,
                SendgridEmailTemplateNames.SERVE_ORDER_CA_PERSONAL_APPLICANT_LIP
            ));
        }
    }

    private void sendNotificationsToParty(CaseData caseData,
                                          Element<PartyDetails> party,
                                          String authorisation,
                                          Map<String, Object> dynamicDataForEmail,
                                          List<Document> orderDocuments,
                                          List<Element<BulkPrintOrderDetail>> bulkPrintOrderDetails,
                                          SendgridEmailTemplateNames sengGridTemplate) {
        log.debug("=== Party contact preference ==== {}", party.getValue().getContactPreferences());
        if (ContactPreferences.email.equals(party.getValue().getContactPreferences())
            && isPartyProvidedWithEmail(party.getValue())) {
            dynamicDataForEmail.put("name", party.getValue().getLabelForDynamicList());
            dynamicDataForEmail.put(DASH_BOARD_LINK, citizenDashboardUrl);

            log.info("*** Party has dashboard access {}", hasDashboardAccess(party));
            if (hasDashboardAccess(party)) {
                log.info("*** Send email to party using notify.gov");
                emailService.send(
                    party.getValue().getEmail(),
                    EmailTemplateNames.CA_APPLICANT_LIP_ORDERS,
                    buildEmailTemplateVarsApplicantLip(dynamicDataForEmail),
                    LanguagePreference.english
                );
            } else {
                log.info("*** Send orders to party via email using send grid {}", party.getId());
                sendEmailViaSendGrid(
                    authorisation,
                    orderDocuments,
                    dynamicDataForEmail,
                    party.getValue().getEmail(),
                    sengGridTemplate
                );
            }
        } else {
            log.info("*** Send orders to party via post using bulk print {}", party.getId());
            sendOrdersToPartyAddressViaPost(
                caseData,
                authorisation,
                orderDocuments,
                bulkPrintOrderDetails,
                party
            );
        }
    }

    private void handleFL401PersonalServiceNotifications(String authorisation,
                                                         CaseData caseData,
                                                         List<Document> orderDocuments,
                                                         Map<String, Object> dynamicDataForEmail,
                                                         String servingOptions,
                                                         List<Element<BulkPrintOrderDetail>> bulkPrintOrderDetails) {
        log.info("*** DA Personal service serving option selected {}", servingOptions);
        //represented applicant options - applicantLegalRepresentative, courtAdmin and courtBailiff
        if (!SoaCitizenServingRespondentsEnum.unrepresentedApplicant.getId().equals(servingOptions)) {
            log.info("===== DA Serving represented applicant ====");
            dynamicDataForEmail.put("name", caseData.getApplicantsFL401().getRepresentativeFullName());
            sendPersonalServiceNotifications(
                element(caseData.getApplicantsFL401().getPartyId(),
                        caseData.getApplicantsFL401()),
                servingOptions,
                authorisation,
                orderDocuments,
                dynamicDataForEmail,
                caseData,
                bulkPrintOrderDetails
            );
        } else {
            //PRL-5206 unrepresented applicant option - unrepresentedApplicant
            log.info("===== DA Serving unrepresented applicant ====");
            sendNotificationsToParty(
                caseData,
                element(caseData.getApplicantsFL401().getPartyId(),
                        caseData.getApplicantsFL401()),
                authorisation,
                dynamicDataForEmail,
                orderDocuments,
                bulkPrintOrderDetails,
                SendgridEmailTemplateNames.SERVE_ORDER_DA_PERSONAL_APPLICANT_LIP
            );
        }
    }

    private void sendPersonalServiceNotifications(Element<PartyDetails> partyElement,
                                                  String respondentOption,
                                                  String authorisation,
                                                  List<Document> orderDocuments,
                                                  Map<String, Object> dynamicDataForEmail,
                                                  CaseData caseData,
                                                  List<Element<BulkPrintOrderDetail>> bulkPrintOrderDetails) {
        log.info("CA personal service email notifications: sendPersonalServiceNotifications: {}",respondentOption);
        PartyDetails party = partyElement.getValue();
        if (SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative.getId().equals(respondentOption)
            && isNotEmpty(party.getSolicitorEmail())) {
            log.info("*** applicantLegalRepresentative: Sending email to applicant LR ");
            sendEmailViaSendGrid(authorisation, orderDocuments, dynamicDataForEmail, party.getSolicitorEmail(),
                                 SendgridEmailTemplateNames.SERVE_ORDER_PERSONAL_APPLICANT_SOLICITOR
            );
        } else if ((SoaSolicitorServingRespondentsEnum.courtAdmin.getId().equals(respondentOption)
            || SoaSolicitorServingRespondentsEnum.courtBailiff.getId().equals(respondentOption))) {
            //PRL-5365, PRL-5556 - send email/post notifications to all C100 applicants
            if (CaseUtils.isCaseCreatedByCitizen(caseData)
                && C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                log.info("*** courtAdmin/courtBailiff: Send email/post notifications to all C100 applicants");
                caseData.getApplicants().forEach(applicant -> sendNotificationsToParty(
                    caseData,
                    applicant,
                    authorisation,
                    dynamicDataForEmail,
                    orderDocuments,
                    bulkPrintOrderDetails,
                    SendgridEmailTemplateNames.SERVE_ORDER_APPLICANT_RESPONDENT
                ));
            } else if (isNotEmpty(party.getSolicitorEmail())) {
                log.info("*** courtAdmin/courtBailiff: Sending email to applicant LR");
                sendEmailViaSendGrid(authorisation, orderDocuments, dynamicDataForEmail, party.getSolicitorEmail(),
                                     SendgridEmailTemplateNames.SERVE_ORDER_NON_PERSONAL_SOLLICITOR
                );
            } else {
                log.info("*** courtAdmin/courtBailiff: Sending email/post to FL401 applicant LiP");
                sendNotificationsToParty(
                    caseData,
                    partyElement,
                    authorisation,
                    dynamicDataForEmail,
                    orderDocuments,
                    bulkPrintOrderDetails,
                    SendgridEmailTemplateNames.SERVE_ORDER_APPLICANT_RESPONDENT
                );
            }
        }
    }

    private void sendEmailViaSendGrid(String authorisation,
                                      List<Document> orderDocuments,
                                      Map<String, Object> dynamicDataForEmail,
                                      String emailAddress,
                                      SendgridEmailTemplateNames sendgridEmailTemplateName) {
        try {
            sendgridService.sendEmailUsingTemplateWithAttachments(
                sendgridEmailTemplateName,
                authorisation,
                SendgridEmailConfig.builder()
                    .toEmailAddress(emailAddress)
                    .dynamicTemplateData(dynamicDataForEmail)
                    .listOfAttachments(orderDocuments)
                    .languagePreference(LanguagePreference.english)
                    .build()
            );
        } catch (IOException e) {
            log.error("There is a failure in sending send grid email with exception {}",
                      e.getMessage(), e);
        }
    }

    private void sendEmailToCafcassCymru(CaseData caseData, String cafcassCymruEmailId,
                                         String authorisation, List<Document> orderDocuments) {

        Map<String, Object> dynamicData = getDynamicDataForEmail(caseData);
        dynamicData.put(DASH_BOARD_LINK, manageCaseUrl + "/" + caseData.getId() + ORDERS);
        try {
            sendgridService.sendEmailUsingTemplateWithAttachments(
                SendgridEmailTemplateNames.SERVE_ORDER_CAFCASS_CYMRU,
                authorisation,
                SendgridEmailConfig.builder().toEmailAddress(
                    cafcassCymruEmailId).dynamicTemplateData(
                    dynamicData).listOfAttachments(
                    orderDocuments).languagePreference(LanguagePreference.english).build()
            );
        } catch (IOException e) {
            log.error("there is a failure in sending email for email {} with exception {}",
                      cafcassCymruEmailId, e.getMessage(), e);
        }

    }

    private void handleNonPersonalServiceNotifications(String authorisation, CaseData caseData, ManageOrders manageOrders,
                                                       List<Element<BulkPrintOrderDetail>> bulkPrintOrderDetails,
                                                       List<Document> orderDocuments, Map<String, Object> dynamicDataForEmail) {
        DynamicMultiSelectList recipientsOptions = manageOrders.getRecipientsOptions();
        if (recipientsOptions != null) {

            //applicants
            sendEmailToSolicitorOrNotifyParties(recipientsOptions.getValue(),
                                                caseData.getApplicants(),
                                                caseData,
                                                authorisation,
                                                dynamicDataForEmail,
                                                bulkPrintOrderDetails,
                                                orderDocuments
            );
            //respondents
            sendEmailToSolicitorOrNotifyParties(recipientsOptions.getValue(),
                                                caseData.getRespondents(),
                                                caseData,
                                                authorisation,
                                                dynamicDataForEmail,
                                                bulkPrintOrderDetails,
                                                orderDocuments
            );
        }
    }

    private void sendEmailToOtherOrganisation(List<EmailInformation> emailInformation,
                                              String authorisation, List<Document> orderDocuments, Map<String, Object> dynamicData) {

        emailInformation.forEach(value ->
             sendEmailViaSendGrid(authorisation,
                                  orderDocuments,
                                  dynamicData,
                                  value.getEmailAddress(),
                                  SendgridEmailTemplateNames.SERVE_ORDER_ANOTHER_ORGANISATION)
        );
    }

    private Map<String, Object> getDynamicDataForEmail(CaseData caseData) {
        Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
        dynamicData.put(DASH_BOARD_LINK, manageCaseUrl + URL_STRING + caseData.getId() + ORDERS);
        if (null != caseData.getManageOrders() && null != caseData.getManageOrders().getServeOrderDynamicList()) {
            List<String> selectedOrderIds = caseData.getManageOrders().getServeOrderDynamicList().getValue()
                .stream().map(DynamicMultiselectListElement::getCode).toList();
            AtomicBoolean newOrdersExists = new AtomicBoolean(false);
            AtomicBoolean finalOrdersExists = new AtomicBoolean(false);
            caseData.getOrderCollection().stream()
                .filter(order -> selectedOrderIds.contains(order.getId().toString()))
                .forEach(order -> {
                    if (StringUtils.equals(
                        order.getValue().getTypeOfOrder(),
                        SelectTypeOfOrderEnum.interim.getDisplayedValue()
                    ) || StringUtils.equals(
                        order.getValue().getTypeOfOrder(),
                        SelectTypeOfOrderEnum.general.getDisplayedValue()
                    )) {
                        log.info("New order is selected to serve {}",order.getId());
                        newOrdersExists.set(true);
                    } else if (StringUtils.equals(
                        order.getValue().getTypeOfOrder(),
                        SelectTypeOfOrderEnum.finl.getDisplayedValue()
                    )) {
                        log.info("Final order is selected to serve {}",order.getId());
                        finalOrdersExists.set(true);
                    }

                });
            setOrderSpecificDynamicFields(dynamicData,newOrdersExists,finalOrdersExists,selectedOrderIds);
            DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
            dynamicData.put(ENGLISH_EMAIL, documentLanguage.isGenEng());
            dynamicData.put(WELSH_EMAIL, documentLanguage.isGenWelsh());
        }
        return dynamicData;
    }

    private void setOrderSpecificDynamicFields(Map<String, Object> dynamicData, AtomicBoolean newOrdersExists,
                                               AtomicBoolean finalOrdersExists, List<String> selectedOrderIds) {
        setTypeOfOrderForEmail(dynamicData, newOrdersExists, finalOrdersExists);
        setMultipleOrdersForEmail(dynamicData, selectedOrderIds);
    }

    private void setMultipleOrdersForEmail(Map<String, Object> dynamicData, List<String> selectedOrderIds) {
        dynamicData.put(MULTIPLE_ORDERS, CollectionUtils.size(selectedOrderIds) > 1);
    }

    private void setTypeOfOrderForEmail(Map<String, Object> dynamicData, AtomicBoolean newOrdersExists, AtomicBoolean finalOrdersExists) {
        dynamicData.put(NEW_AND_FINAL, false);
        dynamicData.put(FINAL, false);
        dynamicData.put(NEW, false);
        if (newOrdersExists.get() && finalOrdersExists.get()) {
            dynamicData.put(NEW_AND_FINAL, true);
        } else if (newOrdersExists.get()) {
            dynamicData.put(NEW, true);
        } else if (finalOrdersExists.get()) {
            dynamicData.put(FINAL, true);
        }
    }

    private void addBulkPrintIdsInOrderCollection(CaseData caseData,
                                                  List<Element<BulkPrintOrderDetail>> bulkPrintOrderDetails) {
        caseData.getManageOrders().getServeOrderDynamicList().getValue()
            .forEach(element -> nullSafeCollection(caseData.getOrderCollection())
                .forEach(orderDetailsElement -> {
                    if (orderDetailsElement.getId().toString().equals(element.getCode())) {
                        List<Element<BulkPrintOrderDetail>> bulkPrints = CollectionUtils.isNotEmpty(orderDetailsElement.getValue()
                                                                                                        .getBulkPrintOrderDetails())
                            ? orderDetailsElement.getValue().getBulkPrintOrderDetails() : new ArrayList<>();
                        bulkPrints.addAll(bulkPrintOrderDetails);
                        orderDetailsElement.getValue()
                            .setBulkPrintOrderDetails(bulkPrints);
                    }
                }));
    }

    private void serveOrdersToOtherOrganisation(CaseData caseData, String authorisation,
                                                List<Document> orderDocuments, List<Element<BulkPrintOrderDetail>> bulkPrintOrderDetails,
                                                List<PostalInformation> postalInformation) {
        postalInformation.forEach(organisationPostalInfo -> {
            if ((isNotEmpty(organisationPostalInfo.getPostalAddress()))
                && isNotEmpty(organisationPostalInfo.getPostalAddress().getAddressLine1())) {
                try {
                    UUID bulkPrintId = sendOrderDocumentViaPost(caseData, organisationPostalInfo.getPostalAddress(),
                                                                organisationPostalInfo.getPostalName(), authorisation, orderDocuments);
                    log.info("** bulk print id {}", bulkPrintId);
                    //PRL-4225 save bulk print details
                    bulkPrintOrderDetails.add(element(
                        buildBulkPrintOrderDetail(bulkPrintId, String.valueOf(organisationPostalInfo.hashCode()),
                                                  organisationPostalInfo.getPostalName())));
                } catch (Exception e) {
                    log.error("Error in sending order docs to other person {}", organisationPostalInfo.hashCode());
                    log.error("Exception occurred in sending order docs to other person", e);
                }
            } else {
                log.info("Couldn't send serve order details to other person, address is null/empty for {}",
                         organisationPostalInfo.hashCode());
            }
        });
    }

    private void sendOrdersToPartyAddressViaPost(CaseData caseData,
                                                 String authorisation,
                                                 List<Document> orderDocuments,
                                                 List<Element<BulkPrintOrderDetail>> bulkPrintOrderDetails,
                                                 Element<PartyDetails> partyElement) {
        if ((isNotEmpty(partyElement.getValue())
            && isNotEmpty(partyElement.getValue().getAddress()))
            && isNotEmpty(partyElement.getValue().getAddress().getAddressLine1())) {
            try {
                UUID bulkPrintId = sendOrderDocumentViaPost(caseData, partyElement.getValue().getAddress(),
                                                            partyElement.getValue().getLabelForDynamicList(),
                                                            authorisation, orderDocuments
                );
                //PRL-4225 save bulk print details
                bulkPrintOrderDetails.add(element(
                    buildBulkPrintOrderDetail(
                        bulkPrintId,
                        String.valueOf(partyElement.getId()),
                        partyElement.getValue().getLabelForDynamicList()
                    )));
            } catch (Exception e) {
                log.error("Error in sending orders to party address {}", partyElement.getId());
                log.error("Exception occurred in sending orders to party address", e);
            }
        } else {
            log.info(
                "Couldn't post orders to party address, as address is null/empty for {}",
                partyElement.getId());
        }
    }

    private void serveOrderToOtherPersons(String authorisation,
                                          DynamicMultiSelectList otherParties,
                                          CaseData caseData,
                                          List<Document> orderDocuments,
                                          List<Element<BulkPrintOrderDetail>> bulkPrintOrderDetails) {
        log.info("*** Send orders to other people via post using bulk print ***");
        otherParties.getValue()
            .stream()
            .map(DynamicMultiselectListElement::getCode)
            .forEach(id -> {
                PartyDetails otherPerson = getOtherPerson(id, caseData);
                sendOrdersToPartyAddressViaPost(
                    caseData,
                    authorisation,
                    orderDocuments,
                    bulkPrintOrderDetails,
                    element(UUID.fromString(id), otherPerson)
                );
            });
    }

    private BulkPrintOrderDetail buildBulkPrintOrderDetail(UUID bulkPrintId,
                                                           String id,
                                                           String name) {
        return BulkPrintOrderDetail.builder()
            .bulkPrintId(String.valueOf(bulkPrintId))
            .partyId(id)
            .partyName(name)
            .servedDateTime(dateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN, Locale.ENGLISH))
                                .replace(AM_LOWER_CASE, AM_UPPER_CASE)
                                .replace(PM_LOWER_CASE, PM_UPPER_CASE))
            .build();
    }

    private String getCafcassCymruEmail(ManageOrders manageOrders) {
        String cafcassCymruEmail = null;
        if (YesOrNo.Yes.equals(manageOrders.getCafcassCymruServedOptions())) {
            cafcassCymruEmail = manageOrders.getCafcassCymruEmail();
        }
        return cafcassCymruEmail;
    }

    private PartyDetails getOtherPerson(String id, CaseData caseData) {
        List<Element<PartyDetails>> otherPartiesToNotify = TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
            ? caseData.getOtherPartyInTheCaseRevised()
            : caseData.getOthersToNotify();
        if (null != otherPartiesToNotify) {
            Optional<Element<PartyDetails>> otherPerson = otherPartiesToNotify.stream()
                .filter(element -> element.getId().toString().equalsIgnoreCase(id))
                .findFirst();
            if (otherPerson.isPresent()) {
                return otherPerson.get().getValue();
            }
        }
        return null;
    }

    private void sendEmailToSolicitorOrNotifyParties(List<DynamicMultiselectListElement> value,
                                                     List<Element<PartyDetails>> partyDetails,
                                                     CaseData caseData,
                                                     String authorisation,
                                                     Map<String, Object> dynamicDataForEmail,
                                                     List<Element<BulkPrintOrderDetail>> bulkPrintOrderDetails,
                                                     List<Document> orderDocuments) {
        value.forEach(element -> {
            Optional<Element<PartyDetails>> partyDataOptional = partyDetails.stream()
                .filter(party -> party.getId().toString().equalsIgnoreCase(element.getCode())).findFirst();
            if (partyDataOptional.isPresent()) {
                PartyDetails partyData = partyDataOptional.get().getValue();
                if (isSolicitorEmailExists(partyData)) {
                    dynamicDataForEmail.put(NAME, partyData.getRepresentativeFullName());
                    sendEmailViaSendGrid(
                        authorisation,
                        orderDocuments,
                        dynamicDataForEmail,
                        partyData.getSolicitorEmail(),
                        SendgridEmailTemplateNames.SERVE_ORDER_NON_PERSONAL_SOLLICITOR
                    );
                } else {
                    log.info("*** Send email/post notifications to parties ***");
                    sendNotificationsToParty(
                        caseData,
                        partyDataOptional.get(),
                        authorisation,
                        dynamicDataForEmail,
                        orderDocuments,
                        bulkPrintOrderDetails,
                        SendgridEmailTemplateNames.SERVE_ORDER_APPLICANT_RESPONDENT
                    );
                }
            }
        });
    }

    private UUID sendOrderDocumentViaPost(CaseData caseData,
                                          Address address,
                                          String name,
                                          String authorisation,
                                          List<Document> orderDocuments) throws Exception {
        List<Document> documents = new ArrayList<>();
        //generate cover letter
        List<Document> coverLetterDocs = serviceOfApplicationPostService.getCoverSheets(
            caseData,
            authorisation,
            address,
            name,
            DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT
        );
        if (CollectionUtils.isNotEmpty(coverLetterDocs)) {
            documents.addAll(coverLetterDocs);
        }

        //cover should be the first doc in the list, append all order docs
        documents.addAll(orderDocuments);

        return bulkPrintService.send(
            String.valueOf(caseData.getId()),
            authorisation,
            ORDER_TYPE,
            documents,
            name
        );
    }

    private static List<Document> getServedOrderDocumentsAndAdditionalDocuments(CaseData caseData) {
        List<Document> orderDocuments = new ArrayList<>();
        if (null != caseData.getManageOrders() && null != caseData.getManageOrders().getServeOrderDynamicList()) {
            List<String> selectedOrderIds = caseData.getManageOrders().getServeOrderDynamicList().getValue()
                .stream().map(DynamicMultiselectListElement::getCode).toList();
            caseData.getOrderCollection().stream()
                .filter(order -> selectedOrderIds.contains(order.getId().toString()))
                .forEach(order -> {
                    if (isNotEmpty(order.getValue().getOrderDocument())) {
                        orderDocuments.add(order.getValue().getOrderDocument());
                    }
                    if (isNotEmpty(order.getValue().getOrderDocumentWelsh())) {
                        orderDocuments.add(order.getValue().getOrderDocumentWelsh());
                    }
                });
            //PRL-5621 - Fix not to duplicate additional document in case of multiple orders served together
            if (CollectionUtils.isNotEmpty(caseData.getManageOrders().getServeOrderAdditionalDocuments())) {
                caseData.getManageOrders().getServeOrderAdditionalDocuments().forEach(
                    additionalDocumentEl -> orderDocuments.add(additionalDocumentEl.getValue()));
            }
        }
        return orderDocuments;
    }

    private boolean isPartyProvidedWithEmail(PartyDetails party) {
        return YesOrNo.Yes.equals(party.getCanYouProvideEmailAddress());
    }

    private boolean isSolicitorEmailExists(PartyDetails party) {
        return StringUtils.isNotEmpty(party.getSolicitorEmail());
    }

    public void sendEmailToLegalRepresentativeOnRejection(CaseDetails caseDetails, DraftOrder draftOrder) {
        CaseData caseData = emailService.getCaseData(caseDetails);
        EmailTemplateVars emailTemplateVars = ManageOrderEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .fullName(draftOrder.getOtherDetails().getOrderCreatedBy())
            .orderLink(manageCaseUrl + "/" + caseData.getId())
            .instructions(caseData.getManageOrders().getInstructionsToLegalRepresentative())
            .build();
        emailService.send(
            draftOrder.getOtherDetails().getOrderCreatedByEmailId(),
            EmailTemplateNames.EMAIL_TO_LEGAL_REP_JUDGE_REJECTED_ORDER,
            emailTemplateVars,
            LanguagePreference.english
        );
    }

}
