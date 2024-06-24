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
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.manageorder.CaseOrder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private String getDocumentId(URL url) {
        String path = url.getPath();
        String documentId = path.split("/")[path.split("/").length - 1];
        return documentId;
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
                    .map(otherDocumentsElement -> updateElementDocumentId(otherDocumentsElement)).toList();
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

    private List<Element<Child>> children;

    public void setMiamCertificationDocumentUpload1(CafCassDocument miamCertificationDocumentUpload1) throws MalformedURLException {
        if (miamCertificationDocumentUpload1 != null
            && StringUtils.hasText(miamCertificationDocumentUpload1.getDocumentUrl())) {
            URL url = new URL(miamCertificationDocumentUpload1.getDocumentUrl());
            miamCertificationDocumentUpload1.setDocumentId(getDocumentId(url));
            miamCertificationDocumentUpload1.setDocumentUrl(null);
        }
        this.miamCertificationDocumentUpload1 = miamCertificationDocumentUpload1;
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

    @Getter(AccessLevel.NONE)
    private Map<String, Object> miamTable;

    private OrderAppliedFor summaryTabForOrderAppliedFor;

    private List<Element<ApplicantDetails>> applicants;

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
    private List<Element<ChildDetailsCafcass>> newChildDetails;

    public void setNewChildDetails(List<Element<ChildDetailsCafcass>> newChildDetails) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        if (newChildDetails != null) {
            newChildDetails.stream().forEach(
                newChildDetail -> {
                    ChildDetailsCafcass childDetailsRevised = newChildDetail.getValue();
                    this.children.add(Element.<Child>builder()
                                          .id(newChildDetail.getId())
                                          .value(Child.builder()
                                                     .firstName(childDetailsRevised.getFirstName())
                                                     .lastName(childDetailsRevised.getLastName())
                                                     .gender(childDetailsRevised.getGender())
                                                     .dateOfBirth(childDetailsRevised.getDateOfBirth())
                                                     .otherGender(childDetailsRevised.getOtherGender())
                                                     .orderAppliedFor(childDetailsRevised.getOrderAppliedFor())
                                                     .parentalResponsibilityDetails(childDetailsRevised.getParentalResponsibilityDetails())
                                                     .build())
                                          .build());

                }
            );
        }

        this.newChildDetails = newChildDetails;
    }

    @Getter(AccessLevel.NONE)
    private List<Element<ApplicantDetails>> otherPartyInTheCaseRevised;

    public void setOtherPartyInTheCaseRevised(List<Element<ApplicantDetails>> otherPartyInTheCaseRevised) {
        if (this.otherPeopleInTheCaseTable == null) {
            this.otherPeopleInTheCaseTable = new ArrayList<>();
        }

        if (otherPartyInTheCaseRevised != null) {
            otherPartyInTheCaseRevised.stream().forEach(
                otherPartyInTheCase -> {
                    ApplicantDetails partyDetails = otherPartyInTheCase.getValue();
                    this.otherPeopleInTheCaseTable.add(Element.<OtherPersonInTheCase>builder()
                                                           .id(otherPartyInTheCase.getId())
                                                           .value(OtherPersonInTheCase.builder()
                                                                      .firstName(partyDetails.getFirstName())
                                                                      .lastName(partyDetails.getLastName())
                                                                      .previousName(partyDetails.getPreviousName())
                                                                      .isDateOfBirthKnown(partyDetails.getIsDateOfBirthKnown())
                                                                      .dateOfBirth(partyDetails.getDateOfBirth())
                                                                      .gender(partyDetails.getGender().getDisplayedValue())
                                                                      .otherGender(partyDetails.getOtherGender())
                                                                      .isPlaceOfBirthKnown(partyDetails.getIsPlaceOfBirthKnown())
                                                                      .isCurrentAddressKnown(partyDetails.getIsCurrentAddressKnown())
                                                                      .address(
                                                                          partyDetails.getAddress() != null
                                                                              ? Address.builder()
                                                                              .addressLine1(partyDetails.getAddress().getAddressLine1())
                                                                              .addressLine2(partyDetails.getAddress().getAddressLine2())
                                                                              .addressLine3(partyDetails.getAddress().getAddressLine3())
                                                                              .country(partyDetails.getAddress().getCountry())
                                                                              .county(partyDetails.getAddress().getCounty())
                                                                              .postCode(partyDetails.getAddress().getPostCode())
                                                                              .postTown(partyDetails.getAddress().getPostTown())
                                                                              .build() : null
                                                                      )
                                                                      .canYouProvideEmailAddress(partyDetails.getCanYouProvideEmailAddress())
                                                                      .email(partyDetails.getEmail())
                                                                      .canYouProvidePhoneNumber(partyDetails.getCanYouProvidePhoneNumber())
                                                                      .phoneNumber(partyDetails.getPhoneNumber())
                                                                      .build())
                                                           .build());
                }
            );
        }
        this.otherPartyInTheCaseRevised = otherPartyInTheCaseRevised;
    }

    public List<Element<RelationshipToPartiesCafcass>> getChildAndApplicantRelations() {
        List<Element<RelationshipToPartiesCafcass>> updatedRelationshipToParties = new ArrayList<>();
        if (this.childAndApplicantRelations != null) {
            this.childAndApplicantRelations.stream()
                .forEach(
                    childAndApplicantRelationsElement -> {
                        RelationshipToPartiesCafcass tempRelationship = childAndApplicantRelationsElement.getValue();
                        updatedRelationshipToParties.add(
                            Element.<RelationshipToPartiesCafcass>builder()
                                .id(childAndApplicantRelationsElement.getId())
                                .value(RelationshipToPartiesCafcass.builder()
                                           .partyId(tempRelationship.getApplicantId())
                                           .partyFullName(tempRelationship.getApplicantFullName())
                                           .partyType(PartyTypeEnum.APPLICANT)
                                           .childId(tempRelationship.getChildId())
                                           .childFullName(tempRelationship.getChildFullName())
                                           .relationType(tempRelationship.getChildAndApplicantRelation())
                                           .otherRelationDetails(tempRelationship.getChildAndApplicantRelationOtherDetails())
                                           .childLivesWith(tempRelationship.getChildLivesWith())
                                           .build())
                                .build()
                        );

                    }
                );
        }
        return updatedRelationshipToParties;
    }

    private List<Element<RelationshipToPartiesCafcass>> childAndApplicantRelations;

    public List<Element<RelationshipToPartiesCafcass>> getChildAndRespondentRelations() {
        List<Element<RelationshipToPartiesCafcass>> updatedRelationshipToParties = new ArrayList<>();
        if (this.childAndRespondentRelations != null) {
            this.childAndRespondentRelations.stream()
                .forEach(
                    childAndApplicantRelationsElement -> {
                        RelationshipToPartiesCafcass tempRelationship = childAndApplicantRelationsElement.getValue();
                        updatedRelationshipToParties.add(
                            Element.<RelationshipToPartiesCafcass>builder()
                                .id(childAndApplicantRelationsElement.getId())
                                .value(RelationshipToPartiesCafcass.builder()
                                           .partyId(tempRelationship.getRespondentId())
                                           .partyFullName(tempRelationship.getRespondentFullName())
                                           .partyType(PartyTypeEnum.RESPONDENT)
                                           .childId(tempRelationship.getChildId())
                                           .childFullName(tempRelationship.getChildFullName())
                                           .relationType(tempRelationship.getChildAndRespondentRelation())
                                           .otherRelationDetails(tempRelationship.getChildAndRespondentRelationOtherDetails())
                                           .childLivesWith(tempRelationship.getChildLivesWith())
                                           .build())
                                .build()
                        );

                    }
                );
        }
        return updatedRelationshipToParties;
    }

    public List<Element<RelationshipToPartiesCafcass>> getChildAndOtherPeopleRelations() {
        List<Element<RelationshipToPartiesCafcass>> updatedRelationshipToParties = new ArrayList<>();
        if (this.childAndOtherPeopleRelations != null) {
            this.childAndOtherPeopleRelations.stream()
                .forEach(
                    childAndApplicantRelationsElement -> {
                        RelationshipToPartiesCafcass tempRelationship = childAndApplicantRelationsElement.getValue();
                        updatedRelationshipToParties.add(
                            Element.<RelationshipToPartiesCafcass>builder()
                                .id(childAndApplicantRelationsElement.getId())
                                .value(RelationshipToPartiesCafcass.builder()
                                           .partyId(tempRelationship.getOtherPeopleId())
                                           .partyFullName(tempRelationship.getOtherPeopleFullName())
                                           .partyType(PartyTypeEnum.OTHERPEOPLE)
                                           .childId(tempRelationship.getChildId())
                                           .childFullName(tempRelationship.getChildFullName())
                                           .relationType(tempRelationship.getChildAndOtherPeopleRelation())
                                           .otherRelationDetails(tempRelationship.getChildAndOtherPeopleRelationOtherDetails())
                                           .childLivesWith(tempRelationship.getChildLivesWith())
                                           .build())
                                .build()
                        );

                    }
                );
        }
        return updatedRelationshipToParties;
    }

    private List<Element<RelationshipToPartiesCafcass>> childAndRespondentRelations;

    private List<Element<RelationshipToPartiesCafcass>> childAndOtherPeopleRelations;

    private List<uk.gov.hmcts.reform.prl.models.Element<UploadedDocuments>> cafcassUploadedDocs;

}
