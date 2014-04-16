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

package org.mariotaku.twidere.util;

import static org.mariotaku.twidere.util.ServiceUtils.bindToService;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.IMediaUploader;
import org.mariotaku.twidere.model.MediaUploadResult;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;

public final class MediaUploaderInterface implements Constants, IMediaUploader {

	private IMediaUploader mUploader;

	private final ServiceConnection mConntecion = new ServiceConnection() {

		@Override
		public void onServiceConnected(final ComponentName service, final IBinder obj) {
			mUploader = IMediaUploader.Stub.asInterface(obj);
		}

		@Override
		public void onServiceDisconnected(final ComponentName service) {
			mUploader = null;
		}
	};

	private MediaUploaderInterface(final Context context, final String uploader_name) {
		final Intent intent = new Intent(INTENT_ACTION_EXTENSION_UPLOAD_MEDIA);
		final ComponentName component = ComponentName.unflattenFromString(uploader_name);
		intent.setComponent(component);
		bindToService(context, intent, mConntecion);
	}

	@Override
	public IBinder asBinder() {
		// Useless here
		return mUploader.asBinder();
	}

	@Override
	public MediaUploadResult upload(final ParcelableStatusUpdate status) throws RemoteException {
		if (mUploader == null) return null;
		try {
			return mUploader.upload(status);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void waitForService() {
		while (mUploader == null) {
			try {
				Thread.sleep(100L);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static MediaUploaderInterface getInstance(final Application application, final String uploaderName) {
		if (uploaderName == null) return null;
		final Intent intent = new Intent(INTENT_ACTION_EXTENSION_UPLOAD_MEDIA);
		final ComponentName component = ComponentName.unflattenFromString(uploaderName);
		intent.setComponent(component);
		if (application.getPackageManager().queryIntentServices(intent, 0).size() != 1) return null;
		return new MediaUploaderInterface(application, uploaderName);
	}
}
