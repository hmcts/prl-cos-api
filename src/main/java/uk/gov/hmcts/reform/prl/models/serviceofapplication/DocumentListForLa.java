package uk.gov.hmcts.reform.prl.models.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class DocumentListForLa {
    @CCD(label = "Select a document", searchable = false, typeOverride = FieldType.DynamicList)
    @JsonProperty("documentsListForLa")
    private DynamicList documentsListForLa;

    @JsonCreator
    public static DocumentListForLa getEmpty() {
        return DocumentListForLa.builder().build();
    }
}
