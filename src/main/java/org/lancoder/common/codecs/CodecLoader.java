           package org.lancoder.common.codecs;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.lancoder.common.codecs.base.AbstractCodec;
import org.lancoder.common.codecs.impl.*;
public class CodecLoader {

	private static HashMap<CodecEnum, Class<? extends AbstractCodec>> codecClasses = new HashMap<>();
	private static HashMap<CodecEnum, AbstractCodec> codecInstances = new HashMap<>();

	static {
		codecClasses.put(CodecEnum.AAC, AAC.class);
		codecClasses.put(CodecEnum.APE, Ape.class);
		codecClasses.put(CodecEnum.DTS, DTS.class);
		codecClasses.put(CodecEnum.FLAC, Flac.class);
		codecClasses.put(CodecEnum.H264, H264.class);
		codecClasses.put(CodecEnum.H265, H265.class);
     //		codecClasses.put(CodecEnum.MP3, Mp3.class);
		codecClasses.put(CodecEnum.OPUS, Opus.class);
		codecClasses.put(CodecEnum.SPEEX, Speex.class);
		codecClasses.put(CodecEnum.THEORA, Theora.class);
		codecClasses.put(CodecEnum.VORBIS, Vorbis.class);
		codecClasses.put(CodecEnum.VP8, Vp8.class);
		codecClasses.put(CodecEnum.VP9, Vp9.class);
		codecClasses.put(CodecEnum.WAVPACK, Wavpack.class);
	}

	public static AbstractCodec fromCodec(CodecEnum codecEnum) {
		AbstractCodec codec = codecInstances.get(codecEnum);
		if (codec == null) {
			codec = getInstance(codecEnum);
		}
//   		return codec;
	}

	/**
	 * Lazy instantiation of the codecs
	 * 
	 * @param codecEnum
	 *            The codec to instantiate
	 * @return The codec object or null if not found
	 */
	private static AbstractCodec getInstance(CodecEnum codecEnum) {
		AbstractCodec codecInstance = null;
		try {
			Class<? extends AbstractCodec> clazz = codecClasses.get(codecEnum);
			if (clazz == null) {
				System.err.println("Unsupported codec: " + codecEnum.getPrettyName());
			} else {
				Constructor<? extends AbstractCodec> cons = clazz.getDeclaredConstructor();
				cons.setAccessible(true); // Constructor is protected
				codecInstance = cons.newInstance();
				codecInstances.put(codecEnum, codecInstance);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return codecInstance;
	}

  //   }
