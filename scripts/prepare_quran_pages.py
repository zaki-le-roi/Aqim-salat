#!/usr/bin/env python3
"""Prepare the 604 offline Quran page images used by the Android app."""
import concurrent.futures
import io
import time
import urllib.request
from pathlib import Path
from PIL import Image

OUTPUT = Path("app/src/main/assets/quran_pages")
BASE_URL = "https://raw.githubusercontent.com/akram-seid/quran-hd-images/main/images"


def prepare(page: int) -> tuple[int, int]:
    name = f"{page:03d}"
    target = OUTPUT / f"{name}.webp"
    if target.exists() and target.stat().st_size > 1000:
        return page, target.stat().st_size

    request = urllib.request.Request(
        f"{BASE_URL}/{name}.jpg",
        headers={"User-Agent": "Aqim-Salat-Offline-Quran/1.0"},
    )
    last_error = None
    for attempt in range(4):
        try:
            with urllib.request.urlopen(request, timeout=45) as response:
                data = response.read()
            with Image.open(io.BytesIO(data)) as image:
                image = image.convert("RGB")
                if image.width > 1080:
                    height = round(image.height * 1080 / image.width)
                    image = image.resize((1080, height), Image.Resampling.LANCZOS)
                image.save(target, "WEBP", quality=82, method=6)
            if target.stat().st_size < 1000:
                raise RuntimeError("Converted page is unexpectedly small")
            return page, target.stat().st_size
        except Exception as exc:
            last_error = exc
            time.sleep(attempt + 1)
    raise RuntimeError(f"Page {name} failed: {last_error}")


def main() -> None:
    OUTPUT.mkdir(parents=True, exist_ok=True)
    total = 0
    with concurrent.futures.ThreadPoolExecutor(max_workers=10) as pool:
        for page, size in sorted(pool.map(prepare, range(1, 605))):
            total += size
            if page % 50 == 0 or page == 604:
                print(f"Prepared Quran pages: {page}/604")
    print(f"Offline Quran pages ready: 604 files, {total / (1024 * 1024):.1f} MiB")


if __name__ == "__main__":
    main()
