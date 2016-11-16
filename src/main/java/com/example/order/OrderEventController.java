package com.example.order;

import javax.transaction.Transactional;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.example.DefaultStateMachineAdapter;
import com.example.order.OrderStateMachineConfiguration.OrderEvent;
import com.example.order.OrderStateMachineConfiguration.OrderState;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RepositoryRestController
@RequiredArgsConstructor
public class OrderEventController {

    final DefaultStateMachineAdapter<OrderState, OrderEvent, Order> orderStateMachineAdapter;

    @RequestMapping(path = "/orders/{id}/receive/{event}", method = RequestMethod.POST)
    @SneakyThrows
    @Transactional
    public HttpEntity<Void> receiveEvent(@PathVariable("id") Order order, @PathVariable("event") OrderEvent event) {
        StateMachine<OrderState, OrderEvent> stateMachine = orderStateMachineAdapter.restore(order);
        if (stateMachine.sendEvent(event)) {
            orderStateMachineAdapter.persist(stateMachine, order);
            return ResponseEntity.accepted().build();
        } else {
            return ResponseEntity.unprocessableEntity().build();
        }
    }
}
