package uk.gov.hmcts.reform.prl.models.dto.citizen;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CitizenDocumentsManagement {

    @JsonProperty("citizenDocuments")
    public List<CitizenDocuments> citizenDocuments;

    @JsonProperty("citizenOrders")
    public List<CitizenDocuments> citizenOrders;

    @JsonProperty("citizenApplicationPacks")
    public List<CitizenDocuments> citizenApplicationPacks;

    /**
     * This is for citizen dashboard notification flags.
     */
    private List<CitizenNotification> citizenNotifications;

    public static final List<String> unReturnedCategoriesForUI =
        List.of(
            "safeguardingLetter",
            "section37Report",
            "section7Report",
            "16aRiskAssessment",
            "guardianReport",
            "specialGuardianshipReport",
            "otherDocs",
            "sec37Report",
            "localAuthorityOtherDoc",
            "emailsToCourtToRequestHearingsAdjourned",
            "publicFundingCertificates",
            "noticesOfActingDischarge",
            "requestForFASFormsToBeChanged",
            "witnessAvailability",
            "lettersOfComplaint",
            "SPIPReferralRequests",
            "homeOfficeDWPResponses",
            "internalCorrespondence",
            "importantInfoAboutAddressAndContact",
            "privacyNotice",
            "specialMeasures",
            "anyOtherDoc",
            "noticeOfHearing",
            "caseSummary"
        );
}
