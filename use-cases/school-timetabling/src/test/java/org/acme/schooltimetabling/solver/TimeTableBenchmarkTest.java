package org.acme.schooltimetabling.solver;

import javax.inject.Inject;

import org.acme.schooltimetabling.rest.TimeTableResource;
import org.junit.jupiter.api.Test;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;

import io.quarkus.test.junit.QuarkusTest;

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
