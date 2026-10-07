package com.yuushya.compat.connected;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;

public final class MaterialStateRegression {
    private static int checks;
    public static void main(String[] args) {
        var full = NeighborMaterials.of(List.of("marble"));
        var split = NeighborMaterials.of(List.of("marble", "marble", "marble"));
        var mixed = NeighborMaterials.of(List.of("other", "marble"));
        check(full.equals(split), "splitting a material into pieces does not change its appearance input");
        check(full.contains("marble") && mixed.contains("marble"), "ordinary and mixed LT neighbors both connect");
        check(!mixed.contains("concrete"), "different materials do not connect");
        check(!NeighborMaterials.of(List.of("beam-east")).contains("beam-west"), "different facing keys do not connect");
        check(!NeighborMaterials.unavailable().available(), "unloaded neighbor must not be treated as air");
        check(NeighborMaterials.of(List.of()).available(), "known empty neighbor is available");
        var list = new ArrayList<>(List.of("marble"));
        var snapshot = NeighborMaterials.of(list);
        list.clear();
        check(snapshot.contains("marble"), "snapshot is stable during one rebuild");
        check(!NeighborMaterials.of(list).contains("marble"), "next rebuild sees neighbor removal");
        for (Direction facing : new Direction[]{null, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            for (boolean negative : new boolean[]{false, true}) for (boolean positive : new boolean[]{false, true}) {
                var below = NeighborMaterials.of(negative ? List.of("m") : List.of());
                var above = NeighborMaterials.of(positive ? List.of("m", "other") : List.of());
                String position = ConnectedMaterialState.position(facing, below.contains("m"), above.contains("m"));
                if (negative && positive) check(position.equals("middle"), "both ends connect");
                else if (!negative && !positive) check(position.equals("none"), "isolated position");
                else if (facing == null) check(position.equals(positive ? "bottom" : "top"), "column end mapping");
                else {
                    boolean negativeIsLeft = facing == Direction.WEST || facing == Direction.SOUTH;
                    check(position.equals(negative == negativeIsLeft ? "left" : "right"), "beam facing mapping");
                }
            }
        }
        System.out.println("Material state regression passed: " + checks + " assertions (position-level presence and orientation; no in-game claim).");
    }
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
}
