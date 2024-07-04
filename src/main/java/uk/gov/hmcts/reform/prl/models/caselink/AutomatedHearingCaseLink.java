package uk.gov.hmcts.reform.prl.models.caselink;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.Date;
import java.util.List;

@Data
@Builder(builderMethodName = "automatedHearingCaseLinkWith")
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class AutomatedHearingCaseLink {
    @JsonProperty("CaseType")
    public String caseType;

    @JsonProperty("CaseReference")
    public String caseReference;

    @JsonProperty("ReasonForLink")
    public List<Element<AutomatedHearingCaseLinkReason>> reasonForLink;

    @JsonProperty("CreatedDateTime")
    public Date createdDateTime;
}
