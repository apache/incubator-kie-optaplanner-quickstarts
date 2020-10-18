/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.acme.factoriolayout.solver;

import org.acme.factoriolayout.domain.Assembly;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;

import static org.optaplanner.core.api.score.stream.Joiners.filtering;
import static org.optaplanner.core.api.score.stream.Joiners.lessThan;
import static org.optaplanner.core.api.score.stream.Joiners.lessThanOrEqual;

public class FactorioConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
            areaConflict(constraintFactory),
            manhattanDistance(constraintFactory),
            downstreamY(constraintFactory)
        };
    }

    Constraint areaConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .fromUniquePair(Assembly.class,
                        Joiners.equal(Assembly::getArea))
                .penalize("Area conflict", HardSoftLongScore.ONE_HARD);
    }

    Constraint manhattanDistance(ConstraintFactory constraintFactory) {
        return constraintFactory
                // Downstream assembly
                .from(Assembly.class)
                // Upstream assembly
                .join(Assembly.class,
                        filtering((down, up)-> down.getInputAssemblyList().contains(up)))
                .penalizeLong("Manhattan distance", HardSoftLongScore.ONE_SOFT,
                        (down, up) -> Math.abs(down.getArea().getX() - up.getArea().getX())
                                    + Math.abs(down.getArea().getY() - up.getArea().getY()));
    }

    Constraint downstreamY(ConstraintFactory constraintFactory) {
        return constraintFactory
                // Downstream assembly
                .from(Assembly.class)
                // Upstream assembly
                .join(Assembly.class,
                        lessThanOrEqual(assembly -> assembly.getArea().getY()),
                        filtering((down, up)-> down.getInputAssemblyList().contains(up)))
                .penalizeLong("Downstream y", HardSoftLongScore.ONE_HARD,
                        (down, up) -> up.getArea().getY() - down.getArea().getY());
    }

}
