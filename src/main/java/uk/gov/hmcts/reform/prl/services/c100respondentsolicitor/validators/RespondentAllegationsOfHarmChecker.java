package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.respondentsolicitor.WhomConsistPassportList;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Behaviours;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarmData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
public class RespondentAllegationsOfHarmChecker implements RespondentEventChecker {
    @Override
    public boolean isStarted(CaseData caseData) {
        Optional<Element<PartyDetails>> activeRespondent = caseData.getRespondents()
            .stream()
            .filter(x -> YesOrNo.Yes.equals(x.getValue().getResponse().getActiveRespondent()))
            .findFirst();
        return activeRespondent.filter(partyDetailsElement -> anyNonEmpty(partyDetailsElement
                                                                              .getValue()
                                                                              .getResponse()
                                                                              .getRespondentAllegationsOfHarmData()
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
            Optional<RespondentAllegationsOfHarmData> respondentAllegationsOfHarm = Optional.ofNullable(activeRespondent.get()
                                                                                                            .getValue()
                                                                                                            .getResponse()
                                                                                                            .getRespondentAllegationsOfHarmData());
            if (!respondentAllegationsOfHarm.isEmpty() && checkAllegationsOfHarmManadatoryCompleted(
                respondentAllegationsOfHarm)) {

                mandatoryInfo = true;
            }
        }
        return mandatoryInfo;
    }

    private boolean checkAllegationsOfHarmManadatoryCompleted(Optional<RespondentAllegationsOfHarmData> respondentAllegationsOfHarmData) {

        List<Optional<?>> fields = new ArrayList<>();
        if (respondentAllegationsOfHarmData.isPresent()) {
            Optional<YesOrNo> respondentAohYesOrNo = ofNullable(respondentAllegationsOfHarmData.get().getRespAohYesOrNo());
            fields.add(respondentAohYesOrNo);
            if (respondentAohYesOrNo.isPresent() && respondentAohYesOrNo.equals(Optional.of((YesOrNo.Yes)))) {
                Optional<YesOrNo> drugOrAlcoholAbuse = ofNullable(respondentAllegationsOfHarmData
                                                                      .get()
                                                                      .getRespAllegationsOfHarmInfo()
                                                                      .getRespondentDrugOrAlcoholAbuse());

                fields.add(drugOrAlcoholAbuse);
                if (drugOrAlcoholAbuse.isPresent() && drugOrAlcoholAbuse.equals(Optional.of((YesOrNo.Yes)))) {
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
            if (respondentChildAbduction.isPresent() && respondentChildAbduction.equals(Optional.of((YesOrNo.Yes)))) {
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespChildAbductionInfo().getReasonForChildAbductionBelief()));
                Optional<YesOrNo> previousThreats = ofNullable(respondentAllegationsOfHarmData
                                                                   .get()
                                                                   .getRespChildAbductionInfo()
                                                                   .getPreviousThreatsForChildAbduction());
                fields.add(previousThreats);
                if (previousThreats.isPresent() && previousThreats.equals(Optional.of((YesOrNo.Yes)))) {
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
                if (orgInvolvedInPreviousAbduction.isPresent() && orgInvolvedInPreviousAbduction.equals(Optional.of(
                    YesOrNo.Yes))) {
                    fields.add(ofNullable(respondentAllegationsOfHarmData
                                              .get()
                                              .getRespChildAbductionInfo()
                                              .getAnyOrgInvolvedInPreviousAbductionDetails()));
                }
                Optional<YesOrNo> childHasPassport = ofNullable(respondentAllegationsOfHarmData
                                                                    .get().getRespChildAbductionInfo().getChildrenHavePassport());
                fields.add(childHasPassport);
                if (childHasPassport.isPresent() && childHasPassport.equals(Optional.of((YesOrNo.Yes)))) {
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
            if (respondentChildAbuse.isPresent() && respondentChildAbuse.equals(Optional.of((YesOrNo.Yes)))) {
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
            if (respondentDomesticAbuse.isPresent() && respondentDomesticAbuse.equals(Optional.of((YesOrNo.Yes)))) {
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
            if (otherSafetyConcerns.isPresent() && otherSafetyConcerns.equals(Optional.of((YesOrNo.Yes)))) {
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespAllegationsOfHarmInfo()
                                          .getRespondentOtherSafetyConcernsDetails()));
            }
        }
    }
}
