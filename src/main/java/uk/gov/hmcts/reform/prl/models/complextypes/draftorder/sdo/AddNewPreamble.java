package uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
public class AddNewPreamble {

    @CCD(label = "Title", searchable = false)
    private final String addNewPreambleTitle;
    @CCD(label = "Description", searchable = false, typeOverride = FieldType.TextArea)
    private final String addNewPreambleDescription;

    @JsonCreator
    public AddNewPreamble(String addNewPreambleTitle, String addNewPreambleDescription) {
        this.addNewPreambleTitle = addNewPreambleTitle;
        this.addNewPreambleDescription = addNewPreambleDescription;
    }
}
