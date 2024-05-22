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
public class RespondentC8Document {

    @JsonProperty("respondentAc8Documents")
    private List<Element<ResponseDocuments>> respondentAc8Documents;
    @JsonProperty("respondentBc8Documents")
    private List<Element<ResponseDocuments>> respondentBc8Documents;
    @JsonProperty("respondentCc8Documents")
    private List<Element<ResponseDocuments>> respondentCc8Documents;
    @JsonProperty("respondentDc8Documents")
    private List<Element<ResponseDocuments>> respondentDc8Documents;
    @JsonProperty("respondentEc8Documents")
    private List<Element<ResponseDocuments>> respondentEc8Documents;

}
