package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.ResSolInternationalElements;
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

        if (response.isPresent()) {
            return ofNullable(response.get().getResSolInternationalElements())
                .filter(resSolInternationalElements -> anyNonEmpty(
                    resSolInternationalElements.getInternationalElementChildInfo(),
                    resSolInternationalElements.getInternationalElementJurisdictionInfo(),
                    resSolInternationalElements.getInternationalElementParentInfo(),
                    resSolInternationalElements.getInternationalElementParentInfo()
                )).isPresent();
        }
        return false;
    }

    @Override
    public boolean isFinished(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);

        if (response.isPresent()) {
            Optional<ResSolInternationalElements> solicitorInternationalElement
                = Optional.ofNullable(response.get().getResSolInternationalElements());
            if (!solicitorInternationalElement.isEmpty() && checkInternationalElementMandatoryCompleted(
                solicitorInternationalElement)) {
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

    private boolean checkInternationalElementMandatoryCompleted(Optional<ResSolInternationalElements> internationalElements) {

        List<Optional<?>> fields = new ArrayList<>();
        if (internationalElements.isPresent()) {

            Optional<YesOrNo> reasonForChild = ofNullable(internationalElements.get().getInternationalElementChildInfo().getReasonForChild());
            fields.add(reasonForChild);
            if (reasonForChild.isPresent() && YesOrNo.Yes.equals(reasonForChild.get())) {
                fields.add(ofNullable(internationalElements.get().getInternationalElementChildInfo().getReasonForChildDetails()));
            }

            Optional<YesOrNo> reasonForParent = ofNullable(internationalElements.get().getInternationalElementParentInfo().getReasonForParent());
            fields.add(reasonForParent);
            if (reasonForParent.isPresent() && YesOrNo.Yes.equals(reasonForParent.get())) {
                fields.add(ofNullable(internationalElements.get().getInternationalElementParentInfo().getReasonForParentDetails()));
            }

            Optional<YesOrNo> reasonForJurisdiction = ofNullable(internationalElements.get()
                                                                     .getInternationalElementJurisdictionInfo().getReasonForJurisdiction());
            fields.add(reasonForJurisdiction);
            if (reasonForJurisdiction.isPresent() && YesOrNo.Yes.equals(reasonForJurisdiction.get())) {
                fields.add(ofNullable(internationalElements.get().getInternationalElementJurisdictionInfo().getReasonForJurisdictionDetails()));
            }

            Optional<YesOrNo> requestToAuthority = ofNullable(internationalElements.get().getInternationalElementRequestInfo().getRequestToAuthority());
            fields.add(requestToAuthority);
            if (requestToAuthority.isPresent() && YesOrNo.Yes.equals(requestToAuthority.get())) {
                fields.add(ofNullable(internationalElements.get().getInternationalElementRequestInfo().getRequestToAuthorityDetails()));
            }
        }
        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }
}
