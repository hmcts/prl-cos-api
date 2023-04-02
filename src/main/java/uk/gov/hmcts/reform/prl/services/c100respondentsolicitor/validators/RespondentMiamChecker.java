package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorMiam;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Slf4j
@Service
public class RespondentMiamChecker implements RespondentEventChecker {
    @Override
    public boolean isStarted(CaseData caseData, String respondent) {
        Optional<Response> response = findResponse(caseData, respondent);

        return response
            .filter(res -> anyNonEmpty(res.getSolicitorMiam()
            )).isPresent();
    }

    @Override
    public boolean isFinished(CaseData caseData, String respondent) {
        boolean mandatoryInfo = false;

        Optional<Response> response = findResponse(caseData, respondent);

        if (response.isPresent()) {
            Optional<SolicitorMiam> miam
                = Optional.ofNullable(response.get().getSolicitorMiam());
            if (!miam.isEmpty()) {
                log.info("before checking miam mandatory info if loop...");
                if (checkMiamManadatoryCompleted(miam)) {
                    log.info("inside checking miam mandatory info if loop...");
                    mandatoryInfo = true;
                }
            }
        }
        return mandatoryInfo;
    }

    private boolean checkMiamManadatoryCompleted(Optional<SolicitorMiam> miam) {
        List<Optional<?>> fields = new ArrayList<>();
        log.info("entering miam checker if loop...");
        if (miam.isPresent()) {
            fields.add(ofNullable(miam.get().getRespSolHaveYouAttendedMiam().getAttendedMiam()));
            YesOrNo attendMiam = miam.get().getRespSolHaveYouAttendedMiam().getAttendedMiam();
            if (attendMiam.equals(YesOrNo.No)) {
                fields.add(ofNullable(miam.get().getRespSolWillingnessToAttendMiam().getWillingToAttendMiam()));
                YesOrNo willingToAttendMiam = miam.get().getRespSolWillingnessToAttendMiam().getWillingToAttendMiam();
                if (willingToAttendMiam.equals(YesOrNo.No)) {
                    fields.add(ofNullable(miam.get().getRespSolWillingnessToAttendMiam().getReasonNotAttendingMiam()));
                }
            }
        }
        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }
}
