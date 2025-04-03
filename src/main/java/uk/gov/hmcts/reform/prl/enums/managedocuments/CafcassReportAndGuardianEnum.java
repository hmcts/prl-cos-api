package uk.gov.hmcts.reform.prl.enums.managedocuments;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CafcassReportAndGuardianEnum {

    childImpactReport1("childImpactReport1", "Child Impact Report 1"),
    childImpactReport2("childImpactReport2", "Child Impact Report 2"),

    safeguardingLetter("safeguardingLetter", "Safeguarding letter/Safeguarding Enquiries Report (SER)"),

    section7Report("section7Report", "Section 7 report/Child Impact Analysis"),

    section37Report("section37Report", "Section 37 report"),

    riskAssessment("16aRiskAssessment", "16a risk assessment"),

    guardianReport("guardianReport", "Guardian report"),

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
