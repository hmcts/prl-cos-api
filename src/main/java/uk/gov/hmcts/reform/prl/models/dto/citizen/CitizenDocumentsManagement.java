package uk.gov.hmcts.reform.prl.models.dto.citizen;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CitizenDocumentsManagement {

    @JsonProperty("citizenDocuments")
    public List<CitizenDocuments> citizenDocuments;

    @JsonProperty("citizenOrders")
    public List<CitizenDocuments> citizenOrders;
}
