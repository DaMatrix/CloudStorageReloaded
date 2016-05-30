package mods.immibis.cloudstorage;

import org.lwjgl.opengl.GL11;

import mods.immibis.core.api.util.BaseGuiContainer;
import net.minecraft.util.ResourceLocation;

public class GuiFilter extends BaseGuiContainer<ContainerFilter> {
	
	private ResourceLocation texPath = new ResourceLocation("cloud_storage", "textures/gui/filter.png");
	
	public GuiFilter(ContainerFilter container) {
		super(container, 176, 222, new ResourceLocation("cloud_storage", "textures/gui/filter.png"));
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		mc.renderEngine.bindTexture(this.texPath);
		GL11.glColor3f(1, 1, 1);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		drawStringWithoutShadow(container.getNameText(), 8, 6, 0x404040);
	}
}
