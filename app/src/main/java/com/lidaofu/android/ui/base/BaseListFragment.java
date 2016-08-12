package com.lidaofu.android.ui.base;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.widget.ListView;

import com.lidaofu.android.R;
import com.lidaofu.android.adapter.BaseViewAdapter;
import com.lidaofu.android.adapter.BaseViewHolder;
import com.lidaofu.android.mode.Entity;
import com.lidaofu.android.mode.PagerInfo;
import com.lidaofu.android.presenter.BaseListFragmentPresenter;
import com.lidaofu.android.presenter.imp.BaseListFragmentPresenterImp;
import com.lidaofu.android.view.ErrorView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import in.srain.cube.util.LocalDisplay;
import in.srain.cube.views.loadmore.LoadMoreContainer;
import in.srain.cube.views.loadmore.LoadMoreHandler;
import in.srain.cube.views.loadmore.LoadMoreListViewContainer;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.StoreHouseHeader;

/**
 * Created by LiDaofu on 16/8/11.
 */
public abstract class BaseListFragment<M extends Entity, V extends BaseViewHolder> extends BaseFragment implements PtrHandler, LoadMoreHandler, BaseListFragmentPresenter.BaseListFragmentView,ErrorView.ErrorInterface {


    @Bind(R.id.list_fragment)
    protected ListView listFragment;
    @Bind(R.id.ptr_frame_load_more)
    LoadMoreListViewContainer ptrFrameLoadMore;
    @Bind(R.id.ptr_frame_list)
    PtrClassicFrameLayout ptrFrameList;
    @Bind(R.id.error_view)
    ErrorView errorView;

    protected BaseViewAdapter baseAdapter;
    private BaseListFragmentPresenter presenter;
    private boolean isFirstLoad = false;
    private List<M> totalList;

    /**
     * 返回list的adapter
     *
     * @return
     */
    protected abstract BaseViewAdapter<M, V> getListAdapter();

    /**
     * 第一次加载时的url
     * @return
     */
    protected abstract String getFirstLoadUrl();

    /**
     * 加载更多的url
     * @return
     */
    protected abstract String getLoadMoreUrl();


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_base_list;
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void setupView(View view) {

        ptrFrameList.setLoadingMinTime(500);
        StoreHouseHeader header = new StoreHouseHeader(getActivity());
        header.setPadding(0, LocalDisplay.dp2px(20), 0, LocalDisplay.dp2px(20));
        header.initWithString(getResources().getString(R.string.app_name), 32);
        int color = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ? getResources().getColor(R.color.colorPrimary, null) : getResources().getColor(R.color.colorPrimary);
        header.setTextColor(color);
        ptrFrameList.setHeaderView(header);
        ptrFrameList.addPtrUIHandler(header);
        baseAdapter = getListAdapter();
        listFragment.setAdapter(baseAdapter);
        totalList = new ArrayList<>();
        baseAdapter.setTotalList(totalList);

        errorView.setEmptyType(ErrorView.ErrorView_Load);

        presenter = new BaseListFragmentPresenterImp<M>(this);
        presenter.loadData();
        isFirstLoad = true;
    }


    @Override
    public String getHttpUrl() {
        return (isFirstLoad)?getFirstLoadUrl():getLoadMoreUrl();
    }

    @Override
    public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
        return PtrDefaultHandler.checkContentCanBePulledDown(frame, listFragment, header);
    }

    /**
     * 刷新数据
     *
     * @param frame
     */
    @Override
    public void onRefreshBegin(PtrFrameLayout frame) {
        if(totalList!=null){
            baseAdapter.removeDataAll();
            presenter.loadData();
        }
    }


    /**
     * 加载更多
     *
     * @param loadMoreContainer
     */
    @Override
    public void onLoadMore(LoadMoreContainer loadMoreContainer) {
        if(presenter!=null){
            presenter.loadData();
        }
    }

    @Override
    public void onPreExecute() {
        if (isFirstLoad) {
            errorView.setEmptyType(ErrorView.ErrorView_Load);
        }
    }

    @Override
    public void onPostExecute() {

    }

    @Override
    public void onLoadError() {
        if(isFirstLoad){
            errorView.setEmptyType(ErrorView.ErrorView_Error);
        }
    }

    @Override
    public void response(PagerInfo data) {

        if (isFirstLoad && data != null && data.getPagerInfos().size() != 0) {
            errorView.setEmptyType(ErrorView.ErrorView_GONE);
            isFirstLoad = false;
        } else if (isFirstLoad && data != null && data.getPagerInfos().size() == 0) {
            errorView.setEmptyType(ErrorView.ErrorView_EMPTY);
            isFirstLoad = true;
        }
        List<M> list = data.getPagerInfos();
        if (list != null && list.size() != 0) {
            totalList.addAll(list);
        }
        baseAdapter.notifyDataSetChanged();
    }

    /**
     * 加载失败,点击重新加载
     */
    @Override
    public void restLoad() {
        if(presenter!=null){
            presenter.loadData();
        }
    }
}