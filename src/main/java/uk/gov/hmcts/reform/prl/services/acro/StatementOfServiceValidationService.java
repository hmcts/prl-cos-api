package uk.gov.hmcts.reform.prl.services.acro;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroCaseData;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;

import java.util.List;

/**
 * Service responsible for determining if order is served via
 * statement of service for ACRO cases.
 */
@Service
@Slf4j
public class StatementOfServiceValidationService {

    /**
     * Determines if an order has been served via statement of service to the respondent.
     *
     * @param order the order to check
     * @param stmtOfServiceForOrder statement of service records
     * @param caseData the case data containing respondent information
     * @return true if the order has been served to the respondent via statement of service
     */
    public boolean isOrderServedViaStatementOfService(
            OrderDetails order,
            List<Element<StmtOfServiceAddRecipient>> stmtOfServiceForOrder,
            AcroCaseData caseData) {


        if (stmtOfServiceForOrder == null || stmtOfServiceForOrder.isEmpty()) {
            log.debug("No statement of service records found");
            return false;
        }

        boolean isServed = stmtOfServiceForOrder.stream()
            .map(Element::getValue)
            .anyMatch(sos -> sos != null
                && statementOfServiceHasServedSubmittedTime(sos)
                && isRespondentIncludedInService(sos, caseData));

        return isServed;
    }

    /**
     * Checks if a statement of service has at least one
     * service date/time field is populated.
     *
     * @param sos the statement of service record to check
     * @return true if the statement of service has been completed
     */
    public boolean statementOfServiceHasServedSubmittedTime(StmtOfServiceAddRecipient sos) {
        return sos.getServedDateTimeOption() != null
            || sos.getSubmittedDateTime() != null
            || isPartiesServedDateTimeValid(sos.getPartiesServedDateTime());
    }

    /**
     * Validates that partiesServedDateTime is not null, empty, or whitespace.
     */
    private boolean isPartiesServedDateTimeValid(String partiesServedDateTime) {
        return partiesServedDateTime != null && !partiesServedDateTime.trim().isEmpty();
    }

    /**
     * Checks if the respondent is included in the statement of service by
     * verifying that the selected party name contains both the respondent's
     * first and last names.
     *
     * @param sos the statement of service record
     * @param caseData the case data containing respondent information
     * @return true if the respondent is included in the service
     */
    public boolean isRespondentIncludedInService(StmtOfServiceAddRecipient sos, AcroCaseData caseData) {
        String selectedPartyName = sos.getSelectedPartyName();

        if (selectedPartyName == null || selectedPartyName.trim().isEmpty()) {
            return false;
        }

        if (caseData.getRespondent() == null) {
            return false;
        }

        String respondentFirstName = caseData.getRespondent().getFirstName();
        String respondentLastName = caseData.getRespondent().getLastName();

        if (respondentFirstName == null || respondentLastName == null) {
            return false;
        }

        return isNameMatch(selectedPartyName, respondentFirstName, respondentLastName);
    }

    /**
     * Performs case-insensitive matching to check if the selected party name
     * contains both the respondent's first and last names as complete words.
     *
     * @param selectedPartyName the party name from statement of service
     * @param firstName the respondent's first name
     * @param lastName the respondent's last name
     * @return true if both names are found as complete words in the selected party name
     */
    private boolean isNameMatch(String selectedPartyName, String firstName, String lastName) {
        String normalizedSelectedParty = selectedPartyName.toLowerCase().trim();
        String normalizedFirstName = firstName.toLowerCase().trim();
        String normalizedLastName = lastName.toLowerCase().trim();

        boolean containsFirstName = containsAsWholeWord(normalizedSelectedParty, normalizedFirstName);
        boolean containsLastName = containsAsWholeWord(normalizedSelectedParty, normalizedLastName);

        log.debug("Checking if selectedPartyName '{}' includes respondent '{} {}': firstName={}, lastName={}",
                  selectedPartyName, firstName, lastName, containsFirstName, containsLastName);

        return containsFirstName && containsLastName;
    }

    /**
     * Checks if a target word exists as a complete word (with word boundaries) in the source text.
     * This prevents partial matches like "doe" matching "doesmith".
     *
     * @param source the source text to search in
     * @param target the target word to find
     * @return true if target exists as a complete word in source
     */
    private boolean containsAsWholeWord(String source, String target) {
        if (source == null || target == null || target.isEmpty()) {
            return false;
        }

        String regex = "\\b" + java.util.regex.Pattern.quote(target) + "\\b";
        return java.util.regex.Pattern.compile(regex).matcher(source).find();
    }
}
