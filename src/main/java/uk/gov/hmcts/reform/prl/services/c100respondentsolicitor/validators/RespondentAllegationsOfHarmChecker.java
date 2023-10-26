package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.respondentsolicitor.WhomConsistPassportList;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Behaviours;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarmData;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentEventErrorsEnum.ALLEGATION_OF_HARM_ERROR;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.ALLEGATION_OF_HARM;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
public class RespondentAllegationsOfHarmChecker implements RespondentEventChecker {
    @Autowired
    RespondentTaskErrorService respondentTaskErrorService;

    @Override
    public boolean isStarted(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);

        return response.filter(value -> ofNullable(value.getRespondentAllegationsOfHarmData())
            .filter(allegations -> anyNonEmpty(
                allegations.getRespAohYesOrNo()
            )).isPresent()).isPresent();
    }

    @Override
    public boolean isFinished(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);

        if (response.isPresent()) {
            Optional<RespondentAllegationsOfHarmData> respondentAllegationsOfHarm = Optional.ofNullable(response.get()
                                                                                                            .getRespondentAllegationsOfHarmData());
            if (respondentAllegationsOfHarm.isPresent() && checkAllegationsOfHarmManadatoryCompleted(
                respondentAllegationsOfHarm.get())) {
                respondentTaskErrorService.removeError(ALLEGATION_OF_HARM_ERROR);
                return true;
            }
        }
        respondentTaskErrorService.addEventError(
            ALLEGATION_OF_HARM,
            ALLEGATION_OF_HARM_ERROR,
            ALLEGATION_OF_HARM_ERROR.getError()
        );
        return false;
    }

    private boolean checkAllegationsOfHarmManadatoryCompleted(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {

        List<Optional<?>> fields = new ArrayList<>();
        Optional<YesOrNo> respondentAohYesOrNo = ofNullable(respondentAllegationsOfHarmData.getRespAohYesOrNo());
        fields.add(respondentAohYesOrNo);
        if (respondentAohYesOrNo.isPresent() && YesOrNo.Yes.equals(respondentAohYesOrNo.get())) {
            Optional<YesOrNo> drugOrAlcoholAbuse = ofNullable(respondentAllegationsOfHarmData
                                                                  .getRespAllegationsOfHarmInfo()
                                                                  .getRespondentDrugOrAlcoholAbuse());

            fields.add(drugOrAlcoholAbuse);
            if (drugOrAlcoholAbuse.isPresent() && YesOrNo.Yes.equals(drugOrAlcoholAbuse.get())) {
                fields.add(ofNullable(respondentAllegationsOfHarmData.getRespAllegationsOfHarmInfo()
                                          .getRespondentDrugOrAlcoholAbuseDetails()));
            }
            populateOtherSafetyConcerns(respondentAllegationsOfHarmData, fields);

            fields.add(ofNullable(respondentAllegationsOfHarmData.getRespAllegationsOfHarmInfo().getRespondentNonMolestationOrder()));
            fields.add(ofNullable(respondentAllegationsOfHarmData.getRespAllegationsOfHarmInfo().getRespondentOccupationOrder()));
            fields.add(ofNullable(respondentAllegationsOfHarmData.getRespAllegationsOfHarmInfo().getRespondentForcedMarriageOrder()));
            fields.add(ofNullable(respondentAllegationsOfHarmData.getRespAllegationsOfHarmInfo().getRespondentOtherInjunctiveOrder()));
            fields.add(ofNullable(respondentAllegationsOfHarmData.getRespAllegationsOfHarmInfo().getRespondentRestrainingOrder()));
            populateRespondentDomesticAbuse(respondentAllegationsOfHarmData, fields);
            populateRespondentChildAbuse(respondentAllegationsOfHarmData, fields);
            populateRespondentChildAbduction(respondentAllegationsOfHarmData, fields);

            fields.add(ofNullable(respondentAllegationsOfHarmData.getRespOtherConcernsInfo().getChildHavingOtherFormOfContact()));
            fields.add(ofNullable(respondentAllegationsOfHarmData.getRespOtherConcernsInfo().getChildSpendingSupervisedTime()));
            fields.add(ofNullable(respondentAllegationsOfHarmData.getRespOtherConcernsInfo().getOrdersRespondentWantFromCourt()));
            fields.add(ofNullable(respondentAllegationsOfHarmData.getRespOtherConcernsInfo().getChildSpendingUnsupervisedTime()));
        }

        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }

    private static void populateRespondentChildAbduction(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData,
                                                         List<Optional<?>> fields) {

        Optional<YesOrNo> respondentChildAbduction = ofNullable(respondentAllegationsOfHarmData
                                                                    .getRespAllegationsOfHarmInfo()
                                                                    .getIsRespondentChildAbduction());
        fields.add(respondentChildAbduction);
        if (respondentChildAbduction.isPresent() && YesOrNo.Yes.equals(respondentChildAbduction.get())) {
            fields.add(ofNullable(respondentAllegationsOfHarmData.getRespChildAbductionInfo().getReasonForChildAbductionBelief()));
            Optional<YesOrNo> previousThreats = ofNullable(respondentAllegationsOfHarmData
                                                               .getRespChildAbductionInfo()
                                                               .getPreviousThreatsForChildAbduction());
            fields.add(previousThreats);
            if (previousThreats.isPresent() && YesOrNo.Yes.equals(previousThreats.get())) {
                fields.add(ofNullable(respondentAllegationsOfHarmData.getRespChildAbductionInfo()
                                          .getPreviousThreatsForChildAbductionDetails()));
            }
            fields.add(ofNullable(respondentAllegationsOfHarmData.getRespChildAbductionInfo().getWhereIsChild()));
            fields.add(ofNullable(respondentAllegationsOfHarmData.getRespChildAbductionInfo().getHasPassportOfficeNotified()));
            Optional<YesOrNo> orgInvolvedInPreviousAbduction = ofNullable(respondentAllegationsOfHarmData
                                                                              .getRespChildAbductionInfo()
                                                                              .getAnyOrgInvolvedInPreviousAbduction());
            fields.add(orgInvolvedInPreviousAbduction);
            if (orgInvolvedInPreviousAbduction.isPresent() && YesOrNo.Yes.equals(orgInvolvedInPreviousAbduction.get())) {
                fields.add(ofNullable(respondentAllegationsOfHarmData
                                          .getRespChildAbductionInfo()
                                          .getAnyOrgInvolvedInPreviousAbductionDetails()));
            }
            Optional<YesOrNo> childHasPassport = ofNullable(respondentAllegationsOfHarmData
                                                                .getRespChildAbductionInfo().getChildrenHavePassport());
            fields.add(childHasPassport);
            populatePassportDetails(respondentAllegationsOfHarmData, fields, childHasPassport);
        }
    }

    private static void populatePassportDetails(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData,
                                                List<Optional<?>> fields, Optional<YesOrNo> childHasPassport) {
        if (childHasPassport.isPresent() && YesOrNo.Yes.equals(childHasPassport.get())) {
            fields.add(ofNullable(respondentAllegationsOfHarmData.getRespChildAbductionInfo()
                                      .getChildrenHaveMoreThanOnePassport()));
            List<WhomConsistPassportList> whoConsistPassportList = respondentAllegationsOfHarmData
                .getRespChildAbductionInfo().getWhoHasChildPassport();
            fields.add(ofNullable(whoConsistPassportList));
            for (WhomConsistPassportList whomConsistPassport : whoConsistPassportList) {
                if (whomConsistPassport.equals(WhomConsistPassportList.otherPeople)) {
                    fields.add(ofNullable(respondentAllegationsOfHarmData
                                              .getRespChildAbductionInfo().getWhoHasChildPassportOther()));

                }
            }
        }
    }

    private static void populateRespondentChildAbuse(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData,
                                                     List<Optional<?>> fields) {
        Optional<YesOrNo> respondentChildAbuse = ofNullable(respondentAllegationsOfHarmData
                                                                .getRespAllegationsOfHarmInfo().getRespondentChildAbuse());
        fields.add(respondentChildAbuse);
        if (respondentChildAbuse.isPresent() && YesOrNo.Yes.equals(respondentChildAbuse.get())) {
            List<Behaviours> childAbuseBehaviour = respondentAllegationsOfHarmData
                .getRespChildAbuseInfo()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
            for (Behaviours childAbuse : childAbuseBehaviour) {
                fields.add(ofNullable(childAbuse.getTypesOfAbuse()));
                fields.add(ofNullable(childAbuse.getNatureOfBehaviour()));
                fields.add(ofNullable(childAbuse.getAbuseStartDateAndLength()));
                fields.add(ofNullable(childAbuse.getRespondentSoughtHelp()));
                if (YesOrNo.Yes.equals(childAbuse.getRespondentSoughtHelp())) {
                    fields.add(ofNullable(childAbuse.getRespondentTypeOfHelp()));
                }
            }
        }
    }

    private static void populateRespondentDomesticAbuse(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData,
                                                        List<Optional<?>> fields) {
        Optional<YesOrNo> respondentDomesticAbuse = ofNullable(respondentAllegationsOfHarmData
                                                                   .getRespAllegationsOfHarmInfo().getRespondentDomesticAbuse());
        fields.add(respondentDomesticAbuse);
        if (respondentDomesticAbuse.isPresent() && YesOrNo.Yes.equals(respondentDomesticAbuse.get())) {
            List<Behaviours> domesticAbuseBehaviour = respondentAllegationsOfHarmData
                .getRespDomesticAbuseInfo()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
            for (Behaviours domesticAbuse : domesticAbuseBehaviour) {
                fields.add(ofNullable(domesticAbuse.getTypesOfAbuse()));
                fields.add(ofNullable(domesticAbuse.getNatureOfBehaviour()));
                fields.add(ofNullable(domesticAbuse.getAbuseStartDateAndLength()));
                fields.add(ofNullable(domesticAbuse.getRespondentSoughtHelp()));
                if (YesOrNo.Yes.equals(domesticAbuse.getRespondentSoughtHelp())) {
                    fields.add(ofNullable(domesticAbuse.getRespondentTypeOfHelp()));
                }
            }
        }
    }

    private static void populateOtherSafetyConcerns(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData,
                                                    List<Optional<?>> fields) {
        Optional<YesOrNo> otherSafetyConcerns = ofNullable(respondentAllegationsOfHarmData
                                                               .getRespAllegationsOfHarmInfo()
                                                               .getRespondentOtherSafetyConcerns());
        fields.add(otherSafetyConcerns);
        if (otherSafetyConcerns.isPresent() && YesOrNo.Yes.equals(otherSafetyConcerns.get())) {
            fields.add(ofNullable(respondentAllegationsOfHarmData.getRespAllegationsOfHarmInfo()
                                      .getRespondentOtherSafetyConcernsDetails()));
        }
    }
}
