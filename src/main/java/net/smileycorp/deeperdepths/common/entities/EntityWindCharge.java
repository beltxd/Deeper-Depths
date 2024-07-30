package net.smileycorp.deeperdepths.common.entities;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.smileycorp.deeperdepths.common.DeeperDepthsSoundEvents;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EntityWindCharge extends EntityThrowable
{
    private static final DataParameter<Float> BURST_RANGE = EntityDataManager.createKey(EntityWindCharge.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> BURST_INTENSITY = EntityDataManager.createKey(EntityWindCharge.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> DO_FALL_REDUCTION = EntityDataManager.createKey(EntityWindCharge.class, DataSerializers.BOOLEAN);
    /** Multiplies the movement speed by this when reflected. Yes, this WILL stack */
    int reflectSpeedMult = 3;
    /** Sets a certain entity to be completely immune. Used so Breeze don't get knockback from their own Wind Charges. */
    protected EntityLivingBase knockbackImmune;
    /** A lot of worthless things copied from EntityThrowable. */
    private int xTile;
    private int yTile;
    private int zTile;
    private Block inTile;
    private int ticksInGround;
    private int ticksInAir;
    public Entity ignoreEntity;
    private int ignoreTime;

    //boolean playerFallReduction;

    public EntityWindCharge(World worldIn, EntityLivingBase throwerIn)
    { super(worldIn, throwerIn); }

    public EntityWindCharge(World worldIn, EntityLivingBase throwerIn, EntityLivingBase knockbackImmuneIn)
    {
        super(worldIn, throwerIn);
        knockbackImmune = knockbackImmuneIn;
    }

    public EntityWindCharge(World worldIn, double x, double y, double z)
    { super(worldIn, x, y, z); }

    public EntityWindCharge(World worldIn)
    {
        super(worldIn);

        this.setSize(0.3125F, 0.3125F);
    }

    protected void entityInit()
    {
        super.entityInit();

        this.dataManager.register(BURST_RANGE, 10F);
        this.dataManager.register(BURST_INTENSITY, 0.9F);
        this.dataManager.register(DO_FALL_REDUCTION, Boolean.FALSE);
    }


    public boolean canBeCollidedWith() {
        return true;
    }

    public float getCollisionBorderSize() {
        return 1.0F;
    }

    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (this.isEntityInvulnerable(source))
        { return false; }
        else
        {
            this.markVelocityChanged();
            if (source.getTrueSource() != null)
            {
                Vec3d vec3d = source.getTrueSource().getLookVec();
                if (vec3d != null)
                {
                    this.motionX = vec3d.x * reflectSpeedMult;
                    this.motionY = vec3d.y * reflectSpeedMult;
                    this.motionZ = vec3d.z * reflectSpeedMult;
                }

                if (source.getTrueSource() instanceof EntityLivingBase)
                { this.thrower = (EntityLivingBase)source.getTrueSource(); }

                return true;
            }
            else
            { return false; }
        }
    }

    /** Forces it to Impact. This is used so it can explode instantly. 'null' is supported.  */
    public void forceExplode(@Nullable RayTraceResult result)
    { this.onImpact(result); }

    protected void onImpact(RayTraceResult result)
    {
        SoundEvent burstSound = knockbackImmune instanceof EntityBreeze ? DeeperDepthsSoundEvents.BREEZE_WIND_BURST : DeeperDepthsSoundEvents.WIND_CHARGE_WIND_BURST;

        this.playSound(burstSound, 1, 1);


        if (!this.world.isRemote)
        {
            if (result != null && result.entityHit != null)
            {
                result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), (float)1);
                if(this.isBurning()) result.entityHit.setFire(5);
            }

            preformKnockbackEffects();
            checkBlockInteraction(this.getPosition());

            this.world.setEntityState(this, (byte)3);
            this.setDead();
        }
        else
        {
            for (int i = 0; i < 20; i++)
            {
                this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 0, 0,0,0);
            }
        }
    }

    /** No gravity, keep going forward!! */
    protected float getGravityVelocity() {
        return 0.0F;
    }

    /** Mostly from Minecraft's Explosion Code, as Wind Charges use the same code, but altered to remove damage. */
    public void preformKnockbackEffects()
    {
        float scale = getBurstRange();
        double knockbackStrength = getBurstPower();
        float k = MathHelper.floor(this.posX - (double) scale - 1.0);
        float l = MathHelper.floor(this.posX + (double) scale + 1.0);
        int i2 = MathHelper.floor(this.posY - (double) scale - 1.0);
        int i1 = MathHelper.floor(this.posY + (double) scale + 1.0);
        int j2 = MathHelper.floor(this.posZ - (double) scale - 1.0);
        int j1 = MathHelper.floor(this.posZ + (double) scale + 1.0);
        List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB((double) k, (double) i2, (double) j2, (double) l, (double) i1, (double) j1));
        Vec3d vec3d = new Vec3d(this.posX, this.posY, this.posZ);

        for (Entity entity : list)
        {
            if (!entity.isImmuneToExplosions() && entity != this.knockbackImmune)
            {
                double d12 = entity.getDistance(this.posX, this.posY, this.posZ) / (double) scale;
                if (d12 <= 1.0)
                {
                    double dx = entity.posX - this.posX;
                    double dy = entity.posY + (double) entity.getEyeHeight() - this.posY;
                    double dz = entity.posZ - this.posZ;
                    double distance = (double) MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
                    if (distance != 0.0) {
                        dx /= distance;
                        dy /= distance;
                        dz /= distance;
                        double blockStoppage = (double) this.checkBlockBlocking(vec3d, entity.getEntityBoundingBox());
                        double kmult = (knockbackStrength - d12) * blockStoppage;

                        entity.motionX += dx * kmult;
                        entity.motionY += dy * kmult;
                        entity.motionZ += dz * kmult;
                        entity.velocityChanged = true;
                    }
                }
            }
        }
    }

    /** Mostly from getBlockDensity, but it properly ignores non-solid blocks. Checks how much of the entity is exposed to the explosion. */
    private float checkBlockBlocking(Vec3d vec, AxisAlignedBB bb)
    {
        double dx = 1.0 / ((bb.maxX - bb.minX) * 2.0 + 1.0);
        double dy = 1.0 / ((bb.maxY - bb.minY) * 2.0 + 1.0);
        double dz = 1.0 / ((bb.maxZ - bb.minZ) * 2.0 + 1.0);
        double d3 = (1.0 - Math.floor(1.0 / dx) * dx) / 2.0;
        double d4 = (1.0 - Math.floor(1.0 / dz) * dz) / 2.0;
        if (dx >= 0.0 && dy >= 0.0 && dz >= 0.0)
        {
            int j2 = 0;
            int k2 = 0;
            for(float fx = 0.0F; fx <= 1.0F; fx = (float)((double)fx + dx))
            {
                for(float fy = 0.0F; fy <= 1.0F; fy = (float)((double)fy + dy))
                {
                    for(float fz = 0.0F; fz <= 1.0F; fz = (float)((double)fz + dz))
                    {
                        double d5 = bb.minX + (bb.maxX - bb.minX) * (double)fx;
                        double d6 = bb.minY + (bb.maxY - bb.minY) * (double)fy;
                        double d7 = bb.minZ + (bb.maxZ - bb.minZ) * (double)fz;
                        /* Might be a good idea to fork this logic, to detect and activate blocks later on. */
                        RayTraceResult result = this.world.rayTraceBlocks(new Vec3d(d5 + d3, d6, d7 + d4), vec, false, true, false);

                        if (result == null)
                        { ++j2; }
                        ++k2;
                    }
                }
            }
            return (float)j2 / (float)k2;
        } else
        { return 0.0F; }
    }

    /** Ray-trace checks surrounding blocks within range. */
    private void checkBlockInteraction(BlockPos pos)
    {
        int radius = (int)this.getBurstRange();
        /* Uses a list, so the same block isn't interacted with multiple times. */
        List<BlockPos> processedBlocks = new ArrayList<>();

        for (int h1 = -radius; h1 <= radius; h1++)
        {
            for (int i1 = -radius; i1 <= radius; i1++)
            {
                for (int j1 = -radius; j1 <= radius; j1++)
                {
                    BlockPos tPos = pos.add(h1, i1, j1);
                    RayTraceResult result = this.world.rayTraceBlocks(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), new Vec3d(tPos.getX(), tPos.getY(), tPos.getZ()));
                    if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK)
                    {
                        if (processedBlocks.contains(result.getBlockPos())) { continue; }
                        activateBlocks(result.getBlockPos());
                        processedBlocks.add(result.getBlockPos());
                    }
                }
            }
        }
    }

    /** Simply activates blocks. */
    private void activateBlocks(BlockPos pos)
    {
        Block block = this.world.getBlockState(pos).getBlock();

        if (block instanceof BlockButton || block instanceof BlockTrapDoor || block instanceof BlockDoor || block instanceof BlockLever)
        { block.onBlockActivated(this.world, pos, this.world.getBlockState(pos), null, EnumHand.MAIN_HAND, EnumFacing.UP, 0.5F, 0.5F, 0.5F); }
        /* Using `onBlockActived` causes Fence Gates to crash? Investigate later. */
        else if (block instanceof BlockFenceGate) {}
    }

    /** onUpdate needs to be overridden to handle complex changes. */
    @Override
    public void onUpdate()
    {
        this.lastTickPosX = this.posX;
        this.lastTickPosY = this.posY;
        this.lastTickPosZ = this.posZ;
        if (!this.world.isRemote) { this.setFlag(6, this.isGlowing()); }
        this.onEntityUpdate();

        if (this.throwableShake > 0)
        { --this.throwableShake; }

        if (this.inGround)
        {
            if (this.world.getBlockState(new BlockPos(this.xTile, this.yTile, this.zTile)).getBlock() == this.inTile)
            {
                ++this.ticksInGround;
                if (this.ticksInGround == 1200)
                { this.setDead(); }

                return;
            }

            this.inGround = false;
            this.motionX *= (double)(this.rand.nextFloat() * 0.2F);
            this.motionY *= (double)(this.rand.nextFloat() * 0.2F);
            this.motionZ *= (double)(this.rand.nextFloat() * 0.2F);
            this.ticksInGround = 0;
            this.ticksInAir = 0;
        }
        else
        { ++this.ticksInAir; }

        Vec3d vec3d = new Vec3d(this.posX, this.posY, this.posZ);
        Vec3d vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
        RayTraceResult raytraceresult = this.world.rayTraceBlocks(vec3d1, vec3d, false, true, false);
        vec3d = new Vec3d(this.posX, this.posY, this.posZ);
        vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
        if (raytraceresult != null)
        { vec3d1 = new Vec3d(raytraceresult.hitVec.x, raytraceresult.hitVec.y, raytraceresult.hitVec.z); }

        Entity entity = null;
        List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0));
        double d0 = 0.0;
        boolean flag = false;

        for (Entity value : list)
        {
            Entity entity1 = (Entity) value;
            if (entity1.canBeCollidedWith())
            {
                if (entity1 == this.ignoreEntity)
                { flag = true; }
                else if (this.thrower != null && this.ticksExisted < 2 && this.ignoreEntity == null)
                {
                    this.ignoreEntity = entity1;
                    flag = true;
                }
                else
                {
                    flag = false;
                    AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(0.30000001192092896);
                    RayTraceResult raytraceresult1 = axisalignedbb.calculateIntercept(vec3d, vec3d1);
                    if (raytraceresult1 != null)
                    {
                        double d1 = vec3d.squareDistanceTo(raytraceresult1.hitVec);
                        if (d1 < d0 || d0 == 0.0)
                        {
                            entity = entity1;
                            d0 = d1;
                        }
                    }
                }
            }
        }

        if (this.ignoreEntity != null)
        {
            if (flag)
            { this.ignoreTime = 2; }
            else if (this.ignoreTime-- <= 0)
            { this.ignoreEntity = null; }
        }

        if (entity != null)
        { raytraceresult = new RayTraceResult(entity); }

        if (raytraceresult != null)
        {
            if (raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK && this.world.getBlockState(raytraceresult.getBlockPos()).getBlock() == Blocks.PORTAL)
            { this.setPortal(raytraceresult.getBlockPos()); }
            else if (!ForgeEventFactory.onProjectileImpact(this, raytraceresult))
            { this.onImpact(raytraceresult); }
        }

        this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;
        float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 57.29577951308232);

        for(this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f) * 57.29577951308232); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
        { }

        while(this.rotationPitch - this.prevRotationPitch >= 180.0F)
        { this.prevRotationPitch += 360.0F; }

        while(this.rotationYaw - this.prevRotationYaw < -180.0F)
        { this.prevRotationYaw -= 360.0F; }

        while(this.rotationYaw - this.prevRotationYaw >= 180.0F)
        { this.prevRotationYaw += 360.0F; }

        this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
        this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
        float f1 = 1.0F;
        float f2 = this.getGravityVelocity();

        this.motionX *= (double)f1;
        this.motionY *= (double)f1;
        this.motionZ *= (double)f1;
        if (!this.hasNoGravity())
        { this.motionY -= (double)f2; }

        this.setPosition(this.posX, this.posY, this.posZ);
    }

    public void setBurstRange(float size)
    { this.dataManager.set(BURST_RANGE, Float.valueOf(size)); }

    public float getBurstRange()
    { return ((Float)this.dataManager.get(BURST_RANGE)).floatValue(); }

    public void setBurstPower(float size)
    { this.dataManager.set(BURST_INTENSITY, Float.valueOf(size)); }

    public float getBurstPower()
    { return ((Float)this.dataManager.get(BURST_INTENSITY)).floatValue(); }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);

        this.dataManager.set(BURST_RANGE, Float.valueOf(compound.getFloat("BurstRange")));
        this.dataManager.set(BURST_INTENSITY, Float.valueOf(compound.getFloat("BurstPower")));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);

        compound.setFloat("BurstRange", getBurstRange());
        compound.setFloat("BurstPower", getBurstPower());
    }
}