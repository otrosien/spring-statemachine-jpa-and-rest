package com.example;

import java.io.Serializable;
import java.util.function.Predicate;

import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.transition.Transition;

import com.example.order.OrderState;
import com.example.order.OrderEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ContextObjectResourceProcessor<S, E> implements ResourceProcessor<Resource<ContextObject<S, E>>> {

    private static final KebabCaseStrategy TO_KEBAB = new KebabCaseStrategy();

    final EntityLinks entityLinks;

    final DefaultStateMachineAdapter<S, E, ContextObject<S, E>> stateMachineAdapter;

    @Override
    public Resource<ContextObject<S, E>> process(Resource<ContextObject<S, E>> resource) {
        ContextObject<S, E> contextObject = resource.getContent();
        StateMachine<S, E> stateMachine = stateMachineAdapter.restore(contextObject);

        for (Transition<S, E> transition : stateMachine.getTransitions()) {
            if(stateMachine.getState().getId().equals(transition.getSource().getId())) {
                E event = transition.getTrigger().getEvent();
                log.info("Found transition triggered by event: " + event);
                resource.add(eventLink(contextObject, event, TO_KEBAB.translate(event.toString())));
            }
        }

        return resource;
    }

    private Link eventLink(ContextObject<S, E> contextObject, E event, String rel) {
        // TODO: Fix this ugly cast!
        return entityLinks.linkForSingleResource((Identifiable<Serializable>)contextObject).slash("receive").slash(event).withRel(rel);
    }

    static class PaidPredicate implements Predicate<StateMachine<OrderState, OrderEvent>> {
        @Override
        public boolean test(StateMachine<OrderState, OrderEvent> stateMachine) {
            return stateMachine.getExtendedState().get("paid", Boolean.class);
        }
    }

    // copied from Jackson.PropertyNamingStrategy
    static class KebabCaseStrategy
    {
        public String translate(String input)
        {
            if (input == null) return input; // garbage in, garbage out
            int length = input.length();
            if (length == 0) {
                return input;
            }

            StringBuilder result = new StringBuilder(length + (length >> 1));

            int upperCount = 0;

            for (int i = 0; i < length; ++i) {
                char ch = input.charAt(i);
                char lc = Character.toLowerCase(ch);
                
                if (lc == ch) { // lower-case letter means we can get new word
                    // but need to check for multi-letter upper-case (acronym), where assumption
                    // is that the last upper-case char is start of a new word
                    if (upperCount > 1) {
                        // so insert hyphen before the last character now
                        result.insert(result.length() - 1, '-');
                    }
                    upperCount = 0;
                } else {
                    // Otherwise starts new word, unless beginning of string
                    if ((upperCount == 0) && (i > 0)) {
                        result.append('-');
                    }
                    ++upperCount;
                }
                result.append(lc);
            }
            return result.toString();
        }
    }

}
