package uk.gov.hmcts.reform.prl.enums;

public enum ManageOrderFieldsSubmittedEnum {
    whatToDoWithOrderSolicitor("whatToDoWithOrderSolicitor"),
    draftOrdersDynamicList("draftOrdersDynamicList"),
    whatToDoWithOrderCourtAdmin("whatToDoWithOrderCourtAdmin");

    private final String value;

    ManageOrderFieldsSubmittedEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
