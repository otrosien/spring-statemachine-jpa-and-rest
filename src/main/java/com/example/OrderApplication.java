package com.example;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.EntityLinks;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;

import com.example.order.OrderStateMachineConfiguration.OrderEvent;
import com.example.order.OrderStateMachineConfiguration.OrderState;

@SpringBootApplication
@EntityScan
class OrderApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(OrderApplication.class).build().run(args);
    }

    @Bean
    public DefaultStateMachineAdapter<OrderState, OrderEvent, ContextObject<OrderState, OrderEvent>> orderStateMachineAdapter(
            StateMachineFactory<OrderState, OrderEvent> stateMachineFactory,
            StateMachinePersister<OrderState, OrderEvent, ContextObject<OrderState, OrderEvent>> persister) {
        return new DefaultStateMachineAdapter<>(stateMachineFactory, persister);
    }

    @Bean
    public ContextObjectResourceProcessor<OrderState, OrderEvent> orderResourceProcessor(EntityLinks entityLinks,
            DefaultStateMachineAdapter<OrderState, OrderEvent, ContextObject<OrderState, OrderEvent>> orderStateMachineAdapter) {
        return new ContextObjectResourceProcessor<>(entityLinks, orderStateMachineAdapter);
    }

    @Bean
    public StateMachinePersister<OrderState, OrderEvent, ContextObject<OrderState, OrderEvent>> persister(
            StateMachinePersist<OrderState, OrderEvent, ContextObject<OrderState, OrderEvent>> persist) {
        return new DefaultStateMachinePersister<>(persist);
    }

    @Bean
    public StateMachinePersist<OrderState, OrderEvent, ContextObject<OrderState, OrderEvent>> persist() {
        return new StateMachinePersist<OrderState, OrderEvent, ContextObject<OrderState, OrderEvent>>() {

            @Override
            public StateMachineContext<OrderState, OrderEvent> read(ContextObject<OrderState, OrderEvent> order) throws Exception {
                return order.getStateMachineContext();
            }

            @Override
            public void write(StateMachineContext<OrderState, OrderEvent> context, ContextObject<OrderState, OrderEvent> order) throws Exception {
                order.setStateMachineContext(context);
            }
        };
    }
}
