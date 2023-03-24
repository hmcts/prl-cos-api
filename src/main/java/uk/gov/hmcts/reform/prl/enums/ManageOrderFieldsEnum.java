package uk.gov.hmcts.reform.prl.enums;

public enum ManageOrderFieldsEnum {

    manageOrdersOptions("manageOrdersOptions"),
    createSelectOrderOptions("createSelectOrderOptions"),
    childArrangementOrders("childArrangementOrders"),
    domesticAbuseOrders("domesticAbuseOrders"),
    fcOrders("fcOrders"),
    otherOrdersOption("otherOrdersOption"),
    amendOrderDynamicList("amendOrderDynamicList"),
    serveOrderDynamicList("serveOrderDynamicList"),
    ordersNeedToBeServed("ordersNeedToBeServed"),
    loggedInUserType("loggedInUserType"),
    doYouWantToServeOrder("doYouWantToServeOrder"),
    whatDoWithOrder("whatDoWithOrder"),
    currentOrderCreatedDateTime("currentOrderCreatedDateTime"),
    approvalDate("approvalDate"),
    previewOrderDoc("previewOrderDoc");

    private final String value;

    ManageOrderFieldsEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
