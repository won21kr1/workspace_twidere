/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.model;

import android.support.v4.app.Fragment;

import org.mariotaku.twidere.Constants;

import java.util.Comparator;
import java.util.Map.Entry;

public final class CustomTabConfiguration implements Constants {

	public static final int FIELD_TYPE_NONE = 0;
	public static final int FIELD_TYPE_USER = 1;
	public static final int FIELD_TYPE_USER_LIST = 2;
	public static final int FIELD_TYPE_TEXT = 3;

	public static final int ACCOUNT_NONE = 0;
	public static final int ACCOUNT_REQUIRED = 1;
	public static final int ACCOUNT_OPTIONAL = 2;

	private final int title, icon, secondaryFieldType, secondaryFieldTitle, sortPosition, accountRequirement;
	private final Class<? extends Fragment> cls;
	private final String secondaryFieldTextKey;
	private final boolean singleTab;

	public CustomTabConfiguration(final Class<? extends Fragment> cls, final int title, final int icon,
			final int accountRequirement, final int secondaryFieldType, final int sortPosition) {
		this(cls, title, icon, accountRequirement, secondaryFieldType, 0, EXTRA_TEXT, sortPosition, false);
	}

	public CustomTabConfiguration(final Class<? extends Fragment> cls, final int title, final int icon,
			final int accountRequirement, final int secondaryFieldType, final int sortPosition, final boolean singleTab) {
		this(cls, title, icon, accountRequirement, secondaryFieldType, 0, EXTRA_TEXT, sortPosition, singleTab);
	}

	public CustomTabConfiguration(final Class<? extends Fragment> cls, final int title, final int icon,
			final int accountRequirement, final int secondaryFieldType, final int secondaryFieldTitle,
			final String secondaryFieldTextKey, final int sortPosition) {
		this(cls, title, icon, accountRequirement, secondaryFieldType, 0, secondaryFieldTextKey, sortPosition, false);
	}

	public CustomTabConfiguration(final Class<? extends Fragment> cls, final int title, final int icon,
			final int accountRequirement, final int secondaryFieldType, final int secondaryFieldTitle,
			final String secondaryFieldTextKey, final int sortPosition, final boolean singleTab) {
		this.cls = cls;
		this.title = title;
		this.icon = icon;
		this.sortPosition = sortPosition;
		this.accountRequirement = accountRequirement;
		this.secondaryFieldType = secondaryFieldType;
		this.secondaryFieldTitle = secondaryFieldTitle;
		this.secondaryFieldTextKey = secondaryFieldTextKey;
		this.singleTab = singleTab;
	}

	public int getAccountRequirement() {
		return accountRequirement;
	}

	public int getDefaultIcon() {
		return icon;
	}

	public int getDefaultTitle() {
		return title;
	}

	public Class<? extends Fragment> getFragmentClass() {
		return cls;
	}

	public String getSecondaryFieldTextKey() {
		return secondaryFieldTextKey;
	}

	public int getSecondaryFieldTitle() {
		return secondaryFieldTitle;
	}

	public int getSecondaryFieldType() {
		return secondaryFieldType;
	}

	public int getSortPosition() {
		return sortPosition;
	}

	public boolean isSingleTab() {
		return singleTab;
	}

	@Override
	public String toString() {
		return "CustomTabConfiguration{title=" + title + ", icon=" + icon + ", secondaryFieldType="
				+ secondaryFieldType + ", secondaryFieldTitle=" + secondaryFieldTitle + ", sortPosition="
				+ sortPosition + ", accountRequirement=" + accountRequirement + ", cls=" + cls
				+ ", secondaryFieldTextKey=" + secondaryFieldTextKey + ", singleTab=" + singleTab + "}";
	}

	public static class CustomTabConfigurationComparator implements Comparator<Entry<String, CustomTabConfiguration>> {

		public static final CustomTabConfigurationComparator SINGLETON = new CustomTabConfigurationComparator();

		@Override
		public int compare(final Entry<String, CustomTabConfiguration> lhs,
				final Entry<String, CustomTabConfiguration> rhs) {
			return lhs.getValue().getSortPosition() - rhs.getValue().getSortPosition();
		}

	}

}
