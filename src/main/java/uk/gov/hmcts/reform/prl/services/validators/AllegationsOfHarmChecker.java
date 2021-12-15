package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Behaviours;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.ALLEGATIONS_OF_HARM_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.ATTENDING_THE_HEARING_ERROR;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
public class AllegationsOfHarmChecker implements EventChecker {


    @Autowired
    TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        boolean finished = validateFields(caseData);

        if (finished) {
            taskErrorService.removeError(ALLEGATIONS_OF_HARM_ERROR);
            return true;
        }
        taskErrorService.addEventError(ALLEGATIONS_OF_HARM, ALLEGATIONS_OF_HARM_ERROR,
                                       ALLEGATIONS_OF_HARM_ERROR.getError());
        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {

        Optional<YesOrNo> allegationsOfHarm = ofNullable(caseData.getAllegationsOfHarmYesNo());

        return allegationsOfHarm.isPresent() && allegationsOfHarm.get().equals(YES);
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }



    private boolean validateFields(CaseData caseData) {
        Optional<YesOrNo> allegationsOfHarmYesNo = ofNullable(caseData.getAllegationsOfHarmYesNo());

        boolean isFinished;

        if (allegationsOfHarmYesNo.isPresent() && allegationsOfHarmYesNo.get().equals(YES)) {

            boolean behavioursCompleted = false;

            if (abusePresent(caseData)) {
                Optional<List<Element<Behaviours>>> behavioursWrapped = ofNullable(caseData.getBehaviours());
                List<Behaviours> behaviours = behavioursWrapped.get()
                                                .stream()
                                                .map(Element::getValue)
                                                .collect(Collectors.toList());

                for (Behaviours behaviour : behaviours) {
                    behavioursCompleted = validateBehaviour(behaviour);
                    if (!behavioursCompleted) {
                        return false;
                    }
                }
            }

            Optional<YesOrNo> ordersNonMolestation = ofNullable(caseData.getOrdersNonMolestation());
            Optional<YesOrNo> ordersOccupation = ofNullable(caseData.getOrdersOccupation());
            Optional<YesOrNo> ordersForcedMarriageProtection = ofNullable(caseData.getOrdersForcedMarriageProtection());
            Optional<YesOrNo> ordersRestraining = ofNullable(caseData.getOrdersRestraining());
            Optional<YesOrNo> ordersOtherInjunctive = ofNullable(caseData.getOrdersOtherInjunctive());
            Optional<YesOrNo> ordersUndertakingInPlace = ofNullable(caseData.getOrdersUndertakingInPlace());

            boolean previousOrders = ordersNonMolestation.isPresent() &&
                                     ordersOccupation.isPresent() &&
                                     ordersForcedMarriageProtection.isPresent() &&
                                     ordersRestraining.isPresent() &&
                                     ordersOtherInjunctive.isPresent() &&
                                     ordersUndertakingInPlace.isPresent();

            isFinished = validateDomesticAbuseSection(caseData) &&
                         validateOrders(caseData) &&
                         previousOrders &&
                         behavioursCompleted &&
                         validateAbductionSection(caseData) &&
                         validateOtherConcerns(caseData);

        }
        else {
            isFinished = allegationsOfHarmYesNo.isPresent();
        }

        if (isFinished) {
            return true;
        }

        return false;
    }


    private boolean abusePresent(CaseData caseData) {
        Optional<YesOrNo> domesticAbuse = ofNullable(caseData.getAllegationsOfHarmDomesticAbuseYesNo());
        Optional<YesOrNo> childAbduction = ofNullable(caseData.getAllegationsOfHarmChildAbductionYesNo());
        Optional<YesOrNo> childAbuse = ofNullable(caseData.getAllegationsOfHarmChildAbuseYesNo());
        Optional<YesOrNo> substanceAbuse = ofNullable(caseData.getAllegationsOfHarmSubstanceAbuseYesNo());
        Optional<YesOrNo> otherAbuse = ofNullable(caseData.getAllegationsOfHarmOtherConcernsYesNo());

        return anyNonEmpty(
                    domesticAbuse,
                    childAbduction,
                    childAbuse,
                    substanceAbuse,
                    otherAbuse);
    }


    private boolean validateDomesticAbuseSection(CaseData caseData) {
        Optional<YesOrNo> domesticAbuse = ofNullable(caseData.getAllegationsOfHarmDomesticAbuseYesNo());
        Optional<List<ApplicantOrChildren>> physicalAbuseVictim = ofNullable(caseData.getPhysicalAbuseVictim());
        Optional<List<ApplicantOrChildren>> emotionalAbuseVictim = ofNullable(caseData.getEmotionalAbuseVictim());
        Optional<List<ApplicantOrChildren>>  psychologicalAbuseVictim = ofNullable(caseData.getPsychologicalAbuseVictim());
        Optional<List<ApplicantOrChildren>>  sexualAbuseVictim = ofNullable(caseData.getSexualAbuseVictim());
        Optional<List<ApplicantOrChildren>>  financialAbuseVictim = ofNullable(caseData.getPhysicalAbuseVictim());

        boolean abuseVictimCompleted = true;

        if (domesticAbuse.isPresent() && domesticAbuse.get().equals(YES)) {
            abuseVictimCompleted = physicalAbuseVictim.isPresent() ||
                emotionalAbuseVictim.isPresent() ||
                psychologicalAbuseVictim.isPresent() ||
                sexualAbuseVictim.isPresent() ||
                financialAbuseVictim.isPresent();
        }
        return abuseVictimCompleted;
    }


    private boolean validateAbductionSection(CaseData caseData) {

        Optional<YesOrNo> childAbduction = ofNullable(caseData.getAllegationsOfHarmChildAbductionYesNo());
        Optional<String> childAbductionReasons = ofNullable(caseData.getChildAbductionReasons());
        Optional<YesOrNo> previousAbductionThreats = ofNullable(caseData.getPreviousAbductionThreats());
        Optional<String> previousAbductionThreatsDetails = ofNullable(caseData.getPreviousAbductionThreatsDetails());
        Optional<YesOrNo> abductionPassportOfficeNotified = ofNullable(caseData.getAbductionPassportOfficeNotified());
        Optional<YesOrNo> abductionPreviousPoliceInvolvement = ofNullable(caseData.getAbductionPreviousPoliceInvolvement());
        Optional<String> abductionPreviousPoliceInvolvementDetails = ofNullable(caseData.getAbductionPreviousPoliceInvolvementDetails());
        Optional<YesOrNo> abductionOtherSafetyConcerns = ofNullable(caseData.getAbductionOtherSafetyConcerns());
        Optional<String> abductionOtherSafetyConcernsDetails = ofNullable(caseData.getAbductionOtherSafetyConcernsDetails());
        Optional<String> abductionCourtStepsRequested = ofNullable(caseData.getAbductionCourtStepsRequested());

        boolean abductionSectionCompleted;
        boolean previousThreatSectionComplete = false;
        boolean passportCompleted = abductionPassportOfficeNotified.isPresent();
        boolean policeCompleted = false;
        boolean otherCompleted = false;

        if (childAbduction.isPresent() && childAbduction.get().equals(NO)){
            return true;
        }

        if (childAbduction.isPresent()) {
            if (childAbduction.get().equals(YES)) {
                abductionSectionCompleted = childAbductionReasons.isPresent();

                boolean previousAbductionThreatsCompleted = previousAbductionThreats.isPresent();

                if (previousAbductionThreatsCompleted) {
                    if (previousAbductionThreats.get().equals(YES)) {
                        previousThreatSectionComplete = previousAbductionThreatsDetails.isPresent();
                    }
                    else {
                        previousThreatSectionComplete = true;
                    }
                }

                boolean abductionPreviousPoliceInvolvementCompleted = abductionPreviousPoliceInvolvement.isPresent();

                if (abductionPreviousPoliceInvolvementCompleted) {
                    if (abductionPreviousPoliceInvolvement.get().equals(YES)) {
                        policeCompleted = abductionPreviousPoliceInvolvementDetails.isPresent();
                    }
                    else {
                        policeCompleted = true;
                    }
                }

                boolean abductionOtherConcernsCompleted = abductionOtherSafetyConcerns.isPresent();

                if (abductionOtherConcernsCompleted) {
                    if (abductionOtherSafetyConcerns.get().equals(YES)) {
                        otherCompleted = abductionOtherSafetyConcernsDetails.isPresent();
                    }
                    else {
                        otherCompleted = true;
                    }
                }
            }
            else {
                abductionSectionCompleted = abductionCourtStepsRequested.isPresent();
            }
            return abductionSectionCompleted &&
                   previousThreatSectionComplete &&
                   passportCompleted &&
                   policeCompleted &&
                   otherCompleted;
        }
        else {
            return false;
        }
    }

    private boolean validateBehaviour(Behaviours behaviour) {

        Optional<String> abuseNatureDescription = ofNullable(behaviour.getAbuseNatureDescription());
        Optional<String> behavioursStartDateAndLength = ofNullable(behaviour.getBehavioursStartDateAndLength());
        Optional<String> behavioursNature = ofNullable(behaviour.getBehavioursNature());
        Optional<YesOrNo> behavioursApplicantSoughtHelp = ofNullable(behaviour.getBehavioursApplicantSoughtHelp());
        Optional<String> behavioursApplicantHelpSoughtWho = ofNullable(behaviour.getBehavioursApplicantHelpSoughtWho());
        Optional<String> behavioursApplicantHelpAction = ofNullable(behaviour.getBehavioursApplicantHelpAction());

        boolean behaviourCompleted;

        behaviourCompleted = abuseNatureDescription.isPresent() &&
                                behavioursStartDateAndLength.isPresent() &&
                                behavioursNature.isPresent();

        if (behavioursApplicantSoughtHelp.isPresent() && behavioursApplicantSoughtHelp.get().equals(YES)) {
            behaviourCompleted = behavioursApplicantHelpSoughtWho.isPresent() &&
                                 behavioursApplicantHelpAction.isPresent();
        }
        return behaviourCompleted;
    }


    private boolean validateNonMolestationOrder(CaseData caseData) {
        Optional<YesOrNo> ordersNonMolestationCurrent = ofNullable(caseData.getOrdersNonMolestationCurrent());
        return ordersNonMolestationCurrent.isPresent();
    }

    private boolean validateOccupationOrder(CaseData caseData) {
        Optional<YesOrNo> ordersOccupationCurrent = ofNullable(caseData.getOrdersOccupationCurrent());
        return ordersOccupationCurrent.isPresent();
    }

    private boolean validateForcedMarriageProtectionOrder(CaseData caseData) {
        Optional<YesOrNo> ordersForcedMarriageProtectionCurrent = ofNullable(caseData.getOrdersForcedMarriageProtectionCurrent());
        return ordersForcedMarriageProtectionCurrent.isPresent();
    }

    private boolean validateRestrainingOrder(CaseData caseData) {
        Optional<YesOrNo> ordersRestrainingCurrent  = ofNullable(caseData.getOrdersRestrainingCurrent());
        return ordersRestrainingCurrent.isPresent();
    }

    private boolean validateOtherInjunctiveOrder(CaseData caseData) {
        Optional<YesOrNo> ordersOtherInjunctiveCurrent = ofNullable(caseData.getOrdersOtherInjunctiveCurrent());
        return ordersOtherInjunctiveCurrent.isPresent();
    }

    private boolean validateUndertakingInPlaceOrder(CaseData caseData) {
        Optional<YesOrNo> ordersUndertakingInPlaceCurrent = ofNullable(caseData.getOrdersUndertakingInPlaceCurrent());
        return ordersUndertakingInPlaceCurrent.isPresent();
    }

    private boolean validateOrders(CaseData caseData) {
        Optional<YesOrNo> ordersNonMolestation = ofNullable(caseData.getOrdersNonMolestation());
        Optional<YesOrNo> ordersOccupation = ofNullable(caseData.getOrdersOccupation());
        Optional<YesOrNo> ordersForcedMarriageProtection = ofNullable(caseData.getOrdersForcedMarriageProtection());
        Optional<YesOrNo> ordersRestraining = ofNullable(caseData.getOrdersRestraining());
        Optional<YesOrNo> ordersOtherInjunctive = ofNullable(caseData.getOrdersOtherInjunctive());
        Optional<YesOrNo> ordersUndertakingInPlace = ofNullable(caseData.getOrdersUndertakingInPlace());


        boolean allOrdersCompleted;

        if (ordersNonMolestation.isPresent() && ordersNonMolestation.get().equals(YES)) {
            allOrdersCompleted = validateNonMolestationOrder(caseData);
        }
        if (ordersOccupation.isPresent() && ordersOccupation.get().equals(YES)) {
            allOrdersCompleted = validateOccupationOrder(caseData);
        }
        if (ordersForcedMarriageProtection.isPresent() && ordersForcedMarriageProtection.get().equals(YES)) {
            allOrdersCompleted = validateForcedMarriageProtectionOrder(caseData);
        }
        if (ordersRestraining.isPresent() && ordersRestraining.get().equals(YES)) {
            allOrdersCompleted = validateRestrainingOrder(caseData);
        }
        if (ordersOtherInjunctive.isPresent() && ordersOtherInjunctive.get().equals(YES)) {
            allOrdersCompleted = validateOtherInjunctiveOrder(caseData);
        }
        if (ordersUndertakingInPlace.isPresent() && ordersUndertakingInPlace.get().equals(YES)) {
            allOrdersCompleted = validateUndertakingInPlaceOrder(caseData);
        }

        allOrdersCompleted = ordersNonMolestation.isPresent() &&
                             ordersOccupation.isPresent() &&
                             ordersForcedMarriageProtection.isPresent() &&
                             ordersRestraining.isPresent() &&
                             ordersOtherInjunctive.isPresent() &&
                             ordersUndertakingInPlace.isPresent();

        return allOrdersCompleted;
    }

    public boolean validateOtherConcerns(CaseData caseData) {
        Optional<YesOrNo> allegationsOfHarmOtherConcerns = ofNullable(caseData.getAllegationsOfHarmOtherConcernsYesNo());
        Optional<String> allegationsOfHarmOtherConcernsDetails = ofNullable(caseData.getAllegationsOfHarmOtherConcernsDetails());
        Optional<String> allegationsOfHarmOtherConcernsCourtActions = ofNullable(caseData.getAllegationsOfHarmOtherConcernsCourtActions());

        if (allegationsOfHarmOtherConcerns.isPresent() && allegationsOfHarmOtherConcerns.get().equals(NO)) {
            return true;
        }


        boolean otherConcernsCompleted = true;

        if (allegationsOfHarmOtherConcerns.isPresent() && allegationsOfHarmOtherConcerns.get().equals(YES)) {
            otherConcernsCompleted = allegationsOfHarmOtherConcernsDetails.isPresent() &&
                                     allegationsOfHarmOtherConcernsCourtActions.isPresent();;
        }
        else {
            otherConcernsCompleted = allegationsOfHarmOtherConcerns.isPresent() &&
                                     allegationsOfHarmOtherConcernsCourtActions.isPresent();
        }

        return otherConcernsCompleted;
        }

}
