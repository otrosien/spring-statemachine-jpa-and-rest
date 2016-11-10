package com.example;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;

import com.example.OrderStateMachineConfiguration.OrderEvent;
import com.example.OrderStateMachineConfiguration.OrderState;

@SpringBootApplication
@EntityScan
class OrderApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(OrderApplication.class).build().run(args);
    }

    @Bean
    public StateMachinePersister<OrderState, OrderEvent, Long> persister(StateMachinePersist<OrderState, OrderEvent, Long> persist) {
        return new DefaultStateMachinePersister<>(persist);
    }

    @Bean
    public StateMachinePersist<OrderState, OrderEvent, Long> persist(OrderRepository repo) {
        return new StateMachinePersist<OrderState, OrderEvent, Long>() {

            @Override
            public StateMachineContext<OrderState, OrderEvent> read(Long contextObj) throws Exception {
                return repo.getOne(contextObj).getStateMachineContext();
            }

            @Override
            public void write(StateMachineContext<OrderState, OrderEvent> context, Long contextObj) throws Exception {
                Order o = repo.getOne(contextObj);
                o.setStateMachineContext(context);
                repo.save(o);
            }
        };
    }
}
