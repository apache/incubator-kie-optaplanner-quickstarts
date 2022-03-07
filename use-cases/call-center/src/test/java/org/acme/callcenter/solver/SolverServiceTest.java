package org.acme.callcenter.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import org.acme.callcenter.data.DataGenerator;
import org.acme.callcenter.domain.Agent;
import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.domain.Skill;
import org.acme.callcenter.service.SolverService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import io.quarkus.test.junit.QuarkusTest;
import org.optaplanner.core.api.solver.SolverManager;

@QuarkusTest
public class SolverServiceTest {

    @Inject
    SolverManager<CallCenter, Long> solverManager;

    @Inject
    DataGenerator dataGenerator;

    private SolverService solverService;

    @BeforeEach
    void setUp() {
        solverService = new SolverService(solverManager);
    }

    @Test
    @Timeout(60)
    void addCall() {
        Call call1 = new Call(1L, "123-456-7891", Skill.ENGLISH, Skill.CAR_INSURANCE);
        Call call2 = new Call(2L, "123-456-7892", Skill.ENGLISH, Skill.CAR_INSURANCE);
        CallCenter bestSolution = solve(dataGenerator.generateCallCenter(), () -> {
            solverService.addCall(call1);
            solverService.addCall(call2);
        });

        Agent agentWithCalls = getFirstAgentWithCallOrFail(bestSolution);

        assertThat(agentWithCalls.getAssignedCalls())
                .containsExactlyInAnyOrder(call1, call2);
        assertThat(agentWithCalls.getSkills()).contains(Skill.ENGLISH, Skill.CAR_INSURANCE);
    }

    @Test
    @Timeout(60)
    void prolongCall() {
        CallCenter inputProblem = dataGenerator.generateCallCenter();
        Call call1 = new Call(1L, "123-456-7891", Skill.ENGLISH, Skill.CAR_INSURANCE);
        Call call2 = new Call(2L, "123-456-7892", Skill.ENGLISH, Skill.CAR_INSURANCE);
        inputProblem.getCalls().add(call1);
        inputProblem.getCalls().add(call2);

        CallCenter bestSolution = solve(inputProblem, () -> {
            solverService.prolongCall(call1.getId());
        });

        Agent agentWithCalls = getFirstAgentWithCallOrFail(bestSolution);
        assertThat(agentWithCalls.getSkills()).contains(Skill.ENGLISH, Skill.CAR_INSURANCE);

        assertThat(agentWithCalls.getAssignedCalls()).hasSize(2);
        Call prolongedCall = agentWithCalls.getAssignedCalls().stream()
                .filter(call -> call.getId().equals(call1.getId()))
                .findFirst()
                .orElseGet(() -> Assertions.fail("The expected prolonged call has not been found."));
        assertThat(prolongedCall.getDuration()).hasMinutes(1L);
        assertThat(prolongedCall.getDurationTillPickUp()).hasMinutes(1L);
    }

    @Test
    @Timeout(60)
    void removeCall() {
        CallCenter inputProblem = dataGenerator.generateCallCenter();
        Call call1 = new Call(1L, "123-456-7891", Skill.ENGLISH, Skill.CAR_INSURANCE);
        Call call2 = new Call(2L, "123-456-7892", Skill.ENGLISH, Skill.CAR_INSURANCE);
        inputProblem.getCalls().add(call1);
        inputProblem.getCalls().add(call2);

        CallCenter bestSolution  = solve(inputProblem, () -> {
            solverService.removeCall(call1.getId());
        });

        Agent agentWithCalls = getFirstAgentWithCallOrFail(bestSolution);
        assertThat(agentWithCalls.getSkills()).contains(Skill.ENGLISH, Skill.CAR_INSURANCE);

        assertThat(agentWithCalls.getAssignedCalls()).hasSize(1);
        Call call = agentWithCalls.getAssignedCalls().get(0);
        assertThat(call.getId()).isEqualTo(call2.getId());
    }

    private CallCenter solve(CallCenter inputProblem, Runnable problemChanges) {
        CountDownLatch allChangesProcessed = new CountDownLatch(1);

        AtomicReference<Throwable> errorDuringSolving = new AtomicReference<>();
        AtomicReference<CallCenter> bestSolution = new AtomicReference<>();
        solverService.startSolving(inputProblem, newBestSolution -> {
            bestSolution.set(newBestSolution);
            allChangesProcessed.countDown();
            solverService.stopSolving();
        }, throwable -> errorDuringSolving.set(throwable));

        problemChanges.run();

        try {
            while (!allChangesProcessed.await(10, TimeUnit.MILLISECONDS)) {
                if (errorDuringSolving.get() != null) {
                    throw new IllegalStateException("Exception during solving", errorDuringSolving.get());
                }
            }
        } catch (InterruptedException interruptedException) {
            throw new IllegalStateException("Interrupted during waiting.", interruptedException);
        }

        return bestSolution.get();
    }

    private Agent getFirstAgentWithCallOrFail(CallCenter callCenter) {
        return callCenter.getAgents().stream()
                .filter(agent -> !agent.getAssignedCalls().isEmpty())
                .findFirst()
                .orElseGet(() -> Assertions.fail("There is no agent with assigned calls."));
    }
}
