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

package org.mariotaku.twidere.activity.support;

import static org.mariotaku.twidere.util.CustomTabUtils.findTabIconKey;
import static org.mariotaku.twidere.util.CustomTabUtils.getIconMap;
import static org.mariotaku.twidere.util.CustomTabUtils.getTabConfiguration;
import static org.mariotaku.twidere.util.CustomTabUtils.getTabTypeName;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.getUserNickname;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.AccountsSpinnerAdapter;
import org.mariotaku.twidere.adapter.ArrayAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.support.BaseSupportDialogFragment;
import org.mariotaku.twidere.model.Account;
import org.mariotaku.twidere.model.CustomTabConfiguration;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.accessor.ViewAccessor;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class CustomTabEditorActivity extends BaseSupportDialogActivity implements OnClickListener {

	private ImageLoaderWrapper mImageLoader;
	private SharedPreferences mPreferences;

	private AccountsSpinnerAdapter mAccountsAdapter;
	private CustomTabIconsAdapter mTabIconsAdapter;

	private View mAccountContainer, mSecondaryFieldContainer;
	private Spinner mTabIconSpinner, mAccountSpinner;
	private EditText mEditTabName;
	private TextView mSecondaryFieldLabel;
	private TextView mTabTypeName;

	private long mTabId;
	private String mTabType;
	private CustomTabConfiguration mTabConfiguration;
	private Object mSecondaryFieldValue;

	@Override
	public void onClick(final View v) {
		final CustomTabConfiguration conf = mTabConfiguration;
		final Object value = mSecondaryFieldValue;
		switch (v.getId()) {
			case R.id.secondary_field: {
				if (conf == null) return;
				switch (conf.getSecondaryFieldType()) {
					case CustomTabConfiguration.FIELD_TYPE_USER: {
						final Intent intent = new Intent(this, UserListSelectorActivity.class);
						intent.setAction(INTENT_ACTION_SELECT_USER);
						intent.putExtra(EXTRA_ACCOUNT_ID, getAccountId());
						startActivityForResult(intent, REQUEST_SELECT_USER);
						break;
					}
					case CustomTabConfiguration.FIELD_TYPE_USER_LIST: {
						final Intent intent = new Intent(this, UserListSelectorActivity.class);
						intent.setAction(INTENT_ACTION_SELECT_USER_LIST);
						intent.putExtra(EXTRA_ACCOUNT_ID, getAccountId());
						startActivityForResult(intent, REQUEST_SELECT_USER_LIST);
						break;
					}
					case CustomTabConfiguration.FIELD_TYPE_TEXT: {
						final int title = conf.getSecondaryFieldTitle();
						SecondaryFieldEditTextDialogFragment.show(this, ParseUtils.parseString(value),
								getString(title > 0 ? title : R.string.content));
						break;
					}
				}
				break;
			}
			case R.id.save: {
				if (!isEditMode()) {
					if (conf == null) return;
					final boolean account_id_required = conf.getAccountRequirement() == CustomTabConfiguration.ACCOUNT_REQUIRED;
					final boolean no_account_id = conf.getAccountRequirement() == CustomTabConfiguration.ACCOUNT_NONE;
					final boolean secondary_field_required = conf.getSecondaryFieldType() != CustomTabConfiguration.FIELD_TYPE_NONE;
					final boolean account_id_invalid = getAccountId() <= 0;
					final boolean secondary_field_invalid = mSecondaryFieldValue == null;
					if (account_id_required && account_id_invalid || secondary_field_required
							&& secondary_field_invalid) {
						Toast.makeText(this, R.string.invalid_settings, Toast.LENGTH_SHORT).show();
						return;
					}
					final Intent data = new Intent();
					final Bundle args = new Bundle();
					if (!no_account_id) {
						args.putLong(EXTRA_ACCOUNT_ID, getAccountId());
					}
					if (secondary_field_required) {
						addSecondaryFieldValueToArguments(args);
					}
					data.putExtra(EXTRA_TYPE, mTabType);
					data.putExtra(EXTRA_NAME, ParseUtils.parseString(mEditTabName.getText()));
					data.putExtra(EXTRA_ICON, getIconKey());
					data.putExtra(EXTRA_ARGUMENTS, ParseUtils.bundleToJSON(args));
					setResult(RESULT_OK, data);
					finish();
				} else {
					if (mTabId < 0) return;
					final Intent data = new Intent();
					data.putExtra(EXTRA_NAME, ParseUtils.parseString(mEditTabName.getText()));
					data.putExtra(EXTRA_ICON, getIconKey());
					data.putExtra(EXTRA_ID, mTabId);
					setResult(RESULT_OK, data);
					finish();
				}
				break;
			}
		}
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mAccountContainer = findViewById(R.id.account_container);
		mSecondaryFieldContainer = findViewById(R.id.secondary_field_container);
		mTabTypeName = (TextView) findViewById(R.id.tab_type_name);
		mEditTabName = (EditText) findViewById(R.id.tab_name);
		mSecondaryFieldLabel = (TextView) findViewById(R.id.secondary_field_label);
		mTabIconSpinner = (Spinner) findViewById(R.id.tab_icon_spinner);
		mAccountSpinner = (Spinner) findViewById(R.id.account_spinner);
	}

	public void setExtraFieldSelectText(final View view, final int text) {
		final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
		final TextView text2 = (TextView) view.findViewById(android.R.id.text2);
		final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
		text1.setVisibility(View.VISIBLE);
		text2.setVisibility(View.GONE);
		icon.setVisibility(View.GONE);
		text1.setText(text);
	}

	public void setExtraFieldView(final View view, final Object value) {
		final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
		final TextView text2 = (TextView) view.findViewById(android.R.id.text2);
		final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
		final boolean display_profile_image = mPreferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true);
		final boolean nickname_only = mPreferences.getBoolean(KEY_NICKNAME_ONLY, false);
		final boolean display_name = mPreferences.getBoolean(KEY_NAME_FIRST, true);
		text1.setVisibility(View.VISIBLE);
		text2.setVisibility(View.VISIBLE);
		icon.setVisibility(display_profile_image ? View.VISIBLE : View.GONE);
		if (value instanceof ParcelableUser) {
			final ParcelableUser user = (ParcelableUser) value;
			final String nick = getUserNickname(this, user.id);
			text1.setText(TextUtils.isEmpty(nick) ? user.name : nickname_only ? nick : getString(
					R.string.name_with_nickname, user.name, nick));
			text2.setText("@" + user.screen_name);
			if (display_profile_image) {
				mImageLoader.displayProfileImage(icon, user.profile_image_url);
			}
		} else if (value instanceof ParcelableUserList) {
			final ParcelableUserList user_list = (ParcelableUserList) value;
			final String created_by;
			if (display_name) {
				created_by = "@" + user_list.user_screen_name;
			} else {
				final String nick = getUserNickname(this, user_list.user_id);
				created_by = TextUtils.isEmpty(nick) ? user_list.user_name : nickname_only ? nick : getString(
						R.string.name_with_nickname, user_list.user_name, nick);
			}
			text1.setText(user_list.name);
			text2.setText(getString(R.string.created_by, created_by));
			if (display_profile_image) {
				mImageLoader.displayProfileImage(icon, user_list.user_profile_image_url);
			}
		} else if (value instanceof CharSequence) {
			text2.setVisibility(View.GONE);
			icon.setVisibility(View.GONE);
			text1.setText((CharSequence) value);
		}
	}

	public void setSecondaryFieldValue(final Object value) {
		mSecondaryFieldValue = value;
		setExtraFieldView(mSecondaryFieldContainer, value);
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (resultCode != RESULT_OK) return;
		switch (requestCode) {
			case REQUEST_SELECT_USER: {
				setSecondaryFieldValue(data.getParcelableExtra(EXTRA_USER));
				break;
			}
			case REQUEST_SELECT_USER_LIST: {
				setSecondaryFieldValue(data.getParcelableExtra(EXTRA_USER_LIST));
				break;
			}
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		mImageLoader = TwidereApplication.getInstance(this).getImageLoaderWrapper();
		final Intent intent = getIntent();
		final String type = mTabType = intent.getStringExtra(EXTRA_TYPE);
		if (type == null) {
			finish();
			return;
		}
		mTabId = intent.getLongExtra(EXTRA_ID, -1);
		setTitle(isEditMode() ? R.string.edit_tab : R.string.add_tab);
		setContentView(R.layout.activity_custom_tab_editor);
		mTabTypeName.setText(getTabTypeName(this, type));
		mTabIconsAdapter = new CustomTabIconsAdapter(this);
		mTabIconsAdapter.setData(getIconMap());
		mAccountsAdapter = new AccountsSpinnerAdapter(this);
		mAccountSpinner.setAdapter(mAccountsAdapter);
		mTabIconSpinner.setAdapter(mTabIconsAdapter);
		final String icon_key;
		if (!isEditMode()) {
			final CustomTabConfiguration conf = mTabConfiguration = getTabConfiguration(type);
			if (conf == null) {
				finish();
				return;
			}
			final boolean has_secondary_field = conf.getSecondaryFieldType() != CustomTabConfiguration.FIELD_TYPE_NONE;
			final boolean account_id_none = conf.getAccountRequirement() == CustomTabConfiguration.ACCOUNT_NONE;
			mAccountContainer.setVisibility(account_id_none ? View.GONE : View.VISIBLE);
			mSecondaryFieldContainer.setVisibility(has_secondary_field ? View.VISIBLE : View.GONE);
			final boolean accountIdRequired = conf.getAccountRequirement() == CustomTabConfiguration.ACCOUNT_REQUIRED;
			if (!accountIdRequired) {
				mAccountsAdapter.add(Account.dummyInstance());
			}
			final boolean officialKeyOnly = intent.getBooleanExtra(EXTRA_OFFICIAL_KEY_ONLY, false);
			mAccountsAdapter.addAll(Account.getAccountsList(this, false, officialKeyOnly));
			switch (conf.getSecondaryFieldType()) {
				case CustomTabConfiguration.FIELD_TYPE_USER: {
					mSecondaryFieldLabel.setText(R.string.user);
					setExtraFieldSelectText(mSecondaryFieldContainer, R.string.select_user);
					break;
				}
				case CustomTabConfiguration.FIELD_TYPE_USER_LIST: {
					mSecondaryFieldLabel.setText(R.string.user_list);
					setExtraFieldSelectText(mSecondaryFieldContainer, R.string.select_user_list);
					break;
				}
				case CustomTabConfiguration.FIELD_TYPE_TEXT: {
					mSecondaryFieldLabel.setText(R.string.content);
					setExtraFieldSelectText(mSecondaryFieldContainer, R.string.input_text);
					break;
				}
			}
			if (conf.getSecondaryFieldTitle() != 0) {
				mSecondaryFieldLabel.setText(conf.getSecondaryFieldTitle());
			}
			icon_key = findTabIconKey(conf.getDefaultIcon());
			mEditTabName.setText(mTabConfiguration.getDefaultTitle());
		} else {
			if (mTabId < 0) {
				finish();
				return;
			}
			mAccountContainer.setVisibility(View.GONE);
			mSecondaryFieldContainer.setVisibility(View.GONE);
			icon_key = intent.getStringExtra(EXTRA_ICON);
			mEditTabName.setText(intent.getStringExtra(EXTRA_NAME));
		}
		final int selection = mTabIconsAdapter.getIconPosition(icon_key);
		mTabIconSpinner.setSelection(selection > 0 ? selection : 0);
	}

	private void addFieldValueToArguments(final Object value, final Bundle args) {
		final CustomTabConfiguration conf = mTabConfiguration;
		if (value == null || args == null || conf == null) return;
		if (value instanceof ParcelableUser) {
			final ParcelableUser user = (ParcelableUser) value;
			args.putLong(EXTRA_USER_ID, user.id);
			args.putString(EXTRA_SCREEN_NAME, user.screen_name);
			args.putString(EXTRA_NAME, user.name);
		} else if (value instanceof ParcelableUserList) {
			final ParcelableUserList user_list = (ParcelableUserList) value;
			args.putInt(EXTRA_LIST_ID, user_list.id);
			args.putString(EXTRA_LIST_NAME, user_list.name);
			args.putLong(EXTRA_USER_ID, user_list.user_id);
			args.putString(EXTRA_SCREEN_NAME, user_list.user_screen_name);
		} else if (value instanceof CharSequence) {
			final String key = conf.getSecondaryFieldTextKey();
			args.putString(TextUtils.isEmpty(key) ? EXTRA_TEXT : key, value.toString());
		}
	}

	private void addSecondaryFieldValueToArguments(final Bundle args) {
		final Object value = mSecondaryFieldValue;
		addFieldValueToArguments(value, args);
	}

	private long getAccountId() {
		final int pos = mAccountSpinner.getSelectedItemPosition();
		if (mAccountSpinner.getCount() > pos && pos >= 0) return mAccountsAdapter.getItem(pos).account_id;
		return -1;
	}

	private String getIconKey() {
		final int pos = mTabIconSpinner.getSelectedItemPosition();
		if (mTabIconsAdapter.getCount() > pos && pos >= 0) return mTabIconsAdapter.getItem(pos).getKey();
		return null;
	}

	private boolean isEditMode() {
		return INTENT_ACTION_EDIT_TAB.equals(getIntent().getAction());
	}

	public static class SecondaryFieldEditTextDialogFragment extends BaseSupportDialogFragment implements
			DialogInterface.OnClickListener {
		private static final String FRAGMENT_TAG_EDIT_SECONDARY_FIELD = "edit_secondary_field";
		private EditText mEditText;

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			final FragmentActivity activity = getActivity();
			if (activity instanceof CustomTabEditorActivity) {
				((CustomTabEditorActivity) activity)
						.setSecondaryFieldValue(ParseUtils.parseString(mEditText.getText()));
			}
		}

		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			final Bundle args = getArguments();
			final Context wrapped = ThemeUtils.getDialogThemedContext(getActivity());
			final AlertDialog.Builder builder = new AlertDialog.Builder(wrapped);
			builder.setTitle(args.getString(EXTRA_TITLE));
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, null);
			final FrameLayout view = new FrameLayout(getActivity());
			mEditText = new EditText(getActivity());
			final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
					FrameLayout.LayoutParams.WRAP_CONTENT);
			lp.leftMargin = lp.topMargin = lp.bottomMargin = lp.rightMargin = getResources().getDimensionPixelSize(
					R.dimen.element_spacing_normal);
			view.addView(mEditText, lp);
			builder.setView(view);
			mEditText.setText(args.getString(EXTRA_TEXT));
			return builder.create();
		}

		public static SecondaryFieldEditTextDialogFragment show(final FragmentActivity activity, final String text,
				final String title) {
			final SecondaryFieldEditTextDialogFragment f = new SecondaryFieldEditTextDialogFragment();
			final Bundle args = new Bundle();
			args.putString(EXTRA_TEXT, text);
			args.putString(EXTRA_TITLE, title);
			f.setArguments(args);
			f.show(activity.getSupportFragmentManager(), FRAGMENT_TAG_EDIT_SECONDARY_FIELD);
			return f;
		}
	}

	static class CustomTabIconsAdapter extends ArrayAdapter<Entry<String, Integer>> {

		private final Resources mResources;

		public CustomTabIconsAdapter(final Context context) {
			super(context, R.layout.spinner_item_custom_tab_icon);
			setDropDownViewResource(R.layout.list_item_two_line_small);
			mResources = context.getResources();
		}

		@Override
		public View getDropDownView(final int position, final View convertView, final ViewGroup parent) {
			final View view = super.getDropDownView(position, convertView, parent);
			view.findViewById(android.R.id.text2).setVisibility(View.GONE);
			final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
			final Entry<String, Integer> item = getItem(position);
			final int value = item.getValue();
			if (value > 0) {
				final String key = item.getKey();
				text1.setText(key.substring(0, 1).toUpperCase(Locale.US) + key.substring(1, key.length()));
			} else {
				text1.setText(R.string.customize);
			}
			bindView(position, item, view);
			return view;
		}

		public int getIconPosition(final String key) {
			if (key == null) return -1;
			for (int i = 0, j = getCount(); i < j; i++) {
				if (key.equals(getItem(i).getKey())) return i;
			}
			return -1;
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			final View view = super.getView(position, convertView, parent);
			bindView(position, getItem(position), view);
			return view;
		}

		public void setData(final Map<String, Integer> map) {
			clear();
			if (map == null) return;
			addAll(map.entrySet());
			sort(new LocationComparator(mResources));
		}

		private void bindView(final int position, final Entry<String, Integer> item, final View view) {
			final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
			final int value = item.getValue();
			if (value > 0) {
				ViewAccessor.setBackground(icon, mResources.getDrawable(value));
			} else {
				ViewAccessor.setBackground(icon, null);
			}
		}

		private static class LocationComparator implements Comparator<Entry<String, Integer>> {
			private final Collator mCollator;

			LocationComparator(final Resources res) {
				mCollator = Collator.getInstance(res.getConfiguration().locale);
			}

			@Override
			public int compare(final Entry<String, Integer> object1, final Entry<String, Integer> object2) {
				if (object1.getValue() <= 0) return Integer.MAX_VALUE;
				if (object2.getValue() <= 0) return Integer.MIN_VALUE;
				return mCollator.compare(object1.getKey(), object2.getKey());
			}

		}

	}

}
