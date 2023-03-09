package uk.gov.hmcts.reform.prl.models.cafcass.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder(builderMethodName = "caseHearingWith")
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseHearing {

    private Long hearingID;

    private String hearingType;

    private String hearingTypeValue;

    private String hmcStatus;
    private List<HearingDaySchedule> hearingDaySchedule;

}
