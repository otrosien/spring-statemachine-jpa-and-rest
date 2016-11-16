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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import lombok.RequiredArgsConstructor;

@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
@Converter(autoApply = true)
public class StateMachineContextConverter implements AttributeConverter<StateMachineContext, byte[]> {

    private static final ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() {

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
    public byte[] convertToDatabaseColumn(StateMachineContext attribute) {
        return serialize(attribute);
    }

    @Override
    public StateMachineContext convertToEntityAttribute(byte[] dbData) {
        return deserialize(dbData);
    }

    private byte[] serialize(StateMachineContext context) {
        if (context == null) {
            return null;
        }
        Kryo kryo = kryoThreadLocal.get();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Output output = new Output(out);
        kryo.writeObject(output, context);
        output.close();
        return out.toByteArray();
    }

    private StateMachineContext deserialize(byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        Kryo kryo = kryoThreadLocal.get();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        Input input = new Input(in);
        return kryo.readObject(input, StateMachineContext.class);
    }

}