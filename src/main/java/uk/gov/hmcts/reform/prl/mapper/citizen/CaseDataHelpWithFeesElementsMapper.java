package uk.gov.hmcts.reform.prl.mapper.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildHelpWithFeesElements;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;


public class CaseDataHelpWithFeesElementsMapper {

    private CaseDataHelpWithFeesElementsMapper() {
    }

    public static void updateHelpWithFeesDetailsForCaseData(CaseData.CaseDataBuilder<?,?> caseDataBuilder,
                                                            C100RebuildData c100RebuildData) throws JsonProcessingException {
        if (StringUtils.isNotEmpty(c100RebuildData.getC100RebuildHelpWithFeesDetails())) {
            ObjectMapper mapper = new ObjectMapper();
            C100RebuildHelpWithFeesElements c100RebuildHelpWithFeesElements = mapper
                .readValue(
                    c100RebuildData.getC100RebuildHelpWithFeesDetails(),
                    C100RebuildHelpWithFeesElements.class
                );

            caseDataBuilder
                .helpWithFees(YesOrNo.Yes.equals(c100RebuildHelpWithFeesElements.getNeedHelpWithFees())
                                  && YesOrNo.Yes.equals(c100RebuildHelpWithFeesElements.getFeesAppliedDetails())
                                  ? YesOrNo.Yes : YesOrNo.No);
        }
    }
}
