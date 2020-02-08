package org.prosolo.services.email.emailLinks.contextParser.impl;

import java.util.Optional;

import javax.inject.Inject;

import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.email.emailLinks.EmailContextToObjectMapper;
import org.prosolo.services.email.emailLinks.EnumUtil;
import org.prosolo.services.email.emailLinks.contextParser.EmailLinkContextParser;
import org.prosolo.services.email.emailLinks.data.LinkObjectContextData;
import org.prosolo.services.email.emailLinks.data.LinkObjectData;
import org.prosolo.services.email.emailLinks.data.Tuple;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.email.emailLinks.contextParser.EmailLinkContextParser")
public class EmailLinkContextParserImpl implements EmailLinkContextParser {

	private UrlIdEncoder idEncoder;

	@Inject
	public EmailLinkContextParserImpl(UrlIdEncoder idEncoder) {
		this.idEncoder = idEncoder;
	}
	
	/*
	 * return parsed learningContext as well as object info from
	 * context so that we can retrieve the actual link that user
	 * should be redirected to 
    */
	/* (non-Javadoc)
	 * @see org.prosolo.services.email.emailLinks.contextParser.EmailLinkContextParser#getLearningContext(java.lang.String)
	 */
	@Override
	public Optional<LinkObjectContextData> getLearningContext(String ctx) {
		if(ctx != null && !ctx.isEmpty()) {
			String[] parts = ctx.split("\\.");
			String returnContext="";
			Optional<LinkObjectData> objData = Optional.empty();
			int size = parts.length;
			for(int i = 0; i < size; i++) {
				String s = parts[i];
				Tuple t = getParsedTupleFromString(s);
				String currentCtx = "name:" + t.getValue() + 
						(t.getId() > 0 ? "|id:" + t.getId() : "");
				if(i == 0) {
					returnContext = currentCtx;
				} else {
					returnContext = ContextJsonParserService.addSubContext(returnContext, currentCtx);
				}
				if(i == size - 1) {
					objData = getObjectDataForTuple(t);
				}
				
			}
			if(objData.isPresent()) {
				return Optional.of(new LinkObjectContextData(returnContext, objData.get()));
			}
		}
		
		return Optional.empty();
	}

	//return object with info needed to retrieve actual link user should
	//be redirected to
	private Optional<LinkObjectData> getObjectDataForTuple(Tuple t) {
		if(t.getId() != 0) {
			Optional<EmailContextToObjectMapper> optContextMapper = EnumUtil.getEnumFromString(
					EmailContextToObjectMapper.class, t.getValue().toUpperCase());
			if(optContextMapper.isPresent()) {
				EmailContextToObjectMapper mapper = optContextMapper.get();
				return Optional.of(new LinkObjectData(t.getId(), mapper.getObjectInfo()));
			}
		}
		
		return Optional.empty();
		
	}

	private Tuple getParsedTupleFromString(String s) {
		int ind = s.indexOf(":");
		String cName = null;
		long id = 0;
		if(ind != -1) {
			cName = s.substring(0, ind);
			String encodedId = s.substring(ind + 1);
			id = idEncoder.decodeId(encodedId);
		} else {
			cName = s;
		}
		return new Tuple(id, cName);
	}
	
}
