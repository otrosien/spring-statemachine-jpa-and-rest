package org.springframework.statemachine.support;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;

public abstract class MyStateMachineUtils extends StateMachineUtils {

    public static <S, E> void setCurrentState(StateMachine<S, E> stateMachine, S state) {
        if (stateMachine instanceof AbstractStateMachine) {
            setCurrentState((AbstractStateMachine<S, E>)stateMachine, state);
        } else {
            throw new IllegalArgumentException("Provided StateMachine is not a valid type");
        }
    }

    public static <S, E> void setCurrentState(AbstractStateMachine<S, E> stateMachine, S state) {
        stateMachine.setCurrentState(findState(stateMachine, state), null, null, false, stateMachine);
    }

    private static <S, E> State<S, E> findState(AbstractStateMachine<S, E> stateMachine, S stateId) {
        for (State<S, E> state : stateMachine.getStates()) {
            if (state.getId() == stateId) {
                return state;
            }
        }

        throw new IllegalArgumentException("Specified State ID is not valid");
    }
}