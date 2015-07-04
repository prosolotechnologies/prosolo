/**
 * 
 */
package org.prosolo.core.jsf.primefaces;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.primefaces.webapp.MultipartRequest;

/**
 * @author "Nikola Milikic"
 *
 */
public class FileUploadFilter extends org.primefaces.webapp.filter.FileUploadFilter {
	
	private final static Logger logger = Logger.getLogger(FileUploadFilter.class.getName());
	
	private String thresholdSize;
	
	private String uploadDir;

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		// this is commented out on purose
//		if(bypass) {
//            filterChain.doFilter(request, response);
//            return;
//        }
        
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		boolean isMultipart = ServletFileUpload.isMultipartContent(httpServletRequest);
		
		if(isMultipart) {
			if(logger.isLoggable(Level.FINE))
				logger.fine("Parsing file upload request");
			
			DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
			if(thresholdSize != null) {
				diskFileItemFactory.setSizeThreshold(Integer.valueOf(thresholdSize));
			}
			if(uploadDir != null) {
				diskFileItemFactory.setRepository(new File(uploadDir));
			}
				
			ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory);
			MultipartRequest multipartRequest = new MultipartRequest(httpServletRequest, servletFileUpload);
			
			if(logger.isLoggable(Level.FINE))
				logger.fine("File upload request parsed succesfully, continuing with filter chain with a wrapped multipart request");
			
			filterChain.doFilter(multipartRequest, response);
		} 
        else {
			filterChain.doFilter(request, response);
		}
	}
}
