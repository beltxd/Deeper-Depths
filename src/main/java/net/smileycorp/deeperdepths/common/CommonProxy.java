package net.smileycorp.deeperdepths.common;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.IProjectile;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.deeperdepths.animation.AnimationMessage;
import net.smileycorp.deeperdepths.common.entities.DeeperDepthsEntities;
import net.smileycorp.deeperdepths.common.entities.EntityWindCharge;
import net.smileycorp.deeperdepths.common.items.DeeperDepthsItems;

public class CommonProxy {
    
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new DeeperDepthsEventHandler());
        //LootTableList.register(Constants.loc("vault"));
        //LootTableList.register(Constants.loc("ominous_vault"));
        //LootTableList.register(Constants.loc("entities/bogged"));
        DeeperDepthsEntities.registerEntities();
        //Registers Entity Spawns even though I think we'll only have one lmao
        DeeperDepthsEntities.registerEntitySpawns();
        DeeperDepthsLootTables.registerLootTables();
    }
    
    public void init(FMLInitializationEvent event) {
        int packetId = 0;
        DeeperDepths.network = NetworkRegistry.INSTANCE.newSimpleChannel(Constants.MODID);
        DeeperDepths.network.registerMessage(AnimationMessage.Handler.class, AnimationMessage.class, packetId++, Side.SERVER);
    }
    
    public void postInit(FMLPostInitializationEvent event)
    {
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(DeeperDepthsItems.WIND_CHARGE, new BehaviorProjectileDispense()
        {
            /**
             * Return the projectile entity spawned by this dispense behavior.
             */
            protected IProjectile getProjectileEntity(World worldIn, IPosition position, ItemStack stackIn)
            {
                NBTTagCompound nbttagcompound = stackIn.getTagCompound();

                EntityWindCharge entitywindcharge = new EntityWindCharge(worldIn, position.getX(), position.getY(), position.getZ());
                entitywindcharge.setBurstPower(nbttagcompound != null && nbttagcompound.hasKey("BurstPower") ? nbttagcompound.getFloat("BurstPower") : 0.9F);
                entitywindcharge.setBurstRange(nbttagcompound != null && nbttagcompound.hasKey("BurstRange") ? nbttagcompound.getFloat("BurstRange") : 2.5F);
                entitywindcharge.setBurstInteractRange(nbttagcompound != null && nbttagcompound.hasKey("BurstInteractRange") ? nbttagcompound.getFloat("BurstInteractRange") : 2.5F);

                return entitywindcharge;
            }
        });
    }
    
    public void serverStart(FMLServerStartingEvent event) {
    }


    public void handleAnimationPacket(int entityId, int index) {

    }
    
}
