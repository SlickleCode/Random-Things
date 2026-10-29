#!/usr/bin/env python3
"""
Downloads every lumien.net-hosted image/video referenced in
data/features/*.json (the "images", "videos", and "recipe_images" fields)
into docs/images/ or docs/videos/, and rewrites each feature's JSON to
point at the local copy instead of the remote URL.

Usage:
    cd scripts && python3 fetch_lumien_assets.py [--dry-run] [--force]

Requires network access to lumien.net, which this sandboxed dev environment
does not have - run it somewhere that does, then commit the downloaded
files together with the updated data/features/*.json.
"""

from __future__ import annotations

import argparse
import glob
import json
import os
import time
import urllib.error
import urllib.parse
import urllib.request

import constants

REQUEST_DELAY_SECONDS = 0.3
ASSET_FIELDS = ("images", "videos", "recipe_images")
VIDEO_EXTENSIONS = {".mp4", ".webm", ".mov"}


def is_lumien_url(value: str) -> bool:
    parsed = urllib.parse.urlparse(value)
    return parsed.scheme in ("http", "https") and parsed.netloc == constants.LUMIEN_HOST


def local_dest_for(url: str, field: str) -> tuple[str, str]:
    """Returns (filesystem path to save to, path to use in the JSON).

    lumien.net keeps screenshots and crafting pictures in separate folders
    that sometimes share a filename (e.g. both have an
    advanced-redstone-interface.png) - flattening them into one local
    images/ folder would silently overwrite one with the other, so crafting
    pictures get a "crafting-" prefix to keep them distinct."""
    filename = os.path.basename(urllib.parse.urlparse(url).path)
    ext = os.path.splitext(filename)[1].lower()
    if ext in VIDEO_EXTENSIONS:
        return os.path.join(constants.OUTPUT_VIDEOS_DIR, filename), f"videos/{filename}"
    if field == "recipe_images":
        filename = f"crafting-{filename}"
    return os.path.join(constants.OUTPUT_IMAGES_DIR, filename), f"images/{filename}"


def download(url: str, dest_path: str) -> None:
    request = urllib.request.Request(url, headers={"User-Agent": constants.USER_AGENT})
    with urllib.request.urlopen(request, timeout=15) as response:
        os.makedirs(os.path.dirname(dest_path), exist_ok=True)
        with open(dest_path, "wb") as dest_file:
            dest_file.write(response.read())


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--dry-run", action="store_true", help="List what would be fetched without downloading")
    parser.add_argument("--force", action="store_true", help="Re-download even if a local file already exists")
    args = parser.parse_args()

    feature_paths = sorted(glob.glob(os.path.join(constants.FEATURES_DIR, "*.json")))

    pending = []  # (feature_path, field, index, url, dest_path, local_ref)
    for feature_path in feature_paths:
        with open(feature_path, "r") as feature_file:
            feature = json.load(feature_file)
        for field in ASSET_FIELDS:
            for index, value in enumerate(feature.get(field, [])):
                if isinstance(value, str) and is_lumien_url(value):
                    dest_path, local_ref = local_dest_for(value, field)
                    pending.append((feature_path, field, index, value, dest_path, local_ref))

    if not pending:
        print("No lumien.net-hosted assets left to fetch.")
        return

    print(f"{len(pending)} asset(s) reference lumien.net.")
    if args.dry_run:
        for _, field, _, url, dest_path, _ in pending:
            print(f"  [{field}] {url} -> {dest_path}")
        return

    changes_by_file: dict[str, dict] = {}
    downloaded, skipped, failed = 0, 0, 0

    for i, (feature_path, field, index, url, dest_path, local_ref) in enumerate(pending):
        if os.path.isfile(dest_path) and not args.force:
            skipped += 1
        else:
            if i > 0:
                time.sleep(REQUEST_DELAY_SECONDS)
            try:
                download(url, dest_path)
                downloaded += 1
                print(f"  downloaded {url} -> {dest_path}")
            except (urllib.error.URLError, urllib.error.HTTPError, TimeoutError) as error:
                failed += 1
                print(f"  FAILED {url}: {error}")
                continue

        changes_by_file.setdefault(feature_path, {})[(field, index)] = local_ref

    for feature_path, changes in changes_by_file.items():
        with open(feature_path, "r") as feature_file:
            feature = json.load(feature_file)
        for (field, index), local_ref in changes.items():
            feature[field][index] = local_ref
        with open(feature_path, "w") as feature_file:
            json.dump(feature, feature_file, indent=2)
            feature_file.write("\n")

    print(f"\nDownloaded {downloaded}, already local {skipped}, failed {failed}.")
    if failed:
        print("Failed URLs are left pointing at lumien.net - rerun this script to retry them.")


if __name__ == "__main__":
    main()
