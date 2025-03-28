package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamDomesticAbuseChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamOtherGroundsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPolicyUpgradeChildProtectionConcernEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPreviousAttendanceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.TypeOfMiamAttendanceEvidenceEnum;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingInformation;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.manageorder.CaseOrder;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.mpuChildProtectionConcern;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.mpuDomesticAbuse;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.mpuOther;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.mpuUrgency;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Builder(toBuilder = true)
public class CafCassCaseData {
    @Setter(AccessLevel.NONE)
    @JsonProperty("submitAndPayDownloadApplicationLink")
    private CafCassDocument submitAndPayDownloadApplicationLink;

    public void setSubmitAndPayDownloadApplicationLink(CafCassDocument submitAndPayDownloadApplicationLink) throws MalformedURLException {
        if (submitAndPayDownloadApplicationLink != null
            && StringUtils.hasText(submitAndPayDownloadApplicationLink.getDocumentUrl())) {
            URL url = new URL(submitAndPayDownloadApplicationLink.getDocumentUrl());
            submitAndPayDownloadApplicationLink.setDocumentId(getDocumentId(url));
            submitAndPayDownloadApplicationLink.setDocumentUrl(null);
        }
        this.submitAndPayDownloadApplicationLink = submitAndPayDownloadApplicationLink;
    }

    @Setter(AccessLevel.NONE)
    @JsonProperty("c8Document")
    private CafCassDocument c8Document;

    public void setC8Document(CafCassDocument c8Document) throws MalformedURLException {
        if (c8Document != null
            && StringUtils.hasText(c8Document.getDocumentUrl())) {
            URL url = new URL(c8Document.getDocumentUrl());
            c8Document.setDocumentId(getDocumentId(url));
            c8Document.setDocumentUrl(null);
        }
        this.c8Document = c8Document;
    }


    @Setter(AccessLevel.NONE)
    @JsonProperty("c1ADocument")
    private CafCassDocument c1ADocument;

    public void setC1ADocument(CafCassDocument c1ADocument) throws MalformedURLException {
        if (c1ADocument != null
            && StringUtils.hasText(c1ADocument.getDocumentUrl())) {
            URL url = new URL(c1ADocument.getDocumentUrl());
            c1ADocument.setDocumentId(getDocumentId(url));
            c1ADocument.setDocumentUrl(null);
        }
        this.c1ADocument = c1ADocument;
    }

    public static String getDocumentId(URL url) {
        String path = url.getPath();
        return path.split("/")[path.split("/").length - 1];
    }

    private String familymanCaseNumber;
    private String dateSubmitted;
    private String issueDate;
    private String caseTypeOfApplication;

    @Setter(AccessLevel.NONE)
    private CafCassDocument draftConsentOrderFile;

    public void setDraftConsentOrderFile(CafCassDocument draftConsentOrderFile) throws MalformedURLException {
        if (draftConsentOrderFile != null
            && StringUtils.hasText(draftConsentOrderFile.getDocumentUrl())) {
            URL url = new URL(draftConsentOrderFile.getDocumentUrl());
            draftConsentOrderFile.setDocumentId(getDocumentId(url));
            draftConsentOrderFile.setDocumentUrl(null);
        }
        this.draftConsentOrderFile = draftConsentOrderFile;
    }

    private ConfidentialDetails confidentialDetails;

    private YesOrNo isInterpreterNeeded;
    private List<Element<InterpreterNeed>> interpreterNeeds;

    private YesNoDontKnow childrenKnownToLocalAuthority;

    public void setOtherDocuments(List<Element<OtherDocuments>> otherDocuments) {
        try {
            if (otherDocuments != null) {
                List<Element<OtherDocuments>> updatedOtherDocumentList = otherDocuments.stream()
                    .map(this:: updateElementDocumentId).toList();
                this.otherDocuments = updatedOtherDocumentList;
            }
        } catch (Exception e) {
            this.otherDocuments = otherDocuments;
        }
    }

    private Element<OtherDocuments> updateElementDocumentId(Element<OtherDocuments> otherDocumentsElement) {
        try {
            if (otherDocumentsElement != null
                && !ObjectUtils.isEmpty(otherDocumentsElement.getValue())
                && !ObjectUtils.isEmpty(otherDocumentsElement.getValue().getDocumentOther())
                && StringUtils.hasText(otherDocumentsElement.getValue().getDocumentOther().getDocumentUrl())) {
                Document documentOther = otherDocumentsElement.getValue().getDocumentOther();
                otherDocumentsElement.getValue().setDocumentOther(documentOther);
            }
        } catch (Exception e) {
            return otherDocumentsElement;
        }
        return otherDocumentsElement;
    }

    @Setter(AccessLevel.NONE)
    private List<Element<OtherDocuments>> otherDocuments;

    public void setFinalDocument(CafCassDocument finalDocument) throws MalformedURLException {
        if (finalDocument != null
            && StringUtils.hasText(finalDocument.getDocumentUrl())) {
            URL url = new URL(finalDocument.getDocumentUrl());
            finalDocument.setDocumentId(getDocumentId(url));
            finalDocument.setDocumentUrl(null);
        }
        this.finalDocument = finalDocument;
    }

    @Setter(AccessLevel.NONE)
    private CafCassDocument finalDocument;

    private List<OrderTypeEnum> ordersApplyingFor;

    public List<Element<Child>> getChildren() {
        if (newChildDetails == null) {
            return List.of(); // safe empty list
        }

        return newChildDetails.stream()
            .map(this::mapToChildElement)
            .toList();
    }

    private Element<Child> mapToChildElement(Element<ChildDetailsCafcass> childElement) {
        ChildDetailsCafcass details = childElement.getValue();

        return Element.<Child>builder()
            .id(childElement.getId())
            .value(
                Child.builder()
                    .firstName(details.getFirstName())
                    .lastName(details.getLastName())
                    .gender(details.getGender())
                    .dateOfBirth(details.getDateOfBirth())
                    .otherGender(details.getOtherGender())
                    .orderAppliedFor(details.getOrderAppliedFor())
                    .parentalResponsibilityDetails(details.getParentalResponsibilityDetails())
                    .whoDoesTheChildLiveWith(resolveChildLivesWith(details))
                    .build()
            )
            .build();
    }

    private WhoDoesTheChildLiveWith resolveChildLivesWith(ChildDetailsCafcass details) {
        if (details.getWhoDoesTheChildLiveWith() == null || partyIdAndPartyTypeMap == null) {
            return null;
        }

        return partyIdAndPartyTypeMap.get(
            details.getWhoDoesTheChildLiveWith().getValue().getCode()
        );
    }

    private List<Element<Child>> children;

    public void setMiamCertificationDocumentUpload1(CafCassDocument miamCertificationDocumentUpload1) throws MalformedURLException {
        this.miamCertificationDocumentUpload1 = processIncomingDocument(miamCertificationDocumentUpload1);
    }

    @Setter(AccessLevel.NONE)
    private CafCassDocument miamCertificationDocumentUpload1;

    private String miamStatus;

    private MiamExemptions miamExemptionsTable;

    private String claimingExemptionMiam;

    private String applicantAttendedMiam;

    private String familyMediatorMiam;

    //New Miam added
    private YesOrNo otherProceedingsMiam;

    private String applicantConsentMiam;

    private String mediatorRegistrationNumber;

    private String familyMediatorServiceName;

    private String soleTraderName;

    public void setMiamTable(Map<String, Object> miamTable) {
        if (miamTable != null) {
            this.claimingExemptionMiam = miamTable.get("claimingExemptionMiam") != null ? miamTable.get(
                "claimingExemptionMiam").toString() : null;
            this.applicantAttendedMiam = miamTable.get("applicantAttendedMiam") != null ? miamTable.get(
                "applicantAttendedMiam").toString() : null;
            this.familyMediatorMiam = miamTable.get("familyMediatorMiam") != null ? miamTable.get("familyMediatorMiam").toString() : null;
        }
    }

    //MIAM policy upgrade changes start

    private YesOrNo mpuChildInvolvedInMiam;

    @Getter(AccessLevel.NONE)
    private YesOrNo mpuApplicantAttendedMiam;

    //TODO: check setter changes another field
    public void setMpuApplicantAttendedMiam(YesOrNo mpuApplicantAttendedMiam) {
        this.applicantAttendedMiam = mpuApplicantAttendedMiam.getDisplayedValue();
    }

    @Getter(AccessLevel.NONE)
    private YesOrNo mpuClaimingExemptionMiam;

    //TODO: check setter changes another field
    public void setMpuClaimingExemptionMiam(YesOrNo mpuClaimingExemptionMiam) {
        this.claimingExemptionMiam = mpuClaimingExemptionMiam.getDisplayedValue();
    }

    @Getter(AccessLevel.NONE)
    private List<MiamExemptionsChecklistEnum> mpuExemptionReasons;


    public void setMpuExemptionReasons(List<MiamExemptionsChecklistEnum> mpuExemptionReasons) {
        final String[] childProtectionEvidence = {""};
        final String[] domesticViolenceEvidence = {""};
        final List<String> reasonsForMiamExemption = new ArrayList<>();
        final String[] otherGroundsEvidence = {""};
        final String[] previousAttendenceEvidence = {""};
        final String[] urgencyEvidence = {""};

        mpuExemptionReasons
            .forEach(
                reasonEnum -> {
                    if (reasonEnum.equals(mpuDomesticAbuse)) {
                        domesticViolenceEvidence[0] = mpuDomesticAbuse.getDisplayedValue();
                        reasonsForMiamExemption.add(mpuDomesticAbuse.getDisplayedValue());
                    } else if (reasonEnum.equals(mpuChildProtectionConcern)) {
                        childProtectionEvidence[0] = mpuChildProtectionConcern.getDisplayedValue();
                        reasonsForMiamExemption.add(mpuChildProtectionConcern.getDisplayedValue());
                    } else if (reasonEnum.equals(mpuUrgency)) {
                        urgencyEvidence[0] = mpuUrgency.getDisplayedValue();
                        reasonsForMiamExemption.add(mpuUrgency.getDisplayedValue());
                    } else if (reasonEnum.equals(mpuPreviousMiamAttendance)) {
                        previousAttendenceEvidence[0] = mpuPreviousMiamAttendance.getDisplayedValue();
                        reasonsForMiamExemption.add(mpuPreviousMiamAttendance.getDisplayedValue());
                    } else if (reasonEnum.equals(mpuOther)) {
                        otherGroundsEvidence[0] = mpuOther.getDisplayedValue();
                        reasonsForMiamExemption.add(mpuOther.getDisplayedValue());
                    }
            }
        );

        this.miamExemptionsTable = MiamExemptions.builder()
            .childProtectionEvidence(childProtectionEvidence[0])
            .domesticViolenceEvidence(domesticViolenceEvidence[0])
            .otherGroundsEvidence(otherGroundsEvidence[0])
            .previousAttendenceEvidence(previousAttendenceEvidence[0])
            .urgencyEvidence(urgencyEvidence[0])
            .reasonsForMiamExemption(String.join(",", reasonsForMiamExemption))
            .build();
    }


    private List<String> miamDomesticAbuseEvidences;

    @Getter(AccessLevel.NONE)
    private List<MiamDomesticAbuseChecklistEnum> mpuDomesticAbuseEvidences;


    public void setMpuDomesticAbuseEvidences(List<MiamDomesticAbuseChecklistEnum> mpuDomesticAbuseEvidences) {
        List<String> updatedMiamDomesticAbuseTypes = new ArrayList<>();
        mpuDomesticAbuseEvidences.stream()
            .forEach(
                miamDomesticAbuseChecklistEnum -> updatedMiamDomesticAbuseTypes.add(miamDomesticAbuseChecklistEnum.getDisplayedValue())
            );
        this.miamDomesticAbuseEvidences = updatedMiamDomesticAbuseTypes;
    }


    private YesOrNo mpuIsDomesticAbuseEvidenceProvided;
    @Setter(AccessLevel.NONE)
    private List<Element<DomesticAbuseEvidenceDocument>> mpuDomesticAbuseEvidenceDocument;

    public void setMpuDomesticAbuseEvidenceDocument(List<Element<DomesticAbuseEvidenceDocument>> mpuDomesticAbuseEvidenceDocument) {
        try {
            if (mpuDomesticAbuseEvidenceDocument != null) {
                List<Element<DomesticAbuseEvidenceDocument>> updatedMpuDocumentList = mpuDomesticAbuseEvidenceDocument.stream()
                    .map(this::updateMpuDocumentId).toList();
                this.mpuDomesticAbuseEvidenceDocument = updatedMpuDocumentList;
            }
        } catch (Exception e) {
            this.mpuDomesticAbuseEvidenceDocument = mpuDomesticAbuseEvidenceDocument;
        }
    }

    private Element<DomesticAbuseEvidenceDocument> updateMpuDocumentId(Element<DomesticAbuseEvidenceDocument> mpuDomesticAbuseEvidenceDocument) {
        try {
            if (mpuDomesticAbuseEvidenceDocument != null
                && !ObjectUtils.isEmpty(mpuDomesticAbuseEvidenceDocument.getValue())
                && !ObjectUtils.isEmpty(mpuDomesticAbuseEvidenceDocument.getValue().getDomesticAbuseDocument())
                && StringUtils.hasText(mpuDomesticAbuseEvidenceDocument.getValue().getDomesticAbuseDocument().getDocumentUrl())) {
                Document documentOther = mpuDomesticAbuseEvidenceDocument.getValue().getDomesticAbuseDocument();
                URL url = new URL(documentOther.getDocumentUrl());
                documentOther.setDocumentId(getDocumentId(url));
                documentOther.setDocumentUrl(null);
                mpuDomesticAbuseEvidenceDocument.getValue().setDomesticAbuseDocument(documentOther);
            }
        } catch (Exception e) {
            return mpuDomesticAbuseEvidenceDocument;
        }
        return mpuDomesticAbuseEvidenceDocument;
    }

    private String mpuNoDomesticAbuseEvidenceReason;


    @Getter(AccessLevel.NONE)
    private MiamUrgencyReasonChecklistEnum mpuUrgencyReason;
    private String miamUrgencyReason;

    //TODO: check setter changes another field
    public void setMpuUrgencyReason(MiamUrgencyReasonChecklistEnum mpuUrgencyReason) {
        this.miamUrgencyReason = mpuUrgencyReason.getDisplayedValue();
    }



    @Getter(AccessLevel.NONE)
    private MiamPreviousAttendanceChecklistEnum mpuPreviousMiamAttendanceReason;
    private String miamPreviousAttendanceReason;

    //TODO: check setter changes another field
    public void setMpuPreviousMiamAttendanceReason(MiamPreviousAttendanceChecklistEnum mpuPreviousMiamAttendanceReason) {
        this.miamPreviousAttendanceReason = mpuPreviousMiamAttendanceReason.getDisplayedValue();
    }




    @Setter(AccessLevel.NONE)
    private CafCassDocument mpuDocFromDisputeResolutionProvider;



    @Getter(AccessLevel.NONE)
    private TypeOfMiamAttendanceEvidenceEnum mpuTypeOfPreviousMiamAttendanceEvidence;
    private String miamTypeOfPreviousAttendanceEvidence;

    //TODO: is this misnamed on purpose?
    public void setMpuTypeOfPreviousMiamAttendanceEvidence(TypeOfMiamAttendanceEvidenceEnum mpuTypeOfPreviousMiamAttendanceEvidence) {
        this.miamTypeOfPreviousAttendanceEvidence = mpuTypeOfPreviousMiamAttendanceEvidence.getDisplayedValue();
    }


    @Setter(AccessLevel.NONE)
    private CafCassDocument mpuCertificateByMediator;
    private String mpuMediatorDetails;



    @Getter(AccessLevel.NONE)
    private MiamOtherGroundsChecklistEnum mpuOtherExemptionReasons;
    private String miamOtherExemptionReasons;

    //TODO: check setter changes another field
    public void setMpuOtherExemptionReasons(MiamOtherGroundsChecklistEnum mpuOtherExemptionReasons) {
        this.miamOtherExemptionReasons = mpuOtherExemptionReasons.getDisplayedValue();
    }


    private String mpuApplicantUnableToAttendMiamReason1;
    private String mpuApplicantUnableToAttendMiamReason2;
    @Setter(AccessLevel.NONE)
    private CafCassDocument miamCertificationDocumentUpload;



    @Getter(AccessLevel.NONE)
    private MiamPolicyUpgradeChildProtectionConcernEnum mpuChildProtectionConcernReason;
    private String miamChildProtectionConcernReason;

    //TODO: check setter changes another field
    public void setMpuChildProtectionConcernReason(MiamPolicyUpgradeChildProtectionConcernEnum mpuChildProtectionConcernReason) {
        this.miamChildProtectionConcernReason = mpuChildProtectionConcernReason.getDisplayedValue();
    }

    public void setMiamCertificationDocumentUpload(CafCassDocument doc) throws MalformedURLException {
        this.miamCertificationDocumentUpload = processIncomingDocument(doc);
        this.miamCertificationDocumentUpload1 = miamCertificationDocumentUpload;

    }

    public void setMpuCertificateByMediator(CafCassDocument doc) throws MalformedURLException {
        this.mpuCertificateByMediator = processIncomingDocument(doc);
    }

    public void setMpuDocFromDisputeResolutionProvider(CafCassDocument doc) throws MalformedURLException {
        this.mpuDocFromDisputeResolutionProvider = processIncomingDocument(doc);
    }

    private CafCassDocument processIncomingDocument(CafCassDocument document) throws MalformedURLException {
        if (document != null && StringUtils.hasText(document.getDocumentUrl())) {
            URL url = new URL(document.getDocumentUrl());
            document.setDocumentId(getDocumentId(url));
            document.setDocumentUrl(null);
        }
        return document;
    }

    //Miam upgrade policy changes end
    @Getter(AccessLevel.NONE)
    private Map<String, Object> miamTable;

    private OrderAppliedFor summaryTabForOrderAppliedFor;

    @Getter(AccessLevel.NONE)
    private Map<String, WhoDoesTheChildLiveWith> partyIdAndPartyTypeMap;


    public void setApplicants(List<Element<ApplicantDetails>> applicants) {
        if (partyIdAndPartyTypeMap == null || partyIdAndPartyTypeMap.isEmpty()) {
            partyIdAndPartyTypeMap = new HashMap<>();
        }

        for (Element<ApplicantDetails> element : applicants) {
            ApplicantDetails value = element.getValue();
            String fullName = Stream.of(value.getFirstName(), value.getLastName())
                .filter(str -> str != null && !str.isBlank())
                .collect(Collectors.joining(" "));

            partyIdAndPartyTypeMap.put(
                String.valueOf(element.getId()),
                WhoDoesTheChildLiveWith.builder()
                    .partyId(String.valueOf(element.getId()))
                    .partyFullName(fullName)
                    .partyType(PartyTypeEnum.APPLICANT)
                    .childAddress(value.getAddress())
                    .build()
            );
        }

        this.applicants = applicants;
    }

    private List<Element<ApplicantDetails>> applicants;

    public void setRespondents(List<Element<ApplicantDetails>> respondents) {
        if (partyIdAndPartyTypeMap == null || partyIdAndPartyTypeMap.isEmpty()) {
            partyIdAndPartyTypeMap = new HashMap<>();
        }

        for (Element<ApplicantDetails> element : respondents) {
            ApplicantDetails value = element.getValue();
            String id = String.valueOf(element.getId());

            String fullName = getFullName(value);

            WhoDoesTheChildLiveWith whoLivesWith = WhoDoesTheChildLiveWith.builder()
                .partyId(id)
                .partyFullName(fullName)
                .partyType(PartyTypeEnum.RESPONDENT)
                .childAddress(value.getAddress())
                .build();

            partyIdAndPartyTypeMap.put(id, whoLivesWith);
        }

        this.respondents = respondents;
    }

    private String getFullName(ApplicantDetails value) {
        return Stream.of(value.getFirstName(), value.getLastName())
            .filter(name -> name != null && !name.trim().isEmpty())
            .collect(Collectors.joining(" "));
    }


    private List<Element<ApplicantDetails>> respondents;

    private List<Element<ApplicantConfidentialityDetails>> applicantsConfidentialDetails;

    private String applicantSolicitorEmailAddress;

    private String solicitorName;

    private String courtEpimsId;

    private String courtTypeId;

    private String courtName;

    private List<Element<OtherPersonInTheCase>> otherPeopleInTheCaseTable;

    public void setOrdersNonMolestationDocument(CafCassDocument ordersNonMolestationDocument) throws MalformedURLException {
        if (ordersNonMolestationDocument != null
            && StringUtils.hasText(ordersNonMolestationDocument.getDocumentUrl())) {
            URL url = new URL(ordersNonMolestationDocument.getDocumentUrl());
            ordersNonMolestationDocument.setDocumentId(getDocumentId(url));
            ordersNonMolestationDocument.setDocumentUrl(null);
        }
        this.ordersNonMolestationDocument = ordersNonMolestationDocument;
    }

    @Setter(AccessLevel.NONE)
    private CafCassDocument ordersNonMolestationDocument;

    public void setHearingOutComeDocument(CafCassDocument hearingOutComeDocument) throws MalformedURLException {
        if (hearingOutComeDocument != null
            && StringUtils.hasText(hearingOutComeDocument.getDocumentUrl())) {
            URL url = new URL(hearingOutComeDocument.getDocumentUrl());
            hearingOutComeDocument.setDocumentId(getDocumentId(url));
            hearingOutComeDocument.setDocumentUrl(null);
        }
        this.hearingOutComeDocument = hearingOutComeDocument;
    }

    @Setter(AccessLevel.NONE)
    private CafCassDocument hearingOutComeDocument;

    private List<Element<ManageOrderCollection>> manageOrderCollection;

    private Hearings hearingData;

    @Setter(AccessLevel.NONE)
    private List<Element<CaseOrder>> orderCollection;

    @Setter(AccessLevel.NONE)
    private CaseManagementLocation caseManagementLocation;


    @Getter(AccessLevel.NONE)
    private List<Element<ApplicantDetails>> otherPartyInTheCaseRevised;

    public void setOtherPartyInTheCaseRevised(List<Element<ApplicantDetails>> otherPartyInTheCaseRevised) {
        if (partyIdAndPartyTypeMap == null) {
            partyIdAndPartyTypeMap = new HashMap<>();
        }

        if (this.otherPeopleInTheCaseTable == null) {
            this.otherPeopleInTheCaseTable = new ArrayList<>();
        }

        if (otherPartyInTheCaseRevised != null) {
            for (Element<ApplicantDetails> el : otherPartyInTheCaseRevised) {
                partyIdAndPartyTypeMap.put(String.valueOf(el.getId()), mapToWhoLivesWith(el));

                otherPeopleInTheCaseTable.add(Element.<OtherPersonInTheCase>builder()
                                                  .id(el.getId())
                                                  .value(mapToOtherPerson(el.getValue()))
                                                  .build());
            }
        }

        this.otherPartyInTheCaseRevised = otherPartyInTheCaseRevised;
    }

    private WhoDoesTheChildLiveWith mapToWhoLivesWith(Element<ApplicantDetails> el) {
        ApplicantDetails val = el.getValue();
        String fullName = getFullName(val);

        return WhoDoesTheChildLiveWith.builder()
            .partyId(String.valueOf(el.getId()))
            .partyFullName(fullName)
            .partyType(PartyTypeEnum.OTHERPEOPLE)
            .childAddress(val.getAddress())
            .build();
    }

    private OtherPersonInTheCase mapToOtherPerson(ApplicantDetails val) {
        return OtherPersonInTheCase.builder()
            .firstName(val.getFirstName())
            .lastName(val.getLastName())
            .previousName(val.getPreviousName())
            .isDateOfBirthKnown(val.getIsDateOfBirthKnown())
            .dateOfBirth(val.getDateOfBirth())
            .gender(val.getGender() != null ? val.getGender().getDisplayedValue() : null)
            .otherGender(val.getOtherGender())
            .isPlaceOfBirthKnown(val.getIsPlaceOfBirthKnown())
            .placeOfBirth(val.getPlaceOfBirth())
            .isCurrentAddressKnown(val.getIsCurrentAddressKnown())
            .address(val.getAddress())
            .canYouProvideEmailAddress(val.getCanYouProvideEmailAddress())
            .email(val.getEmail())
            .canYouProvidePhoneNumber(val.getCanYouProvidePhoneNumber())
            .phoneNumber(val.getPhoneNumber())
            .build();
    }

    @Getter(AccessLevel.NONE)
    private List<Element<ChildDetailsCafcass>> newChildDetails;


    public List<Element<RelationshipToPartiesCafcass>> getChildAndApplicantRelations() {
        if (childAndApplicantRelations == null) {
            return List.of();
        }

        return childAndApplicantRelations.stream()
            .map(this::mapToApplicantRelationshipElement)
            .toList();
    }

    private Element<RelationshipToPartiesCafcass> mapToApplicantRelationshipElement(
        Element<RelationshipToPartiesCafcass> element) {
        RelationshipToPartiesCafcass rel = element.getValue();

        return Element.<RelationshipToPartiesCafcass>builder()
            .id(element.getId())
            .value(RelationshipToPartiesCafcass.builder()
                       .partyId(rel.getApplicantId())
                       .partyFullName(rel.getApplicantFullName())
                       .partyType(PartyTypeEnum.APPLICANT)
                       .childId(rel.getChildId())
                       .childFullName(rel.getChildFullName())
                       .relationType(rel.getChildAndApplicantRelation())
                       .otherRelationDetails(rel.getChildAndApplicantRelationOtherDetails())
                       .childLivesWith(rel.getChildLivesWith())
                       .build())
            .build();
    }


    private List<Element<RelationshipToPartiesCafcass>> childAndApplicantRelations;

    public List<Element<RelationshipToPartiesCafcass>> getChildAndRespondentRelations() {
        if (childAndRespondentRelations == null) {
            return List.of();
        }

        return childAndRespondentRelations.stream()
            .map(this::mapToRespondentRelationshipElement)
            .toList();
    }

    private Element<RelationshipToPartiesCafcass> mapToRespondentRelationshipElement(
        Element<RelationshipToPartiesCafcass> element) {
        RelationshipToPartiesCafcass rel = element.getValue();

        return Element.<RelationshipToPartiesCafcass>builder()
            .id(element.getId())
            .value(RelationshipToPartiesCafcass.builder()
                       .partyId(rel.getRespondentId())
                       .partyFullName(rel.getRespondentFullName())
                       .partyType(PartyTypeEnum.RESPONDENT)
                       .childId(rel.getChildId())
                       .childFullName(rel.getChildFullName())
                       .relationType(rel.getChildAndRespondentRelation())
                       .otherRelationDetails(rel.getChildAndRespondentRelationOtherDetails())
                       .childLivesWith(rel.getChildLivesWith())
                       .build())
            .build();
    }


    public List<Element<RelationshipToPartiesCafcass>> getChildAndOtherPeopleRelations() {
        if (childAndOtherPeopleRelations == null) {
            return List.of();
        }

        return childAndOtherPeopleRelations.stream()
            .map(this::mapToOtherPeopleRelationshipElement)
            .toList();
    }

    private Element<RelationshipToPartiesCafcass> mapToOtherPeopleRelationshipElement(
        Element<RelationshipToPartiesCafcass> element ) {
        RelationshipToPartiesCafcass rel = element.getValue();

        return Element.<RelationshipToPartiesCafcass>builder()
            .id(element.getId())
            .value(RelationshipToPartiesCafcass.builder()
                       .partyId(rel.getOtherPeopleId())
                       .partyFullName(rel.getOtherPeopleFullName())
                       .partyType(PartyTypeEnum.OTHERPEOPLE)
                       .childId(rel.getChildId())
                       .childFullName(rel.getChildFullName())
                       .relationType(rel.getChildAndOtherPeopleRelation())
                       .otherRelationDetails(rel.getChildAndOtherPeopleRelationOtherDetails())
                       .childLivesWith(rel.getChildLivesWith())
                       .build())
            .build();
    }

    private List<Element<RelationshipToPartiesCafcass>> childAndRespondentRelations;

    private List<Element<RelationshipToPartiesCafcass>> childAndOtherPeopleRelations;

    private List<uk.gov.hmcts.reform.prl.models.Element<UploadedDocuments>> cafcassUploadedDocs;

    private List<uk.gov.hmcts.reform.prl.models.documents.Document> c8FormDocumentsUploaded;

    private BundlingInformation bundleInformation;

    private List<uk.gov.hmcts.reform.prl.models.documents.Document> otherDocumentsUploaded;

    private uk.gov.hmcts.reform.prl.models.documents.Document uploadOrderDoc;

    private List<uk.gov.hmcts.reform.prl.models.Element<QuarantineLegalDoc>> courtStaffUploadDocListDocTab;
    private List<uk.gov.hmcts.reform.prl.models.Element<QuarantineLegalDoc>> legalProfUploadDocListDocTab;
    private List<uk.gov.hmcts.reform.prl.models.Element<QuarantineLegalDoc>> cafcassUploadDocListDocTab;
    private List<uk.gov.hmcts.reform.prl.models.Element<QuarantineLegalDoc>> bulkScannedDocListDocTab;
    private List<uk.gov.hmcts.reform.prl.models.Element<QuarantineLegalDoc>> citizenUploadedDocListDocTab;
    private List<uk.gov.hmcts.reform.prl.models.Element<QuarantineLegalDoc>> restrictedDocuments;
    private List<uk.gov.hmcts.reform.prl.models.Element<QuarantineLegalDoc>> confidentialDocuments;


    private List<uk.gov.hmcts.reform.prl.models.Element<ResponseDocuments>> respondentAc8Documents;
    private List<uk.gov.hmcts.reform.prl.models.Element<ResponseDocuments>> respondentBc8Documents;
    private List<uk.gov.hmcts.reform.prl.models.Element<ResponseDocuments>> respondentCc8Documents;
    private List<uk.gov.hmcts.reform.prl.models.Element<ResponseDocuments>> respondentDc8Documents;
    private List<uk.gov.hmcts.reform.prl.models.Element<ResponseDocuments>> respondentEc8Documents;

    private uk.gov.hmcts.reform.prl.models.documents.Document specialArrangementsLetter;
    private uk.gov.hmcts.reform.prl.models.documents.Document additionalDocuments;
    private List<uk.gov.hmcts.reform.prl.models.Element<uk.gov.hmcts.reform.prl.models.documents.Document>> additionalDocumentsList;


    private List<uk.gov.hmcts.reform.prl.models.Element<StmtOfServiceAddRecipient>> stmtOfServiceAddRecipient;
    private List<uk.gov.hmcts.reform.prl.models.Element<StmtOfServiceAddRecipient>> stmtOfServiceForOrder;
    private List<uk.gov.hmcts.reform.prl.models.Element<StmtOfServiceAddRecipient>> stmtOfServiceForApplication;

}
