package mods.immibis.cloudstorage;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;

import mods.immibis.core.api.util.BaseGuiContainer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GuiBrowser extends BaseGuiContainer<ContainerBrowser> {
	
	private ResourceLocation texPath = new ResourceLocation("cloud_storage", "textures/gui/storage.png");
	
	public GuiBrowser(ContainerBrowser container) {
		super(container, 243, 222, new ResourceLocation("cloud_storage", "textures/gui/storage.png"));
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		if(container.needUpdateVisibleIDs)
		{
			container.needUpdateVisibleIDs = false;
			updateSearch();
		}
		
		mc.renderEngine.bindTexture(texPath);
		GL11.glColor3f(1, 1, 1);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		//super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		searchField.drawTextBox();

		drawStringWithoutShadow("RF: " + ((ContainerBrowser) this.container).storage.storedRF, 176, 8, 0);
		drawStringWithoutShadow("EU: " + ((ContainerBrowser) this.container).storage.storedEU, 176, 16, 0);
		//TODO show fluids somehow
		
		//CloudStorage.INSTANCE.log.log(Level.INFO, "Drew background");
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int p_146979_1_,int p_146979_2_) {
		for (int i1 = 0; i1 < this.inventorySlots.inventorySlots.size(); ++i1)
        {
            Slot slot = (Slot)this.inventorySlots.inventorySlots.get(i1);
            this.drawSlot(slot);
        }
		
		super.drawGuiContainerForegroundLayer(p_146979_1_, p_146979_2_);
	}
	
	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		super.mouseClicked(par1, par2, par3);
		searchField.setFocused(true);
	}
	
	@Override
	protected void keyTyped(char par1, int par2) {
		if (!this.checkHotbarKeys(par2))
        {
            if (this.searchField.textboxKeyTyped(par1, par2))
            {
                this.updateSearch();
            }
            else
            {
                super.keyTyped(par1, par2);
            }
        }
	}
	
	private static Map<Integer, String> cachedNames = new HashMap<Integer, String>();
	public static String getItemName(int packedID)
	{
		String s = cachedNames.get(packedID);
		if(s != null)
			return s;
		
		s = new ItemStack(Item.getItemById(Storage.unpackItemID(packedID)), 1, Storage.unpackDamage(packedID)).getDisplayName();
		
		cachedNames.put(packedID, s);
		return s;
	}

	// 146977 = drawSlotInventory which is now private
	protected void drawSlot(Slot par1Slot) {
		ItemStack itemstack = par1Slot.getStack();
		String overrideSizeString = null;
		
		if(par1Slot.inventory == container.fakeInventory)
		{
			int packedID = container.visibleIDs[par1Slot.getSlotIndex()];
			
			if(packedID == 0)
				itemstack = null;
			else
			{
				//System.out.println(packedID);
				
				itemstack = Storage.unpackItemStack(packedID);
				long qty = container.storage.getQty(packedID);
				if(qty >= 10000000000L)
					overrideSizeString = String.valueOf(qty/1000000000)+"G";
				else if(qty >= 10000000)
					overrideSizeString = String.valueOf(qty/1000000)+"M";
				else if(qty >= 10000)
					overrideSizeString = String.valueOf(qty/1000)+"k";
				else
					overrideSizeString = String.valueOf(qty);
			}
		}
		
		int i = par1Slot.xDisplayPosition;
        int j = par1Slot.yDisplayPosition;

        this.zLevel = 100.0F;
        itemRender.zLevel = 100.0F;
        
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), itemstack, i, j);
        if(overrideSizeString == null)
        	itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), itemstack, i, j, overrideSizeString);
        else
        {
        	GL11.glPushMatrix();
        	GL11.glScalef(0.5f, 0.5f, 1f);
        	itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), itemstack, i*2+16, j*2+16, overrideSizeString);
        	GL11.glPopMatrix();
        }

        itemRender.zLevel = 0.0F;
        this.zLevel = 0.0F;
	}
	
	private void updateSearch()
	{
		String search = searchField.getText().toLowerCase();
		int k = 0;
		for(Integer id : container.storage.items.keySet())
		{
			String name = getItemName(id);
			
			if(!name.toLowerCase().contains(search))
				continue;
			
			container.visibleIDs[k++] = id;
			if(k >= container.visibleIDs.length)
				break;
		}
		
		for(; k < container.visibleIDs.length; k++)
			container.visibleIDs[k] = 0;
	}
	
	GuiTextField searchField;
	
	@Override
	public void initGui() {
		super.initGui();
		
		this.searchField = new GuiTextField(this.fontRendererObj, this.guiLeft + 9, this.guiTop + 7, 144, this.fontRendererObj.FONT_HEIGHT);
        this.searchField.setMaxStringLength(35);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setVisible(true);
        this.searchField.setTextColor(16777215);
        searchField.setFocused(true);
        
        updateSearch();
	}
}
