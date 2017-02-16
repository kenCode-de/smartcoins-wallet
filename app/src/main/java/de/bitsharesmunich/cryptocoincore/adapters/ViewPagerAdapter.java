package de.bitsharesmunich.cryptocoincore.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.bitshares_munich.database.SCWallDatabase;
import de.bitsharesmunich.cryptocoincore.base.AccountSeed;
import de.bitsharesmunich.cryptocoincore.base.GeneralCoinAccount;
import de.bitsharesmunich.cryptocoincore.base.SeedType;
import de.bitsharesmunich.cryptocoincore.fragments.BalancesFragment;
import de.bitshares_munich.fragments.ContactsFragment;
import de.bitshares_munich.smartcoinswallet.R;

import de.bitsharesmunich.cryptocoincore.fragments.NoCurrencyAccountFragment;
import de.bitsharesmunich.cryptocoincore.base.Coin;

/**
 * Created by henry on 05/02/17.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {
    private SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
    private Context mContext;
    private FragmentManager mFragmentManager;
    private Fragment fragmentAtBitcoin;

    public ViewPagerAdapter(Context context, FragmentManager manager) {
        super(manager);
        mContext = context;
        mFragmentManager = manager;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return BalancesFragment.newInstance(Coin.BITSHARE);
            case 1:
                final SCWallDatabase db = new SCWallDatabase(mContext);
                GeneralCoinAccount account = db.getGeneralCoinAccount(Coin.BITCOIN.name());

                if (account != null){
                    this.fragmentAtBitcoin = BalancesFragment.newInstance(Coin.BITCOIN);
                } else {
                    this.fragmentAtBitcoin = NoCurrencyAccountFragment.newInstance(Coin.BITCOIN);
                }
                return this.fragmentAtBitcoin;
                //return BalancesFragment.newInstance(Coin.BITCOIN);
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
    public int getItemPosition(Object object)
    {
        if (object instanceof NoCurrencyAccountFragment && this.fragmentAtBitcoin instanceof BalancesFragment) {
            return POSITION_NONE;
        }
        return POSITION_UNCHANGED;
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

    public void changeBitcoinFragment(){
        mFragmentManager.beginTransaction().remove(this.fragmentAtBitcoin).commit();
        this.fragmentAtBitcoin = BalancesFragment.newInstance(Coin.BITCOIN);
        notifyDataSetChanged();
    }

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
