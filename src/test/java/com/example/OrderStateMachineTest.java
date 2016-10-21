package com.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.OrderStateMachineConfiguration.OrderEvent;
import com.example.OrderStateMachineConfiguration.OrderState;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=OrderApplication.class)
public class OrderStateMachineTest {

    @Autowired
    StateMachineFactory<OrderState, OrderEvent> orderStateFactory;

    @Test
    public void doPrepaymentFlow() {
        System.out.println("pre-payment flow");
        StateMachine<OrderState, OrderEvent> orderStateMachine = orderStateFactory.getStateMachine();

        orderStateMachine.start();
        assertThat(orderStateMachine.getExtendedState().get("paid", Boolean.class)).isFalse();
        assertTrue(orderStateMachine.sendEvent(OrderEvent.ReceivePayment));
        assertThat(orderStateMachine.getExtendedState().get("paid", Boolean.class)).isTrue();
        assertTrue(orderStateMachine.sendEvent(OrderEvent.Deliver));
        assertTrue(orderStateMachine.sendEvent(OrderEvent.Refund));
        assertThat(orderStateMachine.getExtendedState().get("paid", Boolean.class)).isFalse();
        assertTrue(orderStateMachine.sendEvent(OrderEvent.Reopen));
        assertTrue(orderStateMachine.sendEvent(OrderEvent.ReceivePayment));
        assertTrue(orderStateMachine.sendEvent(OrderEvent.Deliver));

        assertThat(orderStateMachine.getState().getIds()).containsOnly(OrderState.Completed);
        assertThat(orderStateMachine.getExtendedState().get("paid", Boolean.class)).isTrue();
    }

    @Test
    public void doPostpaymentFlow() {
        System.out.println("post-payment flow");
        StateMachine<OrderState, OrderEvent> orderStateMachine = orderStateFactory.getStateMachine();

        orderStateMachine.start();
        assertThat(orderStateMachine.getExtendedState().get("paid", Boolean.class)).isFalse();
        assertTrue(orderStateMachine.sendEvent(OrderEvent.UnlockDelivery));
        assertTrue(orderStateMachine.sendEvent(OrderEvent.Deliver));
        assertTrue(orderStateMachine.sendEvent(OrderEvent.ReceivePayment));
        assertThat(orderStateMachine.getExtendedState().get("paid", Boolean.class)).isTrue();
        assertTrue(orderStateMachine.sendEvent(OrderEvent.Refund));
        assertThat(orderStateMachine.getExtendedState().get("paid", Boolean.class)).isFalse();
        assertTrue(orderStateMachine.sendEvent(OrderEvent.Reopen));
        assertTrue(orderStateMachine.sendEvent(OrderEvent.UnlockDelivery));
        assertTrue(orderStateMachine.sendEvent(OrderEvent.ReceivePayment));
        assertTrue(orderStateMachine.sendEvent(OrderEvent.Deliver));

        assertThat(orderStateMachine.getState().getIds()).containsOnly(OrderState.Completed);
    }
}
