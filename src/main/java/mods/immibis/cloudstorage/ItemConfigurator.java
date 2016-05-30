package mods.immibis.cloudstorage;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemConfigurator extends Item {
	
	public static final int NUM_MODES = 10;
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
		super.addInformation(par1ItemStack, par2EntityPlayer, par3List, par4);
		par3List.add("Right-click air to change tool mode");
		par3List.add("Rclick block side to see setting");
		par3List.add("Shift-rclick block side to change");
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
		par1ItemStack.setItemDamage((par1ItemStack.getItemDamage() + 1) % NUM_MODES);
		if(par2World.isRemote)
			par3EntityPlayer.addChatMessage(new ChatComponentText("Current mode: "+getItemStackDisplayName(par1ItemStack)));
		return par1ItemStack;
	}
	
	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		
		if(world.isRemote)
		{
			Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(x, y, z, side, player.inventory.getCurrentItem(), hitX, hitY, hitZ));
			return true;
		}
		
		if(!world.blockExists(x, y, z))
			return false;
		
		CloudActionCoords key = new CloudActionCoords(x, y, z, side, world.provider.dimensionId);
		
		Storage s = CloudStorage.getStorage(player.getDisplayName()); // TODO probably incorrect
		CloudAction oldAction = s.actions.get(key);
		
		boolean sneak = player.isSneaking();
		
		if(!sneak)
		{
			if(oldAction == null)
				player.addChatMessage(new ChatComponentText("No action set for this side. Shift-click to adjust."));
			else
				player.addChatMessage(new ChatComponentText("On this side: "+oldAction.toString()));
		}
		else
		{
			CloudAction action = oldAction;
			switch(stack.getItemDamage())
			{
			case 1: // extract
				action = new CloudActionExtract();
				break;
			case -10000: // stock
				action = new CloudActionStock(key, s);
				break;
			case 2: // insert
				if(!(action instanceof CloudActionInsert))
					action = new CloudActionInsert();
				break;
			case 3: // extract RF
				action = new CloudActionExtractRF();
				break;
			case 4: // insert RF
				action = new CloudActionInsertRF();
				break;
			case 5: // extract RF
				action = new CloudActionExtractEU();
				break;
			case 6: // insert RF
				action = new CloudActionInsertEU();
				break;
			/*case 7: // insert MJ
				action = new CloudActionInsertMJ();
				break;*/
			case 7: // extract liquid
				action = new CloudActionExtractFluid();
				break;
			case 8: // insert liquid
				if(!(action instanceof CloudActionInsertFluid))
					action = new CloudActionInsertFluid();
				break;
			case 0: // nothing
				action = null;
				break;
			}
			player.addChatMessage(new ChatComponentText("This side set to: "+(action == null ? "do nothing" : action.toString())));
			if(action != null)
				s.actions.put(key, action);
			else
				s.actions.remove(key);
			
			if(action instanceof CloudActionFiltered)
			{
				ContainerFilter.nextFilterIDArray = ((CloudActionFiltered)action).filterIDs;
				player.openGui(CloudStorage.INSTANCE, CloudStorage.GUI_FILTER, player.worldObj, 0, 0, 0);
				ContainerFilter.nextFilterIDArray = null;
			}
		}
		
		return true;
	}

	public ItemConfigurator() {
		setCreativeTab(CreativeTabs.tabTools);
		setMaxStackSize(1);
		setHasSubtypes(true);
		setTextureName("cloud_storage:setuptool");
	}
	
	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack) {
		return "item.cloudstorage.configurator."+par1ItemStack.getItemDamage();
	}

}
