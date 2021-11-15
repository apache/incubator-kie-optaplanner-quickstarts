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

package org.acme.employeescheduling.bootstrap;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.acme.employeescheduling.domain.Availability;
import org.acme.employeescheduling.domain.AvailabilityType;
import org.acme.employeescheduling.domain.Employee;
import org.acme.employeescheduling.domain.Shift;
import org.acme.employeescheduling.persistence.AvailabilityRepository;
import org.acme.employeescheduling.persistence.EmployeeRepository;
import org.acme.employeescheduling.persistence.ShiftRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class DemoDataGenerator {

    @ConfigProperty(name = "schedule.demoData", defaultValue = "SMALL")
    DemoData demoData;

    public enum DemoData {
        NONE,
        SMALL,
        LARGE
    }

    @Inject
    EmployeeRepository employeeRepository;
    @Inject
    AvailabilityRepository availabilityRepository;
    @Inject
    ShiftRepository shiftRepository;

    @Transactional
    public void generateDemoData(@Observes StartupEvent startupEvent) {
        if (demoData == DemoData.NONE) {
            return;
        }
        final String[] FIRST_NAMES = {"Amy", "Beth", "Chad", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy", "Jay"};
        final String[] LAST_NAMES = {"Cole", "Fox", "Green", "Jones", "King", "Li", "Poe", "Rye", "Smith", "Watt"};
        final String[] SKILLS = { "Anaesthetics", "Cardiology"};
        final int INITIAL_ROSTER_LENGTH_IN_DAYS = 14;
        final LocalDate START_DATE = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

        Random random = new Random(0);
        List<String> namePermutations = joinAllCombinations(FIRST_NAMES, LAST_NAMES);
        Collections.shuffle(namePermutations, random);

        List<Employee> employeeList = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Set<String> skills = pickSubset(List.of(SKILLS), random, 3, 1);
            Employee employee = new Employee(namePermutations.get(i), skills);
            employeeRepository.persist(employee);
            employeeList.add(employee);
        }

        for (int i = 0; i < INITIAL_ROSTER_LENGTH_IN_DAYS; i++) {
            Set<Employee> employeesWithAvailabitiesOnDay = pickSubset(employeeList, random, 4, 3, 2, 1);
            LocalDate date = START_DATE.plusDays(i);
            for (Employee employee : employeesWithAvailabitiesOnDay) {
                AvailabilityType availabilityType = pickRandom(AvailabilityType.values(), random);
                availabilityRepository.persist(new Availability(employee, date, availabilityType));
            }

            generateShiftsForDay(date, SKILLS, random);
        }
    }

    private void generateShiftsForDay(LocalDate date, String[] skills, Random random) {
        LocalDateTime dayStartTime = date.atTime(LocalTime.of(9, 0));
        LocalDateTime dayEndTime = date.atTime(LocalTime.of(17, 0));
        LocalDateTime afternoonStartTime = date.atTime(LocalTime.of(13, 0));
        LocalDateTime afternoonEndTime = date.atTime(LocalTime.of(21, 0));

        String[] LOCATIONS = { "Ambulatory care", "Critical care", "Pediatric care"};

        int dayShiftCount = 1 + random.nextInt(4);
        int afternoonShiftCount = 1 + random.nextInt(4);

        for (int i = 0; i < dayShiftCount; i++) {
            String requiredSkill = pickRandom(skills, random);
            String location = pickRandom(LOCATIONS, random);

            shiftRepository.persist(new Shift(dayStartTime, dayEndTime, location, requiredSkill));
        }
        for (int i = 0; i < afternoonShiftCount; i++) {
            String requiredSkill = pickRandom(skills, random);
            String location = pickRandom(LOCATIONS, random);

            shiftRepository.persist(new Shift(afternoonStartTime, afternoonEndTime, location, requiredSkill));
        }
    }

    private <T> T pickRandom(T[] source, Random random) {
        return source[random.nextInt(source.length)];
    }

    private <T> Set<T> pickSubset(List<T> sourceSet, Random random, int... distribution) {
        int probabilitySum = 0;
        for (int probability : distribution) {
            probabilitySum += probability;
        }
        int choice = random.nextInt(probabilitySum);
        int numOfItems = 0;
        while (choice >= distribution[numOfItems]) {
            choice -= distribution[numOfItems];
            numOfItems++;
        }
        List<T> items = new ArrayList<>(sourceSet);
        Collections.shuffle(items, random);
        return new HashSet<>(items.subList(0, numOfItems + 1));
    }

    private List<String> joinAllCombinations(String[]... partArrays) {
        int size = 1;
        for (String[] partArray : partArrays) {
            size *= partArray.length;
        }
        List<String> out = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            StringBuilder item = new StringBuilder();
            int sizePerIncrement = 1;
            for (String[] partArray : partArrays) {
                item.append(' ');
                item.append(partArray[(i / sizePerIncrement) % partArray.length]);
                sizePerIncrement *= partArray.length;
            }
            item.delete(0,1);
            out.add(item.toString());
        }
        return out;
    }

}
