package de.bitshares_munich.interfaces;

import android.graphics.Bitmap;

import de.bitshares_munich.models.Gravatar;


/**
 * Created by Syed Muhammad Muzzammil on 5/19/16.
 */
public interface GravatarDelegate {
    void updateProfile(Gravatar myGravatar);

    void updateCompanyLogo(Bitmap logo);

    void failureUpdateProfile();

    void failureUpdateLogo();
}
