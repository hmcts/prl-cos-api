package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.enums.MiamChildProtectionConcernChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamOtherGroundsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamPreviousAttendanceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChildDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseNoteDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Applicant;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.CaseNotes;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.AttendingTheHearing;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.ChildDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.HearingUrgency;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.InternationalElement;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.LitigationCapacity;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Miam;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.MiamExemptions;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Order;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.OtherPersonInTheCase;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.OtherProceedingsDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Respondent;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.TypeOfApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.WelshLanguageRequirements;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm.AllegationsOfHarmOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm.AllegationsOfHarmOtherConcerns;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm.AllegationsOfHarmOverview;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm.ChildAbductionDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm.DomesticAbuseVictim;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.TabService;
import uk.gov.hmcts.reform.prl.services.tab.summary.generator.FieldGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.THIS_INFORMATION_IS_CONFIDENTIAL;

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
        applicationTab.put("hearingUrgencyTable", getHearingUrgencyTable(caseData));
        applicationTab.put("applicantTable", getApplicantsTable(caseData));
        //applicationTab.put("addCaseNoteTable", getAddCaseNotesTable(caseData));
        applicationTab.put("respondentTable", getRespondentsTable(caseData));
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

        return applicationTab;
    }

    @Override
    public List<FieldGenerator> getGenerators() {
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

    private ChildDetails mapChildDetails(Child child) {

        List<OtherPersonWhoLivesWithChild> otherPersonList = child.getPersonWhoLivesWithChild().stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<Element<OtherPersonWhoLivesWithChildDetails>> otherPersonLiving = new ArrayList<>();
        for (OtherPersonWhoLivesWithChild otherPersonWhoLivesWithChild : otherPersonList) {
            otherPersonLiving.add(Element.<OtherPersonWhoLivesWithChildDetails>builder()
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
                                       : otherPersonWhoLivesWithChild.getAddress()).build()).build());
        }
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
            .build();
    }

    public Map<String, Object> toMap(Object object) {
        return objectMapper.convertValue(object, Map.class);
    }

    public List<Element<Applicant>> getApplicantsTable(CaseData caseData) {
        List<Element<Applicant>> applicants = new ArrayList<>();
        Optional<List<Element<PartyDetails>>> checkApplicants = ofNullable(caseData.getApplicants());

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

    public List<Element<CaseNotes>> getAddCaseNotesTable(CaseData caseData) {
        List<Element<CaseNotes>> caseNotes = new ArrayList<>();
        Optional<List<Element<CaseNoteDetails>>> checkCaseNotes = ofNullable(caseData.getCaseNotes());

        if (checkCaseNotes.isEmpty()) {
            CaseNotes a = CaseNotes.builder().build();
            Element<CaseNotes> app = Element.<CaseNotes>builder().value(a).build();
            caseNotes.add(app);
            return caseNotes;
        }
        List<CaseNoteDetails> currentCaseNotes = caseData.getCaseNotes().stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        for (CaseNoteDetails caseNote : currentCaseNotes) {
            CaseNotes a = objectMapper.convertValue(caseNote, CaseNotes.class);
            Element<CaseNotes> app = Element.<CaseNotes>builder().value(a).build();
            caseNotes.add(app);
        }
        return caseNotes;
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

        String declarationText = "I understand that proceedings for contempt of court may be brought"
            + " against anyone who makes, or causes to be made, a false statement in a document verified"
            + " by a statement of truth without an honest belief in its truth. The applicant believes "
            + "that the facts stated in this form and any continuation sheets are true. " + solicitor
            + " is authorised by the applicant to sign this statement.";

        declarationMap.put("declarationText", declarationText);
        declarationMap.put("agreedBy", solicitor);

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
        Optional<List<MiamExemptionsChecklistEnum>> miamExemptionsCheck = ofNullable(caseData.getMiamExemptionsChecklist());
        String reasonsForMiamExemption;
        if (miamExemptionsCheck.isPresent()) {
            reasonsForMiamExemption = caseData.getMiamExemptionsChecklist()
                .stream().map(MiamExemptionsChecklistEnum::getDisplayedValue).collect(Collectors.joining(", "));
        } else {
            reasonsForMiamExemption = "";
        }

        String domesticViolenceEvidence;
        Optional<List<MiamDomesticViolenceChecklistEnum>> domesticViolenceCheck = ofNullable(caseData.getMiamDomesticViolenceChecklist());
        if (domesticViolenceCheck.isPresent()) {
            domesticViolenceEvidence = caseData.getMiamDomesticViolenceChecklist()
                .stream().map(MiamDomesticViolenceChecklistEnum::getDisplayedValue)
                .collect(Collectors.joining("\n"));
        } else {
            domesticViolenceEvidence = "";
        }

        String urgencyEvidence;
        Optional<List<MiamUrgencyReasonChecklistEnum>> urgencyCheck = ofNullable(caseData.getMiamUrgencyReasonChecklist());
        if (urgencyCheck.isPresent()) {
            urgencyEvidence = caseData.getMiamUrgencyReasonChecklist()
                .stream().map(MiamUrgencyReasonChecklistEnum::getDisplayedValue)
                .collect(Collectors.joining("\n"));
        } else {
            urgencyEvidence = "";
        }

        String previousAttendenceEvidence;
        Optional<MiamPreviousAttendanceChecklistEnum> prevCheck = ofNullable(caseData.getMiamPreviousAttendanceChecklist());
        if (prevCheck.isPresent()) {
            previousAttendenceEvidence = caseData.getMiamPreviousAttendanceChecklist().getDisplayedValue();
        } else {
            previousAttendenceEvidence = "";
        }

        String otherGroundsEvidence;
        Optional<MiamOtherGroundsChecklistEnum> othCheck = ofNullable(caseData.getMiamOtherGroundsChecklist());
        if (othCheck.isPresent()) {
            otherGroundsEvidence = caseData.getMiamOtherGroundsChecklist().getDisplayedValue();
        } else {
            otherGroundsEvidence = "";
        }

        String childEvidence;
        Optional<List<MiamChildProtectionConcernChecklistEnum>> childCheck = ofNullable(caseData
                                                                                            .getMiamChildProtectionConcernList());
        if (childCheck.isPresent()) {
            childEvidence = caseData.getMiamChildProtectionConcernList()
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
                "previousOrOngoingProceedings",
                caseData.getPreviousOrOngoingProceedingsForChildren().getDisplayedValue()
            );
        }
        return Collections.singletonMap("previousOrOngoingProceedings", "");
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
                .dateIssued(caseData.getOrdersNonMolestationDateIssued())
                .endDate(caseData.getOrdersNonMolestationEndDate())
                .orderCurrent(caseData.getOrdersNonMolestationCurrent())
                .courtName(caseData.getOrdersNonMolestationCourtName())
                .build();
            allegationsOfHarmOrders.setNonMolestationOrder(nonMolOrder);
        }

        Optional<YesOrNo> occYesNo = ofNullable(allegationsOfHarmOrders.getOrdersOccupation());
        if (occYesNo.isPresent() && occYesNo.get().equals(YesOrNo.Yes)) {
            Order occOrder = Order.builder()
                .dateIssued(caseData.getOrdersOccupationDateIssued())
                .endDate(caseData.getOrdersOccupationEndDate())
                .orderCurrent(caseData.getOrdersOccupationCurrent())
                .courtName(caseData.getOrdersOccupationCourtName())
                .build();
            allegationsOfHarmOrders.setOccupationOrder(occOrder);
        }

        Optional<YesOrNo> forcedYesNo = ofNullable(allegationsOfHarmOrders.getOrdersForcedMarriageProtection());
        if (forcedYesNo.isPresent() && forcedYesNo.get().equals(YesOrNo.Yes)) {
            Order forOrder = Order.builder()
                .dateIssued(caseData.getOrdersForcedMarriageProtectionDateIssued())
                .endDate(caseData.getOrdersForcedMarriageProtectionEndDate())
                .orderCurrent(caseData.getOrdersForcedMarriageProtectionCurrent())
                .courtName(caseData.getOrdersForcedMarriageProtectionCourtName())
                .build();
            allegationsOfHarmOrders.setForcedMarriageProtectionOrder(forOrder);
        }

        Optional<YesOrNo> resYesNo = ofNullable(allegationsOfHarmOrders.getOrdersRestraining());
        if (resYesNo.isPresent() && resYesNo.get().equals(YesOrNo.Yes)) {
            Order resOrder = Order.builder()
                .dateIssued(caseData.getOrdersRestrainingDateIssued())
                .endDate(caseData.getOrdersRestrainingEndDate())
                .orderCurrent(caseData.getOrdersRestrainingCurrent())
                .courtName(caseData.getOrdersRestrainingCourtName())
                .build();
            allegationsOfHarmOrders.setRestrainingOrder(resOrder);
        }

        Optional<YesOrNo> othYesNo = ofNullable(allegationsOfHarmOrders.getOrdersOtherInjunctive());
        if (othYesNo.isPresent() && othYesNo.get().equals(YesOrNo.Yes)) {
            Order othOrder = Order.builder()
                .dateIssued(caseData.getOrdersOtherInjunctiveDateIssued())
                .endDate(caseData.getOrdersOtherInjunctiveEndDate())
                .orderCurrent(caseData.getOrdersOtherInjunctiveCurrent())
                .courtName(caseData.getOrdersOtherInjunctiveCourtName())
                .build();
            allegationsOfHarmOrders.setOtherInjunctiveOrder(othOrder);
        }

        Optional<YesOrNo> undYesNo = ofNullable(allegationsOfHarmOrders.getOrdersUndertakingInPlace());
        if (undYesNo.isPresent() && undYesNo.get().equals(YesOrNo.Yes)) {
            Order undOrder = Order.builder()
                .dateIssued(caseData.getOrdersUndertakingInPlaceDateIssued())
                .endDate(caseData.getOrdersUndertakingInPlaceEndDate())
                .orderCurrent(caseData.getOrdersUndertakingInPlaceCurrent())
                .courtName(caseData.getOrdersUndertakingInPlaceCourtName())
                .build();
            allegationsOfHarmOrders.setUndertakingInPlaceOrder(undOrder);
        }

        return allegationsOfHarmOrders;
    }

    public Map<String, Object> getDomesticAbuseTable(CaseData caseData) {

        Optional<List<ApplicantOrChildren>> physVictm = ofNullable(caseData.getPhysicalAbuseVictim());
        String physVictimString = "";
        if (physVictm.isPresent()) {
            physVictimString = caseData.getPhysicalAbuseVictim().stream()
                .map(ApplicantOrChildren::getDisplayedValue)
                .collect(Collectors.joining(", "));
        }

        Optional<List<ApplicantOrChildren>> emoVictim = ofNullable(caseData.getEmotionalAbuseVictim());
        String emoVictimString = "";
        if (emoVictim.isPresent()) {
            emoVictimString = caseData.getEmotionalAbuseVictim().stream()
                .map(ApplicantOrChildren::getDisplayedValue)
                .collect(Collectors.joining(", "));
        }

        Optional<List<ApplicantOrChildren>> psyVictim = ofNullable(caseData.getPsychologicalAbuseVictim());
        String psyVictimString = "";
        if (psyVictim.isPresent()) {
            psyVictimString = caseData.getPhysicalAbuseVictim().stream()
                .map(ApplicantOrChildren::getDisplayedValue)
                .collect(Collectors.joining(", "));
        }

        Optional<List<ApplicantOrChildren>> sexVictim = ofNullable(caseData.getSexualAbuseVictim());
        String sexVictimString = "";
        if (sexVictim.isPresent()) {
            sexVictimString = caseData.getSexualAbuseVictim().stream()
                .map(ApplicantOrChildren::getDisplayedValue)
                .collect(Collectors.joining(", "));
        }

        Optional<List<ApplicantOrChildren>> finVictim = ofNullable(caseData.getFinancialAbuseVictim());
        String finVictimString = "";
        if (finVictim.isPresent()) {
            finVictimString = caseData.getPhysicalAbuseVictim().stream()
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

        if (otherPeopleCheck.isEmpty()) {
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

}
