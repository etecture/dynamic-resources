package de.etecture.opensource.dynamicresources.extension;

import de.etecture.opensource.dynamicresources.api.Version;
import de.etecture.opensource.dynamicresources.spi.VersionNumberResolver;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author rhk
 */
public class DefaultVersionNumberResolverTest {

    VersionNumberResolver resolver = new DefaultVersionNumberResolver();

    @Test
    public void testVersionNumberResolving() {
        Map<Version, String> testCandidates = new HashMap<>();

        testCandidates.put(new VersionExpression("2.1"), "2.1");
        testCandidates.put(new VersionExpression("1.1"), "1.1");
        testCandidates.put(new VersionExpression("4"), "4");
        testCandidates.put(new VersionExpression("1"), "1");
        testCandidates.put(new VersionExpression("3.0.1"), "3.0.1");
        testCandidates.put(new VersionExpression("1.0.1"), "1.0.1");

        assertThat(resolver.resolve(testCandidates, "1"), equalTo("1"));
        assertThat(resolver.resolve(testCandidates, "1.0"), equalTo("1"));
        assertThat(resolver.resolve(testCandidates, "1.0.0"), equalTo("1"));

        assertThat(resolver.resolve(testCandidates, "2.1"), equalTo("2.1"));
        assertThat(resolver.resolve(testCandidates, "2.1.0"), equalTo("2.1"));

        assertThat(resolver.resolve(testCandidates, "3.0.1"), equalTo("3.0.1"));

        assertThat(resolver.resolve(testCandidates, "(,)"), equalTo("4"));

        assertThat(resolver.resolve(testCandidates, "(,3)"), equalTo("2.1"));

        assertThat(resolver.resolve(Collections
                .<Version, String>emptyMap(),
                "(,)"), equalTo(null));
        assertThat(resolver.resolve(testCandidates, "]2.1,3.0.1["),
                equalTo(null));

        assertThat(resolver.resolve(testCandidates, "]2.1,4["), equalTo(
                "3.0.1"));

        assertThat(resolver.resolve(testCandidates, "]2.1,3.0.1]"), equalTo(
                "3.0.1"));
    }
}
