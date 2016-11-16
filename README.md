[ ![Build Status](https://travis-ci.org/otrosien/spring-statemachine-jpa.svg)](https://travis-ci.org/otrosien/spring-statemachine-jpa)


# Spring Statemachine using JPA and REST

This is just a sample project trying out the combination of Spring-Statemachine,
JPA and Spring Data REST.

## The State Machine

The state machine is a simulation of an order fulfillment process, offering two different flows: One for pay-before-shipping
(prepayment, paypal etc.), and one for ship-before-payment (cash-on-delivery, invoice). Ultimately they form one state machine. Some transitions have guards depending on the "paid" status of the order, which gets set when the "ReceivePayment" event occurs (and un-set on "Refund"). Other than that, the state machine is pretty straight-forward.

The two flows are shown below.

```
    +----------------------------------------------------------------------------------------------------------------------------+
    |                                                     pre-payment flow                                                       |
    +----------------------------------------------------------------------------------------------------------------------------+
    |                                (1)                            (2) [if paid]                 (3) [if paid]                  |
    |     +------------------+ ReceivePayment  +-- ---------------+  Deliver +------------------+  Refund  +------------------+  |
    | *-->|       Open       |---------------->| ReadyForDelivery |--------->|    Completed     |--------->|     Canceled     |  |
    |     |                  |                 |                  |          |                  |          |                  |  |
    |     |                  |                 |  ReceivePayment  |          |                  |          |  ReceivePayment  |  |
    |     |                  |                 |  +------------+  |          |                  |          |  +------------+  |  |
    |     |                  |                 |  |    (11)    |  |          |                  |          |  |    (12)    |  |  |
    |     |                  |                 |  |            v  |          |                  |          |  |            v  |  |
    |     +------------------+                 +------------------+          +------------------+          +------------------+  |
    |        | ^                                             | |         [if paid] (4) Refund                ^   ^       | ^     |
    |        | |                                             | +---------------------------------------------+   |       | |     |
    |        | |                                             |                                                   |       | |     |
    |        | |                                             |           [if !paid]  (8) Cancel                  |       | |     |
    |        | |           (5) Reopen                        +---------------------------------------------------+       | |     |
    |        | +---------------------------------------------------------------------------------------------------------+ |     |
    |        |                                              (6) Cancel                                                     |     |
    |        +-------------------------------------------------------------------------------------------------------------+     |
    |                                                                                                                            |
    +----------------------------------------------------------------------------------------------------------------------------+


    +-------------------------------------------------------------------------------------------------------------------------------------------------------------+
    |                                                                     post-payment flow                                                                       |
    +-------------------------------------------------------------------------------------------------------------------------------------------------------------+
    |                                (7)                            (9) [if !paid]                 (10)                            (3) [if paid]                  |
    |     +------------------+ UnlockDelivery  +-- ---------------+  Deliver +------------------+ ReceivePayment +---------------+  Refund  +------------------+  |
    | *-->|       Open       |---------------->| ReadyForDelivery |--------->| AwaitingPayment  |--------------->|   Completed   |--------->|     Canceled     |  |
    |     |                  |                 |                  |          |                  |                |               |          |                  |  |
    |     |                  |                 |  ReceivePayment  |          |                  |                |               |          |  ReceivePayment  |  |
    |     |                  |                 |  +------------+  |          |                  |                |               |          |  +------------+  |  |
    |     |                  |                 |  |    (11)    |  |          |                  |                |               |          |  |    (12)    |  |  |
    |     |                  |                 |  |            v  |          |                  |                |               |          |  |            v  |  |
    |     +------------------+                 +------------------+          +------------------+                +---------------+          +------------------+  |
    |        | ^                                             |  |          [if paid] (4) Refund                                               ^    ^      | ^     |
    |        | |                                             |  +-----------------------------------------------------------------------------+    |      | |     |
    |        | |                                             |                                                                                     |      | |     |
    |        | |                                             |             [if !paid] (8) Cancel                                                   |      | |     |
    |        | |           (5) Reopen                        +-------------------------------------------------------------------------------------+      | |     |
    |        | +------------------------------------------------------------------------------------------------------------------------------------------+ |     |
    |        |                                              (6) Cancel                                                                                      |     |
    |        +----------------------------------------------------------------------------------------------------------------------------------------------+     |
    |                                                                                                                                                             |
    +-------------------------------------------------------------------------------------------------------------------------------------------------------------+
```

## Persistence

I use a simple domain object as state machine context (my `Order` entity), which holds
the StateMachineContext in a binary-serialized form, using Kryo.


## The REST-ful API

The REST API exposes links for receiving events to trigger state transitions for
the state machine. The server either responds with `202 Accepted` if an event was accepted,
or `422 Unprocessable Entity` if the event was not accepted by the state machine.
The resource intentionally does not expose the current state, but links for triggering
the state transitions that are possible for the given state.


```
{
    "_links": {
        "cancel": {
            "href": "http://localhost:8080/orders/1/receive/Cancel"
        }, 
        "order": {
            "href": "http://localhost:8080/orders/1"
        }, 
        "receive-payment": {
            "href": "http://localhost:8080/orders/1/receive/ReceivePayment"
        }, 
        "self": {
            "href": "http://localhost:8080/orders/1"
        }, 
        "unlock-delivery": {
            "href": "http://localhost:8080/orders/1/receive/UnlockDelivery"
        }
    }
}
```

### Order-specfic components

- *Order* : My entity acting as context object

- *OrderStateMachineConfiguration* : The setup of the state machine.

- *OrderCreatedEventListener* : An `AbstractRepositoryEventListener` getting notified
when a new order is created, initializing it with a new state machine.

- *OrderEventController* : RestController for receiving events.

### Generic components

- *ContextEntity* : Interface combining the role of a statemachine context object and spring-hateoas Identifiable

- *ContextObjectResourceProcessor* : For adding the links to the response.

- *StateMachineContextConverter* : The converter from StateMachineContext to byte[] and back.

