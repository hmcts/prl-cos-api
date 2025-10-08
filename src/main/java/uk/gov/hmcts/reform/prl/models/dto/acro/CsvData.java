package uk.gov.hmcts.reform.prl.models.dto.acro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class CsvData {

    private Long id;
    private String courtEpimsId;
    private String courtTypeId;
    private String courtName;
    private String caseTypeOfApplication;
    private LocalDateTime orderExpiryDate;
    private PartyDetails applicant;
    private PartyDetails respondent;
    private String daApplicantContactInstructions;
    private LocalDateTime dateOrderMade;
}
