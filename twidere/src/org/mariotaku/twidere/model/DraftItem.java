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

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.ParseUtils;

public class DraftItem implements Parcelable {

	public static final Parcelable.Creator<DraftItem> CREATOR = new Parcelable.Creator<DraftItem>() {
		@Override
		public DraftItem createFromParcel(final Parcel in) {
			return new DraftItem(in);
		}

		@Override
		public DraftItem[] newArray(final int size) {
			return new DraftItem[size];
		}
	};

	public final long[] account_ids;
	public final long _id, in_reply_to_status_id, timestamp;
	public final String text, media_uri;
	public final boolean is_possibly_sensitive;
	public final ParcelableLocation location;
	public final int media_type, action_type;
	public final JSONObject action_extras;

	public DraftItem(final Cursor cursor, final CursorIndices indices) {
		_id = cursor.getLong(indices._id);
		text = cursor.getString(indices.text);
		media_uri = cursor.getString(indices.media_uri);
		account_ids = ArrayUtils.parseLongArray(cursor.getString(indices.account_ids), ',');
		in_reply_to_status_id = cursor.getLong(indices.in_reply_to_status_id);
		media_type = cursor.getShort(indices.media_type);
		is_possibly_sensitive = cursor.getShort(indices.is_possibly_sensitive) == 1;
		location = new ParcelableLocation(cursor.getString(indices.location));
		timestamp = cursor.getLong(indices.timestamp);
		action_type = cursor.getInt(indices.action_type);
		action_extras = createJSONObject(cursor.getString(indices.action_extras));
	}

	public DraftItem(final Parcel in) {
		account_ids = in.createLongArray();
		_id = in.readLong();
		in_reply_to_status_id = in.readLong();
		text = in.readString();
		media_uri = in.readString();
		media_type = in.readInt();
		is_possibly_sensitive = in.readInt() == 1;
		location = ParcelableLocation.fromString(in.readString());
		timestamp = in.readLong();
		action_type = in.readInt();
		action_extras = createJSONObject(in.readString());
	}

	public DraftItem(final ParcelableStatusUpdate status) {
		_id = 0;
		account_ids = Account.getAccountIds(status.accounts);
		in_reply_to_status_id = status.in_reply_to_status_id;
		text = status.text;
		media_uri = ParseUtils.parseString(status.media_uri);
		media_type = status.media_type;
		is_possibly_sensitive = status.is_possibly_sensitive;
		location = status.location;
		timestamp = System.currentTimeMillis();
		action_type = Drafts.ACTION_UPDATE_STATUS;
		action_extras = createJSONObject(null);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel out, final int flags) {
		out.writeLongArray(account_ids);
		out.writeLong(_id);
		out.writeLong(in_reply_to_status_id);
		out.writeString(text);
		out.writeString(media_uri);
		out.writeInt(media_type);
		out.writeInt(is_possibly_sensitive ? 1 : 0);
		out.writeString(ParcelableLocation.toString(location));
		out.writeLong(timestamp);
		out.writeInt(action_type);
		out.writeString(action_extras.toString());
	}

	private static JSONObject createJSONObject(final String json) {
		if (TextUtils.isEmpty(json)) return new JSONObject();
		try {
			return new JSONObject(json);
		} catch (final JSONException e) {
			e.printStackTrace();
		}
		return new JSONObject();
	}

	public static final class CursorIndices {

		public final int _id, account_ids, in_reply_to_status_id, text, location, media_uri, media_type,
				is_possibly_sensitive, timestamp, action_type, action_extras;

		public CursorIndices(final Cursor cursor) {
			_id = cursor.getColumnIndex(Drafts._ID);
			account_ids = cursor.getColumnIndex(Drafts.ACCOUNT_IDS);
			in_reply_to_status_id = cursor.getColumnIndex(Drafts.IN_REPLY_TO_STATUS_ID);
			timestamp = cursor.getColumnIndex(Drafts.TIMESTAMP);
			text = cursor.getColumnIndex(Drafts.TEXT);
			media_uri = cursor.getColumnIndex(Drafts.MEDIA_URI);
			media_type = cursor.getColumnIndex(Drafts.MEDIA_TYPE);
			is_possibly_sensitive = cursor.getColumnIndex(Drafts.IS_POSSIBLY_SENSITIVE);
			location = cursor.getColumnIndex(Drafts.LOCATION);
			action_type = cursor.getColumnIndex(Drafts.ACTION_TYPE);
			action_extras = cursor.getColumnIndex(Drafts.ACTION_EXTRAS);
		}

	}

}
