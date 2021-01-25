package org.optaplanner;

public interface SolutionRepository<Solution, ProblemId> {

    Solution get(ProblemId problemId);

    void put(ProblemId problemId, Solution solution);
}
