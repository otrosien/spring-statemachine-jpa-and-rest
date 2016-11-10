package com.example;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.statemachine.StateMachineContext;

import com.example.OrderStateMachineConfiguration.OrderEvent;
import com.example.OrderStateMachineConfiguration.OrderState;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@SuppressWarnings("serial")
@Entity
@NoArgsConstructor
@Getter
@FieldDefaults(level=AccessLevel.PRIVATE)
@Table(name="orders")
public class Order extends AbstractPersistable<Long> {

    @Id
    @GeneratedValue(strategy=IDENTITY)
    Long id;

    @Setter
    StateMachineContext<OrderState, OrderEvent> stateMachineContext;

}
