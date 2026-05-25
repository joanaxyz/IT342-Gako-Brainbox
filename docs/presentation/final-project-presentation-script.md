# BrainBox Final Project Presentation Script

This script matches the revised HTML deck and is designed for a `9 to 10 minute` presentation.

## Main Presentation File

Open this deck in your browser:

- [brainbox-presentation.html](C:/Users/Personal%20Computer/Documents/backups/brainbox/docs/presentation/brainbox-presentation.html)

## Simple Navigation

1. `Right Arrow`, `Space`, or `Enter` goes to the next slide.
2. `Left Arrow`, `Backspace`, or `Page Up` goes to the previous slide.
3. `Home` jumps to slide 1.
4. `End` jumps to the last slide.
5. `F` toggles fullscreen.

## Rubric Coverage

This deck now follows your instructor's required parts in this order:

1. `Brief self-introduction`: slide 1
2. `System project introduction`: slide 2
3. `Main features of the system`: slide 3
4. `System architecture, component interaction, and proof of implementation`: slides 4 to 10
5. `System demonstration with voice-over`: slide 11
6. `Closing summary`: slide 12

## Exact Architecture Answer You Can Use

If your instructor asks what architecture BrainBox uses, this is the most accurate answer:

1. BrainBox uses a `client-server architecture`.
2. The backend is a `modular monolith`, because it is one Spring Boot application with one main database, not a microservices system.
3. The API follows a `REST API architecture`.
4. Internally, the backend uses a `layered architecture` through auth interception, controllers, services, repositories, and database persistence.
5. The backend uses `Spring MVC` for HTTP routing, controller handling, request mapping, and exception handling.
6. The web client is a `React single-page application` organized by routes, feature folders, hooks, contexts, and API service modules.
7. The mobile client is a `Jetpack Compose feature-sliced application` organized by scenes or screens, coordinators, repositories, and Retrofit API services.
8. The system also demonstrates `repository pattern` usage and feature-oriented internal organization across the backend and mobile app.

## What To Say About Project Structure

Use this phrasing on the implementation-structure slide:

1. The repository is first divided into `web`, `backend`, `mobile`, and `docs`.
2. That top-level structure proves the project supports multiple platforms and clear separation of concerns.
3. Inside the web client, the code is grouped into feature folders such as `auth`, `home`, `notebook`, and `ai`, with shared areas like `app` and `common`.
4. Inside the backend, the code is organized into feature modules such as `auth`, `notebook`, `quiz`, `flashcard`, `playlist`, `playbackqueue`, `ai`, `user`, and `category`.
5. That is why the backend can also be described as feature-oriented or vertical-slice internally, because each feature keeps related controller, service, repository, DTO, and entity code close together.
6. Inside the mobile app, the code is structured into `app`, `features`, `platform`, and `shared`, which follows the same feature-centered idea while keeping reusable infrastructure separate.

## Slide-By-Slide Script

### Slide 1: Cover
**Time:** 0:00 to 0:30  
**Visual:** BrainBox logo, your name, course or section, and project title.  
**What to do:** Stay on this slide for your self-introduction, then transition directly into the system.

**Say:**

Good day everyone. I am `[Your Name]` from `[Course and Section]`, and for my final project presentation, I will be presenting my individual system project called BrainBox. BrainBox is an integrated study platform designed to combine notebook management, AI assistance, quizzes, flashcards, playlists, playback, and version history in one connected learning system. In the next part of this presentation, I will introduce the purpose of the system, its main features, the architecture used, and how that architecture is reflected in the actual implementation.

### Slide 2: System Project Introduction
**Time:** 0:30 to 1:20  
**Visual:** Purpose, problem, intended users, and overall goal.  
**What to do:** Explain the four sections from left to right or top to bottom.

**Say:**

The main purpose of BrainBox is to provide students with a centralized study platform where multiple learning activities can be performed in one system. The problem it addresses is the fragmented workflow that happens when learners use separate applications for notes, flashcards, quizzes, playback, and other study tasks. The intended users of the system are students and self-directed learners who want a more organized and efficient learning environment. Overall, the goal of the project is to deliver a secure and integrated study system where content creation, review, AI support, and active recall features all work together through one connected architecture.

### Slide 3: Main Features
**Time:** 1:20 to 2:10  
**Visual:** Feature groups arranged by actual implemented functionality.  
**What to do:** Move through the feature groups in order.

**Say:**

The implemented features of BrainBox are grouped into five main areas. First is access and account security, which includes registration, email verification, login, logout, refresh token handling, forgot password, reset password, and Google sign-in. Second is notebook and category management, which includes notebook creation, content editing, note organization, recently edited tracking, recently reviewed tracking, and notebook version history. Third is AI-assisted study support, where the user can configure an AI provider, ask notebook-based questions, and save AI conversations. Fourth is active recall and content delivery, which includes quizzes, flashcards, playlists, playback queue management, and guided review or listening flows. Fifth is cross-platform support, where the same backend capabilities are used by both the web client and the Android mobile app.

### Slide 4: Architecture Used in BrainBox
**Time:** 2:10 to 3:10  
**Visual:** Architecture classification rows.  
**What to do:** Explain the rows in order from top to bottom.

**Say:**

In terms of architecture, BrainBox uses a client-server architecture because the web client and the Android mobile app both communicate with one backend API. The backend is a modular monolith because it runs as one Spring Boot application with one main database, while its logic is still divided into feature modules. The communication style is REST API architecture, since the clients send HTTP requests and receive JSON responses. Internally, the backend follows a layered architecture where the request passes through auth interception, controllers, services, repositories, and database persistence. For the applicable design patterns, the backend uses Spring MVC for request handling, while repository-based access is used in both backend and mobile. On the client side, the web application is a React single-page application organized through routes, hooks, contexts, and API services, while the mobile app is a Jetpack Compose application organized through feature slices, scenes or screens, coordinators, repositories, and Retrofit services. Finally, this architecture also includes database and external-service integration for Google sign-in, email delivery, and AI provider communication.

### Slide 5: BrainBox Architecture Diagram
**Time:** 3:10 to 3:55  
**Visual:** Full architecture diagram.  
**What to do:** Point to the main areas in this order: clients, backend, database, external services.

**Say:**

This is the main architecture diagram of BrainBox. At the top, we can see the two clients, which are the React web client and the Android mobile app. Both of them communicate with the Spring Boot backend API using authenticated REST requests. Inside the backend, we can see the security layer, the controller and service flow, and the feature modules such as authentication, notebook, quiz, flashcard, playlist, playback queue, user profile, and AI. At the bottom, we can see the PostgreSQL database where the system stores persistent data. On the side, we can also see the external integrations, which include Google identity, the email provider, and the AI provider or proxy. This diagram gives the full system view before we move to proof from the actual codebase.

### Slide 6: Codebase Organization
**Time:** 3:55 to 4:45  
**Visual:** Repository structure and notes for web, backend, and mobile organization.  
**What to do:** Start with the tree, then explain the supporting notes.

**Say:**

This slide is the first proof that the architecture is reflected in the real implementation. At the top level, the repository is clearly divided into web, backend, mobile, and docs. That proves the project is multi-platform and not only a single application. Inside the web client, the code is organized into feature folders such as auth, home, notebook, and AI, together with app and common shared areas. Inside the backend, the code is divided into feature modules like auth, notebook, quiz, flashcard, playlist, playback queue, AI, user, and category. This means the backend is not only layered, but also feature-oriented internally, because each feature keeps its related controller, service, repository, DTO, and entity code close together. Inside the mobile app, the code is organized into app, features, platform, and shared packages, which keeps feature flows separate from reusable Android infrastructure. This slide shows that the architecture is visible not only in diagrams, but also in the real project structure.

### Slide 7: Notebook Save Flow in Code
**Time:** 4:45 to 5:40  
**Visual:** Frontend API call, controller route, service logic, and version snapshot listener.  
**What to do:** Explain the flow from the web client to the listener.

**Say:**

This slide is a strong proof of implementation because it shows a real end-to-end feature path in the actual code. In the frontend, the notebook service sends a PUT request to the notebook content endpoint. In the backend controller, Spring MVC receives that request and forwards it to the notebook service together with the authenticated user and mutation metadata. Inside the notebook service, the content is validated and then saved through the notebook repository. After the notebook is successfully persisted, the backend publishes a notebook content saved event. Finally, the notebook version snapshot listener receives that event and creates a version snapshot. This proves that one user action in the editor is directly connected to routing, business logic, persistence, and versioning behavior in the backend.

### Slide 8: External Integrations in Code
**Time:** 5:40 to 6:30  
**Visual:** Google sign-in, email integration, and AI integration code panels.  
**What to do:** Explain the three panels from left to right.

**Say:**

This slide proves how BrainBox integrates with external services in the actual implementation. On the left, the authentication controller and service handle Google sign-in. The backend validates the Google token on the server side before creating or linking a user account. In the middle, the authentication service and email service handle verification and reset workflows. The backend builds the verification or reset link, then sends the email through the configured provider. On the right, the AI controller and AI service show that AI requests are also processed through the backend. The backend checks notebook ownership, loads the user's saved AI configuration, decrypts the API key, builds the prompt, and only then sends the request to the configured AI provider or proxy. This proves that sensitive integrations are controlled by the backend rather than being handled directly by the client.

### Slide 9: Database Model
**Time:** 6:30 to 7:00  
**Visual:** Full ERD.  
**What to do:** Focus on the key entities and their relationships.

**Say:**

This ERD shows the persistent data model of the system. The user is the central owner of most records, including notebooks, AI configurations, conversations, quizzes, flashcards, playlists, playback queues, refresh tokens, and categories. We can also see notebook versions and mutation records, which support version tracking and reliable updates. Study attempts for quizzes and flashcards are also stored as separate records. This proves that BrainBox is supported by a structured relational database model and not only by temporary or loosely connected storage.

### Slide 10: Notebook Save Sequence Diagram
**Time:** 7:00 to 7:40  
**Visual:** Full sequence diagram for the main save flow.  
**What to do:** Explain the flow from left to right.

**Say:**

This sequence diagram shows how the different components communicate during the main notebook save flow. First, the client sends the authenticated save request. Next, the JWT interceptor validates the token and attaches the request context. Then, the controller forwards the request to the notebook service, where the system performs mutation checking, ownership checking, version checking, and content persistence. After the notebook is saved, an event is published, the snapshot listener creates the version record, and the response is returned to the client. This slide is important because it clearly shows component interaction and data flow from the user interface to the backend and database, then back to the client response.

### Slide 11: System Demonstration
**Time:** 7:40 to 9:35  
**Visual:** Recorded demo video placeholder.  
**What to do:** Play your recorded system walkthrough here and narrate what is happening.

**Say during the demo portion:**

At this point, I will now demonstrate the actual BrainBox system. First, I will show the login flow to demonstrate the authentication feature. Next, I will open the notebook module and perform a notebook edit and save operation. While doing that, I will explain that this action follows the same request flow shown in the code and sequence-diagram slides. After that, I will show the AI assistance feature and explain that the request is processed through the backend before reaching the configured AI provider. Then I will show version history to prove that notebook snapshots are stored after content changes. If time allows, I will also show one study module such as quizzes, flashcards, playlists, or playback queue to demonstrate how BrainBox supports active recall and guided study delivery.

### Slide 12: Closing Summary
**Time:** 9:35 to 10:00  
**Visual:** Final technical takeaways.  
**What to do:** Stay on this slide until the end.

**Say:**

In conclusion, BrainBox is an integrated study platform that demonstrates systems integration and architecture through a multi-client design, a modular monolith backend, REST API communication, layered backend processing, secure authentication, relational database persistence, and controlled external-service integration. The architecture is not only described in diagrams, but also reflected in the actual project structure, request flow, database design, and working system behavior. Thank you, and that ends my presentation.

## Best Demo Order

If you want the safest and clearest demo flow, use this order:

1. Login
2. Dashboard or home page
3. Notebook selection
4. Edit notebook content
5. Save notebook content
6. Show version history
7. Show AI assistance
8. Show one study module
9. End with playlist or playback queue if time remains

## If You Need To Cut Time

Cut these first:

1. Keep the codebase organization slide shorter.
2. Summarize the ERD instead of explaining many entities.
3. Shorten the architecture classification slide to the five most important rows.
4. Focus the demo on login, notebook save, AI, and version history.
