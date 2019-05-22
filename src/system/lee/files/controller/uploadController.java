package lee.files.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@Controller
@RequestMapping("/fileUpload")
public class uploadController {
	private static final int BUFFER_SIZE = 100*1024;
	
	/**
	 * plupload上传文件
	 * @param name 文件名
	 * @param chunks	文件块数
	 * @param chunk		当前块
	 * @param request	
	 * @return
	 * @throws IllegalStateException
	 * @throws IOException
	 * 
	 * @RequestParam String name,
            @RequestParam(required = false, defaultValue = "-1") int chunks,
            @RequestParam(required = false, defaultValue = "-1") int chunk,
            HttpServletRequest request,String topicId
	 */
	@ResponseBody
	@RequestMapping(value="/pluploadFile",method=RequestMethod.POST)
	public String pluploadFile(@RequestParam(value="uploadFile",required = false) MultipartFile uploadFile, HttpServletRequest request){
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		String uploadTime = simpleDateFormat.format(date);
		
		File saveFile = null;
		//初始化通用multipart解析器  
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        //判断请求中是否有文件上传  
        if (!multipartResolver.isMultipart(request)) {  
            //请求为空，直接返回  
            return null;  
        }
        
        try {
        	Integer chunk = 0, chunks = 0;
			if(null != request.getParameter("chunk") && !request.getParameter("chunk").equals("")){
				chunk = Integer.valueOf(request.getParameter("chunk"));
			}
			if(null != request.getParameter("chunks") && !request.getParameter("chunks").equals("")){
				chunks = Integer.valueOf(request.getParameter("chunks"));
			}
			String fileName = request.getParameter("name");
			//解析请求，将文件信息放到迭代器里     
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			Iterator<String> iter = multiRequest.getFileNames();  
	        //取得上传文件    
			uploadFile = multiRequest.getFile(iter.next());  
        	String path = buildTopicFilePath1(request);
        	File saveFilePath = new File(path);
			if (!saveFilePath.exists()) {
				saveFilePath.mkdirs();
			}
			//目标文件 
			saveFile = new File(path,fileName);
			/*//文件已存在删除旧文件（上传了同名的文件） 
	        if (chunk == 0 && saveFile.exists()) {  
	        	saveFile.delete();  
	        	saveFile = new File(saveFilePath, fileName);
	        }*/
        	//合成文件
	        appendFile(uploadFile.getInputStream(), saveFile);  
	        if (chunk.equals(chunks-1)) {
	        	System.out.println("上传完成");
	        	
	        }else {
	        	System.out.println("还剩["+(chunks-1-chunk)+"]个块文件");
	        }
        }catch (Exception e) {
		}
        String filePath = saveFile==null?"":saveFile.getPath();
        String filePathBase64 = "";
		try {
			filePathBase64 = URLEncoder.encode(filePath, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成的 catch 块
			//e.printStackTrace();;
		}
		return filePathBase64;
	}
	
	/**
	 * 合成分块文件
	 * @param in
	 * @param saveFile
	 */
	private void appendFile(InputStream in, File saveFile) {
        OutputStream out = null;
        try {
            // plupload 配置了chunk的时候新上传的文件append到文件末尾
            if (saveFile.exists()) {
                out = new BufferedOutputStream(new FileOutputStream(saveFile, true), BUFFER_SIZE);
            } else {
                out = new BufferedOutputStream(new FileOutputStream(saveFile),BUFFER_SIZE);
            }
            in = new BufferedInputStream(in, BUFFER_SIZE);
            
            int len = 0;
            byte[] buffer = new byte[BUFFER_SIZE];         
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        } catch (Exception e) {
        } finally {    
            try {
            	in.close();
            	out.flush();
            	out.close();
            } catch (IOException e) {
            }
        }
    }
	
	public static String buildTopicFilePath1(HttpServletRequest request) {
		
		String cp = request.getContextPath().replace("/", "");
		String path = request.getSession().getServletContext().getRealPath("").replace("\\"+cp, "");
		
		String path_head = path+"/uploadfiles";

		StringBuilder sb = new StringBuilder();
		sb.append(path_head);

		return sb.toString();
	}
}
