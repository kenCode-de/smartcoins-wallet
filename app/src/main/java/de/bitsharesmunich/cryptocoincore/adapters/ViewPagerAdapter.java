package de.bitsharesmunich.cryptocoincore.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import de.bitshares_munich.database.SCWallDatabase;
import de.bitsharesmunich.cryptocoincore.fragments.BalancesFragment;
import de.bitshares_munich.fragments.ContactsFragment;
import de.bitshares_munich.smartcoinswallet.R;

import de.bitsharesmunich.cryptocoincore.models.Coin;

/**
 * Created by qasim on 5/10/16.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {
    private SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
    private Context mContext;

    public ViewPagerAdapter(Context context, FragmentManager manager) {
        super(manager);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return BalancesFragment.newInstance(Coin.BITSHARE);
            case 1:
                if (SCWallDatabase.getAccount(Account seed, String cointype))
                return BalancesFragment.newInstance(Coin.BITCOIN);
            case 2:
                return new ContactsFragment();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getResources().getString(R.string.balances);
            case 1:
                return mContext.getResources().getString(R.string.bitcoin);
            case 2:
                return mContext.getResources().getString(R.string.contacts);
        }
        return null;
    }


    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}
