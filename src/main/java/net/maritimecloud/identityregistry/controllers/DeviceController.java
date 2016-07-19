/* Copyright 2016 Danish Maritime Authority.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.maritimecloud.identityregistry.controllers;

import net.maritimecloud.identityregistry.services.EntityService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import net.maritimecloud.identityregistry.exception.McBasicRestException;
import net.maritimecloud.identityregistry.model.database.Certificate;
import net.maritimecloud.identityregistry.model.data.CertificateRevocation;
import net.maritimecloud.identityregistry.model.database.Organization;
import net.maritimecloud.identityregistry.model.data.PemCertificate;
import net.maritimecloud.identityregistry.model.database.entities.Device;
import net.maritimecloud.identityregistry.services.CertificateService;
import net.maritimecloud.identityregistry.services.OrganizationService;
import net.maritimecloud.identityregistry.utils.CertificateUtil;
import net.maritimecloud.identityregistry.utils.MCIdRegConstants;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class DeviceController extends BaseControllerWithCertificate {
    private EntityService<Device> deviceService;
    private OrganizationService organizationService;
    private CertificateService certificateService;

    @Autowired
    public void setCertificateService(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @Autowired
    public void setOrganizationService(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }
    @Autowired
    public void setDeviceService(EntityService<Device> deviceService) {
        this.deviceService = deviceService;
    }

    @Autowired
    private CertificateUtil certUtil;

    /**
     * Creates a new Device
     * 
     * @return a reply...
     * @throws McBasicRestException 
     */ 
    @RequestMapping(
            value = "/api/org/{orgShortName}/device",
            method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    @PreAuthorize("hasRole('ORG_ADMIN') and @accessControlUtil.hasAccessToOrg(#orgShortName)")
    public ResponseEntity<Device> createDevice(HttpServletRequest request, @PathVariable String orgShortName, @RequestBody Device input) throws McBasicRestException {
        Organization org = this.organizationService.getOrganizationByShortName(orgShortName);
        if (org != null) {
            input.setIdOrganization(org.getId());
            Device newDevice = this.deviceService.save(input);
            return new ResponseEntity<Device>(newDevice, HttpStatus.OK);
        } else {
            throw new McBasicRestException(HttpStatus.NOT_FOUND, MCIdRegConstants.ORG_NOT_FOUND, request.getServletPath());
        }
    }

    /**
     * Returns info about the device identified by the given ID
     * 
     * @return a reply...
     * @throws McBasicRestException 
     */
    @RequestMapping(
            value = "/api/org/{orgShortName}/device/{deviceId}",
            method = RequestMethod.GET,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    @PreAuthorize("@accessControlUtil.hasAccessToOrg(#orgShortName)")
    public ResponseEntity<Device> getDevice(HttpServletRequest request, @PathVariable String orgShortName, @PathVariable Long deviceId) throws McBasicRestException {
        Organization org = this.organizationService.getOrganizationByShortName(orgShortName);
        if (org != null) {
            Device device = this.deviceService.getById(deviceId);
            if (device == null) {
                throw new McBasicRestException(HttpStatus.NOT_FOUND, MCIdRegConstants.DEVICE_NOT_FOUND, request.getServletPath());
            }
            if (device.getIdOrganization().compareTo(org.getId()) == 0) {
                return new ResponseEntity<Device>(device, HttpStatus.OK);
            }
            throw new McBasicRestException(HttpStatus.FORBIDDEN, MCIdRegConstants.MISSING_RIGHTS, request.getServletPath());
        } else {
            throw new McBasicRestException(HttpStatus.NOT_FOUND, MCIdRegConstants.ORG_NOT_FOUND, request.getServletPath());
        }
    }

    /**
     * Updates a Device
     * 
     * @return a reply...
     * @throws McBasicRestException 
     */
    @RequestMapping(
            value = "/api/org/{orgShortName}/device/{deviceId}",
            method = RequestMethod.PUT)
    @ResponseBody
    @PreAuthorize("hasRole('ORG_ADMIN') and @accessControlUtil.hasAccessToOrg(#orgShortName)")
    public ResponseEntity<?> updateDevice(HttpServletRequest request, @PathVariable String orgShortName, @PathVariable Long deviceId, @RequestBody Device input) throws McBasicRestException {
        Organization org = this.organizationService.getOrganizationByShortName(orgShortName);
        if (org != null) {
            Device device = this.deviceService.getById(deviceId);
            if (device == null) {
                throw new McBasicRestException(HttpStatus.NOT_FOUND, MCIdRegConstants.VESSEL_NOT_FOUND, request.getServletPath());
            }
            if (device.getId().compareTo(input.getId()) == 0 && device.getIdOrganization().compareTo(org.getId()) == 0) {
                input.selectiveCopyTo(device);
                this.deviceService.save(device);
                return new ResponseEntity<>(HttpStatus.OK);
            }
            throw new McBasicRestException(HttpStatus.FORBIDDEN, MCIdRegConstants.MISSING_RIGHTS, request.getServletPath());
        } else {
            throw new McBasicRestException(HttpStatus.NOT_FOUND, MCIdRegConstants.ORG_NOT_FOUND, request.getServletPath());
        }
    }

    /**
     * Deletes a Device
     * 
     * @return a reply...
     * @throws McBasicRestException 
     */
    @RequestMapping(
            value = "/api/org/{orgShortName}/device/{deviceId}",
            method = RequestMethod.DELETE)
    @ResponseBody
    @PreAuthorize("hasRole('ORG_ADMIN') and @accessControlUtil.hasAccessToOrg(#orgShortName)")
    public ResponseEntity<?> deleteDevice(HttpServletRequest request, @PathVariable String orgShortName, @PathVariable Long deviceId) throws McBasicRestException {
        Organization org = this.organizationService.getOrganizationByShortName(orgShortName);
        if (org != null) {
            Device device = this.deviceService.getById(deviceId);
            if (device == null) {
                throw new McBasicRestException(HttpStatus.NOT_FOUND, MCIdRegConstants.VESSEL_NOT_FOUND, request.getServletPath());
            }
            if (device.getIdOrganization().compareTo(org.getId()) == 0) {
                this.deviceService.delete(deviceId);
                return new ResponseEntity<>(HttpStatus.OK);
            }
            throw new McBasicRestException(HttpStatus.FORBIDDEN, MCIdRegConstants.MISSING_RIGHTS, request.getServletPath());
        } else {
            throw new McBasicRestException(HttpStatus.NOT_FOUND, MCIdRegConstants.ORG_NOT_FOUND, request.getServletPath());
        }
    }

    /**
     * Returns a list of devices owned by the organization identified by the given ID
     * 
     * @return a reply...
     * @throws McBasicRestException 
     */
    @RequestMapping(
            value = "/api/org/{orgShortName}/devices",
            method = RequestMethod.GET,
            produces = "application/json;charset=UTF-8")
    @PreAuthorize("@accessControlUtil.hasAccessToOrg(#orgShortName)")
    public ResponseEntity<List<Device>> getOrganizationDevices(HttpServletRequest request, @PathVariable String orgShortName) throws McBasicRestException {
        Organization org = this.organizationService.getOrganizationByShortName(orgShortName);
        if (org != null) {
            List<Device> devices = this.deviceService.listFromOrg(org.getId());
            return new ResponseEntity<List<Device>>(devices, HttpStatus.OK);
        } else {
            throw new McBasicRestException(HttpStatus.NOT_FOUND, MCIdRegConstants.ORG_NOT_FOUND, request.getServletPath());
        }
    }

    /**
     * Returns new certificate for the device identified by the given ID
     * 
     * @return a reply...
     * @throws McBasicRestException 
     */
    @RequestMapping(
            value = "/api/org/{orgShortName}/device/{deviceId}/generatecertificate",
            method = RequestMethod.GET,
            produces = "application/json;charset=UTF-8")
    @PreAuthorize("hasRole('ORG_ADMIN') and @accessControlUtil.hasAccessToOrg(#orgShortName)")
    public ResponseEntity<PemCertificate> newDeviceCert(HttpServletRequest request, @PathVariable String orgShortName, @PathVariable Long deviceId) throws McBasicRestException {
        Organization org = this.organizationService.getOrganizationByShortName(orgShortName);
        if (org != null) {
            Device device = this.deviceService.getById(deviceId);
            if (device == null) {
                throw new McBasicRestException(HttpStatus.NOT_FOUND, MCIdRegConstants.DEVICE_NOT_FOUND, request.getServletPath());
            }
            if (device.getIdOrganization().compareTo(org.getId()) == 0) {
                PemCertificate ret = this.issueCertificate(device, org, "device");
                return new ResponseEntity<PemCertificate>(ret, HttpStatus.OK);
            }
            throw new McBasicRestException(HttpStatus.FORBIDDEN, MCIdRegConstants.MISSING_RIGHTS, request.getServletPath());
        } else {
            throw new McBasicRestException(HttpStatus.NOT_FOUND, MCIdRegConstants.ORG_NOT_FOUND, request.getServletPath());
        }
    }

    /**
     * Revokes certificate for the device identified by the given ID
     * 
     * @return a reply...
     * @throws McBasicRestException 
     */
    @RequestMapping(
            value = "/api/org/{orgShortName}/device/{deviceId}/certificates/{certId}/revoke",
            method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    @PreAuthorize("hasRole('ORG_ADMIN') and @accessControlUtil.hasAccessToOrg(#orgShortName)")
    public ResponseEntity<?> revokeDeviceCert(HttpServletRequest request, @PathVariable String orgShortName, @PathVariable Long deviceId, @PathVariable Long certId,  @RequestBody CertificateRevocation input) throws McBasicRestException {
        Organization org = this.organizationService.getOrganizationByShortName(orgShortName);
        if (org != null) {
            Device device = this.deviceService.getById(deviceId);
            if (device == null) {
                throw new McBasicRestException(HttpStatus.NOT_FOUND, MCIdRegConstants.DEVICE_NOT_FOUND, request.getServletPath());
            }
            if (device.getIdOrganization().compareTo(org.getId()) == 0) {
                Certificate cert = this.certificateService.getCertificateById(certId);
                Device certDevice = cert.getDevice();
                if (certDevice != null && certDevice.getId().compareTo(device.getId()) == 0) {
                    if (!input.validateReason()) {
                        throw new McBasicRestException(HttpStatus.BAD_REQUEST, MCIdRegConstants.INVALID_REVOCATION_REASON, request.getServletPath());
                    }
                    if (input.getRevokedAt() == null) {
                        throw new McBasicRestException(HttpStatus.BAD_REQUEST, MCIdRegConstants.INVALID_REVOCATION_DATE, request.getServletPath());
                    }
                    cert.setRevokedAt(input.getRevokedAt());
                    cert.setRevokeReason(input.getRevokationReason());
                    cert.setRevoked(true);
                    this.certificateService.saveCertificate(cert);
                    return new ResponseEntity<>(HttpStatus.OK);
                }
            }
            throw new McBasicRestException(HttpStatus.FORBIDDEN, MCIdRegConstants.MISSING_RIGHTS, request.getServletPath());
        } else {
            throw new McBasicRestException(HttpStatus.NOT_FOUND, MCIdRegConstants.ORG_NOT_FOUND, request.getServletPath());
        }
    }


}

