package com.example;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@SuppressWarnings("serial")
@Entity
@NoArgsConstructor(access=AccessLevel.PRIVATE)
@Getter
@FieldDefaults(level=AccessLevel.PRIVATE)
@Table(name="CUSTOMER_ORDER")
public class Order extends AbstractPersistable<Long> {

    @Id
    @GeneratedValue(strategy=IDENTITY)
    Long id;

    UUID orderStateMachine;

}
