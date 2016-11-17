package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.order.Order;
import com.example.order.OrderEvent;
import com.example.order.OrderRepository;
import com.example.order.OrderState;

import lombok.SneakyThrows;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class OrderStateMachineIntegrationTest {

    @Autowired
    StateMachinePersister<OrderState, OrderEvent, ContextEntity<OrderState, OrderEvent, ? extends Serializable>> persister;

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
        persister.persist(orderStateMachine, o);
        em.flush();
        em.clear();

        // then the state is set on the order.
        o = repo.getOne(o.getId());
        assertThat(o.getStateMachineContext()).isNotNull();
        assertThat(o.getCurrentState()).isEqualTo(OrderState.ReadyForDelivery);

        // and the statemachinecontext can be used to restore a new state
        // machine.
        StateMachine<OrderState, OrderEvent> orderStateMachineNew = orderStateFactory.getStateMachine();
        persister.restore(orderStateMachineNew, o);
        assertThat(orderStateMachineNew.getState().getId()).isEqualTo(OrderState.ReadyForDelivery);

        // and the repository should find one order by its current state.
        assertThat(repo.findByCurrentState(OrderState.ReadyForDelivery, new PageRequest(0, 10)).getNumberOfElements())
                .isEqualTo(1);
    }

}
