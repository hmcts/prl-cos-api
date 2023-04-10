package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorKeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentEventErrorsEnum.KEEP_DETAILS_PRIVATE_ERROR;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.KEEP_DETAILS_PRIVATE;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
public class KeepDetailsPrivateChecker implements RespondentEventChecker {

    @Autowired
    RespondentTaskErrorService respondentTaskErrorService;

    @Override
    public boolean isStarted(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);
        boolean returnValue = false;
        if (response.isPresent()) {
            Optional<SolicitorKeepDetailsPrivate> solicitorKeepDetailsPrivate
                = ofNullable(response.get().getSolicitorKeepDetailsPriate());
            if (solicitorKeepDetailsPrivate.isPresent()) {
                returnValue = ofNullable(solicitorKeepDetailsPrivate.get().getRespKeepDetailsPrivate())
                    .filter(keepDetailsPrivate -> anyNonEmpty(
                        keepDetailsPrivate.getConfidentiality(),
                        keepDetailsPrivate.getOtherPeopleKnowYourContactDetails(),
                        keepDetailsPrivate.getConfidentialityList()
                    )).isPresent();

                if (!returnValue) {
                    returnValue = ofNullable(solicitorKeepDetailsPrivate.get().getRespKeepDetailsPrivateConfidentiality())
                        .filter(keepDetailsPrivate -> anyNonEmpty(
                            keepDetailsPrivate.getConfidentiality(),
                            keepDetailsPrivate.getOtherPeopleKnowYourContactDetails(),
                            keepDetailsPrivate.getConfidentialityList()
                        )).isPresent();
                }
            }
        }

        return returnValue;
    }

    @Override
    public boolean isFinished(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);
        boolean mandatoryInfo = false;

        if (response.isPresent()) {

            Optional<SolicitorKeepDetailsPrivate> keepDetailsPrivate = Optional.ofNullable(response.get()
                                                                                               .getSolicitorKeepDetailsPriate());
            if (!keepDetailsPrivate.isEmpty() && checkKeepDetailsPrivateMandatoryCompleted(keepDetailsPrivate)) {
                respondentTaskErrorService.removeError(KEEP_DETAILS_PRIVATE_ERROR);
                mandatoryInfo = true;
            }
        }
        respondentTaskErrorService.addEventError(KEEP_DETAILS_PRIVATE,
                                                 KEEP_DETAILS_PRIVATE_ERROR,
                                                 KEEP_DETAILS_PRIVATE_ERROR.getError());
        return mandatoryInfo;
    }

    private boolean checkKeepDetailsPrivateMandatoryCompleted(Optional<SolicitorKeepDetailsPrivate> keepDetailsPrivate) {

        List<Optional<?>> fields = new ArrayList<>();
        if (keepDetailsPrivate.isPresent()) {
            fields.add(ofNullable(keepDetailsPrivate.get().getRespKeepDetailsPrivate().getOtherPeopleKnowYourContactDetails()));
            Optional<YesOrNo> confidentiality = ofNullable(keepDetailsPrivate.get()
                                                               .getRespKeepDetailsPrivateConfidentiality().getConfidentiality());
            fields.add(confidentiality);
            if (confidentiality.isPresent() && confidentiality.equals(Optional.of(YesOrNo.Yes))) {
                fields.add(ofNullable(keepDetailsPrivate.get()
                                          .getRespKeepDetailsPrivateConfidentiality().getConfidentialityList()));
            }
        }
        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }
}
