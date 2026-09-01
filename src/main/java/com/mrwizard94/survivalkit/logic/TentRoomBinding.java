package com.mrwizard94.survivalkit.logic;

import java.util.Objects;
import java.util.UUID;

/**
 * Persistent binding between an exterior tent and its camping-room slot.
 *
 * @param tentId     stable id stored on the tent item / block entity
 * @param roomIndex  allocated camping-dimension room index
 * @param exteriorDim exterior dimension id string
 * @param exteriorX  exterior origin X
 * @param exteriorY  exterior origin Y
 * @param exteriorZ  exterior origin Z
 * @param facing     exterior facing index (0=south..3=east)
 * @param seeded     whether the interior starter kit / furniture was placed
 */
public record TentRoomBinding(
        UUID tentId,
        int roomIndex,
        String exteriorDim,
        int exteriorX,
        int exteriorY,
        int exteriorZ,
        int facing,
        boolean seeded) {

    /**
     * Compact validation.
     */
    public TentRoomBinding {
        Objects.requireNonNull(tentId, "tentId");
        Objects.requireNonNull(exteriorDim, "exteriorDim");
        if (roomIndex < 0) {
            throw new IllegalArgumentException("roomIndex must be >= 0");
        }
        if (facing < 0 || facing > 3) {
            throw new IllegalArgumentException("facing must be 0..3");
        }
    }

    /**
     * Returns a copy marked seeded.
     *
     * @return seeded binding
     */
    public TentRoomBinding markSeeded() {
        return new TentRoomBinding(
                tentId, roomIndex, exteriorDim, exteriorX, exteriorY, exteriorZ, facing, true);
    }
}
