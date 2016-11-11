package com.example;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.kryo.MessageHeadersSerializer;
import org.springframework.statemachine.kryo.StateMachineContextSerializer;
import org.springframework.statemachine.kryo.UUIDSerializer;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.example.OrderStateMachineConfiguration.OrderEvent;
import com.example.OrderStateMachineConfiguration.OrderState;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Converter(autoApply=true)
public class OrderStateMachineContextConverter implements AttributeConverter<StateMachineContext<OrderState, OrderEvent>, byte[]> {

    private static final ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() {

        @SuppressWarnings("rawtypes")
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.addDefaultSerializer(StateMachineContext.class, new StateMachineContextSerializer());
            kryo.addDefaultSerializer(MessageHeaders.class, new MessageHeadersSerializer());
            kryo.addDefaultSerializer(UUID.class, new UUIDSerializer());
            return kryo;
        }
    };

    @Override
    public byte[] convertToDatabaseColumn(StateMachineContext<OrderState, OrderEvent> attribute) {
        return serialize(attribute);
    }

    @Override
    public StateMachineContext<OrderState, OrderEvent> convertToEntityAttribute(byte[] dbData) {
        return deserialize(dbData);
    }

    private byte[] serialize(StateMachineContext<OrderState, OrderEvent> context) {
        if(context == null) {
            return null;
        }
        Kryo kryo = kryoThreadLocal.get();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Output output = new Output(out);
        kryo.writeObject(output, context);
        output.close();
        return out.toByteArray();
    }

    @SuppressWarnings("unchecked")
    private StateMachineContext<OrderState, OrderEvent> deserialize(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        Kryo kryo = kryoThreadLocal.get();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        Input input = new Input(in);
        return kryo.readObject(input, StateMachineContext.class);
    }

}