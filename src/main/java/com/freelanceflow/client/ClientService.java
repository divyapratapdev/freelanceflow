package com.freelanceflow.client;

import com.freelanceflow.client.dto.ClientRequest;
import com.freelanceflow.client.dto.ClientResponse;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional
    public ClientResponse create(Long userId, ClientRequest request) {
        MDC.put("userId", String.valueOf(userId));
        try {
            Client client = new Client();
            client.setUserId(userId);
            client.setName(request.getName());
            client.setEmail(request.getEmail());
            client.setPhone(request.getPhone());
            client.setCompany(request.getCompany());
            client.setAddress(request.getAddress());
            return ClientResponse.from(clientRepository.save(client));
        } finally {
            MDC.clear();
        }
    }

    @Transactional(readOnly = true)
    public Page<ClientResponse> listAll(Long userId, Pageable pageable) {
        return clientRepository.findByUserId(userId, pageable).map(ClientResponse::from);
    }

    @Transactional(readOnly = true)
    public ClientResponse getById(Long userId, Long clientId) {
        Client client = findAndVerifyOwnership(userId, clientId);
        return ClientResponse.from(client);
    }

    @Transactional
    public ClientResponse update(Long userId, Long clientId, ClientRequest request) {
        MDC.put("userId", String.valueOf(userId));
        try {
            Client client = findAndVerifyOwnership(userId, clientId);
            client.setName(request.getName());
            client.setEmail(request.getEmail());
            client.setPhone(request.getPhone());
            client.setCompany(request.getCompany());
            client.setAddress(request.getAddress());
            return ClientResponse.from(clientRepository.save(client));
        } finally {
            MDC.clear();
        }
    }

    @Transactional
    public void delete(Long userId, Long clientId) {
        MDC.put("userId", String.valueOf(userId));
        try {
            Client client = findAndVerifyOwnership(userId, clientId);
            client.setDeletedAt(java.time.Instant.now());
            clientRepository.save(client);
        } finally {
            MDC.clear();
        }
    }

    private Client findAndVerifyOwnership(Long userId, Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + clientId));
        if (!client.getUserId().equals(userId)) {
            throw new AccessDeniedException("Access denied");
        }
        return client;
    }
}
