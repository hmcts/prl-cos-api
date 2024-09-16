package uk.gov.hmcts.reform.prl.enums;

public enum ClosingCaseFieldsEnum {

    childOptionsForFinalDecision("childOptionsForFinalDecision"),
    isTheDecisionAboutAllChildren("isTheDecisionAboutAllChildren"),
    finalOutcomeForChildren("finalOutcomeForChildren");

    private final String value;

    ClosingCaseFieldsEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
