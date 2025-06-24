package uk.gov.hmcts.reform.prl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.BulkPrintService;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationPostService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangeContentProvider;
import uk.gov.hmcts.reform.prl.services.pin.C100CaseInviteService;
import uk.gov.hmcts.reform.prl.services.pin.FL401CaseInviteService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfChangeEventHandler {
    public static final String PRL_LEGAL_REP_COVER_LETTER_TEMPLATE = "PRL-LEG-REP-REMOVED.docx";
    private final EmailService emailService;
    private final NoticeOfChangeContentProvider noticeOfChangeContentProvider;
    private final LaunchDarklyClient launchDarklyClient;
    private final ServiceOfApplicationService serviceOfApplicationService;
    private final ServiceOfApplicationPostService serviceOfApplicationPostService;
    private final BulkPrintService bulkPrintService;
    private final SystemUserService systemUserService;
    private final C100CaseInviteService c100CaseInviteService;
    private final FL401CaseInviteService fl401CaseInviteService;

    @Async
    @EventListener(condition = "#event.typeOfEvent eq 'Add Legal Representation'")
    public void notifyLegalRepresentative(final NoticeOfChangeEvent event) {
        CaseData caseData = event.getCaseData();
        //PRL-3211 - notify new LR
        sendEmailToSolicitor(caseData, event, EmailTemplateNames.CA_DA_SOLICITOR_NOC);

        //Get LiP
        Element<PartyDetails> partyElement = getLitigantParty(caseData, event);
        //PRL-3211 - notify LiP
        sendEmailToLitigant(caseData, event, EmailTemplateNames.CA_DA_APPLICANT_RESPONDENT_NOC, false, partyElement, "");

        //PRL-3211 - notify applicants/respondents other parties except litigant
        sendEmailToApplicantsRespondents(caseData, event, EmailTemplateNames.CA_DA_OTHER_PARTIES_NOC, false, partyElement);

        //PRL-3211 - notify other persons if any
        sendEmailToOtherParties(caseData, event, EmailTemplateNames.CA_DA_OTHER_PARTIES_NOC, false);

        //PRL-3211 - notify applicants/respondents LRs
        sendEmailToAppRespSolicitors(caseData, event, EmailTemplateNames.CA_DA_OTHER_PARTIES_NOC);
    }

    private void sendEmailToAppRespSolicitors(CaseData caseData, NoticeOfChangeEvent event, EmailTemplateNames emailTemplateNames) {
        Map<String, String> solicitorsToNotify = new HashMap<>();
        solicitorsToNotify.putAll(CaseUtils.getApplicantSolicitorsToNotify(caseData));
        solicitorsToNotify.putAll(CaseUtils.getRespondentSolicitorsToNotify(caseData));
        if (!solicitorsToNotify.isEmpty()) {
            solicitorsToNotify.forEach(
                (key, value) -> {
                    if (!key.equalsIgnoreCase(event.getSolicitorEmailAddress())) {
                        emailService.send(
                            key,
                            emailTemplateNames,
                            noticeOfChangeContentProvider.buildNocEmailSolicitor(caseData, event.getSolicitorName()),
                            LanguagePreference.getPreferenceLanguage(caseData)
                        );
                    }
                });
        }
    }

    private void sendEmailToOtherParties(CaseData caseData, NoticeOfChangeEvent event,
                                         EmailTemplateNames emailTemplateNames, boolean isRemoveLegalRep) {
        Map<String, String> othersToNotify = CaseUtils.getOthersToNotify(caseData);
        if (!othersToNotify.isEmpty()) {
            othersToNotify.forEach(
                (key, value) -> emailService.send(
                    key,
                    emailTemplateNames,
                    noticeOfChangeContentProvider.buildNocEmailCitizen(
                        caseData,
                        event.getSolicitorName(),
                        value,
                        true,
                        isRemoveLegalRep,
                        ""
                    ),
                    LanguagePreference.getPreferenceLanguage(caseData)
                ));
        }
    }

    private void sendEmailToApplicantsRespondents(CaseData caseData,
                                                  NoticeOfChangeEvent event,
                                                  EmailTemplateNames emailTemplateNames,
                                                  boolean isRemoveLegalRep,
                                                  Element<PartyDetails> partyElement) {
        Map<String, String> applicantsRespondentsToNotify = new HashMap<>();
        applicantsRespondentsToNotify.putAll(CaseUtils.getApplicantsToNotify(
            caseData,
            null != partyElement ? partyElement.getId() : null
        ));
        applicantsRespondentsToNotify.putAll(CaseUtils.getRespondentsToNotify(
            caseData,
            null != partyElement ? partyElement.getId() : null
        ));
        if (!applicantsRespondentsToNotify.isEmpty()) {
            applicantsRespondentsToNotify.forEach(
                (key, value) -> emailService.send(
                    key,
                    emailTemplateNames,
                    noticeOfChangeContentProvider.buildNocEmailCitizen(
                        caseData,
                        event.getSolicitorName(),
                        value,
                        false,
                        isRemoveLegalRep,
                        ""
                    ),
                    LanguagePreference.getPreferenceLanguage(caseData)
                ));
        }
    }

    private void sendEmailToLitigant(CaseData caseData,
                                     NoticeOfChangeEvent event,
                                     EmailTemplateNames emailTemplateName,
                                     boolean isRemoveLegalRep,
                                     Element<PartyDetails> partyElement, String accessCode) {
        if (null != partyElement && null != partyElement.getValue()) {
            PartyDetails partyDetails = partyElement.getValue();
            if (null != partyDetails.getEmail()) {
                emailService.send(
                    partyDetails.getEmail(),
                    emailTemplateName,
                    noticeOfChangeContentProvider.buildNocEmailCitizen(caseData, event.getSolicitorName(),
                                                                       partyDetails.getFirstName() + EMPTY_SPACE_STRING + partyDetails.getLastName(),
                                                                       false,
                                                                       isRemoveLegalRep,
                                                                       accessCode
                    ),
                    LanguagePreference.getPreferenceLanguage(caseData)
                );
            } else {
                log.info(
                    "Unable to send email to LiP {} as the they don't have any email address for case id {}",
                    partyElement.getId(),
                    caseData.getId()
                );
            }
        }
    }

    private void sendEmailToSolicitor(CaseData caseData, NoticeOfChangeEvent event, EmailTemplateNames emailTemplateName) {
        if (null != event.getSolicitorEmailAddress()) {
            log.info("Sending solicitor email on case id {}", caseData.getId());
            emailService.send(
                event.getSolicitorEmailAddress(),
                emailTemplateName,
                noticeOfChangeContentProvider.buildNocEmailSolicitor(caseData, event.getSolicitorName()),
                LanguagePreference.getPreferenceLanguage(caseData)
            );
        } else {
            log.info(
                "Unable to send email to Solicitor as the they don't have any email address for case id {}",
                caseData.getId()
            );
        }
    }

    private Element<PartyDetails> getLitigantParty(CaseData caseData, NoticeOfChangeEvent event) {
        int representingPartyIndex = event.getRepresentedPartyIndex();
        Element<PartyDetails> partyDetailsElement = null;
        switch (event.getRepresenting()) {
            case CAAPPLICANT:
                partyDetailsElement = caseData.getApplicants().get(representingPartyIndex);
                break;
            case CARESPONDENT:
                partyDetailsElement = caseData.getRespondents().get(representingPartyIndex);
                break;
            case DAAPPLICANT:
                partyDetailsElement = element(
                    caseData.getApplicantsFL401().getPartyId(),
                    caseData.getApplicantsFL401()
                );
                break;
            case DARESPONDENT:
                partyDetailsElement = element(
                    caseData.getRespondentsFL401().getPartyId(),
                    caseData.getRespondentsFL401()
                );
                break;
            default:
                break;
        }
        return partyDetailsElement;
    }

    /**
     * Fetches the existing access code for this litigant, or generates (and persists)
     * a new invite + code if none exists yet.
     */
    public String fetchOrCreateAccessCode(
        CaseData caseData,
        Element<PartyDetails> partyElement,
        SolicitorRole.Representing representing
    ) {

        if (partyElement == null || partyElement.getId() == null) {
            return null;
        }
        // Check for existing invite
        CaseInvite caseInvite = CaseUtils.getCaseInvite(partyElement.getId(), caseData.getCaseInvites());
        if (caseInvite != null && caseInvite.getAccessCode() != null) {
            log.info("Fetched access code for case id {}", caseData.getId());
            return caseInvite.getAccessCode();
        }

        // need Yes/No based on applicant/respondent
        boolean isApplicant = switch (representing) {
            case CAAPPLICANT, DAAPPLICANT -> true;
            case CARESPONDENT, DARESPONDENT -> false;
            default -> {
                log.error("NOC lip email error unknown representing: {} on caseid {}", representing, caseData.getId());
                throw new IllegalArgumentException(
                    String.format("NOC lip email error unknown representing: %s on caseid %s",
                                  representing, caseData.getId()));
            }
        };

        // choose service
        String caseType = CaseUtils.getCaseTypeOfApplication(caseData);
        CaseInvite generated;
        if (C100_CASE_TYPE.equalsIgnoreCase(caseType)) {
            generated = c100CaseInviteService.generateCaseInvite(
                partyElement,
                isApplicant ? YesOrNo.Yes : YesOrNo.No
            );
        } else {
            generated = fl401CaseInviteService.generateCaseInvite(
                partyElement.getValue(),
                isApplicant ? YesOrNo.Yes : YesOrNo.No
            );
        }
        if (generated == null) {
            log.error("CaseInvite generation returned null for partyId {} on caseId {}",
                      partyElement.getId(), caseData.getId());
            return null;
        }
        // persist it back into caseData
        if (caseData.getCaseInvites() == null) {
            caseData.setCaseInvites(new ArrayList<>());
        }

        caseData.getCaseInvites().add(element(generated));
        log.info("Generated case invite on case {}", caseData.getId());
        return generated.getAccessCode();
    }

    @Async
    @EventListener(condition = "#event.typeOfEvent eq 'Remove Legal Representation'")
    public void notifyWhenLegalRepresentativeRemoved(final NoticeOfChangeEvent event) {
        CaseData caseData = event.getCaseData();
        //PRL-3215 - notify old LR
        sendEmailToSolicitor(caseData, event, EmailTemplateNames.CA_DA_REMOVE_SOLICITOR_NOC);

        //Get LiP
        Element<PartyDetails> partyElement = getLitigantParty(caseData, event);

        String accessCode = fetchOrCreateAccessCode(
            caseData,
            partyElement,
            event.getRepresenting()
        );

        if (null != accessCode) {
            //PRL-5300 - send email/post to LiP based on contact pref
            sendNotificationToLitigant(caseData, event, partyElement, accessCode);
        }
        //PRL-3215 - notify applicants/respondents other parties for the litigants in case
        sendEmailToApplicantsRespondents(caseData, event, EmailTemplateNames.CA_DA_OTHER_PARTIES_REMOVE_NOC, true, partyElement);

        //PRL-3215 - notify other persons if any
        sendEmailToOtherParties(caseData, event, EmailTemplateNames.CA_DA_OTHER_PARTIES_REMOVE_NOC_REVISED, true);

        //PRL-3215 - notify applicants/respondents LRs
        sendEmailToAppRespSolicitors(caseData, event, EmailTemplateNames.CA_DA_OTHER_PARTIES_REMOVE_NOC);
    }


    private void sendNotificationToLitigant(
        CaseData caseData,
        NoticeOfChangeEvent event,
        Element<PartyDetails> party,
        String accessCode
    ) {
        log.info("*** Send notifications to LiP after legal rep is removed ***");
        if (party != null && party.getValue() != null) {
            String lipEmail = party.getValue().getEmail();
            ContactPreferences pref = party.getValue().getContactPreferences();
            boolean hasEmail = lipEmail != null && !lipEmail.isBlank();
            boolean wantsEmail = ContactPreferences.email.equals(pref);

            if (wantsEmail && hasEmail) {
                log.info("Send email to LiP");
                sendEmailToLitigant(
                    caseData,
                    event,
                    EmailTemplateNames.CA_DA_APPLICANT_REMOVE_RESPONDENT_NOC,
                    true,
                    party,
                    accessCode
                );
            } else {
                if (hasEmail && !wantsEmail) {
                    log.info("LiP has an email address but prefers post");
                } else if (!hasEmail && wantsEmail) {
                    log.warn("LiP prefers email but no address—falling back to post");
                }
                log.info("Send post to LiP via bulk print");
                sendPostViaBulkprint(caseData, party, accessCode);
            }
        }
    }


    private void sendPostViaBulkprint(CaseData caseData,
                                      Element<PartyDetails> party,
                                      String accessCode) {
        if (isNotEmpty(party.getValue().getAddress())
            && isNotEmpty(party.getValue().getAddress().getAddressLine1())) {
            List<Document> documents = new ArrayList<>();
            //generate cover sheets & add to documents
            generateCoverSheets(caseData, party.getValue(), documents);
            //generate cover letter with access code & add to documents
            generateCoverLetter(caseData, party, documents, accessCode);

            UUID bulkPrintId = bulkPrintService.send(
                String.valueOf(caseData.getId()),
                systemUserService.getSysUserToken(),
                "CoverLetterWithAccessCode",
                documents,
                party.getValue().getLabelForDynamicList()
            );
            log.info("Remove legal rep -> Sent cover letter with access code to LiP {} via bulk print id {}", party.getId(), bulkPrintId);
        } else {
            log.info(
                "Couldn't post letters to party address, as address is null/empty for {}", party.getId());
        }
    }

    private void generateCoverSheets(CaseData caseData,
                                     PartyDetails party,
                                     List<Document> documents) {
        List<Document> coverSheets = null;
        try {
            coverSheets = serviceOfApplicationPostService.getCoverSheets(
                caseData,
                systemUserService.getSysUserToken(),
                party.getAddress(),
                party.getLabelForDynamicList(),
                DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT
            );
        } catch (Exception e) {
            log.error("Error occurred in generating cover sheets {}", e.getMessage());
        }
        if (CollectionUtils.isNotEmpty(coverSheets)) {
            documents.addAll(coverSheets);
        }
    }

    private void generateCoverLetter(CaseData caseData,
                                     Element<PartyDetails> party,
                                     List<Document> documents, String accessCode) {
        List<Document> coverLetterWithAccessCode = serviceOfApplicationService.generateAccessCodeLetter(
            systemUserService.getSysUserToken(),
            caseData,
            party,
            CaseInvite.builder().accessCode(accessCode).build(),
            PRL_LEGAL_REP_COVER_LETTER_TEMPLATE
            );
        if (isNotEmpty(coverLetterWithAccessCode)) {
            documents.addAll(coverLetterWithAccessCode);
        }
    }
}
