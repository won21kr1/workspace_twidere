package org.mariotaku.menucomponent.widget;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.menucomponent.R;
import org.mariotaku.menucomponent.internal.Utils;
import org.mariotaku.menucomponent.internal.menu.MenuUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MenuBar extends LinearLayout implements MenuItem.OnMenuItemClickListener {

	private final Menu mMenu;
	private final Context mContext;
	private final LayoutInflater mLayoutInflater;

	private OnMenuItemClickListener mItemClickListener;
	private PopupMenu mPopupMenu;
	private boolean mIsBottomBar;
	private int mMaxItemsShown;

	public MenuBar(final Context context) {
		this(context, null);
	}

	public MenuBar(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		final TypedArray a = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.max });
		mMaxItemsShown = a.getInt(0, getResources().getInteger(R.integer.mc__max_action_buttons));
		a.recycle();
		mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
		mMenu = MenuUtils.createMenu(context);
		setOrientation(HORIZONTAL);
	}

	public Menu getMenu() {
		return mMenu;
	}

	public MenuInflater getMenuInflater() {
		return new MenuInflater(mContext);
	}

	/**
	 * Get listener for action item clicked.
	 * 
	 */
	public OnMenuItemClickListener getOnMenuItemClickListener() {
		return mItemClickListener;
	}

	public void inflate(final int menuRes) {
		mMenu.clear();
		new MenuInflater(mContext).inflate(menuRes, mMenu);
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
		if (mItemClickListener != null) return mItemClickListener.onMenuItemClick(item);
		return false;
	}

	public void setIsBottomBar(final boolean isBottomBar) {
		mIsBottomBar = isBottomBar;
	}

	public void setMaxItemsShown(final int maxItemsShown) {
		mMaxItemsShown = maxItemsShown;
	}

	/**
	 * Set listener for action item clicked.
	 * 
	 * @param listener Listener
	 */
	public void setOnMenuItemClickListener(final OnMenuItemClickListener listener) {
		mItemClickListener = listener;
	}

	@SuppressLint("InlinedApi")
	public void show() {
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		removeAllViews();
		int actionButtonCount = 0;
		final ArrayList<MenuItem> itemsNotShowing = new ArrayList<MenuItem>();
		for (int i = 0, j = mMenu.size(); i < j; i++) {
			final MenuItem item = mMenu.getItem(i);
			final int showAsActionFlags = MenuUtils.getShowAsActionFlags(item);
			final boolean showIfRoom = (showAsActionFlags & MenuItem.SHOW_AS_ACTION_IF_ROOM) != 0;
			final boolean showAlways = (showAsActionFlags & MenuItem.SHOW_AS_ACTION_ALWAYS) != 0;
			if (showIfRoom && actionButtonCount < mMaxItemsShown || showAlways) {
				if (item.isVisible()) {
					addViewToMenuBar(createViewForMenuItem(item));
					actionButtonCount++;
				}
			} else {
				itemsNotShowing.add(item);
			}
		}
		if (hasVisibleItems(itemsNotShowing)) {
			addViewToMenuBar(createMoreOverflowButton(itemsNotShowing));
		}
		for (int i = 0, childCount = getChildCount(); i < childCount; i++) {
			final LinearLayout child = (LinearLayout) getChildAt(i);
			final View itemView = child.getChildAt(0);
			final LinearLayout.LayoutParams lp = (LayoutParams) itemView.getLayoutParams();
			if (mIsBottomBar) {
				if (childCount < 2) {
					lp.weight = 0;
				} else if (childCount == 2 || childCount == 3) {
					if (i == 0) {
						child.setGravity(Gravity.LEFT);
					} else if (i == childCount - 1) {
						child.setGravity(Gravity.RIGHT);
					}
					lp.weight = 0;
				} else {
					lp.weight = 1;
				}
			} else {
				lp.weight = 0;
			}
			itemView.setLayoutParams(lp);
		}
		invalidate();
	}

	@Override
	protected void onDetachedFromWindow() {
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		super.onDetachedFromWindow();
	}

	private void addViewToMenuBar(final View itemView) {
		final LinearLayout itemContainer = new LinearLayout(mContext);
		final LayoutParams itemContainerParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		itemContainerParams.weight = 1;
		final LayoutParams itemViewParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		itemViewParams.weight = 0;
		itemContainer.addView(itemView, itemViewParams);
		itemContainer.setGravity(Gravity.CENTER);
		addView(itemContainer, itemContainerParams);
	}

	private View createMoreOverflowButton(final ArrayList<MenuItem> itemsNotShowing) {
		final View view = mLayoutInflater.inflate(R.layout.mc__menubar_more_overflow_item, this, false);
		view.setOnClickListener(new MoreOverflowOnClickListener(itemsNotShowing, this));
		return view;
	}

	@SuppressLint("InlinedApi")
	private View createViewForMenuItem(final MenuItem item) {
		final View actionView = MenuItemCompat.getActionView(item), view;
		if (actionView != null) {
			view = actionView;
		} else {
			final int showAsActionFlags = MenuUtils.getShowAsActionFlags(item);
			final Drawable icon = item.getIcon();
			final CharSequence title = item.getTitle();
			final boolean isEnabled = item.isEnabled();
			final boolean showText = (showAsActionFlags & MenuItem.SHOW_AS_ACTION_WITH_TEXT) != 0;
			final boolean hasIcon = icon != null, hasTitle = !TextUtils.isEmpty(title);
			view = mLayoutInflater.inflate(R.layout.mc__menubar_item, this, false);
			view.setOnClickListener(isEnabled ? new ActionViewOnClickListener(item, this) : null);
			view.setOnLongClickListener(isEnabled && !showText ? new OnActionItemLongClickListener(item, this) : null);
			final ImageView iconView = (ImageView) view.findViewById(android.R.id.icon);
			final TextView titleView = (TextView) view.findViewById(android.R.id.title);
			iconView.setVisibility(hasIcon ? View.VISIBLE : View.GONE);
			iconView.setImageDrawable(icon);
			iconView.setContentDescription(item.getTitle());
			titleView.setText(title);
			titleView.setVisibility(hasTitle && (showText || !hasIcon) ? View.VISIBLE : View.GONE);
		}
		return view;
	}

	private boolean isBottomBar() {
		return mIsBottomBar;
	}

	private void showPopupMenu(final PopupMenu popupMenu) {
		if (popupMenu == null) return;
		if (mPopupMenu != null && mPopupMenu.isShowing()) {
			mPopupMenu.dismiss();
		}
		mPopupMenu = popupMenu;
		if (!popupMenu.isShowing()) {
			mPopupMenu.show();
		}
	}

	private static boolean hasVisibleItems(final List<MenuItem> menuItems) {
		for (final MenuItem item : menuItems) {
			if (item.isVisible()) return true;
		}
		return false;
	}

	private static class ActionViewOnClickListener implements OnClickListener {
		private final MenuItem menuItem;
		private final MenuBar menuBar;

		private ActionViewOnClickListener(final MenuItem menuItem, final MenuBar menuBar) {
			this.menuItem = menuItem;
			this.menuBar = menuBar;
		}

		@Override
		public void onClick(final View actionView) {
			if (!menuItem.isEnabled()) return;
			if (menuItem.hasSubMenu()) {
				final PopupMenu popupMenu = PopupMenu.getInstance(actionView.getContext(), actionView);
				popupMenu.setOnMenuItemClickListener(menuBar);
				popupMenu.setMenu(menuItem.getSubMenu());
				menuBar.showPopupMenu(popupMenu);
			} else {
				final OnMenuItemClickListener listener = menuBar.getOnMenuItemClickListener();
				if (listener != null) {
					listener.onMenuItemClick(menuItem);
				}
			}
		}
	}

	private static class MoreOverflowOnClickListener implements OnClickListener {
		private final ArrayList<MenuItem> menuItems;
		private final MenuBar menuBar;

		private MoreOverflowOnClickListener(final ArrayList<MenuItem> menuItems, final MenuBar menuBar) {
			this.menuItems = menuItems;
			this.menuBar = menuBar;
		}

		@Override
		public void onClick(final View actionView) {
			if (!hasVisibleItems(menuItems)) return;
			final PopupMenu popupMenu = PopupMenu.getInstance(actionView.getContext(), actionView);
			popupMenu.setOnMenuItemClickListener(menuBar);
			popupMenu.setMenu(MenuUtils.createMenu(menuBar.getContext(), menuItems));
			menuBar.showPopupMenu(popupMenu);
		}

	}

	private static class OnActionItemLongClickListener implements OnLongClickListener {

		private final MenuItem item;
		private final MenuBar menuBar;

		private OnActionItemLongClickListener(final MenuItem item, final MenuBar menuBar) {
			this.item = item;
			this.menuBar = menuBar;
		}

		@Override
		public boolean onLongClick(final View v) {
			// Don't show the cheat sheet for items that already show text.
			if ((MenuUtils.getShowAsActionFlags(item) & MenuItem.SHOW_AS_ACTION_WITH_TEXT) != 0) return false;
			Utils.showMenuItemToast(v, item.getTitle(), menuBar.isBottomBar());
			return true;
		}
	}

}
