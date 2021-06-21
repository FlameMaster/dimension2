package com.melvinhou.kami.model;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.melvinhou.kami.util.FcUtils;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/12/25 10:22
 * <p>
 * = 分 类 说 明：uri返回格式
 * ============================================================
 */
public class UriFileInfo {

    public UriFileInfo(){

    }

    public UriFileInfo(Uri uri){
        setUri(uri);
        setFileName(getRealFileName(uri));
    }

    public UriFileInfo(Uri uri, String fileName){
        setUri(uri);
        setFileName(fileName);
    }

    public UriFileInfo(Uri uri, String fileName, String filePath){
        setUri(uri);
        setFileName(fileName);
        setFilePath(filePath);
    }

    public UriFileInfo(Uri uri, String fileName, String filePath, String id){
        setUri(uri);
        setFileName(fileName);
        setFilePath(filePath);
        setId(id);
    }

    private Uri uri;
    private String fileName;
    private String filePath;
    private String id;



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
                    int index = cursor.getColumnIndex(filePathColumn[1]);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
