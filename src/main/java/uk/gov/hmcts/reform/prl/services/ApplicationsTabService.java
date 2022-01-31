package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren;
import uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Applicant;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.AttendingTheHearing;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.ChildCondensed;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.OrchestrationConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.OrchestrationConstants.JURISDICTION;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationsTabService {

    @Autowired
    CoreCaseDataService coreCaseDataService;

    @Autowired
    ObjectMapper objectMapper;

    public void updateApplicationTabData(CaseData caseData) {

        Map<String, Object> applicationTab = new HashMap<>();
        applicationTab.put("hearingUrgencyTable", getHearingUrgencyTable(caseData));
        applicationTab.put("applicantTable", getApplicantsTable(caseData));
        applicationTab.put("respondentTable", getRespondentsTable(caseData));
        applicationTab.put("declarationTable",getDeclarationTable(caseData));
        applicationTab.put("typeOfApplicationTable",getTypeOfApplicationTable(caseData));
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
        applicationTab.put("childTable", getChildrenTable(caseData));


        coreCaseDataService.triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            "internal-update-application-tab",
            applicationTab
        );
    }


    private Map<String, Object> toMap(Object object) {
        return objectMapper.convertValue(object, Map.class);
    }


    public List<Element<Applicant>> getApplicantsTable(CaseData caseData) {
        List<Element<Applicant>> applicants = new ArrayList<>();
        Optional<List<Element<PartyDetails>>> checkApplicants = ofNullable(caseData.getApplicants());

        if (checkApplicants.isEmpty()) {
            return applicants;
        }

        List<PartyDetails> currentApplicants = caseData.getApplicants().stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        for (PartyDetails applicant : currentApplicants) {
            Applicant a = objectMapper.convertValue(applicant, Applicant.class);
            Element<Applicant> app = Element.<Applicant>builder().value(a).build();
            applicants.add(app);
        }
        return applicants;
    }

    public List<Element<Respondent>> getRespondentsTable(CaseData caseData) {
        List<Element<Respondent>> respondents = new ArrayList<>();
        Optional<List<Element<PartyDetails>>> checkRespondents = ofNullable(caseData.getApplicants());

        if (checkRespondents.isEmpty()) {
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

        //TODO: fetch solicitor name from idam

        String declarationText = "I understand that proceedings for contempt of court may be brought"
            + " against anyone who makes, or causes to be made, a false statement in a document verified"
            + " by a statement of truth without an honest belief in its truth. The applicant believes "
            + "that the facts stated in this form and any continuation sheets are true. [Solicitor Name] "
            + "is authorised by the applicant to sign this statement.";

        declarationMap.put("declarationText", declarationText);
        declarationMap.put("agreedBy", "<Solicitor name>");

        return declarationMap;
    }

    private Map<String, Object> getHearingUrgencyTable(CaseData caseData) {
        HearingUrgency hearingUrgency = objectMapper.convertValue(caseData, HearingUrgency.class);
        return toMap(hearingUrgency);
    }

    private List<Element<ChildCondensed>> getChildrenTable(CaseData caseData) {
        List<Element<ChildCondensed>> childrenMapped = new ArrayList<>();
        List<Child> children = caseData.getChildren().stream().map(Element::getValue).collect(Collectors.toList());

        for (Child c : children) {

            ChildCondensed ch = ChildCondensed.builder()
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .dateOfBirth(c.getDateOfBirth())
                .gender(c.getGender())
                .otherGender(c.getOtherGender())
                .applicantsRelationshipToChild(c.getApplicantsRelationshipToChild())
                .respondentsRelationshipToChild(c.getRespondentsRelationshipToChild())
                .childLiveWith(c.getChildLiveWith())
                .personWhoLivesWithChild(c.getPersonWhoLivesWithChild())
                .parentalResponsibilityDetails(c.getParentalResponsibilityDetails())
                .build();

            Element<ChildCondensed> kid = Element.<ChildCondensed>builder().value(ch).build();
            childrenMapped.add(kid);

        }
        return childrenMapped;

    }


    private Map<String, Object> getTypeOfApplicationTable(CaseData caseData) {

        Optional<List<OrderTypeEnum>> checkOrders = ofNullable(caseData.getOrdersApplyingFor());

        if (checkOrders.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> ordersApplyingFor = caseData.getOrdersApplyingFor().stream()
            .map(OrderTypeEnum::getDisplayedValue)
            .collect(Collectors.toList());

        String typeOfChildArrangementsOrder = caseData.getTypeOfChildArrangementsOrder().getDisplayedValue();
        String natureOfOrder = caseData.getNatureOfOrder();

        TypeOfApplication typeOfApplication = TypeOfApplication.builder()
            .ordersApplyingFor(String.join(", ", ordersApplyingFor))
            .typeOfChildArrangementsOrder(typeOfChildArrangementsOrder)
            .natureOfOrder(natureOfOrder)
            .build();

        return toMap(typeOfApplication);
    }

    private Map<String, Object> getAllegationsOfHarmOverviewTable(CaseData caseData) {
        AllegationsOfHarmOverview allegationsOfHarmOverview = objectMapper
            .convertValue(caseData, AllegationsOfHarmOverview.class);

        return toMap(allegationsOfHarmOverview);

    }

    private Map<String, Object> getMiamTable(CaseData caseData) {
        Miam miam = objectMapper.convertValue(caseData, Miam.class);
        return toMap(miam);
    }

    private Map<String, Object> getMiamExemptionsTable(CaseData caseData) {
        //check if first screen is empty and return empty map if true
        Optional<List<MiamExemptionsChecklistEnum>> miamExemptionsCheck = ofNullable(caseData.getMiamExemptionsChecklist());

        if (miamExemptionsCheck.isEmpty()) {
            return Collections.emptyMap();
        }

        //TODO: add null checks
        String reasonsForMiamExemption = caseData.getMiamExemptionsChecklist()
            .stream().map(MiamExemptionsChecklistEnum::getDisplayedValue).collect(Collectors.joining(", "));

        String domesticViolenceEvidence = caseData.getMiamDomesticViolenceChecklist()
            .stream().map(MiamDomesticViolenceChecklistEnum::getDisplayedValue)
            .collect(Collectors.joining("\n"));

        String urgencyEvidence = caseData.getMiamUrgencyReasonChecklist()
            .stream().map(MiamUrgencyReasonChecklistEnum::getDisplayedValue)
            .collect(Collectors.joining("\n"));

        String previousAttendenceEvidence = caseData.getMiamPreviousAttendanceChecklist().getDisplayedValue();
        String otherGroundsEvidence = caseData.getMiamOtherGroundsChecklist().getDisplayedValue();

        MiamExemptions miamExemptions = MiamExemptions.builder()
            .reasonsForMiamExemption(reasonsForMiamExemption)
            .domesticViolenceEvidence(domesticViolenceEvidence)
            .urgencyEvidence(urgencyEvidence)
            .previousAttendenceEvidence(previousAttendenceEvidence)
            .otherGroundsEvidence(otherGroundsEvidence)
            .build();

        return toMap(miamExemptions);

    }

    private Map<String, Object> getOtherProceedingsTable(CaseData caseData) {
        return Collections.singletonMap("previousOrOngoingProceedings",
                                        caseData.getPreviousOrOngoingProceedingsForChildren().getDisplayedValue());
    }



    private List<Element<OtherProceedingsDetails>> getOtherProceedingsDetailsTable(CaseData caseData) {

        Optional<List<Element<ProceedingDetails>>> proceedingsCheck = ofNullable(caseData.getExistingProceedings());

        if (proceedingsCheck.isEmpty()) {
            return Collections.emptyList();
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

    private Map<String, Object> getInternationalElementTable(CaseData caseData) {
        InternationalElement internationalElement = objectMapper.convertValue(caseData, InternationalElement.class);
        return toMap(internationalElement);
    }

    private Map<String, Object> getAttendingTheHearingTable(CaseData caseData) {
        AttendingTheHearing attendingTheHearing = objectMapper.convertValue(caseData, AttendingTheHearing.class);
        return toMap(attendingTheHearing);
    }

    private Map<String, Object> getLitigationCapacityDetails(CaseData caseData) {
        LitigationCapacity litigationCapacity = objectMapper.convertValue(caseData, LitigationCapacity.class);
        return toMap(litigationCapacity);
    }

    private Map<String, Object> getWelshLanguageRequirementsTable(CaseData caseData) {
        WelshLanguageRequirements welshLanguageRequirements = objectMapper
            .convertValue(caseData, WelshLanguageRequirements.class);
        return toMap(welshLanguageRequirements);
    }

    private Map<String, Object> getAllegationsOfHarmOrdersTable(CaseData caseData) {
        AllegationsOfHarmOrders allegationsOfHarmOrders = objectMapper
            .convertValue(caseData, AllegationsOfHarmOrders.class);

        getSpecificOrderDetails(allegationsOfHarmOrders, caseData);
        return toMap(allegationsOfHarmOrders);
    }

    private AllegationsOfHarmOrders getSpecificOrderDetails(AllegationsOfHarmOrders allegationsOfHarmOrders, CaseData caseData) {
        if (allegationsOfHarmOrders.getOrdersNonMolestation().equals(YesOrNo.Yes)) {
            Order nonMolOrder = Order.builder()
                .dateIssued(caseData.getOrdersNonMolestationDateIssued())
                .endDate(caseData.getOrdersNonMolestationEndDate())
                .orderCurrent(caseData.getOrdersNonMolestationCurrent())
                .courtName(caseData.getOrdersNonMolestationCourtName())
                .build();
            allegationsOfHarmOrders.setNonMolestationOrder(nonMolOrder);
        }

        if (allegationsOfHarmOrders.getOrdersOccupation().equals(YesOrNo.Yes)) {
            Order occOrder = Order.builder()
                .dateIssued(caseData.getOrdersOccupationDateIssued())
                .endDate(caseData.getOrdersOccupationEndDate())
                .orderCurrent(caseData.getOrdersOccupationCurrent())
                .courtName(caseData.getOrdersOccupationCourtName())
                .build();
            allegationsOfHarmOrders.setOccupationOrder(occOrder);
        }

        if (allegationsOfHarmOrders.getOrdersForcedMarriageProtection().equals(YesOrNo.Yes)) {
            Order forOrder = Order.builder()
                .dateIssued(caseData.getOrdersForcedMarriageProtectionDateIssued())
                .endDate(caseData.getOrdersForcedMarriageProtectionEndDate())
                .orderCurrent(caseData.getOrdersForcedMarriageProtectionCurrent())
                .courtName(caseData.getOrdersForcedMarriageProtectionCourtName())
                .build();
            allegationsOfHarmOrders.setForcedMarriageOrder(forOrder);
        }

        if (allegationsOfHarmOrders.getOrdersRestraining().equals(YesOrNo.Yes)) {
            Order resOrder = Order.builder()
                .dateIssued(caseData.getOrdersRestrainingDateIssued())
                .endDate(caseData.getOrdersRestrainingEndDate())
                .orderCurrent(caseData.getOrdersRestrainingCurrent())
                .courtName(caseData.getOrdersRestrainingCourtName())
                .build();
            allegationsOfHarmOrders.setRestrainingOrder(resOrder);
        }

        if (allegationsOfHarmOrders.getOrdersOtherInjunctive().equals(YesOrNo.Yes)) {
            Order othOrder = Order.builder()
                .dateIssued(caseData.getOrdersOtherInjunctiveDateIssued())
                .endDate(caseData.getOrdersOtherInjunctiveEndDate())
                .orderCurrent(caseData.getOrdersOtherInjunctiveCurrent())
                .courtName(caseData.getOrdersOtherInjunctiveCourtName())
                .build();
            allegationsOfHarmOrders.setOtherInjunctiveOrder(othOrder);
        }

        if (allegationsOfHarmOrders.getOrdersUndertakingInPlace().equals(YesOrNo.Yes)) {
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

    private Map<String, Object> getDomesticAbuseTable(CaseData caseData) {



        DomesticAbuseVictim domesticAbuseVictim = DomesticAbuseVictim.builder()
            .physicalAbuseVictim(caseData.getPhysicalAbuseVictim().stream()
                                     .map(ApplicantOrChildren::getDisplayedValue)
                                     .collect(Collectors.joining(", ")))
            .emotionalAbuseVictim(caseData.getEmotionalAbuseVictim().stream()
                                      .map(ApplicantOrChildren::getDisplayedValue)
                                      .collect(Collectors.joining(", ")))
            .psychologicalAbuseVictim(caseData.getPsychologicalAbuseVictim().stream()
                                          .map(ApplicantOrChildren::getDisplayedValue)
                                          .collect(Collectors.joining(", ")))
            .sexualAbuseVictim(caseData.getSexualAbuseVictim().stream()
                                   .map(ApplicantOrChildren::getDisplayedValue)
                                   .collect(Collectors.joining(", ")))
            .financialAbuseVictim(caseData.getFinancialAbuseVictim().stream()
                                      .map(ApplicantOrChildren::getDisplayedValue)
                                      .collect(Collectors.joining(", ")))
            .build();


        return toMap(domesticAbuseVictim);

    }

    private Map<String, Object> getChildAbductionTable(CaseData caseData) {
        ChildAbductionDetails childAbductionDetails = objectMapper.convertValue(caseData, ChildAbductionDetails.class);
        return toMap(childAbductionDetails);
    }


    private Map<String, Object> getAllegationsOfHarmOtherConcerns(CaseData caseData) {
        AllegationsOfHarmOtherConcerns allegationsOfHarmOtherConcerns = objectMapper
            .convertValue(caseData, AllegationsOfHarmOtherConcerns.class);
        return toMap(allegationsOfHarmOtherConcerns);
    }

    private List<Element<OtherPersonInTheCase>> getOtherPeopleInTheCaseTable(CaseData caseData) {

        Optional<List<Element<PartyDetails>>> otherPeopleCheck = ofNullable(caseData.getOthersToNotify());

        if(otherPeopleCheck.isEmpty()) {
            return Collections.emptyList();
        }

        List<PartyDetails> otherPeople = caseData.getOthersToNotify().stream().map(Element::getValue).collect(Collectors.toList());
        List<Element<OtherPersonInTheCase>> otherPersonsInTheCase = new ArrayList<>();

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


}
