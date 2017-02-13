/**
 * 
 */
package com.bailiangroup.osp.modules.sys.web;

import static java.lang.String.format;
import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static java.net.InetAddress.getLocalHost;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import lombok.Data;
import lombok.experimental.Builder;

/**
 * @author Administrator
 *
 */
/*@RestController*/
@Controller
@RequestMapping(value = "/static")
public class HeartBeatController {

	private final String service;
    
	@Inject
    public HeartBeatController(@Value("${app.code}") final String service) {
        this.service = service;
    }
	
    @SuppressWarnings("unchecked")
	/*@RequestMapping(value = "/heartbeat", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)*/
    @RequestMapping(value = "/heartBeat", method = {RequestMethod.GET,RequestMethod.POST})
    public ResponseEntity<?> beatJson() throws UnknownHostException {
        return new ResponseEntity(new HeartBeat(service), HttpStatus.OK);
    }
    
    @Builder
    @Data
    public static class HeartBeat implements Serializable {

    	private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:sss";  
    	private static Calendar cale = Calendar.getInstance(); 
    	private static SimpleDateFormat sdf_datetime_format = new SimpleDateFormat(DATETIME_FORMAT);  
    	
		private static final long serialVersionUID = 1L;
		
		private static final String systemStartTime = sdf_datetime_format.format(cale.getTime());

	    // Field order determines JSON order
	    private String service;
	    private String timestamp;
	    private String hostname = "unknow";
	    private int port;

	    /** Constructs a new {@code HeartBeat} for JSON binding. */
	    public HeartBeat() {}


	    /** Constructs a new {@code HeartBeat} for {@link HeartBeatController}. */
	    public HeartBeat(final String service){
	        this.service = service;
	        timestamp = systemStartTime;
	        
	        try {
				hostname = getLocalHost().getCanonicalHostName();
			} catch (UnknownHostException e) {
			}
	    }
		
		public HeartBeat(final String service, final String hostname, final String timestamp, final int port){ }

	    @Override
	    public String toString() {
	        return format("%s://%s:%d/%s@%d", service, hostname, port, timestamp,
	                beats());
	    }

	    /**
	     * @see <a href="http://en.wikipedia.org/wiki/Swatch_Internet_Time">Swatch
	     * Internet Time</a>
	     */
	    private int beats() {
	        return (int) (getRuntimeMXBean().getUptime() / 86400);
	    }

		public String getService() {
			return service;
		}

		public void setService(String service) {
			this.service = service;
		}

		public String getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(String timestamp) {
			this.timestamp = timestamp;
		}

		public String getHostname() {
			return hostname;
		}

		public void setHostname(String hostname) {
			this.hostname = hostname;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}
	    
    }
}
