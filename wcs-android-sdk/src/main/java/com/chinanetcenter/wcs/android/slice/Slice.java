package com.chinanetcenter.wcs.android.slice;

public class Slice {

    private byte[] mData;
    private long mOffset;
    private ByteArray mByteArray;

    Slice(long offset, ByteArray byteArray){
        this.mOffset = offset;
        this.mByteArray = byteArray;
    }

    Slice(long offset, byte[] data) {
        mOffset = offset;
        mData = data;
    }

    public long size() {
        if (mByteArray != null) {
            return mByteArray.size();
        } else if (mData != null) {
            return mData.length;
        }
        return 0;
    }

    public byte[] toByteArray() {
        if(null != mByteArray){
            return mByteArray.toBuffer();
        }else if(null != mData){
            return mData;
        }
        return new byte[0];
    }

    public long getOffset() {
        return mOffset;
    }

}
