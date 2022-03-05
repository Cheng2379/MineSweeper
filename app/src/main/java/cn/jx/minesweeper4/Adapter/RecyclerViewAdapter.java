package cn.jx.minesweeper4.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private int mSize;
    private List mList;
    private int mLayoutId;
    private Context mContext;
    private Callback mCallback;
    public static ViewGroup parent;


    public RecyclerViewAdapter(Context context, List list, int layoutId, Callback callback) {
        mContext = context;
        mList = list;
        mLayoutId = layoutId;
        mCallback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        return new ViewHolder(LayoutInflater.from(mContext).inflate(mLayoutId,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        mCallback.callback(holder,position);
    }

    @Override
    public int getItemCount() {
        return mSize == 0 ? mList.size() : mSize;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public abstract static class Callback {

        public View mItemView;
        public ViewGroup mParent;

        public void callback(ViewHolder holder, int position) {
            mParent = parent;
            mItemView = holder.itemView;
        }

    }


}
