package com.example;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.test.AbstractStateMachineTests;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;

import com.example.order.OrderEvent;
import com.example.order.OrderState;
import com.example.order.OrderStateMachineConfiguration;

import lombok.SneakyThrows;

public class OrderStateMachineTest extends AbstractStateMachineTests {

    StateMachineFactory<OrderState, OrderEvent> orderStateMachineFactory;

    @SuppressWarnings("unchecked")
    @Override
    protected AnnotationConfigApplicationContext buildContext() {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(OrderStateMachineConfiguration.class);
        orderStateMachineFactory = ctx.getBean(StateMachineFactory.class);
        return ctx;
    }

    @Test
    @SneakyThrows
    public void testPrepaymentFlow() {
        StateMachine<OrderState, OrderEvent> orderStateMachine = orderStateMachineFactory.getStateMachine();
        StateMachineTestPlan<OrderState, OrderEvent> plan =
                StateMachineTestPlanBuilder.<OrderState, OrderEvent>builder()
                    .stateMachine(orderStateMachine)
                    .step()
                        .expectState(OrderState.Open)
                        .expectVariable("paid", Boolean.FALSE)
                        .and()
                    .step()
                        .sendEvent(OrderEvent.ReceivePayment)
                        .expectState(OrderState.ReadyForDelivery)
                        .expectExtendedStateChanged(1)
                        .expectVariable("paid", Boolean.TRUE)
                        .and()
                    .step()
                        .sendEvent(OrderEvent.Deliver)
                        .expectState(OrderState.Completed)
                        .and()
                    .step()
                        .sendEvent(OrderEvent.ReceivePayment)
                        .expectEventNotAccepted(1)
                        .and()
                    .step()
                        .sendEvent(OrderEvent.Refund)
                        .expectState(OrderState.Canceled)
                        .expectExtendedStateChanged(1)
                        .expectVariable("paid", Boolean.FALSE)
                        .and()
                    .step()
                        .sendEvent(OrderEvent.Reopen)
                        .expectState(OrderState.Open)
                        .and()
                    .step()
                        .sendEvent(OrderEvent.ReceivePayment)
                        .expectState(OrderState.ReadyForDelivery)
                        .expectExtendedStateChanged(1)
                        .expectVariable("paid", Boolean.TRUE)
                        .and()
                    .step()
                        .sendEvent(OrderEvent.Deliver)
                        .expectState(OrderState.Completed)
                        .and()
                    .build();

        plan.test();

    }

    @Test
    @SneakyThrows
    public void testPostpaymentFlow() {
        StateMachine<OrderState, OrderEvent> orderStateMachine = orderStateMachineFactory.getStateMachine();
        StateMachineTestPlan<OrderState, OrderEvent> plan =
                StateMachineTestPlanBuilder.<OrderState, OrderEvent>builder()
                    .stateMachine(orderStateMachine)
                    .step()
                        .expectState(OrderState.Open)
                        .expectVariable("paid", Boolean.FALSE)
                        .and()
                    .step()
                        .sendEvent(OrderEvent.UnlockDelivery)
                        .expectState(OrderState.ReadyForDelivery)
                        .and()
                    .step()
                        .sendEvent(OrderEvent.Deliver)
                        .expectState(OrderState.AwaitingPayment)
                        .and()
                    .step()
                        .sendEvent(OrderEvent.ReceivePayment)
                        .expectState(OrderState.Completed)
                        .expectExtendedStateChanged(1)
                        .expectVariable("paid", Boolean.TRUE)
                        .and()
                    .step()
                        .sendEvent(OrderEvent.Refund)
                        .expectState(OrderState.Canceled)
                        .expectExtendedStateChanged(1)
                        .expectVariable("paid", Boolean.FALSE)
                        .and()
                    .step()
                        .sendEvent(OrderEvent.Reopen)
                        .expectState(OrderState.Open)
                        .and()
                    .step()
                        .sendEvent(OrderEvent.UnlockDelivery)
                        .expectState(OrderState.ReadyForDelivery)
                        .and()
                    .step()
                        .sendEvent(OrderEvent.ReceivePayment)
                        .expectStateChanged(0)
                        .expectExtendedStateChanged(1)
                        .expectVariable("paid", Boolean.TRUE)
                        .and()
                    .step()
                        .sendEvent(OrderEvent.Deliver)
                        .expectState(OrderState.Completed)
                        .and()
                    .build();
        plan.test();
    }
}
