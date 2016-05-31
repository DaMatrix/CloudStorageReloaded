package mods.immibis.cloudstorage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import mods.immibis.cobaltite.AssignedItem;
import mods.immibis.cobaltite.CobaltiteMod;
import mods.immibis.cobaltite.ModBase;
import mods.immibis.cobaltite.NonTileGUI;
import mods.immibis.cobaltite.PacketType;
import mods.immibis.core.api.FMLModInfo;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = "Cloud Storage", name = "Cloud Storage", version = "60.0.0")
@CobaltiteMod(channel = CloudStorage.CHANNEL)
public class CloudStorage extends ModBase {

	@Instance("Cloud Storage")
	public static CloudStorage INSTANCE;

	public static Logger log;

	@NonTileGUI(container = ContainerBrowser.class, gui = GuiBrowser.class)
	public static final int GUI_BROWSE = 1;

	@NonTileGUI(container = ContainerFilter.class, gui = GuiFilter.class)
	public static final int GUI_FILTER = 2;

	public static final String CHANNEL = "CloudStorage";

	@PacketType(direction = PacketType.Direction.S2C, type = PacketStorageInfo.class)
	public static final int S2C_STORAGE_GUI_SYNC = 0;

	@PacketType(direction = PacketType.Direction.C2S, type = PacketSetVisibleSlot.class)
	public static final int C2S_SET_VISIBLE_SLOT = 1;

	@AssignedItem(id = "configurator")
	public static ItemConfigurator itemConfigurator;

	@AssignedItem(id = "copier")
	public static ItemCopier itemCopier;

	public static ItemWirelessCloud itemWirelessCloud;

	public static BlockCloudAccessPoint blockCloudAccessPoint;

	/**
	 * Stores which clouds are shared and which ones aren't. Syntax: key =
	 * owner, data = shared cloud
	 */
	public static HashMap<String, String> sharedClouds = new HashMap<String, String>();

	/**
	 * For all shares that aren't yet.
	 */
	public static HashMap<String, String> pendingShares = new HashMap<String, String>();

	private static final String SAVE_DATA_IDENTIFIER = "CloudStorage";

	public static Storage getStorage(String username) {
		assert FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER;

		String toLoad = username;

		if (CloudStorage.INSTANCE.sharedClouds.containsKey(username)) {
			toLoad = CloudStorage.INSTANCE.sharedClouds.get(username);
		}

		String name = SAVE_DATA_IDENTIFIER + "-" + toLoad;

		MapStorage mapStorage = MinecraftServer.getServer().worldServers[0].mapStorage;
		Storage s = (Storage) mapStorage.loadData(Storage.class, name);
		if (s == null) {
			s = new Storage(name);
			mapStorage.setData(name, s);
		}
		return s;
	}

	@EventHandler
	public void base_init(FMLInitializationEvent evt) {
		super._init(evt);
	}

	@EventHandler
	public void base_preinit(FMLPreInitializationEvent evt) {
		super._preinit(evt);
		log = evt.getModLog();
		itemWirelessCloud = new ItemWirelessCloud();
		GameRegistry.registerItem(itemWirelessCloud, itemWirelessCloud.getUnlocalizedName());
		blockCloudAccessPoint = new BlockCloudAccessPoint(Material.iron);
		GameRegistry.registerBlock(blockCloudAccessPoint, blockCloudAccessPoint.getUnlocalizedName());
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected void clientInit() throws Exception {

	}

	@Override
	protected void addRecipes() throws Exception {
		GameRegistry.addShapelessRecipe(new ItemStack(itemConfigurator), Blocks.dirt, Items.redstone);
		GameRegistry.addShapelessRecipe(new ItemStack(itemCopier), Blocks.dirt, Blocks.dirt, Items.redstone);
		GameRegistry.addRecipe(new ItemStack(itemWirelessCloud),
				new Object[] { "E  ", "X#X", "RDR", 'E', Items.ender_pearl, 'X', Items.iron_ingot, '#',
						Blocks.glass_pane, 'R', Items.redstone, 'D', Items.diamond });
		GameRegistry.addRecipe(new ItemStack(blockCloudAccessPoint), new Object[] { "#E#", "RCR", "#R#", '#',
				Items.iron_ingot, 'E', Items.ender_pearl, 'R', Items.redstone, 'C', Blocks.chest });
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent evt) {
		if (evt.phase != TickEvent.Phase.START)
			return;

		for (Object player : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
			Storage s = getStorage(((EntityPlayerMP) player).getGameProfile().getName());

			List<CloudActionCoords> toRemove = null;
			for (Map.Entry<CloudActionCoords, CloudAction> e : s.actions.entrySet()) {
				CloudActionCoords key = e.getKey();
				World world = DimensionManager.getWorld(key.dimension);
				if (world == null || !world.blockExists(key.x, key.y, key.z))
					continue;
				if (!e.getValue().apply(key, world, s)) {
					ForgeDirection side = ForgeDirection.VALID_DIRECTIONS[key.side];

					world.playAuxSFX(2004, key.x + side.offsetX, key.y + side.offsetY, key.z + side.offsetZ, 0);

					if (toRemove == null)
						toRemove = new ArrayList<CloudActionCoords>();
					toRemove.add(e.getKey());
				}
			}
			if (toRemove != null) {
				for (CloudActionCoords key : toRemove) {
					s.actions.remove(key);
				}
				s.setDirty(true);
			}
		}
	}

	@Override
	protected void sharedInit() throws Exception {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@EventHandler
	public void onServerStart(FMLServerStartingEvent evt) {
<<<<<<< refs/remotes/origin/master
		/*
		 * evt.registerServerCommand(new CommandBase() {
		 * 
		 * @Override public void processCommand(ICommandSender icommandsender,
		 * String[] astring) { if (icommandsender instanceof EntityPlayerMP)
		 * ((EntityPlayerMP) icommandsender).openGui(INSTANCE, GUI_BROWSE,
		 * ((EntityPlayerMP) icommandsender).worldObj, 0, 0, 0); }
		 * 
		 * @Override public String getCommandUsage(ICommandSender
		 * icommandsender) { return "/cloud"; }
		 * 
		 * @Override public boolean canCommandSenderUseCommand(ICommandSender
		 * par1iCommandSender) { return par1iCommandSender instanceof
		 * EntityPlayer; }
		 * 
		 * @Override public String getCommandName() { return "cloud"; } });
		 */
=======
		CloudStorage.INSTANCE.sharedClouds.clear();
		CloudStorage.INSTANCE.pendingShares.clear();
>>>>>>> v60.0.0! yay!
		evt.registerServerCommand(new CommandBase() {
			@Override
			public void processCommand(ICommandSender icommandsender, String[] astring) {
				try {
					if (astring[0].equals(null) || astring[0].equals("")) {
						return;
					}
				} catch (IndexOutOfBoundsException e) {
					return;
				}
				if (!(icommandsender instanceof EntityPlayerMP)) {
					return;
				}
				if (CloudStorage.INSTANCE.getPlayer(astring[0]) == null) {
					return;
				}
				CloudStorage.INSTANCE.pendingShares.put(astring[0], ((EntityPlayerMP) icommandsender).getDisplayName());
				EntityPlayer player = CloudStorage.INSTANCE.getPlayer(astring[0]);
				player.addChatMessage(new ChatComponentText("You have been invited to join "
						+ ((EntityPlayerMP) icommandsender).getDisplayName() + "'s cloud."));
				player.addChatMessage(new ChatComponentText("type /cloudaccept or /clouddecline"));
				((EntityPlayerMP) icommandsender).addChatMessage(new ChatComponentText("Request sent."));
			}

			@Override
			public String getCommandUsage(ICommandSender icommandsender) {
				return "/cloudshare <name>";
			}

			@Override
			public boolean canCommandSenderUseCommand(ICommandSender par1iCommandSender) {
				return par1iCommandSender instanceof EntityPlayer;
			}

			@Override
			public String getCommandName() {
				return "cloudshare";
			}
		});
		evt.registerServerCommand(new CommandBase() {
			@Override
			public void processCommand(ICommandSender icommandsender, String[] astring) {
				if (!(icommandsender instanceof EntityPlayerMP)) {
					return;
				}
				if (CloudStorage.INSTANCE.pendingShares
						.containsKey(((EntityPlayerMP) icommandsender).getDisplayName())) {
					CloudStorage.INSTANCE.sharedClouds.put(((EntityPlayerMP) icommandsender).getDisplayName(),
							CloudStorage.INSTANCE.pendingShares
									.get(((EntityPlayerMP) icommandsender).getDisplayName()));
					((EntityPlayerMP) icommandsender).addChatMessage(new ChatComponentText("Sucessully accepted"));
					CloudStorage.INSTANCE
							.getPlayer(CloudStorage.INSTANCE.pendingShares
									.get(((EntityPlayerMP) icommandsender).getDisplayName()))
							.addChatMessage(new ChatComponentText("Your share request was accepted"));
					CloudStorage.INSTANCE.pendingShares.remove(((EntityPlayerMP) icommandsender).getDisplayName());
				} else {
					((EntityPlayerMP) icommandsender)
							.addChatMessage(new ChatComponentText("You don't have any pending shares"));
				}
			}

			@Override
			public String getCommandUsage(ICommandSender icommandsender) {
				return "/cloudaccept";
			}

			@Override
			public boolean canCommandSenderUseCommand(ICommandSender par1iCommandSender) {
				return par1iCommandSender instanceof EntityPlayer;
			}

			@Override
			public String getCommandName() {
				return "cloudaccept";
			}
		});
		evt.registerServerCommand(new CommandBase() {
			@Override
			public void processCommand(ICommandSender icommandsender, String[] astring) {
				if (!(icommandsender instanceof EntityPlayerMP)) {
					return;
				}
				if (CloudStorage.INSTANCE.pendingShares
						.containsKey(((EntityPlayerMP) icommandsender).getDisplayName())) {
					((EntityPlayerMP) icommandsender).addChatMessage(new ChatComponentText("Sucessully declined"));
					CloudStorage.INSTANCE
							.getPlayer(CloudStorage.INSTANCE.pendingShares
									.get(((EntityPlayerMP) icommandsender).getDisplayName()))
							.addChatMessage(new ChatComponentText("Your share request was declined"));
					CloudStorage.INSTANCE.pendingShares.remove(((EntityPlayerMP) icommandsender).getDisplayName());
				} else {
					((EntityPlayerMP) icommandsender)
							.addChatMessage(new ChatComponentText("You don't have any pending shares"));
				}
			}

			@Override
			public String getCommandUsage(ICommandSender icommandsender) {
				return "/clouddecline";
			}

			@Override
			public boolean canCommandSenderUseCommand(ICommandSender par1iCommandSender) {
				return par1iCommandSender instanceof EntityPlayer;
			}

			@Override
			public String getCommandName() {
				return "clouddecline";
			}
		});
		evt.registerServerCommand(new CommandBase() {
			@Override
			public void processCommand(ICommandSender icommandsender, String[] astring) {
				try {
					if (astring[0].equals(null) || astring[0].equals("")) {
						return;
					}
				} catch (IndexOutOfBoundsException e) {
					return;
				}
				if (!(icommandsender instanceof EntityPlayerMP)) {
					return;
				}
<<<<<<< refs/remotes/origin/master
				if (CloudStorage.INSTANCE.getPlayer(astring[0]) == null) {
					return;
				}
				CloudStorage.INSTANCE.sharedClouds.remove(astring[0]);
				EntityPlayer player = CloudStorage.INSTANCE.getPlayer(astring[0]);
				player.addChatMessage(new ChatComponentText("You have been kicked from "
						+ ((EntityPlayerMP) icommandsender).getDisplayName() + "'s cloud."));
				((EntityPlayerMP) icommandsender)
						.addChatMessage(new ChatComponentText("Player " + astring[0] + " kicked."));
				// ((EntityPlayerMP)icommandsender).openGui(INSTANCE,
				// GUI_BROWSE, ((EntityPlayerMP)icommandsender).worldObj, 0, 0,
				// 0);
=======
				if (CloudStorage.INSTANCE.sharedClouds.get(astring[0]).equals(((EntityPlayerMP) icommandsender).getGameProfile().getName())) {
					return;
				}
				CloudStorage.INSTANCE.sharedClouds.remove(astring[0]);
				((EntityPlayerMP) icommandsender)
						.addChatMessage(new ChatComponentText("Player " + astring[0] + " kicked."));
>>>>>>> v60.0.0! yay!
			}

			@Override
			public String getCommandUsage(ICommandSender icommandsender) {
				return "/cloudkick <name>";
			}

			@Override
			public boolean canCommandSenderUseCommand(ICommandSender par1iCommandSender) {
				return par1iCommandSender instanceof EntityPlayer;
			}

			@Override
			public String getCommandName() {
				return "cloudkick";
			}
		});
		evt.registerServerCommand(new CommandBase() {
			@Override
			public void processCommand(ICommandSender icommandsender, String[] astrinag) {
				if (!(icommandsender instanceof EntityPlayerMP)) {
					return;
				}
				if ((!CloudStorage.INSTANCE.sharedClouds
						.containsKey(((EntityPlayerMP) icommandsender).getGameProfile().getName()))) {
					((EntityPlayerMP) icommandsender)
							.addChatMessage(new ChatComponentText("You are not sharing someone else's cloud!"));
					return;
				}
<<<<<<< refs/remotes/origin/master
				EntityPlayer player = CloudStorage.INSTANCE.getPlayer(CloudStorage.INSTANCE.sharedClouds
						.get(((EntityPlayerMP) icommandsender).getGameProfile().getName()));
				CloudStorage.INSTANCE.sharedClouds.remove(((EntityPlayerMP) icommandsender).getGameProfile().getName());
				player.addChatMessage(new ChatComponentText(
						"" + ((EntityPlayerMP) icommandsender).getDisplayName() + " has left your cloud."));
=======
				CloudStorage.INSTANCE.sharedClouds.remove(((EntityPlayerMP) icommandsender).getGameProfile().getName());
>>>>>>> v60.0.0! yay!
				((EntityPlayerMP) icommandsender)
						.addChatMessage(new ChatComponentText("You successfully left someone's cloud"));
			}

			@Override
			public String getCommandUsage(ICommandSender icommandsender) {
				return "/cloudleave";
			}

			@Override
			public boolean canCommandSenderUseCommand(ICommandSender par1iCommandSender) {
				return par1iCommandSender instanceof EntityPlayer;
			}

			@Override
			public String getCommandName() {
				return "cloudleave";
			}
		});
<<<<<<< refs/remotes/origin/master
=======
		evt.registerServerCommand(new CommandBase() {
			@Override
			public void processCommand(ICommandSender icommandsender, String[] astring) {
				try {
					if (astring[0].equals(null) || astring[0].equals("")) {
						return;
					}
				} catch (IndexOutOfBoundsException e) {
					return;
				}
				if (!(icommandsender instanceof EntityPlayerMP)) {
					return;
				}
				CloudStorage.INSTANCE.sharedClouds.put(astring[0], ((EntityPlayerMP) icommandsender).getDisplayName());
				((EntityPlayerMP) icommandsender).addChatMessage(new ChatComponentText("Cloud shared."));
			}

			@Override
			public String getCommandUsage(ICommandSender icommandsender) {
				return "/cloudforceshare <name>";
			}

			@Override
			public boolean canCommandSenderUseCommand(ICommandSender par1iCommandSender) {
				return par1iCommandSender instanceof EntityPlayer;
			}

			@Override
			public String getCommandName() {
				return "cloudforceshare";
			}
		});
>>>>>>> v60.0.0! yay!
		CloudStorage.INSTANCE.readSharesFromFile(evt.getServer().getWorldName());
	}

	public static String worldName = "";

	@EventHandler
	public void onServerStop(FMLServerStoppingEvent e) {
		
		try {
			saveSharesToFile(CloudStorage.INSTANCE.sharedClouds, CloudStorage.INSTANCE.worldName);
		} catch (FileNotFoundException e1) {
			CloudStorage.INSTANCE.log.log(Level.ERROR, "Failed to save shared clouds: java.io.FileNotFoundException");
			CloudStorage.INSTANCE.log.log(Level.ERROR, e1.getMessage());
		} catch (UnsupportedEncodingException e1) {
			CloudStorage.INSTANCE.log.log(Level.ERROR,
					"Failed to save shared clouds: java.io.UnsupportedEncodingException");
		}
	}

	public static void saveSharesToFile(HashMap<String, String> map, String name)
			throws FileNotFoundException, UnsupportedEncodingException {
<<<<<<< refs/remotes/origin/master
		PrintWriter writer = new PrintWriter(getWorkingFolder() + getWorkingFolder().separator + name
				+ getWorkingFolder().separator + "sharedclouds.txt", "UTF-8");
		Iterator it = map.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			// Logic
			writer.println(key + "Â¬"/* Random char nobody has in their name */ + map.get(key));
			// System.out.println(key + " = " + map.get(key));
			/*
			 * if (map.get(key) instanceof HashMap) iterateHashMap((HashMap)
			 * map.get(key));
			 */
=======
		File f = new File(getWorkingFolder() + getWorkingFolder().separator + name + "-sharedclouds.txt");
		f.delete();
		PrintWriter writer = new PrintWriter(new FileOutputStream(getWorkingFolder() + getWorkingFolder().separator + name + "-sharedclouds.txt", false));
		Iterator it = map.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			writer.println(key + "¬"/* Random char nobody has in their name */ + map.get(key));
>>>>>>> v60.0.0! yay!
		}

		writer.close();
	}

	public static void readSharesFromFile(String name) {

		CloudStorage.INSTANCE.worldName = name;

		try {
<<<<<<< refs/remotes/origin/master
			File file = new File(getWorkingFolder() + getWorkingFolder().separator + name + getWorkingFolder().separator
					+ "sharedclouds.txt");
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			// StringBuffer stringBuffer = new StringBuffer();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				// stringBuffer.append(line);
				// stringBuffer.append("\n");
				String[] split = line.split("Â¬");
				CloudStorage.INSTANCE.sharedClouds.put(split[0], split[1]);
			}
			fileReader.close();
			// System.out.println("Contents of file:");
			// System.out.println(stringBuffer.toString());
=======
			File file = new File(getWorkingFolder() + getWorkingFolder().separator + name + "-sharedclouds.txt");
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String[] split = line.split("¬");
				CloudStorage.INSTANCE.sharedClouds.put(split[0], split[1]);
			}
			fileReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("file sharedclouds not found, ignoring");
>>>>>>> v60.0.0! yay!
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static File getWorkingFolder() {
		File toBeReturned;
		try {
			if (FMLCommonHandler.instance().getSide().isClient()) {
				toBeReturned = Minecraft.getMinecraft().mcDataDir;
			} else {
				toBeReturned = MinecraftServer.getServer().getFile("");
			}
			return toBeReturned;

		} catch (Exception ex) {
			CloudStorage.INSTANCE.log.log(Level.ERROR, "Couldn't get the path to the mod directory.");
		}
		return null;
	}

	public EntityPlayer getPlayer(String name) {

		ServerConfigurationManager server = MinecraftServer.getServer().getConfigurationManager();
		ArrayList pl = (ArrayList) server.playerEntityList;
		ListIterator li = pl.listIterator();

		while (li.hasNext()) {

			EntityPlayer p = (EntityPlayer) li.next();
			if (p.getGameProfile().getName().equals(name)) {

				return p;

			}

		}
		return null;
	}
}
