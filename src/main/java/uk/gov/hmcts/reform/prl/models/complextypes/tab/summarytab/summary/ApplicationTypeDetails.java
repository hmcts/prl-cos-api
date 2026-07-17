package uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Builder
@Data
public class ApplicationTypeDetails {

    @CCD(label = "Type of application", searchable = false, typeOverride = FieldType.TextArea)
    private final String typesOfApplication;

}
