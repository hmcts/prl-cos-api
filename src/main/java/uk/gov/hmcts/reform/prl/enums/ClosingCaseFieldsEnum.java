package uk.gov.hmcts.reform.prl.enums;

public enum ClosingCaseFieldsEnum {

    childOptionsForFinalDecision("childOptionsForFinalDecision"),
    isTheDecisionAboutAllChildren("isTheDecisionAboutAllChildren"),
    finalOutcomeForChildren("finalOutcomeForChildren"),
    dateFinalDecisionWasMade("dateFinalDecisionWasMade"),
    finalDecisionDate("finalDecisionDate");

    private final String value;

    ClosingCaseFieldsEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
