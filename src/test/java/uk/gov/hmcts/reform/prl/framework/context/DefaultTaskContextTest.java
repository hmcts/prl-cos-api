package uk.gov.hmcts.reform.prl.framework.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DefaultTaskContextTest {

    private DefaultTaskContext defaultTaskContext;

    @BeforeEach
    void setup() {
        defaultTaskContext = new DefaultTaskContext();
    }

    @Test
    void defaultContextTasFailedIsFalseByDefault() {
        assertThat(defaultTaskContext.hasTaskFailed(), is(false));
    }

    @Test
    void defaultContextStatusIsTrueWhenSetFailedIsCalled() {
        defaultTaskContext.setTaskFailed(true);
        assertTrue(defaultTaskContext.hasTaskFailed());
    }

    @Test
    void computesTransientObjectIfAbsent() {
        defaultTaskContext.setTransientObject("foo", null);
        defaultTaskContext.computeTransientObjectIfAbsent("foo", "bar");

        assertThat(defaultTaskContext.getTransientObject("foo"), is("bar"));
    }

    @Test
    void doesNotComputeTransientObjectIfAbsent() {
        defaultTaskContext.setTransientObject("foo", "bar");
        defaultTaskContext.computeTransientObjectIfAbsent("foo", "baz");

        assertThat(defaultTaskContext.getTransientObject("foo"), is("bar"));
        assertThat(defaultTaskContext.getTransientObjectOptional("foo").isEmpty(), is(false));
        assertThat(defaultTaskContext.getTransientObjectOptional("foo").get(), is("bar"));
    }

    @Test
    void useOverloadedConstructor() {
        defaultTaskContext.setTransientObject("foo", "bar");
        DefaultTaskContext context = new DefaultTaskContext(defaultTaskContext);

        assertThat(context.getTransientObject("foo"), is("bar"));
        assertThat(context.getTransientObjectOptional("oof").isEmpty(), is(true));
        assertThat(context.getTransientObjectOptional("foo").isEmpty(), is(false));
    }
}
