package com.simibubi.create.content.contraptions.components.structureMovement;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.WorldAttached;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class ContraptionHandler {

	/* Global map of loaded contraptions */

	public static WorldAttached<List<WeakReference<AbstractContraptionEntity>>> loadedContraptions;
	static WorldAttached<List<AbstractContraptionEntity>> queuedAdditions;

	static {
		loadedContraptions = new WorldAttached<>(ArrayList::new);
		queuedAdditions = new WorldAttached<>(() -> ObjectLists.synchronize(new ObjectArrayList<>()));
	}

	public static void tick(World world) {
		List<WeakReference<AbstractContraptionEntity>> list = loadedContraptions.get(world);
		List<AbstractContraptionEntity> queued = queuedAdditions.get(world);

		for (AbstractContraptionEntity contraptionEntity : queued)
			list.add(new WeakReference<>(contraptionEntity));
		queued.clear();

		for (Iterator<WeakReference<AbstractContraptionEntity>> iterator = list.iterator(); iterator.hasNext();) {
			WeakReference<AbstractContraptionEntity> weakReference = iterator.next();
			AbstractContraptionEntity contraptionEntity = weakReference.get();
			if (contraptionEntity == null || !contraptionEntity.isAlive()) {
				iterator.remove();
				continue;
			}
			ContraptionCollider.collideEntities(contraptionEntity);
		}
	}

	public static void addSpawnedContraptionsToCollisionList(Entity entity, World world) {
		if (entity instanceof AbstractContraptionEntity)
			queuedAdditions.get(world)
				.add((AbstractContraptionEntity) entity);
	}

	public static void entitiesWhoJustDismountedGetSentToTheRightLocation(LivingEntity entityLiving, World world) {
		if (world.isRemote)
			return;
		CompoundNBT data = entityLiving.getPersistentData();
		if (!data.contains("ContraptionDismountLocation"))
			return;
		Vec3d position = VecHelper.readNBT(data.getList("ContraptionDismountLocation", NBT.TAG_DOUBLE));
		if (entityLiving.getRidingEntity() == null)
			entityLiving.setPositionAndUpdate(position.x, position.y, position.z);
		data.remove("ContraptionDismountLocation");
	}

}
