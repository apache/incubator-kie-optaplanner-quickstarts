package org.acme.schooltimetabling.solver;

import io.quarkus.test.junit.QuarkusTest;
import org.acme.schooltimetabling.rest.TimeTableResource;
import org.junit.jupiter.api.Test;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;

import javax.inject.Inject;

@QuarkusTest
public class TimeTableBenchmarkTest {

    @Inject
    PlannerBenchmarkFactory benchmarkFactory;

    @Inject
    TimeTableResource timeTableResource;

    @Test
    public void benchmark() {
        benchmarkFactory.buildPlannerBenchmark(timeTableResource.getTimeTable())
                .benchmark();
    }
}
