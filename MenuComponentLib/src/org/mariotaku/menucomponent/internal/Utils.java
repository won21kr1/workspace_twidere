package org.mariotaku.menucomponent.internal;

import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

public class Utils {

	public static void showMenuItemToast(final View v, final CharSequence text, final boolean isBottomBar) {
		final int[] screenPos = new int[2];
		final Rect displayFrame = new Rect();
		v.getLocationOnScreen(screenPos);
		v.getWindowVisibleDisplayFrame(displayFrame);
		final int width = v.getWidth();
		final int height = v.getHeight();
		final int midy = screenPos[1] + height / 2;
		final int screenWidth = v.getResources().getDisplayMetrics().widthPixels;
		final Toast cheatSheet = Toast.makeText(v.getContext(), text, Toast.LENGTH_SHORT);
		if (midy >= displayFrame.height() || isBottomBar) {
			// Show along the bottom center
			cheatSheet.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, height);
		} else {
			// Show along the top; follow action buttons
			cheatSheet.setGravity(Gravity.TOP | Gravity.RIGHT, screenWidth - screenPos[0] - width / 2, height);
		}
		cheatSheet.show();
	}
}
