package uk.gov.hmcts.reform.prl.services.validators;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.AbductionChildPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildAbuseBehaviours;
import uk.gov.hmcts.reform.prl.models.complextypes.DomesticAbuseBehaviours;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.AbductionChildPassportPossessionEnum.other;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM_REVISED;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.ALLEGATIONS_OF_HARM_ERROR_NEW;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Service
public class AllegationsOfHarmRevisedChecker implements EventChecker {


    @Autowired
    TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        boolean finished = validateFields(caseData);

        if (finished) {
            taskErrorService.removeError(ALLEGATIONS_OF_HARM_ERROR_NEW);
            return true;
        }
        taskErrorService.addEventError(ALLEGATIONS_OF_HARM_REVISED,
                                       ALLEGATIONS_OF_HARM_ERROR_NEW,
                                       ALLEGATIONS_OF_HARM_ERROR_NEW.getError());
        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {

        Optional<YesOrNo> allegationsOfHarmRevised = ofNullable(caseData.getAllegationOfHarmRevised().getNewAllegationsOfHarmYesNo());

        return allegationsOfHarmRevised.isPresent() && allegationsOfHarmRevised.get().equals(Yes);
    }


    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }


    public boolean validateFields(CaseData caseData) {
        Optional<YesOrNo> allegationsOfHarmYesNo = ofNullable(caseData.getAllegationOfHarmRevised().getNewAllegationsOfHarmYesNo());

        boolean isFinished;

        if (allegationsOfHarmYesNo.isPresent() && allegationsOfHarmYesNo.get().equals(Yes)) {

            boolean domesticBehavioursCompleted = true;
            boolean childBehavioursCompleted = true;

            if (ofNullable(caseData.getAllegationOfHarmRevised().getDomesticBehaviours()).isPresent()) {
                Optional<List<Element<DomesticAbuseBehaviours>>> domesticBehavioursWrapped =
                    ofNullable(caseData.getAllegationOfHarmRevised().getDomesticBehaviours());
                if (domesticBehavioursWrapped.isPresent()
                    && !domesticBehavioursWrapped.get().isEmpty()) {
                    List<DomesticAbuseBehaviours> behaviours = domesticBehavioursWrapped.get()
                        .stream()
                        .map(Element::getValue)
                        .collect(Collectors.toList());

                    for (DomesticAbuseBehaviours behaviour : behaviours) {
                        domesticBehavioursCompleted = validateDomesticAbuseBehaviours(behaviour);
                        if (!domesticBehavioursCompleted) {
                            return false;
                        }
                    }
                }
            }

            if (ofNullable(caseData.getAllegationOfHarmRevised().getChildAbuseBehaviours()).isPresent()) {
                Optional<List<Element<ChildAbuseBehaviours>>> childBehavioursWrapped =
                    ofNullable(caseData.getAllegationOfHarmRevised().getChildAbuseBehaviours());
                if (childBehavioursWrapped.isPresent()
                    && !childBehavioursWrapped.get().isEmpty()) {
                    List<ChildAbuseBehaviours> behaviours = childBehavioursWrapped.get()
                        .stream()
                        .map(Element::getValue)
                        .collect(Collectors.toList());

                    for (ChildAbuseBehaviours behaviour : behaviours) {
                        childBehavioursCompleted = validateChildAbuseBehaviours(behaviour);
                        if (!childBehavioursCompleted) {
                            return false;
                        }
                    }
                }
            }

            Optional<YesOrNo> ordersNonMolestation =
                ofNullable(caseData.getAllegationOfHarmRevised().getNewOrdersNonMolestation());
            Optional<YesOrNo> ordersOccupation =
                ofNullable(caseData.getAllegationOfHarmRevised().getNewOrdersOccupation());
            Optional<YesOrNo> ordersForcedMarriageProtection =
                ofNullable(caseData.getAllegationOfHarmRevised().getNewOrdersForcedMarriageProtection());
            Optional<YesOrNo> ordersRestraining = ofNullable(caseData.getAllegationOfHarmRevised().getNewOrdersRestraining());
            Optional<YesOrNo> ordersOtherInjunctive = ofNullable(caseData.getAllegationOfHarmRevised().getNewOrdersOtherInjunctive());
            Optional<YesOrNo> ordersUndertakingInPlace = ofNullable(caseData.getAllegationOfHarmRevised().getNewOrdersUndertakingInPlace());

            boolean previousOrders = isPreviousOrdersFinished(
                ordersNonMolestation,
                ordersOccupation,
                ordersForcedMarriageProtection,
                ordersRestraining,
                ordersOtherInjunctive,
                ordersUndertakingInPlace
            );

            isFinished = isSectionsFinished(caseData, domesticBehavioursCompleted,childBehavioursCompleted, previousOrders);

        } else {
            isFinished = allegationsOfHarmYesNo.isPresent();
        }

        return isFinished;
    }

    public boolean isPreviousOrdersFinished(Optional<YesOrNo> ordersNonMolestation,
                                            Optional<YesOrNo> ordersOccupation,
                                            Optional<YesOrNo> ordersForcedMarriageProtection,
                                            Optional<YesOrNo> ordersRestraining,
                                            Optional<YesOrNo> ordersOtherInjunctive,
                                            Optional<YesOrNo> ordersUndertakingInPlace) {
        return ordersNonMolestation.isPresent()
            && ordersOccupation.isPresent()
            && ordersForcedMarriageProtection.isPresent()
            && ordersRestraining.isPresent()
            && ordersOtherInjunctive.isPresent()
            && ordersUndertakingInPlace.isPresent();
    }

    public boolean isSectionsFinished(CaseData caseData, boolean domesticBehavioursCompleted, boolean childBehavioursCompleted,
                                      boolean previousOrders) {
        boolean isFinished;
        isFinished =  validateOrders(caseData)
            && previousOrders
            && domesticBehavioursCompleted
            && childBehavioursCompleted
            && validateAbductionSection(caseData)
            && validateOtherConcerns(caseData)
            && validateChildContact(caseData);
        return isFinished;
    }


    public boolean validateAbductionSection(CaseData caseData) {


        Optional<YesOrNo> childAbduction =
            ofNullable(caseData.getAllegationOfHarmRevised().getNewAllegationsOfHarmChildAbductionYesNo());
        Optional<String> childAbductionReasons =
            ofNullable(caseData.getAllegationOfHarmRevised().getNewChildAbductionReasons());
        Optional<YesOrNo> previousAbductionThreats =
            ofNullable(caseData.getAllegationOfHarmRevised().getNewPreviousAbductionThreats());
        Optional<String> previousAbductionThreatsDetails =
            ofNullable(caseData.getAllegationOfHarmRevised().getNewPreviousAbductionThreatsDetails());
        Optional<YesOrNo> abductionPassportOfficeNotified =
            ofNullable(caseData.getAllegationOfHarmRevised().getNewAbductionPassportOfficeNotified());
        Optional<YesOrNo> abductionChildHasPassport =
            ofNullable(caseData.getAllegationOfHarmRevised().getNewAbductionChildHasPassport());
        Optional<YesOrNo> abductionPreviousPoliceInvolvement =
            ofNullable(caseData.getAllegationOfHarmRevised().getNewAbductionPreviousPoliceInvolvement());
        Optional<String> abductionPreviousPoliceInvolvementDetails =
            ofNullable(caseData.getAllegationOfHarmRevised().getNewAbductionPreviousPoliceInvolvementDetails());
        Optional<List<AbductionChildPassportPossessionEnum>> abductionChildPassportPosession =
            ofNullable(caseData.getAllegationOfHarmRevised().getChildPassportDetails().getChildPassportHolder());
        Optional<String> abductionChildPassportPosessionOtherDetail =
            ofNullable(caseData.getAllegationOfHarmRevised().getChildPassportDetails().getAbductionChildPassportPosessionOtherDetail());

        boolean abductionSectionCompleted;
        boolean previousThreatSectionComplete = false;
        boolean passportCompleted = abductionPassportOfficeNotified.isPresent();
        boolean hasPassportCompleted = abductionChildHasPassport.isPresent();
        boolean passportPossessionCompleted = false;
        boolean policeCompleted = false;

        if (childAbduction.isPresent() && No.equals(childAbduction.get())) {
            return true;
        }

        if (childAbduction.isPresent()) {
            if (Yes.equals(childAbduction.get())) {
                abductionSectionCompleted = childAbductionReasons.isPresent();

                boolean previousAbductionThreatsCompleted = previousAbductionThreats.isPresent();

                previousThreatSectionComplete = isPreviousThreatSectionComplete(
                    previousAbductionThreats,
                    previousAbductionThreatsDetails,
                    previousThreatSectionComplete,
                    previousAbductionThreatsCompleted
                );

                boolean abductionChildPassportPosessionCompleted = abductionChildPassportPosession.isPresent();

                passportPossessionCompleted = isPassportPossessionCompleted(
                    abductionChildPassportPosession,
                    abductionChildPassportPosessionOtherDetail,
                    passportPossessionCompleted,
                    abductionChildPassportPosessionCompleted
                );

                boolean
                    abductionPreviousPoliceInvolvementCompleted = abductionPreviousPoliceInvolvement.isPresent();

                policeCompleted = isPoliceInvolvementCompleted(
                    abductionPreviousPoliceInvolvement,
                    abductionPreviousPoliceInvolvementDetails,
                    policeCompleted,
                    abductionPreviousPoliceInvolvementCompleted
                );

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

    private boolean isPoliceInvolvementCompleted(
        Optional<YesOrNo> abductionPreviousPoliceInvolvement,
        Optional<String> abductionPreviousPoliceInvolvementDetails,
        boolean policeCompleted,
        boolean abductionPreviousPoliceInvolvementCompleted) {

        if (abductionPreviousPoliceInvolvementCompleted) {
            if (!abductionPreviousPoliceInvolvement.isEmpty()
                && Yes.equals(abductionPreviousPoliceInvolvement.get())) {
                policeCompleted = abductionPreviousPoliceInvolvementDetails.isPresent();
            } else {
                policeCompleted = true;
            }
        }
        return policeCompleted;
    }

    private boolean isPassportPossessionCompleted(
        Optional<List<AbductionChildPassportPossessionEnum>> abductionChildPassportPossession,
        Optional<String> abductionChildPassportPossessionOtherDetail,
        boolean passportPossessionCompleted,
        boolean abductionChildPassportPossessionCompleted) {

        if (abductionChildPassportPossessionCompleted) {
            if (!abductionChildPassportPossession.isEmpty()
                && other.equals(abductionChildPassportPossession.get())) {
                passportPossessionCompleted = abductionChildPassportPossessionOtherDetail.isPresent();
            } else {
                passportPossessionCompleted = true;
            }
        }
        return passportPossessionCompleted;
    }

    private boolean isPreviousThreatSectionComplete(
        Optional<YesOrNo> previousAbductionThreats,
        Optional<String> previousAbductionThreatsDetails,
        boolean previousThreatSectionComplete,
        boolean previousAbductionThreatsCompleted) {

        if (previousAbductionThreatsCompleted) {
            if (!previousAbductionThreats.isEmpty()
                && Yes.equals(previousAbductionThreats.get())) {
                previousThreatSectionComplete = previousAbductionThreatsDetails.isPresent();
            } else {
                previousThreatSectionComplete = true;
            }
        }
        return previousThreatSectionComplete;
    }

    public boolean validateDomesticAbuseBehaviours(DomesticAbuseBehaviours domesticAbuseBehaviours) {

        Optional<TypeOfAbuseEnum> typeOfAbuse = ofNullable(domesticAbuseBehaviours.getTypeOfAbuse());
        Optional<String> behavioursStartDateAndLength = ofNullable(domesticAbuseBehaviours.getNewBehavioursStartDateAndLength());
        Optional<String> abuseNatureDescription = ofNullable(domesticAbuseBehaviours.getNewAbuseNatureDescription());
        Optional<YesOrNo> behavioursApplicantSoughtHelp = ofNullable(domesticAbuseBehaviours.getNewBehavioursApplicantSoughtHelp());
        Optional<String> behavioursApplicantHelpSoughtWho = ofNullable(domesticAbuseBehaviours.getNewBehavioursApplicantHelpSoughtWho());
        Optional<String> behavioursApplicantHelpAction = ofNullable(domesticAbuseBehaviours.getNewBehavioursApplicantHelpAction());

        List<Optional<?>> fields = new ArrayList<>();
        fields.add(ofNullable(typeOfAbuse.get().getDisplayedValue()));
        fields.add(abuseNatureDescription);
        fields.add(behavioursStartDateAndLength);
        fields.add(behavioursApplicantSoughtHelp);
        if (behavioursApplicantSoughtHelp.isPresent()
            && behavioursApplicantSoughtHelp.get().equals(Yes)) {
            fields.add(behavioursApplicantHelpSoughtWho);
            fields.add(behavioursApplicantHelpAction);
        }

        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent)
            .map(Optional::get).noneMatch(field -> field.equals(""));

    }


    public boolean validateChildAbuseBehaviours(ChildAbuseBehaviours childAbuseBehaviours) {

        Optional<TypeOfAbuseEnum> typeOfAbuse = ofNullable(childAbuseBehaviours.getTypeOfAbuse());
        Optional<String> abuseNatureDescription = ofNullable(childAbuseBehaviours.getNewAbuseNatureDescription());
        Optional<String> behavioursStartDateAndLength = ofNullable(childAbuseBehaviours.getNewBehavioursStartDateAndLength());
        Optional<YesOrNo> behavioursApplicantSoughtHelp = ofNullable(childAbuseBehaviours.getNewBehavioursApplicantSoughtHelp());
        Optional<String> behavioursApplicantHelpSoughtWho = ofNullable(childAbuseBehaviours.getNewBehavioursApplicantHelpSoughtWho());
        Optional<String> behavioursApplicantHelpAction = ofNullable(childAbuseBehaviours.getNewBehavioursApplicantHelpAction());

        List<Optional<?>> fields = new ArrayList<>();
        fields.add(ofNullable(typeOfAbuse.get().getDisplayedValue()));
        fields.add(abuseNatureDescription);
        fields.add(behavioursStartDateAndLength);
        fields.add(behavioursApplicantSoughtHelp);
        if (behavioursApplicantSoughtHelp.isPresent()
            && behavioursApplicantSoughtHelp.get().equals(Yes)) {
            fields.add(behavioursApplicantHelpSoughtWho);
            fields.add(behavioursApplicantHelpAction);
        }

        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent)
            .map(Optional::get).noneMatch(field -> field.equals(""));

    }

    public boolean validateOtherConcerns(CaseData caseData) {
        Optional<YesOrNo> allegationsOfHarmRevisedOtherConcerns = ofNullable(caseData.getAllegationOfHarmRevised()
                                                                          .getNewAllegationsOfHarmOtherConcerns());
        Optional<String> allegationsOfHarmevisedOtherConcernsDetails = ofNullable(caseData.getAllegationOfHarmRevised()
                                                                                .getNewAllegationsOfHarmOtherConcernsDetails());
        Optional<String> allegationsOfHarmevisedOtherConcernsCourtActions = ofNullable(caseData.getAllegationOfHarmRevised()
                                                                                     .getNewAllegationsOfHarmOtherConcernsCourtActions());

        List<Optional<?>> fields = new ArrayList<>();
        fields.add(allegationsOfHarmRevisedOtherConcerns);
        if (allegationsOfHarmRevisedOtherConcerns.isPresent() && allegationsOfHarmRevisedOtherConcerns.get().equals(Yes)) {
            fields.add(allegationsOfHarmevisedOtherConcernsDetails);
        }
        fields.add(allegationsOfHarmevisedOtherConcernsCourtActions);

        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
    }

    public boolean validateChildContact(CaseData caseData) {

        Optional<YesOrNo> agreeChildUnsupervisedTime = ofNullable(caseData.getAllegationOfHarmRevised().getNewAgreeChildUnsupervisedTime());
        Optional<YesOrNo> agreeChildSupervisedTime = ofNullable(caseData.getAllegationOfHarmRevised().getNewAgreeChildSupervisedTime());
        Optional<YesOrNo> agreeChildOtherContact = ofNullable(caseData.getAllegationOfHarmRevised().getNewAgreeChildOtherContact());


        List<Optional<?>> fields = new ArrayList<>();
        fields.add(agreeChildUnsupervisedTime);
        fields.add(agreeChildSupervisedTime);
        fields.add(agreeChildOtherContact);

        return fields.stream().noneMatch(Optional::isEmpty);

    }

    public boolean validateOrders(CaseData caseData) {

        boolean nonMolesComplete = true;
        boolean occupationComplete = true;
        boolean forcedMarComplete = true;
        boolean restrainComplete = true;
        boolean otherComplete = true;
        boolean underComplete = true;

        Optional<YesOrNo> ordersNonMolestation = ofNullable(caseData.getAllegationOfHarmRevised().getNewOrdersNonMolestation());
        if (ordersNonMolestation.isPresent() && ordersNonMolestation.get().equals(Yes)) {
            nonMolesComplete = validateNonMolestationOrder(caseData);
        }
        Optional<YesOrNo> ordersOccupation = ofNullable(caseData.getAllegationOfHarmRevised().getNewOrdersOccupation());
        if (ordersOccupation.isPresent() && ordersOccupation.get().equals(Yes)) {
            occupationComplete = validateOccupationOrder(caseData);
        }
        Optional<YesOrNo> ordersForcedMarriageProtection = ofNullable(caseData.getAllegationOfHarmRevised().getNewOrdersForcedMarriageProtection());
        if (ordersForcedMarriageProtection.isPresent() && ordersForcedMarriageProtection.get().equals(Yes)) {
            forcedMarComplete = validateForcedMarriageProtectionOrder(caseData);
        }
        Optional<YesOrNo> ordersRestraining = ofNullable(caseData.getAllegationOfHarmRevised().getNewOrdersRestraining());
        if (ordersRestraining.isPresent() && ordersRestraining.get().equals(Yes)) {
            restrainComplete = validateRestrainingOrder(caseData);
        }
        Optional<YesOrNo> ordersOtherInjunctive = ofNullable(caseData.getAllegationOfHarmRevised().getNewOrdersOtherInjunctive());
        if (ordersOtherInjunctive.isPresent() && ordersOtherInjunctive.get().equals(Yes)) {
            otherComplete = validateOtherInjunctiveOrder(caseData);
        }
        Optional<YesOrNo> ordersUndertakingInPlace = ofNullable(caseData.getAllegationOfHarmRevised().getNewOrdersUndertakingInPlace());
        if (ordersUndertakingInPlace.isPresent() && ordersUndertakingInPlace.get().equals(Yes)) {
            underComplete = validateUndertakingInPlaceOrder(caseData);
        }

        return nonMolesComplete
            && occupationComplete
            && forcedMarComplete
            && restrainComplete
            && otherComplete
            && underComplete;
    }


    public boolean validateNonMolestationOrder(CaseData caseData) {
        Optional<YesOrNo> ordersNonMolestationCurrent = ofNullable(caseData.getAllegationOfHarmRevised().getNewOrdersNonMolestationCurrent());
        return ordersNonMolestationCurrent.isPresent();
    }

    public boolean validateOccupationOrder(CaseData caseData) {
        Optional<YesOrNo> ordersOccupationCurrent = ofNullable(caseData.getAllegationOfHarmRevised().getNewOrdersOccupationCurrent());
        return ordersOccupationCurrent.isPresent();
    }

    public boolean validateForcedMarriageProtectionOrder(CaseData caseData) {
        Optional<YesOrNo> ordersForcedMarriageProtectionCurrent = ofNullable(caseData.getAllegationOfHarmRevised()
                                                                                 .getNewOrdersForcedMarriageProtectionCurrent());
        return ordersForcedMarriageProtectionCurrent.isPresent();
    }

    public boolean validateRestrainingOrder(CaseData caseData) {
        Optional<YesOrNo> ordersRestrainingCurrent = ofNullable(caseData.getAllegationOfHarmRevised().getNewOrdersRestrainingCurrent());
        return ordersRestrainingCurrent.isPresent();
    }

    public boolean validateOtherInjunctiveOrder(CaseData caseData) {
        Optional<YesOrNo> ordersOtherInjunctiveCurrent = ofNullable(caseData.getAllegationOfHarmRevised().getNewOrdersOtherInjunctiveCurrent());
        return ordersOtherInjunctiveCurrent.isPresent();
    }

    public boolean validateUndertakingInPlaceOrder(CaseData caseData) {
        Optional<YesOrNo> ordersUndertakingInPlaceCurrent = ofNullable(caseData.getAllegationOfHarmRevised().getNewOrdersUndertakingInPlaceCurrent());
        return ordersUndertakingInPlaceCurrent.isPresent();
    }
}
