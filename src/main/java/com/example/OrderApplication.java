package com.example;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;

import com.example.OrderStateMachineConfiguration.OrderEvent;
import com.example.OrderStateMachineConfiguration.OrderState;

import lombok.RequiredArgsConstructor;

@SpringBootApplication
class OrderApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(OrderApplication.class).build().run(args);
    }

    @Configuration
    static class StateMachinePersistenceConfig {

        @Bean
        public JpaStateMachineContextRepository<OrderState, OrderEvent> stateMachineRepo(JdbcTemplate jdbcTemplate) {
            return new JpaStateMachineContextRepository<>(jdbcTemplate);
        }

        @Bean
        public StateMachinePersist<OrderState, OrderEvent, String> stateMachinePersist(JpaStateMachineContextRepository<OrderState, OrderEvent> stateMachineRepo) {
            return new JdbcStateMachinePersist(stateMachineRepo);
        }
    }

    @RequiredArgsConstructor
    static class JdbcStateMachinePersist implements StateMachinePersist<OrderState, OrderEvent, String> {
        private final JpaStateMachineContextRepository<OrderState, OrderEvent> repo;

        @Override
        public void write(StateMachineContext<OrderState, OrderEvent> context, String contextObj) throws Exception {
            repo.save(context, context.getId());
        }

        @Override
        public StateMachineContext<OrderState, OrderEvent> read(String contextObj) throws Exception {
            return repo.getContext(contextObj);
        }
    }

}
