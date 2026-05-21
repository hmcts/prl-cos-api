package uk.gov.hmcts.reform.prl.models.common.dynamic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DynamicListTest {

    @Test
    void shouldSortListItemsByLabel() {
        DynamicListElement elementB = DynamicListElement.builder().label("Banana").code("2").build();
        DynamicListElement elementA = DynamicListElement.builder().label("Apple").code("1").build();
        DynamicListElement elementC = DynamicListElement.builder().label("Cherry").code("3").build();

        List<DynamicListElement> unsorted = List.of(elementB, elementC, elementA);

        DynamicList dynamicList = DynamicList.builder()
            .listItems(unsorted)
            .build();

        DynamicList sortedList = dynamicList.withSortedListItemsByLabel();

        assertEquals("Apple", sortedList.getListItems().get(0).getLabel());
        assertEquals("Banana", sortedList.getListItems().get(1).getLabel());
        assertEquals("Cherry", sortedList.getListItems().get(2).getLabel());
    }

    @Test
    void shouldReturnSameInstanceIfListSingleElement() {
        DynamicList dynamicListSingle = DynamicList.builder()
            .listItems(List.of(DynamicListElement.builder().label("Only").code("1").build()))
            .build();

        assertSame(dynamicListSingle, dynamicListSingle.withSortedListItemsByLabel());
    }

    @Test
    void shouldReturnSameInstanceIfListIsNull() {
        DynamicList dynamicListNull = DynamicList.builder().listItems(null).build();

        assertSame(dynamicListNull, dynamicListNull.withSortedListItemsByLabel());
    }
}
