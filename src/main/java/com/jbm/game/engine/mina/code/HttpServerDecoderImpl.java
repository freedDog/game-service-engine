package com.jbm.game.engine.mina.code;

import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.http.ArrayUtil;
import org.apache.mina.http.HttpRequestImpl;
import org.apache.mina.http.api.HttpMethod;
import org.apache.mina.http.api.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息解码
 * @author JiangBangMing
 *
 * 2018年7月5日 下午1:22:04
 */
public class HttpServerDecoderImpl extends CumulativeProtocolDecoder{
	
	private static final Charset CHARSET=Charset.forName("UTF-8");
	
	private static final Logger logger=LoggerFactory.getLogger(HttpServerDecoderImpl.class);
	
	public static final Pattern REQUEST_LINE_PATTERN=Pattern.compile(" ");
	
	public static final Pattern QUERY_STRING_PATTERN=Pattern.compile("\\?");
	
	public static final Pattern PARAM_STRING_PATTERN=Pattern.compile("\\&|;");
	
	public static final Pattern KEY_VALUE_PATTERN=Pattern.compile("=");
	
	public static final Pattern RAW_VALUE_PATTERN=Pattern.compile("\\r\\n\\r\\n");
	
	public static final Pattern HEADERS_BODY_PATTERN=Pattern.compile("\\r\\n");
	
	public static final Pattern HEADER_VALUE_PATTERN=Pattern.compile(":");
	
	public static final Pattern COOKIE_SEPARATOR_PATTERN=Pattern.compile(";");
	
	public static final String HTTP_REQUEST="http.request";

	@Override
	protected boolean doDecode(IoSession session, IoBuffer msg, ProtocolDecoderOutput out) throws Exception {
		
		/**
		 * 消息已经解析
		 * 谷歌浏览器一次请求存在多次收到请求，还额外请求了 /favicon.ico路径
		 */
		if(session.containsAttribute(HTTP_REQUEST)) {
			return false;
		}
		msg.mark();
		HttpRequestImpl rq=parseHttpRequestHead(msg.buf(), msg);
		if(rq!=null) {
			out.write(rq);
			session.setAttribute(HTTP_REQUEST,rq);
			return true;
		}
		msg.reset();
		return false;
	}
	@Override
	public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
		super.finishDecode(session, out);
	}
	
	private HttpRequestImpl parseHttpRequestHead(final ByteBuffer buffer,IoBuffer msg) throws Exception{
		final String raw=new String(buffer.array(),0,buffer.limit());
		
		final String[] headersAndBody=RAW_VALUE_PATTERN.split(raw, -1);
		
		if(headersAndBody.length<=1) {
			return null;
		}
		
		String[] headerFields=HEADERS_BODY_PATTERN.split(headersAndBody[0]);
		headerFields=ArrayUtil.dropFromEndWhile(headerFields, "");
		
		final String requestLine=headerFields[0];
		final Map<String,String> generalHeaders=new HashMap<String, String>();
		
		for(int i=0;i<headerFields.length;i++) {
			final String[] header=HEADER_VALUE_PATTERN.split(headerFields[i]);
			generalHeaders.put(header[0].toLowerCase(),header[1].trim());
		}
		
		final String[] elements=REQUEST_LINE_PATTERN.split(requestLine);
		final HttpMethod method=HttpMethod.valueOf(elements[0]);
		final String[] pathFrags=QUERY_STRING_PATTERN.split(elements[1]);
		final HttpVersion version=HttpVersion.fromString(elements[2]);
		final String requestedPath=pathFrags[0];
		String queryString=pathFrags.length>=2?pathFrags[1]:"";
		queryString=URLDecoder.decode(queryString,"UTF-8");
		
		buffer.position(headersAndBody[0].length()+4);
		
		//POST 请求
		String contentLen=generalHeaders.get("content-length");
		//post 数据
		if(contentLen!=null&&method==HttpMethod.POST) {
			if(logger.isDebugEnabled()) {
				logger.debug("found content len : {}", contentLen);
				logger.debug("decoding BODY: {} bytes", msg.remaining());
			}
			int contentLength=Integer.valueOf(contentLen);
			if(contentLength<=msg.remaining()) {
				byte[] content=new byte[contentLength];
				msg.get(content);
				String str=new String(content, CHARSET);
				queryString=URLDecoder.decode(str,"UTF-8");
			}
		}
		return new HttpRequestImpl(version, method, requestedPath, queryString, generalHeaders);
	}

}
