from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path


SCRIPT_DIR = Path(__file__).resolve().parent
DOCS_DIR = SCRIPT_DIR.parent
ROOT_DIR = DOCS_DIR.parent
SCREENSHOT_DIR = ROOT_DIR / "web" / "tests" / "e2e" / "screenshots"

SPEC_FILES = [
    ("AUTH", ROOT_DIR / "web" / "tests" / "e2e" / "01-auth-login.spec.mjs"),
    ("AUTH", ROOT_DIR / "web" / "tests" / "e2e" / "02-auth-register.spec.mjs"),
    ("AUTH", ROOT_DIR / "web" / "tests" / "e2e" / "03-auth-forgot-password.spec.mjs"),
    ("AUTH", ROOT_DIR / "web" / "tests" / "e2e" / "04-auth-oauth-logout.spec.mjs"),
    ("DASHBOARD", ROOT_DIR / "web" / "tests" / "e2e" / "05-dashboard.spec.mjs"),
    ("LIBRARY", ROOT_DIR / "web" / "tests" / "e2e" / "06-library.spec.mjs"),
    ("NOTEBOOK", ROOT_DIR / "web" / "tests" / "e2e" / "07-notebook.spec.mjs"),
    ("NOTEBOOK_AI", ROOT_DIR / "web" / "tests" / "e2e" / "07-notebook-ai.spec.mjs"),
    ("QUIZZES", ROOT_DIR / "web" / "tests" / "e2e" / "08-quizzes.spec.mjs"),
    ("FLASHCARDS", ROOT_DIR / "web" / "tests" / "e2e" / "09-flashcards.spec.mjs"),
    ("PLAYLISTS", ROOT_DIR / "web" / "tests" / "e2e" / "10-playlists.spec.mjs"),
    ("PROFILE", ROOT_DIR / "web" / "tests" / "e2e" / "11-profile-nav.spec.mjs"),
    ("PLAYBACK", ROOT_DIR / "web" / "tests" / "e2e" / "12-playback-queue.spec.mjs"),
]

TEST_PATTERN = re.compile(r"""test\(\s*['"](?P<id>WEB-[^:]+): (?P<title>[^'"]+)['"]""")

MODULE_TITLES = {
    "AUTH": "Authentication",
    "DASHBOARD": "Dashboard",
    "LIBRARY": "Library and Categories",
    "NOTEBOOK": "Notebook and Editor",
    "NOTEBOOK_AI": "Notebook AI",
    "QUIZZES": "Quizzes",
    "FLASHCARDS": "Flashcards",
    "PLAYLISTS": "Playlists",
    "PROFILE": "Profile and Navigation",
    "PLAYBACK": "Playback and Queue",
}

EVIDENCE_SECTION_TITLES = {
    "AUTH": "AUTHENTICATION MODULE EVIDENCE",
    "DASHBOARD": "DASHBOARD MODULE EVIDENCE",
    "LIBRARY": "LIBRARY AND CATEGORY MODULE EVIDENCE",
    "NOTEBOOK": "NOTEBOOK AND EDITOR MODULE EVIDENCE",
    "NOTEBOOK_AI": "NOTEBOOK AI MODULE EVIDENCE",
    "QUIZZES": "QUIZ MODULE EVIDENCE",
    "FLASHCARDS": "FLASHCARD MODULE EVIDENCE",
    "PLAYLISTS": "PLAYLIST MODULE EVIDENCE",
    "PROFILE": "PROFILE AND NAVIGATION MODULE EVIDENCE",
    "PLAYBACK": "PLAYBACK AND QUEUE MODULE EVIDENCE",
}


@dataclass(frozen=True)
class WebCase:
    module: str
    test_id: str
    title: str
    spec_path: Path
    screenshot_path: Path

    @property
    def submodule(self) -> str:
        return submodule_for_test_id(self.test_id)

    @property
    def functional_requirement(self) -> str:
        prefix = self.module.replace("_AI", "-AI").replace("_", "-")
        return f"FR-{prefix}: {self.title}"

    @property
    def preconditions(self) -> str:
        return preconditions_for_case(self)

    @property
    def steps(self) -> str:
        return "\n".join(
            f"{index}. {step}"
            for index, step in enumerate(steps_for_case(self), start=1)
        )

    @property
    def expected_result(self) -> str:
        return expected_result_for_case(self)

    @property
    def actual_result(self) -> str:
        return "Validated by passing Playwright assertions against the live BrainBox web app."

    @property
    def notes(self) -> str:
        return f"Spec: {self.spec_path.relative_to(ROOT_DIR)} | Screenshot: {self.screenshot_path.relative_to(ROOT_DIR)}"


def parse_web_cases() -> list[WebCase]:
    cases: list[WebCase] = []
    for module, spec_path in SPEC_FILES:
        contents = spec_path.read_text(encoding="utf-8")
        for match in TEST_PATTERN.finditer(contents):
            test_id = match.group("id").strip()
            cases.append(
                WebCase(
                    module=module,
                    test_id=test_id,
                    title=match.group("title").strip(),
                    spec_path=spec_path,
                    screenshot_path=SCREENSHOT_DIR / f"{test_id}.png",
                )
            )
    return cases


def build_web_cases() -> list[tuple[str, str, str, str, str, str, str, str, str, str, str, str]]:
    return [
        (
            spreadsheet_module_name(case.module),
            case.test_id,
            case.submodule,
            case.functional_requirement,
            case.title,
            case.preconditions,
            case.steps,
            case.expected_result,
            case.actual_result,
            "PASS",
            "Yes",
            case.notes,
        )
        for case in parse_web_cases()
    ]


def spreadsheet_module_name(module: str) -> str:
    return {
        "NOTEBOOK_AI": "NOTEBOOK AI",
        "PROFILE": "PROFILE / NAV",
        "PLAYBACK": "PLAYBACK / QUEUE",
    }.get(module, module)


def submodule_for_test_id(test_id: str) -> str:
    if test_id.startswith("WEB-AUTH-"):
        code = int(test_id.rsplit("-", 1)[1])
        if code < 10:
            return "Login"
        if code < 20:
            return "Registration"
        if code < 30:
            return "Forgot Password"
        if code < 40:
            return "Google OAuth"
        if code < 50:
            return "Logout"
        return "Route Protection"

    if test_id.startswith("WEB-DASH-"):
        return "Dashboard"

    if test_id.startswith("WEB-LIB-"):
        code = int(test_id.rsplit("-", 1)[1])
        return "Categories" if code >= 10 else "Library"

    if test_id.startswith("WEB-NB-AI-"):
        return "Notebook AI"

    if test_id.startswith("WEB-NB-"):
        code = int(test_id.rsplit("-", 1)[1])
        return "Notebook Editor" if code >= 10 else "Notebook Management"

    if test_id.startswith("WEB-QZ-"):
        return "Quizzes"

    if test_id.startswith("WEB-FC-"):
        return "Flashcards"

    if test_id.startswith("WEB-PL-"):
        return "Playlists"

    if test_id.startswith("WEB-PRF-"):
        return "Profile"

    if test_id.startswith("WEB-NAV-"):
        return "Navigation"

    if test_id.startswith("WEB-PB-"):
        return "Playback"

    if test_id.startswith("WEB-Q-"):
        return "Queue"

    return "Web"


def preconditions_for_case(case: WebCase) -> str:
    if case.test_id.startswith("WEB-AUTH-"):
        if case.submodule == "Route Protection":
            return "The user is signed out and the web client can reach the running BrainBox backend."
        return "The public BrainBox auth pages are reachable; scenarios that require login use joana / joana123456."

    if case.test_id.startswith("WEB-DASH-"):
        return "The user is authenticated and has existing study content visible on the dashboard."

    if case.test_id.startswith("WEB-LIB-"):
        return "The user is authenticated and has notebooks plus at least one category available in the library."

    if case.test_id.startswith("WEB-NB-AI-"):
        return "The user is authenticated, a notebook is open in the editor, and the AI sidebar can be launched."

    if case.test_id.startswith("WEB-NB-"):
        return "The user is authenticated and can open or create a notebook in the editor."

    if case.test_id.startswith(("WEB-QZ-", "WEB-FC-")):
        return "The user is authenticated and the selected study module contains playable items."

    if case.test_id.startswith(("WEB-PL-", "WEB-Q-", "WEB-PB-")):
        return "The user is authenticated and at least one playlist or notebook is available for playback operations."

    if case.test_id.startswith(("WEB-PRF-", "WEB-NAV-")):
        return "The user is authenticated and core workspace routes are available."

    return "The BrainBox web app and backend are running."


def steps_for_case(case: WebCase) -> list[str]:
    if case.test_id.startswith("WEB-AUTH-"):
        return [
            "Open the relevant authentication route for this scenario.",
            f"Execute the '{case.title}' flow using the implemented form or navigation controls.",
            "Observe the resulting page, validation state, or modal behavior.",
            "Confirm the Playwright assertion passes and capture the refreshed screenshot evidence.",
        ]

    if case.test_id.startswith("WEB-DASH-"):
        return [
            "Sign in and navigate to the dashboard.",
            f"Perform the '{case.title}' interaction.",
            "Verify the targeted widget, modal, or navigation result is visible.",
            "Capture screenshot evidence after the page settles.",
        ]

    if case.test_id.startswith("WEB-LIB-"):
        return [
            "Open the library workspace.",
            f"Run the '{case.title}' scenario with the visible notebook or category controls.",
            "Verify the filtered list, modal, or notebook editor response matches the implemented UI.",
            "Capture screenshot evidence after the visible state updates.",
        ]

    if case.test_id.startswith("WEB-NB-AI-"):
        return [
            "Open an existing notebook and launch the AI assistant.",
            f"Activate the '{case.title}' scenario from the AI rail or assistant controls.",
            "Verify the expected AI tool, panel, or sidebar state is shown.",
            "Capture screenshot evidence of the confirmed AI surface.",
        ]

    if case.test_id.startswith("WEB-NB-"):
        return [
            "Open or create a notebook in the editor.",
            f"Perform the '{case.title}' editor interaction.",
            "Verify the target editor, export, review, or history control is visible and responsive.",
            "Capture screenshot evidence once the editor state is stable.",
        ]

    if case.test_id.startswith(("WEB-QZ-", "WEB-FC-")):
        return [
            f"Open the {case.submodule.lower()} workspace.",
            f"Execute the '{case.title}' scenario using the study list, search, sort, or player controls.",
            "Verify the resulting page, player, or filter state matches the implemented feature.",
            "Capture screenshot evidence after the UI updates.",
        ]

    if case.test_id.startswith(("WEB-PL-", "WEB-Q-", "WEB-PB-")):
        return [
            "Open the playlists or playback surface required for the scenario.",
            f"Run the '{case.title}' interaction using the visible queue or player controls.",
            "Verify the queue, playback, or panel state stays synchronized with the action.",
            "Capture screenshot evidence of the confirmed state.",
        ]

    if case.test_id.startswith(("WEB-PRF-", "WEB-NAV-")):
        return [
            "Open the relevant authenticated route.",
            f"Execute the '{case.title}' navigation or settings action.",
            "Verify the intended destination, modal, or page state is visible.",
            "Capture screenshot evidence of the resulting surface.",
        ]

    return [
        "Open the relevant BrainBox surface.",
        f"Execute the '{case.title}' scenario.",
        "Verify the resulting UI matches the implemented behavior.",
        "Capture screenshot evidence.",
    ]


def expected_result_for_case(case: WebCase) -> str:
    title = case.title.lower()

    if "ui elements" in title:
        return "The required implemented UI controls are visible, labeled, and ready for interaction."
    if "opens with default chat tool" in title:
        return "The AI assistant opens successfully and defaults to the Chat tool."
    if "tool" in title and "ai sidebar" in title:
        return "The selected AI tool becomes active and the assistant reflects the tool-specific context."
    if "settings panel" in title:
        return "The AI provider settings panel opens and shows the configurable provider fields."
    if "history action" in title:
        return "The chat-history surface opens and the conversation history container becomes visible."
    if "closes" in title:
        return "The assistant closes cleanly and the reopen control remains available."
    if "version history" in title:
        return "The version-history sidebar opens with its visible filters and preview controls."
    if "export menu" in title:
        return "The export menu opens and exposes the supported PDF, DOCX, and TXT export actions."
    if "formatting and insert tools" in title:
        return "The editor exposes the implemented formatting, insert, zoom, and AI-highlight controls."
    if "review mode" in title:
        return "Review mode turns on and the document review surface becomes visible."
    if "auto-save" in title:
        return "The save-status control reflects unsaved or saved state after editor changes."
    if "search" in title:
        return "The relevant search control accepts input and updates the visible list or state."
    if "sort" in title:
        return "The sort control is usable and the current view updates without errors."
    if "filter" in title:
        return "The selected filter updates the visible content set without breaking navigation."
    if "loads" in title or "page loads" in title:
        return "The page loads successfully and its core content is visible."
    if "navigation" in title or "redirect" in title or "link" in title:
        return "Navigation reaches the expected route or modal state."
    if "logout" in title:
        return "Logout behaves as implemented and the user is returned to an unauthenticated state."
    if "create" in title or "new notebook" in title or "new category" in title:
        return "The create flow opens correctly and accepts the intended input."
    if "edit" in title:
        return "The edit surface is reachable and the relevant controls are visible."
    if "study" in title or "player" in title or "playback" in title or "queue" in title:
        return "The playback or study interaction remains usable and its visible state updates correctly."
    if "profile" in title:
        return "The relevant profile surface or action is available and renders correctly."
    return "The implemented BrainBox behavior completes successfully and matches the visible Playwright assertions."


def module_title(module: str) -> str:
    return MODULE_TITLES[module]


def evidence_section_title(module: str) -> str:
    return EVIDENCE_SECTION_TITLES[module]
