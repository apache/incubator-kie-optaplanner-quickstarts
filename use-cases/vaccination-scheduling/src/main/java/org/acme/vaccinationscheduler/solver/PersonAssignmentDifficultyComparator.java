package org.acme.vaccinationscheduler.solver;

import java.util.Comparator;

import org.acme.vaccinationscheduler.domain.solver.PersonAssignment;

public class PersonAssignmentDifficultyComparator implements Comparator<PersonAssignment> {

    private static final Comparator<PersonAssignment> COMPARATOR = Comparator.comparing(PersonAssignment::getDoseNumber)
            .thenComparing(PersonAssignment::getPriorityRating);

    @Override
    public int compare(PersonAssignment a, PersonAssignment b) {
        return COMPARATOR.compare(a, b);
    }

}
