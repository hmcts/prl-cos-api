package uk.gov.hmcts.reform.prl.models.dto.ccd;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewAdditionalApplicationWrapper {

    private YesOrNo isAdditionalApplicationReviewed;
    private AdditionalApplicationsBundle selectedAdditionalApplicationsBundle;
    private String selectedAdditionalApplicationsId;

}
