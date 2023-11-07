package uk.gov.hmcts.reform.prl.services.validators;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.NewPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildAbuse;
import uk.gov.hmcts.reform.prl.models.complextypes.DomesticAbuseBehaviours;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarmRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.AllegationOfHarmRevisedService;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM_REVISED;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.ALLEGATIONS_OF_HARM_REVISED_ERROR;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AllegationsOfHarmRevisedChecker implements EventChecker {

    private final TaskErrorService taskErrorService;
    private final AllegationOfHarmRevisedService allegationOfHarmRevisedService;

    @Override
    public boolean isFinished(CaseData caseData) {

        boolean finished = validateFields(caseData);

        if (finished) {
            taskErrorService.removeError(ALLEGATIONS_OF_HARM_REVISED_ERROR);
            return true;
        }
        taskErrorService.addEventError(ALLEGATIONS_OF_HARM_REVISED,
                ALLEGATIONS_OF_HARM_REVISED_ERROR,
                                       ALLEGATIONS_OF_HARM_REVISED_ERROR.getError());
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

    @Override
    public TaskState getDefaultTaskState(CaseData caseData) {
        return TaskState.NOT_STARTED;
    }


    public boolean validateFields(CaseData caseData) {
        Optional<YesOrNo> allegationsOfHarmYesNo = ofNullable(caseData.getAllegationOfHarmRevised().getNewAllegationsOfHarmYesNo());

        boolean isFinished;

        if (allegationsOfHarmYesNo.isPresent() && allegationsOfHarmYesNo.get().equals(Yes)) {
            boolean domesticBehavioursCompleted = true;
            boolean childBehavioursCompleted = true;

            domesticBehavioursCompleted =  validateDomesticAbuse(caseData);
            if (!domesticBehavioursCompleted) {
                return false;
            }
            childBehavioursCompleted = validateChildAbuse(caseData);
            if (!childBehavioursCompleted) {
                log.info("childBehavioursCompleted validation failed");
                return false;
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

    public boolean validateDomesticAbuse(CaseData caseData) {
        boolean domesticBehavioursCompleted = true;
        Optional<List<Element<DomesticAbuseBehaviours>>> domesticBehavioursWrapped =
                ofNullable(caseData.getAllegationOfHarmRevised().getDomesticBehaviours());
        if (domesticBehavioursWrapped.isPresent()
                && !domesticBehavioursWrapped.get().isEmpty()) {
            domesticBehavioursCompleted = domesticBehavioursWrapped.get()
                    .stream().allMatch(behavioursElement -> validateDomesticAbuseBehaviours(behavioursElement.getValue()));
        }
        return domesticBehavioursCompleted;
    }

    public boolean validateChildAbuse(CaseData caseData) {
        Optional<AllegationOfHarmRevised> allegationOfHarmRevised =
                ofNullable(caseData.getAllegationOfHarmRevised());
        boolean isValidChildAbuse = true;
        if (allegationOfHarmRevised.isPresent() && (!validateChildPhysicalAbuse(allegationOfHarmRevised.get())
                || !validateChildPsychologicalAbuse(allegationOfHarmRevised.get())
                || !validateChildEmotionalAbuse(allegationOfHarmRevised.get()) || !validateChildSexualAbuse(allegationOfHarmRevised.get())
                || !validateChildFinancialAbuse(allegationOfHarmRevised.get()))) {
            isValidChildAbuse = false;
        }
        return isValidChildAbuse;
    }

    private boolean validateChildPhysicalAbuse(AllegationOfHarmRevised allegationOfHarmRevised) {
        Optional<ChildAbuse> childPhysicalAbuse =
                ofNullable(allegationOfHarmRevised.getChildPhysicalAbuse());
        if (childPhysicalAbuse.isPresent() && Objects.nonNull(childPhysicalAbuse.get().getTypeOfAbuse())
                && !validateChildAbuseBehaviours(allegationOfHarmRevised, childPhysicalAbuse.get())) {
            log.info("PhysicalAbuse validation failed");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private boolean validateChildPsychologicalAbuse(AllegationOfHarmRevised allegationOfHarmRevised) {
        Optional<ChildAbuse> childPsychologicalAbuse =
                ofNullable(allegationOfHarmRevised.getChildPsychologicalAbuse());
        if (childPsychologicalAbuse.isPresent() && Objects.nonNull(childPsychologicalAbuse.get().getTypeOfAbuse())
                && !validateChildAbuseBehaviours(allegationOfHarmRevised, childPsychologicalAbuse.get())) {
            log.info("PsychologicalAbuse validation failed");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private boolean validateChildEmotionalAbuse(AllegationOfHarmRevised allegationOfHarmRevised) {
        Optional<ChildAbuse> childEmotionalAbuse =
                ofNullable(allegationOfHarmRevised.getChildEmotionalAbuse());
        if (childEmotionalAbuse.isPresent() && Objects.nonNull(childEmotionalAbuse.get().getTypeOfAbuse())
                && !validateChildAbuseBehaviours(allegationOfHarmRevised, childEmotionalAbuse.get())) {
            log.info("EmotionalAbuse validation failed");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private boolean validateChildSexualAbuse(AllegationOfHarmRevised allegationOfHarmRevised) {
        Optional<ChildAbuse> childSexualAbuse =
                ofNullable(allegationOfHarmRevised.getChildSexualAbuse());
        if (childSexualAbuse.isPresent() && Objects.nonNull(childSexualAbuse.get().getTypeOfAbuse())
                && !validateChildAbuseBehaviours(allegationOfHarmRevised, childSexualAbuse.get())) {
            log.info("SexualAbuse validation failed");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private boolean validateChildFinancialAbuse(AllegationOfHarmRevised allegationOfHarmRevised) {
        Optional<ChildAbuse> childFinancialAbuse =
                ofNullable(allegationOfHarmRevised.getChildFinancialAbuse());
        if (childFinancialAbuse.isPresent() && Objects.nonNull(childFinancialAbuse.get().getTypeOfAbuse())
                && !validateChildAbuseBehaviours(allegationOfHarmRevised, childFinancialAbuse.get())) {
            log.info("PhysicalAbuse validation failed");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
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

        log.debug("validateOrders  :{} ",validateOrders(caseData));
        log.debug("previousOrders  :{} ",previousOrders);
        log.debug("domesticBehavioursCompleted  :{} ",domesticBehavioursCompleted);
        log.debug("childBehavioursCompleted  :{} ",childBehavioursCompleted);
        log.debug("validateAbductionSection(caseData)  :{} ",validateAbductionSection(caseData));
        log.debug("validateSubstanceAbuse(caseData)  :{} ",validateSubstanceAbuse(caseData));
        log.debug("validateOtherConcerns(caseData)  :{} ",validateOtherConcerns(caseData));
        log.debug("validateChildContact(caseData)  :{} ",validateChildContact(caseData));
        boolean isFinished;
        isFinished =  validateOrders(caseData)
            && previousOrders
            && domesticBehavioursCompleted
            && childBehavioursCompleted
            && validateAbductionSection(caseData)
            && validateOtherConcerns(caseData)
            && validateSubstanceAbuse(caseData)
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

        boolean abductionSectionCompleted;
        boolean previousThreatSectionComplete = false;
        boolean passportCompleted = abductionPassportOfficeNotified.isPresent();
        boolean hasPassportCompleted = abductionChildHasPassport.isPresent();
        boolean policeCompleted = false;
        boolean passportPossessionCompleted = false;
        if (childAbduction.isPresent() && No.equals(childAbduction.get())) {
            return true;
        }

        if (childAbduction.isPresent()) {
            if (Yes.equals(childAbduction.get())) {
                abductionSectionCompleted = childAbductionReasons.isPresent();

                previousThreatSectionComplete = isPreviousThreatSectionComplete(
                    previousAbductionThreats,
                    previousAbductionThreatsDetails
                );
                if (abductionChildHasPassport.isPresent() && Yes.equals(abductionChildHasPassport.get())) {
                    Optional<List<NewPassportPossessionEnum>> abductionChildPassportPosessionList =
                            ofNullable(caseData.getAllegationOfHarmRevised().getChildPassportDetails().getNewChildPassportPossession());
                    Optional<String> abductionChildPassportPosessionOtherDetail = ofNullable(caseData.getAllegationOfHarmRevised()
                                                                                                     .getChildPassportDetails()
                                                                                                     .getNewChildPassportPossessionOtherDetails());
                    passportPossessionCompleted = isPassportPossessionCompleted(
                        abductionChildPassportPosessionList,
                        abductionChildPassportPosessionOtherDetail
                    );
                } else {
                    passportPossessionCompleted = true;
                }

                policeCompleted = isPoliceInvolvementCompleted(
                    abductionPreviousPoliceInvolvement,
                    abductionPreviousPoliceInvolvementDetails
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
        Optional<String> abductionPreviousPoliceInvolvementDetails) {
        boolean policeCompleted = true;
        if (abductionPreviousPoliceInvolvement.isPresent()) {
            if (Yes.equals(abductionPreviousPoliceInvolvement.get())) {
                policeCompleted = abductionPreviousPoliceInvolvementDetails.isPresent();
            } else {
                policeCompleted = true;
            }
        }
        return policeCompleted;
    }

    private boolean isPassportPossessionCompleted(
        Optional<List<NewPassportPossessionEnum>> abductionChildPassportPossession,
        Optional<String> abductionChildPassportPossessionOtherDetail) {
        boolean passportPossessionCompleted = false;
        if (abductionChildPassportPossession.isPresent()) {
            if (abductionChildPassportPossession.get().contains(NewPassportPossessionEnum.otherPerson)) {
                passportPossessionCompleted = abductionChildPassportPossessionOtherDetail.isPresent();
            } else {
                passportPossessionCompleted = true;
            }
        }
        return passportPossessionCompleted;
    }

    private boolean isPreviousThreatSectionComplete(
        Optional<YesOrNo> previousAbductionThreats,
        Optional<String> previousAbductionThreatsDetails) {
        boolean previousThreatSectionComplete = true;
        if (previousAbductionThreats.isPresent() && Yes.equals(previousAbductionThreats.get())) {
            if (Yes.equals(previousAbductionThreats.get())) {
                previousThreatSectionComplete = previousAbductionThreatsDetails.isPresent();
            } else {
                previousThreatSectionComplete = true;
            }
        }
        return previousThreatSectionComplete;
    }

    public boolean validateDomesticAbuseBehaviours(DomesticAbuseBehaviours domesticAbuseBehaviours) {

        Optional<String> behavioursStartDateAndLength = ofNullable(domesticAbuseBehaviours.getNewBehavioursStartDateAndLength());
        Optional<String> abuseNatureDescription = ofNullable(domesticAbuseBehaviours.getNewAbuseNatureDescription());
        Optional<YesOrNo> behavioursApplicantSoughtHelp = ofNullable(domesticAbuseBehaviours.getNewBehavioursApplicantSoughtHelp());
        Optional<String> behavioursApplicantHelpSoughtWho = ofNullable(domesticAbuseBehaviours.getNewBehavioursApplicantHelpSoughtWho());
        List<Optional<?>> fields = new ArrayList<>();
        fields.add(ofNullable(domesticAbuseBehaviours.getTypeOfAbuse().getDisplayedValue()));
        fields.add(abuseNatureDescription);
        fields.add(behavioursStartDateAndLength);
        fields.add(behavioursApplicantSoughtHelp);
        if (behavioursApplicantSoughtHelp.isPresent()
            && behavioursApplicantSoughtHelp.get().equals(Yes)) {
            fields.add(behavioursApplicantHelpSoughtWho);
        }

        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent)
            .map(Optional::get).noneMatch(field -> field.equals(""));

    }


    public boolean validateChildAbuseBehaviours(AllegationOfHarmRevised allegationOfHarmRevised, ChildAbuse childAbuse) {
        log.info("Type Of Abuse in method: validateChildAbuseBehaviours" + childAbuse.getTypeOfAbuse());
        Optional<YesOrNo> allChildrenAreRisk = ofNullable(allegationOfHarmRevisedService
                                                              .getIfAllChildrenAreRisk(childAbuse.getTypeOfAbuse(),allegationOfHarmRevised));

        Optional<String> abuseNatureDescription = ofNullable(childAbuse.getAbuseNatureDescription());
        Optional<String> behavioursApplicantHelpSoughtWho = ofNullable(childAbuse.getBehavioursApplicantHelpSoughtWho());

        List<Optional<?>> fields = new ArrayList<>();
        if (allChildrenAreRisk.isPresent()
                && allChildrenAreRisk.get().equals(No)) {
            Optional<List<DynamicMultiselectListElement>> whichChildrenAreRisk = ofNullable(allegationOfHarmRevisedService.getWhichChildrenAreInRisk(
                    childAbuse.getTypeOfAbuse(),allegationOfHarmRevised).getValue());
            fields.add(whichChildrenAreRisk);
        }
        fields.add(abuseNatureDescription);
        Optional<String> behavioursStartDateAndLength = ofNullable(childAbuse.getBehavioursStartDateAndLength());
        fields.add(behavioursStartDateAndLength);
        Optional<YesOrNo> behavioursApplicantSoughtHelp = ofNullable(childAbuse.getBehavioursApplicantSoughtHelp());
        fields.add(behavioursApplicantSoughtHelp);
        if (behavioursApplicantSoughtHelp.isPresent()
                && behavioursApplicantSoughtHelp.get().equals(Yes)) {
            fields.add(behavioursApplicantHelpSoughtWho);
        }
        return fields.stream().noneMatch(Optional::isEmpty)
                && fields.stream().filter(Optional::isPresent)
                .map(Optional::get).noneMatch(field -> field.equals(""));
    }


    public boolean validateSubstanceAbuse(CaseData caseData) {
        Optional<YesOrNo> allegationsOfHarmRevisedSubstanceAbuse = ofNullable(caseData.getAllegationOfHarmRevised()
                                                                                 .getNewAllegationsOfHarmSubstanceAbuseYesNo());
        Optional<String> allegationsOfHarmRevisedOtherSubstanceAbuseDetails = ofNullable(caseData.getAllegationOfHarmRevised()
                                                                                      .getNewAllegationsOfHarmSubstanceAbuseDetails());

        List<Optional<?>> fields = new ArrayList<>();
        fields.add(allegationsOfHarmRevisedSubstanceAbuse);
        if (allegationsOfHarmRevisedSubstanceAbuse.isPresent() && allegationsOfHarmRevisedSubstanceAbuse.get().equals(Yes)) {
            fields.add(allegationsOfHarmRevisedOtherSubstanceAbuseDetails);
        }

        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
    }


    public boolean validateOtherConcerns(CaseData caseData) {
        Optional<YesOrNo> allegationsOfHarmRevisedOtherConcerns = ofNullable(caseData.getAllegationOfHarmRevised()
                                                                          .getNewAllegationsOfHarmOtherConcerns());
        Optional<String> allegationsOfHarmRevisedOtherConcernsDetails = ofNullable(caseData.getAllegationOfHarmRevised()
                                                                                .getNewAllegationsOfHarmOtherConcernsDetails());
        Optional<String> allegationsOfHarmRevisedOtherConcernsCourtActions = ofNullable(caseData.getAllegationOfHarmRevised()
                                                                                     .getNewAllegationsOfHarmOtherConcernsCourtActions());

        List<Optional<?>> fields = new ArrayList<>();
        fields.add(allegationsOfHarmRevisedOtherConcerns);
        if (allegationsOfHarmRevisedOtherConcerns.isPresent() && allegationsOfHarmRevisedOtherConcerns.get().equals(Yes)) {
            fields.add(allegationsOfHarmRevisedOtherConcernsDetails);
        }
        fields.add(allegationsOfHarmRevisedOtherConcernsCourtActions);

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
