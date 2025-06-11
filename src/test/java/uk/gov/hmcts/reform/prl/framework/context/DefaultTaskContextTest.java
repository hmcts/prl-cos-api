package uk.gov.hmcts.reform.prl.framework.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class DefaultTaskContextTest {

    private DefaultTaskContext defaultTaskContext;

    @BeforeEach
    public void setup() {
        defaultTaskContext = new DefaultTaskContext();
    }

    @Test
    public void defaultContextTasFailedIsFalseByDefault() {
        assertThat(defaultTaskContext.hasTaskFailed(), is(false));
    }

    @Test
    public void defaultContextStatusIsTrueWhenSetFailedIsCalled() {
        defaultTaskContext.setTaskFailed(true);
        assertTrue(defaultTaskContext.hasTaskFailed());
    }

    @Test
    public void computesTransientObjectIfAbsent() {
        defaultTaskContext.setTransientObject("foo", null);
        defaultTaskContext.computeTransientObjectIfAbsent("foo", "bar");

        assertThat(defaultTaskContext.getTransientObject("foo"), is("bar"));
    }

    @Test
    public void doesNotComputeTransientObjectIfAbsent() {
        defaultTaskContext.setTransientObject("foo", "bar");
        defaultTaskContext.computeTransientObjectIfAbsent("foo", "baz");

        assertThat(defaultTaskContext.getTransientObject("foo"), is("bar"));
        assertThat(defaultTaskContext.getTransientObjectOptional("foo").isEmpty(), is(false));
        assertThat(defaultTaskContext.getTransientObjectOptional("foo").get(), is("bar"));
    }

    @Test
    public void useOverloadedConstructor() {
        defaultTaskContext.setTransientObject("foo", "bar");
        DefaultTaskContext context = new DefaultTaskContext(defaultTaskContext);

        assertThat(context.getTransientObject("foo"), is("bar"));
        assertThat(context.getTransientObjectOptional("oof").isEmpty(), is(true));
        assertThat(context.getTransientObjectOptional("foo").isEmpty(), is(false));
    }
}
