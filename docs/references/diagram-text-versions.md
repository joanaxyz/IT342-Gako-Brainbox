# BrainBox System Model Text Versions

This document provides text-only replacements for the BrainBox system diagrams. It is intended for use in the SRS or SDD when diagram images are unavailable or when a narrative version is preferred.

## Component Diagram Text Version

### Purpose

The component model describes BrainBox as a client-server study application made of three main application components: a React web client, an Android mobile application, and a Spring Boot backend API. The backend owns business rules, authentication, persistence, and external service integration. The web and mobile clients consume the backend through authenticated REST API calls.

### System Boundary

The BrainBox system boundary contains:

- React Web Client
- Android Mobile Application
- Spring Boot Backend API
- Backend feature modules
- PostgreSQL database
- Backend support services for security, email, encryption, errors, and API response formatting

External systems outside the BrainBox boundary are:

- Email provider, used for email verification and password reset messages
- Google identity provider, used for Google sign-in
- AI provider, called by the BrainBox backend for AI-assisted notebook and study workflows

### Components and Responsibilities

| Component | Responsibility |
| --- | --- |
| React Web Client | Provides browser-based authentication screens, dashboard, library, notebook editor, version history, AI sidebar, quiz and flashcard screens, playlist screens, playback queue panel, profile page, settings modal, and export tools. |
| Android Mobile Application | Provides mobile authentication, dashboard/home tabs, library access, quiz study, flashcard study, playlist screens, profile access, playback queue overlay, global playbar, text-to-speech playback, and session persistence. |
| Spring Boot Backend API | Exposes REST endpoints, validates JWT access tokens, applies business rules, enforces ownership, coordinates services, returns standard API responses, and handles errors. |
| Authentication Module | Manages registration, email verification, login, logout, refresh tokens, forgot password, reset password, and Google sign-in. |
| User/Profile Module | Manages authenticated profile retrieval, profile update, password changes, and selected AI configuration ownership. |
| Notebook Module | Manages notebooks, notebook content saves, review state, version snapshots, version restore, stale-version conflict checks, and notebook mutation idempotency. |
| Category Module | Manages user-owned notebook categories and category deletion behavior. |
| AI Module | Manages AI provider configuration, selected provider settings, AI query requests, prompt building, selected text/highlight inputs, and notebook conversations. |
| Quiz Module | Manages quiz creation, listing, update, deletion, questions, notebook association, and quiz attempt recording. |
| Flashcard Module | Manages flashcard deck creation, listing, update, deletion, cards, notebook association, and mastery attempt recording. |
| Playlist Module | Manages ordered notebook playlists, add/remove notebook behavior, reorder behavior, and current playlist index. |
| Playback Queue Module | Manages each user's current playback queue, selected playlist source, queue notebook order, item removal, clearing, reordering, and current index. |
| PostgreSQL Database | Persists users, refresh tokens, verification codes, notebooks, categories, notebook versions, mutation records, quizzes, flashcards, playlists, playback queues, AI configurations, and AI conversations. |
| Email Provider | Sends verification and password reset email messages requested by the backend. |
| Google Identity Provider | Validates Google sign-in information used by the authentication flow. |
| AI Provider | Produces AI responses for notebook editing, review support, quiz generation, flashcard generation, and other AI-assisted study flows. |

### Component Connections

The React Web Client sends HTTP requests to the Spring Boot Backend API. Public requests include registration, login, email verification, password recovery, token refresh, and Google sign-in. Protected requests include profile, notebooks, categories, AI, quizzes, flashcards, playlists, and playback queue operations. Protected requests include a bearer access token in the `Authorization` header.

The Android Mobile Application also sends HTTP requests to the Spring Boot Backend API. It stores session data locally, refreshes tokens when needed, and uses Retrofit-based services for feature data. Mobile playback behavior uses backend notebook, playlist, and queue data to drive text-to-speech and playbar state.

The Spring Boot Backend API uses the PostgreSQL database through Spring Data JPA repositories. Controllers receive HTTP requests, services apply business rules, repositories load and save entities, and DTOs shape request and response data.

The backend uses the Email Provider through the email service layer when a user registers, verifies an account, requests password recovery, or receives a reset code. Email templates are built inside the backend before sending.

The backend uses server-side provider calls through the AI service layer. Users save an API Base URL, encrypted AI provider API key, select a provider, and submit AI requests. The backend keeps provider keys server-side and returns only generated AI responses or saved conversation metadata to clients.

The backend uses the Google Identity Provider during Google sign-in. The client sends Google identity data to the backend, and the backend uses that data to create or locate a BrainBox user account before issuing BrainBox tokens.

### Data and Control Flow Summary

1. A guest user opens the web or mobile client.
2. The client calls public authentication endpoints to register, verify, log in, recover a password, or use Google sign-in.
3. After login, the backend returns an access token, refresh token, and user information.
4. The client stores the session and sends protected requests with the access token.
5. The authentication interceptor validates the token and places `userId` and `userRole` into the request context.
6. Feature controllers delegate to services.
7. Services enforce ownership, validate inputs, coordinate persistence, and call external services when required.
8. Repositories read or write PostgreSQL tables.
9. The backend wraps successful responses in `ApiResponse.success(...)` and error responses in `ApiResponse.error(...)`.
10. The client updates UI state from the response.

## ERD Text Version

### Purpose

The ERD describes the persistent data model used by BrainBox. The database is centered on the `users` table. Most study records are owned by one user, and ownership is enforced by backend service lookups and security checks.

### Main Entities

| Entity/Table | Description | Key Fields |
| --- | --- | --- |
| `users` | Stores BrainBox accounts and authentication profile data. | `id`, `username`, `email`, `password`, `authProvider`, `googleId`, `banned`, `verified`, `role`, `lastLogin`, `lastLogout`, `createdAt`, `selected_ai_config_id` |
| `RefreshToken` | Stores refresh token sessions for authenticated users. | `id`, `token`, `expiryDate`, `createdAt`, `user_id`, `userAgent`, `ipAddress` |
| `Code` | Stores verification or password reset code data for a user. | `id`, `code`, `createdAt`, `user_id`, `expiryDate` |
| `Category` | Stores user-owned notebook categories. | `id`, `name`, `createdAt`, `updatedAt`, `user_id` |
| `Notebook` | Stores notebook metadata, HTML content, ownership, category, review timestamp, and version number. | `id`, `uuid`, `title`, `content`, `createdAt`, `updatedAt`, `lastReviewedAt`, `version`, `category_id`, `user_id` |
| `NotebookVersion` | Stores timestamped notebook content snapshots. | `id`, `content`, `version`, `notebook_id` |
| `notebook_mutation_record` | Stores client mutation IDs and response JSON for idempotent notebook mutations. | `id`, `user_id`, `notebook_uuid`, `mutation_type`, `client_mutation_id`, `response_json`, `created_at` |
| `Quiz` | Stores quiz metadata and optional notebook association. | `id`, `uuid`, `title`, `description`, `difficulty`, `notebook_id`, `user_id`, `createdAt`, `updatedAt` |
| `QuizQuestion` | Stores quiz questions belonging to a quiz. | `id`, `type`, `text`, `options`, `correctIndex` |
| `quiz_question_options` | Stores the element collection of answer option text for quiz questions. | `question_id`, `option_text` |
| `QuizAttempt` | Stores submitted quiz scores. | `id`, `quiz_id`, `user_id`, `score`, `client_mutation_id`, `createdAt` |
| `Flashcard` | Stores flashcard deck metadata and optional notebook association. | `id`, `uuid`, `title`, `description`, `notebook_id`, `user_id`, `createdAt`, `updatedAt` |
| `FlashcardCard` | Stores individual flashcard cards inside a deck. | `id`, `front`, `back`, `flashcard_id` |
| `FlashcardAttempt` | Stores flashcard mastery attempts. | `id`, `flashcard_id`, `user_id`, `mastery`, `client_mutation_id`, `createdAt` |
| `Playlist` | Stores a user-owned ordered playlist of notebooks. | `id`, `uuid`, `title`, `currentIndex`, `createdAt`, `updatedAt`, `user_id` |
| `playlist_notebooks` | Join table that stores notebook membership for playlists. | `playlist_id`, `notebook_id`, `position` |
| `PlaybackQueue` | Stores a user's current playback queue and optional selected playlist. | `id`, `uuid`, `user_id`, `selected_playlist_id`, `currentIndex`, `updatedAt` |
| `playback_queue_notebooks` | Join table that stores notebook membership for the active playback queue. | `playback_queue_id`, `notebook_id`, `position` |
| `ai_config` | Stores user-owned AI provider configuration. The legacy `proxyUrl` column stores the provider API Base URL. | `id`, `user_id`, `name`, `model`, `proxyUrl`, `apiKey`, `createdAt`, `updatedAt` |
| `ai_conversation` | Stores AI conversation metadata and serialized messages for a notebook. | `id`, `uuid`, `user_id`, `notebookUuid`, `mode`, `title`, `messages`, `createdAt`, `updatedAt` |

### Primary Relationships

| Relationship | Cardinality | Description |
| --- | --- | --- |
| User to RefreshToken | One-to-many | One user can have many refresh token sessions. Each refresh token belongs to exactly one user. |
| User to Code | One-to-many in storage, functionally current-code oriented | Verification and reset code records reference one user. The service uses codes to verify email and reset passwords. |
| User to Category | One-to-many | One user can create many categories. Each category belongs to exactly one user. |
| User to Notebook | One-to-many | One user can own many notebooks. Each notebook belongs to exactly one user. |
| Category to Notebook | One-to-many, optional on notebook | One category can group many notebooks. A notebook can have no category. |
| Notebook to NotebookVersion | One-to-many | One notebook can have many version snapshots. Each snapshot belongs to one notebook. |
| User to NotebookMutationRecord | One-to-many by `user_id` | One user can have many mutation records. Records are used to prevent duplicate effects for repeated notebook mutations. |
| User to Quiz | One-to-many | One user can create many quizzes. Each quiz belongs to one user. |
| Notebook to Quiz | One-to-many, optional on quiz | One notebook can be associated with many quizzes. A quiz may exist without a notebook association. |
| Quiz to QuizQuestion | One-to-many | One quiz contains many questions. Questions are removed when the quiz is updated or deleted. |
| QuizQuestion to quiz_question_options | One-to-many element collection | One question can have multiple answer option strings. |
| Quiz to QuizAttempt | One-to-many | One quiz can have many submitted attempts. Each attempt belongs to one quiz. |
| User to QuizAttempt | One-to-many | One user can submit many quiz attempts. Each attempt belongs to one user. |
| User to Flashcard | One-to-many | One user can create many flashcard decks. Each deck belongs to one user. |
| Notebook to Flashcard | One-to-many, optional on flashcard | One notebook can be associated with many flashcard decks. A deck may exist without a notebook association. |
| Flashcard to FlashcardCard | One-to-many | One flashcard deck contains many cards. Cards are removed when the deck is updated or deleted. |
| Flashcard to FlashcardAttempt | One-to-many | One flashcard deck can have many mastery attempts. |
| User to FlashcardAttempt | One-to-many | One user can submit many flashcard attempts. |
| User to Playlist | One-to-many | One user can create many playlists. Each playlist belongs to one user. |
| Playlist to Notebook | Many-to-many through `playlist_notebooks` | One playlist can contain many notebooks, and one notebook can appear in many playlists. |
| User to PlaybackQueue | One-to-one by unique `user_id` | One user has one current playback queue record. |
| PlaybackQueue to Playlist | Many-to-one, optional | A playback queue may be sourced from a selected playlist. |
| PlaybackQueue to Notebook | Many-to-many through `playback_queue_notebooks` | One playback queue can contain many notebooks in an ordered list. |
| User to AiConfig | One-to-many | One user can save many AI configurations. Each configuration belongs to one user. |
| User to selected AiConfig | Many-to-one, optional | A user can select one AI configuration as the active provider configuration. |
| User to AiConversation | One-to-many | One user can save many AI conversations. |
| AiConversation to Notebook | Logical reference by `notebookUuid` | A conversation stores a notebook UUID string rather than a direct JPA relationship. |

### Important Constraints

- `users.username` is unique.
- `users.email` is unique.
- `RefreshToken.token` is unique and required.
- `Notebook.uuid`, `Quiz.uuid`, `Flashcard.uuid`, `Playlist.uuid`, `PlaybackQueue.uuid`, and `ai_conversation.uuid` are unique public identifiers.
- `PlaybackQueue.user_id` is unique, so a user has only one current queue.
- `QuizAttempt` has a uniqueness constraint on `user_id` and `client_mutation_id`.
- `FlashcardAttempt` has a uniqueness constraint on `user_id` and `client_mutation_id`.
- `notebook_mutation_record` stores `user_id`, `mutation_type`, and `client_mutation_id` so repeated notebook writes can replay or ignore a previously applied mutation.
- User-owned entities are loaded through user-scoped repository methods or service checks before updates and deletes.

## Use Case Diagram Text Version

### Purpose

The use case model describes how guest users, authenticated users, and external services interact with BrainBox. Guest users can only access account-entry flows. Authenticated users can access study, profile, notebook, AI, quiz, flashcard, playlist, and playback features.

### Actors

| Actor | Description |
| --- | --- |
| Guest User | A person who has not yet authenticated. This actor can register, verify email, log in, use Google sign-in, and recover a password. |
| Authenticated User | A logged-in BrainBox user with a valid session. This actor can manage profile data, notebooks, categories, AI settings, quizzes, flashcards, playlists, playback queue, and study activity. |
| Email Provider | External service used by the backend to send verification and password reset email messages. |
| Google Identity Provider | External service involved in Google sign-in. |
| AI Provider | External service used by the backend to generate AI-assisted study responses. |

### Guest User Use Cases

| Use Case | Text Description |
| --- | --- |
| Register Account | The guest user submits username, email, and password. The backend checks uniqueness, hashes the password, creates the user, creates a verification code, and sends a verification email. |
| Verify Email | The guest user opens a verification link or submits verification data. The backend validates the code or token and marks the account as verified. |
| Log In | The guest user submits credentials. The backend finds the user, validates the password, checks account state, creates an access token and refresh token, records login metadata, and returns session data. |
| Use Google Sign-In | The guest user authenticates through Google. The backend validates Google identity data, creates or finds a matching BrainBox user, and returns BrainBox session tokens. |
| Request Password Reset | The guest user submits an email address. The backend creates a reset code and asks the email provider to send the reset message. |
| Verify Reset Code | The guest user submits the email and reset code. The backend validates whether the reset code is correct and not expired. |
| Reset Password | The guest user submits a valid reset token/code and a new password. The backend hashes and stores the new password. |

### Authenticated User Use Cases

| Use Case | Text Description |
| --- | --- |
| Refresh Session | The user or client sends a refresh token. The backend validates the refresh token and issues updated login token data. |
| Log Out | The user sends a logout request containing the refresh token. The backend deletes the stored refresh token and updates logout metadata. |
| View Dashboard | The user opens the authenticated home area and sees recently edited notebooks, recently reviewed notebooks, study cards, playback state, and navigation. |
| Manage Profile | The user views and updates profile fields such as username and email. |
| Change Password | The user submits the current password and a new password. The backend validates the current password and stores the new hashed password. |
| Manage Settings | The user opens account settings, profile settings, password settings, and AI provider settings from the web interface. |
| Create Category | The user creates a named category for organizing notebooks. |
| View Categories | The user retrieves all categories they own. |
| Delete Category | The user deletes a category. Depending on the request, notebooks are either detached from the category or deleted with it. |
| Create Notebook | The user creates a notebook with a title, optional category, and optional content. The backend persists the notebook and records an initial snapshot. |
| View Notebook List | The user retrieves notebook overviews, recently edited notebooks, or recently reviewed notebooks. |
| Open Notebook | The user retrieves full notebook content by UUID. |
| Edit Notebook Metadata | The user changes notebook title or category. The backend validates ownership and saves the metadata. |
| Save Notebook Content | The user saves HTML notebook content. The backend checks version data, stores content, publishes a save event, and creates a snapshot. |
| Mark Notebook Reviewed | The user marks a notebook as reviewed. The backend updates `lastReviewedAt`. |
| Delete Notebook | The user deletes a notebook. The backend detaches related playlists, queue records, quizzes, or flashcards before deletion where needed. |
| View Version History | The user lists saved snapshots for a notebook. |
| Preview Version | The user opens one snapshot to inspect older content before restoring. |
| Create Manual Version Snapshot | The user requests a version snapshot explicitly. |
| Restore Version | The user selects an old version and asks the backend to copy its content back to the current notebook. |
| Configure AI Provider | The user creates, updates, selects, lists, or deletes AI provider configuration. The backend encrypts provider keys before storing them. |
| Submit AI Query | The user sends a prompt, selected notebook text, highlight selections, conversation history, and mode. The backend sends the request to the configured AI provider and returns an AI response. |
| Manage AI Conversations | The user lists, saves, updates, or deletes conversation records associated with notebooks. |
| Create Quiz | The user creates a quiz, optionally associated with a notebook, with questions and answers. |
| Edit Quiz | The user updates quiz metadata or questions. |
| Study Quiz | The user answers questions and submits a score attempt. |
| Delete Quiz | The user deletes an owned quiz. |
| Create Flashcard Deck | The user creates a deck, optionally associated with a notebook, with front/back cards. |
| Edit Flashcard Deck | The user updates deck metadata or cards. |
| Study Flashcards | The user studies cards and submits a mastery attempt. |
| Delete Flashcard Deck | The user deletes an owned deck. |
| Create Playlist | The user creates a named playlist. |
| Manage Playlist Items | The user adds notebooks, removes notebooks, reorders notebooks, and updates the current index. |
| Delete Playlist | The user deletes an owned playlist. |
| Manage Playback Queue | The user retrieves the current queue, adds notebooks, selects a playlist as queue source, removes notebooks, clears the queue, reorders items, and updates the current index. |
| Control Playback | The user plays, pauses, skips, changes speech rate, toggles loop/shuffle where supported, and resumes notebook audio playback on web or mobile. |
| Export Notebook | The user exports notebook content from the web editor to PDF/print, Word `.docx`, or text `.txt`. |

### External Service Use Cases

| External Actor | Use Case | Text Description |
| --- | --- | --- |
| Email Provider | Send Verification Email | The backend asks the email provider to deliver an account verification email. |
| Email Provider | Send Password Reset Email | The backend asks the email provider to deliver a password reset code or link. |
| Google Identity Provider | Validate Google Sign-In | The backend uses Google identity data to authenticate or create the BrainBox account. |
| AI Provider | Generate AI Response | The backend sends a prompt and context to the configured AI provider and receives generated study assistance. |

### Use Case Relationships

- Register Account includes checking username/email uniqueness, password hashing, user creation, verification code creation, and sending a verification email.
- Log In includes credential lookup, password verification, account-state checks, token generation, refresh token persistence, and login timestamp update.
- Request Password Reset includes user lookup, reset code generation, reset code persistence, and password reset email delivery.
- Save Notebook Content includes ownership validation, stale-version check, content persistence, event publication, snapshot creation, mutation record persistence, and response mapping.
- Restore Version includes notebook ownership validation, version lookup, snapshot content extraction, notebook content update, and full notebook response mapping.
- Submit AI Query requires an authenticated user and a selected AI configuration.
- Study Quiz extends quiz retrieval by adding attempt submission and score persistence.
- Study Flashcards extends deck retrieval by adding mastery submission and attempt persistence.
- Manage Playback Queue may include Select Playlist when the user wants the queue to mirror a playlist.

## Activity Diagram Text Version

### Purpose

The activity model describes the main authenticated BrainBox study workflow from app entry to account access, dashboard use, notebook work, AI assistance, study sessions, and playback.

### Main Activity Flow

1. The user opens the web or mobile BrainBox application.
2. The client checks whether a valid access token is available.
3. If no valid access token is available, the user is treated as a guest.
4. The guest user chooses one of the account-entry actions: register, log in, Google sign-in, or forgot password.
5. If the user registers, the system validates form input, checks username/email uniqueness, hashes the password, creates the user, creates a verification code, sends email verification, and waits for the user to verify the account.
6. If the user logs in, the system validates credentials, checks whether the user is verified and not banned, creates access and refresh tokens, stores the refresh token, updates last login, and returns session data.
7. If the user signs in with Google, the system validates the Google identity data, creates or loads the BrainBox account, creates BrainBox tokens, and returns session data.
8. If the user starts forgot password, the system finds the user by email, creates a reset code, sends the reset email, verifies the code, accepts a new password, hashes the password, and updates the account.
9. After successful authentication, the client stores session data and opens the authenticated dashboard or mobile home shell.
10. The user chooses a major study area: profile/settings, library/notebooks, AI-assisted editor work, quizzes, flashcards, playlists, or playback queue.

### Profile and Settings Activity

1. The user opens the Profile page or Settings modal.
2. The client requests the authenticated profile.
3. The backend validates the token and loads the user's profile.
4. The user may update profile fields.
5. The backend validates uniqueness where needed and saves the profile changes.
6. The user may change password.
7. The backend validates the current password, hashes the new password, and saves it.
8. The user may configure AI provider settings.
9. The backend encrypts the AI key and saves or selects the AI configuration.
10. The user may log out.
11. The backend deletes the refresh token, updates logout metadata, and the client clears local session state.

### Notebook Authoring and Recovery Activity

1. The user opens the dashboard or library.
2. The client retrieves notebook overviews and category data.
3. The user creates a notebook or opens an existing notebook.
4. The backend validates ownership and returns notebook content.
5. The user edits notebook content in the editor.
6. The user saves content.
7. The client sends content, `baseVersion`, and optional `clientMutationId`.
8. The backend checks whether the mutation ID was already applied.
9. If the mutation was already applied, the backend replays the stored response.
10. If the mutation is new, the backend compares `baseVersion` with the current notebook version.
11. If the base version is stale, the backend returns a `VERSION_CONFLICT` response with the latest notebook data.
12. If the base version is current, the backend stores the new content.
13. The backend publishes a notebook content saved event.
14. The snapshot listener creates a `NotebookVersion` record.
15. The backend records the mutation result when a client mutation ID is supplied.
16. The client updates the editor with the saved notebook response.
17. The user may open version history.
18. The backend returns saved snapshots for the notebook.
19. The user previews a selected snapshot.
20. The backend returns snapshot content.
21. The user may restore the selected version.
22. The backend copies the selected snapshot content into the notebook and returns the updated notebook.

### AI Assistance Activity

1. The user opens AI tools in the notebook editor or settings.
2. If no AI configuration is selected, the user creates or selects one.
3. The backend saves the AI configuration with encrypted API key storage.
4. The user selects text, selects highlight targets, or enters a prompt.
5. The client sends the prompt, notebook UUID, selected text, selection targets, mode, and optional conversation history.
6. The backend validates the authenticated user.
7. The backend loads the selected AI configuration.
8. The backend builds the provider request and sends it with a server-side provider call.
9. The provider returns generated content.
10. The backend returns the AI response to the client.
11. The user reviews the proposal.
12. If the user accepts useful edits, the editor applies the change and the user saves the notebook.
13. If the user saves the conversation, the backend persists the conversation under the user and notebook UUID.

### Quiz and Flashcard Study Activity

1. The user opens the quiz or flashcard section on web or mobile.
2. The client requests the user's quizzes or decks.
3. The backend validates the token and returns owned study materials.
4. The user creates, edits, opens, or deletes a quiz/deck.
5. For study, the user opens a quiz or deck.
6. The client displays questions or flashcard cards.
7. The user completes the activity.
8. The client submits a score or mastery attempt with an optional client mutation ID.
9. The backend checks ownership and records the attempt.
10. The backend returns updated study material data.
11. The client displays the result, score, or mastery state.

### Playlist and Playback Activity

1. The user opens playlists or the playback queue.
2. The client requests playlists, queue state, or notebook choices.
3. The user creates a playlist, adds notebooks, removes notebooks, reorders items, or updates the current playlist index.
4. The backend validates playlist and notebook ownership, persists the ordered list, and returns playlist data.
5. The user selects a playlist as the playback queue source or adds notebooks directly to the current queue.
6. The backend creates or updates the user's playback queue.
7. The client displays the player bar, queue panel, mobile playbar, or mobile queue overlay.
8. The user starts playback.
9. The client converts notebook content into playable text or audio behavior.
10. The user controls play, pause, skip, loop, shuffle, speech rate, and resume position where supported.
11. The client may mark a notebook as reviewed after playback activity.

### Main Decision Points

- If the access token is missing or invalid, the user must authenticate or refresh the session.
- If a refresh token is valid, the client can restore the session without requiring login.
- If a protected request uses an invalid token, the backend returns `401 Unauthorized`.
- If the user attempts to access another user's record, the backend returns `403 Forbidden`.
- If a requested record does not exist, the backend returns `404 Not Found`.
- If a notebook save uses a stale `baseVersion`, the backend returns `409 Conflict` with the latest notebook data.
- If an AI request has no selected configuration, the AI flow cannot continue until the user selects or saves a provider configuration.

## Class Diagram Text Version

### Purpose

The class model describes the main backend classes and their responsibilities. BrainBox uses a layered Spring Boot structure: controllers expose REST endpoints, services implement business rules, repositories persist entities, entities model database records, and DTOs define API request/response shapes.

### Backend Layering

| Layer | Main Classes | Responsibility |
| --- | --- | --- |
| Controller Layer | `AuthController`, `ProfileController`, `NotebookController`, `NotebookVersionController`, `CategoryController`, `AiController`, `AiConfigController`, `AiConversationController`, `QuizController`, `FlashcardController`, `PlaylistController`, `PlaybackQueueController` | Receives HTTP requests, reads path/body/request attributes, delegates to services, and wraps service results in `ApiResponse`. |
| Service Layer | `AuthService`, `UserService`, `ProfileService`, `NotebookService`, `NotebookVersionService`, `NotebookVersionSnapshotService`, `CategoryService`, `AiService`, `AiConfigService`, `AiConversationService`, `QuizService`, `FlashcardService`, `PlaylistService`, `PlaybackQueueService` | Applies business rules, validates ownership, coordinates repositories, publishes events, talks to external providers, maps entities to responses, and manages transactions. |
| Repository Layer | `UserRepository`, `RefreshTokenRepository`, `CodeRepository`, `NotebookRepository`, `NotebookVersionRepository`, `NotebookMutationRecordRepository`, `CategoryRepository`, `AiConfigRepository`, `AiConversationRepository`, `QuizRepository`, `QuizAttemptRepository`, `FlashcardRepository`, `FlashcardAttemptRepository`, `PlaylistRepository`, `PlaybackQueueRepository` | Provides Spring Data JPA persistence operations and query methods. |
| Entity Layer | `User`, `RefreshToken`, `Code`, `Category`, `Notebook`, `NotebookVersion`, `NotebookMutationRecord`, `Quiz`, `QuizQuestion`, `QuizAttempt`, `Flashcard`, `FlashcardCard`, `FlashcardAttempt`, `Playlist`, `PlaybackQueue`, `AiConfig`, `AiConversation` | Represents database records and relationships. |
| Platform/Shared Layer | `AuthInterceptor`, `JWTService`, `SecurityConfig`, `WebConfig`, `GlobalExceptionHandler`, `ApiResponse`, `ApiError`, `EmailService`, `EmailTemplateService`, `EmailSenderFactory`, `EncryptionUtil` | Handles authentication interception, token operations, response envelopes, global errors, email delivery, encryption, CORS, and security configuration. |

### Key Controller Classes

| Class | Main Operations |
| --- | --- |
| `AuthController` | `register`, `verifyEmail`, `forgotPassword`, `verifyCode`, `resetPassword`, `login`, `logout`, `refreshToken`, `googleAuth` |
| `ProfileController` | `getProfile`, `updateProfile`, `changePassword` |
| `NotebookController` | `createNotebook`, `getNotebookOverview`, `getRecentlyEdited`, `getRecentlyReviewed`, `getNotebook`, `updateNotebook`, `updateReview`, `saveContent`, `deleteNotebook` |
| `NotebookVersionController` | `getNotebookVersions`, `getNotebookVersion`, `createNotebookVersion`, `restoreNotebookVersion` |
| `CategoryController` | `getAllCategories`, `createCategory`, `getCategory`, `deleteCategory` |
| `AiController` | `queryAi` |
| `AiConfigController` | `getConfig`, `listConfigs`, `saveConfig`, `selectConfig`, `deleteConfig` |
| `AiConversationController` | `getConversations`, `saveConversation`, `updateConversation`, `deleteConversation` |
| `QuizController` | `createQuiz`, `getQuizzes`, `getQuiz`, `updateQuiz`, `deleteQuiz`, `recordAttempt` |
| `FlashcardController` | `createFlashcard`, `getFlashcards`, `getFlashcard`, `updateFlashcard`, `deleteFlashcard`, `recordAttempt` |
| `PlaylistController` | `createPlaylist`, `getPlaylists`, `getPlaylist`, `updatePlaylist`, `deletePlaylist`, `addNotebook`, `removeNotebook`, `reorderQueue`, `setCurrentIndex` |
| `PlaybackQueueController` | `getQueue`, `addNotebook`, `selectPlaylist`, `removeNotebook`, `clearQueue`, `setCurrentIndex`, `reorderQueue` |

### Key Service Classes

| Class | Responsibility |
| --- | --- |
| `AuthService` | Coordinates registration, email verification, forgot password, reset password, login, logout, Google login, and refresh token behavior. Uses `UserService`, `JWTService`, `CodeService`, `RefreshTokenService`, `EmailService`, and password encoding. |
| `UserService` | Creates, updates, finds, verifies, saves, and deletes users. Also manages selected AI configuration references. |
| `ProfileService` | Loads the authenticated profile, updates profile fields, and changes passwords. |
| `NotebookService` | Creates notebooks, lists notebooks, loads notebooks by UUID, updates metadata, saves content, checks stale versions, records mutation IDs, marks notebooks reviewed, deletes notebooks, maps responses, and publishes notebook save events. |
| `NotebookVersionService` | Lists notebook versions, loads one version, creates manual snapshots, and restores version content to a notebook. |
| `NotebookVersionSnapshotService` | Serializes notebook content snapshots, reads current or legacy snapshot formats, and avoids duplicate snapshots when configured through `createSnapshotIfChanged`. |
| `NotebookDeletionCoordinator` | Detaches related notebook references before notebook deletion. |
| `CategoryService` | Lists, creates, loads, and deletes categories; coordinates notebook category cleanup. |
| `AiConfigService` | Saves encrypted AI configuration, lists configurations, selects active config, deletes config, and decrypts API keys for server-side provider calls. |
| `AiService` | Builds and sends AI generation requests using the selected provider configuration. |
| `AiConversationService` | Lists, creates, updates, and deletes AI conversations by user and notebook UUID. |
| `QuizService` | Creates, lists, loads, updates, deletes quizzes, clears notebook references, and records score attempts. |
| `FlashcardService` | Creates, lists, loads, updates, deletes decks, clears notebook references, and records mastery attempts. |
| `PlaylistService` | Creates, lists, loads, updates, deletes playlists, manages notebook membership, reorders notebooks, updates current index, and removes notebook references. |
| `PlaybackQueueService` | Gets or creates the user's queue, selects playlist source, adds notebooks, removes notebooks, clears queue, reorders queue, updates current index, and maps queue responses. |

### Main Entity Classes

| Entity | Important Attributes | Relationships |
| --- | --- | --- |
| `User` | `id`, `username`, `email`, `password`, `authProvider`, `googleId`, `banned`, `verified`, `role`, `lastLogin`, `lastLogout`, `createdAt` | Selects one optional `AiConfig`; owns categories, notebooks, quizzes, flashcards, playlists, queue, AI configs, conversations, attempts, codes, and refresh tokens. |
| `RefreshToken` | `id`, `token`, `expiryDate`, `createdAt`, `userAgent`, `ipAddress` | Many refresh tokens belong to one user. |
| `Code` | `id`, `code`, `createdAt`, `expiryDate` | Code belongs to one user. |
| `Category` | `id`, `name`, `createdAt`, `updatedAt` | Category belongs to one user and can be referenced by many notebooks. |
| `Notebook` | `id`, `uuid`, `title`, `content`, `createdAt`, `updatedAt`, `lastReviewedAt`, `version` | Notebook belongs to one user, optionally belongs to one category, has many versions, can be referenced by quizzes, flashcards, playlists, and playback queues. |
| `NotebookVersion` | `id`, `content`, `version` | Version belongs to one notebook. |
| `NotebookMutationRecord` | `id`, `userId`, `notebookUuid`, `mutationType`, `clientMutationId`, `responseJson`, `createdAt` | Tracks applied notebook mutations for a user. |
| `Quiz` | `id`, `uuid`, `title`, `description`, `difficulty`, timestamps | Quiz belongs to one user, optionally references a notebook, contains many questions, and has many attempts. |
| `QuizQuestion` | `id`, `type`, `text`, `options`, `correctIndex` | Question belongs to a quiz through the quiz's one-to-many join column. |
| `QuizAttempt` | `id`, `score`, `clientMutationId`, `createdAt` | Attempt belongs to one quiz and one user. |
| `Flashcard` | `id`, `uuid`, `title`, `description`, timestamps | Deck belongs to one user, optionally references a notebook, contains many cards, and has many attempts. |
| `FlashcardCard` | `id`, `front`, `back` | Card belongs to a flashcard deck through the deck's one-to-many join column. |
| `FlashcardAttempt` | `id`, `mastery`, `clientMutationId`, `createdAt` | Attempt belongs to one flashcard deck and one user. |
| `Playlist` | `id`, `uuid`, `title`, `currentIndex`, timestamps | Playlist belongs to one user and contains many notebooks through `playlist_notebooks`. |
| `PlaybackQueue` | `id`, `uuid`, `currentIndex`, `updatedAt` | Queue belongs to one user, optionally references a selected playlist, and contains many notebooks through `playback_queue_notebooks`. |
| `AiConfig` | `id`, `name`, `model`, `proxyUrl` legacy API Base URL storage, `apiKey`, timestamps | AI configuration belongs to one user. |
| `AiConversation` | `id`, `uuid`, `notebookUuid`, `mode`, `title`, `messages`, timestamps | Conversation belongs to one user and references notebook content by notebook UUID. |

### Important Class Interactions

- `AuthInterceptor` checks `@RequireAuth` and `@RequireRole` annotations before protected controller methods run.
- `AuthInterceptor` uses `JWTService` to validate the bearer token and extract `userId` and `userRole`.
- Controllers read `userId` from request attributes after token validation.
- Controllers delegate business logic to services rather than repositories.
- Services use repositories for persistence and use response DTOs to avoid exposing entity objects directly.
- `NotebookService` publishes `NotebookContentSavedEvent` after content changes.
- `NotebookVersionSnapshotListener` observes `NotebookContentSavedEvent` and calls `NotebookVersionSnapshotService`.
- `GlobalExceptionHandler` maps common exceptions to structured API errors, including `VERSION_CONFLICT` for stale notebook saves.
- `EncryptionUtil` supports encrypted storage for sensitive AI provider API keys.

## Sequence Diagram Text Version

### Purpose

The sequence model describes a representative authenticated notebook content save. It includes client submission, token validation, controller handling, stale-version protection, persistence, version snapshot creation, mutation idempotency, and response handling.

### Participants

| Participant | Role |
| --- | --- |
| Authenticated User | Edits and saves notebook content from the web editor or mobile-hosted editor surface. |
| Client Application | React web client or Android client that sends the save request. |
| AuthInterceptor | Backend interceptor that validates bearer tokens for protected endpoints. |
| NotebookController | REST controller that receives `PUT /api/notebooks/{uuid}/content`. |
| NotebookService | Service that validates ownership, checks mutation IDs, checks notebook version, saves content, publishes events, and maps response DTOs. |
| NotebookRepository | Repository used to load and save notebook records. |
| NotebookMutationRecordRepository | Repository used to detect and record idempotent notebook mutations. |
| ApplicationEventPublisher | Spring event publisher used to decouple notebook save logic from version snapshot creation. |
| NotebookVersionSnapshotListener | Event listener that reacts to saved notebook content. |
| NotebookVersionSnapshotService | Service that creates serialized notebook content snapshots. |
| NotebookVersionRepository | Repository that persists `NotebookVersion` records. |
| GlobalExceptionHandler | Error handler that converts stale-version conflicts and other exceptions into API error responses. |
| PostgreSQL Database | Persistent storage for notebooks, mutation records, and notebook versions. |

### Successful Save Sequence

1. The authenticated user edits notebook content in the client application.
2. The user selects save or the editor triggers a save action.
3. The client sends `PUT /api/notebooks/{uuid}/content` with `content`, `baseVersion`, and optional `clientMutationId`.
4. The request includes `Authorization: Bearer <accessToken>`.
5. `AuthInterceptor` checks whether the controller method requires authentication.
6. `AuthInterceptor` reads the bearer token.
7. `AuthInterceptor` calls `JWTService.validateToken(token)`.
8. If the token is valid, `AuthInterceptor` extracts the user ID and role.
9. `AuthInterceptor` stores `userId` and `userRole` as request attributes and allows the request to continue.
10. `NotebookController.saveContent(...)` receives the notebook UUID, request body, and authenticated `userId`.
11. `NotebookController` calls `NotebookService.saveContent(uuid, userId, content, baseVersion, clientMutationId)`.
12. `NotebookService` asks `NotebookMutationRecordRepository` whether the same `clientMutationId` was already applied for the user.
13. If no applied mutation is found, `NotebookService` loads the notebook through `NotebookRepository.findByUuidAndUserId(...)`.
14. If the notebook exists and belongs to the user, `NotebookService` compares the request `baseVersion` to the current notebook `version`.
15. If the version matches or `baseVersion` is not supplied, `NotebookService` compares the new content with the current stored content.
16. If the content changed, `NotebookService` updates the notebook `content`.
17. `NotebookService` saves and flushes the notebook through `NotebookRepository`.
18. The notebook persistence callbacks update timestamps and version data.
19. `NotebookService` publishes `NotebookContentSavedEvent` using `ApplicationEventPublisher`.
20. `NotebookVersionSnapshotListener` receives the event.
21. `NotebookVersionSnapshotListener` calls `NotebookVersionSnapshotService.createSnapshot(notebook, content)`.
22. `NotebookVersionSnapshotService` serializes the content into the current snapshot format.
23. `NotebookVersionSnapshotService` saves a `NotebookVersion` through `NotebookVersionRepository`.
24. `NotebookService` maps the saved notebook to `NotebookFullResponse`.
25. If `clientMutationId` was supplied, `NotebookService` serializes the response and records a `NotebookMutationRecord`.
26. `NotebookController` wraps the response in `ApiResponse.success(...)`.
27. The backend returns `200 OK` with the full notebook response.
28. The client updates local notebook state, including content, timestamps, category data, word count, and version.

### Duplicate Mutation Alternative

1. The client repeats a save request with the same `clientMutationId`.
2. `NotebookService` finds an existing mutation record for the user and client mutation ID.
3. If the stored mutation has response JSON, `NotebookService` deserializes the previous `NotebookFullResponse`.
4. The backend returns the replayed response without applying the content mutation again.
5. If the stored mutation has no response body, the service returns no response body for mutation types that do not produce one.

### Stale Version Alternative

1. The client sends a save request with a `baseVersion` older than the current notebook `version`.
2. `NotebookService` loads the notebook and compares the request version with the current version.
3. Because the versions do not match, `NotebookService` throws `NotebookVersionConflictException`.
4. `GlobalExceptionHandler` catches the exception.
5. `GlobalExceptionHandler` returns `409 Conflict`.
6. The response uses error code `VERSION_CONFLICT`.
7. The error details include the latest notebook response so the client can refresh or ask the user how to proceed.

### Unauthorized Alternative

1. The request has no bearer token, an invalid bearer token, or an expired bearer token.
2. `AuthInterceptor` rejects the request before the controller runs.
3. The backend returns `401 Unauthorized`.
4. The client may call the refresh-token endpoint if it has a valid refresh token.
5. If token refresh succeeds, the client retries or asks the user to save again.
6. If token refresh fails, the client returns the user to login.

### Forbidden or Missing Notebook Alternative

1. The token is valid, but the requested notebook UUID does not belong to the authenticated user.
2. `NotebookService` checks `findByUuidAndUserId`.
3. If the UUID exists but belongs to another user, the service throws a forbidden error.
4. `GlobalExceptionHandler` returns `403 Forbidden`.
5. If no notebook exists with the UUID, the service throws a not-found error.
6. `GlobalExceptionHandler` returns `404 Not Found`.

