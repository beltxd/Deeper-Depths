package net.smileycorp.deeperdepths.common.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.smileycorp.deeperdepths.common.DeeperDepthsSoundEvents;
import net.smileycorp.deeperdepths.common.entities.EntityWindCharge;

public class ItemWindCharge extends ItemDeeperDepths
{
    public ItemWindCharge()
    { super("wind_charge"); }

    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
    {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        if (!playerIn.capabilities.isCreativeMode)
        { itemstack.shrink(1); }

        worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, DeeperDepthsSoundEvents.WIND_CHARGE_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
        if (!worldIn.isRemote)
        {
            EntityWindCharge entitywindcharge = new EntityWindCharge(worldIn, playerIn);
            entitywindcharge.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.5F, 1.0F);
            entitywindcharge.setBurstPower(1.1F);
            entitywindcharge.setBurstRange(3F);
            worldIn.spawnEntity(entitywindcharge);
        }

        playerIn.addStat(StatList.getObjectUseStats(this));
        return new ActionResult(EnumActionResult.SUCCESS, itemstack);
    }
}