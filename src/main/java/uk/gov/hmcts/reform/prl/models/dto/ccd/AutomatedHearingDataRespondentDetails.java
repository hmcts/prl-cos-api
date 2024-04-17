package uk.gov.hmcts.reform.prl.models.dto.ccd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;

@Data
@Getter
@Setter
@Builder(builderMethodName = "automatedHearingDataRespondentDetails")
@AllArgsConstructor
public class AutomatedHearingDataRespondentDetails {
    private DynamicList respondentHearingChannel1;
    private DynamicList respondentHearingChannel2;
    private DynamicList respondentHearingChannel3;
    private DynamicList respondentHearingChannel4;
    private DynamicList respondentHearingChannel5;

    private DynamicList respondentSolicitorHearingChannel1;
    private DynamicList respondentSolicitorHearingChannel2;
    private DynamicList respondentSolicitorHearingChannel3;
    private DynamicList respondentSolicitorHearingChannel4;
    private DynamicList respondentSolicitorHearingChannel5;

    private String respondentName1;
    private String respondentName2;
    private String respondentName3;
    private String respondentName4;
    private String respondentName5;

    private String respondentSolicitor1;
    private String respondentSolicitor2;
    private String respondentSolicitor3;
    private String respondentSolicitor4;
    private String respondentSolicitor5;
}
