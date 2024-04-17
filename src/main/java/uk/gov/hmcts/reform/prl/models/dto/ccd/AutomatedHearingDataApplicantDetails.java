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
@Builder(builderMethodName = "automatedHearingDataApplicantDetails")
@AllArgsConstructor
public class AutomatedHearingDataApplicantDetails {

    private DynamicList applicantHearingChannel1;
    private DynamicList applicantHearingChannel2;
    private DynamicList applicantHearingChannel3;
    private DynamicList applicantHearingChannel4;
    private DynamicList applicantHearingChannel5;

    private DynamicList applicantSolicitorHearingChannel1;
    private DynamicList applicantSolicitorHearingChannel2;
    private DynamicList applicantSolicitorHearingChannel3;
    private DynamicList applicantSolicitorHearingChannel4;
    private DynamicList applicantSolicitorHearingChannel5;

    private String applicantName1;
    private String applicantName2;
    private String applicantName3;
    private String applicantName4;
    private String applicantName5;

    private String applicantSolicitor1;
    private String applicantSolicitor2;
    private String applicantSolicitor3;
    private String applicantSolicitor4;
    private String applicantSolicitor5;
}
