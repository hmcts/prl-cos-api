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
    previewOrderDoc("previewOrderDoc"),
    previewOrderDocWelsh("previewOrderDocWelsh"),
    wasTheOrderApprovedAtHearing("wasTheOrderApprovedAtHearing"),
    judgeOrMagistratesLastName("judgeOrMagistratesLastName"),
    magistrateLastName("magistrateLastName"),
    justiceLegalAdviserFullName("justiceLegalAdviserFullName"),
    dateOrderMade("dateOrderMade"),
    hasJudgeProvidedHearingDetails("hasJudgeProvidedHearingDetails"),
    amendOrderSelectCheckOptions("amendOrderSelectCheckOptions"),
    hearingsType("hearingsType"),
    ordersHearingDetails("ordersHearingDetails"),
    solicitorOrdersHearingDetails("solicitorOrdersHearingDetails"),
    c21OrderOptions("c21OrderOptions"),
    selectChildArrangementsOrder("selectChildArrangementsOrder"),
    childArrangementsOrdersToIssue("childArrangementsOrdersToIssue"),
    childOption("childOption"),
    makeChangesToUploadedOrder("makeChangesToUploadedOrder"),
    editedUploadOrderDoc("editedUploadOrderDoc"),
    previewUploadedOrder("previewUploadedOrder"),
    uploadOrderDoc("uploadOrderDoc"),
    orderUploadedAsDraftFlag("orderUploadedAsDraftFlag"),
    judgeDirectionsToAdmin("judgeDirectionsToAdmin"),
    instructionsFromJudge("instructionsFromJudge"),
    courtAdminNotes("courtAdminNotes"),
    nameOfOrder("nameOfOrder"),
    appointedGuardianName("appointedGuardianName"),
    orderName("orderName"),
    orderType("orderType"),
    otherParties("otherParties"),
    cafcassEmailId("cafcassEmailId"),
    cafcassCymruEmail("cafcassCymruEmail"),
    previewDraftOrder("previewDraftOrder"),
    emailInformationCA("emailInformationCA"),
    deliveryByOptionsCA("deliveryByOptionsCA"),
    isHearingPageNeeded("isHearingPageNeeded"),
    postalInformationCA("postalInformationCA"),
    serveOtherPartiesCA("serveOtherPartiesCA"),
    cafcassServedOptions("cafcassServedOptions"),
    cafcassCymruDocuments("cafcassCymruDocuments"),
    draftOrdersDynamicList("draftOrdersDynamicList"),
    previewDraftOrderWelsh("previewDraftOrderWelsh"),
    whenReportsMustBeFiled("whenReportsMustBeFiled"),
    doYouWantToEditTheOrder("doYouWantToEditTheOrder"),
    serveToRespondentOptions("serveToRespondentOptions"),
    cafcassCymruServedOptions("cafcassCymruServedOptions"),
    isOrderCreatedBySolicitor("isOrderCreatedBySolicitor"),
    servingRespondentsOptionsCA("servingRespondentsOptionsCA"),
    otherPeoplePresentInCaseFlag("otherPeoplePresentInCaseFlag"),
    serveOrderAdditionalDocuments("serveOrderAdditionalDocuments"),
    isOnlyC47aOrderSelectedToServe("isOnlyC47aOrderSelectedToServe"),
    cafcassOrCymruNeedToProvideReport("cafcassOrCymruNeedToProvideReport"),
    orderEndsInvolvementOfCafcassOrCymru("orderEndsInvolvementOfCafcassOrCymru"),
    selectTypeOfOrder("selectTypeOfOrder"),
    recipientsOptions("recipientsOptions"),

    //Hearing screen field show params
    isCafcassCymru("isCafcassCymru"),
    isFL401ApplicantPresent("isFL401ApplicantPresent"),
    isFL401ApplicantSolicitorPresent("isFL401ApplicantSolicitorPresent"),
    isFL401RespondentPresent("isFL401RespondentPresent"),
    isFL401RespondentSolicitorPresent("isFL401RespondentSolicitorPresent"),
    isApplicant1Present("isApplicant1Present"),
    isApplicant2Present("isApplicant2Present"),
    isApplicant3Present("isApplicant3Present"),
    isApplicant4Present("isApplicant4Present"),
    isApplicant5Present("isApplicant5Present"),
    isApplicant1SolicitorPresent("isApplicant1SolicitorPresent"),
    isApplicant2SolicitorPresent("isApplicant2SolicitorPresent"),
    isApplicant3SolicitorPresent("isApplicant3SolicitorPresent"),
    isApplicant4SolicitorPresent("isApplicant4SolicitorPresent"),
    isApplicant5SolicitorPresent("isApplicant5SolicitorPresent"),
    isRespondent1Present("isRespondent1Present"),
    isRespondent2Present("isRespondent2Present"),
    isRespondent3Present("isRespondent3Present"),
    isRespondent4Present("isRespondent4Present"),
    isRespondent5Present("isRespondent5Present"),
    isRespondent1SolicitorPresent("isRespondent1SolicitorPresent"),
    isRespondent2SolicitorPresent("isRespondent2SolicitorPresent"),
    isRespondent3SolicitorPresent("isRespondent3SolicitorPresent"),
    isRespondent4SolicitorPresent("isRespondent4SolicitorPresent"),
    isRespondent5SolicitorPresent("isRespondent5SolicitorPresent");


    private final String value;

    ManageOrderFieldsEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
