package uk.gov.hmcts.reform.prl.models.dto.citizen;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CitizenDocumentsManagement {

    @JsonIgnore
    public List<CitizenDocuments> citizenDocuments;

    @JsonProperty("applicantDocuments")
    public List<CitizenDocuments> applicantDocuments;

    @JsonProperty("respondentDocuments")
    public List<CitizenDocuments> respondentDocuments;

    @JsonProperty("citizenOtherDocuments")
    public List<CitizenDocuments> citizenOtherDocuments;

    @JsonProperty("citizenOrders")
    public List<CitizenDocuments> citizenOrders;

    @JsonProperty("citizenApplicationPacks")
    public List<CitizenDocuments> citizenApplicationPacks;

    /**
     * This is for citizen dashboard notification flags.
     */
    private List<CitizenNotification> citizenNotifications;

    public static final Set<String> unReturnedCategoriesForUI =
        Set.of(
            "section37Report",
            "16aRiskAssessment",
            "sec37Report",
            "publicFundingCertificates",
            "noticesOfActingDischarge",
            "requestForFASFormsToBeChanged",
            "lettersOfComplaint",
            "SPIPReferralRequests",
            "homeOfficeDWPResponses",
            "internalCorrespondence",
            "importantInfoAboutAddressAndContact",
            "specialMeasures",
            "noticeOfHearing",
            "caseSummary"
        );

    public static final Set<String> otherDocumentsCategoriesForUI =
        Set.of(
            "magistratesFactsAndReasons",
            "otherWitnessStatements",
            "localAuthorityOtherDoc",
            "emailsToCourtToRequestHearingsAdjourned",
            "witnessAvailability",
            "privacyNotice",
            "anyOtherDoc",
            "courtBundle",
            "fm5Statements"
        );

    public static final Set<String> redundantDocumentsCategories =
        Set.of(
            "transcriptsOfJudgements"
        );
}
