"""1.20.1 bulk renames (imports + obvious call renames). python m1_1201_bulk.py <src root>"""
import re, sys, pathlib

REG = {  # Registry.X_REGISTRY -> Registries.X
    'BIOME_REGISTRY': 'BIOME', 'TEMPLATE_POOL_REGISTRY': 'TEMPLATE_POOL', 'DIMENSION_TYPE_REGISTRY': 'DIMENSION_TYPE',
    'STRUCTURE_SET_REGISTRY': 'STRUCTURE_SET', 'PROCESSOR_LIST_REGISTRY': 'PROCESSOR_LIST', 'NOISE_REGISTRY': 'NOISE',
    'BLOCK_REGISTRY': 'BLOCK', 'STRUCTURE_REGISTRY': 'STRUCTURE', 'LEVEL_STEM_REGISTRY': 'LEVEL_STEM', 'DIMENSION_REGISTRY': 'LEVEL_STEM',
    'ITEM_REGISTRY': 'ITEM', 'ENTITY_TYPE_REGISTRY': 'ENTITY_TYPE', 'CONFIGURED_FEATURE_REGISTRY': 'CONFIGURED_FEATURE',
    'PLACED_FEATURE_REGISTRY': 'PLACED_FEATURE', 'SOUND_EVENT_REGISTRY': 'SOUND_EVENT', 'FLUID_REGISTRY': 'FLUID',
}
SIMPLE = [
    (r'import com\.mojang\.math\.Matrix4f;', 'import org.joml.Matrix4f;'),
    (r'import com\.mojang\.math\.Matrix3f;', 'import org.joml.Matrix3f;'),
    (r'import com\.mojang\.math\.Vector3f;', 'import org.joml.Vector3f;'),
    (r'import com\.mojang\.math\.Vector4f;', 'import org.joml.Vector4f;'),
    (r'import com\.mojang\.math\.Quaternion;', 'import org.joml.Quaternionf;'),
    (r'import net\.minecraft\.client\.renderer\.block\.model\.ItemTransforms\.TransformType;', 'import net.minecraft.world.item.ItemDisplayContext;'),
    (r'\bTransformType\b', 'ItemDisplayContext'),
    (r'\bCapabilityFluidHandler\.FLUID_HANDLER_CAPABILITY\b', 'ForgeCapabilities.FLUID_HANDLER'),
    (r'\bCapabilityItemHandler\.ITEM_HANDLER_CAPABILITY\b', 'ForgeCapabilities.ITEM_HANDLER'),
    (r'\bCapabilityEnergy\.ENERGY\b', 'ForgeCapabilities.ENERGY'),
    (r'import net\.minecraftforge\.fluids\.capability\.CapabilityFluidHandler;', 'import net.minecraftforge.common.capabilities.ForgeCapabilities;'),
    (r'import net\.minecraftforge\.items\.CapabilityItemHandler;', 'import net.minecraftforge.common.capabilities.ForgeCapabilities;'),
    (r'import net\.minecraftforge\.energy\.CapabilityEnergy;', 'import net.minecraftforge.common.capabilities.ForgeCapabilities;'),
    (r'\bItemStack\.isSame\(', 'ItemStack.isSameItem('),
    (r'\bItemStack\.tagMatches\(', 'ItemStack.isSameItemSameTags('),   # (a,b) argument form differs; see below
    (r'\.isOnGround\(\)', '.onGround()'),
    (r'\bRenderSystem\.(enable|disable)Texture\(\);\s*', ''),
    (r'\.setFocus\(', '.setFocused('),
]
total = 0
for p in pathlib.Path(sys.argv[1]).rglob('*.java'):
    s = p.read_bytes().decode('utf-8'); o = s
    for a, b in SIMPLE:
        s = re.sub(a, b, s)
    for k, v in REG.items():
        s = re.sub(r'\bRegistry\.' + k + r'\b', 'Registries.' + v, s)
    if 'Registries.' in s and 'import net.minecraft.core.registries.Registries;' not in s:
        s = re.sub(r'(^|\n)(package [^;]+;\r?\n)', r'\1\2\nimport net.minecraft.core.registries.Registries;\n', s, count=1)
    if 'ForgeCapabilities.' in s and 'import net.minecraftforge.common.capabilities.ForgeCapabilities;' not in s:
        s = re.sub(r'(^|\n)(package [^;]+;\r?\n)', r'\1\2\nimport net.minecraftforge.common.capabilities.ForgeCapabilities;\n', s, count=1)
    # de-duplicate the same import line
    lines = s.split('\n'); seen = set(); out = []
    for ln in lines:
        key = ln.strip()
        if key.startswith('import ') and key in seen: continue
        seen.add(key); out.append(ln)
    s = '\n'.join(out)
    if s != o:
        p.write_bytes(s.encode('utf-8')); total += 1
print('files changed', total)
