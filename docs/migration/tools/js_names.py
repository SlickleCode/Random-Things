import re
D='D:/Projects/Clauding/Random-Things/1.18.2/src/main/resources/transformer/'
R={
'BlockRendererDispatcherTransformer.js':[('"renderModel"','"renderBatched"'),('"func_228793_a_"','"m_110924_"')],
'IBlockReaderTransformer.js':[('"getLightValue"','"getLightEmission"'),('"func_217298_h"','"m_7146_"')],
'ItemRendererTransformer.js':[('"renderItem"','"render"'),('"func_229111_a_"','"m_115143_"'),('"renderQuads"','"renderQuadList"'),('"func_229112_a_"','"m_115162_"')],
'MagicHoodTransformer.js':[('"canRenderName"','"shouldShowName"'),('"func_177070_b"','"m_6512_"')],
'PlayerEntityTransformer.js':[('"dropInventory"','"dropEquipment"'),('"func_213337_cE"','"m_5907_"'),('"dropAllItems"','"dropAll"'),('"func_70436_m"','"m_36071_"')],
'RainShieldTransformer.js':[('"func_175727_C"','"m_46758_"')],
'SpawnPlacementTransformer.js':[('"canSpawnEntity"','"checkSpawnRules"'),('"func_223515_a"','"m_21759_"')],
'SuperLubricentBootsTransformer.js':[('"func_213352_e"','"m_7023_"')],
'WorldReaderStrongPowerTransformer.js':[('"getStrongPower"','"getDirectSignal"'),('"func_175627_a"','"m_46852_"')],
'WorldRedstonePowerTransformer.js':[('"getRedstonePower"','"getSignal"'),('"func_175651_c"','"m_46681_"')],
'WorldRendererTransformer.js':[('"renderRainSnow"','"renderSnowAndRain"'),('"func_228438_a_"','"m_109703_"'),('"addRainParticles"','"tickRain"'),('"func_228436_a_"','"m_109693_"'),('"func_226691_t_"','"m_204166_"')],
'FireBlockTransformer.js':[('"getTickCooldown"','"getFireTickDelay"'),('"func_235495_a_"','"m_53468_"')],
}
for f,rules in R.items():
    s=open(D+f,encoding='utf8',newline='').read()
    for a,b in rules:
        if a not in s: print('MISSING',f,a)
        s=s.replace(a,b)
    open(D+f,'w',encoding='utf8',newline='').write(s)
