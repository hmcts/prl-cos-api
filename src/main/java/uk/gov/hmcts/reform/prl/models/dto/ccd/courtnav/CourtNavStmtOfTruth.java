package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ConsentEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.SignatureEnum;

import java.util.List;

@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
public class CourtNavStmtOfTruth {
    private final List<ConsentEnum> declaration;
    private final String signature;
    private final SignatureEnum signatureType;
    private final CourtNavDate signatureDate;
    private final String signatureFullName;
    private final String representativeFirmName;
    private final String representativePositionHeld;
}
