package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.UnderTakingEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CcdPayment {
    private String paymentAmount;
    private String paymentReference;
    private String paymentMethod;
    private String caseReference;
    private String accountNumber;

    @Data
    @Jacksonized
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder(toBuilder = true)
    public static class ManageOrders {
        @JsonProperty("cafcassEmailAddress")
        private final List<Element<String>> cafcassEmailAddress;
        @JsonProperty("otherEmailAddress")
        private final List<Element<String>> otherEmailAddress;

        /**
         * N117 Form Data.
         */

        private final String manageOrdersCourtName;
        @JsonIgnore
        private final Address manageOrdersCourtAddress;
        private final String manageOrdersCaseNo;
        private final String manageOrdersApplicant;
        private final String manageOrdersApplicantReference;
        private final String manageOrdersRespondent;
        private final String manageOrdersRespondentReference;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private final LocalDate manageOrdersRespondentDob;
        @JsonIgnore
        private final Address manageOrdersRespondentAddress;
        private final YesOrNo manageOrdersUnderTakingRepr;
        private final UnderTakingEnum underTakingSolicitorCounsel;
        private final String manageOrdersUnderTakingPerson;
        @JsonIgnore
        private final Address manageOrdersUnderTakingAddress;
        private final String manageOrdersUnderTakingTerms;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private final LocalDate manageOrdersDateOfUnderTaking;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private final LocalDate underTakingDateExpiry;
        private final String underTakingExpiryTime;
        private final YesOrNo underTakingFormSign;

        private final YesOrNo isTheOrderByConsent;
        private final String recitalsOrPreamble;
        private final String orderDirections;
        private final String furtherDirectionsIfRequired;
        private final JudgeOrMagistrateTitleEnum judgeOrMagistrateTitle;
    }
}
