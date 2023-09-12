package uk.gov.hmcts.reform.prl.models.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class SoaToLa {

    private final String soaLaEmailAddress;
    private final YesOrNo soaServeC8ToLocalAuthorityYesOrNo;
    @JsonProperty("soaDocumentDynamicListForLa")
    private List<Element<DocumentListForLa>> soaDocumentDynamicListForLa;

    @JsonCreator
    public static SoaToLa getEmpty() {
        return SoaToLa.builder().build();
    }
}
