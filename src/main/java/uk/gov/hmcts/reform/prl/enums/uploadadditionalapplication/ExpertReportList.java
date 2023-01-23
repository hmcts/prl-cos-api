package uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ExpertReportList {

    @JsonProperty("pediatric")
    pediatric("pediatric", "Pediatric"),
    @JsonProperty("pediatricRadiologist")
    pediatricRadiologist("pediatricRadiologist", "Pediatric Radiologist"),


    @JsonProperty("otherOrder")
    otherOrder("otherOrder", "Other specific order - including C1 and C100 orders, and supplements"),
    @JsonProperty("c2Order")
    c2Order("c2Order", "C2 - to add or remove someone on a case, or for a specific request to the judge");

    @JsonProperty("otherOrder")
    otherOrder("otherOrder", "Other specific order - including C1 and C100 orders, and supplements"),
    @JsonProperty("c2Order")
    c2Order("c2Order", "C2 - to add or remove someone on a case, or for a specific request to the judge");

    @JsonProperty("otherOrder")
    otherOrder("otherOrder", "Other specific order - including C1 and C100 orders, and supplements"),
    @JsonProperty("c2Order")
    c2Order("c2Order", "C2 - to add or remove someone on a case, or for a specific request to the judge");

    @JsonProperty("otherOrder")
    otherOrder("otherOrder", "Other specific order - including C1 and C100 orders, and supplements"),
    @JsonProperty("c2Order")
    c2Order("c2Order", "C2 - to add or remove someone on a case, or for a specific request to the judge");

    @JsonProperty("otherOrder")
    otherOrder("otherOrder", "Other specific order - including C1 and C100 orders, and supplements"),
    @JsonProperty("c2Order")
    c2Order("c2Order", "C2 - to add or remove someone on a case, or for a specific request to the judge");

    @JsonProperty("otherOrder")
    otherOrder("otherOrder", "Other specific order - including C1 and C100 orders, and supplements"),
    @JsonProperty("c2Order")
    c2Order("c2Order", "C2 - to add or remove someone on a case, or for a specific request to the judge");


    @JsonProperty("otherOrder")
    otherOrder("otherOrder", "Other specific order - including C1 and C100 orders, and supplements"),
    @JsonProperty("c2Order")
    c2Order("c2Order", "C2 - to add or remove someone on a case, or for a specific request to the judge")


    @JsonProperty("otherOrder")
    otherOrder("otherOrder", "Other specific order - including C1 and C100 orders, and supplements"),
    @JsonProperty("c2Order")
    c2Order("c2Order", "C2 - to add or remove someone on a case, or for a specific request to the judge");

    @JsonProperty("otherOrder")
    otherOrder("otherOrder", "Other specific order - including C1 and C100 orders, and supplements"),
    @JsonProperty("c2Order")
    c2Order("c2Order", "C2 - to add or remove someone on a case, or for a specific request to the judge");

    @JsonProperty("otherOrder")
    otherOrder("otherOrder", "Other specific order - including C1 and C100 orders, and supplements"),
    @JsonProperty("c2Order")
    c2Order("c2Order", "C2 - to add or remove someone on a case, or for a specific request to the judge");

    @JsonProperty("otherOrder")
    otherOrder("otherOrder", "Other specific order - including C1 and C100 orders, and supplements"),
    @JsonProperty("c2Order")
    c2Order("c2Order", "C2 - to add or remove someone on a case, or for a specific request to the judge");



    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ExpertReportList getValue(String key) {
        return ExpertReportList.valueOf(key);
    }
}



    {
    "LiveFrom": "20/11/2022",
    "ID": "ExpertReportList",
    "ListElementCode": "OtherMedicalReport",
    "ListElement": "Other Medical report",
    "DisplayOrder": 3
    },
    {
    "LiveFrom": "20/11/2022",
    "ID": "ExpertReportList",
    "ListElementCode": "residentialAssessment",
    "ListElement": "Family Centre Assessments - Residential",
    "DisplayOrder": 4
    },
    {
    "LiveFrom": "20/11/2022",
    "ID": "ExpertReportList",
    "ListElementCode": "nonResidentialAssessment",
    "ListElement": "Family Centre Assessments - NonResidential",
    "DisplayOrder": 5
    },
    {
    "LiveFrom": "20/11/2022",
    "ID": "ExpertReportList",
    "ListElementCode": "psychiatricChildAndParent",
    "ListElement": "Psychiatric - On child and Parent(s)/carers",
    "DisplayOrder": 6
    },
    {
    "LiveFrom": "20/11/2022",
    "ID": "ExpertReportList",
    "ListElementCode": "psychiatricOnChild",
    "ListElement": "Psychiatric - On child only",
    "DisplayOrder": 7
    },
    {
    "LiveFrom": "20/11/2022",
    "ID": "ExpertReportList",
    "ListElementCode": "psychiatricOnParents",
    "ListElement": "Adult Psychiatric Report on Parents(s)",
    "DisplayOrder": 8
    },
    {
    "LiveFrom": "20/11/2022",
    "ID": "ExpertReportList",
    "ListElementCode": "clinicalReportOnChild",
    "ListElement": "Physiological Report on Child Only - Clinical",
    "DisplayOrder": 9
    },
    {
    "LiveFrom": "20/11/2022",
    "ID": "ExpertReportList",
    "ListElementCode": "educationalReportOnChild",
    "ListElement": "Physiological Report on Child Only - Educational",
    "DisplayOrder": 10
    },
    {
    "LiveFrom": "20/11/2022",
    "ID": "ExpertReportList",
    "ListElementCode": "cognitiveReportOnParent",
    "ListElement": "Physiological Report on Parent(s) - full cognitive",
    "DisplayOrder": 11
    },
    {
    "LiveFrom": "20/11/2022",
    "ID": "ExpertReportList",
    "ListElementCode": "functioningReportOnParent",
    "ListElement": "Physiological Report on Parent(s) - functioning",
    "DisplayOrder": 12
    },
    {
    "LiveFrom": "20/11/2022",
    "ID": "ExpertReportList",
    "ListElementCode": "physiologicalReportOnParentAndChild",
    "ListElement": "Physiological Report on Parent(s) and child",
    "DisplayOrder": 13
    },
    {
    "LiveFrom": "20/11/2022",
    "ID": "ExpertReportList",
    "ListElementCode": "multiDisciplinaryAssessment",
    "ListElement": "Multi Disciplinary Assessment",
    "DisplayOrder": 14
    },
    {
    "LiveFrom": "20/11/2022",
    "ID": "ExpertReportList",
    "ListElementCode": "independentSocialWorker",
    "ListElement": "Independent social worker",
    "DisplayOrder": 15
    },
    {
    "LiveFrom": "20/11/2022",
    "ID": "ExpertReportList",
    "ListElementCode": "haematologist",
    "ListElement": "Haematologist",
    "DisplayOrder": 16
    },
    {
    "LiveFrom": "20/11/2022",
    "ID": "ExpertReportList",
    "ListElementCode": "opthamologist",
    "ListElement": "Ophthalmologist",
    "DisplayOrder": 17
    },
    {
    "LiveFrom": "20/11/2022",
    "ID": "ExpertReportList",
    "ListElementCode": "neurosurgeon",
    "ListElement": "Neurosurgeon",
    "DisplayOrder": 18
    },
    {
    "LiveFrom": "20/11/2022",
    "ID": "ExpertReportList",
    "ListElementCode": "otherExpertReport",
    "ListElement": "Other Expert Report",
    "DisplayOrder": 19
    },
    {
    "LiveFrom": "20/11/2022",
    "ID": "ExpertReportList",
    "ListElementCode": "professionalDrug",
    "ListElement": "Professional: Drug/Alcohol",
    "DisplayOrder": 20
    },
    {
    "LiveFrom": "20/11/2022",
    "ID": "ExpertReportList",
    "ListElementCode": "professionalHair",
    "ListElement": "Professional: Hair Strand",
    "DisplayOrder": 21
    },
    {
    "LiveFrom": "20/11/2022",
    "ID": "ExpertReportList",
    "ListElementCode": "professionalDNA",
    "ListElement": "Professional: DNA testing",
    "DisplayOrder": 22
    },
    {
    "LiveFrom": "20/11/2022",
    "ID": "ExpertReportList",
    "ListElementCode": "professionalOther",
    "ListElement": "Professional: Other",
    "DisplayOrder": 23
    },
    {
    "LiveFrom": "20/11/2022",
    "ID": "ExpertReportList",
    "ListElementCode": "toxicologyReport",
    "ListElement": "Toxicology report/statement",
    "DisplayOrder": 24
    },
