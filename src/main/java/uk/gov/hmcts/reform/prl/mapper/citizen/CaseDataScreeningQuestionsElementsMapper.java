package uk.gov.hmcts.reform.prl.mapper.citizen;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildScreeningQuestionsElements;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;


public class CaseDataScreeningQuestionsElementsMapper {

    public static final String NONE = "none";

    private CaseDataScreeningQuestionsElementsMapper() {
    }

    public static void updateScreeningQuestionsElementsForCaseData(CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                                                   C100RebuildScreeningQuestionsElements c100RebuildScreeningQuestionsElements) {

        caseDataBuilder
            .orderDetailsForPermissions(StringUtils.isNotEmpty(c100RebuildScreeningQuestionsElements.getSqCourtOrderPreventSubfield())
                                            ? c100RebuildScreeningQuestionsElements.getSqCourtOrderPreventSubfield() : null)
            .uploadOrderDocForPermission(isNotEmpty(c100RebuildScreeningQuestionsElements.getSqUploadDocumentSubfield())
                                ? buildDocument(c100RebuildScreeningQuestionsElements.getSqUploadDocumentSubfield()) : null);
    }

    static Document buildDocument(uk.gov.hmcts.reform.prl.models.c100rebuild.Document document) {
        if (isNotEmpty(document)) {
            return Document.builder()
                .documentUrl(document.getUrl())
                .documentBinaryUrl(document.getBinaryUrl())
                .documentFileName(document.getFilename())
                .build();
        }
        return null;
    }
}
