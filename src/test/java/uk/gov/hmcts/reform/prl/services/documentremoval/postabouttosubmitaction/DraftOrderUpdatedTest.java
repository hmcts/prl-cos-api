package uk.gov.hmcts.reform.prl.services.documentremoval.postabouttosubmitaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DRAFT_ORDER_COLLECTION;

class DraftOrderUpdatedTest {

    private DraftOrderUpdater draftOrderUpdater;

    @BeforeEach
    void setUp() {
        draftOrderUpdater = new DraftOrderUpdater();
    }

    @Test
    void shouldRemoveDraftOrdersWithNullOrderDocument() {
        DraftOrder draftOrderWithDoc = mock(DraftOrder.class);
        when(draftOrderWithDoc.getOrderDocument()).thenReturn(Document.builder().build());

        DraftOrder draftOrderWithoutDoc = mock(DraftOrder.class);
        when(draftOrderWithoutDoc.getOrderDocument()).thenReturn(null);

        List<Element<DraftOrder>> draftOrders = new ArrayList<>();
        UUID orderId = UUID.randomUUID();
        draftOrders.add(new Element<>(orderId, draftOrderWithDoc));
        draftOrders.add(new Element<>(UUID.randomUUID(), draftOrderWithoutDoc));

        CaseData caseData = mock(CaseData.class);
        when(caseData.getDraftOrderCollection()).thenReturn(draftOrders);
        when(caseData.getId()).thenReturn(123L);

        Map<String, Object> caseDataUpdated = new HashMap<>();

        draftOrderUpdater.onAboutToSubmit(caseData, caseDataUpdated);

        // Only the draft order with a non-null document should remain
        List<Element<DraftOrder>> updated = (List<Element<DraftOrder>>) caseDataUpdated.get(DRAFT_ORDER_COLLECTION);
        assertThat(updated).singleElement()
                .extracting(Element::getId)
                .isEqualTo(orderId);
    }

    @Test
    void shouldNotUpdateMapIfNoDraftOrdersRemoved() {
        DraftOrder draftOrderWithDoc = mock(DraftOrder.class);
        when(draftOrderWithDoc.getOrderDocument()).thenReturn(Document.builder().build());

        List<Element<DraftOrder>> draftOrders = new ArrayList<>();
        UUID orderId = UUID.randomUUID();
        draftOrders.add(new Element<>(orderId, draftOrderWithDoc));

        CaseData caseData = CaseData.builder()
            .draftOrderCollection(draftOrders)
            .build();


        Map<String, Object> caseDataUpdated = new HashMap<>();

        draftOrderUpdater.onAboutToSubmit(caseData, caseDataUpdated);

        // Map should not be updated since nothing was removed
        assertThat(caseDataUpdated).isEmpty();
    }

    @Test
    void shouldHandleEmptyDraftOrderCollection() {
        List<Element<DraftOrder>> draftOrders = new ArrayList<>();

        CaseData caseData = CaseData.builder()
            .draftOrderCollection(draftOrders)
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();

        draftOrderUpdater.onAboutToSubmit(caseData, caseDataUpdated);

        // Map should not be updated
        assertThat(caseDataUpdated).isEmpty();
    }

    @Test
    void shouldHandleNullDraftOrderCollection() {
        CaseData caseData = CaseData.builder()
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();

        draftOrderUpdater.onAboutToSubmit(caseData, caseDataUpdated);

        // Map should not be updated
        assertThat(caseDataUpdated).isEmpty();
    }
}
