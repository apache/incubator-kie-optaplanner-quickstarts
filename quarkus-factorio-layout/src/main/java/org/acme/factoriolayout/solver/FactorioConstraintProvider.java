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

import org.acme.factoriolayout.bootstrap.FactorioDataGenerator;
import org.acme.factoriolayout.domain.Assembly;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;

import static org.optaplanner.core.api.score.stream.ConstraintCollectors.max;
import static org.optaplanner.core.api.score.stream.ConstraintCollectors.min;
import static org.optaplanner.core.api.score.stream.Joiners.filtering;
import static org.optaplanner.core.api.score.stream.Joiners.lessThan;
import static org.optaplanner.core.api.score.stream.Joiners.lessThanOrEqual;

public class FactorioConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                areaConflict(constraintFactory),
                downstreamY(constraintFactory),
                manhattanDistance(constraintFactory),
                groupRecipesByY(constraintFactory),
//                frontLoadAssemblies(constraintFactory),
                centerAssemblies(constraintFactory),
        };
    }

    Constraint areaConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .fromUniquePair(Assembly.class,
                        Joiners.equal(Assembly::getArea))
                .penalize("Area conflict", HardSoftLongScore.ONE_HARD);
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
                        // At least 1 when upY == downY
                        (down, up) -> up.getArea().getY() - down.getArea().getY() + 1);
    }

    Constraint manhattanDistance(ConstraintFactory constraintFactory) {
        return constraintFactory
                // Downstream assembly
                .from(Assembly.class)
                // Upstream assembly
                .join(Assembly.class,
                        filtering((down, up)-> down.getInputAssemblyList().contains(up)))
                .penalizeLong("Manhattan distance", HardSoftLongScore.ofSoft(1000L),
                        (down, up) -> Math.abs(down.getArea().getX() - up.getArea().getX())
                                + Math.abs(down.getArea().getY() - up.getArea().getY()));
    }

    Constraint groupRecipesByY(ConstraintFactory constraintFactory) {
        return constraintFactory
                .from(Assembly.class)
                .groupBy(Assembly::getRecipe,
                        (Assembly assembly) -> 1,
                        min((Assembly assembly) -> assembly.getArea().getY()),
                        max((Assembly assembly) -> assembly.getArea().getY()))
                .filter((recipe, workaroundConstant, minY, maxY) -> !minY.equals(maxY))
                .penalizeLong("Group recipes by Y", HardSoftLongScore.ofSoft(10L),
                        (recipe, workaroundConstant, minY, maxY) -> (long) (maxY - minY));
    }

    Constraint frontLoadAssemblies(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Assembly.class)
                .penalizeLong("Front load assemblies", HardSoftLongScore.ofSoft(1L),
                        assembly -> assembly.getArea().getY());
    }

    Constraint centerAssemblies(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Assembly.class)
                .penalizeLong("Center assemblies", HardSoftLongScore.ofSoft(1L),
                        assembly -> Math.abs(FactorioDataGenerator.DEFAULT_AREA_WIDTH - assembly.getArea().getX()));
    }

    // TODO make FactorioDataGenerator.DEFAULT_AREA_WIDTH private and use this instead
//    Constraint centerAssembly(ConstraintFactory constraintFactory) {
//        return constraintFactory
//                .from(FactorioLayout.class) // the solution class
//                .join(Assembly.class)
//                .penalizeLong("Center assembly", HardSoftLongScore.ofSoft(1L),
//                        (factorioLayout, assembly) -> Math.abs((factorioLayout.getAreaWidth() / 2) - assembly.getArea().getX()));
//    }

}
