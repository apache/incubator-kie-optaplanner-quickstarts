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

package org.acme.maintenancescheduling.bootstrap;

import io.quarkus.runtime.StartupEvent;
import org.acme.maintenancescheduling.domain.MaintainableUnit;
import org.acme.maintenancescheduling.domain.MaintenanceCrew;
import org.acme.maintenancescheduling.domain.MaintenanceJob;
import org.acme.maintenancescheduling.domain.MutuallyExclusiveJobs;
import org.acme.maintenancescheduling.domain.TimeGrain;
import org.acme.maintenancescheduling.persistence.MaintainableUnitRepository;
import org.acme.maintenancescheduling.persistence.MaintenanceCrewRepository;
import org.acme.maintenancescheduling.persistence.MaintenanceJobRepository;
import org.acme.maintenancescheduling.persistence.MutuallyExclusiveJobsRepository;
import org.acme.maintenancescheduling.persistence.TimeGrainRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class DemoDataGenerator {

    @ConfigProperty(name = "schedule.demoData", defaultValue = "SMALLEST")
    public DemoData demoData;

    public enum DemoData {
        NONE,
        SMALL,
        SMALLEST
    }

    @Inject
    MaintainableUnitRepository maintainableUnitRepository;
    @Inject
    MaintenanceCrewRepository maintenanceCrewRepository;
    @Inject
    MaintenanceJobRepository maintenanceJobRepository;
    @Inject
    MutuallyExclusiveJobsRepository mutuallyExclusiveJobsRepository;
    @Inject
    TimeGrainRepository timeGrainRepository;

    @Transactional
    public void generateDemoData(@Observes StartupEvent startupEvent) {
        if (demoData == DemoData.NONE) {
            return;
        }

        List<MaintainableUnit> maintainableUnitList = new ArrayList<>();
        maintainableUnitList.add(new MaintainableUnit("Toyota Corolla 1"));
        maintainableUnitList.add(new MaintainableUnit("Toyota Corolla 2"));
        maintainableUnitList.add(new MaintainableUnit("Toyota Corolla 3"));
        maintainableUnitList.add(new MaintainableUnit("Ford Focus 1"));
        maintainableUnitList.add(new MaintainableUnit("Ford Focus 2"));
        maintainableUnitList.add(new MaintainableUnit("Ford Focus 3"));
        maintainableUnitList.add(new MaintainableUnit("Honda Civic 1"));
        maintainableUnitList.add(new MaintainableUnit("Honda Civic 2"));
        maintainableUnitList.add(new MaintainableUnit("Honda Civic 3"));
        if (demoData == DemoData.SMALL) {
            maintainableUnitList.add(new MaintainableUnit("Toyota Corolla 4"));
            maintainableUnitList.add(new MaintainableUnit("Toyota Corolla 5"));
            maintainableUnitList.add(new MaintainableUnit("Toyota Corolla 6"));
            maintainableUnitList.add(new MaintainableUnit("Ford Focus 4"));
            maintainableUnitList.add(new MaintainableUnit("Ford Focus 5"));
            maintainableUnitList.add(new MaintainableUnit("Ford Focus 6"));
            maintainableUnitList.add(new MaintainableUnit("Honda Civic 4"));
            maintainableUnitList.add(new MaintainableUnit("Honda Civic 5"));
            maintainableUnitList.add(new MaintainableUnit("Honda Civic 6"));
        }
        maintainableUnitRepository.persist(maintainableUnitList);

        List<MaintenanceCrew> maintenanceCrewList = new ArrayList<>();
        maintenanceCrewList.add(new MaintenanceCrew("Crew 1"));
        maintenanceCrewList.add(new MaintenanceCrew("Crew 2"));
        maintenanceCrewList.add(new MaintenanceCrew("Crew 3"));
        if (demoData == DemoData.SMALL) {
            maintenanceCrewList.add(new MaintenanceCrew("Crew 4"));
            maintenanceCrewList.add(new MaintenanceCrew("Crew 5"));
            maintenanceCrewList.add(new MaintenanceCrew("Crew 6"));
        }
        maintenanceCrewRepository.persist(maintenanceCrewList);

        List<TimeGrain> timeGrainList = new ArrayList<>();
        for (int i = 0; i <= 24; i++) {
            timeGrainList.add(new TimeGrain(i));
        }
        if (demoData == DemoData.SMALL) {
            for (int i = 25; i <= 48; i++) {
                timeGrainList.add(new TimeGrain(i));
            }
        }
        timeGrainRepository.persist(timeGrainList);

        List<MaintenanceJob> maintenanceJobList = new ArrayList<>();
        maintenanceJobList.add(new MaintenanceJob("Tire change 1", maintainableUnitList.get(0), 0, 24, 1, true));
        maintenanceJobList.add(new MaintenanceJob("Tire change 2", maintainableUnitList.get(1), 0, 24, 1, true));
        maintenanceJobList.add(new MaintenanceJob("Tire change 3", maintainableUnitList.get(2), 0, 24, 1, true));
        maintenanceJobList.add(new MaintenanceJob("Oil change 1", maintainableUnitList.get(3), 8, 24, 2, true));
        maintenanceJobList.add(new MaintenanceJob("Oil change 2", maintainableUnitList.get(4), 8, 24, 2, true));
        maintenanceJobList.add(new MaintenanceJob("Oil change 3", maintainableUnitList.get(5), 8, 24, 2, true));
        maintenanceJobList.add(new MaintenanceJob("Lights inspection 1", maintainableUnitList.get(6), 0, 24, 4, true));
        maintenanceJobList.add(new MaintenanceJob("Lights inspection 2", maintainableUnitList.get(7), 0, 24, 4, true));
        maintenanceJobList.add(new MaintenanceJob("Lights inspection 3", maintainableUnitList.get(8), 0, 24, 4, true));
        maintenanceJobList.add(new MaintenanceJob("Transmission replacement 1", maintainableUnitList.get(0), 0, 24, 8, true));
        if (demoData == DemoData.SMALL) {
            maintenanceJobList.add(new MaintenanceJob("Tire change 4", maintainableUnitList.get(9), 24, 48, 1, true));
            maintenanceJobList.add(new MaintenanceJob("Tire change 5", maintainableUnitList.get(10), 24, 48, 1, true));
            maintenanceJobList.add(new MaintenanceJob("Tire change 6", maintainableUnitList.get(11), 24, 48, 1, true));
            maintenanceJobList.add(new MaintenanceJob("Oil change 4", maintainableUnitList.get(12), 32, 48, 2, true));
            maintenanceJobList.add(new MaintenanceJob("Oil change 5", maintainableUnitList.get(13), 32, 48, 2, true));
            maintenanceJobList.add(new MaintenanceJob("Oil change 6", maintainableUnitList.get(14), 32, 48, 2, true));
            maintenanceJobList.add(new MaintenanceJob("Lights inspection 4", maintainableUnitList.get(15), 24, 48, 4, true));
            maintenanceJobList.add(new MaintenanceJob("Lights inspection 5", maintainableUnitList.get(16), 24, 48, 4, true));
            maintenanceJobList.add(new MaintenanceJob("Lights inspection 6", maintainableUnitList.get(17), 24, 48, 4, true));
            maintenanceJobList.add(new MaintenanceJob("Transmission replacement 2", maintainableUnitList.get(9), 24, 48, 8, true));

            maintenanceJobList.add(new MaintenanceJob("Wax vehicle 1", maintainableUnitList.get(0), 0, 48, 1, false));
            maintenanceJobList.add(new MaintenanceJob("Wax vehicle 2", maintainableUnitList.get(1), 0, 48, 1, false));
            maintenanceJobList.add(new MaintenanceJob("Wax vehicle 3", maintainableUnitList.get(2), 0, 48, 1, false));
            maintenanceJobList.add(new MaintenanceJob("Air filter inspection 1", maintainableUnitList.get(3), 8, 48, 2, false));
            maintenanceJobList.add(new MaintenanceJob("Air filter inspection 2", maintainableUnitList.get(4), 8, 48, 2, false));
            maintenanceJobList.add(new MaintenanceJob("Air filter inspection 3", maintainableUnitList.get(5), 8, 48, 2, false));
            maintenanceJobList.add(new MaintenanceJob("Tire pressure check 1", maintainableUnitList.get(6), 0, 48, 4, false));
            maintenanceJobList.add(new MaintenanceJob("Tire pressure check 2", maintainableUnitList.get(7), 0, 48, 4, false));
            maintenanceJobList.add(new MaintenanceJob("Tire pressure check 3", maintainableUnitList.get(8), 0, 48, 4, false));
            maintenanceJobList.add(new MaintenanceJob("Battery inspection 1", maintainableUnitList.get(0), 0, 48, 8, false));
        }
        maintenanceJobRepository.persist(maintenanceJobList);

        List<MutuallyExclusiveJobs> mutuallyExclusiveJobsList = new ArrayList<>();
        mutuallyExclusiveJobsList.add(
                new MutuallyExclusiveJobs(maintenanceJobList.get(0), maintenanceJobList.get(1), maintenanceJobList.get(2)));
        mutuallyExclusiveJobsList.add(
                new MutuallyExclusiveJobs(maintenanceJobList.get(3), maintenanceJobList.get(4), maintenanceJobList.get(5)));
        mutuallyExclusiveJobsList.add(
                new MutuallyExclusiveJobs(maintenanceJobList.get(6), maintenanceJobList.get(7), maintenanceJobList.get(8)));
        if (demoData == DemoData.SMALL) {
            mutuallyExclusiveJobsList.add(new MutuallyExclusiveJobs(maintenanceJobList.get(10), maintenanceJobList.get(11),
                    maintenanceJobList.get(12)));
            mutuallyExclusiveJobsList.add(new MutuallyExclusiveJobs(maintenanceJobList.get(13), maintenanceJobList.get(14),
                    maintenanceJobList.get(15)));
            mutuallyExclusiveJobsList.add(new MutuallyExclusiveJobs(maintenanceJobList.get(16), maintenanceJobList.get(17),
                    maintenanceJobList.get(18)));
        }
        mutuallyExclusiveJobsRepository.persist(mutuallyExclusiveJobsList);
    }
}
