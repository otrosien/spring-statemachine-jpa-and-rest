package com.example;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access=AccessLevel.PRIVATE)
@FieldDefaults(level=AccessLevel.PRIVATE)
@Table(name="STATE_MACHINE")
public class OrderStateMachineContext {

    @Id
    @NonNull
    String id;

    @NonNull
    @Lob
    byte[] state;

}
