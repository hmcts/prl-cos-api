package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourtNavAddress {

    private String addressLine1;

    private String addressLine2;

    private String addressLine3;

    private String postTown;

    @Size(max = 14)
    @Pattern(regexp = "^[A-Z0-9 ]+$", message = "Invalid postcode format")
    private String postCode;

    private String county;

    private String country;
}
