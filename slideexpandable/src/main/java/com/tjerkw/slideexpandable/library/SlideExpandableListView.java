package com.tjerkw.slideexpandable.library;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Simple subclass of listview which does nothing more than wrap
 * any ListAdapter in a SlideExpandalbeListAdapter
 */
class SlideExpandableListView extends ListView {
    private SlideExpandableListAdapter adapter;

    public SlideExpandableListView(Context context) {
        super(context);
    }

    public SlideExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlideExpandableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Collapses the currently open view.
     *
     * @return true if a view was collapsed, false if there was no open view.
     */
    public boolean collapse() {
        if (adapter != null) {
            return adapter.collapseLastOpen();
        }
        return false;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        this.adapter = new SlideExpandableListAdapter(adapter);
        super.setAdapter(this.adapter);
    }

    public void setAdapter(ListAdapter adapter, AbstractSlideExpandableListAdapter.OnItemExpandCollapseListener onItemExpandCollapseListener) {
        this.adapter = new SlideExpandableListAdapter(adapter);
        this.adapter.setItemExpandCollapseListener(onItemExpandCollapseListener);
        super.setAdapter(this.adapter);
    }

    public void setAdapter(ListAdapter adapter, int toggle_button_id, int expandable_view_id) {
        this.adapter = new SlideExpandableListAdapter(adapter, toggle_button_id, expandable_view_id);
        super.setAdapter(this.adapter);
    }

    public void setAdapter(ListAdapter adapter, AbstractSlideExpandableListAdapter.OnItemExpandCollapseListener onItemExpandCollapseListener, int toggle_button_id, int expandable_view_id) {
        this.adapter = new SlideExpandableListAdapter(adapter, toggle_button_id, expandable_view_id);
        this.adapter.setItemExpandCollapseListener(onItemExpandCollapseListener);
        super.setAdapter(this.adapter);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        return adapter.onSaveInstanceState(super.onSaveInstanceState());
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof AbstractSlideExpandableListAdapter.SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        AbstractSlideExpandableListAdapter.SavedState ss = (AbstractSlideExpandableListAdapter.SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        if (adapter != null) {
            adapter.onRestoreInstanceState(ss);
        }
    }
}
