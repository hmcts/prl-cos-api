package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.ChildAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.RespPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.RespChildAbuse;
import uk.gov.hmcts.reform.prl.models.complextypes.RespDomesticAbuseBehaviours;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarmData;
import uk.gov.hmcts.reform.prl.services.RespondentAllegationOfHarmService;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentEventErrorsEnum.ALLEGATION_OF_HARM_ERROR;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.ALLEGATION_OF_HARM;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentAllegationsOfHarmChecker implements RespondentEventChecker {
    private final RespondentTaskErrorService respondentTaskErrorService;
    private final RespondentAllegationOfHarmService respondentAllegationOfHarmService;

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
            if (respondentAllegationsOfHarm.isPresent() && validateFields(
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

    public boolean validateFields(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {
        Optional<YesOrNo> respAllegationsOfHarmYesNo = ofNullable(respondentAllegationsOfHarmData.getRespAohYesOrNo());

        boolean isFinished;

        if (respAllegationsOfHarmYesNo.isPresent() && Yes.equals(respAllegationsOfHarmYesNo.get())) {
            boolean respDomesticBehavioursCompleted;
            boolean respChildBehavioursCompleted;

            respDomesticBehavioursCompleted =  validateDomesticAbuse(respondentAllegationsOfHarmData);
            if (!respDomesticBehavioursCompleted) {
                return false;
            }
            respChildBehavioursCompleted = validateChildAbuse(respondentAllegationsOfHarmData);
            if (!respChildBehavioursCompleted) {
                log.info("respChildBehavioursCompleted validation failed");
                return false;
            }
            Optional<YesOrNo> respOrdersNonMolestation =
                    ofNullable(respondentAllegationsOfHarmData.getRespOrdersNonMolestation());
            Optional<YesOrNo> respOrdersOccupation =
                    ofNullable(respondentAllegationsOfHarmData.getRespOrdersOccupation());
            Optional<YesOrNo> respOrdersForcedMarriageProtection =
                    ofNullable(respondentAllegationsOfHarmData.getRespOrdersForcedMarriageProtection());
            Optional<YesOrNo> respOrdersRestraining = ofNullable(respondentAllegationsOfHarmData.getRespOrdersRestraining());
            Optional<YesOrNo> respOrdersOtherInjunctive = ofNullable(respondentAllegationsOfHarmData.getRespOrdersOtherInjunctive());
            Optional<YesOrNo> respOrdersUndertakingInPlace = ofNullable(respondentAllegationsOfHarmData.getRespOrdersUndertakingInPlace());

            boolean previousOrders = isPreviousOrdersFinished(
                    respOrdersNonMolestation,
                    respOrdersOccupation,
                    respOrdersForcedMarriageProtection,
                    respOrdersRestraining,
                    respOrdersOtherInjunctive,
                    respOrdersUndertakingInPlace
            );

            isFinished = isSectionsFinished(respondentAllegationsOfHarmData,
                                            previousOrders
            );

        } else {
            isFinished = respAllegationsOfHarmYesNo.isPresent();
        }

        return isFinished;
    }

    public boolean validateDomesticAbuse(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {
        boolean respDomesticBehavioursCompleted = true;
        Optional<List<Element<RespDomesticAbuseBehaviours>>> respDomesticBehavioursWrapped =
                ofNullable(respondentAllegationsOfHarmData.getRespDomesticBehaviours());
        if (respDomesticBehavioursWrapped.isPresent()
                && !respDomesticBehavioursWrapped.get().isEmpty()) {
            respDomesticBehavioursCompleted = respDomesticBehavioursWrapped.get()
                    .stream().allMatch(behavioursElement -> validateDomesticAbuseBehaviours(behavioursElement.getValue()));
        }
        return respDomesticBehavioursCompleted;
    }

    public boolean validateChildAbuse(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {
        Optional<RespondentAllegationsOfHarmData> respAllegationOfHarmRevised =
                ofNullable(respondentAllegationsOfHarmData);
        boolean isValidChildAbuse = true;
        if (respAllegationOfHarmRevised.isPresent() && (!validateChildPhysicalAbuse(respAllegationOfHarmRevised.get())
                || !validateChildPsychologicalAbuse(respAllegationOfHarmRevised.get())
                || !validateChildEmotionalAbuse(respAllegationOfHarmRevised.get()) || !validateChildSexualAbuse(respAllegationOfHarmRevised.get())
                || !validateChildFinancialAbuse(respAllegationOfHarmRevised.get()))) {
            isValidChildAbuse = false;
        }
        return isValidChildAbuse;
    }

    private boolean validateChildPhysicalAbuse(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {
        Optional<RespChildAbuse> respChildPhysicalAbuse =
                ofNullable(respondentAllegationsOfHarmData.getRespChildPhysicalAbuse());
        if (respChildPhysicalAbuse.isPresent()
                && !validateChildAbuseBehaviours(respondentAllegationsOfHarmData, respChildPhysicalAbuse.get(),
                ChildAbuseEnum.physicalAbuse)) {
            log.info("respChildPhysicalAbuse validation failed");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private boolean validateChildPsychologicalAbuse(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {
        Optional<RespChildAbuse> respChildPsychologicalAbuse =
                ofNullable(respondentAllegationsOfHarmData.getRespChildPsychologicalAbuse());
        if (respChildPsychologicalAbuse.isPresent()
                && !validateChildAbuseBehaviours(respondentAllegationsOfHarmData, respChildPsychologicalAbuse.get(),
                ChildAbuseEnum.psychologicalAbuse)) {
            log.info("RespChildPsychologicalAbuse validation failed");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private boolean validateChildEmotionalAbuse(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {
        Optional<RespChildAbuse> respChildEmotionalAbuse =
                ofNullable(respondentAllegationsOfHarmData.getRespChildEmotionalAbuse());
        if (respChildEmotionalAbuse.isPresent()
                && !validateChildAbuseBehaviours(respondentAllegationsOfHarmData, respChildEmotionalAbuse.get(),
                ChildAbuseEnum.emotionalAbuse)) {
            log.info("respChildEmotionalAbuse validation failed");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private boolean validateChildSexualAbuse(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {
        Optional<RespChildAbuse> respChildSexualAbuse =
                ofNullable(respondentAllegationsOfHarmData.getRespChildSexualAbuse());
        if (respChildSexualAbuse.isPresent()
                && !validateChildAbuseBehaviours(respondentAllegationsOfHarmData, respChildSexualAbuse.get(),
                ChildAbuseEnum.sexualAbuse)) {
            log.info("respChildSexualAbuse validation failed");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private boolean validateChildFinancialAbuse(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {
        Optional<RespChildAbuse> respChildFinancialAbuse =
                ofNullable(respondentAllegationsOfHarmData.getRespChildFinancialAbuse());
        if (respChildFinancialAbuse.isPresent()
                && !validateChildAbuseBehaviours(respondentAllegationsOfHarmData, respChildFinancialAbuse.get(),
                ChildAbuseEnum.financialAbuse)) {
            log.info("respChildFinancialAbuse validation failed");
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public boolean isPreviousOrdersFinished(Optional<YesOrNo> respOrdersNonMolestation,
                                            Optional<YesOrNo> respOrdersOccupation,
                                            Optional<YesOrNo> respOrdersForcedMarriageProtection,
                                            Optional<YesOrNo> respOrdersRestraining,
                                            Optional<YesOrNo> respOrdersOtherInjunctive,
                                            Optional<YesOrNo> respOrdersUndertakingInPlace) {
        return respOrdersNonMolestation.isPresent()
                && respOrdersOccupation.isPresent()
                && respOrdersForcedMarriageProtection.isPresent()
                && respOrdersRestraining.isPresent()
                && respOrdersOtherInjunctive.isPresent()
                && respOrdersUndertakingInPlace.isPresent();
    }

    public boolean isSectionsFinished(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData,
                                      boolean resPreviousOrders) {

        boolean isFinished;
        isFinished =  validateOrders(respondentAllegationsOfHarmData)
                && resPreviousOrders
                && validateAbductionSection(respondentAllegationsOfHarmData)
                && validateOtherConcerns(respondentAllegationsOfHarmData)
                && validateSubstanceAbuse(respondentAllegationsOfHarmData)
                && validateChildContact(respondentAllegationsOfHarmData);
        return isFinished;
    }


    public boolean validateAbductionSection(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {

        Optional<YesOrNo> respAohChildAbduction =
                ofNullable(respondentAllegationsOfHarmData.getRespAohChildAbductionYesNo());
        Optional<String> respChildAbductionReasons =
                ofNullable(respondentAllegationsOfHarmData.getRespChildAbductionReasons());
        Optional<YesOrNo> respPreviousAbductionThreats =
                ofNullable(respondentAllegationsOfHarmData.getRespPreviousAbductionThreats());
        Optional<String> respPreviousAbductionThreatsDetails =
                ofNullable(respondentAllegationsOfHarmData.getRespPreviousAbductionThreatsDetails());
        Optional<YesOrNo> respAbductionPassportOfficeNotified =
                ofNullable(respondentAllegationsOfHarmData.getRespAbductionPassportOfficeNotified());
        Optional<YesOrNo> respAbductionChildHasPassport =
                ofNullable(respondentAllegationsOfHarmData.getRespAbductionChildHasPassport());
        Optional<YesOrNo> respAbductionPreviousPoliceInvolvement =
                ofNullable(respondentAllegationsOfHarmData.getRespAbductionPreviousPoliceInvolvement());
        Optional<String> respAbductionPreviousPoliceInvolvementDetails =
                ofNullable(respondentAllegationsOfHarmData.getRespAbductionPreviousPoliceInvolvementDetails());

        boolean respAbductionSectionCompleted;
        boolean respPreviousThreatSectionComplete = false;
        boolean respPassportCompleted = respAbductionPassportOfficeNotified.isPresent();
        boolean respHasPassportCompleted = respAbductionChildHasPassport.isPresent();
        boolean respPoliceCompleted = false;
        boolean respPassportPossessionCompleted = false;
        if (respAohChildAbduction.isPresent() && No.equals(respAohChildAbduction.get())) {
            return true;
        }

        if (respAohChildAbduction.isPresent()) {
            if (Yes.equals(respAohChildAbduction.get())) {
                respAbductionSectionCompleted = respChildAbductionReasons.isPresent();

                respPreviousThreatSectionComplete = isPreviousThreatSectionComplete(
                        respPreviousAbductionThreats,
                        respPreviousAbductionThreatsDetails
                );
                if (respAbductionChildHasPassport.isPresent() && Yes.equals(respAbductionChildHasPassport.get())) {
                    Optional<List<RespPassportPossessionEnum>> respAbductionChildPassportPosessionList =
                            ofNullable(respondentAllegationsOfHarmData.getRespChildPassportDetails().getRespChildPassportPossession());
                    Optional<String> abductionChildPassportPosessionOtherDetail = ofNullable(respondentAllegationsOfHarmData
                            .getRespChildPassportDetails()
                            .getRespChildPassportPossessionOtherDetails());
                    respPassportPossessionCompleted = isPassportPossessionCompleted(
                            respAbductionChildPassportPosessionList,
                            abductionChildPassportPosessionOtherDetail
                    );
                } else {
                    respPassportPossessionCompleted = true;
                }

                respPoliceCompleted = isPoliceInvolvementCompleted(
                        respAbductionPreviousPoliceInvolvement,
                        respAbductionPreviousPoliceInvolvementDetails
                );

            } else {
                respAbductionSectionCompleted = true;
            }
            return respAbductionSectionCompleted
                    && respPreviousThreatSectionComplete
                    && respPassportCompleted
                    && respHasPassportCompleted
                    && respPassportPossessionCompleted
                    && respPoliceCompleted;

        } else {
            return false;
        }
    }

    private boolean isPoliceInvolvementCompleted(
            Optional<YesOrNo> respAbductionPreviousPoliceInvolvement,
            Optional<String> respAbductionPreviousPoliceInvolvementDetails) {
        boolean policeCompleted = true;
        if (respAbductionPreviousPoliceInvolvement.isPresent()) {
            if (Yes.equals(respAbductionPreviousPoliceInvolvement.get())) {
                policeCompleted = respAbductionPreviousPoliceInvolvementDetails.isPresent();
            } else {
                policeCompleted = true;
            }
        }
        return policeCompleted;
    }

    private boolean isPassportPossessionCompleted(
            Optional<List<RespPassportPossessionEnum>> respAbductionChildPassportPossession,
            Optional<String> respAbductionChildPassportPossessionOtherDetail) {
        boolean passportPossessionCompleted = false;
        if (respAbductionChildPassportPossession.isPresent()) {
            if (respAbductionChildPassportPossession.get().contains(RespPassportPossessionEnum.otherPerson)) {
                passportPossessionCompleted = respAbductionChildPassportPossessionOtherDetail.isPresent();
            } else {
                passportPossessionCompleted = true;
            }
        }
        return passportPossessionCompleted;
    }

    private boolean isPreviousThreatSectionComplete(
            Optional<YesOrNo> respPreviousAbductionThreats,
            Optional<String> respPreviousAbductionThreatsDetails) {
        boolean respPreviousThreatSectionComplete = true;
        if (respPreviousAbductionThreats.isPresent() && Yes.equals(respPreviousAbductionThreats.get())) {
            if (Yes.equals(respPreviousAbductionThreats.get())) {
                respPreviousThreatSectionComplete = respPreviousAbductionThreatsDetails.isPresent();
            } else {
                respPreviousThreatSectionComplete = true;
            }
        }
        return respPreviousThreatSectionComplete;
    }

    public boolean validateDomesticAbuseBehaviours(RespDomesticAbuseBehaviours respDomesticAbuseBehaviours) {

        Optional<String> respBehavioursStartDateAndLength = ofNullable(respDomesticAbuseBehaviours.getRespBehavioursStartDateAndLength());
        Optional<String> respAbuseNatureDescription = ofNullable(respDomesticAbuseBehaviours.getRespAbuseNatureDescription());
        Optional<YesOrNo> respBehavioursApplicantSoughtHelp = ofNullable(respDomesticAbuseBehaviours.getRespBehavioursApplicantSoughtHelp());
        Optional<String> respBehavioursApplicantHelpSoughtWho = ofNullable(respDomesticAbuseBehaviours.getRespBehavioursApplicantHelpSoughtWho());
        List<Optional<?>> fields = new ArrayList<>();
        fields.add(ofNullable(respDomesticAbuseBehaviours.getRespTypeOfAbuse().getDisplayedValue()));
        fields.add(respAbuseNatureDescription);
        fields.add(respBehavioursStartDateAndLength);
        fields.add(respBehavioursApplicantSoughtHelp);
        if (respBehavioursApplicantSoughtHelp.isPresent()
                && respBehavioursApplicantSoughtHelp.get().equals(Yes)) {
            fields.add(respBehavioursApplicantHelpSoughtWho);
        }

        return fields.stream().noneMatch(Optional::isEmpty)
                && fields.stream().filter(Optional::isPresent)
                .map(Optional::get).noneMatch(field -> field.equals(""));

    }


    public boolean validateChildAbuseBehaviours(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData, RespChildAbuse respChildAbuse,
                                                ChildAbuseEnum childAbuseEnum) {
        Optional<YesOrNo> allChildrenAreRisk = ofNullable(respondentAllegationOfHarmService
                .getIfAllChildrenAreRisk(childAbuseEnum,respondentAllegationsOfHarmData));

        Optional<String> respAbuseNatureDescription = ofNullable(respChildAbuse.getRespAbuseNatureDescription());
        Optional<String> respBehavioursApplicantHelpSoughtWho = ofNullable(respChildAbuse.getRespBehavioursApplicantHelpSoughtWho());

        List<Optional<?>> fields = new ArrayList<>();
        if (allChildrenAreRisk.isPresent()
                && allChildrenAreRisk.get().equals(No)) {
            Optional<List<DynamicMultiselectListElement>> whichChildrenAreRisk = ofNullable(respondentAllegationOfHarmService
                    .getWhichChildrenAreInRisk(
                            childAbuseEnum,respondentAllegationsOfHarmData).getValue());
            fields.add(whichChildrenAreRisk);
        }
        if (respAbuseNatureDescription.isPresent()) {
            fields.add(respAbuseNatureDescription);
        }
        Optional<String> respBehavioursStartDateAndLength = ofNullable(respChildAbuse.getRespBehavioursStartDateAndLength());
        if (respBehavioursStartDateAndLength.isPresent()) {
            fields.add(respBehavioursStartDateAndLength);
        }
        Optional<YesOrNo> respBehavioursApplicantSoughtHelp = ofNullable(respChildAbuse.getRespBehavioursApplicantSoughtHelp());

        if (respBehavioursApplicantSoughtHelp.isPresent()
                && respBehavioursApplicantSoughtHelp.get().equals(Yes)) {
            fields.add(respBehavioursApplicantSoughtHelp);
            fields.add(respBehavioursApplicantHelpSoughtWho);
        }
        return fields.stream().noneMatch(Optional::isEmpty)
                && fields.stream().filter(Optional::isPresent)
                .map(Optional::get).noneMatch(field -> field.equals(""));
    }


    public boolean validateSubstanceAbuse(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {
        Optional<YesOrNo> respAohSubstanceAbuse = ofNullable(respondentAllegationsOfHarmData
                .getRespAohSubstanceAbuseYesNo());
        Optional<String> respAohOtherSubstanceAbuseDetails = ofNullable(respondentAllegationsOfHarmData
                .getRespAohSubstanceAbuseDetails());

        List<Optional<?>> fields = new ArrayList<>();
        fields.add(respAohSubstanceAbuse);
        if (respAohSubstanceAbuse.isPresent() && respAohSubstanceAbuse.get().equals(Yes)) {
            fields.add(respAohOtherSubstanceAbuseDetails);
        }

        return fields.stream().noneMatch(Optional::isEmpty)
                && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
    }


    public boolean validateOtherConcerns(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {
        Optional<YesOrNo> respAohOtherConcerns = ofNullable(respondentAllegationsOfHarmData
                .getRespAohOtherConcerns());
        Optional<String> respAohOtherConcernsDetails = ofNullable(respondentAllegationsOfHarmData
                .getRespAohOtherConcernsDetails());
        Optional<String> respAohOtherConcernsCourtActions = ofNullable(respondentAllegationsOfHarmData
                .getRespAohOtherConcernsCourtActions());

        List<Optional<?>> fields = new ArrayList<>();
        fields.add(respAohOtherConcerns);
        if (respAohOtherConcerns.isPresent() && respAohOtherConcerns.get().equals(Yes)) {
            fields.add(respAohOtherConcernsDetails);
        }
        fields.add(respAohOtherConcernsCourtActions);

        return fields.stream().noneMatch(Optional::isEmpty)
                && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
    }

    public boolean validateChildContact(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {

        Optional<YesOrNo> respAgreeChildUnsupervisedTime = ofNullable(respondentAllegationsOfHarmData.getRespAgreeChildUnsupervisedTime());
        Optional<YesOrNo> respAgreeChildSupervisedTime = ofNullable(respondentAllegationsOfHarmData.getRespAgreeChildSupervisedTime());
        Optional<YesOrNo> respAgreeChildOtherContact = ofNullable(respondentAllegationsOfHarmData.getRespAgreeChildOtherContact());


        List<Optional<?>> fields = new ArrayList<>();
        fields.add(respAgreeChildUnsupervisedTime);
        fields.add(respAgreeChildSupervisedTime);
        fields.add(respAgreeChildOtherContact);

        return fields.stream().noneMatch(Optional::isEmpty);

    }

    public boolean validateOrders(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {

        boolean nonMolesComplete = true;
        boolean occupationComplete = true;
        boolean forcedMarComplete = true;
        boolean restrainComplete = true;
        boolean otherComplete = true;
        boolean underComplete = true;

        Optional<YesOrNo> ordersNonMolestation = ofNullable(respondentAllegationsOfHarmData.getRespOrdersNonMolestation());
        if (ordersNonMolestation.isPresent() && ordersNonMolestation.get().equals(Yes)) {
            nonMolesComplete = validateNonMolestationOrder(respondentAllegationsOfHarmData);
        }
        Optional<YesOrNo> ordersOccupation = ofNullable(respondentAllegationsOfHarmData.getRespOrdersOccupation());
        if (ordersOccupation.isPresent() && ordersOccupation.get().equals(Yes)) {
            occupationComplete = validateOccupationOrder(respondentAllegationsOfHarmData);
        }
        Optional<YesOrNo> ordersForcedMarriageProtection = ofNullable(respondentAllegationsOfHarmData.getRespOrdersForcedMarriageProtection());
        if (ordersForcedMarriageProtection.isPresent() && ordersForcedMarriageProtection.get().equals(Yes)) {
            forcedMarComplete = validateForcedMarriageProtectionOrder(respondentAllegationsOfHarmData);
        }
        Optional<YesOrNo> ordersRestraining = ofNullable(respondentAllegationsOfHarmData.getRespOrdersRestraining());
        if (ordersRestraining.isPresent() && ordersRestraining.get().equals(Yes)) {
            restrainComplete = validateRestrainingOrder(respondentAllegationsOfHarmData);
        }
        Optional<YesOrNo> ordersOtherInjunctive = ofNullable(respondentAllegationsOfHarmData.getRespOrdersOtherInjunctive());
        if (ordersOtherInjunctive.isPresent() && ordersOtherInjunctive.get().equals(Yes)) {
            otherComplete = validateOtherInjunctiveOrder(respondentAllegationsOfHarmData);
        }
        Optional<YesOrNo> ordersUndertakingInPlace = ofNullable(respondentAllegationsOfHarmData.getRespOrdersUndertakingInPlace());
        if (ordersUndertakingInPlace.isPresent() && ordersUndertakingInPlace.get().equals(Yes)) {
            underComplete = validateUndertakingInPlaceOrder(respondentAllegationsOfHarmData);
        }

        return nonMolesComplete
                && occupationComplete
                && forcedMarComplete
                && restrainComplete
                && otherComplete
                && underComplete;
    }


    public boolean validateNonMolestationOrder(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {
        Optional<YesOrNo> ordersNonMolestationCurrent = ofNullable(respondentAllegationsOfHarmData.getRespOrdersNonMolestationCurrent());
        return ordersNonMolestationCurrent.isPresent();
    }

    public boolean validateOccupationOrder(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {
        Optional<YesOrNo> ordersOccupationCurrent = ofNullable(respondentAllegationsOfHarmData.getRespOrdersOccupationCurrent());
        return ordersOccupationCurrent.isPresent();
    }

    public boolean validateForcedMarriageProtectionOrder(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {
        Optional<YesOrNo> ordersForcedMarriageProtectionCurrent = ofNullable(respondentAllegationsOfHarmData
                .getRespOrdersForcedMarriageProtectionCurrent());
        return ordersForcedMarriageProtectionCurrent.isPresent();
    }

    public boolean validateRestrainingOrder(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {
        Optional<YesOrNo> ordersRestrainingCurrent = ofNullable(respondentAllegationsOfHarmData.getRespOrdersRestrainingCurrent());
        return ordersRestrainingCurrent.isPresent();
    }

    public boolean validateOtherInjunctiveOrder(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {
        Optional<YesOrNo> ordersOtherInjunctiveCurrent = ofNullable(respondentAllegationsOfHarmData.getRespOrdersOtherInjunctiveCurrent());
        return ordersOtherInjunctiveCurrent.isPresent();
    }

    public boolean validateUndertakingInPlaceOrder(RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {
        Optional<YesOrNo> ordersUndertakingInPlaceCurrent = ofNullable(respondentAllegationsOfHarmData.getRespOrdersUndertakingInPlaceCurrent());
        return ordersUndertakingInPlaceCurrent.isPresent();
    }
}
