package uk.gov.hmcts.reform.prl.mapper.citizen;

import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildHelpWithFeesElements;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;


public class CaseDataHelpWithFeesElementsMapper {

    private CaseDataHelpWithFeesElementsMapper() {
    }

    public static void updateHelpWithFeesDetailsForCaseData(CaseData.CaseDataBuilder<?,?> caseDataBuilder,
                                                            C100RebuildHelpWithFeesElements c100RebuildHelpWithFeesElements) {
        caseDataBuilder
                .helpWithFees(c100RebuildHelpWithFeesElements.getNeedHelpWithFees())
                .helpWithFeesNumber(c100RebuildHelpWithFeesElements.getHelpWithFeesReferenceNumber());
    }
}
