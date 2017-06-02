package org.prosolo.common.domainmodel.credential.visitor;

import org.prosolo.common.domainmodel.credential.ExternalToolActivity1;
import org.prosolo.common.domainmodel.credential.TextActivity1;
import org.prosolo.common.domainmodel.credential.UrlActivity1;

/**
 * 
 * @author stefanvuckovic
 *
 * When new activity subclass is added, appropriate visit method should be added too.
 */
public interface ActivityVisitor {

	void visit(TextActivity1 activity);
	
	void visit(UrlActivity1 activity);
	
	void visit(ExternalToolActivity1 activity);
}
