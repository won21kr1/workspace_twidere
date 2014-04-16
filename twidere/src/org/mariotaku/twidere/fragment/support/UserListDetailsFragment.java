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

package org.mariotaku.twidere.fragment.support;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.addIntentToMenu;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getDisplayName;
import static org.mariotaku.twidere.util.Utils.getLocalizedNumber;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.openUserListMembers;
import static org.mariotaku.twidere.util.Utils.openUserListSubscribers;
import static org.mariotaku.twidere.util.Utils.openUserListTimeline;
import static org.mariotaku.twidere.util.Utils.openUserProfile;
import static org.mariotaku.twidere.util.Utils.setMenuItemAvailability;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.mariotaku.menucomponent.widget.PopupMenu;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.UserListSelectorActivity;
import org.mariotaku.twidere.adapter.ListActionAdapter;
import org.mariotaku.twidere.model.ListAction;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.OnLinkClickHandler;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.view.ColorLabelRelativeLayout;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserList;

import java.util.Locale;

public class UserListDetailsFragment extends BaseSupportListFragment implements OnClickListener, OnItemClickListener,
		OnItemLongClickListener, OnMenuItemClickListener, LoaderCallbacks<SingleResponse<ParcelableUserList>>,
		Panes.Right {

	private ImageLoaderWrapper mProfileImageLoader;
	private AsyncTwitterWrapper mTwitterWrapper;

	private ImageView mProfileImageView;
	private TextView mListNameView, mCreatedByView, mDescriptionView, mErrorMessageView;
	private View mListContainer, mErrorRetryContainer;
	private ColorLabelRelativeLayout mProfileContainer;
	private View mDescriptionContainer, mMoreOptionsButton;
	private Button mRetryButton;
	private ListView mListView;
	private View mHeaderView;

	private ListActionAdapter mAdapter;

	private PopupMenu mPopupMenu;

	private ParcelableUserList mUserList;
	private Locale mLocale;

	private boolean mUserListLoaderInitialized;

	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getActivity() == null || !isAdded() || isDetached()) return;
			final String action = intent.getAction();
			final ParcelableUserList user_list = intent.getParcelableExtra(EXTRA_USER_LIST);
			if (user_list == null || mUserList == null || !intent.getBooleanExtra(EXTRA_SUCCEED, false)) return;
			if (BROADCAST_USER_LIST_DETAILS_UPDATED.equals(action)) {
				if (user_list.id == mUserList.id) {
					getUserListInfo(true);
				}
			} else if (BROADCAST_USER_LIST_SUBSCRIBED.equals(action) || BROADCAST_USER_LIST_UNSUBSCRIBED.equals(action)) {
				if (user_list.id == mUserList.id) {
					getUserListInfo(true);
				}
			}
		}
	};

	public void displayUserList(final ParcelableUserList user_list) {
		if (user_list == null || getActivity() == null) return;
		getLoaderManager().destroyLoader(0);
		final boolean is_myself = user_list.account_id == user_list.user_id;
		mErrorRetryContainer.setVisibility(View.GONE);
		mUserList = user_list;
		mProfileContainer.drawEnd(getAccountColor(getActivity(), user_list.account_id));
		mListNameView.setText(user_list.name);
		final String display_name = getDisplayName(getActivity(), user_list.user_id, user_list.user_name,
				user_list.user_screen_name, false);
		mCreatedByView.setText(getString(R.string.created_by, display_name));
		final String description = user_list.description;
		mDescriptionContainer.setVisibility(is_myself || !isEmpty(description) ? View.VISIBLE : View.GONE);
		mDescriptionView.setText(description);
		final TwidereLinkify linkify = new TwidereLinkify(
				new OnLinkClickHandler(getActivity(), getMultiSelectManager()));
		linkify.applyAllLinks(mDescriptionView, user_list.account_id, false);
		mDescriptionView.setMovementMethod(LinkMovementMethod.getInstance());
		mProfileImageLoader.displayProfileImage(mProfileImageView, user_list.user_profile_image_url);
		mAdapter.notifyDataSetChanged();
		invalidateOptionsMenu();
	}

	public void getUserListInfo(final boolean omit_intent_extra) {
		final LoaderManager lm = getLoaderManager();
		lm.destroyLoader(0);
		final Bundle args = new Bundle(getArguments());
		args.putBoolean(EXTRA_OMIT_INTENT_EXTRA, omit_intent_extra);
		if (!mUserListLoaderInitialized) {
			lm.initLoader(0, args, this);
			mUserListLoaderInitialized = true;
		} else {
			lm.restartLoader(0, args, this);
		}
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		mTwitterWrapper = getApplication().getTwitterWrapper();
		mLocale = getResources().getConfiguration().locale;
		mProfileImageLoader = getApplication().getImageLoaderWrapper();
		mAdapter = new ListActionAdapter(getActivity());
		mAdapter.add(new ListTimelineAction(1));
		mAdapter.add(new ListMembersAction(2));
		mAdapter.add(new ListSubscribersAction(3));
		mProfileImageView.setOnClickListener(this);
		mProfileContainer.setOnClickListener(this);
		mMoreOptionsButton.setOnClickListener(this);
		mRetryButton.setOnClickListener(this);
		setListAdapter(null);
		mListView = getListView();
		mListView.addHeaderView(mHeaderView, null, false);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		setListAdapter(mAdapter);
		getUserListInfo(false);
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
			case REQUEST_SELECT_USER: {
				if (resultCode != Activity.RESULT_OK || !data.hasExtra(EXTRA_USER) || mTwitterWrapper == null
						|| mUserList == null) return;
				final ParcelableUser user = data.getParcelableExtra(EXTRA_USER);
				mTwitterWrapper.addUserListMembersAsync(mUserList.account_id, mUserList.id, user);
				return;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(final View view) {
		switch (view.getId()) {
			case R.id.more_options: {
				if (mUserList == null) return;
				mPopupMenu = PopupMenu.getInstance(getActivity(), view);
				mPopupMenu.inflate(R.menu.action_user_list);
				final Menu menu = mPopupMenu.getMenu();
				final Intent extensionsIntent = new Intent(INTENT_ACTION_EXTENSION_OPEN_USER_LIST);
				extensionsIntent.setExtrasClassLoader(getActivity().getClassLoader());
				extensionsIntent.putExtra(EXTRA_USER_LIST, mUserList);
				addIntentToMenu(getActivity(), menu, extensionsIntent);
				mPopupMenu.setOnMenuItemClickListener(this);
				mPopupMenu.show();
				break;
			}
			case R.id.retry: {
				getUserListInfo(true);
				break;
			}
			case R.id.profile_image: {
				if (mUserList == null) return;
				openUserProfile(getActivity(), mUserList.account_id, mUserList.user_id, mUserList.user_screen_name);
				break;
			}
		}

	}

	@Override
	public Loader<SingleResponse<ParcelableUserList>> onCreateLoader(final int id, final Bundle args) {
		mListContainer.setVisibility(View.VISIBLE);
		mErrorMessageView.setText(null);
		mErrorMessageView.setVisibility(View.GONE);
		mErrorRetryContainer.setVisibility(View.GONE);
		setListShown(false);
		setProgressBarIndeterminateVisibility(true);
		final long accountId = args != null ? args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
		final long userId = args != null ? args.getLong(EXTRA_USER_ID, -1) : -1;
		final int listId = args != null ? args.getInt(EXTRA_LIST_ID, -1) : -1;
		final String listName = args != null ? args.getString(EXTRA_LIST_NAME) : null;
		final String screenName = args != null ? args.getString(EXTRA_SCREEN_NAME) : null;
		final boolean omitIntentExtra = args != null ? args.getBoolean(EXTRA_OMIT_INTENT_EXTRA, true) : true;
		return new ParcelableUserListLoader(getActivity(), omitIntentExtra, getArguments(), accountId, listId,
				listName, userId, screenName);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.menu_user_list, menu);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		mHeaderView = inflater.inflate(R.layout.user_list_details_header, null);
		mProfileContainer = (ColorLabelRelativeLayout) mHeaderView.findViewById(R.id.profile);
		mListNameView = (TextView) mHeaderView.findViewById(R.id.list_name);
		mCreatedByView = (TextView) mHeaderView.findViewById(R.id.created_by);
		mDescriptionView = (TextView) mHeaderView.findViewById(R.id.description);
		mProfileImageView = (ImageView) mHeaderView.findViewById(R.id.profile_image);
		mDescriptionContainer = mHeaderView.findViewById(R.id.description_container);
		mMoreOptionsButton = mHeaderView.findViewById(R.id.more_options);
		mListContainer = super.onCreateView(inflater, container, savedInstanceState);
		final View containerView = inflater.inflate(R.layout.fragment_details_page, null);
		((FrameLayout) containerView.findViewById(R.id.details_container)).addView(mListContainer);
		mErrorRetryContainer = containerView.findViewById(R.id.error_retry_container);
		mRetryButton = (Button) containerView.findViewById(R.id.retry);
		mErrorMessageView = (TextView) containerView.findViewById(R.id.error_message);
		final View cardView = mHeaderView.findViewById(R.id.card);
		ThemeUtils.applyThemeAlphaToDrawable(cardView.getContext(), cardView.getBackground());
		return containerView;
	}

	@Override
	public void onDestroyView() {
		mUserList = null;
		getLoaderManager().destroyLoader(0);
		super.onDestroyView();
	}

	@Override
	public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
		final ListAction action = mAdapter.findItem(id);
		if (action != null) {
			action.onClick();
		}
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
		final ListAction action = mAdapter.findItem(id);
		if (action != null) return action.onLongClick();
		return false;
	}

	@Override
	public void onLoaderReset(final Loader<SingleResponse<ParcelableUserList>> loader) {

	}

	@Override
	public void onLoadFinished(final Loader<SingleResponse<ParcelableUserList>> loader,
			final SingleResponse<ParcelableUserList> data) {
		if (data == null) return;
		if (getActivity() == null) return;
		if (data.data != null) {
			final ParcelableUserList list = data.data;
			setListShown(true);
			displayUserList(list);
			mErrorRetryContainer.setVisibility(View.GONE);
		} else {
			if (data.exception != null) {
				mErrorMessageView.setText(data.exception.getMessage());
				mErrorMessageView.setVisibility(View.VISIBLE);
			}
			mListContainer.setVisibility(View.GONE);
			mErrorRetryContainer.setVisibility(View.VISIBLE);
		}
		setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ADD: {
				if (mUserList == null || mUserList.user_id != mUserList.account_id) return false;
				final Intent intent = new Intent(INTENT_ACTION_SELECT_USER);
				intent.setClass(getActivity(), UserListSelectorActivity.class);
				intent.putExtra(EXTRA_ACCOUNT_ID, mUserList.account_id);
				startActivityForResult(intent, REQUEST_SELECT_USER);
				break;
			}
			case MENU_DELETE: {
				if (mUserList.user_id != mUserList.account_id) return false;
				DestroyUserListDialogFragment.show(getFragmentManager(), mUserList);
				break;
			}
			default: {
				if (item.getIntent() != null) {
					try {
						startActivity(item.getIntent());
					} catch (final ActivityNotFoundException e) {
						Log.w(LOGTAG, e);
						return false;
					}
				}
				break;
			}
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_EDIT: {
				if (mUserList == null) return false;
				final Bundle args = new Bundle();
				args.putLong(EXTRA_ACCOUNT_ID, mUserList.account_id);
				args.putString(EXTRA_LIST_NAME, mUserList.name);
				args.putString(EXTRA_DESCRIPTION, mUserList.description);
				args.putBoolean(EXTRA_IS_PUBLIC, mUserList.is_public);
				args.putInt(EXTRA_LIST_ID, mUserList.id);
				final DialogFragment f = new EditUserListDialogFragment();
				f.setArguments(args);
				f.show(getFragmentManager(), "edit_user_list_details");
				return true;
			}
			case MENU_FOLLOW: {
				if (mUserList == null) return false;
				mTwitterWrapper.createUserListSubscriptionAsync(mUserList.account_id, mUserList.id);
				return true;
			}
			case MENU_UNFOLLOW: {
				if (mUserList == null) return false;
				DestroyUserListSubscriptionDialogFragment.show(getFragmentManager(), mUserList);
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPrepareOptionsMenu(final Menu menu) {
		super.onPrepareOptionsMenu(menu);
		final boolean isMyself = mUserList != null && mUserList.user_id == mUserList.account_id;
		final boolean isFollowing = mUserList != null && mUserList.is_following;
		setMenuItemAvailability(menu, MENU_EDIT, isMyself);
		setMenuItemAvailability(menu, MENU_FOLLOW, mUserList != null && !isMyself && !isFollowing);
		setMenuItemAvailability(menu, MENU_UNFOLLOW, mUserList != null && !isMyself && isFollowing);
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_USER_LIST_DETAILS_UPDATED);
		filter.addAction(BROADCAST_USER_LIST_SUBSCRIBED);
		filter.addAction(BROADCAST_USER_LIST_UNSUBSCRIBED);
		registerReceiver(mStatusReceiver, filter);
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		super.onStop();
	}

	public static class EditUserListDialogFragment extends BaseSupportDialogFragment implements
			DialogInterface.OnClickListener {

		private EditText mEditName, mEditDescription;
		private CheckBox mPublicCheckBox;
		private String mName, mDescription;
		private long mAccountId;
		private int mListId;
		private boolean mIsPublic;
		private AsyncTwitterWrapper mTwitterWrapper;

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			if (mAccountId <= 0) return;
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE: {
					mName = ParseUtils.parseString(mEditName.getText());
					mDescription = ParseUtils.parseString(mEditDescription.getText());
					mIsPublic = mPublicCheckBox.isChecked();
					if (mName == null || mName.length() <= 0) return;
					mTwitterWrapper.updateUserListDetails(mAccountId, mListId, mIsPublic, mName, mDescription);
					break;
				}
			}

		}

		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			mTwitterWrapper = getApplication().getTwitterWrapper();
			final Bundle bundle = savedInstanceState == null ? getArguments() : savedInstanceState;
			mAccountId = bundle != null ? bundle.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
			mListId = bundle != null ? bundle.getInt(EXTRA_LIST_ID, -1) : -1;
			mName = bundle != null ? bundle.getString(EXTRA_LIST_NAME) : null;
			mDescription = bundle != null ? bundle.getString(EXTRA_DESCRIPTION) : null;
			mIsPublic = bundle != null ? bundle.getBoolean(EXTRA_IS_PUBLIC, true) : true;
			final Context wrapped = ThemeUtils.getDialogThemedContext(getActivity());
			final AlertDialog.Builder builder = new AlertDialog.Builder(wrapped);
			final View view = LayoutInflater.from(wrapped).inflate(R.layout.edit_user_list_detail, null);
			builder.setView(view);
			mEditName = (EditText) view.findViewById(R.id.name);
			mEditDescription = (EditText) view.findViewById(R.id.description);
			mPublicCheckBox = (CheckBox) view.findViewById(R.id.is_public);
			if (mName != null) {
				mEditName.setText(mName);
			}
			if (mDescription != null) {
				mEditDescription.setText(mDescription);
			}
			mPublicCheckBox.setChecked(mIsPublic);
			builder.setTitle(R.string.user_list);
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, this);
			return builder.create();
		}

		@Override
		public void onSaveInstanceState(final Bundle outState) {
			outState.putLong(EXTRA_ACCOUNT_ID, mAccountId);
			outState.putInt(EXTRA_LIST_ID, mListId);
			outState.putString(EXTRA_LIST_NAME, mName);
			outState.putString(EXTRA_DESCRIPTION, mDescription);
			outState.putBoolean(EXTRA_IS_PUBLIC, mIsPublic);
			super.onSaveInstanceState(outState);
		}

	}

	class ListMembersAction extends ListAction {

		public ListMembersAction(final int order) {
			super(order);
		}

		@Override
		public String getName() {
			return getString(R.string.list_members);
		}

		@Override
		public String getSummary() {
			if (mUserList == null) return null;
			return getLocalizedNumber(mLocale, mUserList.members_count);
		}

		@Override
		public void onClick() {
			openUserListMembers(getActivity(), mUserList);
		}

	}

	class ListSubscribersAction extends ListAction {

		public ListSubscribersAction(final int order) {
			super(order);
		}

		@Override
		public String getName() {
			return getString(R.string.list_subscribers);
		}

		@Override
		public String getSummary() {
			if (mUserList == null) return null;
			return getLocalizedNumber(mLocale, mUserList.subscribers_count);
		}

		@Override
		public void onClick() {
			openUserListSubscribers(getActivity(), mUserList);
		}

	}

	class ListTimelineAction extends ListAction {

		public ListTimelineAction(final int order) {
			super(order);
		}

		@Override
		public String getName() {
			return getString(R.string.list_timeline);
		}

		@Override
		public void onClick() {
			if (mUserList == null) return;
			openUserListTimeline(getActivity(), mUserList);
		}

	}

	static class ParcelableUserListLoader extends AsyncTaskLoader<SingleResponse<ParcelableUserList>> {

		private final boolean mOmitIntentExtra;
		private final Bundle mExtras;
		private final long mAccountId, mUserId;
		private final int mListId;
		private final String mScreenName, mListName;

		private ParcelableUserListLoader(final Context context, final boolean omitIntentExtra, final Bundle extras,
				final long accountId, final int listId, final String listName, final long userId,
				final String screenName) {
			super(context);
			mOmitIntentExtra = omitIntentExtra;
			mExtras = extras;
			mAccountId = accountId;
			mUserId = userId;
			mListId = listId;
			mScreenName = screenName;
			mListName = listName;
		}

		@Override
		public SingleResponse<ParcelableUserList> loadInBackground() {
			if (!mOmitIntentExtra && mExtras != null) {
				final ParcelableUserList cache = mExtras.getParcelable(EXTRA_USER_LIST);
				if (cache != null) return SingleResponse.withData(cache);
			}
			final Twitter twitter = getTwitterInstance(getContext(), mAccountId, true);
			if (twitter == null) return SingleResponse.nullInstance();
			try {
				final UserList list;
				if (mListId > 0) {
					list = twitter.showUserList(mListId);
				} else if (mUserId > 0) {
					list = twitter.showUserList(mListName, mUserId);
				} else if (mScreenName != null) {
					list = twitter.showUserList(mListName, mScreenName);
				} else
					return SingleResponse.nullInstance();
				return new SingleResponse<ParcelableUserList>(new ParcelableUserList(list, mAccountId), null);
			} catch (final TwitterException e) {
				return new SingleResponse<ParcelableUserList>(null, e);
			}
		}

		@Override
		public void onStartLoading() {
			forceLoad();
		}

	}

}
