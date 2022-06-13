package org.acme.schooltimetabling.persistence;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import org.acme.schooltimetabling.domain.Lesson;

public interface LessonRepository extends PagingAndSortingRepository<Lesson, Long> {

    @Override
    List<Lesson> findAll();

}
