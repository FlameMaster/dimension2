package com.melvinhou.dimension2.prespace;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.gson.reflect.TypeToken;
import com.melvinhou.dimension2.CYEntity;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.ar.d3.D3Entity;
import com.melvinhou.dimension2.databinding.ActivityListBinding;
import com.melvinhou.dimension2.databinding.ItemAlbumBinding;
import com.melvinhou.dimension2.net.AssetsFileKey;
import com.melvinhou.dimension2.utils.LoadUtils;
import com.melvinhou.kami.adapter.BindRecyclerAdapter;
import com.melvinhou.kami.adapter.BindViewHolder;
import com.melvinhou.kami.adapter.RecyclerAdapter;
import com.melvinhou.kami.mvvm.BindFragment;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.FileUtils;
import com.melvinhou.kami.util.IOUtils;
import com.melvinhou.kami.util.StringCompareUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cc.shinichi.library.ImagePreview;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2022/8/11 0011 16:43
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class PrePictureFragment extends BindFragment<ActivityListBinding, SpacePreModel> {
    @Override
    protected ActivityListBinding openViewBinding(LayoutInflater inflater, ViewGroup container) {
        return ActivityListBinding.inflate(inflater, container, false);
    }

    @Override
    protected Class<SpacePreModel> openModelClazz() {
        return SpacePreModel.class;
    }

    //获取单例
    private static PrePictureFragment fragment;

    public static PrePictureFragment getInstance() {
        if (fragment == null)
            fragment = new PrePictureFragment();
        return fragment;
    }

    private MyAdapter mAdapter;
    //列数
    private final int spanCount = 3;
    private static final String DIRECTORY_PICTURES = ".picture";

    @Override
    public void back() {
        mModel.page.postValue(SpacePreActivity.PAGE_HOME);
    }

    @Override
    protected int upBarMenuID() {
        return R.menu.bar_add;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add) {
            if (mModel.locationType.getValue() != 5)
                openFileChooser();
            else
                FcUtils.showToast("当前目录不支持哦");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initView() {
//        mBinding.container.setBackgroundColor(Color.parseColor("#60000000"));
//        mBinding.barRoot.getRoot().setVisibility(View.GONE);
        mBinding.barRoot.title.setText("");
        mAdapter = new MyAdapter();
        mBinding.container.setAdapter(mAdapter);
        mBinding.container.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        int decoration = DimenUtils.dp2px(12);
        mBinding.container.setPadding(decoration / 2, 0, 0, decoration / 2);
        mBinding.container.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect,
                                       @NonNull View view,
                                       @NonNull RecyclerView parent,
                                       @NonNull RecyclerView.State state) {
//                super.getItemOffsets(outRect, view, parent, state);
                int position = parent.getChildAdapterPosition(view);
                int left = decoration / 2, top = 0, right = decoration / 2, bottom = decoration;
                //设定边距为
                if (position / spanCount == 0) top = decoration;
                outRect.set(left, top, right, bottom);
            }
        });
    }

    @Override
    protected void initListener() {
        mAdapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener<String, BindViewHolder<ItemAlbumBinding>>() {
            @Override
            public void onItemClick(BindViewHolder<ItemAlbumBinding> viewHolder, int position, String data) {
                ImagePreview
                        .getInstance()
                        // 上下文，必须是activity，不需要担心内存泄漏，本框架已经处理好；
                        .setContext(getAct())
                        // 设置从第几张开始看（索引从0开始）
                        .setIndex(0)
                        //=================================================================================================
                        // 有三种设置数据集合的方式，根据自己的需求进行三选一：
                        // 1：第一步生成的imageInfo List
//                      .setImageInfoList(imageInfoList)
                        // 2：直接传url List
                        .setImageList(mAdapter.getDatas())
                        // 3：只有一张图片的情况，可以直接传入这张图片的url
                        //.setImage(String image)
                        //=================================================================================================
                        .setEnableClickClose(false)
                        .setEnableDragClose(true)
                        .setShowDownButton(false)
                        .setEnableUpDragClose(true)
                        .setIndex(position)
                        .setLoadStrategy(ImagePreview.LoadStrategy.NetworkAuto)
                        .setZoomTransitionDuration(500)
                        // 开启预览
                        .start();
            }
        });
    }

    @SuppressLint("CheckResult")
    @Override
    protected void initData() {
        //本地
//        loadList(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
        mModel.locationType.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer type) {
                mAdapter.clearData();
                if (type == 5) {
                    Uri uri = mModel.useLocationUri.getValue();
                    if (uri != null)
                        loadList(uri);
                } else {
                    loadList(getDirPath(type));
                }
            }
        });
    }

    //获取目录
    private String getDirPath(int type) {
        if (type == 1) {
            return getContext().getFilesDir().getAbsolutePath() + File.separator + DIRECTORY_PICTURES;
        } else if (type != 5) {
            File[] files = getContext().getExternalFilesDirs(DIRECTORY_PICTURES);
            if (type - 2 < files.length)
                return files[type - 2].getAbsolutePath();
        }
        return "";
    }


    private void loadList(String path) {
        Log.e("加载目录", "path=" + path);
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (dir.isDirectory()) {// 处理目录
            File[] files = dir.listFiles();
            for (File file : files) {
                Log.e("文件", "file=" + file.getPath());
                mAdapter.addData(file.getAbsolutePath());
            }
        }
    }

    private void loadList(Uri uri) {
        DocumentFile documentFile = DocumentFile.fromTreeUri(getContext(), uri);
        DocumentFile[] documentFiles = documentFile.listFiles();
        for (DocumentFile file : documentFiles) {
            if (file.isFile() && file.canRead() && StringCompareUtils.isImageUrl(file.getName())) {
                String path = file.getUri().toString();
                Log.e("文件", "file=" + path);
                mAdapter.addData(path);
            }
        }
    }

    private String FILE_UNSPECIFIED = "image/*"; //文件查找格式
    //管理进程的
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    //文件选择器
    private void openFileChooser() {
        Intent intent = new Intent();
        //设置类型
        intent.setType(FILE_UNSPECIFIED);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);//多文件
        toResultActivity(Intent.createChooser(intent, "选择文件"));
    }

    @Override
    protected void onActivityBack(ActivityResult result) {
        super.onActivityBack(result);
        //此处进行数据接收（接收回调）
        if (result.getResultCode() == Activity.RESULT_OK) {
            if (result.getData().getData() != null) {
                copyList(new Uri[]{result.getData().getData()});
            }
            ClipData clipData = result.getData().getClipData();
            if (clipData != null) {
                Uri[] uris = new Uri[clipData.getItemCount()];
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    uris[i] = clipData.getItemAt(i).getUri();
                }
                copyList(uris);
            }
        }
    }

    /**
     * 复制文件
     *
     * @param uris
     */
    private void copyList(final Uri[] uris) {
        String path = getDirPath(mModel.locationType.getValue());
        if (TextUtils.isEmpty(path)) return;
        showProcess("文件复制中...");
        mDisposable.add(Observable.create((ObservableOnSubscribe<String>) emitter -> {
            for (Uri uri : uris) {
                FileUtils.copyfile(uri, new File(path, getRealFileName(uri)), false);
            }
            emitter.onNext("复制成功");
            emitter.onComplete();
        })
                .compose(IOUtils.setThread())
                .subscribe(str -> {
                    hideProcess();
                    mAdapter.clearData();
                    loadList(path);
                }));
    }

    private static String getRealFileName(final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            String[] filePathColumn = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
            Cursor cursor = FcUtils.getContext().getContentResolver()
                    .query(uri, filePathColumn, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndexOrThrow(filePathColumn[1]);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDisposable.dispose();
        mDisposable.clear();
    }

    class MyAdapter extends BindRecyclerAdapter<String, ItemAlbumBinding> {

        @Override
        protected ItemAlbumBinding getViewBinding(@NonNull LayoutInflater inflater, ViewGroup parent) {
            return ItemAlbumBinding.inflate(inflater, parent, false);
        }

        @Override
        protected void bindData(@NonNull ItemAlbumBinding binding, int position, @NonNull String data) {
            Glide.with(FcUtils.getContext()).load(data).into(binding.itemImg);
        }
    }
}