package uk.gov.hmcts.reform.prl.enums;

public enum ServeOrderFieldsEnum {

    serveOrderAdditionalDocuments("serveOrderAdditionalDocuments"),
    isOnlyC47aOrderSelectedToServe("isOnlyC47aOrderSelectedToServe"),
    serveOrderDynamicList("serveOrderDynamicList"),
    serveToRespondentOptions("serveToRespondentOptions"),
    servingRespondentsOptionsCA("servingRespondentsOptionsCA"),
    recipientsOptions("recipientsOptions"),
    otherParties("otherParties"),
    cafcassCymruServedOptions("cafcassCymruServedOptions"),
    cafcassCymruEmail("cafcassCymruEmail"),
    cafcassEmailId("cafcassEmailId"),
    serveOtherPartiesCA("serveOtherPartiesCA"),
    deliveryByOptionsCA("deliveryByOptionsCA"),
    postalInformationCA("postalInformationCA"),
    emailInformationCA("emailInformationCA"),
    otherPeoplePresentInCaseFlag("otherPeoplePresentInCaseFlag"),
    servingRespondentsOptionsDA("servingRespondentsOptionsDA"),
    serveOtherPartiesDA("serveOtherPartiesDA"),
    deliveryByOptionsDA("deliveryByOptionsDA"),
    postalInformationDA("postalInformationDA"),
    emailInformationDA("emailInformationDA"),
    serveToRespondentOptionsOnlyC47a("serveToRespondentOptionsOnlyC47a"),
    servingRespondentsOptionsCaOnlyC47a("servingRespondentsOptionsCaOnlyC47a"),
    recipientsOptionsOnlyC47a("recipientsOptionsOnlyC47a"),
    otherPartiesOnlyC47a("otherPartiesOnlyC47a"),
    serveOtherPartiesCaOnlyC47a("serveOtherPartiesCaOnlyC47a"),
    deliveryByOptionsCaOnlyC47a("deliveryByOptionsCaOnlyC47a"),
    postalInformationCaOnlyC47a("postalInformationCaOnlyC47a"),
    emailInformationCaOnlyC47a("emailInformationCaOnlyC47a"),
    isOrderCompleteToServe("isOrderCompleteToServe"),
    doesOrderClosesCase("doesOrderClosesCase");

    private final String value;

    ServeOrderFieldsEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
