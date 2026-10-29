#!/usr/bin/env python3
"""
Builds the static wiki pages under docs/ from the reusable feature data in
data/features/*.json, using the Jinja2 templates in template-html/.

Adding a new mod feature to the wiki means dropping a new JSON file into
data/features/ - no template or generator changes required. See
data/README.md for the schema.
"""

from __future__ import annotations

import glob
import json
import os

from jinja2 import Environment, FileSystemLoader, select_autoescape

import constants
from texture_utils import find_texture, humanize


def load_categories() -> list:
    with open(constants.CATEGORIES_FILE, "r") as categories_file:
        return json.load(categories_file)


def load_features() -> list:
    features = []
    for path in sorted(glob.glob(os.path.join(constants.FEATURES_DIR, "*.json"))):
        with open(path, "r") as feature_file:
            feature = json.load(feature_file)
        feature["id"] = os.path.splitext(os.path.basename(path))[0]
        feature.setdefault("images", [])
        feature.setdefault("videos", [])
        feature.setdefault("properties", [])
        feature.setdefault("keywords", [])
        feature.setdefault("recipe_images", [])
        feature.setdefault("recipe", None)
        features.append(feature)
    return sorted(features, key=lambda feature: feature["title"])


def build_icon_index(features: list) -> dict:
    """Maps feature id -> resolved texture path (or None), so recipes can
    reference another feature by id and automatically pick up its icon."""
    return {feature["id"]: find_texture(feature["id"]) for feature in features}


def resolve_slot(raw_slot, icon_index: dict) -> dict | None:
    """Normalizes a recipe grid entry (None, a texture key string, or a
    {"key", "label"} object) into a renderable {icon, label} slot."""
    if raw_slot is None:
        return None
    if isinstance(raw_slot, str):
        raw_slot = {"key": raw_slot}

    key = raw_slot.get("key")
    icon = find_texture(key) if key else None
    if icon is None and key in icon_index:
        icon = icon_index[key]

    label = raw_slot.get("label") or (humanize(key) if key else None)
    return {"icon": icon, "label": label}


def resolve_recipe(recipe: dict | None, icon_index: dict) -> dict | None:
    if not recipe:
        return None

    shape = [
        [resolve_slot(slot, icon_index) for slot in row]
        for row in recipe.get("shape", [])
    ]

    result = recipe.get("result", {})
    result_key = result.get("key")
    result_icon = find_texture(result_key) if result_key else None
    if result_icon is None and result_key in icon_index:
        result_icon = icon_index[result_key]

    return {
        "shape": shape,
        "result": {
            "icon": result_icon,
            "label": result.get("label") or (humanize(result_key) if result_key else None),
            "count": result.get("count", 1),
        },
    }


def group_by_category(features: list) -> dict:
    grouped = {}
    for feature in features:
        grouped.setdefault(feature["category"], []).append(feature)
    return grouped


def clean_output_dir() -> None:
    if not os.path.isdir(constants.OUTPUT_DIR):
        os.makedirs(constants.OUTPUT_DIR)
        return
    for stale_page in glob.glob(os.path.join(constants.OUTPUT_DIR, "*.html")):
        os.remove(stale_page)


def main() -> None:
    env = Environment(
        loader=FileSystemLoader(constants.TEMPLATE_DIR),
        autoescape=select_autoescape(["html"]),
    )

    categories = load_categories()
    features = load_features()
    icon_index = build_icon_index(features)
    features_by_category = group_by_category(features)

    for feature in features:
        feature["icon"] = icon_index.get(feature["id"])
        feature["recipe"] = resolve_recipe(feature.get("recipe"), icon_index)

    clean_output_dir()

    index_template = env.get_template("index.html")
    with open(os.path.join(constants.OUTPUT_DIR, "index.html"), "w") as output_file:
        output_file.write(
            index_template.render(
                categories=categories,
                features_by_category=features_by_category,
                active_id=None,
            )
        )

    page_template = env.get_template("page.html")
    for feature in features:
        with open(os.path.join(constants.OUTPUT_DIR, f"{feature['id']}.html"), "w") as output_file:
            output_file.write(
                page_template.render(
                    categories=categories,
                    features_by_category=features_by_category,
                    active_id=feature["id"],
                    feature=feature,
                )
            )

    print(f"Generated {len(features)} feature pages and index.html in {constants.OUTPUT_DIR}/")


if __name__ == "__main__":
    main()
