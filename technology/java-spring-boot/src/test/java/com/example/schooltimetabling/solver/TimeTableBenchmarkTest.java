package com.example.schooltimetabling.solver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.optaplanner.benchmark.api.PlannerBenchmarkFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.schooltimetabling.rest.TimeTableController;

@SpringBootTest
public class TimeTableBenchmarkTest {

    @Autowired
    private TimeTableController timeTableController;

    @Autowired
    private PlannerBenchmarkFactory benchmarkFactory;

    @Test
    @Timeout(600_000)
    public void benchmark() {
        benchmarkFactory.buildPlannerBenchmark(timeTableController.getTimeTable()).benchmark();
    }
}
