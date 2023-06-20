package uk.gov.hmcts.reform.prl.enums.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SdoDocumentationAndEvidenceEnum {

    @JsonProperty("witnessStatements")
    witnessStatements("witnessStatements", "Witness statements"),
    @JsonProperty("specifiedDocuments")
    specifiedDocuments("specifiedDocuments", "Only specified documents to be filed"),
    @JsonProperty("instructionsFiling")
    instructionsFiling("instructionsFiling", "Instructions on filing bundles"),
    @JsonProperty("spipAttendance")
    spipAttendance("spipAttendance", "Planning Together for Children/Working Together for Children (WT4C)"),
    @JsonProperty("medicalDisclosure")
    medicalDisclosure("medicalDisclosure", "Medical disclosure"),
    @JsonProperty("letterFromGP")
    letterFromGP("letterFromGP", "Letter from GP"),
    @JsonProperty("letterFromSchool")
    letterFromSchool("letterFromSchool", "Letter from school"),
    @JsonProperty("scheduleOfAllegations")
    scheduleOfAllegations("scheduleOfAllegations", "Example schedule of allegations and responses for fact finding");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SdoDocumentationAndEvidenceEnum getValue(String key) {
        return SdoDocumentationAndEvidenceEnum.valueOf(key);
    }

}

