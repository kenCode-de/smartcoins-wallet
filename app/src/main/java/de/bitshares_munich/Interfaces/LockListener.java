package de.bitshares_munich.Interfaces;

/**
 * Interface that must be implemented by any party that desires to be notified of the
 * 'lock release' event. This is defined as the event that takes place when the user
 * successfully releases the app lock by entering a correct pin number.
 *
 * Created by nelson on 12/21/16.
 */
public interface LockListener {
    void onLockReleased();
}
