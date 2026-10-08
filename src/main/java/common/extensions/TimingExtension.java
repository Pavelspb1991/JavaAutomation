package common.extensions;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TimingExtension implements BeforeEachCallback, AfterEachCallback {
    private static final ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Override
    public void beforeEach(ExtensionContext context) {
        startTime.set(System.currentTimeMillis());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        long duration = System.currentTimeMillis() - startTime.get();
        System.out.println(context.getDisplayName() + " — " + duration + " ms");
    }
}