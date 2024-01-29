package uk.gov.hmcts.reform.prl.models.dto.ccd;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespondentTaskLists {
    private String respondentTaskListA;
    private String respondentTaskListB;
    private String respondentTaskListC;
    private String respondentTaskListD;
    private String respondentTaskListE;
}
