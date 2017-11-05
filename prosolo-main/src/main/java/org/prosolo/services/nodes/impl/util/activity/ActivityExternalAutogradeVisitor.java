package org.prosolo.services.nodes.impl.util.activity;

import org.prosolo.common.domainmodel.credential.ExternalToolActivity1;
import org.prosolo.common.domainmodel.credential.TextActivity1;
import org.prosolo.common.domainmodel.credential.UrlActivity1;
import org.prosolo.common.domainmodel.credential.visitor.ActivityVisitor;

/**
 * @author stefanvuckovic
 * @date 2017-10-10
 * @since 1.0.0
 */
public class ActivityExternalAutogradeVisitor implements ActivityVisitor {

    private boolean autogradeByExternalGrade;

    @Override
    public void visit(TextActivity1 activity) {
        autogradeByExternalGrade = false;
    }

    @Override
    public void visit(UrlActivity1 activity) {
        autogradeByExternalGrade = false;
    }

    @Override
    public void visit(ExternalToolActivity1 activity) {
        autogradeByExternalGrade = activity.isAcceptGrades();
    }

    public boolean isAutogradeByExternalGrade() {
        return autogradeByExternalGrade;
    }
}
