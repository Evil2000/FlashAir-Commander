package de.evil2000.flashaircommander;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class FileListAdapter extends ArrayAdapter<HashMap<String, String>> {
	private Context appContext;
	private EditAdapterCallback callback;
	private ArrayList<String> ImageExtensions = new ArrayList<String>();
	private ArrayList<String> MovieExtensions = new ArrayList<String>();

	public FileListAdapter(Context context, ArrayList<HashMap<String, String>> objects) {
		super(context, 0, objects);
		appContext = context;
		ImageExtensions.addAll(Arrays.asList("ANI", "ANIM", "APNG", "ART", "BMP", "BPG", "BSAVE", "CAL", "CIN", "CPC", "CPT", "DDS", "DPX", "ECW", "EXR",
				"FITS", "FLIC", "FPX", "GIF", "HDRI", "HEVC", "ICER", "ICNS", "ICO", "CUR", "ICS", "ILBM", "JBIG", "JBIG2", "JNG", "JPEG", "JPEG2000",
				"JPEG-LS", "JPEGXR", "MNG", "MIFF", "NRRD", "PAM", "PBM", "PGM", "PPM", "PNM", "PCX", "PGF", "PICTOR", "PNG", "PSD", "PSB", "PSP", "QTVR",
				"RAS", "RBE", "JPEG-HDR", "SGI", "TGA", "TIFF", "TIFF", "EP", "TIFF", "IT", "WBMP", "WebP", "XBM", "XCF", "XPM", "XWD", "CIFF", "DNG", "AI",
				"CDR", "CGM", "DXF", "EVA", "EMF", "Gerber", "HVIF", "IGES", "PGML", "SVG", "VML", "WMF", "Xar", "CDF", "DjVu", "EPS", "PDF", "PICT", "PS",
				"SWF", "XAML", "CR2"));
		MovieExtensions.addAll(Arrays.asList("3g2", "3gp", "4xm", "a64", "aac", "ac3", "act", "adf", "adts", "adx", "aea", "afc", "aiff", "alaw", "alsa",
				"amr", "anm", "apc", "ape", "aqtitle", "asf", "asf_stream", "ass", "ast", "au", "avi", "avm2", "avr", "avs", "bethsoftvid", "bfi", "bin",
				"bink", "bit", "bmv", "brstm", "c93", "caca", "caf", "cavsvideo", "cdg", "cdxl", "concat", "crc", "daud", "dfa", "dirac", "dnxhd", "dsicin",
				"dts", "dtshd", "dv", "dv1394", "dvd", "dxa", "ea", "ea_cdata", "eac3", "epaf", "f32be", "f32le", "f4v", "f64be", "f64le", "fbdev", "ffm",
				"ffmetadata", "film_cpk", "filmstrip", "flac", "flic", "flv", "framecrc", "framemd5", "frm", "g722", "g723_1", "g729", "gif", "gsm", "gxf",
				"h261", "h263", "h264", "hls", "hls", "applehttp", "ico", "idcin", "idf", "iff", "ilbc", "image2", "image2pipe", "ingenient", "ipmovie",
				"ipod", "ircam", "ismv", "iss", "iv8", "ivf", "jacosub", "jv", "latm", "lavfi", "libcdio", "libdc1394", "lmlm4", "loas", "lvf", "lxf", "m4v",
				"matroska", "matroska", "webm", "md5", "mgsts", "microdvd", "mjpeg", "mkvtimestamp_v2", "mlp", "mm", "mmf", "mov", "mov", "mp4", "m4a", "3gp",
				"3g2", "mj2", "mp2", "mp3", "mp4", "mpc", "mpc8", "mpeg", "mpeg1video", "mpeg2video", "mpegts", "mpegtsraw", "mpegvideo", "mpjpeg", "mpl2",
				"mpsub", "msnwctcp", "mtv", "mulaw", "mv", "mvi", "mxf", "mxf_d10", "mxg", "nc", "nistsphere", "nsv", "null", "nut", "nuv", "ogg", "oma",
				"paf", "pjs", "pmp", "psp", "psxstr", "pva", "pvf", "qcp", "r3d", "rawvideo", "rcv", "realtext", "rl2", "rm", "roq", "rpl", "rso", "rtp",
				"rtsp", "s16be", "s16le", "s24be", "s24le", "s32be", "s32le", "s8", "sami", "sap", "sbg", "sdl", "sdp", "segment", "shn", "siff", "smjpeg",
				"smk", "smoothstreaming", "smush", "sol", "sox", "spdif", "srt", "stream_segment", "ssegment", "subviewer", "subviewer1", "svcd", "swf", "tak",
				"tedcaptions", "tee", "thp", "tiertexseq", "tmv", "truehd", "tta", "tty", "txd", "u16be", "u16le", "u24be", "u24le", "u32be", "u32le", "u8",
				"vc1", "vc1test", "vcd", "video4linux2", "v4l2", "vivo", "vmd", "vob", "vobsub", "voc", "vplayer", "vqf", "w64", "wav", "wc3movie", "webm",
				"webvtt", "wsaud", "wsvqa", "wtv", "wv", "x11grab", "xa", "xbin", "xmv", "xwma", "yop", "yuv4mpegpipe"));
	}
	
	public HashMap<String, String> getFileRecord(String filename) {
		int c = getCount();
		
		for (int i = 0; i < c; i++) {
			HashMap<String, String> t = getItem(i);
			String fqFilename = t.get("directory") + "/" +t.get("filename"); 
			if (fqFilename.equals(filename)) {
				return t;
			}
		}
		
		return null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imgThumb;
		TextView lblLine1;
		TextView lblLine2;
		TextView lblLine3;
		CheckBox chkMark;

		// Check if an existing view is being reused, otherwise inflate the view
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_filelist, parent, false);
			imgThumb = (ImageView) convertView.findViewById(R.id.imgThumbnail);
			lblLine1 = (TextView) convertView.findViewById(R.id.lblLine1);
			lblLine2 = (TextView) convertView.findViewById(R.id.lblLine2);
			lblLine3 = (TextView) convertView.findViewById(R.id.lblLine3);
			chkMark = (CheckBox) convertView.findViewById(R.id.chkMark);
			convertView.setTag(new ViewHolder(imgThumb, lblLine1, lblLine2, lblLine3, chkMark));
		} else {
			ViewHolder viewHolder = (ViewHolder) convertView.getTag();
			imgThumb = viewHolder.imgThumb;
			lblLine1 = viewHolder.lblText1;
			lblLine2 = viewHolder.lblText2;
			lblLine3 = viewHolder.lblText3;
			chkMark = viewHolder.chkMark;
		}

		final HashMap<String, String> file = getItem(position);

		// Check if file entry is a directory
		if (file.get("attributes").contains("d")) {
			// display folder icon
			imgThumb.setImageDrawable(ContextCompat.getDrawable(appContext, R.drawable.folder));
			// hide "mark for download" checkbox
			chkMark.setVisibility(View.GONE);
		} else {
			// show "mark for download" checkbox
			chkMark.setVisibility(View.VISIBLE);
			
			// Generate the cacheFilename fom dirpath, filename and size
			String cacheFilename = appContext.getCacheDir().getAbsolutePath() + File.separator + file.get("directory").replace("/", ".") + file.get("filename")
					+ file.get("size");
			// If file is already cached, use it as thumbnail
			if ((new File(cacheFilename)).exists()) {
				imgThumb.setImageDrawable(Drawable.createFromPath(cacheFilename));
			} else {
				// If file is not cached, display an icon instead.
				String ext = file.get("filename").substring(file.get("filename").lastIndexOf(".") + 1);
				if (ImageExtensions.contains(ext.toUpperCase()))
					imgThumb.setImageDrawable(ContextCompat.getDrawable(appContext, R.drawable.image));
				else if (MovieExtensions.contains(ext.toLowerCase()))
					imgThumb.setImageDrawable(ContextCompat.getDrawable(appContext, R.drawable.movie));
				else
					imgThumb.setImageDrawable(ContextCompat.getDrawable(appContext, R.drawable.file));
			}
		}

		// Set the OnCheckedChangeListener every time to avoid the use of a recycled listener 
		chkMark.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					callback.addFileToDownloadList(file.get("directory") + "/" + file.get("filename"), Long.valueOf(file.get("size")));
				} else {
					callback.removeFileFromDownloadList(file.get("directory") + "/" + file.get("filename"), Long.valueOf(file.get("size")));
				}
			}

		});

		// Check the box if file is already marked for download
		chkMark.setChecked(callback.isFileInDownloadList(file.get("directory") + "/" + file.get("filename")));
		// Display filename and date/time
		lblLine1.setText(file.get("filename"));
		lblLine2.setText(file.get("date") + " " + file.get("time"));
		if (file.get("attributes").contains("d")) {
			lblLine3.setText("");
		} else {
			lblLine3.setText(FlashAirCommander.humanReadableByteCount(Long.valueOf(file.get("size")), false));
		}

		return convertView;
	}

	/**
	 * For data exchange with the FlashAirCommander.class we use an interface "EditAdapterCallback", which callback class (in this case FlashAirCommander class) will be set through this function.
	 * 
	 * @param callback
	 */
	public void setEditAdapterCallback(EditAdapterCallback callback) {
		this.callback = callback;
	}

	/**
	 * The callback interface which every class must implement which wishes to get called from this adapter.
	 * 
	 * @author axnrl
	 *
	 */
	public interface EditAdapterCallback {
		public void addFileToDownloadList(String filename, Long size);

		public void removeFileFromDownloadList(String filename, Long size);

		public boolean isFileInDownloadList(String filename);
	}

	/**
	 * Holds the single items from the listview to get recycled.
	 * 
	 * @author axnrl
	 */
	private static class ViewHolder {
		public final ImageView imgThumb;
		public final TextView lblText1;
		public final TextView lblText2;
		public final TextView lblText3;
		public final CheckBox chkMark;

		public ViewHolder(ImageView imgThumb, TextView lblText1, TextView lblText2, TextView lblText3, CheckBox chkMark) {
			this.imgThumb = imgThumb;
			this.lblText1 = lblText1;
			this.lblText2 = lblText2;
			this.lblText3 = lblText3;
			this.chkMark = chkMark;
		}
	}
}