package uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class AddNewPreamble {

    private final String title;
    private final String description;

    @JsonCreator
    public AddNewPreamble(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
