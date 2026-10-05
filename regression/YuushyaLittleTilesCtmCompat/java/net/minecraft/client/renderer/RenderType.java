package net.minecraft.client.renderer;

/**
 * TEST ONLY: FRAPI BlendMode needs four layer identities to initialize native mesh materials.
 * Real RenderType initializes the entire game registry/loader. No rendering or layer selection
 * is tested here. This source set is never included in the mod or its sources JAR.
 */
public final class RenderType {
    private static final RenderType SOLID = new RenderType();
    private static final RenderType CUTOUT_MIPPED = new RenderType();
    private static final RenderType CUTOUT = new RenderType();
    private static final RenderType TRANSLUCENT = new RenderType();

    private RenderType() {}

    public static RenderType solid() { return SOLID; }
    public static RenderType cutoutMipped() { return CUTOUT_MIPPED; }
    public static RenderType cutout() { return CUTOUT; }
    public static RenderType translucent() { return TRANSLUCENT; }
}
