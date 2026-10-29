# Mod feature data

This is the single source of truth for everything the wiki documents. `scripts/generate_html.py`
reads it and produces the static pages under `docs/`. Nothing here should ever need a template
or generator change to use - adding new content is just adding data.

## Adding a feature

Drop a new file at `data/features/<id>.json`. The filename (without `.json`) becomes the
feature's id, its output filename (`docs/<id>.html`), and the key used to auto-resolve an icon
(`textures/item/<id>.png`, `textures/block/<id>.png`, or `textures/models/armor/<id>.png`, checked
in that order).

```jsonc
{
  "title": "Stable Ender Pearl",      // required
  "category": "item",                 // required, must match an id in categories.json
  "description": "...",               // required
  "keywords": ["Ender", "Pearl"],     // optional, powers the sidebar search filter
  "images": ["images/foo.png"],       // optional, screenshot gallery (relative paths or full URLs)
  "videos": ["videos/foo.mp4"],       // optional, same gallery, rendered as <video>
  "recipe_images": ["images/bar.png"],// optional, one or more flat pictures of the recipe(s)
  "recipe": { ... },                  // optional, structured recipe (see below) - takes
                                       // priority over recipe_images when both are set
  "properties": [                     // optional, freeform quick-facts table
    { "label": "Teleport Delay", "value": "7 seconds" }
  ]
}
```

Only `title`, `category` and `description` are required. Everything else is additive, so a
feature with no recipe or properties yet still renders a perfectly normal page - fill it in later.

## Structured recipes

Instead of (or alongside) flat `recipe_images`, a feature can describe a 3x3 crafting grid, and
the generator will render it as a real recipe grid with resolved icons:

```jsonc
"recipe": {
  "shape": [
    [null, "redstone", null],
    ["redstone", "iron_ingot", "redstone"],
    [null, "stick", null]
  ],
  "result": { "key": "advanced_redstone_torch", "count": 1 }
}
```

Each grid slot is `null` (empty), a texture key string, or `{"key": "...", "label": "..."}` for
an explicit display label. Keys are resolved in this order:

1. `textures/item/<key>.png`, `textures/block/<key>.png`, `textures/models/armor/<key>.png`,
   `textures/vanilla/<key>.png`
2. another feature's own id (so a recipe can point at, say, `"stable_ender_pearl"` and it'll pick
   up that feature's icon automatically)
3. otherwise, the slot renders as a plain text label (the key, title-cased) - useful for vanilla
   Minecraft ingredients this repo has no texture for yet, e.g. `"redstone"` or `"iron_ingot"`

For that last case, run `scripts/fetch_vanilla_textures.py` (see below) to fill in the missing
icon instead of leaving it as a text label.

## Categories

`data/categories.json` defines the sidebar sections (id, label, and an inline SVG icon). Adding a
new category - say `"machine"` - means adding one entry there; the sidebar, homepage, and search
all pick it up automatically for any feature whose `"category"` matches.

## Maintenance scripts

Two scripts keep the data self-sufficient instead of depending on other sites at page-load time.
Both need real network access that this sandboxed dev environment doesn't have, so run them
somewhere that does (a contributor's machine, or a CI job with broader egress), then commit
whatever they download/change:

- **`scripts/fetch_vanilla_textures.py`** scans every structured `recipe` for ingredient/result
  keys that don't resolve to a texture yet (typically vanilla Minecraft items referenced by a
  recipe, rendered as a text-label placeholder in the meantime) and downloads an icon for each
  from the Minecraft Wiki into `textures/vanilla/`. If a key's guessed wiki file title doesn't
  match reality, add an override to `data/vanilla_texture_names.json`, e.g.
  `{"redstone": "Redstone Dust"}`, and rerun it. `--dry-run` lists what's missing without
  downloading anything.

- **`scripts/fetch_lumien_assets.py`** finds every `images`/`videos`/`recipe_images` entry still
  pointing at a `lumien.net` URL (left there because this repo's content was migrated from
  lumien.net/rtwiki and its pictures/videos haven't all been re-hosted here yet), downloads it
  into `docs/images/` or `docs/videos/`, and rewrites the feature's JSON to point at the local
  copy. `--dry-run` lists what it would fetch; `--force` re-downloads files that already exist
  locally.
