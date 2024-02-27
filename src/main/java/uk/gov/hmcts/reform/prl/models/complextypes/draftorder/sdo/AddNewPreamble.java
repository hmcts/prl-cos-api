package uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class AddNewPreamble {

    private final String addNewPreambleTitle;
    private final String addNewPreambleDescription;

    @JsonCreator
    public AddNewPreamble(String addNewPreambleTitle, String addNewPreambleDescription) {
        this.addNewPreambleTitle = addNewPreambleTitle;
        this.addNewPreambleDescription = addNewPreambleDescription;
    }
}
