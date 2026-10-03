package lumien.randomthings.client.screen;

import java.awt.Color;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.DyeingMachineContainer;
import lumien.randomthings.util.DyeUtil;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * Direct port of 1.12.2's {@code GuiDyeingMachine}, including its cosmetic
 * swatch that eases between the machine's "idle" color and whatever dye
 * currently sits in the dye slot.
 */
public class DyeingMachineScreen extends ContainerScreen<DyeingMachineContainer> {
    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/dyeing_machine.png");
    private static final int IDLE_COLOR = 9145227;
    private static final float COLOR_MOD = 1F / 255F;

    private int targetColor = IDLE_COLOR;
    private int currentColor = IDLE_COLOR;
    private float p = 1;

    public DyeingMachineScreen(DyeingMachineContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);

        this.xSize = 176;
        this.ySize = 141;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.currentColor != this.targetColor) {
            this.p -= 0.02f;
            Color currentObj = new Color(currentColor);
            Color targetObj = new Color(targetColor);

            currentColor = new Color(
                    COLOR_MOD * (currentObj.getRed() * p + targetObj.getRed() * (1 - p)),
                    COLOR_MOD * (currentObj.getGreen() * p + targetObj.getGreen() * (1 - p)),
                    COLOR_MOD * (currentObj.getBlue() * p + targetObj.getBlue() * (1 - p))).getRGB();
        }

        if (p <= 0f) {
            this.currentColor = this.targetColor;
            this.p = 1;
        }

        if (this.container.getSlot(1).getHasStack() && p == 1) {
            ItemStack dye = this.container.getSlot(1).getStack();

            int checkDye = DyeUtil.getDyeColor(dye);

            if (checkDye != 0) {
                this.targetColor = checkDye;
            }
        } else if (!this.container.getSlot(1).getHasStack() && p == 1) {
            targetColor = IDLE_COLOR;
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.font.drawString(this.title.getString(), 8, 6, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(GUI_TEXTURES);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.blit(x, y, 0, 0, this.xSize, this.ySize);

        Color c = new Color(currentColor);
        GlStateManager.color3f(COLOR_MOD * c.getRed(), COLOR_MOD * c.getGreen(), COLOR_MOD * c.getBlue());
        this.blit(x + 102, y + 23, 176, 21, 22, 15);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}
