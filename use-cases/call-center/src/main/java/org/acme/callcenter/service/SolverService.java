/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acme.callcenter.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import org.acme.callcenter.domain.Agent;
import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.solver.change.AddCallProblemFactChange;
import org.acme.callcenter.solver.change.PinCallProblemFactChange;
import org.acme.callcenter.solver.change.ProlongCallByMinuteProblemFactChange;
import org.acme.callcenter.solver.change.RemoveCallProblemFactChange;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.optaplanner.core.api.solver.ProblemFactChange;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;

@ApplicationScoped
public class SolverService {

    private final ManagedExecutor managedExecutor;

    // TODO: Replace by @Inject SolverManager once https://issues.redhat.com/browse/PLANNER-2141 is resolved.
    private final Solver<CallCenter> solver;

    private AtomicBoolean solving = new AtomicBoolean(false);
    private CompletableFuture<?> completableSolverFuture;
    private final BlockingQueue<ProblemFactChange<CallCenter>> waitingProblemFactChanges = new LinkedBlockingQueue<>();

    @Inject
    public SolverService(SolverFactory<CallCenter> solverFactory, @Default ManagedExecutor executorService) {
        solver = solverFactory.buildSolver();
        this.managedExecutor = executorService;
    }

    private void pinCallAssignedToAgents(List<Call> calls) {
        List<ProblemFactChange<CallCenter>> pinCallProblemFactChanges = calls.stream()
                .filter(call -> !call.isPinned()
                        && call.getPreviousCallOrAgent() != null
                        && call.getPreviousCallOrAgent() instanceof Agent)
                .map(PinCallProblemFactChange::new)
                .collect(Collectors.toList());
        solver.addProblemFactChanges(pinCallProblemFactChanges);
    }

    public void startSolving(CallCenter inputProblem,
            Consumer<BestSolutionChangedEvent<CallCenter>> bestSolutionChangedEventConsumer, Consumer<Throwable> errorHandler) {
        solving.set(true);
        completableSolverFuture = managedExecutor.runAsync(() -> {

            solver.addEventListener(event -> {
                if (event.isEveryProblemFactChangeProcessed() && event.getNewBestScore().isSolutionInitialized()) {
                    pinCallAssignedToAgents(event.getNewBestSolution().getCalls());
                    bestSolutionChangedEventConsumer.accept(event);
                }
            });

            try {
                solver.solve(inputProblem);
            } catch (Throwable throwable) {
                errorHandler.accept(throwable);
            }
            solver.addProblemFactChanges(new ArrayList<>(waitingProblemFactChanges));
        });
    }

    public void stopSolving() {
        solving.set(false);
        if (completableSolverFuture != null) {
            solver.terminateEarly();
            try {
                completableSolverFuture.get(); // Wait for termination and propagate exceptions.
                completableSolverFuture = null;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Failed to stop solver.", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Failed to stop solver.", e.getCause());
            }
        }
    }

    public boolean isSolving() {
        return solving.get();
    }

    public void addCall(Call call) {
        registerProblemFactChange(new AddCallProblemFactChange(call));
    }

    public void removeCall(long callId) {
        registerProblemFactChange(new RemoveCallProblemFactChange(callId));
    }

    public void prolongCall(long callId) {
        registerProblemFactChange(new ProlongCallByMinuteProblemFactChange(callId));
    }

    private void registerProblemFactChange(ProblemFactChange<CallCenter> problemFactChange) {
        if (isSolving()) {
            assertSolverIsAlive();
            solver.addProblemFactChange(problemFactChange);
        } else {
            waitingProblemFactChanges.add(problemFactChange);
        }
    }

    private void assertSolverIsAlive() {
        if (completableSolverFuture == null) {
            throw new IllegalStateException("Solver has not been started yet.");
        }
        if (completableSolverFuture.isDone()) {
            try {
                completableSolverFuture.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Solver thread was interrupted.", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Solver thread has died.", e.getCause());
            }
            throw new IllegalStateException("Solver has finished solving even though it operates in daemon mode.");
        }
    }
}
