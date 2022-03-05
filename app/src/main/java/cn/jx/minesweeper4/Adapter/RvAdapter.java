package cn.jx.minesweeper4.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RvAdapter extends RecyclerView.Adapter<RvAdapter.Holder> {

    private int[] mMap;
    private int mLayoutId;
    private Context mContext;
    private Callback mCallback;
    public static ViewGroup parent;

    public RvAdapter(Context context, int[] page, int layoutId, Callback callback) {
        mContext = context;
        mMap = page;
        mLayoutId = layoutId;
        mCallback = callback;
    }

    @NonNull
    @Override
    public RvAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        return new Holder(LayoutInflater.from(mContext).inflate(mLayoutId, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RvAdapter.Holder holder, int position) {
        mCallback.callback(holder, position);
    }

    @Override
    public int getItemCount() {
        return mMap.length;
    }

    public class Holder extends RecyclerView.ViewHolder {

        public Holder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public abstract static class Callback {

        public View mItemView;
        public ViewGroup mParent;

        public void callback(Holder holder, int position) {
            mParent = parent;
            mItemView = holder.itemView;
        }
    }


}
