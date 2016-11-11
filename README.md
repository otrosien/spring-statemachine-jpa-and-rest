# Spring Statemachine using JPA

This is just a sample project trying out the combination of Spring-Statemachine,
JPA and Spring Data REST.

## The State Machine

The state machine is a simulation of an order fulfillment process, offering two different flows. One for pay-before-shipping
(prepayment, paypal etc.), and one for ship-before-payment (cash-on-delivery, invoice).

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


## The REST API

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

### The relevant components

- *Order* : My entity acting as context object

- *OrderStateMachineConfiguration* : The setup of the state machine.

- *OrderCreatedEventListener* : An `AbstractRepositoryEventListener` getting notified
when a new order is created, initializing it with a new state machine.

- *OrderEventResourceProcessor* : For adding the links to the response.

- *OrderStateMachineContextConverter* : The converter from OrderStateMachineContext to byte[] and back.

- *OrderEventController* : RestController for receiving events.

## What needs improvement

I haven't fully figured out yet, how to introspect the state machine meta model,
to produce links in a more generic fashion. Until then the `OrderEventResourceProcessor`
has full knowledge of the states and transitions, and renders links accordingly.
