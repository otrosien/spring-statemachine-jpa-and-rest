package com.example;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.BasicLinkBuilder;
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
public class OrderEventResourceProcessor implements ResourceProcessor<Resource<Order>> {

    final StateMachineFactory<OrderState, OrderEvent> orderStateFactory;

    final StateMachinePersister<OrderState, OrderEvent, Order> persister;

    @Override
    @SneakyThrows
    public Resource<Order> process(Resource<Order> resource) {
        StateMachine<OrderState, OrderEvent> stateMachine = orderStateFactory.getStateMachine();
        Order order = resource.getContent();
        persister.restore(stateMachine, order);

        switch(stateMachine.getState().getId()) {
        case Open:
            resource.add(eventLink(OrderEvent.ReceivePayment, "receive-payment")); // (1)
            resource.add(eventLink(OrderEvent.Cancel, "cancel"));                  // (6)
            resource.add(eventLink(OrderEvent.UnlockDelivery, "unlock-delivery")); // (7)
            break;
        case ReadyForDelivery:
            resource.add(eventLink(OrderEvent.ReceivePayment, "receive-payment")); // (11)
            resource.add(eventLink(OrderEvent.Deliver, "deliver"));                // (2/9)
            resource.add(eventLink(OrderEvent.Refund, "refund"));                  // (4)
            resource.add(eventLink(OrderEvent.Cancel, "cancel"));                  // (8)
            break;
        case AwaitingPayment:
            resource.add(eventLink(OrderEvent.ReceivePayment, "receive-payment")); // (10)
            break;
        case Completed:
                resource.add(eventLink(OrderEvent.Refund, "refund"));              // (3)
            break;
        case Canceled:
            resource.add(eventLink(OrderEvent.Reopen, "reopen"));                  // (5)
            resource.add(eventLink(OrderEvent.ReceivePayment, "receive-payment")); // (12)
            break;
        }

        return resource;
    }

    private Link eventLink(OrderEvent event, String rel) {
        return BasicLinkBuilder.linkToCurrentMapping().slash("receive").slash(event).withRel(rel);
    }
}
