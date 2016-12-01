[ ![Build Status](https://travis-ci.org/otrosien/spring-statemachine-jpa-and-rest.svg)](https://travis-ci.org/otrosien/spring-statemachine-jpa-and-rest)


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

![UML state machine](https://github.com/otrosien/spring-statemachine-jpa-and-rest/raw/master/src/main/resources/order.png)

## Persistence

I use a simple domain object as state machine context (my `Order` entity), which holds
the StateMachineContext in a binary-serialized form, using [Kryo](https://github.com/EsotericSoftware/kryo).


## The REST-ful API

The REST API exposes links for receiving events to trigger state transitions for
the state machine. The server either responds with `202 Accepted` if an event was accepted,
or `422 Unprocessable Entity` if the event was not accepted by the state machine.

### Current State and Links

The resource intentionally does not expose its current state as enum, but as a localized text, so that clients
are aware of the fact they should not build business logic upon that.
Instead the client should use the links for triggering the state transitions that are possible for 
the given state. You can get more details about why you should expose links instead of status 
properties in Oliver Gierke's talk
about [DDD and REST](https://spring.io/blog/2016/11/15/springone-platform-2016-replay-ddd-rest-domain-driven-apis-for-the-web).


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

## User guides

Further information can be found by the documentation guides generated from the latest `master` build.

* [API guide](https://otrosien.github.io/spring-statemachine-jpa-and-rest/html5/api-guide.html)
* [Getting started](https://otrosien.github.io/spring-statemachine-jpa-and-rest/html5/getting-started-guide.html)



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

