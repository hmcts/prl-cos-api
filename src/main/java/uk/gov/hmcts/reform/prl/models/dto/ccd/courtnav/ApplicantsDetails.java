package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.PreferredContactEnum;

import java.util.List;


@Data
@Builder
public class ApplicantsDetails {

    @JsonProperty("applicantFirstName")
    private String firstName;

    @JsonProperty("applicantLastName")
    private String lastName;

    @JsonProperty("applicantOtherNames")
    private String previousName;

    @JsonProperty("applicantDateOfBirth")
    private CourtNavDate dateOfBirth;

    @JsonProperty("applicantGender")
    private Gender gender;

    @JsonProperty("applicantGenderOther")
    private String otherGender;

    @JsonProperty("isShareContactDetailsWithRespondent")
    private boolean shareContactDetailsWithRespondent;

    @JsonProperty("applicantPhoneNumber")
    private String phoneNumber;

    @JsonProperty("applicantEmailAddress")
    private String email;

    @JsonProperty("applicantContactInstructions")
    private String applicantContactInstructions;

    @JsonProperty("applicantPreferredContact")
    private List<PreferredContactEnum> applicantPreferredContact;


    @JsonProperty("applicantAddress")
    private CourtNavAddress address;

    @JsonProperty("legalRepresentativeFirm")
    private String solicitorFirmName;

    @JsonProperty("legalRepresentativeAddress")
    private CourtNavAddress solicitorAddress;

    @JsonProperty("legalRepresentativeDx")
    private String dxNumber;

    @JsonProperty("legalRepresentativeReference")
    private String solicitorReference;

    @JsonProperty("legalRepresentativeFirstName")
    private String representativeFirstName;

    @JsonProperty("legalRepresentativeLastName")
    private String representativeLastName;

    @JsonProperty("legalRepresentativeEmail")
    private String solicitorEmail;

    @JsonProperty("legalRepresentativePhone")
    private String solicitorTelephone;

    @JsonProperty("applicantHasLegalRepresentative")
    private Boolean hasLegalRepresentative;
}
