package net.smileycorp.deeperdepths.common;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.smileycorp.deeperdepths.animation.AnimationMessage;
import net.smileycorp.deeperdepths.common.entities.DeeperDepthsEntities;

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
    
    public void postInit(FMLPostInitializationEvent event) {
    }
    
    public void serverStart(FMLServerStartingEvent event) {
    }


    public void handleAnimationPacket(int entityId, int index) {

    }
    
}
