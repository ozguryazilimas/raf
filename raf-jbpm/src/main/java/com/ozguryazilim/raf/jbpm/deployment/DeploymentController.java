/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jbpm.deployment;

import com.ozguryazilim.raf.auth.KJarResourceHandler;
import com.ozguryazilim.raf.auth.RafAsset;
import com.ozguryazilim.telve.audit.AuditLogCommand;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messagebus.command.CommandSender;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.model.DeployedAsset;
import org.jbpm.services.api.model.DeployedUnit;
import org.jbpm.services.api.model.DeploymentUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KJar Deployment işlemlerini yürütmek için arayüz.
 *
 * 
 * FIXME: Aslın undeploy değil inactive implementasyonu yapılmalı. Tarihçede bunlunan form v.s. silinemez!
 * 
 * @author Hakan Uygun
 */
@WindowScoped
@Named
public class DeploymentController implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(DeploymentController.class);

    @Inject
    private DeploymentService deploymentService;

    @Inject
    private CommandSender commandSender;

    @Inject
    private Identity identity;

    @Inject
    private Instance<KJarResourceHandler> resourceHandlers;

    private List<RafAsset> deployedAssets;

    private List<DeployedUnit> units;
    private DeployedUnit selectedUnit;
    private String kjarId;

    public List<DeployedUnit> getDeployedUnits() {

        if (units == null) {
            units = new ArrayList<>(deploymentService.getDeployedUnits());
        }

        return units;
    }

    public void selectUnit(DeployedUnit unit) {
        clear();
        selectedUnit = unit;
    }

    public DeployedUnit getSelectedUnit() {
        return selectedUnit;
    }

    public void setSelectedUnit(DeployedUnit selectedUnit) {
        clear();
        this.selectedUnit = selectedUnit;
    }

    public String getKjarId() {
        return kjarId;
    }

    public void setKjarId(String kjarId) {
        this.kjarId = kjarId;
    }

    public void undeploy() {
        if (selectedUnit == null) {
            return;
        }

        try {
            String id = selectedUnit.getDeploymentUnit().getIdentifier();
            deploymentService.undeploy(selectedUnit.getDeploymentUnit());
            clear();

            //Şimdi de raf kaynaklarından undeploy yapalım. Bu noktaya gelmiş ise kullanan süreç felan kalmamıştır.
            resourceHandlers.forEach(h -> {
                h.undeploy(id);
            });

            AuditLogCommand command = new AuditLogCommand("KJAR_UNDEPLOY", identity.getLoginName(), id);
            command.setCategory("KJAR");
            command.setDomain(id);

            commandSender.sendCommand(command);

        } catch (IllegalStateException ex) {
            LOG.warn("Undeployment error", ex);
            FacesMessages.warn("Aktif süreçler var. Kaldırılamaz!");
        }
    }

    public void deactivate() {
        deploymentService.deactivate(selectedUnit.getDeploymentUnit().getIdentifier());
    }
    
    public void activate() {
        deploymentService.activate(selectedUnit.getDeploymentUnit().getIdentifier());
    }
    
    public void deploy() {
        String[] gav = kjarId.split(":");
        if (gav.length < 3) {
            //FIXME: i18n
            FacesMessages.warn("Paket ismi hatalı. Yükleme yapılamadı");
            return;
        }
        try {
            DeploymentUnit deploymentUnit = new KModuleDeploymentUnit(gav[0], gav[1], gav[2]);
            deploymentService.deploy(deploymentUnit);
            clear();

            AuditLogCommand command = new AuditLogCommand("KJAR_DEPLOY", identity.getLoginName(), kjarId);
            command.setCategory("KJAR");
            command.setDomain(kjarId);

            commandSender.sendCommand(command);

            kjarId = "";

        } catch (IllegalStateException ex) {
            LOG.warn("Deployment error", ex);
            FacesMessages.warn("Paket zaten yüklenmiş durumda.");
        }
    }

    public List<RafAsset> getDeployedAssets() {

        if (deployedAssets == null) {
            populateAssets();
        }

        return deployedAssets;
    }

    private void populateAssets() {
        if (selectedUnit == null) {
            return;
        }

        deployedAssets = new ArrayList<>();

        //Önce jBPM'den alalım
        for (DeployedAsset da : selectedUnit.getDeployedAssets()) {
            deployedAssets.add(new RafAsset(da.getId(), da.getName(), da.getKnowledgeType()));
        }

        //Şimdi de raf kaynaklarından
        resourceHandlers.forEach(h -> {
            List<RafAsset> l = h.getAssests(selectedUnit.getDeploymentUnit().getIdentifier());
            if (!l.isEmpty()) {
                deployedAssets.addAll(l);
            }
        });

    }

    /**
     * Yerel cache temizliği
     */
    private void clear() {
        units = null;
        selectedUnit = null;
        deployedAssets = null;
    }
}
