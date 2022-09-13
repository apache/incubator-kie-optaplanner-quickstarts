package org.acme.vehiclerouting.solver;

import org.acme.vehiclerouting.domain.Vehicle;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

public class VehicleRoutingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                vehicleCapacity(factory),
                totalDistance(factory),
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    protected Constraint vehicleCapacity(ConstraintFactory factory) {
        return factory.forEach(Vehicle.class)
                .filter(vehicle -> vehicle.getTotalDemand() > vehicle.getCapacity())
                .penalizeLong(HardSoftLongScore.ONE_HARD,
                        vehicle -> vehicle.getTotalDemand() - vehicle.getCapacity())
                .asConstraint("vehicleCapacity");
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    protected Constraint totalDistance(ConstraintFactory factory) {
        return factory.forEach(Vehicle.class)
                .penalizeLong(HardSoftLongScore.ONE_SOFT,
                        Vehicle::getTotalDistanceMeters)
                .asConstraint("distanceFromPreviousStandstill");
    }
}
