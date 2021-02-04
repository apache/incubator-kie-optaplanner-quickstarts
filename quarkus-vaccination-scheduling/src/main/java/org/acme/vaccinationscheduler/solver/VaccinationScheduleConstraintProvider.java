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

package org.acme.vaccinationscheduler.solver;

import static java.time.temporal.ChronoUnit.DAYS;

import org.acme.vaccinationscheduler.domain.Injection;
import org.acme.vaccinationscheduler.domain.Person;
import org.acme.vaccinationscheduler.domain.VaccineType;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;

public class VaccinationScheduleConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                personConflict(constraintFactory),
                ageLimitAstrazeneca(constraintFactory),
                secondShotInvalidVaccineType(constraintFactory),
                secondShotMustBeAssigned(constraintFactory),
                assignAllOlderPeople(constraintFactory),
                secondShotIdealDay(constraintFactory),
                distanceCost(constraintFactory),
//                elderlyFirstPerVaccinationCenter(constraintFactory)
        };
    }

    Constraint personConflict(ConstraintFactory constraintFactory) {
        // Don't assign a person to two injections in the same planning window
        // In this implementation, a planning window is at most 2 weeks,
        // so the 1th and 2nd shot won't be in the same schedule (this can be fixed by changing the model).
        return constraintFactory
                .fromUniquePair(Injection.class,
                        Joiners.equal(Injection::getPerson))
                .penalize("Person conflict", HardMediumSoftLongScore.ofHard(100));
    }

    Constraint ageLimitAstrazeneca(ConstraintFactory constraintFactory) {
        // Don't inject older people with AstraZeneca
        return constraintFactory
                .from(Injection.class)
                .filter(injection -> injection.getPerson().getAge() >= 55
                        && injection.getVaccineType() == VaccineType.ASTRAZENECA)
                .penalize("Age limit AstraZeneca", HardMediumSoftLongScore.ONE_HARD);
    }

    Constraint secondShotInvalidVaccineType(ConstraintFactory constraintFactory) {
        // If a person is coming for their 2nd shot, use the same vaccine type as their 1th shot.
        return constraintFactory
                .from(Injection.class)
                .filter((injection -> injection.getPerson().isFirstShotInjected()
                        && injection.getVaccineType() != injection.getPerson().getFirstShotVaccineType()))
                .penalize("Second shot invalid vaccine type", HardMediumSoftLongScore.ofHard(100));
    }

    Constraint secondShotMustBeAssigned(ConstraintFactory constraintFactory) {
        // If a person is coming for their 2nd shot, assign them to a shot, regardless of their age.
        return constraintFactory
                .from(Person.class)
                .filter(Person::isFirstShotInjected)
                .ifNotExists(Injection.class, Joiners.equal(person -> person, Injection::getPerson))
                .penalize("Second shot must be assigned", HardMediumSoftLongScore.ONE_HARD);
    }

    Constraint assignAllOlderPeople(ConstraintFactory constraintFactory) {
        // Schedule all older people for an injection. This is softer than secondShotMustBeAssigned().
        return constraintFactory
                .from(Person.class)
                .ifNotExists(Injection.class, Joiners.equal(person -> person, Injection::getPerson))
                .penalizeLong("Assign all older people", HardMediumSoftLongScore.ONE_MEDIUM, Person::getAge);
    }

    Constraint secondShotIdealDay(ConstraintFactory constraintFactory) {
        // If a person is coming for their 2nd shot, inject it on the ideal day.
        // For example, Pfizer is ideally injected 21 days after the first shot. Moderna after 28 days.
        return constraintFactory
                .from(Injection.class)
                .filter(injection -> injection.getPerson().isFirstShotInjected()
                        && !injection.getPerson().getSecondShotIdealDate().equals(injection.getDateTime().toLocalDate()))
                // 2_000_000 means that to get closer to the ideal day, the person is willing to ride to extra 2km
                // It is 2_000 meters multiplied by distanceCost's soft weight (1000).
                .penalizeLong("Second shot ideal date", HardMediumSoftLongScore.ofSoft(2_000_000),
                        injection -> Math.abs(DAYS.between(injection.getPerson().getSecondShotIdealDate(),
                                injection.getDateTime())));
    }

    Constraint distanceCost(ConstraintFactory constraintFactory) {
        // Minimize the distance from each person's home location to the vaccination center
        return constraintFactory
                .from(Injection.class)
                .penalizeLong("Distance cost", HardMediumSoftLongScore.ofSoft(1000),
                        injection -> injection.getPerson().getHomeLocation().getDistanceTo(
                                injection.getVaccinationCenter().getLocation()));
    }

//    Constraint elderlyFirstPerVaccinationCenter(ConstraintFactory constraintFactory) {
//        Predicate<Injection> firstShotPredicate = (injection) -> !injection.getPerson().isFirstShotInjected();
//        return constraintFactory
//                .from(Injection.class).filter(firstShotPredicate)
//                .join(constraintFactory.from(Injection.class).filter(firstShotPredicate),
//                        Joiners.equal(Injection::getVaccinationCenter),
//                        Joiners.greaterThan(injection -> injection.getPerson().getAge()),
//                        Joiners.lessThan(Injection::getDateTime))
//                .penalize("Elderly first per vaccination center", HardMediumSoftLongScore.ONE_SOFT);
//    }

}
