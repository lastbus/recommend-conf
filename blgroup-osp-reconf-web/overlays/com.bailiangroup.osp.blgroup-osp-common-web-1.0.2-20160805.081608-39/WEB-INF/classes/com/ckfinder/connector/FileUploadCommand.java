/*
 * CKFinder
 * ========
 * http://ckfinder.com
 * Copyright (C) 2007-2013, CKSource - Frederico Knabben. All rights reserved.
 *
 * The software, this file and its contents are subject to the CKFinder
 * License. Please read the license.txt file before using, installing, copying,
 * modifying or distribute this file or part of its contents. The contents of
 * this file is part of the Source Code of CKFinder.
 */
package com.ckfinder.connector;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadBase.IOFileUploadException;
import org.apache.commons.fileupload.FileUploadBase.InvalidContentTypeException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bailiangroup.osp.base.core.util.FtpClientUtil;
import com.bailiangroup.osp.base.core.util.HttpUtilsV2;
import com.bailiangroup.osp.base.core.util.SpringContextHolder;
import com.bailiangroup.osp.base.core.util.StringUtils;
import com.bailiangroup.osp.common.config.FtpProperties;
import com.bailiangroup.osp.common.config.Global;
import com.ckfinder.connector.configuration.Constants;
import com.ckfinder.connector.configuration.Events.EventTypes;
import com.ckfinder.connector.configuration.IConfiguration;
import com.ckfinder.connector.data.AfterFileUploadEventArgs;
import com.ckfinder.connector.errors.ConnectorException;
import com.ckfinder.connector.errors.ErrorUtils;
import com.ckfinder.connector.handlers.command.Command;
import com.ckfinder.connector.handlers.command.IPostCommand;
import com.ckfinder.connector.utils.AccessControlUtil;
import com.ckfinder.connector.utils.FileUtils;
import com.ckfinder.connector.utils.ImageUtils;

import net.sf.json.JSONObject;

/**
 * Class to handle
 * <code>FileUpload</code> command.
 */
public class FileUploadCommand extends Command implements IPostCommand {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadCommand.class);
	/**
	 * uploading file name request.
	 */
	protected String fileName;
	/**
	 * file name after rename.
	 */
	protected String newFileName;
	/**
	 * function number to call after file upload is completed.
	 */
	protected String ckEditorFuncNum;
	/**
	 * the selected response type to be used after file upload is completed.
	 */
	protected String responseType;
	/**
	 * function number to call after file upload is completed.
	 */
	protected String ckFinderFuncNum;
	/**
	 * connector language.
	 */
	private String langCode;
	/**
	 * flag if file was uploaded correctly.
	 */
	protected boolean uploaded;
	/**
	 * error code number.
	 */
	protected int errorCode;
	private static final char[] UNSAFE_FILE_NAME_CHARS = {':', '*', '?', '|', '/'};
	
	private static final String BASE64 = "base64Content";
	
	/**
	 * default constructor.
	 */
	public FileUploadCommand() {
		this.errorCode = 0;
		this.fileName = "";
		this.newFileName = "";
		this.type = "";
		this.uploaded = false;
	}

	/**
	 * execute file upload command.
	 *
	 * @param out output stream from response.
	 * @throws ConnectorException when error occurs.
	 */
	@Override
	public void execute(final OutputStream out) throws ConnectorException {
		if (configuration.isDebugMode() && this.exception != null) {
			throw new ConnectorException(this.errorCode, this.exception);
		}
		try {
			String errorMsg = (this.errorCode == 0) ? "" : ErrorUtils.getInstance().getErrorMsgByLangAndCode(this.langCode,
					this.errorCode, this.configuration);
			errorMsg = errorMsg.replaceAll("%1", this.newFileName);
			String path = "";

			if (!uploaded) {
				this.newFileName = "";
				this.currentFolder = "";
			} else {
				path = configuration.getTypes().get(type).getUrl()
						+ this.currentFolder;
			}

			if (this.responseType != null && this.responseType.equals("txt")) {
				out.write((this.newFileName + "|" + errorMsg).getBytes("UTF-8"));
			} else {
				out.write("<script type=\"text/javascript\">".getBytes("UTF-8"));
				if (checkFuncNum()) {
					handleOnUploadCompleteCallFuncResponse(out, errorMsg, path);
				} else {
					handleOnUploadCompleteResponse(out, errorMsg);
				}
				out.write("</script>".getBytes("UTF-8"));
			}

		} catch (IOException e) {
			throw new ConnectorException(
					Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED, e);
		}

	}

	/**
	 * check if func num is set in request.
	 *
	 * @return true if is.
	 */
	protected boolean checkFuncNum() {
		return this.ckFinderFuncNum != null;
	}

	/**
	 * return response when func num is set.
	 *
	 * @param out response.
	 * @param errorMsg error message
	 * @param path path
	 * @throws IOException when error occurs.
	 */
	protected void handleOnUploadCompleteCallFuncResponse(final OutputStream out,
			final String errorMsg,
			final String path)
			throws IOException {
		this.ckFinderFuncNum = this.ckFinderFuncNum.replaceAll(
				"[^\\d]", "");
		out.write(("window.parent.CKFinder.tools.callFunction("
				+ this.ckFinderFuncNum + ", '"
				+ path
				+ FileUtils.backupWithBackSlash(this.newFileName, "'")
				+ "', '" + errorMsg + "');").getBytes("UTF-8"));
	}

	/**
	 *
	 * @param out out put stream
	 * @param errorMsg error message
	 * @throws IOException when error occurs
	 */
	protected void handleOnUploadCompleteResponse(final OutputStream out,
			final String errorMsg) throws IOException {
		out.write("window.parent.OnUploadCompleted(".getBytes("UTF-8"));
		out.write(("\'" + FileUtils.backupWithBackSlash(this.newFileName, "'") + "\'").getBytes("UTF-8"));
		out.write((", \'"
				+ (this.errorCode
				!= Constants.Errors.CKFINDER_CONNECTOR_ERROR_NONE ? errorMsg
				: "") + "\'").getBytes("UTF-8"));
		out.write(");".getBytes("UTF-8"));
	}

	/**
	 * initializing parametrs for command handler.
	 *
	 * @param request request
	 * @param configuration connector configuration.
	 * @param params execute additional params.
	 * @throws ConnectorException when error occurs.
	 */
	@Override
	public void initParams(final HttpServletRequest request,
			final IConfiguration configuration, final Object... params)
			throws ConnectorException {
		super.initParams(request, configuration, params);
		this.ckFinderFuncNum = request.getParameter("CKFinderFuncNum");
		this.ckEditorFuncNum = request.getParameter("CKEditorFuncNum");
		this.responseType = request.getParameter("response_type");
		this.langCode = request.getParameter("langCode");
		
		if (this.errorCode == Constants.Errors.CKFINDER_CONNECTOR_ERROR_NONE) {
			this.uploaded = uploadFile(request);
		}


	}

	/**
	 * uploads file and saves to file.
	 *
	 * @param request request
	 * @return true if uploaded correctly.
	 */
	private boolean uploadFile(final HttpServletRequest request) {
		if (!AccessControlUtil.getInstance(configuration).checkFolderACL(
				this.type, this.currentFolder, this.userRole,
				AccessControlUtil.CKFINDER_CONNECTOR_ACL_FILE_UPLOAD)) {
			this.errorCode = Constants.Errors.CKFINDER_CONNECTOR_ERROR_UNAUTHORIZED;
			return false;
		}
		return fileUpload(request);
	}
	
	/**
	 * 根据Cookie获取是否压缩图片
	 * @param request HttpServletRequest
	 * @return 压缩图片返回true，否则false
	 */
	private boolean getResizeFlag(final HttpServletRequest request) {
		boolean resizeFlag = false;
		
		Cookie[] cookies = request.getCookies();
		if(cookies != null) {
			for(Cookie cookie:cookies) {
				if("CKFinder_Settings".equals(cookie.getName())) {
					LOGGER.info("cookie key {}={}",cookie.getName(), cookie.getValue());
					String val = cookie.getValue();
					if(val.endsWith("R")) {
						resizeFlag = true;
					}
				}
			}
		}
		
		return resizeFlag;
	}

	/**
	 *
	 * @param request http request
	 * @return true if uploaded correctly
	 */
	@SuppressWarnings("unchecked")
	private boolean fileUpload(final HttpServletRequest request) {
		try {
			DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
			ServletFileUpload uploadHandler = new ServletFileUpload(
					fileItemFactory);
			FtpProperties config=SpringContextHolder.getBean("ftpConfig");
			String baseDir=config.getFtpDir();
			//上传图片的时候，是否需要压缩
			boolean resizeFlag = this.getResizeFlag(request);
			List<FileItem> items = uploadHandler.parseRequest(request);
			for (FileItem item : items) {
				if (!item.isFormField()) {
					String path="/upload"+baseDir+"/images"+(currentFolder!=null?currentFolder:"");
					path=request.getSession().getServletContext().getRealPath(path);
					
					try {
						if (validateUploadItem(item, path)) {
							String uploadFile="";
							this.fileName = getFileItemName(item);
							String newF=this.getFinalFileName(path, fileName);
							if(path!=null&&path.endsWith("/"))
								uploadFile=path+newF;
							else
								uploadFile=path+"/"+newF;
							//判断文件是否存在
							//删除本地文件
							File delFile=new File(uploadFile);
							if(delFile.exists())
							{
								delFile.delete();
								String rfile="";
								if(currentFolder!=null&&!"".equals(currentFolder))
								{
									
									if(currentFolder!=null&&currentFolder.endsWith("/"))
										rfile=currentFolder+newF;
									else
										rfile=currentFolder+"/"+newF;
								}
								String ftpPath="/images" + rfile;
								FtpClientUtil.del(config.getFtpUrl(), config.getFtpPort(), config.getFtpUsername(), config.getFtpPassword(),ftpPath);
							}
							boolean success=false;
							success=saveTemporaryFile(path, item, resizeFlag);
							//判断文件是否完整上传
							/*int timeCount=0;
							while(true)
							{
								if(item.get().length==delFile.length())
								{
									this.fileToFtp(request);
									break;
								}
								else
								{
									Thread.sleep(1000);
									timeCount=timeCount+1;
								}
								if(timeCount>=8)
									break;
							}*/
							this.fileToFtp(request);
							return success;
						}
					} finally {
						item.delete();
					}
				}
			}
			return false;
		} catch (InvalidContentTypeException e) {
			if (configuration.isDebugMode()) {
				this.exception = e;
			}
			this.errorCode = Constants.Errors.CKFINDER_CONNECTOR_ERROR_UPLOADED_CORRUPT;
			return false;
		} catch (IOFileUploadException e) {
			if (configuration.isDebugMode()) {
				this.exception = e;
			}
			this.errorCode = Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED;
			return false;
		} catch (SizeLimitExceededException e) {
			this.errorCode = Constants.Errors.CKFINDER_CONNECTOR_ERROR_UPLOADED_TOO_BIG;
			return false;
		} catch (FileSizeLimitExceededException e) {
			this.errorCode = Constants.Errors.CKFINDER_CONNECTOR_ERROR_UPLOADED_TOO_BIG;
			return false;
		} catch (ConnectorException e) {
			this.errorCode = e.getErrorCode();
			return false;
		} catch (Exception e) {
			if (configuration.isDebugMode()) {
				this.exception = e;
			}
			LOGGER.error("Exception",e);
			this.errorCode = Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED;
			return false;
		}

	}

	
	private void fileToFtp(final HttpServletRequest request)
	{
		
		FtpProperties config=SpringContextHolder.getBean("ftpConfig");
		String userFtp=config.getFtpUse();
		if(!"true".equals(userFtp))
			return ;
		
		if(currentFolder!=null&&!"".equals(currentFolder))
		{
			String rfile="";
			if(currentFolder!=null&&currentFolder.endsWith("/"))
				rfile=currentFolder+newFileName;
			else
				rfile=currentFolder+"/"+newFileName;
			//ContextService cxtService = SpringContextHolder.getBean(ContextService.class);
			//User user = cxtService.getCurrentUser();
			String localR="/upload"+config.getFtpDir()+"/images"+rfile;
			String realPath=request.getSession().getServletContext().getRealPath(localR);
			String ftpPath="/images"+rfile;
			try {
				/*File checkFile=new File(realPath);
				while(true)
				{
					if(!checkFile.exists())
					{
						try {
							Thread.sleep(300);
						} catch (InterruptedException e) {
							
						}
					}
					else
						break;
				}*/
				FtpClientUtil.sendFile2FtpServer(config.getFtpUrl(), config.getFtpPort(), config.getFtpUsername(), config.getFtpPassword(), realPath,ftpPath);
			} catch (NumberFormatException e) {
				LOGGER.error("upload file:"+ftpPath+" error.", e);
			} catch (IOException e) {
				LOGGER.error("upload file:"+ftpPath+" error.", e);
			}
			
		}
	}
	/**
	 * saves temporary file in the correct file path.
	 *
	 * @param path path to save file
	 * @param item file upload item
	 * @return result of saving, true if saved correctly
	 * @throws Exception when error occurs.
	 */
	private boolean saveTemporaryFile(final String path, final FileItem item, boolean resizeFlag)
			throws Exception {
		File file = new File(path, this.newFileName);
		AfterFileUploadEventArgs args = new AfterFileUploadEventArgs();
		args.setCurrentFolder(this.currentFolder);
		args.setFile(file);
		args.setFileContent(item.get());
		if (!ImageUtils.isImage(file)) {
			item.write(file);
			if (configuration.getEvents() != null) {
				configuration.getEvents().run(EventTypes.AfterFileUpload,
						args, configuration);
			}
			return true;
		} else if (ImageUtils.checkImageSize(item.getInputStream(), this.configuration)) {
			//调用图片压缩接口，并生成压缩图片。如果失败，则不压缩该图片
			if(!this.resizeFile(item, file, resizeFlag)) {
				ImageUtils.createTmpThumb(item.getInputStream(), file, getFileItemName(item), this.configuration);
			}
			if (configuration.getEvents() != null) {
				configuration.getEvents().run(EventTypes.AfterFileUpload,
						args, configuration);
			}
			return true;
		} else if (configuration.checkSizeAfterScaling()) {
			//调用图片压缩接口，并生成压缩图片。如果失败，则不压缩该图片
			if(!this.resizeFile(item, file, resizeFlag)) {
				ImageUtils.createTmpThumb(item.getInputStream(), file, getFileItemName(item), this.configuration);
			}
			if (FileUtils.checkFileSize(configuration.getTypes().get(this.type), file.length())) {
				if (configuration.getEvents() != null) {
					configuration.getEvents().run(EventTypes.AfterFileUpload,
							args, configuration);
				}
				return true;
			} else {
				file.delete();
				this.errorCode =
						Constants.Errors.CKFINDER_CONNECTOR_ERROR_UPLOADED_TOO_BIG;
				return false;
			}
		}
		return false;
	}
	
	/**
	 * 创建图片压缩服务入参JSONObject对象
	 *
	 * @param FileItem fileItem
	 * @return JSONObject
	 */
	private JSONObject createJsonObj(FileItem fileItem) {
		if(fileItem == null) {
			return null;
		}
		String fileType = fileItem.getName().substring(fileItem.getName().lastIndexOf(".") + 1);
		if (StringUtils.isEmpty(fileType) || (!"png".equals(StringUtils.lowerCase(fileType))
				&& !"jpg".equals(StringUtils.lowerCase(fileType)) && !"jpeg".equals(StringUtils.lowerCase(fileType)))) {
			return null;
		}
		InputStream ins=null;
		try {
			ins = fileItem.getInputStream();
		} catch (IOException e) {
			LOGGER.error("error in calling fileItem.getInputStream()", e);
			return null;
		}
		JSONObject json = new JSONObject();
		json.put("mediaType", fileType);
		json.put("reSize", "1");
		json.put("fileName", "test");
		json.put("base64Content", FileUtils.inputStreamToBase64Str(ins));
		json.put("appId", "CKFinderFileUpload");
		json.put("quality", "95d");
		return json;
	}
	
	/**
	 * 压缩 inputstream .
	 *
	 * @param FileItem fileItem
	 */
	private boolean resizeFile(FileItem fileItem, File file, boolean resizeFlag) {
		boolean retFlag = false;
		LOGGER.info("ResizeFile started! 文件名:{},是否压缩：{}", fileItem.getName(), resizeFlag);
		if(!resizeFlag) return retFlag;
		//创建JSON参数
		JSONObject json = createJsonObj(fileItem);
		if(json == null){
			LOGGER.info("文件格式不符合压缩要求，不进行压缩处理");
			return retFlag;
		}
		//获取压缩图片地址URL
		String url = Global.getConfig("resize.path");
		if(StringUtils.isEmpty(url)) {
			LOGGER.info("由于没有配置resize.path，故图片不进行压缩处理");
			return retFlag;
		}
		try {
			LOGGER.info("get resize.path url = {}, quality={}", url, json.get("quality"));
			String respose = HttpUtilsV2.doPost(url, json.toString());
			if(respose==null) return retFlag;
			retFlag = createFile(JSONObject.fromObject(respose), file);
		} catch (Exception e) {
			LOGGER.error("调用图片压缩接口失败", e);
			return false;
		}
		
		return retFlag;
	}

	/**
	 * 根据图片压缩接口返回的JSON创建文件
	 *
	 * @param JSONObject jsonsb
	 * @param File file
	 * @return 成功：true.失败：false
	 */
	private boolean createFile(JSONObject jsonsb, File file) {
		boolean retFlag = false;
		
		if(!jsonsb.has(BASE64)) {
         	return retFlag;
        }
		BufferedOutputStream bos = null; 
    	try {
    		//对返回数据进行base64解码，生产字节数组
			byte[] buf = FileUtils.base64ToInputStream(jsonsb.getString(BASE64));
			bos = new BufferedOutputStream(new FileOutputStream(file));
			bos.write(buf);
			bos.flush();
	        retFlag = true;
		} catch (Exception e) {
			LOGGER.error("根据图片压缩接口返回的JSON，创建文件失败", e );
		} finally {
			try {
				bos.close();
			} catch (IOException e) {
				LOGGER.error("根据图片压缩接口返回的JSON，创建文件失败", e );
				retFlag = false;
			}
		}
    	
		return retFlag;
	}

	/**
	 * if file exists this method adds (number) to file.
	 *
	 * @param path folder
	 * @param name file name
	 * @return new file name.
	 */
	private String getFinalFileName(final String path, final String name) {
		File file = new File(path, name);
		int number = 0;

		String nameWithoutExtension = FileUtils.getFileNameWithoutExtension(name, false);
		Pattern p = Pattern.compile("^(AUX|COM\\d|CLOCK\\$|CON|NUL|PRN|LPT\\d)$", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(nameWithoutExtension);
		boolean protectedName = m.find() ? true : false;

		while (true) {
			if (file.exists() || protectedName) {
				number++;
				StringBuilder sb = new StringBuilder();
				sb.append(FileUtils.getFileNameWithoutExtension(name, false));
				sb.append("(" + number + ").");
				sb.append(FileUtils.getFileExtension(name, false));
				this.newFileName = sb.toString();
				file = new File(path, this.newFileName);
				this.errorCode =
						Constants.Errors.CKFINDER_CONNECTOR_ERROR_UPLOADED_FILE_RENAMED;
				protectedName = false;
			} else {
				return this.newFileName;
			}
		}
	}

	/**
	 * validates uploaded file.
	 *
	 * @param item uploaded item.
	 * @param path file path
	 * @return true if validation
	 */
	private boolean validateUploadItem(final FileItem item, final String path) {

		if (item.getName() != null && item.getName().length() > 0) {
			this.fileName = getFileItemName(item);
		} else {
			this.errorCode = Constants.Errors.CKFINDER_CONNECTOR_ERROR_UPLOADED_INVALID;
			return false;
		}
		this.newFileName = this.fileName;

		for (char c : UNSAFE_FILE_NAME_CHARS) {
			this.newFileName = this.newFileName.replace(c, '_');
		}

		if (configuration.isDisallowUnsafeCharacters()) {
			this.newFileName = this.newFileName.replace(';', '_');
		}
		if (configuration.forceASCII()) {
			this.newFileName = FileUtils.convertToASCII(this.newFileName);
		}
		if (!this.newFileName.equals(this.fileName)) {
			this.errorCode =
					Constants.Errors.CKFINDER_CONNECTOR_ERROR_UPLOADED_INVALID_NAME_RENAMED;
		}


		if (FileUtils.checkIfDirIsHidden(this.currentFolder, configuration)) {
			this.errorCode = Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST;
			return false;
		}
		if (!FileUtils.checkFileName(this.newFileName)
				|| FileUtils.checkIfFileIsHidden(this.newFileName,
				configuration)) {
			this.errorCode = Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_NAME;
			return false;
		}
		int checkFileExt = FileUtils.checkFileExtension(this.newFileName,
				configuration.getTypes().get(type));
		if (checkFileExt == 1) {
			this.errorCode = Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_EXTENSION;
			return false;
		}
		if (configuration.ckeckDoubleFileExtensions()) {
			this.newFileName = FileUtils.renameFileWithBadExt(configuration.getTypes().get(type), this.newFileName);
		}

		try {
			File file = new File(path, getFinalFileName(path, this.newFileName));
			if (!FileUtils.checkFileSize(configuration.getTypes().get(this.type),
					item.getSize())
					&& !(configuration.checkSizeAfterScaling() && ImageUtils.isImage(file))) {
				this.errorCode =
						Constants.Errors.CKFINDER_CONNECTOR_ERROR_UPLOADED_TOO_BIG;
				return false;
			}

			if (configuration.getSecureImageUploads() && ImageUtils.isImage(file)
					&& !ImageUtils.checkImageFile(item)) {
				this.errorCode =
						Constants.Errors.CKFINDER_CONNECTOR_ERROR_UPLOADED_CORRUPT;
				return false;
			}

			if (!FileUtils.checkIfFileIsHtmlFile(file.getName(), configuration)
					&& FileUtils.detectHtml(item)) {
				this.errorCode =
						Constants.Errors.CKFINDER_CONNECTOR_ERROR_UPLOADED_WRONG_HTML_FILE;
				return false;
			}
		} catch (SecurityException e) {
			if (configuration.isDebugMode()) {
				this.exception = e;
			}
			this.errorCode = Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED;
			return false;
		} catch (IOException e) {
			if (configuration.isDebugMode()) {
				this.exception = e;
			}
			this.errorCode = Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED;
			return false;
		}


		return true;
	}

	/**
	 * set response headers. Not user in this command.
	 *
	 * @param response response
	 * @param sc servlet context
	 */
	@Override
	public void setResponseHeader(final HttpServletResponse response,
			final ServletContext sc) {
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html");
	}

	/**
	 * save if uploaded file item name is full file path not only file name.
	 *
	 * @param item file upload item
	 * @return file name of uploaded item
	 */
	private String getFileItemName(final FileItem item) {
		Pattern p = Pattern.compile("[^\\\\/]+$");
		Matcher m = p.matcher(item.getName());

		return (m.find()) ? m.group(0) : "";
	}

	/**
	 * check request for security issue.
	 *
	 * @param reqParam request param
	 * @return true if validation passed
	 * @throws ConnectorException if valdation error occurs.
	 */
	protected boolean checkParam(final String reqParam)
			throws ConnectorException {
		if (reqParam == null || reqParam.equals("")) {
			return true;
		}
		if (Pattern.compile(Constants.INVALID_PATH_REGEX).matcher(reqParam).find()) {
			this.errorCode = Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_NAME;
			return false;
		}
		return true;
	}

	@Override
	protected boolean checkHidden()
			throws ConnectorException {
		if (FileUtils.checkIfDirIsHidden(this.currentFolder, configuration)) {
			this.errorCode = Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST;
			return true;
		}
		return false;
	}

	@Override
	protected boolean checkConnector(final HttpServletRequest request)
			throws ConnectorException {
		if (!configuration.enabled() || !configuration.checkAuthentication(request)) {
			this.errorCode =
					Constants.Errors.CKFINDER_CONNECTOR_ERROR_CONNECTOR_DISABLED;
			return false;
		}
		return true;
	}

	@Override
	protected boolean checkIfCurrFolderExists(final HttpServletRequest request)
			throws ConnectorException {
		String tmpType = getParameter(request, "type");
		File currDir = new File(configuration.getTypes().get(tmpType).getPath()
				+ this.currentFolder);
		if (currDir.exists() && currDir.isDirectory()) {
			return true;
		} else {
			this.errorCode = Constants.Errors.CKFINDER_CONNECTOR_ERROR_FOLDER_NOT_FOUND;
			return false;
		}
	}
}
