package net.minecraft.client.renderer;

/** Test-only layer identities. The real class initializes game registries. Never packaged. */
public final class RenderType {
    private static final RenderType SOLID = new RenderType();
    private static final RenderType TRANSLUCENT = new RenderType();
    private RenderType() {}
    public static RenderType solid() { return SOLID; }
    public static RenderType translucent() { return TRANSLUCENT; }
}
