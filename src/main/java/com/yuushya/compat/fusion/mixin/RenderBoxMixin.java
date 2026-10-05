package com.yuushya.compat.fusion.mixin;

import java.util.List;
import com.yuushya.compat.fusion.UnculledQuadCache;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import team.creative.creativecore.client.render.box.RenderBox;

@Mixin(value = RenderBox.class, remap = false)
public abstract class RenderBoxMixin {
    @Redirect(method = "getBakedQuad", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/resources/model/BakedModel;getQuads(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;Lnet/neoforged/neoforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)Ljava/util/List;"), require = 2, allow = 2)
    private List<BakedQuad> yuushyaFusion$includeUnculled(BakedModel model, BlockState state, Direction side,
            RandomSource random, ModelData data, RenderType layer) {
        List<BakedQuad> directional = model.getQuads(state, side, random, data, layer);
        UnculledQuadCache cache = data.get(UnculledQuadCache.PROPERTY);
        return cache == null ? directional : cache.append(model, state, side, data, layer, directional);
    }
}
