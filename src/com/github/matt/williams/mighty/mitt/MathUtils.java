package com.github.matt.williams.mighty.mitt;

public class MathUtils {

    static float dot2(float[] p, float[] q) {
        return p[0] * q[0] + p[1] * q[1];
    }

    static float[] norm2(float[] p) {
        float d = mod2(p);
        return new float[] {p[0] / d, p[1] / d};
    }

    static float mod2(float[] p) {
        return (float)Math.sqrt(p[0] * p[0] + p[1] * p[1]);
    }

    static float[] norm3(float[] p) {
        float d = mod3(p);
        return new float[] {p[0] / d, p[1] / d, p[2] / d};
    }

    static float mod3(float[] p) {
        return (float)Math.sqrt(p[0] * p[0] + p[1] * p[1] + p[2] * p[2]);
    }
}
