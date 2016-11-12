package com.example;

import org.springframework.data.rest.core.event.AbstractRepositoryEventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Component
@RequiredArgsConstructor
public class OrderCreatedEventListener extends AbstractRepositoryEventListener<Order> {

    final OrderStateMachineAdapter orderStateMachineAdapter;

    @Override
    @SneakyThrows
    protected void onBeforeCreate(Order order) {
        orderStateMachineAdapter.persist(orderStateMachineAdapter.create(), order);
    }

}
