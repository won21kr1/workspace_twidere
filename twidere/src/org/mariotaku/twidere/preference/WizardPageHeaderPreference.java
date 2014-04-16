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

package org.mariotaku.twidere.preference;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.mariotaku.twidere.R;

public final class WizardPageHeaderPreference extends Preference {

	public WizardPageHeaderPreference(final Context context) {
		this(context, null);
	}

	public WizardPageHeaderPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public WizardPageHeaderPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		setLayoutResource(R.layout.wizard_page_header_item);
		setSelectable(false);
	}

	@Override
	protected void onBindView(final View view) {
		super.onBindView(view);
		final TextView title = (TextView) view.findViewById(android.R.id.title);
		final TextView summary = (TextView) view.findViewById(android.R.id.summary);
		title.setText(getTitle());
		summary.setText(getSummary());
	}

}
