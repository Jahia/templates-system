import org.jahia.services.content.JCRCallback
import org.jahia.services.content.JCRNodeWrapper
import org.jahia.services.content.JCRSessionWrapper
import org.jahia.services.content.JCRTemplate

import javax.jcr.RepositoryException
import javax.jcr.Value

log.info("Check if jcr:title property is correctly set using i18n translation node.")
// Only execute in default, jnt:category is auto published
JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, "default", Locale.ENGLISH, new JCRCallback<Object>() {
    @Override
    Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
        if (session.nodeExists("/sites/systemsite/categories")) {
            JCRNodeWrapper rootCategory = session.getNode("/sites/systemsite/categories")
            // This property should only exists under a child translation node, not directly on the node it self.
            if (rootCategory.getRealNode().hasProperty("jcr:title")) {
                rootCategory.getRealNode().setProperty("jcr:title", (Value) null)
                session.save()
                log.info("jcr:title property removed from the root category node it was not correctly using i18n child translation node. " +
                        "(the translation node will be automatically re-created by import during module startup.)")
            }
        }
        return null
    }
})