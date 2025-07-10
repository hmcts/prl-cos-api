package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class C8ArchiveService {

    private final ObjectMapper objectMapper;
    private final ConfidentialDetailsChangeHelper confidentialDetailsChangeHelper;

    public void archiveC8DocumentIfConfidentialChanged(CallbackRequest callbackRequest,
                                                       CaseData caseData,
                                                       Map<String, Object> caseDataUpdated) {
        CaseData caseDataBefore = CaseUtils.getCaseData(callbackRequest.getCaseDetailsBefore(), objectMapper);
        boolean confidentialDetailsChanged = confidentialDetailsChangeHelper.haveConfidentialDetailsChanged(caseData, caseDataBefore);

        if (confidentialDetailsChanged) {
            Document c8ToArchive = caseData.getC8Document();

            if (c8ToArchive != null) {
                Document archivedC8 = Document.builder()
                    .documentUrl(c8ToArchive.getDocumentUrl())
                    .documentBinaryUrl(c8ToArchive.getDocumentBinaryUrl())
                    .documentFileName("C8ArchivedDocument.pdf")
                    .build();

                List<Element<Document>> archivedDocuments = new ArrayList<>();

                if (caseData.getC8ArchivedDocuments() != null) {
                    archivedDocuments.addAll(caseData.getC8ArchivedDocuments());
                }

                archivedDocuments.add(Element.<Document>builder()
                                          .value(archivedC8)
                                          .build());

                caseDataUpdated.put("c8ArchivedDocuments", archivedDocuments);
            }
        }
    }
}
