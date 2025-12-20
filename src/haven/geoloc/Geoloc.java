package haven.geoloc;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.awt.image.WritableRaster;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import haven.Coord;
import haven.Gob;
import haven.MCache;
import haven.MiniMap;
import haven.UI;
import jerklib.util.Pair;

public class Geoloc {
	private static final int IMG_W = 100;
	private static final int IMG_H = 100;
	public static Map<Short, List<MapTileData>> geoLocs = new HashMap<Short, List<MapTileData>>();

	public static String lastCheckedTileHash = null;
	public static String lastFoundTileHash = null;
	public static Coord lastFoundTileGlobalCoord = null;

	public static void loadGeoloc() {
		DataInputStream is = null;
		try {
			is = new DataInputStream(new FileInputStream("geoloc.dat"));

			while (is.available() > 0) {
				short weight = is.readShort();
				long hash = is.readLong();
				short c1 = is.readShort();
				short c2 = is.readShort();

				List<MapTileData> geodatas;
				if (geoLocs.containsKey(weight)) {
					geodatas = geoLocs.get(weight);
				} else {
					geodatas = new ArrayList<MapTileData>();
					geoLocs.put(weight, geodatas);
				}

				geodatas.add(new MapTileData(weight, hash, c1, c2));
			}

			is.close();
		} catch (Exception e) {
			System.out.println("Failed to load geoloc.dat: " + e);
		}
	}

	public static MapTileData findMapTileMatch(BufferedImage img) throws GeolocException {
		int THRESHOLD = 200;

		img = Geoloc.preprocessMapTile(img);
		MapTileData curMtd = Geoloc.getHash(img);

		if (curMtd.weight == 0) {
			throw new GeolocException("Stand next to a river and try again!");
		}

		List<MapTileData> mtds = new ArrayList<MapTileData>();

		for (int i = 0; i < THRESHOLD; i++) {
			if (geoLocs.containsKey((short) (curMtd.weight + i))) {
				mtds.addAll(geoLocs.get((short) (curMtd.weight + i)));
			}
			if (geoLocs.containsKey((short) (curMtd.weight - i))) {
				mtds.addAll(geoLocs.get((short) (curMtd.weight - i)));
			}
		}

		if (mtds.size() == 0) {
			throw new GeolocException("This location doesn't seem to have been mapped yet.");
		}

		MapTileData bestMatch = null;
		int best = Integer.MAX_VALUE;
		for (MapTileData mtd : mtds) {
			int dist = Geoloc.hammingDistance(mtd.hash, curMtd.hash);
			if (dist < best) {
				best = dist;
				bestMatch = mtd;
			}
		}

		return bestMatch;
	}

	public static Coord syncGeodataCoords() throws GeolocException {
		MiniMap mm = UI.instance.m_util.m_ui.slen.mini;
		String tileHash = mm.getCurrentMapTileHash();

		if (tileHash == null) {
			throw new GeolocException("Could not get current map tile hash.");
		}

		BufferedImage img = mm.getCurrentMapTile(tileHash);

		if (img == null) {
			throw new GeolocException("Could not get current map tile image.");
		}

		MapTileData geodata = Geoloc.findMapTileMatch(img);

		lastFoundTileHash = tileHash;
		lastFoundTileGlobalCoord = new Coord(geodata.c1, geodata.c2);
		UI.instance.m_util.sendMessage("Sync global position: " + lastFoundTileGlobalCoord);

		return lastFoundTileGlobalCoord;
	}

	public static Coord getCurrentTileGlobalCoord() throws GeolocException {
		if (lastFoundTileGlobalCoord == null) {
			syncGeodataCoords();
		}

		MiniMap mm = UI.instance.m_util.m_ui.slen.mini;
		String tileHash = mm.getCurrentMapTileHash();

		if (tileHash == null) {
			throw new GeolocException("Could not get current map tile hash.");
		}

		Coord prev = mm.gridsHashes.get(lastFoundTileHash);
		Coord curr = mm.gridsHashes.get(tileHash);

		if (prev == null || curr == null) {
			throw new GeolocException("Could not get map tile grid coords.");
		}

		Coord delta = curr.sub(prev);
		return lastFoundTileGlobalCoord.add(delta);
	}

	public static Pair<Double, Double> getPlayerCoords() throws GeolocException {
		final Gob player = UI.instance.m_util.getPlayerGob();

		if (player == null) {
			throw new GeolocException("Could not get player gob.");
		}

		final Coord playerPos = player.getc();

		if (playerPos == null) {
			throw new GeolocException("Could not get player position.");
		}

		Coord tilePos = playerPos.div(MCache.tilesz);
		Coord withinGrid = tilePos.mod(MCache.cmaps);
		Coord withinTile = playerPos.mod(MCache.tilesz);

		Coord tileCoord = getCurrentTileGlobalCoord();

		double fracX = withinTile.x / (double) MCache.tilesz.x;
		double fracY = withinTile.y / (double) MCache.tilesz.y;
		double preciseX = tileCoord.x + (withinGrid.x + fracX) / 100.0;
		double preciseY = tileCoord.y + (withinGrid.y + fracY) / 100.0;

		return new Pair<Double, Double>(preciseX, preciseY);

	}

	public static void clear() {
		lastFoundTileHash = null;
		lastFoundTileGlobalCoord = null;
	}

	public static BufferedImage preprocessMapTile(BufferedImage img) {
		Image imgStripped = stripEverythingButBlue(img);
		return imageToBufferedImage(imgStripped, IMG_W, IMG_H);
	}

	public static BufferedImage imageToBufferedImage(Image image, int width, int height) {
		BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = dest.createGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.dispose();
		return dest;
	}

	public static MapTileData getHash(BufferedImage img) {
		WritableRaster raster = img.getRaster();
		DataBufferInt dataBuffer = (DataBufferInt) raster.getDataBuffer();
		int[] data = dataBuffer.getData();

		short weight = 0;
		for (int i = 0; i < data.length; i++) {
			if ((data[i] & 0xFFFFFF) != 0xFFFFFF) {
				weight++;
			}
		}

		PHash ph = new PHash(8, 8);
		long hash = ph.getHash(img);

		return new MapTileData(weight, hash, (short) 0, (short) 0);
	}

	public static int hammingDistance(String s1, String s2) {
		int d = 0;

		if (s1.length() != s2.length())
			return -1;

		for (int i = 0; i < s1.length(); i++) {
			if (s1.charAt(i) != s2.charAt(i))
				d++;
		}

		return d;
	}

	public static int hammingDistance(long x, long y) {
		int dist = 0;
		long val = x ^ y;

		while (val != 0) {
			dist++;
			val &= val - 1;
		}
		return dist;
	}

	public static Image stripEverythingButBlue(BufferedImage image) {
		ImageFilter filter = new RGBImageFilter() {
			public int filterRGB(int x, int y, int rgb) {
				float MIN_BLUE_HUE = 0.5f; // CYAN
				float MAX_BLUE_HUE = 0.8333333f; // MAGENTA
				float UNMAPPED_HUE = 0.6666667f;

				int r = (rgb & 0x00ff0000) >> 0x10;
				int g = (rgb & 0x0000ff00) >> 0x08;
				int b = (rgb & 0x000000ff);
				float[] hsv = new float[3];
				Color.RGBtoHSB(r, g, b, hsv);

				if (hsv[0] < MIN_BLUE_HUE || hsv[0] > MAX_BLUE_HUE || hsv[0] == UNMAPPED_HUE) {
					return Color.WHITE.getRGB();
				} else {
					return rgb;
				}
			}
		};

		ImageProducer ip = new FilteredImageSource(image.getSource(), filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
	}
}
