package org.mariotaku.menucomponent.internal.menu;

import java.util.List;

import org.mariotaku.menucomponent.R;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

public final class MenuAdapter extends ArrayAdapter<MenuItem> {

	private Menu mMenu;

	public MenuAdapter(final Context context) {
		super(context, R.layout.mc__menu_list_item, android.R.id.text1);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public long getItemId(final int index) {
		return getItem(index).getItemId();
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
		final CheckBox checkBox = (CheckBox) view.findViewById(android.R.id.checkbox);
		final MenuItem item = getItem(position);
		icon.setImageDrawable(item.getIcon());
		icon.setVisibility(item.getIcon() != null ? View.VISIBLE : View.GONE);
		checkBox.setVisibility(item.isCheckable() ? View.VISIBLE : View.GONE);
		checkBox.setChecked(item.isChecked());
		return view;
	}

	@Override
	public boolean isEnabled(final int position) {
		return getItem(position).isEnabled();
	}

	public void setMenu(final Menu menu) {
		mMenu = menu;
		setMenuItems();
	}

	void setMenuItems() {
		clear();
		final List<MenuItem> items = mMenu == null ? null : ((MenuImpl) mMenu).getMenuItems();
		if (items == null) return;
		for (final MenuItem item : items) {
			if (item.isVisible()) {
				add(item);
			}
		}
	}

}
