/**
 * Copyright 2017 FrogSquare. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package org.godotengine.godot.storage;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.godot.game.R;

import org.godotengine.godot.Utils;

/**
 * Base class for Services that keep track of the number of active jobs and self-stop when the
 * count is zero
 *
 * Repo: https://github.com/firebase/quickstart-android/
 * File: MyBaseTaskService.java
 */
public abstract class BaseTaskService extends Service {

	static final int PROGRESS_NOTIFICATION_ID = 6001;
	static final int FINISHED_NOTIFICATION_ID = 6002;

	public void taskStarted() {
		changeNumberOfTasks(1);
	}

	public void taskCompleted() {
		changeNumberOfTasks(-1);
	}

	private synchronized void changeNumberOfTasks(int delta) {
		Utils.d("GodotFireBase", "Storage:ChangeNumOfTasks: {" + mNumTasks + ":" + delta + "}");
		mNumTasks += delta;

		// If there are no tasks left, stop the service
		if (mNumTasks <= 0) {
			Utils.d("GodotFireBase", "stopping");
			stopSelf();
		}
	}

	/**
	 * Show notification with a progress bar.
	 */
	protected void showProgressNotification(String caption, long completedUnits, long totalUnits) {
		int percentComplete = 0;
		if (totalUnits > 0) { percentComplete = (int) (100 * completedUnits / totalUnits); }

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.ic_file_upload_white_24dp)
		.setContentTitle(getString(R.string.godot_project_name_string))
		.setContentText(caption)
		.setProgress(100, percentComplete, false)
		.setOngoing(true)
		.setAutoCancel(false);

		NotificationManager manager =
		(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		manager.notify(PROGRESS_NOTIFICATION_ID, builder.build());
	}

	/**
	 * Show notification that the activity finished.
	 */
	protected void showFinishedNotification(String caption, Intent intent, boolean success) {
		// Make PendingIntent for notification
		PendingIntent pendingIntent = PendingIntent.getActivity(
		this, 6003, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		int icon = success ? R.drawable.ic_check_white_24 : R.drawable.ic_error_white_24dp;

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
		.setSmallIcon(icon)
		.setContentTitle(getString(R.string.godot_project_name_string))
		.setContentText(caption)
		.setAutoCancel(true)
		.setContentIntent(pendingIntent);

		NotificationManager manager =
		(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		manager.notify(FINISHED_NOTIFICATION_ID, builder.build());
	}

	/**
	 * Dismiss the progress notification.
	 */
	protected void dismissProgressNotification() {
		NotificationManager manager =
		(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		manager.cancel(PROGRESS_NOTIFICATION_ID);
	}

	private int mNumTasks = 0;
}
