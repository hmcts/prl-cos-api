package uk.gov.hmcts.reform.prl.mapper.citizen;

import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildConsentOrderDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataOtherProceedingsElementsMapper.buildDocument;


public class CaseDataConsentOrderDetailsElementsMapper {

    private CaseDataConsentOrderDetailsElementsMapper() {
    }

    public static void updateConsentOrderDetailsForCaseData(CaseData.CaseDataBuilder<?,?> caseDataBuilder,
                                                            C100RebuildConsentOrderDetails c100RebuildConsentOrderDetails) {
        caseDataBuilder
                .consentOrder(nonNull(c100RebuildConsentOrderDetails.getConsentOrderCertificate()) ? Yes : No)
                .draftConsentOrderFile(buildDocument(c100RebuildConsentOrderDetails.getConsentOrderCertificate()));
    }
}
