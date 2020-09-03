package net.devtech.grossfabrichacks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

@Testable
class LogTest {
    static Logger LOGGER = LogManager.getLogger();
    static final int iterations = 5000;

    @Test
    void test() {
        LogUtil.logMeanTime(
            new TestInfo("System.out.println 0: %f", iterations, System.out::println),
            new TestInfo("System.out.println 1: %f", iterations, System.out::println),
            new TestInfo("Logger.info 0: %f", iterations, LOGGER::info),
            new TestInfo("Logger.info 1: %f", iterations, LOGGER::info)
        );
    }
}
