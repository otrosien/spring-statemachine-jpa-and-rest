package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.OrderStateMachineConfiguration.OrderEvent;
import com.example.OrderStateMachineConfiguration.OrderState;

import lombok.SneakyThrows;

@SpringBootTest
@RunWith(SpringRunner.class)
public class OrderStateMachinePersistenceTest {

    @Autowired
    StateMachinePersister<OrderState, OrderEvent, Long> persister;

    @Autowired
    OrderRepository repo;

    @Autowired
    EntityManager em;

    @Autowired
    StateMachineFactory<OrderState, OrderEvent> orderStateFactory;

    @Test
    @SneakyThrows
    @Transactional
    public void should_persist() {
        // given
        Order o = new Order();
        o = repo.saveAndFlush(o);
        StateMachine<OrderState, OrderEvent> orderStateMachine = orderStateFactory.getStateMachine();
        orderStateMachine.start();
        orderStateMachine.sendEvent(OrderEvent.UnlockDelivery);

        // when persisting and making sure we flushed and cleared all caches...
        persister.persist(orderStateMachine, o.getId());
        em.flush();
        em.clear();

        // then the state is set on the order.
        o = repo.getOne(o.getId());
        assertThat(o.getStateMachineContext()).isNotNull();

        // and the statemachinecontext can be used to restore a new state machine.
        StateMachine<OrderState, OrderEvent> orderStateMachineNew = orderStateFactory.getStateMachine();
        persister.restore(orderStateMachineNew, o.getId());
        assertThat(orderStateMachineNew.getState().getId()).isEqualTo(OrderState.ReadyForDelivery);
    }

}
