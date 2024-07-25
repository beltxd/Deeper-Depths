package net.smileycorp.deeperdepths.common.entities;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.smileycorp.deeperdepths.common.Constants;
import net.smileycorp.deeperdepths.common.DeeperDepths;

public class DeeperDepthsEntities
{
    public static int id;

    public static void registerEntities()
    {
        registerEntity("bogged", EntityBogged.class, ++id, 80, 9084018, 3231003);
    }

    private static void registerEntity(String name, Class<? extends Entity> entity, int id, int range, int color1, int color2)
    { EntityRegistry.registerModEntity(new ResourceLocation(Constants.MODID, name), entity, Constants.MODID + "." + name, id, DeeperDepths.instance, range, 1, true, color1, color2); }
}
