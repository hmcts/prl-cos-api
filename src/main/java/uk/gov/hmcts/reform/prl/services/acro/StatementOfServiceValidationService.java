package uk.gov.hmcts.reform.prl.services.acro;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroCaseData;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;

import java.util.List;

/**
 * Service responsible for validating statement of service data and determining
 * if orders have been properly served to respondents.
 */
@Service
@Slf4j
public class StatementOfServiceValidationService {

    /**
     * Determines if an order has been served via statement of service to the respondent.
     *
     * @param order the order to check
     * @param stmtOfServiceForOrder list of statement of service records
     * @param caseData the case data containing respondent information
     * @return true if the order has been served to the respondent via statement of service
     */
    public boolean isOrderServedViaStatementOfService(
            OrderDetails order,
            List<Element<StmtOfServiceAddRecipient>> stmtOfServiceForOrder,
            AcroCaseData caseData) {

        log.debug("Checking if order has been served via statement of service for case {}", caseData.getId());

        // Check if statement of service list exists and is not empty
        if (stmtOfServiceForOrder == null || stmtOfServiceForOrder.isEmpty()) {
            log.debug("No statement of service records found");
            return false;
        }

        // Check if any statement of service has been completed and includes the respondent
        boolean isServed = stmtOfServiceForOrder.stream()
            .map(Element::getValue)
            .anyMatch(sos -> sos != null &&
                isStatementOfServiceCompleted(sos) &&
                isRespondentIncludedInService(sos, caseData));

        log.debug("Order served via statement of service: {}", isServed);
        return isServed;
    }

    /**
     * Checks if a statement of service has been completed by verifying
     * that at least one service date/time field is populated.
     *
     * @param sos the statement of service record to check
     * @return true if the statement of service has been completed
     */
    public boolean isStatementOfServiceCompleted(StmtOfServiceAddRecipient sos) {
        boolean isCompleted = sos.getServedDateTimeOption() != null ||
                             sos.getSubmittedDateTime() != null ||
                             sos.getPartiesServedDateTime() != null;

        log.debug("Statement of service completed: {}", isCompleted);
        return isCompleted;
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

        // Validate input parameters
        if (selectedPartyName == null || selectedPartyName.trim().isEmpty()) {
            log.debug("No selected party name found in statement of service");
            return false;
        }

        if (caseData.getRespondent() == null) {
            log.debug("No respondent found in case data");
            return false;
        }

        String respondentFirstName = caseData.getRespondent().getFirstName();
        String respondentLastName = caseData.getRespondent().getLastName();

        // If respondent names are missing, cannot verify
        if (respondentFirstName == null || respondentLastName == null) {
            log.debug("Respondent first name or last name is missing");
            return false;
        }

        // Perform case-insensitive name matching
        return isNameMatch(selectedPartyName, respondentFirstName, respondentLastName);
    }

    /**
     * Performs case-insensitive matching to check if the selected party name
     * contains both the respondent's first and last names.
     *
     * @param selectedPartyName the party name from statement of service
     * @param firstName the respondent's first name
     * @param lastName the respondent's last name
     * @return true if both names are found in the selected party name
     */
    private boolean isNameMatch(String selectedPartyName, String firstName, String lastName) {
        String normalizedSelectedParty = selectedPartyName.toLowerCase().trim();
        String normalizedFirstName = firstName.toLowerCase().trim();
        String normalizedLastName = lastName.toLowerCase().trim();

        boolean containsFirstName = normalizedSelectedParty.contains(normalizedFirstName);
        boolean containsLastName = normalizedSelectedParty.contains(normalizedLastName);

        log.debug("Checking if selectedPartyName '{}' includes respondent '{} {}': firstName={}, lastName={}",
                  selectedPartyName, firstName, lastName, containsFirstName, containsLastName);

        return containsFirstName && containsLastName;
    }
}
