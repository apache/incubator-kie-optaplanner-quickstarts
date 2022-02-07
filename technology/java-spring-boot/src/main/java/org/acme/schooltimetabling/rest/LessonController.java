/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.acme.schooltimetabling.rest;

import java.util.List;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.TimeTable;
import org.acme.schooltimetabling.persistence.LessonRepository;
import org.acme.schooltimetabling.persistence.TimeTableRepository;
import org.optaplanner.core.api.solver.ConsumptionPause;
import org.optaplanner.core.api.solver.SolverManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lessons")
public class LessonController {

    @Autowired
    LessonRepository lessonRepository;

    @Autowired
    TimeTableRepository timeTableRepository;

    @Autowired
    SolverManager<TimeTable, Long> solverManager;

    @GetMapping
    public List<Lesson> getAll() {
        return lessonRepository.findAll();
    }

    @PostMapping
    public Lesson add(@RequestBody Lesson lesson) {
        try (ConsumptionPause pause = solverManager.pauseBestSolutionConsumer(TimeTableRepository.SINGLETON_TIME_TABLE_ID)) {
            lessonRepository.save(lesson);
            solverManager.reloadProblem(TimeTableRepository.SINGLETON_TIME_TABLE_ID, timeTableRepository::findById);
        }
        return lesson;
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") Long id) {
        try (ConsumptionPause pause = solverManager.pauseBestSolutionConsumer(TimeTableRepository.SINGLETON_TIME_TABLE_ID)) {
            lessonRepository.deleteById(id);
            solverManager.reloadProblem(TimeTableRepository.SINGLETON_TIME_TABLE_ID, timeTableRepository::findById);
        }
    }
}
