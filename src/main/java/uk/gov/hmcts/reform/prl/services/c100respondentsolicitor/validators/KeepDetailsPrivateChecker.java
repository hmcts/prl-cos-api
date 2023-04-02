package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorKeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
public class KeepDetailsPrivateChecker implements RespondentEventChecker {

    @Override
    public boolean isStarted(CaseData caseData, String respondent) {
        Optional<Response> response = findResponse(caseData, respondent);

        return response
            .filter(res -> anyNonEmpty(res.getSolicitorKeepDetailsPriate()
            )).isPresent();
    }

    @Override
    public boolean isFinished(CaseData caseData, String respondent) {
        boolean mandatoryInfo = false;
        Optional<Response> response = findResponse(caseData, respondent);

        if (response.isPresent()) {

            Optional<SolicitorKeepDetailsPrivate> keepDetailsPrivate = Optional.ofNullable(response.get()
                                                                                               .getSolicitorKeepDetailsPriate());
            if (!keepDetailsPrivate.isEmpty() && checkKeepDetailsPrivateMandatoryCompleted(keepDetailsPrivate)) {
                mandatoryInfo = true;
            }
        }
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
