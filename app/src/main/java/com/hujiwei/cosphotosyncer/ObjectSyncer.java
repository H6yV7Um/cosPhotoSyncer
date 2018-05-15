package com.hujiwei.cosphotosyncer;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.model.bucket.GetBucketRequest;
import com.tencent.cos.xml.model.bucket.GetBucketResult;
import com.tencent.cos.xml.model.object.GetObjectRequest;
import com.tencent.cos.xml.model.object.GetObjectResult;
import com.tencent.cos.xml.model.object.PutObjectRequest;
import com.tencent.cos.xml.model.object.PutObjectResult;
import com.tencent.cos.xml.model.tag.ListBucket;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class ObjectSyncer {

	private QServiceCfg config;

	private Handler handler;

	public ObjectSyncer(QServiceCfg config, Handler handler) {
		this.config = config;
		this.handler = handler;
	}

	public void start() {
		sendMsg(0, null);
		try {
			List<ListBucket.Contents> cosPhotos = getFileSummaryList();
			File[] needUploadPhotos = getNeedUploadPhotos(cosPhotos);
			List<String> needDownloadPhotos = getNeedDownloadPhotos(cosPhotos);
			int length = needUploadPhotos.length;
			if (length == 0 && needDownloadPhotos.isEmpty()) {
				sendMsg(1, "没有照片需要同步");
				return;
			}
			if (length > 0) {
				sendMsg(2, new Object[] { "同步进度:准备上传" + length + "张照片", length });
			}
			for (int i = 0; i < length; i++) {
				sendMsg(3, new Object[] { "同步进度:正在上传" + (i + 1) + "/" + length, i + 1 });
				File photo = needUploadPhotos[i];
				PutObjectRequest putObjectRequest = new PutObjectRequest(config.getBucket(), config.getCosPhotoPath() + photo.getName(),
						photo.getAbsolutePath());
				putObjectRequest.setSign(6000, null, null);
				PutObjectResult putObjectResult = config.cosXmlService.putObject(putObjectRequest);
			}
			int size = needDownloadPhotos.size();
			if (!needDownloadPhotos.isEmpty()) {
				sendMsg(2, new Object[] { "同步进度:准备下载" + size + "张照片", size });
			}
			for (int i = 0; i < size; i++) {
				String photo = needDownloadPhotos.get(i);
				sendMsg(3, new Object[] { "同步进度:正在下载" + (i + 1) + "/" + size, i + 1 });
				GetObjectRequest getObjectRequest = new GetObjectRequest(config.getBucket(), photo, config.getLocalPhtotPath());
				getObjectRequest.setSign(6000, null, null);
				GetObjectResult getObjectResult = config.cosXmlService.getObject(getObjectRequest);
				config.getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
						Uri.fromFile(new File(config.getLocalPhtotPath() + "/" + photo.substring(photo.lastIndexOf("/") + 1)))));
			}
			sendMsg(1, "同步完成:" + length + "张照片上传," + size + "张照片下载");
		} catch (Exception e) {
			sendMsg(1, "同步失败:" + e.getMessage());
		}
	}

	private void sendMsg(int code, Object obj) {
		Message msg = handler.obtainMessage();
		msg.what = code;
		msg.obj = obj;
		handler.sendMessage(msg);
	}

	private File[] getNeedUploadPhotos(final List<ListBucket.Contents> cosPhotos) throws CosXmlServiceException, CosXmlClientException {
		return new File(config.getLocalPhtotPath()).listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return (file.getName().endsWith("jpg") || file.getName().endsWith("jpeg")) && !photoExistCos(file.getName(), cosPhotos);
			}
		});
	}

	private boolean photoExistCos(String photo, List<ListBucket.Contents> summaryList) {
		for (ListBucket.Contents content : summaryList) {
			if (content.key.equals(config.getCosPhotoPath() + photo)) {
				return true;
			}
		}
		return false;
	}

	private List<String> getNeedDownloadPhotos(List<ListBucket.Contents> cosPhotos) throws CosXmlServiceException, CosXmlClientException {
		List<String> needDownloadPhotos = new ArrayList<>();
		File[] localPhotos = new File(config.getLocalPhtotPath()).listFiles();
		for (ListBucket.Contents cosPhoto : cosPhotos) {
			if (!cosPhoto.key.equals(config.getCosPhotoPath()) && !photoExistLocal(cosPhoto.key, localPhotos)) {
				needDownloadPhotos.add(cosPhoto.key);
			}
		}
		return needDownloadPhotos;
	}

	private boolean photoExistLocal(String photo, File[] localPhotos) {
		for (File localPhoto : localPhotos) {
			if ((config.getCosPhotoPath() + localPhoto.getName()).equals(photo)) {
				return true;
			}
		}
		return false;
	}

	private List<ListBucket.Contents> getFileSummaryList() throws CosXmlClientException, CosXmlServiceException {
		GetBucketRequest getBucketRequest = new GetBucketRequest(config.getBucket());
		getBucketRequest.setMaxKeys(1000);
		getBucketRequest.setSign(6000, null, null);
		List<ListBucket.Contents> summaryList = new ArrayList<>();
		getFileSummaryList(getBucketRequest, summaryList);
		return summaryList;
	}

	private void getFileSummaryList(GetBucketRequest getBucketRequest, List<ListBucket.Contents> summaryList)
			throws CosXmlClientException, CosXmlServiceException {
		GetBucketResult getBucketResult = config.cosXmlService.getBucket(getBucketRequest);
		ListBucket listBucket = getBucketResult.listBucket;
		summaryList.addAll(listBucket.contentsList);
		if (listBucket.isTruncated) {
			getBucketRequest.setMarker(listBucket.nextMarker);
			getFileSummaryList(getBucketRequest, summaryList);
		}

	}

}
