package org.prosolo.services.email.notification;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * @author Zoran Jeremic Sep 20, 2014
 *
 */

public class HTMLDataSource implements DataSource{
	 private String html;
	 
     public HTMLDataSource(String htmlString) {
         html = htmlString;
     }

     // Return html string in an InputStream.
     // A new stream must be returned each time.
     public InputStream getInputStream() throws IOException {
         if (html == null) throw new IOException("Null HTML");
         return new ByteArrayInputStream(html.getBytes());
     }

     public OutputStream getOutputStream() throws IOException {
         throw new IOException("This DataHandler cannot write HTML");
     }

     public String getContentType() {
         return "text/html";
     }

     public String getName() {
         return "JAF text/html dataSource to send e-mail only";
     }
 
}
