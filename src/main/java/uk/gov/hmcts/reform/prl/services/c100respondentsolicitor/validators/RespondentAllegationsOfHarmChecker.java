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

        return response
            .filter(res -> anyNonEmpty(res.getRespondentAllegationsOfHarmData()
            )).isPresent();
    }

    @Override
    public boolean isFinished(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);
        boolean mandatoryInfo = false;

        if (response.isPresent()) {
            Optional<RespondentAllegationsOfHarmData> respondentAllegationsOfHarm = Optional.ofNullable(response.get()
                                                                                                            .getRespondentAllegationsOfHarmData());
            if (!respondentAllegationsOfHarm.isEmpty() && checkAllegationsOfHarmManadatoryCompleted(
                respondentAllegationsOfHarm)) {
                respondentTaskErrorService.removeError(ALLEGATION_OF_HARM_ERROR);
                mandatoryInfo = true;
            }
        }
        respondentTaskErrorService.addEventError(ALLEGATION_OF_HARM,
                                                 ALLEGATION_OF_HARM_ERROR,
                                                 ALLEGATION_OF_HARM_ERROR.getError());
        return mandatoryInfo;
    }

    private boolean checkAllegationsOfHarmManadatoryCompleted(Optional<RespondentAllegationsOfHarmData> respondentAllegationsOfHarmData) {

        List<Optional<?>> fields = new ArrayList<>();
        if (respondentAllegationsOfHarmData.isPresent()) {
            Optional<YesOrNo> respondentAohYesOrNo = ofNullable(respondentAllegationsOfHarmData.get().getRespAohYesOrNo());
            fields.add(respondentAohYesOrNo);
            if (respondentAohYesOrNo.isPresent() && YesOrNo.Yes.equals(respondentAohYesOrNo.get())) {
                Optional<YesOrNo> drugOrAlcoholAbuse = ofNullable(respondentAllegationsOfHarmData
                                                                      .get()
                                                                      .getRespAllegationsOfHarmInfo()
                                                                      .getRespondentDrugOrAlcoholAbuse());

                fields.add(drugOrAlcoholAbuse);
                if (drugOrAlcoholAbuse.isPresent() && YesOrNo.Yes.equals(drugOrAlcoholAbuse.get())) {
                    fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespAllegationsOfHarmInfo()
                                              .getRespondentDrugOrAlcoholAbuseDetails()));
                }
                populateOtherSafetyConcerns(respondentAllegationsOfHarmData, fields);

                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespAllegationsOfHarmInfo().getRespondentNonMolestationOrder()));
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespAllegationsOfHarmInfo().getRespondentOccupationOrder()));
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespAllegationsOfHarmInfo().getRespondentForcedMarriageOrder()));
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespAllegationsOfHarmInfo().getRespondentOtherInjunctiveOrder()));
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespAllegationsOfHarmInfo().getRespondentRestrainingOrder()));
                populateRespondentDomesticAbuse(respondentAllegationsOfHarmData, fields);
                populateRespondentChildAbuse(respondentAllegationsOfHarmData, fields);
                populateRespondentChildAbduction(respondentAllegationsOfHarmData, fields);

                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespOtherConcernsInfo().getChildHavingOtherFormOfContact()));
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespOtherConcernsInfo().getChildSpendingSupervisedTime()));
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespOtherConcernsInfo().getOrdersRespondentWantFromCourt()));
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespOtherConcernsInfo().getChildSpendingUnsupervisedTime()));
            }
        }
        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }

    private static void populateRespondentChildAbduction(Optional<RespondentAllegationsOfHarmData> respondentAllegationsOfHarmData,
                                                         List<Optional<?>> fields) {
        if (respondentAllegationsOfHarmData.isPresent()) {

            Optional<YesOrNo> respondentChildAbduction = ofNullable(respondentAllegationsOfHarmData
                                                                        .get()
                                                                        .getRespAllegationsOfHarmInfo()
                                                                        .getIsRespondentChildAbduction());
            fields.add(respondentChildAbduction);
            if (respondentChildAbduction.isPresent() && YesOrNo.Yes.equals(respondentChildAbduction.get())) {
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespChildAbductionInfo().getReasonForChildAbductionBelief()));
                Optional<YesOrNo> previousThreats = ofNullable(respondentAllegationsOfHarmData
                                                                   .get()
                                                                   .getRespChildAbductionInfo()
                                                                   .getPreviousThreatsForChildAbduction());
                fields.add(previousThreats);
                if (previousThreats.isPresent() && YesOrNo.Yes.equals(previousThreats.get())) {
                    fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespChildAbductionInfo()
                                              .getPreviousThreatsForChildAbductionDetails()));
                }
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespChildAbductionInfo().getWhereIsChild()));
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespChildAbductionInfo().getHasPassportOfficeNotified()));
                Optional<YesOrNo> orgInvolvedInPreviousAbduction = ofNullable(respondentAllegationsOfHarmData
                                                                                  .get()
                                                                                  .getRespChildAbductionInfo()
                                                                                  .getAnyOrgInvolvedInPreviousAbduction());
                fields.add(orgInvolvedInPreviousAbduction);
                if (orgInvolvedInPreviousAbduction.isPresent() && YesOrNo.Yes.equals(orgInvolvedInPreviousAbduction.get())) {
                    fields.add(ofNullable(respondentAllegationsOfHarmData
                                              .get()
                                              .getRespChildAbductionInfo()
                                              .getAnyOrgInvolvedInPreviousAbductionDetails()));
                }
                Optional<YesOrNo> childHasPassport = ofNullable(respondentAllegationsOfHarmData
                                                                    .get().getRespChildAbductionInfo().getChildrenHavePassport());
                fields.add(childHasPassport);
                if (childHasPassport.isPresent() && YesOrNo.Yes.equals(childHasPassport.get())) {
                    fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespChildAbductionInfo()
                                              .getChildrenHaveMoreThanOnePassport()));
                    List<WhomConsistPassportList> whoConsistPassportList = respondentAllegationsOfHarmData
                        .get().getRespChildAbductionInfo().getWhoHasChildPassport();
                    fields.add(ofNullable(whoConsistPassportList));
                    for (WhomConsistPassportList whomConsistPassport : whoConsistPassportList) {
                        if (whomConsistPassport.equals(WhomConsistPassportList.otherPeople)) {
                            fields.add(ofNullable(respondentAllegationsOfHarmData.get()
                                                      .getRespChildAbductionInfo().getWhoHasChildPassportOther()));

                        }
                    }
                }
            }
        }
    }

    private static void populateRespondentChildAbuse(Optional<RespondentAllegationsOfHarmData> respondentAllegationsOfHarmData,
                                                     List<Optional<?>> fields) {
        if (respondentAllegationsOfHarmData.isPresent()) {

            Optional<YesOrNo> respondentChildAbuse = ofNullable(respondentAllegationsOfHarmData.get()
                                                                    .getRespAllegationsOfHarmInfo().getRespondentChildAbuse());
            fields.add(respondentChildAbuse);
            if (respondentChildAbuse.isPresent() && YesOrNo.Yes.equals(respondentChildAbuse.get())) {
                List<Behaviours> childAbuseBehaviour = respondentAllegationsOfHarmData.get()
                    .getRespChildAbuseInfo()
                    .stream()
                    .map(Element::getValue)
                    .collect(Collectors.toList());
                for (Behaviours childAbuse : childAbuseBehaviour) {
                    fields.add(ofNullable(childAbuse.getTypesOfAbuse()));
                    fields.add(ofNullable(childAbuse.getNatureOfBehaviour()));
                    fields.add(ofNullable(childAbuse.getAbuseStartDateAndLength()));
                    fields.add(ofNullable(childAbuse.getRespondentSoughtHelp()));
                    fields.add(ofNullable(childAbuse.getRespondentTypeOfHelp()));
                }
            }
        }
    }

    private static void populateRespondentDomesticAbuse(Optional<RespondentAllegationsOfHarmData> respondentAllegationsOfHarmData,
                                                        List<Optional<?>> fields) {
        if (respondentAllegationsOfHarmData.isPresent()) {

            Optional<YesOrNo> respondentDomesticAbuse = ofNullable(respondentAllegationsOfHarmData.get()
                                                                       .getRespAllegationsOfHarmInfo().getRespondentDomesticAbuse());
            fields.add(respondentDomesticAbuse);
            if (respondentDomesticAbuse.isPresent() && YesOrNo.Yes.equals(respondentDomesticAbuse.get())) {
                List<Behaviours> domesticAbuseBehaviour = respondentAllegationsOfHarmData.get()
                    .getRespDomesticAbuseInfo()
                    .stream()
                    .map(Element::getValue)
                    .collect(Collectors.toList());
                for (Behaviours domesticAbuse : domesticAbuseBehaviour) {
                    fields.add(ofNullable(domesticAbuse.getTypesOfAbuse()));
                    fields.add(ofNullable(domesticAbuse.getNatureOfBehaviour()));
                    fields.add(ofNullable(domesticAbuse.getAbuseStartDateAndLength()));
                    fields.add(ofNullable(domesticAbuse.getRespondentSoughtHelp()));
                    fields.add(ofNullable(domesticAbuse.getRespondentTypeOfHelp()));
                }
            }
        }
    }

    private static void populateOtherSafetyConcerns(Optional<RespondentAllegationsOfHarmData> respondentAllegationsOfHarmData,
                                                    List<Optional<?>> fields) {
        if (respondentAllegationsOfHarmData.isPresent()) {

            Optional<YesOrNo> otherSafetyConcerns = ofNullable(respondentAllegationsOfHarmData
                                                                   .get()
                                                                   .getRespAllegationsOfHarmInfo()
                                                                   .getRespondentOtherSafetyConcerns());
            fields.add(otherSafetyConcerns);
            if (otherSafetyConcerns.isPresent() && YesOrNo.Yes.equals(otherSafetyConcerns.get())) {
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespAllegationsOfHarmInfo()
                                          .getRespondentOtherSafetyConcernsDetails()));
            }
        }
    }
}
