package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.FmPendingParty;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.CitizenEmailVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;
import uk.gov.hmcts.reform.prl.utils.EmailUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LTR_ENG_C100_FM5;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ENG_STATIC_DOCS_PATH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_ENGLISH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_MULTIPART_FILE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.URL_STRING;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.DASH_BOARD_LINK;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.hasDashboardAccess;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Fm5NotificationService {

    public static final String BLANK_FM5_FILE = "FM5_Blank.pdf";

    private final ServiceOfApplicationEmailService serviceOfApplicationEmailService;
    private final ServiceOfApplicationPostService serviceOfApplicationPostService;
    private final CaseDocumentClient caseDocumentClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final SystemUserService systemUserService;
    private final BulkPrintService bulkPrintService;
    private final DocumentLanguageService documentLanguageService;
    private final DgsService dgsService;

    @Value("${xui.url}")
    private String manageCaseUrl;
    @Value("${citizen.url}")
    private String citizenUrl;

    public void checkFmPendingParties(FmPendingParty fmPendingParty, CaseData caseData, String authorization) {
        List<Element<PartyDetails>> listOfRecipientsOfNudge = new ArrayList<>();
        if (fmPendingParty.equals(FmPendingParty.APPLICANT)) {
            listOfRecipientsOfNudge.addAll(caseData.getApplicants());
            sendFm5ReminderNotification(listOfRecipientsOfNudge, caseData, authorization);
        } else if (fmPendingParty.equals(FmPendingParty.RESPONDENT)) {
            listOfRecipientsOfNudge.addAll(caseData.getRespondents());
            sendFm5ReminderNotification(listOfRecipientsOfNudge, caseData, authorization);
        } else if ((fmPendingParty.equals(FmPendingParty.BOTH))) {
            listOfRecipientsOfNudge.addAll(caseData.getApplicants());
            listOfRecipientsOfNudge.addAll(caseData.getRespondents());
            sendFm5ReminderNotification(listOfRecipientsOfNudge, caseData, authorization);
        }
    }

    private void sendFm5ReminderNotification(List<Element<PartyDetails>> listOfRecipientsOfNudge,
                                             CaseData caseData,
                                             String authorization) {
        listOfRecipientsOfNudge.forEach(party -> {
            //if represented then send reminder to solicitor
            if (isNotEmpty(party.getValue().getSolicitorEmail())) {
                sendFm5ReminderToSolicitor(caseData, party, authorization);
            } else {
                //Not represented, remind citizen LiP
                sendFm5ReminderToCitizen(caseData, party, authorization);
            }
        });
    }

    private void sendFm5ReminderToSolicitor(CaseData caseData,
                                            Element<PartyDetails> party,
                                            String authorization) {
        Map<String, Object> dynamicData = getEmailDynamicData(caseData,
                                                              party.getValue(),
                                                              false);

        serviceOfApplicationEmailService
            .sendEmailUsingTemplateWithAttachments(
                authorization,
                party.getValue().getSolicitorEmail(),
                getBlankFm5Form(authorization),
                SendgridEmailTemplateNames.FM5_REMINDER_APPLICANT_RESPONDENT_SOLICITOR,
                dynamicData,
                SOLICITOR
        );
    }

    private void sendFm5ReminderToCitizen(CaseData caseData,
                                          Element<PartyDetails> party,
                                          String authorization) {
        log.info("Contact pref is {} for party {}", party.getValue().getContactPreferences(), party.getId());
        if (ContactPreferences.digital.equals(party.getValue().getContactPreferences())
            && YesOrNo.Yes.equals(party.getValue().getCanYouProvideEmailAddress())) {
            sendFm5ReminderToLipViaEmail(caseData, party, authorization);
        } else {
            sendFm5ReminderToLipViaPost(caseData, party, authorization);
        }
    }

    private void sendFm5ReminderToLipViaEmail(CaseData caseData,
                                              Element<PartyDetails> party,
                                              String authorization) {
        //if party has access to dashboard then send gov notify email else send grid
        if (hasDashboardAccess(party)) {
            //Send a gov notify email
            serviceOfApplicationEmailService.sendGovNotifyEmail(
                LanguagePreference.getPreferenceLanguage(caseData),
                party.getValue().getEmail(),
                EmailTemplateNames.FM5_REMINDER_APPLICANT_RESPONDENT,
                buildCitizenEmailVars(
                    caseData,
                    party.getValue()
                )
            );
        } else {
            Map<String, Object> dynamicData = getEmailDynamicData(caseData,
                                                                  party.getValue(),
                                                                  true);
            serviceOfApplicationEmailService
                .sendEmailUsingTemplateWithAttachments(
                    authorization,
                    party.getValue().getEmail(),
                    getBlankFm5Form(authorization),
                    SendgridEmailTemplateNames.FM5_REMINDER_APPLICANT_RESPONDENT,
                    dynamicData,
                    SOA_CITIZEN
            );
        }
    }

    private EmailTemplateVars buildCitizenEmailVars(CaseData caseData,
                                                    PartyDetails party) {
        return CitizenEmailVars.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .partyName(party.getLabelForDynamicList())
            .caseLink(citizenUrl)
            .build();
    }

    private void sendFm5ReminderToLipViaPost(CaseData caseData,
                                             Element<PartyDetails> party,
                                             String authorization) {

        if (isNotEmpty(party.getValue().getAddress())
            && isNotEmpty(party.getValue().getAddress().getAddressLine1())) {
            //generate cover sheets & add to documents
            List<Document> documents = new ArrayList<>(generateCoverSheets(authorization, caseData, party.getValue()));
            //generate LTR-FM5 letter & add to documents
            documents.add(generateFm5CoverLetter(authorization, caseData, party));
            //get blank fm5 form & add to documents
            documents.addAll(getBlankFm5Form(authorization));

            UUID bulkPrintId = bulkPrintService.send(
                String.valueOf(caseData.getId()),
                systemUserService.getSysUserToken(),
                "FM5Reminder",
                documents,
                party.getValue().getLabelForDynamicList()
            );
            log.info("FM5 reminder -> Sent Blank FM5 form with cover sheet to LiP {} via bulk print id {}", party.getId(), bulkPrintId);
        } else {
            log.info(
                "Couldn't post letters to party address, as address is null/empty for {}", party.getId());
        }
    }

    private Document generateFm5CoverLetter(String authorisation,
                                            CaseData caseData,
                                            Element<PartyDetails> party) {

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", caseData.getId());
        dataMap.put("serviceUrl", citizenUrl);
        dataMap.put("address", party.getValue().getAddress());
        dataMap.put("name", party.getValue().getLabelForDynamicList());
        dataMap.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        dataMap.put("dashboardAccess", hasDashboardAccess(party) ? YesOrNo.Yes : YesOrNo.No);

        log.info("*** Generating FM5 reminder cover letter ***");
        try {
            GeneratedDocumentInfo fm5CoverLetter = dgsService.generateDocument(
                authorisation,
                String.valueOf(caseData.getId()),
                PRL_LTR_ENG_C100_FM5,
                dataMap
            );
            return Document.builder()
                .documentUrl(fm5CoverLetter.getUrl())
                .documentFileName(fm5CoverLetter.getDocName())
                .documentBinaryUrl(fm5CoverLetter.getBinaryUrl())
                .documentCreatedOn(new Date())
                .build();
        } catch (Exception e) {
            log.error("generate FM5 cover letter failed for {} ",caseData.getId(), e);
        }
        return null;
    }

    private List<Document> getBlankFm5Form(String authorisation) {
        UploadResponse uploadResponse = caseDocumentClient.uploadDocuments(
            authorisation,
            authTokenGenerator.generate(),
            PrlAppsConstants.CASE_TYPE,
            PrlAppsConstants.JURISDICTION,
            List.of(
                new InMemoryMultipartFile(
                    SOA_MULTIPART_FILE,
                    BLANK_FM5_FILE,
                    APPLICATION_PDF_VALUE,
                    DocumentUtils.readBytes(URL_STRING + ENG_STATIC_DOCS_PATH + BLANK_FM5_FILE)
                )
            )
        );
        if (null != uploadResponse) {
            return uploadResponse.getDocuments().stream()
                .map(DocumentUtils::toPrlDocument)
                .toList();
        }
        return Collections.emptyList();
    }

    private List<Document> generateCoverSheets(String authorisation,
                                     CaseData caseData,
                                     PartyDetails party) {
        List<Document> coverSheets = null;
        try {
            coverSheets = serviceOfApplicationPostService.getCoverSheets(
                caseData,
                authorisation,
                party.getAddress(),
                party.getLabelForDynamicList(),
                DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT
            );
        } catch (Exception e) {
            log.error("Error occurred in generating cover sheets", e);
        }
        log.info("Cover sheets generated {}", coverSheets);
        return coverSheets;
    }

    private Map<String, Object> getEmailDynamicData(CaseData caseData,
                                                    PartyDetails party,
                                                    boolean isCitizen) {
        Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
        dynamicData.put(NAME, isCitizen ? party.getLabelForDynamicList() : party.getRepresentativeFullName());
        dynamicData.put(DASH_BOARD_LINK, isCitizen ? citizenUrl
            : manageCaseUrl + PrlAppsConstants.URL_STRING + caseData.getId());
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        dynamicData.put(IS_ENGLISH, documentLanguage.isGenEng());
        dynamicData.put(IS_WELSH, documentLanguage.isGenWelsh());

        return dynamicData;
    }

}
