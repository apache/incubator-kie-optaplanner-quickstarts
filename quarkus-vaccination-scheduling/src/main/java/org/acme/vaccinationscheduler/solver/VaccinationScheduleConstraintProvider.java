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

import java.util.function.Predicate;

import org.acme.vaccinationscheduler.domain.Person;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintCollectors;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

public class VaccinationScheduleConstraintProvider implements ConstraintProvider {

    // Because the @PlanningVariable is nullable=true, the from() classes needed to be filtered
    private Predicate<Person> personAssignedFilter = (person -> person.getVaccinationSlot() != null);

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                vaccinationSlotCapacity(constraintFactory),
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

    Constraint vaccinationSlotCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory
                .from(Person.class).filter(personAssignedFilter)
                .groupBy(Person::getVaccinationSlot, ConstraintCollectors.count())
                .filter((vaccinationSlot, personCount) -> personCount > vaccinationSlot.getCapacity())
                .penalizeLong("Vaccination slot capacity", HardMediumSoftLongScore.ONE_HARD,
                        (vaccinationSlot, personCount) -> personCount - vaccinationSlot.getCapacity());
    }

    Constraint secondDoseInvalidVaccineType(ConstraintFactory constraintFactory) {
        // If a person is coming for their 2nd dose, use the same vaccine type as their 1st dose.
        return constraintFactory
                .from(Person.class).filter(personAssignedFilter)
                .filter((person -> person.isFirstDoseInjected()
                        && person.getVaccinationSlot().getVaccineType() != person.getFirstDoseVaccineType()))
                .penalize("Second dose invalid vaccine type", HardMediumSoftLongScore.ofHard(1000));
    }

    Constraint secondDoseReadyDate(ConstraintFactory constraintFactory) {
        // If a person is coming for their 2nd dose, don't inject it before the ready day.
        // For example, Pfizer is ready to injected 19 days after the first dose. Moderna after 26 days.
        return constraintFactory
                .from(Person.class).filter(personAssignedFilter)
                .filter(person -> person.isFirstDoseInjected()
                        && person.getVaccinationSlot().getTimeslot().getDate().compareTo(
                                person.getFirstDoseDate().plusDays(
                                        person.getFirstDoseVaccineType().getSecondDoseReadyDays()))
                        < 0)
                .penalizeLong("Second dose ready date", HardMediumSoftLongScore.ONE_HARD,
                        person -> Math.abs(DAYS.between(person.getFirstDoseDate()
                                .plusDays(person.getFirstDoseVaccineType().getSecondDoseReadyDays()),
                                person.getVaccinationSlot().getTimeslot().getDate())));
    }

    Constraint secondDoseIdealDate(ConstraintFactory constraintFactory) {
        // If a person is coming for their 2nd dose, inject it on the ideal day.
        // For example, Pfizer is ideally injected 21 days after the first dose. Moderna after 28 days.
        return constraintFactory
                .from(Person.class).filter(personAssignedFilter)
                .filter(person -> person.isFirstDoseInjected()
                        && !person.getFirstDoseDate()
                        .plusDays(person.getFirstDoseVaccineType().getSecondDoseIdealDays())
                        .equals(person.getVaccinationSlot().getTimeslot().getDate()))
                // 2_000_000 means that to get closer to the ideal day, the person is willing to ride to extra 2km
                // It is 2_000 meters multiplied by distanceCost's soft weight (1000).
                .penalizeLong("Second dose ideal date", HardMediumSoftLongScore.ofSoft(2_000_000),
                        person -> Math.abs(DAYS.between(person.getFirstDoseDate()
                                .plusDays(person.getFirstDoseVaccineType().getSecondDoseIdealDays()),
                                person.getVaccinationSlot().getTimeslot().getDate())));
    }

    Constraint secondDoseMustBeAssigned(ConstraintFactory constraintFactory) {
        // If a person is coming for their 2nd dose, assign them to a dose, regardless of their age.
        return constraintFactory
                .from(Person.class)
                .filter(person -> person.isFirstDoseInjected() && person.getVaccinationSlot() == null)
                .penalize("Second dose must be assigned", HardMediumSoftLongScore.ONE_HARD);
    }

    Constraint assignAllOlderPeople(ConstraintFactory constraintFactory) {
        // Schedule all older people for an injection. This is softer than secondDoseMustBeAssigned().
        return constraintFactory
                .from(Person.class)
                .filter(person -> person.getVaccinationSlot() == null)
                .penalizeLong("Assign all older people", HardMediumSoftLongScore.ONE_MEDIUM, Person::getAge);
    }

    Constraint vaccinationTypeMaximumAge(ConstraintFactory constraintFactory) {
        // Don't inject older people with a vaccine that has maximum age (for example AstraZeneca)
        return constraintFactory
                .from(Person.class).filter(personAssignedFilter)
                .filter(person -> !person.getVaccinationSlot().getVaccineType().isOkForMaximumAge(person.getAge()))
                .penalizeLong("Maximum age of vaccination type", HardMediumSoftLongScore.ONE_HARD,
                        person -> person.getAge() - person.getVaccinationSlot().getVaccineType().getMaximumAge());
    }

    Constraint distanceCost(ConstraintFactory constraintFactory) {
        // Minimize the distance from each person's home location to the vaccination center
        return constraintFactory
                .from(Person.class).filter(personAssignedFilter)
                .penalizeLong("Distance cost", HardMediumSoftLongScore.ofSoft(1000),
                        person -> person.getHomeLocation().getDistanceTo(
                                person.getVaccinationSlot().getVaccinationCenter().getLocation()));
    }

}
