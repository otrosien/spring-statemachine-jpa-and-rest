package com.example;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Component;

import com.example.OrderStateMachineConfiguration.OrderEvent;
import com.example.OrderStateMachineConfiguration.OrderState;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
@Component
public class OrderStateMachineAdapter {

    final StateMachineFactory<OrderState, OrderEvent> stateMachineFactory;

    final StateMachinePersister<OrderState, OrderEvent, Order> persister;

    @SneakyThrows
    public StateMachine<OrderState, OrderEvent> restore(Order order) {
        StateMachine<OrderState, OrderEvent> stateMachine = stateMachineFactory.getStateMachine();
        return persister.restore(stateMachine, order);
    }

    @SneakyThrows
    public void persist(StateMachine<OrderState, OrderEvent> stateMachine, Order order) {
        persister.persist(stateMachine, order);
    }

    public StateMachine<OrderState, OrderEvent> create() {
        StateMachine<OrderState, OrderEvent> stateMachine = stateMachineFactory.getStateMachine();
        stateMachine.start();
        return stateMachine;
    }

}
