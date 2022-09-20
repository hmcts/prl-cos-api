package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder(toBuilder = true)
public class CourtNavMetaData {

    @Valid
    @NonNull
    private final String caseOrigin;
    private final boolean courtNavApproved;
    private final boolean hasDraftOrder;
    @Valid
    @NonNull
    @NotBlank
    private final int numberOfAttachments;
    private final String courtSpecialRequirements;

}
