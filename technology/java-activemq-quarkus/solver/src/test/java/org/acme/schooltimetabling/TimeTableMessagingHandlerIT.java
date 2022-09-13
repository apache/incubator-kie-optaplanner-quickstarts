package org.acme.schooltimetabling;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Disabled;

@Disabled("The native test hangs after starting the native image and before entering the first test method.")
@QuarkusIntegrationTest
public class TimeTableMessagingHandlerIT extends TimeTableMessagingHandlerTest {

}
