package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.*;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ViewDraftOrdersService {

    private final OrganisationService organisationService;
    private final UserService userService;
    private final SystemUserService systemUserService;
    private final ObjectMapper objectMapper;

    public List<Element<DraftOrder>> getDraftOrdersForUser(CaseDetails caseDetails, String authorisation) {

        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        Optional<Organisations> currentUserOrganisationsOpt = organisationService.findUserOrganisation(authorisation);

        List<Element<DraftOrder>> viewFilteredDraftOrders = new ArrayList<>();
        if (currentUserOrganisationsOpt.isPresent()) {
            /* Map the email of each user email to the list of orders they've created. */
            Map<String, List<Element<DraftOrder>>> createdEmailToDraftOrderMap = caseData.getDraftOrderCollection().stream()
                .collect(Collectors.groupingBy(e -> e.getValue().getOtherDetails().getOrderCreatedByEmailId()));

            /* Get the list of emails of the current user's organisation and iterate though to find which of the lists
             * to include in the result. */
            String currentUserOrganisationId = currentUserOrganisationsOpt.get().getOrganisationIdentifier();
            String systemAuthorisation = systemUserService.getSysUserToken();
            OrgSolicitors orgSolicitors = organisationService.getOrganisationSolicitorDetails(systemAuthorisation, currentUserOrganisationId);

            for (SolicitorUser solicitorUser : orgSolicitors.getUsers()) {
                List<Element<DraftOrder>> userDraftOrders = createdEmailToDraftOrderMap.remove(solicitorUser.getEmail());
                if (Objects.nonNull(userDraftOrders)) {
                    viewFilteredDraftOrders.addAll(userDraftOrders);
                }
                if (createdEmailToDraftOrderMap.isEmpty()) {
                    break;
                }
            }
            viewFilteredDraftOrders.sort(Comparator.comparing(
                draftOrderElement -> draftOrderElement.getValue().getDateOrderMade())
            );
        } else {
            /* No organisation so just include draft orders that have been created by the current user. */
            UserDetails currentUserDetails = userService.getUserDetails(authorisation);
            caseData.getDraftOrderCollection().stream()
                .filter(e -> Objects.equals(currentUserDetails.getEmail(), e.getValue().getOtherDetails().getOrderCreatedByEmailId()))
                .forEach(viewFilteredDraftOrders::add);
        }

        return viewFilteredDraftOrders;
    }
}
