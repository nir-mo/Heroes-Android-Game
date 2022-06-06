package com.nirmo.heroesgame.utils;

public final class MathUtils {
    private MathUtils() {}

    public static int getRandomInRange(int min, int max) {
        // TODO: check inputs.
        return (int) ((Math.random() * (max - min)) + min);
    }
}
