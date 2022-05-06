package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CourtDetailsPilotEnum {

    swanseaCountyCourt("family.swansea.countycourt@justice.gov.uk", "Swansea Family Court", "344"),
    southamptonCountyCourt("family.southampton.countycourt@justice.gov.uk", "Southampton Family Court", "328"),
    exeterCountyCourt("family.exeter.countycourt@justice.gov.uk", "Exeter Family Court", "198"),
    gloucesterCountyCourt("family.gloucester.countycourt@justice.gov.uk", "Gloucester Family Court", "203");

    private final String courtEmail;
    private final String courtName;
    private final String courtCode;

}
