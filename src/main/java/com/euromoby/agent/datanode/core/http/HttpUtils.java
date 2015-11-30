package com.euromoby.agent.datanode.core.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.euromoby.agent.utils.Strings;


public class HttpUtils {
    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 30 * 24 * 60 * 60; // 30 days	
    
	public static final Pattern WILDCARD_REGEX = Pattern.compile("[^*]+|(\\*)");	
    
    
	public static boolean ifModifiedSince(HttpRequest request, File file) {

        String ifModifiedSince = request.headers().get(HttpHeaders.Names.IF_MODIFIED_SINCE);
        if (!Strings.nullOrEmpty(ifModifiedSince)) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HttpUtils.HTTP_DATE_FORMAT, Locale.US);
            Date ifModifiedSinceDate;
			try {
				ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);
			} catch (ParseException e) {
				return true;
			}

            // Only compare up to the second
            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
            long fileLastModifiedSeconds = file.lastModified() / 1000;
            if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
            	return false;
            }
        }		
		return true;
	}    

    public static void setDateHeader(FullHttpResponse response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HttpUtils.HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HttpUtils.HTTP_DATE_GMT_TIMEZONE));
        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));
    }	

	public static ByteBuf fromString(String s) {
		return Unpooled.copiedBuffer(s, CharsetUtil.UTF_8);
	}    
	
	public static Map<String, List<String>> getUriAttributes(FullHttpRequest request) {
		QueryStringDecoder decoderQuery = new QueryStringDecoder(request.getUri());
		return decoderQuery.parameters();
	}	

	public static boolean bypassProxy(String[] bypassList, String host) {
		for (String mask : bypassList) {
			Matcher m = WILDCARD_REGEX.matcher(mask);
			StringBuffer b = new StringBuffer();
			while (m.find()) {
				if (m.group(1) != null) {
					m.appendReplacement(b, ".*");
				} else {
					m.appendReplacement(b, "\\\\Q" + m.group(0) + "\\\\E");
				}
			}
			m.appendTail(b);
			
			if (host.matches(b.toString())) {
				return true;
			}
		}
		return false;		
	}
	
}
