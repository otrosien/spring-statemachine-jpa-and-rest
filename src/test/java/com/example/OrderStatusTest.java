package com.example;

import static org.assertj.core.api.Assertions.assertThat;

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
public class OrderStatusTest {

    @Autowired
    StateMachineFactory<OrderState, OrderEvent> orderStateFactory;

    @Test
    public void doPrepaymentFlow() {
        System.out.println("pre-payment flow");
        StateMachine<OrderState, OrderEvent> orderStateMachine = orderStateFactory.getStateMachine();
        orderStateMachine.start();
        orderStateMachine.sendEvent(OrderEvent.ReceivePayment);
        orderStateMachine.sendEvent(OrderEvent.Deliver);
        orderStateMachine.sendEvent(OrderEvent.Refund);
        orderStateMachine.sendEvent(OrderEvent.Reopen);
        orderStateMachine.sendEvent(OrderEvent.ReceivePayment);
        orderStateMachine.sendEvent(OrderEvent.Deliver);

        assertThat(orderStateMachine.getState().getIds()).containsOnly(OrderState.Completed);
    }

    @Test
    public void doPostpaymentFlow() {
        System.out.println("post-payment flow");
        StateMachine<OrderState, OrderEvent> orderStateMachine = orderStateFactory.getStateMachine();
        orderStateMachine.start();
        orderStateMachine.sendEvent(OrderEvent.UnlockDelivery);
        orderStateMachine.sendEvent(OrderEvent.Deliver);
        orderStateMachine.sendEvent(OrderEvent.ReceivePayment);
        orderStateMachine.sendEvent(OrderEvent.Refund);
        orderStateMachine.sendEvent(OrderEvent.Reopen);
        orderStateMachine.sendEvent(OrderEvent.UnlockDelivery);
        orderStateMachine.sendEvent(OrderEvent.ReceivePayment);
        orderStateMachine.sendEvent(OrderEvent.Deliver);
        assertThat(orderStateMachine.getState().getIds()).containsOnly(OrderState.Completed);
    }
}
