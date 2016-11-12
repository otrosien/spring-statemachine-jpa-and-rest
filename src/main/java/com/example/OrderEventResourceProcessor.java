package com.example;

import java.util.function.Predicate;

import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Component;

import com.example.OrderStateMachineConfiguration.OrderEvent;
import com.example.OrderStateMachineConfiguration.OrderState;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderEventResourceProcessor implements ResourceProcessor<Resource<Order>> {

    final EntityLinks entityLinks;

    final OrderStateMachineAdapter orderStateMachineAdapter;

    @Override
    public Resource<Order> process(Resource<Order> resource) {
        Order order = resource.getContent();
        StateMachine<OrderState, OrderEvent> stateMachine = orderStateMachineAdapter.restore(order);
        boolean paid = new PaidPredicate().test(stateMachine);

        switch(stateMachine.getState().getId()) {
        case Open:
            if(!paid) {
                resource.add(eventLink(order, OrderEvent.ReceivePayment, "receive-payment")); // (1)
            }
            resource.add(eventLink(order, OrderEvent.Cancel, "cancel"));                      // (6)
            resource.add(eventLink(order, OrderEvent.UnlockDelivery, "unlock-delivery"));     // (7)
            break;
        case ReadyForDelivery:
            resource.add(eventLink(order, OrderEvent.Deliver, "deliver"));                    // (2/9)
            if(paid) {
                resource.add(eventLink(order, OrderEvent.Refund, "refund"));                  // (4)
            } else {
                resource.add(eventLink(order, OrderEvent.Cancel, "cancel"));                  // (8)
                resource.add(eventLink(order, OrderEvent.ReceivePayment, "receive-payment")); // (11)
            }
            break;
        case AwaitingPayment:
            resource.add(eventLink(order, OrderEvent.ReceivePayment, "receive-payment"));     // (10)
            break;
        case Completed:
            if(paid) {
                resource.add(eventLink(order, OrderEvent.Refund, "refund"));                  // (3)
            }
            break;
        case Canceled:
            resource.add(eventLink(order, OrderEvent.Reopen, "reopen"));                      // (5)
            if(!paid) {
                resource.add(eventLink(order, OrderEvent.ReceivePayment, "receive-payment")); // (12)
            }
            break;
        default:
            // none.
            break;
        }

        return resource;
    }

    private Link eventLink(Order order, OrderEvent event, String rel) {
        return entityLinks.linkForSingleResource(order).slash("receive").slash(event).withRel(rel);
    }

    static class PaidPredicate implements Predicate<StateMachine<OrderState, OrderEvent>> {
        @Override
        public boolean test(StateMachine<OrderState, OrderEvent> stateMachine) {
            return stateMachine.getExtendedState().get("paid", Boolean.class);
        }
    }
}
