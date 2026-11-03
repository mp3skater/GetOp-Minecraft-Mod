package net.mp3skater.getop.client.model;


import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.level.block.Block;
import net.mp3skater.getop.GetOP;
import net.mp3skater.getop.block.entity.custom.AltarBlockEntity;
import software.bernie.geckolib3.model.AnimatedGeoModel;


public class AltarBlockModel extends AnimatedGeoModel<AltarBlockEntity> {


    @Override
    public ResourceLocation getModelLocation(AltarBlockEntity animatable) {
        Block block = animatable.getBlockState().getBlock();
        String blockName = block.getRegistryName().getPath();

// Lädt automatisch z.B. "geo/fire_altar.geo.json", "geo/ice_altar.geo.json" usw.
        return new ResourceLocation(GetOP.MOD_ID, "geo/" + blockName + ".geo.json");

    }


    @Override
    public ResourceLocation getTextureLocation(AltarBlockEntity animatable) {
        Block block = animatable.getBlockState().getBlock();
        String blockName = block.getRegistryName().getPath();


// Lädt automatisch die passende Textur dazu
        return new ResourceLocation(GetOP.MOD_ID, "textures/block/" + blockName + ".png");

    }


    @Override
    public ResourceLocation getAnimationFileLocation(AltarBlockEntity animatable) {

// Falls alle dieselbe Animation nutzen oder auch dynamisch:
        return new ResourceLocation(GetOP.MOD_ID, "animations/altar.animation.json");

    }

}