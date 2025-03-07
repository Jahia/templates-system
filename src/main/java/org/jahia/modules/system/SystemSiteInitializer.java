/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.system;

import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.api.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.StaticAssetMapping;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.api.settings.SettingsBean;
import org.osgi.service.component.annotations.*;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.util.HashMap;
import java.util.Map;

@Component(service = StaticAssetMapping.class, immediate = true)
public class SystemSiteInitializer extends StaticAssetMapping {
    private JCRTemplate jcrTemplate;
    private JahiaSitesService sitesService;
    private SettingsBean settingsBean;

    @Activate
    public void activate() throws Exception {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("/modules/templates-system/css/01web.css", "/modules/templates-system/css/templates-system.min.css");
        mappings.put("/modules/templates-system/css/navigation.css", "/modules/templates-system/css/templates-system.min.css");
        mappings.put("/modules/templates-system/css/navigationN2-1.css", "/modules/templates-system/css/templates-system.min.css");
        setMapping(mappings);

        init();
    }

    @Reference
    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    @Reference
    public void setSitesService(JahiaSitesService sitesService) {
        this.sitesService = sitesService;
    }

    @Reference
    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }

    private void init() throws Exception {
        if (settingsBean.isProcessingServer()) {
            jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Object>() {
                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:virtualsite]", Query.JCR_SQL2);
                    NodeIterator ni = q.execute().getNodes();

                    while (ni.hasNext()) {
                        JCRSiteNode node = (JCRSiteNode) ni.next();
                        if (!node.getName().equals(JahiaSitesService.SYSTEM_SITE_KEY) && node.hasProperty("j:languages")) {
                            sitesService.updateSystemSiteLanguages(node, session);
                        }
                    }
                    JCRSiteNode siteByKey = sitesService.getSiteByKey(JahiaSitesService.SYSTEM_SITE_KEY, session);
                    sitesService.updateSystemSitePermissions(siteByKey, session);
                    session.save();
                    return null;
                }
            });
        }
    }
}
