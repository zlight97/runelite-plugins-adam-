package tictac7x.tithe;

import tictac7x.Overlay;
import java.time.Instant;
import java.time.Duration;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.GameObject;

public class TithePatch extends Overlay {
    // Tithe empty patch.
    protected static final int TITHE_EMPTY_PATCH = 27383;

    // Golovanova plants.
    protected static final int GOLOVANOVA_SEEDLING = 27384;
    protected static final int GOLOVANOVA_SEEDLING_WATERED = 27385;
    protected static final int GOLOVANOVA_SEEDLING_BLIGHTED = 27386;
    protected static final int GOLOVANOVA_PLANT_1 = 27387;
    protected static final int GOLOVANOVA_PLANT_1_WATERED = 27388;
    protected static final int GOLOVANOVA_PLANT_1_BLIGHTED = 27389;
    protected static final int GOLOVANOVA_PLANT_2 = 27390;
    protected static final int GOLOVANOVA_PLANT_2_WATERED = 27391;
    protected static final int GOLOVANOVA_PLANT_2_BLIGHTED = 27392;
    protected static final int GOLOVANOVA_GROWN = 27393;
    protected static final int GOLOVANOVA_GROWN_BLIGHTED = 27394;

    // Bologano plants.
    protected static final int BOLOGANO_SEEDLING = 27395;
    protected static final int BOLOGANO_SEEDLING_WATERED = 27396;
    protected static final int BOLOGANO_SEEDLING_BLIGHTED = 27397;
    protected static final int BOLOGANO_PLANT_1 = 27398;
    protected static final int BOLOGANO_PLANT_1_WATERED = 27399;
    protected static final int BOLOGANO_PLANT_1_BLIGHTED = 27400;
    protected static final int BOLOGANO_PLANT_2 = 27401;
    protected static final int BOLOGANO_PLANT_2_WATERED = 27402;
    protected static final int BOLOGANO_PLANT_2_BLIGHTED = 27403;
    protected static final int BOLOGANO_GROWN = 27404;
    protected static final int BOLOGANO_GROWN_BLIGHTED = 27405;

    // Logavano plants.
    protected static final int LOGAVANO_SEEDLING = 27406;
    protected static final int LOGAVANO_SEEDLING_WATERED = 27407;
    protected static final int LOGAVANO_SEEDLING_BLIGHTED = 27408;
    protected static final int LOGAVANO_PLANT_1 = 27409;
    protected static final int LOGAVANO_PLANT_1_WATERED = 27410;
    protected static final int LOGAVANO_PLANT_1_BLIGHTED = 27411;
    protected static final int LOGAVANO_PLANT_2 = 27412;
    protected static final int LOGAVANO_PLANT_2_WATERED = 27413;
    protected static final int LOGAVANO_PLANT_2_BLIGHTED = 27414;
    protected static final int LOGAVANO_GROWN = 27415;
    protected static final int LOGAVANO_GROWN_BLIGHTED = 27416;

    private enum State {
        EMPTY,
        SEEDLING_DRY,
        SEEDLING_WATERED,
        PLANT_1_DRY,
        PLANT_1_WATERED,
        PLANT_2_DRY,
        PLANT_2_WATERED,
        GROWN,
        BLIGHTED
    }

    private final TitheConfig config;
    private final Duration CYCLE_DURATION = Duration.ofMinutes(1);
    private final Instant cycle_start;
    private int cycle_count = 0;
    private GameObject patch = null;
    private State state = State.EMPTY;

    public TithePatch(final GameObject seedling, final TitheConfig config) {
        this.config = config;
        this.cycle_start = Instant.now();
        setPatch(seedling);
        updateState();
    }

    public void setPatch(final GameObject patch) {
        if (isPatch(patch)) this.patch = patch;

        if (isWatered(patch)) {
            if (state == State.SEEDLING_DRY) {
                state = State.SEEDLING_WATERED;
            } else if (state == State.PLANT_1_DRY) {
                state = State.PLANT_1_WATERED;
            } else if (state == State.PLANT_2_DRY) {
                state = State.PLANT_2_WATERED;
            }
        }
    }

    @Override
    public Dimension render(final Graphics2D graphics) {
        updateState();

        final Color color = getCycleColor();
        if (color != null) renderPie(graphics, patch, getCycleColor(), (float) getCycleProgress());
        return null;
    }

    private void updateState() {
        if (state != null && getCycleDuration() >= cycle_count * CYCLE_DURATION.toMillis()) {
            if (state == State.EMPTY) {
                state = State.SEEDLING_DRY;
            } else if (
                state == State.SEEDLING_DRY
                || state == State.PLANT_1_DRY
                || state == State.PLANT_2_DRY
                || state == State.GROWN
            ) {
                state = State.BLIGHTED;
            } else if (state == State.SEEDLING_WATERED) {
                state = State.PLANT_1_DRY;
            } else if (state == State.PLANT_1_WATERED) {
                state = State.PLANT_2_DRY;
            } else if (state == State.PLANT_2_WATERED) {
                state = State.GROWN;
            } else if (state == State.BLIGHTED) {
                state = null;
            }

            cycle_count++;
        }
    }

    private Color getCycleColor() {
        if (config.highlightPlantsDry() && (state == State.SEEDLING_DRY || state == State.PLANT_1_DRY || state == State.PLANT_2_DRY)) {
            return config.getPlantsDryColor();
        } else if (config.highlightPlantsGrown() && state == State.GROWN) {
            return config.getPlantsGrownColor();
        } else if (config.highlightPlantsWatered() && (state == State.SEEDLING_WATERED || state == State.PLANT_1_WATERED || state == State.PLANT_2_WATERED)) {
            return config.getPlantsWateredColor();
        } else if (config.highlightPlantsBlighted() && state == State.BLIGHTED) {
            return config.getPlantsBlightedColor();
        }

        return null;
    }

    private double getCycleProgress() {
        return 1 - ((getCycleDuration() % CYCLE_DURATION.toMillis()) / CYCLE_DURATION.toMillis());
    }

    protected double getCycleDuration() {
        if (state == null) {
            return CYCLE_DURATION.toMillis();
        } else {
            return Duration.between(cycle_start, Instant.now()).toMillis();
        }

    }

    protected static boolean isSeedling(final GameObject patch) {
        final int id = patch.getId();
        return id == GOLOVANOVA_SEEDLING || id == BOLOGANO_SEEDLING || id == LOGAVANO_SEEDLING;
    }

    protected static boolean isDry(final GameObject patch) {
        final int id = patch.getId();
        return (
            id == GOLOVANOVA_SEEDLING
            || id == GOLOVANOVA_PLANT_1
            || id == GOLOVANOVA_PLANT_2
            || id == BOLOGANO_SEEDLING
            || id == BOLOGANO_PLANT_1
            || id == BOLOGANO_PLANT_2
            || id == LOGAVANO_SEEDLING
            || id == LOGAVANO_PLANT_1
            || id == LOGAVANO_PLANT_2
        );
    }

    protected static boolean isWatered(final GameObject patch) {
        final int id = patch.getId();
        return (
            id == GOLOVANOVA_SEEDLING_WATERED
            || id == GOLOVANOVA_PLANT_1_WATERED
            || id == GOLOVANOVA_PLANT_2_WATERED
            || id == BOLOGANO_SEEDLING_WATERED
            || id == BOLOGANO_PLANT_1_WATERED
            || id == BOLOGANO_PLANT_2_WATERED
            || id == LOGAVANO_SEEDLING_WATERED
            || id == LOGAVANO_PLANT_1_WATERED
            || id == LOGAVANO_PLANT_2_WATERED
        );
    }

    protected static boolean isGrown(final GameObject patch) {
        final int id = patch.getId();
        return (
            id == GOLOVANOVA_GROWN
            || id == BOLOGANO_GROWN
            || id == LOGAVANO_GROWN
        );
    }

    protected static boolean isBlighted(final GameObject patch) {
        final int id = patch.getId();
        return (
            id == GOLOVANOVA_SEEDLING_BLIGHTED
            || id == GOLOVANOVA_PLANT_1_BLIGHTED
            || id == GOLOVANOVA_PLANT_2_BLIGHTED
            || id == GOLOVANOVA_GROWN_BLIGHTED
            || id == BOLOGANO_SEEDLING_BLIGHTED
            || id == BOLOGANO_PLANT_1_BLIGHTED
            || id == BOLOGANO_PLANT_2_BLIGHTED
            || id == BOLOGANO_GROWN_BLIGHTED
            || id == LOGAVANO_SEEDLING_BLIGHTED
            || id == LOGAVANO_PLANT_1_BLIGHTED
            || id == LOGAVANO_PLANT_2_BLIGHTED
            || id == LOGAVANO_GROWN_BLIGHTED
        );
    }

    protected static boolean isEmptyPatch(final GameObject patch) {
        return patch.getId() == TITHE_EMPTY_PATCH;
    }

    protected static boolean isPatch(final GameObject patch) {
        return isDry(patch) || isWatered(patch) || isGrown(patch) || isBlighted(patch) || isEmptyPatch(patch);
    }
}
