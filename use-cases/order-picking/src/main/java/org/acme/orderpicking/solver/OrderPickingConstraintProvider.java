/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acme.orderpicking.solver;

import org.acme.orderpicking.domain.TrolleyStep;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

import static org.acme.orderpicking.solver.DistanceCalculator.calculateDistance;
import static org.optaplanner.core.api.score.stream.ConstraintCollectors.countDistinctLong;
import static org.optaplanner.core.api.score.stream.ConstraintCollectors.sum;

public class OrderPickingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                requiredNumberOfBuckets(constraintFactory),
                minimumBucketCapacity(constraintFactory),
                orderDueTime(constraintFactory),
                minimizeDistanceFromPreviousTrolleyStep(constraintFactory),
                minimizeDistanceFromLastTrolleyStepToPathOrigin(constraintFactory),
                minimizeOrderSplitByTrolley(constraintFactory)
        };
    }

    /**
     * Ensure that a Trolley has a sufficient number of buckets for holding all elements picked along the path and
     * consider that buckets are not shared between orders.
     */
    Constraint requiredNumberOfBuckets(ConstraintFactory constraintFactory) {
        return constraintFactory
                .from(TrolleyStep.class)
                //raw total volume per order
                .groupBy(trolleyStep -> trolleyStep.getTrolley(),
                        trolleyStep -> trolleyStep.getOrderItem().getOrder(),
                        sum(trolleyStep -> trolleyStep.getOrderItem().getVolume()))
                //required buckets per order
                .groupBy((trolley, order, orderTotalVolume) -> trolley,
                        (trolley, order, orderTotalVolume) -> order,
                        sum((trolley, order, orderTotalVolume) -> calculateOrderRequiredBuckets(orderTotalVolume, trolley.getBucketCapacity())))
                //required buckets per trolley
                .groupBy((trolley, order, orderTotalBuckets) -> trolley,
                        sum((trolley, order, orderTotalBuckets) -> orderTotalBuckets))
                //penalization if the trolley don't have enough buckets to hold the orders
                .filter((trolley, orderTotalBuckets) -> trolley.getBucketCount() < orderTotalBuckets)
                .penalize("Required number of buckets", HardMediumSoftLongScore.ONE_HARD);
    }

    /**
     * An Order item represent an indivisible volume of product, "a pack of 6 milk bricks", "a bottle of wine", etc.,
     * that can not be split into buckets.
     */
    Constraint minimumBucketCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory
                .from(TrolleyStep.class)
                .filter(trolleyStep -> trolleyStep.getTrolley().getBucketCapacity() < trolleyStep.getOrderItem().getVolume())
                .penalize("Minimum bucket capacity", HardMediumSoftLongScore.ONE_HARD);
    }

    /**
     * Prioritize items picking according with the order dueTime.
     */
    Constraint orderDueTime(ConstraintFactory constraintFactory) {
        return constraintFactory.from(TrolleyStep.class)
                .penalize("Order due time",
                        HardMediumSoftLongScore.ONE_MEDIUM,
                        trolleyStep -> trolleyStep.getOrderItem().getOrder().getDueTime().toSecondOfDay());
    }

    /**
     * An Order should ideally be prepared on the same trolley, penalize the order splitting into different trolleys.
     */
    Constraint minimizeOrderSplitByTrolley(ConstraintFactory constraintFactory) {
        return constraintFactory.from(TrolleyStep.class)
                .groupBy(trolleyStep -> trolleyStep.getOrderItem().getOrder(),
                        countDistinctLong(TrolleyStep::getTrolley))
                .penalizeLong("Minimize order split by trolley",
                        HardMediumSoftLongScore.ONE_SOFT, (order, trolleySpreadCount) -> trolleySpreadCount);
    }

    /**
     * Minimize the distance travelled by the trolley.
     */
    Constraint minimizeDistanceFromPreviousTrolleyStep(ConstraintFactory constraintFactory) {
        return constraintFactory.from(TrolleyStep.class)
                .penalizeLong(
                        "Minimize the distance from the previous trolley step",
                        HardMediumSoftLongScore.ONE_MEDIUM,
                        trolleyStep -> calculateDistance(trolleyStep.getPreviousElement().getLocation(), trolleyStep.getLocation()));
    }

    /**
     * Minimize the distance travelled by the trolley.
     */
    Constraint minimizeDistanceFromLastTrolleyStepToPathOrigin(ConstraintFactory constraintFactory) {
        return constraintFactory.from(TrolleyStep.class)
                .filter(TrolleyStep::isLast)
                .penalizeLong(
                        "Minimize the distance form last trolley step to the path origin",
                        HardMediumSoftLongScore.ONE_MEDIUM,
                        trolleyStep -> calculateDistance(trolleyStep.getLocation(), trolleyStep.getTrolley().getLocation()));
    }

    private int calculateOrderRequiredBuckets(int orderVolume, int bucketVolume) {
        return (orderVolume + (bucketVolume - 1)) / bucketVolume;
    }
}
