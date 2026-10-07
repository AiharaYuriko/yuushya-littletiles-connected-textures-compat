package com.yuushya.compat.connected;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

/** Conservative recognition of the pre233 lookup, without loading Minecraft classes. */
final class NativeNeighborLookup {
    static final String BLOCK_TILE = "team/creative/littletiles/common/block/mc/BlockTile";
    private static final String ACCESSOR = "net/minecraft/world/level/LevelAccessor";
    private static final String LEVEL = "net/minecraft/world/level/Level";
    private static final String CHUNK = "net/minecraft/world/level/chunk/LevelChunk";
    private static final String CREATION = CHUNK + "$EntityCreationType";
    private static final String POS = "Lnet/minecraft/core/BlockPos;";
    private static final String ENTITY = "Lnet/minecraft/world/level/block/entity/BlockEntity;";
    private static final String TILES = "team/creative/littletiles/common/block/entity/BETiles";
    private static final String CLIENT_DESC = "(L" + ACCESSOR + ";" + POS + ")" + ENTITY;

    private NativeNeighborLookup() {}

    static boolean isFixed(ClassNode node) {
        if (node == null || !BLOCK_TILE.equals(node.name))
            return false;
        MethodNode client = find(node, "tryGetClient", CLIENT_DESC);
        MethodNode load = find(node, "loadBE", "(Lnet/minecraft/world/level/BlockGetter;" + POS + ")L" + TILES + ";");
        if (client == null || load == null || (client.access & Opcodes.ACC_STATIC) == 0)
            return false;
        boolean checkOnly = false;
        for (var instruction : client.instructions) {
            if (instruction instanceof TypeInsnNode type && type.getOpcode() == Opcodes.CHECKCAST && LEVEL.equals(type.desc))
                return false;
            if (instruction instanceof FieldInsnNode field && field.getOpcode() == Opcodes.GETSTATIC
                    && CREATION.equals(field.owner) && "CHECK".equals(field.name) && ("L" + CREATION + ";").equals(field.desc))
                checkOnly = true;
        }
        return checkOnly
            && calls(client, ACCESSOR, "getChunk", "(" + POS + ")Lnet/minecraft/world/level/chunk/ChunkAccess;")
            && calls(client, CHUNK, "getBlockEntity", "(" + POS + "L" + CREATION + ";)" + ENTITY)
            && calls(load, BLOCK_TILE, "tryGetClient", CLIENT_DESC)
            && calls(load, TILES, "hasLoaded", "()Z");
    }

    private static MethodNode find(ClassNode node, String name, String descriptor) {
        for (var method : node.methods)
            if (name.equals(method.name) && descriptor.equals(method.desc))
                return method;
        return null;
    }

    private static boolean calls(MethodNode method, String owner, String name, String descriptor) {
        for (var instruction : method.instructions)
            if (instruction instanceof MethodInsnNode call && owner.equals(call.owner)
                    && name.equals(call.name) && descriptor.equals(call.desc))
                return true;
        return false;
    }
}
