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
    @JsonProperty("ra_needInterpreterInCertainLanguageDetails")
    private String needInterpreterInCertainLanguageDetails;
    @JsonProperty("ra_specialArrangements")
    private String[] specialArrangements;
    @JsonProperty("ra_specialArrangementsOtherSubField")
    private String specialArrangementsOtherSubField;
    @JsonProperty("ra_disabilityRequirements")
    private String[] disabilityRequirements;
    @JsonProperty("ra_documentInformation")
    private String[] documentInformation;
    @JsonProperty("ra_specifiedColorDocumentsDetails")
    private String specifiedColorDocumentsDetails;
    @JsonProperty("ra_largePrintDocumentsDetails")
    private String largePrintDocumentsDetails;
    @JsonProperty("ra_otherDetails")
    private String otherDetails;
    @JsonProperty("ra_communicationHelp")
    private String[] communicationHelp;
    @JsonProperty("ra_signLanguageInterpreterDetails")
    private String signLanguageInterpreterDetails;
    @JsonProperty("ra_communicationHelpOtherDetails")
    private String communicationHelpOtherDetails;
    @JsonProperty("ra_supportCourt")
    private String[] supportCourt;
    @JsonProperty("ra_supportWorkerCarerSubField")
    private String supportWorkerCarerSubField;
    @JsonProperty("ra_friendFamilyMemberSubField")
    private String friendFamilyMemberSubField;
    @JsonProperty("ra_therapyAnimalSubField")
    private String therapyAnimalSubField;
    @JsonProperty("ra_supportCourtOtherSubField")
    private String supportCourtOtherSubField;
    @JsonProperty("ra_feelComportable")
    private String[] feelComfortable;
    @JsonProperty("ra_appropriateLightingSubField")
    private String appropriateLightingSubField;
    @JsonProperty("ra_feelComportableOtherSubField")
    private String feelComfortableOtherSubField;
    @JsonProperty("ra_travellingCourt")
    private String[] travellingCourt;
    @JsonProperty("ra_parkingSpaceSubField")
    private String parkingSpaceSubField;
    @JsonProperty("ra_differentTypeChairSubField")
    private String differentTypeChairSubField;
    @JsonProperty("ra_travellingCourtOtherSubField")
    private String travellingCourtOtherSubField;
}
