#!/usr/bin/env python3
"""Validate bundled Uthmani Quran assets before building the Android app."""
import json
from pathlib import Path

ASSETS = Path("app/src/main/assets")
PARTS = sorted(ASSETS.glob("quran_uthmani_part_*.json"))
EXPECTED_SURAHS = 114
EXPECTED_AYAHS = 6236

if len(PARTS) != 15:
    raise SystemExit(f"Expected 15 Quran asset parts, found {len(PARTS)}")

surahs = {}
for path in PARTS:
    with path.open(encoding="utf-8") as stream:
        payload = json.load(stream)
    for surah in payload.get("surahs", []):
        sid = int(surah["id"])
        if sid in surahs:
            raise SystemExit(f"Duplicate surah id: {sid}")
        verses = surah.get("verses", [])
        if int(surah.get("total_verses", -1)) != len(verses):
            raise SystemExit(f"Verse count mismatch in surah {sid}")
        if [int(ayah["id"]) for ayah in verses] != list(range(1, len(verses) + 1)):
            raise SystemExit(f"Non-sequential ayah numbering in surah {sid}")
        if any(not str(ayah.get("text", "")).strip() for ayah in verses):
            raise SystemExit(f"Empty ayah text in surah {sid}")
        surahs[sid] = surah

if sorted(surahs) != list(range(1, EXPECTED_SURAHS + 1)):
    raise SystemExit("Quran data must contain each surah from 1 to 114 exactly once")

ayah_count = sum(len(surah["verses"]) for surah in surahs.values())
if ayah_count != EXPECTED_AYAHS:
    raise SystemExit(f"Expected {EXPECTED_AYAHS} ayahs, found {ayah_count}")

print(f"Quran asset validation passed: {len(surahs)} surahs, {ayah_count} ayahs.")
