import os

# Resolved from this file's own location rather than the current working
# directory, so every script here runs the same whether it's invoked as
# `python3 generate_html.py` from inside scripts/, `python3 scripts/generate_html.py`
# from the repo root (e.g. the GitHub Actions workflow), or by an absolute path.
SCRIPTS_DIR = os.path.dirname(os.path.abspath(__file__))
REPO_ROOT = os.path.dirname(SCRIPTS_DIR)

DATA_DIR = os.path.join(REPO_ROOT, "data")
FEATURES_DIR = os.path.join(DATA_DIR, "features")
CATEGORIES_FILE = os.path.join(DATA_DIR, "categories.json")
VANILLA_TEXTURE_OVERRIDES_FILE = os.path.join(DATA_DIR, "vanilla_texture_names.json")

TEMPLATE_DIR = os.path.join(REPO_ROOT, "template-html")
OUTPUT_DIR = os.path.join(REPO_ROOT, "docs")
TEXTURES_DIR = os.path.join(OUTPUT_DIR, "textures")
OUTPUT_IMAGES_DIR = os.path.join(OUTPUT_DIR, "images")
OUTPUT_VIDEOS_DIR = os.path.join(OUTPUT_DIR, "videos")

# Subdirectory (relative to TEXTURES_DIR) that downloaded vanilla Minecraft
# icons land in. Listed last in TEXTURE_SEARCH_DIRS so a mod's own texture
# always wins if a key happens to collide.
VANILLA_TEXTURE_DIR = "vanilla"

# Subdirectories (relative to TEXTURES_DIR) searched, in order, when
# resolving a texture key to an icon for a feature or a recipe slot.
TEXTURE_SEARCH_DIRS = [
    "item",
    "block",
    "models/armor",
    VANILLA_TEXTURE_DIR,
]

# Used by scripts/fetch_vanilla_textures.py
MINECRAFT_WIKI_FILEPATH_URL = "https://minecraft.wiki/w/Special:FilePath/{}"

# Used by scripts/fetch_lumien_assets.py
LUMIEN_HOST = "lumien.net"

USER_AGENT = "rt-wiki-asset-fetcher/1.0 (+https://github.com/SlickleCode/rt_wiki)"
