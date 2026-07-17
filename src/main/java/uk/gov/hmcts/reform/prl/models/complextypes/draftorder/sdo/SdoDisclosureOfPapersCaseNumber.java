package uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
public class SdoDisclosureOfPapersCaseNumber {
    @CCD(label = "Case number", searchable = false)
    @JsonProperty("caseNumber")
    private final String caseNumber;

    @CCD(label = "Court location", searchable = false, typeOverride = FieldType.DynamicList)
    @JsonProperty("sdoDisclosureCourtList")
    private final DynamicList sdoDisclosureCourtList;

    @JsonCreator
    public SdoDisclosureOfPapersCaseNumber(String caseNumber, DynamicList sdoDisclosureCourtList) {
        this.caseNumber = caseNumber;
        this.sdoDisclosureCourtList = sdoDisclosureCourtList;
    }

}
