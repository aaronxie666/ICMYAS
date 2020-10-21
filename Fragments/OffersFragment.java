package icn.icmyas.Fragments;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.android.vending.billing.IInAppBillingService;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import icn.icmyas.Adapters.RecyclerViewOffersAdapter;
import icn.icmyas.MainActivity;
import icn.icmyas.Misc.Constants;
import icn.icmyas.Misc.SashidoHelper;
import icn.icmyas.Misc.Utils;
import icn.icmyas.Models.Offer;
import icn.icmyas.R;
import icn.icmyas.Util.IabHelper;
import icn.icmyas.Util.IabResult;
import icn.icmyas.Util.Inventory;
import icn.icmyas.Util.Purchase;

import static android.widget.Toast.LENGTH_LONG;
import static icn.icmyas.Misc.Constants.IMP_COMP_HAS_ENTERED_KEY;
import static icn.icmyas.Misc.Constants.IMP_COMP_USER_KEY;
import static icn.icmyas.Misc.Constants.IMP_COMP_VOTED_ON_KEY;
import static icn.icmyas.Misc.Constants.IMP_COMP_VOTES_KEY;

public class OffersFragment extends Fragment  {

    private RecyclerView offersRecyclerView, shopRecyclerView;
    private IInAppBillingService mService;
    private ServiceConnection mServiceConn;
    private icn.icmyas.Util.IabHelper mHelper;
    private final static String TAG = OffersFragment.class.getSimpleName();
    private ViewAnimator offers_va;
    private boolean offersSelected;
    private Utils utils;
    private TextView silverCoins, goldCoins;
    private Offer selectedOffer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("test", "OffersFragment onCreate()");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        utils = new Utils(getActivity());
        View view = inflater.inflate(R.layout.fragment_offers, container, false);
        initViews(view);
        Log.e("test", "OffersFragment onCreateViews()");
        return view;
    }

    private void initViews(View view) {
        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);
            }
        };

        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        getActivity().bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        offersSelected = true;
        offers_va = view.findViewById(R.id.offers_va);
        Animation inAnim = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_in_left);
        Animation outAnim = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_out_right);
        // offers_va.setInAnimation(inAnim);
        // offers_va.setOutAnimation(outAnim);

        final TextView tv_offers = view.findViewById(R.id.tv_offers);
        final TextView tv_store = view.findViewById(R.id.tv_store);

        tv_offers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!offersSelected) {
                    tv_offers.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                    tv_offers.setBackgroundResource(R.drawable.rounded_left_selected);
                    tv_store.setTextColor(Color.WHITE);
                    tv_store.setBackgroundResource(R.drawable.rounded_right);
                    offersSelected = true;
                    offers_va.setDisplayedChild(0);
                }
            }
        });

        tv_store.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (offersSelected) {
                    tv_store.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                    tv_store.setBackgroundResource(R.drawable.rounded_right_selected);
                    tv_offers.setTextColor(Color.WHITE);
                    tv_offers.setBackgroundResource(R.drawable.rounded_left);
                    offersSelected = false;
                    offers_va.setDisplayedChild(1);
                }
            }
        });

        TextView fullName = (TextView) view.findViewById(R.id.offer_full_name);
        fullName.setText(ParseUser.getCurrentUser().getString(Constants.USER_FULL_NAME_KEY));
        silverCoins = (TextView) view.findViewById(R.id.offers_user_points_silver);
        silverCoins.setText(String.valueOf(ParseUser.getCurrentUser().getInt(Constants.USER_SILVER_COINS_KEY)));
        goldCoins = (TextView) view.findViewById(R.id.offers_user_point_gold);
        goldCoins.setText(String.valueOf(ParseUser.getCurrentUser().getInt(Constants.USER_GOLD_COINS_KEY)));

        offersRecyclerView = (RecyclerView) view.findViewById(R.id.offers_recycler_view);
        RecyclerView.LayoutManager oLayoutManager = new GridLayoutManager(getActivity(), 2);
        offersRecyclerView.setLayoutManager(oLayoutManager);
        populateOffersRecyclerView(true);

        shopRecyclerView = (RecyclerView) view.findViewById(R.id.coins_recycler_view);
        RecyclerView.LayoutManager sLayoutManager = new GridLayoutManager(getActivity(), 2);
        shopRecyclerView.setLayoutManager(sLayoutManager);
        populateOffersRecyclerView(false);
    }

    private void populateOffersRecyclerView(final boolean isOffer) {
        final ArrayList<Offer> offersList = new ArrayList<Offer>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.OFFERS_CLASS_KEY);
        query.whereEqualTo(Constants.OFFERS_IS_AVAILABLE_KEY, true);
        query.whereEqualTo(Constants.OFFERS_IS_OFFER_KEY, isOffer);
        query.orderByDescending(Constants.OFFERS_IS_FEATURED_KEY);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject j : objects) {
                        Offer offer = new Offer();
                        offer.setObjectId(j.getObjectId());
                        offer.setImageUrl(j.getParseFile(Constants.OFFERS_IMAGE_KEY).getUrl());
                        offer.setText(j.getString(Constants.OFFERS_TEXT_KEY));
                        offer.setPrice(j.getInt(Constants.OFFERS_COST_KEY));
                        offer.setFeatured(j.getBoolean(Constants.OFFERS_IS_FEATURED_KEY));
                        offer.setOffer(j.getBoolean(Constants.OFFERS_IS_OFFER_KEY));
                        offer.setTitle(j.getString(Constants.OFFERS_TITLE_KEY));
                        offer.setGold(j.getBoolean(Constants.OFFERS_IS_GOLD_KEY));
                        if (isOffer) {
                            offer.setTransparentImage(j.getParseFile(Constants.OFFERS_TRANSPARENT_IMAGE_KEY).getUrl());
                            offer.setCode(j.getString(Constants.OFFERS_CODE_KEY));
                            offer.setWebsiteURL(j.getString(Constants.OFFERS_WEBSITE_URL_KEY));
                        } else {
                            String[] extras = {Constants.OFFERS_EXTRA_25_KEY, Constants.OFFERS_EXTRA_50_KEY, Constants.OFFERS_EXTRA_75_KEY, Constants.OFFERS_EXTRA_100_KEY};
                            for (int i = 0; i < extras.length; i++) {
                                if (j.getBoolean(extras[i])) {
                                    offer.setExtra((i + 1) * 25);
                                    offer.setBonusStars(j.getInt(Constants.OFFERS_EXTRA_GOLD_KEY));
                                    break;
                                }
                            }
                        }
                        offersList.add(offer);
                    }
                    RecyclerViewOffersAdapter adapter = new RecyclerViewOffersAdapter(getActivity(), offersList);
                    if (isOffer) {
                        offersRecyclerView.setAdapter(adapter);
                    } else {
                        shopRecyclerView.setAdapter(adapter);
                    }
                    adapter.setOnItemClickListener(new RecyclerViewOffersAdapter.onOfferItemClickListener() {
                        @Override
                        public void onItemClickListener(View view, int position, Offer offer) {
                            if (offer.isOffer()) {
                                showOfferDialog(offer);
                            } else {
                                selectedOffer = offer;
                                final String sku = "gold_stars_" + offer.getTitle();
                                mHelper = new IabHelper(getActivity(), Constants.BILLING_KEY_PT1 + Constants.BILLING_KEY_PT2 + Constants.BILLING_KEY_PT3 + Constants.BILLING_KEY_PT4);
                                mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                                    @Override
                                    public void onIabSetupFinished(IabResult result) {
                                        if (result.isSuccess()) {
                                            try {
                                                mHelper.launchPurchaseFlow(getActivity(), sku, 10001,
                                                        mPurchaseFinishedListener, ParseUser.getCurrentUser().getObjectId());
                                            } catch (IabHelper.IabAsyncInProgressException e1) {
                                                Log.e("Failed", "failed" + e1.getLocalizedMessage());
                                            }
                                        } else {
                                            Log.e(TAG, "failed " + result.getMessage());
                                        }
                                    }
                                });
                            }
                        }
                    });
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("failed", "failed" + e.getLocalizedMessage());
                }
            }
        });
    }

    private String ITEM_SKU;
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase info) {
            if (result.isFailure()) {
                return;
            } else if (info.getSku().equals("gold_stars_50")) {
                ITEM_SKU = "gold_stars_50";
                consumeItem();
            } else if (info.getSku().equals("gold_stars_100")) {
                ITEM_SKU = "gold_stars_100";
                consumeItem();
            } else if (info.getSku().equals("gold_stars_150")) {
                ITEM_SKU = "gold_stars_150";
                consumeItem();
            } else if (info.getSku().equals("gold_stars_250")) {
                ITEM_SKU = "gold_stars_250";
                consumeItem();
            } else if (info.getSku().equals("gold_stars_500")) {
                ITEM_SKU = "gold_stars_500";
                consumeItem();
            } else if (info.getSku().equals("gold_stars_1000")) {
                ITEM_SKU = "gold_stars_1000";
                consumeItem();
            } else if (info.getSku().equals("gold_stars_2500")) {
                ITEM_SKU = "gold_stars_2500";
                consumeItem();
            }
        }
    };

    private void consumeItem() {
        try {
            mHelper.queryInventoryAsync(mReceivedInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            Log.e("failed", "failed " + e.getLocalizedMessage());
        }
    }

    IabHelper.QueryInventoryFinishedListener mRecievedInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
            if (result.isFailure()) {
                return;
            } else {
                try {
                    mHelper.consumeAsync(inv.getPurchase(ITEM_SKU),
                            mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {
            if (result.isFailure()) {
                return;
            } else {
                try {
                    mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU),
                            mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {
                    if (result.isSuccess()) {
                        int goldStars = ParseUser.getCurrentUser().getInt(Constants.USER_GOLD_COINS_KEY);
                        switch (purchase.getSku()) {
                            case "gold_stars_50":
                                goldStars = goldStars + 50;
                                break;
                            case "gold_stars_100":
                                goldStars = goldStars + 100;
                                break;
                            case "gold_stars_150":
                                goldStars = goldStars + 150;
                                break;
                            case "gold_stars_250":
                                goldStars = goldStars + 250;
                                break;
                            case "gold_stars_500":
                                goldStars = goldStars + 500;
                                break;
                            case "gold_stars_1000":
                                goldStars = goldStars + 1000;
                                break;
                            case "gold_stars_2500":
                                goldStars = goldStars + 2500;
                                break;
                        }
                        // adds any bonus stars if the offer has an extra x% free, and 0 otherwise
                        goldStars = goldStars + selectedOffer.getBonusStars();
                        ParseUser.getCurrentUser().put(Constants.USER_GOLD_COINS_KEY, goldStars);
                        ParseUser.getCurrentUser().saveInBackground();
                        goldCoins.setText(String.valueOf(goldStars));
                    } else {
                        return;
                    }
                }
            };

    private void showOfferDialog(final Offer offer) {
        final Dialog dialog = new Dialog(getContext(), R.style.customAlertDialog);
        dialog.setContentView(R.layout.dialog_offers_redeem);

        TextView offerTitle = (TextView) dialog.findViewById(R.id.offers_title);
        offerTitle.setText(offer.getTitle());

        ImageView offerImage = (ImageView) dialog.findViewById(R.id.offers_image);
        Picasso.with(getActivity()).load(offer.getTransparentImage()).fit().centerCrop().into(offerImage);

        TextView offersText = (TextView) dialog.findViewById(R.id.offers_text);
        String stars = offer.isGold() ? " gold stars?" : " silver stars?";
        offersText.setText("Are you sure you want to redeem '" + offer.getText() + "' for " + offer.getPrice() + stars);

        TextView cancelButton = dialog.findViewById(R.id.cancel_offer_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        TextView redeemButton = dialog.findViewById(R.id.redeem_offer_button);
        redeemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String offerId = offer.getObjectId();
                if (offerIsFeatured(offerId, false) && currentUserIsFeatured(false)) {
                    // if offer is featured (not IMP) and the current user is already featured (not IMP)
                    dialog.cancel();
                    utils.makeText("You already have featured status! Try again when it expires.", Toast.LENGTH_LONG);
                } else if (offerIsFeatured(offerId, true) && currentUserIsFeatured(true)) {
                    // if offer is featured (IMP) and the current user is already featured (IMP)
                    dialog.cancel();
                    utils.makeText("You already have IMP featured status! Try again when it expires.", Toast.LENGTH_LONG);
                } else if (offerIsFeatured(offerId, false) && !userProfileIsComplete()) {
                    // if offer is featured (not IMP) and the current user's profile is incomplete
                    dialog.cancel();
                    utils.makeText("Your profile must be 100% complete to enter the competition!", Toast.LENGTH_LONG);
                } else {
                    final String coinsKey = offer.isGold() ? Constants.USER_GOLD_COINS_KEY : Constants.USER_SILVER_COINS_KEY;
                    int userStars = ParseUser.getCurrentUser().getInt(coinsKey);
                    if (userStars >= offer.getPrice()) {
                        ParseUser.getCurrentUser().put(coinsKey, userStars - offer.getPrice());
                        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    String offerId = offer.getObjectId();
                                    if (offer.getCode() != null && !offer.getCode().isEmpty()) {
                                        // if the offer grants the user a code
                                        showCodeDialog(offer);
                                    } else if (offerIsFeatured(offerId, false)) {
                                        // offer is for a featured model position (not IMP)
                                        boolean is3Hours = (offerId.equals(Constants.FEATURED_3HRS_OBJECT_ID));
                                        int hours = is3Hours ? 3 : 24;
                                        addUserToFeaturedModels(hours, false);
                                    } else if (offerIsFeatured(offerId, true)) {
                                        // offer is for a featured model position (IMP)
                                        boolean is3Hours = (offerId.equals(Constants.FEATURED_IMP_3HRS_OBJECT_ID));
                                        int hours = is3Hours ? 3 : 24;
                                        addUserToFeaturedModels(hours, true);
                                    } else {
                                        // offer has no code, and is not for a featured model position
                                        addToOffersClaimed(offer);
                                    }
                                    dialog.cancel();
                                    silverCoins.setText(String.valueOf(ParseUser.getCurrentUser().getInt(Constants.USER_SILVER_COINS_KEY)));
                                    goldCoins.setText(String.valueOf(ParseUser.getCurrentUser().getInt(Constants.USER_GOLD_COINS_KEY)));
                                } else {
                                    Log.e("Failed", "failed" + e.getLocalizedMessage());
                                }
                            }
                        });
                    } else {
                        String stars = offer.isGold() ? " gold stars " : " silver stars ";
                        ((MainActivity) getActivity()).utils.makeText("You need at least " + offer.getPrice() + stars + "to redeem this offer.", LENGTH_LONG);
                        dialog.cancel();
                    }
                }
            }
        });
        dialog.show();
    }

    private boolean offerIsFeatured(String offerId, boolean isIMP) {
        if (isIMP) {
            return (offerId.equals(Constants.FEATURED_IMP_3HRS_OBJECT_ID)) || (offerId.equals(Constants.FEATURED_IMP_24HRS_OBJECT_ID));
        } else {
            return (offerId.equals(Constants.FEATURED_3HRS_OBJECT_ID)) || (offerId.equals(Constants.FEATURED_24HRS_OBJECT_ID));
        }
    }

    private boolean userProfileIsComplete() {
        return ParseUser.getCurrentUser().getBoolean(Constants.USER_PROFILE_COMPLETED_KEY);
    }

    private void addToOffersClaimed(Offer offer) {
        ParseQuery<ParseObject> getOffer = ParseQuery.getQuery(Constants.OFFERS_CLASS_KEY);
        getOffer.whereEqualTo(Constants.OFFERS_TITLE_KEY, offer.getTitle());
        getOffer.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    ParseObject newClaimed = new ParseObject(Constants.OFFERS_CLAIMED_CLASS_KEY);
                    newClaimed.put(Constants.OFFERS_CLAIMED_OFFER_KEY, object);
                    newClaimed.put(Constants.OFFERS_CLAIMED_REDEEMED_KEY, false);
                    newClaimed.put(Constants.OFFERS_CLAIMED_USER_KEY, ParseUser.getCurrentUser());
                    newClaimed.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                // success
                            } else {
                                Log.e("debug", "failed to claim offer: " + e.getLocalizedMessage());
                            }
                        }
                    });
                } else {
                    Log.e("debug", "failed to retrieve offer: " + e.getLocalizedMessage());
                }
            }
        });
        utils.makeText("Offer redeemed. Look out for an email from us soon!", Toast.LENGTH_LONG);
    }

    private boolean currentUserIsFeatured(boolean isIMP) {
        String classKey = isIMP ? Constants.IMP_FEATURED_CLASS_KEY : Constants.FEATURED_MODELS_CLASS_KEY;
        String userKey = isIMP ? Constants.IMP_FEATURED_USER_KEY : Constants.FEATURED_MODELS_USER_KEY;
        ParseQuery<ParseObject> getFeaturedEntry = ParseQuery.getQuery(classKey);
        getFeaturedEntry.whereEqualTo(userKey, ParseUser.getCurrentUser());
        try {
            ParseObject featuredEntry = getFeaturedEntry.getFirst();
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private void addUserToFeaturedModels(int hours, boolean isIMP) {
        if (isIMP) {
            enterUserInIMPIfNecessary();
        } else {
            enterUserInCompetitionIfNecessary();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        Date expiryDate = calendar.getTime();

        String classKey = isIMP ? Constants.IMP_FEATURED_CLASS_KEY : Constants.FEATURED_MODELS_CLASS_KEY;
        String userKey = isIMP ? Constants.IMP_FEATURED_USER_KEY: Constants.FEATURED_MODELS_USER_KEY;
        String dateKey = isIMP ? Constants.IMP_FEATURED_DATE_KEY : Constants.FEATURED_MODELS_DATE_KEY;
        ParseObject newFeatured = new ParseObject(classKey);
        newFeatured.put(userKey, ParseUser.getCurrentUser());
        newFeatured.put(dateKey, expiryDate);
        newFeatured.saveInBackground();

        if (isIMP) {
            utils.makeText("Success! You are now a featured IMP model for the next " + Integer.toString(hours) + " hours.", Toast.LENGTH_LONG);
        } else {
            utils.makeText("Success! You are now a featured model for the next " + Integer.toString(hours) + " hours.", Toast.LENGTH_LONG);
        }
    }

    private void enterUserInIMPIfNecessary() {
        enterIMPTable();
        enterIMPCompTable();
        enterIMPDetailsTable();
    }

    private void enterIMPTable() {
        ParseQuery<ParseUser> getEntry = ParseQuery.getQuery(Constants.IMP_CLASS_KEY);
        getEntry.whereEqualTo(Constants.IMP_USER_KEY, ParseUser.getCurrentUser());
        try {
            ParseObject entry = getEntry.getFirst();
            // if this line is reached, user is entered in the table
        } catch (ParseException e) {
            // if this line is reached, user is not entered in the table
            final ParseObject newEntry = new ParseObject(Constants.IMP_CLASS_KEY);
            newEntry.put(Constants.IMP_TUTORIAL_COMPLETE_KEY, false);
            newEntry.put(Constants.IMP_USER_KEY, ParseUser.getCurrentUser());
            newEntry.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.e(TAG, "entered into IMP");
                    } else {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                }
            });
        }
    }

    private void enterIMPCompTable() {
        ParseQuery<ParseUser> getEntry = ParseQuery.getQuery(Constants.IMP_COMP_CLASS_KEY);
        getEntry.whereEqualTo(Constants.IMP_COMP_USER_KEY, ParseUser.getCurrentUser());
        try {
            ParseObject entry = getEntry.getFirst();
            // if this line is reached, user is entered in the table
            if (!entry.getBoolean(Constants.IMP_COMP_HAS_ENTERED_KEY)) {
                entry.put(Constants.IMP_COMP_HAS_ENTERED_KEY, true);
                entry.saveInBackground();
            }
        } catch (ParseException e) {
            // if this line is reached, user is not entered in the table
            final ParseObject newEntry = new ParseObject(Constants.IMP_COMP_CLASS_KEY);
            newEntry.put(IMP_COMP_VOTES_KEY, 0);
            newEntry.put(IMP_COMP_USER_KEY, ParseUser.getCurrentUser());
            newEntry.put(IMP_COMP_VOTED_ON_KEY, new JSONArray());
            newEntry.put(IMP_COMP_HAS_ENTERED_KEY, true);
            newEntry.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.e(TAG, "entered into IMPComp");
                    } else {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                }
            });
        }
    }

    private void enterIMPDetailsTable() {
        ParseQuery<ParseUser> getEntry = ParseQuery.getQuery(Constants.IMP_DETAILS_CLASS_KEY);
        getEntry.whereEqualTo(Constants.IMP_DETAILS_USER_KEY, ParseUser.getCurrentUser());
        try {
            ParseObject entry = getEntry.getFirst();
            // if this line is reached, user is entered in the table
        } catch (ParseException e) {
            ParseObject newEntry = new ParseObject(Constants.IMP_DETAILS_CLASS_KEY);
            newEntry.put(Constants.IMP_DETAILS_USER_KEY, ParseUser.getCurrentUser());
            newEntry.put(Constants.IMP_DETAILS_IMAGES_KEY, new JSONArray());
            newEntry.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.e(TAG, "entered into IMPDetails");
                    } else {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                }
            });
        }
    }

    private void enterUserInCompetitionIfNecessary() {
        ParseQuery<ParseObject> getCurrentUserEntry = ParseQuery.getQuery(Constants.SKYPE_COMP_CLASS_KEY);
        getCurrentUserEntry.whereEqualTo(Constants.SKYPE_COMP_USER_KEY, ParseUser.getCurrentUser());
        try {
            ParseObject currentUserEntry = getCurrentUserEntry.getFirst();
            // if this line is reached, user has an entry in the SkypeCompetition table
            if (!currentUserEntry.getBoolean(Constants.SKYPE_COMP_HAS_ENTERED_KEY)) {
                currentUserEntry.put(Constants.SKYPE_COMP_HAS_ENTERED_KEY, true);
                SashidoHelper.giveStars(20, true);
                currentUserEntry.saveInBackground();
            }
        } catch (ParseException e) {
            // if this line is reached, the user has no entry in the SkypeCompetition table
            final ParseObject newEntry = new ParseObject(Constants.SKYPE_COMP_CLASS_KEY);
            newEntry.put(Constants.SKYPE_COMP_USER_KEY, ParseUser.getCurrentUser());
            newEntry.put(Constants.SKYPE_COMP_VOTES_KEY, 0);
            newEntry.put(Constants.SKYPE_COMP_HAS_ENTERED_KEY, true);
            SashidoHelper.giveStars(20, true);
            newEntry.put(Constants.SKYPE_COMP_HAS_SHARED_KEY, false);
            newEntry.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        JSONArray votedOnArray = new JSONArray();
                        votedOnArray.put(newEntry.getObjectId());
                        newEntry.put(Constants.SKYPE_COMP_VOTED_ON_KEY, votedOnArray);
                        newEntry.saveInBackground();
                    }
                }
            });
        }
    }

    private void showCodeDialog(final Offer offer) {
        final Dialog dialog = new Dialog(getContext(), R.style.customAlertDialog);
        dialog.setContentView(R.layout.dialog_offers_code);

        TextView offerTitle = (TextView) dialog.findViewById(R.id.offers_title);
        offerTitle.setText(offer.getTitle());

        final TextView codeView = dialog.findViewById(R.id.offers_code);
        codeView.setText(offer.getCode());
        codeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Offer Code", codeView.getText().toString());
                clipboard.setPrimaryClip(clip);
                Log.e("ClipData", clipboard.toString());
                ((MainActivity) getActivity()).utils.makeText("Copied to clipboard", LENGTH_LONG);
            }
        });

        TextView cancelButton = dialog.findViewById(R.id.cancel_offer_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        TextView goToWebsiteButton = dialog.findViewById(R.id.website_offers_button);
        goToWebsiteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(offer.getWebsiteURL()));
                getActivity().startActivity(i);
                dialog.cancel();
            }
        });

        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mHelper != null) {
            if (!mHelper.handleActivityResult(requestCode,
                    resultCode, data)) {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    public static OffersFragment newInstance(int imageNum) {
        final OffersFragment f = new OffersFragment();
        final Bundle args = new Bundle();
        args.putInt("Position", imageNum);
        f.setArguments(args);
        return f;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            silverCoins.setText(String.valueOf(ParseUser.getCurrentUser().getInt(Constants.USER_SILVER_COINS_KEY)));
            goldCoins.setText(String.valueOf(ParseUser.getCurrentUser().getInt(Constants.USER_GOLD_COINS_KEY)));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            getActivity().unbindService(mServiceConn);
        }
    }
}
