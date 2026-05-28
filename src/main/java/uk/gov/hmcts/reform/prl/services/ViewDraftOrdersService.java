package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrgSolicitors;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.SolicitorUser;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ViewDraftOrdersService {

    private final OrganisationService organisationService;
    private final UserService userService;
    private final SystemUserService systemUserService;
    private final ObjectMapper objectMapper;

    private static final String FILTERED_DRAFT_ORDERS_CASE_FIELD = "filteredDraftOrders";
    private static final String FILTERED_DRAFT_ORDERS_PRESENT_YES_NO_FLAG_CASE_FIELD = "filteredDraftOrdersPresentYesNoFlag";

    private List<Element<DraftOrder>> getDraftOrdersForUser(CaseDetails caseDetails, String authorisation) {
        List<Element<DraftOrder>> filteredDraftOrders = new ArrayList<>();

        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        List<Element<DraftOrder>> draftOrderCollection = caseData.getDraftOrderCollection();
        if (CollectionUtils.isEmpty(draftOrderCollection)) {
            return filteredDraftOrders;
        }

        Optional<Organisations> currentUserOrganisationsOpt = organisationService.findUserOrganisation(authorisation);
        if (currentUserOrganisationsOpt.isPresent()) {
            /* Map the email of each user email to the list of orders they've created. */
            Map<String, List<Element<DraftOrder>>> createdEmailToDraftOrderMap = draftOrderCollection.stream()
                .collect(Collectors.groupingBy(e -> e.getValue().getOtherDetails().getOrderCreatedByEmailId()));

            /* Get the list of emails of the current user's organisation and iterate though to find which of the lists
             * to include in the result. */
            String currentUserOrganisationId = currentUserOrganisationsOpt.get().getOrganisationIdentifier();
            String systemAuthorisation = systemUserService.getSysUserToken();
            OrgSolicitors orgSolicitors = organisationService.getOrganisationSolicitorDetails(systemAuthorisation, currentUserOrganisationId);

            for (SolicitorUser solicitorUser : orgSolicitors.getUsers()) {
                List<Element<DraftOrder>> userDraftOrders = createdEmailToDraftOrderMap.remove(solicitorUser.getEmail());
                if (Objects.nonNull(userDraftOrders)) {
                    filteredDraftOrders.addAll(userDraftOrders);
                }
                if (createdEmailToDraftOrderMap.isEmpty()) {
                    break;
                }
            }

            filteredDraftOrders.sort(Comparator.<Element<DraftOrder>, LocalDateTime>comparing(
                container -> Optional.ofNullable(container)
                    .map(Element::getValue)
                    .map(DraftOrder::getOtherDetails)
                    .map(OtherDraftOrderDetails::getDateCreated)
                    .orElse(LocalDateTime.now())).reversed()
            );

        } else {
            /* No organisation so just include draft orders that have been created by the current user. */
            UserDetails currentUserDetails = userService.getUserDetails(authorisation);
            draftOrderCollection.stream()
                .filter(e -> Objects.equals(currentUserDetails.getEmail(), e.getValue().getOtherDetails().getOrderCreatedByEmailId()))
                .forEach(filteredDraftOrders::add);
        }

        return filteredDraftOrders;
    }

    public Map<String, Object> getViewDraftOrdersCaseFieldsMap(CaseDetails caseDetails, String authorisation) {
        List<Element<DraftOrder>> filteredDraftOrders = this.getDraftOrdersForUser(caseDetails, authorisation);

        YesOrNo filteredDraftOrdersPresentFlag = CollectionUtils.isEmpty(filteredDraftOrders)
            ? YesOrNo.No : YesOrNo.Yes;

        return Map.of(
            FILTERED_DRAFT_ORDERS_CASE_FIELD, filteredDraftOrders,
            FILTERED_DRAFT_ORDERS_PRESENT_YES_NO_FLAG_CASE_FIELD, filteredDraftOrdersPresentFlag);
    }
}
