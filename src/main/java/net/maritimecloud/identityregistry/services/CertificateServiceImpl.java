/*
 * Copyright 2017 Danish Maritime Authority.
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
package net.maritimecloud.identityregistry.services;

import net.maritimecloud.identityregistry.model.database.Certificate;
import net.maritimecloud.identityregistry.model.database.entities.Device;
import net.maritimecloud.identityregistry.model.database.entities.User;
import net.maritimecloud.identityregistry.model.database.entities.Vessel;
import net.maritimecloud.identityregistry.repositories.CertificateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Service
public class CertificateServiceImpl implements CertificateService {
    private CertificateRepository CertificateRepository;

    @Autowired
    public void setCertificateRepository(CertificateRepository CertificateRepository) {
        this.CertificateRepository = CertificateRepository;
    }

    @Override
    public Certificate getCertificateBySerialNumber(BigInteger serialNumber) {
        return CertificateRepository.getBySerialNumber(serialNumber);
    }

    @Override
    public Certificate saveCertificate(Certificate certificate) {
        return CertificateRepository.save(certificate);
    }

    @Override
    public void deleteCertificate(Long id) {
        throw new UnsupportedOperationException("Deletion of certificates is not supported, please revoke them");
    }

    @Override
    public List<Certificate> listVesselCertificate(Vessel vessel) {
        return CertificateRepository.findByvessel(vessel);
    }
    
    @Override
    public List<Certificate> listUserCertificate(User user) {
        return CertificateRepository.findByuser(user);
    }
    
    @Override
    public List<Certificate> listDeviceCertificate(Device device) {
        return CertificateRepository.findBydevice(device);
    }

    @Override
    public List<Certificate> listRevokedCertificate(String caAlias) {
        return CertificateRepository.findByCertificateAuthorityIgnoreCaseAndRevokedTrue(caAlias);
    }

}

