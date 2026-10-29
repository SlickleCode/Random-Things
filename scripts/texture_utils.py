"""
Shared texture-resolution helpers used by both generate_html.py (to render
icons) and fetch_vanilla_textures.py (to know which keys are still missing).
Keeping this in one place means both always agree on what counts as "we
already have this texture".
"""

from __future__ import annotations

import glob
import json
import os

import constants


def humanize(key: str) -> str:
    return key.replace("_", " ").replace("-", " ").title()


def find_texture(key: str) -> str | None:
    """Looks for <key>.png under each of TEXTURE_SEARCH_DIRS. Returns the
    path as it should appear in a generated page (docs/*.html), or None."""
    for subdir in constants.TEXTURE_SEARCH_DIRS:
        on_disk = os.path.join(constants.TEXTURES_DIR, subdir, key + ".png")
        if os.path.isfile(on_disk):
            return f"textures/{subdir}/{key}.png"
    return None


def has_local_texture(key: str) -> bool:
    return find_texture(key) is not None


def load_feature_ids() -> set[str]:
    return {
        os.path.splitext(os.path.basename(path))[0]
        for path in glob.glob(os.path.join(constants.FEATURES_DIR, "*.json"))
    }


def iter_recipe_keys():
    """Yields every texture key referenced by a structured recipe (grid
    slots and the result) across all features, one at a time (duplicates
    included - callers that want a unique set should wrap this in set())."""
    for path in sorted(glob.glob(os.path.join(constants.FEATURES_DIR, "*.json"))):
        with open(path, "r") as feature_file:
            feature = json.load(feature_file)

        recipe = feature.get("recipe")
        if not recipe:
            continue

        for row in recipe.get("shape", []):
            for slot in row:
                if slot is None:
                    continue
                key = slot if isinstance(slot, str) else slot.get("key")
                if key:
                    yield key

        result_key = recipe.get("result", {}).get("key")
        if result_key:
            yield result_key


def load_vanilla_name_overrides() -> dict:
    """Optional data/vanilla_texture_names.json mapping a recipe key to the
    exact Minecraft Wiki file title, for the keys where humanize(key)
    doesn't match the wiki's actual naming (e.g. "redstone" -> "Redstone
    Dust")."""
    if not os.path.isfile(constants.VANILLA_TEXTURE_OVERRIDES_FILE):
        return {}
    with open(constants.VANILLA_TEXTURE_OVERRIDES_FILE, "r") as overrides_file:
        return json.load(overrides_file)
