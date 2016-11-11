package com.example;

import org.springframework.data.rest.core.event.AbstractRepositoryEventListener;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Component;

import com.example.OrderStateMachineConfiguration.OrderEvent;
import com.example.OrderStateMachineConfiguration.OrderState;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Component
@RequiredArgsConstructor
public class OrderCreatedEventListener extends AbstractRepositoryEventListener<Order> {

    final StateMachineFactory<OrderState, OrderEvent> orderStateFactory;

    final StateMachinePersister<OrderState, OrderEvent, Order> persister;

    @Override
    @SneakyThrows
    protected void onBeforeCreate(Order entity) {
        StateMachine<OrderState, OrderEvent> stateMachine = orderStateFactory.getStateMachine();
        stateMachine.start();
        persister.persist(stateMachine, entity);
    }

}
