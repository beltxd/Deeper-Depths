package net.smileycorp.deeperdepths.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCopperBulb extends BlockDeeperDepths {
    
    public static final PropertyBool LIT = PropertyBool.create("lit");
    public static final PropertyBool POWERED = PropertyBool.create("powered");
    private final boolean waxed;
    
    public BlockCopperBulb(boolean waxed) {
        super((waxed ? "Waxed_" : "") + "Copper_Bulb", Material.IRON, 3, 6, 1);
        setDefaultState(getBlockState().getBaseState().withProperty(BlockCopper.WEATHER_STAGE, EnumWeatherStage.NORMAL)
                .withProperty(LIT, false).withProperty(POWERED, false));
        this.waxed = waxed;
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockCopper.WEATHER_STAGE, LIT, POWERED);
    }
    
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getStateFromMeta(placer.getHeldItem(hand).getMetadata());
    }
    
    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        checkPower(state, world, pos);
    }
    
    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (world.isRemote) return;
        checkPower(state, world, pos);
    }
    
    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(BlockCopper.WEATHER_STAGE).getMapColor();
    }
    
    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BlockCopper.WEATHER_STAGE).ordinal() + (state.getValue(LIT) ? 4 : 0) + (state.getValue(POWERED) ? 8 : 0);
    }
    
    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(this, 1, getMetaFromState(state) % 8);
    }
    
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BlockCopper.WEATHER_STAGE, EnumWeatherStage.values()[meta % 4])
                .withProperty(LIT, meta % 8 >= 4).withProperty(POWERED, meta >= 8);
    }
    
    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        for (int i = 0; i < 4; i++) items.add(new ItemStack(this, 1, i));
    }
    
    @Override
    public int getMaxMeta() {
        return 4;
    }
    
    @Override
    public String byMeta(int meta) {
        StringBuilder builder = new StringBuilder();
        if (waxed) builder.append("waxed_");
        if (meta % 4 > 0) builder.append(EnumWeatherStage.values()[meta % 4].getName() + "_");
        if (meta > 8) builder.append("lit_");
        return builder + "copper_bulb";
    }
    
    @Override
    public boolean shouldCheckWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return false;
    }
    
    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (!state.getValue(LIT)) return 0;
        switch (state.getValue(BlockCopper.WEATHER_STAGE)) {
            case EXPOSED:
                return 12;
            case WEATHERED:
                return 8;
            case OXIDIZED:
                return 4;
            default:
                return 15;
        }
    }
    
    public void checkPower(IBlockState state, World world, BlockPos pos) {
        if (world.isRemote) return;
        if (world.isBlockPowered(pos) == state.getValue(POWERED)) return;
        if (state.getValue(POWERED)) world.setBlockState(pos, state.withProperty(POWERED, false), 3);
        else {
            world.setBlockState(pos, state.withProperty(LIT, !state.getValue(LIT)).withProperty(POWERED, true), 3);
        }
    }
    
    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }
    
    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        return state.getValue(LIT) ? 15 : 0;
    }
    
    public boolean isWaxed() {
        return waxed;
    }
    
}
