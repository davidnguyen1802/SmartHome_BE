#!/usr/bin/env python3
"""PreToolUse hook: remind Claude to call load_skills_for_files() before any code edit.

Installed by agent-skills-standard (ags sync). Remove via: ags hooks uninstall

Why a hook (not just AGENTS.md or the MCP alone):
    Context compaction silently drops the check-AGENTS.md instruction.
    The MCP is on-demand - Claude must remember to call it.
    A hook fires mechanically on every edit - 100% trigger rate, cannot be forgotten.

Decision contract:
    - stdout: short MCP trigger prompt (injected as prompt context)
    - exit 0 always - never blocks the edit, only adds context
    - Silent on any error - hook bugs must never block legitimate work
"""
from __future__ import annotations

import json
import os
import sys
from pathlib import Path

REPO_ROOT = Path(os.environ.get("CLAUDE_PROJECT_DIR") or Path(__file__).resolve().parents[2])

EDIT_TOOLS = {"Edit", "Write", "MultiEdit", "NotebookEdit"}

_SKIP_DIRS = (
    str(Path(os.path.realpath(REPO_ROOT / ".claude"))),
    str(Path(os.path.realpath(REPO_ROOT / ".gemini"))),
)
_SKIP_FILES = (
    str(Path(os.path.realpath(REPO_ROOT / "AGENTS.md"))),
    str(Path(os.path.realpath(REPO_ROOT / "CLAUDE.md"))),
)


def _should_skip(file_path: str) -> bool:
    try:
        real = str(Path(os.path.realpath(file_path)))
    except Exception:
        return False
    if any(real.startswith(d) for d in _SKIP_DIRS):
        return True
    if real in _SKIP_FILES:
        return True
    return False


def main() -> None:
    try:
        data = json.load(sys.stdin)
    except Exception:
        sys.exit(0)

    if data.get("tool_name") not in EDIT_TOOLS:
        sys.exit(0)

    file_path = data.get("tool_input", {}).get("file_path", "")
    if not file_path or _should_skip(file_path):
        sys.exit(0)

    print(
        f"[SKILL TRIGGER] Editing: {Path(file_path).name}\n"
        f"-> Call load_skills_for_files(files=[ \"{file_path}\" ]) on the "
        f"agent-skills-standard MCP. It returns applicable SKILL.md rules, "
        f"or nothing if no skills match this file type."
    )

    sys.exit(0)


if __name__ == "__main__":
    main()
