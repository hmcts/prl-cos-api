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


    @JsonProperty("miam_mediatorDocument")
    private YesOrNo miamMediatorDocument;
    @JsonProperty("miam_noMediatorAccessSubfields")
    private String[] miamNoMediatorAccessSubfields;
    @JsonProperty("miam_consent")
    private String miamConsent;


    //
    @JsonProperty("miam_otherProceedings")
    private YesOrNo miamOtherProceedings;
    @JsonProperty("miam_attendance")
    private YesOrNo miamAttendance;
    @JsonProperty("miam_validReason")
    private YesOrNo miamValidReason;

    @JsonProperty("miam_nonAttendanceReasons")
    private String[] miamNonAttendanceReasons;

    @JsonProperty("miam_domesticAbuse")
    private String[] miamDomesticAbuse;
    @JsonProperty("miam_domesticAbuse_policeInvolvement_subfields")
    private String[] miamDomesticAbusePoliceInvolvementSubfields;
    @JsonProperty("miam_domesticabuse_courtInvolvement_subfields")
    private String[] miamDomesticAbuseCourtInvolvementSubfields;
    @JsonProperty("miam_domesticabuse_letterOfBeingVictim_subfields")
    private String[] miamDomesticAbuseLetterOfBeingVictimSubfields;
    @JsonProperty("miam_domesticabuse_letterFromAuthority_subfields")
    private String[] miamDomesticAbuseLetterFromAuthoritySubfields;
    @JsonProperty("miam_domesticabuse_letterFromSupportService_subfields")
    private String[] miamDomesticAbuseLetterFromSupportServiceSubfields;
    @JsonProperty("miam_canProvideDomesticAbuseEvidence")
    private YesOrNo miamCanProvideDomesticAbuseEvidence;
    @JsonProperty("miam_detailsOfDomesticAbuseEvidence")
    private String miamDetailsOfDomesticAbuseEvidence;
    @JsonProperty("miam_domesticAbuseEvidenceDocs")
    private uk.gov.hmcts.reform.prl.models.documents.Document[] miamDomesticAbuseEvidenceDocs;

    @JsonProperty("miam_childProtectionEvidence")
    private String miamChildProtectionEvidence;

    @JsonProperty("miam_urgency")
    private String miamUrgency;

    @JsonProperty("miam_previousAttendance")
    private String miamPreviousAttendance;
    @JsonProperty("miam_previousAttendanceEvidenceDoc")
    private uk.gov.hmcts.reform.prl.models.documents.Document miamPreviousAttendanceEvidenceDoc;
    @JsonProperty("miam_haveDocSignedByMediatorForPrevAttendance")
    private YesOrNo miamHaveDocSignedByMediatorForPrevAttendance;
    @JsonProperty("miam_detailsOfEvidence")
    private String miamDetailsOfEvidence;

    @JsonProperty("miam_notAttendingReasons")
    private String miamNotAttendingReasons;
    @JsonProperty("miam_noMediatorReasons")
    private String miamNoMediatorReasons;
    @JsonProperty("miam_noAppointmentAvailableDetails")
    private String miamNoAppointmentAvailableDetails;
    @JsonProperty("miam_unableToAttainDueToDisablityDetails")
    private String miamUnableToAttainDueToDisablityDetails;
    @JsonProperty("miam_noMediatorIn15mileDetails")
    private String miamNoMediatorIn15mileDetails;

    @JsonProperty("miam_haveDocSigned")
    private YesOrNo miamHaveDocSigned;
    @JsonProperty("miam_certificate")
    private Document miamCertificate;


}
