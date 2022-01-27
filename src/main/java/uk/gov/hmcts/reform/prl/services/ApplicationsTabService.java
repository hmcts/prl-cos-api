package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren;
import uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.AllegationsOfHarmOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.AllegationsOfHarmOverview;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Applicant;

import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.AttendingTheHearing;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.DomesticAbuse;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.HearingUrgency;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.InternationalElement;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.LitigationCapacity;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Miam;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.MiamExemptions;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Order;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.OtherProceedingsDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Respondent;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.TypeOfApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.WelshLanguageRequirements;
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

        HearingUrgency hearingUrgency = objectMapper.convertValue(caseData, HearingUrgency.class);

        Map<String, Object> hearingUrgencyMap = toMap(hearingUrgency);

        List<Element<Applicant>> applicants = new ArrayList<>();
        List<PartyDetails> currentApplicants = caseData.getApplicants().stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        for (PartyDetails applicant : currentApplicants) {
            Applicant a = objectMapper.convertValue(applicant, Applicant.class);
            Element<Applicant> app = Element.<Applicant>builder().value(a).build();
            applicants.add(app);
        }

        List<Element<Respondent>> respondents = new ArrayList<>();
        List<PartyDetails> currentRespondents = caseData.getRespondents().stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        for (PartyDetails respondent : currentRespondents) {
            Respondent r = objectMapper.convertValue(respondent, Respondent.class);
            Element<Respondent> res = Element.<Respondent>builder().value(r).build();
            respondents.add(res);
        }


        Map<String, Object> declarationMap = new HashMap<>();
        declarationMap.put("declarationText", "I understand that proceedings for contempt of court may be brought against anyone who makes, or causes to be made, a false statement in a document verified by a statement of truth without an honest belief in its truth. The applicant believes that the facts stated in this form and any continuation sheets are true. [Solicitor Name] is authorised by the applicant to sign this statement.");
        declarationMap.put("agreedBy", "<Solicitor name>");


        coreCaseDataService.triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            "internal-update-application-tab",
            Map.of("hearingUrgencyTable", hearingUrgencyMap,
                   "applicantTable", applicants,
                   "respondentTable", respondents,
                   "declarationTable",declarationMap,
                   "typeOfApplicationTable",getTypeOfApplicationTable(caseData),
                   "allegationsOfHarmOverviewTable", getAllegationsOfHarmOverviewTable(caseData),
                   "miamTable", getMiamTable(caseData),
                   "miamExemptionsTable", getMiamExemptionsTable(caseData),
                   "otherProceedingsTable", getOtherProceedingsTable(caseData),
                   "otherProceedingsDetailsTable", getOtherProceedingsDetailsTable(caseData))
            );

        coreCaseDataService.triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            "internal-update-application-tab",
            Map.of("internationalElementTable", getInternationalElementTable(caseData),
                   "attendingTheHearingTable", getAttendingTheHearingTable(caseData),
                   "litigationCapacityTable", getLitigationCapacityDetails(caseData),
                   "welshLanguageRequirementsTable", getWelshLanguageRequirementsTable(caseData),
                   "allegationsOfHarmOrdersTable", getAllegationsOfHarmOrdersTable(caseData))
//                   "allegationsOfHarmDomesticAbuseTable", getDomesticAbuseTable(caseData))
        );
    }

    private Map<String, Object> toMap(Object object) {
        return objectMapper.convertValue(object, Map.class);
    }

    private String getAddressString(Address address) {
        Optional<String> firstLine = ofNullable(address.getAddressLine1());
        Optional<String> town = ofNullable(address.getPostTown());
        Optional<String> postCode = ofNullable(address.getPostCode());

        List<Optional<String>> addressFields = new ArrayList<>();
        addressFields.add(firstLine);
        addressFields.add(town);
        addressFields.add(postCode);

        addressFields.removeIf(Optional::isEmpty);
        return addressFields.stream().map(Optional::get).collect(Collectors.joining(","));

    }

    private Map<String, Object> getTypeOfApplicationTable(CaseData caseData) {

        List<String> ordersApplyingFor = caseData.getOrdersApplyingFor().stream()
            .map(OrderTypeEnum::getDisplayedValue)
            .collect(Collectors.toList());

        String typeOfChildArrangementsOrder = caseData.getTypeOfChildArrangementsOrder().getDisplayedValue();
        String natureOfOrder = caseData.getNatureOfOrder();

        TypeOfApplication typeOfApplication = TypeOfApplication.builder()
            .ordersApplyingFor(String.join(",", ordersApplyingFor))
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
            allegationsOfHarmOrders.setNonMolestationOrder(othOrder);
        }

        if (allegationsOfHarmOrders.getOrdersUndertakingInPlace().equals(YesOrNo.Yes)) {
            Order undOrder = Order.builder()
                .dateIssued(caseData.getOrdersUndertakingInPlaceDateIssued())
                .endDate(caseData.getOrdersUndertakingInPlaceEndDate())
                .orderCurrent(caseData.getOrdersUndertakingInPlaceCurrent())
                .courtName(caseData.getOrdersUndertakingInPlaceCourtName())
                .build();
            allegationsOfHarmOrders.setRestrainingOrder(undOrder);
        }

        return allegationsOfHarmOrders;
    }

    private Map<String, Object> getDomesticAbuseTable(CaseData caseData) {

        DomesticAbuse domesticAbuse = DomesticAbuse.builder()
            .physicalAbuseVictim(caseData.getPhysicalAbuseVictim().stream()
                                     .map(ApplicantOrChildren::getDisplayedValue)
                                     .collect(Collectors.joining(",")))
            .emotionalAbuseVictim(caseData.getEmotionalAbuseVictim().stream()
                                      .map(ApplicantOrChildren::getDisplayedValue)
                                      .collect(Collectors.joining(",")))
            .psychologicalAbuseVictim(caseData.getPsychologicalAbuseVictim().stream()
                                          .map(ApplicantOrChildren::getDisplayedValue)
                                          .collect(Collectors.joining(",")))
            .sexualAbuseVictim(caseData.getSexualAbuseVictim().stream()
                                   .map(ApplicantOrChildren::getDisplayedValue)
                                   .collect(Collectors.joining(",")))
            .financialAbuseVictim(caseData.getFinancialAbuseVictim().stream()
                                      .map(ApplicantOrChildren::getDisplayedValue)
                                      .collect(Collectors.joining(",")))
            .build();

        return toMap(domesticAbuse);

    }




}
