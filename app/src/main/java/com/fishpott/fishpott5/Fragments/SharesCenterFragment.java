package com.fishpott.fishpott5.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fishpott.fishpott5.Activities.ProfileOfDifferentPottActivity;
import com.fishpott.fishpott5.Activities.SellersListActivity;
import com.fishpott.fishpott5.Activities.StockProfileActivity;
import com.fishpott.fishpott5.Adapters.SharesHosted_DatabaseAdapter;
import com.fishpott.fishpott5.Inc.Config;
import com.fishpott.fishpott5.Inc.Connectivity;
import com.fishpott.fishpott5.ListDataGenerators.ShareHostedListDataGenerators;
import com.fishpott.fishpott5.ListDataGenerators.Notifications_ListDataGenerator;
import com.fishpott.fishpott5.Miscellaneous.Home;
import com.fishpott.fishpott5.Miscellaneous.LocaleHelper;
import com.fishpott.fishpott5.Models.ShareHostedModel;
import com.fishpott.fishpott5.R;
import com.fishpott.fishpott5.Services.NewsFetcherAndPreparerService;

import me.leolin.shortcutbadger.ShortcutBadger;


public class SharesCenterFragment extends Fragment {

    public static RecyclerView mHostedsharesRecyclerView;
    private ImageView mReloadImageView;
    private ProgressBar mLoadingContentProgressBar;
    private Thread imageLoaderThread = null, newsFetchThread = null;
    private SwipeRefreshLayout mMainSwipeRefreshLayout;
    private LinearLayoutManager mLayoutManager;
    private View view;

    public SharesCenterFragment() {}

    public static SharesCenterFragment newInstance() {
        SharesCenterFragment fragment = new SharesCenterFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_sharescenter, container, false);

        newsFetchThread = new Thread(new Runnable() {
            @Override
            public void run() {
                getHostedShares();
            }
        });
        newsFetchThread.start();

        mHostedsharesRecyclerView = view.findViewById(R.id.activity_main_hostedshares_fragment_recyclerview);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mMainSwipeRefreshLayout = view.findViewById(R.id.activity_main_hostedshares_fragment_swiperefreshayout);
        mReloadImageView = view.findViewById(R.id.sharecenterfragment_reload_imageview);
        mLoadingContentProgressBar = view.findViewById(R.id.sharecenterfragment_loader);

        mHostedsharesRecyclerView.setItemViewCacheSize(20);
        mHostedsharesRecyclerView.setDrawingCacheEnabled(true);
        mHostedsharesRecyclerView.setHasFixedSize(true);
        mHostedsharesRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mHostedsharesRecyclerView.setLayoutManager(mLayoutManager);
        mHostedsharesRecyclerView.setAdapter(new RecyclerViewAdapter());

        mReloadImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Connectivity.isConnected(getActivity())){
                    mReloadImageView.setVisibility(View.INVISIBLE);
                    mLoadingContentProgressBar.setVisibility(View.VISIBLE);
                    NewsFetcherAndPreparerService.fetchAvailableHostedShares(getActivity().getApplicationContext(), LocaleHelper.getLanguage(getActivity().getApplicationContext()));
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    newsFetchThread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            getHostedShares();
                                        }
                                    });
                                    newsFetchThread.start();
                                    mLoadingContentProgressBar.setVisibility(View.INVISIBLE);
                                    mHostedsharesRecyclerView.getAdapter().notifyDataSetChanged();
                                }
                            }, 10000);
                        }
                    });
                }
            }
        });

        mMainSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if(Connectivity.isConnected(getActivity())){
                            NewsFetcherAndPreparerService.fetchAvailableHostedShares(getActivity().getApplicationContext(), LocaleHelper.getLanguage(getActivity().getApplicationContext()));
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            newsFetchThread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    getHostedShares();
                                                }
                                            });
                                            newsFetchThread.start();
                                            mMainSwipeRefreshLayout.setRefreshing(false);
                                        }
                                    }, 10000);
                                }
                            });
                        } else {

                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    mMainSwipeRefreshLayout.setRefreshing(false);
                                }
                            });
                        }
                    }
                }
        );

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        Config.freeMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(imageLoaderThread != null){
            imageLoaderThread.interrupt();
            imageLoaderThread = null;
        }
        if(newsFetchThread != null){
            newsFetchThread.interrupt();
            newsFetchThread = null;
        }
        if(view != null){
            Config.unbindDrawables(view.findViewById(R.id.root_sharescenter_fragment));
            view = null;
        }
        Config.freeMemory();
        //Home.getRefWatcher(getActivity()).watch(this);
    }


    public void getHostedShares() {
        // populate the message from the cursor
        if (getActivity() != null) {

            SharesHosted_DatabaseAdapter sharesHostedDatabaseAdapter = new SharesHosted_DatabaseAdapter(getActivity().getApplication());
            sharesHostedDatabaseAdapter.openDatabase();
            Cursor cursor = sharesHostedDatabaseAdapter.getAllRows();
            if (cursor.moveToLast()) {
                if(ShareHostedListDataGenerators.getAllData().size() > 0){
                    ShareHostedListDataGenerators.getAllData().clear();
                }
                do {
                    // Process the data:
                    ShareHostedModel shareHostedModel = new ShareHostedModel();
                    shareHostedModel.setRowId(cursor.getLong(sharesHostedDatabaseAdapter.COL_ROWID));
                    shareHostedModel.setShareId(cursor.getString(sharesHostedDatabaseAdapter.COL_SHARE_PARENT_ID));
                    shareHostedModel.setShareName(cursor.getString(sharesHostedDatabaseAdapter.COL_SHARE_NAME));
                    shareHostedModel.setShareLogo(cursor.getString(sharesHostedDatabaseAdapter.COL_SHARE_LOGO));
                    shareHostedModel.setValuePerShare(cursor.getString(sharesHostedDatabaseAdapter.COL_VALUE_PER_SHARE));
                    shareHostedModel.setDividendPerShare(cursor.getString(sharesHostedDatabaseAdapter.COL_DIVIDEND_PER_SHARE));
                    shareHostedModel.setCompanyName(cursor.getString(sharesHostedDatabaseAdapter.COL_COMPANY_NAME));
                    shareHostedModel.setCompanyPottName(cursor.getString(sharesHostedDatabaseAdapter.COL_COMPANY_POTTNAME));
                    shareHostedModel.setShareInfo(cursor.getString(sharesHostedDatabaseAdapter.COL_INFO_SENT));
                    //ADDING STORY OBJECT TO LIST
                    ShareHostedListDataGenerators.addOneData(shareHostedModel);

                } while (cursor.moveToPrevious());
            }

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(ShareHostedListDataGenerators.getAllData().size() < 1){
                        mReloadImageView.setVisibility(View.VISIBLE);
                    } else {
                        mReloadImageView.setVisibility(View.INVISIBLE);
                    }
                }
            });


            cursor.close();
            sharesHostedDatabaseAdapter.closeDatabase();
            cursor = null;
            sharesHostedDatabaseAdapter = null;

        }
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter{
        @Override
        public int getItemViewType(int position) {
            return 1;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            View v;
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_sharesbuycard, parent, false);
            vh = new SharesBuyCardViewHolder(v);

            return vh;
        }


        public class SharesBuyCardViewHolder extends RecyclerView.ViewHolder {
            private TextView mShareName, mShareInfo, mValuePerShare, mDividendPerShare, mShareCompanyName, mViewCompany;
            private ImageView mSharesLogoImageView;
            private Button mViewValueHistoryButton, mBuyButton;

            private View.OnClickListener innerClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allOnClickHandlers(v, getAdapterPosition());
                }
            };

            public SharesBuyCardViewHolder(View v) {
                super(v);
                mShareName = itemView.findViewById(R.id.shares_name);
                mShareInfo = itemView.findViewById(R.id.shares_info);
                mValuePerShare = itemView.findViewById(R.id.item_for_sale_price_textview);
                mDividendPerShare = itemView.findViewById(R.id.item_for_sale_quantity_textview);
                mShareCompanyName = itemView.findViewById(R.id.shares_offering_company_name);
                mViewCompany = itemView.findViewById(R.id.shares_offering_company_name_view_profile);
                mSharesLogoImageView = itemView.findViewById(R.id.shares_logo_imageview);
                mViewValueHistoryButton = itemView.findViewById(R.id.history_button);
                mBuyButton = itemView.findViewById(R.id.buy_button);

                // ALL ON-CLICK LISTENERS
                mBuyButton.setOnClickListener(innerClickListener);
                mViewValueHistoryButton.setOnClickListener(innerClickListener);
                mViewCompany.setOnClickListener(innerClickListener);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            // LOADING A PROFILE PICTURE IF URL EXISTS
            ((SharesBuyCardViewHolder) holder).mShareName.setText(ShareHostedListDataGenerators.getAllData().get(position).getShareName());
            ((SharesBuyCardViewHolder) holder).mShareInfo.setText(" @" +  ShareHostedListDataGenerators.getAllData().get(position).getShareInfo());
            ((SharesBuyCardViewHolder) holder).mValuePerShare.setText(ShareHostedListDataGenerators.getAllData().get(position).getValuePerShare());
            ((SharesBuyCardViewHolder) holder).mDividendPerShare.setText(ShareHostedListDataGenerators.getAllData().get(position).getDividendPerShare());
            ((SharesBuyCardViewHolder) holder).mShareCompanyName.setText(getString(R.string.offered_by) + " " +  ShareHostedListDataGenerators.getAllData().get(position).getCompanyName());
            imageLoaderThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (ShareHostedListDataGenerators.getAllData().get(position).getShareLogo().trim().length() > 1) {
                        Config.loadUrlImage(getActivity(), true, ShareHostedListDataGenerators.getAllData().get(position).getShareLogo().trim(), ((SharesBuyCardViewHolder) holder).mSharesLogoImageView, 0, 60, 60);
                    }
                }
            });
            imageLoaderThread.start();

        }

        @Override
        public int getItemCount() {
            return ShareHostedListDataGenerators.getAllData().size();
        }

    }

    private void allOnClickHandlers(View v, int position) {
        if(v.getId() == R.id.buy_button){

            String[] buyData = {
                    "",
                    ShareHostedListDataGenerators.getAllData().get(position).getShareId(),
                    ShareHostedListDataGenerators.getAllData().get(position).getShareName(),
                    "",
                    ShareHostedListDataGenerators.getAllData().get(position).getShareLogo(),
                    ""
            };
            Log.e("SHAREPARENTID", "CENTER SIDE ID : " + buyData[1]);
            Log.e("SHAREPARENTID", "CENTER SIDE NAME : " + buyData[2]);
            Log.e("SHAREPARENTID", "CENTER SIDE LOGO : " + buyData[4]);
            Config.openActivity4(getActivity(), SellersListActivity.class, 1, 0, 1, "BUY_INFO", buyData);
        } else if(v.getId() == R.id.history_button){
            Config.openActivity(getActivity(), StockProfileActivity.class, 1, 0, 1, "shareparentid",  ShareHostedListDataGenerators.getAllData().get(position).getShareId());
        } else if(v.getId() == R.id.shares_offering_company_name_view_profile){
            Config.openActivity(getActivity(), ProfileOfDifferentPottActivity.class, 1, 0, 1, "pottname", ShareHostedListDataGenerators.getAllData().get(position).getCompanyPottName());
        }
    }

}
