package uk.gov.hmcts.reform.prl.enums;

public enum TestEnum {
    DYNAMIC_ENUM_EXAMPLE {

        @Override
        public String setGetValue(String yourValue) {
            return yourValue;
        }
    };
    public abstract String setGetValue(String value);
}
