package uk.gov.hmcts.reform.prl.mapper.citizen;

import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildHelpWithFeesElements;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;


public class CaseDataHelpWithFeesElementsMapper {

    private CaseDataHelpWithFeesElementsMapper() {
    }

    public static void updateHelpWithFeesDetailsForCaseData(CaseData.CaseDataBuilder<?,?> caseDataBuilder,
                                                            C100RebuildHelpWithFeesElements c100RebuildHelpWithFeesElements) {
        caseDataBuilder
                .helpWithFees(YesOrNo.Yes.equals(c100RebuildHelpWithFeesElements.getNeedHelpWithFees())
                                  && YesOrNo.Yes.equals(c100RebuildHelpWithFeesElements.getFeesAppliedDetails())
                ? YesOrNo.Yes : YesOrNo.No);
    }
}
