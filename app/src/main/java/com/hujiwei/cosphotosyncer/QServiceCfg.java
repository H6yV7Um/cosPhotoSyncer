package com.hujiwei.cosphotosyncer;

import android.content.Context;
import android.os.Environment;
import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.common.Region;
import com.tencent.qcloud.core.auth.ShortTimeCredentialProvider;

public class QServiceCfg {

	/** 腾讯云 cos 服务的 appid */
	private final String appid = "1256182125";

	/** appid 对应的 秘钥 */
	private final String secretId = "AKIDw1yjIeFnjXHo9IsyZ0FNJ3bx74qVAL5Y";

	/** appid 对应的 秘钥 */
	private final String secretKey = "qcCZkkcQJ2oruqbAcsxqXCeBWfbLF397";

	/** bucketForObjectAPITest 所处在的地域 */
	private String region = Region.AP_Shanghai.getRegion();

	private String bucket = "hjw-1256182125";

	private String cosPhotoPath = "photo/";

	private String localPhotoPath;

	/**
	 * xml sdk 服务类: 通过 CosXmlService 调用各种API服务
	 */
	public CosXmlService cosXmlService;

	private static volatile QServiceCfg instance;

	private Context context;

	public static QServiceCfg instance(Context context) {
		if (instance == null) {
			synchronized (QServiceCfg.class) {
				instance = new QServiceCfg(context);
			}
		}
		return instance;
	}

	@SuppressWarnings("deprecation")
    private QServiceCfg(Context context) {
		this.context = context;
		CosXmlServiceConfig cosXmlServiceConfig = new CosXmlServiceConfig.Builder().isHttps(false).setAppidAndRegion(appid, region)
				.setDebuggable(false).builder();
		cosXmlService = new CosXmlService(context, cosXmlServiceConfig, new ShortTimeCredentialProvider(secretId, secretKey, 600));
		localPhotoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
		if (android.os.Build.BRAND.contains("Xiaomi")) {
			localPhotoPath += "/Camera";
		}
	}

	public String getBucket() {
		return bucket;
	}

	public String getCosPhotoPath() {
		return cosPhotoPath;
	}

	public String getGetCosPath() {
		return cosPhotoPath;
	}

	public String getLocalPhtotPath() {
		return localPhotoPath;
	}

	public Context getContext() {
		return context;
	}
}
