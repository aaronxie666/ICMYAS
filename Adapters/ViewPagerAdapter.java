package icn.icmyas.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import icn.icmyas.Fragments.ForumsFragment;
import icn.icmyas.Fragments.HomeFragment;
import icn.icmyas.Fragments.OffersFragment;
import icn.icmyas.Fragments.ProfileFragment;

/**
 * Author:  Bradley Wilson
 * Date: 17/07/2017
 * Package: icn.icmyas.Adapters
 * Project Name: ICMYAS
 */

public class ViewPagerAdapter extends FragmentStatePagerAdapter  {
    private final List<Fragment> pFragmentList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0:
                return HomeFragment.newInstance(position);
            case 1:
                return ProfileFragment.newInstance(position);
            case 2:
                return OffersFragment.newInstance(position);
            case 3:
                return ForumsFragment.newInstance(position);
            default:
                return HomeFragment.newInstance(position);
        }
    }

    @Override
    public int getCount() {
        return pFragmentList.size();
    }

    public void addFrag(android.support.v4.app.Fragment fragment) {
        pFragmentList.add(fragment);
    }

    public Fragment getActiveFragment(int position) {
        return  pFragmentList.get(position);
    }

    private static String makeFragmentName(int viewId, int index) {
        return "android:switcher:" + viewId + ":" + index;
    }
}
