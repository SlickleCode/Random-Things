# Mechanical parity check vs 1.14.4

Baseline: 355 classes (without debug), 20 config keys, 222 recipes, 375 lang keys.

## 1.16.5
classes 361, config keys 20, recipes 222, lang keys 375
- classes missing vs 1.14.4 (3): MathUtils, SpectreDimension, SpiritSpawnEggItem
- classes only here (9): AlphaVertexBuilder, BiomeUtil, IClientItemProperties, RenderLayerSetup, SpectreDimensionType, SpectreRenderInfo, SpectreTeleporter, TintedGlintRenderType, UuidTag
- config keys missing: []; only here: []
- config default differs: {}
- recipe files missing: 0 []; only here: 0 []
- recipe content differs (0): 
- item/block lang keys missing: 0 []

## 1.18.2
classes 363, config keys 20, recipes 222, lang keys 375
- classes missing vs 1.14.4 (3): MathUtils, SpectreDimension, SpiritSpawnEggItem
- classes only here (11): AlphaVertexBuilder, BiomeUtil, IClientItemProperties, RenderLayerSetup, ScreenWidgets, SpectreRenderInfo, SpectreTeleporter, Tickers, TimeWheelRenderType, TintedGlintRenderType, UuidTag
- config keys missing: []; only here: []
- config default differs: {}
- recipe files missing: 0 []; only here: 0 []
- recipe content differs (0): 
- item/block lang keys missing: 0 []

## 1.19.2
classes 364, config keys 20, recipes 222, lang keys 375
- classes missing vs 1.14.4 (3): MathUtils, SpectreDimension, SpiritSpawnEggItem
- classes only here (12): AlphaVertexBuilder, BiomeUtil, IClientItemProperties, RegUtil, RenderLayerSetup, ScreenWidgets, SpectreRenderInfo, SpectreTeleporter, Tickers, TimeWheelRenderType, TintedGlintRenderType, UuidTag
- config keys missing: []; only here: []
- config default differs: {}
- recipe files missing: 0 []; only here: 0 []
- recipe content differs (0): 
- item/block lang keys missing: 0 []

## 1.20.1
classes 364, config keys 20, recipes 222, lang keys 375
- classes missing vs 1.14.4 (3): MathUtils, SpectreDimension, SpiritSpawnEggItem
- classes only here (12): AlphaVertexBuilder, BiomeUtil, IClientItemProperties, RegUtil, RenderLayerSetup, ScreenWidgets, SpectreRenderInfo, SpectreTeleporter, Tickers, TimeWheelRenderType, TintedGlintRenderType, UuidTag
- config keys missing: []; only here: []
- config default differs: {}
- recipe files missing: 0 []; only here: 0 []
- recipe content differs (0): 
- item/block lang keys missing: 0 []

## 1.20.6
classes 372, config keys 20, recipes 222, lang keys 375
- classes missing vs 1.14.4 (3): MathUtils, SpectreDimension, SpiritSpawnEggItem
- classes only here (20): AABBs, AlphaVertexBuilder, BiomeUtil, BlockPosNbt, CapUtil, IClientItemProperties, ModCapabilities, ModDataComponents, ObjectHolder, ObjectHolderInjector, PotionHelper, RTRegistry, RegUtil, RenderLayerSetup, ScreenWidgets, SpectreRenderInfo, SpectreTeleporter, StackTag, Tickers, TintedGlintRenderType
- config keys missing: []; only here: []
- config default differs: {}
- recipe files missing: 0 []; only here: 0 []
- recipe content differs (1): super_lubricent_tincture: ('randomthings:super_lubricent_tincture', 4, ('item:minecraft:wheat_seeds/item:minecraft:potion', 'item:randomthings:beans/_')) -> ('randomthings:super_lubricent_tincture', 4, ('item:minecraft:wheat_seeds/{"components": {"minecraft:potion_contents": {"potion": "minecraft:water"}}, "items": "minecraft:potion", "type": "neoforge:components"}', 'item:randomthings:beans/_'))
- item/block lang keys missing: 0 []

## 1.21.1
classes 371, config keys 20, recipes 222, lang keys 375
- classes missing vs 1.14.4 (4): MagneticEnchantment, MathUtils, SpectreDimension, SpiritSpawnEggItem
- classes only here (20): AABBs, AlphaVertexBuilder, BiomeUtil, BlockPosNbt, CapUtil, IClientItemProperties, ModCapabilities, ModDataComponents, ObjectHolder, ObjectHolderInjector, PotionHelper, RTRegistry, RegUtil, RenderLayerSetup, ScreenWidgets, SpectreRenderInfo, SpectreTeleporter, StackTag, Tickers, TintedGlintRenderType
- config keys missing: []; only here: []
- config default differs: {}
- recipe files missing: 0 []; only here: 0 []
- recipe content differs (1): super_lubricent_tincture: ('randomthings:super_lubricent_tincture', 4, ('item:minecraft:wheat_seeds/item:minecraft:potion', 'item:randomthings:beans/_')) -> ('randomthings:super_lubricent_tincture', 4, ('item:minecraft:wheat_seeds/{"components": {"minecraft:potion_contents": {"potion": "minecraft:water"}}, "items": "minecraft:potion", "type": "neoforge:components"}', 'item:randomthings:beans/_'))
- item/block lang keys missing: 0 []

## 1.21.4
classes 373, config keys 20, recipes 222, lang keys 375
- classes missing vs 1.14.4 (6): BucketFluidModel, MagneticEnchantment, MathUtils, PortkeyItemRenderer, SpectreDimension, SpiritSpawnEggItem
- classes only here (24): AABBs, AlphaVertexBuilder, BiomeUtil, BlockPosNbt, CapUtil, ClientFuel, DyedModel, FuelHelper, ModCapabilities, ModDataComponents, ObjectHolder, ObjectHolderInjector, PotionHelper, RTIds, RTItemModels, RTRegistry, RegUtil, RenderLayerSetup, ScreenWidgets, SpectreRenderInfo, SpectreTeleporter, StackTag, Tickers, TintedGlintRenderType
- config keys missing: []; only here: []
- config default differs: {}
- recipe files missing: 0 []; only here: 0 []
- recipe content differs (1): super_lubricent_tincture: ('randomthings:super_lubricent_tincture', 4, ('item:minecraft:wheat_seeds/item:minecraft:potion', 'item:randomthings:beans/_')) -> ('randomthings:super_lubricent_tincture', 4, ('item:minecraft:wheat_seeds/{"components": {"minecraft:potion_contents": {"potion": "minecraft:water"}}, "items": "minecraft:potion", "neoforge:ingredient_type": "neoforge:components"}', 'item:randomthings:beans/_'))
- item/block lang keys missing: 0 []

## 1.21.11
classes 386, config keys 20, recipes 222, lang keys 375
- classes missing vs 1.14.4 (6): BucketFluidModel, MagneticEnchantment, MathUtils, PortkeyItemRenderer, SpectreDimension, SpiritSpawnEggItem
- classes only here (37): AABBs, BiomeUtil, BlockPosNbt, CapUtil, ClientFuel, ClientPayloads, EnergyStorageHandler, FuelHelper, IBlockTooltip, IItemHandler, InvWrapper, ItemHandlerAdapter, ItemHandlerHelper, ItemStackHandler, ModCapabilities, ModDataComponents, ObjectHolder, ObjectHolderInjector, PotionHelper, RTClientTransformers, RTIds, RTItemModels, RTRegistry, RTTransformer, RTTransformers, RangedWrapper, RegUtil, RenderLayerSetup, ResourceHandlerItemAdapter, ScreenWidgets, SlotItemHandler, SpectreTeleporter, StackTag, TagIO, Tickers, TintedGlintRenderType, TooltipBlockItem
- config keys missing: []; only here: []
- config default differs: {}
- recipe files missing: 0 []; only here: 0 []
- recipe content differs (1): super_lubricent_tincture: ('randomthings:super_lubricent_tincture', 4, ('item:minecraft:wheat_seeds/item:minecraft:potion', 'item:randomthings:beans/_')) -> ('randomthings:super_lubricent_tincture', 4, ('item:minecraft:wheat_seeds/{"components": {"minecraft:potion_contents": {"potion": "minecraft:water"}}, "items": "minecraft:potion", "neoforge:ingredient_type": "neoforge:components"}', 'item:randomthings:beans/_'))
- item/block lang keys missing: 0 []

