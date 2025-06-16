package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ConsentEnum;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourtNavStatementOfTruth {

    @JsonProperty("declaration")
    private List<ConsentEnum> applicantConsent;

    @JsonProperty("signature")
    private String signature;

    @JsonProperty("signatureDate")
    private CourtNavDate date;

    @JsonProperty("signatureFullName")
    private String fullname;

    @JsonProperty("representativeFirmName")
    private String nameOfFirm;

    @JsonProperty("representativePositionHeld")
    private String signOnBehalf;
}
