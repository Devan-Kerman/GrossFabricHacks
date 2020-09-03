package net.devtech.grossfabrichacks;

import net.devtech.grossfabrichacks.transformer.asm.AsmClassTransformer;
import net.devtech.grossfabrichacks.transformer.asm.RawClassTransformer;

public class GFHState {
    public static boolean mixinLoaded;

    public static boolean shouldWrite;
    // micro-optimization: cache transformer presence
    public static boolean transformPreMixinRawClass;
    public static boolean transformPreMixinAsmClass;
    public static boolean transformPostMixinRawClass;
    public static boolean transformPostMixinAsmClass;
    public static RawClassTransformer preMixinRawClassTransformer;
    public static RawClassTransformer postMixinRawClassTransformer;
    public static AsmClassTransformer preMixinAsmClassTransformer;
    public static AsmClassTransformer postMixinAsmClassTransformer;
}
