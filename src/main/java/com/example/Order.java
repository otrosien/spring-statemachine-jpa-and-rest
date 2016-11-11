package com.example;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.hateoas.Identifiable;
import org.springframework.statemachine.StateMachineContext;

import com.example.OrderStateMachineConfiguration.OrderEvent;
import com.example.OrderStateMachineConfiguration.OrderState;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@NoArgsConstructor
@FieldDefaults(level=AccessLevel.PRIVATE)
@Access(AccessType.FIELD)
@Table(name="orders")
public class Order extends AbstractPersistable<Long> implements Identifiable<Long> {

    private static final long serialVersionUID = 8848887579564649636L;

    @Getter
    @Setter
    @JsonIgnore
    StateMachineContext<OrderState, OrderEvent> stateMachineContext;

}
