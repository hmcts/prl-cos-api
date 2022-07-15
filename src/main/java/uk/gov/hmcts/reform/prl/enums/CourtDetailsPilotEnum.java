package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CourtDetailsPilotEnum {

    swanseaCountyCourt("family.swansea.countycourt@justice.gov.uk", "Swansea Family Court", "344"),
    southamptonCountyCourt("family.southampton.countycourt@justice.gov.uk", "Southampton Family Court", "328"),
    exeterCountyCourt("family.exeter.countycourt@justice.gov.uk", "Exeter Family Court", "198"),
    gloucesterCountyCourt("family.gloucester.countycourt@justice.gov.uk", "Gloucester Family Court", "203"),
    porttalbotCountyCourt("family.porttalbot.countycourt@justice.gov.uk", "Port Talbot Justice Centre", "3357"),
    llanelliCountyCourt("family.llanelli.countycourt@justice.gov.uk", "Llanelli Law Courts", "253"),
    aberystwythCountyCourt("family.aberystwyth.countycourt@justice.gov.uk", "Aberystwyth Justice Centre", "102"),
    haverfordwestCountyCourt("family.haverfordwest.countycourt@justice.gov.uk", "Haverfordwest County Court and Family Court", "217"),
    eastlondonfamilycourt("eastlondonfamilypr@justice.gov.uk","East London Family Court","121"),
    kingstonuponhullCountyCourt("hull.private.filing@justice.gov.uk","Kingston upon Hull County Court","239"),
    newcastlecivilandfamilycourtsandTribunalCentre("newcastle.c100applications@justice.gov.uk","Newcastle Civil and Family Courts and Tribunal Centre ","278");

    private final String courtEmail;
    private final String courtName;
    private final String courtCode;

}
