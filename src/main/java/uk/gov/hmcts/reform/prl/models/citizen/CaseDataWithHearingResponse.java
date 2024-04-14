package uk.gov.hmcts.reform.prl.models.citizen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.CitizenDocumentsManagement;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseDataWithHearingResponse {
    private CaseData caseData;
    private Hearings hearings;
    /**
     * This is a non-persistent list of documents to send to Citizen frontend.
     */
    @JsonUnwrapped
    private CitizenDocumentsManagement citizenDocumentsManagement;
}
