package uk.gov.hmcts.reform.prl.models.dto.ccd;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoveDraftOrderFields {

    //Amend Draft order
    private String removeDraftOrderText;

    private Object removeDraftOrdersDynamicList;
}
