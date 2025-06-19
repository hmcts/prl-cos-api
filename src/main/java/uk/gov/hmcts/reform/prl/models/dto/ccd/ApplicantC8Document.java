package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;

import java.util.List;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicantC8Document {

    @JsonProperty("applicantAc8Documents")
    private List<Element<ResponseDocuments>> applicantAc8Documents;
    @JsonProperty("applicantBc8Documents")
    private List<Element<ResponseDocuments>> applicantBc8Documents;
    @JsonProperty("applicantCc8Documents")
    private List<Element<ResponseDocuments>> applicantCc8Documents;
    @JsonProperty("applicantDc8Documents")
    private List<Element<ResponseDocuments>> applicantDc8Documents;
    @JsonProperty("applicantEc8Documents")
    private List<Element<ResponseDocuments>> applicantEc8Documents;

}
