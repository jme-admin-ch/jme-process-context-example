package ch.admin.bit.jeap.jme.processcontext.test;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for integration tests involving a spring context, based on
 * {@link BootServiceTestBase} for service lifecycle management.
 */
@SpringBootTest(classes = BootServiceSpringTestBase.TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles(resolver = BootServiceSpringTestBase.TestProfileResolver.class)
public abstract class BootServiceSpringTestBase extends BootServiceTestBase {
    @SpringBootApplication
    public static class TestApp {
    }
}
