package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class C100RebuildMiamElements {

    @JsonProperty("miam_otherProceedings")
    private YesOrNo miamOtherProceedings;
    @JsonProperty("miam_consent")
    private String miamConsent;
    @JsonProperty("miam_attendance")
    private YesOrNo miamAttendance;
    @JsonProperty("miam_mediatorDocument")
    private YesOrNo miamMediatorDocument;
    @JsonProperty("miam_validReason")
    private YesOrNo miamValidReason;
    @JsonProperty("miam_haveDocSigned")
    private YesOrNo miamHaveDocSigned;
    @JsonProperty("miam_certificate")
    private Document miamCertificate;
    @JsonProperty("miam_nonAttendanceReasons")
    private String[] miamNonAttendanceReasons;
    @JsonProperty("miam_domesticAbuse")
    private String[] miamDomesticAbuse;
    @JsonProperty("miam_domesticabuse_involvement_subfields")
    private String[] miamDomesticAbuseInvolvementSubfields;
    @JsonProperty("miam_domesticabuse_courtInvolvement_subfields")
    private String[] miamDomesticAbuseCourtInvolvementSubfields;
    @JsonProperty("miam_domesticabuse_letterOfBeingVictim_subfields")
    private String[] miamDomesticAbuseLetterOfBeingVictimSubfields;
    @JsonProperty("miam_domesticabuse_letterFromAuthority_subfields")
    private String[] miamDomesticAbuseLetterFromAuthoritySubfields;
    @JsonProperty("miam_domesticabuse_letterFromSupportService_subfields")
    private String[] miamDomesticAbuseLetterFromSupportServiceSubfields;
    @JsonProperty("miam_childProtectionEvidence")
    private String[] miamChildProtectionEvidence;
    @JsonProperty("miam_urgency")
    private String[] miamUrgency;
    @JsonProperty("miam_previousAttendance")
    private String[] miamPreviousAttendance;
    @JsonProperty("miam_notAttendingReasons")
    private String[] miamNotAttendingReasons;
    @JsonProperty("miam_noMediatorAccessSubfields")
    private String[] miamNoMediatorAccessSubfields;
}