package uk.gov.hmcts.reform.prl.models.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@JsonSerialize(using = CustomEnumSerializer.class)
public class DocumentListForLa {
    @JsonProperty("documentsListForLa")
    private DynamicList documentsListForLa;
}
