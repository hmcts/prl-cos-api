package uk.gov.hmcts.reform.prl.models.cafcass.hearing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder(builderMethodName = "hearingsWith")
@NoArgsConstructor
@AllArgsConstructor
public class Hearings {

    private String caseRef;

    private List<CaseHearing> caseHearings;

}
