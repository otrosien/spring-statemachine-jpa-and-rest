package com.example.order;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.hateoas.Identifiable;
import org.springframework.statemachine.StateMachineContext;

import com.example.ContextObject;
import com.example.order.OrderStateMachineConfiguration.OrderEvent;
import com.example.order.OrderStateMachineConfiguration.OrderState;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@Entity
@NoArgsConstructor
@FieldDefaults(level=AccessLevel.PRIVATE)
@Access(AccessType.FIELD)
@Table(name="orders", indexes=@Index(columnList="currentState"))
public class Order extends AbstractPersistable<Long> implements Identifiable<Long>, ContextObject<OrderState, OrderEvent> { // NOSONAR

    private static final long serialVersionUID = 8848887579564649636L;

    @Getter
    @JsonIgnore
    StateMachineContext<OrderState, OrderEvent> stateMachineContext; // NOSONAR

    @Getter
    @JsonIgnore
    @Enumerated(EnumType.STRING)
    OrderState currentState;

    public void setStateMachineContext(@NonNull StateMachineContext<OrderState, OrderEvent> stateMachineContext) {
        this.currentState = stateMachineContext.getState();
        this.stateMachineContext = stateMachineContext;
    }

    @JsonIgnore
    @Override
    public boolean isNew() {
        return super.isNew();
    }
}
