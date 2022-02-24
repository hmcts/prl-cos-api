package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.gov.hmcts.reform.prl.enums.AbductionChildPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.DocumentCategoryEnum;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.enums.MiamChildProtectionConcernChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamOtherGroundsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamPreviousAttendanceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.PermissionRequiredEnum;
import uk.gov.hmcts.reform.prl.enums.ProceedingsEnum;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.SpokenOrWrittenWelshEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Behaviours;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.InterpreterNeed;
import uk.gov.hmcts.reform.prl.models.complextypes.LocalCourtAdminEmail;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonRelationshipToChild;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.WelshNeed;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

public class DgsSerializer extends JsonSerializer<CaseData> {

    @Override
    public void serialize(CaseData value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        //Type of application
        gen.writeStringField("id", String.valueOf(ofNullable(value.getId()).orElse(null)));
        gen.writeStringField("applicantCaseName", ofNullable(value.getApplicantCaseName()).orElse(null));

        if (ofNullable(value.getOrdersApplyingFor()).isPresent()) {
            List<String> ordersApplyingFor = value.getOrdersApplyingFor()
                .stream()
                .map(OrderTypeEnum::getDisplayedValue).collect(
                    Collectors.toList());
            gen.writeObjectField("ordersApplyingFor",ordersApplyingFor);
        }

        gen.writeStringField("typeOfChildArrangementsOrder",ofNullable(value.getTypeOfChildArrangementsOrder()).map(
            ChildArrangementOrderTypeEnum::getDisplayedValue).orElse(null));

        gen.writeStringField("natureOfOrder", ofNullable(value.getNatureOfOrder()).orElse(null));
        gen.writeStringField("consentOrder",ofNullable(value.getConsentOrder()).map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("applicationPermissionRequired", ofNullable(value.getApplicationPermissionRequired()).map(
                PermissionRequiredEnum::getDisplayedValue)
            .orElse(null));
        gen.writeStringField("applicationPermissionRequiredReason", ofNullable(value.getApplicationPermissionRequiredReason()).orElse(null));
        gen.writeStringField("applicationDetails", ofNullable(value.getApplicationDetails()).orElse(null));

        //Hearing urgency
        gen.writeStringField("isCaseUrgent", ofNullable(value.getIsCaseUrgent()).map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("caseUrgencyTimeAndReason", ofNullable(value.getCaseUrgencyTimeAndReason()).orElse(null));
        gen.writeStringField("effortsMadeWithRespondents", ofNullable(value.getEffortsMadeWithRespondents()).orElse(null));
        gen.writeStringField("doYouNeedAWithoutNoticeHearing", ofNullable(value.getDoYouNeedAWithoutNoticeHearing())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("reasonsForApplicationWithoutNotice", ofNullable(value.getReasonsForApplicationWithoutNotice()).orElse(null));
        gen.writeStringField("doYouRequireAHearingWithReducedNotice", ofNullable(value.getDoYouRequireAHearingWithReducedNotice())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("setOutReasonsBelow", ofNullable(value.getSetOutReasonsBelow()).orElse(null));
        gen.writeStringField("areRespondentsAwareOfProceedings", ofNullable(value.getAreRespondentsAwareOfProceedings())
            .map(YesOrNo::getValue).orElse(null));

        //Applicant details
        if (ofNullable(value.getApplicants()).isPresent()) {
            List<Element<Map<String, Object>>> mappedApplicants = new ArrayList<>();
            for (Element<PartyDetails> app : value.getApplicants()) {
                Map<String, Object> apps = new HashMap<>(serializeApplicantDetails(app.getValue()));
                Element<Map<String, Object>> appElement = element(app.getId(), apps);
                mappedApplicants.add(appElement);
            }
            gen.writeObjectField("applicants", mappedApplicants);
        }

        //Children
        if (ofNullable(value.getChildren()).isPresent()) {
            List<Element<Map<String, Object>>> mappedChildren = new ArrayList<>();
            for (Element<Child> c : value.getChildren()) {
                Map<String, Object> child = new HashMap<>(serializeChild(c.getValue()));
                Element<Map<String, Object>> childElement = element(c.getId(), child);
                mappedChildren.add(childElement);
            }
            gen.writeObjectField("children", mappedChildren);
        }
        gen.writeStringField("childrenKnownToLocalAuthority", ofNullable(value.getChildrenKnownToLocalAuthority())
            .map(YesNoDontKnow::getDisplayedValue).orElse(null));
        gen.writeStringField("childrenKnownToLocalAuthorityTextArea", ofNullable(value.getChildrenKnownToLocalAuthorityTextArea()).orElse(null));
        gen.writeStringField("childrenSubjectOfChildProtectionPlan", ofNullable(value.getChildrenSubjectOfChildProtectionPlan())
            .map(YesNoDontKnow::getDisplayedValue).orElse(null));

        //Respondent details
        if (ofNullable(value.getRespondents()).isPresent()) {
            List<Element<Map<String, Object>>> mappedRespondents = new ArrayList<>();
            for (Element<PartyDetails> res : value.getRespondents()) {
                Map<String, Object> respondent = new HashMap<>(serializeRespondentDetails(res.getValue()));
                Element<Map<String, Object>> resElement = element(res.getId(), respondent);
                mappedRespondents.add(resElement);
            }
            gen.writeObjectField("respondents", mappedRespondents);
        }

        //MIAM
        gen.writeStringField("applicantAttendedMiam", ofNullable(value.getApplicantAttendedMiam())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("claimingExemptionMiam", ofNullable(value.getClaimingExemptionMiam())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("familyMediatorMiam", ofNullable(value.getFamilyMediatorMiam())
            .map(YesOrNo::getValue).orElse(null));

        if (ofNullable(value.getMiamExemptionsChecklist()).isPresent()) {
            gen.writeObjectField("miamExemptionsChecklist", value.getMiamExemptionsChecklist().stream()
                .map(MiamExemptionsChecklistEnum::getDisplayedValue)
                .collect(Collectors.toList()));
        } else {
            gen.writeObjectField("miamExemptionsChecklist", null);
        }
        if (ofNullable(value.getMiamDomesticViolenceChecklist()).isPresent()) {
            gen.writeObjectField("miamDomesticViolenceChecklist", value.getMiamDomesticViolenceChecklist().stream()
                .map(MiamDomesticViolenceChecklistEnum::getDisplayedValue)
                .collect(Collectors.toList()));
        } else {
            gen.writeObjectField("miamDomesticViolenceChecklist", null);
        }
        if (ofNullable(value.getMiamUrgencyReasonChecklist()).isPresent()) {
            gen.writeObjectField("miamUrgencyReasonChecklist", value.getMiamUrgencyReasonChecklist().stream()
                .map(MiamUrgencyReasonChecklistEnum::getDisplayedValue)
                .collect(Collectors.toList()));
        } else {
            gen.writeObjectField("miamUrgencyReasonChecklist", null);
        }
        if (ofNullable(value.getMiamChildProtectionConcernList()).isPresent()) {
            gen.writeObjectField("miamChildProtectionConcernList", value.getMiamChildProtectionConcernList().stream()
                .map(MiamChildProtectionConcernChecklistEnum::getDisplayedValue)
                .collect(Collectors.toList()));
        } else {
            gen.writeObjectField("miamChildProtectionConcernList", null);
        }

        gen.writeStringField("miamPreviousAttendanceChecklist", ofNullable(value.getMiamPreviousAttendanceChecklist()).map(
            MiamPreviousAttendanceChecklistEnum::getDisplayedValue).orElse(null));
        gen.writeStringField("miamOtherGroundsChecklist", ofNullable(value.getMiamOtherGroundsChecklist()).map(
            MiamOtherGroundsChecklistEnum::getDisplayedValue).orElse(null));

        gen.writeStringField("mediatorRegistrationNumber", ofNullable(value.getMediatorRegistrationNumber())
            .orElse(null));
        gen.writeStringField("familyMediatorServiceName", ofNullable(value.getFamilyMediatorServiceName())
            .orElse(null));
        gen.writeStringField("soleTraderName", ofNullable(value.getSoleTraderName())
            .orElse(null));
        gen.writeStringField("mediatorRegistrationNumber1", ofNullable(value.getMediatorRegistrationNumber1())
            .orElse(null));
        gen.writeStringField("familyMediatorServiceName1", ofNullable(value.getFamilyMediatorServiceName1())
            .orElse(null));
        gen.writeStringField("soleTraderName1", ofNullable(value.getSoleTraderName1())
            .orElse(null));

        //Allegations of harm
        gen.writeStringField("allegationsOfHarmYesNo", ofNullable(value.getAllegationsOfHarmYesNo())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("allegationsOfHarmDomesticAbuseYesNo", ofNullable(value.getAllegationsOfHarmSubstanceAbuseYesNo())
            .map(YesOrNo::getValue).orElse(null));

        if (ofNullable(value.getPhysicalAbuseVictim()).isPresent()) {
            gen.writeObjectField("physicalAbuseVictim", value.getPhysicalAbuseVictim().stream()
                .map(ApplicantOrChildren::getDisplayedValue)
                .collect(Collectors.toList()));
        } else {
            gen.writeObjectField("physicalAbuseVictim", null);
        }
        if (ofNullable(value.getEmotionalAbuseVictim()).isPresent()) {
            gen.writeObjectField("emotionalAbuseVictim", value.getEmotionalAbuseVictim().stream()
                .map(ApplicantOrChildren::getDisplayedValue)
                .collect(Collectors.toList()));
        } else {
            gen.writeObjectField("emotionalAbuseVictim", null);
        }
        if (ofNullable(value.getPsychologicalAbuseVictim()).isPresent()) {
            gen.writeObjectField("psychologicalAbuseVictim", value.getPsychologicalAbuseVictim().stream()
                .map(ApplicantOrChildren::getDisplayedValue)
                .collect(Collectors.toList()));
        } else {
            gen.writeObjectField("psychologicalAbuseVictim", null);
        }
        if (ofNullable(value.getSexualAbuseVictim()).isPresent()) {
            gen.writeObjectField("sexualAbuseVictim", value.getSexualAbuseVictim().stream()
                .map(ApplicantOrChildren::getDisplayedValue)
                .collect(Collectors.toList()));
        } else {
            gen.writeObjectField("sexualAbuseVictim", null);
        }
        if (ofNullable(value.getFinancialAbuseVictim()).isPresent()) {
            gen.writeObjectField("financialAbuseVictim", value.getFinancialAbuseVictim().stream()
                .map(ApplicantOrChildren::getDisplayedValue)
                .collect(Collectors.toList()));
        } else {
            gen.writeObjectField("financialAbuseVictim", null);
        }

        gen.writeStringField("allegationsOfHarmChildAbductionYesNo",
                             ofNullable(value.getAllegationsOfHarmChildAbductionYesNo())
                                 .map(YesOrNo::getValue).orElse(null));

        gen.writeStringField("childAbductionReasons", ofNullable(value.getChildAbductionReasons()).orElse(null));
        gen.writeStringField("previousAbductionThreats",
                             ofNullable(value.getPreviousAbductionThreats())
                                 .map(YesOrNo::getValue).orElse(null));

        gen.writeStringField("previousAbductionThreatsDetails", ofNullable(value.getPreviousAbductionThreatsDetails())
            .orElse(null));
        gen.writeStringField("childrenLocationNow", ofNullable(value.getChildrenLocationNow()).orElse(null));
        gen.writeStringField("abductionPassportOfficeNotified",
                             ofNullable(value.getAbductionPassportOfficeNotified())
                                 .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("abductionChildHasPassport",
                             ofNullable(value.getAbductionChildHasPassport())
                                 .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("abductionChildPassportPosession",
                             ofNullable(value.getAbductionChildPassportPosession())
                                 .map(AbductionChildPassportPossessionEnum::getDisplayedValue).orElse(null));
        gen.writeStringField("abductionChildPassportPosessionOtherDetail",
                             ofNullable(value.getAbductionChildPassportPosessionOtherDetail()).orElse(null));
        gen.writeStringField("abductionPreviousPoliceInvolvement",
                             ofNullable(value.getAbductionPreviousPoliceInvolvement())
                                 .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("abductionPreviousPoliceInvolvementDetails",
                             ofNullable(value.getAbductionPreviousPoliceInvolvementDetails()).orElse(null));
        gen.writeStringField("allegationsOfHarmChildAbuseYesNo",
                             ofNullable(value.getAllegationsOfHarmChildAbuseYesNo())
                                 .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("allegationsOfHarmSubstanceAbuseYesNo",
                             ofNullable(value.getAllegationsOfHarmSubstanceAbuseYesNo())
                                 .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("allegationsOfHarmOtherConcernsYesNo",
                             ofNullable(value.getAllegationsOfHarmOtherConcerns())
                                 .map(YesOrNo::getValue).orElse(null));

        if (ofNullable(value.getBehaviours()).isPresent()) {
            List<Element<Map<String, Object>>> mappedBehaviours = new ArrayList<>();
            for (Element<Behaviours> beh : value.getBehaviours()) {
                Map<String, Object> behaviour = new HashMap<>(serializeBehaviour(beh.getValue()));
                Element<Map<String, Object>> behElement = element(beh.getId(), behaviour);
                mappedBehaviours.add(behElement);
            }
            gen.writeObjectField("behaviours", mappedBehaviours);
        } else {
            gen.writeObjectField("behaviours",null);
        }
        gen.writeStringField("ordersNonMolestation", ofNullable(value.getOrdersNonMolestation())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("ordersOccupation", ofNullable(value.getOrdersOccupation())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("ordersForcedMarriageProtection", ofNullable(value.getOrdersForcedMarriageProtection())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("ordersRestraining", ofNullable(value.getOrdersRestraining())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("ordersOtherInjunctive", ofNullable(value.getOrdersOtherInjunctive())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("ordersUndertakingInPlace", ofNullable(value.getOrdersUndertakingInPlace())
            .map(YesOrNo::getValue).orElse(null));

        if (ofNullable(value.getOrdersNonMolestationDateIssued()).isPresent()) {
            gen.writeStringField("ordersNonMolestationDateIssued", value.getOrdersNonMolestationDateIssued()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            gen.writeStringField("ordersNonMolestationDateIssued", null);
        }
        if (ofNullable(value.getOrdersNonMolestationEndDate()).isPresent()) {
            gen.writeStringField("ordersNonMolestationEndDate", value.getOrdersNonMolestationEndDate()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            gen.writeStringField("ordersNonMolestationEndDate", null);
        }
        gen.writeStringField("ordersNonMolestationCurrent", ofNullable(value.getOrdersNonMolestationCurrent())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("ordersNonMolestationCourtName",
                             ofNullable(value.getOrdersNonMolestationCourtName()).orElse(null));

        if (ofNullable(value.getOrdersOccupationDateIssued()).isPresent()) {
            gen.writeStringField("ordersOccupationDateIssued", value.getOrdersOccupationDateIssued()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            gen.writeStringField("ordersOccupationDateIssued", null);
        }
        if (ofNullable(value.getOrdersOccupationEndDate()).isPresent()) {
            gen.writeStringField("ordersOccupationEndDate", value.getOrdersOccupationEndDate()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            gen.writeStringField("ordersOccupationEndDate", null);
        }
        gen.writeStringField("ordersOccupationCurrent", ofNullable(value.getOrdersOccupationCurrent())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("ordersOccupationCourtName",
                             ofNullable(value.getOrdersOccupationCourtName()).orElse(null));

        if (ofNullable(value.getOrdersForcedMarriageProtectionDateIssued()).isPresent()) {
            gen.writeStringField("ordersForcedMarriageProtectionDateIssued",
                                 value.getOrdersForcedMarriageProtectionDateIssued()
                                     .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            gen.writeStringField("ordersForcedMarriageProtectionDateIssued", null);
        }
        if (ofNullable(value.getOrdersForcedMarriageProtectionEndDate()).isPresent()) {
            gen.writeStringField("ordersForcedMarriageProtectionEndDate",
                                 value.getOrdersForcedMarriageProtectionEndDate()
                                     .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            gen.writeStringField("ordersForcedMarriageProtectionEndDate", null);
        }
        gen.writeStringField("ordersForcedMarriageProtectionCurrent",
                             ofNullable(value.getOrdersForcedMarriageProtectionCurrent())
                                 .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("ordersForcedMarriageProtectionCourtName",
                             ofNullable(value.getOrdersForcedMarriageProtectionCourtName()).orElse(null));

        if (ofNullable(value.getOrdersRestrainingDateIssued()).isPresent()) {
            gen.writeStringField("ordersRestrainingDateIssued", value.getOrdersRestrainingDateIssued()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            gen.writeStringField("ordersRestrainingDateIssued", null);
        }
        if (ofNullable(value.getOrdersRestrainingEndDate()).isPresent()) {
            gen.writeStringField("ordersRestrainingEndDate", value.getOrdersRestrainingEndDate()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            gen.writeStringField("ordersRestrainingEndDate", null);
        }
        gen.writeStringField("ordersRestrainingCurrent", ofNullable(value.getOrdersRestrainingCurrent())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("ordersRestrainingCourtName",
                             ofNullable(value.getOrdersRestrainingCourtName()).orElse(null));

        if (ofNullable(value.getOrdersOtherInjunctiveDateIssued()).isPresent()) {
            gen.writeStringField("ordersOtherInjunctiveDateIssued", value.getOrdersOtherInjunctiveDateIssued()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            gen.writeStringField("ordersOtherInjunctiveDateIssued", null);
        }
        if (ofNullable(value.getOrdersOtherInjunctiveEndDate()).isPresent()) {
            gen.writeStringField("ordersOtherInjunctiveEndDate", value.getOrdersOtherInjunctiveEndDate()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            gen.writeStringField("ordersOtherInjunctiveEndDate", null);
        }
        gen.writeStringField("ordersOtherInjunctiveCurrent", ofNullable(value.getOrdersOtherInjunctiveCurrent())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("ordersOtherInjunctiveCourtName",
                             ofNullable(value.getOrdersOtherInjunctiveCourtName()).orElse(null));

        if (ofNullable(value.getOrdersUndertakingInPlaceDateIssued()).isPresent()) {
            gen.writeStringField("ordersUndertakingInPlaceDateIssued", value.getOrdersUndertakingInPlaceDateIssued()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            gen.writeStringField("ordersUndertakingInPlaceDateIssued", null);
        }
        if (ofNullable(value.getOrdersUndertakingInPlaceEndDate()).isPresent()) {
            gen.writeStringField("ordersUndertakingInPlaceEndDate", value.getOrdersUndertakingInPlaceEndDate()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            gen.writeStringField("ordersUndertakingInPlaceEndDate", null);
        }
        gen.writeStringField("ordersUndertakingInPlaceCurrent", ofNullable(value.getOrdersUndertakingInPlaceCurrent())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("ordersUndertakingInPlaceCourtName",
                             ofNullable(value.getOrdersUndertakingInPlaceCourtName()).orElse(null));

        gen.writeStringField("allegationsOfHarmOtherConcerns", ofNullable(value.getAllegationsOfHarmOtherConcernsYesNo())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("allegationsOfHarmOtherConcernsDetails",
                             ofNullable(value.getAllegationsOfHarmOtherConcernsDetails()).orElse(null));
        gen.writeStringField("allegationsOfHarmOtherConcernsCourtActions",
                             ofNullable(value.getAllegationsOfHarmOtherConcernsCourtActions()).orElse(null));
        gen.writeStringField("agreeChildUnsupervisedTime", ofNullable(value.getAgreeChildUnsupervisedTime())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("agreeChildSupervisedTime", ofNullable(value.getAgreeChildSupervisedTime())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("agreeChildOtherContact", ofNullable(value.getAgreeChildOtherContact())
            .map(YesOrNo::getValue).orElse(null));

        //Other people in the case

        if (ofNullable(value.getOthersToNotify()).isPresent()) {
            List<Element<Map<String, Object>>> mappedOthers = new ArrayList<>();
            for (Element<PartyDetails> o : value.getOthersToNotify()) {
                Map<String, Object> other = new HashMap<>(serializeOtherPeopleInTheCase(o.getValue()));
                Element<Map<String, Object>> othEle = element(o.getId(), other);
                mappedOthers.add(othEle);
            }
            gen.writeObjectField("othersToNotify", mappedOthers);
        } else {
            gen.writeObjectField("othersToNotify",null);
        }

        //Other proceedings
        gen.writeStringField("previousOrOngoingProceedingsForChildren",
                             ofNullable(value.getPreviousOrOngoingProceedingsForChildren())
                                 .map(YesNoDontKnow::getDisplayedValue).orElse(null));
        if (ofNullable(value.getExistingProceedings()).isPresent()) {
            List<Element<Map<String, Object>>> mappedProceedings = new ArrayList<>();
            for (Element<ProceedingDetails> p : value.getExistingProceedings()) {
                Map<String, Object> pro = new HashMap<>(serializeExistingProceedings(p.getValue()));
                Element<Map<String, Object>> proEle = element(p.getId(), pro);
                mappedProceedings.add(proEle);
            }
            gen.writeObjectField("existingProceedings", mappedProceedings);
        } else {
            gen.writeObjectField("existingProceedings",null);
        }

        //Attending the hearing
        gen.writeStringField("isWelshNeeded", ofNullable(value.getAgreeChildOtherContact())
            .map(YesOrNo::getValue).orElse(null));
        if (ofNullable(value.getWelshNeeds()).isPresent()) {
            List<Element<Map<String,Object>>> mappedWelsh = new ArrayList<>();
            for (Element<WelshNeed> welshNeed:value.getWelshNeeds()) {
                Map<String,Object> wels = new HashMap<>(serializedWelshNeeds(welshNeed.getValue()));
                Element<Map<String,Object>> welEle = element(welshNeed.getId(),wels);
                mappedWelsh.add(welEle);
            }
            gen.writeObjectField("welshNeeds",mappedWelsh);
        } else {
            gen.writeObjectField("welshNeeds",null);
            gen.writeStringField("isInterpreterNeeded",ofNullable(value.getIsInterpreterNeeded())
                .map(YesOrNo::getValue).orElse(null));
        }

        if (ofNullable(value.getInterpreterNeeds()).isPresent()) {
            List<Element<Map<String,Object>>> mappedInter = new ArrayList<>();
            for (Element<InterpreterNeed> interNeed:value.getInterpreterNeeds()) {
                Map<String,Object> inter = new HashMap<>(serializedInterNeed(interNeed.getValue()));
                Element<Map<String,Object>> interEle = element(interNeed.getId(),inter);
                mappedInter.add(interEle);
            }
            gen.writeObjectField("interpreterNeeds",mappedInter);
        } else {
            gen.writeObjectField("interpreterNeeds",null);
        }

        gen.writeStringField("isDisabilityPresent",ofNullable(value.getIsDisabilityPresent())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("adjustmentsRequired",ofNullable(value.getAdjustmentsRequired()).orElse(null));
        gen.writeStringField("isSpecialArrangementsRequired",ofNullable(value.getIsSpecialArrangementsRequired())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("specialArrangementsRequired",ofNullable(value.getSpecialArrangementsRequired()).orElse(null));
        gen.writeStringField("isIntermediaryNeeded",ofNullable(value.getIsIntermediaryNeeded())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("reasonsForIntermediary",ofNullable(value.getReasonsForIntermediary()).orElse(null));


        //International element.
        gen.writeStringField("habitualResidentInOtherState", ofNullable(value.getHabitualResidentInOtherState())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("habitualResidentInOtherStateGiveReason",ofNullable(value.getHabitualResidentInOtherStateGiveReason()).orElse(null));
        gen.writeStringField("jurisdictionIssue",ofNullable(value.getJurisdictionIssue())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("jurisdictionIssueGiveReason",ofNullable(value.getJurisdictionIssueGiveReason()).orElse(null));
        gen.writeStringField("requestToForeignAuthority",ofNullable(value.getRequestToForeignAuthority())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("requestToForeignAuthorityGiveReason",ofNullable(value.getRequestToForeignAuthorityGiveReason()).orElse(null));


        // Litigation capacity.
        gen.writeStringField("litigationCapacityFactors",ofNullable(value.getLitigationCapacityFactors()).orElse(null));
        gen.writeStringField("litigationCapacityReferrals",ofNullable(value.getLitigationCapacityReferrals()).orElse(null));
        gen.writeStringField("litigationCapacityOtherFactors",ofNullable(value.getLitigationCapacityOtherFactors())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("litigationCapacityOtherFactorsDetails",ofNullable(value.getLitigationCapacityOtherFactorsDetails()).orElse(null));


        //Welsh language requirements.
        gen.writeStringField("welshLanguageRequirement",ofNullable(value.getWelshLanguageRequirement())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("languageRequirementApplicationNeedWelsh",ofNullable(value.getLanguageRequirementApplicationNeedWelsh())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("welshLanguageRequirementApplicationNeedEnglish",ofNullable(value.getWelshLanguageRequirementApplicationNeedEnglish())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeObjectField("paymentCallbackServiceRequestUpdate",ofNullable(value.getPaymentCallbackServiceRequestUpdate()).orElse(null));


        //Add case number.
        gen.writeStringField("familymanCaseNumber",ofNullable(value.getFamilymanCaseNumber()).orElse(null));

        //Manage Documents.
        gen.writeStringField("documentCategory",ofNullable(value.getDocumentCategory())
            .map(DocumentCategoryEnum::getDisplayedValue).orElse(null));


        //Return Application.
        gen.writeStringField("returnMessage",ofNullable(value.getReturnMessage()).orElse(null));

        //Issue and send to local court
        if (ofNullable(value.getLocalCourtAdminEmail()).isPresent()) {
            List<Element<Map<String,Object>>> mappedCourtAdminEmail = new ArrayList<>();
            for (Element<LocalCourtAdminEmail> localAdmin:value.getLocalCourtAdminEmail()) {
                Map<String,Object> localAdminCourt = new HashMap<>(serilizedLocalAdmin(localAdmin.getValue()));
                Element<Map<String,Object>> localElement = element(localAdmin.getId(),localAdminCourt);
                mappedCourtAdminEmail.add(localElement);
            }
            gen.writeObjectField("localCourtAdminEmail",mappedCourtAdminEmail);
        } else {
            gen.writeObjectField("localCourtAdminEmail",null);
        }


        //This field contains Application Submitter solicitor email address.
        gen.writeStringField("applicantSolicitorEmailAddress",ofNullable(value.getApplicantSolicitorEmailAddress())
            .orElse(null));
        gen.writeStringField("respondentSolicitorEmailAddress",ofNullable(value.getRespondentSolicitorEmailAddress())
            .orElse(null));
        gen.writeStringField("caseworkerEmailAddress",ofNullable(value.getCaseworkerEmailAddress())
            .orElse(null));


        //Court details.
        gen.writeStringField("courtName",ofNullable(value.getCourtName()).orElse(null));
        gen.writeStringField("courtId",ofNullable(value.getCourtId()).orElse(null));
    }



    public Map<String,Object> serilizedLocalAdmin(LocalCourtAdminEmail localCourt) {
        Map<String,Object> localAdminMap = new HashMap<>();
        localAdminMap.put("email",ofNullable(localCourt.getEmail()).orElse(null));
        return localAdminMap;
    }


    public Map<String,Object> serializedInterNeed(InterpreterNeed inter) {
        Map<String, Object> interMap = new HashMap<>();
        if (ofNullable(inter.getParty()).isPresent()) {
            interMap.put("party", inter.getParty().stream()
                .map(PartyEnum::getDisplayedValue)
                .collect(Collectors.toList()));
        }

        interMap.put("name",ofNullable(inter.getName()).orElse(null));
        interMap.put("language",ofNullable(inter.getLanguage()).orElse(null));
        interMap.put("otherAssistance",ofNullable(inter.getOtherAssistance()).orElse(null));
        return interMap;
    }

    public Map<String,Object> serializedWelshNeeds(WelshNeed wel) {
        Map<String,Object> welshMap = new HashMap<>();
        welshMap.put("whoNeedsWelsh",ofNullable(wel.getWhoNeedsWelsh()).orElse(null));
        if (ofNullable(wel.getSpokenOrWritten()).isPresent()) {
            welshMap.put("spokenOrWritten",wel.getSpokenOrWritten().stream()
                .map(SpokenOrWrittenWelshEnum::getDisplayedValue)
                .collect(Collectors.toList()));
        }
        return welshMap;
    }

    public Map<String, Object> serializeExistingProceedings(ProceedingDetails pro) {
        Map<String, Object> proceedingsMap = new HashMap<>();
        proceedingsMap.put("previousOrOngoingProceedings", ofNullable(pro.getPreviousOrOngoingProceedings()).map(
            ProceedingsEnum::getDisplayedValue).orElse(null));
        proceedingsMap.put("caseNumber", ofNullable(pro.getCaseNumber()).orElse(null));
        if (ofNullable(pro.getDateStarted()).isPresent()) {
            proceedingsMap.put("dateStarted", pro.getDateStarted()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            proceedingsMap.put("dateStarted", null);
        }
        if (ofNullable(pro.getDateEnded()).isPresent()) {
            proceedingsMap.put("dateEnded", pro.getDateEnded()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            proceedingsMap.put("dateEnded", null);
        }
        if (ofNullable(pro.getTypeOfOrder()).isPresent()) {
            proceedingsMap.put("typeOfOrder", pro.getTypeOfOrder().stream()
                .map(TypeOfOrderEnum::getDisplayedValue)
                .collect(Collectors.toList()));
        } else {
            proceedingsMap.put("typeOfOrder", null);
        }
        proceedingsMap.put("otherTypeOfOrder", ofNullable(pro.getOtherTypeOfOrder()));
        proceedingsMap.put("nameOfJudge", ofNullable(pro.getNameOfJudge()));
        proceedingsMap.put("nameOfCourt", ofNullable(pro.getNameOfCourt()));
        proceedingsMap.put("nameOfChildrenInvolved", ofNullable(pro.getNameOfChildrenInvolved()));
        proceedingsMap.put("nameOfGuardian", ofNullable(pro.getNameOfGuardian()));
        proceedingsMap.put("nameAndOffice", ofNullable(pro.getNameAndOffice()));

        return proceedingsMap;
    }


    public Map<String, Object> serializeOtherPeopleInTheCase(PartyDetails party) {
        Map<String, Object> partyMap = new HashMap<>();
        partyMap.put("firstName", ofNullable(party.getFirstName()).orElse(null));
        partyMap.put("lastName", ofNullable(party.getLastName()).orElse(null));
        partyMap.put("isDateOfBirthKnown", ofNullable(party.getIsDateOfBirthKnown()).map(YesOrNo::getValue).orElse(null));
        if (ofNullable(party.getIsDateOfBirthKnown()).isPresent()
            && ofNullable(party.getIsDateOfBirthKnown()).get().equals(YesOrNo.Yes)) {
            partyMap.put("dateOfBirth", party.getDateOfBirth()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        partyMap.put("gender", ofNullable(party.getGender()).map(Gender::getDisplayedValue).orElse(null));
        if (ofNullable(party.getGender()).isPresent() && ofNullable(party.getGender()).get().equals(Gender.other)) {
            partyMap.put("otherGender", ofNullable(party.getOtherGender()).orElse(null));
        }
        partyMap.put("isPlaceOfBirthKnown", ofNullable(party.getIsPlaceOfBirthKnown()).map(YesOrNo::getValue).orElse(null));
        if (ofNullable(party.getIsPlaceOfBirthKnown()).isPresent()
            && ofNullable(party.getIsPlaceOfBirthKnown()).get().equals(YesOrNo.Yes)) {
            partyMap.put("placeOfBirth", ofNullable(party.getPlaceOfBirth()).orElse(null));
        }
        partyMap.put("isCurrentAddressKnown", ofNullable(party.getIsCurrentAddressKnown())
            .map(YesOrNo::getValue).orElse(null));
        if (ofNullable(party.getIsCurrentAddressKnown()).isPresent()
            && ofNullable(party.getIsCurrentAddressKnown()).get().equals(YesOrNo.Yes)) {
            partyMap.put("address", ofNullable(party.getAddress()).orElse(null));
        }
        partyMap.put("canYouProvideEmailAddress", ofNullable(party.getCanYouProvideEmailAddress())
            .map(YesOrNo::getValue).orElse(null));
        if (ofNullable(party.getCanYouProvideEmailAddress()).isPresent()
            && ofNullable(party.getCanYouProvideEmailAddress()).get().equals(YesOrNo.Yes)) {
            partyMap.put("email", ofNullable(party.getEmail()).orElse(null));
        }
        partyMap.put("canYouProvidePhoneNumber", ofNullable(party.getCanYouProvidePhoneNumber())
            .map(YesOrNo::getValue).orElse(null));
        if (ofNullable(party.getCanYouProvidePhoneNumber()).isPresent()
            && ofNullable(party.getCanYouProvidePhoneNumber()).get().equals(YesOrNo.Yes)) {
            partyMap.put("phoneNumber", ofNullable(party.getPhoneNumber()).orElse(null));
        }
        if (ofNullable(party.getOtherPersonRelationshipToChildren()).isPresent()) {
            List<Element<Map<String, Object>>> othRel = new ArrayList<>();
            for (Element<OtherPersonRelationshipToChild> o : party.getOtherPersonRelationshipToChildren()) {
                Map<String, Object> relMap = new HashMap<>(serializeOtherPersonRelationshipToChildren(o.getValue()));
                Element<Map<String, Object>> relElement = element(o.getId(), relMap);
                othRel.add(relElement);
            }
            partyMap.put("otherPersonRelationshipToChildren", othRel);

        } else {
            partyMap.put("otherPersonRelationshipToChildren", null);
        }

        return partyMap;
    }

    public Map<String, Object> serializeOtherPersonRelationshipToChildren(OtherPersonRelationshipToChild rel) {
        Map<String, Object> oth = new HashMap<>();
        oth.put("personRelationshipToChild", ofNullable(rel.getPersonRelationshipToChild()).orElse(null));
        return oth;
    }



    public Map<String, Object> serializeBehaviour(Behaviours behaviour) {
        Map<String, Object> behaviourMap = new HashMap<>();
        behaviourMap.put("abuseNatureDescription", ofNullable(behaviour.getAbuseNatureDescription()).orElse(null));
        behaviourMap.put("behavioursStartDateAndLength", ofNullable(behaviour.getBehavioursStartDateAndLength())
            .orElse(null));
        behaviourMap.put("behavioursNature", ofNullable(behaviour.getBehavioursNature()).orElse(null));
        behaviourMap.put("behavioursApplicantSoughtHelp", ofNullable(behaviour.getBehavioursApplicantSoughtHelp())
            .map(YesOrNo::getValue).orElse(null));
        behaviourMap.put("behavioursApplicantHelpSoughtWho", ofNullable(behaviour.getBehavioursApplicantHelpSoughtWho())
            .orElse(null));
        behaviourMap.put("behavioursApplicantHelpAction", ofNullable(behaviour.getBehavioursApplicantHelpAction())
            .orElse(null));
        return behaviourMap;
    }


    public Map<String, Object> serializeChild(Child child) {
        Map<String, Object> childMap = new HashMap<>();
        childMap.put("firstName", ofNullable(child.getFirstName()).orElse(null));
        childMap.put("lastName", ofNullable(child.getLastName()).orElse(null));
        if (ofNullable(child.getDateOfBirth()).isPresent()) {
            childMap.put("dateOfBirth", child.getDateOfBirth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            childMap.put("dateOfBirth", null);
        }
        childMap.put("gender", ofNullable(child.getGender()).map(Gender::getDisplayedValue).orElse(null));
        childMap.put("otherGender", ofNullable(child.getOtherGender()).orElse(null));
        if (ofNullable(child.getOrderAppliedFor()).isPresent()) {
            List<String> orderType = child.getOrderAppliedFor().stream().map(OrderTypeEnum::getDisplayedValue).collect(
                Collectors.toList());
            childMap.put("orderAppliedFor", orderType);
        } else {
            childMap.put("orderAppliedFor", null);
        }
        childMap.put("applicantsRelationshipToChild", ofNullable(child.getApplicantsRelationshipToChild())
            .map(RelationshipsEnum::getDisplayedValue).orElse(null));
        childMap.put("otherApplicantsRelationshipToChild", ofNullable(child.getApplicantsRelationshipToChild()).orElse(null));
        childMap.put("respondentsRelationshipToChild", ofNullable(child.getRespondentsRelationshipToChild())
            .map(RelationshipsEnum::getDisplayedValue).orElse(null));
        childMap.put("otherRespondentsRelationshipToChild", ofNullable(child.getOtherRespondentsRelationshipToChild()).orElse(null));
        if (ofNullable(child.getChildLiveWith()).isPresent()) {
            childMap.put("childLiveWith", child.getChildLiveWith().stream().map(LiveWithEnum::getDisplayedValue)
                .collect(Collectors.toList()));
        } else {
            childMap.put("childLiveWith", null);
        }
        List<Element<Map<String, Object>>> otherPerson = new ArrayList<>();
        if (ofNullable(child.getPersonWhoLivesWithChild()).isPresent()) {
            for (Element<OtherPersonWhoLivesWithChild> o : child.getPersonWhoLivesWithChild()) {
                Map<String, Object> personMap = new HashMap<>(serializeOtherPerson(o.getValue()));
                Element<Map<String, Object>> otherElement = element(o.getId(), personMap);
                otherPerson.add(otherElement);
            }
            childMap.put("personWhoLivesWithChild", otherPerson);
        } else {
            childMap.put("personWhoLivesWithChild", null);
        }

        return childMap;
    }

    public Map<String, Object> serializeOtherPerson(OtherPersonWhoLivesWithChild other) {
        Map<String, Object> otherMap = new HashMap<>();
        otherMap.put("firstName", ofNullable(other.getFirstName()).orElse(null));
        otherMap.put("lastName", ofNullable(other.getLastName()).orElse(null));
        otherMap.put("relationshipToChildDetails", ofNullable(other.getRelationshipToChildDetails()).orElse(null));
        otherMap.put("address", ofNullable(other.getAddress()).orElse(null));
        otherMap.put("isPersonIdentityConfidential", ofNullable(other.getIsPersonIdentityConfidential()).map(YesOrNo::getValue)
            .orElse(null));
        return otherMap;
    }

    public Map<String, Object> serializeApplicantDetails(PartyDetails party) {
        Map<String, Object> partyMap = new HashMap<>();
        partyMap.put("firstName", ofNullable(party.getFirstName()).orElse(null));
        partyMap.put("lastName", ofNullable(party.getLastName()).orElse(null));
        partyMap.put("previousName", ofNullable(party.getPreviousName()).orElse(null));
        if (ofNullable(party.getDateOfBirth()).isPresent()) {
            partyMap.put("dateOfBirth", party.getDateOfBirth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            partyMap.put("dateOfBirth", null);
        }
        partyMap.put("gender", ofNullable(party.getGender()).map(Gender::getDisplayedValue).orElse(null));
        partyMap.put("otherGender", ofNullable(party.getOtherGender()).orElse(null));
        partyMap.put("placeOfBirth", ofNullable(party.getPlaceOfBirth()).orElse(null));
        partyMap.put("address", ofNullable(party.getAddress()).orElse(null));
        partyMap.put("isAddressConfidential", ofNullable(party.getIsAddressConfidential()).map(YesOrNo::getValue).orElse(null));
        partyMap.put("isAtAddressLessThan5Years", ofNullable(party.getIsAtAddressLessThan5Years()).map(YesOrNo::getValue).orElse(null));
        partyMap.put("addressLivedLessThan5YearsDetails", ofNullable(party.getAddressLivedLessThan5YearsDetails()).orElse(null));
        partyMap.put("canYouProvideEmailAddress",ofNullable(party.getCanYouProvideEmailAddress()).map(YesOrNo::getValue).orElse(null));
        partyMap.put("email", ofNullable(party.getEmail()).orElse(null));
        partyMap.put("isEmailAddressConfidential", ofNullable(party.getIsEmailAddressConfidential()).map(YesOrNo::getValue).orElse(null));
        partyMap.put("phoneNumber", ofNullable(party.getPhoneNumber()).orElse(null));
        partyMap.put("isPhoneNumberConfidential", ofNullable(party.getIsPhoneNumberConfidential()).map(YesOrNo::getValue)
            .orElse(null));
        partyMap.put("solicitorOrg", ofNullable(party.getSolicitorOrg()).orElse(null));
        partyMap.put("solicitorAddress", ofNullable(party.getSolicitorAddress()).orElse(null));
        partyMap.put("dxNumber", ofNullable(party.getDxNumber()).orElse(null));
        partyMap.put("solicitorReference", ofNullable(party.getSolicitorReference()).orElse(null));
        partyMap.put("representativeFirstName", ofNullable(party.getRepresentativeFirstName()).orElse(null));
        partyMap.put("representativeLastName", ofNullable(party.getRepresentativeLastName()).orElse(null));
        partyMap.put("solicitorEmail", ofNullable(party.getSolicitorEmail()).orElse(null));

        return partyMap;
    }

    public Map<String, Object> serializeRespondentDetails(PartyDetails party) {
        Map<String, Object> partyMap = new HashMap<>();
        partyMap.put("firstName", ofNullable(party.getFirstName()).orElse(null));
        partyMap.put("lastName", ofNullable(party.getLastName()).orElse(null));
        partyMap.put("previousName", ofNullable(party.getPreviousName()).orElse(null));
        partyMap.put("isDateOfBirthKnown", ofNullable(party.getIsDateOfBirthKnown())
            .map(YesOrNo::getValue).orElse(null));
        Optional<LocalDate> dob = ofNullable(party.getDateOfBirth());
        if (dob.isPresent()) {
            partyMap.put("dateOfBirth", party.getDateOfBirth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
            partyMap.put("dateOfBirth", null);
        }
        partyMap.put("gender", ofNullable(party.getGender()).map(Gender::getDisplayedValue).orElse(null));
        partyMap.put("otherGender", ofNullable(party.getOtherGender()).orElse(null));
        partyMap.put("isPlaceOfBirthKnown", ofNullable(party.getIsPlaceOfBirthKnown()).map(YesOrNo::getValue)
            .orElse(null));
        partyMap.put("placeOfBirth", ofNullable(party.getPlaceOfBirth()).orElse(null));
        partyMap.put("isCurrentAddressKnown", ofNullable(party.getIsCurrentAddressKnown()).map(YesOrNo::getValue)
            .orElse(null));
        partyMap.put("address", ofNullable(party.getAddress()).orElse(null));
        partyMap.put("isAtAddressLessThan5YearsWithDontKnow", ofNullable(party.getIsAtAddressLessThan5YearsWithDontKnow())
            .map(YesNoDontKnow::getDisplayedValue).orElse(null));
        partyMap.put("addressLivedLessThan5YearsDetails", ofNullable(party.getAddressLivedLessThan5YearsDetails()).orElse(null));
        partyMap.put("canYouProvideEmailAddress", ofNullable(party.getCanYouProvideEmailAddress()).map(YesOrNo::getValue).orElse(null));
        partyMap.put("email", ofNullable(party.getEmail()).orElse(null));
        partyMap.put("canYouProvidePhoneNumber", ofNullable(party.getCanYouProvidePhoneNumber()).map(YesOrNo::getValue).orElse(null));
        partyMap.put("phoneNumber", ofNullable(party.getPhoneNumber()).orElse(null));
        partyMap.put("doTheyHaveLegalRepresentation", ofNullable(party.getDoTheyHaveLegalRepresentation()).map(YesNoDontKnow::getDisplayedValue)
            .orElse(null));
        partyMap.put("representativeFirstName", ofNullable(party.getRepresentativeFirstName()).orElse(null));
        partyMap.put("representativeLastName", ofNullable(party.getRepresentativeLastName()).orElse(null));
        partyMap.put("solicitorEmail", ofNullable(party.getSolicitorEmail()).orElse(null));
        partyMap.put("solicitorOrg", ofNullable(party.getSolicitorOrg()).orElse(null));
        partyMap.put("dxNumber", ofNullable(party.getDxNumber()).orElse(null));
        partyMap.put("sendSignUpLink", ofNullable(party.getSendSignUpLink()).orElse(null));

        return partyMap;
    }

}

