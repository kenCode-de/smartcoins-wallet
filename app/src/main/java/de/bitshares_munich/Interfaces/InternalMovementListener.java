package de.bitshares_munich.interfaces;

/**
 * Interface to be implemented by any class interested in getting reports from internal
 * app displacements.
 *
 * This is useful in case the component, an activity for instance needs to tell apart
 * users coming from the outside (opening the app for the first time) from users
 * coming back from other activities by clearing the task stack.
 *
 * Created by nelson on 12/19/16.
 */
public interface InternalMovementListener {

    /* Inform the component that an internal app movement is about to be performed */
    public void onInternalAppMove();
}
