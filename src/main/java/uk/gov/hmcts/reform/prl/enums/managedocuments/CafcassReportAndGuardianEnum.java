package uk.gov.hmcts.reform.prl.enums.managedocuments;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CafcassReportAndGuardianEnum {

    childImpactReport1("childImpactReport1", "Child Impact Report 1"),
    childImpactReport2("childImpactReport2", "Child Impact Report 2"),

    safeguardingLetter("safeguardingLetter", "Safeguarding letter/Safeguarding Enquiries Report (SER)"),

    section7Report("section7Report", "Section 7 report"),

    section37Report("section37Report", "Section 37 report"),

    riskAssessment("16aRiskAssessment", "Section 16A risk assessment"),

    enforcementOrderSuitabilityReport("enforcementOrderSuitabilityReport", "Enforcement Order Suitability Report"),

    parentalOrderReporterReport("parentalOrderReporterReport", "Parental Order Reporter Report"),

    cirTransferRequest("cirTransferRequest", "CIR Transfer Request"),

    cirExtensionRequest("cirExtensionRequest", "CIR Extension Request"),

    guardianReport("guardianReport", "Section 16.4 Guardian Report"),

    specialGuardianshipReport("specialGuardianshipReport", "Special guardianship report"),

    otherDocs("otherDocs", "Cafcass/Cafcass Cymru other documents");


    private final String categoryId;
    private final String categoryName;

    public String getCategoryName() {
        return categoryName;
    }

    public String geCategoryId() {
        return categoryId;
    }
}
