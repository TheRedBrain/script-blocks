package com.github.theredbrain.scriptblocks.client.render.model;

import com.github.theredbrain.scriptblocks.ScriptBlocksMod;
import com.github.theredbrain.scriptblocks.entity.mob.BossEntity;
import mod.azure.azurelib.model.GeoModel;
import net.minecraft.util.Identifier;

public class BossEntityModel extends GeoModel<BossEntity> {
    Identifier defaultModelIdentifier = ScriptBlocksMod.identifier("spawner_bound_entity/default_spawner_bound_entity");
    Identifier defaultTextureIdentifier = ScriptBlocksMod.identifier("spawner_bound_entity/default_spawner_bound_entity");;
    Identifier defaultAnimationsIdentifier = ScriptBlocksMod.identifier("spawner_bound_entity/default_spawner_bound_entity");;
    @Override
    public Identifier getModelResource(BossEntity animatable) {
        Identifier modelIdentifier = Identifier.tryParse(animatable.getModelIdentifierString());
        if (modelIdentifier == null) {
            modelIdentifier = defaultModelIdentifier;
        }
        return new Identifier(modelIdentifier.getNamespace(), "geo/entity/" + modelIdentifier.getPath() + ".geo.json");
    }

    @Override
    public Identifier getTextureResource(BossEntity animatable) {
        Identifier textureIdentifier = Identifier.tryParse(animatable.getTextureIdentifierString());
        if (textureIdentifier == null) {
            textureIdentifier = defaultTextureIdentifier;
        }
        return new Identifier(textureIdentifier.getNamespace(), "textures/entity/" + textureIdentifier.getPath() + ".png");
    }

    @Override
    public Identifier getAnimationResource(BossEntity animatable) {
        Identifier animationIdentifier = Identifier.tryParse(animatable.getAnimationIdentifierString());
        if (animationIdentifier == null) {
            animationIdentifier = defaultAnimationsIdentifier;
        }
        return new Identifier(animationIdentifier.getNamespace(), "animations/entity/" + animationIdentifier.getPath() + ".animation.json");
    }
}
