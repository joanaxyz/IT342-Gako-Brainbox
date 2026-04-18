# Software Design Patterns — Research Document

**Course:** IT342 — System Integration and Architecture  
**Project:** BrainBox  
**Author:** Gako, Joana Carla D.  
**Date:** April 7, 2026

---

## Table of Contents

1. [Creational Patterns](#creational-patterns)
   - [Factory Method](#1-factory-method)
   - [Builder](#2-builder)
2. [Structural Patterns](#structural-patterns)
   - [Adapter](#3-adapter)
   - [Facade](#4-facade)
3. [Behavioral Patterns](#behavioral-patterns)
   - [Strategy](#5-strategy)
   - [Observer](#6-observer)
   - [Template Method](#7-template-method)

---

## Creational Patterns

Creational patterns deal with **object creation mechanisms**. They abstract the instantiation process so the system is independent of how its objects are created, composed, and represented.

---

### 1. Factory Method

| | |
|---|---|
| **Category** | Creational |
| **Also Known As** | Virtual Constructor |

#### Problem it Solves

When a class cannot anticipate the exact type of object it needs to create, hard-coding `new ConcreteClass()` throughout the codebase creates tight coupling. If the concrete type needs to change — based on configuration, environment, or runtime state — every call site must be updated. The Factory Method pattern solves this by defining an interface for creating an object but letting subclasses (or a dedicated factory component) decide which class to instantiate.

#### How it Works

A **creator** declares a factory method that returns an object conforming to a product interface. Concrete creators override the factory method to return different concrete products. Clients use the product through the interface and never reference the concrete type directly.

```
Creator
  └── factoryMethod() → Product (interface)
        ├── ConcreteCreatorA → ConcreteProductA
        └── ConcreteCreatorB → ConcreteProductB
```

#### Real-World Example

A payment gateway integration in an e-commerce backend. A `PaymentProcessorFactory` examines the user's selected method (`STRIPE`, `PAYPAL`, `GCASH`) and returns the appropriate processor implementing a `PaymentProcessor` interface. The checkout service calls `factory.create(method).charge(amount)` without knowing which processor is active.

#### Use Case in BrainBox

`EmailSenderFactory` selects between `SmtpEmailSender` (debug/dev) and `ResendEmailSender` (production) based on the `app.debug` property. The factory method `create()` returns an `EmailSender` interface. `EmailService` calls `factory.getSender().send(...)` without any knowledge of which transport is used, making environment switching zero-code-change.

---

### 2. Builder

| | |
|---|---|
| **Category** | Creational |
| **Also Known As** | Step Builder |

#### Problem it Solves

When an object requires many parameters for construction — some optional, some required — constructors become unwieldy ("telescoping constructor" anti-pattern). Using setters after construction leaves the object in a partially initialized, potentially invalid state. The Builder pattern provides a fluent API that constructs the object step-by-step and only produces a fully valid instance at the final `build()` call.

#### How it Works

A **Builder** class mirrors the target object's fields with setter-style methods that return `this` for chaining. A `build()` method validates required fields and constructs the final object. The **Director** (or calling code) chains builder calls in any order and calls `build()` when ready.

```
new ProductBuilder()
    .field1(value)
    .field2(value)
    .optionalField(value)
    .build()  ← validates and returns Product
```

#### Real-World Example

Building HTTP requests in Java's `HttpRequest` API: `HttpRequest.newBuilder().uri(...).header(...).POST(...).build()`. The same pattern appears in Spring's `ResponseEntity.ok().header(...).body(...)` and in test data factories (e.g., building User fixtures with only the fields relevant to a specific test).

#### Use Case in BrainBox

`NotebookBuilder` constructs `Notebook` JPA entities in `NotebookService.createNotebook()`. Before refactoring, five consecutive setter calls could leave a `Notebook` without a required `user` field if one was accidentally skipped. The builder enforces `owner` as a required field — `build()` throws `IllegalStateException` if it is missing — and keeps construction logic out of the service method entirely.

---

## Structural Patterns

Structural patterns deal with **object composition**. They create relationships between objects to form larger structures while keeping those structures flexible and efficient.

---

### 3. Adapter

| | |
|---|---|
| **Category** | Structural |
| **Also Known As** | Wrapper |

#### Problem it Solves

When two components need to work together but have incompatible interfaces, the Adapter pattern introduces a translator between them. This avoids modifying either component, which is especially valuable when one of them is a third-party library or legacy code you do not own.

#### How it Works

The **Adapter** class implements the **Target** interface expected by the client, holds a reference to the **Adaptee** (the incompatible component), and translates each target method call into the corresponding adaptee call.

```
Client → Target Interface
              ↑
           Adapter (wraps Adaptee)
              ↓
           Adaptee (third-party / legacy)
```

#### Real-World Example

Spring Data's `JpaRepository` adapts Hibernate's `Session` API into a clean generic repository interface. Application code calls `repository.findById(id)` — the repository adapter translates that into HQL queries against the Hibernate session. The application never interacts with Hibernate directly.

#### Use Case in BrainBox

`AiProvider` is the target interface. `ProxyProvider` is the adapter that wraps raw `java.net.http.HttpClient` calls to an OpenAI-compatible HTTP endpoint and translates them into the `generateResponse()` and `transcribeAudio()` methods the rest of the system expects. `AiService` depends only on `AiProvider`, so the HTTP implementation can be swapped (e.g., for a direct OpenAI client, Ollama, or a mock in tests) without touching the service.

---

### 4. Facade

| | |
|---|---|
| **Category** | Structural |
| **Also Known As** | Service Layer Facade |

#### Problem it Solves

A complex subsystem with many interacting classes exposes too many entry points to calling code. Every caller must understand how the subsystem components interact, leading to duplication and fragility. The Facade pattern provides a single, simplified interface that hides internal complexity, coordinates the subsystem components, and exposes only the operations the client actually needs.

#### How it Works

A **Facade** class aggregates references to subsystem classes and implements high-level methods that orchestrate lower-level subsystem calls. Clients interact only with the facade; the subsystem classes have no knowledge of the facade and can still be used directly when needed.

```
Client → Facade
           ├── SubsystemA
           ├── SubsystemB
           └── SubsystemC
```

#### Real-World Example

A hotel booking API. The `BookingFacade` exposes a single `book(room, guest, dates)` method that internally calls the room availability service, the payment service, the notification service, and the loyalty points service. The mobile app calls one method instead of orchestrating four services itself.

#### Use Case in BrainBox

`AuthFacade` is the interface that `AuthController` depends on. `AuthService` is the concrete facade that orchestrates five services: `JWTService`, `UserService`, `CodeService`, `RefreshTokenService`, and `EmailService`. Before this pattern was applied, the controller depended directly on `AuthService`, coupling it to the concrete implementation. Now the controller knows only the `AuthFacade` interface, simplifying testing (a mock facade can be injected) and keeping HTTP concerns separate from business logic.

---

## Behavioral Patterns

Behavioral patterns deal with **communication between objects**. They describe how objects interact and distribute responsibility to achieve flexibility in carrying out complex behavior.

---

### 5. Strategy

| | |
|---|---|
| **Category** | Behavioral |
| **Also Known As** | Policy |

#### Problem it Solves

When a class performs a task that can be done in multiple ways — and the algorithm needs to be selectable at runtime — embedding all variants inside one class creates a large conditional block (`if/else` or `switch`) that grows every time a new variant is needed. The Strategy pattern extracts each algorithm variant into its own class behind a common interface, making variants independently changeable, testable, and extendable.

#### How it Works

A **Strategy** interface declares the algorithm. **Concrete Strategies** implement it. The **Context** holds a reference to a Strategy and delegates the work to it, either set at construction time or switched at runtime.

```
Context → Strategy (interface)
               ├── ConcreteStrategyA
               ├── ConcreteStrategyB
               └── ConcreteStrategyC
```

#### Real-World Example

Sorting algorithms in a data grid UI. A `SortStrategy` interface with `sort(List<Row>)` is implemented by `AlphabeticalSort`, `NumericSort`, and `DateSort`. The grid holds a reference to the active strategy and re-sorts its data whenever the user clicks a column header, without any conditional logic in the grid class itself.

#### Use Case in BrainBox

`EmailSender` is the strategy interface with `send(to, subject, htmlContent)`. `SmtpEmailSender` implements it using `JavaMailSender` (for development/debug), and `ResendEmailSender` implements it using the Resend SDK (for production). `EmailSenderFactory` selects and wires the correct strategy at startup. The old `EmailService` had an `if (debug)` block that mixed two delivery implementations; the Strategy pattern eliminates that branch entirely and makes each implementation independently testable.

---

### 6. Observer

| | |
|---|---|
| **Category** | Behavioral |
| **Also Known As** | Event-Listener, Publish-Subscribe |

#### Problem it Solves

When one object changes state and other objects need to react, direct method calls from the subject to each dependent create tight coupling. Every new dependent requires modifying the subject. The Observer pattern lets dependents subscribe to events; the subject just publishes and never knows who is listening.

#### How it Works

The **Subject** (publisher) maintains an event channel. When relevant state changes, it publishes an **Event**. **Observers** (subscribers) register interest in that event type and are called automatically when it fires. Subject and observers are fully decoupled — neither holds a direct reference to the other.

```
Subject ──publishes──→ Event Bus
                           ↓ dispatches to registered
                       ObserverA
                       ObserverB
```

#### Real-World Example

Android's `LiveData` / `ViewModel` pattern. A `ViewModel` holds `LiveData<T>` (the subject). UI components call `liveData.observe(lifecycleOwner, callback)`. When the ViewModel updates the data, all observing UI components update automatically without the ViewModel knowing anything about the UI layer.

#### Use Case in BrainBox

`NotebookService` previously called `NotebookVersionSnapshotService.createSnapshot()` directly after every save, coupling two unrelated concerns. Now `NotebookService` publishes a `NotebookContentSavedEvent` via Spring's `ApplicationEventPublisher`. `NotebookVersionSnapshotListener` is annotated with `@EventListener` and reacts by creating the snapshot. `NotebookService` no longer depends on `NotebookVersionSnapshotService` at all. Future observers (e.g., full-text indexing, word-count analytics) can be added as new `@EventListener` beans without changing `NotebookService`.

---

### 7. Template Method

| | |
|---|---|
| **Category** | Behavioral |
| **Also Known As** | Template Pattern |

#### Problem it Solves

When multiple classes share the same algorithm skeleton but differ in a few specific steps, duplicating the skeleton in each class causes maintenance drift — fixing a bug in the shared steps means updating every copy. The Template Method pattern places the invariant algorithm skeleton in a base class as a final method, and delegates the variable steps to abstract "hook" methods that subclasses override.

#### How it Works

An **Abstract Class** defines `templateMethod()` as `final`, calling a series of steps in a fixed order. Some steps have default implementations; others are `abstract` — subclasses must override them. Subclasses provide only the varying pieces without touching the skeleton.

```
AbstractClass
  └── templateMethod() [final]
        ├── step1()        ← shared, defined in base
        ├── hookStep()     ← abstract, overridden by subclass
        └── step3()        ← shared, defined in base

ConcreteClassA overrides hookStep()
ConcreteClassB overrides hookStep()
```

#### Real-World Example

Spring's `AbstractController` and `JdbcTemplate`. `JdbcTemplate.query()` is a template method — it handles connection acquisition, statement preparation, exception translation, and connection release (invariant steps), while the `RowMapper` lambda you supply is the hook that varies per query.

#### Use Case in BrainBox

`AiPromptBuilder` defines `buildSystemPrompt()` as the final template method. It assembles all invariant prompt sections (working scope, action rules, quality standards, HTML format) in a fixed order and calls the abstract hook `buildModeBlock()` at the appropriate position. `EditorModePromptBuilder` overrides `buildModeBlock()` to allow full editor actions; `ReviewModePromptBuilder` overrides it to restrict the assistant to read-only actions only. `AiService` selects the correct subclass and calls `buildSystemPrompt()` — the mode logic is cleanly isolated in the subclass, removing the `reviewMode` boolean that previously threaded through every private helper method.

---

*Document prepared for IT342 — System Integration and Architecture, Design Patterns Activity.*
