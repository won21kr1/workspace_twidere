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

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.matcherGroup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.PreviewMedia;
import org.mariotaku.twidere.util.HtmlLinkExtractor.HtmlLink;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.URLEntity;
import twitter4j.http.HttpClientWrapper;
import twitter4j.http.HttpParameter;
import twitter4j.http.HttpResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MediaPreviewUtils {

	public static final String AVAILABLE_URL_SCHEME_PREFIX = "(https?:\\/\\/)?";
	public static final String AVAILABLE_IMAGE_SHUFFIX = "(png|jpeg|jpg|gif|bmp)";
	public static final String SINA_WEIBO_IMAGES_AVAILABLE_SIZES = "(woriginal|large|thumbnail|bmiddle|mw[\\d]+)";
	public static final String GOOGLE_IMAGES_AVAILABLE_SIZES = "((([whs]\\d+|no)\\-?)+)";

	private static final String STRING_PATTERN_TWITTER_IMAGES_DOMAIN = "(p|pbs)\\.twimg\\.com";
	private static final String STRING_PATTERN_SINA_WEIBO_IMAGES_DOMAIN = "[\\w\\d]+\\.sinaimg\\.cn|[\\w\\d]+\\.sina\\.cn";
	private static final String STRING_PATTERN_LOCKERZ_DOMAIN = "lockerz\\.com";
	private static final String STRING_PATTERN_PLIXI_DOMAIN = "plixi\\.com";
	private static final String STRING_PATTERN_INSTAGRAM_DOMAIN = "instagr\\.am|instagram\\.com";
	private static final String STRING_PATTERN_TWITPIC_DOMAIN = "twitpic\\.com";
	private static final String STRING_PATTERN_IMGLY_DOMAIN = "img\\.ly";
	private static final String STRING_PATTERN_YFROG_DOMAIN = "yfrog\\.com";
	private static final String STRING_PATTERN_TWITGOO_DOMAIN = "twitgoo\\.com";
	private static final String STRING_PATTERN_MOBYPICTURE_DOMAIN = "moby\\.to";
	private static final String STRING_PATTERN_IMGUR_DOMAIN = "imgur\\.com|i\\.imgur\\.com";
	private static final String STRING_PATTERN_PHOTOZOU_DOMAIN = "photozou\\.jp";
	private static final String STRING_PATTERN_GOOGLE_IMAGES_DOMAIN = "(lh|gp|s)(\\d+)?\\.(ggpht|googleusercontent)\\.com";

	private static final String STRING_PATTERN_IMAGES_NO_SCHEME = "[^:\\/\\/].+?\\." + AVAILABLE_IMAGE_SHUFFIX;
	private static final String STRING_PATTERN_TWITTER_IMAGES_NO_SCHEME = STRING_PATTERN_TWITTER_IMAGES_DOMAIN
			+ "(\\/media)?\\/([\\d\\w\\-_]+)\\." + AVAILABLE_IMAGE_SHUFFIX;
	private static final String STRING_PATTERN_SINA_WEIBO_IMAGES_NO_SCHEME = "("
			+ STRING_PATTERN_SINA_WEIBO_IMAGES_DOMAIN + ")" + "\\/" + SINA_WEIBO_IMAGES_AVAILABLE_SIZES
			+ "\\/(([\\d\\w]+)\\." + AVAILABLE_IMAGE_SHUFFIX + ")";
	private static final String STRING_PATTERN_LOCKERZ_NO_SCHEME = "(" + STRING_PATTERN_LOCKERZ_DOMAIN + ")"
			+ "\\/s\\/(\\w+)\\/?";
	private static final String STRING_PATTERN_PLIXI_NO_SCHEME = "(" + STRING_PATTERN_PLIXI_DOMAIN + ")"
			+ "\\/p\\/(\\w+)\\/?";
	private static final String STRING_PATTERN_INSTAGRAM_NO_SCHEME = "(" + STRING_PATTERN_INSTAGRAM_DOMAIN + ")"
			+ "\\/p\\/([_\\-\\d\\w]+)\\/?";
	private static final String STRING_PATTERN_TWITPIC_NO_SCHEME = STRING_PATTERN_TWITPIC_DOMAIN + "\\/([\\d\\w]+)\\/?";
	private static final String STRING_PATTERN_IMGLY_NO_SCHEME = STRING_PATTERN_IMGLY_DOMAIN + "\\/([\\w\\d]+)\\/?";
	private static final String STRING_PATTERN_YFROG_NO_SCHEME = STRING_PATTERN_YFROG_DOMAIN + "\\/([\\w\\d]+)\\/?";
	private static final String STRING_PATTERN_TWITGOO_NO_SCHEME = STRING_PATTERN_TWITGOO_DOMAIN + "\\/([\\d\\w]+)\\/?";
	private static final String STRING_PATTERN_MOBYPICTURE_NO_SCHEME = STRING_PATTERN_MOBYPICTURE_DOMAIN
			+ "\\/([\\d\\w]+)\\/?";
	private static final String STRING_PATTERN_IMGUR_NO_SCHEME = "(" + STRING_PATTERN_IMGUR_DOMAIN + ")"
			+ "\\/([\\d\\w]+)((?-i)s|(?-i)l)?(\\." + AVAILABLE_IMAGE_SHUFFIX + ")?";
	private static final String STRING_PATTERN_PHOTOZOU_NO_SCHEME = STRING_PATTERN_PHOTOZOU_DOMAIN
			+ "\\/photo\\/show\\/([\\d]+)\\/([\\d]+)\\/?";
	private static final String STRING_PATTERN_GOOGLE_IMAGES_NO_SCHEME = "(" + STRING_PATTERN_GOOGLE_IMAGES_DOMAIN
			+ ")" + "((\\/[\\w\\d\\-\\_]+)+)\\/" + GOOGLE_IMAGES_AVAILABLE_SIZES + "\\/.+";
	private static final String STRING_PATTERN_GOOGLE_PROXY_IMAGES_NO_SCHEME = "("
			+ STRING_PATTERN_GOOGLE_IMAGES_DOMAIN + ")" + "\\/proxy\\/([\\w\\d\\-\\_]+)="
			+ GOOGLE_IMAGES_AVAILABLE_SIZES;

	private static final String STRING_PATTERN_IMAGES = AVAILABLE_URL_SCHEME_PREFIX + STRING_PATTERN_IMAGES_NO_SCHEME;
	private static final String STRING_PATTERN_TWITTER_IMAGES = AVAILABLE_URL_SCHEME_PREFIX
			+ STRING_PATTERN_TWITTER_IMAGES_NO_SCHEME;
	private static final String STRING_PATTERN_SINA_WEIBO_IMAGES = AVAILABLE_URL_SCHEME_PREFIX
			+ STRING_PATTERN_SINA_WEIBO_IMAGES_NO_SCHEME;
	private static final String STRING_PATTERN_LOCKERZ = AVAILABLE_URL_SCHEME_PREFIX + STRING_PATTERN_LOCKERZ_NO_SCHEME;
	private static final String STRING_PATTERN_PLIXI = AVAILABLE_URL_SCHEME_PREFIX + STRING_PATTERN_PLIXI_NO_SCHEME;
	private static final String STRING_PATTERN_INSTAGRAM = AVAILABLE_URL_SCHEME_PREFIX
			+ STRING_PATTERN_INSTAGRAM_NO_SCHEME;
	private static final String STRING_PATTERN_TWITPIC = AVAILABLE_URL_SCHEME_PREFIX + STRING_PATTERN_TWITPIC_NO_SCHEME;
	private static final String STRING_PATTERN_IMGLY = AVAILABLE_URL_SCHEME_PREFIX + STRING_PATTERN_IMGLY_NO_SCHEME;
	private static final String STRING_PATTERN_YFROG = AVAILABLE_URL_SCHEME_PREFIX + STRING_PATTERN_YFROG_NO_SCHEME;
	private static final String STRING_PATTERN_TWITGOO = AVAILABLE_URL_SCHEME_PREFIX + STRING_PATTERN_TWITGOO_NO_SCHEME;
	private static final String STRING_PATTERN_MOBYPICTURE = AVAILABLE_URL_SCHEME_PREFIX
			+ STRING_PATTERN_MOBYPICTURE_NO_SCHEME;
	private static final String STRING_PATTERN_IMGUR = AVAILABLE_URL_SCHEME_PREFIX + STRING_PATTERN_IMGUR_NO_SCHEME;
	private static final String STRING_PATTERN_PHOTOZOU = AVAILABLE_URL_SCHEME_PREFIX
			+ STRING_PATTERN_PHOTOZOU_NO_SCHEME;
	private static final String STRING_PATTERN_GOOGLE_IMAGES = AVAILABLE_URL_SCHEME_PREFIX
			+ STRING_PATTERN_GOOGLE_IMAGES_NO_SCHEME;
	private static final String STRING_PATTERN_GOOGLE_PROXY_IMAGES = AVAILABLE_URL_SCHEME_PREFIX
			+ STRING_PATTERN_GOOGLE_PROXY_IMAGES_NO_SCHEME;

	public static final Pattern PATTERN_ALL_AVAILABLE_IMAGES = Pattern.compile(AVAILABLE_URL_SCHEME_PREFIX + "("
			+ STRING_PATTERN_IMAGES_NO_SCHEME + "|" + STRING_PATTERN_TWITTER_IMAGES_NO_SCHEME + "|"
			+ STRING_PATTERN_INSTAGRAM_NO_SCHEME + "|" + STRING_PATTERN_GOOGLE_IMAGES_NO_SCHEME + "|"
			+ STRING_PATTERN_SINA_WEIBO_IMAGES_NO_SCHEME + "|" + STRING_PATTERN_LOCKERZ_NO_SCHEME + "|"
			+ STRING_PATTERN_PLIXI_NO_SCHEME + "|" + STRING_PATTERN_TWITPIC_NO_SCHEME + "|"
			+ STRING_PATTERN_IMGLY_NO_SCHEME + "|" + STRING_PATTERN_YFROG_NO_SCHEME + "|"
			+ STRING_PATTERN_TWITGOO_NO_SCHEME + "|" + STRING_PATTERN_MOBYPICTURE_NO_SCHEME + "|"
			+ STRING_PATTERN_IMGUR_NO_SCHEME + "|" + STRING_PATTERN_PHOTOZOU_NO_SCHEME + ")", Pattern.CASE_INSENSITIVE);

	public static final Pattern PATTERN_PREVIEW_AVAILABLE_IMAGES_MATCH_ONLY = Pattern.compile(
			AVAILABLE_URL_SCHEME_PREFIX + "(" + STRING_PATTERN_IMAGES_NO_SCHEME + "|"
					+ STRING_PATTERN_TWITTER_IMAGES_DOMAIN + "|" + STRING_PATTERN_INSTAGRAM_DOMAIN + "|"
					+ STRING_PATTERN_GOOGLE_IMAGES_DOMAIN + "|" + STRING_PATTERN_SINA_WEIBO_IMAGES_DOMAIN + "|"
					+ STRING_PATTERN_LOCKERZ_DOMAIN + "|" + STRING_PATTERN_PLIXI_DOMAIN + "|"
					+ STRING_PATTERN_TWITPIC_DOMAIN + "|" + STRING_PATTERN_IMGLY_DOMAIN + "|"
					+ STRING_PATTERN_YFROG_DOMAIN + "|" + STRING_PATTERN_TWITGOO_DOMAIN + "|"
					+ STRING_PATTERN_MOBYPICTURE_DOMAIN + "|" + STRING_PATTERN_IMGUR_DOMAIN + "|"
					+ STRING_PATTERN_PHOTOZOU_DOMAIN + ")", Pattern.CASE_INSENSITIVE);

	public static final Pattern PATTERN_IMAGES = Pattern.compile(STRING_PATTERN_IMAGES, Pattern.CASE_INSENSITIVE);
	public static final Pattern PATTERN_TWITTER_IMAGES = Pattern.compile(STRING_PATTERN_TWITTER_IMAGES,
			Pattern.CASE_INSENSITIVE);
	public static final Pattern PATTERN_SINA_WEIBO_IMAGES = Pattern.compile(STRING_PATTERN_SINA_WEIBO_IMAGES,
			Pattern.CASE_INSENSITIVE);
	public static final Pattern PATTERN_LOCKERZ = Pattern.compile(STRING_PATTERN_LOCKERZ, Pattern.CASE_INSENSITIVE);
	public static final Pattern PATTERN_PLIXI = Pattern.compile(STRING_PATTERN_PLIXI, Pattern.CASE_INSENSITIVE);

	public static final Pattern PATTERN_INSTAGRAM = Pattern.compile(STRING_PATTERN_INSTAGRAM, Pattern.CASE_INSENSITIVE);
	public static final int INSTAGRAM_GROUP_ID = 3;

	public static final Pattern PATTERN_TWITPIC = Pattern.compile(STRING_PATTERN_TWITPIC, Pattern.CASE_INSENSITIVE);
	public static final int TWITPIC_GROUP_ID = 2;

	public static final Pattern PATTERN_IMGLY = Pattern.compile(STRING_PATTERN_IMGLY, Pattern.CASE_INSENSITIVE);
	public static final int IMGLY_GROUP_ID = 2;

	public static final Pattern PATTERN_YFROG = Pattern.compile(STRING_PATTERN_YFROG, Pattern.CASE_INSENSITIVE);
	public static final int YFROG_GROUP_ID = 2;

	public static final Pattern PATTERN_TWITGOO = Pattern.compile(STRING_PATTERN_TWITGOO, Pattern.CASE_INSENSITIVE);
	public static final int TWITGOO_GROUP_ID = 2;

	public static final Pattern PATTERN_MOBYPICTURE = Pattern.compile(STRING_PATTERN_MOBYPICTURE,
			Pattern.CASE_INSENSITIVE);
	public static final int MOBYPICTURE_GROUP_ID = 2;

	public static final Pattern PATTERN_IMGUR = Pattern.compile(STRING_PATTERN_IMGUR, Pattern.CASE_INSENSITIVE);
	public static final int IMGUR_GROUP_ID = 3;

	public static final Pattern PATTERN_PHOTOZOU = Pattern.compile(STRING_PATTERN_PHOTOZOU, Pattern.CASE_INSENSITIVE);
	public static final int PHOTOZOU_GROUP_ID = 3;

	public static final Pattern PATTERN_GOOGLE_IMAGES = Pattern.compile(STRING_PATTERN_GOOGLE_IMAGES,
			Pattern.CASE_INSENSITIVE);
	public static final int GOOGLE_IMAGES_GROUP_SERVER = 2;
	public static final int GOOGLE_IMAGES_GROUP_ID = 6;

	public static final Pattern PATTERN_GOOGLE_PROXY_IMAGES = Pattern.compile(STRING_PATTERN_GOOGLE_PROXY_IMAGES,
			Pattern.CASE_INSENSITIVE);
	public static final int GOOGLE_PROXY_IMAGES_GROUP_SERVER = 2;
	public static final int GOOGLE_PROXY_IMAGES_GROUP_ID = 6;

	private static final Pattern[] SUPPORTED_PATTERNS = { PATTERN_TWITTER_IMAGES, PATTERN_INSTAGRAM,
			PATTERN_GOOGLE_IMAGES, PATTERN_GOOGLE_PROXY_IMAGES, PATTERN_SINA_WEIBO_IMAGES, PATTERN_TWITPIC,
			PATTERN_IMGUR, PATTERN_IMGLY, PATTERN_YFROG, PATTERN_LOCKERZ, PATTERN_PLIXI, PATTERN_TWITGOO,
			PATTERN_MOBYPICTURE, PATTERN_PHOTOZOU };

	private static final String URL_PHOTOZOU_PHOTO_INFO = "https://api.photozou.jp/rest/photo_info.json";

	public static void addToLinearLayout(final LinearLayout container, final ImageLoaderWrapper loader,
			final List<ParcelableMedia> medias, final int maxColumnCount, final OnMediaClickListener listener) {
		if (container.getOrientation() != LinearLayout.VERTICAL) throw new IllegalArgumentException();
		final Context context = container.getContext();
		final ImageLoadingHandler loadingHandler = new ImageLoadingHandler();
		final LayoutInflater inflater = LayoutInflater.from(context);
		final ListIterator<ParcelableMedia> iterator = medias.listIterator();
		final int imageCount = medias.size();
		final double imageCountSqrt = Math.sqrt(imageCount);
		final int bestColumnCount = imageCountSqrt % 1 == 0 ? (int) imageCountSqrt : maxColumnCount;
		final int firstColumn = imageCount % bestColumnCount, fullRowCount = imageCount / bestColumnCount;
		final int rowCount = fullRowCount + (firstColumn > 0 ? 1 : 0);
		final View.OnClickListener clickListener = new ImageGridClickListener(listener);
		container.setMotionEventSplittingEnabled(false);
		for (int currentRow = 0; currentRow < rowCount; currentRow++) {
			final LinearLayout rowContainer = new LinearLayout(context);
			rowContainer.setOrientation(LinearLayout.HORIZONTAL);
			rowContainer.setMotionEventSplittingEnabled(false);
			container.addView(rowContainer, LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			final int columnCount = currentRow == 0 && firstColumn > 0 ? firstColumn : bestColumnCount;
			for (int currentColumn = 0; currentColumn < columnCount; currentColumn++) {
				final ParcelableMedia media = iterator.next();
				final View item = inflater.inflate(R.layout.grid_item_image_preview, rowContainer, false);
				item.setTag(media);
				if (listener != null) {
					item.setOnClickListener(clickListener);
				}
				final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) item.getLayoutParams();
				lp.weight = 1.0f;
				rowContainer.addView(item, lp);
				final ImageView imageView = (ImageView) item.findViewById(R.id.image_preview_item);
				loader.displayPreviewImage(imageView, media.url, loadingHandler);
			}
		}
	}

	public static void addToLinearLayout(final LinearLayout container, final ImageLoaderWrapper loader,
			final ParcelableMedia[] medias, final int maxColumnCount, final OnMediaClickListener listener) {
		addToLinearLayout(container, loader, Arrays.asList(medias), maxColumnCount, listener);
	}

	public static PreviewMedia getAllAvailableImage(final String link, final boolean fullImage) {
		try {
			return getAllAvailableImage(link, fullImage, null);
		} catch (final IOException e) {
			throw new AssertionError("This should never happen");
		}
	}

	public static PreviewMedia getAllAvailableImage(final String link, final boolean fullImage,
			final HttpClientWrapper client) throws IOException {
		if (link == null) return null;
		StrictModeUtils.checkLengthyOperation();
		Matcher m;
		m = PATTERN_TWITTER_IMAGES.matcher(link);
		if (m.matches()) return getTwitterImage(link, fullImage);
		m = PATTERN_INSTAGRAM.matcher(link);
		if (m.matches()) return getInstagramImage(matcherGroup(m, INSTAGRAM_GROUP_ID), link, fullImage);
		m = PATTERN_GOOGLE_IMAGES.matcher(link);
		if (m.matches())
			return getGoogleImage(matcherGroup(m, GOOGLE_IMAGES_GROUP_SERVER), matcherGroup(m, GOOGLE_IMAGES_GROUP_ID),
					fullImage);
		m = PATTERN_GOOGLE_PROXY_IMAGES.matcher(link);
		if (m.matches())
			return getGoogleProxyImage(matcherGroup(m, GOOGLE_PROXY_IMAGES_GROUP_SERVER),
					matcherGroup(m, GOOGLE_PROXY_IMAGES_GROUP_ID), fullImage);
		m = PATTERN_SINA_WEIBO_IMAGES.matcher(link);
		if (m.matches()) return getSinaWeiboImage(link, fullImage);
		m = PATTERN_TWITPIC.matcher(link);
		if (m.matches()) return getTwitpicImage(matcherGroup(m, TWITPIC_GROUP_ID), link, fullImage);
		m = PATTERN_IMGUR.matcher(link);
		if (m.matches()) return getImgurImage(matcherGroup(m, IMGUR_GROUP_ID), link, fullImage);
		m = PATTERN_IMGLY.matcher(link);
		if (m.matches()) return getImglyImage(matcherGroup(m, IMGLY_GROUP_ID), link, fullImage);
		m = PATTERN_YFROG.matcher(link);
		if (m.matches()) return getYfrogImage(matcherGroup(m, YFROG_GROUP_ID), link, fullImage);
		m = PATTERN_LOCKERZ.matcher(link);
		if (m.matches()) return getLockerzAndPlixiImage(link, fullImage);
		m = PATTERN_PLIXI.matcher(link);
		if (m.matches()) return getLockerzAndPlixiImage(link, fullImage);
		m = PATTERN_TWITGOO.matcher(link);
		if (m.matches()) return getTwitgooImage(matcherGroup(m, TWITGOO_GROUP_ID), link, fullImage);
		m = PATTERN_MOBYPICTURE.matcher(link);
		if (m.matches()) return getMobyPictureImage(matcherGroup(m, MOBYPICTURE_GROUP_ID), link, fullImage);
		m = PATTERN_PHOTOZOU.matcher(link);
		if (m.matches()) return getPhotozouImage(client, matcherGroup(m, PHOTOZOU_GROUP_ID), link, fullImage);
		return null;
	}

	public static PreviewMedia[] getImagesInStatus(final String status_string, final boolean fullImage) {
		if (status_string == null) return new PreviewMedia[0];
		final List<PreviewMedia> images = new ArrayList<PreviewMedia>();
		final HtmlLinkExtractor extractor = new HtmlLinkExtractor();
		for (final HtmlLink link : extractor.grabLinks(status_string)) {
			final PreviewMedia spec = getAllAvailableImage(link.getLink(), fullImage);
			if (spec != null) {
				images.add(spec);
			}
		}
		return images.toArray(new PreviewMedia[images.size()]);
	}

	public static String getSupportedFirstLink(final Status status) {
		if (status == null) return null;
		final MediaEntity[] medias = status.getMediaEntities();
		if (medias != null) {
			for (final MediaEntity entity : medias) {
				final String expanded = ParseUtils.parseString(entity.getMediaURLHttps());
				if (getSupportedLink(expanded) != null) return expanded;
			}
		}
		final URLEntity[] urls = status.getURLEntities();
		if (urls != null) {
			for (final URLEntity entity : urls) {
				final String expanded = ParseUtils.parseString(entity.getExpandedURL());
				if (getSupportedLink(expanded) != null) return expanded;
			}
		}
		return null;
	}

	public static String getSupportedFirstLink(final String html) {
		if (html == null) return null;
		final HtmlLinkExtractor extractor = new HtmlLinkExtractor();
		for (final HtmlLink link : extractor.grabLinks(html)) {
			if (getSupportedLink(link.getLink()) != null) return link.getLink();
		}
		return null;
	}

	public static String getSupportedLink(final String link) {
		if (link == null) return null;
		for (final Pattern pattern : SUPPORTED_PATTERNS) {
			if (pattern.matcher(link).matches()) return link;
		}
		return null;
	}

	public static List<String> getSupportedLinksInStatus(final String statusString) {
		if (statusString == null) return Collections.emptyList();
		final List<String> links = new ArrayList<String>();
		final HtmlLinkExtractor extractor = new HtmlLinkExtractor();
		for (final HtmlLink link : extractor.grabLinks(statusString)) {
			final String spec = getSupportedLink(link.getLink());
			if (spec != null) {
				links.add(spec);
			}
		}
		return links;
	}

	public static boolean isLinkSupported(final String link) {
		if (link == null) return false;
		for (final Pattern pattern : SUPPORTED_PATTERNS) {
			if (pattern.matcher(link).matches()) return true;
		}
		return false;
	}

	private static PreviewMedia getGoogleImage(final String server, final String id, final boolean fullImage) {
		if (isEmpty(server) || isEmpty(id)) return null;
		final String full = "https://" + server + id + "/s0/full";
		final String preview = fullImage ? full : "https://" + server + id + "/s480/full";
		return PreviewMedia.newImage(preview, full);
	}

	private static PreviewMedia getGoogleProxyImage(final String server, final String id, final boolean fullImage) {
		if (isEmpty(server) || isEmpty(id)) return null;
		final String full = "https://" + server + "/proxy/" + id + "=s0";
		final String preview = fullImage ? full : "https://" + server + "/proxy/" + id + "=s480";
		return PreviewMedia.newImage(preview, full);
	}

	private static PreviewMedia getImglyImage(final String id, final String orig, final boolean fullImage) {
		if (isEmpty(id)) return null;
		final String preview = String.format("http://img.ly/show/%s/%s", fullImage ? "full" : "medium", id);
		return PreviewMedia.newImage(preview, orig);
	}

	private static PreviewMedia getImgurImage(final String id, final String orig, final boolean fullImage) {
		if (isEmpty(id)) return null;
		final String preview = fullImage ? String.format("http://i.imgur.com/%s.jpg", id) : String.format(
				"http://i.imgur.com/%sl.jpg", id);
		return PreviewMedia.newImage(preview, orig);
	}

	private static PreviewMedia getInstagramImage(final String id, final String orig, final boolean fullImage) {
		if (isEmpty(id)) return null;
		final String preview = String.format("https://instagr.am/p/%s/media/?size=%s", id, fullImage ? "l" : "t");
		return PreviewMedia.newImage(preview, orig);
	}

	private static PreviewMedia getLockerzAndPlixiImage(final String url, final boolean fullImage) {
		if (isEmpty(url)) return null;
		final String preview = String.format("https://api.plixi.com/api/tpapi.svc/imagefromurl?url=%s&size=%s", url,
				fullImage ? "big" : "small");
		return PreviewMedia.newImage(preview, url);

	}

	private static PreviewMedia getMobyPictureImage(final String id, final String orig, final boolean fullImage) {
		if (isEmpty(id)) return null;
		final String preview = String.format("http://moby.to/%s:%s", id, fullImage ? "full" : "thumb");
		return PreviewMedia.newImage(preview, orig);
	}

	private static PreviewMedia getPhotozouImage(final HttpClientWrapper client, final String id, final String orig,
			final boolean fullImage) throws IOException {
		if (isEmpty(id)) return null;
		if (client != null) {
			try {
				final HttpParameter[] parameters = { new HttpParameter("photo_id", id) };
				final HttpResponse resp = client.get(URL_PHOTOZOU_PHOTO_INFO, URL_PHOTOZOU_PHOTO_INFO, parameters);
				final JSONObject json = resp.asJSONObject().getJSONObject("info").getJSONObject("photo");
				final String key = fullImage ? "original_image_url" : "image_url";
				return PreviewMedia.newImage(json.getString(key), orig);
			} catch (final TwitterException e) {
				return null;
			} catch (final JSONException e) {
				throw new IOException(e);
			}
		}
		final String preview = String.format(Locale.US, "http://photozou.jp/p/img/%s", id);
		return PreviewMedia.newImage(preview, orig);
	}

	private static PreviewMedia getSinaWeiboImage(final String url, final boolean fullImage) {
		if (isEmpty(url)) return null;
		final String full = url.replaceAll("\\/" + SINA_WEIBO_IMAGES_AVAILABLE_SIZES + "\\/", "/woriginal/");
		final String preview = fullImage ? full : url.replaceAll("\\/" + SINA_WEIBO_IMAGES_AVAILABLE_SIZES + "\\/",
				"/bmiddle/");
		return PreviewMedia.newImage(preview, full);
	}

	private static PreviewMedia getTwitgooImage(final String id, final String orig, final boolean fullImage) {
		if (isEmpty(id)) return null;
		final String preview = String.format("http://twitgoo.com/show/%s/%s", fullImage ? "img" : "thumb", id);
		return PreviewMedia.newImage(preview, orig);
	}

	private static PreviewMedia getTwitpicImage(final String id, final String orig, final boolean fullImage) {
		if (isEmpty(id)) return null;
		final String preview = String.format("http://twitpic.com/show/%s/%s", fullImage ? "large" : "thumb", id);
		return PreviewMedia.newImage(preview, orig);
	}

	private static PreviewMedia getTwitterImage(final String url, final boolean fullImage) {
		if (isEmpty(url)) return null;
		final String full = (url + ":large").replaceFirst("https?://", "https://");
		final String preview = fullImage ? full : (url + ":medium").replaceFirst("https?://", "https://");
		return PreviewMedia.newImage(preview, full);
	}

	private static PreviewMedia getYfrogImage(final String id, final String orig, final boolean fullImage) {
		if (isEmpty(id)) return null;
		final String preview = String.format("http://yfrog.com/%s:%s", id, fullImage ? "medium" : "iphone");
		return PreviewMedia.newImage(preview, orig);

	}

	public interface OnMediaClickListener {
		void onMediaClick(View view, ParcelableMedia media);
	}

	private static class ImageGridClickListener implements View.OnClickListener {
		private final OnMediaClickListener mListener;

		ImageGridClickListener(final OnMediaClickListener listener) {
			mListener = listener;
		}

		@Override
		public void onClick(final View v) {
			if (mListener == null) return;
			mListener.onMediaClick(v, (ParcelableMedia) v.getTag());
		}

	}
}
