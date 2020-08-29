package net.devtech.grossfabrichacks.entrypoints;

/**
 * the earliest GrossFabricHacks entrypoint. If a {@code gfh:prePrePreLaunch} class does not implement this interface,
 * then it is not instantiated and {@link PrePrePreLaunch#onPrePrePreLaunch} is not called, but the class is still initialized.
 */
public interface PrePrePreLaunch {
    void onPrePrePreLaunch();
}
