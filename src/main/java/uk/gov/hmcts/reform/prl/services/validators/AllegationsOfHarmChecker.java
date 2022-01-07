package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.AbductionChildPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Behaviours;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.AbductionChildPassportPossessionEnum.OTHER;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.ALLEGATIONS_OF_HARM_ERROR;
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



    public boolean validateFields(CaseData caseData) {
        Optional<YesOrNo> allegationsOfHarmYesNo = ofNullable(caseData.getAllegationsOfHarmYesNo());

        boolean isFinished;

        if (allegationsOfHarmYesNo.isPresent() && allegationsOfHarmYesNo.get().equals(YES)) {

            boolean behavioursCompleted = true;

            if (abusePresent(caseData)) {
                Optional<List<Element<Behaviours>>> behavioursWrapped = ofNullable(caseData.getBehaviours());
                if (behavioursWrapped.isPresent() && behavioursWrapped.get().size() > 0) {
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
            }

            Optional<YesOrNo> ordersNonMolestation = ofNullable(caseData.getOrdersNonMolestation());
            Optional<YesOrNo> ordersOccupation = ofNullable(caseData.getOrdersOccupation());
            Optional<YesOrNo> ordersForcedMarriageProtection = ofNullable(caseData.getOrdersForcedMarriageProtection());
            Optional<YesOrNo> ordersRestraining = ofNullable(caseData.getOrdersRestraining());
            Optional<YesOrNo> ordersOtherInjunctive = ofNullable(caseData.getOrdersOtherInjunctive());
            Optional<YesOrNo> ordersUndertakingInPlace = ofNullable(caseData.getOrdersUndertakingInPlace());

            boolean previousOrders = ordersNonMolestation.isPresent()
                && ordersOccupation.isPresent()
                && ordersForcedMarriageProtection.isPresent()
                && ordersRestraining.isPresent()
                && ordersOtherInjunctive.isPresent()
                && ordersUndertakingInPlace.isPresent();

            isFinished = validateDomesticAbuseSection(caseData)
                && validateOrders(caseData)
                && previousOrders
                && behavioursCompleted
                && validateAbductionSection(caseData)
                && validateOtherConcerns(caseData)
                && validateChildContact(caseData);

        } else {
            isFinished = allegationsOfHarmYesNo.isPresent();
        }

        return isFinished;
    }


    public boolean abusePresent(CaseData caseData) {
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


    public boolean validateDomesticAbuseSection(CaseData caseData) {
        Optional<YesOrNo> domesticAbuse = ofNullable(caseData.getAllegationsOfHarmDomesticAbuseYesNo());
        Optional<List<ApplicantOrChildren>> physicalAbuseVictim = ofNullable(caseData.getPhysicalAbuseVictim());
        Optional<List<ApplicantOrChildren>> emotionalAbuseVictim = ofNullable(caseData.getEmotionalAbuseVictim());
        Optional<List<ApplicantOrChildren>>  psychologicalAbuseVictim = ofNullable(caseData.getPsychologicalAbuseVictim());
        Optional<List<ApplicantOrChildren>>  sexualAbuseVictim = ofNullable(caseData.getSexualAbuseVictim());
        Optional<List<ApplicantOrChildren>>  financialAbuseVictim = ofNullable(caseData.getPhysicalAbuseVictim());

        List<ApplicantOrChildren> emptyList = Collections.emptyList();

        boolean abuseVictimCompleted = true;
        boolean behaviourRequired = true;

        if (domesticAbuse.isPresent() && domesticAbuse.get().equals(YES)) {
            abuseVictimCompleted = physicalAbuseVictim.isPresent() && !(physicalAbuseVictim.get().equals(emptyList))
                || emotionalAbuseVictim.isPresent() && !(emotionalAbuseVictim.get().equals(emptyList))
                || psychologicalAbuseVictim.isPresent() && !(psychologicalAbuseVictim.get().equals(emptyList))
                || sexualAbuseVictim.isPresent() && !(sexualAbuseVictim.get().equals(emptyList))
                || financialAbuseVictim.isPresent() && !(financialAbuseVictim.get().equals(emptyList));

            Optional<List<Element<Behaviours>>> behavioursWrapped = ofNullable(caseData.getBehaviours());

            behaviourRequired = behavioursWrapped.isPresent()
                && behavioursWrapped.get().size() > 0;

        }
        return abuseVictimCompleted & behaviourRequired;
    }


    public boolean validateAbductionSection(CaseData caseData) {

        Optional<YesOrNo> childAbduction = ofNullable(caseData.getAllegationsOfHarmChildAbductionYesNo());
        Optional<String> childAbductionReasons = ofNullable(caseData.getChildAbductionReasons());
        Optional<YesOrNo> previousAbductionThreats = ofNullable(caseData.getPreviousAbductionThreats());
        Optional<String> previousAbductionThreatsDetails = ofNullable(caseData.getPreviousAbductionThreatsDetails());
        Optional<YesOrNo> abductionPassportOfficeNotified = ofNullable(caseData.getAbductionPassportOfficeNotified());
        Optional<YesOrNo> abductionChildHasPassport = ofNullable(caseData.getAbductionChildHasPassport());
        Optional<AbductionChildPassportPossessionEnum> abductionChildPassportPosession = ofNullable(caseData.getAbductionChildPassportPosession());
        Optional<String> abductionChildPassportPosessionOtherDetail = ofNullable(caseData.getAbductionChildPassportPosessionOtherDetail());
        Optional<YesOrNo> abductionPreviousPoliceInvolvement = ofNullable(caseData.getAbductionPreviousPoliceInvolvement());
        Optional<String> abductionPreviousPoliceInvolvementDetails = ofNullable(caseData.getAbductionPreviousPoliceInvolvementDetails());

        boolean abductionSectionCompleted;
        boolean previousThreatSectionComplete = false;
        boolean passportCompleted = abductionPassportOfficeNotified.isPresent();
        boolean hasPassportCompleted = abductionChildHasPassport.isPresent();
        boolean passportPossessionCompleted = false;
        boolean policeCompleted = false;

        if (childAbduction.isPresent() && childAbduction.get().equals(NO)) {
            return true;
        }

        if (childAbduction.isPresent()) {
            if (childAbduction.get().equals(YES)) {
                abductionSectionCompleted = childAbductionReasons.isPresent();

                boolean previousAbductionThreatsCompleted = previousAbductionThreats.isPresent();

                if (previousAbductionThreatsCompleted) {
                    if (previousAbductionThreats.get().equals(YES)) {
                        previousThreatSectionComplete = previousAbductionThreatsDetails.isPresent();
                    } else {
                        previousThreatSectionComplete = true;
                    }
                }

                boolean abductionChildPassportPosessionCompleted = abductionChildPassportPosession.isPresent();

                if(abductionChildPassportPosessionCompleted) {
                    if (abductionChildPassportPosession.get().equals(OTHER)) {
                        passportPossessionCompleted = abductionChildPassportPosessionOtherDetail.isPresent();
                    }else{
                        passportPossessionCompleted = true;
                    }
                }

                boolean abductionPreviousPoliceInvolvementCompleted = abductionPreviousPoliceInvolvement.isPresent();

                if (abductionPreviousPoliceInvolvementCompleted) {
                    if (abductionPreviousPoliceInvolvement.get().equals(YES)) {
                        policeCompleted = abductionPreviousPoliceInvolvementDetails.isPresent();
                    } else {
                        policeCompleted = true;
                    }
                }

            } else {
                abductionSectionCompleted = true;
            }
            return abductionSectionCompleted
                && previousThreatSectionComplete
                && passportCompleted
                && hasPassportCompleted
                && passportPossessionCompleted
                && policeCompleted;

        } else {
            return false;
        }
    }

    public boolean validateBehaviour(Behaviours behaviour) {

        Optional<String> abuseNatureDescription = ofNullable(behaviour.getAbuseNatureDescription());
        Optional<String> behavioursStartDateAndLength = ofNullable(behaviour.getBehavioursStartDateAndLength());
        Optional<String> behavioursNature = ofNullable(behaviour.getBehavioursNature());
        Optional<YesOrNo> behavioursApplicantSoughtHelp = ofNullable(behaviour.getBehavioursApplicantSoughtHelp());
        Optional<String> behavioursApplicantHelpSoughtWho = ofNullable(behaviour.getBehavioursApplicantHelpSoughtWho());
        Optional<String> behavioursApplicantHelpAction = ofNullable(behaviour.getBehavioursApplicantHelpAction());

        List<Optional> fields = new ArrayList<>();
        fields.add(abuseNatureDescription);
        fields.add(behavioursStartDateAndLength);
        fields.add(behavioursNature);
        fields.add(behavioursApplicantSoughtHelp);
        if (behavioursApplicantSoughtHelp.isPresent() && behavioursApplicantSoughtHelp.get().equals(YES)) {
            fields.add(behavioursApplicantHelpSoughtWho);
            fields.add(behavioursApplicantHelpAction);
        }

        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }


    public boolean validateNonMolestationOrder(CaseData caseData) {
        Optional<YesOrNo> ordersNonMolestationCurrent = ofNullable(caseData.getOrdersNonMolestationCurrent());
        return ordersNonMolestationCurrent.isPresent();
    }

    public boolean validateOccupationOrder(CaseData caseData) {
        Optional<YesOrNo> ordersOccupationCurrent = ofNullable(caseData.getOrdersOccupationCurrent());
        return ordersOccupationCurrent.isPresent();
    }

    public boolean validateForcedMarriageProtectionOrder(CaseData caseData) {
        Optional<YesOrNo> ordersForcedMarriageProtectionCurrent = ofNullable(caseData.getOrdersForcedMarriageProtectionCurrent());
        return ordersForcedMarriageProtectionCurrent.isPresent();
    }

    public boolean validateRestrainingOrder(CaseData caseData) {
        Optional<YesOrNo> ordersRestrainingCurrent  = ofNullable(caseData.getOrdersRestrainingCurrent());
        return ordersRestrainingCurrent.isPresent();
    }

    public boolean validateOtherInjunctiveOrder(CaseData caseData) {
        Optional<YesOrNo> ordersOtherInjunctiveCurrent = ofNullable(caseData.getOrdersOtherInjunctiveCurrent());
        return ordersOtherInjunctiveCurrent.isPresent();
    }

    public boolean validateUndertakingInPlaceOrder(CaseData caseData) {
        Optional<YesOrNo> ordersUndertakingInPlaceCurrent = ofNullable(caseData.getOrdersUndertakingInPlaceCurrent());
        return ordersUndertakingInPlaceCurrent.isPresent();
    }

    public boolean validateOrders(CaseData caseData) {

        boolean nonMolesComplete = true;
        boolean occupationComplete = true;
        boolean forcedMarComplete = true;
        boolean restrainComplete = true;
        boolean otherComplete = true;
        boolean underComplete = true;

        Optional<YesOrNo> ordersNonMolestation = ofNullable(caseData.getOrdersNonMolestation());
        if (ordersNonMolestation.isPresent() && ordersNonMolestation.get().equals(YES)) {
            nonMolesComplete = validateNonMolestationOrder(caseData);
        }
        Optional<YesOrNo> ordersOccupation = ofNullable(caseData.getOrdersOccupation());
        if (ordersOccupation.isPresent() && ordersOccupation.get().equals(YES)) {
            occupationComplete = validateOccupationOrder(caseData);
        }
        Optional<YesOrNo> ordersForcedMarriageProtection = ofNullable(caseData.getOrdersForcedMarriageProtection());
        if (ordersForcedMarriageProtection.isPresent() && ordersForcedMarriageProtection.get().equals(YES)) {
            forcedMarComplete = validateForcedMarriageProtectionOrder(caseData);
        }
        Optional<YesOrNo> ordersRestraining = ofNullable(caseData.getOrdersRestraining());
        if (ordersRestraining.isPresent() && ordersRestraining.get().equals(YES)) {
            restrainComplete = validateRestrainingOrder(caseData);
        }
        Optional<YesOrNo> ordersOtherInjunctive = ofNullable(caseData.getOrdersOtherInjunctive());
        if (ordersOtherInjunctive.isPresent() && ordersOtherInjunctive.get().equals(YES)) {
            otherComplete = validateOtherInjunctiveOrder(caseData);
        }
        Optional<YesOrNo> ordersUndertakingInPlace = ofNullable(caseData.getOrdersUndertakingInPlace());
        if (ordersUndertakingInPlace.isPresent() && ordersUndertakingInPlace.get().equals(YES)) {
            underComplete = validateUndertakingInPlaceOrder(caseData);
        }

        return nonMolesComplete
            && occupationComplete
            && forcedMarComplete
            && restrainComplete
            && otherComplete
            && underComplete;
    }

    public boolean validateOtherConcerns(CaseData caseData) {
        Optional<YesOrNo> allegationsOfHarmOtherConcerns = ofNullable(caseData.getAllegationsOfHarmOtherConcernsYesNo());
        Optional<String> allegationsOfHarmOtherConcernsDetails = ofNullable(caseData.getAllegationsOfHarmOtherConcernsDetails());
        Optional<String> allegationsOfHarmOtherConcernsCourtActions = ofNullable(caseData.getAllegationsOfHarmOtherConcernsCourtActions());

        List<Optional> fields = new ArrayList<>();
        fields.add(allegationsOfHarmOtherConcerns);
        if (allegationsOfHarmOtherConcerns.isPresent() && allegationsOfHarmOtherConcerns.get().equals(YES)) {
            fields.add(allegationsOfHarmOtherConcernsDetails);
        }
        fields.add(allegationsOfHarmOtherConcernsCourtActions);

        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
    }

    public boolean validateChildContact(CaseData caseData){

        Optional<YesOrNo> agreeChildUnsupervisedTime = ofNullable(caseData.getAgreeChildUnsupervisedTime());
        Optional<YesOrNo> agreeChildSupervisedTime = ofNullable(caseData.getAgreeChildSupervisedTime());
        Optional<YesOrNo> agreeChildOtherContact = ofNullable(caseData.getAgreeChildOtherContact());


        List<Optional> fields = new ArrayList<>();
        fields.add(agreeChildUnsupervisedTime);
        fields.add(agreeChildSupervisedTime);
        fields.add(agreeChildOtherContact);

        return fields.stream().noneMatch(Optional::isEmpty);
            //&& fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));

    }


}
