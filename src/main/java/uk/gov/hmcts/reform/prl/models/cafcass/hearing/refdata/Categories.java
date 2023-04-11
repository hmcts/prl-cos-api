package uk.gov.hmcts.reform.prl.models.cafcass.hearing.refdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Categories {

    @JsonProperty("list_of_values")
    private List<Category> listOfCategory;
}
