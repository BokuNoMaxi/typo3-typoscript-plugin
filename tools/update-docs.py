#!/usr/bin/env python3
"""Refresh src/main/resources/data/typoscript.json from the official
TYPO3-Documentation/TYPO3CMS-Reference-Typoscript sources.

docs.typo3.org renders its TypoScript/PageTS/UserTS reference directly from
the `..  confval::` Sphinx directives in that repo, so parsing them here is
the same source of truth, without needing a Sphinx build.

This is a maintenance script, not part of the Gradle build: re-run it
whenever TYPO3 ships a new reference version, commit the refreshed JSON.

Usage: python3 tools/update-docs.py
"""
import json
import re
import subprocess
import sys
from pathlib import Path

TOOLS_DIR = Path(__file__).resolve().parent
SOURCES_DIR = TOOLS_DIR / ".sources"
OUTPUT_FILE = TOOLS_DIR.parent / "src/main/resources/data/typoscript.json"

REPO_URL = "https://github.com/TYPO3-Documentation/TYPO3CMS-Reference-Typoscript.git"
REPO_DIR = SOURCES_DIR / "ts-ref"
DOCS_ROOT = REPO_DIR / "Documentation"

# folder name -> category label
CATEGORIES = {
    "ContentObjects": "ContentObject",
    "Functions": "Function",
    "Conditions": "Condition",
    "TopLevelObjects": "TopLevelObject",
    "DataProcessing": "DataProcessing",
    "PageTsconfig": "PageTSConfig",
    "UserTsconfig": "UserTSConfig",
}

CONFVAL_RE = re.compile(r"^(\s*)\.\.\s+confval::\s+(.+?)\s*$")
FIELD_RE = re.compile(r"^(\s*):(\w+):\s*(.*)$")
REF_ROLE_RE = re.compile(r":ref:`([^`<]+?)(?:\s*<[^>`]+>)?`")
GENERIC_ROLE_RE = re.compile(r":(?:guilabel|file|php|code|py|option|term):`([^`]+)`")
CODE_ROLE_RE = re.compile(r"``([^`]+)``")


def clone_or_update_repo() -> None:
    SOURCES_DIR.mkdir(parents=True, exist_ok=True)
    if REPO_DIR.exists():
        print(f"Updating {REPO_DIR} ...")
        subprocess.run(["git", "-C", str(REPO_DIR), "pull", "--ff-only"], check=True)
    else:
        print(f"Cloning {REPO_URL} ...")
        subprocess.run(["git", "clone", "--depth", "1", REPO_URL, str(REPO_DIR)], check=True)


def clean_rst(text: str) -> str:
    text = REF_ROLE_RE.sub(r"\1", text)
    text = GENERIC_ROLE_RE.sub(r"\1", text)
    text = CODE_ROLE_RE.sub(r"`\1`", text)
    text = text.replace("..  code-block:: typoscript", "")
    return text.strip()


def indent_of(line: str) -> int:
    return len(line) - len(line.lstrip(" "))


def parse_confvals(text: str) -> list[dict]:
    lines = text.splitlines()
    entries = []
    i = 0
    n = len(lines)
    while i < n:
        m = CONFVAL_RE.match(lines[i])
        if not m:
            i += 1
            continue
        base_indent = len(m.group(1))
        name = m.group(2).strip()
        i += 1
        fields = {}
        while i < n:
            line = lines[i]
            if line.strip() == "":
                i += 1
                continue
            fm = FIELD_RE.match(line)
            if fm and indent_of(line) > base_indent:
                fields[fm.group(2)] = fm.group(3).strip()
                i += 1
                continue
            break
        desc_lines = []
        while i < n:
            line = lines[i]
            if line.strip() == "":
                if i + 1 < n and lines[i + 1].strip() != "" and indent_of(lines[i + 1]) <= base_indent:
                    break
                desc_lines.append("")
                i += 1
                continue
            if indent_of(line) <= base_indent:
                break
            desc_lines.append(line.strip())
            i += 1
        description = clean_rst("\n".join(desc_lines).strip())
        entries.append({
            "name": name,
            "type": clean_rst(fields.get("type", "")),
            "description": description,
        })
    return entries


def main():
    clone_or_update_repo()

    out = []
    for folder, category in CATEGORIES.items():
        root = DOCS_ROOT / folder
        if not root.exists():
            print(f"WARN: missing {root}", file=sys.stderr)
            continue
        for rst_file in sorted(root.rglob("*.rst")):
            rel = rst_file.relative_to(root)
            group_parts = list(rel.with_suffix("").parts)
            if group_parts and group_parts[-1].lower() == "index":
                group_parts = group_parts[:-1]
            group = ".".join(group_parts) if group_parts else rst_file.stem
            text = rst_file.read_text(encoding="utf-8", errors="replace")
            for entry in parse_confvals(text):
                out.append({
                    "category": category,
                    "group": group,
                    "name": entry["name"],
                    "type": entry["type"],
                    "description": entry["description"],
                })

    seen = set()
    deduped = []
    for e in out:
        key = (e["category"], e["group"], e["name"])
        if key in seen:
            continue
        seen.add(key)
        deduped.append(e)

    OUTPUT_FILE.write_text(json.dumps(deduped, indent=2, ensure_ascii=False), encoding="utf-8")
    print(f"Extracted {len(deduped)} TypoScript/PageTS/UserTS properties -> {OUTPUT_FILE}")


if __name__ == "__main__":
    main()
