package org.acme.schooltimetabling.rest;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.persistence.LessonRepository;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheRepositoryResource;
import io.quarkus.rest.data.panache.ResourceProperties;

@ResourceProperties(path = "lessons")
public interface LessonResource extends PanacheRepositoryResource<LessonRepository, Lesson, Long> {

}
