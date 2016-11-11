package com.example;

import javax.transaction.Transactional;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.example.OrderStateMachineConfiguration.OrderEvent;
import com.example.OrderStateMachineConfiguration.OrderState;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RepositoryRestController
@RequiredArgsConstructor
public class OrderEventController {

    final StateMachineFactory<OrderState, OrderEvent> stateMachineFactory;

    final StateMachinePersister<OrderState, OrderEvent, Order> persister;

    @RequestMapping(path = "/orders/{id}/receive/{event}", method = RequestMethod.POST)
    @SneakyThrows
    @Transactional
    public HttpEntity<Void> receiveEvent(@PathVariable("id") Order order, @PathVariable("event") OrderEvent event) {
        StateMachine<OrderState, OrderEvent> stateMachine = stateMachineFactory.getStateMachine();
        persister.restore(stateMachine, order);
        if (stateMachine.sendEvent(event)) {
            persister.persist(stateMachine, order);
            return ResponseEntity.accepted().build();
        } else {
            return ResponseEntity.unprocessableEntity().build();
        }
    }
}
