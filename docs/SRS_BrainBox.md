# IT342-[Section] Systems Integration and Architecture

# Software Requirements Specification (SRS)

**Project Title:** BrainBox  
**Prepared By:** Gako, Joana Carla D.  
**Date of Submission:** May 1, 2026  
**Version:** 1.1  
**Status:** Revised

## Revision History

| Version | Date | Author | Changes Made | Status |
| --- | --- | --- | --- | --- |
| 1.0 | May 1, 2026 | Gako, Joana Carla D. | Initial SRS content for BrainBox requirements. | Final |
| 1.1 | May 1, 2026 | Gako, Joana Carla D. | Aligned requirements with the implemented backend, web, and mobile application; expanded user journeys; added explicit notebook version history, restore, editor export, and feature coverage details. | Revised |

## Table of Contents

0. [Revision History](#revision-history)  
1. [Introduction](#1-introduction)  
   1.1. [Purpose](#11-purpose)  
   1.2. [Scope](#12-scope)  
   1.3. [Definitions, Acronyms, and Abbreviations](#13-definitions-acronyms-and-abbreviations)  
2. [Overall Description](#2-overall-description)  
   2.1. [System Perspective](#21-system-perspective)  
   2.2. [User Classes and Characteristics](#22-user-classes-and-characteristics)  
   2.3. [Operating Environment](#23-operating-environment)  
   2.4. [Assumptions and Dependencies](#24-assumptions-and-dependencies)  
   2.5. [Core User Journeys](#25-core-user-journeys)  
   2.6. [Application Feature Coverage Matrix](#26-application-feature-coverage-matrix)  
3. [System Features and Functional Requirements](#3-system-features-and-functional-requirements)  
   3.1. [Feature 1: Authentication and Account Access](#31-feature-1-authentication-and-account-access)  
   3.2. [Feature 2: Profile Management](#32-feature-2-profile-management)  
   3.3. [Feature 3: Notebook Management](#33-feature-3-notebook-management)  
   3.4. [Feature 4: Category Management](#34-feature-4-category-management)  
   3.5. [Feature 5: AI Configuration and Conversations](#35-feature-5-ai-configuration-and-conversations)  
   3.6. [Feature 6: Quiz Management and Study](#36-feature-6-quiz-management-and-study)  
   3.7. [Feature 7: Flashcard Management and Study](#37-feature-7-flashcard-management-and-study)  
   3.8. [Feature 8: Playlist Management](#38-feature-8-playlist-management)  
   3.9. [Feature 9: Playback Queue and Audio Study](#39-feature-9-playback-queue-and-audio-study)  
   3.10. [Feature 10: Mobile Embedded Editor](#310-feature-10-mobile-embedded-editor)  
   3.11. [Feature 11: Admin Management](#311-feature-11-admin-management)  
4. [Non-Functional Requirements](#4-non-functional-requirements)  
5. [System Models (Diagrams)](#5-system-models-diagrams)  
   5.1. [ERD](#51-erd)  
   5.2. [Use Case Diagram](#52-use-case-diagram)  
   5.3. [Activity Diagram](#53-activity-diagram)  
   5.4. [Class Diagram](#54-class-diagram)  
   5.5. [Sequence Diagram](#55-sequence-diagram)  
6. [Appendices](#6-appendices)

## 1. Introduction

### 1.1. Purpose

This document describes the functional and non-functional requirements of BrainBox. It is intended for the project developer, instructor, testers, and evaluators who need to understand what the system must do and what constraints the system must satisfy.

### 1.2. Scope

BrainBox is a multi-platform study application that allows users to manage notebooks, generate and study quizzes and flashcards, organize study materials, use AI-assisted notebook features, maintain playlists and playback queues, and recover previous notebook content through version history.

The system includes a Spring Boot backend API, a React/Vite web application, and an Android/Kotlin mobile application. The system covers authentication, profile management, notebook management, notebook content versioning, categories, AI configuration and conversations, quizzes, flashcards, playlists, playback queue, mobile embedded editor support, mobile audio playback, and admin API management.

The system does not cover payment processing, real-time collaborative editing, public social sharing, production analytics dashboards, or offline mobile study mode.

### 1.3. Definitions, Acronyms, and Abbreviations

| Term | Definition |
| --- | --- |
| AI | Artificial intelligence support used for notebook assistance and generated study materials. |
| API | Application Programming Interface. |
| Client Mutation ID | Client-generated identifier used to make selected write operations idempotent. |
| DTO | Data Transfer Object used for API request and response payloads. |
| JWT | JSON Web Token used for authenticated API access. |
| SDD | System Design Document. |
| SRS | Software Requirements Specification. |
| Version History | Saved notebook content snapshots that users can list, preview, and restore. |
| WebView | Android component used to host the embedded notebook editor. |

## 2. Overall Description

### 2.1. System Perspective

BrainBox is a client-server system. The web and mobile clients communicate with the Spring Boot backend through REST API endpoints. The backend handles authentication, user-owned study data, generated study content, AI configuration, notebook versioning, playback queue state, and admin operations. The web application provides the full notebook editor, study management screens, and playback queue UI. The Android application provides authenticated mobile screens, an embedded WebView notebook editor, quiz and flashcard study screens, and audio playback behavior through backend-provided data.

### 2.2. User Classes and Characteristics

| User Class | Characteristics |
| --- | --- |
| Guest User | Can register, verify email, log in, use Google sign-in, and request password recovery. |
| Authenticated User | Can manage profile, notebooks, notebook versions, categories, AI settings, AI conversations, quizzes, flashcards, playlists, playback queue, exports, and mobile study flows. |
| Admin User | Can access admin API endpoints for viewing and managing users and system records. |
| External Service | Email and AI provider services used by the backend for verification, reset, and AI workflows. |

### 2.3. Operating Environment

| Component | Operating Environment |
| --- | --- |
| Backend | Java 21, Spring Boot, Maven, PostgreSQL, REST over HTTP. |
| Web Frontend | React, Vite, React Router, TanStack Query, TipTap editor, modern browser. |
| Mobile Application | Android API 24 or higher, Kotlin, Jetpack Compose, Retrofit, Media3. |
| Development Tools | Git, Maven wrapper, Gradle wrapper, Node.js/npm, Android Studio or compatible IDE. |

### 2.4. Assumptions and Dependencies

- Users need internet access for authentication, API access, and AI requests.
- The backend depends on a configured PostgreSQL database.
- Email verification and password recovery depend on a configured email sender.
- Google sign-in depends on a valid Google web client ID.
- AI features depend on configured AI model, proxy URL, and API key values.
- The Android embedded notebook editor depends on the bundled web editor assets and WebView bridge readiness.

### 2.5. Core User Journeys

| Journey | Actor | Main Flow | Covered Features |
| --- | --- | --- | --- |
| Account access and session recovery | Guest User | User registers or signs in, verifies email when required, refreshes an authenticated session, logs out, or completes forgot-password verification and reset. | Authentication, email verification, Google sign-in, token refresh, password reset. |
| Notebook authoring and recovery | Authenticated User | User creates a notebook, selects or creates a category, writes content in the TipTap editor, saves changes, opens version history, filters saved versions, previews an older version, and restores it when needed. | Notebook CRUD, category management, version snapshots, version preview, version restore, conflict handling. |
| AI-assisted notebook study | Authenticated User | User configures/selects an AI provider, opens a notebook, selects text or AI highlight targets, sends an editor/review prompt, reviews the proposal, accepts useful changes, and saves the resulting notebook or generated study material. | AI configuration, AI query, AI conversations, selected-text assistance, quiz generation, flashcard generation. |
| Quiz and flashcard study | Authenticated User | User opens quizzes or flashcards from the web or mobile study tabs, filters available study sets, completes a quiz or deck, submits the attempt, and sees the updated score or mastery state. | Quiz CRUD, flashcard CRUD, attempts, score/mastery tracking, mobile study screens. |
| Playlist and playback study | Authenticated User | User creates a playlist, adds notebooks, reorders notebook items, sets the current index, starts notebook audio playback, and controls play, pause, skip, loop, shuffle, rate, and resume position. | Playlists, playback queue, web player bar, mobile global playbar, TTS/audio service, reviewed-state update. |
| Mobile embedded notebook flow | Authenticated User | User opens a notebook on Android, the app hosts the web editor in a WebView, the editor reports readiness, and mobile continues study through authenticated online API data. | Embedded editor, mobile feature tabs, token refresh, playback queue. |
| Admin API management | Admin User | Admin authenticates with an admin role, calls admin API endpoints, reviews users and system records, updates supported user data, and deletes valid records where the backend allows it. | Admin user, notebook, category, quiz, flashcard, playlist, playback queue, AI config, and AI conversation endpoints. |

### 2.6. Application Feature Coverage Matrix

| Feature Area | Backend Coverage | Web Coverage | Mobile Coverage |
| --- | --- | --- | --- |
| Authentication | `/api/auth` registration, login, logout, refresh, verification, reset, and Google auth endpoints. | Login, registration, forgot password, Google button, protected routes, logout. | Auth scenes, session store, token refresh, Google-capable auth DTOs. |
| Profile | `/api/user/me` and `/api/users/me` aliases for retrieval, update, and password change. | Profile page and profile dropdown. | Profile tab and user profile DTOs. |
| Notebooks and categories | Notebook/category CRUD, recent lists, review updates, ownership checks, mutation records. | Dashboard, library, categories, notebook cards, full editor route. | Dashboard/library tabs, notebook repository, WebView editor host. |
| Version history | Notebook version list, detail, manual snapshot, automatic snapshot on content save, restore, conflict response. | Version history sidebar, date filters, version preview overlay, restore action. | Notebook version API service methods available to the embedded editor flow. |
| Editor tools | Backend stores HTML content and versioned snapshots. | Formatting toolbar, tables, math, outline navigation, review mode, export to PDF/Word/Text. | Embedded web editor, mobile dock actions, Android host bridge readiness. |
| AI assistance | AI query endpoint, provider configs, selected config, per-notebook conversations. | AI config panel, editor/review AI sidebars, selected text/highlight tools, proposal preview. | AI is accessed through the embedded web editor rather than duplicate native editor panes. |
| Quizzes | Quiz CRUD and attempt recording with optional client mutation IDs. | Quiz list, create/edit composer, quiz player/results. | Quiz tab, quiz study screen, attempt submission. |
| Flashcards | Flashcard CRUD and attempt recording with optional client mutation IDs. | Flashcard list, create/edit composer, flashcard player/results. | Flashcard tab, study screen, attempt submission. |
| Playlists and queue | Playlist CRUD, reorder/current index, current playback queue endpoints and aliases. | Playlist page, player bar, queue panel. | Playlist tab, playback queue overlay, global playbar. |
| Admin management | Admin role endpoints for users, notebooks, notebook versions, categories, quizzes, flashcards, playlists, playback queues, AI configs, and conversations. | No dedicated admin route in the current React router. | No dedicated admin screen in the current Android app. |

## 3. System Features and Functional Requirements

### 3.1. Feature 1: Authentication and Account Access

**Description:** The system allows guest users to create an account, verify identity, log in, refresh sessions, log out, use Google sign-in, and recover forgotten passwords.

**Functional Requirements:**

- FR-AUTH-01: The system shall allow a guest user to register using username, email, and password.
- FR-AUTH-02: The system shall support email verification for registered users.
- FR-AUTH-03: The system shall allow a registered user to log in using valid credentials.
- FR-AUTH-04: The system shall issue access and refresh tokens after successful login.
- FR-AUTH-05: The system shall allow token refresh using a valid refresh token.
- FR-AUTH-06: The system shall allow a user to log out and end the active session.
- FR-AUTH-07: The system shall support forgot password, verification code, and reset password steps.
- FR-AUTH-08: The system shall support Google sign-in using a valid Google identity token.

### 3.2. Feature 2: Profile Management

**Description:** The system allows authenticated users to view and manage their own profile.

**Functional Requirements:**

- FR-PROF-01: The system shall allow an authenticated user to retrieve their profile.
- FR-PROF-02: The system shall allow an authenticated user to update editable profile information.
- FR-PROF-03: The system shall allow an authenticated user to change their password.

### 3.3. Feature 3: Notebook Management

**Description:** The system allows authenticated users to create, edit, review, organize, export, version, restore, and delete notebooks.

**Functional Requirements:**

- FR-NOTE-01: The system shall allow an authenticated user to create a notebook.
- FR-NOTE-02: The system shall allow an authenticated user to retrieve their notebook list.
- FR-NOTE-03: The system shall allow an authenticated user to retrieve recently edited notebooks.
- FR-NOTE-04: The system shall allow an authenticated user to retrieve recently reviewed notebooks.
- FR-NOTE-05: The system shall allow an authenticated user to retrieve a notebook by UUID.
- FR-NOTE-06: The system shall allow an authenticated user to update notebook metadata.
- FR-NOTE-07: The system shall allow an authenticated user to save notebook content.
- FR-NOTE-08: The system shall allow an authenticated user to update notebook review state.
- FR-NOTE-09: The system shall create version snapshots when notebook content is saved.
- FR-NOTE-10: The system shall allow an authenticated user to view notebook version history.
- FR-NOTE-11: The system shall allow an authenticated user to preview a previous notebook version before restoration.
- FR-NOTE-12: The system shall allow an authenticated user to delete a notebook they own.
- FR-NOTE-13: The system shall allow an authenticated user to restore a previous notebook version.
- FR-NOTE-14: The system shall allow an authenticated user to manually create a notebook version snapshot.
- FR-NOTE-15: The system shall detect stale notebook content updates through version information and return a conflict response with the latest notebook data.
- FR-NOTE-16: The web editor shall allow notebook content export to PDF/print, Word `.docx`, and text `.txt` formats.
- FR-NOTE-17: The web editor shall provide formatting, table, math, page break, outline navigation, and review-mode controls where supported by the implemented editor.
- FR-NOTE-18: The Android application shall host the notebook editor through WebView-compatible embedded web editor assets.

### 3.4. Feature 4: Category Management

**Description:** The system allows authenticated users to organize notebooks using categories.

**Functional Requirements:**

- FR-CAT-01: The system shall allow an authenticated user to retrieve categories.
- FR-CAT-02: The system shall allow an authenticated user to create a category.
- FR-CAT-03: The system shall allow an authenticated user to retrieve a category by ID.
- FR-CAT-04: The system shall allow an authenticated user to delete a category.
- FR-CAT-05: The system shall update affected notebooks when a category is deleted.
- FR-CAT-06: The system shall allow notebook records to reference a user-owned category.

### 3.5. Feature 5: AI Configuration and Conversations

**Description:** The system allows authenticated users to configure AI access and use AI assistance in notebook workflows.

**Functional Requirements:**

- FR-AI-01: The system shall allow an authenticated user to view the selected AI configuration.
- FR-AI-02: The system shall allow an authenticated user to list saved AI configurations.
- FR-AI-03: The system shall allow an authenticated user to create or update an AI configuration.
- FR-AI-04: The system shall allow an authenticated user to select an AI configuration.
- FR-AI-05: The system shall allow an authenticated user to delete an AI configuration.
- FR-AI-06: The system shall allow an authenticated user to submit an AI query.
- FR-AI-07: The system shall allow an authenticated user to list, create, update, and delete AI conversations for a notebook.
- FR-AI-08: The system shall support AI-assisted notebook editing, review, quiz creation, and flashcard creation when configured.
- FR-AI-09: The system shall support selected text and multiple AI selection targets in AI requests where provided by the editor.
- FR-AI-10: The system shall keep AI provider keys server-side and prevent them from being exposed in client-visible source code.

### 3.6. Feature 6: Quiz Management and Study

**Description:** The system allows authenticated users to create quizzes, study them, and record quiz attempts.

**Functional Requirements:**

- FR-QUIZ-01: The system shall allow an authenticated user to create a quiz.
- FR-QUIZ-02: The system shall allow an authenticated user to list their quizzes.
- FR-QUIZ-03: The system shall allow an authenticated user to retrieve a quiz by UUID.
- FR-QUIZ-04: The system shall allow an authenticated user to update a quiz.
- FR-QUIZ-05: The system shall allow an authenticated user to delete a quiz.
- FR-QUIZ-06: The system shall allow an authenticated user to record quiz attempts and scores.
- FR-QUIZ-07: The system shall associate quizzes with notebooks when a notebook UUID is provided.
- FR-QUIZ-08: The web and mobile clients shall allow users to study quizzes and submit attempts.

### 3.7. Feature 7: Flashcard Management and Study

**Description:** The system allows authenticated users to create flashcard decks, study them, and record flashcard attempts.

**Functional Requirements:**

- FR-FLASH-01: The system shall allow an authenticated user to create a flashcard deck.
- FR-FLASH-02: The system shall allow an authenticated user to list their flashcard decks.
- FR-FLASH-03: The system shall allow an authenticated user to retrieve a flashcard deck by UUID.
- FR-FLASH-04: The system shall allow an authenticated user to update a flashcard deck.
- FR-FLASH-05: The system shall allow an authenticated user to delete a flashcard deck.
- FR-FLASH-06: The system shall allow an authenticated user to record flashcard attempts and mastery.
- FR-FLASH-07: The system shall associate flashcard decks with notebooks when a notebook UUID is provided.
- FR-FLASH-08: The web and mobile clients shall allow users to study flashcard decks and submit mastery attempts.

### 3.8. Feature 8: Playlist Management

**Description:** The system allows authenticated users to organize notebooks into ordered study playlists.

**Functional Requirements:**

- FR-PLAYLIST-01: The system shall allow an authenticated user to create a playlist.
- FR-PLAYLIST-02: The system shall allow an authenticated user to list playlists.
- FR-PLAYLIST-03: The system shall allow an authenticated user to retrieve a playlist by UUID.
- FR-PLAYLIST-04: The system shall allow an authenticated user to update playlist metadata.
- FR-PLAYLIST-05: The system shall allow an authenticated user to delete a playlist.
- FR-PLAYLIST-06: The system shall allow an authenticated user to add notebooks to a playlist.
- FR-PLAYLIST-07: The system shall allow an authenticated user to remove notebooks from a playlist.
- FR-PLAYLIST-08: The system shall allow an authenticated user to reorder playlist notebooks.
- FR-PLAYLIST-09: The system shall allow an authenticated user to update the playlist current index.

### 3.9. Feature 9: Playback Queue and Audio Study

**Description:** The system allows authenticated users to build and control a notebook playback queue.

**Functional Requirements:**

- FR-QUEUE-01: The system shall allow an authenticated user to retrieve the current playback queue.
- FR-QUEUE-02: The system shall allow an authenticated user to add notebooks to the current playback queue.
- FR-QUEUE-03: The system shall allow an authenticated user to select a playlist as the current queue source.
- FR-QUEUE-04: The system shall allow an authenticated user to remove notebooks from the current playback queue.
- FR-QUEUE-05: The system shall allow an authenticated user to clear the current playback queue.
- FR-QUEUE-06: The system shall allow an authenticated user to update the queue current index.
- FR-QUEUE-07: The system shall allow an authenticated user to reorder the queue.
- FR-QUEUE-08: The mobile application shall provide playback controls for play, pause, skip, loop, shuffle, speech rate, playback position resume, and notebook review-state updates where supported.
- FR-QUEUE-09: The web application shall provide a player bar and queue panel for current queue visibility and playback control.
- FR-QUEUE-10: The system shall keep playlist and playback queue endpoints consistent through supported alias paths.

### 3.10. Feature 10: Mobile Embedded Editor

**Description:** The system supports mobile notebook editing by embedding the web notebook editor inside the Android application. Offline mobile study mode is not included in the current system scope.

**Functional Requirements:**

- FR-MOBILE-01: The Android application shall use Retrofit and token refresh support for authenticated backend calls.
- FR-MOBILE-02: The Android application shall host the web notebook editor through a WebView and wait for editor readiness before interacting with the editor.
- FR-MOBILE-03: The Android application shall avoid duplicating the deleted native notebook editor/review/AI stack while the embedded editor is the active implementation.
- FR-MOBILE-04: The Android application shall require a backend connection for authenticated study data and shall not claim offline study support.

### 3.11. Feature 11: Admin Management

**Description:** The system allows admin users to inspect and manage users and system records.

**Functional Requirements:**

- FR-ADMIN-01: The system shall allow admin users to list, view, update, and delete users.
- FR-ADMIN-02: The system shall allow admin users to list, view, and delete notebooks.
- FR-ADMIN-03: The system shall allow admin users to list, view, and delete categories, quizzes, flashcards, playlists, AI configurations, AI conversations, and playback queues.
- FR-ADMIN-04: The system shall allow admin users to list and view notebook versions.
- FR-ADMIN-05: The system shall restrict admin operations to authorized admin users.
- FR-ADMIN-06: The system shall expose admin management as backend API endpoints; dedicated admin web and mobile screens are not included in the current client routes.

## 4. Non-Functional Requirements

- NFR-01: The system shall require authenticated access for protected API endpoints.
- NFR-02: The system shall enforce ownership so users can only access records they own, except for authorized admin users.
- NFR-03: The system shall store passwords using one-way encryption or hashing.
- NFR-04: The system shall keep refresh token sessions with expiry information.
- NFR-05: The system shall keep AI keys and email credentials outside client-visible source code.
- NFR-06: The web application shall run in modern browsers supported by the React/Vite toolchain.
- NFR-07: The mobile application shall support Android API level 24 and higher.
- NFR-08: The embedded notebook editor shall support browser use and Android WebView host integration.
- NFR-09: The system shall provide clear navigation for authentication, dashboard, library, notebook editor, quizzes, flashcards, playlists, playback, and profile workflows.
- NFR-10: The system shall provide reliable error messages for invalid input, unauthorized access, missing records, route load failures, and failed external service requests.
- NFR-11: The system shall support automated testing for major backend, web, and mobile features.
- NFR-12: The system shall keep external service credentials and environment-specific values configurable outside the client-visible application code.
- NFR-13: The system shall use version information to protect notebook saves from stale-client overwrites.
- NFR-14: The system shall use client mutation IDs where implemented to avoid duplicate effects for repeated notebook, quiz, or flashcard attempt requests.
- NFR-15: The Android application shall keep feature boundaries between app orchestration, feature slices, platform networking/session code, and shared UI/study components.
- NFR-16: The web application shall provide route-level fallback behavior for lazy-loaded pages and route errors.
- NFR-17: Mobile playback shall split notebook text into manageable text-to-speech chunks and preserve resume information where supported.

## 5. System Models (Diagrams)

The system diagrams are stored in `docs/diagrams/brainbox-system-models.drawio`.

### 5.1. ERD

The ERD shows the main persistence entities: User, RefreshToken, Code, Category, Notebook, NotebookVersion, NotebookMutationRecord, Quiz, QuizQuestion, QuizAttempt, Flashcard, FlashcardCard, FlashcardAttempt, Playlist, PlaybackQueue, AiConfig, and AiConversation.

### 5.2. Use Case Diagram

The use case diagram shows Guest User, Authenticated User, Admin User, Email Service, and AI Proxy actors. It covers account access, profile management, notebook management, notebook version history, AI assistance, quiz and flashcard study, playlist and queue management, mobile embedded editor support, and admin operations.

### 5.3. Activity Diagram

The activity diagram shows the main study workflow from authentication to dashboard access, notebook work, version save/restore decisions, AI assistance, study sessions, and playback.

### 5.4. Class Diagram

The class diagram summarizes the major backend modules and supporting web/mobile feature slices.

### 5.5. Sequence Diagram

The sequence diagram shows a representative authenticated notebook content save request, including token validation, controller handling, service persistence, stale-version conflict checks, database save, version snapshot event, and API response.

## 6. Appendices

Additional references:

- `docs/SDD_BrainBox.md`
- `docs/diagrams/brainbox-system-models.drawio`
- `docs/wireframes/brainbox-web-wireframe.html`
- `docs/wireframes/brainbox-mobile-wireframe.html`

Automated test references:

| Platform | Test Coverage Areas |
| --- | --- |
| Backend | AI config service, flashcard service, notebook deletion coordination, quiz service, admin user controller, user-facing controller smoke tests. |
| Web | Optimistic updates, outline tree behavior, pagination, playback model, queue playback behavior, route fallback behavior. |
| Mobile | Optimistic home updates, playback coordinator, global playbar, playback UI state, TTS timing, playback formatters, mobile architecture boundaries. |
