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

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;

import io.quarkus.test.junit.QuarkusTest;
import org.acme.orderpicking.domain.Location;
import org.acme.orderpicking.domain.Order;
import org.acme.orderpicking.domain.OrderItem;
import org.acme.orderpicking.domain.OrderPickingSolution;
import org.acme.orderpicking.domain.PathElement;
import org.acme.orderpicking.domain.Trolley;
import org.acme.orderpicking.domain.TrolleyStep;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

@QuarkusTest
class OrderPickingConstraintProviderTest {

    @Inject
    ConstraintVerifier<OrderPickingConstraintProvider, OrderPickingSolution> constraintVerifier;

    private static Stream<Arguments> requiredNumberOfBucketsParams() {
        return Stream.of(
                Arguments.of(2, 5, 2, 5, 0),
                Arguments.of(2, 4, 2, 5, 1),
                Arguments.of(2, 5, 2, 4, 1),
                Arguments.of(2, 4, 2, 4, 2));
    }

    @ParameterizedTest
    @MethodSource("requiredNumberOfBucketsParams")
    void requiredNumberOfBuckets(int trolley1BucketCount, int trolley1BucketCapacity,
            int trolley2BucketCount, int trolley2BucketCapacity,
            long expectedPenalty) {
        Order order1 = mockOrder("order1", mockOrderItem(1), mockOrderItem(2), mockOrderItem(4), mockOrderItem(1));
        Order order2 = mockOrder("order2", mockOrderItem(4), mockOrderItem(1), mockOrderItem(2), mockOrderItem(3));

        TrolleyStep trolley1Step1 = mockTrolleyStep(order1.getItems().get(0));
        TrolleyStep trolley1Step2 = mockTrolleyStep(order1.getItems().get(1));
        TrolleyStep trolley1Step3 = mockTrolleyStep(order2.getItems().get(0));
        TrolleyStep trolley1Step4 = mockTrolleyStep(order2.getItems().get(1));

        Trolley trolley1 = mockTrolley(trolley1BucketCount, trolley1BucketCapacity,
                trolley1Step1,
                trolley1Step2,
                trolley1Step3,
                trolley1Step4);

        TrolleyStep trolley2Step1 = mockTrolleyStep(order1.getItems().get(2));
        TrolleyStep trolley2Step2 = mockTrolleyStep(order1.getItems().get(3));
        TrolleyStep trolley2Step3 = mockTrolleyStep(order2.getItems().get(2));
        TrolleyStep trolley2Step4 = mockTrolleyStep(order2.getItems().get(3));

        Trolley trolley2 = mockTrolley(trolley2BucketCount, trolley2BucketCapacity,
                trolley2Step1,
                trolley2Step2,
                trolley2Step3,
                trolley2Step4);

        constraintVerifier.verifyThat(OrderPickingConstraintProvider::requiredNumberOfBuckets)
                .given(trolley1Step1,
                        trolley1Step2,
                        trolley1Step3,
                        trolley1Step4,
                        trolley2Step1,
                        trolley2Step2,
                        trolley2Step3,
                        trolley2Step4)
                .penalizesBy(expectedPenalty);
    }

    private static Stream<Arguments> minimumBucketCapacityParams() {
        return Stream.of(
                Arguments.of(7, 0),
                Arguments.of(6, 1),
                Arguments.of(5, 2),
                Arguments.of(4, 3),
                Arguments.of(3, 4),
                Arguments.of(2, 5),
                Arguments.of(1, 5));
    }

    @ParameterizedTest
    @MethodSource("minimumBucketCapacityParams")
    void minimumBucketCapacity(int trolleyBucketCapacity, long expectedPenalization) {
        Order order = mockOrder("order1", mockOrderItem(3), mockOrderItem(4), mockOrderItem(5),
                mockOrderItem(6), mockOrderItem(7));

        TrolleyStep[] trolleySteps = order.getItems().stream()
                .map(OrderPickingConstraintProviderTest::mockTrolleyStep)
                .toArray(TrolleyStep[]::new);

        Trolley trolley = mockTrolley(1, trolleyBucketCapacity, trolleySteps);
        constraintVerifier.verifyThat(OrderPickingConstraintProvider::minimumBucketCapacity)
                .given((Object[]) trolleySteps)
                .penalizesBy(expectedPenalization);
    }

    @Test
    void orderDueTime() {
        LocalTime dueTime = LocalTime.of(15, 20, 30);
        int expectedPenalization = dueTime.toSecondOfDay();
        Order order = mockOrder("order1", dueTime, mockOrderItem(5));
        TrolleyStep trolleyStep = mockTrolleyStep(order.getItems().get(0));
        Trolley trolley = mockTrolley(1, 50, trolleyStep);

        constraintVerifier.verifyThat(OrderPickingConstraintProvider::orderDueTime)
                .given(trolleyStep)
                .penalizesBy(expectedPenalization);
    }

    @Test
    void minimizeDistanceFromPreviousTrolleyStep() {
        TrolleyStep currentTrolleyStep = mockTrolleyStep(new Location(3, 4));
        TrolleyStep previousTrolleyStep = mockTrolleyStep(new Location(0, 0));
        currentTrolleyStep.setPreviousElement(previousTrolleyStep);

        constraintVerifier.verifyThat(OrderPickingConstraintProvider::minimizeDistanceFromPreviousTrolleyStep)
                .given(currentTrolleyStep)
                .penalizesBy(5);
    }

    @Test
    void minimizeDistanceFromLastTrolleyStepToPathOrigin() {
        TrolleyStep lastTrolleyStep = mockTrolleyStep(new Location(3, 4));

        TrolleyStep intermediateTrolleyStep1 = new TrolleyStep();
        TrolleyStep intermediateTrolleyStep2 = new TrolleyStep();

        Trolley trolley = mockTrolley(1, 1,
                intermediateTrolleyStep1,
                intermediateTrolleyStep2,
                lastTrolleyStep);

        Location pathOriginLocation = new Location(0, 0);
        trolley.setLocation(pathOriginLocation);
        constraintVerifier.verifyThat(OrderPickingConstraintProvider::minimizeDistanceFromLastTrolleyStepToPathOrigin)
                .given(intermediateTrolleyStep1,
                        intermediateTrolleyStep2,
                        lastTrolleyStep)
                .penalizesBy(5);
    }

    @Test
    void minimizeOrderSplitByTrolley() {
        Order order1 = mockOrder("order1", mockOrderItem(1), mockOrderItem(1), mockOrderItem(1), mockOrderItem(1));
        Order order2 = mockOrder("order2", mockOrderItem(1), mockOrderItem(1), mockOrderItem(1), mockOrderItem(1));

        Trolley order1Trolley1 = mockTrolley(2, 1,
                mockTrolleyStep(order1.getItems().get(0)),
                mockTrolleyStep(order1.getItems().get(1)));
        Trolley order1Trolley2 = mockTrolley(1, 1,
                mockTrolleyStep(order1.getItems().get(2)));
        Trolley order1Trolley3 = mockTrolley(1, 1,
                mockTrolleyStep(order1.getItems().get(3)));

        Trolley order2Trolley1 = mockTrolley(4, 1,
                mockTrolleyStep(order2.getItems().get(0)),
                mockTrolleyStep(order2.getItems().get(1)),
                mockTrolleyStep(order2.getItems().get(2)),
                mockTrolleyStep(order2.getItems().get(3)));

        Object[] allSteps = Stream.of(trolleySteps(order1Trolley1),
                trolleySteps(order1Trolley2),
                trolleySteps(order1Trolley3),
                trolleySteps(order2Trolley1))
                .flatMap(Collection::stream).toArray();

        constraintVerifier.verifyThat(OrderPickingConstraintProvider::minimizeOrderSplitByTrolley)
                .given(allSteps)
                .penalizesBy(4);
    }

    private static Order mockOrder(String id, OrderItem... items) {
        return mockOrder(id, null, items);
    }

    private static Order mockOrder(String id, LocalTime dueTime, OrderItem... items) {
        Order order = new Order();
        order.setId(id);
        order.setDueTime(dueTime);
        for (int i = 0; i < items.length; i++) {
            OrderItem item = items[i];
            item.setOrder(order);
            item.setId(order.getId() + "_item_" + i);
            order.getItems().add(item);
        }
        return order;
    }

    private static OrderItem mockOrderItem(int volume) {
        OrderItem item = new OrderItem();
        item.setVolume(volume);
        return item;
    }

    private static TrolleyStep mockTrolleyStep(OrderItem item) {
        return new TrolleyStep(item, null);
    }

    private static TrolleyStep mockTrolleyStep(Location location) {
        return new TrolleyStep(null, location);
    }

    private static Trolley mockTrolley(int bucketCount, int bucketCapacity, TrolleyStep... steps) {
        Trolley trolley = new Trolley();
        trolley.setBucketCapacity(bucketCapacity);
        trolley.setBucketCount(bucketCount);
        linkPathElements(trolley, steps);
        return trolley;
    }

    private static List<TrolleyStep> trolleySteps(Trolley trolley) {
        ArrayList<TrolleyStep> result = new ArrayList<>();
        TrolleyStep nextElement = trolley.getNextElement();
        while (nextElement != null) {
            result.add(nextElement);
            nextElement = nextElement.getNextElement();
        }
        return result;
    }

    private static void linkPathElements(Trolley trolley, TrolleyStep... trolleySteps) {
        PathElement previousStep = trolley;
        for (TrolleyStep trolleyStep : trolleySteps) {
            trolleyStep.setTrolley(trolley);
            trolleyStep.setPreviousElement(previousStep);
            previousStep.setNextElement(trolleyStep);
            previousStep = trolleyStep;
        }
    }
}
