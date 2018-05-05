package com.caucraft.mciguiv3.util;

import com.caucraft.mciguiv3.launch.Launcher;
import java.awt.image.BufferedImage;

/**
 *
 * @author caucow
 */
public class ImageResources {
    
    public static BufferedImage MCIGUI;
    public static BufferedImage MCIGUI_UPDATE;
    
    public static BufferedImage DIRT;
    public static BufferedImage STONE;
    public static BufferedImage BEDROCK;
    public static BufferedImage COAL_ORE;
    public static BufferedImage IRON_ORE;
    public static BufferedImage REDSTONE_ORE;
    public static BufferedImage GOLD_ORE;
    public static BufferedImage LAPIS_ORE;
    public static BufferedImage DIAMOND_ORE;
    public static BufferedImage EMERALD_ORE;
    public static BufferedImage GRASS_BLOCK;
    public static BufferedImage TNT_BLOCK;
    public static BufferedImage REDSTONE;
    public static BufferedImage PLANKS_OAK;
    public static BufferedImage PLANKS_BIRCH;
    public static BufferedImage PLANKS_BIG_OAK;
    
    static {
        MCIGUI = Launcher.getImageResource("/com/caucraft/mciguiv3/resources/MCIGUI.png");
        MCIGUI_UPDATE = Launcher.getImageResource("/com/caucraft/mciguiv3/resources/MCIGUI_update.png");
        DIRT = Launcher.getImageResource("/com/caucraft/mciguiv3/resources/dirt.png");
        STONE = Launcher.getImageResource("/com/caucraft/mciguiv3/resources/stone.png");
        BEDROCK = Launcher.getImageResource("/com/caucraft/mciguiv3/resources/bedrock.png");
        COAL_ORE = Launcher.getImageResource("/com/caucraft/mciguiv3/resources/coal_ore.png");
        IRON_ORE = Launcher.getImageResource("/com/caucraft/mciguiv3/resources/iron_ore.png");
        REDSTONE_ORE = Launcher.getImageResource("/com/caucraft/mciguiv3/resources/redstone_ore.png");
        GOLD_ORE = Launcher.getImageResource("/com/caucraft/mciguiv3/resources/gold_ore.png");
        LAPIS_ORE = Launcher.getImageResource("/com/caucraft/mciguiv3/resources/lapis_ore.png");
        DIAMOND_ORE = Launcher.getImageResource("/com/caucraft/mciguiv3/resources/diamond_ore.png");
        EMERALD_ORE = Launcher.getImageResource("/com/caucraft/mciguiv3/resources/emerald_ore.png");
        GRASS_BLOCK = Launcher.getImageResource("/com/caucraft/mciguiv3/resources/grass_block.png");
        TNT_BLOCK = Launcher.getImageResource("/com/caucraft/mciguiv3/resources/tnt_block.png");
        REDSTONE = Launcher.getImageResource("/com/caucraft/mciguiv3/resources/redstone.png");
        PLANKS_OAK = Launcher.getImageResource("/com/caucraft/mciguiv3/resources/planks_oak.png");
        PLANKS_BIRCH = Launcher.getImageResource("/com/caucraft/mciguiv3/resources/planks_birch.png");
        PLANKS_BIG_OAK = Launcher.getImageResource("/com/caucraft/mciguiv3/resources/planks_big_oak.png");
    }
    
}
