package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.DocumentCategory;
import uk.gov.hmcts.reform.prl.models.dto.citizen.DocumentRequest;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.apache.logging.log4j.util.Strings.isNotBlank;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_UPDATE;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CitizenDocumentService {

    private final ObjectMapper objectMapper;
    private final AllTabServiceImpl allTabService;
    private final UserService userService;
    private final ManageDocumentsService manageDocumentsService;

    public CaseDetails citizenSubmitDocuments(String authorisation, DocumentRequest documentRequest) {

        String caseId = documentRequest.getCaseId();

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
            = allTabService.getStartUpdateForSpecificEvent(String.valueOf(caseId), CITIZEN_CASE_UPDATE.getValue());
        Map<String, Object> updatedCaseDataMap = startAllTabsUpdateDataContent.caseDataMap();
        CaseData updatedCaseData = startAllTabsUpdateDataContent.caseData();

        UserDetails userDetails = userService.getUserDetails(authorisation);

        if (isNotBlank(documentRequest.getCategoryId())
            && CollectionUtils.isNotEmpty(documentRequest.getDocuments())) {
            DocumentCategory category = DocumentCategory.getValue(documentRequest.getCategoryId());

            List<QuarantineLegalDoc> quarantineLegalDocs = documentRequest.getDocuments().stream()
                .map(document -> getCitizenQuarantineDocument(
                    document,
                    documentRequest,
                    category,
                    userDetails
                ))
                .toList();

            if (DocumentCategory.FM5_STATEMENTS.equals(category)) {
                for (QuarantineLegalDoc quarantineLegalDoc : quarantineLegalDocs) {
                    String userRole = CaseUtils.getUserRole(userDetails);
                    manageDocumentsService.moveDocumentsToRespectiveCategoriesNew(
                        quarantineLegalDoc,
                        userDetails,
                        updatedCaseData,
                        updatedCaseDataMap,
                        userRole
                    );
                }
            } else {
                //move all documents to citizen quarantine except fm5 documents
                manageDocumentsService.setFlagsForWaTask(
                    updatedCaseData,
                    updatedCaseDataMap,
                    CITIZEN,
                    quarantineLegalDocs.get(0)
                );

                moveCitizenDocumentsToQuarantineTab(
                    quarantineLegalDocs,
                    updatedCaseDataMap
                );
            }
            //update all tabs
            return allTabService.submitAllTabsUpdate(
                startAllTabsUpdateDataContent.authorisation(),
                caseId,
                startAllTabsUpdateDataContent.startEventResponse(),
                startAllTabsUpdateDataContent.eventRequestData(),
                updatedCaseDataMap
            );

        }
        return null;
    }

    private void moveCitizenDocumentsToQuarantineTab(List<QuarantineLegalDoc> quarantineLegalDocs,
                                                     Map<String, Object> caseDataMap) {
        for (QuarantineLegalDoc quarantineLegalDoc : quarantineLegalDocs) {
            //PRL-6208 - fixed document missing issue for multiple docs
            CaseData caseData = objectMapper.convertValue(caseDataMap, CaseData.class);
            //invoke common manage docs
            manageDocumentsService.moveDocumentsToQuarantineTab(
                quarantineLegalDoc,
                caseData,
                caseDataMap,
                CITIZEN
            );
        }
    }

    private QuarantineLegalDoc getCitizenQuarantineDocument(Document document,
                                                            DocumentRequest documentRequest,
                                                            DocumentCategory category,
                                                            UserDetails userDetails) {
        return QuarantineLegalDoc.builder()
            .citizenQuarantineDocument(document.toBuilder()
                                           .documentCreatedOn(Date.from(ZonedDateTime.now(ZoneId.of(LONDON_TIME_ZONE)).toInstant()))
                                           .build())
            .documentParty(documentRequest.getPartyType())
            .documentUploadedDate(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)))
            .categoryId(category.getCategoryId())
            .categoryName(category.getDisplayedValue())
            .isConfidential(!DocumentCategory.FM5_STATEMENTS.equals(category)
                                && YesOrNo.No.equals(documentRequest.getIsConfidential())
                                && YesOrNo.No.equals(documentRequest.getIsRestricted())
                                ? YesOrNo.Yes : documentRequest.getIsConfidential())
            .isRestricted(documentRequest.getIsRestricted())
            .restrictedDetails(documentRequest.getRestrictDocumentDetails())
            .uploadedBy(documentRequest.getPartyName())
            .uploadedByIdamId(null != userDetails ? userDetails.getId() : null)
            .uploaderRole(CITIZEN)
            .build();
    }
}
