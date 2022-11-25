package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class C100RebuildReasonableAdjustmentsElements {

    @JsonProperty("ra_typeOfHearing")
    private String[] typeOfHearings;
    @JsonProperty("ra_languageNeeds")
    private String[] languageNeeds;
    @JsonProperty("ra_needInterpreterInCertainLanguage_subfield")
    private String needInterpreterInCertainLanguageDetails;
    @JsonProperty("ra_specialArrangements")
    private String[] specialArrangements;
    @JsonProperty("ra_specialArrangementsOther_subfield")
    private String specialArrangementsOtherSubField;
    @JsonProperty("ra_disabilityRequirements")
    private String[] disabilityRequirements;
    @JsonProperty("ra_documentInformation")
    private String[] documentInformation;
    @JsonProperty("ra_specifiedColorDocuments_subfield")
    private String specifiedColorDocumentsDetails;
    @JsonProperty("ra_largePrintDocuments_subfield")
    private String largePrintDocumentsDetails;
    @JsonProperty("ra_documentHelpOther_subfield")
    private String otherDetails;
    @JsonProperty("ra_communicationHelp")
    private String[] communicationHelp;
    @JsonProperty("ra_signLanguageInterpreter_subfield")
    private String signLanguageInterpreterDetails;
    @JsonProperty("ra_communicationHelpOther_subfield")
    private String communicationHelpOtherDetails;
    @JsonProperty("ra_supportCourt")
    private String[] supportCourt;
    @JsonProperty("ra_supportWorkerCarer_subfield")
    private String supportWorkerCarerSubField;
    @JsonProperty("ra_friendFamilyMember_subfield")
    private String friendFamilyMemberSubField;
    @JsonProperty("ra_therapyAnimal_subfield")
    private String therapyAnimalSubField;
    @JsonProperty("ra_supportCourtOther_subfield")
    private String supportCourtOtherSubField;
    @JsonProperty("ra_feelComportable")
    private String[] feelComfortable;
    @JsonProperty("ra_appropriateLighting_subfield")
    private String appropriateLightingSubField;
    @JsonProperty("ra_feelComportableOther_subfield")
    private String feelComfortableOtherSubField;
    @JsonProperty("ra_travellingCourt")
    private String[] travellingCourt;
    @JsonProperty("ra_parkingSpace_subfield")
    private String parkingSpaceSubField;
    @JsonProperty("ra_differentTypeChair_subfield")
    private String differentTypeChairSubField;
    @JsonProperty("ra_travellingCourtOther_subfield")
    private String travellingCourtOtherSubField;
}
