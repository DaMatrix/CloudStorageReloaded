package mods.immibis.cloudstorage;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class ItemWirelessCloud extends Item {
	public ItemWirelessCloud()	{
		setCreativeTab(CreativeTabs.tabTools);
		setUnlocalizedName("itemWirelessAccessPoint");
		setMaxStackSize(1);
		setTextureName("cloud_storage:itemWirelessAccessPoint");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
		super.addInformation(par1ItemStack, par2EntityPlayer, par3List, par4);
		par3List.add("Right-click to open your cloud");
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		
		if (!world.isRemote)	{
			player.openGui(CloudStorage.INSTANCE, CloudStorage.INSTANCE.GUI_BROWSE, world, 0, 0, 0);
		}
		
		return stack;
	}
}
