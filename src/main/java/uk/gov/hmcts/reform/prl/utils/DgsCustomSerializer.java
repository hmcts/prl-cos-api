package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.enums.MiamChildProtectionConcernChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.Behaviours;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonRelationshipToChild;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
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

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

public class DgsCustomSerializer extends JsonSerializer<CaseData> {
    @Override
    public void serialize(CaseData value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();

        //Type of application
        gen.writeStringField("id", String.valueOf(value.getId()));
        gen.writeStringField("applicantCaseName", value.getApplicantCaseName());

        if(ofNullable(value.getOrdersApplyingFor()).isPresent()) {
            List<String> ordersApplyingFor = value.getOrdersApplyingFor()
                .stream()
                .map(OrderTypeEnum::getDisplayedValue).collect(
                    Collectors.toList());
            gen.writeObjectField("ordersApplyingFor",ordersApplyingFor);
        }

        gen.writeStringField("typeOfChildArrangementsOrder",ofNullable(value.getTypeOfChildArrangementsOrder()).map(
                ChildArrangementOrderTypeEnum::getDisplayedValue).orElse(null));

        gen.writeStringField("natureOfOrder", value.getNatureOfOrder());
        gen.writeStringField("consentOrder",value.getConsentOrder().getValue());
        gen.writeStringField("applicationPermissionRequired", value.getApplicationPermissionRequired()
            .getDisplayedValue());
        gen.writeStringField("applicationPermissionRequiredReason", value.getApplicationPermissionRequiredReason());
        gen.writeStringField("applicationDetails", value.getApplicationDetails());

        //Hearing urgency
        gen.writeStringField("isCaseUrgent", value.getIsCaseUrgent().getValue());
        gen.writeStringField("caseUrgencyTimeAndReason", value.getCaseUrgencyTimeAndReason());
        gen.writeStringField("effortsMadeWithRespondents", value.getEffortsMadeWithRespondents());
        gen.writeStringField("doYouNeedAWithoutNoticeHearing", value.getDoYouNeedAWithoutNoticeHearing()
            .getValue());
        gen.writeStringField("reasonsForApplicationWithoutNotice", value.getReasonsForApplicationWithoutNotice());
        gen.writeStringField("doYouRequireAHearingWithReducedNotice", value.getDoYouRequireAHearingWithReducedNotice()
            .getValue());
        gen.writeStringField("setOutReasonsBelow", value.getSetOutReasonsBelow());
        gen.writeStringField("areRespondentsAwareOfProceedings", value.getAreRespondentsAwareOfProceedings()
            .getValue());

        //Applicant details
        List<Element<Map<String, Object>>> mappedApplicants = new ArrayList<>();
        for (Element<PartyDetails> app : value.getApplicants()) {
            Map<String, Object> apps = new HashMap<>(serializeApplicantDetails(app.getValue()));
            Element<Map<String, Object>> appElement = element(app.getId(), apps);
            mappedApplicants.add(appElement);
        }
        gen.writeObjectField("applicants", mappedApplicants);

        //Children
        List<Element<Map<String, Object>>> mappedChildren = new ArrayList<>();
        for (Element<Child> c : value.getChildren()) {
            Map<String, Object> child = new HashMap<>(serializeChild(c.getValue()));
            Element<Map<String, Object>> childElement = element(c.getId(), child);
            mappedChildren.add(childElement);
        }
        gen.writeObjectField("children", mappedChildren);
        gen.writeStringField("childrenKnownToLocalAuthority", value.getChildrenKnownToLocalAuthority()
            .getDisplayedValue());
        gen.writeStringField("childrenKnownToLocalAuthorityTextArea", value.getChildrenKnownToLocalAuthorityTextArea());
        gen.writeStringField("childrenSubjectOfChildProtectionPlan", value.getChildrenSubjectOfChildProtectionPlan()
            .getDisplayedValue());

        //Respondent details
        List<Element<Map<String, Object>>> mappedRespondents = new ArrayList<>();
        for (Element<PartyDetails> res : value.getRespondents()) {
            Map<String, Object> respondent = new HashMap<>(serializeRespondentDetails(res.getValue()));
            Element<Map<String, Object>> resElement = element(res.getId(), respondent);
            mappedRespondents.add(resElement);
        }
        gen.writeObjectField("respondents", mappedRespondents);

        //MIAM
        gen.writeStringField("applicantAttendedMiam", ofNullable(value.getApplicantAttendedMiam())
                                                                     .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("claimingExemptionMiam", ofNullable(value.getClaimingExemptionMiam())
            .map(YesOrNo::getValue).orElse(null));
        gen.writeStringField("familyMediatorMiam", ofNullable(value.getFamilyMediatorMiam())
            .map(YesOrNo::getValue).orElse(null));
        List<String> exemptions = Optional.of(value.getMiamExemptionsChecklist().stream()
                                                 .map(MiamExemptionsChecklistEnum::getDisplayedValue)
                                                 .collect(Collectors.toList())).orElse(null);
        gen.writeObjectField("miamExemptionsChecklist", exemptions);
        List<String> violence = Optional.of(value.getMiamDomesticViolenceChecklist().stream()
                                                  .map(MiamDomesticViolenceChecklistEnum::getDisplayedValue)
                                                  .collect(Collectors.toList())).orElse(null);
        gen.writeObjectField("miamDomesticViolenceChecklist", exemptions);
        List<String> urgency = Optional.of(value.getMiamUrgencyReasonChecklist().stream()
                                                  .map(MiamUrgencyReasonChecklistEnum::getDisplayedValue)
                                                  .collect(Collectors.toList())).orElse(null);
        gen.writeObjectField("miamUrgencyReasonChecklist", exemptions);
        List<String> childProtection = Optional.of(value.getMiamChildProtectionConcernList().stream()
                                                  .map(MiamChildProtectionConcernChecklistEnum::getDisplayedValue)
                                                  .collect(Collectors.toList())).orElse(null);
        gen.writeObjectField("miamChildProtectionConcernList", exemptions);
        gen.writeStringField("miamPreviousAttendanceChecklist", ofNullable(value.
                                                                               getMiamPreviousAttendanceChecklist()
                                                                               .getDisplayedValue()).orElse(null));
        gen.writeStringField("miamOtherGroundsChecklist", ofNullable(value.getMiamOtherGroundsChecklist()
                                                                         .getDisplayedValue()).orElse(null));
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
        gen.writeStringField("allegationsOfHarmYesNo", ofNullable(value.getAllegationsOfHarmYesNo()
                                                                      .getValue()).orElse(null));
        gen.writeStringField("allegationsOfHarmDomesticAbuseYesNo",
                             ofNullable(value.getAllegationsOfHarmDomesticAbuseYesNo().getValue()).orElse(null));
        gen.writeObjectField("physicalAbuseVictim", Optional.of(value.getPhysicalAbuseVictim().stream()
                                                                    .map(ApplicantOrChildren::getDisplayedValue)
                                                                    .collect(Collectors.toList())).orElse(null));
        gen.writeObjectField("emotionalAbuseVictim", Optional.of(value.getEmotionalAbuseVictim().stream()
                                                                    .map(ApplicantOrChildren::getDisplayedValue)
                                                                    .collect(Collectors.toList())).orElse(null));
        gen.writeObjectField("psychologicalAbuseVictim", Optional.of(value.getPsychologicalAbuseVictim().stream()
                                                                    .map(ApplicantOrChildren::getDisplayedValue)
                                                                    .collect(Collectors.toList())).orElse(null));
        gen.writeObjectField("sexualAbuseVictim", Optional.of(value.getSexualAbuseVictim().stream()
                                                                    .map(ApplicantOrChildren::getDisplayedValue)
                                                                    .collect(Collectors.toList())).orElse(null));
        gen.writeObjectField("financialAbuseVictim", Optional.of(value.getFinancialAbuseVictim().stream()
                                                                    .map(ApplicantOrChildren::getDisplayedValue)
                                                                    .collect(Collectors.toList())).orElse(null));
        gen.writeStringField("allegationsOfHarmChildAbductionYesNo",
                             ofNullable(value.getAllegationsOfHarmChildAbductionYesNo()
                                            .getValue()).orElse(null));
        gen.writeStringField("childAbductionReasons", ofNullable(value.getChildAbductionReasons()).orElse(null));
        gen.writeStringField("previousAbductionThreats", ofNullable(value.getPreviousAbductionThreats()
                                                                      .getValue()).orElse(null));
        gen.writeStringField("previousAbductionThreatsDetails", ofNullable(value.getPreviousAbductionThreatsDetails())
            .orElse(null));
        gen.writeStringField("childrenLocationNow", ofNullable(value.getChildrenLocationNow()).orElse(null));
        gen.writeStringField("abductionPassportOfficeNotified", ofNullable(value.getAbductionPassportOfficeNotified()
                                                                        .getValue()).orElse(null));
        gen.writeStringField("abductionChildHasPassport", ofNullable(value.getAbductionChildHasPassport()
                                                                        .getValue()).orElse(null));
        gen.writeStringField("abductionChildPassportPosession", ofNullable(value.getAbductionChildPassportPosession()
                                                                         .getDisplayedValue()).orElse(null));
        gen.writeStringField("abductionChildPassportPosessionOtherDetail",
                             ofNullable(value.getAbductionChildPassportPosessionOtherDetail()).orElse(null));
        gen.writeStringField("abductionPreviousPoliceInvolvement", ofNullable(value.getAbductionPreviousPoliceInvolvement()
                                                                         .getValue()).orElse(null));
        gen.writeStringField("abductionPreviousPoliceInvolvementDetails",
                             ofNullable(value.getAbductionPreviousPoliceInvolvementDetails()).orElse(null));
        gen.writeStringField("allegationsOfHarmChildAbuseYesNo", ofNullable(value.getAllegationsOfHarmChildAbuseYesNo()
                                                                                  .getValue()).orElse(null));
        gen.writeStringField("allegationsOfHarmSubstanceAbuseYesNo", ofNullable(value.getAllegationsOfHarmSubstanceAbuseYesNo()
                                                                                  .getValue()).orElse(null));
        gen.writeStringField("allegationsOfHarmOtherConcernsYesNo", ofNullable(value.getAllegationsOfHarmOtherConcernsYesNo()
                                                                                  .getValue()).orElse(null));
        List<Element<Map<String, Object>>> mappedBehaviours = new ArrayList<>();
        for (Element<Behaviours> beh : value.getBehaviours()) {
            Map<String, Object> behaviour = new HashMap<>(serializeBehaviour(beh.getValue()));
            Element<Map<String, Object>> behElement = element(beh.getId(), behaviour);
            mappedBehaviours.add(behElement);
        }
        gen.writeObjectField("behaviours", mappedBehaviours);






    }

    public Map<String, Object> serializeBehaviour(Behaviours behaviour) {
        Map<String, Object> behaviourMap = new HashMap<>();
        behaviourMap.put("abuseNatureDescription", ofNullable(behaviour.getAbuseNatureDescription()).orElse(null));
        behaviourMap.put("behavioursStartDateAndLength", ofNullable(behaviour.getBehavioursStartDateAndLength())
            .orElse(null));
        behaviourMap.put("behavioursNature", ofNullable(behaviour.getBehavioursNature()).orElse(null));
        behaviourMap.put("behavioursApplicantSoughtHelp", ofNullable(behaviour.getBehavioursApplicantSoughtHelp()
                                                                               .getValue()).orElse(null));
        behaviourMap.put("behavioursApplicantHelpSoughtWho", ofNullable(behaviour.getBehavioursApplicantHelpSoughtWho())
            .orElse(null));
        behaviourMap.put("behavioursApplicantHelpAction", ofNullable(behaviour.getBehavioursApplicantHelpAction())
            .orElse(null));
        return behaviourMap;
    }


    public Map<String, Object> serializeChild(Child child) {
        Map<String, Object> childMap = new HashMap<>();
        childMap.put("firstName", child.getFirstName());
        childMap.put("lastName", child.getLastName());
        childMap.put("dateOfBirth", child.getDateOfBirth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        childMap.put("gender", child.getGender().getDisplayedValue());
        childMap.put("otherGender", child.getOtherGender());
        List<String> orderType = child.getOrderAppliedFor().stream()
                .map(OrderTypeEnum::getDisplayedValue).collect(Collectors.toList());
        childMap.put("orderAppliedFor", orderType);
        childMap.put("applicantsRelationshipToChild", child.getRespondentsRelationshipToChild().getDisplayedValue());
        childMap.put("otherApplicantsRelationshipToChild", child.getApplicantsRelationshipToChild());
        childMap.put("respondentsRelationshipToChild", child.getRespondentsRelationshipToChild().getDisplayedValue());
        childMap.put("otherRespondentsRelationshipToChild", child.getOtherRespondentsRelationshipToChild());
        List<String> childLiveWith = child.getChildLiveWith().stream().map(LiveWithEnum::getDisplayedValue)
                .collect(Collectors.toList());
        childMap.put("childLiveWith", childLiveWith);
        List<Element<Map<String, Object>>> otherPerson = new ArrayList<>();
        for (Element<OtherPersonWhoLivesWithChild> o : child.getPersonWhoLivesWithChild()) {
            Map<String, Object> personMap = new HashMap<>(serializeOtherPerson(o.getValue()));
            Element<Map<String, Object>> otherElement = element(o.getId(), personMap);
            otherPerson.add(otherElement);
        }
        childMap.put("personWhoLivesWithChild", otherPerson);
        return childMap;
    }

    public Map<String, Object> serializeOtherPerson(OtherPersonWhoLivesWithChild other) {
        Map<String, Object> otherMap = new HashMap<>();
        otherMap.put("firstName", other.getFirstName());
        otherMap.put("lastName", other.getLastName());
        otherMap.put("relationshipToChildDetails", other.getRelationshipToChildDetails());
        otherMap.put("address", other.getAddress());
        otherMap.put("isPersonIdentityConfidential", other.getIsPersonIdentityConfidential().getValue());
        return otherMap;
    }

    public Map<String, Object> serializeApplicantDetails(PartyDetails party) {
        Map<String, Object> partyMap = new HashMap<>();
        partyMap.put("firstName", party.getFirstName());
        partyMap.put("lastName", party.getLastName());
        partyMap.put("previousName", party.getPreviousName());
        partyMap.put("dateOfBirth", party.getDateOfBirth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        partyMap.put("gender", party.getGender().getDisplayedValue());
        partyMap.put("otherGender", party.getOtherGender());
        partyMap.put("placeOfBirth", party.getPlaceOfBirth());
        partyMap.put("address", party.getAddress());
        partyMap.put("isAddressConfidential", party.getIsAddressConfidential().getValue());
        partyMap.put("isAtAddressLessThan5Years", party.getIsAtAddressLessThan5Years().getValue());
        partyMap.put("addressLivedLessThan5YearsDetails", party.getAddressLivedLessThan5YearsDetails());
        partyMap.put("canYouProvideEmailAddress", party.getCanYouProvideEmailAddress().getValue());
        partyMap.put("email", party.getEmail());
        partyMap.put("isEmailAddressConfidential", party.getIsEmailAddressConfidential().getValue());
      //  partyMap.put("landline", party.getLandline());
        partyMap.put("phoneNumber", party.getPhoneNumber());
        partyMap.put("isPhoneNumberConfidential", ofNullable(party.getIsPhoneNumberConfidential().getValue()).orElse(null));
      //  partyMap.put("relationshipToChildren", party.getRelationshipToChildren());
      //  partyMap.put("isDateOfBirthKnown", party.getIsDateOfBirthKnown().getValue());
       // partyMap.put("canYouProvidePhoneNumber", party.getCanYouProvidePhoneNumber().getValue());
      //  partyMap.put("isPlaceOfBirthKnown", party.getIsPlaceOfBirthKnown().getValue());
       // partyMap.put("isCurrentAddressKnown", party.getIsCurrentAddressKnown().getValue());
//        List<Element<Map<String, Object>>> otherPersonRelationship = new ArrayList<>();
//        for (Element<OtherPersonRelationshipToChild> o : party.getOtherPersonRelationshipToChildren()) {
//            Map<String, Object> otherMap = new HashMap<>();
//            otherMap.put("personRelationshipToChild", o.getValue().getPersonRelationshipToChild());
//            Element<Map<String, Object>> otherElement = element(o.getId(), otherMap);
//            otherPersonRelationship.add(otherElement);
//        }
        partyMap.put("solicitorOrg", party.getSolicitorOrg());
        partyMap.put("solicitorAddress", party.getSolicitorAddress());
        partyMap.put("dxNumber", party.getDxNumber());
        partyMap.put("solicitorReference", party.getSolicitorReference());
        partyMap.put("representativeFirstName", party.getRepresentativeFirstName());
        partyMap.put("representativeLastName", party.getRepresentativeLastName());
        //partyMap.put("isAtAddressLessThan5YearsWithDontKnow", party.getIsAtAddressLessThan5YearsWithDontKnow()
         //   .getDisplayedValue());
     //   partyMap.put("doTheyHaveLegalRepresentation", party.getDoTheyHaveLegalRepresentation().getDisplayedValue());
      //  partyMap.put("sendSignUpLink", party.getSendSignUpLink());
        partyMap.put("solicitorEmail", party.getSolicitorEmail());

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
        partyMap.put("canYouProvideEmailAddress", party.getCanYouProvideEmailAddress().getValue());
        partyMap.put("email", party.getEmail());
        partyMap.put("canYouProvidePhoneNumber", party.getCanYouProvidePhoneNumber().getValue());
        partyMap.put("phoneNumber", party.getPhoneNumber());
        partyMap.put("doTheyHaveLegalRepresentation", party.getDoTheyHaveLegalRepresentation().getDisplayedValue());
        partyMap.put("representativeFirstName", party.getRepresentativeFirstName());
        partyMap.put("representativeLastName", party.getRepresentativeLastName());
        partyMap.put("solicitorEmail", party.getSolicitorEmail());
        partyMap.put("solicitorOrg", party.getSolicitorOrg());
        partyMap.put("dxNumber", party.getDxNumber());
        partyMap.put("sendSignUpLink", party.getSendSignUpLink());

        return partyMap;
    }



}
