package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.enums.LivingSituationEnum;
import uk.gov.hmcts.reform.prl.enums.MiamChildProtectionConcernChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamOtherGroundsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamPreviousAttendanceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MortgageNamedAfterEnum;
import uk.gov.hmcts.reform.prl.enums.NewPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.PeopleLivingAtThisAddressEnum;
import uk.gov.hmcts.reform.prl.enums.ReasonForOrderWithoutGivingNoticeEnum;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamDomesticAbuseChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPolicyUpgradeChildProtectionConcernEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildAbuse;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.complextypes.DomesticAbuseBehaviours;
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
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.MiamPolicyUpgrade;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.MiamPolicyUpgradeExemptions;
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
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised.AllegationsOfHarmRevisedChildContact;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised.AllegationsOfHarmRevisedOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised.AllegationsOfHarmRevisedOtherConcerns;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised.AllegationsOfHarmRevisedOverview;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised.ChildAbuseBehaviour;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised.DomesticAbuseBehaviour;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised.OrderRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised.RevisedChildAbductionDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarmRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ChildPassportDetails;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;
import uk.gov.hmcts.reform.prl.services.tab.TabService;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.FieldGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_RESPONDENT_TABLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CHILD_AND_CAFCASS_OFFICER_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CHILD_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.THIS_INFORMATION_IS_CONFIDENTIAL;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper.COMMA_SEPARATOR;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;



@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationsTabService implements TabService {
    private final ObjectMapper objectMapper;
    private final ApplicationsTabServiceHelper applicationsTabServiceHelper;
    private final AllegationOfHarmRevisedService allegationOfHarmRevisedService;
    private final MiamPolicyUpgradeService miamPolicyUpgradeService;

    @Override
    public Map<String, Object> updateTab(CaseData caseData) {

        Map<String, Object> applicationTab = new HashMap<>();
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            applicationTab.put("hearingUrgencyTable", getHearingUrgencyTable(caseData));
            applicationTab.put("applicantTable", getApplicantsTable(caseData));
            applicationTab.put(C100_RESPONDENT_TABLE, getRespondentsTable(caseData));
            applicationTab.put("declarationTable", getDeclarationTable(caseData));
            applicationTab.put("typeOfApplicationTable", getTypeOfApplicationTable(caseData));
            caseData = upTabForMiam(caseData, applicationTab);
            applicationTab.put("otherProceedingsTable", getOtherProceedingsTable(caseData));
            applicationTab.put("otherProceedingsDetailsTable", getOtherProceedingsDetailsTable(caseData));
            applicationTab.put("internationalElementTable", getInternationalElementTable(caseData));
            applicationTab.put("attendingTheHearingTable", getAttendingTheHearingTable(caseData));
            applicationTab.put("litigationCapacityTable", getLitigationCapacityDetails(caseData));
            applicationTab.put("welshLanguageRequirementsTable", getWelshLanguageRequirementsTable(caseData));
            applicationTab.put(CHILD_AND_CAFCASS_OFFICER_DETAILS, prePopulateChildAndCafcassOfficerDetails(caseData));
            if (PrlAppsConstants.TASK_LIST_VERSION_V2.equals(caseData.getTaskListVersion())
                || PrlAppsConstants.TASK_LIST_VERSION_V3.equals(caseData.getTaskListVersion())) {
                applicationTab.put("childDetailsRevisedTable", applicationsTabServiceHelper.getChildRevisedDetails(caseData));
                applicationTab.put("childDetailsRevisedExtraTable", getExtraChildDetailsTable(caseData));
                applicationTab.put("otherPeopleInTheCaseRevisedTable", applicationsTabServiceHelper.getOtherPeopleInTheCaseRevisedTable(caseData));
                applicationTab.put("otherChildNotInTheCaseTable", applicationsTabServiceHelper.getOtherChildNotInTheCaseTable(caseData));
                applicationTab.put("childAndApplicantsRelationTable", applicationsTabServiceHelper.getChildAndApplicantsRelationTable(caseData));
                applicationTab.put("childAndRespondentRelationsTable", applicationsTabServiceHelper.getChildAndRespondentRelationsTable(caseData));
                applicationTab.put("childAndOtherPeopleRelationsTable",
                                   applicationsTabServiceHelper.getChildAndOtherPeopleRelationsTable(caseData));
                applicationTab.put("allegationsOfHarmRevisedOverviewTable", getAllegationsOfHarmRevisedOverviewTable(caseData));
                applicationTab.put("allegationsOfHarmRevisedDATable", getAllegationsOfHarmRevisedDaTable(caseData));
                applicationTab.put("allegationsOfHarmRevisedCATable", getAllegationsOfHarmRevisedCaTable(caseData));
                applicationTab.put("allegationsOfHarmRevisedOrdersTable", getAllegationsOfHarmRevisedOrdersTable(caseData));
                applicationTab.put("allegationsOfHarmRevisedChildAbductionTable", getRevisedChildAbductionTable(caseData));
                applicationTab.put("allegationsOfHarmRevisedOtherConcernsTable", getAllegationsOfHarmRevisedOtherConcerns(caseData));
                applicationTab.put("allegationsOfHarmRevisedChildContactTable", getAllegationsOfHarmRevisedChildContact(caseData));
                applicationTab.put(CHILD_AND_CAFCASS_OFFICER_DETAILS, prePopulateRevisedChildAndCafcassOfficerDetails(caseData));

                log.info("application tab data v2 & v3");
            } else {
                applicationTab.put("childDetailsTable", getChildDetails(caseData));
                applicationTab.put("childDetailsExtraTable", getExtraChildDetailsTable(caseData));
                applicationTab.put("otherPeopleInTheCaseTable", getOtherPeopleInTheCaseTable(caseData));
                applicationTab.put("allegationsOfHarmOrdersTable", getAllegationsOfHarmOrdersTable(caseData));
                applicationTab.put("allegationsOfHarmOverviewTable", getAllegationsOfHarmOverviewTable(caseData));
                applicationTab.put("allegationsOfHarmDomesticAbuseTable", getDomesticAbuseTable(caseData));
                applicationTab.put("allegationsOfHarmChildAbductionTable", getChildAbductionTable(caseData));
                applicationTab.put("allegationsOfHarmOtherConcernsTable", getAllegationsOfHarmOtherConcerns(caseData));
            }
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

    private CaseData upTabForMiam(CaseData caseData, Map<String, Object> applicationTab) {
        if (PrlAppsConstants.TASK_LIST_VERSION_V3.equals(caseData.getTaskListVersion())) {
            if (ObjectUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails())) {
                caseData = miamPolicyUpgradeService.updateMiamPolicyUpgradeDetails(caseData, new HashMap<>());
            }
            applicationTab.put("miamPolicyUpgradeTable", getMiamPolicyUpgradeTable(caseData));
            applicationTab.put("miamPolicyUpgradeExemptionsTable", getMiamExemptionsTableForPolicyUpgrade(caseData));
        } else {
            applicationTab.put("miamTable", getMiamTable(caseData));
            applicationTab.put("miamExemptionsTable", getMiamExemptionsTable(caseData));
        }
        return caseData;
    }

    /*  public Map<String, Object> updateCitizenPartiesTab(CaseData caseData) {

        Map<String, Object> applicationTab = new HashMap<>();
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            applicationTab.put(C100_APPLICANT_TABLE, getApplicantsTable(caseData));
            applicationTab.put(C100_RESPONDENT_TABLE, getRespondentsTable(caseData));
        } else if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            applicationTab.put(FL401_APPLICANT_TABLE, getFl401ApplicantsTable(caseData));
            applicationTab.put(FL401_RESPONDENT_TABLE, getFl401RespondentTable(caseData));
        }
        return applicationTab;
     }*/

    private List<Element<DomesticAbuseBehaviour>> getAllegationsOfHarmRevisedDaTable(CaseData caseData) {

        List<Element<DomesticAbuseBehaviour>> domesticAbuseBehaviourList = new ArrayList<>();
        if (YesOrNo.Yes.equals(caseData.getAllegationOfHarmRevised().getNewAllegationsOfHarmDomesticAbuseYesNo())) {
            Optional<List<Element<DomesticAbuseBehaviours>>> domesticBehaviours = ofNullable(caseData.getAllegationOfHarmRevised()
                    .getDomesticBehaviours());

            if (domesticBehaviours.isPresent()) {
                domesticBehaviours.get().forEach(each -> {
                    DomesticAbuseBehaviour domesticAbuseBehaviour = DomesticAbuseBehaviour
                                    .builder().newAbuseNatureDescription(each.getValue().getNewAbuseNatureDescription())
                                    .typeOfAbuse(each.getValue().getTypeOfAbuse() != null
                                                     ? each.getValue().getTypeOfAbuse().getDisplayedValue() : null)
                                    .newBehavioursApplicantHelpSoughtWho(YesOrNo.Yes.equals(each.getValue()
                                            .getNewBehavioursApplicantSoughtHelp()) ? each.getValue().getNewBehavioursApplicantHelpSoughtWho() : null)
                                    .newBehavioursApplicantSoughtHelp(each.getValue().getNewBehavioursApplicantSoughtHelp())
                                    .newBehavioursStartDateAndLength(each.getValue().getNewBehavioursStartDateAndLength())
                                    .build();
                    Element<DomesticAbuseBehaviour> app = Element.<DomesticAbuseBehaviour>builder().value(domesticAbuseBehaviour).build();
                    domesticAbuseBehaviourList.add(app);
                }
                );
                return domesticAbuseBehaviourList;
            }
        }
        DomesticAbuseBehaviour domesticAbuseBehaviour = DomesticAbuseBehaviour.builder().build();
        Element<DomesticAbuseBehaviour> app = Element.<DomesticAbuseBehaviour>builder().value(domesticAbuseBehaviour).build();
        domesticAbuseBehaviourList.add(app);
        return domesticAbuseBehaviourList;
    }

    private List<Element<ChildAbuseBehaviour>> getAllegationsOfHarmRevisedCaTable(CaseData caseData) {
        List<ChildAbuse> childAbuseBehavioursList = new ArrayList<>();

        Optional<ChildAbuse> childPhysicalAbuse =
                ofNullable(caseData.getAllegationOfHarmRevised().getChildPhysicalAbuse());

        Optional<ChildAbuse> childPsychologicalAbuse =
                ofNullable(caseData.getAllegationOfHarmRevised().getChildPsychologicalAbuse());


        Optional<ChildAbuse> childFinancialAbuse =
                ofNullable(caseData.getAllegationOfHarmRevised().getChildFinancialAbuse());
        List<Element<ChildAbuseBehaviour>> childAbuseBehaviourList = new ArrayList<>();

        childPhysicalAbuse.ifPresent(abuse -> {
            if (Objects.nonNull(abuse.getTypeOfAbuse())) {
                childAbuseBehavioursList.add(abuse);
            }
        }
        );

        childFinancialAbuse.ifPresent(abuse -> {
            if (Objects.nonNull(abuse.getTypeOfAbuse())) {
                childAbuseBehavioursList.add(abuse);
            }
        }
        );

        childPsychologicalAbuse.ifPresent(abuse -> {
            if (Objects.nonNull(abuse.getTypeOfAbuse())) {
                childAbuseBehavioursList.add(abuse);
            }
        }
        );

        Optional<ChildAbuse> childEmotionalAbuse =
                ofNullable(caseData.getAllegationOfHarmRevised().getChildEmotionalAbuse());

        Optional<ChildAbuse> childSexualAbuse =
                ofNullable(caseData.getAllegationOfHarmRevised().getChildSexualAbuse());

        childEmotionalAbuse.ifPresent(abuse -> {
            if (Objects.nonNull(abuse.getTypeOfAbuse())) {
                childAbuseBehavioursList.add(abuse);
            }
        }
        );

        childSexualAbuse.ifPresent(abuse -> {
            if (Objects.nonNull(abuse.getTypeOfAbuse())) {
                childAbuseBehavioursList.add(abuse);
            }
        }
        );


        AllegationOfHarmRevised allegationOfHarmRevised = caseData.getAllegationOfHarmRevised();


        if (YesOrNo.Yes.equals(allegationOfHarmRevised.getNewAllegationsOfHarmChildAbuseYesNo())) {
            childAbuseBehavioursList.forEach(each -> {
                Optional<DynamicMultiSelectList> whichChildrenAreRisk = ofNullable(
                        allegationOfHarmRevisedService.getWhichChildrenAreInRisk(each.getTypeOfAbuse(), allegationOfHarmRevised));
                ChildAbuseBehaviour childAbuseBehaviour = ChildAbuseBehaviour
                                .builder().newAbuseNatureDescription(each.getAbuseNatureDescription())
                                .typeOfAbuse(each.getTypeOfAbuse().getDisplayedValue())
                                .newBehavioursApplicantHelpSoughtWho(YesOrNo.Yes
                                        .equals(each.getBehavioursApplicantSoughtHelp()) ? each.getBehavioursApplicantHelpSoughtWho() : null)
                                .newBehavioursApplicantSoughtHelp(each.getBehavioursApplicantSoughtHelp())
                                .newBehavioursStartDateAndLength(each.getBehavioursStartDateAndLength())
                                .allChildrenAreRisk(
                                    allegationOfHarmRevisedService.getIfAllChildrenAreRisk(each.getTypeOfAbuse(),allegationOfHarmRevised))
                                .whichChildrenAreRisk(whichChildrenAreRisk.map(dynamicMultiSelectList -> dynamicMultiSelectList
                                        .getValue().stream()
                                        .map(DynamicMultiselectListElement::getLabel)
                                        .collect(Collectors.joining(","))).orElse(null))
                                .build();
                Element<ChildAbuseBehaviour> app = Element.<ChildAbuseBehaviour>builder().value(childAbuseBehaviour).build();
                childAbuseBehaviourList.add(app);

            });
            return childAbuseBehaviourList;
        }
        ChildAbuseBehaviour childAbuseBehaviour = ChildAbuseBehaviour.builder().build();
        Element<ChildAbuseBehaviour> app = Element.<ChildAbuseBehaviour>builder().value(childAbuseBehaviour).build();
        childAbuseBehaviourList.add(app);
        return childAbuseBehaviourList;
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
            .toList();
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
                    .toList();

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
            applicants.add(Element.<Applicant>builder().value(Applicant.builder().build()).build());
            return applicants;
        }
        List<Element<PartyDetails>> currentApplicants = maskConfidentialDetails(caseData.getApplicants());
        for (Element<PartyDetails> currentApplicant : currentApplicants) {
            Applicant applicant = objectMapper.convertValue(currentApplicant.getValue(), Applicant.class);
            Element<Applicant> applicantElement = Element.<Applicant>builder().id(currentApplicant.getId())
                .value(applicant.toBuilder().gender(Gender.getDisplayedValueFromEnumString(applicant.getGender())
                                                        .getDisplayedValue())
                           .canYouProvideEmailAddress(StringUtils.isNotEmpty(applicant.getEmail()) ? YesOrNo
                               .Yes : YesOrNo.No)
                           .isAtAddressLessThan5Years(
                               applicant.getIsAtAddressLessThan5Years() != null ? YesOrNo.getValue(applicant
                               .getIsAtAddressLessThan5Years().getDisplayedValue()) : null)
                           .build())
                .build();
            applicants.add(applicantElement);
        }
        return applicants;
    }

    public List<Element<PartyDetails>> maskConfidentialDetails(List<Element<PartyDetails>> parties) {
        List<Element<PartyDetails>> updatedPartyDetails = new ArrayList<>();
        for (Element<PartyDetails> party : parties) {
            if ((YesOrNo.Yes).equals(party.getValue().getIsPhoneNumberConfidential())) {
                party = Element.<PartyDetails>builder()
                    .value(party.getValue().toBuilder().phoneNumber(THIS_INFORMATION_IS_CONFIDENTIAL).build())
                    .id(party.getId())
                    .build();
            }
            if ((YesOrNo.Yes).equals(party.getValue().getIsEmailAddressConfidential())) {
                party = Element.<PartyDetails>builder()
                    .value(party.getValue().toBuilder().email(THIS_INFORMATION_IS_CONFIDENTIAL).build())
                    .id(party.getId())
                    .build();
            }
            if ((YesOrNo.Yes).equals(party.getValue().getIsAddressConfidential())) {
                party = Element.<PartyDetails>builder()
                    .value(party.getValue().toBuilder().address(Address.builder()
                                                                    .addressLine1(THIS_INFORMATION_IS_CONFIDENTIAL)
                                                                    .build()).build())
                    .id(party.getId())
                    .build();
            }
            updatedPartyDetails.add(party);
        }
        return updatedPartyDetails;
    }

    public PartyDetails maskFl401ConfidentialDetails(PartyDetails applicantDetails) {

        if ((YesOrNo.Yes).equals(applicantDetails.getIsPhoneNumberConfidential())) {
            applicantDetails = applicantDetails.toBuilder().phoneNumber(THIS_INFORMATION_IS_CONFIDENTIAL).build();
        }
        if ((YesOrNo.Yes).equals(applicantDetails.getIsEmailAddressConfidential())) {
            applicantDetails = applicantDetails.toBuilder().email(THIS_INFORMATION_IS_CONFIDENTIAL).build();
        }
        if ((YesOrNo.Yes).equals(applicantDetails.getIsAddressConfidential())) {
            applicantDetails = applicantDetails.toBuilder().address(Address.builder()
                                                                        .addressLine1(THIS_INFORMATION_IS_CONFIDENTIAL)
                                                                        .build()).build();
        }
        return applicantDetails;
    }

    public List<Element<Respondent>> getRespondentsTable(CaseData caseData) {
        List<Element<Respondent>> respondents = new ArrayList<>();
        Optional<List<Element<PartyDetails>>> checkRespondents = ofNullable(caseData.getRespondents());
        if (checkRespondents.isEmpty()) {
            respondents.add(Element.<Respondent>builder().value(Respondent.builder().build()).build());
            return respondents;
        }
        List<Element<PartyDetails>> currentRespondents = maskConfidentialDetails(caseData.getRespondents());
        for (Element<PartyDetails> currentRespondent : currentRespondents) {
            Respondent respondent = objectMapper.convertValue(currentRespondent.getValue(), Respondent.class);

            Element<Respondent> respondentElement = Element.<Respondent>builder().id(currentRespondent.getId()).value(respondent.toBuilder()
                .gender(respondent.getGender() != null ? Gender.getDisplayedValueFromEnumString(respondent.getGender()).getDisplayedValue() : null)
                .isAtAddressLessThan5YearsWithDontKnow(respondent.getIsAtAddressLessThan5YearsWithDontKnow() != null
                                                   ? YesNoDontKnow.getDisplayedValueIgnoreCase(
                                                       respondent.getIsAtAddressLessThan5YearsWithDontKnow()).getDisplayedValue() : null)
                .doTheyHaveLegalRepresentation(respondent.getDoTheyHaveLegalRepresentation() != null
                                                   ? YesNoDontKnow.getDisplayedValueIgnoreCase(
                                                       respondent.getDoTheyHaveLegalRepresentation()).getDisplayedValue() : null)
                .build()).build();
            respondents.add(respondentElement);
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
            .toList();

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

    public Map<String, Object> getAllegationsOfHarmRevisedOverviewTable(CaseData caseData) {
        AllegationsOfHarmRevisedOverview allegationsOfHarmRevisedOverview = objectMapper
                .convertValue(caseData, AllegationsOfHarmRevisedOverview.class);
        return toMap(allegationsOfHarmRevisedOverview);

    }

    public Map<String, Object> getMiamTable(CaseData caseData) {
        Miam miam = objectMapper.convertValue(caseData, Miam.class);
        return toMap(miam);
    }

    public Map<String, Object> getMiamPolicyUpgradeTable(CaseData caseData) {
        MiamPolicyUpgrade miam = objectMapper.convertValue(caseData, MiamPolicyUpgrade.class);
        return toMap(miam);
    }

    public Map<String, Object> getMiamExemptionsTableForPolicyUpgrade(CaseData caseData) {
        Optional<List<uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum>> miamExemptionsCheck
            = ofNullable(caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons());
        String reasonsForMiamExemption = PrlAppsConstants.EMPTY_STRING;
        if (miamExemptionsCheck.isPresent()) {
            reasonsForMiamExemption = caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons()
                .stream().map(uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum::getDisplayedValue)
                .collect(Collectors.joining(", "));
        }

        String domesticAbuseEvidence = PrlAppsConstants.EMPTY_STRING;
        Optional<List<MiamDomesticAbuseChecklistEnum>> domesticAbuseCheck
            = ofNullable(caseData.getMiamPolicyUpgradeDetails()
                             .getMpuDomesticAbuseEvidences());
        if (domesticAbuseCheck.isPresent()) {
            domesticAbuseEvidence = caseData.getMiamPolicyUpgradeDetails()
                .getMpuDomesticAbuseEvidences()
                .stream().map(MiamDomesticAbuseChecklistEnum::getDisplayedValue)
                .collect(Collectors.joining("\n"));
        }

        String urgencyEvidence = PrlAppsConstants.EMPTY_STRING;
        Optional<uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamUrgencyReasonChecklistEnum> urgencyCheck =
            ofNullable(caseData.getMiamPolicyUpgradeDetails()
                           .getMpuUrgencyReason());
        if (urgencyCheck.isPresent()) {
            urgencyEvidence = urgencyCheck.get().getDisplayedValue();
        }

        String previousAttendenceEvidence = PrlAppsConstants.EMPTY_STRING;
        Optional<uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPreviousAttendanceChecklistEnum> prevCheck =
            ofNullable(caseData.getMiamPolicyUpgradeDetails().getMpuPreviousMiamAttendanceReason());
        if (prevCheck.isPresent()) {
            previousAttendenceEvidence = prevCheck.get().getDisplayedValue();
        }

        String otherGroundsEvidence = PrlAppsConstants.EMPTY_STRING;
        Optional<uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamOtherGroundsChecklistEnum> othCheck =
            ofNullable(caseData.getMiamPolicyUpgradeDetails().getMpuOtherExemptionReasons());
        if (othCheck.isPresent()) {
            otherGroundsEvidence = othCheck.get().getDisplayedValue();
        }

        String childEvidence = PrlAppsConstants.EMPTY_STRING;
        Optional<MiamPolicyUpgradeChildProtectionConcernEnum> childCheck =
            ofNullable(caseData.getMiamPolicyUpgradeDetails().getMpuChildProtectionConcernReason());
        if (childCheck.isPresent()) {
            childEvidence = childCheck.get().getDisplayedValue();
        }

        YesOrNo mpuIsDomesticAbuseEvidenceProvided = ObjectUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails()
                                                                                .getMpuIsDomesticAbuseEvidenceProvided())
            ? caseData.getMiamPolicyUpgradeDetails().getMpuIsDomesticAbuseEvidenceProvided() : null;
        String mpuTypeOfPreviousMiamAttendanceEvidence = ObjectUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails()
                                                                                    .getMpuTypeOfPreviousMiamAttendanceEvidence())
            ? caseData.getMiamPolicyUpgradeDetails().getMpuTypeOfPreviousMiamAttendanceEvidence().getDisplayedValue() : null;

        MiamPolicyUpgradeExemptions miamExemptions = MiamPolicyUpgradeExemptions.builder()
            .mpuReasonsForMiamExemption(reasonsForMiamExemption)
            .mpuDomesticAbuseEvidence(domesticAbuseEvidence)
            .mpuChildProtectionEvidence(childEvidence)
            .mpuUrgencyEvidence(urgencyEvidence)
            .mpuPreviousAttendenceEvidence(previousAttendenceEvidence)
            .mpuOtherGroundsEvidence(otherGroundsEvidence)
            .mpuIsDomesticAbuseEvidenceProvided(mpuIsDomesticAbuseEvidenceProvided)
            .mpuTypeOfPreviousMiamAttendanceEvidence(mpuTypeOfPreviousMiamAttendanceEvidence)
            .build();

        return toMap(miamExemptions);

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
            .map(Element::getValue).toList();
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
            .map(Element::getValue).toList();
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

    public Map<String, Object> getAllegationsOfHarmRevisedOrdersTable(CaseData caseData) {
        AllegationsOfHarmRevisedOrders allegationsOfHarmRevisedOrders = objectMapper
                .convertValue(caseData, AllegationsOfHarmRevisedOrders.class);
        getSpecificOrderRevisedDetails(allegationsOfHarmRevisedOrders, caseData);
        return toMap(allegationsOfHarmRevisedOrders);
    }

    public AllegationsOfHarmRevisedOrders getSpecificOrderRevisedDetails(
             AllegationsOfHarmRevisedOrders allegationsOfHarmRevisedOrders, CaseData caseData) {

        Optional<YesOrNo> nonMolYesNo = ofNullable(allegationsOfHarmRevisedOrders.getNewOrdersNonMolestation());
        if (nonMolYesNo.isPresent() && nonMolYesNo.get().equals(YesOrNo.Yes)) {
            OrderRevised nonMolOrder = OrderRevised.builder()
                    .dateIssued(caseData.getAllegationOfHarmRevised().getNewOrdersNonMolestationDateIssued())
                    .endDate(caseData.getAllegationOfHarmRevised().getNewOrdersNonMolestationEndDate())
                    .orderCurrent(caseData.getAllegationOfHarmRevised().getNewOrdersNonMolestationCurrent())
                    .courtName(caseData.getAllegationOfHarmRevised().getNewOrdersNonMolestationCourtName())
                    .caseNumber(caseData.getAllegationOfHarmRevised().getNewOrdersNonMolestationCaseNumber())
                    .build();
            allegationsOfHarmRevisedOrders.setNonMolestationOrder(nonMolOrder);
        }

        Optional<YesOrNo> occYesNo = ofNullable(allegationsOfHarmRevisedOrders.getNewOrdersOccupation());
        if (occYesNo.isPresent() && occYesNo.get().equals(YesOrNo.Yes)) {
            OrderRevised occOrder = OrderRevised.builder()
                    .dateIssued(caseData.getAllegationOfHarmRevised().getNewOrdersOccupationDateIssued())
                    .endDate(caseData.getAllegationOfHarmRevised().getNewOrdersOccupationEndDate())
                    .orderCurrent(caseData.getAllegationOfHarmRevised().getNewOrdersOccupationCurrent())
                    .courtName(caseData.getAllegationOfHarmRevised().getNewOrdersOccupationCourtName())
                    .caseNumber(caseData.getAllegationOfHarmRevised().getNewOrdersOccupationCaseNumber())
                    .build();
            allegationsOfHarmRevisedOrders.setOccupationOrder(occOrder);
        }

        Optional<YesOrNo> forcedYesNo = ofNullable(allegationsOfHarmRevisedOrders.getNewOrdersForcedMarriageProtection());
        if (forcedYesNo.isPresent() && forcedYesNo.get().equals(YesOrNo.Yes)) {
            OrderRevised forOrder = OrderRevised.builder()
                    .dateIssued(caseData.getAllegationOfHarmRevised().getNewOrdersForcedMarriageProtectionDateIssued())
                    .endDate(caseData.getAllegationOfHarmRevised().getNewOrdersForcedMarriageProtectionEndDate())
                    .orderCurrent(caseData.getAllegationOfHarmRevised().getNewOrdersForcedMarriageProtectionCurrent())
                    .courtName(caseData.getAllegationOfHarmRevised().getNewOrdersForcedMarriageProtectionCourtName())
                    .caseNumber(caseData.getAllegationOfHarmRevised().getNewOrdersForcedMarriageProtectionCaseNumber())
                    .build();
            allegationsOfHarmRevisedOrders.setForcedMarriageProtectionOrder(forOrder);
        }

        Optional<YesOrNo> resYesNo = ofNullable(allegationsOfHarmRevisedOrders.getNewOrdersRestraining());
        if (resYesNo.isPresent() && resYesNo.get().equals(YesOrNo.Yes)) {
            OrderRevised resOrder = OrderRevised.builder()
                    .dateIssued(caseData.getAllegationOfHarmRevised().getNewOrdersRestrainingDateIssued())
                    .endDate(caseData.getAllegationOfHarmRevised().getNewOrdersRestrainingEndDate())
                    .orderCurrent(caseData.getAllegationOfHarmRevised().getNewOrdersRestrainingCurrent())
                    .courtName(caseData.getAllegationOfHarmRevised().getNewOrdersRestrainingCourtName())
                    .caseNumber(caseData.getAllegationOfHarmRevised().getNewOrdersRestrainingCaseNumber())
                    .build();
            allegationsOfHarmRevisedOrders.setRestrainingOrder(resOrder);
        }

        Optional<YesOrNo> othYesNo = ofNullable(allegationsOfHarmRevisedOrders.getNewOrdersOtherInjunctive());
        if (othYesNo.isPresent() && othYesNo.get().equals(YesOrNo.Yes)) {
            OrderRevised othOrder = OrderRevised.builder()
                    .dateIssued(caseData.getAllegationOfHarmRevised().getNewOrdersOtherInjunctiveDateIssued())
                    .endDate(caseData.getAllegationOfHarmRevised().getNewOrdersOtherInjunctiveEndDate())
                    .orderCurrent(caseData.getAllegationOfHarmRevised().getNewOrdersOtherInjunctiveCurrent())
                    .courtName(caseData.getAllegationOfHarmRevised().getNewOrdersOtherInjunctiveCourtName())
                    .caseNumber(caseData.getAllegationOfHarmRevised().getNewOrdersOtherInjunctiveCaseNumber())
                    .build();
            allegationsOfHarmRevisedOrders.setOtherInjunctiveOrder(othOrder);
        }

        Optional<YesOrNo> undYesNo = ofNullable(allegationsOfHarmRevisedOrders.getNewOrdersUndertakingInPlace());
        if (undYesNo.isPresent() && undYesNo.get().equals(YesOrNo.Yes)) {
            OrderRevised undOrder = OrderRevised.builder()
                    .dateIssued(caseData.getAllegationOfHarmRevised().getNewOrdersUndertakingInPlaceDateIssued())
                    .endDate(caseData.getAllegationOfHarmRevised().getNewOrdersUndertakingInPlaceEndDate())
                    .orderCurrent(caseData.getAllegationOfHarmRevised().getNewOrdersUndertakingInPlaceCurrent())
                    .courtName(caseData.getAllegationOfHarmRevised().getNewOrdersUndertakingInPlaceCourtName())
                    .caseNumber(caseData.getAllegationOfHarmRevised().getNewOrdersUndertakingInPlaceCaseNumber())
                    .build();
            allegationsOfHarmRevisedOrders.setUndertakingInPlaceOrder(undOrder);
        }

        return allegationsOfHarmRevisedOrders;
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

    public Map<String, Object> getRevisedChildAbductionTable(CaseData caseData) {
        RevisedChildAbductionDetails revisedChildAbductionDetails = objectMapper.convertValue(caseData, RevisedChildAbductionDetails.class);
        Optional<ChildPassportDetails> childPassportDetails = Optional.ofNullable(caseData.getAllegationOfHarmRevised().getChildPassportDetails());
        if (YesOrNo.Yes.equals(revisedChildAbductionDetails.getNewAbductionChildHasPassport()) && childPassportDetails.isPresent()) {
            revisedChildAbductionDetails.setNewChildHasMultiplePassports(childPassportDetails.get().getNewChildHasMultiplePassports());
            revisedChildAbductionDetails.setNewChildPassportPossession(childPassportDetails.get().getNewChildPassportPossession().stream()
                    .map(NewPassportPossessionEnum::getDisplayedValue).collect(Collectors.joining(COMMA_SEPARATOR)));
        }
        return toMap(revisedChildAbductionDetails);
    }


    public Map<String, Object> getAllegationsOfHarmOtherConcerns(CaseData caseData) {
        AllegationsOfHarmOtherConcerns allegationsOfHarmOtherConcerns = objectMapper
            .convertValue(caseData, AllegationsOfHarmOtherConcerns.class);
        return toMap(allegationsOfHarmOtherConcerns);
    }

    public Map<String, Object> getAllegationsOfHarmRevisedOtherConcerns(CaseData caseData) {
        AllegationsOfHarmRevisedOtherConcerns allegationsOfHarmRevisedOtherConcerns = AllegationsOfHarmRevisedOtherConcerns
                .builder().newAllegationsOfHarmOtherConcernsCourtActions(caseData.getAllegationOfHarmRevised()
                        .getNewAllegationsOfHarmOtherConcernsCourtActions()).build();
        return toMap(allegationsOfHarmRevisedOtherConcerns);
    }

    public Map<String, Object> getAllegationsOfHarmRevisedChildContact(CaseData caseData) {
        AllegationsOfHarmRevisedChildContact allegationsOfHarmRevisedChildContact = objectMapper
                .convertValue(caseData, AllegationsOfHarmRevisedChildContact.class);
        return toMap(allegationsOfHarmRevisedChildContact);
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

        List<PartyDetails> otherPeople = caseData.getOthersToNotify().stream().map(Element::getValue).toList();

        for (PartyDetails currentOtherPerson : otherPeople) {
            OtherPersonInTheCase otherPerson = objectMapper.convertValue(currentOtherPerson, OtherPersonInTheCase.class);
            Element<OtherPersonInTheCase> wrappedPerson = Element.<OtherPersonInTheCase>builder()
                .value(otherPerson.toBuilder()
                           .relationshipToChild(currentOtherPerson.getOtherPersonRelationshipToChildren())
                           .gender(otherPerson.getGender() != null
                                       ? Gender.getDisplayedValueFromEnumString(otherPerson.getGender()).getDisplayedValue() : null)
                           .build())
                .build();
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
                .toList();

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
                    .toList() : new ArrayList<>();
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
        PartyDetails currentApplicant = maskFl401ConfidentialDetails(caseData.getApplicantsFL401());
        FL401Applicant applicant = objectMapper.convertValue(currentApplicant, FL401Applicant.class);
        return toMap(applicant.toBuilder().gender(Gender.getDisplayedValueFromEnumString(applicant.getGender()).getDisplayedValue()).build());
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
        PartyDetails currentRespondent = maskFl401ConfidentialDetails(caseData.getRespondentsFL401());
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
            .toList();

        List<String> applicantStopFromRespondentDoingToChildEnum = new ArrayList<>();
        if (respondentBehaviour.getApplicantWantToStopFromRespondentDoingToChild() != null) {
            applicantStopFromRespondentDoingToChildEnum = respondentBehaviour.getApplicantWantToStopFromRespondentDoingToChild().stream()
                .map(ApplicantStopFromRespondentDoingToChildEnum::getDisplayedValue)
                .toList();
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
            .toList();

        List<String> familyHomeEnum = home.getFamilyHome().stream()
            .map(FamilyHomeEnum::getDisplayedValue)
            .toList();

        List<String> livingSituationEnum = home.getLivingSituation().stream()
            .map(LivingSituationEnum::getDisplayedValue)
            .toList();

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
                .toList();

            builder.mortgageAddress(mortgage.getAddress())
                .mortgageNumber(mortgage.getMortgageNumber())
                .mortgageNamedAfter(String.join(", ", mortgageNameAft))
                .mortgageLenderName(mortgage.getMortgageLenderName());
        }
        if (home.getLandlords() != null && home.getLandlords().getMortgageNamedAfterList() != null) {
            Landlord landlord = home.getLandlords();

            List<String> landlordNamedAft = landlord.getMortgageNamedAfterList().stream()
                .map(MortgageNamedAfterEnum::getDisplayedValue)
                .toList();

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
                .map(Element::getValue).toList();
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

    public List<Element<ChildAndCafcassOfficer>> prePopulateRevisedChildAndCafcassOfficerDetails(CaseData caseData) {
        List<Element<ChildAndCafcassOfficer>> childAndCafcassOfficers = new ArrayList<>();
        if (caseData.getNewChildDetails() != null) {
            caseData.getNewChildDetails().stream().forEach(childElement -> {
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
