package de.bitsharesmunich.cryptocoincore.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import de.bitshares_munich.database.SCWallDatabase;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAccount;
import de.bitsharesmunich.cryptocoincore.fragments.GeneralCoinBalancesFragment;
import de.bitshares_munich.fragments.ContactsFragment;
import de.bitshares_munich.smartcoinswallet.R;

import de.bitsharesmunich.cryptocoincore.fragments.GeneralCoinContactsFragment;
import de.bitsharesmunich.cryptocoincore.fragments.NoCurrencyAccountFragment;
import de.bitsharesmunich.cryptocoincore.base.Coin;

/**
 * Created by henry on 05/02/17.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {
    private SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
    private Context mContext;
    private FragmentManager mFragmentManager;
    //private Fragment fragmentAtBitcoin;
    //private Fragment fragmentAtBitcoinContacts;

    public ViewPagerAdapter(Context context, FragmentManager manager) {
        super(manager);
        mContext = context;
        mFragmentManager = manager;
    }

    @Override
    public Fragment getItem(int position) {
        final SCWallDatabase db;
        GeneralCoinAccount account;

        switch(position){
            case 0:
                return GeneralCoinBalancesFragment.newInstance(Coin.BITSHARE);
            case 1:
                return GeneralCoinContactsFragment.newInstance(Coin.BITSHARE);
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
                return mContext.getResources().getString(R.string.contacts);
        }
        return null;
    }


    /*@Override
    public int getItemPosition(Object object)
    {
        if (object instanceof NoCurrencyAccountFragment && this.fragmentAtBitcoin instanceof GeneralCoinBalancesFragment) {
            return POSITION_NONE;
        }

        return POSITION_UNCHANGED;
    }*/


    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    /*public void changeBitcoinFragment(){
        mFragmentManager.beginTransaction().remove(this.fragmentAtBitcoin).commit();
        mFragmentManager.beginTransaction().remove(this.fragmentAtBitcoinContacts).commit();
        this.fragmentAtBitcoin = GeneralCoinBalancesFragment.newInstance(Coin.BITCOIN);
        this.fragmentAtBitcoinContacts = GeneralCoinContactsFragment.newInstance(Coin.BITCOIN);
        notifyDataSetChanged();
    }*/

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        int index = registeredFragments.indexOfValue((Fragment)object);
        registeredFragments.remove(index);
        super.destroyItem(container, index, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}
