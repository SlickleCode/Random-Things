package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.renderer.GameRenderer;

import java.awt.Color;


import lumien.randomthings.container.DyeingMachineContainer;
import lumien.randomthings.util.DyeUtil;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Direct port of 1.12.2's {@code GuiDyeingMachine}, including its cosmetic
 * swatch that eases between the machine's "idle" color and whatever dye
 * currently sits in the dye slot.
 */
public class DyeingMachineScreen extends AbstractContainerScreen<DyeingMachineContainer> {
    private static final Identifier GUI_TEXTURES = Identifier.parse("randomthings:textures/gui/dyeing_machine.png");
    private static final int IDLE_COLOR = 9145227;
    private static final float COLOR_MOD = 1F / 255F;

    private int targetColor = IDLE_COLOR;
    private int currentColor = IDLE_COLOR;
    private float p = 1;

    public DyeingMachineScreen(DyeingMachineContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);

        this.imageWidth = 176;
        this.imageHeight = 141;
    }

    @Override
    public void containerTick() {
        super.containerTick();

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

        if (this.menu.getSlot(1).hasItem() && p == 1) {
            ItemStack dye = this.menu.getSlot(1).getItem();

            int checkDye = DyeUtil.getDyeColor(dye);

            if (checkDye != 0) {
                this.targetColor = checkDye;
            }
        } else if (!this.menu.getSlot(1).hasItem() && p == 1) {
            targetColor = IDLE_COLOR;
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title.getString(), 8, 6, 0xFF404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, GUI_TEXTURES, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        Color c = new Color(currentColor);
        graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, GUI_TEXTURES, x + 102, y + 23, 176, 21, 22, 15, 256, 256, c.getRGB());
        // 1.20.5+ draws slot items after renderBg without resetting the colour: leave it white or every item shows the swatch tint
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
