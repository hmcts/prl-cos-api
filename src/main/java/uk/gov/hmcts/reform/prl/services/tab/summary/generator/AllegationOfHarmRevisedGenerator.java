package uk.gov.hmcts.reform.prl.services.tab.summary.generator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.AllegationOfHarmRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;

@Component
public class AllegationOfHarmRevisedGenerator implements FieldGenerator {

    @Override
    public CaseSummary generate(CaseData caseData) {
        String typeOfHarm = getTypeOfHarm(caseData);
        CaseSummary.CaseSummaryBuilder builder = CaseSummary.builder();
        builder.allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                            .typesOfHarmRevised(typeOfHarm)
                                            .build()).build();
        return builder.build();
    }

    private String getTypeOfHarm(CaseData caseData) {
        List<String> typeOfHarm = new ArrayList<>();
        uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarmRevised allegationOfHarm = caseData.getAllegationOfHarmRevised();
        if (YesOrNo.Yes.equals(allegationOfHarm.getNewAllegationsOfHarmDomesticAbuseYesNo())) {
            typeOfHarm.add("Domestic abuse");
        }
        if (YesOrNo.Yes.equals(allegationOfHarm.getNewAllegationsOfHarmChildAbductionYesNo())) {
            typeOfHarm.add("Child abduction");
        }
        if (YesOrNo.Yes.equals(allegationOfHarm.getNewAllegationsOfHarmChildAbuseYesNo())) {
            typeOfHarm.add("Child abuse");
        }
        if (YesOrNo.Yes.equals(allegationOfHarm.getNewAllegationsOfHarmSubstanceAbuseYesNo())) {
            typeOfHarm.add("Drugs, alcohol or substance abuse");
        }
        if (YesOrNo.Yes.equals(allegationOfHarm.getNewAllegationsOfHarmOtherConcerns())) {
            typeOfHarm.add("Safety or welfare concerns");
        }

        if (typeOfHarm.isEmpty()) {
            return "No Allegations of harm";
        }

        String join = String.join(", ", typeOfHarm);
        return join.substring(0,1).toUpperCase() + join.substring(1).toLowerCase();

    }
}
