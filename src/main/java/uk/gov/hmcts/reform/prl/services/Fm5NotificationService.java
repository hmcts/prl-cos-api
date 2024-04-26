package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.config.templates.Templates.BLANK_FM5_DOCUMENT;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Fm5NotificationService {

    private final DgsService dgsService;
    private final ServiceOfApplicationEmailService serviceOfApplicationEmailService;

    public void checkWhomToSendNudgeEmail() {

    }

    private void sendNudgeEmail(CaseData caseData, String authorization,
                                PartyDetails party,
                                Map<String, Object> dynamicData,
                                String servedParty) {
        List<Document> blankNudgeDocument = new ArrayList<>();
        blankNudgeDocument.add(generateBlankNudgeDocument(caseData, authorization));

        serviceOfApplicationEmailService
            .sendEmailUsingTemplateWithAttachments(
                authorization, party.getSolicitorEmail(),
                blankNudgeDocument,
                SendgridEmailTemplateNames.SOA_NUDGE_REMINDER_SOLICITOR,
                dynamicData,
                servedParty
            );
    }

    private Document generateBlankNudgeDocument(CaseData caseData, String authorisation) {

        String template = BLANK_FM5_DOCUMENT;
        try {
            log.info("generating blank fm5 document : {} for case : {}", template, caseData.getId());
            GeneratedDocumentInfo accessCodeLetter = dgsService.generateDocument(
                authorisation,
                String.valueOf(caseData.getId()),
                template,
                new HashMap<>()
            );

            return Document.builder().documentUrl(accessCodeLetter.getUrl())
                .documentFileName(accessCodeLetter.getDocName()).documentBinaryUrl(accessCodeLetter.getBinaryUrl())
                .documentCreatedOn(new Date())
                .build();
        } catch (Exception e) {
            log.error("*** Blank fm5 document failed for {} :: because of {}", template, e.getMessage());
        }
        return null;
    }
}
