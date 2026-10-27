package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DfjArea {
    // Early Adopters
    SWANSEA("SWANSEA", "swanseaDFJCourt", true),
    HUMBERSIDE("HUMBERSIDE", "humbersideDFJCourt", false),
    ESSEX_AND_SUFFOLK("ESSEX_AND_SUFFOLK", "essexAndSuffolkDFJCourt", false),
    WOLVERHAMPTON("WOLVERHAMPTON", "wolverhamptonDFJCourt", true),
    // Tranche 1
    CARDIFF("CARDIFF", "cardiffDFJCourt", true),
    NORTH_WALES("NORTH_WALES", "northWalesDFJCourt", true),
    WEST_MIDLANDS("WEST_MIDLANDS", "westMidlandsDFJCourt", true),
    WEST_LONDON("WEST_LONDON", "westLondonDFJCourt", false),
    KENT("KENT", "kentDFJCourt", false),
    PORTSMOUTH("PORTSMOUTH", "portsmouthDFJCourt", true),
    BOURNEMOUTH("BOURNEMOUTH", "bournemouthDFJCourt", true),
    SHEFFIELD("SHEFFIELD", "sheffieldDFJCourt", true),
    NOTTINGHAM("NOTTINGHAM", "nottinghamDFJCourt", true),
    DERBY("DERBY", "derbyDFJCourt", true);

    private final String name;
    private final String courtField;
    private final boolean pathFinderEnabled;
}
