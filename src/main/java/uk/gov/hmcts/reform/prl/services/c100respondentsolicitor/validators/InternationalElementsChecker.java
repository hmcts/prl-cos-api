package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.ResSolInternationalElements;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
public class InternationalElementsChecker implements RespondentEventChecker {
    @Override
    public boolean isStarted(CaseData caseData) {
        Optional<Element<PartyDetails>> activeRespondent = caseData.getRespondents()
            .stream()
            .filter(x -> YesOrNo.Yes.equals(x.getValue().getResponse().getActiveRespondent()))
            .findFirst();
        return activeRespondent.filter(partyDetailsElement -> anyNonEmpty(partyDetailsElement
                                                                              .getValue()
                                                                              .getResponse()
                                                                              .getResSolInternationalElements()
        )).isPresent();
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        boolean mandatoryInfo = false;
        Optional<Element<PartyDetails>> activeRespondent = caseData.getRespondents()
            .stream()
            .filter(x -> YesOrNo.Yes.equals(x.getValue().getResponse().getActiveRespondent()))
            .findFirst();

        if (activeRespondent.isPresent()) {
            Optional<ResSolInternationalElements> solicitorInternationalElement = Optional.ofNullable(activeRespondent.get()
                                                                                                          .getValue()
                                                                                                          .getResponse()
                                                                                                          .getResSolInternationalElements());
            if (!solicitorInternationalElement.isEmpty() && checkInternationalElementMandatoryCompleted(
                solicitorInternationalElement)) {
                mandatoryInfo = true;
            }
        }
        return mandatoryInfo;
    }

    private boolean checkInternationalElementMandatoryCompleted(Optional<ResSolInternationalElements> internationalElements) {

        List<Optional<?>> fields = new ArrayList<>();
        if (internationalElements.isPresent()) {

            Optional<YesOrNo> reasonForChild = ofNullable(internationalElements.get().getInternationalElementChildInfo().getReasonForChild());
            fields.add(reasonForChild);
            if (reasonForChild.isPresent() && reasonForChild.equals(Optional.of((YesOrNo.Yes)))) {
                fields.add(ofNullable(internationalElements.get().getInternationalElementChildInfo().getReasonForChildDetails()));
            }

            Optional<YesOrNo> reasonForParent = ofNullable(internationalElements.get().getInternationalElementChildInfo().getReasonForParent());
            fields.add(reasonForParent);
            if (reasonForParent.isPresent() && reasonForParent.equals(Optional.of((YesOrNo.Yes)))) {
                fields.add(ofNullable(internationalElements.get().getInternationalElementParentInfo().getReasonForParentDetails()));
            }

            Optional<YesOrNo> reasonForJurisdiction = ofNullable(internationalElements.get()
                                                                     .getInternationalElementChildInfo().getReasonForJurisdiction());
            fields.add(reasonForJurisdiction);
            if (reasonForJurisdiction.isPresent() && reasonForJurisdiction.equals(Optional.of((YesOrNo.Yes)))) {
                fields.add(ofNullable(internationalElements.get().getInternationalElementParentInfo().getReasonForJurisdictionDetails()));
            }

            Optional<YesOrNo> requestToAuthority = ofNullable(internationalElements.get().getInternationalElementChildInfo().getRequestToAuthority());
            fields.add(requestToAuthority);
            if (requestToAuthority.isPresent() && requestToAuthority.equals(Optional.of((YesOrNo.Yes)))) {
                fields.add(ofNullable(internationalElements.get().getInternationalElementParentInfo().getRequestToAuthorityDetails()));
            }
        }

        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }
}
