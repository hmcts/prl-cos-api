package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.internationalelements.CitizenInternationalElements;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentEventErrorsEnum.INTERNATIONAL_ELEMENT_ERROR;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Slf4j
@Service
public class InternationalElementsChecker implements RespondentEventChecker {
    @Autowired
    RespondentTaskErrorService respondentTaskErrorService;

    @Override
    public boolean isStarted(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);
        return response.filter(value -> ofNullable(value.getCitizenInternationalElements())
            .filter(citizenInternationalElements -> anyNonEmpty(
                citizenInternationalElements.getChildrenLiveOutsideOfEnWlDetails(),
                citizenInternationalElements.getAnotherCountryAskedInformation(),
                citizenInternationalElements.getParentsAnyOneLiveOutsideEnWl()
            )).isPresent()).isPresent();
    }

    @Override
    public boolean isFinished(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);

        if (response.isPresent()) {
            Optional<CitizenInternationalElements> solicitorInternationalElement
                = Optional.ofNullable(response.get().getCitizenInternationalElements());
            if (solicitorInternationalElement.isPresent() && checkInternationalElementMandatoryCompleted(
                solicitorInternationalElement.get())) {
                respondentTaskErrorService.removeError(INTERNATIONAL_ELEMENT_ERROR);
                return true;
            }
        }
        respondentTaskErrorService.addEventError(
            INTERNATIONAL_ELEMENT,
            INTERNATIONAL_ELEMENT_ERROR,
            INTERNATIONAL_ELEMENT_ERROR.getError()
        );
        return false;
    }

    private boolean checkInternationalElementMandatoryCompleted(CitizenInternationalElements internationalElements) {
        List<Optional<?>> fields = new ArrayList<>();
        Optional<YesOrNo> reasonForChild = ofNullable(internationalElements.getChildrenLiveOutsideOfEnWl());
        fields.add(reasonForChild);
        if (reasonForChild.isPresent() && YesOrNo.Yes.equals(reasonForChild.get())) {
            fields.add(ofNullable(internationalElements.getChildrenLiveOutsideOfEnWlDetails()));
            fields.add(ofNullable(internationalElements.getParentsAnyOneLiveOutsideEnWlDetails()));
            fields.add(ofNullable(internationalElements.getAnotherPersonOrderOutsideEnWlDetails()));
            fields.add(ofNullable(internationalElements.getAnotherCountryAskedInformationDetaails()));
        }
        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }
}
