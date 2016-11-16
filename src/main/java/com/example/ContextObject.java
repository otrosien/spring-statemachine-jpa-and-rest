package com.example;

import org.springframework.statemachine.StateMachineContext;

public interface ContextObject<S, E> {

    StateMachineContext<S, E> getStateMachineContext();

    void setStateMachineContext(StateMachineContext<S, E> context);

}
