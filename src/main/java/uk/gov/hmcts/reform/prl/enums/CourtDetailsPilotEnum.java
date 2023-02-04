package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CourtDetailsPilotEnum {

    swanseaCountyCourt("family.swansea.countycourt@justice.gov.uk", "Swansea Family Court", "344",
                       "Swansea Civil Justice Centre", "QUAY WEST, QUAY PARADE", "SA1 1SP", "234946"),
    southamptonCountyCourt("family.southampton.countycourt@justice.gov.uk", "Southampton Family Court", "328",
                           "Southampton Combined Court Centre", "THE COURTS OF JUSTICE, LONDON ROAD", "SO15 2XQ", "43104"),
    exeterCountyCourt("family.exeter.countycourt@justice.gov.uk", "Exeter Family Court", "198",
                      "Exeter Combined Court Centre", "SOUTHERNHAY GARDENS, EXETER", "EX1 1UH", "735217"),
    gloucesterCountyCourt("family.gloucester.countycourt@justice.gov.uk", "Gloucester Family Court", "203",
                          "Gloucestershire Family and Civil Court", "KIMBROSE WAY, GLOUCESTER DOCKS", "GL1 2DE", "198592"),
    porttalbotCountyCourt("family.porttalbot.countycourt@justice.gov.uk", "Port Talbot Justice Centre", "3357",
                          "Port Talbot Justice Centre", "HARBOURSIDE ROAD", "SA13 1SB", "846055"),
    llanelliCountyCourt("family.llanelli.countycourt@justice.gov.uk", "Llanelli Law Courts", "253",
                        "Llanelli Law Courts", "TOWN HALL SQUARE, LLANELLI", "SA15 3AW", "390932"),
    aberystwythCountyCourt("family.aberystwyth.countycourt@justice.gov.uk", "Aberystwyth Justice Centre", "102",
                           "Aberystwyth Justice Centre", "TREFECHAN", "SY23 1AS", "827534"),
    haverfordwestCountyCourt("family.haverfordwest.countycourt@justice.gov.uk", "Haverfordwest County Court and Family Court", "217",
                             "Haverfordwest County and Family", "PENFFYNNON, HAWTHORN RISE", "SA61 2AZ", "700596"),
    eastlondonfamilycourt("eastlondonfamilypr@justice.gov.uk","East London Family Court","121",
                          "East London Family Court", "WESTFERRY CIRCUS (WESTFERRY HOUSE), PART GROUND, 6TH AND 7TH FLOORS, 11 WESTFERRY CIRCUS,"
                              + " CANARY WHARF, LONDON, E14 4HE", "E14 4HD", "898213"),
    kingstonuponhullCountyCourt("hull.private.filing@justice.gov.uk","Kingston upon Hull County Court","239",
                                "Kingston upon Thames County Court and Family Court", "Hearing Centre St James Road"
                                    + "Kingston-upon-Thames", "KT1 2AD", "239"),
    newcastlecivilandfamilycourtsandTribunalCentre("newcastle.c100applications@justice.gov.uk",
                                                   "Newcastle Civil and Family Courts and Tribunal Centre","278",
                                                   "Newcastle Civil & Family Courts and Tribunals Centre", "BARRAS BRIDGE, NEWCASTLE-UPON-TYNE",
                                                   "NE99 1NA", "366796"),
    medwayCountyCourtandFamilyCourt("KentPRL@justice.gov.uk","Medway County Court and Family Court","267",
                                    "Medway County Court and Family Court", "47-67 HIGH STREET CHATHAM KENT", "ME4 4DW", "487294");

    private final String courtEmail;
    private final String courtName;
    private final String courtCode;
    private final String siteName;
    private final String courtAddress;
    private final String postcode;
    private final String courtEpimmsId;

}
