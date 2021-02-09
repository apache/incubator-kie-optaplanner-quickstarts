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
                secondDoseInvalidVaccineType(constraintFactory),
                secondDoseReadyDate(constraintFactory),
                secondDoseIdealDate(constraintFactory),
                secondDoseMustBeAssigned(constraintFactory),
                assignAllOlderPeople(constraintFactory),
                vaccinationTypeMaximumAge(constraintFactory),
                distanceCost(constraintFactory),
//                elderlyFirstPerVaccinationCenter(constraintFactory)
        };
    }

    Constraint personConflict(ConstraintFactory constraintFactory) {
        // Don't assign a person to two injections in the same planning window
        // In this implementation, a planning window is at most 2 weeks,
        // so the 1st and 2nd dose won't be in the same schedule (this can be fixed by changing the model).
        return constraintFactory
                .fromUniquePair(Injection.class,
                        Joiners.equal(Injection::getPerson))
                .penalize("Person conflict", HardMediumSoftLongScore.ofHard(1000));
    }

    Constraint secondDoseInvalidVaccineType(ConstraintFactory constraintFactory) {
        // If a person is coming for their 2nd dose, use the same vaccine type as their 1st dose.
        return constraintFactory
                .from(Injection.class)
                .filter((injection -> injection.getPerson().isFirstDoseInjected()
                        && injection.getVaccineType() != injection.getPerson().getFirstDoseVaccineType()))
                .penalize("Second dose invalid vaccine type", HardMediumSoftLongScore.ofHard(1000));
    }

    Constraint secondDoseReadyDate(ConstraintFactory constraintFactory) {
        // If a person is coming for their 2nd dose, don't inject it before the ready day.
        // For example, Pfizer is ready to injected 19 days after the first dose. Moderna after 26 days.
        return constraintFactory
                .from(Injection.class)
                .filter(injection -> injection.getPerson().isFirstDoseInjected()
                        && injection.getDateTime().toLocalDate().compareTo(
                                injection.getPerson().getFirstDoseDate().plusDays(
                                        injection.getPerson().getFirstDoseVaccineType().getSecondDoseReadyDays()))
                        < 0)
                .penalizeLong("Second dose ready date", HardMediumSoftLongScore.ONE_HARD,
                        injection -> Math.abs(DAYS.between(injection.getPerson().getFirstDoseDate()
                                .plusDays(injection.getPerson().getFirstDoseVaccineType().getSecondDoseReadyDays()),
                                injection.getDateTime())));
    }

    Constraint secondDoseIdealDate(ConstraintFactory constraintFactory) {
        // If a person is coming for their 2nd dose, inject it on the ideal day.
        // For example, Pfizer is ideally injected 21 days after the first dose. Moderna after 28 days.
        return constraintFactory
                .from(Injection.class)
                .filter(injection -> injection.getPerson().isFirstDoseInjected()
                        && !injection.getPerson().getFirstDoseDate()
                        .plusDays(injection.getPerson().getFirstDoseVaccineType().getSecondDoseIdealDays())
                        .equals(injection.getDateTime().toLocalDate()))
                // 2_000_000 means that to get closer to the ideal day, the person is willing to ride to extra 2km
                // It is 2_000 meters multiplied by distanceCost's soft weight (1000).
                .penalizeLong("Second dose ideal date", HardMediumSoftLongScore.ofSoft(2_000_000),
                        injection -> Math.abs(DAYS.between(injection.getPerson().getFirstDoseDate()
                                .plusDays(injection.getPerson().getFirstDoseVaccineType().getSecondDoseIdealDays()),
                                injection.getDateTime())));
    }

    Constraint secondDoseMustBeAssigned(ConstraintFactory constraintFactory) {
        // If a person is coming for their 2nd dose, assign them to a dose, regardless of their age.
        return constraintFactory
                .from(Person.class)
                .filter(Person::isFirstDoseInjected)
                .ifNotExists(Injection.class, Joiners.equal(person -> person, Injection::getPerson))
                .penalize("Second dose must be assigned", HardMediumSoftLongScore.ONE_HARD);
    }

    Constraint assignAllOlderPeople(ConstraintFactory constraintFactory) {
        // Schedule all older people for an injection. This is softer than secondDoseMustBeAssigned().
        return constraintFactory
                .from(Person.class)
                .ifNotExists(Injection.class, Joiners.equal(person -> person, Injection::getPerson))
                .penalizeLong("Assign all older people", HardMediumSoftLongScore.ONE_MEDIUM, Person::getAge);
    }

    Constraint vaccinationTypeMaximumAge(ConstraintFactory constraintFactory) {
        // Don't inject older people with a vaccine that has maximum age (for example AstraZeneca)
        return constraintFactory
                .from(Injection.class)
                .filter(injection -> !injection.getVaccineType().isOkForMaximumAge(injection.getPerson().getAge()))
                .penalize("Maximum age of vaccination type", HardMediumSoftLongScore.ONE_HARD,
                        injection -> injection.getPerson().getAge() - injection.getVaccineType().getMaximumAge());
    }

    Constraint distanceCost(ConstraintFactory constraintFactory) {
        // Minimize the distance from each person's home location to the vaccination center
        return constraintFactory
                .from(Injection.class)
                .penalizeLong("Distance cost", HardMediumSoftLongScore.ofSoft(1000),
                        injection -> injection.getPerson().getHomeLocation().getDistanceTo(
                                injection.getVaccinationCenter().getLocation()));
    }

    // TODO This works, but it hurts performance and there are better ways to write this
    // Do not confuse with assignAllOlderPeople
//    Constraint elderlyFirstPerVaccinationCenter(ConstraintFactory constraintFactory) {
//        Predicate<Injection> firstDosePredicate = (injection) -> !injection.getPerson().isFirstDoseInjected();
//        return constraintFactory
//                .from(Injection.class).filter(firstDosePredicate)
//                .join(constraintFactory.from(Injection.class).filter(firstDosePredicate),
//                        Joiners.equal(Injection::getVaccinationCenter),
//                        Joiners.greaterThan(injection -> injection.getPerson().getAge()),
//                        Joiners.lessThan(Injection::getDateTime))
//                .penalize("Elderly first per vaccination center", HardMediumSoftLongScore.ONE_SOFT);
//    }

}
