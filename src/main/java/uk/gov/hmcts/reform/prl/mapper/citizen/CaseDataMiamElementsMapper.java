package uk.gov.hmcts.reform.prl.mapper.citizen;

import com.google.common.base.Strings;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamCitizenChildProtectionConcernEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamCitizenDomesticAbuseReasonEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamCitizenExemptionsReasonEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamCitizenOtherGroundsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamCitizenPreviousAttendanceReasonEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamCitizenUrgencyReasonEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamDomesticAbuseChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamOtherGroundsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPolicyUpgradeChildProtectionConcernEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPreviousAttendanceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.TypeOfMiamAttendanceEvidenceEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildMiamElements;
import uk.gov.hmcts.reform.prl.models.complextypes.DomesticAbuseEvidenceDocument;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamPolicyUpgradeDetails;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamCitizenDomesticAbuseReasonEnum.ILRDuetoDomesticAbuse;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamCitizenDomesticAbuseReasonEnum.financialAbuse;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamCitizenOtherGroundsChecklistEnum.canNotAccessMediator;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamCitizenOtherGroundsChecklistEnum.disability;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamCitizenOtherGroundsChecklistEnum.noAppointmentAvailable;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamCitizenPreviousAttendanceReasonEnum.fourMonthsPriorAttended;


public class CaseDataMiamElementsMapper {

    public static final String NONE = "none";

    private CaseDataMiamElementsMapper() {
    }

    public static void updateMiamElementsForCaseData(CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                                     C100RebuildMiamElements c100RebuildMiamElements) {

        caseDataBuilder
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails.builder()
                                          .mpuChildInvolvedInMiam(isNotEmpty(c100RebuildMiamElements.getMiamOtherProceedings())
                                                                      ? c100RebuildMiamElements.getMiamOtherProceedings() : null)
                                          .mpuApplicantAttendedMiam(isNotEmpty(c100RebuildMiamElements.getMiamAttendance())
                                                                        ? c100RebuildMiamElements.getMiamAttendance() : null)
                                          .mpuClaimingExemptionMiam(isNotEmpty(c100RebuildMiamElements.getMiamValidReason())
                                                                        ? c100RebuildMiamElements.getMiamValidReason() : null)
                                          .mpuExemptionReasons(buildMiamExemptionReasons(c100RebuildMiamElements))
                                          .mpuDomesticAbuseEvidences(buildMiamDomesticAbuseChecklist(
                                              c100RebuildMiamElements))
                                          .mpuIsDomesticAbuseEvidenceProvided(isNotEmpty(c100RebuildMiamElements
                                                                                             .getMiamCanProvideDomesticAbuseEvidence())
                                                                                  ? c100RebuildMiamElements
                                              .getMiamCanProvideDomesticAbuseEvidence() : null)
                                          .mpuNoDomesticAbuseEvidenceReason(StringUtils.isNotEmpty(
                                              c100RebuildMiamElements.getMiamDetailsOfDomesticAbuseEvidence())
                                                                                ? c100RebuildMiamElements
                                              .getMiamDetailsOfDomesticAbuseEvidence().trim() : null)
                                          .mpuDomesticAbuseEvidenceDocument(buildDomesticAbuseEvidenceDocuments(
                                              c100RebuildMiamElements))
                                          .mpuChildProtectionConcernReason(buildMiamChildProtectionConcernList(
                                              c100RebuildMiamElements))
                                          .mpuUrgencyReason(buildMiamUrgencyReasonChecklist(
                                              c100RebuildMiamElements))
                                          .mpuPreviousMiamAttendanceReason(buildMiamPreviousAttendanceChecklist(
                                              c100RebuildMiamElements))
                                          .mpuDocFromDisputeResolutionProvider(fourMonthsPriorAttended.toString()
                                                                                   .equalsIgnoreCase(
                                                                                       c100RebuildMiamElements.getMiamPreviousAttendance())
                                                                                   && isNotEmpty(c100RebuildMiamElements
                                                                                                     .getMiamPreviousAttendanceEvidenceDoc())
                                                                                   ? c100RebuildMiamElements.getMiamPreviousAttendanceEvidenceDoc()
                                                                                   : null)
                                          .mpuCertificateByMediator(YesOrNo.Yes.equals(c100RebuildMiamElements
                                                                                           .getMiamHaveDocSignedByMediatorForPrevAttendance())
                                                                        && isNotEmpty(c100RebuildMiamElements.getMiamPreviousAttendanceEvidenceDoc())
                                                                        ? c100RebuildMiamElements.getMiamPreviousAttendanceEvidenceDoc() : null)
                                          .mpuMediatorDetails(StringUtils.isNotEmpty(c100RebuildMiamElements.getMiamDetailsOfEvidence())
                                                                  ? c100RebuildMiamElements.getMiamDetailsOfEvidence().trim() : null)
                                          .mpuTypeOfPreviousMiamAttendanceEvidence(
                                              buildTypeOfPreviousMiamAttendanceEvidence(c100RebuildMiamElements))
                                          .mpuOtherExemptionReasons(buildMiamOtherGroundsChecklist(
                                              c100RebuildMiamElements))
                                          .mpuApplicantUnableToAttendMiamReason1(buildApplicantUnableToAttendMiamReason1(
                                              c100RebuildMiamElements))
                                          .mpuApplicantUnableToAttendMiamReason2(StringUtils.isNotEmpty(
                                              c100RebuildMiamElements
                                                  .getMiamNoMediatorIn15mileDetails())
                                                                                     ? c100RebuildMiamElements
                                              .getMiamNoMediatorIn15mileDetails().trim() : null)
                                          .miamCertificationDocumentUpload(YesOrNo.Yes.equals(c100RebuildMiamElements
                                                                                                  .getMiamHaveDocSigned())
                                                                               ? buildDocument(c100RebuildMiamElements.getMiamCertificate()) : null)
                                          .build());
    }

    private static TypeOfMiamAttendanceEvidenceEnum buildTypeOfPreviousMiamAttendanceEvidence(C100RebuildMiamElements c100RebuildMiamElements) {
        if (ObjectUtils.isEmpty(c100RebuildMiamElements.getMiamHaveDocSignedByMediatorForPrevAttendance())) {
            return null;
        } else {
            return YesOrNo.Yes.equals(c100RebuildMiamElements.getMiamHaveDocSignedByMediatorForPrevAttendance())
                ? TypeOfMiamAttendanceEvidenceEnum.miamCertificate : TypeOfMiamAttendanceEvidenceEnum.miamAttendanceDetails;
        }
    }

    private static List<Element<DomesticAbuseEvidenceDocument>> buildDomesticAbuseEvidenceDocuments(C100RebuildMiamElements c100RebuildMiamElements) {
        List<Element<DomesticAbuseEvidenceDocument>> domesticAbuseEvidenceDocuments = null;
        if (ArrayUtils.isNotEmpty(c100RebuildMiamElements.getMiamDomesticAbuseEvidenceDocs())) {
            domesticAbuseEvidenceDocuments = new ArrayList<>();
            for (uk.gov.hmcts.reform.prl.models.documents.Document document : c100RebuildMiamElements.getMiamDomesticAbuseEvidenceDocs()) {
                DomesticAbuseEvidenceDocument domesticAbuseEvidenceDocument = DomesticAbuseEvidenceDocument.builder()
                    .domesticAbuseDocument(document)
                    .build();
                domesticAbuseEvidenceDocuments.add(ElementUtils.element(domesticAbuseEvidenceDocument));
            }
        }
        return domesticAbuseEvidenceDocuments;
    }

    private static String buildApplicantUnableToAttendMiamReason1(C100RebuildMiamElements c100RebuildMiamElements) {
        if (noAppointmentAvailable.toString().equalsIgnoreCase(c100RebuildMiamElements.getMiamNoMediatorReasons())
            && StringUtils.isNotEmpty(c100RebuildMiamElements.getMiamNoAppointmentAvailableDetails())) {
            return c100RebuildMiamElements.getMiamNoAppointmentAvailableDetails().trim();
        } else if (disability.toString().equalsIgnoreCase(c100RebuildMiamElements.getMiamNoMediatorReasons())
            && StringUtils.isNotEmpty(c100RebuildMiamElements.getMiamUnableToAttainDueToDisablityDetails())) {
            return c100RebuildMiamElements.getMiamUnableToAttainDueToDisablityDetails().trim();
        } else {
            return null;
        }
    }


    private static MiamOtherGroundsChecklistEnum buildMiamOtherGroundsChecklist(C100RebuildMiamElements c100RebuildMiamElements) {
        if (StringUtils.isEmpty(c100RebuildMiamElements.getMiamNotAttendingReasons())
            || c100RebuildMiamElements.getMiamNotAttendingReasons().equalsIgnoreCase(NONE)) {
            return null;
        } else {
            MiamCitizenOtherGroundsChecklistEnum miamCitizenPreviousAttendanceReasons;
            if (c100RebuildMiamElements.getMiamNotAttendingReasons().equalsIgnoreCase(canNotAccessMediator.toString())
                && StringUtils.isNotEmpty(c100RebuildMiamElements.getMiamNoMediatorReasons())
                && !c100RebuildMiamElements.getMiamNoMediatorReasons().equalsIgnoreCase(NONE)) {
                miamCitizenPreviousAttendanceReasons
                    = MiamCitizenOtherGroundsChecklistEnum.valueOf(c100RebuildMiamElements.getMiamNoMediatorReasons());
            } else {
                miamCitizenPreviousAttendanceReasons
                    = MiamCitizenOtherGroundsChecklistEnum.valueOf(c100RebuildMiamElements.getMiamNotAttendingReasons());
            }
            if (isNotEmpty(miamCitizenPreviousAttendanceReasons)) {
                return MiamOtherGroundsChecklistEnum.valueOf(miamCitizenPreviousAttendanceReasons.getDisplayedValue());
            } else {
                return null;
            }
        }
    }

    private static MiamPreviousAttendanceChecklistEnum buildMiamPreviousAttendanceChecklist(C100RebuildMiamElements c100RebuildMiamElements) {
        if (StringUtils.isEmpty(c100RebuildMiamElements.getMiamPreviousAttendance())
            || c100RebuildMiamElements.getMiamPreviousAttendance().equalsIgnoreCase(NONE)) {
            return null;
        } else {
            MiamCitizenPreviousAttendanceReasonEnum miamCitizenPreviousAttendanceReasons
                = MiamCitizenPreviousAttendanceReasonEnum.valueOf(c100RebuildMiamElements.getMiamPreviousAttendance());
            if (isNotEmpty(miamCitizenPreviousAttendanceReasons)) {
                return MiamPreviousAttendanceChecklistEnum.valueOf(miamCitizenPreviousAttendanceReasons.getDisplayedValue());
            } else {
                return null;
            }
        }

    }

    private static MiamUrgencyReasonChecklistEnum buildMiamUrgencyReasonChecklist(C100RebuildMiamElements c100RebuildMiamElements) {
        if (StringUtils.isEmpty(c100RebuildMiamElements.getMiamUrgency())
            || c100RebuildMiamElements.getMiamUrgency().equalsIgnoreCase(NONE)) {
            return null;
        } else {
            MiamCitizenUrgencyReasonEnum miamCitizenUrgencyReasons
                = MiamCitizenUrgencyReasonEnum.valueOf(c100RebuildMiamElements.getMiamUrgency());
            if (isNotEmpty(miamCitizenUrgencyReasons)) {
                return MiamUrgencyReasonChecklistEnum.valueOf(miamCitizenUrgencyReasons.getDisplayedValue());
            } else {
                return null;
            }
        }
    }

    private static MiamPolicyUpgradeChildProtectionConcernEnum buildMiamChildProtectionConcernList(C100RebuildMiamElements c100RebuildMiamElements) {
        if (StringUtils.isEmpty(c100RebuildMiamElements.getMiamChildProtectionEvidence())
            || c100RebuildMiamElements.getMiamChildProtectionEvidence().equalsIgnoreCase(NONE)) {
            return null;
        } else {
            MiamCitizenChildProtectionConcernEnum miamCitizenChildProtectionReasons
                = MiamCitizenChildProtectionConcernEnum.valueOf(c100RebuildMiamElements.getMiamChildProtectionEvidence());
            if (isNotEmpty(miamCitizenChildProtectionReasons)) {
                return MiamPolicyUpgradeChildProtectionConcernEnum.valueOf(miamCitizenChildProtectionReasons.getDisplayedValue());
            } else {
                return null;
            }
        }
    }

    private static List<MiamDomesticAbuseChecklistEnum> buildMiamDomesticAbuseChecklist(C100RebuildMiamElements
                                                                                            c100RebuildMiamElements) {
        List<String> domesticAbuses = new java.util.ArrayList<>(nonNull(c100RebuildMiamElements.getMiamDomesticAbuse())
                                                                    ? List.of(c100RebuildMiamElements.getMiamDomesticAbuse())
                                                                    : Collections.emptyList());

        if (domesticAbuses.isEmpty() || domesticAbuses.contains(NONE)) {
            return Collections.emptyList();
        } else {
            String[] domesticAbuse1 = c100RebuildMiamElements.getMiamDomesticAbusePoliceInvolvementSubfields();
            String[] domesticAbuse2 = c100RebuildMiamElements.getMiamDomesticAbuseCourtInvolvementSubfields();
            String[] domesticAbuse3 = c100RebuildMiamElements.getMiamDomesticAbuseLetterOfBeingVictimSubfields();
            String[] domesticAbuse4 = c100RebuildMiamElements.getMiamDomesticAbuseLetterFromAuthoritySubfields();
            String[] domesticAbuse5 = c100RebuildMiamElements.getMiamDomesticAbuseLetterFromSupportServiceSubfields();

            List<String> domesticAbusesList = populateDomesticAbuseList(
                domesticAbuse1,
                domesticAbuse2,
                domesticAbuse3,
                domesticAbuse4,
                domesticAbuse5
            );

            List<String> domesticAbuseHeads = List.of(c100RebuildMiamElements.getMiamDomesticAbuse());

            if (domesticAbuseHeads.contains(ILRDuetoDomesticAbuse.toString())) {
                domesticAbusesList.add(ILRDuetoDomesticAbuse.toString());
            }
            if (domesticAbuseHeads.contains(financialAbuse.toString())) {
                domesticAbusesList.add(financialAbuse.toString());
            }

            List<MiamCitizenDomesticAbuseReasonEnum> miamCitizenDomesticAbuseReasons = domesticAbusesList.stream().map(
                    MiamCitizenDomesticAbuseReasonEnum::valueOf)
                .toList();
            if (miamCitizenDomesticAbuseReasons.isEmpty()) {
                return Collections.emptyList();
            } else {
                return miamCitizenDomesticAbuseReasons.stream().map(
                        value -> MiamDomesticAbuseChecklistEnum.valueOf(value.getDisplayedValue()))
                    .toList();
            }
        }
    }

    private static List<String> populateDomesticAbuseList(String[] domesticAbuse1,
                                                          String[] domesticAbuse2,
                                                          String[] domesticAbuse3,
                                                          String[] domesticAbuse4,
                                                          String[] domesticAbuse5) {
        List<String> domesticAbuse1List = nonNull(domesticAbuse1) ? List.of(domesticAbuse1) : Collections.emptyList();
        List<String> domesticAbuse2List = nonNull(domesticAbuse2) ? List.of(domesticAbuse2) : Collections.emptyList();
        List<String> domesticAbuse3List = nonNull(domesticAbuse3) ? List.of(domesticAbuse3) : Collections.emptyList();
        List<String> domesticAbuse4List = nonNull(domesticAbuse4) ? List.of(domesticAbuse4) : Collections.emptyList();
        List<String> domesticAbuse5List = nonNull(domesticAbuse5) ? List.of(domesticAbuse5) : Collections.emptyList();

        List<String> domesticAbusesList = Stream.of(domesticAbuse1List, domesticAbuse2List, domesticAbuse3List,
                                                    domesticAbuse4List, domesticAbuse5List
            )
            .flatMap(Collection::stream).collect(Collectors.toList());

        domesticAbusesList.removeIf(Strings::isNullOrEmpty);
        return domesticAbusesList;
    }

    private static List<MiamExemptionsChecklistEnum> buildMiamExemptionReasons(C100RebuildMiamElements c100RebuildMiamElements) {
        List<String> miamNonAttendanceReasons = nonNull(c100RebuildMiamElements.getMiamNonAttendanceReasons())
            ? List.of(c100RebuildMiamElements.getMiamNonAttendanceReasons()) : Collections.emptyList();
        if (miamNonAttendanceReasons.isEmpty() || miamNonAttendanceReasons.contains(NONE)) {
            return Collections.emptyList();
        } else {
            List<MiamCitizenExemptionsReasonEnum> miamCitizenExemptionsReasonList = miamNonAttendanceReasons.stream()
                .map(MiamCitizenExemptionsReasonEnum::valueOf)
                .toList();
            if (miamCitizenExemptionsReasonList.isEmpty()) {
                return Collections.emptyList();
            } else {
                return miamCitizenExemptionsReasonList.stream().map(
                        value -> MiamExemptionsChecklistEnum.valueOf(value.getDisplayedValue()))
                    .toList();
            }
        }
    }

    private static Document buildDocument(uk.gov.hmcts.reform.prl.models.c100rebuild.Document maimDocument) {
        if (isNotEmpty(maimDocument)) {
            return Document.builder()
                .documentUrl(maimDocument.getUrl())
                .documentBinaryUrl(maimDocument.getBinaryUrl())
                .documentFileName(maimDocument.getFilename())
                .build();
        }
        return null;
    }
}
