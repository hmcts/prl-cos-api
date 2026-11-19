package uk.gov.hmcts.reform.prl.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndApplicantRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Relations;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.father;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.mother;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

/** CCD-8320: editing existing child details must preserve identity and relationship references. */
@ExtendWith(MockitoExtension.class)
class UpdatePartyDetailsChildIdentityTest {

    private static final UUID FIRST_CHILD_ID = UUID.fromString("2d148b3a-7f45-4346-bc3d-d4e12306940f");
    private static final UUID SECOND_CHILD_ID = UUID.fromString("11111111-2222-4333-8444-555555555555");

    @InjectMocks
    private UpdatePartyDetailsService service;

    @ParameterizedTest
    @ValueSource(strings = {"v2", "v3"})
    void shouldPreserveEachExistingChildIdWhenRefreshingDetails(String version) {
        CaseData original = existingCase(version);

        List<Element<ChildDetailsRevised>> updated = revisedChildren(service.setDefaultEmptyChildDetails(original));

        assertThat(updated).hasSize(2);
        assertThat(updated).extracting(child -> child.getValue().getFirstName())
            .containsExactly("First child", "Second child");
        assertAll(
            () -> assertThat(updated.getFirst().getId()).as("first child's existing UUID").isEqualTo(FIRST_CHILD_ID),
            () -> assertThat(updated.get(1).getId()).as("second child's existing UUID").isEqualTo(SECOND_CHILD_ID)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"v2", "v3"})
    void shouldKeepFatherAndMotherRelationshipsLinkedToTheirChildren(String version) {
        CaseData original = existingCase(version);

        final Map<String, Object> updates = service.setDefaultEmptyChildDetails(original);
        final List<Element<ChildDetailsRevised>> updated = revisedChildren(updates);

        // The real helper returns only newChildDetails; the caller retains the relationship collection.
        assertThat(updates).containsOnlyKeys("newChildDetails");
        final List<Element<ChildrenAndApplicantRelation>> relationships = original.getRelations()
            .getChildAndApplicantRelations();
        assertThat(relationships).extracting(relation -> relation.getValue().getChildAndApplicantRelation())
            .containsExactly(father, mother);
        assertThat(relationships).extracting(relation -> relation.getValue().getChildId())
            .containsExactly(FIRST_CHILD_ID.toString(), SECOND_CHILD_ID.toString());
        assertAll(
            () -> assertThat(relationships.getFirst().getValue().getChildId()).as("Father still references first child")
                .isEqualTo(updated.getFirst().getId().toString()),
            () -> assertThat(relationships.get(1).getValue().getChildId()).as("Mother still references second child")
                .isEqualTo(updated.get(1).getId().toString())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"v2", "v3"})
    void shouldKeepChildIdsStableWhenDetailsAreRefreshedRepeatedly(String version) {
        CaseData original = existingCase(version);
        final List<Element<ChildDetailsRevised>> first = revisedChildren(service.setDefaultEmptyChildDetails(original));
        CaseData afterFirst = original.toBuilder().newChildDetails(first).build();

        final List<Element<ChildDetailsRevised>> second = revisedChildren(service.setDefaultEmptyChildDetails(afterFirst));

        assertAll(
            () -> assertThat(first).as("first refresh preserves original identities").extracting(Element::getId)
                .containsExactly(FIRST_CHILD_ID, SECOND_CHILD_ID),
            () -> assertThat(second).as("second refresh preserves original identities").extracting(Element::getId)
                .containsExactly(FIRST_CHILD_ID, SECOND_CHILD_ID),
            () -> assertThat(second).as("repeating refresh does not change identities").extracting(Element::getId)
                .containsExactlyElementsOf(first.stream().map(Element::getId).toList())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"v2", "v3"})
    void shouldGenerateAnIdForEachNewDefaultChild(String version) {
        CaseData emptyCase = CaseData.builder().caseTypeOfApplication("C100").taskListVersion(version).build();

        final List<Element<ChildDetailsRevised>> first = revisedChildren(service.setDefaultEmptyChildDetails(emptyCase));
        final List<Element<ChildDetailsRevised>> second = revisedChildren(service.setDefaultEmptyChildDetails(emptyCase));

        assertThat(first).hasSize(1);
        assertThat(second).hasSize(1);
        assertThat(first.getFirst().getId()).isNotNull();
        assertThat(second.getFirst().getId()).isNotNull().isNotEqualTo(first.getFirst().getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"v2", "v3"})
    void shouldKeepIdentityAttachedToEachChildWhenChildrenAreReordered(String version) {
        CaseData original = existingCase(version);
        CaseData reordered = original.toBuilder().newChildDetails(List.of(
            original.getNewChildDetails().get(1), original.getNewChildDetails().get(0))).build();

        List<Element<ChildDetailsRevised>> updated = revisedChildren(service.setDefaultEmptyChildDetails(reordered));

        assertThat(updated).extracting(Element::getId).containsExactly(SECOND_CHILD_ID, FIRST_CHILD_ID);
        assertThat(updated).extracting(child -> child.getValue().getFirstName())
            .containsExactly("Second child", "First child");
    }

    @ParameterizedTest
    @ValueSource(strings = {"v2", "v3"})
    void shouldKeepIdsAndRelationshipsWhenChildDetailsAreEditedBetweenRefreshes(String version) {
        CaseData original = existingCase(version);
        final List<Element<ChildDetailsRevised>> first = revisedChildren(service.setDefaultEmptyChildDetails(original));
        ChildDetailsRevised editedChild = first.get(0).getValue().toBuilder().firstName("Edited child").build();
        CaseData edited = original.toBuilder().newChildDetails(List.of(
            element(first.get(0).getId(), editedChild), first.get(1))).build();

        final List<Element<ChildDetailsRevised>> second = revisedChildren(service.setDefaultEmptyChildDetails(edited));

        assertThat(second).extracting(Element::getId).containsExactly(FIRST_CHILD_ID, SECOND_CHILD_ID);
        assertThat(second.getFirst().getValue().getFirstName()).isEqualTo("Edited child");
        assertThat(edited.getRelations().getChildAndApplicantRelations())
            .extracting(relation -> relation.getValue().getChildId())
            .containsExactlyElementsOf(second.stream().map(child -> child.getId().toString()).toList());
    }

    @ParameterizedTest
    @ValueSource(strings = {"v2", "v3"})
    void shouldRefreshLivingArrangementChoicesAndPreserveChangedSelectionWithoutChangingChildIds(String version) {
        final UUID applicantId = UUID.fromString("44444444-4444-4444-8444-444444444444");
        final UUID respondentId = UUID.fromString("55555555-5555-4555-8555-555555555555");
        DynamicListElement initialSelection = DynamicListElement.builder().code(applicantId).label("Applicant").build();
        CaseData original = existingCase(version);
        ChildDetailsRevised child = original.getNewChildDetails().get(0).getValue().toBuilder()
            .whoDoesTheChildLiveWith(DynamicList.builder().value(initialSelection).listItems(List.of()).build()).build();
        original = original.toBuilder()
            .applicants(List.of(element(applicantId, PartyDetails.builder().firstName("Updated").lastName("Applicant").build())))
            .respondents(List.of(element(respondentId, PartyDetails.builder().firstName("Example").lastName("Respondent").build())))
            .newChildDetails(List.of(element(FIRST_CHILD_ID, child), original.getNewChildDetails().get(1))).build();

        final List<Element<ChildDetailsRevised>> refreshed = revisedChildren(service.setDefaultEmptyChildDetails(original));

        DynamicList choices = refreshed.getFirst().getValue().getWhoDoesTheChildLiveWith();
        assertThat(choices.getListItems()).extracting(DynamicListElement::getCode)
            .containsExactly(applicantId.toString(), respondentId.toString());
        assertThat(choices.getListItems()).extracting(DynamicListElement::getLabel)
            .containsExactly("Updated Applicant", "Example Respondent");
        assertThat(choices.getValue()).isEqualTo(initialSelection);
        assertThat(refreshed.get(1).getValue().getWhoDoesTheChildLiveWith().getValue()).isEqualTo(DynamicListElement.EMPTY);
        DynamicList changedSelection = choices.toBuilder().value(choices.getListItems().get(1)).build();
        CaseData edited = original.toBuilder().newChildDetails(List.of(
            element(refreshed.get(0).getId(), refreshed.get(0).getValue().toBuilder()
                .whoDoesTheChildLiveWith(changedSelection).build()), refreshed.get(1))).build();

        final List<Element<ChildDetailsRevised>> second = revisedChildren(service.setDefaultEmptyChildDetails(edited));

        assertThat(second).extracting(Element::getId).containsExactly(FIRST_CHILD_ID, SECOND_CHILD_ID);
        assertThat(second.getFirst().getValue().getWhoDoesTheChildLiveWith().getValueCode()).isEqualTo(respondentId.toString());
    }

    @Test
    void shouldGenerateDistinctIdsForNewLegacyChildren() {
        CaseData emptyCase = CaseData.builder().caseTypeOfApplication("C100").build();

        Map<String, Object> first = service.setDefaultEmptyChildDetails(emptyCase);
        Map<String, Object> second = service.setDefaultEmptyChildDetails(emptyCase);

        @SuppressWarnings("unchecked")
        List<Element<Child>> firstChildren = (List<Element<Child>>) first.get("children");
        @SuppressWarnings("unchecked")
        List<Element<Child>> secondChildren = (List<Element<Child>>) second.get("children");
        assertThat(firstChildren).hasSize(1);
        assertThat(secondChildren).hasSize(1);
        assertThat(firstChildren.getFirst().getId()).isNotNull();
        assertThat(secondChildren.getFirst().getId()).isNotNull().isNotEqualTo(firstChildren.getFirst().getId());
    }

    @Test
    void shouldPreserveExistingChildIdForLegacyChildDetails() {
        List<Element<Child>> children = List.of(element(FIRST_CHILD_ID, Child.builder().firstName("Example").build()));
        CaseData original = CaseData.builder().caseTypeOfApplication("C100").children(children).build();

        Map<String, Object> updates = service.setDefaultEmptyChildDetails(original);

        assertThat(updates.get("children")).isEqualTo(children);
    }

    private static CaseData existingCase(String version) {
        ChildrenAndApplicantRelation firstRelationship = ChildrenAndApplicantRelation.builder()
            .childId(FIRST_CHILD_ID.toString()).childAndApplicantRelation(father).build();
        ChildrenAndApplicantRelation secondRelationship = ChildrenAndApplicantRelation.builder()
            .childId(SECOND_CHILD_ID.toString()).childAndApplicantRelation(mother).build();
        return CaseData.builder().caseTypeOfApplication("C100").taskListVersion(version)
            .newChildDetails(List.of(
                element(FIRST_CHILD_ID, ChildDetailsRevised.builder().firstName("First child").build()),
                element(SECOND_CHILD_ID, ChildDetailsRevised.builder().firstName("Second child").build())))
            .relations(Relations.builder().childAndApplicantRelations(List.of(
                element(firstRelationship), element(secondRelationship))).build())
            .build();
    }

    @SuppressWarnings("unchecked")
    private static List<Element<ChildDetailsRevised>> revisedChildren(Map<String, Object> updates) {
        return (List<Element<ChildDetailsRevised>>) updates.get("newChildDetails");
    }
}
