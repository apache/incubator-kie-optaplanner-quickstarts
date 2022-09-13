package org.acme.kotlin.schooltimetabling.persistence

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import org.acme.kotlin.schooltimetabling.domain.Timeslot
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class TimeslotRepository : PanacheRepository<Timeslot>
