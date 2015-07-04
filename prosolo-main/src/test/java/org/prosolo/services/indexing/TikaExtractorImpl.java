package org.prosolo.services.indexing;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.prosolo.services.indexing.impl.ExtractedTikaDocument;


public class TikaExtractorImpl {
	private static Logger logger = Logger.getLogger(TikaExtractorImpl.class);
	public ExtractedTikaDocument parseInputStream(InputStream inputStream, TikaConfig tikaConfig, Metadata metadata) {
        SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        TransformerHandler handler=null;
        StringWriter sw =new StringWriter();
		try {
			handler = factory.newTransformerHandler();
			 handler.getTransformer().setOutputProperty(OutputKeys.METHOD, "xml");
		        handler.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
		        handler.setResult( new StreamResult(sw));
		} catch (TransformerConfigurationException e1) {
			e1.printStackTrace();
		}
       

        Parser parser = new AutoDetectParser(tikaConfig);
        ParseContext pc = new ParseContext();
        try {
            parser.parse(inputStream, handler, metadata, pc);
           String title= metadata.get(TikaCoreProperties.TITLE);
           String contentType=metadata.get(Metadata.CONTENT_TYPE);
           String content=sw.toString();
           System.out.println("parsed:"+title);
           ExtractedTikaDocument doc=new ExtractedTikaDocument(title,content, contentType);
            return doc;
        } catch (Exception e) {
            logger.error("Failed to parse file ${file.absolutePath}", e);
            return null;
        }
    }
public ExtractedTikaDocument parseInputStream(InputStream inputStream){
    TikaConfig tikaConfig=null;
    ExtractedTikaDocument doc=null;
 	try {
		tikaConfig = new TikaConfig();
	} catch (TikaException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    Metadata tikaMeta = new Metadata();
    try {
		doc= parseInputStream(inputStream, tikaConfig, tikaMeta);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
   // System.out.println("TIKA OUTPUT:"+output);
    return doc;
}
}
