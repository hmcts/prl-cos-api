package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingToChildEnum;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.FamilyHomeEnum;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.enums.LivingSituationEnum;
import uk.gov.hmcts.reform.prl.enums.MiamChildProtectionConcernChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamOtherGroundsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamPreviousAttendanceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MortgageNamedAfterEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.PeopleLivingAtThisAddressEnum;
import uk.gov.hmcts.reform.prl.enums.ReasonForOrderWithoutGivingNoticeEnum;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401Proceedings;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.complextypes.Landlord;
import uk.gov.hmcts.reform.prl.models.complextypes.LinkToCA;
import uk.gov.hmcts.reform.prl.models.complextypes.Mortgage;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChildDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ReasonForWithoutNoticeOrder;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBailConditionDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBehaviour;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationDateInfo;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationObjectType;
import uk.gov.hmcts.reform.prl.models.complextypes.addcafcassofficer.ChildAndCafcassOfficer;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Applicant;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.ApplicantFamily;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.AttendingTheHearing;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.ChildDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.FL401Applicant;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.FL401Respondent;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.FL401SolicitorDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Fl401OtherProceedingsDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Fl401TypeOfApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.HearingUrgency;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.HomeChild;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.HomeDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.InternationalElement;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.LitigationCapacity;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Miam;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.MiamExemptions;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Order;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.OtherPersonInTheCase;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.OtherProceedingsDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.RelationshipToRespondent;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Respondent;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.RespondentBehaviourTable;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.TypeOfApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.WelshLanguageRequirements;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.WithoutNoticeOrder;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm.AllegationsOfHarmOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm.AllegationsOfHarmOtherConcerns;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm.AllegationsOfHarmOverview;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm.ChildAbductionDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm.DomesticAbuseVictim;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;
import uk.gov.hmcts.reform.prl.services.tab.TabService;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.FieldGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CHILD_AND_CAFCASS_OFFICER_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CHILD_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.THIS_INFORMATION_IS_CONFIDENTIAL;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationsTabService implements TabService {

    @Autowired
    CoreCaseDataService coreCaseDataService;

    @Autowired
    ObjectMapper objectMapper;


    @Override
    public Map<String, Object> updateTab(CaseData caseData) {

        Map<String, Object> applicationTab = new HashMap<>();
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            applicationTab.put("hearingUrgencyTable", getHearingUrgencyTable(caseData));
            log.info("*** Respondents before table {}", caseData.getRespondents());
            applicationTab.put("applicantTable", getApplicantsTable(caseData));
            applicationTab.put("respondentTable", getRespondentsTable(caseData));
            log.info("*** Respondents after table {}", caseData.getRespondents());
            applicationTab.put("declarationTable", getDeclarationTable(caseData));
            applicationTab.put("typeOfApplicationTable", getTypeOfApplicationTable(caseData));
            applicationTab.put("allegationsOfHarmOverviewTable", getAllegationsOfHarmOverviewTable(caseData));
            applicationTab.put("miamTable", getMiamTable(caseData));
            applicationTab.put("miamExemptionsTable", getMiamExemptionsTable(caseData));
            applicationTab.put("otherProceedingsTable", getOtherProceedingsTable(caseData));
            applicationTab.put("otherProceedingsDetailsTable", getOtherProceedingsDetailsTable(caseData));
            applicationTab.put("internationalElementTable", getInternationalElementTable(caseData));
            applicationTab.put("attendingTheHearingTable", getAttendingTheHearingTable(caseData));
            applicationTab.put("litigationCapacityTable", getLitigationCapacityDetails(caseData));
            applicationTab.put("welshLanguageRequirementsTable", getWelshLanguageRequirementsTable(caseData));
            applicationTab.put("otherPeopleInTheCaseTable", getOtherPeopleInTheCaseTable(caseData));
            applicationTab.put("allegationsOfHarmOrdersTable", getAllegationsOfHarmOrdersTable(caseData));
            applicationTab.put("allegationsOfHarmDomesticAbuseTable", getDomesticAbuseTable(caseData));
            applicationTab.put("allegationsOfHarmChildAbductionTable", getChildAbductionTable(caseData));
            applicationTab.put("allegationsOfHarmOtherConcernsTable", getAllegationsOfHarmOtherConcerns(caseData));
            applicationTab.put("childDetailsTable", getChildDetails(caseData));
            applicationTab.put("childDetailsExtraTable", getExtraChildDetailsTable(caseData));
            applicationTab.put(CHILD_AND_CAFCASS_OFFICER_DETAILS, prePopulateChildAndCafcassOfficerDetails(caseData));
        } else if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            applicationTab.put("fl401TypeOfApplicationTable", getFL401TypeOfApplicationTable(caseData));
            applicationTab.put("withoutNoticeOrderTable", getWithoutNoticeOrder(caseData));
            applicationTab.put("fl401ApplicantTable", getFl401ApplicantsTable(caseData));
            applicationTab.put("fl401SolicitorDetailsTable", getFl401ApplicantsSolictorDetailsTable(caseData));
            applicationTab.put("fl401RespondentTable", getFl401RespondentTable(caseData));
            Map<String,Object> applicantFamilyMap = getApplicantsFamilyDetails(caseData);
            applicationTab.put("applicantFamilyTable", applicantFamilyMap);
            if (("Yes").equals(applicantFamilyMap.get("doesApplicantHaveChildren"))) {
                applicationTab.put("fl401ChildDetailsTable", applicantFamilyMap.get("applicantChild"));
            }
            applicationTab.put("respondentBehaviourTable", getFl401RespondentBehaviourTable(caseData));
            applicationTab.put("relationshipToRespondentTable", getFl401RelationshipToRespondentTable(caseData));
            Map<String, Object> homeDetails = getHomeDetails(caseData);
            applicationTab.put("homeDetailsTable", homeDetails);
            applicationTab.put("isHomeEntered", !homeDetails.isEmpty() ? "true" : "false");

            applicationTab.put("otherProceedingsTable", getFL401OtherProceedingsTable(caseData));
            applicationTab.put("fl401OtherProceedingsDetailsTable", getFl401OtherProceedingsDetailsTable(caseData));
            applicationTab.put("internationalElementTable", getInternationalElementTable(caseData));
            applicationTab.put("attendingTheHearingTable", getAttendingTheHearingTable(caseData));
            applicationTab.put("welshLanguageRequirementsTable", getWelshLanguageRequirementsTable(caseData));
            applicationTab.put("declarationTable", getDeclarationTable(caseData));
        }
        return applicationTab;
    }

    @Override
    public List<FieldGenerator> getGenerators(CaseData caseData) {
        return Collections.emptyList();
    }

    @Override
    public void calEventToRefreshUI() {
        // no current implementation required.
    }

    public List<Element<ChildDetails>> getChildDetails(CaseData caseData) {

        Optional<List<Element<Child>>> childElementsCheck = ofNullable(caseData.getChildren());
        List<Element<ChildDetails>> childFinalList = new ArrayList<>();
        if (childElementsCheck.isEmpty()) {
            ChildDetails child = ChildDetails.builder().build();
            Element<ChildDetails> app = Element.<ChildDetails>builder().value(child).build();
            childFinalList.add(app);
            return childFinalList;
        }
        List<Child> childList = caseData.getChildren().stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
        for (Child child : childList) {
            ChildDetails c = mapChildDetails(child);
            Element<ChildDetails> res = Element.<ChildDetails>builder().value(c).build();
            childFinalList.add(res);
        }
        return childFinalList;
    }

    private ChildDetails getChildDetails(Child child, List<Element<OtherPersonWhoLivesWithChildDetails>> otherPersonLiving) {
        Optional<RelationshipsEnum> applicantsRelationshipToChild =
                ofNullable(child.getApplicantsRelationshipToChild());
        Optional<RelationshipsEnum> respondentsRelationshipToChild =
                ofNullable(child.getRespondentsRelationshipToChild());
        Optional<List<LiveWithEnum>> childLivesWith = ofNullable(child.getChildLiveWith());
        Optional<List<OrderTypeEnum>> orderAppliedFor = ofNullable(child.getOrderAppliedFor());

        return ChildDetails.builder().firstName(child.getFirstName())
                .lastName(child.getLastName())
                .dateOfBirth(child.getDateOfBirth())
                .gender(child.getGender())
                .otherGender(child.getOtherGender())
                .applicantsRelationshipToChild(applicantsRelationshipToChild.isEmpty()
                        ? null : child.getApplicantsRelationshipToChild().getDisplayedValue())
                .otherApplicantsRelationshipToChild(child.getOtherApplicantsRelationshipToChild())
                .respondentsRelationshipToChild(respondentsRelationshipToChild.isEmpty()
                        ? null : child.getRespondentsRelationshipToChild().getDisplayedValue())
                .otherRespondentsRelationshipToChild(child.getOtherRespondentsRelationshipToChild())
                .personWhoLivesWithChild(otherPersonLiving)
                .childLiveWith(childLivesWith.isEmpty() ? null : child.getChildLiveWith().stream()
                        .map(LiveWithEnum::getDisplayedValue).collect(
                                Collectors.joining(", ")))
                .orderAppliedFor(orderAppliedFor.isEmpty() ? null : child.getOrderAppliedFor().stream()
                        .map(OrderTypeEnum::getDisplayedValue).collect(
                                Collectors.joining(", ")))
                .parentalResponsibilityDetails(child.getParentalResponsibilityDetails())
                .cafcassOfficerAdded(!StringUtils.isBlank(child.getCafcassOfficerName()) ? YesOrNo.Yes : YesOrNo.No)
                .cafcassOfficerName(child.getCafcassOfficerName())
                .cafcassOfficerEmailAddress(child.getCafcassOfficerEmailAddress())
                .cafcassOfficerPhoneNo(child.getCafcassOfficerPhoneNo())
                .build();
    }

    private ChildDetails mapChildDetails(Child child) {

        List<Element<OtherPersonWhoLivesWithChildDetails>> otherPersonLiving = new ArrayList<>();

        if (nonNull(child.getPersonWhoLivesWithChild())) {
            List<OtherPersonWhoLivesWithChild> otherPersonList = child.getPersonWhoLivesWithChild().stream()
                    .map(Element::getValue)
                    .collect(Collectors.toList());

            for (OtherPersonWhoLivesWithChild otherPersonWhoLivesWithChild : otherPersonList) {
                otherPersonLiving.add(getOtherPersonWhoLivesWithChildDetails(otherPersonWhoLivesWithChild));
            }
        }
        return getChildDetails(child, otherPersonLiving);
    }

    private Element<OtherPersonWhoLivesWithChildDetails> getOtherPersonWhoLivesWithChildDetails(
            OtherPersonWhoLivesWithChild otherPersonWhoLivesWithChild) {
        return Element.<OtherPersonWhoLivesWithChildDetails>builder()
                .value(OtherPersonWhoLivesWithChildDetails.builder()
                        .firstName((YesOrNo.Yes).equals(otherPersonWhoLivesWithChild
                                .getIsPersonIdentityConfidential()) ? THIS_INFORMATION_IS_CONFIDENTIAL
                                : otherPersonWhoLivesWithChild.getFirstName())
                        .lastName((YesOrNo.Yes).equals(otherPersonWhoLivesWithChild
                                .getIsPersonIdentityConfidential()) ? THIS_INFORMATION_IS_CONFIDENTIAL :
                                otherPersonWhoLivesWithChild.getLastName())
                        .relationshipToChildDetails((YesOrNo.Yes).equals(otherPersonWhoLivesWithChild
                                .getIsPersonIdentityConfidential()) ? THIS_INFORMATION_IS_CONFIDENTIAL :
                                otherPersonWhoLivesWithChild.getRelationshipToChildDetails())
                        .isPersonIdentityConfidential(otherPersonWhoLivesWithChild.getIsPersonIdentityConfidential())
                        .address((YesOrNo.Yes).equals(otherPersonWhoLivesWithChild
                                .getIsPersonIdentityConfidential())
                                ? Address.builder().addressLine1(THIS_INFORMATION_IS_CONFIDENTIAL).build()
                                : otherPersonWhoLivesWithChild.getAddress()).build()).build();
    }



    public Map<String, Object> toMap(Object object) {
        return objectMapper.convertValue(object, Map.class);
    }

    public List<Element<Applicant>> getApplicantsTable(CaseData caseData) {
        List<Element<Applicant>> applicants = new ArrayList<>();
        Optional<List<Element<PartyDetails>>> checkApplicants = ofNullable(caseData.getApplicants());
        if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            Element<PartyDetails> fl401Applicant = Element.<PartyDetails>builder().value(caseData.getApplicantsFL401()).build();
            checkApplicants = Optional.of(List.of(fl401Applicant));
        }

        if (checkApplicants.isEmpty()) {
            Applicant a = Applicant.builder().build();
            Element<Applicant> app = Element.<Applicant>builder().value(a).build();
            applicants.add(app);
            return applicants;
        }
        List<PartyDetails> currentApplicants = caseData.getApplicants().stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
        currentApplicants = maskConfidentialDetails(currentApplicants);
        for (PartyDetails applicant : currentApplicants) {
            Applicant a = objectMapper.convertValue(applicant, Applicant.class);
            Element<Applicant> app = Element.<Applicant>builder().value(a).build();
            applicants.add(app);
        }
        return applicants;
    }

    public List<PartyDetails> maskConfidentialDetails(List<PartyDetails> currentApplicants) {
        for (PartyDetails applicantDetails : currentApplicants) {
            if ((YesOrNo.Yes).equals(applicantDetails.getIsPhoneNumberConfidential())) {
                applicantDetails.setPhoneNumber(THIS_INFORMATION_IS_CONFIDENTIAL);
            }
            if ((YesOrNo.Yes).equals(applicantDetails.getIsEmailAddressConfidential())) {
                applicantDetails.setEmail(THIS_INFORMATION_IS_CONFIDENTIAL);
            }
            if ((YesOrNo.Yes).equals(applicantDetails.getIsAddressConfidential())) {
                applicantDetails.setAddress(Address.builder().addressLine1(THIS_INFORMATION_IS_CONFIDENTIAL).build());
            }
        }
        return currentApplicants;
    }

    public PartyDetails maskFl401ConfidentialDetails(PartyDetails applicantDetails) {
        if ((YesOrNo.Yes).equals(applicantDetails.getIsPhoneNumberConfidential())) {
            applicantDetails.setPhoneNumber(THIS_INFORMATION_IS_CONFIDENTIAL);
        }
        if ((YesOrNo.Yes).equals(applicantDetails.getIsEmailAddressConfidential())) {
            applicantDetails.setEmail(THIS_INFORMATION_IS_CONFIDENTIAL);
        }
        if ((YesOrNo.Yes).equals(applicantDetails.getIsAddressConfidential())) {
            applicantDetails.setAddress(Address.builder().addressLine1(THIS_INFORMATION_IS_CONFIDENTIAL).build());
        }
        return applicantDetails;
    }

    public List<Element<Respondent>> getRespondentsTable(CaseData caseData) {
        List<Element<Respondent>> respondents = new ArrayList<>();
        Optional<List<Element<PartyDetails>>> checkRespondents = ofNullable(caseData.getRespondents());
        if (checkRespondents.isEmpty()) {
            Respondent r = Respondent.builder().build();
            Element<Respondent> app = Element.<Respondent>builder().value(r).build();
            respondents.add(app);
            return respondents;
        }
        List<PartyDetails> currentRespondents = caseData.getRespondents().stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
        currentRespondents = maskConfidentialDetails(currentRespondents);
        for (PartyDetails respondent : currentRespondents) {
            Respondent r = objectMapper.convertValue(respondent, Respondent.class);
            Element<Respondent> res = Element.<Respondent>builder().value(r).build();
            respondents.add(res);
        }
        return respondents;
    }

    public Map<String, Object> getDeclarationTable(CaseData caseData) {
        Map<String, Object> declarationMap = new HashMap<>();
        String solicitor = caseData.getSolicitorName();
        String statementOfTruthPlaceHolder = null;

        if (nonNull(solicitor)) {
            statementOfTruthPlaceHolder = solicitor;
        } else if (isNotEmpty(caseData.getUserInfo())) {
            UserInfo userInfo = caseData.getUserInfo().get(0).getValue();
            statementOfTruthPlaceHolder = userInfo.getFirstName() + " " + userInfo.getLastName();
        }

        String declarationText = "I understand that proceedings for contempt of court may be brought"
            + " against anyone who makes, or causes to be made, a false statement in a document verified"
            + " by a statement of truth without an honest belief in its truth. The applicant believes "
            + "that the facts stated in this form and any continuation sheets are true. " + statementOfTruthPlaceHolder
            + " is authorised by the applicant to sign this statement.";

        declarationMap.put("declarationText", declarationText);
        declarationMap.put("agreedBy", statementOfTruthPlaceHolder);
        return declarationMap;
    }

    public Map<String, Object> getHearingUrgencyTable(CaseData caseData) {
        HearingUrgency hearingUrgency = objectMapper.convertValue(caseData, HearingUrgency.class);
        return toMap(hearingUrgency);
    }

    public Map<String, Object> getTypeOfApplicationTable(CaseData caseData) {
        Optional<List<OrderTypeEnum>> checkOrders = ofNullable(caseData.getOrdersApplyingFor());
        if (checkOrders.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> ordersApplyingFor = caseData.getOrdersApplyingFor().stream()
            .map(OrderTypeEnum::getDisplayedValue)
            .collect(Collectors.toList());

        String typeOfChildArrangementsOrder = "";
        Optional<ChildArrangementOrderTypeEnum> childArrangementCheck = ofNullable(caseData.getTypeOfChildArrangementsOrder());
        if (childArrangementCheck.isPresent()) {
            typeOfChildArrangementsOrder = caseData.getTypeOfChildArrangementsOrder().getDisplayedValue();
        }
        String natureOfOrder = caseData.getNatureOfOrder();

        TypeOfApplication typeOfApplication = TypeOfApplication.builder()
            .ordersApplyingFor(String.join(", ", ordersApplyingFor))
            .typeOfChildArrangementsOrder(typeOfChildArrangementsOrder)
            .applicationPermissionRequired(nonNull(caseData.getApplicationPermissionRequired())
                    ? caseData.getApplicationPermissionRequired().getDisplayedValue() : null)
            .applicationPermissionRequiredReason(caseData.getApplicationPermissionRequiredReason())
            .natureOfOrder(natureOfOrder)
            .build();

        return toMap(typeOfApplication);
    }

    public Map<String, Object> getAllegationsOfHarmOverviewTable(CaseData caseData) {
        AllegationsOfHarmOverview allegationsOfHarmOverview = objectMapper
            .convertValue(caseData, AllegationsOfHarmOverview.class);
        return toMap(allegationsOfHarmOverview);

    }

    public Map<String, Object> getMiamTable(CaseData caseData) {
        Miam miam = objectMapper.convertValue(caseData, Miam.class);
        return toMap(miam);
    }

    public Map<String, Object> getMiamExemptionsTable(CaseData caseData) {
        Optional<List<MiamExemptionsChecklistEnum>> miamExemptionsCheck = ofNullable(caseData.getMiamDetails().getMiamExemptionsChecklist());
        String reasonsForMiamExemption;
        if (miamExemptionsCheck.isPresent()) {
            reasonsForMiamExemption = caseData.getMiamDetails().getMiamExemptionsChecklist()
                .stream().map(MiamExemptionsChecklistEnum::getDisplayedValue).collect(Collectors.joining(", "));
        } else {
            reasonsForMiamExemption = "";
        }

        String domesticViolenceEvidence;
        Optional<List<MiamDomesticViolenceChecklistEnum>> domesticViolenceCheck
            = ofNullable(caseData.getMiamDetails()
                             .getMiamDomesticViolenceChecklist());
        if (domesticViolenceCheck.isPresent()) {
            domesticViolenceEvidence = caseData.getMiamDetails().getMiamDomesticViolenceChecklist()
                .stream().map(MiamDomesticViolenceChecklistEnum::getDisplayedValue)
                .collect(Collectors.joining("\n"));
        } else {
            domesticViolenceEvidence = "";
        }

        String urgencyEvidence;
        Optional<List<MiamUrgencyReasonChecklistEnum>> urgencyCheck =
            ofNullable(caseData.getMiamDetails()
                           .getMiamUrgencyReasonChecklist());
        if (urgencyCheck.isPresent()) {
            urgencyEvidence = caseData.getMiamDetails().getMiamUrgencyReasonChecklist()
                .stream().map(MiamUrgencyReasonChecklistEnum::getDisplayedValue)
                .collect(Collectors.joining("\n"));
        } else {
            urgencyEvidence = "";
        }

        String previousAttendenceEvidence;
        Optional<MiamPreviousAttendanceChecklistEnum> prevCheck = ofNullable(caseData.getMiamDetails().getMiamPreviousAttendanceChecklist());
        if (prevCheck.isPresent()) {
            previousAttendenceEvidence = caseData.getMiamDetails().getMiamPreviousAttendanceChecklist().getDisplayedValue();
        } else {
            previousAttendenceEvidence = "";
        }

        String otherGroundsEvidence;
        Optional<MiamOtherGroundsChecklistEnum> othCheck = ofNullable(caseData.getMiamDetails().getMiamOtherGroundsChecklist());
        if (othCheck.isPresent()) {
            otherGroundsEvidence = caseData.getMiamDetails().getMiamOtherGroundsChecklist().getDisplayedValue();
        } else {
            otherGroundsEvidence = "";
        }

        String childEvidence;
        Optional<List<MiamChildProtectionConcernChecklistEnum>> childCheck = ofNullable(caseData
                                                                                            .getMiamDetails().getMiamChildProtectionConcernList());
        if (childCheck.isPresent()) {
            childEvidence = caseData.getMiamDetails().getMiamChildProtectionConcernList()
                .stream().map(MiamChildProtectionConcernChecklistEnum::getDisplayedValue)
                .collect(Collectors.joining("\n"));
        } else {
            childEvidence = "";
        }

        MiamExemptions miamExemptions = MiamExemptions.builder()
            .reasonsForMiamExemption(reasonsForMiamExemption)
            .domesticViolenceEvidence(domesticViolenceEvidence)
            .childProtectionEvidence(childEvidence)
            .urgencyEvidence(urgencyEvidence)
            .previousAttendenceEvidence(previousAttendenceEvidence)
            .otherGroundsEvidence(otherGroundsEvidence)
            .build();

        return toMap(miamExemptions);

    }

    public Map<String, Object> getOtherProceedingsTable(CaseData caseData) {
        Optional<YesNoDontKnow> proceedingCheck = ofNullable(caseData.getPreviousOrOngoingProceedingsForChildren());
        if (proceedingCheck.isPresent()) {
            return Collections.singletonMap(
                PrlAppsConstants.PREVIOUS_OR_ONGOING_PROCEEDINGS,
                caseData.getPreviousOrOngoingProceedingsForChildren().getDisplayedValue()
            );
        }
        return Collections.singletonMap(PrlAppsConstants.PREVIOUS_OR_ONGOING_PROCEEDINGS, "");
    }

    public Map<String, Object> getFL401OtherProceedingsTable(CaseData caseData) {
        if (caseData.getFl401OtherProceedingDetails() != null) {
            Optional<YesNoDontKnow> proceedingCheck = ofNullable(caseData.getFl401OtherProceedingDetails().getHasPrevOrOngoingOtherProceeding());
            if (proceedingCheck.isPresent()) {
                return Collections.singletonMap(
                    PrlAppsConstants.PREVIOUS_OR_ONGOING_PROCEEDINGS,
                    caseData.getFl401OtherProceedingDetails().getHasPrevOrOngoingOtherProceeding().getDisplayedValue()
                );
            }
        }

        return Collections.singletonMap(PrlAppsConstants.PREVIOUS_OR_ONGOING_PROCEEDINGS, "");
    }

    public List<Element<OtherProceedingsDetails>> getOtherProceedingsDetailsTable(CaseData caseData) {
        Optional<YesNoDontKnow> proceedingCheck = ofNullable(caseData.getPreviousOrOngoingProceedingsForChildren());
        Optional<List<Element<ProceedingDetails>>> proceedingsCheck = ofNullable(caseData.getExistingProceedings());

        if (proceedingsCheck.isEmpty() || (proceedingCheck.isPresent() && !proceedingCheck.get().equals(YesNoDontKnow.yes))) {
            OtherProceedingsDetails op = OtherProceedingsDetails.builder().build();
            Element<OtherProceedingsDetails> other = Element.<OtherProceedingsDetails>builder().value(op).build();
            return Collections.singletonList(other);
        }
        List<ProceedingDetails> proceedings = caseData.getExistingProceedings().stream()
            .map(Element::getValue).collect(Collectors.toList());
        List<Element<OtherProceedingsDetails>> otherProceedingsDetailsList = new ArrayList<>();

        for (ProceedingDetails p : proceedings) {
            String ordersMade = p.getTypeOfOrder().stream().map(TypeOfOrderEnum::getDisplayedValue)
                .collect(Collectors.joining(", "));

            OtherProceedingsDetails otherProceedingsDetails = OtherProceedingsDetails.builder()
                .previousOrOngoingProceedings(p.getPreviousOrOngoingProceedings().getDisplayedValue())
                .caseNumber(p.getCaseNumber())
                .dateStarted(p.getDateStarted())
                .dateEnded(p.getDateEnded())
                .typeOfOrder(ordersMade)
                .otherTypeOfOrder(p.getOtherTypeOfOrder())
                .nameOfJudge(p.getNameOfJudge())
                .nameOfCourt(p.getNameOfCourt())
                .nameOfChildrenInvolved(p.getNameOfChildrenInvolved())
                .nameOfGuardian(p.getNameOfGuardian())
                .nameAndOffice(p.getNameAndOffice())
                .build();

            Element<OtherProceedingsDetails> details = Element.<OtherProceedingsDetails>builder()
                .value(otherProceedingsDetails).build();
            otherProceedingsDetailsList.add(details);
        }
        return otherProceedingsDetailsList;
    }

    public List<Element<Fl401OtherProceedingsDetails>> getFl401OtherProceedingsDetailsTable(CaseData caseData) {
        if (caseData.getFl401OtherProceedingDetails() == null) {
            return getEmptyFl401OtherProceedings();
        }
        Optional<YesNoDontKnow> proceedingCheck = ofNullable(caseData.getFl401OtherProceedingDetails().getHasPrevOrOngoingOtherProceeding());
        Optional<List<Element<FL401Proceedings>>> proceedingsCheck = ofNullable(caseData.getFl401OtherProceedingDetails().getFl401OtherProceedings());

        if (proceedingsCheck.isEmpty() || (proceedingCheck.isPresent() && !proceedingCheck.get().equals(YesNoDontKnow.yes))) {
            return getEmptyFl401OtherProceedings();
        }
        List<FL401Proceedings> proceedings = caseData.getFl401OtherProceedingDetails().getFl401OtherProceedings().stream()
            .map(Element::getValue).collect(Collectors.toList());
        List<Element<Fl401OtherProceedingsDetails>> otherProceedingsDetailsList = new ArrayList<>();

        for (FL401Proceedings p : proceedings) {
            Fl401OtherProceedingsDetails otherProceedingsDetails = Fl401OtherProceedingsDetails.builder()
                .caseNumber(p.getCaseNumber())
                .anyOtherDetails(p.getAnyOtherDetails())
                .typeOfCase(p.getTypeOfCase())
                .nameOfCourt(p.getNameOfCourt())
                .build();

            Element<Fl401OtherProceedingsDetails> details = Element.<Fl401OtherProceedingsDetails>builder()
                .value(otherProceedingsDetails).build();
            otherProceedingsDetailsList.add(details);
        }
        return otherProceedingsDetailsList;
    }

    private List<Element<Fl401OtherProceedingsDetails>> getEmptyFl401OtherProceedings() {
        Fl401OtherProceedingsDetails op = Fl401OtherProceedingsDetails.builder().build();
        Element<Fl401OtherProceedingsDetails> other = Element.<Fl401OtherProceedingsDetails>builder().value(op).build();
        return Collections.singletonList(other);
    }

    public Map<String, Object> getInternationalElementTable(CaseData caseData) {
        InternationalElement internationalElement = objectMapper.convertValue(caseData, InternationalElement.class);
        return toMap(internationalElement);
    }

    public Map<String, Object> getAttendingTheHearingTable(CaseData caseData) {
        AttendingTheHearing attendingTheHearing = objectMapper.convertValue(caseData, AttendingTheHearing.class);
        return toMap(attendingTheHearing);
    }

    public Map<String, Object> getLitigationCapacityDetails(CaseData caseData) {
        LitigationCapacity litigationCapacity = objectMapper.convertValue(caseData, LitigationCapacity.class);
        return toMap(litigationCapacity);
    }

    public Map<String, Object> getWelshLanguageRequirementsTable(CaseData caseData) {
        WelshLanguageRequirements welshLanguageRequirements = objectMapper
            .convertValue(caseData, WelshLanguageRequirements.class);
        return toMap(welshLanguageRequirements);
    }

    public Map<String, Object> getAllegationsOfHarmOrdersTable(CaseData caseData) {
        AllegationsOfHarmOrders allegationsOfHarmOrders = objectMapper
            .convertValue(caseData, AllegationsOfHarmOrders.class);
        getSpecificOrderDetails(allegationsOfHarmOrders, caseData);
        return toMap(allegationsOfHarmOrders);
    }

    public AllegationsOfHarmOrders getSpecificOrderDetails(AllegationsOfHarmOrders allegationsOfHarmOrders, CaseData caseData) {

        Optional<YesOrNo> nonMolYesNo = ofNullable(allegationsOfHarmOrders.getOrdersNonMolestation());
        if (nonMolYesNo.isPresent() && nonMolYesNo.get().equals(YesOrNo.Yes)) {
            Order nonMolOrder = Order.builder()
                .dateIssued(caseData.getAllegationOfHarm().getOrdersNonMolestationDateIssued())
                .endDate(caseData.getAllegationOfHarm().getOrdersNonMolestationEndDate())
                .orderCurrent(caseData.getAllegationOfHarm().getOrdersNonMolestationCurrent())
                .courtName(caseData.getAllegationOfHarm().getOrdersNonMolestationCourtName())
                .build();
            allegationsOfHarmOrders.setNonMolestationOrder(nonMolOrder);
        }

        Optional<YesOrNo> occYesNo = ofNullable(allegationsOfHarmOrders.getOrdersOccupation());
        if (occYesNo.isPresent() && occYesNo.get().equals(YesOrNo.Yes)) {
            Order occOrder = Order.builder()
                .dateIssued(caseData.getAllegationOfHarm().getOrdersOccupationDateIssued())
                .endDate(caseData.getAllegationOfHarm().getOrdersOccupationEndDate())
                .orderCurrent(caseData.getAllegationOfHarm().getOrdersOccupationCurrent())
                .courtName(caseData.getAllegationOfHarm().getOrdersOccupationCourtName())
                .build();
            allegationsOfHarmOrders.setOccupationOrder(occOrder);
        }

        Optional<YesOrNo> forcedYesNo = ofNullable(allegationsOfHarmOrders.getOrdersForcedMarriageProtection());
        if (forcedYesNo.isPresent() && forcedYesNo.get().equals(YesOrNo.Yes)) {
            Order forOrder = Order.builder()
                .dateIssued(caseData.getAllegationOfHarm().getOrdersForcedMarriageProtectionDateIssued())
                .endDate(caseData.getAllegationOfHarm().getOrdersForcedMarriageProtectionEndDate())
                .orderCurrent(caseData.getAllegationOfHarm().getOrdersForcedMarriageProtectionCurrent())
                .courtName(caseData.getAllegationOfHarm().getOrdersForcedMarriageProtectionCourtName())
                .build();
            allegationsOfHarmOrders.setForcedMarriageProtectionOrder(forOrder);
        }

        Optional<YesOrNo> resYesNo = ofNullable(allegationsOfHarmOrders.getOrdersRestraining());
        if (resYesNo.isPresent() && resYesNo.get().equals(YesOrNo.Yes)) {
            Order resOrder = Order.builder()
                .dateIssued(caseData.getAllegationOfHarm().getOrdersRestrainingDateIssued())
                .endDate(caseData.getAllegationOfHarm().getOrdersRestrainingEndDate())
                .orderCurrent(caseData.getAllegationOfHarm().getOrdersRestrainingCurrent())
                .courtName(caseData.getAllegationOfHarm().getOrdersRestrainingCourtName())
                .build();
            allegationsOfHarmOrders.setRestrainingOrder(resOrder);
        }

        Optional<YesOrNo> othYesNo = ofNullable(allegationsOfHarmOrders.getOrdersOtherInjunctive());
        if (othYesNo.isPresent() && othYesNo.get().equals(YesOrNo.Yes)) {
            Order othOrder = Order.builder()
                .dateIssued(caseData.getAllegationOfHarm().getOrdersOtherInjunctiveDateIssued())
                .endDate(caseData.getAllegationOfHarm().getOrdersOtherInjunctiveEndDate())
                .orderCurrent(caseData.getAllegationOfHarm().getOrdersOtherInjunctiveCurrent())
                .courtName(caseData.getAllegationOfHarm().getOrdersOtherInjunctiveCourtName())
                .build();
            allegationsOfHarmOrders.setOtherInjunctiveOrder(othOrder);
        }

        Optional<YesOrNo> undYesNo = ofNullable(allegationsOfHarmOrders.getOrdersUndertakingInPlace());
        if (undYesNo.isPresent() && undYesNo.get().equals(YesOrNo.Yes)) {
            Order undOrder = Order.builder()
                .dateIssued(caseData.getAllegationOfHarm().getOrdersUndertakingInPlaceDateIssued())
                .endDate(caseData.getAllegationOfHarm().getOrdersUndertakingInPlaceEndDate())
                .orderCurrent(caseData.getAllegationOfHarm().getOrdersUndertakingInPlaceCurrent())
                .courtName(caseData.getAllegationOfHarm().getOrdersUndertakingInPlaceCourtName())
                .build();
            allegationsOfHarmOrders.setUndertakingInPlaceOrder(undOrder);
        }

        return allegationsOfHarmOrders;
    }

    public Map<String, Object> getDomesticAbuseTable(CaseData caseData) {

        Optional<List<ApplicantOrChildren>> physVictm = ofNullable(caseData.getAllegationOfHarm().getPhysicalAbuseVictim());
        String physVictimString = "";
        if (physVictm.isPresent()) {
            physVictimString = caseData.getAllegationOfHarm().getPhysicalAbuseVictim().stream()
                .map(ApplicantOrChildren::getDisplayedValue)
                .collect(Collectors.joining(", "));
        }

        Optional<List<ApplicantOrChildren>> emoVictim = ofNullable(caseData.getAllegationOfHarm().getEmotionalAbuseVictim());
        String emoVictimString = "";
        if (emoVictim.isPresent()) {
            emoVictimString = caseData.getAllegationOfHarm().getEmotionalAbuseVictim().stream()
                .map(ApplicantOrChildren::getDisplayedValue)
                .collect(Collectors.joining(", "));
        }

        Optional<List<ApplicantOrChildren>> psyVictim = ofNullable(caseData.getAllegationOfHarm().getPsychologicalAbuseVictim());
        String psyVictimString = "";
        if (psyVictim.isPresent()) {
            psyVictimString = caseData.getAllegationOfHarm().getPhysicalAbuseVictim().stream()
                .map(ApplicantOrChildren::getDisplayedValue)
                .collect(Collectors.joining(", "));
        }

        Optional<List<ApplicantOrChildren>> sexVictim = ofNullable(caseData.getAllegationOfHarm().getSexualAbuseVictim());
        String sexVictimString = "";
        if (sexVictim.isPresent()) {
            sexVictimString = caseData.getAllegationOfHarm().getSexualAbuseVictim().stream()
                .map(ApplicantOrChildren::getDisplayedValue)
                .collect(Collectors.joining(", "));
        }

        Optional<List<ApplicantOrChildren>> finVictim = ofNullable(caseData.getAllegationOfHarm().getFinancialAbuseVictim());
        String finVictimString = "";
        if (finVictim.isPresent()) {
            finVictimString = caseData.getAllegationOfHarm().getPhysicalAbuseVictim().stream()
                .map(ApplicantOrChildren::getDisplayedValue)
                .collect(Collectors.joining(", "));
        }

        DomesticAbuseVictim domesticAbuseVictim = DomesticAbuseVictim.builder()
            .physicalAbuseVictim(physVictimString)
            .emotionalAbuseVictim(emoVictimString)
            .psychologicalAbuseVictim(psyVictimString)
            .sexualAbuseVictim(sexVictimString)
            .financialAbuseVictim(finVictimString)
            .build();

        return toMap(domesticAbuseVictim);

    }

    public Map<String, Object> getChildAbductionTable(CaseData caseData) {
        ChildAbductionDetails childAbductionDetails = objectMapper.convertValue(caseData, ChildAbductionDetails.class);
        return toMap(childAbductionDetails);
    }


    public Map<String, Object> getAllegationsOfHarmOtherConcerns(CaseData caseData) {
        AllegationsOfHarmOtherConcerns allegationsOfHarmOtherConcerns = objectMapper
            .convertValue(caseData, AllegationsOfHarmOtherConcerns.class);
        return toMap(allegationsOfHarmOtherConcerns);
    }

    public List<Element<OtherPersonInTheCase>> getOtherPeopleInTheCaseTable(CaseData caseData) {
        Optional<List<Element<PartyDetails>>> otherPeopleCheck = ofNullable(caseData.getOthersToNotify());
        List<Element<OtherPersonInTheCase>> otherPersonsInTheCase = new ArrayList<>();

        if (otherPeopleCheck.isEmpty() || otherPeopleCheck.get().isEmpty()) {
            OtherPersonInTheCase op = OtherPersonInTheCase.builder().build();
            Element<OtherPersonInTheCase> other = Element.<OtherPersonInTheCase>builder().value(op).build();
            otherPersonsInTheCase.add(other);
            return otherPersonsInTheCase;
        }

        List<PartyDetails> otherPeople = caseData.getOthersToNotify().stream().map(Element::getValue).collect(Collectors.toList());

        for (PartyDetails p : otherPeople) {
            OtherPersonInTheCase other = objectMapper.convertValue(p, OtherPersonInTheCase.class);
            //field below is not mapping correctly with the object mapper
            other.setRelationshipToChild(p.getOtherPersonRelationshipToChildren());
            Element<OtherPersonInTheCase> wrappedPerson = Element.<OtherPersonInTheCase>builder()
                .value(other).build();
            otherPersonsInTheCase.add(wrappedPerson);
        }
        return otherPersonsInTheCase;
    }

    public Map<String, Object> getExtraChildDetailsTable(CaseData caseData) {

        Map<String, Object> childExtraDetails = new HashMap<>();
        Optional<YesNoDontKnow> childrenKnownToLocalAuthority = ofNullable(caseData.getChildrenKnownToLocalAuthority());
        childrenKnownToLocalAuthority.ifPresent(yesNoDontKnow -> childExtraDetails.put(
            "childrenKnownToLocalAuthority",
            yesNoDontKnow.getDisplayedValue()
        ));
        Optional<String> childrenKnownToLocalAuthorityTextArea = ofNullable(caseData.getChildrenKnownToLocalAuthorityTextArea());
        childrenKnownToLocalAuthorityTextArea.ifPresent(s -> childExtraDetails.put(
            "childrenKnownToLocalAuthorityTextArea",
            s
        ));
        Optional<YesNoDontKnow> childrenSubjectOfChildProtectionPlan = ofNullable(caseData.getChildrenSubjectOfChildProtectionPlan());
        childrenSubjectOfChildProtectionPlan.ifPresent(yesNoDontKnow -> childExtraDetails.put(
            "childrenSubjectOfChildProtectionPlan",
            yesNoDontKnow.getDisplayedValue()
        ));
        return childExtraDetails;
    }

    // FL401 Related Events
    /**
     * *
     * FL401 Type of Application.
     */

    public Map<String, Object> getFL401TypeOfApplicationTable(CaseData caseData) {
        if (caseData.getTypeOfApplicationOrders() != null) {
            List<String> ordersApplyingFor = caseData.getTypeOfApplicationOrders().getOrderType().stream()
                .map(FL401OrderTypeEnum::getDisplayedValue)
                .collect(Collectors.toList());

            Fl401TypeOfApplication.Fl401TypeOfApplicationBuilder builder = Fl401TypeOfApplication.builder()
                .ordersApplyingFor(String.join(", ", ordersApplyingFor));

            LinkToCA linkToCA = caseData.getTypeOfApplicationLinkToCA();
            if (linkToCA != null) {
                builder
                    .isLinkedToChildArrangementApplication(linkToCA.getLinkToCaApplication())
                    .caCaseNumber(linkToCA.getCaApplicationNumber());
            }
            return toMap(builder.build());
        }
        return Collections.emptyMap();
    }

    public Map<String, Object> getWithoutNoticeOrder(CaseData caseData) {
        if (caseData.getOrderWithoutGivingNoticeToRespondent() != null) {
            WithoutNoticeOrder.WithoutNoticeOrderBuilder builder = WithoutNoticeOrder.builder();
            builder.orderWithoutGivingNotice(caseData.getOrderWithoutGivingNoticeToRespondent().getOrderWithoutGivingNotice());

            if (caseData.getReasonForOrderWithoutGivingNotice() != null) {
                ReasonForWithoutNoticeOrder reason = caseData.getReasonForOrderWithoutGivingNotice();
                List<String> reasonForOrderWithoutNoticeEnum = reason.getReasonForOrderWithoutGivingNotice() != null
                    ? reason.getReasonForOrderWithoutGivingNotice().stream()
                    .map(ReasonForOrderWithoutGivingNoticeEnum::getDisplayedValue)
                    .collect(Collectors.toList()) : new ArrayList<>();
                builder.reasonForOrderWithoutGivingNotice(String.join(", ",
                    reasonForOrderWithoutNoticeEnum)).futherDetails(reason.getFutherDetails());
            }
            if (caseData.getBailDetails() != null) {
                RespondentBailConditionDetails bail = caseData.getBailDetails();
                builder.isRespondentAlreadyInBailCondition(bail.getIsRespondentAlreadyInBailCondition())
                        .bailConditionEndDate(bail.getBailConditionEndDate());
            }
            if (caseData.getAnyOtherDtailsForWithoutNoticeOrder() != null) {
                builder.anyOtherDtailsForWithoutNoticeOrder(caseData.getAnyOtherDtailsForWithoutNoticeOrder().getOtherDetails());
            }
            return toMap(builder.build());
        }
        return Collections.emptyMap();
    }

    public Map<String, Object> getFl401ApplicantsTable(CaseData caseData) {
        if (caseData.getApplicantsFL401() == null) {
            return Collections.emptyMap();
        }
        PartyDetails currentApplicant = caseData.getApplicantsFL401();
        currentApplicant = maskFl401ConfidentialDetails(currentApplicant);
        FL401Applicant a = objectMapper.convertValue(currentApplicant, FL401Applicant.class);

        return toMap(a);
    }

    public Map<String, Object> getFl401ApplicantsSolictorDetailsTable(CaseData caseData) {
        if (caseData.getApplicantsFL401() == null) {
            return Collections.emptyMap();
        }
        PartyDetails currentApplicant = caseData.getApplicantsFL401();
        FL401SolicitorDetails a = objectMapper.convertValue(currentApplicant, FL401SolicitorDetails.class);

        return toMap(a);
    }

    public Map<String, Object> getFl401RespondentTable(CaseData caseData) {
        if (caseData.getRespondentsFL401() == null) {
            return Collections.emptyMap();
        }
        PartyDetails currentRespondent = caseData.getRespondentsFL401();
        currentRespondent = maskFl401ConfidentialDetails(currentRespondent);

        FL401Respondent a = objectMapper.convertValue(currentRespondent, FL401Respondent.class);
        return toMap(a);
    }

    public Map<String, Object> getFl401RespondentBehaviourTable(CaseData caseData) {
        if (caseData.getRespondentBehaviourData() == null) {
            return Collections.emptyMap();
        }
        RespondentBehaviour respondentBehaviour = caseData.getRespondentBehaviourData();
        RespondentBehaviourTable.RespondentBehaviourTableBuilder rs = RespondentBehaviourTable.builder();
        List<String> applicantStopFromRespondentDoingEnum = respondentBehaviour.getApplicantWantToStopFromRespondentDoing().stream()
            .map(ApplicantStopFromRespondentDoingEnum::getDisplayedValue)
            .collect(Collectors.toList());

        List<String> applicantStopFromRespondentDoingToChildEnum = new ArrayList<>();
        if (respondentBehaviour.getApplicantWantToStopFromRespondentDoingToChild() != null) {
            applicantStopFromRespondentDoingToChildEnum = respondentBehaviour.getApplicantWantToStopFromRespondentDoingToChild().stream()
                .map(ApplicantStopFromRespondentDoingToChildEnum::getDisplayedValue)
                .collect(Collectors.toList());
        }

        rs.applicantWantToStopFromRespondentDoing(String.join(", ", applicantStopFromRespondentDoingEnum))
            .applicantWantToStopFromRespondentDoingToChild(String.join(", ", applicantStopFromRespondentDoingToChildEnum))
            .otherReasonApplicantWantToStopFromRespondentDoing(respondentBehaviour.getOtherReasonApplicantWantToStopFromRespondentDoing());

        return toMap(rs.build());
    }

    public Map<String, Object> getFl401RelationshipToRespondentTable(CaseData caseData) {
        if (caseData.getRespondentRelationObject() == null) {
            return Collections.emptyMap();
        }
        RespondentRelationObjectType resRelObj = caseData.getRespondentRelationObject();
        RelationshipToRespondent.RelationshipToRespondentBuilder rs = RelationshipToRespondent.builder();

        rs.applicantRelationship(resRelObj.getApplicantRelationship().getDisplayedValue());

        if (caseData.getRespondentRelationDateInfoObject() != null) {
            RespondentRelationDateInfo resRelInfo = caseData.getRespondentRelationDateInfoObject();
            if (resRelInfo.getRelationStartAndEndComplexType() != null) {
                rs.relationshipDateComplexEndDate(resRelInfo.getRelationStartAndEndComplexType().getRelationshipDateComplexStartDate());
                rs.relationshipDateComplexEndDate(resRelInfo.getRelationStartAndEndComplexType().getRelationshipDateComplexEndDate());
            }
            rs.applicantRelationshipDate(resRelInfo.getApplicantRelationshipDate());
        }

        if (caseData.getRespondentRelationOptions() != null && caseData.getRespondentRelationOptions().getApplicantRelationshipOptions() != null) {
            rs.applicantRelationshipOptions(caseData.getRespondentRelationOptions().getApplicantRelationshipOptions().getDisplayedValue());
        }

        return toMap(rs.build());
    }

    public Map<String, Object> getHomeDetails(CaseData caseData) {
        if (caseData.getHome() == null) {
            return Collections.emptyMap();
        }

        HomeDetails.HomeDetailsBuilder builder = HomeDetails.builder();
        Home home = caseData.getHome();

        List<String> peopleLivingAtThisAddressEnum = home.getPeopleLivingAtThisAddress().stream()
            .map(PeopleLivingAtThisAddressEnum::getDisplayedValue)
            .collect(Collectors.toList());

        List<String> familyHomeEnum = home.getFamilyHome().stream()
            .map(FamilyHomeEnum::getDisplayedValue)
            .collect(Collectors.toList());

        List<String> livingSituationEnum = home.getLivingSituation().stream()
            .map(LivingSituationEnum::getDisplayedValue)
            .collect(Collectors.toList());

        builder
            .address(home.getAddress())
            .doAnyChildrenLiveAtAddress(home.getDoAnyChildrenLiveAtAddress())
            .everLivedAtTheAddress(home.getEverLivedAtTheAddress() != null ? home.getEverLivedAtTheAddress().getDisplayedValue() : "")
            .howIsThePropertyAdapted(home.getIsPropertyAdapted())
            .furtherInformation(home.getFurtherInformation())
            .doesApplicantHaveHomeRights(home.getDoesApplicantHaveHomeRights())
            .intendToLiveAtTheAddress(home.getIntendToLiveAtTheAddress() != null ? home.getIntendToLiveAtTheAddress().getDisplayedValue() : "")
            .isPropertyAdapted(home.getIsPropertyAdapted())
            .isPropertyRented(home.getIsPropertyRented())
            .peopleLivingAtThisAddress(String.join(", ", peopleLivingAtThisAddressEnum))
            .familyHome(String.join(", ", familyHomeEnum))
            .livingSituation(String.join(", ", livingSituationEnum))
            .isThereMortgageOnProperty(home.getIsThereMortgageOnProperty());

        if (home.getMortgages() != null && home.getMortgages().getMortgageNamedAfter() != null) {
            Mortgage mortgage = home.getMortgages();

            List<String> mortgageNameAft = mortgage.getMortgageNamedAfter().stream()
                .map(MortgageNamedAfterEnum::getDisplayedValue)
                .collect(Collectors.toList());

            builder.mortgageAddress(mortgage.getAddress())
                .mortgageNumber(mortgage.getMortgageNumber())
                .mortgageNamedAfter(String.join(", ", mortgageNameAft))
                .mortgageLenderName(mortgage.getMortgageLenderName());
        }
        if (home.getLandlords() != null && home.getLandlords().getMortgageNamedAfterList() != null) {
            Landlord landlord = home.getLandlords();

            List<String> landlordNamedAft = landlord.getMortgageNamedAfterList().stream()
                .map(MortgageNamedAfterEnum::getDisplayedValue)
                .collect(Collectors.toList());

            builder.landlordAddress(landlord.getAddress())
                .landlordName(landlord.getLandlordName())
                .landLordNamedAfter(String.join(", ", landlordNamedAft));
        }
        HomeDetails homeDetails = builder.build();
        homeDetails = loadOrMaskHomeChildDetails(homeDetails, home);
        return toMap(homeDetails);
    }

    private HomeDetails loadOrMaskHomeChildDetails(HomeDetails homeDetails, Home home) {
        List<Element<ChildrenLiveAtAddress>> children = home.getChildren();
        if (isNotEmpty(children)) {
            List<ChildrenLiveAtAddress> eachChildren = children.stream()
                .map(Element::getValue).collect(Collectors.toList());
            List<Element<HomeChild>> childList = new ArrayList<>();
            for (ChildrenLiveAtAddress eachChild : eachChildren) {
                HomeChild.HomeChildBuilder builder =  HomeChild.builder()
                    .childsAge(getMaskTextIfConfIsChoosenAsYes(eachChild.getChildsAge(), eachChild.getKeepChildrenInfoConfidential()))
                    .childFullName(getMaskTextIfConfIsChoosenAsYes(eachChild.getChildFullName(), eachChild.getKeepChildrenInfoConfidential()));
                builder.isRespondentResponsibleForChild(eachChild.getIsRespondentResponsibleForChild().getDisplayedValue());
                if (YesOrNo.Yes.equals(eachChild.getKeepChildrenInfoConfidential())) {
                    builder.isRespondentResponsibleForChild(THIS_INFORMATION_IS_CONFIDENTIAL);
                }
                Element<HomeChild> homeChild = Element.<HomeChild>builder()
                    .value(builder.build()).build();
                childList.add(homeChild);
            }
            homeDetails = homeDetails.toBuilder().children(childList).build();
        }
        return homeDetails;
    }

    private String getMaskTextIfConfIsChoosenAsYes(String value, YesOrNo keepChildrenInfoConfidential) {
        if (YesOrNo.Yes.equals(keepChildrenInfoConfidential)) {
            return THIS_INFORMATION_IS_CONFIDENTIAL;
        }
        return value;
    }

    public Map<String, Object> getApplicantsFamilyDetails(CaseData caseData) {
        if (caseData.getApplicantFamilyDetails() == null) {
            return Collections.emptyMap();
        }

        ApplicantFamily.ApplicantFamilyBuilder builder =  ApplicantFamily.builder()
            .doesApplicantHaveChildren(caseData.getApplicantFamilyDetails().getDoesApplicantHaveChildren());

        if (YesOrNo.Yes.equals(caseData.getApplicantFamilyDetails().getDoesApplicantHaveChildren())) {
            builder.applicantChild(caseData.getApplicantChildDetails());
        }

        return toMap(builder.build());
    }

    public List<Element<ChildAndCafcassOfficer>> prePopulateChildAndCafcassOfficerDetails(CaseData caseData) {
        List<Element<ChildAndCafcassOfficer>> childAndCafcassOfficers = new ArrayList<>();
        if (caseData.getChildren() != null) {
            caseData.getChildren().stream().forEach(childElement -> {
                ChildAndCafcassOfficer childAndCafcassOfficer = ChildAndCafcassOfficer.builder()
                    .childId(childElement.getId().toString())
                    .childName(CHILD_NAME + childElement.getValue().getFirstName() + " " + childElement.getValue().getLastName())
                    .cafcassOfficerName(childElement.getValue().getCafcassOfficerName())
                    .cafcassOfficerPosition(childElement.getValue().getCafcassOfficerPosition())
                    .cafcassOfficerOtherPosition(childElement.getValue().getCafcassOfficerOtherPosition())
                    .cafcassOfficerEmailAddress(childElement.getValue().getCafcassOfficerEmailAddress())
                    .cafcassOfficerPhoneNo(childElement.getValue().getCafcassOfficerPhoneNo())
                    .build();
                childAndCafcassOfficers.add(element(childAndCafcassOfficer));
            });
        }
        return childAndCafcassOfficers;
    }

}
