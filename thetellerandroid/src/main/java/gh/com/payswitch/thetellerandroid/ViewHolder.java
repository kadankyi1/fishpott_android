package gh.com.payswitch.thetellerandroid;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import gh.com.payswitch.thetellerandroid.data.ItemModel;

public abstract class ViewHolder extends RecyclerView.ViewHolder {
    public ViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bindType(ItemModel item);
}