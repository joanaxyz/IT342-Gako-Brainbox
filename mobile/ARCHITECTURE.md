# Mobile Architecture

The mobile app keeps one Android Gradle module and four root packages:
`app`, `features`, `platform`, and `shared`. The backend is only the
inspiration for intentional boundaries; mobile slices are organized around
mobile flows, UI state, playback behavior, and Android infrastructure.

## Package Roles

- `edu.cit.gako.brainbox.app`
  - App bootstrap, dependency graph creation, route selection, top-level state,
    and cross-feature orchestration.
- `edu.cit.gako.brainbox.app.playback`
  - App-level playback orchestration that maps notebook, playlist, auth,
    API, and playback state into feature-owned playback contracts.
- `edu.cit.gako.brainbox.app.study`
  - Cross-feature study-session orchestration, such as opening quiz/deck
    sessions, recording attempts through the API, and returning to notebooks.
- `edu.cit.gako.brainbox.features.*`
  - Product capabilities. Each feature owns its screens, local UI state,
    repositories, service contracts, DTOs, mappers, and feature-specific
    behavior.
- `edu.cit.gako.brainbox.features.home`
  - Authenticated home shell, home tab navigation, aggregate home read-model
    loading, and the tab surfaces that mirror the web `src/home` tree.
    Quizzes, flashcards, playlists, and profile live here fully, including
    their mobile screens, data contracts, DTOs, and tab-specific helpers.
- `edu.cit.gako.brainbox.platform`
  - Generic Android infrastructure only: Retrofit/session setup, network
    envelope support, preferences, and reusable Android services.
- `edu.cit.gako.brainbox.shared.ui`
  - Generic Compose building blocks, theme, formatting helpers, and reusable
    UI primitives.
- `edu.cit.gako.brainbox.shared.study`
  - Generic study display primitives. These accept UI models or primitives,
    never network DTOs.

## Feature Slices

- `features.auth`
  - Login, registration, password reset, session-facing auth data, and auth DTOs.
- `features.home`
  - Dashboard, library, quizzes, flashcards, playlists, and profile tab
    surfaces. The matching mobile data logic lives beside those surfaces:
    `features.home.quizzes.data`, `features.home.flashcards.data`,
    `features.home.playlists.data`, `features.home.profile.data`, and
    tab-local helpers such as `features.home.library.data`.
- `features.notebook`
  - Notebook data contracts, notebook DTOs, embedded web editor host, and
    notebook API access. The editor/review UI is the web surface shipped as
    embedded assets; old native editor/review/AI panes should not return.
- `features.playback`
  - Audio service/client/store live under `audio`, playback models under
    `model`, TTS text handling under `tts`, player/queue Compose UI under
    `ui`, and REST queue data under `data`.

## Dependency Rules

- `app` may depend on `features.*`, `platform.*`, and `shared.*`.
- `features.*` may depend on `platform.*` and `shared.*`.
- Feature UI must not import sibling feature repositories, API services, or
  data implementations directly. Cross-feature composition belongs in `app`.
- `platform` must not import `app` or `features`. It stays generic.
- `shared` must not import `app`, `features`, or `platform.network`.
- Network DTOs live in the owning feature under `data/dto` or another clearly
  owned feature DTO package. `platform.network` keeps only generic Retrofit,
  session, refresh-token, and envelope code.
- Feature-specific repositories, DTO mappers, and product workflows belong to
  the owning feature or to app-level orchestration.
- New REST endpoints should be added to the owning feature service, not to a
  central aggregate API interface.

## Guardrails

`MobileArchitectureBoundaryTest` source-scans the Android source tree to keep
the package boundaries honest:

- `platform` cannot import `app` or `features`.
- `shared` cannot import `app`, `features`, or `platform.network`.
- `platform.network.models` must not return as a DTO dumping ground.
- Feature UI cannot grab sibling feature repositories or API services directly.
- `app/home` must not return; authenticated home UI belongs under
  `features.home`.
- Dashboard, library, quizzes, flashcards, playlists, and profile home tabs
  must stay grouped under `features.home`, not scattered across top-level
  feature roots.
- `features/quiz`, `features/flashcard`, `features/playlist`, and
  `features/profile` must not return as duplicate top-level folders.
- Home tab UI receives home-owned adapters from app composition instead of
  reaching into `BrainBoxAppGraph` or sibling feature repositories directly.
- Notebook must not resurrect the deleted native editor/review/AI stack while
  mobile is hosted by the embedded web editor.
- Playback Kotlin files must stay in responsibility folders instead of piling
  up at the feature root.

These tests are intentionally simple. They are not a full architecture engine;
they catch the easy regressions that caused the mobile project to become chunky
and cross-wired in the first place.
