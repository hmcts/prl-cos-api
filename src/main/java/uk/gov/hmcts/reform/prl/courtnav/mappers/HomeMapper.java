package uk.gov.hmcts.reform.prl.courtnav.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.BehaviourTowardsChildrenEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.CurrentResidentAtAddressEnum;
import uk.gov.hmcts.reform.prl.rpa.mappers.AddressMapper;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;

import java.util.stream.Collectors;
import javax.json.JsonObject;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HomeMapper {

    private final AddressMapper addressMapper;

    private JsonObject map(CourtNavCaseData courtNavCaseData) {

        String currentlyLivesAtAddress = null;

        if (courtNavCaseData.getCurrentlyLivesAtAddress() != null && !courtNavCaseData.getCurrentlyLivesAtAddress().isEmpty()) {
            currentlyLivesAtAddress = courtNavCaseData.getCurrentlyLivesAtAddress()
                .stream()
                .map(CurrentResidentAtAddressEnum::getDisplayedValue)
                .collect(Collectors.joining(", "));
        }

        String behaviourTowardsChildren = null;

        if (courtNavCaseData.getStopBehaviourTowardsChildren() != null && !courtNavCaseData.getStopBehaviourTowardsChildren().isEmpty()) {
            behaviourTowardsChildren = courtNavCaseData.getStopBehaviourTowardsChildren()
                .stream()
                .map(BehaviourTowardsChildrenEnum::getDisplayedValue)
                .collect(Collectors.joining(", "));
        }

        return new NullAwareJsonObjectBuilder()
            .add("address", addressMapper.mapAddress(courtNavCaseData.getOccupationOrderAddress()))
            .add("peopleLivingAtThisAddress", currentlyLivesAtAddress)
            .add("textAreaSomethingElse", "other".equals(currentlyLivesAtAddress)
                ? courtNavCaseData.getCurrentlyLivesAtAddressOther() : null)
            .add("everLivedAtTheAddress", courtNavCaseData.getPreviouslyLivedAtAddress().getDisplayedValue())
            .add("intendToLiveAtTheAddress", courtNavCaseData.getPreviouslyLivedAtAddress().getDisplayedValue().equals("No")
                ? courtNavCaseData.getIntendedToLiveAtAddress().getDisplayedValue() : null)
            .add("doAnyChildrenLiveAtAddress", null != courtNavCaseData.getChildrenApplicantResponsibility()
                ? "Yes" : "No")
            .build();
    }
}
