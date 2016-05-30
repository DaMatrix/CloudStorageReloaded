package mods.immibis.cloudstorage;

import java.util.List;

import mods.immibis.core.api.util.NBTType;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemCopier extends Item {

	public ItemCopier() {
		setCreativeTab(CreativeTabs.tabTools);
		setUnlocalizedName("cloudstorage.copier");
		setMaxStackSize(1);
		setHasSubtypes(true);
		setTextureName("cloud_storage:copier");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
		super.addInformation(par1ItemStack, par2EntityPlayer, par3List, par4);
		par3List.add("Sneak-right-click to copy");
		par3List.add("Right-click to paste");
	}
	
	private NBTTagCompound copy(EntityPlayer player, World world, int x, int y, int z)
	{
		Storage s = CloudStorage.getStorage(player.getDisplayName()); // TODO WRONG!
		
		NBTTagCompound savedActionsTag = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		savedActionsTag.setTag("actions", list);
		for(int side = 0; side < 6; side++)
		{
			CloudActionCoords key = new CloudActionCoords(x, y, z, side, world.provider.dimensionId);
			
			CloudAction action = s.actions.get(key);
			if(action == null)
				continue;
			
			NBTTagCompound c = new NBTTagCompound();
			action.save(c);
			c.setString("type", action.getType().name());
			c.setInteger("side", key.side);
			list.appendTag(c);
		}
		return savedActionsTag;
	}
	
	private void paste(EntityPlayer player, World world, int x, int y, int z, NBTTagCompound savedActionsTag)
	{
		Storage s = CloudStorage.getStorage(player.getDisplayName()); // TODO WRONG!
		NBTTagList list = savedActionsTag.getTagList("actions", NBTType.COMPOUND);
		for(int k = 0; k < list.tagCount(); k++)
		{
			NBTTagCompound c = list.getCompoundTagAt(k);
			
			try {
				CloudAction action = CloudActionType.valueOf(c.getString("type")).clazz.newInstance();
				action.load(c);
				CloudActionCoords key = new CloudActionCoords(x, y, z, c.getInteger("side"), world.provider.dimensionId);
				s.actions.put(key, action);
				
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int sideClicked, float hitX, float hitY, float hitZ) {
		
		if(world.isRemote)
		{
			Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(x, y, z, sideClicked, player.inventory.getCurrentItem(), hitX, hitY, hitZ));
			return true;
		}
		
		if(!world.blockExists(x, y, z))
			return false;
		
		if(player.isSneaking())
		{
			if(!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setTag("saved", copy(player, world, x, y, z));
			player.addChatMessage(new ChatComponentText("Copied data from block."));
		}
		else
		{
			if(!stack.hasTagCompound() || !stack.getTagCompound().hasKey("saved"))
				player.addChatMessage(new ChatComponentText("No data saved. Sneak-right-click a block to copy it."));
			else
			{
				paste(player, world, x, y, z, stack.getTagCompound().getCompoundTag("saved"));
				player.addChatMessage(new ChatComponentText("Pasted data onto block."));
			}
		}
		
		return true;
	}

}
