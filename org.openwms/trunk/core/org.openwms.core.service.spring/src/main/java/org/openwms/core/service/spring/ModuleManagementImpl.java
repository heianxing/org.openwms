/*
 * openwms.org, the Open Warehouse Management System.
 *
 * This file is part of openwms.org.
 *
 * openwms.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * openwms.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software. If not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.openwms.core.service.spring;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openwms.core.domain.Module;
import org.openwms.core.integration.ModuleDao;
import org.openwms.core.service.ModuleManagementService;
import org.openwms.core.service.exception.ServiceRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A ModuleManagementImpl.
 * 
 * @author <a href="mailto:openwms@googlemail.com">Heiko Scherrer</a>
 * @version $Revision: $
 * @since 0.1
 */
@Service
@Transactional
public class ModuleManagementImpl extends EntityServiceImpl<Module, Long> implements ModuleManagementService<Module> {

    @Autowired
    @Qualifier("moduleDao")
    protected ModuleDao dao;

    /**
     * @see org.openwms.core.service.ModuleManagementService#getModules()
     */
    @Override
    @Transactional(readOnly = true)
    public List<Module> getModules() {
        return dao.findAll();
    }

    /**
     * @see org.openwms.core.service.ModuleManagementService#login()
     */
    @Override
    @Transactional(readOnly = true)
    public void login() {
        logger.debug("Login successful!");
    }

    /**
     * @see org.openwms.core.service.ModuleManagementService#saveStartupOrder(java.util.List)
     */
    @Override
    public void saveStartupOrder(List<Module> modules) {
        for (Module module : modules) {
            Module toSave = dao.findById(module.getId());
            toSave.setStartupOrder(module.getStartupOrder());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.openwms.core.service.spring.EntityServiceImpl#remove(org.openwms.core.domain.AbstractEntity)
     */
    @Override
    public void remove(Module entity) {
        if (entity.isNew()) {
            logger.info("Do not remove a transient Module");
            return;
        }
        Module rem = dao.findById(entity.getId());
        if (rem == null) {
            throw new ServiceRuntimeException("Module to be removed not found, probably it was removed before");
        }
        super.remove(rem);
    }

    /**
     * {@inheritDoc}
     * 
     * Additionally the startup order is calculated for new modules.
     * 
     * @see org.openwms.core.service.spring.EntityServiceImpl#save(org.openwms.core.domain.AbstractEntity)
     */
    @Override
    public Module save(Module entity) {
        if (entity.isNew()) {
            List<Module> all = dao.findAll();
            if (!all.isEmpty()) {
                Collections.sort(all, new Comparator<Module>() {
                    @Override
                    public int compare(Module o1, Module o2) {
                        return o1.getStartupOrder() >= o2.getStartupOrder() ? 1 : -1;
                    }
                });
                entity.setStartupOrder(all.get(all.size() - 1).getStartupOrder() + 1);
            }
        }
        return super.save(entity);
    }
}