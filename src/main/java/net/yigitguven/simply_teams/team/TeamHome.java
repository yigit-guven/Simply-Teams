package net.yigitguven.simply_teams.team;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class TeamHome {
    private final ResourceLocation dimension;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public TeamHome(ResourceLocation dimension, double x, double y, double z, float yaw, float pitch) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static TeamHome fromPlayer(ServerPlayer player) {
        ResourceLocation dim = player.level().dimension().location();
        return new TeamHome(dim, player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
    }

    public ResourceLocation getDimension() {
        return dimension;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void teleport(ServerPlayer player) {
        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, dimension);
        ServerLevel targetLevel = player.server.getLevel(dimKey);
        if (targetLevel != null) {
            player.teleportTo(targetLevel, x, y, z, yaw, pitch);
        }
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("dimension", dimension.toString());
        tag.putDouble("x", x);
        tag.putDouble("y", y);
        tag.putDouble("z", z);
        tag.putFloat("yaw", yaw);
        tag.putFloat("pitch", pitch);
        return tag;
    }

    public static TeamHome fromNBT(CompoundTag tag) {
        if (tag == null || !tag.contains("dimension")) return null;
        ResourceLocation dim = ResourceLocation.parse(tag.getString("dimension"));
        double x = tag.getDouble("x");
        double y = tag.getDouble("y");
        double z = tag.getDouble("z");
        float yaw = tag.getFloat("yaw");
        float pitch = tag.getFloat("pitch");
        return new TeamHome(dim, x, y, z, yaw, pitch);
    }
}

