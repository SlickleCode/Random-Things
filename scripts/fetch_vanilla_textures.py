#!/usr/bin/env python3
"""
Scans every structured recipe in data/features/*.json for ingredient/result
keys we don't have a texture for yet, and downloads an icon for each from
the Minecraft Wiki (minecraft.wiki) into textures/vanilla/.

A key is considered "already covered" if it resolves via the normal
generator lookup (textures/item, textures/block, textures/models/armor, or
textures/vanilla) or if it matches another feature's own id (recipes can
reference the mod's own items/blocks, which already have textures).

Usage:
    cd scripts && python3 fetch_vanilla_textures.py [--dry-run] [--force]

Requires network access to minecraft.wiki, which this sandboxed dev
environment does not have - run it somewhere that does (a contributor's
machine, or CI with broader egress), then commit the results.

If a key's guessed wiki file title (Title Cased, underscores for spaces)
doesn't match the wiki's actual naming, add an explicit mapping to
data/vanilla_texture_names.json, e.g. {"redstone": "Redstone Dust"}.
"""

from __future__ import annotations

import argparse
import os
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

import constants
from texture_utils import (
    has_local_texture,
    humanize,
    iter_recipe_keys,
    load_feature_ids,
    load_vanilla_name_overrides,
)

REQUEST_DELAY_SECONDS = 0.5


def wiki_url_for(title: str) -> str:
    filename = title.replace(" ", "_") + ".png"
    return constants.MINECRAFT_WIKI_FILEPATH_URL.format(urllib.parse.quote(filename))


def download(url: str, dest_path: str) -> None:
    request = urllib.request.Request(url, headers={"User-Agent": constants.USER_AGENT})
    with urllib.request.urlopen(request, timeout=15) as response:
        os.makedirs(os.path.dirname(dest_path), exist_ok=True)
        with open(dest_path, "wb") as dest_file:
            dest_file.write(response.read())


def find_missing_keys() -> list[str]:
    feature_ids = load_feature_ids()
    seen, missing = set(), []
    for key in iter_recipe_keys():
        if key in seen:
            continue
        seen.add(key)
        if key in feature_ids or has_local_texture(key):
            continue
        missing.append(key)
    return sorted(missing)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--dry-run", action="store_true", help="List missing keys without downloading")
    parser.add_argument("--force", action="store_true", help="Re-download even if a local file already exists")
    args = parser.parse_args()

    overrides = load_vanilla_name_overrides()
    missing_keys = find_missing_keys()

    if not missing_keys:
        print("No missing vanilla textures - every recipe key already resolves.")
        return

    print(f"{len(missing_keys)} recipe key(s) have no local texture: {missing_keys}")
    if args.dry_run:
        return

    vanilla_dir = os.path.join(constants.TEXTURES_DIR, constants.VANILLA_TEXTURE_DIR)
    downloaded, failed = [], []

    for i, key in enumerate(missing_keys):
        dest_path = os.path.join(vanilla_dir, f"{key}.png")
        if os.path.isfile(dest_path) and not args.force:
            continue

        title = overrides.get(key, humanize(key))
        url = wiki_url_for(title)

        if i > 0:
            time.sleep(REQUEST_DELAY_SECONDS)

        try:
            download(url, dest_path)
            downloaded.append(key)
            print(f"  downloaded {key} <- {title!r} ({url})")
        except (urllib.error.URLError, urllib.error.HTTPError, TimeoutError) as error:
            failed.append(key)
            print(f"  FAILED {key} <- {title!r} ({url}): {error}")

    print(f"\nDownloaded {len(downloaded)} texture(s) into {vanilla_dir}/")
    if failed:
        print(
            f"Could not resolve {len(failed)} key(s): {failed}\n"
            "Add an entry to data/vanilla_texture_names.json with the exact "
            "Minecraft Wiki file title for each, or add a texture manually."
        )
        sys.exit(1)


if __name__ == "__main__":
    main()
