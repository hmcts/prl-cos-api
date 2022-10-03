package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.google.common.collect.ImmutableSet;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class PreSubmitCallbackResponse<T extends CaseData> {

    private T data;
    private Set<String> errors = new LinkedHashSet<>();
    private Set<String> warnings = new LinkedHashSet<>();

    private PreSubmitCallbackResponse() {
        // noop -- for deserializer
    }

    public PreSubmitCallbackResponse(
        T data
    ) {
        requireNonNull(data);
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    public void addErrors(Collection<String> errors) {
        this.errors.addAll(errors);
    }

    public void addWarning(String error) {
        this.warnings.add(error);
    }

    public void addWarnings(Collection<String> errors) {
        this.warnings.addAll(errors);
    }

    public Set<String> getErrors() {
        return ImmutableSet.copyOf(errors);
    }

    public Set<String> getWarnings() {
        return ImmutableSet.copyOf(warnings);
    }

    public void setData(T data) {
        requireNonNull(data);
        this.data = data;
    }
}
