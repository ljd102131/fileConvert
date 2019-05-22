package lee.files.controller;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.util.GraphicsRenderingHints;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;

import lee.files.entity.RequestResult;

@Controller
@RequestMapping("/fileConvert")
public class fileConvertController{

	private static final int wdFormatPDF = 17;// PDF 格式 
	
	private static Lock wordLock = new ReentrantLock();
	
	private static Lock pdfLock = new ReentrantLock();
	
	private static Integer filePngSize = 0;

	@ResponseBody
	@RequestMapping("/toPDF")
	public RequestResult toPDF(HttpServletRequest request,String filePath,String fileName) {
		RequestResult requestResult = new RequestResult(RequestResult.FAILURE);
		try {
			filePath = filePath==null?"":URLDecoder.decode(filePath, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成的 catch 块
			//e.printStackTrace();;
		}
		String thefile = fileName==""||fileName==null?"test":fileName.substring(0, fileName.lastIndexOf("."));
		String path = uploadController.buildTopicFilePath1(request)+"\\"+thefile;
		File saveFilePath = new File(path);
		if (!saveFilePath.exists()) {
			saveFilePath.mkdirs();
		}
		String pdfPath = path +"\\"+ thefile + ".pdf";
		//wordToPDF(filePath,pdfPath);
		

		ConvertResult cr = new ConvertResult(filePath, pdfPath,"0");
		/*ExecutorService executorService = Executors.newCachedThreadPool();//创建一个线程池
		executorService.submit(cr);*/
		FutureTask<String> result = new FutureTask<>(cr);//执行Callable方式，需要FutureTask实现类的支持，用于接收运算结果
		new Thread(result).start();
		String res = "";
		try {
			res = result.get();//获取call返回的结果
			if (res.equals("true")) {
				requestResult.setCode(RequestResult.SUCCESS);
				requestResult.setData("/uploadfiles/"+ thefile + "/" + thefile + ".pdf");
			}
		} catch (Exception e) {
		} 
		return requestResult;
	}
	
	@ResponseBody
	@RequestMapping("/toPNG")
	public RequestResult toPNG(HttpServletRequest request,String filePath,String fileName) {
		filePngSize = 0;
		RequestResult requestResult = new RequestResult(RequestResult.FAILURE);
		try {
			filePath = filePath==null?"":URLDecoder.decode(filePath, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO 自动生成的 catch 块
			//e.printStackTrace();;
		}
		String thefile = fileName==null?"test":fileName.substring(0, fileName.lastIndexOf("."));
		String path = uploadController.buildTopicFilePath1(request)+"\\"+thefile;
		File saveFilePath = new File(path);
		if (!saveFilePath.exists()) {
			saveFilePath.mkdirs();
		}
		//pdfToPNG(filePath,path);

		ConvertResult cr = new ConvertResult(filePath, path,"1");
		FutureTask<String> result2 = new FutureTask<>(cr);//执行Callable方式，需要FutureTask实现类的支持，用于接收运算结果
		new Thread(result2).start();
		String res = "";
		try {
			res = result2.get();//获取call返回的结果
			if (res.equals("true")) {
				requestResult.setCode(RequestResult.SUCCESS);
				JSONObject fileObject = new JSONObject();
				fileObject.put("pngSize", filePngSize);
				fileObject.put("savePath", "/uploadfiles/"+thefile+"/");
				requestResult.setData(fileObject);
			}
		} catch (Exception e) {
		} 
		return requestResult;
	}
	
	/**
	 * 使用Jacob转换
	 * @param wordPath
	 * @param pdfPath
	 */
	public static String wordToPDF(String wordPath,String pdfPath) {
		String convertResult = "false";
		wordLock.lock();
		try {
			ActiveXComponent axComponent = null;//声明ActiveX组件
			Dispatch dispatch = null;//调度处理类，进行操作office
			try {
				ComThread.InitSTA();//初始化com线程,初始化一个线程并放入内存中等待调用
				axComponent = new ActiveXComponent("Word.Application");//打开word应用程序
				axComponent.setProperty("Visible", false); //设置word不可见
				Dispatch doc = axComponent.getProperty("Documents").toDispatch();//获得word中所有打开的文档,返回Documents对象
				//调用Documents对象中Open方法打开文档，并返回打开的文档对象Document
				dispatch = Dispatch.call(doc, "Open", wordPath, false, true).toDispatch();
				Dispatch.call(dispatch, "ExportAsFixedFormat", pdfPath, wdFormatPDF);//word保存为pdf格式宏，值为17
				
			} catch (Exception e) {
				System.out.println(e.toString());
			} finally {
				if (dispatch != null) {
					Dispatch.call(dispatch, "Close", false); //关闭文档
				}
				if (axComponent != null) {
					axComponent.invoke("Quit", 0); //关闭word应用程序
				}
				ComThread.Release();//释放占用的内存空间，因为com的线程回收不由java的垃圾回收器处理 
				convertResult = "true";
			} 
			/*Thread thread = new Thread(new Runnable() {
				public void run() {
					
				}
			});
			thread.start();*/
			
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			wordLock.unlock();
		}
		return convertResult;
	}
	
	/**
	 * 使用icepdf转换
	 * @param pdfPath
	 * @param pngPath
	 */
	public String pdfToPNG(String pdfPath,String pngPath) {
		String convertResult = "false";
		pdfLock.lock();
		try {
			File pngFile = new File(pngPath);
			if (!pngFile.exists()) {
				pngFile.mkdirs();
			}
			Document document = new Document();
			document.setFile(pdfPath);
			float scale = 2.5f;//缩放比例
			float rotation = 0f;//旋转角度
			filePngSize = document.getNumberOfPages();
			for (int i=0;i<document.getNumberOfPages();i++){
				BufferedImage bImage = (BufferedImage) document.getPageImage(i, GraphicsRenderingHints.SCREEN,Page.BOUNDARY_CROPBOX, rotation, scale);
				RenderedImage rImage = bImage;
				try {
					File file = new File(pngPath+"\\"+i+1+".png");
					ImageIO.write(rImage, "png", file);
				} catch (IOException e) {
					// TODO 自动生成的 catch 块
					//e.printStackTrace();;
				}
				bImage.flush();
				document.dispose();
			}
			convertResult = "true";
		}catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			pdfLock.unlock();
		}
		return convertResult;
	}
}

class ConvertResult implements Callable<String> {
	private String sourcePath;
	private String savePath;
	private String converType;//0-word转pdf 1-pdf转png
	
	public ConvertResult(String sourcePath,String savePath,String converType) {
		this.sourcePath=sourcePath;
		this.savePath=savePath;
		this.converType=converType;
	}
	
	@Override
	public String call() throws Exception {
		String convertResult = "false";
		fileConvertController fileConvert = new fileConvertController();
 		if (converType.equals("0")) {
 			convertResult = fileConvertController.wordToPDF(sourcePath,savePath);
		}else if (converType.equals("1")) {
			convertResult = fileConvert.pdfToPNG(sourcePath,savePath);
		}
		return convertResult;
	}
}
