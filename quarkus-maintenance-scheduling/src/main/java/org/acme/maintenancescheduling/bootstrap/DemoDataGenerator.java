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
import org.acme.maintenancescheduling.domain.MaintenanceJobAssignment;
import org.acme.maintenancescheduling.domain.MutuallyExclusiveJobs;
import org.acme.maintenancescheduling.domain.TimeGrain;
import org.acme.maintenancescheduling.persistence.MaintainableUnitRepository;
import org.acme.maintenancescheduling.persistence.MaintenanceCrewRepository;
import org.acme.maintenancescheduling.persistence.MaintenanceJobAssignmentRepository;
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

    @ConfigProperty(name = "schedule.demoData", defaultValue = "SMALL")
    public DemoData demoData;

    public enum DemoData {
        NONE,
        SMALL,
        LARGE
    }

    @Inject
    MaintainableUnitRepository maintainableUnitRepository;
    @Inject
    MaintenanceCrewRepository maintenanceCrewRepository;
    @Inject
    MaintenanceJobRepository maintenanceJobRepository;
    @Inject
    MaintenanceJobAssignmentRepository maintenanceJobAssignmentRepository;
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
        maintainableUnitList.add(new MaintainableUnit("ABC-001"));
        maintainableUnitList.add(new MaintainableUnit("BCD-002"));
        maintainableUnitList.add(new MaintainableUnit("CDE-003"));
        maintainableUnitList.add(new MaintainableUnit("DEF-004"));
        maintainableUnitList.add(new MaintainableUnit("EFG-005"));
        maintainableUnitList.add(new MaintainableUnit("FGH-006"));
        maintainableUnitList.add(new MaintainableUnit("GHI-007"));
        maintainableUnitList.add(new MaintainableUnit("HIJ-008"));

        if (demoData == DemoData.LARGE) {
            maintainableUnitList.add(new MaintainableUnit("IJK-010"));
            maintainableUnitList.add(new MaintainableUnit("JKL-011"));
            maintainableUnitList.add(new MaintainableUnit("KLM-012"));
            maintainableUnitList.add(new MaintainableUnit("LMN-013"));
            maintainableUnitList.add(new MaintainableUnit("MNO-014"));
            maintainableUnitList.add(new MaintainableUnit("NOP-015"));
            maintainableUnitList.add(new MaintainableUnit("OPQ-016"));
            maintainableUnitList.add(new MaintainableUnit("PQR-017"));
        }
        maintainableUnitRepository.persist(maintainableUnitList);

        List<MaintenanceCrew> maintenanceCrewList = new ArrayList<>();
        maintenanceCrewList.add(new MaintenanceCrew("Crew Alpha"));
        maintenanceCrewList.add(new MaintenanceCrew("Crew Beta"));

        maintenanceCrewRepository.persist(maintenanceCrewList);

        List<TimeGrain> timeGrainList = new ArrayList<>();
        for (int i = 0; i <= 48; i++) {
            timeGrainList.add(new TimeGrain(i));
        }
        if (demoData == DemoData.LARGE) {
            for (int i = 49; i <= 96; i++) {
                timeGrainList.add(new TimeGrain(i));
            }
        }
        timeGrainRepository.persist(timeGrainList);

        List<MaintenanceJob> maintenanceJobList = new ArrayList<>();
        maintenanceJobList.add(new MaintenanceJob("Tire change 1", maintainableUnitList.get(0), 0, 8, 1, 2, true));
        maintenanceJobList.add(new MaintenanceJob("Tire change 2", maintainableUnitList.get(1), 0, 8, 1, 2, true));
        maintenanceJobList.add(new MaintenanceJob("Tire change 3", maintainableUnitList.get(2), 0, 8, 1, 2, true));
        maintenanceJobList.add(new MaintenanceJob("Tire change 4", maintainableUnitList.get(3), 0, 8, 1, 2, true));
        maintenanceJobList.add(new MaintenanceJob("Tire change 5", maintainableUnitList.get(4), 24, 32, 1, 2, true));
        maintenanceJobList.add(new MaintenanceJob("Tire change 6", maintainableUnitList.get(5), 24, 32, 1, 2, true));
        maintenanceJobList.add(new MaintenanceJob("Tire change 7", maintainableUnitList.get(6), 24, 32, 1, 2, true));
        maintenanceJobList.add(new MaintenanceJob("Tire change 8", maintainableUnitList.get(7), 24, 32, 1, 2, true));

        maintenanceJobList.add(new MaintenanceJob("Oil change 1", maintainableUnitList.get(0), 0, 8, 2, 2, true));
        maintenanceJobList.add(new MaintenanceJob("Oil change 2", maintainableUnitList.get(1), 0, 8, 2, 2, true));
        maintenanceJobList.add(new MaintenanceJob("Oil change 3", maintainableUnitList.get(2), 24, 32, 2, 2, true));
        maintenanceJobList.add(new MaintenanceJob("Oil change 4", maintainableUnitList.get(3), 24, 32, 2, 2, true));

        maintenanceJobList.add(new MaintenanceJob("Car wash 1", maintainableUnitList.get(4), 0, 8, 1, 2, true));
        maintenanceJobList.add(new MaintenanceJob("Car wash 2", maintainableUnitList.get(5), 0, 8, 1, 2, true));
        maintenanceJobList.add(new MaintenanceJob("Car wash 3", maintainableUnitList.get(6), 24, 32, 1, 2, true));
        maintenanceJobList.add(new MaintenanceJob("Car wash 4", maintainableUnitList.get(7), 24, 32, 1, 2, true));

        if (demoData == DemoData.LARGE) {
            maintenanceJobList.add(new MaintenanceJob("Tire change 9", maintainableUnitList.get(8), 48, 56, 1, 2,
                    true));
            maintenanceJobList.add(new MaintenanceJob("Tire change 10", maintainableUnitList.get(9), 48, 56, 1, 2, true));
            maintenanceJobList.add(new MaintenanceJob("Tire change 11", maintainableUnitList.get(10), 48, 56, 1, 2, true));
            maintenanceJobList.add(new MaintenanceJob("Tire change 12", maintainableUnitList.get(11), 48, 56, 1, 2, true));
            maintenanceJobList.add(new MaintenanceJob("Tire change 13", maintainableUnitList.get(12), 72, 80, 1, 2, true));
            maintenanceJobList.add(new MaintenanceJob("Tire change 14", maintainableUnitList.get(13), 72, 80, 1, 2, true));
            maintenanceJobList.add(new MaintenanceJob("Tire change 15", maintainableUnitList.get(14), 72, 80, 1, 2, true));
            maintenanceJobList.add(new MaintenanceJob("Tire change 16", maintainableUnitList.get(15), 72, 80, 1, 2, true));

            maintenanceJobList.add(new MaintenanceJob("Oil change 5", maintainableUnitList.get(8), 48, 56, 2, 2,
                    true));
            maintenanceJobList.add(new MaintenanceJob("Oil change 6", maintainableUnitList.get(9), 48, 56, 2, 2,
                    true));
            maintenanceJobList.add(new MaintenanceJob("Oil change 7", maintainableUnitList.get(10), 72, 80, 2, 2,
                    true));
            maintenanceJobList.add(new MaintenanceJob("Oil change 8", maintainableUnitList.get(11), 72, 80, 2, 2,
                    true));

            maintenanceJobList.add(new MaintenanceJob("Car wash 5", maintainableUnitList.get(12), 48, 56, 1, 2, true));
            maintenanceJobList.add(new MaintenanceJob("Car wash 6", maintainableUnitList.get(13), 48, 56, 1, 2, true));
            maintenanceJobList.add(new MaintenanceJob("Car wash 7", maintainableUnitList.get(14), 72, 80, 1, 2, true));
            maintenanceJobList.add(new MaintenanceJob("Car wash 8", maintainableUnitList.get(15), 72, 80, 1, 2, true));

            maintenanceJobList.add(new MaintenanceJob("Wax vehicle 1", maintainableUnitList.get(0), 0, 8, 2, 2,
                    false));
            maintenanceJobList.add(new MaintenanceJob("Wax vehicle 2", maintainableUnitList.get(1), 0, 8, 2, 2,
                    false));
            maintenanceJobList.add(new MaintenanceJob("Wax vehicle 3", maintainableUnitList.get(2), 24, 32, 2, 2, false));
            maintenanceJobList.add(new MaintenanceJob("Wax vehicle 4", maintainableUnitList.get(3), 24, 32, 2, 2, false));
            maintenanceJobList.add(new MaintenanceJob("Wax vehicle 5", maintainableUnitList.get(4), 48, 56, 2, 2, false));
            maintenanceJobList.add(new MaintenanceJob("Wax vehicle 6", maintainableUnitList.get(5), 48, 56, 2, 2, false));
            maintenanceJobList.add(new MaintenanceJob("Wax vehicle 7", maintainableUnitList.get(6), 72, 80, 2, 2, false));
            maintenanceJobList.add(new MaintenanceJob("Wax vehicle 8", maintainableUnitList.get(7), 72, 80, 2, 2, false));

            maintenanceJobList.add(new MaintenanceJob("Battery inspection 1", maintainableUnitList.get(8), 0, 8, 1, 2, false));
            maintenanceJobList.add(new MaintenanceJob("Battery inspection 2", maintainableUnitList.get(9), 0, 8, 1, 2, false));
            maintenanceJobList.add(new MaintenanceJob("Battery inspection 3", maintainableUnitList.get(10), 24, 32, 1, 2, false));
            maintenanceJobList.add(new MaintenanceJob("Battery inspection 4", maintainableUnitList.get(11), 24, 32, 1, 2, false));
            maintenanceJobList.add(new MaintenanceJob("Battery inspection 5", maintainableUnitList.get(12), 48, 56, 1, 2, false));
            maintenanceJobList.add(new MaintenanceJob("Battery inspection 6", maintainableUnitList.get(13), 48, 56, 1, 2, false));
            maintenanceJobList.add(new MaintenanceJob("Battery inspection 7", maintainableUnitList.get(14), 72, 80, 1, 2, false));
            maintenanceJobList.add(new MaintenanceJob("Battery inspection 8", maintainableUnitList.get(15), 72, 80, 1, 2, false));
        }
        maintenanceJobRepository.persist(maintenanceJobList);

        List<MaintenanceJobAssignment> maintenanceJobAssignmentList = new ArrayList<>();
        for (MaintenanceJob job : maintenanceJobList) {
            maintenanceJobAssignmentList.add(new MaintenanceJobAssignment(job));
        }
        maintenanceJobAssignmentRepository.persist(maintenanceJobAssignmentList);

        List<MutuallyExclusiveJobs> mutuallyExclusiveJobsList = new ArrayList<>();
        if (demoData == DemoData.SMALL) {
            mutuallyExclusiveJobsList.add(new MutuallyExclusiveJobs("Tire change", maintenanceJobList.get(0),
                    maintenanceJobList.get(1), maintenanceJobList.get(2), maintenanceJobList.get(3), maintenanceJobList.get(4),
                    maintenanceJobList.get(5), maintenanceJobList.get(6), maintenanceJobList.get(7)));

            mutuallyExclusiveJobsList.add(new MutuallyExclusiveJobs("Oil change", maintenanceJobList.get(8),
                    maintenanceJobList.get(9), maintenanceJobList.get(10), maintenanceJobList.get(11)));

            mutuallyExclusiveJobsList.add(new MutuallyExclusiveJobs("Car wash", maintenanceJobList.get(12),
                    maintenanceJobList.get(13), maintenanceJobList.get(14), maintenanceJobList.get(15)));
        }
        else if (demoData == DemoData.LARGE) {
            mutuallyExclusiveJobsList.add(new MutuallyExclusiveJobs("Tire change", maintenanceJobList.get(0),
                    maintenanceJobList.get(1), maintenanceJobList.get(2), maintenanceJobList.get(3), maintenanceJobList.get(4),
                    maintenanceJobList.get(5), maintenanceJobList.get(6), maintenanceJobList.get(7), maintenanceJobList.get(16),
                    maintenanceJobList.get(17), maintenanceJobList.get(18), maintenanceJobList.get(19),
                    maintenanceJobList.get(20), maintenanceJobList.get(21), maintenanceJobList.get(22),
                    maintenanceJobList.get(23)));
            mutuallyExclusiveJobsList.add(new MutuallyExclusiveJobs("Oil change", maintenanceJobList.get(8),
                    maintenanceJobList.get(9), maintenanceJobList.get(10), maintenanceJobList.get(11), maintenanceJobList.get(24),
                    maintenanceJobList.get(25), maintenanceJobList.get(26), maintenanceJobList.get(27)));
            mutuallyExclusiveJobsList.add(new MutuallyExclusiveJobs("Car wash", maintenanceJobList.get(12),
                    maintenanceJobList.get(13), maintenanceJobList.get(14), maintenanceJobList.get(15), maintenanceJobList.get(28),
                    maintenanceJobList.get(29), maintenanceJobList.get(30), maintenanceJobList.get(31)));
        }
        mutuallyExclusiveJobsRepository.persist(mutuallyExclusiveJobsList);
    }
}
