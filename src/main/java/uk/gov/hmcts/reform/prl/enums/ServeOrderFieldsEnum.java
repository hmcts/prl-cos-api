package uk.gov.hmcts.reform.prl.enums;

public enum ServeOrderFieldsEnum {

    serveOrderAdditionalDocuments("serveOrderAdditionalDocuments"),
    isCafcass("isCafcass"),
    otherPeoplePresentInCaseFlag("otherPeoplePresentInCaseFlag"),
    isOnlyC47aOrderSelectedToServe("isOnlyC47aOrderSelectedToServe"),
    serveOrderDynamicList("serveOrderDynamicList"),
    serveToRespondentOptions("serveToRespondentOptions"),
    servingRespondentsOptionsCA("servingRespondentsOptionsCA"),
    recipientsOptions("recipientsOptions"),
    otherParties("otherParties"),
    cafcassServedOptions("cafcassServedOptions"),
    cafcassCymruServedOptions("cafcassCymruServedOptions"),
    cafcassCymruEmail("cafcassCymruEmail"),
    cafcassEmailId("cafcassEmailId"),
    serveOtherPartiesCA("serveOtherPartiesCA"),
    deliveryByOptionsCA("deliveryByOptionsCA"),
    postalInformationCA("postalInformationCA"),
    emailInformationCA("emailInformationCA"),
    servingRespondentsOptionsDA("servingRespondentsOptionsDA"),
    serveOtherPartiesDA("serveOtherPartiesDA"),
    deliveryByOptionsDA("deliveryByOptionsDA");

    private final String value;

    ServeOrderFieldsEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
