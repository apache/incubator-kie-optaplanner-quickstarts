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

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.acme.callcenter.domain.Agent;
import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.solver.change.AddCallProblemChange;
import org.acme.callcenter.solver.change.PinCallProblemChange;
import org.acme.callcenter.solver.change.ProlongCallByMinuteProblemChange;
import org.acme.callcenter.solver.change.RemoveCallProblemChange;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.api.solver.SolverStatus;
import org.optaplanner.core.api.solver.change.ProblemChange;

@ApplicationScoped
public class SolverService {

    private final SolverManager<CallCenter, Long> solverManager;
    public static final long SINGLETON_ID = 1L;

    private final BlockingQueue<ProblemChange<CallCenter>> waitingProblemChanges = new LinkedBlockingQueue<>();

    @Inject
    public SolverService(SolverManager<CallCenter, Long> solverManager) {
        this.solverManager = solverManager;
    }

    private void pinCallAssignedToAgents(List<Call> calls) {
        calls.stream()
                .filter(call -> !call.isPinned()
                        && call.getPreviousCallOrAgent() != null
                        && call.getPreviousCallOrAgent() instanceof Agent)
                .map(PinCallProblemChange::new)
                .forEach(problemChange -> solverManager.addProblemChange(SINGLETON_ID, problemChange));
    }

    public void startSolving(CallCenter inputProblem,
            Consumer<CallCenter> bestSolutionConsumer, Consumer<Throwable> errorHandler) {
        solverManager.solveAndListen(SINGLETON_ID, id -> inputProblem, bestSolution -> {
                                         if (bestSolution.getScore().isSolutionInitialized()) {
                                             bestSolutionConsumer.accept(bestSolution);
                                             pinCallAssignedToAgents(bestSolution.getCalls());
                                         }
                                     },
                                     (id, error) -> errorHandler.accept(error));
        waitingProblemChanges.forEach(problemChange -> solverManager.addProblemChange(SINGLETON_ID, problemChange));
        waitingProblemChanges.clear();
    }

    public void stopSolving() {
        solverManager.terminateEarly(SINGLETON_ID);
    }

    public boolean isSolving() {
        return solverManager.getSolverStatus(SINGLETON_ID) != SolverStatus.NOT_SOLVING;
    }

    public void addCall(Call call) {
        registerProblemChange(new AddCallProblemChange(call));
    }

    public void removeCall(long callId) {
        registerProblemChange(new RemoveCallProblemChange(callId));
    }

    public void prolongCall(long callId) {
        registerProblemChange(new ProlongCallByMinuteProblemChange(callId));
    }

    private void registerProblemChange(ProblemChange<CallCenter> problemChange) {
        if (isSolving()) {
            solverManager.addProblemChange(SINGLETON_ID, problemChange);
        } else {
            waitingProblemChanges.add(problemChange);
        }
    }
}
