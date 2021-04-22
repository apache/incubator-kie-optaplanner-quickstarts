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

package org.acme.maintenancescheduling.domain;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Arrays;
import java.util.List;

@Entity
public class MutuallyExclusiveJobs {

    @Id
    @GeneratedValue
    private Long id;

    private String exclusiveTag;

    @OneToMany(fetch = FetchType.EAGER)
    private List<MaintenanceJob> mutuallyExclusiveJobList;

    public MutuallyExclusiveJobs() {
    }

    public MutuallyExclusiveJobs(String exclusiveTag, MaintenanceJob... mutuallyExclusiveJobs) {
        this.exclusiveTag = exclusiveTag;
        this.mutuallyExclusiveJobList = Arrays.asList(mutuallyExclusiveJobs);
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public boolean isMutuallyExclusive(MaintenanceJob MaintenanceJob, MaintenanceJob otherJob) {
        if (mutuallyExclusiveJobList.contains(MaintenanceJob) && mutuallyExclusiveJobList.contains(otherJob)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "MutuallyExclusiveJobs{" +
                "id=" + id +
                ", exclusiveTag='" + exclusiveTag + '\'' +
                ", mutuallyExclusiveJobList=" + mutuallyExclusiveJobList +
                '}';
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExclusiveTag() {
        return exclusiveTag;
    }

    public void setExclusiveTag(String exclusiveTag) {
        this.exclusiveTag = exclusiveTag;
    }

    public List<MaintenanceJob> getMutuallyExclusiveJobList() {
        return mutuallyExclusiveJobList;
    }

    public void setMutuallyExclusiveJobList(List<MaintenanceJob> mutuallyExclusiveJobs) {
        this.mutuallyExclusiveJobList = mutuallyExclusiveJobs;
    }
}
