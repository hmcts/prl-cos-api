package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.framework.exceptions.DocumentGenerationException;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.ServedParties;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.documents.DocumentResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.DocumentCategory;
import uk.gov.hmcts.reform.prl.models.dto.citizen.DocumentRequest;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import static org.apache.logging.log4j.util.Strings.isNotBlank;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_UPDATE;
import static uk.gov.hmcts.reform.prl.utils.DocumentUtils.addCitizenQuarantineFields;
import static uk.gov.hmcts.reform.prl.utils.DocumentUtils.getExistingCitizenQuarantineDocuments;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseDocumentService {

    @Autowired
    private final DocumentGenService documentGenService;

    @Autowired
    private final CaseService caseService;

    @Autowired
    private final ObjectMapper objectMapper;

    @Autowired
    private final AuthTokenGenerator authTokenGenerator;

    private static final Date localZoneDate = Date.from(ZonedDateTime.now(ZoneId.of(LONDON_TIME_ZONE)).toInstant());

    public DocumentResponse generateAndUploadDocument(String authorisation, DocumentRequest documentRequest) throws DocumentGenerationException {
        return documentGenService.generateDocument(authorisation, documentRequest);
    }

    public DocumentResponse uploadDocument(String authorisation, DocumentRequest documentRequest) throws IOException {
        return documentGenService.uploadDocument(authorisation, documentRequest.getFile());
    }

    public CaseDetails submitCitizenDocuments(String authorisation, DocumentRequest documentRequest) throws JsonProcessingException {
        log.info("upload and move citizen documents to quarantine {}", documentRequest);
        //Get case data from caseId
        String caseId = documentRequest.getCaseId();
        CaseData caseData = CaseUtils.getCaseData(caseService.getCase(authorisation, caseId), objectMapper);

        if (null == caseData) {
            log.info("Retrieved caseData is null for caseId {}", caseId);
            return null;
        }

        List<Element<QuarantineLegalDoc>> citizenQuarantineDocs = getExistingCitizenQuarantineDocuments(caseData);

        if (isNotBlank(documentRequest.getCategoryId())
            && CollectionUtils.isNotEmpty(documentRequest.getDocuments())) {

            DocumentCategory category = DocumentCategory.getValue(documentRequest.getCategoryId());
            ServedParties servedParties = ServedParties.builder()
                .partyId(documentRequest.getPartyId())
                .partyName(documentRequest.getPartyName())
                .build();

            //move all documents to citizen quarantine
            for (Document document : documentRequest.getDocuments()) {
                QuarantineLegalDoc quarantineLegalDoc = getCitizenQuarantineDocument(document);
                quarantineLegalDoc = addCitizenQuarantineFields(quarantineLegalDoc,
                                                                documentRequest.getPartyType(),
                                                                category.getCategoryId(),
                                                                category.getDisplayedValue(),
                                                                documentRequest.getRestrictDocumentDetails(),
                                                                servedParties);
                //add to citizen quarantine list
                citizenQuarantineDocs.add(element(quarantineLegalDoc));
            }

            //update caseData with quarantine list
            caseData = caseData.toBuilder().citizenQuarantineDocsList(citizenQuarantineDocs).build();
            return caseService.updateCase(caseData, authorisation, authTokenGenerator.generate(), caseId, CITIZEN_CASE_UPDATE.getValue(), null);

        }
        return null;
    }

    private static QuarantineLegalDoc getCitizenQuarantineDocument(Document document) {
        return QuarantineLegalDoc.builder()
            .citizenQuarantineDocument(document.toBuilder()
                                           .documentCreatedOn(localZoneDate).build())
            .build();
    }
}
