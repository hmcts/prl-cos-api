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
        Optional<Element<PartyDetails>> activeRespondent = Optional.empty();
        activeRespondent = caseData.getRespondents()
            .stream()
            .filter(x -> YesOrNo.Yes.equals(x.getValue().getResponse().getActiveRespondent()))
            .findFirst();
        return anyNonEmpty(activeRespondent
                               .get()
                               .getValue()
                               .getResponse()
                               .getRespondentAllegationsOfHarmData()
        );
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        boolean mandatoryInfo = false;

        Optional<Element<PartyDetails>> activeRespondent = Optional.empty();
        activeRespondent = caseData.getRespondents()
            .stream()
            .filter(x -> YesOrNo.Yes.equals(x.getValue().getResponse().getActiveRespondent()))
            .findFirst();

        Optional<RespondentAllegationsOfHarmData> respondentAllegationsOfHarm = Optional.ofNullable(activeRespondent.get()
                                                                                                            .getValue()
                                                                                                            .getResponse()
                                                                                                            .getRespondentAllegationsOfHarmData());
        if (!respondentAllegationsOfHarm.isEmpty()) {
            if (checkAllegationsOfHarmManadatoryCompleted(respondentAllegationsOfHarm)) {
                mandatoryInfo = true;
            }
        }
        return mandatoryInfo;
    }

    private boolean checkAllegationsOfHarmManadatoryCompleted(Optional<RespondentAllegationsOfHarmData> respondentAllegationsOfHarmData) {

        List<Optional<?>> fields = new ArrayList<>();
        Optional<YesOrNo> respondentAohYesOrNo = ofNullable(respondentAllegationsOfHarmData.get().getRespondentAohYesOrNo());
        fields.add(respondentAohYesOrNo);
        if (respondentAohYesOrNo.isPresent() && respondentAohYesOrNo.equals(YesOrNo.Yes)) {
            Optional<YesOrNo> drugOrAlcoholAbuse = ofNullable(respondentAllegationsOfHarmData
                                                                  .get()
                                                                  .getRespondentAllegationsOfHarmInfo()
                                                                  .getRespondentDrugOrAlcoholAbuse());

            fields.add(drugOrAlcoholAbuse);
            if (drugOrAlcoholAbuse.isPresent() && drugOrAlcoholAbuse.equals(YesOrNo.Yes)) {
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentAllegationsOfHarmInfo()
                    .getRespondentDrugOrAlcoholAbuseDetails()));
            }
            Optional<YesOrNo> otherSafetyConcerns = ofNullable(respondentAllegationsOfHarmData
                                                                   .get()
                                                                   .getRespondentAllegationsOfHarmInfo()
                                                                   .getRespondentOtherSafetyConcerns());
            fields.add(otherSafetyConcerns);
            if (otherSafetyConcerns.isPresent() && otherSafetyConcerns.equals(YesOrNo.Yes)) {
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentAllegationsOfHarmInfo()
                                          .getRespondentOtherSafetyConcernsDetails()));
            }

            fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentAllegationsOfHarmInfo().getRespondentNonMolestationOrder()));
            fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentAllegationsOfHarmInfo().getRespondentOccupationOrder()));
            fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentAllegationsOfHarmInfo().getRespondentForcedMarriageOrder()));
            fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentAllegationsOfHarmInfo().getRespondentOtherInjunctiveOrder()));
            fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentAllegationsOfHarmInfo().getRespondentRestrainingOrder()));
            Optional<YesOrNo> respondentDomesticAbuse = ofNullable(respondentAllegationsOfHarmData.get()
                                                                       .getRespondentAllegationsOfHarmInfo().getRespondentDomesticAbuse());
            fields.add(respondentDomesticAbuse);
            if (respondentDomesticAbuse.isPresent() && respondentDomesticAbuse.equals(YesOrNo.Yes)) {
                List<Behaviours> domesticAbuseBehaviour = respondentAllegationsOfHarmData.get()
                    .getRespondentDomesticAbuseBehaviourInfo()
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
            Optional<YesOrNo> respondentChildAbuse = ofNullable(respondentAllegationsOfHarmData.get()
                                                                    .getRespondentAllegationsOfHarmInfo().getRespondentChildAbuse());
            fields.add(respondentChildAbuse);
            if (respondentChildAbuse.isPresent() && respondentChildAbuse.equals(YesOrNo.Yes)) {
                List<Behaviours> childAbuseBehaviour = respondentAllegationsOfHarmData.get()
                    .getRespondentChildAbuseBehaviourInfo()
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
            Optional<YesOrNo> respondentChildAbduction = ofNullable(respondentAllegationsOfHarmData
                                                                        .get()
                                                                        .getRespondentAllegationsOfHarmInfo()
                                                                        .getRespondentChildAbduction());
            fields.add(respondentChildAbduction);
            if (respondentChildAbduction.isPresent() && respondentChildAbduction.equals(YesOrNo.Yes)) {
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentChildAbductionInfo().getReasonForChildAbductionBelief()));
                Optional<YesOrNo> previousThreats = ofNullable(respondentAllegationsOfHarmData
                                                                   .get()
                                                                   .getRespondentChildAbductionInfo()
                                                                   .getPreviousThreatsForChildAbduction());
                fields.add(previousThreats);
                if (previousThreats.isPresent() && previousThreats.equals(YesOrNo.Yes)) {
                    fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentChildAbductionInfo()
                                              .getPreviousThreatsForChildAbductionDetails()));
                }
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentChildAbductionInfo().getWhereIsChild()));
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentChildAbductionInfo().getHasPassportOfficeNotified()));
                Optional<YesOrNo> orgInvolvedInPreviousAbduction = ofNullable(respondentAllegationsOfHarmData
                                                                                  .get()
                                                                                  .getRespondentChildAbductionInfo()
                                                                                  .getAnyOrgInvolvedInPreviousAbduction());
                fields.add(orgInvolvedInPreviousAbduction);
                if (orgInvolvedInPreviousAbduction.isPresent() && orgInvolvedInPreviousAbduction.equals(YesOrNo.Yes)) {
                    fields.add(ofNullable(respondentAllegationsOfHarmData
                                              .get()
                                              .getRespondentChildAbductionInfo()
                                              .getAnyOrgInvolvedInPreviousAbductionDetails()));
                }
                Optional<YesOrNo> childHasPassport = ofNullable(respondentAllegationsOfHarmData
                                                                    .get().getRespondentChildAbductionInfo().getChildrenHavePassport());
                fields.add(childHasPassport);
                if (childHasPassport.isPresent() && childHasPassport.equals(YesOrNo.Yes)) {
                    fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentChildAbductionInfo()
                                              .getChildrenHaveMoreThanOnePassport()));
                    List<WhomConsistPassportList> whoConsistPassportList = respondentAllegationsOfHarmData
                        .get().getRespondentChildAbductionInfo().getWhoHasChildPassport();
                    fields.add(ofNullable(whoConsistPassportList));
                    for (WhomConsistPassportList whomConsistPassport : whoConsistPassportList) {
                        if (whomConsistPassport.equals(WhomConsistPassportList.otherPeople)) {
                            fields.add(ofNullable(respondentAllegationsOfHarmData.get()
                                                      .getRespondentChildAbductionInfo().getWhoHasChildPassportOther()));

                        }
                    }
                }
            }

            fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentOtherConcernsInfo().getChildHavingOtherFormOfContact()));
            fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentOtherConcernsInfo().getChildSpendingSupervisedTime()));
            fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentOtherConcernsInfo().getOrdersRespondentWantFromCourt()));
            fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentOtherConcernsInfo().getChildSpendingUnsupervisedTime()));
        }

        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }
}
