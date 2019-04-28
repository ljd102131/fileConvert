package lee.files.entity;

/**
 * 请求结果返回体
 * @author lijd
 *
 */
public class RequestResult {
	/**
	 * 成功
	 */
	public final static int SUCCESS = 0;
	/**
	 * 失败
	 */
	public final static int FAILURE = 1;
	/**
	 * 返回码
	 */
	private int code;
	/**
	 * 返回消息
	 */
	private String msg;
	/**
	 * 返回数据
	 */
	private Object data;
	
	/**
	 * 构造方法1
	 * @param code
	 */
	public RequestResult(int code) {
		this.code = code;
	}
	/**
	 * 构造方法2
	 * @param code
	 * @param msg
	 */
	public RequestResult(int code,String msg) {
		this.code = code;
		this.msg = msg;
	}
	/**
	 * 构造方法3
	 * @param code
	 * @param msg
	 * @param data
	 */
	public RequestResult(int code,String msg,Object data) {
		this.code = code;
		this.msg = msg;
		this.data = data;
	}
	
	
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
