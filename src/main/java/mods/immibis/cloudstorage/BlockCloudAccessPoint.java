package mods.immibis.cloudstorage;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class BlockCloudAccessPoint extends Block {

	public BlockCloudAccessPoint(Material mat) {
		super(mat);
		setBlockTextureName("cloud_storage:blockAccessPoint");
		setBlockName("blockAccessPoint");
		setCreativeTab(CreativeTabs.tabTools);
	}

	public boolean onBlockActivated(World w, int x, int y, int z,
			EntityPlayer p, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
		if (!w.isRemote)	{
			p.openGui(CloudStorage.INSTANCE, CloudStorage.INSTANCE.GUI_BROWSE, w, 0, 0, 0);
		}
		return true;
	}

}
