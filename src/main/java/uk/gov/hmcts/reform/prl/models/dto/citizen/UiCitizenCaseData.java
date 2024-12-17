package uk.gov.hmcts.reform.prl.models.dto.citizen;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;


@Data

@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuperBuilder(toBuilder = true)
public class UiCitizenCaseData {

    @JsonUnwrapped
    private CaseData caseData;

    /**
     * This is a non-persistent list of documents to send to Citizen frontend.
     */
    @JsonUnwrapped
    private CitizenDocumentsManagement citizenDocumentsManagement;

}
